
/**
 * Client component of S17 SYSC3303 project
 * 
 * @author Team4
 *
 */
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.Scanner;

import TftpPacketHelper.TftpAckPacket;
import TftpPacketHelper.TftpDataPacket;
import TftpPacketHelper.TftpErrorPacket;
import TftpPacketHelper.TftpPacket;
import TftpPacketHelper.TftpPacket.TftpType;
import TftpPacketHelper.TftpReadRequestPacket;
import TftpPacketHelper.TftpRequestPacket.TftpTransferMode;
import TftpPacketHelper.TftpWriteRequestPacket;

public class Client {
	/**
	 * Default operation mode when client startup
	 */
	private static boolean testMode = true;
	/**
	 * Verbose or not when client startup
	 */
	private static boolean verbose = true;
	
	private static Scanner sc = new Scanner(System.in);
	private static File file;

	private static int destinationPort = (testMode) ? CommonConstants.HOST_LISTEN_PORT
			: CommonConstants.SERVER_LISTEN_PORT;
	private int tid;
	private int retries;

	private DatagramPacket sendPacket, receivePacket;
	private static DatagramSocket sendReceiveSocket;
	
	 //IP address of server
	private static InetAddress destinationAddress = null;

	public Client() {
		try {
			sendReceiveSocket = new DatagramSocket();
			sendReceiveSocket.setSoTimeout(CommonConstants.SOCKET_TIMEOUT_MS);

		} catch (SocketException se) { // Can't create the socket.
			// unlikely to get there as there should be plenty of free ports
			se.printStackTrace();
			sc.close();
			System.exit(1);
		}
	}

	public static void main(String args[]) throws IllegalArgumentException, IOException {
		Client c = new Client();
		byte[] data;

		// query user for RRQ or WRQ or toggle between modes
		for (;;) {
			System.out.println("Client: currently in: " + c.getOutputMode() + " output mode and " + c.getOperationMode()
					+ " operation mode");
			
			String request = queryUserRequest(c);

			//user wants to shutdown
			if (request.equals("6"))
				shutdown();

			//on startup make sure to ask for IP
			if(destinationAddress==null)
				setDestinationAddress();
//			try {
//				// code for quick testing on server located on another machine
//				// destinationAddress =
//				// InetAddress.getByName("192.168.217.128");
//				destinationAddress = InetAddress.getLocalHost();
//			} catch (UnknownHostException uhe) {
//				System.out.println("Failed to resolved host name");
//				continue;
//			}

			String filename = queryFilename();
			file = new File(filename);
			System.out.println("Filename: " + filename);
			if (request.equals("1")) {
				if (checkFile(file, false)) {
					data = new TftpReadRequestPacket(filename, TftpTransferMode.MODE_OCTET).generatePayloadArray();
					c.sendRequest(data);
					c.readFromHost(filename);
				}

			} else if (request.equals("2")) {
				if (checkFile(file, true)) {
					data = new TftpWriteRequestPacket(filename, TftpTransferMode.MODE_OCTET).generatePayloadArray();
					c.sendRequest(data);
					c.writeToHost(filename);
				}
			}
		}
	}

	/**
	 * Check the provides file object to see if it can be used For WRQ, set read
	 * to true because we are reading from client/write to host For RRQ, set
	 * read to false because we are writing to client/read from host
	 * 
	 * @param file
	 *            file to be checked
	 * @param read
	 *            true if this is a read operation, false if write
	 * @return true if this file is usable
	 */
	private static boolean checkFile(File file, boolean read) {
		if (read) {
			System.out.println(file.toPath());
			if (!file.exists()) {
				System.out.println("No such file");
				return false;
			} else if (file.isDirectory()) {
				System.out.println("This is a directory");
				return false;
			} else if (!Files.isReadable(file.toPath())) {
				System.out.println("Unable to read the file from local machine due to insufficient permission");
				return false;
			}
		} else {
			System.out.println(file.toPath());
			if (file.exists()) {
				if (file.isDirectory()) {
					System.out.println("This is a directory");
					return false;
				} else if (!Files.isWritable(file.toPath())) {
					System.out.println("Unable to write the file to local machine due to insufficient permission");
					return false;
				}
			}
		}

		return true;
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
			sendReceiveSocket.send(packet);
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

	private void writeToHost(String fileName) {
		byte receiveBuffer[] = new byte[CommonConstants.TFTP_RECEIVE_BUFFER_SIZE];
		byte fileReadBuffer[] = new byte[CommonConstants.DATA_BLOCK_SZ];

		receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

		int blockNumber = 1, byteRead = 0;
		BufferedInputStream in;
		TftpPacket recvTftpPacket;
		boolean finished = false;
		boolean acked = true;
		boolean successful = false;
		
		retries = CommonConstants.TFTP_MAX_NUM_RETRIES; // reset
		
		// for special ACK 0
		// TODO: clean this code up 
		while (retries > 0) {
			try {
				if (verbose) {
					System.out.println("\nReceiving...");
				}
				sendReceiveSocket.receive(receivePacket);
				if (verbose) {
					Utils.tryPrintTftpPacket(receivePacket);
				}
				break;
			} catch (SocketTimeoutException te) {
				if (verbose)
					System.out.println("Timed out on receiving ACK0, resend RRQ\n");
				trySend(sendPacket, "");
				retries--;

			} catch (IOException e) {
				System.out.println("CLIENT RECEPTION ERROR\n" + e.getMessage());
			}
		}

		try {
			recvTftpPacket = TftpPacket.decodeTftpPacket(receivePacket);
		} catch (IllegalArgumentException ile) {
			// not a TFTP packet, what to do?
			// retry?
			return;
		}

		retries = CommonConstants.TFTP_MAX_NUM_RETRIES; // reset
		
		if (receivePacket.getAddress().equals(destinationAddress) && recvTftpPacket.getType() == TftpType.ACK) {
			TftpAckPacket ackPacket = (TftpAckPacket) recvTftpPacket;
			if (ackPacket.getBlockNumber() == 0) {
				// we got the special ack 0 from server, take note of the tid
				tid = receivePacket.getPort();
			} else {
				// not the special ack? what going on? terminate by sending
				// error 04
				trySend(new TftpErrorPacket(TftpErrorPacket.ILLEGAL_OP, 
						"Did not receive expected request acknowledge.").generateDatagram(receivePacket.getAddress(),
						receivePacket.getPort()));
				return;
			}
		}else if(receivePacket.getAddress().equals(destinationAddress) && recvTftpPacket.getType()== TftpType.ERROR )
		{
			// should execute this code if we received an error packet when we were expecting ack0
			TftpErrorPacket errorPacket = (TftpErrorPacket) recvTftpPacket;
			if (errorPacket.getErrorCode() != TftpErrorPacket.UNKNOWN_TID) {
					System.out.println("Error code " + errorPacket.getErrorCode() + ": " + errorPacket.getErrorMsg());
				return;
			} else {
				System.out.println(
						"Received Unknown TID error: " + errorPacket.getErrorCode() + " :" + errorPacket.getErrorMsg());
			}
		}
		else {
			trySend(new TftpErrorPacket(TftpErrorPacket.ILLEGAL_OP, "Not TFTP ACK").generateDatagram(receivePacket.getAddress(),
					receivePacket.getPort()));
			return;
		}

		try {
			in = new BufferedInputStream(new FileInputStream(file));
		} catch (Exception e) {
			// File was checked in main loop, we could only reach here when
			// something very bad happened
			trySend(new TftpErrorPacket(TftpErrorPacket.NOT_DEFINED, e.getMessage()).generateDatagram(destinationAddress, tid));
			return;
		}

		do {
			if (acked) {
				// only read the next chunk if we have received an ack
				try {
					byteRead = in.read(fileReadBuffer);
				} catch (IOException e) {
					// something went wrong while reading file, don't think we
					// can continue
					// is there an error code suitable for this?
					System.out.println("ERROR READING FILE\n" + e.getMessage());
					break;
				}
				acked = false;

				if (byteRead < CommonConstants.DATA_BLOCK_SZ) {
					// read returns less than 512, that's the end of file
					if (byteRead == -1) {
						// if nothing to read the FileInputStream returns -1,
						// make this to zero since we need to pass this a length
						// parameter to helper
						byteRead = 0;
					}
					// take a note that we reached end of this
					finished = true;
				}
				sendPacket = new TftpDataPacket(blockNumber, fileReadBuffer, byteRead).generateDatagram(destinationAddress,
						tid);
				trySend(sendPacket, "ERROR SENDING DATA");
			}

			while (retries > 0) {
				try {
					if (verbose) {
						System.out.println("\nReceiving...");
					}
					sendReceiveSocket.receive(receivePacket);
					if (verbose) {
						Utils.tryPrintTftpPacket(receivePacket);
					}
					break;
				} catch (SocketTimeoutException te) {
					if (verbose)
						System.out.println("Timed out on receiving ACK, resend DATA\n");
					trySend(sendPacket, "");
					retries--;
				} catch (IOException e) {
					System.out.println("HOST RECEPTION ERROR\n" + e.getMessage());
				}
			}

			if (retries == 0) {
				// We have exhausted all the retries and still
				// haven't received a proper ack
				System.out.println("TIMED OUT\n");
				// and quit this file transfer...
				break;
			}

			if (!receivePacket.getAddress().equals(destinationAddress) || receivePacket.getPort() != tid) {
				// received a packet with wrong tid, notify the sender with
				// error 5
				trySend(new TftpErrorPacket(TftpErrorPacket.UNKNOWN_TID, "Wrong Transfer ID").generateDatagram(receivePacket.getAddress(),
						receivePacket.getPort()));
				retries--;
				continue;
			}

			try {
				recvTftpPacket = TftpPacket.decodeTftpPacket(receivePacket);
			} catch (IllegalArgumentException ile) {
				// not a TFTP packet, try again
				trySend(new TftpErrorPacket(TftpErrorPacket.ILLEGAL_OP, ile.getMessage())
						.generateDatagram(receivePacket.getAddress(), receivePacket.getPort()));
				break;
			}

			// let's check if the packet we received is an ack
			if (recvTftpPacket.getType() == TftpType.ACK) {
				TftpAckPacket ackPacket = (TftpAckPacket) recvTftpPacket;
				if (ackPacket.getBlockNumber() == blockNumber) {
					// ack contains correct block number, increment out count
					blockNumber++;
					if (blockNumber > CommonConstants.TFTP_MAX_BLOCK_NUMBER) {
						// reached 65535, wrap to 0
						blockNumber = 0;
					}
					acked = true;					
					retries = CommonConstants.TFTP_MAX_NUM_RETRIES; // reset
					if (finished) {
						successful = true;
					}
				} else if ((blockNumber - ackPacket.getBlockNumber() >= 0 && blockNumber
						- ackPacket.getBlockNumber() <= CommonConstants.TFTP_BLOCK_MISMATCH_THRESHOLD)) {
					// do not re-send data for duplicated ACK
					retries--;
				} else {
					trySend(new TftpErrorPacket(TftpErrorPacket.ILLEGAL_OP, "Block number mismatch")
							.generateDatagram(destinationAddress, tid));
					break;
				}
			} else if (recvTftpPacket.getType() == TftpType.ERROR) {
				TftpErrorPacket errorPacket = (TftpErrorPacket) recvTftpPacket;
				if (errorPacket.getErrorCode() != TftpErrorPacket.UNKNOWN_TID) {
					System.out.println("Error code " + errorPacket.getErrorCode() + ": " + errorPacket.getErrorMsg());
					break;
				} else {
					System.out.println("Received Unknown TID error: " + errorPacket.getErrorCode() + " :"
							+ errorPacket.getErrorMsg());
				}
			} else {
				trySend(new TftpErrorPacket(TftpErrorPacket.ILLEGAL_OP, "Not TFTP ACK")
						.generateDatagram(destinationAddress, destinationPort));
				break;
			}

		} while (!finished);

		try {
			in.close();
		} catch (IOException e) {
			// Failed to close input stream for reading, this is not expected
			System.out.println(e.getMessage());
		}
		if (successful) {
			System.out.println("File transfer successfully completed.");
		}
		System.out.println();
	}

	private void readFromHost(String fileName) {
		boolean deleteFile = false;
		boolean endOfFile = false;
		boolean successful = false;
		byte[] writeData = new byte[CommonConstants.DATA_PACKET_SZ];
		receivePacket = new DatagramPacket(writeData, writeData.length);
		int blockNumber = 1;
		BufferedOutputStream out = null;
		TftpPacket recvTftpPacket;
		try {
			out = new BufferedOutputStream(new FileOutputStream(fileName));
		} catch (Exception e) {
			// File was checked in main loop, we could only reach here when
			// something very bad happened
			trySend(new TftpErrorPacket(TftpErrorPacket.NOT_DEFINED, e.getMessage()).generateDatagram(destinationAddress, destinationPort));
			return;
		}

		retries = CommonConstants.TFTP_MAX_NUM_RETRIES; // reset
		
		while (!endOfFile) {
			while (retries > 0) {
				try {
					if (verbose) {
						System.out.println("\nReceiving...");
					}
					sendReceiveSocket.receive(receivePacket);
					if (verbose) {
						Utils.tryPrintTftpPacket(receivePacket);
					}
					break;
				} catch (SocketTimeoutException te) {
					if (verbose)
						System.out.println("Timed out on receiving DATA, resend ACK/RRQ\n");
					trySend(sendPacket, "");
					retries--;
				} catch (IOException e) {
					System.out.println("CLIENT RECEPTION ERROR\n" + e.getMessage());
				}
			}

			if (retries == 0) {
				// We have exhausted all the retries and still
				// haven't received a proper ack
				System.out.println("TIMED OUT\n");
				// and quit this file transfer...
				break;
			}

			try {
				// use the helper to decode the packet, if no exception is thrown
				// then it is a TFTP packet
				recvTftpPacket = TftpPacket.decodeTftpPacket(receivePacket);
			} catch (IllegalArgumentException ile) {
				trySend(new TftpErrorPacket(TftpErrorPacket.ILLEGAL_OP, ile.getMessage()).generateDatagram(receivePacket.getAddress(),
						receivePacket.getPort()));
				break;
			}

			if (tid != -1 && (!receivePacket.getAddress().equals(destinationAddress) || receivePacket.getPort() != tid)) {
				trySend(new TftpErrorPacket(TftpErrorPacket.UNKNOWN_TID, "Wrong Transfer ID")
						.generateDatagram(receivePacket.getAddress(), receivePacket.getPort()), "ISSUE SENDING ERROR PACKET");
				retries--;
				continue;
			}
			// now let's check what type of TFTP packet is this
			if (recvTftpPacket.getType() == TftpType.DATA) {
				// this is a TFTP data packet, let's cast it to TftpDataPacket
				// and use the methods of the class to make our life easier
				TftpDataPacket dataPacket = (TftpDataPacket) recvTftpPacket;

				if (tid == -1 && receivePacket.getAddress().equals(destinationAddress) && dataPacket.getBlockNumber() == 1) {
					// first DATA packet received from server side, take a note
					// of the TID
					tid = receivePacket.getPort();
				} 
				// System.out.println("block" + blockNumber);
				if (dataPacket.getBlockNumber() == blockNumber) {
					// ok, we got correct block. write it to file system...
					retries = CommonConstants.TFTP_MAX_NUM_RETRIES; // reset
					try {
						out.write(dataPacket.getData());
					} catch (IOException e) {
						// parsing the error message is generally a bad idea as
						// message may differ from os
						//this doesnt work, so using a hack
						//if (file.getUsableSpace() < dataPacket.getDataLength()) 
						//hack=
						if(e.getMessage().equals("There is not enough space on the disk"))
						{
							trySend(new TftpErrorPacket(TftpErrorPacket.DISK_FULL, "Disk full, can't write to file").generateDatagram(destinationAddress, tid));
							deleteFile = true;
						} else {
							// if io error not due to space, send the message as
							// a custom error
							trySend(new TftpErrorPacket(TftpErrorPacket.NOT_DEFINED, e.getMessage()).generateDatagram(destinationAddress, tid));
							System.out.println("ERROR WRITING TO FILE\n" + e.getMessage());
						}
						break;
					}

					if (receivePacket.getLength() < CommonConstants.DATA_PACKET_SZ) {
						// end of file
						// do a flush so that error can be detected and sent
						// before sending last ack
						try {
							out.flush();
							successful = true;
						} catch (IOException e) {
							if (e.getMessage().equals("There is not enough space on the disk")) {
								trySend(new TftpErrorPacket(TftpErrorPacket.DISK_FULL, "Disk full, can't write to file").generateDatagram(destinationAddress, tid));
								deleteFile = true;
							} else {
								trySend(new TftpErrorPacket(TftpErrorPacket.NOT_DEFINED, e.getMessage()).generateDatagram(destinationAddress,
										tid));
								System.out.println("ERROR WRITING TO FILE\n" + e.getMessage());
							}
							break;
						}
						endOfFile = true;
					}

					// generate an ack packet with the help and sent it to
					// server
					sendPacket = new TftpAckPacket(blockNumber).generateDatagram(destinationAddress, tid);
					trySend(sendPacket, "ERROR SENDING ACK\n");
					blockNumber++;
					if (blockNumber > CommonConstants.TFTP_MAX_BLOCK_NUMBER) {
						// it hits 65535! wrap it back to zero
						blockNumber = 0;
					}
				} else if ((blockNumber - dataPacket.getBlockNumber() >= 0 && blockNumber
						- dataPacket.getBlockNumber() <= CommonConstants.TFTP_BLOCK_MISMATCH_THRESHOLD)) {
					trySend(new TftpAckPacket(dataPacket.getBlockNumber()).generateDatagram(destinationAddress, tid),
							"ERROR SENDING ACK\n");
					retries--;
				} else {
					trySend(new TftpErrorPacket(TftpErrorPacket.ILLEGAL_OP, "Block number mismatch")
							.generateDatagram(destinationAddress, tid));
					break;
				}
			} else if (recvTftpPacket.getType() == TftpType.ERROR) {
				TftpErrorPacket errorPacket = (TftpErrorPacket) recvTftpPacket;
				int errorCode = errorPacket.getErrorCode();	
				
				// should not return on error type 5 though
				if (errorCode != TftpErrorPacket.UNKNOWN_TID) {
					if (errorCode == TftpErrorPacket.FILE_NOT_FOUND) {
						deleteFile = true;
					}
					System.out.println("Error code " + errorPacket.getErrorCode() + ": " + errorPacket.getErrorMsg());
					break;
				} else {
					System.out.println("Received Unknown TID error: " + errorPacket.getErrorCode() + " :"
							+ errorPacket.getErrorMsg());
				}

			} else {
				// we got TFTP packet but it is either DATA nor ERROR. something
				// is indeed wrong
				trySend(new TftpErrorPacket(TftpErrorPacket.ILLEGAL_OP, "Not TFTP DATA")
						.generateDatagram(destinationAddress, receivePacket.getPort()));
				break;
			}
		}//end while

		try {
			out.close();
		} catch (IOException e) {
			// Failed to close output stream, this can happen due to various
			// reason
			// at this point there is no point of sending error to server
			// either it was errored before, or last ack had already been
			// sent
			successful = false;
			System.out.println(e.getMessage());
		}
		
		// consolidate file deletion code
		// if file needs to be deleted set the boolean
		if (deleteFile) {
			try {
				Files.deleteIfExists(file.toPath());
			} catch (IOException e) {
				System.out.println("FAILED TO DELETE INCOMEPLETED FILE\n" + e.getMessage());
			}
		}

		if (successful) {
			System.out.println("File transfer successfully completed.");
		}
		
		System.out.println();
	}

	/**
	 * Send a UDP packet with data packed to the destination
	 * 
	 * @param payload
	 *            byte array containing the UDP data
	 * @throws IOException
	 *             when things goes wrong
	 */
	public void sendRequest(byte[] payload) {
		sendPacket = new DatagramPacket(payload, payload.length, destinationAddress, destinationPort);

		System.out.println("\nStarting the transfer...");
		trySend(sendPacket);
		tid = -1;
	}
	
	public InetAddress getIP(){
		return Client.destinationAddress;
	}

	public String getOutputMode() {
		if (verbose)
			return CommonConstants.VERBOSE;
		else
			return CommonConstants.QUIET;
	}

	public String getOperationMode() {
		// put it somewhere common
		// or is it client only? if so don't
		if (testMode)
			return CommonConstants.TEST;
		else
			return CommonConstants.NORM;
	}

	private static void toggleOperation(Client c) {
		testMode = !testMode;
		if (testMode) {
			destinationPort = CommonConstants.HOST_LISTEN_PORT;
		} else {
			destinationPort = CommonConstants.SERVER_LISTEN_PORT;
		}
		System.out.println("Operation Mode changed to: " + c.getOperationMode());
	}

	private static void toggleMode(Client c) {
		verbose = !verbose;
		System.out.println("Output Mode changed to: " + c.getOutputMode());
	}

	private static String queryUserRequest(Client c) {
		// ask user for input
		System.out.println(
				"\nEnter:\n1 to read a file\n2 to write a file\n3 to toggle output mode\n4 to toggle operation mode\n5 Change server IP address\n6 to shutdown");
		String request = sc.nextLine().trim();

		while (!request.equals("1") && !request.equals("2") && !request.equals("6")) {
			// request to toggle mode
			if (request.equals("3"))
				toggleMode(c);
			else if (request.equals("4"))
				toggleOperation(c);
			else if(request.equals("5"))
				setDestinationAddress();
			System.out.println(
					"\nEnter:\n1 to read a file\n2 to write a file\n3 to toggle output mode\n4 to toggle operation mode\n5 Change server IP address\n6 to shutdown");
			request = sc.nextLine().trim();
		}
		return request;
	}

	private static String queryFilename() {
		String filename = "";
		while(filename.equals(""))
		{
			System.out.println("Enter a filename:");
			filename = sc.nextLine();
			if(filename.equals(""))
				System.out.println("Can't have blank file name.");
		}
		return filename;
	}
	
	
	/**
	 * This method will prompt the user to get the IP address of the server	
	 */
	private static void setDestinationAddress(){
		//user might want to change server address, so set it back to null
		destinationAddress = null;
		do{
			try {
				System.out.println("\nPlease enter the server's IP:\n"
						+ "If you'd like to use the local host enter 1 or local host");
				String ip = sc.nextLine().trim();
				
				//wants local host
				if(ip.equals("local host")|| ip.equals("1"))
					destinationAddress = InetAddress.getLocalHost();
				//optimizing code, getByName function might take a while to parse invalid IP address
				//thus this code will make program faster
				else if(Utils.quickResolve(ip) == null)
					continue;
				else {
					try {
						destinationAddress = InetAddress.getByName(ip); 
					} catch(UnknownHostException e) {
						System.out.println("The provided address could not be indentified.");
						System.out.println("Please re-enter the address.");
					}
				}
				
//				IF WE DON'T ALLOW THE CLIENT AND SERVER TO BE ON SAME COMPUTER
//				THEN UNCOMMENT THIS CODE
//				if(ip.equals(InetAddress.getLocalHost().getHostAddress())||ip.equals("127.0.0.1")){
//					System.out.println("Server address can't have same address as this computer.");
//					destinationAddress = null;
//					continue;
//				}
			} catch (UnknownHostException e) {
				System.out.println("Please enter a valid IP address\n");
			}
		}while(destinationAddress==null);
		
		System.out.println("Server IP is: "+destinationAddress.getHostAddress()+"\n");
	}

	private static void shutdown() {
		System.out.println("Client is exiting");
		sendReceiveSocket.close();
		sc.close();
		System.exit(1);
	}
}
