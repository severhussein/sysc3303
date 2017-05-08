import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.io.IOException;
//import java.util.Arrays;

public class IntHostListener {
	public final int DEFAULT_LISTENER_PORT = 23;

	private DatagramSocket receiveSocket;
	private DatagramPacket receivePacket;

	public IntHostListener() {
	      try {
	          // Construct a datagram socket and bind it to port 23
	          // on the local host machine. This socket will be used to
	          // receive UDP Datagram packets from clients.
	          receiveSocket = new DatagramSocket(DEFAULT_LISTENER_PORT);

	       } catch (SocketException se) {
	          se.printStackTrace();
	          //System.exit(1);
	       }
	}

	public void receiveRequests() {
		byte data[] = new byte[1000];
		receivePacket = new DatagramPacket(data, data.length);

		try {
			receiveSocket.receive(receivePacket);
		} catch(IOException e) {
			e.printStackTrace();
			//System.exit(1);
		}
		
        // Process the received datagram.
        System.out.println("Simulator: Packet received:");
        System.out.println("From host: " + receivePacket.getAddress());
        System.out.println("Host port: " + receivePacket.getPort());
        int len = receivePacket.getLength();
        System.out.println("Length: " + len);
        System.out.println("Containing: " );
        
        // print the bytes
        for (int j=0;j<len;j++) {
           System.out.println("byte " + j + " " + data[j]);
        }

        // Form a String from the byte array, and print the string.
        String received = new String(data,0,len);
        System.out.println(received);
        


		new Thread(new IntHostManager(receivePacket)).start();
	}
	
	   public static void main( String args[] )
	   {
		   IntHostListener s = new IntHostListener();
	      s.receiveRequests();
	   }
}	