package org.s449;

import java.util.*;

/**
 * Class holding data representing a team.
 * 
 * @author Stephen Carlson
 * @version 4.0.0
 */
public class Team implements Comparable, java.io.Serializable {
	private static final long serialVersionUID = 5321978342178349871L;

	/**
	 * Rounds a number to 1 decimal place.
	 * 
	 * @param in the number
	 * @return the number rounded to 1 decimal place
	 */
	public static final double round1(double in) {
		return Math.round(in * 10.) / 10.;
	}

	/**
	 * The name of this team.
	 */
	private String name;
	/**
	 * The number of this team.
	 */
	private int number;
	/**
	 * The comments for this team.
	 */
	private Set comments;
	/**
	 * The robot type of this team.
	 */
	private String type;
	/**
	 * The User-Defined Field values for this team.
	 */
	private transient List data;
	/**
	 * The total scores for this team.
	 */
	private transient List scores;
	/**
	 * The FIRST rank of this team.
	 */
	private transient int firstRank;
	/**
	 * The number of wins for this team in this context.
	 */
	private transient int wins;
	/**
	 * The number of losses for this team in this context.
	 */
	private transient int losses;
	/**
	 * The number of ties for this team in this context.
	 */
	private transient int ties;
	/**
	 * The total points for this team in this context.
	 */
	private transient int points;
	/**
	 * The total alliance points for this team in this context.
	 */
	private transient int teamPoints;
	/**
	 * The total alliance points against this team in this context.
	 */
	private transient int enPoints;
	/**
	 * The number of ranking points for this team.
	 */
	private transient int rp;
	/**
	 * The cached rating.
	 */
	private transient double cachedRating;
	/**
	 * Whether the cached stats are valid.
	 */
	private transient boolean statsValid = false;
	/**
	 * The matches for this team.
	 */
	private Map matches;
	/**
	 * The size of the UDFs array.
	 */
	private int numUDFs;

	/**
	 * Creates a team with the specified name and number.
	 * 
	 * @param newName the name of the team
	 * @param newNum the number of the team
	 * @param alloc the number of UDFs to allocate
	 */
	public Team(String newName, int newNum, int alloc) {
		name = newName;
		number = newNum;
		statsValid = false;
		matches = new TreeMap();
		comments = new TreeSet();
		data = null;
		scores = null;
		numUDFs = alloc;
	}
	/**
	 * Gets the comments list for this team.
	 * 
	 * @return the list of comments on this team
	 */
	public Set getComments() {
		return comments;
	}
	/**
	 * Gets the team name.
	 * 
	 * @return the team name. May be "" if unknown or not entered (!)
	 */
	public String getName() {
		return name;
	}
	/**
	 * Gets the team's matches.
	 * 
	 * @return the map of matches for this team
	 */
	public Map getMatches() {
		return matches;
	}
	/**
	 * Changes this team's matches.
	 * 
	 * @param matches the new map of matches
	 */
	public void setMatches(Map matches) {
		this.matches = matches;
		validate();
	}
	/**
	 * Changes the team name.
	 * 
	 * @param name the new team name
	 */
	public void setName(String name) {
		if (name != null) this.name = name;
	}
	/**
	 * Gets the team number.
	 * 
	 * @return the team number
	 */
	public int getNumber() {
		return number;
	}
	// Returns the hash code (the number).
	public int hashCode() {
		return getNumber();
	}
	// Checks for equality with the specified object.
	public boolean equals(Object obj) {
		if (!(obj instanceof Team)) return false;
		else return equals((Team)obj);
	}
	// Checks for equality with the specified team.
	private boolean equals(Team team) {
		return team.getName().equals(getName()) && team.getNumber() == getNumber();
	}
	// Converts to a string.
	public String toString() {
		return getNumber() + " " + getName();
	}
	// Compares first by number and then by name.
	public int compareTo(Object o) {
		if (!(o instanceof Team)) return -1;
		Team other = (Team)o;
		if (other.getNumber() != getNumber()) return getNumber() - other.getNumber();
		return getName().compareTo(other.getName());
	}
	/**
	 * Gets the number of losses.
	 * 
	 * @return the number of losses
	 */
	public int getLosses() {
		v();
		return losses;
	}
	/**
	 * Gets the number of points per game.
	 * 
	 * @return the average number of points per game
	 */
	public double getPPG() {
		return countGames() == 0 ? 0. : round1((double)getPoints() / countGames());
	}
	/**
	 * Gets the number of seeding points accumulated by this team.
	 * 
	 * @return the number of seeding points
	 */
	public int getSP() {
		return 2 * getWins() + getTies();
	}
	/**
	 * Gets the number of ranking points accumulated by this team.
	 * 
	 * @return the number of ranking points
	 */
	public int getRP() {
		v();
		return rp;
	}
	/**
	 * Gets the number of team points per game.
	 * 
	 * @return the average number of team points per game
	 */
	public double getTeamPPG() {
		return countGames() == 0 ? 0. : round1((double)getTeamPoints() / countGames());
	}
	/**
	 * Gets the number of opposing team points per game.
	 * 
	 * @return the average number of enemy team points per game
	 */
	public double getEnPPG() {
		return countGames() == 0 ? 0. : round1((double)getEnPoints() / countGames());
	}
	/**
	 * Gets the winning percentage of the team.
	 * 
	 * @return the winning percentage of this team
	 */
	public double getWinPct() {
		return (countGames() == 0) ? 0 : (Math.round(1000. * (0.5 * getTies() + getWins()) /
			countGames()) / 10.);
	}
	/**
	 * Gets the number of ties.
	 * 
	 * @return the number of ties
	 */
	public int getTies() {
		v();
		return ties;
	}
	/**
	 * Gets the number of wins.
	 * 
	 * @return the number of wins
	 */
	public int getWins() {
		v();
		return wins;
	}
	/**
	 * Gets the number of games played.
	 * 
	 * @return the number of games played.
	 */
	public int countGames() {
		return getWins() + getLosses() + getTies();
	}
	/**
	 * Gets the FIRST rank for this team. Beware, it may not be set!
	 * 
	 * @return the FIRST rank
	 */
	public int getFIRSTRank() {
		return firstRank;
	}
	/**
	 * Changes the FIRST rank for this team.
	 * 
	 * @param firstRank the new FIRST rank
	 */
	public void setFIRSTRank(int firstRank) {
		this.firstRank = firstRank;
	}
	/**
	 * Changes a comment for a team.
	 * 
	 * @param comment the comment to update
	 */
	public void updateComment(Comment comment) {
		comments.remove(comment);
		if (comment.getText() != null) comments.add(comment);
		validate();
	}
	/**
	 * Gets the total number of points scored.
	 * 
	 * @return the total number of points scored
	 */
	public int getPoints() {
		v();
		return points;
	}
	/**
	 * Gets the total number of points scored by alliances.
	 * 
	 * @return the total number of points scored by alliances
	 */
	public int getTeamPoints() {
		v();
		return teamPoints;
	}
	/**
	 * Gets the total number of points scored by opposing alliances.
	 * 
	 * @return the total number of points scored by opposing alliances
	 */
	public int getEnPoints() {
		v();
		return enPoints;
	}
	/**
	 * Gets the general 5-star rating of the team; 0 is
	 *  no rating. This iterates through summary information; if you are
	 *  getting lots of summary information, avoid this method.
	 * 
	 * @return the rating of the team or 0 if the team is not yet rated
	 */
	public double getRating() {
		v();
		return cachedRating;
	}
	/**
	 * Gets the type of the team.
	 * 
	 * @return the type
	 */
	public String getType() {
		if (type == null)
			return "Other";
		return type;
	}
	/**
	 * Changes the type of the team.
	 * 
	 * @param type the new type
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * Gets the data for the UDFs (User Defined Fields).
	 * 
	 * @return the UDF data (this can include detailed ratings!)
	 */
	public List getData() {
		v();
		return data;
	}
	/**
	 * Gets the accumulated scores.
	 * 
	 * @return the scores
	 */
	public List getScores() {
		v();
		return scores;
	}
	private void v() {
		if (!statsValid) validate();
	}
	/**
	 * Rebuilds the team statistics.
	 */
	public synchronized void validate() {
		statsValid = false;
		Iterator it = comments.iterator();
		Comment item;
		// clean up the comments list - remove dead matches
		while (it.hasNext()) {
			item = (Comment)it.next();
			if (item.getMatch() != null && !matches.containsKey(new SecondTime(
					item.getMatch().getTime()))) {
				AppLib.printDebug("Stripping comment from " + getNumber());
				it.remove();
			}
		}
		// init
		it = comments.iterator();
		if (data == null) data = new ArrayList(numUDFs);
		int total = 0, num = 0, udf = 0;
		int[] totalUDFs = new int[numUDFs];
		int[] countUDFs = new int[numUDFs];
		while (it.hasNext()) {
			item = (Comment)it.next();
			// run up rating and UDFs
			if (item.getRating() > 0.) {
				num++;
				total += item.getRating();
			}
			for (int i = 0; i < item.getUDFs().size(); i++) {
				udf = ((Integer)item.getUDFs().get(i)).intValue();
				if (udf != 0) {
					totalUDFs[i] += udf;
					countUDFs[i]++;
				}
			}
		}
		// total and save
		data.clear();
		for (int i = 0; i < countUDFs.length; i++) {
			if (countUDFs[i] == 0)
				data.add(new Integer(0));
			else
				data.add(new Integer((int)Math.round((double)totalUDFs[i] / countUDFs[i])));
		}
		if (num == 0) cachedRating = 0;
		else cachedRating = round1((double)total / num);
		scores = new ArrayList();
		// match stats
		Iterator sit = matches.values().iterator();
		ScheduleItem match; List sc; Score myScore;
		int index, diff; boolean side;
		points = teamPoints = enPoints = wins = ties = losses = rp = 0;
		while (sit.hasNext()) {
			match = (ScheduleItem)sit.next();
			index = match.getTeams().indexOf(new Integer(getNumber()));
			// sp, rp, record
			if (index >= 0 && match.getStatus() == ScheduleItem.COMPLETE
					&& match.counts() && !match.getSurrogate().get(index)) {
				side = index < ScheduleItem.TPA; // true is red, false is blue
				diff = match.getBlueScore() - match.getRedScore();
				if ((!side && diff > 0) || (side && diff < 0)) wins++;
				else if (diff == 0) ties++;
				else losses++;
				sc = match.getScores();
				myScore = null;
				if (sc != null) {
					myScore = (Score)sc.get(index);
					points += myScore.totalScore();
					// accumulate
					if (scores.size() < 1) {
						scores = new ArrayList(myScore.size());
						for (int i = 0; i < myScore.size(); i++) scores.add(new Integer(0));
					}
					// add it up!
					for (int i = 0; i < myScore.size() && i < scores.size(); i++)
						scores.set(i, new Integer(((Integer)scores.get(i)).intValue() +
							myScore.getScoreAt(i)));
				}
				if (side) {
					teamPoints += match.getRedScore();
					enPoints += match.getBlueScore();
				} else {
					teamPoints += match.getBlueScore();
					enPoints += match.getRedScore();
				}
				rp += Math.min(match.getBlueScore(), match.getRedScore());
				if (myScore != null && ((!side && diff > 0) || (side && diff < 0))) {
					// penalty points for RP
					for (int i = 0; i < ScheduleItem.TPA; i++)
						rp += 10 * ((Score)sc.get(i + (side ? ScheduleItem.TPA : 0)))
							.getPenaltyCount();
				}
			}
		}
		statsValid = true;
	}
}