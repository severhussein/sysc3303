import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.io.IOException;
import java.util.Scanner;
//import java.util.Arrays;

/**
 * IntHostListener to run the Host
 * need Helper.java
 * @author Dawei Chen 101020959
 */

public class IntHostListener {


	private DatagramSocket receiveSocket;
	private DatagramPacket receivePacket;
	
	public static int mode = -1;

	public IntHostListener() {
	    mode = decideMode();
		receiveSocket = Helper.newSocket(Helper.DEFAULT_HOST_PORT);
	}

	
	public void receiveRequests() {
		System.out.println("Host waiting for requests...\n");
		receivePacket = Helper.newReceive(Helper.PACKAGE_SIZE);
		Helper.receive(receiveSocket, receivePacket);
		Utils.printDatagramContentWiresharkStyle(receivePacket);
		
		System.out.println("Creating new thread...\n");
		new Thread(new IntHostManager(receivePacket, mode, 2)).start();
	}	
	
	public static void main( String args[] ) {
		IntHostListener s = new IntHostListener();
		while(true) {
			s.receiveRequests();
		}
	}
	
	
	public int decideMode() {
		Scanner sc = new Scanner(System.in);
		System.out.println("Type the digit number to choose which error you want to simulate");
		System.out.println("0 = No Error");
		System.out.println("1 = Error type 5 packet send to Client");
		System.out.println("2 = Error type 5 packet send to Sever");
		System.out.println("3 = Error type 4 packet send to Client");
		System.out.println("4 = Error type 4 packet send to Sever");
		String str = sc.next();
		int number =Integer.parseInt(str);
		while (number < 0 || number > 4) {//number is not 0,1,2,3
			str = sc.next();
		}
		System.out.println("Will simulate the #" + number + " type error.");
		sc.close();
		return number;
	}

}	
