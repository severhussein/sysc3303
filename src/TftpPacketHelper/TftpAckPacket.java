package TftpPacketHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

public class TftpAckPacket extends TftpPacket {

	protected static final byte ACK_PACKET_SIZE = 4;

	private final short blockNumber;

	TftpAckPacket(int blockNumber) {
		super(TftpType.ACK);
		this.blockNumber = (short) blockNumber;
	}

	TftpAckPacket(DatagramPacket packet) throws IllegalArgumentException {

		super(TftpType.ACK);

		byte payload[] = packet.getData();

		if (packet.getLength() != ACK_PACKET_SIZE) {
			throw new IllegalArgumentException("Malformed ack packet");
		}

		blockNumber = (short) (payload[2] << 8 & payload[3]);
	}

	protected TftpAckPacket(TftpType type, DatagramPacket packet) {

		super(type);

		byte payload[] = packet.getData();

		if (packet.getLength() != ACK_PACKET_SIZE) {
			throw new IllegalArgumentException("Malformed ack packet");
		}

		blockNumber = (short) (payload[2] << 8 & payload[3]);
		// TODO Auto-generated constructor stub
	}

	public byte[] generatePayloadArray() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		baos.write(getType().getArray());
		baos.write((byte) (blockNumber >> 8 & 0xff));
		baos.write((byte) (blockNumber & 0xff));

		return baos.toByteArray();
	}

	public short getBlockNumber() {
		return blockNumber;
	}
	
	@Override
	public String toString() {
		return ("Type: " + getType().name() + "Block Number: " + getBlockNumber());
	}
}
