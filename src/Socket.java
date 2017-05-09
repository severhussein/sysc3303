import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.io.IOException;

/**
 * Helper functions to save a bit of time
 * @author Dawei Chen 101020959
 */

public class Socket {
	
	//using by calling: DatagramSocket socket = Socket.newSocket();
	public static DatagramSocket newSocket() {
		try {
			DatagramSocket socket = new DatagramSocket();
			//socket.setSoTimeout(5000);
			return socket;
		} catch(SocketException e) {
			System.out.println(e.getMessage());
		}
		return null;
	}
	//using by calling: DatagramSocket recieveSocket = Socket.newSocket(69);
	public static DatagramSocket newSocket(int port) {
		try {
			DatagramSocket socket = new DatagramSocket(port);
			//socket.setSoTimeout(5000);
			return socket;
		} catch(SocketException e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	
	public static DatagramPacket newReceive() {
			byte datagram[] = new byte[1000];
			return new DatagramPacket(datagram, datagram.length);
	}
	public static DatagramPacket newReceive(int size) {
			byte datagram[] = new byte[size];
			return new DatagramPacket(datagram, datagram.length);
	}
	
	
	//Socket.receive(socket, packet);
	public static void receive(DatagramSocket socket, DatagramPacket received) {
			try {
				socket.receive(received);
			} catch(IOException e) {
				//if(e instanceof SocketTimeoutException) return;
				System.out.println(e.getMessage());
			}
	}
	
	//Socket.send(socket, packet);
	public static void send(DatagramSocket socket, DatagramPacket send) {
			try {
				socket.send(send);
			} catch(IOException e) {
				System.out.println(e.getMessage());
			}
	}
}
