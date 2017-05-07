import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class RequestManager implements Runnable {
	public final int READ = 1, WRITE = 2, DATA = 3, ACKNOWLEDGE = 4, DATA_LENGTH = 512;

	private DatagramSocket socket;
	private DatagramPacket send, received;
	private String fileName;
	private int hostPort, type;

	public RequestManager(int hostPort, String fileName, int type) {
		try {
			socket = new DatagramSocket();
			this.hostPort = hostPort;
			this.fileName = fileName;
			this.type = type;
		} catch(SocketException e) {
			System.out.println(e.getMessage());
		}
	}

	public void run() {
System.out.println(type + "\n");
		if(type == 1) {
			byte readData[] = new byte[DATA_LENGTH], ack[] = new byte[4];
			int i = 0, size = 0;
			BufferedInputStream in = null;
			try {
				in = new BufferedInputStream(new FileInputStream(fileName));
			} catch(IOException e) {
				System.out.println("ERROR OPENING FILE\n" + e.getMessage());
			}
System.out.println("Reading File...\n");
			try {System.out.println(in.read(readData) + "\n");
				while((size = in.read(readData)) != -1) {
					i += 1;
System.out.println("Reached Here.\n");
					ByteArrayOutputStream buf = new ByteArrayOutputStream();
					buf.write(0);
					buf.write(3);
					byte readBlock[] = new byte[2];
					readBlock[0] = (byte) (i >> 8);
					readBlock[1] = (byte) i;
					//byte readBlock[] = ByteBuffer.allocate(2).putInt(i).array();
					try {
						buf.write(readBlock);
						buf.write(readData);
					} catch(IOException e) {
						System.out.println("ERROR READING DATA INTO BYTE ARRAY\n" + e.getMessage());
					}
					
					byte dataSend[] = buf.toByteArray();
System.out.println("Sending:\n" + new String(dataSend, 0, dataSend.length));
					try {
						send = new DatagramPacket(dataSend,
							dataSend.length,
							InetAddress.getLocalHost(),
							hostPort);
						socket.send(send);
					} catch(IOException e) {
						System.out.println("ERROR SENDING READ\n" + e.getMessage());
					}
System.out.println("Waiting for ack...\n");
					received = new DatagramPacket(ack, ack.length);
					try {
						socket.receive(received);
					} catch(IOException e) {
						System.out.println("RECEPTION ERROR AT MANAGER ACK\n" + e.getMessage());
					}
					// Check acknowledge packet, before continuing.
					// Right now, does not throw exception nor 
					// requests re-transmission.
					// Just prints to console.
					int block = ((ack[2] & 0xFF) >> 8) | (ack[3] & 0xFF);
					if(ack[1] != ACKNOWLEDGE && block != i) {
						System.out.println("ACK BLOCK DOES NOT MATCH\n");
					}
				}
			} catch(IOException e) {
				System.out.println("ERROR READING FILE\n" + e.getMessage());
			}

			if(size == DATA_LENGTH) {
				i += 1;
				ByteArrayOutputStream buf = new ByteArrayOutputStream();
				buf.write(0);
				buf.write(3);
				byte readBlock[] = new byte[2];
				readBlock[0] = (byte) (i >> 8);
				readBlock[1] = (byte) i;
				buf.write(0);
				byte dataSend[] = buf.toByteArray();
				
				try {
					send = new DatagramPacket(dataSend,
						dataSend.length,
						InetAddress.getLocalHost(),
						hostPort);
					socket.send(send);
				} catch(IOException e) {
					System.out.println("ERROR SENDING READ\n" + e.getMessage());
				}
			}
		}
		else if(type == 2) {
			boolean serve = true;
			byte writeData[] = new byte[516], ack[] = new byte[4];
			BufferedOutputStream out = null;
				try {
					out = new BufferedOutputStream(new FileOutputStream(fileName));
				} catch(IOException e) {
					System.out.println("ERROR CREATING FILE\n" + e.getMessage());
				}

			ack[0] = 0;
			ack[1] = 4;
			ack[2] = 0;
			ack[3] = 0;

			try {
				send = new DatagramPacket(ack,
					ack.length,
					InetAddress.getLocalHost(),
					hostPort);
				socket.send(send);
			} catch(IOException e) {
				System.out.println("ERROR SENDING ACK\n" + e.getMessage());
			}

			while(serve) {
				received = new DatagramPacket(writeData, writeData.length);
				try {
					socket.receive(received);
				} catch(IOException e) {
					System.out.println("HOST RECEPTION ERROR\n" + e.getMessage());
				}
			
				if(writeData[1] == DATA) {

					try {
						out.write(writeData, 4, received.getLength()-4);
					} catch (IOException e) {
						System.out.println("ERROR WRITING TO FILE\n" + e.getMessage());
					}
					
					ack[2] = writeData[2];
					ack[3] = writeData[3];

					try {
						send = new DatagramPacket(ack,
							ack.length,
							InetAddress.getLocalHost(),
							hostPort);
						socket.send(send);
					} catch(IOException e) {
						System.out.println("ERROR SENDING ACK\n" + e.getMessage());
					}
				}

				if(received.getLength() < DATA_LENGTH) serve = false;
			}
		}
	}
}
