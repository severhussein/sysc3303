package sysc3303;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

/**
 * TFTP  Option Acknowledgment as specified in RFC 7440
 * 
 * @author eyanyuk
 *
 */
public class TftpOackPacket extends TftpPacket{
	
	public static final int OPT_BLOCKSIZE_MAX = 65464;
	public static final int OPT_BLOCKSIZE_MIN = 8;

	public static final int OPT_TIMEOUT_MAX = 255;
	public static final int OPT_TIMEOUT_MIN = 1;
	
	public static final int OPT_WINDOWSIZE_MAX = 65535;
	public static final int OPT_WINDOWSIZE_MIN = 1;
	
	private boolean has_blksize;
	private boolean has_timeout;
	private boolean has_tsize;
	private boolean has_windowsize;
	
	private int blksize;
	private short timeout;
	private long tsize;
	private int windowsize;
	
	TftpOackPacket(){
		super(TftpType.OACK);//ok.. type will be an issue here
	}
	
	TftpOackPacket(DatagramPacket packet) throws IllegalArgumentException {
		super(TftpType.OACK);//ok.. type will be an issue here
		
		byte[] payload = packet.getData();
		int position = TftpAckPacket.ACK_PACKET_SIZE;
		int len = packet.getLength();
		StringBuilder sb;
	
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

	public void setBlksize(int blksize) {

		if (blksize > OPT_BLOCKSIZE_MAX || blksize < OPT_BLOCKSIZE_MIN) {
			throw new IllegalArgumentException("Block size must be within " + OPT_BLOCKSIZE_MIN +"~"+OPT_BLOCKSIZE_MAX);
		}
		this.blksize = blksize;
		has_blksize = true;
	}

	public void setTimeout(int timeout) {
		if (timeout > OPT_TIMEOUT_MAX || timeout < OPT_TIMEOUT_MIN) {
			throw new IllegalArgumentException("Timeout must be within " + OPT_TIMEOUT_MIN +"~"+OPT_TIMEOUT_MAX);
		}
		this.timeout = (short) timeout;
		has_timeout = true;
	}

	public void setTransfersize(long tsize) {
		this.tsize = tsize;
		has_tsize = true;
	}

	public void setWindowsize(int windowsize) {
		if (timeout > OPT_WINDOWSIZE_MAX || timeout < OPT_WINDOWSIZE_MIN) {
			throw new IllegalArgumentException("Window size must be within " + OPT_WINDOWSIZE_MIN +"~"+OPT_WINDOWSIZE_MAX);
		}
		this.windowsize = windowsize;
		has_windowsize = true;
	}
	
	@Override
	public byte[] generatePayloadArray() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		baos.write(getType().getArray());

		/* TFTP OPTIONS */
		if (has_blksize) {
			baos.write(TftpPacket.OPTION_BLKSIZE_STRING.getBytes());
			baos.write((byte) 0);
			baos.write(Integer.toString(blksize).getBytes());
			baos.write((byte) 0);

		} 
		if (has_timeout) {
			baos.write(TftpPacket.OPTION_TIMEOUT_STRING.getBytes());
			baos.write((byte) 0);
			baos.write(Short.toString(timeout).getBytes());
			baos.write((byte) 0);
		} 
		if (has_tsize) {
			baos.write(TftpPacket.OPTION_TSIZE_STRING.getBytes());
			baos.write((byte) 0);
			baos.write(Long.toString(tsize).getBytes());
			baos.write((byte) 0);

		} 
		if (has_windowsize) {
			baos.write(TftpPacket.OPTION_WINDOWSIZE_STRING.getBytes());
			baos.write((byte) 0);
			baos.write(Integer.toString(windowsize).getBytes());
			baos.write((byte) 0);
		}
		
		return baos.toByteArray();
	}
	//still needs toString

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}
}
