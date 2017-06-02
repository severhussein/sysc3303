import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
	public static boolean PRINT_PACKET = true;
	
	
	private DatagramSocket receiveSocket;
	private DatagramPacket receivePacket;
	
	private static Scanner sc;
	
	//IP address of server
	private static InetAddress destinationAddress = null;

	public ErrorSimulator() {
		receiveSocket = ErrorSimulatorHelper.newSocket(DEFAULT_HOST_PORT);
	}
	
	public void receiveRequests(int[] userChoice) {
		System.out.println("Host waiting for request...");
		receivePacket = ErrorSimulatorHelper.newReceive(PACKAGE_SIZE);
		ErrorSimulatorHelper.receive(receiveSocket, receivePacket);
		//System.out.print("Received");
		System.out.print("    |Ip "+ receivePacket.getAddress());
		System.out.print("    |port "+ receivePacket.getPort());
		System.out.print("    |Opcode "+ ""+receivePacket.getData()[0]+ receivePacket.getData()[1]);
		System.out.println("    |BLK#"+ (-1));
		//clean printing//Utils.tryPrintTftpPacket(receivePacket);
		
		//System.out.println("Create new thread\n");
		new Thread(new ErrorSimulatorThread(receivePacket, userChoice,destinationAddress)).start();
	}	
	
	public static void main( String args[] ) {
		sc = new Scanner(System.in);
		
		//ask where the destination address is
		//on startup make sure to ask for IP
		System.out.println("Welcome to the Error Simulator:\n");
		if(destinationAddress==null)
			queryDestinationAddress();
		
		//simulate error options
		int[] userChoice = new int[5];
		Arrays.fill(userChoice, -1);
		
		int mainBranchIndex = 0;
		
	    printOptions(new String[]{
				"<Choose Error Type> (Network Issue or Invalid Data)",
				"0     No Error",
	            "1     Invalid Data",
	            "2     Network Issue"
	            });
	    userChoice[mainBranchIndex] = getUserInput(0, 2);
	    
	//network error
	    if (userChoice[mainBranchIndex] == 2) {
	    	int typeIndex = 1;
	    	int valueIndex = 2;
	    	int packetIndex = 3;
	    	int blockIndex = 4;
	    	
			printOptions(new String[]{
					"<Choose Network Issue Type>",
		            "1     Duplicate",
		            "2     Delayed",
		            "3     Lost"
		            });
		    userChoice[typeIndex] = getUserInput(1, 3);
		    
		    
		    if (userChoice[typeIndex] == 1) {
				printOptions(new String[]{
						"<How many Duplicate packets?>",
						"(Any Integer >= 1)"
			            });
			    userChoice[valueIndex] = getUserInput(1);
		    } else if  (userChoice[typeIndex] == 2) {
				printOptions(new String[]{
						"<How many mili seconds?>",
						"(Any Integer >= 1)"
			            });
			    userChoice[valueIndex] = getUserInput(1);
		    } else if  (userChoice[typeIndex] == 3) {
				printOptions(new String[]{
						"<How many packets lost in a row?>",
						"(Any Integer >= 1)"
			            });
			    userChoice[valueIndex] = getUserInput(1);
		    }
		    
		    
			printOptions(new String[]{
					"<Choose Packet Type> (Error in what type of packet?)",
		            "1     Read request (RRQ)",
		            "2     Write request (WRQ)",
		            "3     Data (DATA)",
		            "4     Acknowledgment (ACK)",
		            "5     Error (ERROR)"
		            });
		    userChoice[packetIndex] = getUserInput(1, 5);
		    
		    if (userChoice[packetIndex] == 3 || userChoice[packetIndex] == 4) {
				printOptions(new String[]{
						"<Choose the Block #>",
						"(Any Integer >= 0)"
			            });
			    userChoice[blockIndex] = getUserInput(0);
		    }
		    
	    }
	    
	    
	    
	    
	//data error, TID error
	    else if (userChoice[mainBranchIndex] == 1){
	    	
	    	int problemIndex = 1;
	    	int packetIndex = 2;
	    	int fieldIndex = 3;
	    	int sizeIndex = 3;
	    	int blockIndex = 4;
	    	
			printOptions(new String[]{
					"<Choose Problem Type>",
					"0     Remove Field",
		            "1     Corruptted Field",
		            "2     Incorrect Size",
		            "3     Invalid TID",
		            "4     Invalid IP"
		            });
		    userChoice[problemIndex] = getUserInput(0, 4);
		    
			printOptions(new String[]{
					"<Choose Packet> (Error in what type of packet?)",
		            "1     Read request (RRQ)",
		            "2     Write request (WRQ)",
		            "3     Data (DATA)",
		            "4     Acknowledgment (ACK)",
		            "5     Error (ERROR)"
		            });
		    userChoice[packetIndex] = getUserInput(1, 5);
		    
		    
		    if (userChoice[problemIndex] == 2) {
				printOptions(new String[]{
						"<Choose Size #>"
			            });
			    userChoice[sizeIndex] = getUserInput(0);
		    }
		    
		    else if (userChoice[problemIndex] == 1 || userChoice[problemIndex] == 0) {
			    if (userChoice[packetIndex] == 1 || userChoice[packetIndex] == 2) {
					printOptions(new String[]{
							"<Choose Field>",
				            "1     Opcode",
				            "2     File Name",
				            "3     Null byte 1 {0}",
				            "4     Mode",
				            "5     Null byte 2 {0}"
				            });
				    userChoice[fieldIndex] = getUserInput(1, 5);
			    } else if (userChoice[packetIndex] == 3) {
					printOptions(new String[]{
							"<Choose Field>",//Opcode |   Block #  |   Data
				            "1     Opcode",
				            "2     Block #",
				            "3     Data"
				            });
				    userChoice[fieldIndex] = getUserInput(1, 3);
			    } else if (userChoice[packetIndex] == 4) {
					printOptions(new String[]{
							"<Choose Field>",//| Opcode |   Block #  |
				            "1     Opcode",
				            "2     Block #"
				            });
				    userChoice[fieldIndex] = getUserInput(1, 2);
			    } else if (userChoice[packetIndex] == 5) {
					printOptions(new String[]{
							"<Choose Field>",
				            "1     Opcode",
				            "2     ErrorCode",
				            "3     ErrMsg",
				            "4     Null byte {0}"
				            });
				    userChoice[fieldIndex] = getUserInput(1, 4);
			    }
		    }

		    if (userChoice[packetIndex] == 3 || userChoice[packetIndex] == 4) {
				printOptions(new String[]{
						"<Choose the Block #>",
						"(Any Integer >= 0)"
			            });
			    userChoice[blockIndex] = getUserInput(0);
		    }

		    
	    }
	    
	    sc.close();
	    		
		ErrorSimulator s = new ErrorSimulator();
		while(true) {
			s.receiveRequests(userChoice);
		}
	}
	
	private static void printOptions(String[] optionList) {
		for (String str: optionList) {
			System.out.println(str);
		}
	}
	
	private static int getUserInput(int min, int max) {
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
	
	private static int getUserInput(int min) {
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
	
	//get destination address from user
	private static void queryDestinationAddress(){
		//user might want to change server address, so set it back to null
		destinationAddress = null;
		do{
			try {
				System.out.println("Please enter the server's IP:\n"
						+ "If you'd like to use the local host enter 1 or local host");
				String ip = sc.nextLine().trim();
				
				//wants local host
				if(ip.equals("local host")|| ip.equals("1"))
					destinationAddress = InetAddress.getLocalHost();
				//optimizing code, getByName function might take a while to parse invalid IP address
				//thus this code will make program faster
				else if(ip.length()<3||!ip.contains("."))
					continue;
				else
					destinationAddress = InetAddress.getByName(ip);
				
//				IF WE DON'T ALLOW THE CLIENT AND SERVER TO BE ON SAME COMPUTER
//				THEN UNCOMMENT THIS CODE
//				if(ip.equals(InetAddress.getLocalHost().getHostAddress())||ip.equals("127.0.0.1")){
//					System.out.println("Server address can't have same address as this computer.");
//					destinationAddress = null;
//					continue;
//				}
			} catch (UnknownHostException e) {
				System.out.println("Please enter a valid IP address\n");
			}
		}while(destinationAddress==null);
		
		System.out.println("Server IP is: "+destinationAddress.getHostAddress()+"\n");
	}

}
