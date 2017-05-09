import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.io.IOException;
//import java.util.Arrays;

/**
 * IntHostListener to run the Host
 * need Helper.java
 * @author Dawei Chen 101020959
 */

public class IntHostListener {


	private DatagramSocket receiveSocket;
	private DatagramPacket receivePacket;

	public IntHostListener() {
		receiveSocket = Helper.newSocket(Helper.DEFAULT_HOST_PORT);
	}

	
	public void receiveRequests() {
		Helper.print("Host waiting for requests...\n");
		receivePacket = Helper.newReceive(Helper.PACKAGE_SIZE);
		Helper.receive(receiveSocket, receivePacket);
		Helper.printPacket(receivePacket);
		
		new Thread(new IntHostManager(receivePacket)).start();
	}	
	
	public static void main( String args[] ) {
		IntHostListener s = new IntHostListener();
		while(true) {
			s.receiveRequests();
		}
	}
		

}	