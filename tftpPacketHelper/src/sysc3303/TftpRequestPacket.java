package sysc3303;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

/**
 * A class for constructing or dissecting Request packets
 * 
 * @author Yu-Kai Yang 1000786472
 *
 */
public class TftpRequestPacket extends TftpPacket {

	public static final String MODE_ASCII_STRING = "netascii";
	public static final String MODE_OCTET_STRING = "octet";

	/**
	 * 
	 * Probably not worth using an enum....
	 *
	 */
	public enum Mode {
		MODE_ASCII(MODE_ASCII_STRING.getBytes()), MODE_OCTET(MODE_OCTET_STRING.getBytes());

		private byte type[];

		Mode(byte type[]) {
			this.type = type;
		}

		public byte[] getArray() {
			return type;
		}
	}

	private final Mode mode;
	private final String filename;

	///make these final? constructor will have a lot of parameters...
	private boolean has_blksize;
	private boolean has_timeout;
	private boolean has_tsize;
	private boolean has_windowsize;

	private int blksize;
	private short timeout;
	private long tsize;
	private int windowsize;

	/**
	 * Constructs a RequestPacket with provided input
	 * 
	 * @param request
	 *            read or write
	 * @param filename
	 *            the filename
	 * @param mode
	 *            octet or ASCII
	 * @throws IllegalArgumentException
	 */
	TftpRequestPacket(TftpType type, String filename, Mode mode) throws IllegalArgumentException {

		super(type);
		this.filename = filename;
		this.mode = mode;

		// is this worth it? UDP can't be more than 65535
		// different ip stack may have different kind of support
		// or just let underlying class throw exception?
		// filesystem probably won't permit long filename anyway
		if (filename.length() > 64000) {
			throw new IllegalArgumentException("Filename too long");
		}
	}

	/**
	 * Dissects the DatagramPacket into various fields
	 * 
	 * @param packet
	 *            The UDP datagram to be passed into this dissector
	 * @throws IllegalArgumentException
	 *             if packet passed in is not in proper request format defined
	 *             in assignment 1
	 */
	TftpRequestPacket(TftpType type, DatagramPacket packet) throws IllegalArgumentException {
		super(type);

		byte[] payload = packet.getData();
		int position = 2;
		int len = packet.getLength();
		StringBuilder sb = new StringBuilder();

		while (position < len && payload[position] != 0) {
			sb.append((char) payload[position]);
			position++;
		}
		if (position == len) {
			throw new IllegalArgumentException("Reached end of packet after getting filename");
		}
		filename = sb.toString();
		position++;
		sb = new StringBuilder();

		while (position < len && payload[position] != 0) {
			sb.append((char) payload[position]);
			position++;
		}
		String modeStr = sb.toString().toLowerCase();
		System.out.println("   This packet contains:" + modeStr);
		if (modeStr.equals(MODE_ASCII_STRING)) {
			mode = Mode.MODE_ASCII;
		} else if (modeStr.equals(MODE_OCTET_STRING)) {
			mode = Mode.MODE_OCTET;
		} else {
			throw new IllegalArgumentException("Neither ascii nor octet mode");
		}
		position++;

		// we probably have a tftp option....
		while (position < len) {
			sb = new StringBuilder();
			while (position < len && payload[position] != 0) {
				sb.append((char) payload[position]);
				position++;
			}
			String option = sb.toString().toLowerCase();
			position++;
			// now parse the value, they are all in ascii
			sb = new StringBuilder();
			while (position < len && payload[position] != 0) {
				sb.append((char) payload[position]);
				position++;
			}
			String optValueStr = sb.toString().toLowerCase();
			position++;
			int optValue = Integer.parseInt(optValueStr);
			if (option.equals(OPTION_BLKSIZE_STRING)) {
				has_blksize = true;
				blksize = optValue;

			} else if (option.equals(OPTION_TIMEOUT_STRING)) {
				has_timeout = true;
				timeout = (short) optValue;

			} else if (option.equals(OPTION_TSIZE_STRING)) {
				has_tsize = true;
				tsize = optValue;

			} else if (option.equals(OPTION_WINDOWSIZE_STRING)) {
				has_windowsize = true;
				windowsize = optValue;
			}
		}

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

		return baos.toByteArray();
	}

	/**
	 * @return mode specified in the request packet. either octet or ascii
	 */
	public final Mode getMode() {
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
		return ("Type: " + getType().name() + "Filename: " + filename + "Mode: " + mode.name());
	}
}
