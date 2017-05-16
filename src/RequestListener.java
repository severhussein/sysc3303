import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.Scanner;

public class RequestListener {
	public static boolean shutdown = false;
	private DatagramSocket receiveSock;
	private DatagramSocket sendSocket;;
	private DatagramPacket send, received;
	private String response;
	private static Scanner sc = new Scanner(System.in);
	private static boolean verbose = false;

	public RequestListener() {
		try {
			receiveSock = new DatagramSocket(CommonConstants.SERVER_LISTEN_PORT);
			sendSocket = new DatagramSocket();
			// receiveSock.setSoTimeout(120000);
		} catch (SocketException e) {
			System.out.println(e.getMessage());
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

		if (verbose)
			Utils.tryPrintTftpPacket(received);

		packetLength = received.getLength();

		// String packet = new String(datagram, 0, packetLength);
		// System.out.println("RequestListener received:\n\n" + packet);
		// System.out.println("\n" + Arrays.toString(packet.getBytes()) + "\n");

		// This is using the Professor's error handling from assignment1

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
			new Thread(new RequestManager(received.getPort(), received.getAddress(), filename, datagram[1], verbose))
					.start();
			System.out.println("\nSending Request and creating new thread\n");
		} else { // it was invalid, just quit
			ByteArrayOutputStream error = new ByteArrayOutputStream();
			error.write(0);
			error.write(5);
			error.write(0);
			error.write(4);
			try {
				error.write("READ/WRITE REQUEST INVALID FORMAT".getBytes());
			} catch (IOException e) {
				System.out.println("ERROR CREATING ERROR BYTE ARRAY\n" + e.getMessage());
			}
			error.write(0);

			byte errBuf[] = error.toByteArray();

			try {
				send = new DatagramPacket(errBuf, errBuf.length, received.getAddress(), received.getPort());
				if (verbose)
					Utils.tryPrintTftpPacket(send);
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
		RequestListener s = new RequestListener();
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
	}

	private static void queryServerMode(RequestListener s) {
		// ask user for input
		System.out.println("Enter:\n1 Toggle mode\n2 Begin Server");
		String request = sc.next();

		while (!request.equals("2")) {
			// request to toggle mode
			if (request.equals("1")) {
				toggleMode(s);
				// mode = toggleMode(mode);
			}
			System.out.println("Enter:\n1 Toggle mode\n2 Begin Server");
			request = sc.next();
		}
		// return mode;
	}

	private static void toggleMode(RequestListener s) {
		verbose = !verbose;
		System.out.println("Mode changed to: " + s.getOutputMode());
	}

	private static String queryServerShutDown() {
		System.out.println("Enter:\n1 For Shutdown\n2 Continue:");
		String response = sc.next();

		while (!response.equals("1") && !response.equals("2")) {
			System.out.println("Enter:\n1 For Shutdown\n2 Continue:");
			response = sc.next();
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
}
