import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class Server {
	public final int DEFAULT_SERVER_PORT = 69;

	private DatagramSocket sendSock, receiveSock;
	private DatagramPacket send, received;

	public Server() {
		try {
			receiveSock = new DatagramSocket(DEFAULT_SERVER_PORT);
		} catch(SocketException e) {
			System.out.println(e.getMessage());
		}
	}

	public void serviceRequest() throws InvalidPacketException {
		byte datagram[] = new byte[100];
		received = new DatagramPacket(datagram, datagram.length);

		try {
			receiveSock.receive(received);
		} catch(IOException e) {
			System.out.println("HOST RECEPTION ERROR\n" + e.getMessage());
		}

		String packet = new String(datagram, 0, datagram.length);

		System.out.println("Server received:\n\n" + packet);
		System.out.println("\n" + Arrays.toString(datagram) + "\n");

		String strArr[] = packet.split("\0");
		if(strArr.length != 3) throw new InvalidPacketException("Packet does not meet standard format.");

		byte response[] = {0, 0, 0, 0};
		if(datagram[1] == 1) {
			response[1] = 3;
			response[3] = 1;
		} else if(datagram[1] == 2) {
			response[1] = 4;
		}

		try {
			sendSock = new DatagramSocket();
		} catch(SocketException e) {
			System.out.println("ERROR CREATING RESPONSE SOCKET\n" + e.getMessage());
		}

		try {
			send = new DatagramPacket(response,
				response.length,
				InetAddress.getLocalHost(),
				received.getPort());
		} catch(UnknownHostException e ) {
			System.out.println("ERROR CREATING PACKET");
		}

		System.out.println("Server created packet:\n\n" + new String(response, 0, response.length));
		System.out.println("\n" + Arrays.toString(response) + "\n");

		try {
			sendSock.send(send);
		} catch(IOException e) {
			System.out.println("ERROR SENDING RESONSE\n" + e.getMessage());
		}

		sendSock.close();
	}

	public static void main(String args[]) {
		Server s = new Server();
		while(true) {
			System.out.println("Server is waiting for requests.\n");
			try {
				s.serviceRequest();
			} catch(InvalidPacketException e) {
				System.out.println(e.getMessage());
			}
			System.out.println("Request was served.\n");
		}
	}
}
