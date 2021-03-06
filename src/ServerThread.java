import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import TftpPacketHelper.TftpErrorPacket;
import TftpPacketHelper.TftpAckPacket;
import TftpPacketHelper.TftpDataPacket;
import TftpPacketHelper.TftpPacket;
import TftpPacketHelper.TftpPacket.TftpType;

public class ServerThread implements Runnable {
	/**
	 * The specific socket used for interacting with client 
	 */
	private DatagramSocket socket;
	private DatagramPacket send, received;
	private File file;
	/**
	 * AKA TID of client
	 */
	private int clientPort;
	/**
	 * Either RRQ or WRQ
	 */
	private int type;
	/**
	 * Address of the client
	 */
	private InetAddress clientAddress;
	private boolean verbose;
	/**
	 * Amount of retries left before we terminate the transfer
	 */
	private int retries;
	private Server server;

	/**
	 * Server-Client interaction thread for dealing with the actual file
	 * transfer
	 * 
	 * @param clientPort
	 *            AKA TID of client
	 * @param clientAddress
	 *            Address of the client
	 * @param file
	 *            File being accessed
	 * @param type
	 *            Either RRQ or WRQ
	 * @param verbose
	 *            Verbose mode or not
	 * @param server
	 *            the server instance creating this thread
	 */
	public ServerThread(int clientPort, InetAddress clientAddress, File file, int type, boolean verbose,
			Server server) {
		this.clientPort = clientPort;
		this.clientAddress = clientAddress;
		this.file = file;
		this.type = type;
		this.verbose = verbose;
		this.server = server;
		try {
			this.socket = new DatagramSocket();
			this.socket.setSoTimeout(CommonConstants.SOCKET_TIMEOUT_MS);
		} catch (SocketException e) {
			System.out.println(e.getMessage());
		}
	}

	public void run() {
		int blockNumber = 1, byteRead = 0;
		TftpPacket recvTftpPacket;
		byte receiveBuffer[] = new byte[CommonConstants.TFTP_RECEIVE_BUFFER_SIZE];
		
		retries = CommonConstants.TFTP_MAX_NUM_RETRIES;
		received = new DatagramPacket(receiveBuffer, receiveBuffer.length);
		
		if (type == CommonConstants.RRQ) {
			byte readData[] = new byte[CommonConstants.DATA_BLOCK_SZ];
			BufferedInputStream bis;	
			//this can be either EOF or any error condition
			boolean finished = false;
					
			if(!file.isFile()) {
				ByteArrayOutputStream error = new ByteArrayOutputStream();
				error.write(0);
				error.write(5);
				error.write(0);
				error.write(1);
				try {
					error.write((file + " could not be found or is not a file.").getBytes());
				} catch (IOException e) {
					System.out.println("ISSUE MAKING ERROR TYPE 1\n" + e.getMessage());
				}
				error.write(0);
				
				byte errBuf[] = error.toByteArray();
				
				DatagramPacket send = new DatagramPacket(errBuf,
										errBuf.length,
										clientAddress,
										clientPort);

				trySend(send);

				socket.close();
				return;
			}
			if (!Files.isReadable(file.toPath())) {
				trySend(new TftpErrorPacket(TftpErrorPacket.ACCESS_VIOLATION,
						"Unable to read the file due to insufficient permission").generateDatagram(clientAddress,
								clientPort));

				socket.close();
				return;
			}
			if (!server.canThisFileBeRead(file)) {
				trySend(new TftpErrorPacket(TftpErrorPacket.ACCESS_VIOLATION, "This file is being written")
						.generateDatagram(clientAddress, clientPort));

				socket.close();
				return;
			}

			//true if the data we read from disk has been acked
			//init to true to allow first read
			boolean acked = true;
			
			try {
				bis = new BufferedInputStream(new FileInputStream(file));
				server.declareThisFileInRead(file);
			} catch (Exception e) {
				// File was checked in main loop, we could only reach here when
				// something very bad happened
				trySend(new TftpErrorPacket(TftpErrorPacket.NOT_DEFINED, e.getMessage()).generateDatagram(clientAddress,
						clientPort));
				socket.close();
				return;
			}

			System.out.println("Reading File...\n");
			do {
				if (acked) {
					// only read the next chunk if we have received an ack
					try {
						byteRead = bis.read(readData);
					} catch (IOException e) {
						// Unexpected error
						// send the error message as custom error
						trySend(new TftpErrorPacket(TftpErrorPacket.NOT_DEFINED, e.getMessage())
								.generateDatagram(clientAddress, clientPort));
						System.out.println("ERROR READING FILE\n" + e.getMessage());
						break;
					}
					acked = false;

					if (byteRead < CommonConstants.DATA_BLOCK_SZ) {
						// read returns less than 512, that's the end of file
						if (byteRead == -1) {
							// if nothing to read the FileInputStream returns
							// -1, make this to zero since we need to pass this
							// length as a parameter to helper
							byteRead = 0;
						}
						// take a note that we reached end of this file
						finished = true;
					}
					
					// pack TFTP data packet
					send = new TftpDataPacket(blockNumber, readData, byteRead).generateDatagram(clientAddress, clientPort);
					trySend(send, "ERROR SENDING DATA");
				}

				while (retries > 0) {
					try {
						if (verbose)
							System.out.println("\nReceiving...");
						socket.receive(received);
						if (verbose)
							Utils.tryPrintTftpPacket(received);
						break;
					} catch (SocketTimeoutException te) {
						if (verbose)
							System.out.println("Timed out on receiving ACK, resend DATA\n");
						trySend(send);
						retries--;
					} catch (IOException e) {
						System.out.println("HOST RECEPTION ERROR\n" + e.getMessage());
					}
				}

				if (retries == 0) {
					// We have exhausted all the retries and still
					// haven't
					// received a proper ack
					System.out.println("TIMED OUT\n");
					// and quit this file transfer...
					break;
				}

				if (!received.getAddress().equals(clientAddress) || received.getPort() != clientPort) {
					// received a packet with wrong tid, notify the sender with
					// error 5
					trySend(new TftpErrorPacket(TftpErrorPacket.UNKNOWN_TID, "Wrong Transfer ID")
							.generateDatagram(received.getAddress(), received.getPort()), "ISSUE SENDING ERROR PACKET");
					retries--;
					continue;
				}

				try {
					recvTftpPacket = TftpPacket.decodeTftpPacket(received);
				} catch (IllegalArgumentException ile) {
					// not a TFTP packet, notifier sender (which may or may
					// not be a TFTP client) and try receive again
					trySend(new TftpErrorPacket(TftpErrorPacket.ILLEGAL_OP, ile.getMessage())
							.generateDatagram(received.getAddress(), received.getPort()));
					break;
				}

				// let's check if the packet we received is an ack
				if (recvTftpPacket.getType() == TftpType.ACK) {
					TftpAckPacket ackPacket = (TftpAckPacket) recvTftpPacket;
					if (ackPacket.getBlockNumber() == blockNumber) {
						// ACK contains correct block number, increment
						// count
						blockNumber++;
						if (blockNumber > CommonConstants.TFTP_MAX_BLOCK_NUMBER) {
							// reached 65535, wrap to 0
							blockNumber = 0;
						}
						acked = true;

						// reset retry counter only when we receive a proper ACK
						retries = CommonConstants.TFTP_MAX_NUM_RETRIES;
					} else if ((blockNumber - ackPacket.getBlockNumber() >= 0 && blockNumber
							- ackPacket.getBlockNumber() <= CommonConstants.TFTP_BLOCK_MISMATCH_THRESHOLD)) {
						// do not re-send data for duplicated ACK
						retries--;
					} else {
						trySend(new TftpErrorPacket(TftpErrorPacket.ILLEGAL_OP, "Block number mismatch")
								.generateDatagram(clientAddress, clientPort));
						break;
					}
				} else if (recvTftpPacket.getType() == TftpType.ERROR) {
					TftpErrorPacket errorPacket = (TftpErrorPacket) recvTftpPacket;
					// Client give us the TID. In what case will we send
					// data to wrong port?
					// Maybe the delay caused by network interruption between
					// send/received is too long?
					if (errorPacket.getErrorCode() != TftpErrorPacket.UNKNOWN_TID)
						finished = true;
				} else {
					// not ACK, nor ERROR. this is not expected in TFTP file
					// transfer
					trySend(new TftpErrorPacket(TftpErrorPacket.ILLEGAL_OP, "Not TFTP ACK")
							.generateDatagram(clientAddress, clientPort));
					retries--;
				}
			} while (!finished);

			try {
				if (bis != null) {
					bis.close();
				}
			} catch (IOException e) {
				// we have checked the file before opening it
				// this is not expected
				System.out.println("ERROR CLOSING FILE\n" + e.getMessage());
			}
			server.declareThisFileNotInRead(file);
		} else if (type == CommonConstants.WRQ) {
			BufferedOutputStream bos;
			boolean serve = true;
			boolean deleteFile = false;

			/* Sanity check on the file to be written */
			if (file.exists()) {
				if (file.isDirectory()) {

						trySend(new TftpErrorPacket(TftpErrorPacket.NOT_DEFINED, "This is a directory")
								.generateDatagram(clientAddress, clientPort));

					socket.close();
					return;
				} else if (!Files.isWritable(file.toPath())) {
						trySend(new TftpErrorPacket(TftpErrorPacket.ACCESS_VIOLATION, "Unable to write the file due to insufficient permission")
								.generateDatagram(clientAddress, clientPort));
					socket.close();
					return;
				}
			}
			if (!server.canThisFileBeWritten(file)) {
				trySend(new TftpErrorPacket(TftpErrorPacket.ACCESS_VIOLATION, "This file is being read/written")
							.generateDatagram(clientAddress, clientPort));

				socket.close();
				return;
			}

			try {
				bos = new BufferedOutputStream(new FileOutputStream(file));
				server.declareThisFileInWrite(file);
			} catch (IOException e) {
				// we have checked the file before opening it
				// this is not expected
				trySend(new TftpErrorPacket(TftpErrorPacket.NOT_DEFINED, e.getMessage()).generateDatagram(clientAddress,
						clientPort));
				System.out.println(e.getMessage());
				
				socket.close();
				return;
			}

			// special ACK 0
			send = new TftpAckPacket(0).generateDatagram(clientAddress, clientPort);
			if (verbose) {
				System.out.println("Initial Ack sent for WRQ:");
			}
			trySend(send, "ERROR SENDING ACK 0\n");

			do {
				while (retries > 0) {
					try {
						if (verbose) {
							System.out.println("\nReceiving...");
						}
						socket.receive(received);
						if (verbose) {
							Utils.tryPrintTftpPacket(received);
						}
						break;
					} catch (SocketTimeoutException te) {
						if (verbose)
							System.out.println("Timed out on receiving DATA, resend ACK\n");
						trySend(send);
						retries--;
					} catch (IOException e) {
						System.out.println("HOST RECEPTION ERROR\n" + e.getMessage());
					}
				}

				if (retries == 0) {
					// We have exhausted all the retries and still
					// haven't
					// received a proper ack
					System.out.println("TIMED OUT\n");
					// and quit this file transfer...
					break;
				}

				if (!received.getAddress().equals(clientAddress) || received.getPort() != clientPort) {
					// received a packet with wrong tid, notify the sender
					// with error 5
					trySend(new TftpErrorPacket(TftpErrorPacket.UNKNOWN_TID, "Wrong Transfer ID")
							.generateDatagram(received.getAddress(), received.getPort()), "ISSUE SENDING ERROR PACKET");
					retries--;
					continue;
				}

				try {
					// use the help to decode the packet, if no exception is
					// thrown then this is a TFTP packet
					recvTftpPacket = TftpPacket.decodeTftpPacket(received);
				} catch (IllegalArgumentException ile) {
					trySend(new TftpErrorPacket(TftpErrorPacket.ILLEGAL_OP, ile.getMessage())
							.generateDatagram(received.getAddress(), received.getPort()));
					break;
				}

				// now let's check what type of TFTP packet is this
				if (recvTftpPacket.getType() == TftpType.DATA) {
					TftpDataPacket dataPacket = (TftpDataPacket) recvTftpPacket;
					// System.out.println("block" + blockNumber);
					if (dataPacket.getBlockNumber() == blockNumber) {
						// correct block, reset retry
						retries = CommonConstants.TFTP_MAX_NUM_RETRIES;
						try {
							bos.write(dataPacket.getData());
						} catch (IOException e) {
							// this doesn't work, so using a hack
							// if (file.getUsableSpace() <
							// dataPacket.getDataLength())
							// hack=
							if (e.getMessage().equals("There is not enough space on the disk")) {
								trySend(new TftpErrorPacket(TftpErrorPacket.DISK_FULL, "Disk full, can't write to file")
										.generateDatagram(clientAddress, clientPort));
								deleteFile = true;
							} else {
								// if io error not due to space, send the
								// message as a custom error
								trySend(new TftpErrorPacket(TftpErrorPacket.NOT_DEFINED, e.getMessage())
										.generateDatagram(clientAddress, clientPort));
								System.out.println("ERROR WRITING TO FILE\n" + e.getMessage());
							}
							break;
						}
						if (dataPacket.getDataLength() < CommonConstants.DATA_BLOCK_SZ) {
							// EOF, do a flush so that error can be detected
							// before sending last ACK
							if (verbose)
								System.out.println("Data <512 bytes, going to stop writing to file");
							try {
								bos.flush();
							} catch (IOException e) {
								// hack, what's the proper way of doing
								// this?
								if (e.getMessage().equals("There is not enough space on the disk")) {
									trySend(new TftpErrorPacket(TftpErrorPacket.DISK_FULL,
											"Disk full, can't write to file").generateDatagram(clientAddress,
													clientPort));
									deleteFile = true;
								} else {
									trySend(new TftpErrorPacket(TftpErrorPacket.NOT_DEFINED, e.getMessage())
											.generateDatagram(clientAddress, clientPort));
									System.out.println("ERROR WRITING TO FILE\n" + e.getMessage());
								}
								break;
							}
							serve = false;
						}

						// write success, send ACK
						send = new TftpAckPacket(blockNumber).generateDatagram(clientAddress, clientPort);
						trySend(send, "ERROR SENDING ACK\n");
						blockNumber++;
						if (blockNumber > CommonConstants.TFTP_MAX_BLOCK_NUMBER) {
							// it hits 65535! wrap it back to zero
							blockNumber = 0;
						}
					} else if ((blockNumber - dataPacket.getBlockNumber() >= 0 && blockNumber
							- dataPacket.getBlockNumber() <= CommonConstants.TFTP_BLOCK_MISMATCH_THRESHOLD)) {
						trySend(new TftpAckPacket(dataPacket.getBlockNumber()).generateDatagram(clientAddress,
								clientPort), "ERROR SENDING ACK\n");
						retries--;
					} else {
						trySend(new TftpErrorPacket(TftpErrorPacket.ILLEGAL_OP, "Block number mismatch")
								.generateDatagram(clientAddress, clientPort));
						break;
					}
				} else if (recvTftpPacket.getType() == TftpType.ERROR) {
					TftpErrorPacket errorPacket = (TftpErrorPacket) recvTftpPacket;
					//FIXME delete this later
					//Utils.printDatagramContentWiresharkStyle(recvTftpPacket.generateDatagram());
					
					//if we receive an error packet, that is not error code 5, then we are done with this request
					if (errorPacket.getErrorCode() != TftpErrorPacket.UNKNOWN_TID){
						//FIXME might want to set deleteFile=true
						break;
					}
				} else {
					trySend(new TftpErrorPacket(TftpErrorPacket.ILLEGAL_OP, "Not TFTP DATA")
							.generateDatagram(clientAddress, clientPort));
					retries--;
				}
			} while (serve);

			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e1) {
					// Failed to close output stream, this can happen due to various
					// reason
					// at this point there is no point of sending error to server
					// either it was errored before, or last ACK had already been sent
					System.out.println(e1.getMessage());
				}
			}
			server.declareThisFileNotInWrite(file);
			// consolidate file deletion code
			// if file needs to be deleted set the boolean
			if (deleteFile) {
				try {
					Files.deleteIfExists(file.toPath());
				} catch (IOException e) {
					System.out.println("FAILED TO DELETE INCOMEPLETED FILE\n" + e.getMessage());
				}
			}
					
		}
		socket.close();//Should close the socket after the thread is done (David)
	}
	
	/**
	 * Wrapped version of send to reduce duplicated code
	 * 
	 * @param packet
	 *            packet to be sent
	 * @param errMsg
	 *            error msg to be printed in console if send goes wrong
	 */
	private void trySend(DatagramPacket packet, String errMsg) {
		if (verbose) {
			System.out.println("\nSending...");
			Utils.tryPrintTftpPacket(packet);
		}

		try {
			socket.send(packet);
		} catch (IOException e) {
			if (errMsg.length() != 0)
				System.out.println(errMsg + e.getMessage());
		}
	}

	/**
	 * Wrapped version of send to reduce duplicated code
	 * 
	 * @param packet
	 *            packet to be sent
	 */
	private void trySend(DatagramPacket packet) {
		trySend(packet, "");
	}
}

