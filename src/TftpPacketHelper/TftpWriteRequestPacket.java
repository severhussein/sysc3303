package TftpPacketHelper;

import java.net.DatagramPacket;


public class TftpWriteRequestPacket extends TftpRequestPacket {

	TftpWriteRequestPacket(String filename, Mode mode) throws IllegalArgumentException {

		super(TftpType.REQUEST_WRTIE, filename, mode);

		if (filename.length() > 64000) {
			throw new IllegalArgumentException("Filename too long");
		}
	}
	
	TftpWriteRequestPacket(DatagramPacket packet) throws IllegalArgumentException {
		super(TftpType.REQUEST_WRTIE, packet);
	}
}
