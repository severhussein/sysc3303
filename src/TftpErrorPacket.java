package TftpPacketHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

/**
 * TFTP Error
 * 
 * @author Yu-Kai Yang 100786472
 *
 */
public class TftpErrorPacket extends TftpPacket {
	public static final int NOT_DEFINED = 0;
	public static final int FILE_NOT_FOUND = 1;
	public static final int ACCESS_VIOLATION = 2;
	public static final int DISK_FULL = 3;
	public static final int ILLEGAL_OP = 4;
	public static final int UNKNOWN_TID = 5;
	public static final int FILE_EXIST = 6;
	public static final int NO_SUCH_USER = 7;

	public static final String[] errorString = { "Not defined, see error message (if any).", "File not found.",
			"Access violation.", "Disk full or allocation exceeded.", "Illegal TFTP operation.", "Unknown transfer ID.",
			"File already exists.", "No such user." };

	private final int errorCode;
	private final String errorMsg;

	public TftpErrorPacket(int errorCode, String errMsg) {
		super(TftpType.ERROR);
		this.errorCode = (short) errorCode;
		this.errorMsg = errMsg;
	}

	TftpErrorPacket(DatagramPacket packet) throws IllegalArgumentException {
		super(TftpType.ERROR);

		byte payload[] = packet.getData();
		int position = 4;
		int len = packet.getLength();
		StringBuilder sb = new StringBuilder();

		errorCode = (int) ((payload[2] & 0xff) << 8 | payload[3] & 0xff);

		if (errorCode > NO_SUCH_USER) {
			throw new IllegalArgumentException("Malformed TFTP Error Packet: Invalid Error Code " + errorCode);
		}

		while (position < len && payload[position] != 0) {
			sb.append((char) payload[position]);
			position++;
		}
		if (position+1 == position) {
			throw new IllegalArgumentException("Malformed TFTP Error Packet: Reached end of packet after getting error code");
		}
		errorMsg = sb.toString();
		if (position+1 == len) {
			//System.out.println("Good packet");
			return;
		} else {
			//System.out.println("Trailing bytes");
			throw new IllegalArgumentException("Malformed TFTP Error Packet: Trailing bytes after error message");
		}
	
	}

	public final String getErrorMsg() {
		return errorMsg;
	}

	public short getErrorCode() {
		return (short) errorCode;
	}

	@Override
	public byte[] generatePayloadArray() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			baos.write(getType().getOpcodeBytes());
			baos.write((byte) (errorCode >> 8 & 0xff));
			baos.write((byte) (errorCode & 0xff));
			baos.write(errorMsg.getBytes());
			baos.write((byte) 0);
		} catch (IOException e) {
			throw new RuntimeException("ByteArrayOutputStream throws Exception, something really bad happening", e);
		}

		return baos.toByteArray();
	}

	@Override
	public String toString() {
		return ("Type: " + getType().name() + " Error Code: " + getErrorCode() + " Error Message: " + getErrorMsg());
	}

}
