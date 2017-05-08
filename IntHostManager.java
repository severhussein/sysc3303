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
	          //System.exit(1);
	       }
	}

	public void run() {

			int j;
			//InetAddress clientAddress = receivePacket.getAddress();
			//byte[] data;
			//data = receivePacket.getData();
			
			int len = receivePacket.getLength();
	         sendPacket = new DatagramPacket(receivePacket.getData(), len, receivePacket.getAddress(), 69);
	        
	         System.out.println("Simulator: sending packet.");
	         System.out.println("To host: " + sendPacket.getAddress());
	         System.out.println("Destination host port: " + sendPacket.getPort());
	         len = sendPacket.getLength();
	         System.out.println("Length: " + len);
	         System.out.println("Containing: ");
	         for (j=0;j<len;j++) {
	             System.out.println("byte " + j + " " + receivePacket.getData()[j]);
	         }

	         // Send the datagram packet to the server via the send/receive socket.

	         try {
	            sendReceiveSocket.send(sendPacket);
	         } catch (IOException e) {
	            e.printStackTrace();
	            System.exit(1);
	         }
	         
	         // Construct a DatagramPacket for receiving packets up
	         // to 100 bytes long (the length of the byte array).

	         byte[] data = new byte[1000];
	         receivePacket = new DatagramPacket(data, data.length);

	         System.out.println("Simulator: Waiting for packet.");
	         try {
	            // Block until a datagram is received via sendReceiveSocket.
	            sendReceiveSocket.receive(receivePacket);
	         } catch(IOException e) {
	            e.printStackTrace();
	            System.exit(1);
	         }

	         // Process the received datagram.
	         System.out.println("Simulator: Packet received:");
	         System.out.println("From host: " + receivePacket.getAddress());
	         System.out.println("Host port: " + receivePacket.getPort());
	         len = receivePacket.getLength();
	         System.out.println("Length: " + len);
	         System.out.println("Containing: ");
	         for (j=0;j<len;j++) {
	            System.out.print("byte " + j + " " + data[j]);
	         }
	         System.out.println();

	         // Construct a datagram packet that is to be sent to a specified port
	         // on a specified host.
	         // The arguments are:
	         //  data - the packet data (a byte array). This is the response.
	         //  receivePacket.getLength() - the length of the packet data.
	         //     This is the length of the msg we just created.
	         //  receivePacket.getAddress() - the Internet address of the
	         //     destination host. Since we want to send a packet back to the
	         //     client, we extract the address of the machine where the
	         //     client is running from the datagram that was sent to us by
	         //     the client.
	         //  receivePacket.getPort() - the destination port number on the
	         //     destination host where the client is running. The client
	         //     sends and receives datagrams through the same socket/port,
	         //     so we extract the port that the client used to send us the
	         //     datagram, and use that as the destination port for the TFTP
	         //     packet.

	         sendPacket = new DatagramPacket(data, receivePacket.getLength(),
	                               receivePacket.getAddress(), clientPort);

	         System.out.println( "Simulator: Sending packet:");
	         System.out.println("To host: " + sendPacket.getAddress());
	         System.out.println("Destination host port: " + sendPacket.getPort());
	         len = sendPacket.getLength();
	         System.out.println("Length: " + len);
	         System.out.println("Containing: ");
	         for (j=0;j<len;j++) {
		            System.out.print("byte " + j + " " + data[j]);
		         }
		     System.out.println();

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
	         //sendSocket.close();
	      } // end of loop

	}