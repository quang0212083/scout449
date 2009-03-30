package org.s449;

import java.io.IOException;
import java.util.*;
import org.s449.Server.ScoutConnection;

/**
 * A class that recieves commands (possibly queued lists of commands)
 *  from a web backend and processes them into a real backend.
 * 
 * @author Stephen Carlson
 * @version 1.0.0
 */
public class WebReciever {
	/**
	 * The place to record the commands.
	 */
	private Backend data;
	/**
	 * The server configuration.
	 */
	private Object[] config;
	/**
	 * The config file with user list.
	 */
	private UserStore users;

	/**
	 * Creates a new web reciever with commands going to the specified sink.
	 * 
	 * @param conf the user list configuration file
	 * @param sink the backend where all commands will be recorded
	 * @param config the configuration of this server.
	 */
	public WebReciever(UserStore conf, Backend sink, Object[] cfg) {
		users = conf;
		data = sink;
		config = cfg;
	}
	/**
	 * Changes the server configuration.
	 * 
	 * @param newCfg the new configuration
	 */
	public void setConfiguration(Object[] newCfg) {
		config = newCfg;
	}
	/**
	 * Sends a complete data update to the given client.
	 * 
	 * @param conn the connection to update
	 * @throws IOException if an I/O error occurs
	 */
	public void update(Connection conn) {
		try {
			conn.write(RequestObject.updateSend(data.getData()));
			conn.flush();
		} catch (IOException e) {
			throw new RuntimeException("Client closed!");
		}
	}
	/**
	 * Processes a request from the given request object.
	 * 
	 * @param first the request object to process
	 * @return whether an update is needed
	 */
	public boolean processRequest(RequestObject first, ScoutConnection input) throws IOException {
		boolean changed = false;
		int op = first.op;
		int teamNum = first.teamNum;
		UserData u;
		if (first.op == RequestObject.HANDSHAKE && first.obj != null &&
				(first.obj instanceof UserData)) {
			u = users.authUser((UserData)first.obj);
			// good or bad?
			input.write(RequestObject.handshake(u));
			input.flush();
			if (u == null) input.close();
			else input.setAuth(u);
			return false;
		}
		if (!input.isAuth()) return false;
		u = input.getAuth();
		Team targ = data.get(teamNum);
		if (op == RequestObject.CONFIG) {
			// send configuration
			input.write(RequestObject.serverConfigSend(config, Constants.VERSION_FULL));
			input.flush();
		} else if (op == RequestObject.UPDATE)
			// force update
			update(input);
		else if (op == RequestObject.COMMCHANGE && first.data != null
				&& targ != null && u.canWrite()) {
			// set type
			data.setType(teamNum, first.data);
			changed = true;
		} else if (op == RequestObject.COMMSET && first.obj != null
				&& (first.obj instanceof Comment) && targ != null) {
			// set comments
			Comment c = (Comment)first.obj;
			if (c != null && (u.isAdmin() || (u.canWrite() && u.equals(c.getOwner())))) {
				// must be allowed
				data.updateComment(teamNum, c);
				changed = true;
			}
		} else if (op == RequestObject.LATE && u.canScore()) {
			// running late
			data.runLate(first.index);
			changed = true;
		} else if (op == RequestObject.ADDTEAM && first.obj != null
				&& (first.obj instanceof Team) && u.isAdmin()) {
			// add a team
			data.addTeam((Team)first.obj);
			changed = true;
		} else if (op == RequestObject.DELTEAM && targ != null && u.isAdmin()) {
			// remove a team
			data.removeTeam(first.teamNum);
			changed = true;
		} else if (op == RequestObject.ADDMATCH && first.obj != null
				&& (first.obj instanceof ScheduleItem) && u.canScore()) {
			// add a match at the given time
			data.addMatch((ScheduleItem)first.obj);
			changed = true;
		} else if (op == RequestObject.ADDMATCH && first.obj != null
				&& (first.obj instanceof Collection) && u.canScore()) {
			// add a list of matches
			Collection ls = (Collection)first.obj;
			if (ls.size() > 0) {
				data.addMatches(ls);
				changed = true;
			}
		} else if (op == RequestObject.DELMATCH && first.obj != null
				&& (first.obj instanceof ScheduleItem) && u.canScore()) {
			// remove a match
			data.delMatch((ScheduleItem)first.obj);
			changed = true;
		} else if (op == RequestObject.DELMATCH && first.obj != null
				&& (first.obj instanceof Collection) && u.canScore()) {
			// remove a list of matches
			Collection ls = (Collection)first.obj;
			if (ls.size() > 0) {
				data.delMatches(ls);
				changed = true;
			}
		} else if (op == RequestObject.EDITMATCH && first.obj != null
				&& (first.obj instanceof ScheduleItem) && first.time > 0L
				&& u.canScore()) {
			// re-time a match
			data.editMatch(first.time, (ScheduleItem)first.obj);
			changed = true;
		} else if (op == RequestObject.SCOREMATCH && first.obj != null &&
				(first.obj instanceof ScheduleItem) && u.canScore()) {
			// match score entry
			data.scoreMatch((ScheduleItem)first.obj);
			changed = true;
		}
		return changed;
	}
	/**
	 * Changes the user configuration of this web reciever.
	 * 
	 * @param users the new configuration
	 */
	public void setUsers(UserStore users) {
		this.users = users;
	}
}