import java.net.DatagramPacket;
import java.net.DatagramSocket;
//import java.net.InetAddress;
import java.net.SocketException;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.BufferedInputStream;
//import java.io.BufferedOutputStream;
//import java.io.ByteArrayOutputStream;
import java.io.IOException;
//import java.util.Arrays;

public class IntHostManager implements Runnable {
	//public final int READ = 1, WRITE = 2, DATA = 3, ACKNOWLEDGE = 4, DATA_LENGTH = 512;

	private DatagramSocket sendSocket, sendReceiveSocket;
	private DatagramPacket sendPacket, receivePacket;
	int clientPort;

	public IntHostManager(DatagramPacket packet_from_client) {
	      try {
	    	  sendReceiveSocket = new DatagramSocket();
	    	  this.receivePacket = packet_from_client;
	    	  this.clientPort = packet_from_client.getPort();
	       } catch (SocketException se) {
	          se.printStackTrace();
	          System.exit(1);
	       }
	}

	
	
	public void run() {

		//int j;
		//int len = receivePacket.getLength();
		
		
		
			//step 1, send recieved data to server
			
	         sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), IntHostListener.DEFAULT_SERVER_PORT);
	         IntHostListener.printPacket(sendPacket);


	         // Send the datagram packet to the server via the send/receive socket.

	         try {
	            sendReceiveSocket.send(sendPacket);
	         } catch (IOException e) {
	            e.printStackTrace();
	            System.exit(1);
	         }
	         
	         
	         //step 2, recieve from server

	         byte[] data = new byte[IntHostListener.PACKAGE_SIZE];
	         receivePacket = new DatagramPacket(data, data.length);

	         System.out.println("Simulator: Waiting for packet.");
	         try {
	            // Block until a datagram is received via sendReceiveSocket.
	            sendReceiveSocket.receive(receivePacket);
	         } catch(IOException e) {
	            e.printStackTrace();
	            System.exit(1);
	         }
	         IntHostListener.printPacket(receivePacket);


	         //step 3, send back to client
	         sendPacket = new DatagramPacket(data, receivePacket.getLength(),
	                               receivePacket.getAddress(), clientPort);

	         IntHostListener.printPacket(sendPacket);
	         
	         // Send the datagram packet to the client via a new socket.

	         try {
	            // Construct a new datagram socket and bind it to any port
	            // on the local host machine. This socket will be used to
	            // send UDP Datagram packets.
	            sendSocket = new DatagramSocket();
	         } catch (SocketException se) {
	            se.printStackTrace();
	            System.exit(1);
	         }

	         try {
	            sendSocket.send(sendPacket);
	         } catch (IOException e) {
	            e.printStackTrace();
	            System.exit(1);
	         }

	         System.out.println("Simulator: packet sent using port " + sendSocket.getLocalPort());
	         System.out.println();

	         // We're finished with this socket, so close it.
	         sendSocket.close();
	         sendReceiveSocket.close();
	      } // end of loop

	}