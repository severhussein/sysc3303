import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * IntHostManager for multi threads need ErrorSimulatorHelper.java
 * @author Dawei Chen 101020959
 */

public class ErrorSimulatorThread implements Runnable {

	private DatagramSocket socket;
	private DatagramPacket sendPacket, receivePacket;
	int clientPort, serverPort;
	int mode, packetNum, errorSize;
	int[] userChoice;

	public ErrorSimulatorThread(DatagramPacket receivePacket, int[] userChoice) {
		socket = ErrorSimulatorHelper.newSocket();
		if (socket != null) {
			this.serverPort = ErrorSimulator.DEFAULT_SERVER_PORT;
			this.clientPort = receivePacket.getPort();
			this.receivePacket = receivePacket;
			
			this.userChoice = userChoice;
			this.mode = userChoice[0];
			this.packetNum = userChoice[1];
			this.errorSize = userChoice[2];
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
				ErrorSimulatorHelper.send(socket, sendPacket);
				//clean printing//Utils.tryPrintTftpPacket(sendPacket);
			}

			i++;// first round of request msg was done, increase i here

			System.out.println("Host receiving from server...\n");
			receivePacket = ErrorSimulatorHelper.newReceive();
			ErrorSimulatorHelper.receive(socket, receivePacket);//form a new packet
			//clean printing//Utils.tryPrintTftpPacket(receivePacket);
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
				ErrorSimulatorHelper.send(socket, sendPacket);
				//clean printing//Utils.tryPrintTftpPacket(sendPacket);
			}

			System.out.println("Host waiting for client data...\n");
			// receiving and placing it into same receive packet as above, might
			// have remnants from old data
			// ErrorSimulatorHelper.receive(socket, receivePacket);

			receivePacket = ErrorSimulatorHelper.newReceive();
			ErrorSimulatorHelper.receive(socket, receivePacket);
			//clean printing//Utils.tryPrintTftpPacket(receivePacket);
		}
	}

	public void simulate_wrong_port(int port) {

		DatagramSocket new_socket = ErrorSimulatorHelper.newSocket();

		System.out.println("simulate wrong port packet to port: " + port + "\n");
		sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(),
				port);
		ErrorSimulatorHelper.send(new_socket, sendPacket);
		//clean printing//Utils.tryPrintTftpPacket(sendPacket);

	}

	public void simulate_wrong_opcode(int port) {

		System.out.println("simulate wrong opcode to port: " + port + "\n");
		receivePacket.getData()[0] = (byte) 255;
		receivePacket.getData()[1] = (byte) 255;
		sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(),
				port);
		ErrorSimulatorHelper.send(socket, sendPacket);
		//clean printing//Utils.tryPrintTftpPacket(sendPacket);
	}

	public void simulate_wrong_size(int port) {

		System.out.println("simulate wrong size packet to port: " + port + "\n");
		sendPacket = new DatagramPacket(receivePacket.getData(), errorSize , receivePacket.getAddress(),
				port);
		ErrorSimulatorHelper.send(socket, sendPacket);
		//clean printing//Utils.tryPrintTftpPacket(sendPacket);
	}
	
	public void simulate_wrong_blockNum(int port) {

		System.out.println("simulate wrong size packet to port: " + port + "\n");
		receivePacket.getData()[2] = (byte) 255;
		receivePacket.getData()[3] = (byte) 255;
		sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength() , receivePacket.getAddress(),
				port);
		ErrorSimulatorHelper.send(socket, sendPacket);
		//clean printing//Utils.tryPrintTftpPacket(sendPacket);
	}
	
}
