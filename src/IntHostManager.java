import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.io.IOException;

/**
 * IntHostManager for multi threads need Helper.java
 * 
 * @author Dawei Chen 101020959
 */

public class IntHostManager implements Runnable {

	private DatagramSocket socket;
	private DatagramPacket sendPacket, receivePacket;
	int clientPort, serverPort;
	int mode, packetNum;

	public IntHostManager(DatagramPacket receivePacket, int mode, int packetNum) {
		socket = Helper.newSocket();
		if (socket != null) {
			this.serverPort = Helper.DEFAULT_SERVER_PORT;
			this.clientPort = receivePacket.getPort();
			this.receivePacket = receivePacket;
			this.mode = mode;
			this.packetNum = packetNum;
		}
	}

	public void run() {

		boolean server_port_needed = true;

		// create error on the 2nd pass of the file transfer
		int i = 0;
		while (true) {

			if (i == packetNum && mode == 2) {
				simulate_wrong_port(serverPort);// 1 is to server
			}
			if (i == packetNum && mode == 4) {
				simulate_wrong_opcode(serverPort);// 1 is to server
			} else if (i == packetNum && mode == 6) {
				simulate_wrong_size(serverPort);// 1 is to server
			} else if (i == packetNum && mode == 8) {
				simulate_wrong_blockNum(serverPort);// 1 is to server
			} else {
				System.out.println("\nHost sending to server...\n");
				sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(),
						receivePacket.getAddress(), serverPort);
				Helper.send(socket, sendPacket);
				Utils.printDatagramContentWiresharkStyle(sendPacket);
			}

			i++;// first round of request msg was done, increase i here

			System.out.println("Host receiving from server...\n");
			Helper.receive(socket, receivePacket);
			Utils.printDatagramContentWiresharkStyle(receivePacket);
			if (server_port_needed) {
				server_port_needed = false;
				serverPort = receivePacket.getPort();
			} // get server port here!

			if (i == packetNum && mode == 1) {
				simulate_wrong_port(clientPort);// to client
			}
			if (i == packetNum && mode == 3) {
				simulate_wrong_opcode(clientPort);// to client
			} else if (i == packetNum && mode == 5) {
				simulate_wrong_size(clientPort);// to client
			} else if (i == packetNum && mode == 7) {
				simulate_wrong_blockNum(clientPort);// to client
			} else {
				System.out.println("Host sending to Client...\n");
				sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(),
						receivePacket.getAddress(), clientPort);
				Helper.send(socket, sendPacket);
				Utils.printDatagramContentWiresharkStyle(sendPacket);
			}

			System.out.println("Host waiting for client data...\n");
			// receiving and placing it into same receive packet as above, might
			// have remnants from old data
			// Helper.receive(socket, receivePacket);

			receivePacket = Helper.newReceive();
			Helper.receive(socket, receivePacket);
			Utils.printDatagramContentWiresharkStyle(receivePacket);
		}
	}

	public void simulate_wrong_port(int port) {

		DatagramSocket new_socket = Helper.newSocket();

		System.out.println("simulate wrong port packet to port: " + port + "\n");
		sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(),
				port);
		Helper.send(new_socket, sendPacket);
		Utils.printDatagramContentWiresharkStyle(sendPacket);

	}

	public void simulate_wrong_opcode(int port) {

		System.out.println("simulate wrong opcode to port: " + port + "\n");
		receivePacket.getData()[0] = (byte) 255;
		receivePacket.getData()[1] = (byte) 255;
		sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(),
				port);
		Helper.send(socket, sendPacket);
		Utils.printDatagramContentWiresharkStyle(sendPacket);
	}

	public void simulate_wrong_size(int port) {

		System.out.println("simulate wrong size packet to port: " + port + "\n");
		sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength() + 1 , receivePacket.getAddress(),
				port);
		Helper.send(socket, sendPacket);
		Utils.printDatagramContentWiresharkStyle(sendPacket);
	}
	
	public void simulate_wrong_blockNum(int port) {

		System.out.println("simulate wrong size packet to port: " + port + "\n");
		receivePacket.getData()[2] = (byte) 255;
		receivePacket.getData()[3] = (byte) 255;
		sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength() , receivePacket.getAddress(),
				port);
		Helper.send(socket, sendPacket);
		Utils.printDatagramContentWiresharkStyle(sendPacket);
	}
	
}
