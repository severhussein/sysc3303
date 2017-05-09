import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.io.IOException;

/**
 * IntHostManager for multi threads
 * need Helper.java
 * @author Dawei Chen 101020959
 */

public class IntHostManager implements Runnable {

	private DatagramSocket socket;
	private DatagramPacket sendPacket, receivePacket;
	int clientPort, serverPort;

	public IntHostManager(DatagramPacket receivePacket) {
		socket = Helper.newSocket();
		if (socket!= null) {
			this.serverPort = Helper.DEFAULT_SERVER_PORT;
			this.clientPort = receivePacket.getPort();
			this.receivePacket = receivePacket;
		}
	}

	
	public void run() {
		boolean server_port_needed = true;
		
		while(true) {
			Helper.print("Host sending to server...\n");
			sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), serverPort);
			Helper.send(socket, sendPacket);
			Helper.printPacket(sendPacket);
			
			Helper.print("Host receiving from server...\n");
			Helper.receive(socket, receivePacket);
			Helper.printPacket(receivePacket);
			if (server_port_needed) {server_port_needed = false; serverPort = receivePacket.getPort();}//get server port here!
			
			Helper.print("Host sending to Client...\n");
			sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), clientPort);
			Helper.send(socket, sendPacket);
			Helper.printPacket(sendPacket);
			
			Helper.print("Host waiting for client data...\n");
			Helper.receive(socket, receivePacket);
			Helper.printPacket(receivePacket);			
		}
		
	}
}

