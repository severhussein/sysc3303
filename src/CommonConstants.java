/**
 * As the name implies, common constants are kept here for easier management
 * 
 * @author Yu-Kai Yang 100786472
 *
 */
public class CommonConstants {

	/**
	 * Client send to this port, host listen on this port
	 */
	public static final int HOST_LISTEN_PORT = 23;
	
	/**
	 * Host forward request sent by client to this port, server listen on this port
	 */
	public static final int SERVER_LISTEN_PORT = 69;
	
	/**
	 * Size of buffer used to back DatagramPacket created in the three components
	 */
	public static final int PACKET_BUFFER_SIZE = 100;

	/**
	* TFTP transfer opcode for read request.
	*/
	public static final int RRQ = 1;

	/**
	* TFTP opcode for write request.
	*/
	public static final int WRQ = 2;

	/**
	* TFTP opcode for data datagram.
	*/
	public static final int DATA = 3;

	/**
	* TFTP opcode for acknowledge datagram.
	*/
	public static final int ACK = 4;

	/**
	* TFTP opcode for error datagram.
	*/
	public static final int ERR = 5;

	/**
	* TFTP data datagram size.
	*/
	public static final int DATA_PACKET_SZ = 516;

	/**
	* TFTP data datagram block size.
	*/
	public static final int DATA_BLOCK_SZ = 512;

	/**
	* TFTP acknowledge datagram size.
	*/
	public static final int ACK_PACKET_SZ = 4;

	/**
	* User interface option for dumping program activity logs.
	*/
	public static final String VERBOSE = "Verbose";

	/**
	* User interface option for silent output.
	*/
	public static final String QUIET = "Quiet";

	/**
	* System set-up option for enabling error simulator.
	*/
	public static final String TEST = "Test";

	/**
	* System set-up option for direct client-server communication.
	*/
	public static final String NORM = "Normal";
	
	public static final int TFTP_MAX_NUM_RETRIES = 5;
	
	public static final int TFTP_MAX_BLOCK_NUMBER = 65535;
		
	//let's make it 5s for now so that it is easier to test
	//in real life this is probably tooo long
	public static final int SOCKET_TIMEOUT_MS = 5000;
}
