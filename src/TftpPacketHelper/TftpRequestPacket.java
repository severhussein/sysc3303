package TftpPacketHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

/**
 * A class for constructing or dissecting Request packets
 * 
 * @author Yu-Kai Yang 1000786472
 *
 */
public abstract class TftpRequestPacket extends TftpPacket {

	public static final String MODE_ASCII_STRING = "netascii";
	public static final String MODE_OCTET_STRING = "octet";

	public enum TftpTransferMode {
		MODE_ASCII(MODE_ASCII_STRING.getBytes()), MODE_OCTET(MODE_OCTET_STRING.getBytes());

		private byte type[];

		TftpTransferMode(byte type[]) {
			this.type = type;
		}

		public byte[] getArray() {
			return type;
		}
	}

	private final TftpTransferMode mode;
	private final String filename;

	/// make these final? constructor will have a lot of parameters...
	private boolean has_blksize = false;
	private boolean has_timeout = false;
	private boolean has_tsize = false;
	private boolean has_windowsize = false;

	private int blksize;
	private short timeout;
	private long tsize;
	private int windowsize;

	TftpRequestPacket(TftpType type, String filename, TftpTransferMode mode) throws IllegalArgumentException {
		super(type);
		this.filename = filename;
		this.mode = mode;

		if (filename.length() == 0) {
			throw new IllegalArgumentException("Empty Filename");
		}
	}

	/**
	 * Dissects the Tftp Request into various fields, should not be used directly
	 * 
	 * @param packet
	 *            The UDP datagram to be passed into this dissector
	 * @throws IllegalArgumentException
	 *             if packet passed in is not in proper request format 
	 */
	TftpRequestPacket(TftpType type, DatagramPacket packet) throws IllegalArgumentException {
		super(type);

		byte[] payload = packet.getData();
		int idx = 2;
		int len = packet.getLength();
		StringBuilder sb = new StringBuilder();

		while (idx < len && payload[idx] != 0) {
			sb.append((char) payload[idx]);
			idx++;
		}
		if (idx+1 == len) {
			throw new IllegalArgumentException("Malformed TFTP Request Packet: Reached end of packet after getting filename");
		}
		filename = sb.toString();
		if (filename.length() == 0) {
			throw new IllegalArgumentException("Malformed TFTP Request Packet: Empty filename");
		}
		idx++;
		sb = new StringBuilder();

		while (idx < len && payload[idx] != 0) {
			sb.append((char) payload[idx]);
			idx++;
		}
		String modeStr = sb.toString().toLowerCase();

		if (payload[idx] != 0) {
			throw new IllegalArgumentException("Malformed TFTP Request Packet: Mode not null terminated");
		}
		if (modeStr.equals(MODE_ASCII_STRING)) {
			mode = TftpTransferMode.MODE_ASCII;
		} else if (modeStr.equals(MODE_OCTET_STRING)) {
			mode = TftpTransferMode.MODE_OCTET;
		} else {
			throw new IllegalArgumentException("Malformed TFTP Request Packet: Neither ascii nor octet mode");
		}

		if (idx+1 == len) {
			//System.out.println("No options, return");
			return;
		} else {
			//System.out.println("Trailing bytes");
			throw new IllegalArgumentException("Malformed TFTP Request Packet: Trailing bytes after mode");
		}
		
		//no plan to support TFTP options, comment it out for now
		
//		idx++;
//		// we probably have a tftp option....
//		while (idx < len) {
//			sb = new StringBuilder();
//			while (idx < len && payload[idx] != 0) {
//				System.out.println("idx" + idx);
//				sb.append((char) payload[idx]);
//				idx++;
//			}
//			String option = sb.toString().toLowerCase();
//			if (option.length() == 0) {
//				throw new IllegalArgumentException("Empty option");
//			}
//			idx++;
//			//System.out.println("option:" + option);
//			// now parse the value, they are all in ascii
//			sb = new StringBuilder();
//			while (idx < len && payload[idx] != 0) {
//				sb.append((char) payload[idx]);
//				idx++;
//			}
//			String optValueStr = sb.toString().toLowerCase();
//			if (optValueStr.length() == 0) {
//				throw new IllegalArgumentException("Empty option value");
//			}
//			//System.out.println("optValueStr:" + optValueStr);
//			idx++;
//
//			if (option.equals(OPTION_BLKSIZE_STRING)) {
//				has_blksize = true;
//				blksize = Integer.parseInt(optValueStr);
//			} else if (option.equals(OPTION_TIMEOUT_STRING)) {
//				has_timeout = true;
//				timeout = (short) Integer.parseInt(optValueStr);
//			} else if (option.equals(OPTION_TSIZE_STRING)) {
//				has_tsize = true;
//				tsize = Integer.parseInt(optValueStr);
//			} else if (option.equals(OPTION_WINDOWSIZE_STRING)) {
//				has_windowsize = true;
//				windowsize = Integer.parseInt(optValueStr);
//			}
//		}

	}

	/**
	 * Generate the byte array to be packed in a DatagramPacket for this TFTP packet
	 * 
	 * @return an byte array ready to be used
	 */
	public byte[] generatePayloadArray() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			baos.write(getType().getOpcodeBytes());
			baos.write(filename.getBytes());
			baos.write((byte) 0);
			baos.write(mode.getArray());
			baos.write((byte) 0);

			/* TFTP OPTIONS */
			if (has_blksize()) {
				baos.write(OPTION_BLKSIZE_STRING.getBytes());
				baos.write((byte) 0);
				baos.write(Integer.toString(getBlksize()).getBytes());
				baos.write((byte) 0);

			}
			if (has_timeout()) {
				baos.write(OPTION_TIMEOUT_STRING.getBytes());
				baos.write((byte) 0);
				baos.write(Short.toString(getTimeout()).getBytes());
				baos.write((byte) 0);
			}
			if (has_Transfersize()) {
				baos.write(OPTION_TSIZE_STRING.getBytes());
				baos.write((byte) 0);
				baos.write(Long.toString(getTransfersize()).getBytes());
				baos.write((byte) 0);

			}
			if (has_windowsize()) {
				baos.write(OPTION_WINDOWSIZE_STRING.getBytes());
				baos.write((byte) 0);
				baos.write(Integer.toString(getWindowsize()).getBytes());
				baos.write((byte) 0);
			}
		} catch (IOException e) {
			throw new RuntimeException("ByteArrayOutputStream throws Exception, something really bad happening", e);
		}

		return baos.toByteArray();
	}

	/**
	 * @return mode specified in the request packet. either octet or ascii
	 */
	public final TftpTransferMode getMode() {
		return mode;
	}

	/**
	 * @return filename specified in the request
	 */
	public final String getFilename() {
		return filename;
	}

	public boolean has_blksize() {
		return has_blksize;
	}

	public boolean has_timeout() {
		return has_timeout;
	}

	public boolean has_Transfersize() {
		return has_tsize;
	}

	public boolean has_windowsize() {
		return has_windowsize;
	}

	public int getBlksize() {
		return blksize;
	}

	public short getTimeout() {
		return timeout;
	}

	public long getTransfersize() {
		return tsize;
	}

	public int getWindowsize() {
		return windowsize;
	}

	public void setBlksize(int blksize) {
		this.blksize = blksize;
		has_blksize = true;
	}

	public void setTimeout(int timeout) {
		this.timeout = (short) timeout;
		has_timeout = true;
	}

	public void setTransfersize(long tsize) {
		this.tsize = tsize;
		has_tsize = true;
	}

	public void setWindowsize(int windowsize) {
		this.windowsize = windowsize;
		has_windowsize = true;
	}

	@Override
	public String toString() {
		return ("Type: " + getType().name() + " Filename: " + filename + " Mode: " + mode.name());
	}
}
