package org.s449;

import java.util.*;

/**
 * An object that stores information about hotkeys.
 * 
 * @author Stephen Carlson
 * @version 1.0.0
 */
public class HotkeyList implements java.io.Serializable {
	private static final long serialVersionUID = 5321978342178349878L;

	/**
	 * Returns "-x" or "+x" as appropriate.
	 * 
	 * @param num the number to format
	 * @return the formatted number
	 */
	public static String plusMinus(int num) {
		return num > 0 ? ("+" + num) : ("-" + Math.abs(num));
	}

	/**
	 * The list of hotkeys. This is a little bit inefficient...
	 */
	private List<Hotkey> list;

	/**
	 * Creates a hotkey list with nothing but the penalty hotkey.
	 */
	public HotkeyList() {
		list = new ArrayList<Hotkey>(10);
	}
	/**
	 * Checks to see if any user hotkeys (excluding the penalty hotkey) are in the map.
	 * 
	 * @return whether the hotkey list is essentially empty
	 */
	public boolean isEmpty() {
		return list.isEmpty();
	}
	/**
	 * Gets the list of hotkeys.
	 * 
	 * @return the hotkey list
	 */
	public List<Hotkey> getList() {
		return list;
	}
	/**
	 * Gets the point value for a given hotkey character.
	 * 
	 * @param hotkey the hotkey character to fetch
	 * @return the point value, or 0 if no such
	 */
	public int getPointValue(char hotkey) {
		Iterator<Hotkey> it = list.iterator();
		Hotkey item;
		while (it.hasNext()) {
			item = it.next();
			if (hotkey == item.getKeyChar())
				return item.getPointValue();
		}
		return 0;
	}
	/**
	 * Gets the description for a given hotkey character.
	 * 
	 * @param hotkey the hotkey character to fetch
	 * @return the description, or "" if no such
	 */
	public String getDescription(char hotkey) {
		Iterator<Hotkey> it = list.iterator();
		Hotkey item;
		while (it.hasNext()) {
			item = it.next();
			if (hotkey == item.getKeyChar())
				return item.getDescription();
		}
		return "";
	}
	/**
	 * Sets the hotkey list to the given list.
	 * 
	 * @param hotkeys the hotkey list
	 */
	public void setList(List<Hotkey> hotkeys) {
		list.clear();
		list.addAll(hotkeys);
	}
	/**
	 * Gets the index of a given hotkey
	 * 
	 * @param hotkey the hotkey to look up
	 * @return the index
	 */
	public int indexOf(char hotkey) {
		Iterator<Hotkey> it = list.iterator();
		Hotkey item;
		int i = 0;
		while (it.hasNext()) {
			item = it.next();
			if (hotkey == item.getKeyChar())
				return i;
			i++;
		}
		return -1;
	}
	/**
	 * Converts the hotkey list to an array of point values.
	 * 
	 * @return an array of point values
	 */
	public int[] toScoreArray() {
		int[] scores = new int[size()];
		Iterator<Hotkey> it = list.iterator();
		int i = 0;
		while (it.hasNext()) {
			scores[i] = it.next().getPointValue();
			i++;
		}
		return scores;
	}
	/**
	 * Gets the number of hotkeys.
	 * 
	 * @return the number of hotkeys
	 */
	public int size() {
		return list.size();
	}
	public String toString() {
		String output = "";
		Iterator<Hotkey> it = list.iterator();
		while (it.hasNext())
			output += it.next() + "   ";
		return output.trim();
	}

	/**
	 * A class describing a hot key.
	 */
	public static class Hotkey implements java.io.Serializable {
		private static final long serialVersionUID = 5321978342178349879L;

		private String desc;
		private char ch;
		private int num;

		public Hotkey(char ch, String desc, int num) {
			this.ch = ch;
			this.desc = desc;
			this.num = num;
		}
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Hotkey)) return false;
			Hotkey other = (Hotkey)o;
			return other.ch == ch && other.num == num;
		}
		public String toString() {
			return ch + ": " + desc + " (" + plusMinus(num) + ")";
		}
		public char getKeyChar() {
			return ch;
		}
		public String getDescription() {
			return desc;
		}
		public int getPointValue() {
			return num;
		}
	}
}