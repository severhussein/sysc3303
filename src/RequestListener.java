import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class RequestListener {
	public final int DEFAULT_SERVER_PORT = 69, READ = 1, WRITE = 2;

	private DatagramSocket receiveSock;
	private DatagramPacket send, received;
	private String response, serverMode = "quiet";

	// types of requests we can receive
	public static enum Request {
		READ, WRITE, ERROR
	};

	public RequestListener() {
		try {
			receiveSock = new DatagramSocket(DEFAULT_SERVER_PORT);
			receiveSock.setSoTimeout(120000);
		} catch (SocketException e) {
			System.out.println(e.getMessage());
		}
	}

	public void receiveRequests() throws InvalidPacketException {
		Request req; // READ, WRITE or ERROR
		byte datagram[] = new byte[512];

		String filename = "", mode = "";
		int packetLength, j = 0, k = 0;
				
		received = new DatagramPacket(datagram, datagram.length);
		System.out.println("Server is waiting for request...\n");
		try {
			receiveSock.receive(received);
		} catch (IOException e) {
			if(e instanceof SocketTimeoutException)
			{
				response = queryServerShutDown();
				if(response.equals("1")){
					shutDown();
				}		
			}
			else
				System.out.println("HOST RECEPTION ERROR\n" + e.getMessage());
		}

		if(serverMode.equals("verbose"))
			Utils.printVerbose(received);
		
		packetLength = received.getLength();

		// String packet = new String(datagram, 0, packetLength);
		// System.out.println("RequestListener received:\n\n" + packet);
		// System.out.println("\n" + Arrays.toString(packet.getBytes()) + "\n");

		// This is using the Professor's error handling from assignment1

		// If it's a read, send back DATA (03) block 1
		// If it's a write, send back ACK (04) block 0
		// Otherwise, ignore it
		if(serverMode.equals("verbose")) Utils.printPacketContent(received);
		if (datagram[0] != 0)
			req = Request.ERROR; // bad
		else if (datagram[1] == 1)
			req = Request.READ; // could be read
		else if (datagram[1] == 2)
			req = Request.WRITE; // could be write
		else
			req = Request.ERROR; // bad

		if (req != Request.ERROR) { // check for filename
			// search for next all 0 byte
			for (j = 2; j < packetLength; j++) {
				if (datagram[j] == 0)
					break;
			}
			if (j == packetLength)
				req = Request.ERROR; // didn't find a 0 byte
			if (j == 2)
				req = Request.ERROR; // filename is 0 bytes long
			//if(req==Request.ERROR)
				//System.out.println("INDEX J= "+j);
			// otherwise, extract filename
			filename = new String(datagram, 2, j - 2);
			System.out.println(filename);
		}

		if (req != Request.ERROR) { // check for mode
			// search for next all 0 byte
			for (k = j + 1; k < packetLength; k++) {
				if (datagram[k] == 0)
					break;
			}
			if (k == packetLength)
				req = Request.ERROR; // didn't find a 0 byte
			if (k == j + 1)
				req = Request.ERROR; // mode is 0 bytes long
			//if(req==Request.ERROR)
				//System.out.println("INDEX J= "+j+" INDEX K="+k);
				
			mode = new String(datagram, j+1, k - j).trim();
			
		}

		if (k != packetLength - 1)
			req = Request.ERROR; // other stuff at end of packet
		//if(req==Request.ERROR) for debugging
			//System.out.println("INDEX J= "+j+" INDEX K="+k);
		if (!mode.equalsIgnoreCase("netascii") && !mode.equalsIgnoreCase("octet"))
			req = Request.ERROR;// mode is not correct
		//if(req==Request.ERROR)
			//System.out.println(mode);

		if (req == Request.READ || req == Request.WRITE)
			new Thread(new RequestManager(received.getPort(), filename, datagram[1],serverMode)).start();
		else { // it was invalid, just quit
			throw new InvalidPacketException("Request is not in valid format");
		}

		// alex's error handling
		// String strArr[] = packet.split("\0");
		// System.out.println(Arrays.toString(strArr) + "\n");
		// if(datagram[1] == READ || datagram[1] == WRITE) {
		// if(strArr.length != 3) throw new InvalidPacketException("Request is
		// not in valid format.");
		// else {
		// new Thread(new RequestManager(received.getPort(),
		// strArr[1].substring(1), datagram[1])).start();
		// }
		// }
	}

	public String getOutputMode() {
		return this.serverMode;
	}

	public void setOutputMode(String serverMode) {
		this.serverMode = serverMode;
	}

	public static void main(String args[]) {
		RequestListener s = new RequestListener();
		System.out.println("Server: currently in " + s.getOutputMode() + " mode");
		queryServerMode(s);

		while (true) {
			try {
				s.receiveRequests();
			} catch (InvalidPacketException e) {
				System.out.println("PACKET FORMAT ERROR\n" + e.getMessage());
			}
		}
	}

	private static void queryServerMode(RequestListener s) {
		// start a scanner
		Scanner sc = new Scanner(System.in);

		// ask user for input
		System.out.println("Enter:\n1 Toggle mode\n2 Begin Server");
		String request = sc.next();

		while (!request.equals("2")) {
			// request to toggle mode
			if (request.equals("1")) {
				toggleMode(s);
				//mode = toggleMode(mode);
			}
			System.out.println("Enter:\n1 Toggle mode\n2 Begin Server");
			request = sc.next();
		}
		//sc.close();
		//return mode;
	}
	private static void toggleMode(RequestListener s) {
		if(s.getOutputMode().equals("verbose")) s.setOutputMode("quiet");
		else if(s.getOutputMode().equals("quiet")) s.setOutputMode("verbose");
		/*if (mode.equals("verbose"))
			mode = "quiet";
		else
			mode = "verbose";

		System.out.println("Mode changed to: " + mode);
		return mode;*/
		System.out.println("Mode changed to: " + s.getOutputMode());
	}
	private static String queryServerShutDown(){
		Scanner sc = new Scanner(System.in);
		
		System.out.println("Enter:\n1 For Shutdown\n2 Continue:");
		String response = sc.next();
		
		while(!response.equals("1") && !response.equals("2")){
			System.out.println("Enter:\n1 For Shutdown\n2 Continue:");
			response = sc.next();
		}

		// sc.close();

		return response;
	}
	private void shutDown()
	{
		System.out.println("Server is exiting.");
		receiveSock.close();
		System.exit(1);
		
	}
}
