package org.s449;

import javax.swing.Icon;
import org.s449.ui.AntialiasedJLabel;

/**
 * A class that displays a spinning loading wheel and text.
 * 
 * @author Stephen Carlson
 * @version 1.0
 */
public class Loading extends AntialiasedJLabel {
	private static final long serialVersionUID = 0L;

	/**
	 * The loading icon.
	 */
	private static Icon loading;
	/**
	 * The stopped loading icon.
	 */
	private static Icon notLoading;
	/**
	 * Gets the loading icon.
	 * 
	 * @return the loading icon. Loads it if necessary.
	 */
	public static Icon getLoadingImage() {
		if (loading == null)
			loading = AppLib.loadClassImage("images/loading.gif");
		return loading;
	}
	/**
	 * Gets the not loading icon.
	 * 
	 * @return the not loading icon. Loads it if necessary.
	 */
	public static Icon getNotLoadingImage() {
		if (notLoading == null)
			notLoading = AppLib.loadClassImage("images/nloading.gif");
		return notLoading;
	}

	/**
	 * Creates a new loading icon.
	 */
	public Loading() {
		super("Loading...");
		setIcon(getLoadingImage());
		getNotLoadingImage();
		setHorizontalTextPosition(RIGHT);
		setBackground(Constants.BLACK);
		setForeground(Constants.WHITE);
		setOpaque(false);
		setHorizontalAlignment(LEFT);
		setMaximumSize(getPreferredSize());
	}
	/**
	 * Starts the loading indicator.
	 */
	public void start() {
		setIcon(getLoadingImage());
	}
	/**
	 * Stops the loading indicator.
	 */
	public void stop() {
		setIcon(getNotLoadingImage());
	}
}