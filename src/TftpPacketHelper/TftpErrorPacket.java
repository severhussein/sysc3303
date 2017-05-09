package TftpPacketHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

public class TftpErrorPacket extends TftpPacket {

	
	public static final String[] errorString = { "Not defined, see error message (if any).", "File not found.",
			"Access violation.", "Disk full or allocation exceeded.", "Illegal TFTP operation.", "Unknown transfer ID.",
			"File already exists.", "No such user." };
		
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
		int position = 4;
		int len = packet.getLength();
		StringBuilder sb = new StringBuilder();

		errorCode = (short) ((payload[2] & 0xff) << 8 | payload[3] & 0xff);

		while (position < len && payload[position] != 0) {
			sb.append((char) payload[position]);
			position++;
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

		baos.write(getType().getOpcodeBytes());
		baos.write((byte) (errorCode >> 8 & 0xff));
		baos.write((byte) (errorCode & 0xff));
		baos.write(errorMsg.getBytes());
		
		return baos.toByteArray();
	}

	@Override
	public String toString() {
		return ("Type: " + getType().name() + " Error Code: " + getErrorCode() + " Error Message: " + getErrorMsg());
	}

}
