import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.io.IOException;
//import java.util.Arrays;

/**
 * IntHostListener to run the Host
 * need Socket.java
 * @author Dawei Chen 101020959
 */

public class IntHostListener {
	public static final int DEFAULT_HOST_PORT = 23;
	public static final int DEFAULT_SERVER_PORT = 69;
	public static final int PACKAGE_SIZE = 1000;
	public static final boolean VERB_MODE = true;

	private DatagramSocket receiveSocket;
	private DatagramPacket receivePacket;

	public IntHostListener() {
		receiveSocket = Socket.newSocket(DEFAULT_HOST_PORT);
	}

	
	public void receiveRequests() {
		print("Host waiting for requests...\n");
		receivePacket = Socket.newReceive(PACKAGE_SIZE);
		Socket.receive(receiveSocket, receivePacket);
		printPacket(receivePacket);
		
		new Thread(new IntHostManager(receivePacket)).start();
	}	
	
	public static void main( String args[] ) {
		IntHostListener s = new IntHostListener();
		while(true) {
			s.receiveRequests();
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