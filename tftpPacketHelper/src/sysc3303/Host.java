package sysc3303;

import java.io.*;
import java.net.*;

import sysc3303.CommonConstants;

/**
 * Host component of SYSC3303 assignment 1, based on the simple echo program provided in class
 * 
 * @author Yu-Kai Yang 100786472
 *
 */
public class Host {

	private DatagramPacket responsePacket, requestPacket, forwardPacket;
	private DatagramSocket sendReceiveSocket, receiveSocket, sendSocket;

	public Host()
	{
		try {
			receiveSocket = new DatagramSocket(CommonConstants.HOST_LISTEN_PORT);
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		} 
	}

	/**
	 * Receive and forward the packet (Client <-> Host <-> Server)
	 */
	public void receiveAndForward()
	{

		while (true){

			byte data[] = new byte[CommonConstants.PACKET_BUFFER_SIZE];
			requestPacket = new DatagramPacket(data, data.length);
			System.out.println("Host: Waiting for Packet.\n");

			// Block until a datagram packet is received from receiveSocket.
			try {        
				System.out.println("Waiting for client..."); // so we know we're waiting
				receiveSocket.receive(requestPacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

			// forward the received datagram to server.
			System.out.println("Host: Packet received from client:");
			Utils.printPacketContent(requestPacket);
			try {
				forwardPacket = new DatagramPacket(data, requestPacket.getLength(),
						InetAddress.getLocalHost(), CommonConstants.SERVER_LISTEN_PORT);
			} catch (UnknownHostException uhe) {
				uhe.printStackTrace();
				System.exit(1);
			}

			System.out.println("Host: Forwarding packet:");
			Utils.printPacketContent(forwardPacket); 
			try {
				sendReceiveSocket.send(forwardPacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

			System.out.println("Host: request packet forwarded to server");


			//now waiting for a response from the server
			data = new byte[CommonConstants.PACKET_BUFFER_SIZE];
			responsePacket = new DatagramPacket(data, data.length);

			try {        
				System.out.println("Waiting for server..."); // so we know we're waiting
				sendReceiveSocket.receive(responsePacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

			// and forward back to client
			System.out.println("Host: Response received from server:");
			Utils.printPacketContent(responsePacket);
			try {
				forwardPacket = new DatagramPacket(data, responsePacket.getLength(),
						InetAddress.getLocalHost(), requestPacket.getPort());
			} catch (UnknownHostException uhe) {
				uhe.printStackTrace();
				System.exit(1);
			}

			try {
				sendSocket = new DatagramSocket();
			} catch (SocketException e) {
				e.printStackTrace();
				System.exit(1);
			}
			System.out.println("Host: Forwarding response back to client:");
			Utils.printPacketContent(forwardPacket);
			// Send the datagram packet to the client via the send socket. 
			try {
				sendSocket.send(forwardPacket);
				sendSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

			System.out.println("Host: response forwarded to client");
		}
	}

	public static void main( String args[] )
	{
		Host c = new Host();
		c.receiveAndForward();
	}
}
