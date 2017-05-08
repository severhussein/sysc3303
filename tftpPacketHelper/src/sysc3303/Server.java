package sysc3303;

import java.io.*;
import java.net.*;

import sysc3303.CommonConstants;

/**
 * Server component of SYSC3303 assignment 1, based on the simple echo program provided in class
 * 
 * @author Yu-Kai Yang 100786472
 *
 */
public class Server {

	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket sendSocket, receiveSocket;
	private TftpPacket requestPacket;

	public Server()
	{
		try {
			receiveSocket = new DatagramSocket(CommonConstants.SERVER_LISTEN_PORT);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		} 
	}

	/**
	 * Receive and reply to request packet forever unless something goes wrong 
	 */
	public void receiveAndReply()
	{

		while (true) {

			byte data[] = new byte[CommonConstants.PACKET_BUFFER_SIZE];
			receivePacket = new DatagramPacket(data, data.length);
			System.out.println("Server: Waiting for request.\n");

			// Block until a datagram packet is received from receiveSocket.
			try {        
				System.out.println("Waiting for client..."); // so we know we're waiting
				receiveSocket.receive(receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

			// Process the received datagram.
			System.out.println("Server: Packet received:");
			Utils.printPacketContent(receivePacket);

			try {
				sendSocket = new DatagramSocket();
			} catch (SocketException e1) {
				e1.printStackTrace();
				System.exit(1);
			}
			try {  
				requestPacket = TftpPacket.decodeTftpPacket(receivePacket);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				System.exit(1);
			}
			if (requestPacket.getType() == TftpPacket.TftpType.REQUEST_READ) {
				sendPacket = new DatagramPacket(CommonConstants.READ_RESPONSE_BYTES, 
						CommonConstants.READ_RESPONSE_BYTES.length, 
						receivePacket.getAddress(), receivePacket.getPort());
			} else {
				sendPacket = new DatagramPacket(CommonConstants.WRITE_RESPONSE_BYTES, 
						CommonConstants.WRITE_RESPONSE_BYTES.length, 
						receivePacket.getAddress(), receivePacket.getPort());
			}
			
			System.out.println("Server: Sending packet:");
			Utils.printPacketContent(sendPacket);

			// Send the datagram packet to the client via the send socket. 
			try {
				sendSocket.send(sendPacket);
				sendSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

			System.out.println("Server: packet sent");
		}
	}

	public static void main( String args[] )
	{
		Server c = new Server();
		c.receiveAndReply();
	}
}
