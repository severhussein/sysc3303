package TftpPacketHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Arrays;

public class TftpDataPacket extends TftpPacket {

	private final short blockNumber;
	private final byte[] data;

	/**
	 * Construct a TFTP Data packet object.
	 * 
	 * @param blockNumber block number in the data packet
	 * @param data data to be put into the packet
	 * @throws IllegalArgumentException
	 */
	public TftpDataPacket(int blockNumber, byte[] data) throws IllegalArgumentException {
		super(TftpType.DATA);
		this.blockNumber = (short) blockNumber;
		this.data = data.clone();
	}

	TftpDataPacket(DatagramPacket packet) throws IllegalArgumentException {

		super(TftpType.DATA);

		if (packet.getLength() > TftpOackPacket.OPT_BLOCKSIZE_MAX) {
			throw new IllegalArgumentException("Trying to pack large than possible data block in packet");
		}

		byte payload[] = packet.getData();

		blockNumber = (short) ((payload[2] & 0xff) << 8 | payload[3] & 0xff);
		data = Arrays.copyOfRange(payload, 4, packet.getLength());
	}

	/**
	 * Generate the byte array to be packed in a DatagramPacket for this object
	 * 
	 * @return an byte array ready to be used
	 * @throws IOException
	 */
	public byte[] generatePayloadArray() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		baos.write(getType().getOpcodeBytes());
		baos.write((byte) (blockNumber >> 8 & 0xff));
		baos.write((byte) (blockNumber & 0xff));
		baos.write(data);

		return baos.toByteArray();
	}

	public short getBlockNumber() {
		return blockNumber;
	}

	public byte[] getData() {
		return data;
	}

	@Override
	public String toString() {
		return ("Type: " + getType().name() + " Block Number: " + getBlockNumber() + ", and " + data.length + " byte of data follows");
	}
}
