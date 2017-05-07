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

	/**
	 * @return true if OS is Windows
	 */
	public static boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}
}
