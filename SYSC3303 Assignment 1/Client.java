package sysc3303;

/**
 * Client component of SYSC3303 assignment 1, based on the simple echo program provided in class
 * 
 * @author Yu-Kai Yang 100786472
 *
 */
import java.io.*;
import java.net.*;
import java.util.Scanner;

import sysc3303.RequestPacket;
import sysc3303.Utils;

public class Client {

	private final static int DATA = 1;
	private final static int MAX_DATA_LENGTH = 512;
	private final static int HOST_PORT = 23;
	private final static int ACKNOWLEDGE = 4;
	private final static int ACKNOWLEDGE_PACKAGE_SIZE = 4;
	private final static int MAX_DATA_PACKET_SIZE = 516;
	byte[] ack = { 0, 4, 0, 0 };

	private DatagramPacket sendPacket, receivePacket;
	private static DatagramSocket sendReceiveSocket;

	public Client() {
		try {
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException se) { // Can't create the socket.
			se.printStackTrace();
			System.exit(1);
		}
	}

	private void writeToHost(String fileName) throws IOException {

		//BEFORE WRITING TO HOST, MAKE SURE TO RECEIVE ACKNOWLEDGE BLK#0
		// Construct a DatagramPacket for receiving packets
		
		byte data[] = new byte[ACKNOWLEDGE_PACKAGE_SIZE];
		receivePacket = new DatagramPacket(data, data.length);

		// wait for a packet to be returned back
		try {
			// Block until a datagram is received via sendReceiveSocket.
			sendReceiveSocket.receive(receivePacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		//have not done any error checking on receivepacket ack blk#0 
		
		byte readData[] = new byte[MAX_DATA_LENGTH], ack[] = new byte[ACKNOWLEDGE_PACKAGE_SIZE];
		short blockNumber = 1;
		int i = 0;
		BufferedInputStream in = null;

		try {
			in = new BufferedInputStream(new FileInputStream(fileName));
		} catch (IOException e) {
			System.out.println("ERROR OPENING FILE\n" + e.getMessage());
		}

		try {
			while ((i = in.read(readData)) != -1) {
				ByteArrayOutputStream buf = new ByteArrayOutputStream();
				buf.write(0);
				buf.write(3);
				// write block number
				buf.write(blockNumber >> 8);
				buf.write(blockNumber);// test me

				buf.write(readData, 0, i); // alex check

				byte dataSend[] = buf.toByteArray();
				try {
					sendPacket = new DatagramPacket(dataSend, dataSend.length,
							InetAddress.getLocalHost(), HOST_PORT);
					sendReceiveSocket.send(sendPacket);
				} catch (IOException e) {
					System.out.println("ERROR SENDING READ\n" + e.getMessage());
				}
				Utils.printPacketContent(sendPacket);

				//receive the ack
				receivePacket = new DatagramPacket(ack, ack.length);
				try {
					sendReceiveSocket.receive(receivePacket);
				} catch (IOException e) {
					System.out.println("RECEPTION ERROR AT MANAGER ACK\n"
							+ e.getMessage());
				}

				// Check acknowledge packet, before continuing.
				// Right now, does not throw exception nor
				// requests re-transmission.
				// Just prints to console.
				int block = ((ack[2] & 0xFF) >> 8) | (ack[3] & 0xFF);
				if (ack[1] != ACKNOWLEDGE && block != i) {
					System.out.println("ACK BLOCK DOES NOT MATCH\n");
				}
				
				//increment block#
				blockNumber++;
			}
		} catch (IOException e) {
			System.out.println("ERROR READING FILE\n" + e.getMessage());
		}
	}

	private void readFromHost(String fileName) throws IOException {

		boolean endOfFile = false;
		byte[] writeData = new byte[MAX_DATA_PACKET_SIZE]; 
		BufferedOutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(fileName));
		} catch (IOException e) {
			System.out.println("ERROR CREATING FILE\n" + e.getMessage());
		}
		while (!endOfFile) {

			receivePacket = new DatagramPacket(writeData, writeData.length);
			try {
				sendReceiveSocket.receive(receivePacket);
			} catch (IOException e) {
				System.out.println("HOST RECEPTION ERROR\n" + e.getMessage());
			}

			if (writeData[1] == DATA) {

				try {
					out.write(writeData, 4, receivePacket.getLength() - 4);
				} catch (IOException e) {
					System.out.println("ERROR WRITING TO FILE\n"
							+ e.getMessage());
				}

				ack[2] = writeData[2];
				ack[3] = writeData[3];

				try {
					sendPacket = new DatagramPacket(ack, ack.length,
							InetAddress.getLocalHost(), receivePacket.getPort());
					sendReceiveSocket.send(sendPacket);
				} catch (IOException e) {
					System.out.println("ERROR SENDING ACK\n" + e.getMessage());
				}
			}

			if (receivePacket.getLength() < MAX_DATA_LENGTH)
				endOfFile = false;
		}
		out.close();
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
		try {
			sendPacket = new DatagramPacket(payload, payload.length,
					InetAddress.getLocalHost(),
					CommonConstants.HOST_LISTEN_PORT);
			// InetAddress.getByName("192.168.1.1"),
			// CommonConstants.HOST_LISTEN_PORT);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println("Client: Sending packet:");
		Utils.printPacketContent(sendPacket);

		try {
			sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("Client: Packet sent.\n");
	}

	public static void main(String args[]) throws IllegalArgumentException,
			IOException {
		Client c = new Client();
		String mode = "quiet";
		// assumption is to have it in quiet mode

		// query user for RRQ or WRQ or toggle between modes
		System.out.println("Client: currently in " + mode + " mode");
		String request = queryUserRequest(mode);
		String filename = queryFilename();

		System.out.println("Filename: " + filename);
		if (request.equals("1")) {
			// read request
			System.out.println("Read request");
			// create a file
			// read file put it in
			byte[] data = new RequestPacket(
					RequestPacket.RequestType.REQUEST_READ, filename,
					RequestPacket.Mode.MODE_ASCII).generatePayloadArray();

			c.sendRequest(data);
			c.readFromHost(filename);

		} else {
			// write request
			System.out.println("write request");
			byte[] data = new RequestPacket(
					RequestPacket.RequestType.REQUEST_WRTIE, filename,
					RequestPacket.Mode.MODE_ASCII).generatePayloadArray();
			c.sendRequest(data);
			c.writeToHost(filename);
			//
		}
		sendReceiveSocket.close();

	}

	private static String toggleMode(String mode) {
		if (mode.equals("verbose"))
			mode = "quiet";
		else
			mode = "verbose";

		System.out.println("Mode changed to: " + mode);
		return mode;
	}

	private static String queryUserRequest(String mode) {
		// start a scanner
		Scanner sc = new Scanner(System.in);

		// ask user for input
		System.out
				.println("Enter:\n1 to read a file\n2 to write a file\n3 to toggle mode");
		String request = sc.next();

		while (!request.equals("1") && !request.equals("2")) {
			// request to toggle mode
			if (request.equals("3")) {
				mode = toggleMode(mode);
			}
			System.out
					.println("Enter:\n1 to read a file\n2 to write a file\n3 to toggle mode");
			request = sc.next();
		}
		// sc.close();
		return request;
	}

	private static String queryFilename() {
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter a filename:");
		String filename = sc.next();

		// sc.close();

		return filename;
	}
}