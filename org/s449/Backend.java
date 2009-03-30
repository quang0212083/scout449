package org.s449;

import java.util.*;

/**
 * Controls java access to the scouting data. An abstract superclass
 *  of all backends.
 * 
 * @author Stephen Carlson
 * @version 4.0
 */
public abstract class Backend {
	/**
	 * The data store.
	 */
	protected DataStore data;
	/**
	 * Whether write-back is enabled or available.
	 */
	protected boolean writable;

	/**
	 * Creates a new backend.
	 */
	protected Backend() {
		writable = false;
		data = null;
	}
	/**
	 * Refreshes the data from data storage.
	 */
	public abstract void flush();
	/**
	 * Closes the backend.
	 */
	public void close() { }
	/**
	 * Changes out the entire data store. This is a dangerous method!
	 * 
	 * @param data the new data store
	 */
	public void setData(DataStore data) {
		this.data = data;
	}
	/**
	 * Sets the active event to a specified event.
	 * 
	 * @param what the event to specify as active
	 */
	public void setActive(Event what) {
		data.setActive(what);
	}
	/**
	 * Gets the currently active event.
	 * 
	 * @return the active event, or null if no event is active
	 */
	public Event getActive() {
		return data.getActive();
	}
	/**
	 * Gets the Team object for a given team number.
	 * 
	 * @param team the team number
	 * @return the team for that team number, or null if there is not
	 *  a team by that number in the active event
	 */
	public Team get(int team) {
		return data.getActive().get(team);
	}
	/**
	 * Gets the Team object for a given team number.
	 * 
	 * @param team the team number
	 * @return the team for that team number, or null if there is not
	 *  a team by that number in the active event
	 */
	public Team get(Integer team) {
		return data.getActive().get(team);
	}
	/**
	 * Gets the number of teams in the current event's list.
	 * 
	 * @return the number of teams in the list
	 */
	public int count() {
		return data.getActive().numTeams();
	}
	/**
	 * Gets the sorted set of all teams for the current event's list.
	 * 
	 * @return a Set of the team numbers in this list
	 */
	public Set getTeams() {
		return data.getActive().teamSet();
	}
	/**
	 * Gets the version of the backend program.
	 * 
	 * @return the version
	 * @deprecated Use Constants.VERSION instead.
	 */
	public String getVersion() {
		return Constants.VERSION;
	}
	/**
	 * Gets the current event's schedule.
	 * 
	 * @return the schedule
	 */
	public Map getSchedule() {
		return data.getActive().getSchedule();
	}
	/**
	 * Determines whether the file is writable.
	 * 
	 * @return the writable flag
	 */
	public boolean isWritable() {
		return writable;
	}
	/**
	 * Gets the data store responsible for this backend
	 * 
	 * @return the data store
	 */
	public DataStore getData() {
		return data;
	}
	/**
	 * Gets the event for the given regional code.
	 * 
	 * @param code the code to look
	 * @return the event
	 */
	public Event getEvent(String code) {
		return data.getEvent(code);
	}
	/**
	 * Initializes the FIRST rank for all teams in the pool.
	 */
	public void firstRank() {
		if (data != null) data.firstRank();
	}
	/**
	 * Gets the configuration data.
	 * 
	 * @return the configuration data
	 */
	public Object[] getConfig() {
		return null;
	}
	/**
	 * Adds the specified team. Note that this may cause issues with some parts
	 *  of the program.
	 * 
	 * @param toAdd the team to add
	 */
	public void addTeam(Team toAdd) {
		data.getActive().getTeams().put(new Integer(toAdd.getNumber()), toAdd);
	}
	/**
	 * Removes the specified team. This WILL cause issues with some parts
	 *  of the program; use with care!
	 * 
	 * @param toRemove
	 */
	public void removeTeam(int toRemove) {
		data.getActive().getTeams().remove(new Integer(toRemove));
	}
	/**
	 * Adds the match to the queue.
	 * 
	 * @param match the match to add
	 */
	public void addMatch(ScheduleItem match) {
		synchronized (getSchedule()) {
			addMatch0(match);
		}
	}
	/**
	 * Adds a list of matches to the queue.
	 * 
	 * @param match the match list to add
	 */
	public void addMatches(Collection match) {
		synchronized (getSchedule()) {
			Iterator it = match.iterator();
			while (it.hasNext())
				addMatch0((ScheduleItem)it.next());
		}
	}
	private void addMatch0(ScheduleItem match) {
		SecondTime tm = new SecondTime(match.getTime());
		getSchedule().put(tm, match);
		Iterator it = match.getTeams().iterator();
		Team team;
		while (it.hasNext()) {
			team = get((Integer)it.next());
			team.getMatches().put(tm, match);
			team.validate();
		}
		firstRank();
	}
	/**
	 * Removes the match from the queue.
	 * 
	 * @param match the match to remove
	 */
	public void delMatch(ScheduleItem match) {
		synchronized (getSchedule()) {
			delMatch0(match);
		}
	}
	/**
	 * Removes a list of matches from the queue.
	 * 
	 * @param matches the matches to remove
	 */
	public void delMatches(Collection match) {
		synchronized (getSchedule()) {
			Iterator it = match.iterator();
			while (it.hasNext())
				delMatch0((ScheduleItem)it.next());
		}
	}
	private void delMatch0(ScheduleItem match) {
		SecondTime tm = new SecondTime(match.getTime());
		getSchedule().remove(tm);
		Iterator it = match.getTeams().iterator();
		Team team;
		while (it.hasNext()) {
			team = get((Integer)it.next());
			team.getMatches().remove(tm);
			team.validate();
		}
		firstRank();
	}
	/**
	 * Edits the match (possibly changing its time).
	 * 
	 * @param oldTime the old time
	 * @param match the new match data
	 */
	public void editMatch(long oldTime, ScheduleItem match) {
		synchronized (getSchedule()) {
			ScheduleItem old = (ScheduleItem)getSchedule().get(new SecondTime(oldTime));
			if (old != null) delMatch0(old);
			addMatch0(match);
		}
	}
	/**
	 * Indicates that FIRST is running late.
	 * 
	 * @param minutes the number of minutes that FIRST is late.
	 */
	public void runLate(int minutes) {
		getActive().setMinutesLate(minutes);
	}
	/**
	 * Scores a match.
	 * 
	 * @param match the match to score, with scored data filled in
	 */
	public void scoreMatch(ScheduleItem match) {
		// init vars
		SecondTime tm = new SecondTime(match.getTime());
		Map schedule = getSchedule();
		Iterator it = match.getTeams().iterator();
		Team team;
		match.setStatus(ScheduleItem.COMPLETE);
		// update schedule
		synchronized (schedule) {
			schedule.put(tm, match);
		}
		// update team scores
		while (it.hasNext()) {
			team = get((Integer)it.next());
			if (team != null) {
				team.getMatches().put(tm, match);
				team.validate();
			}
		}
		firstRank();
	}
	/**
	 * Updates a comment for this team.
	 * 
	 * @param team the team to edit
	 * @param comment the new comment
	 */
	public void updateComment(int team, Comment comment) {
		get(team).updateComment(comment);
	}
	/**
	 * Sets the robot type for a team.
	 * 
	 * @param team the team to edit
	 * @param newType the new robot type
	 */
	public void setType(int team, String newType) {
		get(team).setType(newType);
	}
	/**
	 * Checks for an update for server-push backends.
	 * 
	 * @return whether the data has changed (thus requiring UI update!)
	 */
	public boolean updateCheck() {
		return false;
	}
	/**
	 * Does a self-check on the data and throws a RuntimeException
	 *  if something is awry.
	 */
	public void selfCheck() {
		if (data.getEvents() == null || data.getEvents().size() < 1)
			throw new RuntimeException("no events");
		if (data.getExtraData() == null)
			throw new RuntimeException("no extra data");
		if (data.getHotkeys() == null)
			throw new RuntimeException("no hotkeys");
		if (data.getMyTeam() < 1)
			throw new RuntimeException("no user team defined");
		if (data.getTypes() == null)
			throw new RuntimeException("no robot types");
		if (data.getUDFs() == null)
			throw new RuntimeException("no UDFs");
		Event evt;
		Iterator it = data.getEvents().iterator();
		while (it.hasNext()) {
			evt = (Event)it.next();
			if (evt.getCode() == null || evt.getName() == null)
				throw new RuntimeException("event has no code or name");
			if (evt.getStartDate() / 1000L >= evt.getEndDate() / 1000L)
				throw new RuntimeException("event " + evt + " has an invalid date");
			if (evt.getSchedule() == null || evt.getTeams() == null)
				throw new RuntimeException("event " + evt + " has no teams or schedule");
		}
	}
}