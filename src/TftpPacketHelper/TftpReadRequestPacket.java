package TftpPacketHelper;

import java.net.DatagramPacket;

public class TftpReadRequestPacket extends TftpRequestPacket {

	/**
	 * Construct a TFTP Read Request packet object. Use the public method to control options.
	 * 
	 * @param filename filename in the request packet
	 * @param mode transfer mode
	 * @throws IllegalArgumentException
	 */
	public TftpReadRequestPacket(String filename, TftpTransferMode mode) throws IllegalArgumentException {
		super(TftpType.READ_REQUEST, filename, mode);
	}

	TftpReadRequestPacket(DatagramPacket packet) throws IllegalArgumentException {
		super(TftpType.READ_REQUEST, packet);
	}
}
