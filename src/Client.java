
/**
 * Client component of S17 SYSC3303 project
 * 
 * @author Team4
 *
 */
import java.io.*;
import java.net.*;
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
	private static boolean testMode = false;
	private static boolean verbose = false;
	private static Scanner sc = new Scanner(System.in);
	private static File file;
	private static InetAddress destinationAddress;
	private static int destinationPort = (testMode) ? CommonConstants.HOST_LISTEN_PORT
			: CommonConstants.SERVER_LISTEN_PORT; // FIX ME, this is ugly
	private int tid = 0;
	// private int retries;

	private DatagramPacket sendPacket, receivePacket;
	private static DatagramSocket sendReceiveSocket;

	public Client() {
		try {
			sendReceiveSocket = new DatagramSocket();
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
		// assumption is to have it in quiet mode

		// query user for RRQ or WRQ or toggle between modes
		for (;;) {
			System.out.println("Client: currently in:\n" + c.getOutputMode() + " output mode\n" + c.getOperationMode()
					+ " operation mode");
			String request = queryUserRequest(c);

			if (request.equals("5"))
				shutdown();

			try {
				//what is this used for?
				//destinationAddress = InetAddress.getByName("192.168.217.128");
				destinationAddress = InetAddress.getLocalHost();
			} catch (UnknownHostException uhe) {
				System.out.println("Failed to resolved host name");
				continue;
			}

			String filename = queryFilename();
			file = new File(filename);
			System.out.println("Filename: " + filename);
			if (request.equals("1")) {
				if (file.exists()) {
					// is it worth putting a warning here?
					// System.out.println("Overwriting local file");
				}

				data = new TftpReadRequestPacket(filename, TftpTransferMode.MODE_OCTET).generatePayloadArray();

				c.sendRequest(data);
				c.readFromHost(filename);
			} else if (request.equals("2")) {
				if (!file.exists()) {
					System.out.println("No such file");
				} else if (file.isDirectory()) {
					System.out.println("This is a directory");
				} else {
					// write request
					// System.out.println("write request");
					data = new TftpWriteRequestPacket(filename, TftpTransferMode.MODE_OCTET).generatePayloadArray();
					c.sendRequest(data);
					c.writeToHost(filename);
				}
			}
		}
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
		TftpPacket tftpPacket = null;
		boolean isTftp = true;

		System.out.println("Sending one packet...");

		try {
			tftpPacket = TftpPacket.decodeTftpPacket(packet);
		} catch (Exception e) {
			isTftp = false;
		}

		if (verbose) {
			if (isTftp && tftpPacket != null) {
				System.out.println(tftpPacket);
			} else {
				Utils.printDatagramContentWiresharkStyle(packet);
			}
			System.out.println("");
		}

		try {
			sendReceiveSocket.send(packet);
		} catch (IOException e) {
			// do we really want to quit when send fails?
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

	private void receiveAndMaybePrint(DatagramPacket packet) throws IOException {
		TftpPacket tftpPacket = null;
		boolean isTftp = true;

		sendReceiveSocket.receive(packet);
		try {
			tftpPacket = TftpPacket.decodeTftpPacket(packet);
		} catch (Exception e) {
			isTftp = false;
		}
		
		if (verbose) {
			System.out.println("Received one packet...");
			if (isTftp && tftpPacket != null) {
				System.out.println(tftpPacket);
			} else {
				Utils.printDatagramContentWiresharkStyle(packet);
			}
			System.out.println("");
		}
	}

	private void writeToHost(String fileName) throws IOException {
		// BEFORE WRITING TO HOST, MAKE SURE TO RECEIVE ACKNOWLEDGE BLK#0
		// Construct a DatagramPacket for receiving packets
		byte receiveBuffer[] = new byte[CommonConstants.ACK_PACKET_SZ];
		byte fileReadBuffer[] = new byte[CommonConstants.DATA_BLOCK_SZ];
		receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

		int blockNumber = 1, byteRead = 0;
		BufferedInputStream in;
		TftpPacket recvTftpPacket;
		boolean finished = false;
		boolean acked = true;

		// wait for a packet to be returned back
		try {
			// Block until a datagram is received via sendReceiveSocket.
			receiveAndMaybePrint(receivePacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		try {
			recvTftpPacket = TftpPacket.decodeTftpPacket(receivePacket);
		} catch (IllegalArgumentException ile) {
			// not a TFTP packet, what to do?
			// retry?
			return;
		}

		if (recvTftpPacket.getType() == TftpType.ACK) {
			TftpAckPacket ackPacket = (TftpAckPacket) recvTftpPacket;
			if (ackPacket.getBlockNumber() == 0) {
				// we got the special ack 0 from server, take note of the tid
				tid = receivePacket.getPort();
			} else {
				// not the special ack? what going on? terminate by sending
				// error 04
				trySend(new TftpErrorPacket(4, "not ack 0").generateDatagram(receivePacket.getAddress(),
						receivePacket.getPort()));
				return;
			}
		} else {
			// not even an ack!
			trySend(new TftpErrorPacket(4, "not tftp ack").generateDatagram(receivePacket.getAddress(),
					receivePacket.getPort()));
			return;
		}

		try {
			// open a FileInputStream for the file first
			// if this fails no point to continue
			in = new BufferedInputStream(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			// we checked the File before, so we should not reach here
			System.out.println("THIS IS A DIRECTORY\n" + e.getMessage());
			return;
		} catch (SecurityException se) {
			// permission issue?
			// // iteration 3
			// trySend(new TftpErrorPacket(2,
			// "").generateDatagram(clientAddress, clientPort), "");
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
			}
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

			// retries = CommonConstants.TFTP_MAX_NUM_RETRIES; // reset
			sendPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

			// iteration 4 stuff....
			// while (retries > 0) {
			try {
				receiveAndMaybePrint(receivePacket);
				// break;
				// } catch (SocketTimeoutException te) {
				// if (vervose)
				// System.out.println("Timed out on receving ACK, resend
				// DATA\n");
				// trySend(send, "");
				// retries--;

			} catch (IOException e) {
				System.out.println("HOST RECEPTION ERROR\n" + e.getMessage());
			}
			// }

			if (receivePacket.getPort() != tid) {
				// receive a packet with wrong tid, notify the sender with error
				// 5
				trySend(new TftpErrorPacket(5, "Wrong Transfer ID").generateDatagram(receivePacket.getAddress(),
						receivePacket.getPort()));
				acked = false;
				// retries--;
				// for iteration 4
				// continue;
				System.out.println("Will terminate this transfer.");
				break;
			}

			// if (retries == 0) {
			// System.out.println("TIMED OUT\n" );
			// break;
			// }

			try {
				recvTftpPacket = TftpPacket.decodeTftpPacket(receivePacket);
			} catch (IllegalArgumentException ile) {
				// not a TFTP packet, what to do?
				trySend(new TftpErrorPacket(4, "not tftp").generateDatagram(receivePacket.getAddress(),
						receivePacket.getPort()));
				// re-transmission in iteration 4....
 				// retries--;
				// continue;
				finished = true;
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
				} // else if (ackPacket.getBlockNumber() > blockNumber)
					// {
					// is this possible?
					// }
				else {
					finished = true;
					trySend(new TftpErrorPacket(4, "PACKET BLOCK # MISMATCH").generateDatagram(destinationAddress, tid));
					// retries--;
				}
			} else {
				trySend(new TftpErrorPacket(4, "not ack").generateDatagram(destinationAddress, destinationPort));
			}

		} while (!finished);

		try {
			in.close();
		} catch (IOException e) {
			// seriously there is nothing you can do here if something is
			// wrong
			e.printStackTrace();
		} // Apache commons-io's closeQuietly would be handy..

	}

	private void readFromHost(String fileName) throws IOException {
		boolean endOfFile = false;
		byte[] writeData = new byte[CommonConstants.DATA_PACKET_SZ];
		int blockNumber = 1;
		BufferedOutputStream out = null;
		TftpPacket recvTftpPacket;

		try {
			out = new BufferedOutputStream(new FileOutputStream(fileName));
		} catch (FileNotFoundException e) {
			System.out.println("THERE EXIST A DIRECTORY WITH THIS NAME, NOT FILE\n" + e.getMessage());
			return;
		} catch (SecurityException e) {
			// should client fire an ERROR packet to server in iteration 3?
			System.out.println("ERROR CREATING FILE\n" + e.getMessage());
			return;
		}

		while (!endOfFile) {
			// retries = CommonConstants.TFTP_MAX_NUM_RETRIES; // reset
			receivePacket = new DatagramPacket(writeData, writeData.length);

			// iteration 4
			// while (retries > 0) {
			try {
				receiveAndMaybePrint(receivePacket);
				// break;
				// } catch (SocketTimeoutException te) {
				// if (vervose)
				// System.out.println("Timed out on receiving DATA, resend
				// ACK\n");
				// trySend(send, "");
				// retries--;
			} catch (IOException e) {
				System.out.println("HOST RECEPTION ERROR\n" + e.getMessage());
			}
			// }

			// if (retries == 0) {
			// System.out.println("TIMED OUT\n" );
			// break;
			// }

			
			try {
				// use the help to decode the packet, if no exception is thrown
				// then is a TFTP packet
				recvTftpPacket = TftpPacket.decodeTftpPacket(receivePacket);
			} catch (IllegalArgumentException ile) {
				// not a TFTP packet, what to do?
				// keep waiting until timeout to avoid Sorcerer's Apprentice
				// bug?
				// send an TFTP error may not be appropriate as it may be a
				// ping, custom protocol... etc
				// or check the buffer to see if it is starting as {0, x}?
				// decodeTftpPacket() will throw IllegalArgumentException if
				// any part of the format is incorrect (this includes opcode,
				// missing filename/mode in request.. etc)
				// is indeed wrong
				trySend(new TftpErrorPacket(4, "not tftp").generateDatagram(receivePacket.getAddress(),
						receivePacket.getPort()));
				continue;
			}

			// now let's check what type of TFTP packet is this
			if (recvTftpPacket.getType() == TftpType.DATA) {
				// this is a TFTP data packet, let's cast it to TftpDataPacket
				// and use the methods of the class to make our life easier
				TftpDataPacket dataPacket = (TftpDataPacket) recvTftpPacket;

				if (receivePacket.getAddress().equals(destinationAddress) && dataPacket.getBlockNumber() == 1) {
					// first DATA packet received from server side, take a note
					// of the TID
					// should we record the IP?
					tid = receivePacket.getPort();
				} else if (tid != receivePacket.getPort()) {
					// TID changed! What happened? some packet lost in network?
					// send from someone else?

					// send Error Code 5 Unknown transfer ID to terminate this
					// connection
					trySend(new TftpErrorPacket(5, "").generateDatagram(receivePacket.getAddress(),
							receivePacket.getPort()));
					continue;
				}

				//System.out.println("block" + blockNumber);
				if (dataPacket.getBlockNumber() == blockNumber) {
					// ok, we got correct block. write it to file system...
					try {
						out.write(dataPacket.getData());
					} catch (IOException e) {
						// iteration 3...
						// can't tell from Exception unless you do parsing
						// but the error msg differs between OS
						// so why don't we check the disk size instead?
						// or maybe one of the NIO library call can have this
						// kind of exception?

						// does client need to send error to server ?

						// if (file.getUsableSpace() <
						// CommonConstants.DATA_BLOCK_SZ) {
						// trySend(new TftpErrorPacket(3, "Invalid
						// response").generateDatagram(clientAddress,
						// clientPort), "ISSUE CREATING DATA ERROR PACKET\n");
						// serve = false;
						// }else
						System.out.println("ERROR WRITING TO FILE\n" + e.getMessage());
					}
					// generate an ack packet with the help and sent it tp
					// server
					sendPacket = new TftpAckPacket(blockNumber).generateDatagram(destinationAddress, tid);
					trySend(sendPacket, "ERROR SENDING ACK\n");
					blockNumber++;
					if (blockNumber > CommonConstants.TFTP_MAX_BLOCK_NUMBER) {
						// it hits 65535! wrap it back to zero
						blockNumber = 0;
					}
				} 
				/*
//				else if (dataPacket.getBlockNumber() == blockNumber - 1) {
//					 got same DATA again, previous one lost in transmission?
//					sendPacket = new TftpAckPacket(blockNumber).generateDatagram(destinationAddress, tid);
//					trySend(sendPacket, "ERROR RESENDING ACK");
 * */

				 else if (dataPacket.getDataLength() > CommonConstants.DATA_BLOCK_SZ) {
					// well.. we had specified the underlying buffer as a byte
					// 512 + 4 array , will it reach here?
					// the payload should have been truncated to 512 so don't
					// think we will get here
					// trySend(new TftpErrorPacket(4, "Incorrect block
					// size..").generateDatagram(clientAddress,
					// clientPort), "");
				} else {
					// if we are here then the block number is really off
					// I guess we should terminate the connection now?
					trySend(new TftpErrorPacket(4, "PACKET BLOCK # MISMATCH").generateDatagram(destinationAddress, tid),
							"");
					endOfFile = true;
				}
			} else if (recvTftpPacket.getType() == TftpType.ERROR) {
				TftpErrorPacket errorPacket = (TftpErrorPacket) recvTftpPacket;
				// got error from server, terminate and tell user the error code
				// and error message
				// do we need to enhance the help to translate the error code as
				// well?
				System.out.println("RECEIVED ERROR :" + errorPacket.getErrorCode() + " :" + errorPacket.getErrorMsg());
				endOfFile = true;
			} else {
				// we got TFTP packet but it is either DATA nor ERROR. something
				// is indeed wrong
				trySend(new TftpErrorPacket(4, "PACKET OPCODE IS NOT DATA").generateDatagram(destinationAddress,
						receivePacket.getPort()));
				endOfFile = true;
			}

			if (receivePacket.getLength() < CommonConstants.DATA_PACKET_SZ) {
				// received data packet has a length smaller than block size,
				// end of file
				endOfFile = true;
			}
		}

		try {
			// don't forget to close the FileOutput Stream
			out.close();
		} catch (IOException e) {
			// iteration 3...
			// something bad happened.. disk dead? full? permission issue?
		}
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

		System.out.println("Client: Sending request packet:");
		trySend(sendPacket);
		System.out.println("Client: Request packet sent.\n");
	}

	public String getOutputMode() {
		if (verbose)
			return CommonConstants.VERBOSE;
		else
			return CommonConstants.QUIET;
	}

	public void setOutputMode() {
		verbose = !verbose;
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
				"Enter:\n1 to read a file\n2 to write a file\n3 to toggle output mode\n4 to toggle operation mode\n5 to shutdown");
		String request = sc.next();

		while (!request.equals("1") && !request.equals("2") && !request.equals("5")) {
			// request to toggle mode
			if (request.equals("3"))
				toggleMode(c);
			else if (request.equals("4"))
				toggleOperation(c);
			System.out.println(
					"Enter:\n1 to read a file\n2 to write a file\n3 to toggle output mode\n4 to toggle operation mode\n5 to shutdown");
			request = sc.next();
		}
		return request;
	}

	private static String queryFilename() {
		System.out.println("Enter a filename:");
		String filename = sc.next();

		return filename;
	}

	private static void shutdown() {
		System.out.println("Client is exiting");
		sendReceiveSocket.close();
		sc.close();
		System.exit(1);
	}
}
