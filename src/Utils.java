import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import TftpPacketHelper.TftpPacket;

/**
 * Utilities which may be handy
 * 
 * @author Yu-Kai Yang 100786472
 *
 */
public class Utils {
	private static final int BYTE_PER_LINE = 16;
	private static final int BYTE_PER_GROUP = BYTE_PER_LINE / 2;

	/**
	 * Decode the packet with TFTP packet helper and print it If decoding fails
	 * use printDatagramContentWiresharkStyle() instead
	 * 
	 * @param packet
	 *            packet to be printed
	 */
	public static void tryPrintTftpPacket(DatagramPacket packet) {
		TftpPacket tftpPacket = null;
		boolean isTftp = true;
		StringBuilder sb = new StringBuilder();

		try {
			tftpPacket = TftpPacket.decodeTftpPacket(packet);
		} catch (IllegalArgumentException iae) {
			isTftp = false;
			sb.append(iae.getMessage());
		}
		catch (Exception e) {
			isTftp = false;
		}
		System.out.println("   This packet contains:");
		System.out.println("   Address: " + packet.getAddress() + ":" + packet.getPort());
		System.out.println("   Length: " + packet.getLength());		
		if (isTftp && tftpPacket != null) {
				System.out.println("   TFTP " + tftpPacket+"\n");
		} else {
			//Utils.printDatagramContentWiresharkStyle(packet);
			System.out.println("   Not proper TFTP packet. " + sb + "\n");
		}
	}
	
	/**
	 * Sample output
	 * 
	 * <pre>
	 *   This packet contains:
	 *   Address: /192.168.1.45:54282
	 *   Length: 21
	 *   Content:
	 *    0000 | 00 01 61 63 72 6f 6e 79    6d 73 2e 74 78 74 00 6f    ..acrony   ms.txt.o
	 *    0010 | 63 74 65 74 00                                        ctet.
	 * </pre>
	 * 
	 * @param packet
	 *            packet to be printed
	 */
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
				} 
				if (idx + 1 == len) {
					// end of payload, switch to hex and compensate the space between hex/ASCII
					printHex = false;
					idx -= count;
					for (int j = 1; j <= (BYTE_PER_LINE - count); j++) {
						System.out.print("   "); //2 space for hex, and 1 as separator
					}
					if (count < BYTE_PER_GROUP) {
						System.out.print("   ");//extra three to compensate the group separator
					}
					System.out.print("   ");//separator between hex and ascii
					count = 0;
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
		
	}
	
	/**
	 * Do a quick resolve by calling InetAddress.getByName in a new thread.
	 * 
	 * @param hostname
	 * @return resolved address if resolved in time, null when not in time or
	 *         UnknownHostException was thrown
	 */
	public static InetAddress quickResolve(String hostname) {
		Resolver resolver = new Resolver(hostname);
		Thread t = new Thread(resolver);
		t.start();
		try {
			t.join(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return resolver.get();
	}

	private static class Resolver implements Runnable {
		private String hostname;
		private InetAddress address = null;
		public Resolver(String hostname) {
			this.hostname = hostname;
		}

		public void run() {
			try {
				InetAddress addr = InetAddress.getByName(hostname);
				this.address = addr;
			} catch (UnknownHostException e) {
			}
		}

		public synchronized InetAddress get() {
			return address;
		}
	}
}
