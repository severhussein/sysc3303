import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

/**
 * IntHostManager for multi threads need ErrorSimulatorHelper.java
 * @author Dawei Chen 101020959
 */

public class ErrorSimulatorThread implements Runnable {

	private DatagramSocket socket;
	private DatagramPacket sendPacket, receivePacket;
	private int clientPort, serverPort;
	private int[] userChoice;
	
	public ErrorSimulatorThread(DatagramPacket receivePacket, int[] userChoice) {
			this.receivePacket = receivePacket;
			this.userChoice = userChoice;
	}

	public void run() {
		
		this.serverPort = -1;
		this.clientPort = receivePacket.getPort();
		this.socket = ErrorSimulatorHelper.newSocket();
		
		//////////////////"Sending to server..."//////////////////////////////////////
		
		if (!simulateError(ErrorSimulator.DEFAULT_SERVER_PORT)) {
			System.out.print("Sending to server...");
			sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), ErrorSimulator.DEFAULT_SERVER_PORT);
			ErrorSimulatorHelper.send(socket, sendPacket);
			//System.out.print("Sent");
			System.out.print("    |Port "+ ErrorSimulator.DEFAULT_SERVER_PORT);
			System.out.print("    |Opcode "+ getOpcode());
			System.out.println("    |BLK#"+ getBlockNum());
			//clean printing//Utils.tryPrintTftpPacket(sendPacket);
		}
		//round++;// first round of request msg was done, increase i here
		//////////////////"Sending to server..."//////////////////////////////////////
		
		
		boolean serverPortUpdated = false;
		int receivedPort = -1;
		
		while (true) {

			
			//////////////////"Receiving..."//////////////////////////////////////
			System.out.print("Receiving...        ");
			receivePacket = ErrorSimulatorHelper.newReceive();
			ErrorSimulatorHelper.receive(socket, receivePacket);//form a new packet
			//clean printing//Utils.tryPrintTftpPacket(receivePacket);
			//System.out.print("Received");
			receivedPort = receivePacket.getPort();
			System.out.print("    |port "+ receivedPort);
			System.out.print("    |Opcode "+ getOpcode());
			System.out.println("    |BLK#"+ getBlockNum());
			//////////////////"Received"//////////////////////////////////////
			
			if (serverPortUpdated &&(receivedPort == clientPort)) {
				
				//////////////////"Sending to server..."//////////////////////////////////////
				if (!simulateError(serverPort)) {
					System.out.print("Sending to server...");
					sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), serverPort);
					ErrorSimulatorHelper.send(socket, sendPacket);
					//System.out.print("Sent");
					System.out.print("    |port "+ serverPort);
					System.out.print("    |Opcode "+ getOpcode());
					System.out.println("    |BLK#"+ getBlockNum());
					//clean printing//Utils.tryPrintTftpPacket(sendPacket);
				}
				//////////////////"Sending to server..."//////////////////////////////////////
			} else if ( (!serverPortUpdated && (receivedPort != clientPort))
					 || (serverPortUpdated && (receivedPort == serverPort)) ) {
				
				if (!serverPortUpdated) {
					serverPort = receivePacket.getPort();
					serverPortUpdated = true;
				} // get server port here!
				
				//////////////////"Sending to Client...\n"//////////////////////////////////////
				if (!simulateError(clientPort)) {
					System.out.print("Sending to Client...");
					sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), clientPort);
					ErrorSimulatorHelper.send(socket, sendPacket);
					//System.out.print("Sent");
					System.out.print("    |port "+ clientPort);
					System.out.print("    |Opcode "+ getOpcode());
					System.out.println("    |BLK#"+ getBlockNum());
					//clean printing//Utils.tryPrintTftpPacket(sendPacket);
				}
				//////////////////"Sending to Client...\n"//////////////////////////////////////
			} else if (!serverPortUpdated && receivedPort == clientPort) {
				System.out.println("Order incorrect, receive packet from client when server port not set");
			} else {//remaining case is: serverPortUpdated && (port is neither client nor server)
				System.out.println("unknown port received");
			}


		}
	}
	
	
	

	
	
	public boolean simulateError(int port){
			
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
		    	return simulateDuplicate(port, userChoice[valueIndex]);
		    } else if (userChoice[typeIndex] == 2) {
		    	return simulateDelayed(port, userChoice[valueIndex]);
		    } else if (userChoice[typeIndex] == 3) {
		    	return simulateLost(port, userChoice[valueIndex]);
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

	    	
		    if (userChoice[problemIndex] == 3) {//"3     Invalid TID"
		    	return simulateIncorrectTID(port);
		    }
		    if (userChoice[problemIndex] == 2) {//"2     Incorrect Size",
		    	return simulateIncorrectSize(port, userChoice[sizeIndex]);
		    }
		    if (userChoice[problemIndex] == 1) {//"1     Corruptted Field",
			    if (userChoice[packetIndex] == 1 || userChoice[packetIndex] == 2) {
			    	return simulateCorruptedRequest(port, userChoice[fieldIndex]);
			    } else if (userChoice[packetIndex] == 3) {
			    	return simulateCorruptedData(port, userChoice[fieldIndex]);
			    } else if (userChoice[packetIndex] == 4) {
			    	return simulateCorruptedAck(port, userChoice[fieldIndex]);
			    } else if (userChoice[packetIndex] == 5) {
			    	return simulateCorruptedError(port, userChoice[fieldIndex]);
			    }
		    }
		    if (userChoice[problemIndex] == 0) {//"1     Remove Field",
			    if (userChoice[packetIndex] == 1 || userChoice[packetIndex] == 2) {
			    	return simulateRemoveRequest(port, userChoice[fieldIndex]);
			    } else if (userChoice[packetIndex] == 3) {
			    	return simulateRemoveData(port, userChoice[fieldIndex]);
			    } else if (userChoice[packetIndex] == 4) {
			    	return simulateRemoveAck(port, userChoice[fieldIndex]);
			    } else if (userChoice[packetIndex] == 5) {
			    	return simulateRemoveError(port, userChoice[fieldIndex]);
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
	
	
	public int getBlockNum(){
		//invalid opcode
    	if (receivePacket.getData()[0] != (byte) 0) return -1;
    	//check block number
    	if (receivePacket.getData()[1] == (byte)3 || receivePacket.getData()[1] == (byte)4) {
	    	return (int) (receivePacket.getData()[2] * Math.pow(2, 8) + receivePacket.getData()[3]);
    	}
    	return -1;
	}

	public String getOpcode(){
	    	return ""+receivePacket.getData()[0]+ receivePacket.getData()[1];
	}
	

	
	public boolean simulateDuplicate(int port, int value) {
		userChoice[0] = 0;//to mark that the error was simulated, do not simulate it again
		
		if (value<0) value = 1;
		for (int i=0; i<value; i++) {
			System.out.println("!");
			System.out.println("<simulateDuplicate>");
			System.out.println("!");
			sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), port);
			ErrorSimulatorHelper.send(socket, sendPacket);
		}
		return false;//false means error packet do not replace normal packet
	}
	
	public boolean simulateDelayed(int port, int value) {
		userChoice[0] = 0;//to mark that the error was simulated, do not simulate it again
		
		System.out.println("!");
		System.out.println("<simulateDelayed>");
		System.out.println("!");
        try {
            Thread.sleep(value);
        } catch (InterruptedException e) {}
        return false;//false means error packet do not replace normal packet
	}
	
	public boolean simulateLost(int port, int value) {
		userChoice[0] = 0;//to mark that the error was simulated, do not simulate it again
		
		if (value<0) value = 1;
		for (int i=0; i<value; i++) {
			System.out.println("!");
			System.out.println("<simulateLost>");
			System.out.println("!");
			
			receivePacket = ErrorSimulatorHelper.newReceive();
			ErrorSimulatorHelper.receive(socket, receivePacket);
			
		}
		return false;//false means error packet do not replace normal packet
	}

	
	public boolean simulateIncorrectTID(int port) {
		userChoice[0] = 0;//to mark that the error was simulated, do not simulate it again
		
		System.out.println("!");
		System.out.println("<simulateIncorrectTID>");
		System.out.println("!");
		
		DatagramSocket new_socket = ErrorSimulatorHelper.newSocket();

		//System.out.print("<Error TID> Sending to port...");
		sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), port);
		ErrorSimulatorHelper.send(new_socket, sendPacket);
		//clean printing//Utils.tryPrintTftpPacket(sendPacket);
		//System.out.print("    |port "+ port);
		//System.out.print("    |Opcode "+ getOpcode());
		//System.out.println("    |BLK#"+ getBlockNum());
		return false;//false means error packet do not replace normal packet
	}
	
	
	
	
	public boolean simulateIncorrectSize(int port, int errorSize) {
		userChoice[0] = 0;//to mark that the error was simulated, do not simulate it again
		System.out.println("!");
		System.out.println("<simulateIncorrectSize>");
		System.out.println("!");
		
		int len = receivePacket.getLength();
		if (errorSize > len) {
			byte[] newData = new byte[errorSize];
			Arrays.fill(newData, (byte) 255);
			System.arraycopy(receivePacket.getData(), 0, newData, 0, len);
		}
		
		sendPacket = new DatagramPacket(receivePacket.getData(), errorSize , receivePacket.getAddress(), port);
		ErrorSimulatorHelper.send(socket, sendPacket);
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
	public boolean simulateCorruptedRequest(int port, int field) {
		userChoice[0] = 0;//to mark that the error was simulated, do not simulate it again
		
		System.out.println("!");
		System.out.println("<simulateCorruptedRequest>");
		System.out.println("!");
		
		int len = receivePacket.getLength();
		
		if (field == 1) {
			receivePacket.getData()[0] = (byte) 255;
			receivePacket.getData()[1] = (byte) 255;
		} else if (field == 2) {
			for (int i = 2; i < len; i++) {
				if (receivePacket.getData()[i] == 0) break;
				receivePacket.getData()[i] = (byte) 255;
			}
		} else if (field == 3) {
			for (int i = 2; i < len; i++) {
				if (receivePacket.getData()[i] == 0) {
					receivePacket.getData()[i] = (byte) 255;
					break;
				}
			}
		} else if (field == 4) {
			for (int i = 2; i < len; i++) {
				if (receivePacket.getData()[i] == 0) {
					for (int j = i+1; j < len; j++) {
						if (receivePacket.getData()[j] == 0) break;
						receivePacket.getData()[j] = (byte) 255;
					}
					break;
				}
			}
		} else if (field == 5) {
			receivePacket.getData()[len - 1] = (byte) 255;
		} else {
			System.out.println("simulateCorruptedRequest() unknown field");
			return false;//unknown field
		}

		sendPacket = new DatagramPacket(receivePacket.getData(), len, receivePacket.getAddress(), port);
		ErrorSimulatorHelper.send(socket, sendPacket);
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
	public boolean simulateCorruptedData(int port, int field) {
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

		sendPacket = new DatagramPacket(receivePacket.getData(), len, receivePacket.getAddress(), port);
		ErrorSimulatorHelper.send(socket, sendPacket);
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
	public boolean simulateCorruptedAck(int port, int field) {
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

		sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(),	port);
		ErrorSimulatorHelper.send(socket, sendPacket);
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
	public boolean simulateCorruptedError(int port, int field) {
		userChoice[0] = 0;//to mark that the error was simulated, do not simulate it again

		System.out.println("!");
		System.out.println("<simulateCorruptedError>");
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
				if (receivePacket.getData()[i] == 0) break;
				receivePacket.getData()[i] = (byte) 255;
			}
		} else if (field == 4) {
			receivePacket.getData()[len - 1] = (byte) 255;
		} else {
			System.out.println("simulateCorruptedError() can not generate error, unknown field");
			return false;//unknown field
		}

		sendPacket = new DatagramPacket(receivePacket.getData(), len, receivePacket.getAddress(), port);
		ErrorSimulatorHelper.send(socket, sendPacket);
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
	public boolean simulateRemoveRequest(int port, int field) {
		userChoice[0] = 0;//to mark that the error was simulated, do not simulate it again
		
		System.out.println("!");
		System.out.println("<simulateRemoveRequest>");
		System.out.println("!");
		
		int len = receivePacket.getLength();
		byte[] newData = new byte[len];
		
		int first = -1;
		int second = -1;
		int i = 2;
		for (; i < len; i++) {
			if (receivePacket.getData()[i] == 0) {
				first = i;
				break;
			}
		}
		i++;
		for (; i < len; i++) {
			if (receivePacket.getData()[i] == 0) {
				second = i;
				break;
			}
		}
		if (second < 0) {
			System.out.println("<Error> invalid format");
			return false;
		}
		
		
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
			len--;
		} else {
			System.out.println("<Error> unknown field");
			return false;//unknown field
		}

		sendPacket = new DatagramPacket(newData, len, receivePacket.getAddress(), port);
		ErrorSimulatorHelper.send(socket, sendPacket);
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
	public boolean simulateRemoveData(int port, int field) {
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

		sendPacket = new DatagramPacket(newData, len, receivePacket.getAddress(), port);
		ErrorSimulatorHelper.send(socket, sendPacket);
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
	public boolean simulateRemoveAck(int port, int field) {
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
		
		sendPacket = new DatagramPacket(newData, len, receivePacket.getAddress(),	port);
		ErrorSimulatorHelper.send(socket, sendPacket);
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
	public boolean simulateRemoveError(int port, int field) {
		userChoice[0] = 0;//to mark that the error was simulated, do not simulate it again

		System.out.println("!");
		System.out.println("<simulateRemoveError>");
		System.out.println("!");
		
		int len = receivePacket.getLength();
		byte[] newData = new byte[len];
		
		int first = -1;
		int i = 4;
		for (; i < len; i++) {
			if (receivePacket.getData()[i] == 0) {
				first = i;
				break;
			}
		}
		if (first < 0) {
			System.out.println("<Error> invalid format");
			return false;
		}
		
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
			len = len - 1;
		}  else {
			System.out.println("<Error> unknown field");
			return false;//unknown field
		}

		sendPacket = new DatagramPacket(newData, len, receivePacket.getAddress(), port);
		ErrorSimulatorHelper.send(socket, sendPacket);
		return true;//true means error packet replace normal packet
		
	}
	
	
	
	
	
	
	
	

}

/*
public void simulate_wrong_opcode(int port) {

	System.out.println("simulate wrong opcode to port: " + port + "\n");
	receivePacket.getData()[0] = (byte) 255;
	receivePacket.getData()[1] = (byte) 255;
	sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(),
			port);
	ErrorSimulatorHelper.send(socket, sendPacket);
	//clean printing//Utils.tryPrintTftpPacket(sendPacket);
}



public void simulate_wrong_blockNum(int port) {

	System.out.println("simulate wrong size packet to port: " + port + "\n");
	receivePacket.getData()[2] = (byte) 255;
	receivePacket.getData()[3] = (byte) 255;
	sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength() , receivePacket.getAddress(),
			port);
	ErrorSimulatorHelper.send(socket, sendPacket);
	//clean printing//Utils.tryPrintTftpPacket(sendPacket);
}
*/





/*
if (i == packetNum && mode == 2) {
	simulate_wrong_port(serverPort);// 1 is to server
}
if (i == packetNum && mode == 4) {
	simulate_wrong_opcode(serverPort);// 1 is to server
} else if (i == packetNum && mode == 6) {
	simulate_wrong_size(serverPort, 1234);// 1 is to server
} else if (i == packetNum && mode == 8) {
	simulate_wrong_blockNum(serverPort);// 1 is to server
} else {

}
*/

/*
if (i == packetNum && mode == 1) {
	simulate_wrong_port(clientPort);// to client
}
if (i == packetNum && mode == 3) {
	simulate_wrong_opcode(clientPort);// to client
} else if (i == packetNum && mode == 5) {
	simulate_wrong_size(clientPort);// to client
} else if (i == packetNum && mode == 7) {
	simulate_wrong_blockNum(clientPort);// to client
} else {

}
*/
