package org.s449;

import java.security.MessageDigest;

/**
 * Class to describe user data including user name, password,
 *  and access rights.
 * 
 * @author Stephen Carlson
 * @version 4.0.0
 */
public class UserData implements Comparable<UserData>, java.io.Serializable {
	private static final long serialVersionUID = 5321978342178349880L;

	/**
	 * The hex character key for encoding.
	 */
	private static final String hexChars = "0123456789ABCDEF";

	/**
	 * Hashes the char array (from a password field) into an SHA string.
	 * 
	 * @param text the string to be hashed
	 * @return the SHA1 hash
	 */
	public static final String hash(char[] text) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			byte[] dig = new byte[text.length * 2];
			for (int i = 0; i < text.length; i++) {
				dig[2 * i] = (byte)((int)text[i] / 256);
				dig[2 * i + 1] = (byte)((int)text[i] % 256);
				text[i] = '\u0000';
			}
			// hash, hash, hash!
			md.update(dig);
			for (int i = 0; i < dig.length; i++)
				dig[i] = (byte)0;
			StringBuilder bf = new StringBuilder(dig.length * 2);
			dig = md.digest();
			int hd;
			// convert to hex string
			for (int i = 0; i < dig.length; i++) {
				hd = 128 + (int)dig[i];
				bf.append(hexChars.charAt(hd / 16));
				bf.append(hexChars.charAt(hd % 16));
				dig[i] = (byte)0;
			}
			return bf.toString();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * User name.
	 */
	private String name;
	/**
	 * True user name.
	 */
	private String realName;
	/**
	 * The hashed password.
	 */
	private String pass;
	/**
	 * Access right bitmask.
	 */
	private int access;
	/**
	 * The team number for this user.
	 */
	private int teamNum;

	/**
	 * Creates a user right bitmask out of the given permissions.
	 * 
	 * @param canRead whether the user can read.
	 * @param canWrite whether the user can write other information.
	 *  If canWrite is true, canRead is assumed.
	 * @param canScore whether the user can save scores.
	 *  If canScore is true, canRead is assumed.
	 * @param isAdmin whether the user can use administrative fields.
	 *  If isAdmin is true, canWrite, canRead, and canScore are assumed.
	 * @return the access rights bitmask
	 */
	public static int accessMask(boolean canRead, boolean canWrite,
			boolean canScore, boolean isAdmin) {
		if (isAdmin) canScore = canWrite = true;
		if (canScore || canWrite) canRead = true;
		// if no permissions, kiosk only
		return ((isAdmin ? 1 : 0) << 3) | ((canWrite ? 1 : 0) << 2) |
			((canScore ? 1 : 0) << 1) | (canRead ? 1 : 0);
	}
	/**
	 * Returns whether this user can use anything other than the read-only kiosk.
	 * 
	 * @return whether the user can see our scouting data!
	 */
	public boolean canRead() {
		return (access & 1) > 0;
	}
	/**
	 * Returns whether this user can score matches.
	 * 
	 * @return whether the user is a scorer
	 */
	public boolean canScore() {
		return (access & 2) > 0;
	}
	/**
	 * Returns whether this user can write to team and match data.
	 * 
	 * @return whether the user can write to non-score fields
	 */
	public boolean canWrite() {
		return (access & 4) > 0;
	}
	/**
	 * Returns whether this user can use the admin tools.
	 * 
	 * @return whether the user is an administrator
	 */
	public boolean isAdmin() {
		return (access & 8) > 0;
	}
	/**
	 * Creates the default user with administrative privileges,
	 *  no password, and user name "admin". This is NOT RECOMMENDED;
	 *  only for initial configuration. This user should be removed
	 *  or password protected immediately!
	 */
	public UserData() {
		this.name = "admin";
		this.realName = "Administrator";
		this.pass = "";
		this.teamNum = Constants.DEFAULT_TEAM;
		this.access = 15; // 1 | 2 | 4 | 8
	}
	/**
	 * Creates a user authentication packet to be filled in later.
	 * 
	 * @param name the user name
	 * @param pass the password (WILL BE CLEARED!)
	 */
	public UserData(String name, char[] pass) {
		this.name = name;
		realName = null;
		if (pass.length < 1)
			this.pass = "";
		else
			this.pass = hash(pass);
		this.access = 0;
		this.teamNum = Constants.DEFAULT_TEAM;
	}
	/**
	 * Creates a new user with the given names, password, team number, and access.
	 * 
	 * @param name the user name
	 * @param realName the real user name
	 * @param pass the password
	 * @param access the access rights
	 * @param teamNum the team number
	 */
	public UserData(String name, String realName, char[] pass, int access, int teamNum) {
		this.name = name;
		this.realName = realName;
		if (pass.length < 1)
			this.pass = "";
		else
			this.pass = hash(pass);
		this.access = access;
		this.teamNum = teamNum;
	}
	public int compareTo(UserData o) {
		return name.compareTo(o.getName());
	}
	public int hashCode() {
		return name.hashCode();
	}
	public String toString() {
		return realName;
	}
	/**
	 * Gets the user name of this user.
	 * 
	 * @return the user name
	 */
	public String getName() {
		return name;
	}
	/**
	 * Gets the hashed password of this user.
	 * 
	 * @return the password
	 */
	public String getPass() {
		return pass;
	}
	/**
	 * Gets the access rights bitmask of this user.
	 * 
	 * @return the access rights
	 */
	public int getAccess() {
		return access;
	}
	/**
	 * Gets the real name of this user.
	 *
	 * @return the user's real name
	 */
	public String getRealName() {
		return realName;
	}
	/**
	 * Gets the team number of this user.
	 *
	 * @return the team number
	 */
	public int getTeamNum() {
		return teamNum;
	}
	/**
	 * Changes the team number of this user.
	 *
	 * @param teamNum the new team number
	 */
	protected void setTeamNum(int teamNum) {
		this.teamNum = teamNum;
	}
	/**
	 * Changes the real name of this user.
	 *
	 * @param realName the user's new real name
	 */
	protected void setRealName(String realName) {
		this.realName = realName;
	}
	/**
	 * Changes the password of this user.
	 *
	 * @param pass the new password
	 */
	protected void setPass(String pass) {
		this.pass = pass;
	}
	/**
	 * Changes the access rights of this user.
	 *
	 * @param access the new access rights
	 */
	protected void setAccess(int access) {
		this.access = access;
	}
	public boolean equals(Object o) {
		if (o == null || !(o instanceof UserData)) return false;
		UserData other = (UserData)o;
		return compareTo(other) == 0;
	}
}