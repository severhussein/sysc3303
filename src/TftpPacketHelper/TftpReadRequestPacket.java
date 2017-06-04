package TftpPacketHelper;

import java.net.DatagramPacket;

/**
 * TFTP Read Request
 * 
 * @author Yu-Kai Yang 100786472
 *
 */
public class TftpReadRequestPacket extends TftpRequestPacket {

	/**
	 * Construct a TFTP Read Request packet object. Use the public method to control options.
	 * 
	 * @param filename filename in the request packet
	 * @param mode transfer mode
	 * @throws IllegalArgumentException when input is invalid for tftp RRQ
	 */
	public TftpReadRequestPacket(String filename, TftpTransferMode mode) throws IllegalArgumentException {
		super(TftpType.READ_REQUEST, filename, mode);
	}

	TftpReadRequestPacket(DatagramPacket packet) throws IllegalArgumentException {
		super(TftpType.READ_REQUEST, packet);
	}
}
