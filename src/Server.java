import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import TftpPacketHelper.TftpPacket;

public class Server {
	public static boolean shutdown = false;
	private DatagramSocket receiveSock;
	private DatagramSocket sendSocket;;
	private DatagramPacket send, received;
	private String response;
	private static Scanner sc = new Scanner(System.in);
	private static boolean verbose = true;
	private static List<File> fileInRead = Collections.synchronizedList(new LinkedList<File>());
	private static List<File> fileInWrite = Collections.synchronizedList(new LinkedList<File>());

	public Server() {
		try {
			receiveSock = new DatagramSocket(CommonConstants.SERVER_LISTEN_PORT);
			sendSocket = new DatagramSocket();
			// receiveSock.setSoTimeout(120000);
		} catch (SocketException e) {
			System.out.println(e.getMessage());
			System.out.println("Failed to bind to TFTP port" + CommonConstants.SERVER_LISTEN_PORT);
			System.out.println("Is there another TFTP server running on this machine?");
			sc.close();
			System.exit(1);
		}
	}

	public void receiveRequests() throws InvalidPacketException {
		int req; // READ, WRITE or ERROR
		byte datagram[] = new byte[CommonConstants.DATA_BLOCK_SZ];

		String filename = "", mode = "";
		int packetLength, j = 0, k = 0;

		received = new DatagramPacket(datagram, datagram.length);
		System.out.println("Server Listener is waiting for request...\n");
		try {
			receiveSock.receive(received);
		} catch (IOException e) {
			if (e instanceof SocketTimeoutException) {
				response = queryServerShutDown();
				if (response.equals("1")) {
					shutDown();
				}
			} else if(e instanceof SocketException) {
				return;
			}
			else System.out.println("HOST RECEPTION ERROR\n" + e.getMessage());
		}

		if (verbose){
			System.out.println("Receiving...");
			Utils.tryPrintTftpPacket(received);
		}

		packetLength = received.getLength();

		// String packet = new String(datagram, 0, packetLength);
		// System.out.println("RequestListener received:\n\n" + packet);
		// System.out.println("\n" + Arrays.toString(packet.getBytes()) + "\n");

		
		//************************************************************
		//CREDIT GOES TO PROFESSOR FOR THIS PART OF THE CODE
		// This is using the Professor's error handling from assignment1
		//************************************************************
		
		// If it's a read, send back DATA (03) block 1
		// If it's a write, send back ACK (04) block 0
		// Otherwise, ignore it
		if (datagram[0] != 0)
			req = CommonConstants.ERR; // bad
		else if (datagram[1] == 1)
			req = CommonConstants.RRQ; // could be read
		else if (datagram[1] == 2)
			req = CommonConstants.WRQ; // could be write
		else
			req = CommonConstants.ERR; // bad

		if (req != CommonConstants.ERR) { // check for filename
			// search for next all 0 byte
			for (j = 2; j < packetLength; j++) {
				if (datagram[j] == 0)
					break;
			}
			if (j == packetLength)
				req = CommonConstants.ERR; // didn't find a 0 byte
			if (j == 2)
				req = CommonConstants.ERR; // filename is 0 bytes long
			// if(req==Request.ERROR)
			// System.out.println("INDEX J= "+j);
			// otherwise, extract filename
			filename = new String(datagram, 2, j - 2);
			// System.out.println(filename);
		}

		if (req != CommonConstants.ERR) { // check for mode
			// search for next all 0 byte
			for (k = j + 1; k < packetLength; k++) {
				if (datagram[k] == 0)
					break;
			}
			if (k == packetLength)
				req = CommonConstants.ERR; // didn't find a 0 byte
			if (k == j + 1)
				req = CommonConstants.ERR; // mode is 0 bytes long
			// if(req==Request.ERROR)
			// System.out.println("INDEX J= "+j+" INDEX K="+k);

			mode = new String(datagram, j + 1, k - j).trim();

		}
		//No support for RFC2347, any trailing bytes after mode will be treated as error
		if (k != packetLength - 1)
			req = CommonConstants.ERR; // other stuff at end of packet
		// if(req==Request.ERROR) for debugging
		// System.out.println("INDEX J= "+j+" INDEX K="+k);
		if (!mode.equalsIgnoreCase("netascii") && !mode.equalsIgnoreCase("octet"))
			req = CommonConstants.ERR;// mode is not correct
		// if(req==Request.ERROR)
		// System.out.println(mode);

		if (req == CommonConstants.RRQ || req == CommonConstants.WRQ) {
			new Thread(new ServerThread(received.getPort(), received.getAddress(), new File(filename), datagram[1], verbose, this))
					.start();
			System.out.println("\nCreating new thread\n");
		} else { // it was invalid, just quit
			ByteArrayOutputStream error = new ByteArrayOutputStream();
			error.write(0);
			error.write(5);
			error.write(0);
			error.write(4);
			String errorMessage = "READ/WRITE Request invalid format";
			try {
				// use the help to decode the packet, if no exception is
				// thrown then this is a TFTP packet
				TftpPacket.decodeTftpPacket(received);
			} catch (IllegalArgumentException ile) {
				errorMessage = ile.getMessage();
			}
			try {
				error.write(errorMessage.getBytes());
			} catch (IOException e) {
				System.out.println("ERROR CREATING ERROR BYTE ARRAY\n" + e.getMessage());
			}
			error.write(0);

			byte errBuf[] = error.toByteArray();

			try {
				send = new DatagramPacket(errBuf, errBuf.length, received.getAddress(), received.getPort());
				if (verbose){
					System.out.println("Sending...");
					Utils.tryPrintTftpPacket(send);
				}
				sendSocket.send(send);
			} catch (IOException | IllegalArgumentException e) {
				if(e instanceof IllegalArgumentException) return;
				else System.out.println("ISSUE CREATING REQUEST ERROR PACKET\n" + e.getMessage());
			}
		}

	}

	public DatagramSocket getSocket() {
		return this.receiveSock;
	}

	public String getOutputMode() {
		if (verbose)
			return CommonConstants.VERBOSE;
		else
			return CommonConstants.QUIET;
	}

	public static void main(String args[]) {
		Server s = new Server();
		System.out.println("Server: currently in " + s.getOutputMode() + " mode");
		queryServerMode(s);
		new Thread(new ServerScanner(s.getSocket())).start();

		while (!shutdown) {
			try {
				s.receiveRequests();
			} catch (InvalidPacketException e) {
				System.out.println("PACKET FORMAT ERROR\n" + e.getMessage());
			}
		}
		System.out.println("Server shutting down.");
	}

	private static void queryServerMode(Server s) {
		// ask user for input
		System.out.println("Enter:\n1 Begin Server\n2 Toggle mode");
		String request = sc.nextLine();

		while (!request.equals("1")) {
			// request to toggle mode
			if (request.equals("2")) {
				toggleMode(s);
				// mode = toggleMode(mode);
			}
			else if(request.equals("shutdown"))
			{
				shutdown = true;
				//DO PROPER THINGS TO SHUTDOWN LIKE CLOSE SOCKETS
				//PRINT STATEMENT 
				//system.exit(1);
				
			}
			System.out.println("Enter:\n1 Begin Server\n2 Toggle mode");
			request = sc.nextLine();
		}
		// return mode;
	}

	private static void toggleMode(Server s) {
		verbose = !verbose;
		System.out.println("Mode changed to: " + s.getOutputMode());
	}

	private static String queryServerShutDown() {
		System.out.println("Enter:\n1 For Shutdown\n2 Continue:");
		String response = sc.nextLine();

		while (!response.equals("1") && !response.equals("2")) {
			System.out.println("Enter:\n1 For Shutdown\n2 Continue:");
			response = sc.nextLine();
		}

		return response;
	}

	private void shutDown() {
		System.out.println("Server is exiting.");
		receiveSock.close();
		sendSocket.close();
		sc.close();
		System.exit(1);

	}

	
	public void declareThisFileNotInWrite(File file) {
		fileInWrite.remove(file);
	}

	public void declareThisFileNotInRead(File file) {
		fileInRead.remove(file);
	}

	private void declareThisFileInWrite(File file) {
		fileInWrite.add(file);
	}

	private void declareThisFileInRead(File file) {
		fileInRead.add(file);
	}

	public synchronized boolean canThisFileBeWritten(File file) {
		if (fileInRead.contains(file) || fileInWrite.contains(file)) {
			return false;
		}
		declareThisFileInWrite(file);
		return true;
	}

	public synchronized boolean canThisFileBeRead(File file) {
		if (fileInWrite.contains(file)) {
			return false;
		}
		declareThisFileInRead(file);
		return true;
	}
}
