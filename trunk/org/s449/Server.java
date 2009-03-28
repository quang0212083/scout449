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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import javax.imageio.*;
import javax.swing.*;

import org.s449.Client.JpegFilter;
import org.s449.ui.*;

/**
 * The server to be run as a backend to multiple connected
 *  scouting programs. Runs on scoutv4.dat!
 * 
 * @author Stephen Carlson
 * @version 4.0.0
 */
public class Server implements ServerPage, Runnable {
	/**
	 * The directory of all images.
	 */
	private static final File allImages = new File("images");
	/**
	 * The directory of team images.
	 */
	private static final File teams = new File(allImages, "teams");
	/**
	 * The directory of team images.
	 */
	private static final File thumbs = new File(allImages, "thumbnails");
	/**
	 * The choices for FLASH.
	 */
	private static final String[] choices = new String[] { "Sync To Flash",
		"Sync From Flash", "Cancel" };
	/**
	 * The size of thumbnails.
	 */
	private static final Dimension thumbSize = new Dimension(160, 120);
	/**
	 * Default file name for scouting data if not chosen at
	 *  command line.
	 */
	public static final String defaultScoutName = "scoutv4.dat";
	/**
	 * Default file name for configuration if not chosen at
	 *  command line.
	 */
	public static final String defaultConfigName = "users.dat";
	/**
	 * The web pattern to match.
	 */
	private static final Pattern toMatch = Pattern.compile("[^.]*?(\\?.*)?");
	/**
	 * The SID find-and-replace pattern to match.
	 */
	private static final Pattern match_sid = Pattern.compile("\\%SID\\%");
	/**
	 * The web session timeout in ms.
	 */
	private static final int STIMEOUT = 600000;
	/**
	 * The filename filter for JPEG files.
	 */
	private JpegFilter jpegs;
	/**
	 * Backend for data.
	 */
	private Backend data;
	/**
	 * Configuration file.
	 */
	private UserFile config;
	/**
	 * List of clients connected.
	 */
	private ArrayList<ScoutConnection> outputs;
	/**
	 * The default control port.
	 */
	private int port = Constants.CONTROL_PORT;
	/**
	 * The web server's port.
	 */
	private int wPort = Constants.WEB_PORT;
	/**
	 * The time when the server was started.
	 */
	private long startDate;
	/**
	 * Server running?
	 */
	private boolean running;
	/**
	 * The GUI window if there is a GUI.
	 */
	private JFrame window;
	/**
	 * The server socket.
	 */
	private ServerSocket ss;
	/**
	 * The configuration extras to be sent to client.
	 */
	private Object[] configuration;
	/**
	 * The web reciever.
	 */
	private WebReciever recv;
	/**
	 * The scout status.
	 */
	private ScoutStatus status;
	/**
	 * The scouting data file name.
	 */
	private String scoutName;
	/**
	 * The configuration file name.
	 */
	private String configName;
	/**
	 * The event listener.
	 */
	private EventListener events;
	/**
	 * The visible server status box.
	 */
	private StatusBox sstat;
	/**
	 * The daemon web server.
	 */
	private WebServer daemon;
	/**
	 * The FLASH thread.
	 */
	private FLASHThread flash;
	/**
	 * The web server session map.
	 */
	private Map<String, Session> sid;
	/**
	 * The time of the last update.
	 */
	private volatile long lastSend;
	/**
	 * Whether an update needs to be sent soon.
	 */
	private volatile boolean sendEventually;

	/**
	 * Helper for web methods to escape "'" to "\'".
	 * 
	 * @param in the input string
	 * @param out the output string
	 */
	public static String escapeQuotes(String in) {
		StringBuffer nt = new StringBuffer(in.length() + 32);
		char c;
		for (int i = 0; i < in.length(); i++) {
			c = in.charAt(i);
			switch (c) {
			case '\'':
				nt.append("\\\'");
				break;
			default:
				nt.append(c);
			}
		}
		return nt.toString();
	}
	/**
	 * Helper for web methods to escape &, <, and > characters.
	 * 
	 * @param toEscape the string to escape
	 * @return the answer
	 */
	public static String htmlspecial(String toEscape) {
		StringBuffer nt = new StringBuffer(toEscape.length() + 32);
		char c;
		for (int i = 0; i < toEscape.length(); i++) {
			c = toEscape.charAt(i);
			switch (c) {
			case '&':
				nt.append("&amp;");
				break;
			case '<':
				nt.append("&lt;");
				break;
			case '>':
				nt.append("&gt;");
				break;
			default:
				nt.append(c);
			}
		}
		return nt.toString();
	}

	/**
	 * Creates a new server.
	 * 
	 * @param stat the ScoutStatus object responsible for this server.
	 */
	public Server(ScoutStatus stat) {
		AppLib.printDebug("Initializing server");
		jpegs = new JpegFilter();
		status = stat;
		events = new EventListener();
		status.addActionListener(events);
		Map<String, String> input = stat.getMaster().getCommandLine();
		scoutName = defaultScoutName;
		configName = defaultConfigName;
		// check for scout data file change
		String param = input.get("-f");
		if (param != null && (param = param.trim()).length() > 0)
			scoutName = param;
		// check for config file change
		param = input.get("-c");
		if (param != null && (param = param.trim()).length() > 0)
			configName = param;
		// check for web port change
		param = input.get("-w");
		if (AppLib.positiveInteger(param))
			wPort = Integer.parseInt(param) % 60000;
		// compute local IP
		String localHost;
		try {
			localHost = InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {
			localHost = "127.0.0.1";
		}
		initGUI(localHost);
		configure();
	}
	/**
	 * Gets a list of events.
	 * 
	 * @return a list of available events
	 */
	public Event[] eventList() {
		if (data == null) return null;
		List<Event> list = data.getData().getEvents();
		Event[] out = new Event[list.size()];
		int i = 0;
		Iterator<Event> it = list.iterator();
		while (it.hasNext())
			out[i++] = it.next();
		return out;
	}
	/**
	 * Run to start a server.
	 */
	public void start() {
		if (running) return;
		AppLib.printDebug("Starting server");
		sstat.setStatus(StatusBox.STATUS_SOSO);
		sstat.setText("Server Starting");
		sid = new HashMap<String, Session>(64);
		lastSend = 0L;
		sendEventually = false;
		if (data == null) {
			// fatal with a better message
			AppLib.printWarn(status.getWindow(), "It seems like the server has not been configured" +
				" properly.\nPlease configure the program if you plan to use the server.");
			status.setServerStatus(false);
			sstat.setText("No Config");
			return;
		}
		try {
			data.selfCheck();
		} catch (RuntimeException e) {
			AppLib.debugException(e);
			AppLib.printWarn(status.getWindow(), "Data failed to pass self-check. Suggestions:\n" +
				"1. Check the server configuration.\n2. Re-configure the server.");
			status.setServerStatus(false);
			sstat.setText("Failed to Start");
			return;
		}
		// check times
		Object[] o = data.getData().getExtraData();
		if (o.length > 0 && o[0] instanceof Long) {
			// knock off 6 months from current time. if that's still after the
			//  last configuration date, it's last year's!
			Calendar one = Calendar.getInstance();
			Calendar two = Calendar.getInstance();
			two.setTimeInMillis(((Long)o[0]).longValue());
			one.add(Calendar.MONTH, -6);
			if (one.after(two) &&
				!AppLib.confirm(status.getWindow(), "It seems like the scouting data is over " +
				" six months old.\nYou probably should re-configure the program.\n" +
				"Continue starting server anyway?")) {
				status.setServerStatus(false);
				sstat.setText("Config Old");
				return;
			}
		}
		if (o.length > 2 && o[2] instanceof Integer) {
			// load am/pm
			int hrs = ((Integer)o[2]).intValue();
			if (hrs == 12)
				ScheduleItem.is24 = false;
			else if (hrs == 24)
				ScheduleItem.is24 = true;
		}
		// configure
		configuration = new Object[2];
		configuration[0] = System.currentTimeMillis();
		configuration[1] = wPort;
		// initialize reciever
		recv = new WebReciever(config.getData(), data, configuration);
		NPC3.setName(config.getData().getName());
		NPC3.broadcast();
		// calibrate date
		startDate = System.currentTimeMillis();
		// start listener
		Thread listener = new Thread(this);
		listener.setDaemon(true);
		listener.setName("Connection Listener");
		listener.setPriority(Thread.MIN_PRIORITY);
		listener.start();
		outputs = new ArrayList<ScoutConnection>(8);
		//myTeam = data.getData().getMyTeam();
		// register web server
		daemon = new WebServer();
		daemon.setPort(wPort);
		daemon.register(this);
		daemon.registerAsErrorHandler(this);
		daemon.start();
		// FLASH
		flash = new FLASHThread();
		flash.start();
		// go, go, go!!!
		new MainThread().start();
		sstat.setStatus(StatusBox.STATUS_GOOD);
		sstat.setText("Server Running");
		running = true;
		AppLib.printDebug("Server running");
	}
	/**
	 * Configures the server.
	 */
	public void configure() {
		if (running) return;
		AppLib.printDebug("Opening file " + configName);
		// load config data
		try {
			config = new UserFile(configName);
		} catch (Exception e) {
			AppLib.debugException(e);
			data = null;
			config = null;
		}
		AppLib.printDebug("Opening file " + scoutName);
		if (config != null) {
			// load initial data
			try {
				data = new FileBackend(scoutName);
			} catch (Exception e) {
				AppLib.debugException(e);
				data = null;
			}
		}
	}
	/**
	 * Configures only the users. Can happen while the server is running.
	 */
	public void configUsers() {
		AppLib.printDebug("Opening file " + configName);
		// load config data
		try {
			config = new UserFile(configName);
			if (running && recv != null) recv.setUsers(config.getData());
		} catch (Exception e) {
			AppLib.debugException(e);
		}
	}
	/**
	 * Gets the closest event.
	 * 
	 * @return the closest event, or null if there are no events
	 */
	public Event closestEvent() {
		if (data == null) return null;
		int i = 0;
		long minDelta = Long.MAX_VALUE, delta;
		long date = System.currentTimeMillis();
		Iterator<Event> it = data.getData().getEvents().iterator();
		Event e, event = null;
		while (it.hasNext()) {
			e = it.next();
			// obvious!
			if (e.duringRegional(date))
				return e;
			// compute dt and check
			delta = Math.abs(e.getStartDate() - date);
			if (Math.abs(e.getEndDate() - date) < delta)
				delta = Math.abs(e.getEndDate() - date);
			if (minDelta > delta) {
				minDelta = delta;
				event = e;
			}
			i++;
		}
		return event;
	}
	/**
	 * Sets the active event code.
	 */
	public void setEvent(Event e) {
		if (e != null) data.setActive(e);
	}
	/**
	 * Stops the server if it is running.
	 */
	public void stop() {
		if (!running) return;
		AppLib.printDebug("Stopping server");
		sstat.setStatus(StatusBox.STATUS_SOSO);
		sstat.setText("Stopping Server");
		NPC3.stopBroadcast();
		try {
			// ensure closure
			ss.close();
		} catch (Exception e) { }
		// close sockets
		synchronized (outputs) {
			Iterator<ScoutConnection> it = outputs.iterator();
			while (it.hasNext()) {
				it.next().close();
				it.remove();
			}
		}
		if (flash != null) flash.stopThread();
		if (daemon != null) daemon.stop();
		sstat.setStatus(StatusBox.STATUS_BAD);
		sstat.setText("Server Stopped");
		AppLib.printDebug("Server stopped");
		running = false;
	}
	/**
	 * Runs the main loop.
	 */
	private void loop() {
		long newDate, oldDate, delta, lastDelta = 0;
		AppLib.printDebug("Main loop start");
		oldDate = System.currentTimeMillis();
		while (data != null) {
			// start daemon to work with times and clean-up
			newDate = System.currentTimeMillis();
			if (newDate / 1000 != oldDate / 1000) {
				// load new data and correct
				configuration[0] = newDate;
				// compute up-time
				oldDate = newDate;
				delta = (newDate - startDate) / 1000;
				synchronized (this) {
					// fire update if required 
					if (sendEventually) {
						sendEventually = false;
						fireUpdate();
					}
				}
				if (delta % 15 == 0 && delta > 0 && lastDelta != delta) {
					if (delta % 60 == 0) {
						System.runFinalization();
						System.gc();
						// clean up clean up
						synchronized (sid) {
							Iterator<String> it = sid.keySet().iterator();
							String s; Session sess;
							// look for expired sessions
							while (it.hasNext()) {
								s = it.next();
								sess = sid.get(s);
								if (sess.lastActivity + STIMEOUT < newDate) {
									// destroy
									AppLib.printDebug("Destroying session for " +
										sess.user.getName());
									it.remove();
								}
							}
						}
					}
					lastDelta = delta;
				}
			}
			// read inputs and sleep to conserve CPU
			readInputs();
			AppLib.sleep(50L);
		}
	}
	/**
	 * Creates the GUI.
	 * 
	 * @param ipAddress the server's current IP
	 */
	private void initGUI(String ipAddress) {
		// init variables for window and screen
		EventListener el = new EventListener();
		AppLib.printDebug("Loading server GUI");
		Dimension screen = AppLib.winInfo.getScreenSize();
		window = new JFrame("Server Status");
		window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		window.addWindowListener(el);
		window.setIconImage(status.getImage("winicon"));
		window.setResizable(false);
		Container c = window.getContentPane();
		// lay out from top to bottom
		c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
		JPanel box = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
		// status
		sstat = new StatusBox("Server Stopped", StatusBox.STATUS_BAD);
		box.add(sstat);
		c.add(box);
		box = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
		// ip address
		JLabel ipAddr = new JLabel("IP Address: " + ipAddress);
		box.add(ipAddr);
		c.add(box);
		// version label
		box = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
		box.add(new JLabel("Server Version: " + Constants.VERSION));
		c.add(box);
		// return to main
		box = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
		box.add(ButtonFactory.getButton("Main Window", "show", el, KeyEvent.VK_S));
		// pack up
		c.add(box);
		window.pack();
		window.setLocation(screen.width - window.getWidth() - 1,
			screen.height - window.getHeight() - 30);
	}
	/**
	 * Reads the input streams for data.
	 */
	private void readInputs() {
		boolean changed = false;
		synchronized (outputs) {
			Iterator<ScoutConnection> it = outputs.iterator();
			ScoutConnection input;
			//recv.setConfiguration(configuration);
			while (it.hasNext()) {
				input = it.next();
				if ((!input.isAuth() && System.currentTimeMillis() > input.getTime() + 5000L) ||
						input.isDead()) {
					// dead client, time to close
					AppLib.printDebug("Closing " + input.getIP());
					input.close();
					it.remove();
				} else if (input.ready()) try {
					// process request
					if (recv.processRequest((RequestObject)input.read(), input))
						changed = true;
				} catch (IOException e) {
					// this could be a client failure
					AppLib.debugException(e);
					input.close();
					it.remove();
				}
			}
		}
		if (changed) fireUpdate();
	}
	/**
	 * Fires a change event sent to all control port listeners.
	 *  This means that the hash was changed by this program or
	 *  another program.
	 */
	private synchronized void fireUpdate() {
		if (System.currentTimeMillis() - lastSend < 1000L) {
			sendEventually = true;
			return;
		}
		sendEventually = false;
		AppLib.printDebug("Save and send data...");
		synchronized (outputs) {
			// notify this listener
			Iterator<ScoutConnection> it = outputs.iterator();
			ScoutConnection conn;
			Socket sock;
			while (it.hasNext()) {
				conn = it.next();
				sock = conn.getSocket();
				if (sock == null || sock.isClosed())
					// died
					it.remove();
				else if (conn.isAuth())
					try {
						recv.update(conn);
					} catch (RuntimeException e) {
						// disconnected
						conn.close();
						it.remove();
					}
			}
		}
		lastSend = System.currentTimeMillis();
	}
	public void run() {
		accept();
	}
	/**
	 * Accepts incoming connections on the port.
	 */
	private void accept() {
		try {
			// listen on port
			ss = new ServerSocket(port);
			new ImageDaemon();
			ScoutConnection client;
			while (true) {
				client = new ScoutConnection(ss.accept());
				client.setAuth(null);
				// start up connection
				AppLib.printDebug("Opening " + client.getIP());
				synchronized (outputs) {
					outputs.add(client);
				}
			}
		} catch (Exception e) {
			// port taken!
			if (e.getMessage().indexOf("already in use") > 0) {
				AppLib.printWarn(status.getWindow(), "The Scout449 server port, " + port +
					", is already in use.\nPlease stop or close other servers and the transfer.");
				status.setServerStatus(false);
				stop();
			}
		}
	}

	/**
	 * Class to handle window closings (swap hide). Also handes action commands.
	 */
	private class EventListener extends WindowAdapter implements ActionListener {
		public void windowClosing(WindowEvent e) {
			window.setVisible(false);
			status.getWindow().setVisible(true);
		}
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd == null) return;
			else if (cmd.equals("exit"));
			else if (cmd.equals("s_server")) {
				boolean toRun = status.isServerRunning();
				if (toRun && !running) start();
				else if (!toRun && running) stop();
			} else if (cmd.equals("show")) {
				window.setVisible(false);
				status.getWindow().setVisible(true);
			} else if (cmd.equals("s_hide")) {
				window.setVisible(true);
				status.getWindow().setVisible(false);
			}
		}
	}

	/**
	 * Class to manage information with server connections.
	 */
	protected static class ScoutConnection extends Connection {
		/**
		 * Auth information.
		 */
		private UserData auth;
		/**
		 * When was the connection made?
		 */
		private long time;

		/**
		 * Creates a new server connection.
		 * 
		 * @param socket the source
		 */
		public ScoutConnection(Socket socket) {
			super(socket);
			auth = null;
			time = System.currentTimeMillis();
		}
		/**
		 * Gets the authentication flag.
		 * 
		 * @return whether the client is authenticated
		 */
		public boolean isAuth() {
			return auth != null;
		}
		/**
		 * Gets the authentication information.
		 * 
		 * @return the client's authentication information
		 */
		public UserData getAuth() {
			return auth;
		}
		/**
		 * Sets the authentication flag.
		 * 
		 * @param auth whether the client should be authenticated
		 */
		public void setAuth(UserData auth) {
			this.auth = auth;
		}
		/**
		 * Gets the time of connection creation.
		 * 
		 * @return the time in milliseconds since epoch
		 */
		public long getTime() {
			return time;
		}
	}

	/**
	 * Class to run the loop method in the background.
	 */
	private class MainThread extends Thread {
		public MainThread() {
			setName("Main Loop");
		}
		public void run() {
			loop();
		}
	}

	/**
	 * Class to run the loop method in the background.
	 */
	private class FLASHThread extends Thread {
		/**
		 * The current volume list.
		 */
		private File[] volumes;
		/**
		 * The volumes that have already been synced or ignored (already seen)
		 */
		private List<File> seen;
		/**
		 * The copy buffer.
		 */
		private byte[] buffer;

		/**
		 * Creates a FLASH thread on the current drive list. Therefore, do not
		 *  start a server with a FLASH drive inserted, or it will not recognize it.
		 */
		public FLASHThread() {
			setName("Scout449 FLASH");
			setDaemon(true);
			setPriority(Thread.MIN_PRIORITY + 1);
			VolumeLister.flushCache();
			volumes = VolumeLister.listVolumes();
			seen = new ArrayList<File>(10);
			buffer = new byte[16384];
		}
		public void run() {
			while (volumes != null) {
				synchronized (volumes) {
					// check for changes
					File[] newRoots = VolumeLister.listVolumes();
					if (newRoots.length != volumes.length) {
						volumes = newRoots;
						checkFlash();
						AppLib.sleep(2000L);
						checkFlash();
					} else for (int i = 0; i < volumes.length; i++)
						if (!volumes[i].equals(newRoots[i])) {
							volumes = newRoots;
							checkFlash();
							AppLib.sleep(2000L);
							checkFlash();
							break;
						}
				}
				AppLib.sleep(1000L);
			}
		}
		/**
		 * Stops the FLASH thread.
		 */
		public void stopThread() {
			synchronized (volumes) {
				volumes = null;
			}
		}
		/**
		 * Lists ALL of the JPEG files found in the given directory.
		 * 
		 * @param base the directory to search
		 * @return a list of JPEG files
		 */
		private File[] listAll(File base) {
			LinkedList<File> files = new LinkedList<File>();
			listAll0(base, files);
			File[] ret = new File[files.size()];
			// copy to array
			Iterator<File> it = files.iterator();
			int i = 0;
			while (it.hasNext())
				ret[i++] = it.next();
			return ret;
		}
		/**
		 * Adds all of the JPEG files in the given directory into the given list.
		 * 
		 * @param base the directory to search
		 * @param files the list to which to append
		 */
		private void listAll0(File base, java.util.List<File> files) {
			File[] list = base.listFiles();
			if (list == null) return;
			File file;
			for (int i = 0; i < list.length; i++) {
				file = list[i];
				// image
				if (jpegs.accept(file)) files.add(file);
				// climb tree
				else if (file.isDirectory() && file.canRead() && !file.isHidden())
					listAll0(file, files);
			}
		}
		/**
		 * Builds a random-access map for files.
		 * 
		 * @param in the list of files
		 * @return a faster way to get at files by name
		 */
		private Map<String, File> buildMap(File[] in) {
			Map<String, File> ret = new HashMap<String, File>();
			for (int i = 0; i < in.length; i++)
				ret.put(in[i].getName().toLowerCase(), in[i]);
			return ret;
		}
		/**
		 * Copies file one to two. Specially optimized for huge files.
		 * 
		 * @param one file one
		 * @param two file two
		 */
		private synchronized void copy(File one, File two) {
			try {
				AppLib.printDebug("Copying " + one + " to " + two);
				FileInputStream is = new FileInputStream(one);
				FileOutputStream os = new FileOutputStream(two);
				int i;
				while (true) {
					// read from the input...
					i = is.read(buffer, 0, buffer.length);
					if (i < 0) break;
					// and write it right back to the output
					os.write(buffer, 0, i);
					os.flush();
				}
				is.close();
				os.close();
			} catch (IOException e) {
				AppLib.debugException(e);
				AppLib.printWarn(status.getWindow(), "Could not copy \"" + one.getAbsolutePath()
					+ "\" to \"" + two.getAbsolutePath() + "\".");
			}
		}
		/**
		 * Checks the FLASH volume and does the actual syncing.
		 */
		private void checkFlash() {
			AppLib.printDebug("Re-checking FLASH volumes");
			File s449, dataFile, imageFile; int i; boolean kioskOpen;
			Iterator<File> it2 = seen.iterator();
			// clean off old drives
			while (it2.hasNext()) {
				dataFile = it2.next();
				for (i = 0; i < volumes.length; i++)
					if (volumes[i].equals(dataFile)) break;
				if (i >= volumes.length) it2.remove();
			}
			for (i = 0; i < volumes.length; i++) {
				// look for new ones
				s449 = new File(volumes[i], ".scout449");
				dataFile = new File(s449, "scoutv4.dat");
				imageFile = new File(s449, "images");
				if (s449.canRead() && s449.isDirectory()) {
					// here we go
					AppLib.printDebug("Detected " + s449);
					kioskOpen = status.getClient().isKioskShowing();
					if (kioskOpen) status.getClient().showKiosk(false);
					int choice = JOptionPane.showOptionDialog(status.getWindow(),
						"Scout449 FLASH drive detected.\nSynchronize?", "Scout449 FLASH",
						JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
						choices, "Cancel");
					File[] images; Map<String, File> match; String name; File dest;
					switch (choice) {
					case 0:
						// to
						if (!imageFile.exists()) imageFile.mkdir();
						AppLib.printDebug("Sync Data");
						try {
							ObjectOutputStream oos = new ObjectOutputStream(
								new FileOutputStream(dataFile));
							oos.writeObject(data.getData());
							oos.close();
						} catch (Exception e) {
							AppLib.debugException(e);
							AppLib.printWarn(status.getWindow(), "Couldn't flash data to removable" +
								" disk.\nPlease ensure that the disk is not write-protected.");
							break;
						}
						AppLib.printDebug("Sync Images");
						match = buildMap(listAll(imageFile));
						images = listAll(teams);
						for (int j = 0; j < images.length; j++) {
							// copy changed ones (we're assuming, given that the files are images,
							//  that different images are very unlikely to have the same size)
							name = images[j].getName().toLowerCase();
							dest = match.get(name);
							if (dest == null) dest = new File(imageFile, name);
							if (dest.length() != images[j].length())
								copy(images[j], dest);
						}
						AppLib.printDebug("Ejecting");
						VolumeLister.eject(volumes[i]);
						AppLib.printWarn(status.getWindow(), "Sync done - OK to remove.");
						break;
					case 1:
						// from
						try {
							Backend newBack = new FileBackend(dataFile);
							newBack.selfCheck();
							DataStore newData = newBack.getData();
							newBack.close();
							Event old = data.getActive();
							AppLib.printDebug("Sync Data");
							synchronized (outputs) {
								// make this always work (I know it's not really feasible...)
								data.setData(newData);
								Iterator<Event> it = data.getData().getEvents().iterator();
								Event item;
								while (it.hasNext()) {
									item = it.next();
									if (item.equals(old)) {
										data.setActive(item);
										break;
									}
								}
								if (data.getActive() == null && data.getData().getEvents().size() > 0)
									data.setActive(data.getData().getEvents().get(0));
							}
							fireUpdate();
						} catch (Exception e) {
							AppLib.debugException(e);
							AppLib.printWarn(status.getWindow(), "Couldn't read data from " +
								"removable disk.\nPlease ensure that data had been previously " +
								"written to the disk.");
							break;
						}
						AppLib.printDebug("Sync Images");
						match = buildMap(listAll(teams));
						images = listAll(imageFile);
						for (int j = 0; j < images.length; j++) {
							// copy changed ones (we're assuming, given that the files are images,
							//  that different images are very unlikely to have the same size)
							name = images[j].getName().toLowerCase();
							dest = match.get(name);
							if (dest == null) dest = new File(teams, name);
							if (dest.length() != images[j].length()) {
								copy(images[j], dest);
								try {
									generateThumb(dest);
								} catch (IOException e) {
									AppLib.debugException(e);
									AppLib.printDebug("Could not get thumbnail for " + dest.getName()
										+ " - corrupted file?");
								}
							}
						}
						AppLib.printDebug("Ejecting");
						VolumeLister.eject(volumes[i]);
						AppLib.printWarn(status.getWindow(), "Sync done - OK to remove.");
						break;
					default:
					}
					if (kioskOpen) status.getClient().showKiosk(true);
					seen.add(volumes[i]);
				}
			}
		}
	}

	/**
	 * Runs the image loader.
	 */
	private static class ImageDaemon extends Thread {
		/**
		 * Creates an image daemon and verifies the directory.
		 */
		public ImageDaemon() {
			setName("Image Connection Listener");
			setPriority(Thread.MIN_PRIORITY);
			if (teams != null) {
				// fatal for files
				if (teams.exists() && !teams.isDirectory()) return;
				// create the directory
				if (!teams.exists()) teams.mkdirs();
			}
			if (thumbs != null) {
				// fatal for files
				if (thumbs.exists() && !thumbs.isDirectory()) return;
				// create the directory
				if (!thumbs.exists()) thumbs.mkdirs();
			}
			start();
		}
		public void run() {
			if (teams == null || !teams.isDirectory())
				return;
			if (thumbs == null || !thumbs.isDirectory())
				return;
			// open listener
			AppLib.printDebug("[Image] Opening listener on port "
				+ Constants.BULK_PORT);
			ServerSocket iss = null;
			try {
				iss = new ServerSocket(Constants.BULK_PORT);
				while (true) {
					// accept incoming connection
					Socket sock = iss.accept();
					AppLib.printDebug("[Image] Connecting to "
						+ sock.getInetAddress().getHostAddress());
					new ImageServerThread(sock).start();
				}
			} catch (Exception e) {
				// forget the "already in use" message.
				if (e.getMessage().indexOf("already in use") < 0)
					AppLib.debugException(e);
			} finally {
				// ensure closure
				if (iss != null)
					try {
						iss.close();
					} catch (Exception e) { }
			}
		}
	}

	/**
	 * Generates a thumbnail.
	 * 
	 * @param file the file to thumbnail
	 */
	private static void generateThumb(File file) throws IOException {
		String name = file.getName();
		Image img = ImageIO.read(file);
		int iw = img.getWidth(null), fiw, ih = img.getHeight(null), fih;
		// get width and height
		if (ih > iw) {
			fiw = thumbSize.height;
			fih = thumbSize.width;
		} else {
			fiw = thumbSize.width;
			fih = thumbSize.height;
		}
		BufferedImage toWrite = new BufferedImage(fiw, fih, BufferedImage.TYPE_INT_RGB);
		// set graphics options
		Graphics2D g2d = toWrite.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_OFF);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
			RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		// copy to write
		g2d.drawImage(img, 0, 0, fiw, fih, null);
		ImageIO.write(toWrite, "jpeg", new File(thumbs.getAbsolutePath(), name));
		// ugly, but we can't run out of memory here!
		g2d.dispose(); toWrite.flush(); toWrite = null;
		AppLib.printDebug("[Image] Thumbnail done for " + name);
	}

	/**
	 * Class to handle image uploads. Spawned as needed.
	 */
	private static class ImageServerThread extends Thread {
		/**
		 * The socket where data is coming in.
		 */
		private Socket sock;

		/**
		 * Creates a new image server thread on the given socket.
		 * 
		 * @param s the socket of the client
		 */
		public ImageServerThread(Socket s) {
			setName("Image Reciever from " + s.getInetAddress().getHostAddress());
			sock = s;
		}
		public void run() {
			if (sock == null) return;
			try {
				// initialize variables
				InputStream is = sock.getInputStream();
				StringBuilder name; char in = '\n';
				String datName; int ind;
				try {
					name = new StringBuilder(64);
					// read the name and parse it
					while ((in = (char)is.read()) > 0 && in != Constants.BULK_EOL && in < 256)
						name.append(in);
					datName = name.toString().trim();
					ind = datName.indexOf(Constants.BULK_DELIM);
					if (ind > 0) {
						// read file name and length
						String fnm = datName.substring(0, ind);
						int len = Integer.parseInt(datName.substring(ind + 1, datName.length()));
						// prepare output files
						AppLib.printDebug("[Image] Recieving " + fnm + " (" + (len / 1024) + "K)");
						File out = new File(teams.getAbsolutePath(), fnm);
						FileOutputStream os = new FileOutputStream(out);
						int read, sent = 0;
						byte[] buffer = new byte[Constants.BULK_BUFFER];
						// recieve image
						while (sent < len && (read = is.read(buffer, 0,
								Math.min(Constants.BULK_BUFFER, len - sent))) > 0) {
							os.write(buffer, 0, read);
							sent += read;
						}
						// save image and close
						os.flush();
						os.close();
						AppLib.printDebug("[Image] Saved " + fnm);
						// thumbnail generate
						Thread.sleep(500L);
						generateThumb(out);
					}
				} catch (Exception e) {
					AppLib.debugException(e);
				}
				// close down
				AppLib.printDebug("[Image] Closing " + sock.getInetAddress().getHostAddress());
			} catch (IOException e) {
				// ensure closure
				if (sock != null) try {
					sock.close();
				} catch (Exception ex) { }
			}
		}
	}

	// AJAX MODULE
	/**
	 * Sends the AJAX interface.
	 * 
	 * @param url the URL requested
	 * @param out the output stream
	 */
	private void sendAJAX(String url, PrintWriter out) {
		// reach into the JAR!
		String nUrl = "ajax.js";
		if (url.equals("ajax_css"))
			nUrl = "ajax.css";
		sendContent(nUrl, out);
	}
	/**
	 * Sends the desired content file from the JAR.
	 * 
	 * @param nUrl the file name
	 * @param out the output stream
	 */
	private void sendContent(String nUrl, PrintWriter out) {
		try {
			URL scoringURL = getClass().getResource("/" + nUrl);
			URLConnection conn = scoringURL.openConnection();
			// pass thru
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			// headers
			Worker.sendHTTP(out, HTTPConstants.HTTP_OK);
			sendInitialHeaders(out);
			out.print("Content-Type: ");
			if (nUrl.endsWith(".js"))
				out.print("text/javascript");
			else if (nUrl.endsWith(".css"))
				out.print("text/css");
			else
				out.print("text/html");
			out.print("\r\n\r\n");
			while (br.ready())
				out.println(br.readLine());
			br.close();
			out.flush();
		} catch (IOException e) {
			sendErrorMessage(out, true, "This server is not configured for Web usage.", e);
		}
	}
	/**
	 * Sends the desired content file from the JAR with text replacement.
	 * 
	 * @param nUrl the file name
	 * @param out the output stream
	 * @param find the item to find (all instances not spanning lines will be found),
	 * @param replace the replacement
	 */
	private void sendContent(String nUrl, PrintWriter out, Pattern find, String replace) {
		try {
			URL scoringURL = getClass().getResource("/" + nUrl);
			URLConnection conn = scoringURL.openConnection();
			// pass thru
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			// headers
			Worker.sendHTTP(out, HTTPConstants.HTTP_OK);
			sendInitialHeaders(out);
			out.print("Content-Type: ");
			if (nUrl.endsWith(".js"))
				out.print("text/javascript");
			else if (nUrl.endsWith(".css"))
				out.print("text/css");
			else
				out.print("text/html");
			out.print("\r\n\r\n");
			while (br.ready())
				out.println(find.matcher(br.readLine()).replaceAll(replace));
			br.close();
			out.flush();
		} catch (IOException e) {
			sendErrorMessage(out, true, "This server is not configured for Web usage.", e);
		}
	}
	/**
	 * Tries to login to the server.
	 * 
	 * @param out the output stream
	 * @param ip the client IP
	 * @param extra the extra data from the client (for POST)
	 */
	private void sendLogin(String url, PrintWriter out, String ip, String extra) {
		if (extra == null) {
			// log out
			Map<String, String> login = Worker.parseGET(url.substring(7));
			Session sess;
			if (login.containsKey("sid") && (sess = sid.get(login.get("sid"))) != null) {
				// destroy
				AppLib.printDebug(sess.user + " logout");
				synchronized (sid) {
					sid.remove(sess.id);
				}
				httpOK(out);
				out.print("<html><head><title>Logout</title></head><body><div align=\"center\">");
				out.print("<b>You have signed out of Scout449.</b><br /><br />You can <a href=\"/\">");
				out.print("login again</a> if you want to, or get the <a href=\"get-s449\">JAVA ");
				out.print("client</a> for faster speed and better looks.</div></body></html>");
			} else
				sendInvalid(out, "Invalid SID.");
			return;
		}
		String[] lines = extra.split("\\n\\n");
		if (lines.length < 2) {
			sendInvalid(out, "Invalid login.");
			return;
		}
		// we have POST data
		Map<String, String> login = Worker.parseGET(lines[1]);
		if (login.containsKey("action") && login.get("action").equalsIgnoreCase("login")
				&& login.containsKey("user") && login.containsKey("pass")) {
			// login attempt
			UserData u = new UserData(login.get("user"), login.get("pass").toCharArray());
			u = config.getData().authUser(u);
			if (u == null) {
				// invalid username and password
				AppLib.printDebug("Bad password from " + ip);
				httpOK(out);
				out.print("<html><head><title>Invalid</title></head><body><div align=\"center\">");
				out.print("<b>Invalid username and password.</b><br /><br /><a href=\"/\">Retry");
				out.print(" login</a> or contact the scoutmaster if you have forgotten your ");
				out.print("password.<br /><b>Usernames and passwords are case sensitive.</b></div>");
				out.print("</body></html>");
			} else if (u.canRead()) {
				// session
				AppLib.printDebug(ip + " login as " + u.getName());
				String sess = UserData.hash((Math.round(Math.random() * 1E9) +
					"scout449").toCharArray());
				sid.put(sess, new Session(sess, u));
				sendContent("home.htm", out, match_sid, sess);
			} else
				sendInvalid(out, "Your account cannot use the Web Interface.");
		} else
			sendInvalid(out, "Invalid login. Some browsers haven't been tested for Web usage." +
				" If you believe that this message is in error, contact the scoutmaster.");
	}
	/**
	 * Sends the javascript configuration to AJAX.
	 * 
	 * @param url the URL requested
	 * @param out the output stream
	 */
	private void sendConfig(String url, PrintWriter out) {
		Map<String, String> params = Worker.parseGET(url.substring(8));
		String id = params.get("sid");
		Session sess;
		if (id != null && (sess = sid.get(id)) != null) {
			AppLib.printDebug("Sending AJAX config");
			StringBuilder output = new StringBuilder(2048);
			output.append("var tpa = ");
			output.append(ScheduleItem.TPA);
			output.append(";\nvar sid = '");
			output.append(sess.id);
			output.append("';\nvar perms = ");
			output.append(sess.user.getAccess());
			output.append(";\nvar name = '");
			output.append(escapeQuotes(sess.user.getName()));
			output.append("';\nvar realName = '");
			output.append(escapeQuotes(sess.user.getRealName()));
			output.append("';\nvar myTeam = ");
			output.append(sess.user.getTeamNum());
			output.append(";\nvar advScore = ");
			output.append(data.getData().isAdvScore() ? "true" : "false");
			output.append(";\nvar spacing = ");
			Object[] o = data.getData().getExtraData();
			if (o.length > 1 && o[1] instanceof Integer)
				output.append(o[1]);
			else
				output.append("6");
			int i;
			// UDF field names
			output.append(";\nvar udfs = new Array();\n");
			i = 0;
			List<UDF> udfs = data.getData().getUDFs();
			if (udfs != null) {
				Iterator<UDF> it = udfs.iterator();
				while (it.hasNext()) {
					output.append("udfs[");
					output.append(i);
					output.append("] = '");
					output.append(escapeQuotes(it.next().getName()));
					output.append("';\n");
					i++;
				}
			}
			// Robot types
			output.append("var types = new Array();\n");
			i = 0;
			List<String> types = data.getData().getTypes();
			if (types != null) {
				Iterator<String> it = types.iterator();
				while (it.hasNext()) {
					output.append("types[");
					output.append(i);
					output.append("] = '");
					output.append(escapeQuotes(it.next()));
					output.append("';\n");
					i++;
				}
			}
			// Match labels
			output.append("var labels = new Array();\n");
			i = 0;
			List<MatchLabel> labels = data.getData().getLabels();
			Iterator<MatchLabel> it = labels.iterator();
			while (it.hasNext()) {
				output.append("labels[");
				output.append(i);
				output.append("] = '");
				output.append(escapeQuotes(it.next().getLabel()));
				output.append("';\n");
				i++;
			}
			// send to browser
			Worker.sendHTTP(out, HTTPConstants.HTTP_OK);
			sendInitialHeaders(out);
			out.print("Content-Type: text/javascript\r\n\r\n");
			out.print(output.toString());
		} else
			sendInvalid(out, "Not a valid SID.");
	}
	/**
	 * Sends AJAX data to the browser.
	 * 
	 * @param url the URL requested
	 * @param out the output stream
	 */
	private void sendData(String url, PrintWriter out) {
		// parse get string and validate
		StringBuilder output = new StringBuilder(4096);
		Map<String, String> params = Worker.parseGET(url.substring(5));
		String tm = params.get("team");
		String dat = params.get("data");
		String ind = params.get("index");
		String op = params.get("op");
		String tme = params.get("time");
		String id = params.get("sid");
		int teamNum = -1, index = -1; Session sess;
		Team team = null; long time = -1L;
		if (op != null && id != null && (sess = sid.get(id)) != null && sess.user.canRead()) {
			// parse team, time, and index
			try {
				teamNum = Integer.parseInt(tm);
				team = data.get(teamNum);
			} catch (Exception e) { }
			try {
				index = Integer.parseInt(ind);
			} catch (Exception e) { }
			try {
				time = Long.parseLong(tme);
			} catch (Exception e) { }
			if (op.equals("name")) {
				if (team != null) { // specified team
					AppLib.printDebug("AJAX Sending data: team " + teamNum);
					sendTeamInfo(output, team);
				} else { // all teams
					AppLib.printDebug("AJAX Sending data: all teams");
					Iterator<Integer> it = data.getTeams().iterator();
					while (it.hasNext())
						sendTeamInfo(output, data.get(it.next()));
				}
			} else if (op.equals("stime")) {
				AppLib.printDebug("AJAX Synchronizing time and lateness");
				output.append(System.currentTimeMillis()); // synchronize server time
				output.append('\n');
				output.append(data.getData().minutesLate());
				output.append('\n');
			} else if (op.equals("comments") && team != null) {
				// get comments
				AppLib.printDebug("AJAX Sending comments for: team " + teamNum);
				Iterator<Comment> it = team.getComments().iterator();
				Iterator<Integer> it2;
				Comment comment;
				while (it.hasNext()) {
					comment = it.next();
					output.append(comment.getOwner().getName());
					output.append(',');
					output.append(comment.getOwner().getRealName());
					output.append(',');
					output.append(comment.getRating());
					output.append(',');
					it2 = comment.getUDFs().iterator();
					while (it2.hasNext()) {
						output.append(it2.next());
						output.append(',');
					}
					output.append(Worker.urlencode(comment.getText()));
					output.append('\n');
				}
			} else if (op.equals("comm") && team != null && dat != null && dat.length() > 0
					&& sess.user.canWrite()) {
				// update comment
				AppLib.printDebug("AJAX updating comment: team " + teamNum);
				Iterator<Comment> it = team.getComments().iterator();
				Comment comment; boolean changed = false;
				StringTokenizer str = new StringTokenizer(dat, ",");
				while (it.hasNext()) {
					comment = it.next();
					if (comment.getOwner().equals(sess.user)) {
						// match - update
						// todo: make this work for comments when there's a match tagged onto it
						try {
							int rate = Integer.parseInt(str.nextToken());
							String text = Worker.urldecode(str.nextToken());
							int sz = data.getData().getUDFs().size();
							// update text and udfs
							List<Integer> udfs = new ArrayList<Integer>(sz);
							while (str.hasMoreTokens() && udfs.size() < sz)
								udfs.add(Integer.parseInt(str.nextToken()));
							if (udfs.size() == sz) {
								// good to go
								comment.setText(text);
								comment.setWhen(System.currentTimeMillis());
								comment.setRating(rate);
								comment.getUDFs().clear();
								comment.getUDFs().addAll(udfs);
								data.updateComment(team.getNumber(), comment);
								changed = true;
							}
						} catch (Exception e) {
							AppLib.debugException(e);
						}
						break;
					}
				}
				// add?
				if (!changed) try {
					str = new StringTokenizer(dat, ",");
					int rate = Integer.parseInt(str.nextToken());
					String text = Worker.urldecode(str.nextToken()).trim();
					int sz = data.getData().getUDFs().size();
					// update text and udfs
					List<Integer> udfs = new ArrayList<Integer>(sz);
					while (str.hasMoreTokens() && udfs.size() < sz)
						udfs.add(Integer.parseInt(str.nextToken()));
					if (udfs.size() == sz) {
						// good to go, add it on
						data.updateComment(team.getNumber(), new Comment(sess.user, null,
							text, rate, udfs, System.currentTimeMillis()));
						changed = true;
					}
				} catch (Exception e) {
					AppLib.debugException(e);
				}
				if (changed) {
					fireUpdate();
					output.append("success\n");
				} else
					output.append("error\n");
			} else if (op.equals("type") && team != null && dat != null && dat.length() > 0
					&& sess.user.canWrite()) {
				dat = Worker.urldecode(dat);
				AppLib.printDebug("AJAX Setting type: team " + teamNum);
				if (data.getData().getTypes().contains(dat)) { // set type
					data.setType(teamNum, dat);
					fireUpdate();
					output.append("success\n");
				} else
					output.append("error\n");
			} else if (op.equals("late") && sess.user.canScore()) {
				AppLib.printDebug("AJAX Running Late: " + index);
				data.runLate(index); // running late?
				fireUpdate();
				output.append("success\n");
			} else if (op.equals("match") && time > 0L && dat != null && dat.length() > 0
					&& index > 0 && sess.user.canScore()) {
				AppLib.printDebug("AJAX Match Entry@" + time);
				int theTeam; // match entry
				StringTokenizer str = new StringTokenizer(dat, ",");
				try {
					List<Integer> teams = new ArrayList<Integer>(ScheduleItem.TPA * 2);
					BitSet surrogate = new BitSet(ScheduleItem.TPA * 2);
					for (int i = 0; i < ScheduleItem.TPA * 2; i++) {
						// add teams
						theTeam = Integer.parseInt(str.nextToken());
						if (data.get(theTeam) == null) teams.add(0);
						else teams.add(theTeam);
						surrogate.set(i, Character.toLowerCase(str.nextToken().charAt(0)) == 't');
					}
					// init match-to-be
					ScheduleItem match = new ScheduleItem(teams, time, true);
					match.getSurrogate().or(surrogate);
					match.setNum(index);
					Iterator<MatchLabel> it = data.getData().getLabels().iterator();
					String toMatch = str.nextToken();
					MatchLabel dest = MatchLabel.blank, item;
					while (it.hasNext()) {
						item = it.next();
						if (item.getLabel().equalsIgnoreCase(toMatch)) {
							dest = item;
							break;
						}
					}
					match.setLabel(dest);
					data.addMatch(match);
					fireUpdate();
					output.append("success\n");
				} catch (Exception e) {
					AppLib.debugException(e);
					output.append("error\n");
				}
			} else if (op.equals("matches"))
				// send all matches
				synchronized (data.getSchedule()) {
					AppLib.printDebug("AJAX Sending data: all matches");
					ScheduleItem match; Iterator<Score> it3;
					Iterator<ScheduleItem> it = data.getSchedule().values().iterator();
					Iterator<Integer> it2;
					while (it.hasNext()) {
						match = it.next();
						output.append(match.getTime());
						output.append(',');
						output.append(match.getNum());
						output.append(',');
						output.append(match.getLabel());
						output.append(',');
						output.append(match.getStatus() ? "comp" : "sched");
						output.append(',');
						output.append(match.getRedScore());
						output.append(',');
						output.append(match.getBlueScore());
						it2 = match.getTeams().iterator();
						while (it2.hasNext()) {
							output.append(',');
							output.append(it2.next());							
						}
						if (match.getScores() != null && data.getData().isAdvScore()) {
							it3 = match.getScores().iterator();
							while (it3.hasNext()) {
								output.append(',');
								output.append(it3.next().totalScore());
							}
						} else
							output.append(",null");
						output.append('\n');
					}
				}
			// send to browser
			sendText(out, output.toString());
		} else {
			AppLib.printDebug("Invalid AJAX query");
			sendInvalid(out, "Invalid AJAX query.");
		}
	}
	/**
	 * Sends the given team in an AJAX format.
	 * 
	 * @param output the output buffer
	 * @param team the team to send
	 */
	private void sendTeamInfo(StringBuilder output, Team team) {
		output.append(team.getNumber());
		output.append(',');
		output.append(team.getName());
		output.append(',');
		output.append(team.getRating());
		output.append(',');
		output.append(team.getPoints());
		output.append(',');
		output.append(team.getTeamPoints());
		output.append(',');
		output.append(team.getWins());
		output.append(',');
		output.append(team.getLosses());
		output.append(',');
		output.append(team.getTies());
		output.append(',');
		output.append(team.getType());
		Iterator<Integer> it = team.getData().iterator();
		while (it.hasNext()) {
			output.append(',');
			output.append(it.next());
		}
		output.append('\n');
	}
	/**
	 * Sends the given text in an AJAX text file.
	 * 
	 * @param out the output stream
	 * @param text the text to send
	 */
	private void sendText(PrintWriter out, String text) {
		Worker.sendHTTP(out, HTTPConstants.HTTP_OK);
		sendInitialHeaders(out);
		out.print("Content-Type: text/plain\r\n\r\n");
		out.println(text);
	}
	public boolean match(String url) {
		return toMatch.matcher(url).matches();
	}
	public void action(String url, String inet, String inData, PrintWriter out,
			OutputStream os) {
		if (url.length() < 1)
			sendContent("index.htm", out);
		else if (url.equals("ajax_css") || url.equals("ajaxsrc_js"))
			sendAJAX(url, out);
		else if (url.equals("error"))
			sendHTTPError(out, inData);
		else if (url.equals("login"))
			sendLogin(url, out, inet, inData);
		else if (url.startsWith("logout?"))
			sendLogin(url, out, inet, null);
		else if (url.startsWith("ajax_js?"))
			sendConfig(url, out);
		else if (url.startsWith("ajax?"))
			sendData(url, out);
		else if (url.startsWith("thumbnail?"))
			sendThumbnail(url, out, os);
		else if (url.startsWith("image?"))
			sendBigImage(url, out, os);
		else if (url.startsWith("report?"))
			sendReport(url, out);
		else if (url.startsWith("imgrs?"))
			sendIntImage(url, out, os);
		else if (url.equals("get-s449"))
			sendGetS449(out);
		else
			sendInvalid(out, "The specified module could not be found.");
	}
	/**
	 * Sends a general image or no image if none.
	 * 
	 * @param url the image URL
	 * @param out the output writer
	 * @param os the output stream
	 */
	private void sendIntImage(String url, PrintWriter out, OutputStream os) {
		String img = url.substring(6);
		if (img.indexOf('.') <= 0 || img.indexOf('/') >= 0 || img.indexOf('\\') >= 0) {
			// no hacking!
			Worker.sendHTTP(out, HTTPConstants.HTTP_FORBIDDEN);
			sendInitialHeaders(out);
			out.print("\r\n");
			return;
		}
		try {
			String ctype = "png";
			if (img.endsWith(".jpg") || img.endsWith(".jpeg")) ctype = "jpg";
			if (img.endsWith(".gif")) ctype = "gif";
			// get the image
			URL imgURL = getClass().getResource("/images/" + img);
			InputStream is = imgURL.openStream();
			Worker.sendHTTP(out, HTTPConstants.HTTP_OK);
			sendInitialHeaders(out);
			// correct content type
			out.print("Content-Type: image/");
			out.print(ctype);
			out.print("\r\n\r\n");
			out.flush();
			copyStream(is, os);
			is.close();
		} catch (Exception e) {
			// no such image
			Worker.sendHTTP(out, HTTPConstants.HTTP_NOT_FOUND);
			sendInitialHeaders(out);
			out.print("\r\n");
		}
	}
	/**
	 * Sends an invalid error message page, with help.
	 * 
	 * @param out the output stream
	 * @param reason the reason for the error.
	 */
	private void sendInvalid(PrintWriter out, String reason) {
		sendErrorMessage(out, false, reason, null);
	}
	/**
	 * Sends the "Get Scout449 JAVA Client" web page.
	 * 
	 * @param out the output stream
	 */
	private void sendGetS449(PrintWriter out) {
		httpOK(out);
		out.print("<html><head><title>Get Scout449 JAVA Client</title></head><body><b>Get Scout449");
		out.print("</b><br />For improved performance over the web interface, get the <a href=\"");
		out.print("scout449.jar\">JAVA</a> client now.<br />System Requirements:<ul><li>Java SE ");
		out.print("5.0 or later</li><li>Windows, Mac, or Linux operating system</li><li>A halfway ");
		out.print("decent processor (anything will do, but slower means Scout449 will be slower)");
		out.print("</li><li>At least 128 MB of RAM (JAVA hogs memory...)</li></ul>");
		out.print("</body></html>");
	}
	/**
	 * Sends the HTTP headers and all OK.
	 * 
	 * @param out the output stream
	 */
	private void httpOK(PrintWriter out) {
		Worker.sendHTTP(out, HTTPConstants.HTTP_OK);
		sendInitialHeaders(out);
		out.print("Content-Type: text/html\r\n\r\n");
	}
	/**
	 * Handles an HTTP error such as not found, bad request...
	 * 
	 * @param out the output stream
	 * @param message the error message (from the extra headers)
	 */
	private void sendHTTPError(PrintWriter out, String message) {
		try {
			message = message.trim();
			int index = message.indexOf(';'), code;
			String page;
			if (index >= 0) {
				// url was specified; pull it out
				code = Integer.parseInt(message.substring(0, index));
				page = message.substring(index + 1, message.length());
			} else {
				code = Integer.parseInt(message); page = "";
			}
			if (code == HTTPConstants.HTTP_NOT_FOUND) {
				// try to determine content type
				String ext = new File(page).getName();
				index = ext.indexOf('.');
				if (index > 0) {
					// look at content type; feed the error message only for html
					ext = ext.substring(index, ext.length());
					ext = HTTPConstants.mimeTypes.get(ext);
					if (ext != null && ext.equals("text/html"))
						sendInvalid(out, "The specified URL or module was not found.");
					else {
						Worker.sendHTTP(out, HTTPConstants.HTTP_NOT_FOUND);
						sendInitialHeaders(out);
						out.print("\r\n");
					}
				} else
					// it's a module request (no dot). Epicfail.
					sendInvalid(out, "The specified URL or module was not found.");
			} else
				// general HTTP exception
				sendInvalid(out, code + " " + HTTPConstants.messages.get(code));
		} catch (Exception e) {
			sendInvalid(out, "The specified URL or module was not found.");
		}
	}
	/**
	 * The parent for exception handling. Does everything needed to print a
	 *  graceful failure message.
	 * 
	 * @param out the output stream
	 * @param maybeValid whether the request was an unexpected error
	 * @param reason the reason for the error
	 * @param e the exception (if any)
	 */
	private void sendErrorMessage(PrintWriter out, boolean maybeValid, String reason,
		Exception e) {
		StringBuffer output = new StringBuffer(4096);
		output.append("<b><font size=\"+1\">We're sorry...</font></b><br>\n... but the request ");
		output.append("that you made");
		if (maybeValid)
			output.append(", while possibly valid, could not be processed");
		else
			output.append(" was not valid");
		output.append(".<br>\n<i>The reason stated by the ");
		if (reason == null) reason = "An internal error occurred.";
		output.append("server code module is: ");
		output.append(htmlspecial(reason));
		output.append("</font></i><br>\n");
		output.append("<b><font size=\"+1\">Suggestions:</font></b><br>\n<ul><li>If you accessed ");
		output.append("this page by direct URL entry or using a bookmark, the link is likely to ");
		output.append("be outdated or old.<br>\nTry going to the <a href=\"/\">home page</a>.</li>\n");
		output.append("<li>If you tried to edit information about a team, another user may have ");
		output.append("since modified this team's information. Try re-starting your edit from ");
		output.append("the beginning, or going <a href=\"javascript:history.go(-1)\">back</a> ");
		output.append("and trying again. Scout449 users always have priority over web users.</li>");
		output.append("\n<li>Try contacting the Administrator of this server. ");
		output.append("Ask for help on QuickChat, but do <i>not</i> ring the bell!</li>\n<li>");
		output.append("Still stuck? Contact the scoutmaster or another operator.</li>\n");
		if (e != null) {
			output.append("</ul><br><br>Detailed Error Information:<br>\n<code>");
			// can't use printStackTrace because of html escapes
			StackTraceElement[] trace = e.getStackTrace();
            for (int i = 0; i < trace.length; i++)
                output.append("\tat " + htmlspecial(trace[i].toString()));
			output.append("</code>\n");
		} else output.append("</ul>\n");
		httpOK(out);
		out.print("<html><head><title>Error</title></head><body><b>");
		// body text
		out.print(maybeValid ? "Server Error" : "User Error");
		out.println("</b><br><br>");
		out.println(output.toString());
		out.println("</body></html>");
	}
	/**
	 * Prints the (even better) start-up headers.
	 * 
	 * @param out the output stream
	 */
	private void sendInitialHeaders(PrintWriter out) {
		out.print("Server: Scout449 ");
		out.print(Constants.VERSION);
		out.print("\r\nDate: ");
		out.print(new Date());
		out.print("\r\n");
	}
	/**
	 * Sends the scouting report page for a given match.
	 * 
	 * @param out the output stream
	 * @param match the match to report
	 */
	private void sendRealReport(PrintWriter out, ScheduleItem match) {
		StringBuilder output = new StringBuilder(8192);
		// compute title
		String title = match.getLabel() + " #" + match.getNum() + " at " +
			ScheduleItem.timeFormat(match.getTime()) +
			" (" + ScheduleItem.dateFormat(match.getTime()) + ")";
		// html headers
		output.append("<html><head><title>Scouting Report for ");
		output.append(title);
		// html body and header
		output.append("</title>\n</head><body><font size=\"-2\"><i><a href=\"/\">Return to ");
		output.append("homepage</a></i></font><hr>\n<center><b><font size=\"+1\">");
		output.append(title);
		output.append("</font></b></center>\n<font face=\"Verdana, Arial, sans-serif\">\n");
		// body text
		output.append("<table border=\"1\" cellpadding=\"0\" cellspacing=\"1\" width=\"660");
		output.append("\"><tr><th width=\"330\" bgcolor=\"#FF0000\">Red</th>");
		output.append("<th width=\"330\" bgcolor=\"#00FFFF\">Blue</th></tr>\n");
		// print each row
		List<Integer> teams = match.getTeams();
		for (int i = 0; i < ScheduleItem.TPA; i++) {
			// the red team
			output.append("<tr>");
			sendMiniReport(output, teams.get(i));
			// the blue team
			sendMiniReport(output, teams.get(i + ScheduleItem.TPA));
			output.append("</tr>");
		}
		// generated time and version
		output.append("</table><hr><font size=\"-2\"><i><a href=\"/\">Return to homepage");
		output.append("</a></i> : <i>Printed at ");
		long date = System.currentTimeMillis();
		output.append(ScheduleItem.timeFormat(date));
		output.append(" on ");
		output.append(ScheduleItem.dateFormat(date));
		output.append("; version ");
		output.append(Constants.VERSION);
		// html footer
		output.append("</i></font></font></body></html>");
		// http startup
		Worker.sendHTTP(out, HTTPConstants.HTTP_OK);
		sendInitialHeaders(out);
		out.print("Content-Type: text/html; charset=ISO-8859-1\r\n\r\n");
		out.print(output.toString());
	}
	/**
	 * Helper to send a mini-report (used in match report)
	 * 
	 * @param output the output buffer
	 * @param tm the team number to send
	 */
	private void sendMiniReport(StringBuilder output, int tm) {
		int j; Team team = data.get(tm);
		List<Integer> myData; Set<Comment> coms;
		Iterator<Integer> comDt;
		if (team == null) {
			// this should never happen. make it graceful!
			output.append("<td height=\"250\" valign=\"top\">Team Not Found</td>");
			return;
		}
		output.append("<td height=\"250\" valign=\"top\">\n<img src=\"thumbnail?team=");
		output.append(team.getNumber());
		output.append("\" alt=\"[No Image]\" width=\"120\" height=\"90\" align=\"left");
		output.append("\">\n");
		// team number
		String nn = team.toString();
		if (nn.length() > 20) nn = nn.substring(0, 20) + "...";
		output.append(htmlspecial(nn));
		output.append("<br>");
		// record, ppg
		output.append("<font size=\"-1\"><b>Record: ");
		output.append(team.getWins());
		output.append("-");
		output.append(team.getLosses());
		output.append("-");
		output.append(team.getTies());
		output.append("<br>Points/Game: ");
		output.append(team.getPPG());
		output.append("</b><br>\n");
		output.append(htmlspecial(team.getType()));
		output.append(" ");
		// stars
		double stars = team.getRating();
		int st = (int)Math.floor(stars);
		double starP = stars - st;
		for (j = 0; j < st && j < 5; j++)
			output.append("<img src=\"imgrs?star-lit.png\" alt=\"*\">");
		if (starP < .125 && j < 5) {
			output.append("<img src=\"imgrs?star-unlit.png\" alt=\" \">");
			j++;
		} else if (starP >= .125 && starP < .375 && j < 5) {
			output.append("<img src=\"imgrs?star-25.png\" alt=\" \">");
			j++;
		} else if (starP >= .375 && starP < .625 && j < 5) {
			output.append("<img src=\"imgrs?star-50.png\" alt=\" \">");
			j++;
		} else if (starP >= .625 && starP < .875 && j < 5) {
			output.append("<img src=\"imgrs?star-75.png\" alt=\"*\">");
			j++;
		} else if (j < 5) {
			output.append("<img src=\"imgrs?star-lit.png\" alt=\"*\">");
			j++;
		}
		for (; j < 5; j++)
			output.append("<img src=\"imgrs?star-unlit.png\" alt=\" \">");
		output.append("<br>\n");
		myData = team.getData();
		// UDFs. Only the 1st 3 (at most) get printed!
		coms = team.getComments();
		List<UDF> udfs = data.getData().getUDFs();
		if (myData != null && udfs != null) {
			comDt = myData.iterator();
			Iterator<UDF> it = udfs.iterator();
			for (j = 0; comDt.hasNext() && j < Math.max(3, 7 - coms.size()) && it.hasNext(); j++) {
				output.append("<b>");
				output.append(htmlspecial(it.next().getName()));
				output.append(":</b>&nbsp; ");
				output.append(comDt.next());
				output.append("<br>\n");
			}
		}
		// comments. Only the 1st 4 (at most) get printed!
		if (coms != null) {
			Iterator<Comment> it = coms.iterator();
			for (j = 0; it.hasNext() && j < 4; j++) {
				output.append(htmlspecial(it.next().getText()));
				output.append("<br>\n");
			}
		}
		output.append("<hr><br></font></td>\n");
	}
	/**
	 * Sends the scouting report page.
	 * 
	 * @param url the input URL
	 * @param out the output stream
	 */
	private void sendReport(String url, PrintWriter out) {
		Map<String, String> params = Worker.parseGET(url.substring(7));
		if (params.containsKey("time")) {
			// validate match
			long tm = 0L;
			String event = null;
			try {
				tm = Long.parseLong(params.get("time"));
			} catch (Exception e) {
				sendInvalid(out, "Match number or time is not valid.");
				return;
			}
			if (params.containsKey("event")) {
				event = params.get("event");
				if (event == null || event.length() != 2) event = null;
				else event = event.toUpperCase();
			}
			// select a regional
			Map<SecondTime, ScheduleItem> sched;
			if (event == null)
				sched = data.getSchedule();
			else
				sched = data.getEvent(event).getSchedule();
			synchronized (sched) {
				ScheduleItem match = sched.get(new SecondTime(tm));
				if (match == null)
					sendInvalid(out, "No matches matched this query.");
				else
					sendRealReport(out, match);
			}
		} else
			sendInvalid(out, "No match identifier supplied.");
	}
	/**
	 * Sends a team thumbnail or no image if none.
	 * 
	 * @param url the thumbnail URL
	 * @param out the output writer
	 * @param os the output stream
	 */
	private void sendThumbnail(String url, PrintWriter out, OutputStream os) {
		// parse get string and validate
		Map<String, String> params = Worker.parseGET(url.substring(10));
		if (params.containsKey("team")) {
			// validate team number and index
			String tm = params.get("team");
			if (!AppLib.positiveInteger(tm)) {
				Worker.sendHTTP(out, HTTPConstants.HTTP_NOT_FOUND);
				sendInitialHeaders(out);
				out.print("\r\n");
			} else try {
				// get the image
				FileInputStream is = new FileInputStream(new File(thumbs, tm + ".jpg"));
				AppLib.printDebug("Sending thumbnail for " + tm);
				Worker.sendHTTP(out, HTTPConstants.HTTP_OK);
				sendInitialHeaders(out);
				out.print("Content-Type: image/jpg\r\n\r\n");
				out.flush();
				copyStream(is, os);
				is.close();
			} catch (Exception e) {
				try {
					// get the image
					URL scoringURL = Server.class.getResource("/images/none.jpg");
					URLConnection conn = scoringURL.openConnection();
					AppLib.printDebug("No image for " + tm);
					InputStream is = conn.getInputStream();
					Worker.sendHTTP(out, HTTPConstants.HTTP_OK);
					sendInitialHeaders(out);
					out.print("Content-Type: image/jpg\r\n\r\n");
					out.flush();
					copyStream(is, os);
					is.close();
				} catch (Exception e2) {
					AppLib.debugException(e2);
					// true epic fail
					Worker.sendHTTP(out, HTTPConstants.HTTP_NOT_FOUND);
					sendInitialHeaders(out);
					out.print("\r\n");
				}
			}
		} else {
			Worker.sendHTTP(out, HTTPConstants.HTTP_NOT_FOUND);
			sendInitialHeaders(out);
			out.print("\r\n");
		}
	}
	/**
	 * Sends a team image or 404 if none.
	 * 
	 * @param url the image URL
	 * @param out the output writer
	 * @param os the output stream
	 */
	private void sendBigImage(String url, PrintWriter out, OutputStream os) {
		// parse get string and validate
		Map<String, String> params = Worker.parseGET(url.substring(6));
		if (params.containsKey("team")) {
			// validate team number and index
			String tm = params.get("team");
			if (!AppLib.positiveInteger(tm)) {
				Worker.sendHTTP(out, HTTPConstants.HTTP_NOT_FOUND);
				sendInitialHeaders(out);
				out.print("\r\n");
			} else try {
				// get the image
				FileInputStream is = new FileInputStream(new File(teams, tm + ".jpg"));
				AppLib.printDebug("Sending image for " + tm);
				Worker.sendHTTP(out, HTTPConstants.HTTP_OK);
				sendInitialHeaders(out);
				out.print("Content-Type: image/jpg\r\n\r\n");
				out.flush();
				copyStream(is, os);
				is.close();
			} catch (Exception e) {
				// true epic fail
				Worker.sendHTTP(out, HTTPConstants.HTTP_NOT_FOUND);
				sendInitialHeaders(out);
				out.print("\r\n");
			}
		} else {
			Worker.sendHTTP(out, HTTPConstants.HTTP_NOT_FOUND);
			sendInitialHeaders(out);
			out.print("\r\n");
		}
	}
	/**
	 * Copies one stream to another.
	 * 
	 * @param is the input stream
	 * @param os the output stream
	 */
	private void copyStream(InputStream is, OutputStream os) throws IOException {
		byte[] buffer = new byte[1024];
		int i;
		while (true) {
			// read from the input...
			i = is.read(buffer, 0, buffer.length);
			if (i < 0) break;
			// and write it right back to the output
			os.write(buffer, 0, i);
			os.flush();
		}
	}

	/**
	 * A class representing a web server session.
	 */
	private class Session {
		protected String id;
		protected UserData user;
		protected long lastActivity;

		public Session(String id, UserData user) {
			this.id = id;
			this.user = user;
			active();
		}
		public void active() {
			lastActivity = System.currentTimeMillis();
		}
	}
}