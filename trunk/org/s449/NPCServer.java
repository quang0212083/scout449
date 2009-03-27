package org.s449;

/**
 * A class that represents a server found by NPCv3.
 * 
 * @author Stephen Carlson
 */
public class NPCServer {
	/**
	 * The server name.
	 */
	private String name;
	/**
	 * The server IP.
	 */
	private String ip;
	/**
	 * The ping (milliseconds to and from) the target.
	 */
	private long ping;

	/**
	 * Creates a new NPC server with the given parameters.
	 * 
	 * @param ip the IP address
	 * @param name the server name
	 * @param ping the ping time
	 */
	public NPCServer(String ip, String name, long ping) {
		this.ip = ip;
		this.name = name;
		this.ping = ping;
	}
	/**
	 * Gets the name of the NPC server.
	 * 
	 * @return the server name
	 */
	public String getName() {
		return name;
	}
	/**
	 * Gets the IP of the NPC server.
	 * 
	 * @return the server IP
	 */
	public String getIP() {
		return ip;
	}
	/**
	 * Gets the ping time of the NPC server.
	 * 
	 * @return the server ping time in milliseconds
	 */
	public long getPing() {
		return ping;
	}
	public boolean equals(Object other) {
		if (other == null || !(other instanceof NPCServer)) return false;
		return equals((NPCServer)other);
	}
	/**
	 * Checks for equality with the given server.
	 * 
	 * @param other the other object
	 * @return equality check!
	 */
	private boolean equals(NPCServer other) {
		return other.name.equals(name) && other.ping == ping && other.ip.equals(ip);
	}
	public String toString() {
		return "(" + ping + ") " + ip + " \"" + name + "\"";
	}
}