import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.io.IOException;

/**
 * Helper functions to save a bit of time
 * @author Dawei Chen 101020959
 */


public class ErrorSimulatorHelper {

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
	
	public static DatagramSocket newSocket(int port, InetAddress laddr) {
		try {
			DatagramSocket socket = new DatagramSocket(port, laddr);
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
	
	
	
	
	
	
	//
	//printing helper functions
	//not needed since this is used in intermediate host, where inthost is the error simulator,
	//thus you always want to be printing statements (ie, no verbose mode in error simulator)
	
	public static void print(String str){
		if (ErrorSimulator.PRINT_PACKET) System.out.println(str);
	}
	
	public static void printPacket(DatagramPacket receivePacket){
		if (ErrorSimulator.PRINT_PACKET) {
		    // Process the received datagram.
		    //System.out.println("Simulator: Packet received:");
			/*
			System.out.println("<Packet>");
		    System.out.println("From host: " + receivePacket.getAddress());
		    System.out.println("Host port: " + receivePacket.getPort());
		    */
		    int len = receivePacket.getLength();
		    System.out.println("Length: " + len);	    
		    // print the bytes
		    System.out.println("Bytes:"  + Arrays.toString(Arrays.copyOfRange(receivePacket.getData(),0,len)));
		    // print the Strings
	        String received = new String(receivePacket.getData(),0,len);
	        System.out.println("String:" + received);
		    System.out.println();
		}
	}
}
