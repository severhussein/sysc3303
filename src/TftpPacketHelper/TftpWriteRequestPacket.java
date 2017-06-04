package TftpPacketHelper;

import java.net.DatagramPacket;

/**
 * TFTP Write Request
 * 
 * @author Yu-Kai Yang 100786472
 *
 */
public class TftpWriteRequestPacket extends TftpRequestPacket {

	/**
	 * Construct a TFTP Write Request packet object. Use the public method to control options.
	 * 
	 * @param filename filename in the request packet
	 * @param mode transfer mode
	 * @throws IllegalArgumentException when input is invalid for tftp WRQ
	 */
	public TftpWriteRequestPacket(String filename, TftpTransferMode mode) throws IllegalArgumentException {
		super(TftpType.WRTIE_REQUEST, filename, mode);
	}

	TftpWriteRequestPacket(DatagramPacket packet) throws IllegalArgumentException {
		super(TftpType.WRTIE_REQUEST, packet);
	}
}
