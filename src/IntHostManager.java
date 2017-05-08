import java.net.DatagramPacket;
import java.net.DatagramSocket;
//import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.BufferedInputStream;
//import java.io.BufferedOutputStream;
//import java.io.ByteArrayOutputStream;
import java.io.IOException;
//import java.util.Arrays;

public class IntHostManager implements Runnable {
	//public final int READ = 1, WRITE = 2, DATA = 3, ACKNOWLEDGE = 4, DATA_LENGTH = 512;

	private DatagramSocket socket;
	private DatagramPacket sendPacket, receivePacket;
	int clientPort, serverPort;

	public IntHostManager(int clientPort, int serverPort) {
	      try {
	    	  socket = new DatagramSocket();
	    	  socket.setSoTimeout(5000);
	    	  this.serverPort = serverPort;
	    	  this.clientPort = clientPort;
	       } catch (SocketException se) {
	          se.printStackTrace();
	          System.exit(1);
	       }
	}

	
	
	public void run() {
		while(true){
			transfer();
		}
	} // end of loop

	private void transfer() {
		//step 1
		byte data[] = new byte[IntHostListener.PACKAGE_SIZE];
		receivePacket = new DatagramPacket(data, data.length);
		try {
			socket.receive(receivePacket);
		} catch(IOException e) {
			if(e instanceof SocketTimeoutException) {
				socket.close();// We're finished with this socket, so close it.
				System.out.println("File Transfer was done");
				return;
			}
			System.out.println("ERROR RECEIVING DATA FROM CLIENT\n" + e.getMessage());
		}
		IntHostListener.printPacket(receivePacket);
		int clientPort = receivePacket.getPort();//save client port
		
		//step 2
        sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), serverPort);
        IntHostListener.printPacket(sendPacket);
        try {
           socket.send(sendPacket);
        } catch (IOException e) {
           e.printStackTrace();
           System.exit(1);
        }

		//step 3
		//byte data[] = new byte[PACKAGE_SIZE];
		receivePacket = new DatagramPacket(data, data.length);
		try {
			socket.receive(receivePacket);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		IntHostListener.printPacket(receivePacket);
		int serverPort = receivePacket.getPort();//save server port
		
		//step 4
        sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), clientPort);
        IntHostListener.printPacket(sendPacket);
        try {
           socket.send(sendPacket);
        } catch (IOException e) {
           e.printStackTrace();
           System.exit(1);
        }
	}
}