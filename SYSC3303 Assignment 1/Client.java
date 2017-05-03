import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class Client {
	public final int DEFAULT_HOST_SOCKET = 23;

	DatagramSocket socket;
	DatagramPacket send, receive;

	public Client() {
		try {
			socket = new DatagramSocket();
			socket.setSoTimeout(3000);
		} catch (SocketException e) {
			System.out.println(e.getMessage());
		}
	}

	public void sendDatagram(int requestType, String file, String encoding) {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();

		buf.write(0);
		buf.write(requestType);
		try {
			buf.write(file.getBytes());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		buf.write(0);
		try {
			buf.write(encoding.getBytes());
		} catch(IOException e) {
			System.out.println(e.getMessage());
		}
		buf.write(0);
		byte datagram[] = buf.toByteArray();

		try {
			send = new DatagramPacket(datagram,
				datagram.length,
				InetAddress.getLocalHost(),
				DEFAULT_HOST_SOCKET);
		} catch (UnknownHostException e) {
			System.out.println(e.getMessage());
		}

		System.out.println("Client created packet with:\n\n" + new String(datagram, 0, datagram.length));
		System.out.println("\n" + Arrays.toString(datagram) + "\n");


		try {
			socket.send(send);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public void receiveDatagram() {
		byte response[] = new byte[100];
		receive = new DatagramPacket(response, response.length);
		try {
			socket.receive(receive);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

		String file = new String(response, 0, receive.getLength());
		System.out.println("Client received:\n\n" + file + "\n" + Arrays.toString(response));
	}

	public static void main(String args[]) {
		Client c = new Client();
		for(int i = 0; i < 11; i += 1) {
			if(i < 10) {
				System.out.println("Building Packet.\n");
				c.sendDatagram((i%2)+1, "test_file_"+i+".txt", "netascii");
				System.out.println("Sent Packet. Waiting for response.\n");
				c.receiveDatagram();
			} else {
				System.out.println("Building Packet.\n");
				c.sendDatagram(0, "\0", "netascii");
				System.out.println("Sent Packet. Waiting for response.\n");
				c.receiveDatagram();
			}
		}
	}
}
