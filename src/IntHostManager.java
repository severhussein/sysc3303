import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.io.IOException;

/**
 * IntHostManager for multi threads
 * need Socket.java
 * @author Dawei Chen 101020959
 */

public class IntHostManager implements Runnable {

	private DatagramSocket socket;
	private DatagramPacket sendPacket, receivePacket;
	int clientPort, serverPort;

	public IntHostManager(DatagramPacket receivePacket) {
		socket = Socket.newSocket();
		if (socket!= null) {
			//this.serverPort = IntHostListener.DEFAULT_SERVER_PORT;
			this.clientPort = receivePacket.getPort();
			this.receivePacket = receivePacket;
		}
	}

	
	public void run() {
		IntHostListener.print("Host sending request to server...\n");
		sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), IntHostListener.DEFAULT_SERVER_PORT);
		Socket.send(socket, sendPacket);
		IntHostListener.printPacket(sendPacket);
		
		IntHostListener.print("Host receiving from server...\n");
		Socket.receive(socket, receivePacket);
		serverPort = receivePacket.getPort();
		IntHostListener.printPacket(receivePacket);
		
		IntHostListener.print("Host sending to Client...\n");
		sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), clientPort);
		Socket.send(socket, sendPacket);
		IntHostListener.printPacket(sendPacket);
		
		while(true) {
			IntHostListener.print("Host waiting for client data...\n");
			Socket.receive(socket, receivePacket);
			IntHostListener.printPacket(receivePacket);
			
			IntHostListener.print("Host sending to server...\n");
			sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), serverPort);
			Socket.send(socket, sendPacket);
			IntHostListener.printPacket(sendPacket);
			
			IntHostListener.print("Host receiving from server...\n");
			Socket.receive(socket, receivePacket);
			serverPort = receivePacket.getPort();
			IntHostListener.printPacket(receivePacket);
			
			IntHostListener.print("Host sending to Client...\n");
			sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), clientPort);
			Socket.send(socket, sendPacket);
			IntHostListener.printPacket(sendPacket);
			
		}
	}
}

