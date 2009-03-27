package org.s449;

import java.awt.*;
import javax.swing.*;

/**
 * The superclass of all Scout449 dialogs.
 * 
 * @author Stephen Carlson
 * @version 3.0.0
 */
public abstract class BasicDialog {
	/**
	 * Holds the screen height.
	 */
	protected static int screenHeight;
	/**
	 * Holds the screen width.
	 */
	protected static int screenWidth;
	/**
	 * The dialog's window.
	 */
	protected JDialog window;
	/**
	 * The dialog's parent.
	 */
	protected ScoutStatus status;

	/**
	 * Creates a basic dialog on the given parent.
	 * 
	 * @param status the ScoutStatus responsible for this dialog
	 */
	protected BasicDialog(ScoutStatus status) {
		this.status = status;
		window = new JDialog(status.getWindow(), true);
	}
	/**
	 * Centers the dialog on the screen.
	 */
	public void centerWindow() {
		if (window == null) return;
		if (screenWidth <= 0 || screenHeight <= 0) {
			Dimension d = AppLib.winInfo.getScreenSize();
			screenWidth = d.width;
			screenHeight = d.height;
		}
		int w = window.getWidth();
		int h = window.getHeight();
		window.setBounds((screenWidth - w) / 2, (screenHeight - h) / 2, w, h);
	}
	/**
	 * Changes the visibility of the window.
	 * 
	 * @param visible the visibility flag.
	 */
	public void setVisible(boolean visible) {
		window.setVisible(visible);
	}
	/**
	 * Gets the window of this dialog.
	 * 
	 * @return the dialog's window
	 */
	public Component getWindow() {
		return window;
	}
}