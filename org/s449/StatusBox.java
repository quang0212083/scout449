package org.s449;

import javax.swing.*;

/**
 * A status box that displays text and a status light (red/yellow/green).
 * 
 * @author Stephen Carlson
 */
public class StatusBox extends JLabel {
	private static final long serialVersionUID = 0L;
	/**
	 * The red light icon (icons borrowed from a Mac).
	 */
	private static Icon redIcon;
	/**
	 * The green light icon.
	 */
	private static Icon greenIcon;
	/**
	 * The yellow light icon.
	 */
	private static Icon yellowIcon;

	/**
	 * The constant for setStatus that means good (green).
	 */
	public static final int STATUS_GOOD = 2;
	/**
	 * The constant for setStatus that means so-so (yellow).
	 */
	public static final int STATUS_SOSO = 1;
	/**
	 * The constant for setStatus that means bad (red).
	 */
	public static final int STATUS_BAD = 0;

	private int status;

	/**
	 * Creates a status box with no message and a red light.
	 */
	public StatusBox() {
		this("", STATUS_BAD);
	}
	/**
	 * Creates a status box with the specified message and a red light.
	 * 
	 * @param message the initial message
	 */
	public StatusBox(String message) {
		this(message, STATUS_BAD);
	}
	/**
	 * Creates a status box with the specified message and light color.
	 * 
	 * @param message the initial message
	 * @param status the initial light color
	 * @throws IllegalArgumentException if the status light color is
	 *  not a status constant defined by this class
	 */
	public StatusBox(String message, int status) {
		super(message);
		if (redIcon == null) setupIcons();
		setStatus(status);
	}
	/**
	 * Initializes the icons.
	 */
	private void setupIcons() {
		redIcon = new ImageIcon(getClass().getResource("/images/bad.png"));
		greenIcon = new ImageIcon(getClass().getResource("/images/good.png"));
		yellowIcon = new ImageIcon(getClass().getResource("/images/idle.png"));
	}
	/**
	 * Gets the current status.
	 * 
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}
	/**
	 * Changes the current status.
	 * 
	 * @param stat the new status
	 * @throws IllegalArgumentException if the status is not a
	 *  status constant defined by this class
	 */
	public void setStatus(int stat) {
		switch (stat) {
		case STATUS_BAD:
			setIcon(redIcon);
			break;
		case STATUS_SOSO:
			setIcon(yellowIcon);
			break;
		case STATUS_GOOD:
			setIcon(greenIcon);
			break;
		default:
			throw new IllegalArgumentException("status " + stat + " not defined");
		}
		status = stat;
	}
}