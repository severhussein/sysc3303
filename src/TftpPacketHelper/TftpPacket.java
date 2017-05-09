package TftpPacketHelper;

import java.io.IOException;
import java.net.DatagramPacket;

/**
 * Helper class to encode/decode TFTP packet with support for RFC 1350, 2347, 2348, 2349, 7440
 * blksize works, other options not tested. 
 * 
 * @author Yu-Kai Yang 100786472
 *
 */
public abstract class TftpPacket {
	public static final int TFTP_RRQ = 1;
	public static final int TFTP_WRQ = 2;
	public static final int TFTP_DATA = 3;
	public static final int TFTP_ACK = 4;
	public static final int TFTP_ERROR = 5;
	public static final int TFTP_OACK = 6;
	
	public static final String OPTION_BLKSIZE_STRING = "blksize";
	public static final String OPTION_TIMEOUT_STRING = "timeout";
	public static final String OPTION_TSIZE_STRING = "tsize";
	public static final String OPTION_WINDOWSIZE_STRING = "windowsize";

	public enum TftpType {
		READ_REQUEST(TFTP_RRQ), WRTIE_REQUEST(TFTP_WRQ), DATA(TFTP_DATA), ACK(TFTP_ACK), ERROR(
				TFTP_ERROR), OACK(TFTP_OACK);

		private int opcode;

		private TftpType(int opcode) {
			this.opcode = opcode;
		}

		public byte[] getOpcodeBytes() {
			byte[] opcodeArray = { 0, 0 };
			opcodeArray[1] = (byte) opcode;

			return opcodeArray;
		}
	}

	private final TftpType packetType;

	TftpPacket(TftpType type) {
		this.packetType = type;
	}

	/**
	 * Use this method to decode TFTP packets
	 * 
	 * @param packet DatagramPacket to be decoded
	 * @return decoded TFTP packet
	 * @throws IllegalArgumentException if something goes wrong while decoding
	 */
	public static TftpPacket decodeTftpPacket(DatagramPacket packet) throws IllegalArgumentException {
		byte[] payload = packet.getData();

		if (payload[0] != 0) {
			throw new IllegalArgumentException("First byte not zero.");
		}

		switch (payload[1]) {
		case TFTP_RRQ:
			return new TftpReadRequestPacket(packet);
		case TFTP_WRQ:
			return new TftpWriteRequestPacket(packet);
		case TFTP_DATA:
			return new TftpDataPacket(packet);
		case TFTP_ACK:
			return new TftpAckPacket(packet);
		case TFTP_ERROR:
			return new TftpErrorPacket(packet);
		case TFTP_OACK:
			return new TftpOackPacket(packet);
		default:
			throw new IllegalArgumentException("Invalid opcode.. is this really a TFTP packet?");
		}
	}

	/**
	 * Generates a byte array to be packed in a DatagramPacket (or anywhere else)
	 * 
	 * @return Tftp packet in byte array form
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
	 * Get the type of this TFTP packet
	 * 
	 * @return the type of this TFTP packet
	 */
	public final TftpType getType() {
		return packetType;
	}

	@Override
	public abstract String toString();
}
