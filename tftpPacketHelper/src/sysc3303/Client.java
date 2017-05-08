package sysc3303;

/**
 * Client component of SYSC3303 assignment 1, based on the simple echo program provided in class
 * 
 * @author Yu-Kai Yang 100786472
 *
 */
import java.io.*;
import java.net.*;

import sysc3303.TftpPacket.TftpType;
import sysc3303.TftpRequestPacket;
import sysc3303.Utils;

public class Client {

	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket sendReceiveSocket;

	public Client()
	{
		try {
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException se) {   // Can't create the socket.
			se.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * A method for building arbitrary payload to be used in client
	 * 
	 * @param request
	 * @param filename
	 * @param mode
	 * @return byte array to be used in DatagramPacket class
	 * @throws IOException when something strange has happened
	 */
	private static byte[] buildPayload(byte[] request, String filename, byte[] mode) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		baos.write(request);
		baos.write(filename.getBytes());
		baos.write((byte)0);
		baos.write(mode);
		baos.write((byte)0);
		
		return baos.toByteArray();
	}
	
	/**
	 * Send a UDP packet with data packed to the destination
	 * 
	 * @param payload byte array containing the UDP data
	 * @throws IOException when things goes wrong
	 */
	public void sendAndReceive(byte [] payload)
	{
		try {
			sendPacket = new DatagramPacket(payload, payload.length,
					InetAddress.getLocalHost(), CommonConstants.HOST_LISTEN_PORT);
					//InetAddress.getByName("192.168.1.1"), CommonConstants.HOST_LISTEN_PORT);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println("Client: Sending packet:");
		Utils.printDatagramContentWiresharkStyle(sendPacket);

		try {
			sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println("Client: Packet sent.\n");

		byte data[] = new byte[CommonConstants.PACKET_BUFFER_SIZE];
		receivePacket = new DatagramPacket(data, data.length);

		try {
			// Block until a datagram is received via sendReceiveSocket.
			System.out.println("Waiting for reponse...");
			sendReceiveSocket.receive(receivePacket);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		Utils.printPacketContent(receivePacket);
	}

	public static void main(String args[])
	{
		Client c = new Client();
		byte msg[];
		try {
//			TftpOackPacket meow = new TftpOackPacket();
//			meow.setBlksize(777);
//			meow.setTimeout(255);
//			meow.setTransfersize(666);
//			meow.setWindowsize(99999);
//			c.sendAndReceive(meow.generatePayloadArray());
			
			
//			TftpAckPacket meow = new TftpAckPacket(65534);
//			c.sendAndReceive(meow.generatePayloadArray());
			
			
			
			TftpReadRequestPacket meow = new TftpReadRequestPacket("meow.txt", TftpRequestPacket.Mode.MODE_ASCII);
			meow.setBlksize(7777);
			meow.setTimeout(888);
			meow.setTransfersize(666);
			meow.setWindowsize(999);
			System.out.println(meow);
			System.out.println();
			c.sendAndReceive(meow.generatePayloadArray());
			
//			byte [] woof = {0,1,2,3,4,5,6,7,8};
//			TftpDataPacket meow = new TftpDataPacket(255, 
//					woof);
//			System.out.println(meow);
//			System.out.println();
//			c.sendAndReceive(meow.generatePayloadArray());
			
			
			//1st
			c.sendAndReceive(new TftpRequestPacket(TftpType.REQUEST_WRTIE, "meow.txt", 
					TftpRequestPacket.Mode.MODE_ASCII).generatePayloadArray());
			c.sendAndReceive(new TftpRequestPacket(TftpType.REQUEST_READ, "woof.txt",
					TftpRequestPacket.Mode.MODE_ASCII).generatePayloadArray());
			c.sendAndReceive(new TftpRequestPacket(TftpType.REQUEST_WRTIE, "123.txt",
					TftpRequestPacket.Mode.MODE_ASCII).generatePayloadArray());
			c.sendAndReceive(new TftpRequestPacket(TftpType.REQUEST_READ, "Canada.jpg",
					TftpRequestPacket.Mode.MODE_OCTET).generatePayloadArray());
			//5th
			c.sendAndReceive(new TftpRequestPacket(TftpType.REQUEST_WRTIE, "snow.mp4", 
					TftpRequestPacket.Mode.MODE_OCTET).generatePayloadArray());
			c.sendAndReceive(new TftpRequestPacket(TftpType.REQUEST_WRTIE, "IF.mib", 
					TftpRequestPacket.Mode.MODE_ASCII).generatePayloadArray());
			c.sendAndReceive(new TftpRequestPacket(TftpType.REQUEST_READ, "invoice.pdf",
					TftpRequestPacket.Mode.MODE_OCTET).generatePayloadArray());
			c.sendAndReceive(new TftpRequestPacket(TftpType.REQUEST_READ, "msconfig", 
					TftpRequestPacket.Mode.MODE_OCTET).generatePayloadArray());
			c.sendAndReceive(new TftpRequestPacket(TftpType.REQUEST_WRTIE, "stdio.h", 
					TftpRequestPacket.Mode.MODE_OCTET).generatePayloadArray());
			//10th, try mixed case in the mode string
			msg = buildPayload(TftpType.REQUEST_WRTIE.getArray(), "log4j.zip", "netASCii".getBytes());
			c.sendAndReceive(msg);
			//11th.. invalid
			msg = buildPayload("randomdata".getBytes(), "blah.txt", "invaliddata".getBytes());
			c.sendAndReceive(msg);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}