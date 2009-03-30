package org.s449;

import java.awt.*;
import java.text.*;

/**
 * A class full of Scout449 constants.
 * 
 * @author Stephen Carlson
 * @version 4.0.0
 */
public interface Constants {
	/**
	 * Control port to use.
	 */
	public static final int CONTROL_PORT = 4006;
	/**
	 * The default port for the internal web server.
	 */
	public static final int WEB_PORT = 8080;
	/**
	 * How many minutes worth of matches in the analysis display?
	 */
	public static final int PAST = 10;
	/**
	 * The default team number.
	 */
	public static final int DEFAULT_TEAM = 449;
	/**
	 * The light blue color.
	 */
	public static final Color LIGHT_BLUE_UI = new Color(195, 217, 255);
	/**
	 * The background blue color.
	 */
	public static final Color BACK_BLUE = new Color(136, 181, 255);
	/**
	 * The light gray color.
	 */
	public static final Color LIGHT_GRAY = new Color(212, 212, 212);
	/**
	 * The dark blue color (color of titles in the login window).
	 */
	public static final Color DARK_BLUE = new Color(28, 81, 168);
	/**
	 * A color with alpha only.
	 */
	public static final Color ALPHA_ONLY = new Color(0, 0, 0, 0);
	/**
	 * The purple color used in gradient backgrounds.
	 */
	public static final Color PURPLE = new Color(31, 46, 75);
	/**
	 * The green indicator color.
	 */
	public static final Color GREEN = Color.green;
	/**
	 * The program background color.
	 */
	public static final Color WHITE = Color.white;
	/**
	 * The regular foreground color.
	 */
	public static final Color BLACK = Color.black;
	/**
	 * The color for the #1 alliance.
	 */
	public static final Color RED = Color.red;
	/**
	 * The color for the #2 alliance.
	 */
	public static final Color BLUE = Color.blue;
	/**
	 * A light gray used for some borders.
	 */
	public static final Color GRAY = new Color(144, 144, 144);
	/**
	 * Teams per alliance!
	 */
	public static final int TPA = 3;
	/**
	 * The version of this program.
	 */
	public static final String VERSION = "4.0";
	/**
	 * The full version of this program.
	 */
	public static final String VERSION_FULL = "4.0.1.1";
	/**
	 * The bulk transfer port.
	 */
	public static final int BULK_PORT = 4099;
	/**
	 * The size of the bulk byte buffer.
	 */
	public static final int BULK_BUFFER = 1024;
	/**
	 * Name-value separator. Must not be equal to the end-of-line character.
	 */
	public static final char BULK_DELIM = ',';
	/**
	 * End-of-line character.
	 */
	public static final int BULK_EOL = (int)'\n' % 255;
	/**
	 * The intro window's width.
	 */
	public static final int INTRO_WIDTH = 500;
	/**
	 * The intro window's height.
	 */
	public static final int INTRO_HEIGHT = 350;
	/**
	 * The client window's width.
	 */
	public static final int CLIENT_WIDTH = 1024;
	/**
	 * The client window's height.
	 */
	public static final int CLIENT_HEIGHT = 768;
	/**
	 * The light green color.
	 */
	public static final Color LIGHT_GREEN = new Color(204, 255, 204);
	/**
	 * The light green color.
	 */
	public static final Color LIGHT_RED = new Color(255, 204, 204);
	/**
	 * The light blue color.
	 */
	public static final Color LIGHT_BLUE = new Color(204, 204, 255);
	/**
	 * The wait cursor.
	 */
	public static final Cursor WAIT = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
	/**
	 * Clear thumbnails every?
	 */
	public static final int TTL_SECONDS = 120;
	/**
	 * The date format for most dates.
	 */
	public static final DateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yy");
}