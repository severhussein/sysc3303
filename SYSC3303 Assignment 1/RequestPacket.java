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
public class RequestPacket {

	public static final String MODE_ASCII_STRING = "netascii";
	public static final String MODE_OCTET_STRING = "octet";
	public static final byte[] READ_REQUEST_BYTES = {0, 1};
	public static final byte[] WRITE_REQUEST_BYTES = {0, 2};	
	
	/**
	 * 
	 * Probably not worth using an enum.... 
	 *
	 */
	public enum Mode {
		MODE_ASCII(MODE_ASCII_STRING.getBytes()), MODE_OCTET(MODE_OCTET_STRING.getBytes());
	
		private byte type[];
	
		Mode(byte type[]){
			this.type = type;
		}
		public byte[] getArray() {
			return type;
		}
	}

	/**
	 * 
	 * Probably not worth using an enum.... 
	 *
	 */
	public enum RequestType {
		REQUEST_WRTIE(READ_REQUEST_BYTES), REQUEST_READ(WRITE_REQUEST_BYTES);
	
		private byte type[];
	
		RequestType(byte type[]){
			this.type = type;
		}
		public byte[] getArray() {
			return type;
		}
	}

	private final RequestType type;
	private final Mode mode;
	private final String filename;

	/**
	 * Constructs a RequestPacket with provided input
	 * 
	 * @param request read or write
	 * @param filename the filename
	 * @param mode octet or ASCII
	 * @throws IllegalArgumentException
	 */
	RequestPacket(RequestType request, String filename, Mode mode) throws IllegalArgumentException
	{
		this.type = request;
		this.filename = filename;
		this.mode = mode;
		
		//is this worth it? UDP can't be more than 65535
		//different ip stack may have different kind of support 
		//or just let underlying class throw exception?
		//filesystem probably won't permit long filename anyway
		if (filename.length() > 64000) {
			throw new IllegalArgumentException("Filename too long");
		}
	}

	/**
	 * Dissects the DatagramPacket into various fields
	 * 
	 * @param packet - The UDP datagram to be passed into this dissector
	 * @throws IllegalArgumentException - if packet passed in is not in proper request 
	 * format defined in assignment 1
	 */
	RequestPacket(DatagramPacket packet) throws IllegalArgumentException
	{
		byte[] payload = packet.getData();
		int position = 2;
		int len  = packet.getLength();	
		StringBuilder sb = new StringBuilder();		
		
		if (payload[0] != 0) {
			throw new IllegalArgumentException("First byte not zero.");
		}
		
		switch (payload[1]) {
		case 1:
			type = RequestType.REQUEST_READ;
			break;
		case 2:
			type = RequestType.REQUEST_WRTIE;
			break;
		default:
			throw new IllegalArgumentException("Neither write nor read rqeust");
		}

		while (position < len && payload[position] != 0)
		{
			sb.append((char)payload[position]);
			position++;
		}
		if (position == len) {
			throw new IllegalArgumentException("Reached end of packet after getting filename");
		}
		filename = sb.toString();
		position++;
		sb = new StringBuilder();
		
		while (position < len && payload[position] != 0)
		{
			sb.append((char)payload[position]);
			position++;
		}
		String modeStr = sb.toString().toLowerCase(); //mixed case does not matter
		System.out.println("   This packet contains:" + modeStr);
		if (modeStr.equals(MODE_ASCII_STRING)) {
			mode = Mode.MODE_ASCII;
		} else if (modeStr.equals(MODE_OCTET_STRING)) {
			mode = Mode.MODE_OCTET;
		}
		else  {
			throw new IllegalArgumentException("Neither ascii nor octet mode");
		}

		if ((position + 1) != len || payload[position] != 0 ) {
			throw new IllegalArgumentException("Request not properly terminated");		
		}

	}

	/**
	 * Generate the byte array to be packed in a DatagramPacket for this object 
	 * 
	 * @return an byte array ready to be used
	 * @throws IOException
	 */
	public byte[] generatePayloadArray() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		baos.write(type.getArray());
		baos.write(filename.getBytes());
		baos.write((byte)0);
		baos.write(mode.getArray());
		baos.write((byte)0);

		return baos.toByteArray();
	}

	/**
	 * @return mode specified in the request packet. either octet or ascii
	 */
	public final Mode getMode()
	{
		return mode;
	}

	/**
	 * @return either read or write
	 */
	public final RequestType getType()
	{
		return type;
	}

	/**
	 * @return filename specified in the request
	 */
	public final String getFilename()
	{
		return filename;
	}
	
	@Override
	public String toString()
	{
		return ("Type: " + type.name() + "Filename: " + filename + "Mode: " + mode.name());
	}
}
