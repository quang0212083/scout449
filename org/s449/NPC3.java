package org.s449;

import java.net.*;
import java.util.*;

/**
 * A class that manages automatic server detection.
 * 
 * @author Stephen Carlson
 * @version revision 1
 */
public class NPC3 implements Runnable {
	/**
	 * The broadcast address. This is the same as NPCv2.
	 */
	private static final String broadcast = "230.0.0.0";
	/**
	 * The broadcast port. This is one less than NPCv2.
	 */
	private static final int port = 65532;
	/**
	 * The time-to-live of UDP packets.
	 */
	private static final int ttl = 31;
	/**
	 * The broadcast address as an InetAddress.
	 */
	private static InetAddress broad = null;
	/**
	 * The server list (filled by detect())
	 */
	private static List<NPCServer> servers;
	/**
	 * The ID of the source application.
	 * 0 is not allowed. 1 is taken by the "test" application.
	 * Others are available, but choose wisely!
	 */
	private static char id = 0;
	/**
	 * The thread used by the broadcaster to send server information.
	 */
	private static Thread thread = null;
	/**
	 * Whether the broadcaster is currently running.
	 */
	private static boolean running = false;
	/**
	 * The high byte of the ID.
	 */
	private static byte idHigh = 0;
	/**
	 * The low byte of the ID.
	 */
	private static byte idLow = 0;
	/**
	 * The name of this NPC instance.
	 */
	private static char[] myName;

	/**
	 * Gets the high byte of the given character.
	 * 
	 * @param in the source 2-byte character
	 * @return the high byte
	 */
	private static byte getHighByte(char in) {
		return (byte)(in / 256);
	}
	/**
	 * Gets the low byte of the given character.
	 * 
	 * @param in the source 2-byte character
	 * @return the low byte
	 */
	private static byte getLowByte(char in) {
		return (byte)(in % 256);
	}
	/**
	 * Sets the name of this computer. The limit is 32 characters
	 *  (any additional ones are truncated). Problems may occur
	 *  with some applications if this name is not unique (try
	 *  setting it to a random number or the IP address of the
	 *  source computer). This is more meaningful to servers than
	 *  clients.
	 * 
	 * @param name the new computer name/ID
	 * @throws IllegalStateException if the broadcaster is running
	 */
	public synchronized static void setName(String name) {
		if (running)
			throw new IllegalStateException("running");
		myName = new char[32];
		final char[] chars = name.toCharArray();
		for (int i = 0; i < 32 && i < chars.length; i++)
			myName[i] = chars[i];
	}
	/**
	 * Waits for 50 milliseconds.
	 */
	private static void step() {
		try {
			// this seems to be needed!
			Thread.sleep(50L);
		} catch (Exception e) { }
	}
	/**
	 * Sets the application ID. This allows multiple apps to
	 *  broadcast simultaneously.
	 * 
	 * @param id the new application ID
	 * @throws IllegalStateException if the broadcaster is running
	 */
	public synchronized static void setApplicationID(int id) {
		if (running)
			throw new IllegalStateException("running");
		NPC3.id = (char)id;
		idHigh = getHighByte(NPC3.id);
		idLow = getLowByte(NPC3.id);
	}
	/**
	 * Instances are limited to broadcasting.
	 */
	private NPC3() { }
	/**
	 * Initializes the broadcast address from the stored name.
	 */
	private static void initAddress() {
		if (broad == null) {
			try {
				broad = InetAddress.getByName(broadcast);
			} catch (Exception e) { }
		}
	}
	/**
	 * Starts broadcasting this computer's location to NPCv3 clients.
	 *  It might be wise to broadcast on NPCv2 as well, if appropriate.
	 *  If the system is already broadcasting, nothing happens.
	 * 
	 * @throws IllegalStateException if the application ID or name has
	 *  not yet been set
	 */
	public synchronized static void broadcast() {
		if (id == 0 || myName == null)
			throw new IllegalStateException("name and id must be set");
		if (!running) {
			thread = new Thread(new NPC3());
			thread.setPriority(Thread.MIN_PRIORITY);
			thread.setDaemon(true);
			thread.start();
			running = true;
		}
	}
	/**
	 * Stops the broadcasting of this computer's IP. If the broadcaster
	 *  is not running, nothing happens.
	 */
	public synchronized static void stopBroadcast() {
		if (running && sock != null) {
			sock.close();
			while (running) step();
		}
	}
	/**
	 * Searches the network for servers broadcasting their location.
	 *  This populates the servers list which can be retrieved by
	 *  using getServers().
	 * 
	 * @throws IllegalStateException if the application ID has not yet
	 *  been set
	 */
	public synchronized static void detect() {
		if (id == 0)
			throw new IllegalStateException("id must be set");
		initAddress();
		MulticastSocket sock = null;
		long start = System.currentTimeMillis();
		servers = new ArrayList<NPCServer>(8);
		try {
			final byte[] buffer = new byte[72];
			final byte[] local = InetAddress.getLocalHost().getAddress();
			populatePacket(buffer, 0, local, myName);
			final DatagramPacket toSend = new DatagramPacket(buffer, 72, broad, port);
			sock = new MulticastSocket(port);
			// 1.5 second timeout. This might need to be raised for Internet operations.
			sock.setSoTimeout(1500);
			sock.joinGroup(broad);
			sock.setTimeToLive(ttl);
			sock.send(toSend);
			String ip, name;
			final DatagramPacket pack = new DatagramPacket(buffer, 72);
			while (true) {
				try {
					sock.receive(pack);
				} catch (Exception e) {
					break;
				}
				if (opFromPacket(buffer) == 1) {
					// IP information packet
					ip = pack.getAddress().getHostAddress();
					name = nameFromPacket(buffer);
					step();
					servers.add(new NPCServer(ip, name, System.currentTimeMillis() - start));
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
		} finally {
			if (sock != null) try {
				sock.leaveGroup(broad);
				sock.close();
			} catch (Exception e) { }
		}
	}
	/**
	 * Gets the list of detected servers. detect() must be run first.
	 *  Note that this is quite unnecessary (why not return it from
	 *  detect()?), but exists for better NPCv2 compatibility.
	 * 
	 * @return the list of servers
	 */
	public static List<NPCServer> getServers() {
		return Collections.unmodifiableList(servers);
	}
	/**
	 * Populates a 72-byte NPC packet with information.
	 * 
	 * @param buffer the buffer to populate
	 * @param op the operation to perform
	 * @param ip the IP of the originating computer (this might be helpful
	 *  for NPCv3 relays)
	 * @param name the data (32 bytes) to place in the packet
	 */
	private static void populatePacket(byte[] buffer, int op, byte[] ip, char[] name) {
		buffer[0] = (byte)55;
		buffer[1] = (byte)op;
		buffer[2] = idHigh;
		buffer[3] = idLow;
		int i;
		for (i = 0; i < 4; i++)
			buffer[4 + i] = ip[i];
		for (i = 0; i < 32; i++) {
			buffer[8 + 2 * i] = getHighByte(name[i]);
			buffer[9 + 2 * i] = getLowByte(name[i]);
		}
	}
	/**
	 * Retrieves the application ID from a packet.
	 * 
	 * @param buffer the packet
	 * @return the application ID
	 */
	private static char idFromPacket(byte[] buffer) {
		return (char)((int)buffer[2] * 256 + (int)buffer[3]);
	}
	/**
	 * Retrieves the operation from a packet.
	 * 
	 * @param buffer the packet
	 * @return the operation
	 */
	private static int opFromPacket(byte[] buffer) {
		return (int)buffer[1];
	}
	/**
	 * Retrieves the name data from a packet.
	 * 
	 * @param buffer the packet
	 * @return the data
	 */
	private static String nameFromPacket(byte[] buffer) {
		final char[] name = new char[32];
		int hb, lb, size = 0;
		for (int i = 0; i < 32; i++) {
			hb = (int)buffer[8 + 2 * i];
			lb = (int)buffer[9 + 2 * i];
			if (hb == 0 && lb == 0) continue;
			size = i + 1;
			name[i] = (char)(hb * 256 + lb);
		}
		return new String(name, 0, size);
	}

	/**
	 * The instance multicast socket.
	 */
	private static MulticastSocket sock;
	/**
	 * Internal method to run the broadcaster.
	 */
	public void run() {
		sock = null;
		try {
			initAddress();
			final byte[] buffer = new byte[72];
			final DatagramPacket pack = new DatagramPacket(buffer, 72);
			DatagramPacket toSend;
			final byte[] local = InetAddress.getLocalHost().getAddress();
			sock = new MulticastSocket(port);
			sock.setBroadcast(true);
			sock.setTimeToLive(ttl);
			sock.joinGroup(broad);
			int op;
			while (true) {
				try {
					sock.receive(pack);
				} catch (Exception e) {
					break;
				}
				if (buffer[0] != 55 || idFromPacket(buffer) != id) continue;
				op = opFromPacket(buffer);
				if (op == 0) {
					// request IP and name of broadcasters
					step();
					populatePacket(buffer, 1, local, myName);
					toSend = new DatagramPacket(buffer, 72, pack.getAddress(), port);
					sock.send(toSend);
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
		} finally {
			try {
				if (sock != null) {
					sock.leaveGroup(broad);
					sock.close();
				}
			} catch (Exception e) { }
		}
		running = false;
	}
}