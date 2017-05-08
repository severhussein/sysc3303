import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class Host {
	public final int DEFAULT_SERVER_PORT = 69;
	public final int DEFAULT_HOST_PORT = 23;

	private DatagramSocket sendSock, receiveSock;
	private DatagramPacket send, received;
	private int clientPort;
	
	public Host() {
		try {
			receiveSock = new DatagramSocket(DEFAULT_HOST_PORT);
			sendSock = new DatagramSocket();
			sendSock.setSoTimeout(3000);
		} catch(SocketException e) {
			System.out.println("RECEIVE SOCKET ERROR\n" + e.getMessage());
		}
	}

	public void forwardClient() {
		byte datagram[] = new byte[100];
		received = new DatagramPacket(datagram, datagram.length);

		try {
			receiveSock.receive(received);
		} catch(IOException e) {
			System.out.println("CLIENT RECEPTION ERROR\n" + e.getMessage());
		}
		
		System.out.println("Host receiving from client:\n\n" + new String(datagram, 0, datagram.length));
		System.out.println("\n" + Arrays.toString(datagram) + "\n");
		clientPort = received.getPort();

		try {
			send = new DatagramPacket(datagram, 
				datagram.length, 
				InetAddress.getLocalHost(), 
				DEFAULT_SERVER_PORT);
		} catch(IOException e) {
			System.out.println("CLIENT REPACK ERROR\n" + e.getMessage());
		}
		
		try {
			sendSock.send(send);
		} catch(IOException e) {
			System.out.println("CLIENT FORWARD ERROR\n" + e.getMessage());
		}
	}

	public void forwardServer() {
		byte datagram[] = new byte[100];
		received = new DatagramPacket(datagram, datagram.length);

		try {
			sendSock.receive(received);
		} catch(IOException e) {
			System.out.println("SERVER RECEPTION ERROR\n" + e.getMessage());
		}

		System.out.println("Host receiving from server:\n\n" + new String(datagram, 0, datagram.length));
		System.out.println("\n" + Arrays.toString(datagram) + "\n");

		try {
			send = new DatagramPacket(datagram,
				datagram.length,
				InetAddress.getLocalHost(),
				clientPort);
		} catch(IOException e) {
			System.out.println("SERVER REPACK ERROR\n" + e.getMessage());
		}

		try {
			sendSock.send(send);
		} catch(IOException e) {
			System.out.println("SERVER FORWARD ERROR\n" + e.getMessage());
		}
	}

	public static void main(String args[]) {
		Host h = new Host();
		while(true) {
			System.out.println("Waiting for client request.\n");
			h.forwardClient();
			System.out.println("Forwarded client to server. Waiting for server response.\n");
			h.forwardServer();
			System.out.println("Forwarded server to client.\n");
		}
	}
}
