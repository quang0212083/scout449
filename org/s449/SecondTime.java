package org.s449;
/**
 * SecondTime objects are used to key the map of matches. The millisecond
 *  part of the long times is ignored in comparison, hashing, and equals.
 * 
 * @author Stephen Carlson
 * @version 1.0.0
 */
public class SecondTime implements java.io.Serializable, Comparable {
	private static final long serialVersionUID = 4321978342178349870L;

	/**
	 * The actual time.
	 */
	private long time;
	/**
	 * The time without milliseconds.
	 */
	private long t1000;

	/**
	 * Creates a new SecondTime object
	 * 
	 * @param time
	 */
	public SecondTime(long time) {
		this.time = time;
		t1000 = time / 1000;
	}
	public int hashCode() {
		// taken from Long.class
		return (int)(t1000 ^ (t1000 >>> 32L));
	}
	public boolean equals(Object o) {
		// compare only to the nearest second
		if (o == null || !(o instanceof SecondTime)) return false;
		return ((SecondTime)o).t1000 == t1000;
	}
	public int compareTo(Object other) {
		if (!(other instanceof SecondTime)) return 0;
		SecondTime o = (SecondTime)other;
		if (o == null) return -1;
		long diff = t1000 - o.t1000;
		if (diff < 0L) return -1;
		if (diff > 0L) return 1;
		return 0;
	}
	public String toString() {
		return Long.toString(time);
	}
	/**
	 * Gets the time stored in this object to the nearest millisecond.
	 * 
	 * @return the time
	 */
	public long getTime() {
		return time;
	}
	/**
	 * Gets the time stored in this object to the nearest second.
	 * 
	 * @return the time to the nearest second
	 */
	public long getTimeSeconds() {
		return t1000 * 1000L;
	}
}