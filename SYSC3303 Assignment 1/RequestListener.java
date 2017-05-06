import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.io.IOException;
import java.util.Arrays;

public class RequestListener {
	public final int DEFAULT_SERVER_PORT = 69, READ = 1, WRITE = 2;

	private DatagramSocket receiveSock;
	private DatagramPacket send, received;

	public RequestListener() {
		try {
			receiveSock = new DatagramSocket(DEFAULT_SERVER_PORT);
		} catch(SocketException e) {
			System.out.println(e.getMessage());
		}
	}

	public void receiveRequests() throws InvalidPacketException {
		byte datagram[] = new byte[512];
		received = new DatagramPacket(datagram, datagram.length);

		try {
			receiveSock.receive(received);
		} catch(IOException e) {
			System.out.println("HOST RECEPTION ERROR\n" + e.getMessage());
		}

		String packet = new String(datagram, 0, datagram.length);
		System.out.println("RequestListener received:\n\n" + packet);
		System.out.println("\n" + Arrays.toString(packet.getBytes()) + "\n");

		String strArr[] = packet.split("\0");
		if(datagram[1] == READ || datagram[1] == WRITE) {
			if(strArr.length != 2) throw new InvalidPacketException("Request is not in valid format.");
			else {
				new Thread(new RequestManager(received.getPort(), strArr[1], datagram[1])).start();
			}
		}
	}

	public static void main(String args[]) {
		RequestListener s = new RequestListener();
		while(true) {
			s.receiveRequests();
		}
	}
}	
