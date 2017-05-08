package TftpPacketHelper;

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
	 * Host forward request sent by client to this port, server listen on this
	 * port
	 */
	public static final int SERVER_LISTEN_PORT = 69;

	/**
	 * Size of buffer used to back DatagramPacket created in the three
	 * components
	 */
	public static final int PACKET_BUFFER_SIZE = 999;

	public static final int BLOCK_SIZE = 512;

	/**
	 * Response to read request as specified in assignment It will probably
	 * better to move this into it's own class later on
	 */
	public static final byte[] READ_RESPONSE_BYTES = { 0, 3, 0, 1 };

	/**
	 * Response to write request as specified in assignment It will probably
	 * better to move this into it's own class later on
	 */
	public static final byte[] WRITE_RESPONSE_BYTES = { 0, 4, 0, 0 };

}
