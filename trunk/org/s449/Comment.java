package org.s449;

import java.util.*;

/**
 * A class defining a comment: the user source, the regional and match,
 *  and more!
 * 
 * @author Stephen Carlson
 * @version 4.0.0
 */
public class Comment implements java.io.Serializable, Comparable<Comment> {
	private static final long serialVersionUID = 4321978342178349875L;

	/**
	 * The owner of this comment.
	 */
	private UserData owner;
	/**
	 * The match during which this comment was made.
	 */
	private ScheduleItem match;
	/**
	 * The text of this comment.
	 */
	private String text;
	/**
	 * The rating 1-5 that the user assigns. 0 means none.
	 */
	private int rating;
	/**
	 * The UDFs assigned to the team in this comment.
	 */
	private List<Integer> udfs;
	/**
	 * The date and time of last update.
	 */
	private long when;

	/**
	 * Creates a comment with the specified owner, match, event, and text.
	 * 
	 * @param owner the comment owner
	 * @param match the match corresponding to this comment
	 * @param text the text of this comment
	 * @param rating the rating, or 0 if none
	 * @param udfs the UDFs given, or null if none
	 * @param when the date of update
	 */
	public Comment(UserData owner, ScheduleItem match, String text, int rating, List<Integer> udfs,
			long when) {
		this.owner = owner;
		this.match = match;
		this.text = text;
		this.rating = rating;
		this.udfs = udfs;
		this.when = when;
	}
	/**
	 * Gets the owner of this comment.
	 *
	 * @return the owner
	 */
	public UserData getOwner() {
		return owner;
	}
	/**
	 * Gets the date of last modification.
	 * 
	 * @return the date of update
	 */
	public long getWhen() {
		return when;
	}
	/**
	 * Sets the date of last modification.
	 * 
	 * @param when the date of update
	 */
	public void setWhen(long when) {
		this.when = when;
	}
	/**
	 * Gets the match of this comment. null means no match.
	 *
	 * @return the match
	 */
	public ScheduleItem getMatch() {
		return match;
	}
	/**
	 * Gets the text of this comment.
	 *
	 * @return the text
	 */
	public String getText() {
		return text;
	}
	/**
	 * Changes the text of this comment.
	 *
	 * @param text the new text
	 */
	public void setText(String text) {
		this.text = text;
	}
	/**
	 * Gets the rating of this comment.
	 *
	 * @return the rating
	 */
	public int getRating() {
		return rating;
	}
	/**
	 * Changes the rating of this comment.
	 *
	 * @param rating the new rating
	 */
	public void setRating(int rating) {
		this.rating = rating;
	}
	/**
	 * Gets the UDFs of this comment.
	 *
	 * @return the UDFs
	 */
	public List<Integer> getUDFs() {
		return udfs;
	}
	public int hashCode() {
		return text.hashCode();
	}
	public int compareTo(Comment other) {
		if (match != null && other.match == null) return 1;
		if (match == null && other.match != null) return -1;
		if (match != null && other.match != null) {
			int c = match.compareTo(other.match);
			if (c != 0) return c;
		}
		return owner.compareTo(other.getOwner());
	}
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Comment)) return false;
		return compareTo((Comment)o) == 0;
	}
	public String toString() {
		return text;
	}
}