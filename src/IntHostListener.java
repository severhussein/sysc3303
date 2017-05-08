import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.io.IOException;
//import java.util.Arrays;

public class IntHostListener {
	public static final int DEFAULT_LISTENER_PORT = 23;
	public static final int DEFAULT_SERVER_PORT = 69;
	public static final int PACKAGE_SIZE = 69;
	
	public static final boolean VERB_MODE = true;

	private DatagramSocket receiveSocket;
	private DatagramPacket receivePacket;

	public IntHostListener() {
	      try {
	          receiveSocket = new DatagramSocket(DEFAULT_LISTENER_PORT);
	       } catch (SocketException se) {
	          se.printStackTrace();
	          System.exit(1);
	       }
	}

	
	
	public void receiveRequests() {
		
		byte data[] = new byte[PACKAGE_SIZE];
		receivePacket = new DatagramPacket(data, data.length);
		try {
			receiveSocket.receive(receivePacket);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		printPacket(receivePacket);
		
		new Thread(new IntHostManager(receivePacket)).start();
	}
	
	
	
	public static void main( String args[] ) {
		IntHostListener s = new IntHostListener();
		while(true) {
			s.receiveRequests();
		}
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