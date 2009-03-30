package org.s449;

import java.util.Collection;

/**
 * The actual object carrying binary information that is passed
 *  over the network.
 * 
 * @author Stephen Carlson
 * @version 2.0.0
 */
public class RequestObject implements Requestable {
	private static final long serialVersionUID = 4321978342178349870L;
	/**
	 * Constant meaning "update request/send"
	 */
	public static final int UPDATE = 0;
	/**
	 * Constant meaning "add match"
	 */
	public static final int ADDMATCH = 1;
	/**
	 * Constant meaning "delete match"
	 */
	public static final int DELMATCH = 2;
	/**
	 * Constant meaning "edit match"
	 */
	public static final int EDITMATCH = 3;
	/**
	 * Constant meaning "score match"
	 */
	public static final int SCOREMATCH = 4;
	/**
	 * Constant meaning "comment change"
	 */
	public static final int COMMCHANGE = 7;
	/**
	 * Constant meaning "config request/send"
	 */
	public static final int CONFIG = 8;
	/**
	 * Constant meaning "running late"
	 */
	public static final int LATE = 9;
	/**
	 * Constant meaning "set comments"
	 */
	public static final int COMMSET = 10;
	/**
	 * Constant meaning "send UDF"
	 */
	public static final int UDF = 11;
	/**
	 * Constant meaning "add/update team"
	 */
	public static final int ADDTEAM = 5;
	/**
	 * Constant meaning "delete team"
	 */
	public static final int DELTEAM = 6;
	/**
	 * Constant meaning "authentication"
	 */
	public static final int HANDSHAKE = 13;
	/**
	 * Constant meaning "system message"
	 */
	public static final int MSG = 14;
	//public static final int STATS = 12;

	/**
	 * The index (if applicable).
	 */
	public int index;
	/**
	 * The team number (if applicable).
	 */
	public int teamNum;
	/**
	 * A long integer for times (if applicable).
	 */
	public long time;
	/**
	 * The request operation.
	 */
	public int op;
	/**
	 * The string data (if applicable).
	 */
	public String data;
	/**
	 * The object data (if applicable).
	 */
	public Object obj;

	/**
	 * Gets a request object to add a team.
	 * 
	 * @param toAdd the team to add
	 * @return a request object to execute the command
	 */
	public static RequestObject teamAdd(Team toAdd) {
		RequestObject r = new RequestObject(ADDTEAM, null, 0, 0);
		r.obj = toAdd;
		return r;
	}
	/**
	 * Gets a request object to delete a team.
	 * 
	 * @param toDel the team to remove
	 * @return a request object to execute the command
	 */
	public static RequestObject teamDel(int toDel) {
		return new RequestObject(DELTEAM, null, 0, toDel);
	}
	/**
	 * Gets a request object to add a match.
	 * 
	 * @param match the match to add
	 * @return a request object to execute the command
	 */
	public static RequestObject matchAdd(ScheduleItem match) {
		RequestObject r = new RequestObject(ADDMATCH, null, 0, 0);
		r.obj = match;
		return r;
	}
	/**
	 * Gets a request object to add a list of matches.
	 * 
	 * @param match the match list to add
	 * @return a request object to execute the command
	 */
	public static RequestObject matchAdd(Collection match) {
		RequestObject r = new RequestObject(ADDMATCH, null, 0, 0);
		r.obj = match;
		return r;
	}
	/**
	 * Gets a request object to request server configuration.
	 * 
	 * @return a request object to execute the command
	 */
	public static RequestObject serverConfigRequest() {
		return new RequestObject(CONFIG, null, 0, 0);
	}
	/**
	 * Gets a request object to send server configuration.
	 * 
	 * @param data the server configuration
	 * @param vers the server version
	 * @return a request object to execute the command
	 */
	public static RequestObject serverConfigSend(Object[] data, String vers) {
		RequestObject r = new RequestObject(CONFIG, vers, 0, 0);
		r.obj = data;
		return r;
	}
	/**
	 * Gets a request object to update local data.
	 * 
	 * @return a request object to execute the command
	 */
	public static RequestObject updateRequest() {
		return new RequestObject(UPDATE, null, 0, 0);
	}
	/**
	 * Gets a request object to send an update.
	 * 
	 * @param data the data to send
	 * @return a request object to execute the command
	 */
	public static RequestObject updateSend(Object data) {
		RequestObject r = new RequestObject(UPDATE, null, 0, 0);
		r.obj = data;
		return r;
	}
	/**
	 * Gets a request object to delete a match.
	 * 
	 * @param toDel the match to remove
	 * @return a request object to execute the command
	 */
	public static RequestObject matchDel(ScheduleItem toDel) {
		RequestObject r = new RequestObject(DELMATCH, null, 0, 0);
		r.obj = toDel;
		return r;
	}
	/**
	 * Gets a request object to delete a list of matches.
	 * 
	 * @param toDel the match to remove
	 * @return a request object to execute the command
	 */
	public static RequestObject matchDel(Collection toDel) {
		RequestObject r = new RequestObject(DELMATCH, null, 0, 0);
		r.obj = toDel;
		return r;
	}
	/**
	 * Gets a request object to edit a match.
	 * 
	 * @param time the old time
	 * @param toEdit the match to edit
	 * @return a request object to execute the command
	 */
	public static RequestObject matchEdit(long time, ScheduleItem toEdit) {
		RequestObject r = new RequestObject(EDITMATCH, null, 0, 0);
		r.obj = toEdit;
		r.time = time;
		return r;
	}
	/**
	 * Gets a request object to score a match.
	 * 
	 * @param match the match to score (with the scored data!)
	 * @return a request object to execute the command
	 */
	public static RequestObject matchSend(ScheduleItem scores) {
		RequestObject r = new RequestObject(SCOREMATCH, null, 0, 0);
		r.obj = scores;
		return r;
	}
	/**
	 * Gets a request object to run late.
	 * 
	 * @param offset the number of minutes late
	 * @return a request object to execute the command
	 */
	public static RequestObject runLate(int offset) {
		return new RequestObject(LATE, null, offset, 0);
	}
	/**
	 * Gets a request object to set comments.
	 * 
	 * @param team the team to edit
	 * @param comment the new comment
	 * @return a request object to execute the command
	 */
	public static RequestObject commentSet(int team, Comment comment) {
		RequestObject r = new RequestObject(COMMSET, null, 0, team);
		r.obj = comment;
		return r;
	}
	/**
	 * Gets a request object to set a UDF.
	 * 
	 * @param team the team to edit
	 * @param field the field index
	 * @param value the new value
	 * @return a request object to execute the command
	 */
	public static RequestObject udf(int team, int field, int value) {
		RequestObject r = new RequestObject(UDF, null, field, team);
		r.obj = new Integer(value);
		return r;
	}
	/**
	 * Gets a request object to change a robot type.
	 * 
	 * @param team the team to edit
	 * @param newType the new robot type
	 * @return a request object to execute the command
	 */
	public static RequestObject setType(int team, String newType) {
		return new RequestObject(COMMCHANGE, newType, -1, team);
	}
	/**
	 * Authenticates with the server.
	 * 
	 * @param user the user information
	 * @return a request object to execute the command
	 */
	public static RequestObject handshake(UserData user) {
		RequestObject r = new RequestObject(HANDSHAKE, null, 0, 0);
		r.obj = user;
		return r;
	}
	/**
	 * Sends a system message.
	 * 
	 * @param message the message to send
	 * @return a request object to execute the command
	 */
	public static RequestObject sendMessage(String message) {
		return new RequestObject(MSG, message, 0, 0);
	}

	/**
	 * Creates a RequestObject with the given parameters.
	 * 
	 * @param loginInfo the login information
	 * @param op the operation (see the constants)
	 * @param data the string data in the object
	 * @param index the index to change (in a list)
	 * @param teamNum the team number for comments
	 */
	public RequestObject(int op, String data, int index, int teamNum) {
		this.op = op;
		this.data = data;
		this.index = index;
		this.teamNum = teamNum;
		obj = null;
		time = 0L;
	}
}