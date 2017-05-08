package TftpPacketHelper;

import java.io.IOException;
import java.net.DatagramPacket;

/**
 * Helper class to encode/decode TFTP packet with support for RFC 1350, 2347, 2348, 2349, 7440
 * not finished yet, not completed tested
 * 
 * @author Yu-Kai Yang 100786472
 *
 */
public abstract class TftpPacket {

	public static final byte[] READ_REQUEST_BYTES = { 0, 1 };
	public static final byte[] WRITE_REQUEST_BYTES = { 0, 2 };
	public static final byte[] DATA_BYTES = { 0, 3 };
	public static final byte[] ACK_BYTES = { 0, 4 };
	public static final byte[] ERROR_BYTES = { 0, 5 };
	public static final byte[] OACK_BYTES = { 0, 6 };
	
	public static final String OPTION_BLKSIZE_STRING = "blksize";
	public static final String OPTION_TIMEOUT_STRING = "timeout";
	public static final String OPTION_TSIZE_STRING = "tsize";
	public static final String OPTION_WINDOWSIZE_STRING = "windowsize";

	/**
	 * 
	 * Probably not worth using an enum.... what's the overhead?
	 *
	 */
	public enum TftpType {
		REQUEST_WRTIE(READ_REQUEST_BYTES), REQUEST_READ(WRITE_REQUEST_BYTES), DATA(DATA_BYTES), ACK(ACK_BYTES), ERROR(
				ERROR_BYTES), OACK(OACK_BYTES);

		private byte type[];

		TftpType(byte type[]) {
			this.type = type;
		}

		public byte[] getArray() {
			return type;
		}
	}

	private final TftpType packetType;

	TftpPacket(TftpType type) {
		this.packetType = type;
	}

	public static TftpPacket decodeTftpPacket(DatagramPacket packet) throws IllegalArgumentException {
		byte[] payload = packet.getData();

		if (payload[0] != 0) {
			throw new IllegalArgumentException("First byte not zero.");
		}

		switch (payload[1]) {
		/// magic number...
		case 1:
			return new TftpReadRequestPacket(packet);
		case 2:
			return new TftpWriteRequestPacket(packet);
		case 3:
			return new TftpDataPacket(packet);
		case 4:
			return new TftpAckPacket(packet);
		case 5:
			return new TftpErrorPacket(packet);
		case 6:
			return new TftpOackPacket(packet);
		default:
			throw new IllegalArgumentException("Invalid opcode");
		}
	}

	/**
	 * Should just use generateDatagram instead
	 * 
	 * @return the payload to be used in a DatagramPacket
	 * @throws IOException
	 */
	public abstract byte[] generatePayloadArray() throws IOException;

	/**
	 * Generates a DatagramPacket for this TFTP packet
	 * 
	 * @return DatagramPacket to be send
	 * @throws IOException
	 */
	public DatagramPacket generateDatagram() throws IOException {
		byte[] payload = generatePayloadArray();

		return new DatagramPacket(payload, payload.length);
	}

	/**
	 * @return
	 */
	public final TftpType getType() {
		return packetType;
	}

	@Override
	public String toString(){
		return "" + getType().name() + " ... you really want to print out the data?";
	}
}
