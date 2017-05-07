import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class HostSender implements Runnable {
	public final int READ = 1, WRITE = 2, DATA = 3, ACKNOWLEDGE = 4, DATA_LENGTH = 512;

	private DatagramSocket socket;
	private DatagramPacket send, received;
	private int clientPort, serverPort;

	public HostSender(DatagramSocket socket, int clientPort, int serverPort) {
		try {
			this.socket = socket;
			socket.setSoTimeout(5000);
			this.clientPort = clientPort;
			this.serverPort = serverPort;
		} catch(SocketException e) {
			System.out.println(e.getMessage());
		}
	}

	public void run() {
		while(true) {
			byte datagram[] = new byte[516];
			received = new DatagramPacket(datagram, datagram.length);
System.out.println("Waiting for client data...\n");		
			// Receive datagram from client and forward to server.
			try {
				socket.receive(received);
			} catch(IOException e) {
				if(e instanceof SocketTimeoutException) return;
				System.out.println("ERROR RECEIVING DATA FROM CLIENT\n" + e.getMessage());
			}
System.out.println("Forwarding client data...\n");
			try {
				send = new DatagramPacket(received.getData(),
					received.getLength(),
					InetAddress.getLocalHost(),
					serverPort);
				socket.send(send);
			} catch(IOException e) {
				System.out.println("ERROR FORWARDING TO SERVER\n" + e.getMessage());
			}
System.out.println("Waiting for server data...\n");
			// Receive datagram from server and forward to client.
			try {
				socket.receive(received);
			} catch(IOException e) {
				if(e instanceof SocketTimeoutException) return; 
				System.out.println("ERROR RECEIVE DATA FROM SERVER\n" + e.getMessage());
			}
System.out.println("Forwarding server data...\n");
			try {
				send = new DatagramPacket(received.getData(),
					received.getLength(),
					InetAddress.getLocalHost(),
					clientPort);
				socket.send(send);
			} catch(IOException e) {
				System.out.println("ERROR FORWARDING TO CLIENT\n" + e.getMessage());
			}
		}
	}
}
