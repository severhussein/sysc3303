import java.net.DatagramSocket;
import java.util.Scanner;

public class ServerScanner implements Runnable {
	private DatagramSocket receiveSock;
	public boolean done = false;

	public ServerScanner(DatagramSocket receiveSock) {
		this.receiveSock = receiveSock;
	}

	public void run() {
		Scanner sc = new Scanner(System.in);
		String request = sc.next();
		while (!request.equals("shutdown")) {
			request = sc.next();
		}
		RequestListener.shutdown = true;
		receiveSock.close();
		sc.close();
	}
}
