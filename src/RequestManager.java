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

import TftpPacketHelper.TftpErrorPacket;

public class RequestManager implements Runnable {
	private DatagramSocket socket;
	private DatagramPacket send, received;
	private String fileName, serverMode;
	private int clientPort, type;
	private InetAddress clientAddress;
	

	public RequestManager(int clientPort, InetAddress clientAddress, String fileName, int type, String serverMode) {
		try {
			this.socket = new DatagramSocket();
			this.clientPort = clientPort;
			this.clientAddress = clientAddress;
			this.fileName = fileName;
			this.type = type;
			this.serverMode = serverMode;
		} catch (SocketException e) {
			System.out.println(e.getMessage());
		}
	}

	public void run() {
		if (type == CommonConstants.RRQ) {
			byte readData[] = new byte[CommonConstants.DATA_BLOCK_SZ];
			byte ackRRQ[] = new byte[CommonConstants.ACK_PACKET_SZ];
			int i = 0, lastSize = 0, n;
			BufferedInputStream in = null;
			try {
				in = new BufferedInputStream(new FileInputStream(fileName));
			} catch (IOException e) {
				System.out.println("ERROR OPENING FILE\n" + e.getMessage());
			}
			System.out.println("Reading File...\n");
			try {
				while ((n = in.read(readData)) != -1) {
					i += 1;
					lastSize = n;
					ByteArrayOutputStream buf = new ByteArrayOutputStream();
					buf.write(0);
					buf.write(3);
					byte readBlock[] = new byte[2];
					readBlock[0] = (byte) (i >> 8);
					readBlock[1] = (byte) i;
					try {
						buf.write(readBlock);
						buf.write(readData, 0, n);
					} catch (IOException e) {
						System.out.println("ERROR READING DATA INTO BYTE ARRAY\n" + e.getMessage());
					}

					byte dataSend[] = buf.toByteArray();
					try {
						send = new DatagramPacket(dataSend, dataSend.length, InetAddress.getLocalHost(), clientPort);
						socket.send(send);
						if(serverMode.equals(CommonConstants.VERBOSE))
							Utils.tryPrintTftpPacket(send);
					} catch (IOException e) {
						System.out.println("ERROR SENDING READ\n" + e.getMessage());
					}
					if(serverMode.equals(CommonConstants.VERBOSE)) System.out.println("Waiting for ack...\n");
					received = new DatagramPacket(ackRRQ, ackRRQ.length);
					try {
						socket.receive(received);
						if(serverMode.equals(CommonConstants.VERBOSE))
							Utils.tryPrintTftpPacket(received);
					} catch (IOException e) {
						System.out.println("RECEPTION ERROR AT MANAGER ACK\n" + e.getMessage());
					}

					if(!received.getAddress().equals(clientAddress) || 
					   !(received.getPort() == clientPort)) {
						ByteArrayOutputStream error = new ByteArrayOutputStream();
						error.write(0);
						error.write(5);
						error.write(0);
						error.write(5);
						try {
							error.write("RECEIVED FROM UNEXPECTED TID".getBytes());
						} catch(IOException e) {
							System.out.println("ISSUE MAKING ERROR BYTE ARR\n" + e.getMessage());
						}
						error.write(0);

						byte errBuf[] = error.toByteArray();

						try {
							send = new DatagramPacket(errBuf,
									errBuf.length,
									received.getAddress(),
									received.getPort());
							socket.send(send);
						} catch(IOException e) {
							System.out.println("ISSUE CREATING ERROR PACKET" + e.getMessage());
						}

						i -= 1;
					}
					// Check acknowledge packet, before continuing.
					// Right now, does not throw exception nor
					// requests re-transmission.
					// Just prints to console.
					int block = ((ackRRQ[2] & 0xFF) >> 8) | (ackRRQ[3] & 0xFF);
					if (ackRRQ[1] != CommonConstants.ACK && block != i) {
						buf = new ByteArrayOutputStream();
						buf.write(0);
						buf.write(5);
						buf.write(0);
						buf.write(4);
						try {
							if(ackRRQ[1] != CommonConstants.ACK) {
								buf.write("PACKET OPCODE IS NOT ACK\n".getBytes());
							}
							else {
								buf.write("PACKET BLOCK # MISMATCH\n".getBytes());
							}
						} catch(IOException e) {
							System.out.println("ISSUE CREATING ERROR MSG\n" + e.getMessage());
						}
						buf.write(0);
						
						byte errBuf[] = buf.toByteArray();

						try {
							send = new DatagramPacket(errBuf,
									errBuf.length,
									InetAddress.getLocalHost(),
									clientPort);
							socket.send(send);
						} catch(IOException e) {
							System.out.println("ISSUE CREATING DATA ERROR PACKET\n" + e.getMessage());
						}
						return;
					}
				}
				if(i==0){
					byte[] emptyData = {0,3,0,1};
					DatagramPacket emptyPacket = new DatagramPacket(emptyData,emptyData.length,InetAddress.getLocalHost(),clientPort);
					socket.send(emptyPacket);
				}
				try {
					in.close();
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			} catch (IOException e) {
				System.out.println("ERROR READING FILE\n" + e.getMessage());
			}
			if(serverMode.equals(CommonConstants.VERBOSE)) System.out.println("Last block sizee: " + lastSize);
			if (lastSize == CommonConstants.DATA_BLOCK_SZ) {
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
					send = new DatagramPacket(dataSend, dataSend.length, InetAddress.getLocalHost(), clientPort);
					socket.send(send);
					if(serverMode.equals(CommonConstants.VERBOSE))
						Utils.tryPrintTftpPacket(send);
				} catch (IOException e) {
					System.out.println("ERROR SENDING READ\n" + e.getMessage());
				}
			}
		} else if (type == CommonConstants.WRQ) {
			boolean serve = true;
			byte writeData[] = new byte[CommonConstants.DATA_PACKET_SZ];
			byte ack[] = {0,4,0,0}; //not immutable
			BufferedOutputStream out = null;
			
			System.out.println("Beginning a new WRQ:");
			
			try {
				out = new BufferedOutputStream(new FileOutputStream(fileName));
			} catch (IOException e) {
				System.out.println("ERROR CREATING FILE\n" + e.getMessage());
			}

			
			//sending initial ack to a WRQ
			try {
				send = new DatagramPacket(ack, ack.length, InetAddress.getLocalHost(), clientPort);
				socket.send(send);
				if(serverMode.equals(CommonConstants.VERBOSE)){
					System.out.println("Initial Ack sent for WRQ:");
					Utils.tryPrintTftpPacket(send);
				}

			} catch (IOException e) {
				System.out.println("ERROR SENDING ACK\n" + e.getMessage());
			}
			
			System.out.println("Writing a File...\n");
			while (serve) {
				received = new DatagramPacket(writeData, writeData.length);
				try {
					//sending back ack0 for WRQ
					socket.receive(received);
					if(serverMode.equals(CommonConstants.VERBOSE)){
						Utils.tryPrintTftpPacket(received);
					}
				} catch (IOException e) {
					System.out.println("HOST RECEPTION ERROR\n" + e.getMessage());
				}
				if (received.getPort() != clientPort) {
					// receive a packet with wrong tid, notify the sender with error
					// 5
					
					try {
						send = new TftpErrorPacket(5, "Wrong Transfer ID").generateDatagram(received.getAddress(),
								received.getPort());
						socket.send(send);
					} catch (IOException e) {
						System.out.println("Error sending the error.\n" + e.getMessage());
					}
					if(serverMode.equals(CommonConstants.VERBOSE)){
						System.out.println("Error sent for wrong TID:");
						Utils.tryPrintTftpPacket(send);
					}

					// retries--;
					//for iteration 4
					//continue;
					System.out.println("Will terminate this transfer.");
					break;
				}

				if (writeData[1] == CommonConstants.DATA) {

					try {
						out.write(writeData, 4, received.getLength() - 4);
					} catch (IOException e) {
						System.out.println("ERROR WRITING TO FILE\n" + e.getMessage());
					}
					//change block#
					ack[2] = writeData[2];
					ack[3] = writeData[3];

					try {
						send = new DatagramPacket(ack,
								ack.length,
								InetAddress.getLocalHost(),
								clientPort);
						socket.send(send);
						if(serverMode.equals(CommonConstants.VERBOSE)){
							System.out.println("Sending Ack:");
							Utils.tryPrintTftpPacket(send);
						}
					} catch (IOException e) {
						System.out.println("ERROR SENDING ACK\n" + e.getMessage());
					}
				}
				else {
					ByteArrayOutputStream buf = new ByteArrayOutputStream();
					buf.write(0);
					buf.write(5);
					buf.write(0);
					buf.write(4);
					try {
						if(ack[1] != CommonConstants.ACK) {
							buf.write("PACKET OPCODE IS NOT DATA\n".getBytes());
						}
						else {
							buf.write("PACKET BLOCK # MISMATCH\n".getBytes());
						}
					} catch(IOException e) {
						System.out.println("ISSUE CREATING ERROR MSG\n" + e.getMessage());
					}
					buf.write(0);
					
					byte errBuf[] = buf.toByteArray();

					try {
						send = new DatagramPacket(errBuf,
								errBuf.length,
								InetAddress.getLocalHost(),
								clientPort);
						socket.send(send);
					} catch(IOException e) {
						System.out.println("ISSUE CREATING DATA ERROR PACKET\n" + e.getMessage());
					}
					return;
				}

				if (received.getLength() < CommonConstants.DATA_PACKET_SZ){ // Length < 516, changed from <512, edited by David
					System.out.println("Data <512 bytes, going to stop writing to file");
					serve = false;
					try{
						out.close();
					}catch(IOException e){
						System.out.println(e.getMessage());
					}
				}
					
			}//END WHILE
		}
		socket.close();//Should close the socket after the thread is done (David)
	}
}
