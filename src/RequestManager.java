import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.Files;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import TftpPacketHelper.TftpErrorPacket;

public class RequestManager implements Runnable {
	private DatagramSocket socket;
	private DatagramPacket send, received;
	private String fileName;
	private int clientPort, type;
	private InetAddress clientAddress;
	private boolean verbose;
	

	public RequestManager(int clientPort, InetAddress clientAddress, String fileName, int type, boolean verbose) {
		try {
			this.socket = new DatagramSocket();
			this.clientPort = clientPort;
			this.clientAddress = clientAddress;
			this.fileName = fileName;
			this.type = type;
			this.verbose = verbose;
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
			
			File check = new File(fileName);
			if(!check.isFile()) {
				ByteArrayOutputStream error = new ByteArrayOutputStream();
				error.write(0);
				error.write(5);
				error.write(0);
				error.write(1);
				try {
					error.write((fileName + " could not be found or is not a file.").getBytes());
				} catch (IOException e) {
					System.out.println("ISSUE MAKING ERROR TYPE 1\n" + e.getMessage());
				}
				error.write(0);
				
				byte errBuf[] = error.toByteArray();
				
				DatagramPacket send = new DatagramPacket(errBuf,
										errBuf.length,
										clientAddress,
										clientPort);
				try {
					socket.send(send);
				} catch (IOException e) {
					System.out.println("ISSUE SENDING ERROR TYPE 1\n" + e.getMessage());
				}
				return;
			}
			if (!Files.isReadable(check.toPath()) || !check.canRead()) {
				try {
					socket.send(new TftpErrorPacket(2, "Unable to read the file due to insufficient permission")
							.generateDatagram(clientAddress, clientPort));
				} catch (IOException e) {
					System.out.println("ISSUE SENDING ERROR TYPE 2\n" + e.getMessage());
				}
				return;
			}

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
						send = new DatagramPacket(dataSend, dataSend.length, clientAddress, clientPort);
						socket.send(send);
						if(verbose)
							Utils.tryPrintTftpPacket(send);
					} catch (IOException e) {
						System.out.println("ERROR SENDING READ\n" + e.getMessage());
					}
					if(verbose) System.out.println("Waiting for ack...\n");
					received = new DatagramPacket(ackRRQ, ackRRQ.length);
					try {
						socket.receive(received);
						if(verbose)
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
									clientAddress,
									clientPort);
							socket.send(send);
						} catch(IOException e) {
							System.out.println("ISSUE CREATING DATA ERROR PACKET\n" + e.getMessage());
						}
						try {
							in.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return;
					}
				}
				if(i==0){
					byte[] emptyData = {0,3,0,1};
					DatagramPacket emptyPacket = new DatagramPacket(emptyData,emptyData.length,clientAddress,clientPort);
					socket.send(emptyPacket);
					//FIXME : kludge to receive ack, else ICMP 3.3 would be bounced back to client
					try {
						socket.receive(received);
						if(verbose)
							Utils.tryPrintTftpPacket(received);
					} catch (IOException e) {
						System.out.println("RECEPTION ERROR AT MANAGER ACK\n" + e.getMessage());
					}
				}
				try {
					in.close();
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			} catch (IOException e) {
				System.out.println("ERROR READING FILE\n" + e.getMessage());
			}
			if(verbose) System.out.println("Last block sizee: " + lastSize);
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
					send = new DatagramPacket(dataSend, dataSend.length, clientAddress, clientPort);
					socket.send(send);
					if(verbose)
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
			File check = new File(fileName);

			if (check.exists()) {
				if (check.isDirectory()) {
					try {
						socket.send(new TftpErrorPacket(0, "This is a directory").generateDatagram(clientAddress,
								clientPort));
					} catch (IOException e) {
						System.out.println("ISSUE SENDING ERROR TYPE 0\n" + e.getMessage());
					}
					return;
				} else if (!Files.isWritable(check.toPath())) {
					try {
						socket.send(new TftpErrorPacket(2, "Unable to write the file due to insufficient permission")
								.generateDatagram(clientAddress, clientPort));
					} catch (IOException e) {
						System.out.println("ISSUE SENDING ERROR TYPE 2\n" + e.getMessage());
					}
					return;
				}
			} else if (check.canWrite()) {
				// canWrite and friends does not work on Windows, method from
				// Files class above will do the job instead
				try {
					socket.send(new TftpErrorPacket(2, "Unable to write the file due to insufficient permission")
							.generateDatagram(clientAddress, clientPort));
				} catch (IOException e) {
					System.out.println("ISSUE SENDING ERROR TYPE 2\n" + e.getMessage());
				}
				return;
			}

			try {
				out = new BufferedOutputStream(new FileOutputStream(fileName));
			} catch (IOException e) {
				System.out.println("ERROR CREATING FILE\n" + e.getMessage());
			}

			
			//sending initial ack to a WRQ
			try {
				send = new DatagramPacket(ack, ack.length, clientAddress, clientPort);
				socket.send(send);
				if(verbose){
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
					if(verbose){
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
					if(verbose){
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
								clientAddress,
								clientPort);
						socket.send(send);
						if(verbose){
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
								clientAddress,
								clientPort);
						socket.send(send);
					} catch(IOException e) {
						System.out.println("ISSUE CREATING DATA ERROR PACKET\n" + e.getMessage());
					}
					try {
						out.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return;
				}

				if (received.getLength() < CommonConstants.DATA_PACKET_SZ){ // Length < 516, changed from <512, edited by David
					if (verbose) {
						System.out.println("Data <512 bytes, going to stop writing to file");
					}
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
