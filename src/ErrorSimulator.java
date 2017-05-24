import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.Scanner;

/**
 * IntHostListener to run the Host
 * need ErrorSimulatorHelper.java
 * @author Dawei Chen 101020959
 */

public class ErrorSimulator {
	public static final int DEFAULT_HOST_PORT = 23, DEFAULT_SERVER_PORT = 69, PACKAGE_SIZE = 1000;
	//public static boolean PRINT_PACKET = false;
	
	private DatagramSocket receiveSocket;
	private DatagramPacket receivePacket;
	
	//public static int mode, errorSize, packetNum = -1;
	public static Scanner sc;

	public ErrorSimulator() {
		receiveSocket = ErrorSimulatorHelper.newSocket(DEFAULT_HOST_PORT);
	}
	
	public void receiveRequests(int[] userChoice) {
		System.out.println("Host waiting for requests...\n");
		receivePacket = ErrorSimulatorHelper.newReceive(PACKAGE_SIZE);
		ErrorSimulatorHelper.receive(receiveSocket, receivePacket);
		//clean printing//Utils.tryPrintTftpPacket(receivePacket);
		
		System.out.println("Creating new thread...\n");
		new Thread(new ErrorSimulatorThread(receivePacket, userChoice)).start();
	}	
	
	public static void main( String args[] ) {
		sc = new Scanner(System.in);
		int[] userChoice = new int[5];
		Arrays.fill(userChoice, -1);
		
		String[] optionList0 = {
				"<Choose Error Type> (Network Issue or Invalid Data)",
	            "1     Invalid Data",
	            "2     Network Issue"
		};
	    printOptions(optionList0);
	    userChoice[0] = getUserInput(1, 2);
	    
	    if (userChoice[0] == 2) {
	    	
	    	int typeIndex = 1;
	    	int valueIndex = 2;
	    	int packetIndex = 3;
	    	int blockIndex = 4;
	    	
	    	
			String[] optionList1 = {
					"<Choose Network Issue Type>",
		            "1     Duplicate",
		            "2     Delayed",
		            "3     Lost"
			};
		    printOptions(optionList1);
		    userChoice[typeIndex] = getUserInput(1, 3);
		    
		    if (userChoice[typeIndex] == 1) {
				String[] optionList2 = {
						"<How many Duplicate packets?>",
						"?     (Any Integer >= 1)"
				};
			    printOptions(optionList2);
			    userChoice[valueIndex] = getUserInput(1);
		    } else if  (userChoice[typeIndex] == 2) {
				String[] optionList2 = {
						"<How many mili seconds?>",
						"?     (Any Integer >= 1)"
				};
			    printOptions(optionList2);
			    userChoice[valueIndex] = getUserInput(1);
		    } else if  (userChoice[typeIndex] == 2) {
				String[] optionList2 = {
						"<How many packets lost in a row?>",
						"?     (Any Integer >= 1)"
				};
			    printOptions(optionList2);
			    userChoice[valueIndex] = getUserInput(1);
		    }
		    
			String[] optionList3 = {
					"<Choose Packet Type> (Error in what type of packet?)",
		            "1     Read request (RRQ)",
		            "2     Write request (WRQ)",
		            "3     Data (DATA)",
		            "4     Acknowledgment (ACK)",
		            "5     Error (ERROR)"
			};
		    printOptions(optionList3);
		    userChoice[packetIndex] = getUserInput(1, 5);
		    
			String[] optionList4 = { 
					"<Choose the Block #>"
			};
		    printOptions(optionList4);
		    userChoice[blockIndex] = getUserInput(0);
		    
	    } else if (userChoice[0] == 1){
	    	
	    	int problemIndex = 1;
	    	int packetIndex = 2;
	    	int fieldIndex = 3;
	    	int sizeIndex = 3;
	    	int blockIndex = 4;
	    	
			String[] optionList1 = { 
					"<Choose Problem Type>",
		            "1     Corruptted Field",
		            "2     Incorrect Size",
		            "3     Invalid TID"
			};
		    printOptions(optionList1);
		    userChoice[problemIndex] = getUserInput(1, 3);
		    
			String[] optionList2 = {
					"<Choose Packet> (Error in what type of packet?)",
		            "1     Read request (RRQ)",
		            "2     Write request (WRQ)",
		            "3     Data (DATA)",
		            "4     Acknowledgment (ACK)",
		            "5     Error (ERROR)"
			};
		    printOptions(optionList2);
		    userChoice[packetIndex] = getUserInput(1, 5);
		    
		    
		    if (userChoice[problemIndex] == 2) {
				String[] optionList3 = { 
						"<Choose Size #>"
				};
			    printOptions(optionList3);
			    userChoice[sizeIndex] = getUserInput(0);
		    }
		    
		    if (userChoice[problemIndex] == 1) {
			    if (userChoice[packetIndex] == 1 || userChoice[packetIndex] == 2) {
					String[] optionList3 = { //| Opcode |  Filename  |   0  |    Mode    |   0  |
							"<Choose Field>",
				            "1     Opcode",
				            "2     File Name",
				            "3     Null byte 1 {0}",
				            "4     Mode",
				            "5     Null byte 2 {0}"
					};
				    printOptions(optionList3);
				    userChoice[fieldIndex] = getUserInput(1, 5);
			    } else if (userChoice[packetIndex] == 3) {
					String[] optionList3 = { 
							"<Choose Field>",//Opcode |   Block #  |   Data
				            "1     Opcode",
				            "2     Block #",
				            "3     Data"
					};
				    printOptions(optionList3);
				    userChoice[fieldIndex] = getUserInput(1, 3);
			    } else if (userChoice[packetIndex] == 4) {
					String[] optionList3 = { 
							"<Choose Field>",//| Opcode |   Block #  |
				            "1     Opcode",
				            "2     Block #"
					};
				    printOptions(optionList3);
				    userChoice[fieldIndex] = getUserInput(1, 2);
			    } else if (userChoice[packetIndex] == 5) {
					String[] optionList3 = { // | Opcode |  ErrorCode |   ErrMsg   |   0  |
							"<Choose Field>",
				            "1     Opcode",
				            "2     ErrorCode",
				            "3     ErrMsg",
				            "4     Null byte {0}"
					};
				    printOptions(optionList3);
				    userChoice[fieldIndex] = getUserInput(1, 4);
			    }
		    }

		    
			String[] optionList4 = { 
					"<Choose the Block #>"
			};
		    printOptions(optionList4);
		    userChoice[blockIndex] = getUserInput(0);
		    
	    }
	    
	    sc.close();
	    		
		ErrorSimulator s = new ErrorSimulator();
		while(true) {
			s.receiveRequests(userChoice);
		}
	}
	
	public static void printOptions(String[] optionList) {
		for (String str: optionList) {
			System.out.println(str);
		}
	}
	
	public static int getUserInput(int min, int max) {
		if (min < 0) min = 0;
		if (max < min) {
			System.out.println("getUserInput() error input, max < min");
			max = min;
		}
		
		String str = "";
		int number = -1;
		while (number < min || number > max) {
			str = sc.next();
			try {
				number = Integer.parseInt(str);
			} catch (NumberFormatException e) {
				System.out.println("You've entered non-integer number");
				//System.out.println("This caused " + e);
			}
		} 
		return number;
	}
	
	public static int getUserInput(int min) {
		if (min < 0) min = 0;
		String str = "";
		int number = -1;
		while (number < min) {
			str = sc.next();
			try {
				 number = Integer.parseInt(str);
			} catch (NumberFormatException e) {
				System.out.println("You've entered non-integer number");
				//System.out.println("This caused " + e);
			}
		}
		return number;
	}

}
	
	
	/*
		System.out.println("Type the digit number to choose which error you want to simulate");
		System.out.println("0 = No Error");
		System.out.println("1 = send to Client, Port is incorrect (Error type 5)");
		System.out.println("2 = send to Server, Port is incorrect (Error type 5)");
		System.out.println("3 = send to Client, Opcode is incorrect (Mess 0th & 1st byte, Error type 4)");
		System.out.println("4 = send to Server, Opcode is incorrect (Mess 0th & 1st byte, Error type 4)");
		System.out.println("5 = send to Client, Packet Size is incorrect (size modified, Error type 4)");
		System.out.println("6 = send to Server, Packet Size is incorrect (size modified, Error type 4)");
		System.out.println("7 = send to Client, BlockNum is incorrect (Mess 2nd & 3rd byte, Error type 4)");
		System.out.println("8 = send to Server, BlockNum is incorrect (Mess 2nd & 3rd byte, Error type 4)");
	 

	public static int decidePacketNum() {
		//Scanner sc = new Scanner(System.in);
		System.out.println("Type the digit number to choose which packet you want to insert the error");
		String str = "";
		int number = -1;
		while (number < 0) {
			str = sc.next();
 			 try {
				 number = Integer.parseInt(str);
 			 } catch (NumberFormatException e) {
				 number = -1;
				 System.out.println("Input was not number, please try again");
 			 }
		}
		System.out.println("Will insert at the #" + number + " of packet");
		return number;
	}
	
	public static int decideErrorSize() {
		//Scanner sc = new Scanner(System.in);
		System.out.println("Type the size value, which the error packet would become that size");
		String str = "";
		int number = -1;
		while (number < 0) {
			str = sc.next();
 			 try {
				 number = Integer.parseInt(str);
 			 } catch (NumberFormatException e) {
				 number = -1;
				 System.out.println("Input was not number, please try again");
 			 }
		}
		System.out.println("size set to value: " + number);
		return number;
	}
	*/
