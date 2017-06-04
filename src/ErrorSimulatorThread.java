import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

/**
 * IntHostManager for multi threads need ErrorSimulatorHelper.java
 * @author Dawei Chen 101020959
 */

public class ErrorSimulatorThread implements Runnable {

	private DatagramSocket socket;
	private DatagramPacket sendPacket, receivePacket;
	
	private int clientPort, serverPort;
	private InetAddress clientAddress, serverAddress;
	private boolean serverPortUpdated = false;
	
	private int[] userChoice;
	
	//change descriptions:
	//1. renamed destinationAddress to serverAddress (to be consistent with clientAddress)
	//2. added more strict conditions in while loop in run() to determine received source
	//(previous code was fine when there is no 2nd client luckily use the same port to communicate)
	//3. also changed the way to call simulateError from simulateError(port) to simulateError(ip, port)
	//so I dont need to add the new if condition inside every methods	
	//4. print IP with port number
	
	public ErrorSimulatorThread(DatagramPacket receivePacket, int[] userChoice, InetAddress newDestinationAddress) {
			this.receivePacket = receivePacket;
			this.userChoice = new int[userChoice.length];
			System.arraycopy(userChoice, 0, this.userChoice, 0, userChoice.length);//do not use equal here. copy content not reference
			
			this.serverPort = -1;
			this.serverAddress = newDestinationAddress;//server address remain the same in a file transfer
			this.clientPort = receivePacket.getPort();
			this.clientAddress = receivePacket.getAddress();;//client address remain the same in a file transfer
			System.out.println("client IP: "+clientAddress+", Port: "+clientPort);
			System.out.println("server IP: "+serverAddress+", Port: ??");
			
			this.socket = ErrorSimulatorHelper.newSocket();
			if (socket == null){
				System.out.print("Failed to create socket!");
			}
	}

	public void run() {

		//////////////////"Sending to server..."//////////////////////////////////////
		
		if (!simulateError(serverAddress, ErrorSimulator.DEFAULT_SERVER_PORT)) {
			System.out.print("Sending to server...");
			sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), serverAddress, ErrorSimulator.DEFAULT_SERVER_PORT);
			ErrorSimulatorHelper.send(socket, sendPacket);
			//System.out.print("Sent");
			System.out.print("    |Ip "+ serverAddress);
			System.out.print("    |Port "+ ErrorSimulator.DEFAULT_SERVER_PORT);
			System.out.print("    |Opcode "+ getOpcode());
			System.out.println("    |BLK#"+ getBlockNum());
			//clean printing//Utils.tryPrintTftpPacket(sendPacket);
		}
		//round++;// first round of request msg was done, increase i here
		//////////////////"Sent to server..."//////////////////////////////////////
		
		int receivedPort = -1;
		InetAddress receivedAddress = null;
		
		while (true) {

			//////////////////"Receiving..."//////////////////////////////////////
			System.out.println();
			System.out.print("Receiving...        ");
			receivePacket = ErrorSimulatorHelper.newReceive();//new packet
			ErrorSimulatorHelper.receive(socket, receivePacket);//receive
			//clean printing//Utils.tryPrintTftpPacket(receivePacket);
			//System.out.print("Received");
			receivedPort = receivePacket.getPort();
			receivedAddress = receivePacket.getAddress();
			System.out.print("    |Ip "+ receivedAddress);
			System.out.print("    |port "+ receivedPort);
			System.out.print("    |Opcode "+ getOpcode());
			System.out.println("    |BLK#"+ getBlockNum());
			//////////////////"Received"//////////////////////////////////////
			
			/**
			 * Ip Port Updated
			 * c	c	T	//send to server
			 * c	s	T
			 * c	?	T
			 * 
			 * s	c	T
			 * s	s	T	//send to client
			 * s	?	T
			 * 
			 * ?	c	T
			 * ?	s	T
			 * ?	?	T
			 * 
			 * c	c	F	//order error, server port not updated //should "not" just send to port 69
			 * c	?	F
			 * 
			 * s	c	F
			 * s	?	F	//update port, then send to client
			 * 
			 * ?	c	F
			 * ?	?	F
			 */
			
			if ( (receivedAddress.equals(clientAddress)) &&  serverPortUpdated && (receivedPort == clientPort) ) {
				
				//////////////////"Sending to server..."//////////////////////////////////////
				if (!simulateError(serverAddress, serverPort)) {
					System.out.print("Sending to server...");
					sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), serverAddress, serverPort);
					ErrorSimulatorHelper.send(socket, sendPacket);
					//System.out.print("Sent");
					System.out.print("    |Ip "+ serverAddress);
					System.out.print("    |port "+ serverPort);
					System.out.print("    |Opcode "+ getOpcode());
					System.out.println("    |BLK#"+ getBlockNum());
					//clean printing//Utils.tryPrintTftpPacket(sendPacket);
				}
				//////////////////"Sent to server..."//////////////////////////////////////
				
			} else if ( (receivedAddress.equals(serverAddress)) && 
			( (!serverPortUpdated && ((receivedPort != clientPort) )) || (serverPortUpdated && (receivedPort == serverPort)) ) ) {
				
				// update server port
				if (!serverPortUpdated) {
					serverPort = receivePacket.getPort();
					serverPortUpdated = true;
					System.out.println("updated server port: "+serverPort);
				}
				
				//////////////////"Sending to Client...\n"//////////////////////////////////////
				if (!simulateError(clientAddress, clientPort)) {
					System.out.print("Sending to Client...");
					sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), clientAddress, clientPort);
					ErrorSimulatorHelper.send(socket, sendPacket);
					//System.out.print("Sent");
					System.out.print("    |Ip "+ clientAddress);
					System.out.print("    |port "+ clientPort);
					System.out.print("    |Opcode "+ getOpcode());
					System.out.println("    |BLK#"+ getBlockNum());
					//clean printing//Utils.tryPrintTftpPacket(sendPacket);
				}
				//////////////////"Sent to Client...\n"//////////////////////////////////////
			} else if (!serverPortUpdated && (receivedPort == clientPort) && receivedAddress.equals(clientAddress)) {
				System.out.println("Order incorrect, receive packet from client when server port not set");
			} else {//remaining case is: serverPortUpdated && (port is neither client nor server)
				System.out.println("unknown source received");
				System.out.println("received IP: "+receivedAddress+", Port: "+receivedPort);
				System.out.println("receivedAddress == serverAddress: "+ (receivedAddress == serverAddress));
			}

		}
	}
	
	
	

	
	
	public boolean simulateError(InetAddress ip, int port){
			
		int mainBranchIndex = 0;
		//"<Choose Error Type> (Network Issue or Invalid Data)",
        //"1     Invalid Data",
        //"2     Network Issue"
        
	//network error
	    if (userChoice[mainBranchIndex] == 2) {
	    	int typeIndex = 1;
	    	int valueIndex = 2;
	    	int packetIndex = 3;
	    	int blockIndex = 4;
	    	
	    	//check OpCode
	    	if (receivePacket.getData()[0] != (byte) 0) return false;
	    	if (receivePacket.getData()[1] != (byte) userChoice[packetIndex]) return false;
	    	
	    	//check block number
	    	if (receivePacket.getData()[1] == (byte)3 || receivePacket.getData()[1] == (byte)4) {
		    	if (receivePacket.getData()[2] != intToByte(userChoice[blockIndex])[0]) return false;
		    	if (receivePacket.getData()[3] != intToByte(userChoice[blockIndex])[1]) return false;
	    	}
	    	
					//"<Choose Network Issue Type>",
		            //"1     Duplicate",
		            //"2     Delayed",
		            //"3     Lost"
		    if (userChoice[typeIndex] == 1) {
		    	return simulateDuplicate(ip, port, userChoice[valueIndex]);
		    } else if (userChoice[typeIndex] == 2) {
		    	return simulateDelayed(ip, port, userChoice[valueIndex]);
		    } else if (userChoice[typeIndex] == 3) {
		    	return simulateLost(ip, port, userChoice[valueIndex]);
		    } else {
		    	System.out.println("Unknown Error TypeIndex: "+userChoice[typeIndex]);
		    	return false;
		    }
	    }

	//data error, TID error
	    else if (userChoice[mainBranchIndex] == 1){
	    	
	    	int problemIndex = 1;
	    	int packetIndex = 2;
	    	int fieldIndex = 3;
	    	int sizeIndex = 3;
	    	int blockIndex = 4;
	    	
	    	//check OpCode
	    	if (receivePacket.getData()[0] != (byte) 0) return false;
	    	if (receivePacket.getData()[1] != (byte) userChoice[packetIndex]) return false;
	    	
	    	//check block number
	    	if (receivePacket.getData()[1] == (byte)3 || receivePacket.getData()[1] == (byte)4) {
		    	if (receivePacket.getData()[2] != intToByte(userChoice[blockIndex])[0]) return false;
		    	if (receivePacket.getData()[3] != intToByte(userChoice[blockIndex])[1]) return false;
	    	}

		    /*
		    if (userChoice[problemIndex] == 4) {//"4     Invalid IP"
		    	return simulateIncorrectIP(ip, port);
		    }
		    */
		    if (userChoice[problemIndex] == 3) {//"3     Invalid TID"
		    	return simulateIncorrectTID(ip, port);
		    }
		    if (userChoice[problemIndex] == 2) {//"2     Incorrect Size",
		    	return simulateIncorrectSize(ip, port, userChoice[sizeIndex]);
		    }
		    if (userChoice[problemIndex] == 1) {//"1     Corrupted Field",
			    if (userChoice[packetIndex] == 1 || userChoice[packetIndex] == 2) {
			    	return simulateCorruptedRequest(ip, port, userChoice[fieldIndex]);
			    } else if (userChoice[packetIndex] == 3) {
			    	return simulateCorruptedData(ip, port, userChoice[fieldIndex]);
			    } else if (userChoice[packetIndex] == 4) {
			    	return simulateCorruptedAck(ip, port, userChoice[fieldIndex]);
			    } else if (userChoice[packetIndex] == 5) {
			    	return simulateCorruptedError(ip, port, userChoice[fieldIndex]);
			    }
		    }
		    if (userChoice[problemIndex] == 0) {//"1     Remove Field",
			    if (userChoice[packetIndex] == 1 || userChoice[packetIndex] == 2) {
			    	return simulateRemoveRequest(ip, port, userChoice[fieldIndex]);
			    } else if (userChoice[packetIndex] == 3) {
			    	return simulateRemoveData(ip, port, userChoice[fieldIndex]);
			    } else if (userChoice[packetIndex] == 4) {
			    	return simulateRemoveAck(ip, port, userChoice[fieldIndex]);
			    } else if (userChoice[packetIndex] == 5) {
			    	return simulateRemoveError(ip, port, userChoice[fieldIndex]);
			    }
		    }
	    }
		return false;
	}
		
	
	public byte[] intToByte(int value) {
	    return new byte[] {
	            (byte)(value >>> 8),
	            (byte)value};
	}
	
	  public int unsignedToBytes(byte b) {
		    return b & 0xFF;
		  }
	  
	public int getBlockNum(){
		//invalid opcode
    	if (receivePacket.getData()[0] != (byte) 0) return -1;
    	//check block number
    	if (receivePacket.getData()[1] == (byte)3 || receivePacket.getData()[1] == (byte)4) {
	    	return ((int) Math.pow(2, 8))*unsignedToBytes(receivePacket.getData()[2]) + unsignedToBytes(receivePacket.getData()[3]);
    	}
    	return -1;
	}

	public String getOpcode(){
	    	return ""+receivePacket.getData()[0]+ receivePacket.getData()[1];
	}
	

	
	public boolean simulateDuplicate(InetAddress ip, int port, int value) {
		userChoice[0] = 0;//to mark that the error was simulated, do not simulate it again
		System.out.println("!");
		System.out.println("<simulateDuplicate>");
		System.out.println("!");
		
		if (value<0) value = 1;
		for (int i=0; i<value; i++) {
			ErrorSimulatorHelper.print("Sending Duplicated Packet...");
			
			sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), ip, port);
			ErrorSimulatorHelper.send(socket, sendPacket);
			
			ErrorSimulatorHelper.print("Sent, Print Duplicated Packet:");
			ErrorSimulatorHelper.printPacket(sendPacket);
		}
		return false;//false means error packet do not replace normal packet
	}
	
	public boolean simulateDelayed(InetAddress ip, int port, int value) {
		userChoice[0] = 0;//to mark that the error was simulated, do not simulate it again
		System.out.println("!");
		System.out.println("<simulateDelayed>");
		System.out.println("!");
		
		System.out.println("Waiting " + value + " ms delay");
        try {
            Thread.sleep(value);
        } catch (InterruptedException e) {}
        System.out.println("Delay finished");
        return false;//false means error packet do not replace normal packet
	}
	
	public boolean simulateLost(InetAddress ip, int port, int value) {
		int valueIndex = 2;//need change accordingly to class field!!
		if (userChoice[valueIndex]<=0) {
			return false;//do not replace the normal packet
		}
		userChoice[valueIndex] = userChoice[valueIndex] - 1;
		if (userChoice[valueIndex]==0) {
			userChoice[0] = 0;//to mark that the error was simulated, do not simulate it again
		}
		
		System.out.println("!");
		System.out.println("<simulateLost>");
		System.out.println("!");
		
		/*
		for (int i=0; i<value; i++) {
			System.out.println("Last Received ignored, Receiving new packet...");
			receivePacket = ErrorSimulatorHelper.newReceive();
			ErrorSimulatorHelper.receive(socket, receivePacket);
			System.out.println("Received");
		}
		*/
		return true;//replace the normal packet
	}

	
	public boolean simulateIncorrectTID(InetAddress ip, int port) {
		userChoice[0] = 0;//to mark that the error was simulated, do not simulate it again
		
		System.out.println("!");
		System.out.println("<simulateIncorrectTID>");
		System.out.println("!");
		
		DatagramSocket new_socket = ErrorSimulatorHelper.newSocket();

		//System.out.print("<Error TID> Sending to port...");
		sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), ip, port);
		//simulate a packet with the Wrong TID
		ErrorSimulatorHelper.send(new_socket, sendPacket);
		
				
		//clean printing//Utils.tryPrintTftpPacket(sendPacket);
		//System.out.print("    |port "+ port);
		//System.out.print("    |Opcode "+ getOpcode());
		//System.out.println("    |BLK#"+ getBlockNum());
		System.out.println("Sent Fake TID Packet to port: "+ sendPacket.getPort());
		System.out.println("Receiving ERROR...");
		
		//block until it receives wrong tid error packet
		DatagramPacket tempErrorReceivePacket = ErrorSimulatorHelper.newReceive();
		ErrorSimulatorHelper.receive(new_socket, tempErrorReceivePacket);
		
		System.out.println("Received ERORR from port: "+ tempErrorReceivePacket.getPort());
		ErrorSimulatorHelper.printPacket(tempErrorReceivePacket);

		//close the new socket to simulate wrong tid
		new_socket.close();


		return false;//false means error packet do not replace normal packet
	}
	/*
	public boolean simulateIncorrectIP(InetAddress ip, int port) {
		userChoice[0] = 0;//to mark that the error was simulated, do not simulate it again
		
		System.out.println("!");
		System.out.println("<simulateIncorrect IP>");
		System.out.println("!");
		
		InetAddress fakeIp = null;
		try {
			fakeIp = InetAddress.getByName("7.7.7.7");
		} catch (UnknownHostException e) {
			System.out.print("Create fake IP failed");
		}
		DatagramSocket new_socket = ErrorSimulatorHelper.newSocket(socket.getPort(), fakeIp);

		//System.out.print("<Error TID> Sending to port...");
		sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), ip, port);
		//simulate a packet with the Wrong TID
		ErrorSimulatorHelper.send(new_socket, sendPacket);
		
				
		//clean printing//Utils.tryPrintTftpPacket(sendPacket);
		//System.out.print("    |port "+ port);
		//System.out.print("    |Opcode "+ getOpcode());
		//System.out.println("    |BLK#"+ getBlockNum());
		System.out.println("Sent Fake IP Packet to port: "+ sendPacket.getPort());
		System.out.println("Receiving ERROR...");
		
		//block until it receives wrong tid error packet
		DatagramPacket tempErrorReceivePacket = ErrorSimulatorHelper.newReceive();
		ErrorSimulatorHelper.receive(new_socket, tempErrorReceivePacket);
		
		System.out.println("Received ERORR from port: "+ tempErrorReceivePacket.getPort());
		ErrorSimulatorHelper.printPacket(tempErrorReceivePacket);

		//close the new socket to simulate wrong tid
		new_socket.close();

		return false;//false means error packet do not replace normal packet
	}
	*/
	
	
	public boolean simulateIncorrectSize(InetAddress ip, int port, int errorSize) {
		userChoice[0] = 0;//to mark that the error was simulated, do not simulate it again
		System.out.println("!");
		System.out.println("<simulateIncorrectSize>");
		System.out.println("!");
		
		int len = receivePacket.getLength();
		if (errorSize > len) {
			byte[] newData = new byte[errorSize];
			Arrays.fill(newData, (byte) 255);
			System.arraycopy(receivePacket.getData(), 0, newData, 0, len);
			sendPacket = new DatagramPacket(newData, errorSize , ip, port);
		} else {
			sendPacket = new DatagramPacket(receivePacket.getData(), errorSize , ip, port);
		}
		
		ErrorSimulatorHelper.send(socket, sendPacket);
		
		ErrorSimulatorHelper.print("Print Size Modified Packet:");
		ErrorSimulatorHelper.printPacket(sendPacket);
		
		return true;//true means error packet replace normal packet
		
	}
	
	
	
	
	
	
	
	
	/*
	printOptions(new String[]{
			"<Choose Field>",
            "1     Opcode",
            "2     File Name",
            "3     Null byte 1 {0}",
            "4     Mode",
            "5     Null byte 2 {0}"
            });
    userChoice[fieldIndex] = getUserInput(1, 5);
    */
	public boolean simulateCorruptedRequest(InetAddress ip, int port, int field) {
		userChoice[0] = 0;//to mark that the error was simulated, do not simulate it again
		
		System.out.println("!");
		System.out.println("<simulateCorruptedRequest>");
		System.out.println("!");
		
		int len = receivePacket.getLength();
		int first = -1;
		int second = -1;
		int pos = 2;
		
		for (; pos < len; pos++) {
			if (receivePacket.getData()[pos] == 0) {first = pos; break;}
		}
		pos++;
		for (; pos < len; pos++) {
			if (receivePacket.getData()[pos] == 0) {second = pos; break;}
		}
		
		if ((second < 0) || (second != len - 1)) {
			System.out.println("<Error> invalid format, null byte at position" + second);
			ErrorSimulatorHelper.print("Print receivePacket:");
			ErrorSimulatorHelper.printPacket(receivePacket);
			//return false;
		}
		
		
		if (field == 1) {
			receivePacket.getData()[0] = (byte) 255;
			receivePacket.getData()[1] = (byte) 255;
		} else if (field == 2) {
			for (int i = 2; i < first; i++) {
				receivePacket.getData()[i] = (byte) 255;
			}
		} else if (field == 3) {
			receivePacket.getData()[first] = (byte) 255;
		} else if (field == 4) {
			for (int i = first + 1; i < second; i++) {
				receivePacket.getData()[i] = (byte) 255;
			}
		} else if (field == 5) {
			receivePacket.getData()[second] = (byte) 255;
		} else {
			System.out.println("simulateCorruptedRequest() unknown field");
			return false;//unknown field
		}

		sendPacket = new DatagramPacket(receivePacket.getData(), len, ip, port);
		ErrorSimulatorHelper.send(socket, sendPacket);
		
		ErrorSimulatorHelper.print("Print Modified Packet:");
		ErrorSimulatorHelper.printPacket(sendPacket);
		
		return true;//true means error packet replace normal packet
	}
	
	/*
	printOptions(new String[]{
			"<Choose Field>",//Opcode |   Block #  |   Data
            "1     Opcode",
            "2     Block #",
            "3     Data"
            });
    userChoice[fieldIndex] = getUserInput(1, 3);
    */
	public boolean simulateCorruptedData(InetAddress ip, int port, int field) {
		userChoice[0] = 0;//to mark that the error was simulated, do not simulate it again
		
		System.out.println("!");
		System.out.println("<simulateCorruptedData>");
		System.out.println("!");
		
		int len = receivePacket.getLength();
		
		if (field == 1) {
			receivePacket.getData()[0] = (byte) 255;
			receivePacket.getData()[1] = (byte) 255;
		} else if (field == 2) {
			receivePacket.getData()[2] = (byte) 255;
			receivePacket.getData()[3] = (byte) 255;
		} else if (field == 3) {
			for (int i = 4; i < len; i++) {
				receivePacket.getData()[i] = (byte) 255;
			}
		} else {
			System.out.println("simulateCorruptedData() unknown field");
			return false;//unknown field
		}

		sendPacket = new DatagramPacket(receivePacket.getData(), len, ip, port);
		ErrorSimulatorHelper.send(socket, sendPacket);
		
		ErrorSimulatorHelper.print("Print Modified Packet:");
		ErrorSimulatorHelper.printPacket(sendPacket);
		
		return true;//true means error packet replace normal packet
		
	}
	
	/*
	printOptions(new String[]{
			"<Choose Field>",//| Opcode |   Block #  |
            "1     Opcode",
            "2     Block #"
            });
    userChoice[fieldIndex] = getUserInput(1, 2);
    */
	public boolean simulateCorruptedAck(InetAddress ip, int port, int field) {
		userChoice[0] = 0;//to mark that the error was simulated, do not simulate it again
		
		System.out.println("!");
		System.out.println("<simulateCorruptedAck>");
		System.out.println("!");
		
		if (field == 1) {
			receivePacket.getData()[0] = (byte) 255;
			receivePacket.getData()[1] = (byte) 255;
		} else if (field == 2) {
			receivePacket.getData()[2] = (byte) 255;
			receivePacket.getData()[3] = (byte) 255;
		} else {
			System.out.println("simulateCorruptedAck() unknown field");
			return false;//unknown field
		}

		sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), ip,	port);
		ErrorSimulatorHelper.send(socket, sendPacket);
		
		ErrorSimulatorHelper.print("Print Modified Packet:");
		ErrorSimulatorHelper.printPacket(sendPacket);
		
		return true;//true means error packet replace normal packet
		
	}
	
	/*
	printOptions(new String[]{
			"<Choose Field>",
            "1     Opcode",
            "2     ErrorCode",
            "3     ErrMsg",
            "4     Null byte {0}"
            });
    userChoice[fieldIndex] = getUserInput(1, 4);
    */
	public boolean simulateCorruptedError(InetAddress ip, int port, int field) {
		userChoice[0] = 0;//to mark that the error was simulated, do not simulate it again

		System.out.println("!");
		System.out.println("<simulateCorruptedError>");
		System.out.println("!");
		
		int len = receivePacket.getLength();
		int first = -1;
		int pos = 4;
		
		for (; pos < len; pos++) {
			if (receivePacket.getData()[pos] == 0) {first = pos; break;}
		}		
		if ((first < 0) || (first != len - 1)) {
			System.out.println("<Error> invalid format, null byte at position" + first);
			ErrorSimulatorHelper.print("Print receivePacket:");
			ErrorSimulatorHelper.printPacket(receivePacket);
			//return false;
		}
		
		if (field == 1) {
			receivePacket.getData()[0] = (byte) 255;
			receivePacket.getData()[1] = (byte) 255;
		} else if (field == 2) {
			receivePacket.getData()[2] = (byte) 255;
			receivePacket.getData()[3] = (byte) 255;
		} else if (field == 3) {
			for (int i = 4; i < first; i++) {
				receivePacket.getData()[i] = (byte) 255;
			}
		} else if (field == 4) {
			receivePacket.getData()[first] = (byte) 255;
		} else {
			System.out.println("simulateCorruptedError() can not generate error, unknown field");
			return false;//unknown field
		}

		sendPacket = new DatagramPacket(receivePacket.getData(), len, ip, port);
		ErrorSimulatorHelper.send(socket, sendPacket);
		
		ErrorSimulatorHelper.print("Print Modified Packet:");
		ErrorSimulatorHelper.printPacket(sendPacket);
		
		return true;//true means error packet replace normal packet
		
	}
	
	
	
	
	
	
	

	
	
	
	
	
	
	
	
	
	
	

	
	/*
	printOptions(new String[]{
			"<Choose Field>",
            "1     Opcode",
            "2     File Name",
            "3     Null byte 1 {0}",
            "4     Mode",
            "5     Null byte 2 {0}"
            });
    userChoice[fieldIndex] = getUserInput(1, 5);
    */
	public boolean simulateRemoveRequest(InetAddress ip, int port, int field) {
		userChoice[0] = 0;//to mark that the error was simulated, do not simulate it again
		
		System.out.println("!");
		System.out.println("<simulateRemoveRequest>");
		System.out.println("!");
		
		int len = receivePacket.getLength();
		int first = -1;
		int second = -1;
		int pos = 2;
		
		for (; pos < len; pos++) {
			if (receivePacket.getData()[pos] == 0) {first = pos; break;}
		}
		pos++;
		for (; pos < len; pos++) {
			if (receivePacket.getData()[pos] == 0) {second = pos; break;}
		}
		
		if ((second < 0) || (second != len - 1)) {
			System.out.println("<Error> invalid format, second null byte at position" + second);
			ErrorSimulatorHelper.print("Print receivePacket:");
			ErrorSimulatorHelper.printPacket(receivePacket);
			//return false;
		}
		
		byte[] newData = new byte[len];
		
		if (field == 1) {//Opcode",
			System.arraycopy(receivePacket.getData(), 2, newData, 0, len - 2);
			len = len - 2;
		} else if (field == 2) {//File Name",
			System.arraycopy(receivePacket.getData(), 0, newData, 0, 2);//copy until just before fielname
			int lenFileName = first - 2;//start at 2, end at (first -1)
			System.arraycopy(receivePacket.getData(), first, newData, 2, len - lenFileName - 2);
			len = len - lenFileName;
		} else if (field == 3) {//Null byte 1 {0}",
			System.arraycopy(receivePacket.getData(), 0, newData, 0, first);//copy until just before first
			System.arraycopy(receivePacket.getData(), first+1, newData, first, len - first - 1);//just after first -> end
			len = len - 1;
		} else if (field == 4) {//Mode",
			System.arraycopy(receivePacket.getData(), 0, newData, 0, first + 1);//copy until just before mode
			newData[first + 1] = 0;//this is second null byte
			int lenMode = second - first - 1;
			len = len - lenMode;
		} else if (field == 5) {
			System.arraycopy(receivePacket.getData(), 0, newData, 0, len-1);
			len--;
		} else {
			System.out.println("<Error> unknown field");
			return false;//unknown field
		}

		sendPacket = new DatagramPacket(newData, len, ip, port);
		ErrorSimulatorHelper.send(socket, sendPacket);
		
		ErrorSimulatorHelper.print("Print Modified Packet:");
		ErrorSimulatorHelper.printPacket(sendPacket);
		
		return true;//true means error packet replace normal packet
	}
	
	/*
	printOptions(new String[]{
			"<Choose Field>",//Opcode |   Block #  |   Data
            "1     Opcode",
            "2     Block #",
            "3     Data"
            });
    userChoice[fieldIndex] = getUserInput(1, 3);
    */
	public boolean simulateRemoveData(InetAddress ip, int port, int field) {
		userChoice[0] = 0;//to mark that the error was simulated, do not simulate it again
		
		System.out.println("!");
		System.out.println("<simulateRemoveData>");
		System.out.println("!");
		
		int len = receivePacket.getLength();
		byte[] newData = new byte[len];
		
		if (field == 1) {
			System.arraycopy(receivePacket.getData(), 2, newData, 0, len - 2);
			len = len - 2;
		} else if (field == 2) {
			newData[0] = receivePacket.getData()[0];
			newData[1] = receivePacket.getData()[1];
			System.arraycopy(receivePacket.getData(), 4, newData, 2, len - 4);
			len = len - 2;
		} else if (field == 3) {
			System.arraycopy(receivePacket.getData(), 0, newData, 0, 4);
			len = 4;
		} else {
			System.out.println("simulateRemoveData:  unknown field");
			return false;//unknown field
		}

		sendPacket = new DatagramPacket(newData, len, ip, port);
		ErrorSimulatorHelper.send(socket, sendPacket);
		
		ErrorSimulatorHelper.print("Print Modified Packet:");
		ErrorSimulatorHelper.printPacket(sendPacket);
		
		return true;//true means error packet replace normal packet
		
	}
	
	/*
	printOptions(new String[]{
			"<Choose Field>",//| Opcode |   Block #  |
            "1     Opcode",
            "2     Block #"
            });
    userChoice[fieldIndex] = getUserInput(1, 2);
    */
	public boolean simulateRemoveAck(InetAddress ip, int port, int field) {
		userChoice[0] = 0;//to mark that the error was simulated, do not simulate it again
		
		System.out.println("!");
		System.out.println("<simulateRemoveAck>");
		System.out.println("!");
		
		int len = 2;
		byte[] newData = new byte[len];
		
		if (field == 1) {
			newData[0] = receivePacket.getData()[2];
			newData[1] = receivePacket.getData()[3];
		} else if (field == 2) {
			newData[0] = receivePacket.getData()[0];
			newData[1] = receivePacket.getData()[1];
		} else {
			System.out.println("simulateRemoveAck() unknown field");
			return false;//unknown field
		}
		
		sendPacket = new DatagramPacket(newData, len, ip,	port);
		ErrorSimulatorHelper.send(socket, sendPacket);
		
		ErrorSimulatorHelper.print("Print Modified Packet:");
		ErrorSimulatorHelper.printPacket(sendPacket);
		
		return true;//true means error packet replace normal packet
		
	}
	
	/*
	printOptions(new String[]{
			"<Choose Field>",
            "1     Opcode",
            "2     ErrorCode",
            "3     ErrMsg",
            "4     Null byte {0}"
            });
    userChoice[fieldIndex] = getUserInput(1, 4);
    */
	public boolean simulateRemoveError(InetAddress ip, int port, int field) {
		userChoice[0] = 0;//to mark that the error was simulated, do not simulate it again

		System.out.println("!");
		System.out.println("<simulateRemoveError>");
		System.out.println("!");
		
		int len = receivePacket.getLength();
		int first = -1;
		int pos = 4;
		
		for (; pos < len; pos++) {
			if (receivePacket.getData()[pos] == 0) {first = pos; break;}
		}		
		if ((first < 0) || (first != len - 1)) {
			System.out.println("<Error> invalid format, null byte at position" + first);
			ErrorSimulatorHelper.print("Print receivePacket:");
			ErrorSimulatorHelper.printPacket(receivePacket);
			//return false;
		}
		
		byte[] newData = new byte[len];
		
		if (field == 1) {//Opcode",
			System.arraycopy(receivePacket.getData(), 2, newData, 0, len - 2);
			len = len - 2;
		} else if (field == 2) {//ErrorCode
			newData[0] = receivePacket.getData()[0];
			newData[1] = receivePacket.getData()[1];
			System.arraycopy(receivePacket.getData(), 4, newData, 2, len - 4);
			len = len - 2;
		} else if (field == 3) {//ErrMsg",
			System.arraycopy(receivePacket.getData(), 0, newData, 0, 4);
			newData[4] = 0;
			len = 5;
		} else if (field == 4) {//Null byte 1 {0}",
			System.arraycopy(receivePacket.getData(), 0, newData, 0, len-1);
			len--;
		}  else {
			System.out.println("<Error> unknown field");
			return false;//unknown field
		}

		sendPacket = new DatagramPacket(newData, len, ip, port);
		ErrorSimulatorHelper.send(socket, sendPacket);
		
		ErrorSimulatorHelper.print("Print Modified Packet:");
		ErrorSimulatorHelper.printPacket(sendPacket);
		
		return true;//true means error packet replace normal packet
		
	}
	
}
