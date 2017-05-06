import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.InetAddress;
import java.io.IOException;
import java.util.Arrays;

public class HostListener {
	public final int DEFAULT_HOST_PORT = 23, DEFAULT_SERVER_PORT = 69, READ = 1, WRITE = 2;

	private DatagramSocket receiveSock, sendSock;
	private DatagramPacket send, received;
	private int clientPort, serverPort;

	public HostListener() {
		try {
			receiveSock = new DatagramSocket(DEFAULT_HOST_PORT);
		} catch(SocketException e) {
			System.out.println(e.getMessage());
		}
	}

	public void receiveRequests() throws InvalidPacketException {
		try {
			sendSock = new DatagramSocket();
		} catch(SocketException e) {
			System.out.println(e.getMessage());
		}

		byte datagram[] = new byte[516];
		received = new DatagramPacket(datagram, datagram.length);

		// Receive initial request from client.
		try {
			receiveSock.receive(received);
		} catch(IOException e) {
			System.out.println("HOST RECEPTION ERROR\n" + e.getMessage());
		}
		
		clientPort = received.getPort();
		
		// Create new forward packet and send to server. Then wait for response.
		try {
			send = new DatagramPacket(received.getData(),
				received.getLength(),
				InetAddress.getLocalHost(),
				DEFAULT_SERVER_PORT);
			sendSock.send(send);
		} catch(IOException e) {
			System.out.println("ERROR CREATING CLIENT FORWARD PACKET" + e.getMessage());
		}

		try {
			sendSock.receive(received);
		} catch(IOException e) {
			System.out.println("ERROR RECEIVING RESPONSE FROM SERVER" + e.getMessage());
		}

		serverPort = received.getPort();

		// Create new forward packet and send to client. Then create new manager thread.
		try {
			send = new DatagramPacket(received.getData(),
				received.getLength(),
				InetAddress.getLocalHost(),
				clientPort);
		} catch(IOException e) {
			System.out.println("ERROR CREATING SERVER FORWARD PACKET" + e.getMessage());
		}

		new Thread(new HostSender(sendSock, clientPort, serverPort)).start();
	}

	public static void main(String args[]) {
		HostListener h = new HostListener();
		while(true) {
			h.receiveRequests();
		}
	}
}	
