package org.s449;

/*
 * Scout449 - Scouting Data Collection Program
 * Made by: The Blair Robot Project, FRC Team 449
 * 
 *  Scout449 is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  Scout449 is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with Scout449. If not, see <http://www.gnu.org/licenses/>.
 */

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import javax.swing.*;

/**
 * Main class of the Scout449 AUC interface. A repository for icons and scouting
 *  status objects, as well as the command line parameters, main window, and more.
 * 
 * @author Stephen Carlson
 * @version 4.0.0
 */
public class Scout449 {
	/**
	 * The update URL for content.
	 */
	public static final String updateURL = "http://robot.mbhs.edu/scout/update?r=content";
	/**
	 * The update URL for version.
	 */
	public static final String versionURL = "http://robot.mbhs.edu/scout/update?r=version";
	/**
	 * The icon cache.
	 */
	private Map<String, ImageIcon> icons;
	/**
	 * The digit images (separate!)
	 */
	private Image[] digits;
	/**
	 * The command line.
	 */
	private Map<String, String> cmdLine;
	/**
	 * More command line.
	 */
	private String noCmd;
	/**
	 * The introduction.
	 */
	private Intro intro;

	/**
	 * Called from the command line to run Scout449!!!
	 * 
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		new Scout449(args).start();
	}

	/**
	 * Starts up Scout449 with the given command line parameters.
	 * 
	 * @param cmdArgs the command line arguments
	 */
	public Scout449(String[] cmdArgs) {
		// parse command line and save it away
		cmdLine = AppLib.parseCommandLine(cmdArgs);
		noCmd = cmdLine.get("");
		if (noCmd == null) noCmd = "";
		if (cmdLine.containsKey("-v") || noCmd.indexOf("-v") >= 0) {
			System.out.println(Constants.VERSION_FULL);
			System.exit(0);
		}
		// no more headless
		if (AppLib.isHeadless())
			AppLib.fatalError(null, "Must be run in a graphical environment.");
	}
	/**
	 * Checks for updates.
	 */
	private void updateCheck() {
		String theirVersion = null;
		AppLib.printDebug("Checking for updates");
		try {
			URL url = new URL(versionURL);
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			AppLib.sleep(100L);
			if (br.ready()) theirVersion = br.readLine().trim();
			br.close();
		} catch (Exception e) {
			AppLib.debugException(e);
			return;
		}
		AppLib.printDebug("Contacted update server, version is " + theirVersion);
		if (theirVersion != null && VolumeLister.versionCompare(Constants.VERSION_FULL, theirVersion)
			< 0 && AppLib.confirm(intro, "A new version of Scout449 (" + theirVersion + ") is " +
				"available.\nUpdate your copy now?\nClients will automatically update to the " +
				"version on the server.")) {
			VolumeLister.invokeJVM("-jar updater.jar \"" + updateURL + "\"");
			System.exit(0);
		}
	}
	/**
	 * Loads the programs' images.
	 */
	private void loadImages() {
		AppLib.printDebug("Loading images");
		// load images
		loadImage("delete");
		loadImage("edit");
		loadImage("back");
		loadImage("forward");
		loadImage("refresh");
		loadImage("goto");
		loadImage("rotatel");
		loadImage("rotater");
		loadImage("drive");
		loadImage("scout449");
		loadImage("star-lit");
		loadImage("star-unlit");
		loadImage("star-25");
		loadImage("star-50");
		loadImage("star-75");
		loadImage("up");
		loadImage("plus");
		loadImage("down");
		loadImage("minus");
		digits = new Image[10];
		ImageIcon im;
		for (int i = 0; i < 10; i++) {
			im = loadOrDie(i + ".gif");
			digits[i] = im.getImage();
		}
		loadImage("semi", "gif");
		loadImage("expand", "gif");
		loadImage("contract", "gif");
	}
	/**
	 * Loads an image or dies with an image error.
	 * 
	 * @param path the path of the image to load.
	 * @return the loaded image
	 */
	private ImageIcon loadOrDie(String path) {
		ImageIcon icon = AppLib.loadClassImage("images/" + path);
		if (icon == null) err(path);
		while (icon.getImageLoadStatus() == MediaTracker.LOADING)
			AppLib.sleep(5L);
		if (icon.getImage() == null) err(path);
		return icon;
	}
	private void err(String path) {
		AppLib.fatalError(null, "Missing file \"images/" + path + "\".\nPlease check the" +
			"application's JAR file, or upgrade to the latest version of Scout449.");
	}

	/**
	 * The current status.
	 */
	private ScoutStatus status;

	/**
	 * Gets the crucial ScoutStatus object holding all states.
	 * 
	 * @return the status
	 */
	public ScoutStatus getStatus() {
		return status;
	}
	/**
	 * Gets the specified icon's image.
	 * 
	 * @param name the name of the icon to retrieve
	 * @return the icon's image
	 */
	public Image getImage(String name) {
		return getIcon(name).getImage();
	}
	/**
	 * Gets the specified icon.
	 * 
	 * @param name the name of the icon to retrieve
	 * @return the icon
	 */
	public ImageIcon getIcon(String name) {
		if (!icons.containsKey(name))
			loadImage(name);
		return icons.get(name);
	}
	/**
	 * Loads the specified image.
	 * 
	 * @param name the image name
	 */
	private void loadImage(String name) {
		loadImage(name, "png");
	}
	/**
	 * Loads the specified image with the given type extension.
	 * 
	 * @param name the image name
	 * @param ext the extension like "png" or "gif"
	 */
	private void loadImage(String name, String ext) {
		icons.put(name, loadOrDie(name + "." + ext));
	}
	/**
	 * Gets the digit icons (they are separate...)
	 * 
	 * @return the array of digit icons
	 */
	public Image[] getDigitIcons() {
		return digits;
	}
	/**
	 * Gets the command line arguments of Scout449.
	 * 
	 * @return the command line arguments, or null if not run from command line
	 */
	public Map<String, String> getCommandLine() {
		return cmdLine;
	}
	/**
	 * Gets the list of unkeyed command line parameters.
	 * 
	 * @return the unkeyed command line params
	 */
	public String getCommandParams() {
		return noCmd;
	}
	/**
	 * Gets the Scout449 introduction.
	 * 
	 * @return the intro!
	 */
	public Intro getIntro() {
		return intro;
	}
	/**
	 * Initializes the GUI and opens the chooser.
	 */
	public void start() {
		AppLib.printDebug("Initializing");
		icons = new HashMap<String, ImageIcon>(16);
		loadImage("winicon");
		intro = new Intro(this);
		AppLib.printDebug("Intro open");
		NPC3.setApplicationID(3);
		NPC3.setName("client" + Math.round(Math.random() * 1E6));
		// init
		loadImages();
		AppLib.loadSystemUI();
		AppLib.printDebug("Loading authentication window");
		// init window
		Dimension screenSize = AppLib.winInfo.getScreenSize();
		JFrame win = new JFrame("Scout449 " + Constants.VERSION + " AUC");
		win.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		win.addWindowListener(new EventListener());
		win.setResizable(true);
		win.setIconImage(getImage("winicon"));
		// size and center window
		win.setSize(Constants.CLIENT_WIDTH, Constants.CLIENT_HEIGHT);
		if (screenSize.width > Constants.CLIENT_WIDTH &&
				screenSize.height > Constants.CLIENT_HEIGHT)
			win.setLocation((screenSize.width - Constants.CLIENT_WIDTH) / 2,
				(screenSize.height - Constants.CLIENT_HEIGHT) / 2);
		else
			win.setLocation(0, 0);
		status = new ScoutStatus(this, win);
		AppLib.printDebug("Initializing client view");
		status.getHomeScreen().init();
		status.getClient().init();
		// go!
		updateCheck();
		AppLib.printDebug("Done with init");
		win.setVisible(true);
		intro.setVisible(false);
		intro.dispose();
		win.requestFocus();
		status.getHomeScreen().focus();
	}

	/**
	 * A class to listen for window closing events and ask for confirmation.
	 */
	private class EventListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			if (status.isClientShowing())
				status.showClient(false);
			else if (status.isServerRunning() || status.isTransferRunning()) {
				if (AppLib.confirm(status.getWindow(), "This will stop the server and transfer." +
						"\n\nAre you sure that you want to exit?"))
					System.exit(0);
			} else
				System.exit(0);
		}
	}
}