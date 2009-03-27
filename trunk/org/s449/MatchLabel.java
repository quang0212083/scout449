package org.s449;

/**
 * A class representing a label for a match.
 * 
 * @author Stephen Carlson
 * @version 4.0.0
 */
public class MatchLabel implements java.io.Serializable {
	private static final long serialVersionUID = 4321978342178349880L;

	public static final MatchLabel blank = new MatchLabel("Match", true);

	/**
	 * The actual label.
	 */
	private String label;
	/**
	 * Does it count?
	 */
	private boolean counts;

	/**
	 * Creates a new match label.
	 * 
	 * @param label the label
	 * @param counts whether it counts
	 */
	public MatchLabel(String label, boolean counts) {
		this.label = label;
		this.counts = counts;
	}
	/**
	 * Returns whether this match type counts by default
	 * 
	 * @return whether it counts
	 */
	public boolean counts() {
		return counts;
	}
	/**
	 * Gets the label for this match.
	 * 
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}
	public boolean equals(Object o) {
		if (o == null || !(o instanceof MatchLabel)) return false;
		MatchLabel other = (MatchLabel)o;
		return other.counts() == counts && other.getLabel().equalsIgnoreCase(label);
	}
	public String toString() {
		return label;
	}
}