import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.io.IOException;

/**
 * Helper functions to save a bit of time
 * @author Dawei Chen 101020959
 */


public class Helper {

	public static boolean VERB_MODE = true;
	public static final int DEFAULT_HOST_PORT = 23, DEFAULT_SERVER_PORT = 69, PACKAGE_SIZE = 1000;
	
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
	
	
	
	
	
	
	//
	//printing helper functions
	//
	public static void print(String str){
		if (VERB_MODE) System.out.println(str);
	}
	
	public static void printPacket(DatagramPacket receivePacket){
		if (!VERB_MODE) return;

	    // Process the received datagram.
	    //System.out.println("Simulator: Packet received:");
		System.out.println("<Packet>");
	    System.out.println("From host: " + receivePacket.getAddress());
	    System.out.println("Host port: " + receivePacket.getPort());
	    int len = receivePacket.getLength();
	    System.out.println("Length: " + len);
	    System.out.println("Containing: " );
	    
	    // print the bytes
	    System.out.print("Byte: ");
	    for (int j=0;j<len;j++) {
	    	System.out.print(receivePacket.getData()[j] + ", ");
	       //System.out.print("byte " + j + " " + data[j]);
	    }
	    System.out.println();
	    
	    // print the Strings
        String received = new String(receivePacket.getData(),0,len);
        System.out.println("String: " + received);
        
	    System.out.println();
	    System.out.println();
	}
}
