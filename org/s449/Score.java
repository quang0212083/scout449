package org.s449;

import java.util.*;

/**
 * Stores a score for a match. It divides scoring into categories for each
 *  hot key.
 * 
 * @author Stephen Carlson
 * @version 1.0.0
 */
public class Score implements java.io.Serializable, Comparable {
	private static final long serialVersionUID = 5321978342178349876L;

	/**
	 * Copies the given score deeply.
	 * 
	 * @param other the score to copy
	 * @return a deep copy of that score
	 */
	public static Score copyValueOf(Score other) {
		Score nw = new Score(other.multiply);
		nw.scores.clear();
		nw.scores.addAll(other.scores);
		nw.ts = other.ts;
		return nw;
	}

	/**
	 * The list of scores. Note that this is specific to a hot key list.
	 */
	private List scores;
	/**
	 * The cached score total.
	 */
	private int ts;
	/**
	 * The multiplier array of score fields.
	 */
	private int[] multiply;
	/**
	 * The number of penalties.
	 */
	private int pen;

	/**
	 * Creates a list of scores with the specified number of 0s.
	 * 
	 * @param num the number of scores to initialize
	 * @param multiply the multiplier array for the total score
	 */
	public Score(int[] multiply) {
		this.multiply = multiply;
		scores = new ArrayList(multiply.length + 1);
		for (int i = 0; i < multiply.length; i++)
			scores.add(new Integer(0));
		ts = pen = 0;
	}
	/**
	 * Gets the multipliers for the scores.
	 * 
	 * @return the multipliers
	 */
	public int[] getMultipliers() {
		return multiply;
	}
	/**
	 * Checks to see if the score list is empty.
	 * 
	 * @return whether the score list is empty
	 */
	public boolean isEmpty() {
		return scores.isEmpty();
	}
	/**
	 * Gets the score at the specified index.
	 * 
	 * @param index the index of the score
	 * @return the score
	 */
	public int getScoreAt(int index) {
		return ((Integer)scores.get(index)).intValue();
	}
	/**
	 * Gets an iterator over the scores.
	 * 
	 * @return the iterator
	 */
	public Iterator iterator() {
		return Collections.unmodifiableList(scores).iterator();
	}
	/**
	 * Gets the multiplied score at the specified index.
	 * 
	 * @param index the index of the score
	 * @return the score times the multiplier
	 */
	public int getMultipliedScoreAt(int index) {
		return ((Integer)scores.get(index)).intValue() * multiply[index];
	}
	/**
	 * Changes the score at the specified index.
	 * 
	 * @param index the index of the score
	 * @param score the new score
	 */
	public void setScoreAt(int index, int score) {
		ts -= getMultipliedScoreAt(index);
		scores.set(index, new Integer(score));
		ts += score * multiply[index];
	}
	/**
	 * Gets the total score of the score list.
	 * 
	 * @return the total score
	 */
	public int totalScore() {
		return ts;
	}
	/**
	 * Gets the number of penalties.
	 * 
	 * @return the number of penalties
	 */
	public int getPenaltyCount() {
		return pen;
	}
	/**
	 * Changes the number of penalties.
	 * 
	 * @param pen the new number of penalties
	 */
	public void setPenaltyCount(int pen) {
		ts += this.pen * 10;
		this.pen = pen;
		ts -= pen * 10;
	}
	/**
	 * Returns the number of scores allocated in this score.
	 * 
	 * @return the number of scores
	 */
	public int size() {
		return scores.size();
	}
	/**
	 * Changes the total score of the score list. Note that this
	 *  will be overwritten if any score in the list is changed.
	 * 
	 * @param newScore the new total score
	 */
	public void setTotalScore(int newScore) {
		ts = newScore;
	}
	public String toString() {
		return Integer.toString(ts);
	}
	public int compareTo(Object o) {
		if (!(o instanceof Score)) return 0;
		Score other = (Score)o;
		int val = scores.size() - other.scores.size();
		if (val != 0) return val;
		for (int i = 0; i < scores.size(); i++) {
			val = ((Integer)scores.get(i)).intValue() -
				((Integer)other.scores.get(i)).intValue();
			if (val != 0) return val;
		}
		return other.ts - ts;
	}
	public boolean equals(Object other) {
		if (other == null || !(other instanceof Score)) return false;
		return compareTo((Score)other) == 0;
	}
}