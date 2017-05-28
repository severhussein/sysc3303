import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * IntHostManager for multi threads need ErrorSimulatorHelper.java
 * @author Dawei Chen 101020959
 */

public class ErrorSimulatorThread implements Runnable {

	private DatagramSocket socket;
	private DatagramPacket sendPacket, receivePacket;
	private int clientPort, serverPort;
	private int[] userChoice;

	boolean skipNormalPacketSend;
	
	public ErrorSimulatorThread(DatagramPacket receivePacket, int[] userChoice) {
		socket = ErrorSimulatorHelper.newSocket();
		if (socket != null) {
			this.serverPort = ErrorSimulator.DEFAULT_SERVER_PORT;
			this.clientPort = receivePacket.getPort();
			this.receivePacket = receivePacket;
			this.userChoice = userChoice;
		}
	}

	public void run() {

		boolean serverPortUpdated = false;
		//int block = 0;
		skipNormalPacketSend = false;
				
		while (true) {
			//////////////////"Sending to server..."//////////////////////////////////////
			simulateError(serverPort);
			
			if (!skipNormalPacketSend) {
				System.out.print("Sending to server...");
				sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), serverPort);
				ErrorSimulatorHelper.send(socket, sendPacket);
				System.out.print("Sent");
				System.out.println(" #"+ getBlockNum());
				//clean printing//Utils.tryPrintTftpPacket(sendPacket);
			}
			skipNormalPacketSend = false;
			//round++;// first round of request msg was done, increase i here
			//////////////////"Sending to server..."//////////////////////////////////////

			//////////////////"Receiving from server..."//////////////////////////////////////
			System.out.print("Receiving from server...");
			receivePacket = ErrorSimulatorHelper.newReceive();
			ErrorSimulatorHelper.receive(socket, receivePacket);//form a new packet
			//clean printing//Utils.tryPrintTftpPacket(receivePacket);
			System.out.print("Received");
			System.out.println(" #"+ getBlockNum());
			
			if (!serverPortUpdated) {
				serverPort = receivePacket.getPort();
				serverPortUpdated = true;
			} // get server port here!
			//////////////////"Receiving from server..."//////////////////////////////////////
			
			//////////////////"Sending to Client...\n"//////////////////////////////////////
			simulateError(clientPort);
			
			if (!skipNormalPacketSend) {
				System.out.print("Sending to Client...");
				sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), clientPort);
				ErrorSimulatorHelper.send(socket, sendPacket);
				System.out.print("Sent");
				System.out.println(" #"+ getBlockNum());
				//clean printing//Utils.tryPrintTftpPacket(sendPacket);
			}
			skipNormalPacketSend = false;
			//////////////////"Sending to Client...\n"//////////////////////////////////////

			//////////////////"Receiving from client...\n"//////////////////////////////////////
			System.out.print("Receiving from client...");
			receivePacket = ErrorSimulatorHelper.newReceive();
			ErrorSimulatorHelper.receive(socket, receivePacket);
			System.out.println("Received");
			System.out.println(" #"+ getBlockNum());
			//clean printing//Utils.tryPrintTftpPacket(receivePacket);
			// ErrorSimulatorHelper.receive(socket, receivePacket);
			//////////////////"Receiving from client...\n"//////////////////////////////////////
		}
	}
	
	
	

	
	
	public void simulateError(int port){
		
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
	    	if (receivePacket.getData()[0] != (byte) 0) return;
	    	if (receivePacket.getData()[1] != (byte) userChoice[packetIndex]) return;
	    	
	    	//check block number
	    	if (receivePacket.getData()[1] == (byte)3 || receivePacket.getData()[1] == (byte)4) {
		    	if (receivePacket.getData()[2] != intToByte(userChoice[blockIndex])[0]) return;
		    	if (receivePacket.getData()[3] != intToByte(userChoice[blockIndex])[1]) return;
	    	}
	    	
					//"<Choose Network Issue Type>",
		            //"1     Duplicate",
		            //"2     Delayed",
		            //"3     Lost"
		    if (userChoice[typeIndex] == 1) {
		    	simulateDuplicate(port, userChoice[valueIndex]);
		    	return;
		    } else if (userChoice[typeIndex] == 2) {
		    	simulateDelayed(port, userChoice[valueIndex]);
		    	return;
		    } else if (userChoice[typeIndex] == 3) {
		    	simulateLost(port, userChoice[valueIndex]);
		    	return;
		    } else {
		    	System.out.println("Unknown Error TypeIndex: "+userChoice[typeIndex]);
		    	return;
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
	    	if (receivePacket.getData()[0] != (byte) 0) return;
	    	if (receivePacket.getData()[1] != (byte) userChoice[packetIndex]) return;
	    	
	    	//check block number
	    	if (receivePacket.getData()[1] == (byte)3 || receivePacket.getData()[1] == (byte)4) {
		    	if (receivePacket.getData()[2] != intToByte(userChoice[blockIndex])[0]) return;
		    	if (receivePacket.getData()[3] != intToByte(userChoice[blockIndex])[1]) return;
	    	}
	    	
					//"<Choose Problem Type>",
		            //"1     Corruptted Field",
		            //"2     Incorrect Size",
		            //"3     Invalid TID"
	    	
		    if (userChoice[problemIndex] == 3) {//"3     Invalid TID"
		    	simulateIncorrectTID(port);
		    	return;
		    }
		    if (userChoice[problemIndex] == 2) {//"2     Incorrect Size",
		    	simulateIncorrectSize(port, userChoice[sizeIndex]);
		    	return;
		    }
		    if (userChoice[problemIndex] == 1) {//"1     Corruptted Field",
			    if (userChoice[packetIndex] == 1 || userChoice[packetIndex] == 2) {
			    	simulateCorruptedRequest(port, userChoice[fieldIndex]);
			    	return;
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
			    } else if (userChoice[packetIndex] == 3) {
			    	simulateCorruptedData(port, userChoice[fieldIndex]);
			    	return;
			    	/*
					printOptions(new String[]{
							"<Choose Field>",//Opcode |   Block #  |   Data
				            "1     Opcode",
				            "2     Block #",
				            "3     Data"
				            });
				    userChoice[fieldIndex] = getUserInput(1, 3);
				    */
			    } else if (userChoice[packetIndex] == 4) {
			    	simulateCorruptedAck(port, userChoice[fieldIndex]);
			    	return;
			    	/*
					printOptions(new String[]{
							"<Choose Field>",//| Opcode |   Block #  |
				            "1     Opcode",
				            "2     Block #"
				            });
				    userChoice[fieldIndex] = getUserInput(1, 2);
				    */
			    } else if (userChoice[packetIndex] == 5) {
			    	simulateCorruptedError(port, userChoice[fieldIndex]);
			    	return;
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
			    }
		    }
		    
		    
	    }
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

	
	

	
	public void simulateDuplicate(int port, int value) {
		if (value<0) value = 1;
		for (int i=0; i<value; i++) {
			System.out.println("!");
			System.out.println("<simulateDuplicate>");
			System.out.println("!");
			sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), port);
			ErrorSimulatorHelper.send(socket, sendPacket);
		}
	}
	
	public void simulateDelayed(int port, int value) {
		System.out.println("!");
		System.out.println("<simulateDelayed>");
		System.out.println("!");
        try {
            Thread.sleep(value);
        } catch (InterruptedException e) {}
	}
	
	public void simulateLost(int port, int value) {
		if (value<0) value = 1;
		for (int i=0; i<value; i++) {
			System.out.println("!");
			System.out.println("<simulateLost>");
			System.out.println("!");
			
			receivePacket = ErrorSimulatorHelper.newReceive();
			ErrorSimulatorHelper.receive(socket, receivePacket);
		}

		
		//skipNormalPacketSend = false;
	}

	
	public void simulateIncorrectTID(int port) {
		System.out.println("!");
		System.out.println("<simulateIncorrectTID>");
		System.out.println("!");
		
		DatagramSocket new_socket = ErrorSimulatorHelper.newSocket();
		sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), port);
		ErrorSimulatorHelper.send(new_socket, sendPacket);
		//clean printing//Utils.tryPrintTftpPacket(sendPacket);

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
	public void simulateCorruptedRequest(int port, int field) {
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
			return;//unknown field
		}

		sendPacket = new DatagramPacket(receivePacket.getData(), len, receivePacket.getAddress(), port);
		ErrorSimulatorHelper.send(socket, sendPacket);
		skipNormalPacketSend = true;
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
	public void simulateCorruptedData(int port, int field) {
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
			return;//unknown field
		}

		sendPacket = new DatagramPacket(receivePacket.getData(), len, receivePacket.getAddress(), port);
		ErrorSimulatorHelper.send(socket, sendPacket);
		skipNormalPacketSend = true;
	}
	
	/*
	printOptions(new String[]{
			"<Choose Field>",//| Opcode |   Block #  |
            "1     Opcode",
            "2     Block #"
            });
    userChoice[fieldIndex] = getUserInput(1, 2);
    */
	public void simulateCorruptedAck(int port, int field) {
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
			return;//unknown field
		}

		sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(),	port);
		ErrorSimulatorHelper.send(socket, sendPacket);
		skipNormalPacketSend = true;
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
	public void simulateCorruptedError(int port, int field) {

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
			System.out.println("simulateCorruptedError() unknown field");
			return;//unknown field
		}

		sendPacket = new DatagramPacket(receivePacket.getData(), len, receivePacket.getAddress(), port);
		ErrorSimulatorHelper.send(socket, sendPacket);
		skipNormalPacketSend = true;
	}
	
	public void simulateIncorrectSize(int port, int errorSize) {
		System.out.println("!");
		System.out.println("<simulateIncorrectSize>");
		System.out.println("!");
		
		sendPacket = new DatagramPacket(receivePacket.getData(), errorSize , receivePacket.getAddress(), port);
		ErrorSimulatorHelper.send(socket, sendPacket);
		skipNormalPacketSend = true;
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
