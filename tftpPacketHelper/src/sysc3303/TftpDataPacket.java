package sysc3303;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Arrays;

public class TftpDataPacket extends TftpPacket {

	private final short blockNumber;
	private final byte[] data;

	/**
	 * @param blockNumber
	 * @param data
	 * @throws IllegalArgumentException
	 */
	TftpDataPacket(int blockNumber, byte[] data) throws IllegalArgumentException {
		super(TftpType.DATA);
		this.blockNumber = (short) blockNumber;
		this.data = data.clone();
	}

	TftpDataPacket(DatagramPacket packet) throws IllegalArgumentException {

		super(TftpType.DATA);

		if (packet.getLength() > (CommonConstants.BLOCK_SIZE + 2 + 2)) {// fix
																		// me,
																		// magic
																		// number
			throw new IllegalArgumentException("Large block size not supported");
		}

		byte temp[] = packet.getData();

		blockNumber = (short) (temp[2] & temp[3] << 8);
		data = Arrays.copyOfRange(temp, 4, temp.length - 1);
	}

	/**
	 * Generate the byte array to be packed in a DatagramPacket for this object
	 * 
	 * @return an byte array ready to be used
	 * @throws IOException
	 */
	public byte[] generatePayloadArray() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		baos.write(getType().getArray());
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
