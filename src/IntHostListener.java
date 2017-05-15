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
	
	public static int mode;
	public static int packetNum;
	public static Scanner sc;

	public IntHostListener() {
		receiveSocket = Helper.newSocket(Helper.DEFAULT_HOST_PORT);
	}

	
	public void receiveRequests() {
		System.out.println("Host waiting for requests...\n");
		receivePacket = Helper.newReceive(Helper.PACKAGE_SIZE);
		Helper.receive(receiveSocket, receivePacket);
		Utils.printDatagramContentWiresharkStyle(receivePacket);
		
		System.out.println("Creating new thread...\n");
		new Thread(new IntHostManager(receivePacket, mode, packetNum)).start();
	}	
	
	public static void main( String args[] ) {
		sc = new Scanner(System.in);
	    	mode = decideMode();
		if (mode != 0) {
			packetNum = decidePacketNum();
		}
		sc.close();
		
		IntHostListener s = new IntHostListener();
		while(true) {
			s.receiveRequests();
		}
	}
	
	
	public static int decideMode() {
		//Scanner sc = new Scanner(System.in);
		System.out.println("Type the digit number to choose which error you want to simulate");
		System.out.println("0 = No Error");
		System.out.println("1 = send to Client, using incorrect port (Error type 5)");
		System.out.println("2 = send to Server, using incorrect port (Error type 5)");
		System.out.println("3 = send to Client, Opcode is incorrect (Mess 0th & 1st byte, Error type 4)");
		System.out.println("4 = send to Server, Opcode is incorrect (Mess 0th & 1st byte, Error type 4)");
		System.out.println("5 = send to Client, Packet Size is incorrect (size = size+1, Error type 4)");
		System.out.println("6 = send to Server, Packet Size is incorrect (size = size+1, Error type 4)");
		System.out.println("7 = send to Client, BlockNum is incorrect (Mess 2nd & 3rd byte, Error type 4)");
		System.out.println("8 = send to Server, BlockNum is incorrect (Mess 2nd & 3rd byte, Error type 4)");
		String str = sc.next();
		int number =Integer.parseInt(str);
		while (number < 0 || number > 8) {//number is not 0,1,2,3
			str = sc.next();
			number =Integer.parseInt(str);
		}
		System.out.println("Will simulate the #" + number + " type error");
		return number;
	}

	public static int decidePacketNum() {
		//Scanner sc = new Scanner(System.in);
		System.out.println("Type the digit number to choose which packet you want to insert the error");
		String str = sc.next();
		int number =Integer.parseInt(str);
		while (number < 0) {
			str = sc.next();
			number =Integer.parseInt(str);
		}
		System.out.println("Will insert at the #" + number + " of packet");
		return number;
	}
}	
