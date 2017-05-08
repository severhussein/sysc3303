package sysc3303;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

public class TftpErrorPacket extends TftpPacket {

	private final short errorCode;
	private final String errorMsg;

	TftpErrorPacket(short errorCode, String errMsg) {
		super(TftpType.ERROR);
		this.errorCode = errorCode;
		this.errorMsg = errMsg;
	}

	TftpErrorPacket(DatagramPacket packet) throws IllegalArgumentException {
		super(TftpType.ERROR);

		byte payload[] = packet.getData();
		int position = 2;
		int len = packet.getLength();
		StringBuilder sb = new StringBuilder();

		errorCode = (short) (payload[2] << 8 & payload[3]);

		while (position < len && payload[position] != 0) {
			sb.append((char) payload[position]);
			position++;
		}
		if (position == len) {
			throw new IllegalArgumentException("Reached end of packet after getting filename");
		}
		errorMsg = sb.toString();
		position++;

	}

	public final String getErrorMsg() {
		return errorMsg;
	}

	public short getErrorCode() {
		return errorCode;
	}

	@Override
	public byte[] generatePayloadArray() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		baos.write(getType().getArray());
		baos.write((byte) (errorCode >> 8 & 0xff));
		baos.write((byte) (errorCode & 0xff));
		baos.write(errorMsg.getBytes());
		
		return baos.toByteArray();
	}

	@Override
	public String toString() {
		return ("Type: " + getType().name() + "Error Code: " + getErrorCode() + "Error Message:" + getErrorMsg());
	}

}
