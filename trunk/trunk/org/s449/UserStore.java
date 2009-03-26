package org.s449;

import java.util.*;

/**
 * The version 4 server configuration file, typically found at
 *  "users.dat". It contains authentication information.
 * 
 * @author Stephen Carlson
 */
public class UserStore implements java.io.Serializable {
	private static final long serialVersionUID = 5321978342178349877L;

	/**
	 * The user data store.
	 */
	private HashMap<String, UserData> user;
	/**
	 * The name of the computer/server.
	 */
	private String myName;
	/**
	 * The extra configuration data.
	 */
	private Object[] extraData;

	/**
	 * Creates a default configuration file.
	 */
	public UserStore() {
		// default configuration
		UserData def = new UserData();
		user = new HashMap<String, UserData>(16);
		user.put(def.getName(), def);
		extraData = null;
		myName = "default";
	}
	/**
	 * Authenticates the specified user.
	 * 
	 * @param name the user name to look up
	 * @return access allowed? nil if false or user if true
	 */
	public UserData authUser(UserData what) {
		UserData u = user.get(what.getName());
		if (u != null && u.getName().equals(what.getName()) &&
				u.getPass().equals(what.getPass()))
			return u;
		return null;
	}
	/**
	 * Gets the user with the specified name.
	 * 
	 * @param name the user name to look up
	 * @return the user's info, or null if no such user
	 */
	public UserData getUser(String name) {
		return user.get(name);
	}
	/**
	 * Changes the user with the specified name.
	 * 
	 * @param name the user name to modify
	 * @param newData the new user data
	 */
	public void setUserData(String name, UserData newData) {
		if (!name.equals(newData.getName()))
			throw new IllegalArgumentException("user name must match");
		user.put(name, newData);
	}
	/**
	 * Gets the entire user array.
	 * 
	 * @return the user array
	 */
	protected Map<String, UserData> getUsers() {
		return user;
	}
	/**
	 * Removes the user with the specified name.
	 * 
	 * @param name the user name to remove
	 */
	public void removeUser(String name) {
		user.remove(name);
	}
	/**
	 * Gets the name of this computer.
	 * 
	 * @return the name
	 */
	public String getName() {
		return myName;
	}
	/**
	 * Changes the name of this computer.
	 * 
	 * @param newName the new name
	 */
	public void setName(String newName) {
		myName = newName;
	}
	/**
	 * Gets the extra data in this configuration.
	 * 
	 * @return the extra data (could be anything...)
	 */
	public Object[] getExtraData() {
		return extraData;
	}
	/**
	 * Changes the extra data in this configuration.
	 * 
	 * @param extraData the new extra data
	 */
	public void setExtraData(Object[] extraData) {
		this.extraData = extraData;
	}
}