import java.net.DatagramSocket;
import java.util.Scanner;

public class ServerScanner implements Runnable {
	private DatagramSocket receiveSock;
	public done = 0;

	public ServerScanner(DatagramSocket receiveSock) {
		this.receiveSock = receiveSock;
	}

	public void run() {
		Scanner sc = new Scanner(System.in);
		String request = sc.next();
		while (!request.equals("shutdown")) {
			request = sc.next();
		}
		receiveSock.close();
		done = 1;
		System.out.println("<Server shutdown, do not accept any new request, not affect current transfer>");
		sc.close();
	}
}
