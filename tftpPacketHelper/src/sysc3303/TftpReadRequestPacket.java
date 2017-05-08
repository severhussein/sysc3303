package sysc3303;

import java.net.DatagramPacket;

public class TftpReadRequestPacket extends TftpRequestPacket {

	TftpReadRequestPacket(String filename, Mode mode) throws IllegalArgumentException {

		super(TftpType.REQUEST_READ, filename, mode);

		if (filename.length() > 64000) {
			throw new IllegalArgumentException("Filename too long");
		}
	}

	TftpReadRequestPacket(DatagramPacket packet) throws IllegalArgumentException {
		super(TftpType.REQUEST_READ, packet);
	}
}
