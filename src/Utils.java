import java.net.DatagramPacket;
import java.util.Arrays;

/**
 * Utilities which may be handy
 * 
 * @author Yu-Kai Yang 100786472
 *
 */
public class Utils {

	private static String OS = System.getProperty("os.name").toLowerCase();

	/**
	 * Prints the content in DatagramPacket
	 * 
	 * @param packet DatagramPacket to be printed
	 */
	public static void printPacketContent(DatagramPacket packet) 
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
	}
	public static void printVerbose(DatagramPacket packet)
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
	}


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
		int i = 0, count = 1;
		boolean printHex = true, printHeader = true;

		System.out.println("   This packet contains:");
		System.out.println("   Address: " + packet.getAddress());
		System.out.println("   Port: " + packet.getPort());
		System.out.println("   Length: " + len);
		System.out.print("   Content:");
		while (i < len) {
			if (printHex) {
				if (printHeader) {
					System.out.println();
					System.out.format("%04x | ", i); //
					printHeader = false;
				}
				System.out.print(String.format("%02x ", payload[i]));
				if (count == 16) {
					printHex = false;
					i -= 16;
					count = 0;
					System.out.print("   ");
				} else if (count == 8) {
					System.out.print("   ");
				} else if (i + 1 == payload.length) {
					// end of payload, switch to hex and compensate the space between hex/ASCII
					printHex = false;
					i -= count;
					for (int j = 1; j <= (16 - count); j++) {
						System.out.print("   ");
					}
					if (count <= 8) {
						System.out.print("      ");
					}
					count = 1;
				}
			} else {
				if (Character.isISOControl(payload[i])) {
					System.out.print(".");//
				} else {
					System.out.print((char) payload[i]);
				}
				if (count == 16) {
					printHex = true;
					printHeader = true;
					count = 0;
				} else if (count == 8) {
					System.out.print("   ");
				}
			}
			i++;
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
