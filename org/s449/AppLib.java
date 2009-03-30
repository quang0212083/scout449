package org.s449;
/*
 * AppLib.java last modified 3/6/08 (rev 6)
 */

import java.awt.Component;
import java.awt.Toolkit;
import java.io.*;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import javax.swing.*;

/**
 * Library of commonly used GUI functions, Java functions and stuff to cut
 *  down on development time of Swing frames, AWT frames, and applets.
 * 
 * @author Stephen Carlson
 * @version 1.0.0
 */
public class AppLib {
	/**
	 * Program input stream (default standard input stream)
	 */
	private static BufferedReader sysIn;
	/**
	 * Program output stream (default standard output stream)
	 */
	private static PrintWriter sysOut = new PrintWriter(
		new OutputStreamWriter(System.out), true);
	/**
	 * Whether the user interface is headless (text-only)
	 */
	private static final boolean headless = false;
	/**
	 * Constant indicating all messages.
	 */
	public static final int ALL = 15;
	/**
	 * Constant indicating no messages.
	 */
	public static final int NONE = 0;
	/**
	 * Constant indicating debug messages.
	 */
	public static final int DEBUG = 1;
	/**
	 * Constant indicating warning messages.
	 */
	public static final int WARNING = 2;
	/**
	 * Constant indicating error messages.
	 */
	public static final int ERROR = 4;
	/**
	 * Constant indicating the notice bit.
	 */
	public static final int NOTICE = 8;
	/**
	 * Current debugging level.
	 */
	private static int debug = ALL & ~DEBUG;
	/**
	 * The default toolkit.
	 */
	public static final Toolkit winInfo = Toolkit.getDefaultToolkit();
	/**
	 * A DateFormat object.
	 */
	public static final DateFormat dateFormat = DateFormat.getInstance();

	/**
	 * This class should not be instantiated directly, and it cannot be usefully
	 *  subclassed.
	 */
	private AppLib() { }

	/**
	 * Checks the status of input on the defined program input stream.
	 * 
	 * @return true if input is waiting on the program input (usually System.in),
	 *  false if end of file or no user input since last read
	 */
	public static boolean inputWaiting() {
		ensureOpen();
		try {
			return sysIn.ready();
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Ensures that the system input stream is open.
	 */
	private static void ensureOpen() {
		if (sysIn == null)
			sysIn = new BufferedReader(new InputStreamReader(System.in));
	}

	/**
	 * Reads a line of text from the program input stream.
	 * 
	 * @return a String with the text entered into the program input or null
	 *  if end of file or other input error
	 */
	public static String readLine() {
		ensureOpen();
		try {
			return sysIn.readLine();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Reads an integer from the program input stream.
	 * 
	 * @return an integer with the text entered into the program input or
	 *  Integer.MIN_VALUE if end of file or other input error
	 */
	public static int readInt() {
		ensureOpen();
		try {
			return Integer.parseInt(sysIn.readLine());
		} catch (Exception e) {
			return Integer.MIN_VALUE;
		}
	}

	/**
	 * Reads a floating-point number from the program input stream.
	 * 
	 * @return a double with the text entered into the program input or
	 *  NaN if end of file or other input error
	 */
	public static double readDouble() {
		ensureOpen();
		try {
			return Double.parseDouble(sysIn.readLine());
		} catch (Exception e) {
			return Double.NaN;
		}
	}

	/**
	 * Reads a time in a variety of forms from the program input stream.
	 *  Values not in the input are set to their current values.
	 * 
	 * @return a Date object with the entered date and time, or null
	 *  if an invalid date and time was entered
	 */
	public static Date readTime() {
		ensureOpen();
		try {
			return dateFormat.parse(sysIn.readLine());
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Gets the status of the user's environment.
	 * 
	 * @return true if the user is in a command-line-only environment;
	 *  false if the environment is graphical
	 */
	public static boolean isHeadless() {
		return headless;
	}

	/**
	 * Prints a debugging message to the program's output stream, if
	 *  the warning level has the DEBUG flag set.
	 * 
	 * @param text information regarding the debug message
	 */
	public static void printDebug(String text) {
		if (getDebug(DEBUG))
			sysOut.println("[DEBUG " + getSystemTime() + "]: " + text);
	}

	/**
	 * Prints a warning message to the program's output stream, if
	 *  the warning level has the WARNING flag set. If in a graphical
	 *  environment, a dialog box will pop up with the warning message.
	 * 
	 * @param base the base component
	 * @param text information regarding the warning message
	 */
	public static void printWarn(Component base, String text) {
		if (getDebug(WARNING)) {
			sysOut.println("[WARN " + getSystemTime() + "]: " + text);
			if (!headless) JOptionPane.showMessageDialog(base, text, "Warning",
					JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Brings up a confirmation message to the user.
	 * 
	 * @param base the base component
	 * @param message the message to display
	 * @return whether the user confirmed the operation
	 */
	public static boolean confirm(Component base, String message) {
		return JOptionPane.showConfirmDialog(base, message, "Confirm",
			JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
	}

	/**
	 * Prints out a raw string to the program's output stream.
	 */
	public static void print(String text) {
		sysOut.println(text);
	}

	/**
	 * Prints an error message to the program's output stream. If in a
	 *  graphical environment, a dialog box will pop up with the error
	 *  message. The JRE will exit with return code 1.
	 * 
	 * @param base the base component
	 * @param text information regarding the error message
	 */
	public static void fatalError(Component base, String text) {
		if (getDebug(ERROR)) {
			sysOut.println("[ERROR " + getSystemTime() + "]: " + text);
			if (!headless) JOptionPane.showMessageDialog(base, text + "\n\nThe " +
					"program will now exit.", "Fatal Error", JOptionPane.ERROR_MESSAGE);
		}
		System.exit(1);
	}

	/**
	 * Gets the current date.
	 * 
	 * @return a Date object holding the current system date and time.
	 */
	public static Date getSystemTime() {
		return new Date();
	}

	/**
	 * Reassigns the debug stream (the output stream) to another stream.
	 * 
	 * @param str an OutputStream object with the new output stream
	 */
	public static void setOutputStream(OutputStream str) {
		try {
			sysOut.flush();
			sysOut.close();
		} catch (Exception e) { }
		sysOut = new PrintWriter(new OutputStreamWriter(str));
	}

	/**
	 * Gets the output stream of the program.
	 * 
	 * @return a PrintWriter that will print to the output stream
	 */
	public static PrintWriter getOutputStream() {
		return sysOut;
	}

	/**
	 * Reassigns the output stream to a file. The file will be opened
	 *  for appending; if it does not exist, it will be created.
	 * 
	 * @param file the file to which messages should be output
	 * @return whether the stream was successfully changed
	 */
	public static boolean setOutputStream(File file) {
		try {
			sysOut = new PrintWriter(new BufferedWriter(
				new FileWriter(file)));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Reassigns the input stream to another stream.
	 * 
	 * @param str an InputStream object with the new input stream
	 */
	public static void setInputStream(InputStream str) {
		try {
			if (sysIn == null) sysIn.close();
		} catch (Exception e) { }
		sysIn = new BufferedReader(new InputStreamReader(str));
	}

	/**
	 * Parses command line arguments into a useful form.
	 * 
	 * @param args the input to the program (the command line)
	 * @return a HashMap object mapping a String with the name of the key
	 *  to a String with the value, or an empty map with no arguments.
	 */
	public static HashMap parseCommandLine(String[] args) {
		HashMap data = new HashMap();
		if (args.length < 1) return data;
		StringBuffer files = new StringBuffer(128);
		for (int i = 0; i < args.length; i++) {
			String arg = args[i].trim();
			if (arg.charAt(0) == '-')
				if (i < args.length - 1 && args[i + 1].charAt(0) != '-')
					data.put(arg, args[++i]);
				else {
					int index = arg.indexOf('='); 
					if (index > 0) {
						String q = arg.substring(index + 1, arg.length());
						if (q.charAt(0) == '"' && q.charAt(q.length() - 1) == '"')
							q = q.substring(1, q.length() - 1);
						data.put(arg.substring(0, index), q);
					} else
						data.put(arg, "");
				}
			else if (arg.length() > 0)
				files.append(arg + " ");
		}
		data.put("", files.toString().trim());
		String d = (String)data.get("--debug");
		if (d != null) {
			if (d.length() > 0)
				try {
					setDebugLevel(Integer.parseInt(d));
				} catch (Exception e) {
					printWarn(null, "Invalid argument " + d +
						" for debug parameter, setting to ALL.");
					setDebugLevel(AppLib.ALL);
				}
			else
				setDebugLevel(AppLib.ALL);
		}
		if (data.containsKey("--quiet")) setDebugLevel(AppLib.NONE);
		d = (String)data.get("--log");
		if (d != null) {
			if (d.length() < 1 || d.equals("-"))
				setOutputStream(System.out);
			else
				setOutputStream(new File(d));
		}
		return data;
	}

	/**
	 * Sets the verbosity (amount of output shown).
	 * 
	 * @param level the new debugging level. Values can be one
	 * of AppLib.WARNING, AppLib.DEBUG, AppLib.ERROR, AppLib.ALL,
	 * AppLib.NONE, or some bitwise combination of these.
	 */
	public static void setDebugLevel(int level) {
		if (level >= NONE && level <= ALL) debug = level;
	}

	/**
	 * Waits for the specified number of milliseconds.
	 * 
	 * @param millis how long to wait
	 * @return true if the method timed out; false if the method was interrupted
	 */
	public static boolean sleep(long millis) {
		try {
			Thread.sleep(millis);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Shows an open file dialog using the specified component
	 *  (usually a frame or applet). Filters files with the specified
	 *   extension.
	 * 
	 * @param base the Component to lock while displaying
	 * @param extension the extension of files to allow
	 * @return the File selected in the dialog box
	 */
	public static File openFile(Component base, String extension) {
		JFileChooser jf = new JFileChooser();
		File ret = null;
		jf.setFileFilter(new ExtensionFileFilter(extension, null));
		jf.setDialogTitle("Open File");
		do {
			jf.showOpenDialog(base);
			ret = jf.getSelectedFile();
		} while (ret != null && !ret.canRead());
		return ret;
	}

	/**
	 * Shows an save file dialog using the specified component
	 *  (usually a frame or applet). Filters files with the specified
	 *   extension.
	 * 
	 * @param base the Component to lock while displaying
	 * @param extension the extension of files to allow
	 * @return the File selected in the dialog box
	 */
	public static File saveFile(Component base, String extension) {
		JFileChooser jf = new JFileChooser();
		File ret = null;
		jf.setFileFilter(new ExtensionFileFilter(extension, null));
		jf.setDialogTitle("Save File");
		do {
			jf.showSaveDialog(base);
			ret = jf.getSelectedFile();
		} while (ret != null && !ret.canRead());
		return ret;
	}

	/**
	 * Gets the current debugging level.
	 * 
	 * @return the current debugging level
	 */
	public static int getDebug() {
		return debug;
	}

	/**
	 * Returns whether the program is in debug mode.
	 * 
	 * @return the debug flag
	 */
	public static boolean isDebugging() {
		return getDebug(DEBUG);
	}

	/**
	 * Prints the throwable's stack trace if the program is in debug mode.
	 * 
	 * @param t the exception
	 */
	public static void debugException(Throwable t) {
		if (isDebugging())
			t.printStackTrace(getOutputStream());
	}

	/**
	 * Gets whether a certain message type is enabled. See setDebugLevel to
	 *  find valid levels.
	 * 
	 * @return whether the current debugging level includes the specified
	 * type of message.
	 * @see setDebugLevel(int)
	 */
	public static boolean getDebug(int level) {
		return (debug & level) > 0;
	}

	/**
	 * Loads an image using the specified path relative to the location
	 *  of the AppLib class file. This works in JAR files as well as all
	 *  file systems. However, the location must be relative to this file
	 *  and <b>not</b> the calling class's file!
	 * 
	 * @param path the relative path to the image such as "images/1.jpg" 
	 * @return an Image object containing the contents of the specified file
	 * or null if an error occurs
	 */
	public static ImageIcon loadClassImage(String path) {
		java.net.URL uri = AppLib.class.getResource("/" + path);
		if (uri == null) return null;
		return new ImageIcon(uri);
	}

	/**
	 * Loads an image using the specified path, relative or absolute.
	 *  Not proven to work in a JAR file.
	 * 
	 * @param path the relative or absolute path to the image such as
	 *  "images/1.jpg"
	 * @return an Image object containing the contents of the specified file
	 * or null if an error occurs
	 */
	public static ImageIcon loadImage(String path) {
		return new ImageIcon(path);
	}

	/**
	 * Loads the system Look and Feel.
	 */
	public static void loadSystemUI() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			printDebug("Loading system UI");
		} catch (Exception e) {
			printDebug("Loading default UI");
		}
	}

	/**
	 * Checks to see whether the string passed in is a valid integer.
	 * 
	 * @param i the String to test
	 * @return true if the String is an integer; false if it is not
	 */
	public static boolean validInteger(String i) {
		try {
			Integer.parseInt(i);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Checks to see whether the string passed in is a valid integer
	 *  and is greater than 0.
	 * 
	 * @param i the String to test
	 * @return true if the String is an integer and greater than zero;
	 *  false if it is not
	 */
	public static boolean positiveInteger(String i) {
		try {
			return Integer.parseInt(i) > 0;
		} catch (Exception e) {
			return false;
		}
	}
}