package org.s449;

import java.io.IOException;
import java.util.*;

/**
 * Controls java access to the scouting data (user interface).
 * 
 * @author Stephen Carlson
 * @version 4.0.0
 */
public class WebBackend extends Backend {
	/**
	 * The connection!
	 */
	private Connection conn;
	/**
	 * The current host.
	 */
	private String host;
	/**
	 * The current port.
	 */
	private int port;
	/**
	 * The parent of this WebBackend.
	 */
	private ScoutStatus status;
	/**
	 * The cached configuration.
	 */
	private Object[] cachedConfig;
	/**
	 * The saved user data from the last auth. Used for reconnect.
	 */
	private UserData user;
	/**
	 * The last command. Used for reconnect.
	 */
	private RequestObject last;
	/**
	 * The version fetched from getConfig.
	 */
	private String serverVersion;
	/**
	 * The last connection try.
	 */
	private long lastTry;

	/**
	 * Creates a web backend on the specified host and port.
	 * 
	 * @param newHost the host to which to connect
	 * @param newPort the port to which to connect
	 */
	public WebBackend(String newHost, int newPort, ScoutStatus parent) throws IOException {
		super();
		cachedConfig = null;
		this.status = parent;
		host = newHost;
		port = newPort;
		last = null;
		serverVersion = Constants.VERSION;
		createHost();
		lastTry = 0L;
		AppLib.sleep(50L);
	}
	private void createHost() throws IOException {
		conn = new Connection(host, port);
	}
	/**
	 * Gets the server version (if configuration has been sent)
	 * 
	 * @return the server version
	 */
	public String getServerVersion() {
		return serverVersion;
	}
	public synchronized Object[] getConfig() {
		if (conn == null) return null;
		if (cachedConfig == null) {
			send(RequestObject.serverConfigRequest());
			RequestObject r = null;
			AppLib.sleep(50L);
			long bail = System.currentTimeMillis() + 2000L;
			while (r == null) try {
				if (conn == null)
					return null;
				while (!conn.ready()) {
					if (System.currentTimeMillis() > bail) {
						AppLib.printDebug("No server configuration - bailing out!");
						return null;
					}
					AppLib.sleep(10L);
				}
				r = (RequestObject) conn.read();
				if (r.op != RequestObject.CONFIG || r.time < 0L
						|| r.obj == null || !(r.obj instanceof Object[]))
					r = null;
				else if (r.data != null) serverVersion = r.data;
			} catch (Exception e) {
				AppLib.debugException(e);
				return null;
			}
			cachedConfig = (Object[]) r.obj;
		}
		return cachedConfig;
	}
	public synchronized void addTeam(Team toAdd) {
		send(RequestObject.teamAdd(toAdd));
		super.addTeam(toAdd);
	}
	public synchronized void removeTeam(int toDel) {
		send(RequestObject.teamDel(toDel));
		super.removeTeam(toDel);
	}
	public synchronized void addMatch(ScheduleItem match) {
		send(RequestObject.matchAdd(match));
		super.addMatch(match);
	}
	public synchronized void addMatches(Collection<ScheduleItem> match) {
		send(RequestObject.matchAdd(match));
		super.addMatches(match);
	}
	public synchronized void delMatch(ScheduleItem match) {
		send(RequestObject.matchDel(match));
		super.delMatch(match);
	}
	public synchronized void delMatches(Collection<ScheduleItem> match) {
		send(RequestObject.matchDel(match));
		super.delMatches(match);
	}
	public synchronized void editMatch(long oldTime, ScheduleItem match) {
		send(RequestObject.matchEdit(oldTime, match));
		super.editMatch(oldTime, match);
	}
	public synchronized void runLate(int minutes) {
		send(RequestObject.runLate(minutes));
		super.runLate(minutes);
	}
	public synchronized void scoreMatch(ScheduleItem match) {
		send(RequestObject.matchSend(match));
		super.scoreMatch(match);
	}
	public synchronized void updateComment(int team, Comment comment) {
		send(RequestObject.commentSet(team, comment));
		super.updateComment(team, comment);
	}
	public synchronized void setType(int team, String newType) {
		send(RequestObject.setType(team, newType));
		super.setType(team, newType);
	}
	public synchronized UserData auth(UserData user) {
		if (conn == null) return null;
		send(RequestObject.handshake(user));
		RequestObject r = null;
		AppLib.sleep(50L);
		long bail = System.currentTimeMillis() + 2000L;
		while (r == null) try {
			while (!conn.ready()) {
				if (System.currentTimeMillis() > bail || conn.isDead()) {
					AppLib.printDebug("No authentication - bailing out!");
					this.user = null;
					return null;
				}
				AppLib.sleep(10L);
			}
			r = (RequestObject) conn.read();
			if (r.op != RequestObject.HANDSHAKE || r.obj == null || !(r.obj instanceof UserData))
				r = null;
		} catch (Exception e) {
			AppLib.debugException(e);
			return null;
		}
		this.user = (UserData)r.obj;
		return this.user;
	}
	public boolean updateCheck() {
		if (conn != null && conn.ready() && (conn.next() instanceof RequestObject)) {
			RequestObject r = (RequestObject)conn.next();
			if (r.op == RequestObject.UPDATE && r.obj != null && r.obj instanceof DataStore) {
				flushInput();
				return true;
			}
		} else if (conn != null && conn.isDead() && lastTry <= 0L) {
			status.sendMessage("conndrop");
			lastTry = System.currentTimeMillis();
		}
		return false;
	}
	private boolean sendRaw(Object o) {
		try {
			if (conn == null) return false;
			conn.write(o);
			last = null;
			lastTry = 0L;
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	private void send(RequestObject o) {
		if (conn == null) return;
		last = o;
		if (!sendRaw(o)) {
			reconnectPrompt();
			sendRaw(o);
		}
	}
	private void reconnectPrompt() {
		AppLib.printDebug("Attempting reconnection");
		try {
			createHost();
			if (auth(user) == null)
				throw new RuntimeException("authentication rejected");
			cachedConfig = null;
			getConfig();
			if (last != null && !sendRaw(last))
				AppLib.printDebug("Couldn't send cached request!");
		} catch (Exception e) {
			conn = null;
			if (status.isConnected()) status.setConnectionStatus(false);
		}
	}
	protected void finalize() {
		close();
	}
	public synchronized void close() {
		if (conn != null) conn.close();
		conn = null;
	}
	public synchronized void flush() {
		if (conn == null) reconnectPrompt();
		send(RequestObject.updateRequest());
		AppLib.sleep(50L);
		try {
			flushInput();
		} catch (Exception e) {
			reconnectPrompt();
			flushInput();
		}
	}
	private void flushInput() {
		data = null;
		while (data == null && conn != null) {
			Object o = conn.read();
			if (o instanceof RequestObject) {
				RequestObject r = (RequestObject)o;
				if (r.op == RequestObject.UPDATE && r.obj != null && r.obj instanceof DataStore)
					data = (DataStore)r.obj;
			}
		}
		firstRank();
	}
	/**
	 * Gets the current port.
	 * 
	 * @return the port to which this backend is connected
	 */
	public int getPort() {
		return port;
	}
	/**
	 * Gets the current host.
	 * 
	 * @return the host to which this backend is connected
	 */
	public String getHost() {
		return host;
	}
}