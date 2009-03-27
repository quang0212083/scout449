package org.s449;

import java.util.Map;
import java.util.Set;
import org.s449.SecondTime;

/**
 * An event is a regional (or national) with its own set of teams
 *  and matches.
 * 
 * @author Stephen Carlson
 * @version 2.0.0
 */
public class Event implements java.io.Serializable, Comparable<Event> {
	private static final long serialVersionUID = 5321978342178349870L;

	/**
	 * Schedule of matches.
	 */
	private Map<SecondTime, ScheduleItem> schedule;
	/**
	 * The map of teams.
	 */
	private Map<Integer, Team> data;
	/**
	 * The regional's name code.
	 */
	private String code;
	/**
	 * The regional's informal name.
	 */
	private String name;
	/**
	 * The starting date of this event.
	 */
	private long startDate;
	/**
	 * The ending date of this event.
	 */
	private long endDate;
	/**
	 * How late is FIRST?
	 */
	private int minutesLate;

	/**
	 * Creates a blank event.
	 */
	public Event() {
		schedule = null;
		data = null;
		code = "";
		name = "";
		setStartDate(0L);
		setEndDate(0L);
		minutesLate = 0;
	}
	/**
	 * Creates a new event with the given parameters.
	 * 
	 * @param sched the schedule of events
	 * @param dat the list of teams
	 * @param cd the regional's code. Can be "" but not null.
	 * @param name the regional's name. Can be "" but not null.
	 * @param startDate the starting date
	 * @param endDate the ending date
	 */
	public Event(Map<SecondTime, ScheduleItem> sched, Map<Integer, Team> dat, String cd,
			 String name, long startDate, long endDate) {
		schedule = sched;
		data = dat;
		code = cd;
		this.name = name;
		setStartDate(startDate);
		setEndDate(endDate);
		minutesLate = 0;
	}
	/**
	 * Gets the map of teams.
	 * 
	 * @return the map of teams
	 */
	public Map<Integer, Team> getTeams() {
		return data;
	}
	/**
	 * Convenience operation to get a team.
	 * 
	 * @param team the team to look up
	 * @return the team, or null if there is no such team
	 */
	public Team get(int team) {
		return data.get(team);
	}
	/**
	 * Replaces the map of teams.
	 * 
	 * @param data the new map of teams
	 */
	public void setTeams(Map<Integer, Team> data) {
		this.data = data;
	}
	/**
	 * Gets the schedule.
	 * 
	 * @return the schedule
	 */
	public Map<SecondTime, ScheduleItem> getSchedule() {
		return schedule;
	}
	/**
	 * Replaces the schedule wholesale with a new list.
	 * 
	 * @param schedule the new schedule
	 */
	public void setSchedule(Map<SecondTime, ScheduleItem> schedule) {
		this.schedule = schedule;
	}
	/**
	 * Gets the events's code.
	 * 
	 * @return the event's name code
	 */
	public String getCode() {
		return code;
	}
	/**
	 * Sets the events's code.
	 * 
	 * @param code the new name code
	 */
	public void setCode(String code) {
		this.code = code;
	}
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Event)) return false;
		Event e = (Event)o;
		return e.getCode().equals(code) && e.getName().equals(name);
	}
	public int hashCode() {
		return code.hashCode();
	}
	public String toString() {
		return name;
	}
	
	/**
	 * Gets the set of all the teams in this event.
	 * 
	 * @return a Set with all of the team numbers in this event
	 */
	public Set<Integer> teamSet() {
		return data.keySet();
	}
	/**
	 * Gets the number of teams in this event.
	 * 
	 * @return the number of teams
	 */
	public int numTeams() {
		return data.size();
	}
	/**
	 * Gets the event's name.
	 * 
	 * @return the event's name
	 */
	public String getName() {
		return name;
	}
	/**
	 * Sets the event's name.
	 * 
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * Gets the starting date of this event.
	 * 
	 * @return the starting date
	 */
	public long getStartDate() {
		return startDate;
	}
	/**
	 * Sets the starting date of this event.
	 * 
	 * @param startDate the new starting date
	 */
	public void setStartDate(long startDate) {
		this.startDate = startDate;
	}
	/**
	 * Gets the ending date of this event.
	 * 
	 * @return the ending date
	 */
	public long getEndDate() {
		return endDate;
	}
	/**
	 * Sets the ending date of this event.
	 * 
	 * @param endDate the new ending date
	 */
	public void setEndDate(long endDate) {
		this.endDate = endDate;
	}
	/**
	 * Checks to see if the given time is within this event.
	 * 
	 * @return whether this event is now!
	 */
	public boolean duringRegional(long time) {
		return startDate < time && time < endDate;
	}
	public int compareTo(Event other) {
		if (equals(other)) return 0;
		long d = other.getStartDate() - startDate;
		if (d < 0) return -1;
		if (d > 0) return 1;
		return 0;
	}
	/**
	 * Gets the minutes late of this event.
	 *
	 * @return how many minutes FIRST is late
	 */
	public int getMinutesLate() {
		return minutesLate;
	}
	/**
	 * Changes the minutes late of this event.
	 *
	 * @param minutesLate how many minutes FIRST is now late (very late...)
	 */
	public void setMinutesLate(int minutesLate) {
		this.minutesLate = minutesLate;
	}
}