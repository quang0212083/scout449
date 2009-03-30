package org.s449;

import java.util.*;
import java.text.*;

/**
 * A class representing a match on the schedule.
 * 
 * @author Stephen Carlson
 * @version 4.0.0
 */
public class ScheduleItem implements Comparable, java.io.Serializable, Constants {
	private static final long serialVersionUID = 5321978342178349873L;
	/**
	 * The 12-hour time format.
	 */
	private static final DateFormat hour12 = new SimpleDateFormat("h:mm a");
	/**
	 * The 24-hour time format.
	 */
	private static final DateFormat hour24 = new SimpleDateFormat("k:mm");
	/**
	 * The format for dates.
	 */
	private static final DateFormat date = new SimpleDateFormat("EEEE, MMMM dd");
	/**
	 * Whether matches are to be in 24-hour time.
	 */
	public static boolean is24 = true;
	/**
	 * Status meaning match has not yet been scored.
	 */
	public static final boolean SCHEDULED = false;
	/**
	 * Status meaning match has been scored.
	 */
	public static final boolean COMPLETE = true;
	/**
	 * The unallocated team.
	 */
	public static final Team blankTeam = new Team("No Entrant", 0, 0);

	/**
	 * The teams in this match.
	 */
	private List teams;
	/**
	 * The scores of the teams.
	 */
	private List scores;
	/**
	 * The score of the #1 alliance.
	 */
	private int oneScore;
	/**
	 * The score of the #2 alliance.
	 */
	private int twoScore;
	/**
	 * The time that the match is scheduled to be played.
	 */
	private long time;
	/**
	 * The status of the match - completed or scheduled.
	 */
	private boolean status;
	/**
	 * The match number.
	 */
	private int num;
	/**
	 * The match label.
	 */
	private MatchLabel label;
	/**
	 * Whether the match counts towards records (practice?)
	 */
	private boolean counts;
	/**
	 * Surrogates?
	 */
	private BitSet surrogate;

	/**
	 * Creates a new match at a specified time.
	 * 
	 * @param newTeams the teams in this match
	 * @param timeAt the time
	 */
	public ScheduleItem(List newTeams, long timeAt, boolean counts) {
		teams = newTeams; time = timeAt;
		twoScore = oneScore = num = 0;
		scores = null; label = MatchLabel.blank;
		status = SCHEDULED;
		this.counts = counts;
		surrogate = new BitSet(newTeams.size());
	}
	/**
	 * Creates a duplicate ScheduleItem from an existing one.
	 * 
	 * @param sc the existing item
	 * @return a deep copy
	 */
	public static ScheduleItem copyValue(ScheduleItem sc) {
		// copy teams
		List nt = new ArrayList(sc.getTeams().size());
		nt.addAll(sc.getTeams());
		List scores;
		// copy scores
		if (sc.getScores() == null) scores = null;
		else {
			scores = new ArrayList(TPA * 2 + 1);
			Iterator it = sc.getScores().iterator();
			while (it.hasNext())
				scores.add(Score.copyValueOf((Score)it.next()));
		}
		// create item
		ScheduleItem n = new ScheduleItem(nt, sc.getTime(), sc.counts());
		n.setStatus(sc.getStatus());
		n.setTeams(nt);
		n.setScores(scores);
		n.setLabel(sc.getLabel());
		n.setRedScore(sc.getRedScore());
		n.setBlueScore(sc.getBlueScore());
		n.setNum(sc.getNum());
		for (int i = 0; i < 2 * TPA; i++)
			n.getSurrogate().clear(i);
		n.getSurrogate().or(sc.getSurrogate());
		return n;
	}
	/**
	 * Gets the time of this match.
	 * 
	 * @return the time
	 */
	public long getTime() {
		return time;
	}
	/**
	 * Sets the time of this match.
	 * 
	 * @param time the new time
	 */
	public void setTime(long time) {
		this.time = time;
	}
	/**
	 * Gets the list of teams in this match.
	 * 
	 * @return the list of team numbers in this match
	 */
	public List getTeams() {
		return teams;
	}
	public int compareTo(Object o) {
		if (!(o instanceof ScheduleItem)) return 0;
		ScheduleItem other = (ScheduleItem)o;
		if (time > other.time) return 1;
		if (time < other.time) return -1;
		return 0;
	}
	public boolean equals(Object other) {
		if (!(other instanceof ScheduleItem)) return false;
		return equals((ScheduleItem)other);
	}
	private boolean equals(ScheduleItem other) {
		return other.getTime() == time;
	}
	public String toString() {
		return getLabel() + " " + getNum();
	}
	/**
	 * Sets whether the timeFormat methods should be in 24 hour mode.
	 *  The default is true.
	 * 
	 * @param is24 the 24 hour mode
	 */
	public static void set24Hour(boolean is24) {
		ScheduleItem.is24 = is24;
	}
	/**
	 * Formats a time to hh:mm with a possible AM/PM.
	 * 
	 * @param time the calendar with the time
	 * @return the time in the form hh:mm. Whether an AM/PM is appended
	 *  depends on the current mode.
	 */
	public static String timeFormat(Calendar target) {
		return timeFormat(target.getTime());
	}
	/**
	 * Formats a time to hh:mm with a possible AM/PM.
	 * 
	 * @param time the time in milliseconds since epoch
	 * @return the time in the form hh:mm. Whether an AM/PM is appended
	 *  depends on the current mode.
	 */
	public static String timeFormat(long time) {
		return timeFormat(new Date(time));
	}
	/**
	 * Formats a time to hh:mm with a possible AM/PM.
	 * 
	 * @param time the time in milliseconds since epoch
	 * @return the time in the form hh:mm. Whether an AM/PM is appended
	 *  depends on the current mode.
	 */
	public static String timeFormat(Date time) {
		if (is24)
			return hour24.format(time);
		else
			return hour12.format(time);
	}
	/**
	 * Formats a date to "Tuesday, March 2"
	 * 
	 * @param time the calendar with the time
	 * @return the date in the form "dow, month day"
	 */
	public static String dateFormat(Calendar target) {
		return dateFormat(target.getTime());
	}
	/**
	 * Formats a date to "Tuesday, March 2"
	 * 
	 * @param time the time in milliseconds since epoch
	 * @return the date in the form "dow, month day"
	 */
	public static String dateFormat(long time) {
		return dateFormat(new Date(time));
	}
	/**
	 * Formats a date to "Tuesday, March 2"
	 * 
	 * @param time the time in milliseconds since epoch
	 * @return the date in the form "dow, month day"
	 */
	public static String dateFormat(Date time) {
		return date.format(time);
	}
	/**
	 * Gets the match status.
	 * 
	 * @return the status
	 */
	public boolean getStatus() {
		return status;
	}
	/**
	 * Changes the match status.
	 * 
	 * @param status the new status
	 */
	public void setStatus(boolean status) {
		this.status = status;
	}
	/**
	 * Gets the red alliance's score.
	 * 
	 * @return the score of the red alliance
	 */
	public int getRedScore() {
		return oneScore;
	}
	/**
	 * Sets the red alliance's score.
	 * 
	 * @param oneScore the new score for the red alliance
	 */
	public void setRedScore(int oneScore) {
		this.oneScore = oneScore;
	}
	/**
	 * Gets the scores for this match, or null if the match has not
	 *  been scored.
	 * 
	 * @return the scores of this match
	 */
	public List getScores() {
		return scores;
	}
	/**
	 * Sets the scores for this match.
	 * 
	 * @param scores the new list of scores
	 */
	public void setScores(List scores) {
		this.scores = scores;
	}
	/**
	 * Gets the blue alliance's score.
	 * 
	 * @return the score of the blue alliance
	 */
	public int getBlueScore() {
		return twoScore;
	}
	/**
	 * Sets the blue alliance's score.
	 * 
	 * @param twoScore the new score for the blue alliance
	 */
	public void setBlueScore(int twoScore) {
		this.twoScore = twoScore;
	}
	/**
	 * Returns the (non-unique) match number.
	 * 
	 * @return the match number
	 */
	public int getNum() {
		return num;
	}
	/**
	 * Sets the match number. How this is used is up to the implementation.
	 * 
	 * @param num the new number
	 */
	public void setNum(int num) {
		this.num = num;
	}
	/**
	 * Sets the teams in this match.
	 * 
	 * @param teams the new list of teams
	 */
	public void setTeams(List teams) {
		this.teams = teams;
	}
	/**
	 * Gets the label of this match.
	 * 
	 * @return the label. Default is "None".
	 */
	public MatchLabel getLabel() {
		return label;
	}
	/**
	 * Changes the label of this match.
	 * 
	 * @param label the new match label
	 */
	public void setLabel(MatchLabel label) {
		if (label == null) this.label = MatchLabel.blank;
		else this.label = label;
	}
	/**
	 * Gets the counts of this match.
	 *
	 * @return whether the match counts
	 */
	public boolean counts() {
		return counts;
	}
	/**
	 * Changes the counts of this match.
	 *
	 * @param counts whether the match should count
	 */
	public void setCounts(boolean counts) {
		this.counts = counts;
	}
	/**
	 * Gets the teams that are surrogates of this match.
	 *
	 * @return the surrogate teams
	 */
	public BitSet getSurrogate() {
		return surrogate;
	}
}