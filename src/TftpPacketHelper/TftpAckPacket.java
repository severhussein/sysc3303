package TftpPacketHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

public class TftpAckPacket extends TftpPacket {

	protected static final byte ACK_PACKET_SIZE = 4;

	private final int blockNumber;

	/**
	 * Construct a TFTP ACK packet object.
	 * 
	 * @param blockNumber
	 *            block number in the data packet.
	 */
	public TftpAckPacket(int blockNumber) {
		super(TftpType.ACK);
		this.blockNumber = (short) blockNumber;
	}

	TftpAckPacket(DatagramPacket packet) throws IllegalArgumentException {
		super(TftpType.ACK);

		byte payload[] = packet.getData();

		if (packet.getLength() != ACK_PACKET_SIZE) {
			throw new IllegalArgumentException("Malformed TFTP ACK packet, size is not " + ACK_PACKET_SIZE + " bytes");
		}

		blockNumber = (int) ((payload[2] & 0xff) << 8 | payload[3] & 0xff);
	}

	public byte[] generatePayloadArray() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			baos.write(getType().getOpcodeBytes());
			baos.write((byte) (blockNumber >> 8 & 0xff));
			baos.write((byte) (blockNumber & 0xff));
		} catch (IOException e) {
			throw new RuntimeException("ByteArrayOutputStream throws Exception, something really bad happening", e);
		}

		return baos.toByteArray();
	}

	/**
	 * Retrieves the block number of this TFTP ack packet
	 * 
	 * @return block number
	 */
	public int getBlockNumber() {
		return blockNumber;
	}

	@Override
	public String toString() {
		return ("Type: " + getType().name() + " Block Number: " + getBlockNumber());
	}
}
