package org.s449;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JComponent;

/**
 * A class that represents a large digital clock (count-up or count-down).
 */
public class CountdownClock extends JComponent {
	private static final long serialVersionUID = 0L;

	/**
	 * Hours field 0-99.
	 */
	private int hours;
	/**
	 * Minutes field 0-59.
	 */
	private int minutes;
	/**
	 * Seconds field 0-59.
	 */
	private int seconds;
	/**
	 * Digit width in pixels.
	 */
	private int digiWidth;
	/**
	 * Digit height in pixels - equals 3/2 * digiWidth.
	 */
	private int digiHeight;
	/**
	 * The digit images (loaded from the parent).
	 */
	private Image[] digits;
	/**
	 * The colon images (loaded from the parent).
	 */
	private Image colon;

	/**
	 * Creates a ClockComponent with a default digit width of 32 (and
	 *  a digit height of 48).
	 */
	public CountdownClock(ScoutStatus status) {
		this(status, 32);
	}
	/**
	 * Creates a ClockComponent with a specified digit width. The digit height
	 *  will be 3/2 * the digit width to maintain aspect ratio. The total
	 *  width will be 8 * the digit width.
	 * 
	 * @param width the width of a digit in pixels. Should not be less than 20.
	 */
	public CountdownClock(ScoutStatus status, int width) {
		// compute height and width
		digiHeight = width * 3 / 2;
		Dimension d = new Dimension(width * 8, digiHeight);
		digiWidth = width;
		hours = 0;
		minutes = 0;
		seconds = 0;
		// lock in size
		setMinimumSize(d);
		setMaximumSize(d);
		setPreferredSize(d);
		digits = status.getMaster().getDigitIcons();
		colon = status.getImage("semi");
	}
	public void paint(Graphics g) {
		paintImageAt(g, digits[(hours / 10) % 10], 0);
		paintImageAt(g, digits[hours % 10], 1);
		paintImageAt(g, colon, 2);
		paintImageAt(g, digits[(minutes / 10) % 10], 3);
		paintImageAt(g, digits[minutes % 10], 4);
		paintImageAt(g, colon, 5);
		paintImageAt(g, digits[(seconds / 10) % 10], 6);
		paintImageAt(g, digits[seconds % 10], 7);
	}
	/**
	 * Draws the specified image with the correct size at the specified
	 *  column on the given Graphics object.
	 * 
	 * @param g the Graphics on which to paint
	 * @param im the source Image to paint
	 * @param col the column
	 */
	private void paintImageAt(Graphics g, Image im, int col) {
		g.drawImage(im, col * digiWidth, 0, digiWidth, digiHeight, null);
	}
	/**
	 * Returns the value of the hours field.
	 * 
	 * @return the hours field
	 */
	public int getHours() {
		return hours;
	}
	/**
	 * Changes the value of the hours field.
	 * 
	 * @param hours the new value of the hours field between 0 and 99
	 */
	public void setHours(int hours) {
		this.hours = hours % 100;
	}
	/**
	 * Returns the value of the minutes field.
	 * 
	 * @return the minutes field
	 */
	public int getMinutes() {
		return minutes;
	}
	/**
	 * Changes the value of the minutes field.
	 * 
	 * @param minutes the new value of the minutes field between 0 and 59
	 */
	public void setMinutes(int minutes) {
		this.minutes = minutes % 60;
	}
	/**
	 * Returns the value of the seconds field.
	 * 
	 * @return the seconds field
	 */
	public int getSeconds() {
		return seconds;
	}
	/**
	 * Changes the value of the seconds field.
	 * 
	 * @param seconds the new value of the seconds field between 0 and 99
	 */
	public void setSeconds(int seconds) {
		this.seconds = seconds % 60;
	}
}