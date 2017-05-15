import java.net.DatagramPacket;
import java.util.Arrays;

/**
 * Utilities which may be handy
 * 
 * @author Yu-Kai Yang 100786472
 *
 */
public class Utils {
	private static final int BYTE_PER_LINE = 16;
	private static final int BYTE_PER_GROUP = BYTE_PER_LINE / 2;
	
	private static String OS = System.getProperty("os.name").toLowerCase();

	/**
	 * Prints the content in DatagramPacket
	 * 
	 * @param packet DatagramPacket to be printed
	 */
	/*public static void printPacketContent(DatagramPacket packet) 
	{
		
		int len = packet.getLength();
		byte payload[] = packet.getData();
		String str = new String(payload,0,len);  

		System.out.println("   This packet contains:");
		System.out.println("   Address: " + packet.getAddress());
		System.out.println("   Port: " + packet.getPort());
		System.out.println("   Length: " + len);
		System.out.print("   In string form: ");
		//well, null character may cause some issue
		System.out.println(str.replace('\u0000', ' '));
		System.out.println("   In raw bytes: "  + Arrays.toString(Arrays.copyOfRange(payload,0,len)));
		String received = new String(packet.getData(),0,len);
        System.out.println("String: " + received);
	}*/

	public static byte[] trimPacket(byte[] buf) {
		int i, consecNulls = 0;
		for(i = 0; i < buf.length-1; i += 1) {
			if(buf[i] == 0) consecNulls += 1;
			if(consecNulls == 2) break;
		}

		return Arrays.copyOf(buf, i-1);
	}
	
	public static void printDatagramContentWiresharkStyle(DatagramPacket packet) {
		int len = packet.getLength();
		byte payload[] = packet.getData();
		int idx = 0, count = 1;
		boolean printHex = true, printHeader = true;

		System.out.println("   This packet contains:");
		System.out.println("   Address: " + packet.getAddress() + ":" + packet.getPort());
		System.out.println("   Length: " + len);
		System.out.print("   Content:");
		while (idx < len) {
			if (printHex) {
				if (printHeader) {
					System.out.println();
					System.out.format("    %04x | ", idx);
					printHeader = false;
				}
				System.out.print(String.format("%02x ", payload[idx]));
				if (count == BYTE_PER_LINE) {
					// reached max number per line, rewind index
					printHex = false;
					idx -= BYTE_PER_LINE;
					count = 0;
					System.out.print("   ");//separator between hex and ascii
				} else if (count == BYTE_PER_GROUP) {
					System.out.print("   ");//separator between groups
				} else if (idx + 1 == len) {
					// end of payload, switch to hex and compensate the space between hex/ASCII
					printHex = false;
					idx -= count;
					for (int j = 1; j <= (BYTE_PER_LINE - count); j++) {
						System.out.print("   "); //2 space for hex, and 1 as separator
					}
					if (count <= BYTE_PER_GROUP) {
						System.out.print("   ");//extra three to compensate the group separator
					}
					System.out.print("   ");//separator between hex and ascii
					count = 1;
				}
			} else {
				if (Character.isISOControl(payload[idx])) {
					System.out.print(".");//swap "non-printable"
				} else {
					System.out.print((char) payload[idx]);
				}
				if (count == BYTE_PER_LINE) {
					printHex = true;
					printHeader = true;
					count = 0;
				} else if (count == BYTE_PER_GROUP) {
					System.out.print("   ");//separator between groups
				}
			}
			idx++;
			count++;
		}
		System.out.println();
		System.out.println();
		
	}


	/**
	 * @return true if OS is Windows
	 */
	public static boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}
}
