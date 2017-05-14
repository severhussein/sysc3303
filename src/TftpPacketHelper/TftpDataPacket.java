package TftpPacketHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Arrays;

/**
 * TFTP Data
 * 
 * @author Yu-Kai Yang 100786472
 *
 */
public class TftpDataPacket extends TftpPacket {

	private final int blockNumber;
	private final byte[] data;
	private final int length;

	/**
	 * Construct a TFTP Data packet object.
	 * 
	 * @param blockNumber
	 *            block number in the data packet
	 * @param data
	 *            data to be put into the packet
	 * @throws IllegalArgumentException if the date is too long
	 */
	public TftpDataPacket(int blockNumber, byte[] data, int length) throws IllegalArgumentException {
		super(TftpType.DATA);
		
		if (length > TftpOackPacket.OPT_BLOCKSIZE_MAX) {
			throw new IllegalArgumentException("Trying to pack large than possible data block in packet");
		}
		
		this.blockNumber = (short) blockNumber;
		this.data = Arrays.copyOf(data, length);
		this.length = length;
	}

	TftpDataPacket(DatagramPacket packet) throws IllegalArgumentException {

		super(TftpType.DATA);

		if (packet.getLength() > TftpOackPacket.OPT_BLOCKSIZE_MAX) {
			throw new IllegalArgumentException("Trying to pack large than possible data block in packet");
		}

		byte payload[] = packet.getData();

		blockNumber = (int) ((payload[2] & 0xff) << 8 | payload[3] & 0xff);
		data = Arrays.copyOfRange(payload, 4, packet.getLength());
		length = packet.getLength() - 4;
	}

	public byte[] generatePayloadArray() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			baos.write(getType().getOpcodeBytes());
			baos.write((byte) (blockNumber >> 8 & 0xff));
			baos.write((byte) (blockNumber & 0xff));
			baos.write(data);
		} catch (IOException e) {
			throw new RuntimeException("ByteArrayOutputStream throws Exception, something really bad happening", e);
		}

		return baos.toByteArray();
	}

	/**
	 * Retrieves the block number of this TFTP data packet
	 * 
	 * @return block number
	 */
	public int getBlockNumber() {
		return blockNumber;
	}

	/**
	 * Retrieves the packed data of this TFTP data packet
	 * 
	 * @return data in array of bytes
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * Retrieves the length packed data of this TFTP data packet
	 * 
	 * @return length in bytes
	 */
	public int getDataLength() {
		return length;
	}
	
	@Override
	public String toString() {
		return ("Type: " + getType().name() + " Block Number: " + getBlockNumber() + ", and " + data.length
				+ " byte of data follows");
	}
}
