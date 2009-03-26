package org.s449;

import java.util.*;

/**
 * A DataStore is a master data holder with a list of regionals.
 *  Corresponds to a yearly robotics competition (all of it!)
 * 
 * @author Stephen Carlson
 * @version 4.0.0
 */
public class DataStore implements java.io.Serializable {
	private static final long serialVersionUID = 5321978342178349872L;

	/**
	 * The events in this data file.
	 */
	private List<Event> events;
	/**
	 * The User-Defined Field names.
	 */
	private List<UDF> udfs;
	/**
	 * The match labels.
	 */
	private List<MatchLabel> labels;
	/**
	 * The hotkeys to be used in score keeping.
	 */
	private HotkeyList hotkeys;
	/**
	 * The list of robot types.
	 */
	private List<String> types;
	/**
	 * The default team!
	 */
	private int myTeam;
	/**
	 * The extra configuration.
	 */
	private Object[] extra;
	/**
	 * The currently active event.
	 */
	private Event active;
	/**
	 * Advanced scoring enabled?
	 */
	private boolean advScore;

	/**
	 * Creates a blank data file.
	 */
	public DataStore() {
		events = null;
		hotkeys = null;
		udfs = null;
		active = null;
		advScore = false;
	}
	/**
	 * Creates a data file with the given list of events.
	 * 
	 * @param events the list of events
	 */
	public DataStore(List<Event> events) {
		this.events = events;
		hotkeys = null;
		udfs = null;
		advScore = false;
	}
	/**
	 * Gets the User-Defined Field list.
	 * 
	 * @return the list of UDF titles
	 */
	public List<UDF> getUDFs() {
		return udfs;
	}
	/**
	 * Sets the User-Defined Field list.
	 * 
	 * @param udfs the new list of UDF titles
	 */
	public void setUDFs(List<UDF> udfs) {
		this.udfs = udfs;
	}
	/**
	 * Gets the events contained in this data store.
	 * 
	 * @return the list of events
	 */
	public List<Event> getEvents() {
		return events;
	}
	/**
	 * Gets the event for the given regional code.
	 * 
	 * @param code the code to look
	 * @return the event
	 */
	public Event getEvent(String code) {
		Iterator<Event> it = events.iterator();
		Event item;
		while (it.hasNext()) {
			item = it.next();
			if (item.getCode().equalsIgnoreCase(code))
				return item;
		}
		return null;
	}
	/**
	 * Sets the events contained in this data store.
	 * 
	 * @param events the new list of events
	 */
	public void setEvents(List<Event> events) {
		this.events = events;
	}
	/**
	 * Gets the hot key values associated with characters.
	 * 
	 * @return the hot key values
	 */
	public HotkeyList getHotkeys() {
		return hotkeys;
	}
	/**
	 * Sets the hot key values associated with characters
	 * 
	 * @param hotkeys the new hot key values
	 */
	public void setHotkeys(HotkeyList hotkeys) {
		this.hotkeys = hotkeys;
	}
	/**
	 * Gets the list of robot types.
	 * 
	 * @return the list of robot types
	 */
	public List<String> getTypes() {
		return types;
	}
	/**
	 * Sets the list of robot types.
	 * 
	 * @param types the new list of robot types
	 */
	public void setTypes(List<String> types) {
		this.types = types;
	}
	/**
	 * Gets your team.
	 * 
	 * @return your team number
	 */
	public int getMyTeam() {
		return myTeam;
	}
	/**
	 * Changes your team.
	 * 
	 * @param myTeam your new team
	 */
	public void setMyTeam(int myTeam) {
		this.myTeam = myTeam;
	}
	/**
	 * Gets the extra data.
	 * 
	 * @return the extra data
	 */
	public Object[] getExtraData() {
		return extra;
	}
	/**
	 * Sets the extra data.
	 * 
	 * @param extra the new extra data
	 */
	public void setExtraData(Object[] extra) {
		this.extra = extra;
	}
	/**
	 * Gets the match labels.
	 *
	 * @return the match labels
	 */
	public List<MatchLabel> getLabels() {
		return labels;
	}
	/**
	 * Sets the match labels.
	 *
	 * @param labels the new match labels
	 */
	public void setLabels(List<MatchLabel> labels) {
		this.labels = labels;
	}
	/**
	 * Sets the active event to a specified event.
	 * 
	 * @param what the event to specify as active
	 */
	public void setActive(Event what) {
		active = what;
		firstRank();
	}
	/**
	 * Gets the currently active event.
	 * 
	 * @return the active event, or null if no event is active
	 */
	public Event getActive() {
		return active;
	}
	/**
	 * Gets how many minutes late FIRST is.
	 * 
	 * @return the number of minutes late
	 */
	public int minutesLate() {
		return active.getMinutesLate();
	}
	/**
	 * Gets whether advanced scoring is enabled.
	 *
	 * @return whether advanced scoring is enabled
	 */
	public boolean isAdvScore() {
		return advScore;
	}
	/**
	 * Enables or disables advanced scoring.
	 *
	 * @param advScore whether advanced scoring should be enabled
	 */
	public void setAdvScore(boolean advScore) {
		this.advScore = advScore;
	}
	/**
	 * Initializes the FIRST rank for all teams in the pool.
	 */
	public void firstRank() {
		if (active != null) {
			ArrayList<Team> list = new ArrayList<Team>(active.getTeams().values());
			Collections.sort(list, FIRSTComparator.instance);
			int i = 1;
			Iterator<Team> it = list.iterator();
			while (it.hasNext()) {
				it.next().setFIRSTRank(i);
				i++;
			}
		}
	}
}