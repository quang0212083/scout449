package org.s449;

/*
 * Stephen Carlson
 * Montgomery Blair HS - Team 449
 * Scouting Data Collection Program
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.sound.sampled.*;
import java.text.*;
import org.s449.HotkeyList.Hotkey;
import org.s449.ui.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * The new combination, authenticated client for Scout449.
 * 
 * @author Stephen Carlson
 * @version 4.0.0
 */
public class Client extends JPanel implements Runnable {
	private static final long serialVersionUID = 0L;
	/**
	 * Default font name.
	 */
	private static final String fontName = "SansSerif";
	/**
	 * Default font size (in most of the program's main window).
	 */
	private static final int textFontSize = 16;
	/**
	 * Default font size (in the title).
	 */
	private static final int titleFontSize = 26;
	/**
	 * The text font.
	 */
	protected static final Font textFont = new Font(fontName, Font.PLAIN, textFontSize);
	/**
	 * The title font.
	 */
	protected static final Font titleFont = new Font(fontName, Font.BOLD, titleFontSize);
	/**
	 * Dimension to keep buttons at a size appropriate to the icon.
	 */
	private static final Dimension buttonSize = new Dimension(24, 24);
	/**
	 * The byte buffer for image uploading.
	 */
	private static final byte[] buffer = new byte[Constants.BULK_BUFFER];
	/**
	 * The size of the images to be uploaded. Be careful with this value;
	 *  setting it too high can make uploads prohibitively slow!
	 */
	protected static final Dimension imageSize = new Dimension(800, 600);
	/**
	 * The date formats for importing.
	 */
	private static final DateFormat[] formats = new DateFormat[] {
		new SimpleDateFormat("h:mm a"),
		new SimpleDateFormat("k:mm"),
		new SimpleDateFormat("MM/dd/yyyy h:mm a"),
		new SimpleDateFormat("MM/dd/yyyy k:mm"),
		new SimpleDateFormat("yyyy/MM/dd h:mm a"),
		new SimpleDateFormat("yyyy/MM/dd k:mm"),
		new SimpleDateFormat("MM/dd/yy h:mm a"),
		new SimpleDateFormat("MM/dd/yy k:mm")
	};

	/**
	 * The number of alliances to show in queue.
	 */
	private int showAlliance;
	/**
	 * Your team number goes here! Defaults to 449 (of course)
	 */
	private int myTeam = Constants.DEFAULT_TEAM;
	/**
	 * Data store.
	 */
	private Backend data;
	/**
	 * The bottom clock showing real time.
	 */
	private JLabel realClock;
	/**
	 * The team sorter box.
	 */
	private TeamList teamLister;
	/**
	 * Shared Calendar object with correct date and time.
	 */
	private long date;
	/**
	 * The comment viewer.
	 */
	private TeamViewer showComments;
	/**
	 * Offset for time.
	 */
	private long timeOffset;
	/**
	 * Previous match number.
	 */
	private int lastMatchNum;
	/**
	 * Previous time.
	 */
	private long lastTime;
	/**
	 * # of minutes between matches.
	 */
	private int spacing;
	/**
	 * Sorted array of teams updated by updateTeamList() method.
	 */
	private ArrayList<Team> teamsSorted;
	/**
	 * The team list always sorted by team # ascending.
	 */
	private ArrayList<Team> sortList;
	/**
	 * A backup array for updateTeamList().
	 */
	private ArrayList<Team> teamsSorted0;
	/**
	 * The visible backing queue of matches to come.
	 */
	private ArrayList<ScheduleItem> queue;
	/**
	 * The kiosk queue.
	 */
	private ArrayList<ScheduleItem> kQueue;
	/**
	 * Convenience alias to the hotkey list.
	 */
	private HotkeyList hotkeys;
	/**
	 * Match enterer.
	 */
	private MatchDialog matchDialog;
	/**
	 * Match logger.
	 */
	private MatchList matchList;
	/**
	 * The ticker.
	 */
	private AnalysisTicker ticker;
	/**
	 * The old match for editing.
	 */
	private ScheduleItem oldMatch;
	/**
	 * The port of the web server.
	 */
	private int webPort;
	/**
	 * Connection status.
	 */
	private StatusBox connStat;
	/**
	 * The background transfer proxy for web requests.
	 */
	private PortListener webTransfer;
	/**
	 * The refreshing thread.
	 */
	private LoaderThread refresher;
	/**
	 * The loading icon.
	 */
	private Loading load;
	/**
	 * Loading is stopped automatically when this time is reached.
	 */
	private long stopWhen;
	/**
	 * The scout status.
	 */
	private ScoutStatus status;
	/**
	 * The top clock showing countdown to next match.
	 */
	private CountdownClock mainClock;
	/**
	 * The kiosk clock showing countdown to next match.
	 */
	private CountdownClock kioskClock;
	/**
	 * Label showing the next team in the queue.
	 */
	private JLabel nextTeam;
	/**
	 * Label showing the next team in the queue.
	 */
	private JLabel kNextTeam;
	/**
	 * Label showing the next match in the queue.
	 */
	private JLabel nextMatch;
	/**
	 * The visual queue of teams to come.
	 */
	private JComponent queueList;
	/**
	 * The kiosk team list.
	 */
	private JLabel[] kTeamList;
	/**
	 * The buttons for each team.
	 */
	private JButton[] teamList;
	/**
	 * The running late button.
	 */
	private JButton runlate;
	/**
	 * The kiosk running late note.
	 */
	private JLabel kRunLate;
	/**
	 * The number of tabs.
	 */
	private int tabs;
	/**
	 * Does an item need to be loaded?
	 */
	private volatile boolean itemNeedsLoading;
	/**
	 * Closest match?
	 */
	private JButton closest;
	/**
	 * Was CAPS LOCK on?
	 */
	private boolean capsLockWasOn;
	/**
	 * CAPS LOCK warning.
	 */
	private JLabel clWarn;
	/**
	 * The edit buttons for each match.
	 */
	private JButton[] editButton;
	/**
	 * The delete buttons for each match.
	 */
	private JButton[] delButton;
	/**
	 * The clocks for each match.
	 */
	private JLabel[] clocks;
	/**
	 * The stars for each team.
	 */
	private StarRating[] stars;
	/**
	 * List of teams on alliance #1 (red alliance).
	 */
	private ArrayList<AllianceTeam> alliance1;
	/**
	 * Total team score on the left.
	 */
	private JTextField leftTotal;
	/**
	 * List of teams on alliance #2 (blue alliance).
	 */
	private ArrayList<AllianceTeam> alliance2;
	/**
	 * Total team score on the right.
	 */
	private JTextField rightTotal;
	/**
	 * Index of selected team.
	 */
	private int selectedIndex;
	/**
	 * Side of the said index.
	 */
	private boolean selectedSide;
	/**
	 * Label showing lead or tie.
	 */
	private JLabel result;
	/**
	 * A back-reference for inner classes.
	 */
	private Client me;
	/**
	 * Internal flag to handle notification of timer.
	 */
	private volatile boolean forceNotify;
	/**
	 * Event listener.
	 */
	private LocalEventListener events;
	/**
	 * The match list panel.
	 */
	private JComponent matchPanel;
	/**
	 * The kiosk pit window panel.
	 */
	private JPanel kencl;
	/**
	 * The pit window panel.
	 */
	private JPanel pencl;
	/**
	 * The scorekeeper panel.
	 */
	private JPanel sencl;
	/**
	 * The image uploader panel.
	 */
	private JPanel iencl;
	/**
	 * Select?
	 */
	private volatile boolean select;
	/**
	 * The match currently in scoring.
	 */
	private ScheduleItem toScore;
	/**
	 * The label at the top of the scorekeeper window.
	 */
	private JLabel scoreTop;
	/**
	 * Hotkey help label.
	 */
	private JLabel hkhelp;
	/**
	 * The index of the currently scored match.
	 */
	private int sIndex;
	/**
	 * Whether the user has changed a team or not.
	 */
	private boolean sDirty;
	/**
	 * Whether the user has entered a comment or not.
	 */
	private boolean cDirty;
	/**
	 * The scorekeeper skip ahead button.
	 */
	private JButton sAhead;
	/**
	 * The scorekeeper skip back button.
	 */
	private JButton sBack;
	/**
	 * The progress of the upload.
	 */
	private JProgressBar progress;
	/**
	 * The text box with the team number.
	 */
	private TeamTextField teamNum;
	/**
	 * The image to be sent.
	 */
	private BufferedImage toSend;
	/**
	 * The custom image directory.
	 */
	private JFileChooser customImage;
	/**
	 * The drives (roots) where pictures could be found.
	 */
	private JComponent drives;
	/**
	 * The loading dialog for reading.
	 */
	private Loading reading;
	/**
	 * The buttons for the drives.
	 */
	private JButton[] driveButtons;
	/**
	 * The list of drive roots.
	 */
	private File[] roots;
	/**
	 * The size of the file being sent.
	 */
	private int size;
	/**
	 * The rotation of the current image.
	 */
	private int rotate;
	/**
	 * The current image, unrotated.
	 */
	private Image unrotated;
	/**
	 * The JPEG files to be uploaded.
	 */
	private File[] files;
	/**
	 * The index of the current file.
	 */
	private int index;
	/**
	 * The current drive.
	 */
	private File current;
	/**
	 * The name of the file to be uploaded.
	 */
	private String upName;
	/**
	 * The image displayer.
	 */
	private JLabel img;
	/**
	 * The tab list.
	 */
	private JTabbedPane iTabs;
	/**
	 * The filename filter for JPEG files.
	 */
	private JpegFilter jpegs;
	/**
	 * "Until Match" clip for announcements.
	 */
	private Clip untilMatch;
	/**
	 * "Attention" clip for announcements.
	 */
	private Clip attention;
	/**
	 * "Red" clip for announcements.
	 */
	private Clip allRed;
	/**
	 * "Blue" clip for announcements.
	 */
	private Clip allBlue;
	/**
	 * "Alliance" clip for announcements.
	 */
	private Clip alliance;
	/**
	 * Sounds for time intervals.
	 */
	private Clip[] interval;
	/**
	 * Flag to stop multi-sound overlap.
	 */
	private volatile boolean soundPlaying;
	/**
	 * List of times before which to be warned of an upcoming match.
	 */
	private int[] warnBefore;
	/**
	 * The last height (used to generate gradient paints)
	 */
	private int lastHeight;
	/**
	 * The cached gradient paint.
	 */
	private GradientPaint paint;
	/**
	 * The six team labels next to the main clock.
	 */
	private JLabel[] firstTeams;
	/**
	 * The (kiosk) six team labels next to the main clock.
	 */
	private JLabel[] kFirstTeams;
	/**
	 * The queue delay. Similar to running late, but affects only this instance and
	 *  only the kiosks.
	 */
	private JTextField queueDelay;
	/**
	 * The actual queue delay.
	 */
	private int qd;
	/**
	 * Running?
	 */
	private boolean running;
	/**
	 * The current user.
	 */
	private UserData user;
	/**
	 * Show all matches?
	 */
	private JCheckBox allMatches;
	/**
	 * Quantitative scoring?
	 */
	private JRadioButton quan;
	/**
	 * Qualitative scoring?
	 */
	private JRadioButton qual;
	/**
	 * The quantitative panel.
	 */
	private JComponent quanPanel;
	/**
	 * The qualitative panel.
	 */
	private JComponent qualPanel;
	/**
	 * The parent of both Q panels.
	 */
	private JScrollPane qqParent;
	/**
	 * The match comment log.
	 */
	private LinkedList<TC> vList;
	/**
	 * The giant HTML field for the match comment log.
	 */
	private JTextPane vLog;
	/**
	 * The scroll pane for the log to auto goto the bottom.
	 */
	private JScrollPane vScroll;
	/**
	 * The kiosk window.
	 */
	private KioskWindow kiosk;
	/**
	 * The cached images.
	 */
	private HashMap<String, ImageData> cache;
	/**
	 * The MediaTracker to load images to be uploaded.
	 */
	private MediaTracker loader;
	/**
	 * The model for the image list.
	 */
	private ImageListModel model;
	/**
	 * The image list.
	 */
	private JList iList;
	/**
	 * The qualitative team list.
	 */
	private JButton[] qualTeams;
	/**
	 * The text field for team info entry.
	 */
	private JTextField qualEntry;
	/**
	 * The reconnect dialog.
	 */
	private JDialog rcDialog;

	/**
	 * Creates a new Scout449 client.
	 * 
	 * @param stat the ScoutStatus that is managing this object
	 */
	public Client(ScoutStatus stat) {
		status = stat;
	}
	/**
	 * Gets this client's data store.
	 * 
	 * @return the data store
	 */
	public Backend getData() {
		return data;
	}
	/**
	 * Gets whether the kiosk is open.
	 * 
	 * @return whether the kiosk is open
	 */
	public boolean isKioskShowing() {
		return running && kiosk.isShowing();
	}
	/**
	 * Shows or hides the kiosk.
	 * 
	 * @param toShow whether the kiosk should be visible
	 */
	public void showKiosk(boolean toShow) {
		if (running)
			kiosk.show(toShow);
	}
	/**
	 * Gets the current user.
	 * 
	 * @return the active user
	 */
	public UserData getUser() {
		return user;
	}
	/**
	 * Changes the current user, if not running.
	 * 
	 * @param user the new user information
	 */
	protected void setUser(UserData user) {
		if (!running) this.user = user;
	}
	/**
	 * Gets the web port.
	 * 
	 * @return the web port
	 */
	public int getWebPort() {
		return webPort;
	}
	/**
	 * Updates the list of teams (sorts it as a copy from the hash)
	 */
	private void updateTeamList() {
		AppLib.printDebug("Sorting team list");
		sortList.clear();
		sortList.addAll(data.getActive().getTeams().values());
		if (teamsSorted.size() != data.count()) {
			// rebuild
			teamsSorted.clear();
			teamsSorted.addAll(data.getActive().getTeams().values());
		} else {
			// in-line update
			Iterator<Team> it = teamsSorted.iterator();
			while (it.hasNext())
				teamsSorted0.add(data.get(it.next().getNumber()));
			teamsSorted.clear();
			teamsSorted.addAll(teamsSorted0);
			teamsSorted0.clear();
		}
		teamLister.updateList();
		if (user.canRead()) {
			ticker.setEvent(data.getActive());
			ticker.setWatchTeam(data.get(myTeam));
		}
		stopLoad();
	}
	/**
	 * Gets a list of teams. The sort order is by #, ascending.
	 * 
	 * @return a team list
	 */
	public java.util.List<Team> getTeamList() {
		return Collections.unmodifiableList(sortList);
	}
	/**
	 * Gets the list of teams as sorted in the team list window
	 * 
	 * @return the team list
	 */
	public java.util.List<Team> getVisualTeamList() {
		return teamsSorted;
	}
	/**
	 * Are you SURE that you want to erase everything and restart from the beginning?
	 */
	private void shutdown() {
		connStat.setText("Not Logged In");
		connStat.setStatus(StatusBox.STATUS_BAD);
		rcDialog.setVisible(false);
		showComments.reset();
		matchDialog.setVisible(false);
		data.close();
		teamsSorted.clear();
		if (webTransfer != null) webTransfer.close();
		running = false;
		status.setConnectionStatus(false);
	}
	/**
	 * Determines whether a match exists at the given time.
	 */
	private boolean matchAt(long time) {
		ScheduleItem match;
		synchronized (data.getSchedule()) {
			match = data.getSchedule().get(new SecondTime(time));
		}
		if (match != null)
			return !AppLib.confirm(status.getWindow(), "A match is already scheduled at " +
				ScheduleItem.timeFormat(time) + " on " + ScheduleItem.dateFormat(time) +
				".\n\nDo you want to over-write it with this match?");
		return false;
	}
	/**
	 * Shows the loading indicator.
	 */
	protected void load() {
		load.start();
		stopWhen = date + 3000L;
	}
	/**
	 * Hides the loading indicator.
	 */
	protected void stopLoad() {
		load.stop();
		stopWhen = 0L;
	}
	/**
	 * Establishes the connection to the server.
	 * 
	 * @param host the host to which to connect
	 */
	private void loadData() {
		String host = status.getRemoteHost();
		webPort = Constants.WEB_PORT;
		Object[] cfg;
		long serverTime = -1L;
		try {
			// attempt connection
			AppLib.printDebug("Opening web backend");
			WebBackend wb = new WebBackend(host, Constants.CONTROL_PORT, status);
			user = wb.auth(user);
			if (user == null) {
				status.sendMessage("autherr");
				return;
			}
			// configuration
			cfg = wb.getConfig();
			if (cfg.length > 1) {
				// server time
				if (cfg[0] != null && cfg[0] instanceof Long)
					serverTime = ((Long)cfg[0]).longValue();
				// web port
				if (cfg[1] != null && cfg[1] instanceof Integer)
					webPort = ((Integer)cfg[1]).intValue();
			}
			// success!
			AppLib.printDebug("Connection established");
			wb.flush();
			if (VolumeLister.versionCompare(Constants.VERSION_FULL, wb.getServerVersion()) < 0 &&
					!status.isServerRunning()
					&& AppLib.confirm(status.getWindow(), "A new version of Scout449 (" + 
					wb.getServerVersion() + ") is available.\nUpdate now?")) {
				// update Scout449!
				VolumeLister.invokeJVM("-jar updater.jar \"http://" + host + ":" + webPort
					+ "/scout449.jar\"");
				System.exit(0);
			}
			data = wb;
		} catch (Exception e) {
			AppLib.debugException(e);
			AppLib.printWarn(status.getWindow(), "Cannot connect to server. Suggestions:\n" +
				"1. Check the network connection.\n2. Ensure that the " +
				"server is running and supports version " + Constants.VERSION + " of the" +
				" program.\n3. Contact the server administrator.");
			status.showClient(false);
			return;
		}
		try {
			data.selfCheck();
		} catch (RuntimeException e) {
			AppLib.debugException(e);
			data.close();
			AppLib.printWarn(status.getWindow(), "Data failed to pass self-check. Suggestions:\n" +
				"1. Check the server configuration.\n2. Run in stand-alone mode.");
			status.showClient(false);
			return;
		}
		// read hotkeys
		DataStore myData = data.getData();
		hotkeys = myData.getHotkeys();
		myTeam = user.getTeamNum();
		// try fetching the time
		timeOffset = 0;
		date = System.currentTimeMillis();
		// server time
		if (cfg == null) {
			AppLib.printWarn(status.getWindow(), "Server is not responding. Suggestions:\n" +
				"1. Check the network connection.\n2. Ensure that the server is not overloaded.");
			status.showClient(false);
			return;
		}
		// synchronize server time
		if (serverTime > 0L)
			timeOffset = serverTime - System.currentTimeMillis();
		else timeOffset = 0L;
		if (!status.isTransferRunning() && !status.isServerRunning()) {
			webTransfer = new PortListener(host, webPort);
			webTransfer.start();
		}
		// data configuration (time, spacing)
		cfg = data.getData().getExtraData();
		if (cfg.length > 2) {
			// index 0 is the time configured. Kinda useless.
			if (cfg[1] != null && cfg[1] instanceof Integer)
				spacing = ((Integer)cfg[1]).intValue();
			if (cfg[2] != null && cfg[2] instanceof Integer) {
				int hrs = ((Integer)cfg[2]).intValue();
				boolean is24 = false;
				if (hrs == 12)
					is24 = false;
				else if (hrs == 24)
					is24 = true;
				ScheduleItem.is24 = is24;
			}
		}
		running = true;
		connStat.setStatus(StatusBox.STATUS_GOOD);
		connStat.setText(user.getRealName() + ", Team " + user.getTeamNum());
		status.setConnectionStatus(true);
	}
	/**
	 * Loads the schedule into the queue.
	 */
	private void loadSchedule() {
		synchronized (queue) {
			queue.clear();
			AppLib.printDebug("Loading schedule");
			synchronized (data.getSchedule()) {
				Iterator<ScheduleItem> it = data.getSchedule().values().iterator();
				while (it.hasNext())
					queue.add(it.next());
			}
		}
		updateListQueue();
		forceNotify = true;
	}
	/**
	 * Allows entry of a match.
	 */
	private void matchEntry() {
		if (!user.canScore()) return;
		matchDialog.setMatchNum(lastMatchNum + 1);
		oldMatch = null;
		if (lastTime > 0L)
			matchDialog.setTime(lastTime);
		else
			matchDialog.setTime(date);
		matchDialog.clearTeamFields();
		matchDialog.setVisible(true);
	}
	/**
	 * Finishes match entry. Do not call outside of appropriate place!
	 */
	private void doMatchEntry() {
		if (!user.canScore()) return;
		ArrayList<Integer> teams = new ArrayList<Integer>(ScheduleItem.TPA * 2 + 1);
		long time = 0;
		// first take in and validate teams
		java.util.List<Integer> nums = matchDialog.getTeams();
		if (nums == null) return;
		Iterator<Integer> it = nums.iterator();
		int num;
		while (it.hasNext()) {
			num = it.next();
			if (teams.contains(num)) {
				AppLib.printWarn(status.getWindow(), num + " is a duplicate.");
				return;
			}
			teams.add(num);
		}
		AppLib.printDebug("Finishing match entry");
		long newTime = matchDialog.getTime();
		if (newTime < 0L) return;
		Calendar local = Calendar.getInstance();
		local.setTimeInMillis(newTime);
		local.set(Calendar.SECOND, 0);
		local.set(Calendar.MILLISECOND, 0);
		time = local.getTimeInMillis();
		if (time / 1000L < date / 1000L) {
			if (!AppLib.confirm(status.getWindow(), "The match time that you have entered is " +
				"in the past.\n\nContinue entering the match anyway?"))
			return;
		} else if ((time / 1000L < data.getActive().getStartDate() / 1000L ||
			time / 1000L > data.getActive().getEndDate() / 1000L)) {
			if (!AppLib.confirm(status.getWindow(), "The match time that you have entered is " +
				"not during the current event.\nContinue entering the match anyway?"))
			return;
		}
		// set into seconds array
		if (matchAt(time)) return;
		// match number
		String matchNum = matchDialog.getMatchNum();
		if (matchNum == null || matchNum.length() < 1) lastMatchNum++;
		else try {
			int match = Integer.parseInt(matchNum);
			if ((match > 200 || match < 1) && !AppLib.confirm(status.getWindow(),
				"That match number looks wrong.\nContinue entering the match anyway?"))
				return;
			lastMatchNum = match;
		} catch (Exception e) {
			AppLib.printWarn(status.getWindow(), "Not a valid match number.");
			return;
		}
		lastTime = time + 60000 * spacing;
		// add to queue
		MatchLabel lbl = matchDialog.getLabel();
		if (lbl == null) lbl = MatchLabel.blank;
		ScheduleItem match = new ScheduleItem(nums, time, lbl.counts());
		match.setLabel(lbl);
		match.setNum(lastMatchNum);
		match.getSurrogate().or(matchDialog.getSurrogate());
		load();
		if (oldMatch != null) data.editMatch(oldMatch.getTime(), match);
		else data.addMatch(match);
		matchDialog.setVisible(false);
	}
	/**
	 * Bumps all matches forward or back.
	 */
	private boolean late() {
		if (!user.canScore()) return false;
		String newTime = JOptionPane.showInputDialog(this, "How many minutes late is FIRST?",
			"Running Late?", JOptionPane.WARNING_MESSAGE);
		if (newTime == null) return false;
		requestFocus();
		try {
			load();
			data.runLate(Integer.parseInt(newTime));
			return true;
		} catch (Exception e) {
			AppLib.printWarn(status.getWindow(), "Invalid offset: must be an integer.");
			return false;
		}
	}
	/**
	 * Edits the match time of the given match.
	 * 
	 * @param match the match to edit
	 */
	protected void editQueue(ScheduleItem match) {
		if (!user.canScore() || match == null) return;
		matchDialog.setMatch(match);
		oldMatch = match;
		matchDialog.setVisible(true);
	}
	/**
	 * Removes the specified match.
	 * 
	 * @param match the match to remove
	 */
	protected void removeQueue(ScheduleItem match) {
		if (!user.canScore() || match == null) return;
		load();
		data.delMatch(match);
	}
	/**
	 * Gets the client time.
	 * 
	 * @return the client time
	 */
	public long getTime() {
		return date;
	}
	/**
	 * Gets whether an item should be shown on the pit window screen.
	 * 
	 * @param item the item to test
	 * @return whether it should be shown
	 */
	private boolean shouldShow(ScheduleItem item) {
		return item.getStatus() != ScheduleItem.COMPLETE &&
			item.getTime() + (data.getData().minutesLate() - qd) * 60000L > date
			&& (allMatches.isSelected() || item.getTeams().indexOf(myTeam) >= 0);
	}
	/**
	 * Runs the timer countdown loop of the program.
	 */
	public void run() {
		long newDate, delta, oldDate = 0;
		forceNotify = false;
		AppLib.printDebug("Starting main loop");
		String str, title = null, mm = null, str2; Color col, col2;
		Iterator<ScheduleItem> it; ScheduleItem item, firstItem = null;
		int i, index; boolean caps;
		while (running) {
			newDate = System.currentTimeMillis();
			// synchronize - as few as possible updates
			if (newDate / 1000 != oldDate / 1000 || forceNotify) {
				// set the real clock
				date = newDate + timeOffset;
				realClock.setText(ScheduleItem.timeFormat(date));
				oldDate = newDate;
				col = Constants.DARK_BLUE;
				col2 = Constants.WHITE;
				firstItem = null;
				title = "";
				mm = "queue is empty";
				synchronized (queue) {
					it = queue.iterator();
					i = 0;
					while (it.hasNext()) {
						item = it.next();
						index = item.getTeams().indexOf(myTeam);
						if (shouldShow(item)) {
							if (index >= 0 && firstItem == null) {
								// set up the coming up title
								firstItem = item;
								if (index < ScheduleItem.TPA) {
									title = "Red";
									col = Constants.RED;
									col2 = Constants.LIGHT_RED;
								} else {
									title = "Blue";
									col = Constants.BLUE;
									col2 = Constants.LIGHT_BLUE;
								}
								title = "Next Match: " + item.getLabel() +
									" " + item.getNum() + " on " + title;
								mm = "until " + item.getLabel() + " " + item.getNum();
							}
							// individual clocks
							if (i < clocks.length) {
								delta = (item.getTime() - date + (data.getData().minutesLate() -
									qd) * 60000L) / 1000L;
								str = Long.toString(delta / 3600L);
								if (str.length() < 2) str = "0" + str;
								str2 = Long.toString((delta % 3600L / 60L) % 60L);
								if (str2.length() < 2) str2 = "0" + str2;
								str += ":" + str2;
								str2 = Long.toString(delta % 60L);
								if (str2.length() < 2) str2 = "0" + str2;
								str += ":" + str2;
								clocks[i].setText(str);
								i++;
							}
						}
					}
					setDisplay(title);
					kiosk.setDisplay(title);
					nextMatch.setText(mm);
					nextTeam.setForeground(col);
					kNextTeam.setForeground(col);
					nextMatch.setForeground(col2);
					// update the big display and warning beeps
					if (firstItem != null) {
						for (i = 0; i < ScheduleItem.TPA * 2; i++) {
							str = Integer.toString(firstItem.getTeams().get(i));
							firstTeams[i].setText(str);
							kFirstTeams[i].setText(str);
						}
						delta = (firstItem.getTime() - date + (data.getData().minutesLate() -
							qd) * 60000L) / 1000L;
						// handle sound
						for (i = 0; i < warnBefore.length; i++)
							if (delta == warnBefore[i])
								if (untilMatch == null || attention == null || alliance == null ||
										interval[i] == null || allRed == null || allBlue == null)
									Toolkit.getDefaultToolkit().beep();
								else
									new SoundPlayerThread(i, firstItem.getTeams().indexOf(myTeam) >=
										ScheduleItem.TPA).start();
						mainClock.setSeconds((int)(delta % 60L));
						mainClock.setMinutes((int)(delta / 60L % 60L));
						mainClock.setHours((int)(delta / 3600L % 100L));
						kioskClock.setSeconds((int)(delta % 60L));
						kioskClock.setMinutes((int)(delta / 60L % 60L));
						kioskClock.setHours((int)(delta / 3600L % 100L));
					} else {
						mainClock.setSeconds(0);
						mainClock.setMinutes(0);
						mainClock.setHours(0);
						kioskClock.setSeconds(0);
						kioskClock.setMinutes(0);
						kioskClock.setHours(0);
						for (i = 0; i < ScheduleItem.TPA * 2; i++) {
							firstTeams[i].setText("---");
							kFirstTeams[i].setText("---");
						}
					}
					mainClock.repaint();
					if (kiosk.isShowing()) kiosk.repaint();
				}
				oldDate = newDate;
				forceNotify = false;
			}
			if (user.canRead()) ticker.tick();
			if (newDate / 1000 % 5 == 0) updateDrives();
			while (!status.isConnected()) {
				// try a reconnect
				data.flush();
				if (data.getData() != null) {
					// go!
					loadSchedule();
					updateTeamList();
					status.setConnectionStatus(true);
					rcDialog.setVisible(false);
					break;
				}
				AppLib.sleep(1000L);
			}
			try {
				caps = AppLib.winInfo.getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
			} catch (Exception e) { caps = false; }
			if (caps && !capsLockWasOn)
				clWarn.setVisible(true);
			else if (!caps && capsLockWasOn)
				clWarn.setVisible(false);
			capsLockWasOn = caps;
			if (status.isConnected() && data.updateCheck()) {
				AppLib.printDebug("Server update");
				loadSchedule();
				updateTeamList();
				status.setConnectionStatus(true);
			}
			if (stopWhen > 0 && stopWhen < date)
				stopLoad();
			if (select) {
				switch (iTabs.getSelectedIndex()) {
				case 0:
					requestFocus();
					break;
				case 1:
					requestFocus();
					break;
				case 2:
					teamLister.focus();
					break;
				case 3:
					sFocus();
					break;
				case 4:
					teamNum.requestFocus();
					break;
				default:
				}
				select = false;
			}
			AppLib.sleep(20L);
		}
	}
	/**
	 * Changes the next team on the big display.
	 * 
	 * @param name the name of the team to show
	 */
	private void setDisplay(String name) {
		if (name.length() > 0)
			nextTeam.setText(name);
		else
			nextTeam.setText("Queue Empty");
	}
	/**
	 * Runs initialization code and starts program.
	 */
	protected void start() {
		if (running) return;
		select = itemNeedsLoading = capsLockWasOn = false;
		lastMatchNum = rotate = 0;
		spacing = 6;
		index = 0;
		files = null;
		ScheduleItem.is24 = true;
		lastTime = 0L;
		qd = 0;
		stopLoad();
		loadData();
		if (!status.isClientShowing()) return;
		updateUserUI();
		loadSchedule();
		gotoClosest();
		loadItem();
		select(1);
		updateTeamList();
		showComments.init();
		matchList.init();
		emptyLog();
		AppLib.sleep(200L);
		new Thread(this).start();
		if (!user.canRead()) kiosk.show(true);
	}
	/**
	 * Initializes the client (first-time start)
	 */
	protected void init() {
		// prevent re-init
		if (queue != null || running) return;
		AppLib.printDebug("Initializing client");
		queue = new ArrayList<ScheduleItem>(80);
		kQueue = new ArrayList<ScheduleItem>(showAlliance + 1);
		teamsSorted = new ArrayList<Team>(80);
		teamsSorted0 = new ArrayList<Team>(80);
		sortList = new ArrayList<Team>(80);
		cache = new HashMap<String, ImageData>(100);
		hotkeys = new HotkeyList();
		me = this;
		forceNotify = false;
		lastHeight = -1;
		soundPlaying = false;
		showAlliance = 5;
		warnBefore = new int[] { 300, 600, 1800 };
		events = new LocalEventListener();
		status.addActionListener(events);
		alliance1 = new ArrayList<AllianceTeam>(6);
		alliance2 = new ArrayList<AllianceTeam>(6);
		selectedIndex = sIndex = 0; selectedSide = false;
		kiosk = new KioskWindow();
		running = false;
		index = 0;
		files = null;
		vList = new LinkedList<TC>();
		refresher = new LoaderThread();
		refresher.start();
		setupUI();
		loader = new MediaTracker(this);
		AppLib.printDebug("Loading sounds");
		untilMatch = loadAudioClip("wav/until_match.wav");
		attention = loadAudioClip("wav/attention.wav");
		alliance = loadAudioClip("wav/alliance.wav");
		allRed = loadAudioClip("wav/red.wav");
		allBlue = loadAudioClip("wav/blue.wav");
		interval = new Clip[] {
			loadAudioClip("wav/minute5.wav"),
			loadAudioClip("wav/minute10.wav"),
			loadAudioClip("wav/minute30.wav")
		};
	}
	/**
	 * Loads an audio clip.
	 */
	private Clip loadAudioClip(String fileName) {
		try {
			// go
			Clip output = AudioSystem.getClip();
			output.open(AudioSystem.getAudioInputStream(new File(fileName)));
			return output;
		} catch (Exception e) {
			// no go
			return null;
		}
	}
	/**
	 * Sets up the user interface (tabs).
	 */
	private void setupUI() {
		// initial setup
		AppLib.printDebug("Setting up UI");
		setLayout(new BorderLayout());
		addKeyListener(new UIKeyListener());
		// each method in turn
		setOpaque(false);
		setupPitUI();
		setupScoreUI();
		setupImageUI();
		setupMatchUI();
		// connection
		connStat = new StatusBox("Not Logged In", StatusBox.STATUS_BAD);
		connStat.setOpaque(false);
		connStat.setForeground(Constants.WHITE);
		// loading indicator
		load = new Loading();
		load.setText("");
		stopLoad();
		// the top (logo, clocks...)
		JComponent vert = new JPanel(new VerticalFlow(true));
		vert.setOpaque(false);
		JComponent pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		pnl.setOpaque(false);
		pnl.add(reading);
		pnl.add(connStat);
		pnl.add(load);
		vert.add(pnl);
		// logo
		pnl = new Box(BoxLayout.X_AXIS);
		pnl.setOpaque(false);
		JLabel logo = new JLabel(status.getIcon("scout449"));
		pnl.add(logo);
		nextMatch = new AntialiasedJLabel("until next match");
		nextMatch.setForeground(Constants.WHITE);
		nextMatch.setHorizontalAlignment(SwingConstants.CENTER);
		// until next match
		mainClock = new CountdownClock(status, 32);
		realClock = new AntialiasedJLabel();
		realClock.setForeground(Constants.WHITE);
		realClock.setFont(textFont);
		pnl.add(Box.createHorizontalStrut(20));
		pnl.add(Box.createHorizontalGlue());
		pnl.add(module(mainClock, nextMatch));
		pnl.add(Box.createHorizontalStrut(5));
		// next teams
		firstTeams = new JLabel[ScheduleItem.TPA * 2];
		JLabel lbl;
		JPanel v2 = new JPanel(new GridLayout(2, 0, 3, 2));
		v2.setOpaque(false);
		v2.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(Constants.WHITE),
			BorderFactory.createEmptyBorder(5, 10, 5, 0)));
		lbl = new AntialiasedJLabel("red   ");
		lbl.setForeground(Constants.RED);
		v2.add(lbl);
		// red
		for (int i = 0; i < ScheduleItem.TPA; i++) {
			lbl = new AntialiasedJLabel("---");
			lbl.setForeground(Constants.WHITE);
			firstTeams[i] = lbl;
			v2.add(lbl);
		}
		lbl = new AntialiasedJLabel("blue   ");
		lbl.setForeground(Constants.BLUE);
		v2.add(lbl);
		// blue
		for (int i = 0; i < ScheduleItem.TPA; i++) {
			lbl = new AntialiasedJLabel("---");
			lbl.setForeground(Constants.WHITE);
			firstTeams[i + ScheduleItem.TPA] = lbl;
			v2.add(lbl);
		}
		v2.setMaximumSize(v2.getPreferredSize());
		pnl.add(v2);
		// run late
		runlate = ButtonFactory.getButton("on time", "late", events, -1);
		runlate.setForeground(Constants.WHITE);
		runlate.setOpaque(false);
		runlate.setBorder(BorderFactory.createEmptyBorder());
		runlate.setHorizontalAlignment(SwingConstants.CENTER);
		// real clock
		pnl.add(Box.createHorizontalGlue());
		pnl.add(module(realClock, runlate));
		pnl.add(Box.createHorizontalStrut(5));
		vert.add(pnl);
		add(vert, BorderLayout.NORTH);
		// complete tab list
		pnl = new Box(BoxLayout.X_AXIS);
		pnl.setOpaque(false);
		iTabs = new JTabbedPane();
		iTabs.setUI(new STabbedPaneUI());
		iTabs.setOpaque(false);
		iTabs.addChangeListener(new EventListener());
		pnl.add(Box.createHorizontalStrut(5));
		pnl.add(iTabs);
		pnl.add(Box.createHorizontalStrut(5));
		add(pnl, BorderLayout.CENTER);
		// init clocks
		realClock.setText(ScheduleItem.timeFormat(date));
		// the analysis ticker
		ticker = new AnalysisTicker(status);
		ticker.setForeground(Constants.BLACK);
		ticker.setFont(textFont);
		ticker.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		// finish the panel!
		ticker.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		add(ticker, BorderLayout.SOUTH);
		showComments = new TeamViewer(status);
		// reconnect now?
		rcDialog = new JDialog(status.getWindow(), "Waiting", true);
		rcDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		Container c = rcDialog.getContentPane();
		c.setLayout(new VerticalFlow(false, 10, 10));
		lbl = new JLabel("Waiting for server to respond...");
		lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		c.add(lbl);
		c.add(Box.createVerticalStrut(10));
		JButton btn = new JButton("Stop Waiting and Disconnect");
		btn.setFocusable(false);
		btn.setActionCommand("disconn");
		btn.addActionListener(events);
		btn.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		c.add(btn);
		rcDialog.pack();
		// center dialog
		Dimension d = AppLib.winInfo.getScreenSize();
		int w = rcDialog.getWidth();
		int h = rcDialog.getHeight();
		rcDialog.setBounds((d.width - w) / 2, (d.height - h) / 2, w, h);
	}
	/**
	 * Initializes the tabs.
	 */
	private void setupTabs() {
		tabs = 1;
		iTabs.removeAll();
		iTabs.addTab("kiosk", null, pencl, "Upcoming Match List");
		iTabs.setMnemonicAt(0, KeyEvent.VK_K);
		if (user.canRead()) {
			tabs = 3;
			// matches and teams
			iTabs.addTab("matches", null, matchPanel, "List of Matches");
			iTabs.setMnemonicAt(1, KeyEvent.VK_M);
			iTabs.addTab("teams", null, teamLister, "List of Teams");
			iTabs.setMnemonicAt(2, KeyEvent.VK_T);
			if (user.canScore()) {
				tabs = 5;
				iTabs.addTab("scoring", null, sencl, "Score a Match");
				iTabs.setMnemonicAt(3, KeyEvent.VK_S);
				iTabs.addTab("images", null, iencl, "Upload Team Images");
				iTabs.setMnemonicAt(4, KeyEvent.VK_I);
			}
		}
		validate();
	}
	/**
	 * Returns a two-component vertical module.
	 * 
	 * @param top the component on the top
	 * @param bot the component on the bottom
	 * @return the module
	 */
	private JComponent module(JComponent top, JComponent bot) {
		// vertical panel
		JComponent panel = new Box(BoxLayout.Y_AXIS);
		panel.setOpaque(false);
		// add all
		top.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		bot.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		panel.add(top);
		panel.add(bot);
		return panel;
	}
	public void paint(Graphics g) {
		int h = getHeight();
		if (lastHeight != h) {
			paint = new GradientPaint(0, 0, Constants.PURPLE, 0, h, Constants.WHITE);
			lastHeight = h;
		}
		Graphics2D g2d = (Graphics2D)g;
		g2d.setPaint(paint);
		g2d.fillRect(0, 0, getWidth(), getHeight());
		paintChildren(g);
	}
	public void update(Graphics g) {
		paint(g);
	}
	/**
	 * Sets up the match list tab UI.
	 */
	private void setupMatchUI() {
		matchPanel = new JPanel(new BorderLayout());
		matchPanel.setBackground(Constants.WHITE);
		JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 5));
		pnl.setOpaque(false);
		pnl.add(ButtonFactory.getButton("Import Matches", "imp", events, -1));
		pnl.add(ButtonFactory.getButton("Enter Match", "match", events, KeyEvent.VK_E));
		matchPanel.add(pnl, BorderLayout.NORTH);
	}
	/**
	 * Sets quantitative/qualitative accordingly.
	 * 
	 * @param which true for quan, false for qual
	 */
	private void qq(boolean which) {
		AppLib.printDebug("Switching view to " + (which ? "Quan" : "Qual"));
		quan.setSelected(which);
		qual.setSelected(!which);
		if (which)
			qqParent.setViewportView(quanPanel);
		else
			qqParent.setViewportView(qualPanel);
		sencl.validate();
		sencl.repaint();
		AppLib.sleep(1L);
		sFocus();
	}
	/**
	 * Empties the qualitative log.
	 */
	private void emptyLog() {
		qualEntry.setBackground(Constants.WHITE);
		qualEntry.setText("");
		vLog.setText("");
		vList.clear();
		cDirty = false;
	}
	/**
	 * Sets the focus to the correct scorekeeper item.
	 */
	private void sFocus() {
		if (quan.isSelected())
			requestFocus();
		else
			qualEntry.requestFocus();
	}
	/**
	 * Qualitative entry.
	 */
	private void qualEntry() {
		if (!qual.isSelected() || iTabs.getSelectedIndex() != 3 || toScore == null ||
			!user.canScore()) return;
		qualEntry.setBackground(Constants.WHITE);
		String text = Server.htmlspecial(qualEntry.getText());
		if (text == null || (text = text.trim()).length() < 1) return;
		// strip "Team"
		String lc = text.toLowerCase();
		if (lc.startsWith("team"))
			text = text.substring(4).trim();
		// strip #
		if (text.startsWith("#"))
			text = text.substring(1).trim();
		int teamNum = 0;
		// now the #
		String num = "";
		while (Character.isDigit(text.charAt(0)) && text.length() > 0) {
			num += text.charAt(0);
			text = text.substring(1);
		}
		text = text.trim();
		if (text.length() < 1) return;
		try {
			teamNum = Integer.parseInt(num);
			if (toScore.getTeams().indexOf(teamNum) < 0) {
				qualEntry.setBackground(Constants.LIGHT_RED);
				return;
			}
		} catch (Exception e) {
			qualEntry.setBackground(Constants.LIGHT_RED);
			return;
		}
		qualEntry.setText("");
		// strip ":"
		if (text.startsWith(":"))
			text = text.substring(1).trim();
		lc = text.toLowerCase();
		// strip "'s"
		if (lc.startsWith("'s")) {
			text = text.substring(2).trim();
			lc = text.toLowerCase();
		}
		// strip "is"
		if (lc.startsWith("is"))
			text = text.substring(2).trim();
		AppLib.printDebug("Appending " + text + " for " + teamNum);
		vList.add(new TC(teamNum, text));
		Document doc = vLog.getDocument();
		try {
			vLog.getEditorKit().read(new StringReader("<b>" + teamNum + "</b>: " + text),
				doc, doc.getLength());
			vLog.repaint();
			vLog.setCaretPosition(doc.getLength());
			cDirty = true;
		} catch (Exception e) {
			AppLib.debugException(e);
		}
	}
	/**
	 * Updates the user UI.
	 */
	private void updateUserUI() {
		queueDelay.setText("0");
		allMatches.setSelected(false);
		qq(false);
		matchDialog = new MatchDialog(status);
		matchDialog.setActionListener(events);
		matchDialog.setMatchNum(1);
		teamLister = new TeamList(status);
		teamLister.setCommentViewer(showComments);
		ticker.setVisible(user.canRead());
		clWarn.setVisible(false);
		matchList = new MatchList(status);
		JScrollPane pane = new JScrollPane(matchList);
		pane.setBorder(ButtonFactory.getThinBorder());
		pane.getViewport().setBackground(Constants.WHITE);
		matchPanel.add(pane, BorderLayout.CENTER);
		setupTabs();
		NumericOnlyListener nl;
		scoreTop.setText("No Match");
		// actual boxes
		int i; AllianceTeam tm; JComponent scp; JTextField score;
		JTextField[] scoreList = null; Hotkey item;
		Iterator<Hotkey> it = null;
		for (i = 0; i < ScheduleItem.TPA; i++) {
			nl = new NumericOnlyListener(false, i);
			// scoring panel
			tm = alliance1.get(i);
			scp = tm.panel;
			scp.removeAll();
			if (data.getData().isAdvScore()) {
				it = hotkeys.getList().iterator();
				scoreList = new JTextField[hotkeys.size()];
				for (int j = 0; j < hotkeys.size() && it.hasNext(); j++) {
					item = it.next();
					score = new JTextField(2);
					score.setText("0");
					score.addKeyListener(nl);
					score.setMaximumSize(score.getPreferredSize());
					scoreList[j] = score;
					// strut, label, space, score
					scp.add(getScoringBox(score, item.toString()));
					scp.add(Box.createVerticalStrut(2));
				}
				tm.scores = scoreList;
				tm.total.setVisible(true);
			} else
				tm.total.setVisible(false);
			score = new JTextField(2);
			score.setText("0");
			score.addFocusListener(TextSelector.INSTANCE);
			score.addKeyListener(nl);
			score.setMaximumSize(score.getPreferredSize());
			tm.pen = score;
			scp.add(getScoringBox(score, "X: Penalty (-10)"));
			scp.setVisible(false);
			tm.rescore();
			// ---------------------------------
			nl = new NumericOnlyListener(false, i + ScheduleItem.TPA);
			scoreList = new JTextField[hotkeys.size()];
			// scoring panel
			tm = alliance2.get(i);
			scp = tm.panel;
			scp.removeAll();
			if (data.getData().isAdvScore()) {
				it = hotkeys.getList().iterator();
				for (int j = 0; j < hotkeys.size(); j++) {
					item = it.next();
					score = new JTextField(2);
					score.setText("0");
					score.addKeyListener(nl);
					score.setMaximumSize(score.getPreferredSize());
					scoreList[j] = score;
					// strut, label, space, score
					scp.add(getScoringBox(score, item.toString()));
					scp.add(Box.createVerticalStrut(2));
				}
				tm.scores = scoreList;
				tm.total.setVisible(true);
			} else
				tm.total.setVisible(false);
			score = new JTextField(2);
			score.setText("0");
			score.addFocusListener(TextSelector.INSTANCE);
			score.addKeyListener(nl);
			score.setMaximumSize(score.getPreferredSize());
			tm.pen = score;
			scp.add(getScoringBox(score, "X: Penalty (-10)"));
			scp.setVisible(false);
			tm.rescore();
		}
		// result
		result.setText("Tie Game");
		// hotkey help!
		hkhelp.setText(hotkeys.toString());
		validate();
		repaint();
		requestFocus();
	}
	/**
	 * Gets a scoring panel box.
	 * 
	 * @param score the scoring field
	 * @param label the scoring label
	 * @return the box
	 */
	private JComponent getScoringBox(JTextField score, String label) {
		// set up horizontal for penalties
		JComponent horiz = new Box(BoxLayout.X_AXIS);
		horiz.setOpaque(false);
		horiz.add(Box.createHorizontalStrut(15));
		JLabel jl = new AntialiasedJLabel(label);
		jl.setFont(textFont);
		jl.setForeground(Constants.DARK_BLUE);
		horiz.add(jl);
		horiz.add(Box.createHorizontalGlue());
		jl = new AntialiasedJLabel("x ");
		jl.setFont(textFont);
		jl.setForeground(Constants.DARK_BLUE);
		horiz.add(jl);
		horiz.add(score);
		horiz.add(Box.createHorizontalStrut(15));
		return horiz;
	}
	/**
	 * Initializes the scorekeeper UI.
	 */
	private void setupScoreUI() {
		quan = new JRadioButton("Scoring");
		quan.setActionCommand("qq");
		quan.setSelected(false);
		quan.setOpaque(false);
		quan.setFocusable(false);
		quan.addActionListener(events);
		qual = new JRadioButton("Comments");
		qual.setOpaque(false);
		qual.setActionCommand("qq");
		qual.setSelected(true);
		qual.setFocusable(false);
		qual.addActionListener(events);
		NumericOnlyListener nl = new NumericOnlyListener(false, -1);
		NumericOnlyListener nlt = new NumericOnlyListener(true, -1);
		ButtonGroup group = new ButtonGroup();
		group.add(quan);
		group.add(qual);
		sencl = new JPanel(new BorderLayout());
		scoreTop = new AntialiasedJLabel("No Match");
		// left panel of teams
		JComponent leftPanel = new JPanel(new VerticalFlow(true));
		leftPanel.setOpaque(false);
		leftPanel.setBorder(BorderFactory.createTitledBorder(ButtonFactory.getThinBorder(),
			"Red Alliance", TitledBorder.LEFT, TitledBorder.TOP, null, Constants.RED));
		leftTotal = new JTextField(2);
		leftTotal.setMaximumSize(leftTotal.getPreferredSize());
		leftTotal.setText("0");
		leftTotal.addFocusListener(TextSelector.INSTANCE);
		leftTotal.addKeyListener(nl);
		// right panel of teams
		JComponent rightPanel = new JPanel(new VerticalFlow(true));
		rightPanel.setOpaque(false);
		rightPanel.setBorder(BorderFactory.createTitledBorder(ButtonFactory.getThinBorder(),
			"Blue Alliance", TitledBorder.LEFT, TitledBorder.TOP, null, Constants.BLUE));
		rightTotal = new JTextField(2);
		rightTotal.setMaximumSize(rightTotal.getPreferredSize());
		rightTotal.setText("0");
		rightTotal.addFocusListener(TextSelector.INSTANCE);
		rightTotal.addKeyListener(nl);
		qualTeams = new JButton[2 * ScheduleItem.TPA];
		// actual boxes
		int i = 0;
		JComponent row, scp, vert;
		JLabel title; JButton but; JTextField score;
		ReplaceListener rl;
		JPanel qTeams = new JPanel(new GridLayout(0, 2, 40, 0));
		qTeams.setOpaque(false);
		title = new JLabel("Red Alliance");
		title.setHorizontalAlignment(SwingConstants.RIGHT);
		title.setForeground(Constants.RED);
		qTeams.add(title);
		title = new JLabel("Blue Alliance");
		title.setHorizontalAlignment(SwingConstants.LEFT);
		title.setForeground(Constants.BLUE);
		qTeams.add(title);
		for (i = 0; i < ScheduleItem.TPA; i++) {
			nl = new NumericOnlyListener(false, i);
			rl = new ReplaceListener(i, false);
			// left side
			row = new Box(BoxLayout.X_AXIS);
			row.setOpaque(false);
			// team name
			but = ButtonFactory.getButton("No Entrant", "red", rl, -1);
			but.setOpaque(false);
			but.setContentAreaFilled(false);
			but.setBorder(BorderFactory.createEmptyBorder());
			but.setForeground(Constants.DARK_BLUE);
			but.setFont(titleFont);
			// scoring panel
			scp = new JPanel(new VerticalFlow(true));
			scp.setOpaque(false);
			// total field
			score = new JTextField(2);
			score.setText("0");
			score.addFocusListener(TextSelector.INSTANCE);
			score.addKeyListener(nlt);
			score.addFocusListener(nl);
			score.setMaximumSize(score.getPreferredSize());
			// replace button
			row.add(ButtonFactory.getButton(status.getIcon("edit"),
				"edit", rl, buttonSize));
			row.add(Box.createHorizontalStrut(1));
			// delete button
			row.add(ButtonFactory.getButton(status.getIcon("delete"),
				"del", rl, buttonSize));
			row.add(Box.createHorizontalStrut(5));
			// the rest
			row.add(but);
			row.add(Box.createHorizontalGlue());
			row.add(score);
			leftPanel.add(row);
			leftPanel.add(scp);
			scp.setVisible(false);
			alliance1.add(new AllianceTeam(0, but, null, scp, score, null));
			// ---------------------------------
			nl = new NumericOnlyListener(false, i + ScheduleItem.TPA);
			rl = new ReplaceListener(i, true);
			// right side
			row = new Box(BoxLayout.X_AXIS);
			row.setOpaque(false);
			// team name
			but = ButtonFactory.getButton("No Entrant", "blue", rl, -1);
			but.setOpaque(false);
			but.setContentAreaFilled(false);
			but.setBorder(BorderFactory.createEmptyBorder());
			but.setForeground(Constants.DARK_BLUE);
			but.setFont(titleFont);
			// scoring panel
			scp = new JPanel(new VerticalFlow(true));
			scp.setOpaque(false);
			// total field
			score = new JTextField(2);
			score.setText("0");
			score.addFocusListener(TextSelector.INSTANCE);
			score.addKeyListener(nlt);
			score.addFocusListener(nl);
			score.setMaximumSize(score.getPreferredSize());
			// replace button
			row.add(ButtonFactory.getButton(status.getIcon("edit"),
				"edit", rl, buttonSize));
			row.add(Box.createHorizontalStrut(1));
			// delete button
			row.add(ButtonFactory.getButton(status.getIcon("delete"),
				"del", rl, buttonSize));
			row.add(Box.createHorizontalStrut(5));
			// the rest
			row.add(but);
			row.add(Box.createHorizontalGlue());
			row.add(score);
			rightPanel.add(row);
			rightPanel.add(scp);
			scp.setVisible(false);
			alliance2.add(new AllianceTeam(0, but, null, scp, score, null));
			// qualitative
			but = ButtonFactory.getButton("No Entrant", "red", rl, -1);
			but.setForeground(Constants.DARK_BLUE);
			but.setOpaque(false);
			but.setContentAreaFilled(false);
			but.setBorder(BorderFactory.createEmptyBorder());
			but.setFont(titleFont);
			but.setHorizontalAlignment(SwingConstants.RIGHT);
			qualTeams[i] = but;
			qTeams.add(but);
			but = ButtonFactory.getButton("No Entrant", "blue", rl, -1);
			but.setForeground(Constants.DARK_BLUE);
			but.setOpaque(false);
			but.setContentAreaFilled(false);
			but.setBorder(BorderFactory.createEmptyBorder());
			but.setFont(titleFont);
			but.setHorizontalAlignment(SwingConstants.LEFT);
			qualTeams[i + ScheduleItem.TPA] = but;
			qTeams.add(but);
		}
		// finish off this step
		leftPanel.add(Box.createVerticalStrut(10));
		leftPanel.add(Box.createHorizontalStrut(460));
		row = new Box(BoxLayout.X_AXIS);
		row.setOpaque(false);
		// total red
		title = new AntialiasedJLabel("Total Red");
		title.setForeground(Constants.DARK_BLUE);
		title.setFont(textFont);
		row.add(title);
		row.add(Box.createHorizontalGlue());
		row.add(leftTotal);
		leftPanel.add(row);
		// right side
		rightPanel.add(Box.createVerticalStrut(10));
		rightPanel.add(Box.createHorizontalStrut(460));
		row = new Box(BoxLayout.X_AXIS);
		row.setOpaque(false);
		// total blue
		title = new AntialiasedJLabel("Total Blue");
		title.setForeground(Constants.DARK_BLUE);
		title.setFont(textFont);
		row.add(title);
		row.add(Box.createHorizontalGlue());
		row.add(rightTotal);
		rightPanel.add(row);
		// caps lock warning
		clWarn = new JLabel("Caps Lock is on.");
		clWarn.setFont(clWarn.getFont().deriveFont(Font.BOLD));
		clWarn.setForeground(Constants.BLACK);
		clWarn.setBackground(Constants.LIGHT_RED);
		clWarn.setOpaque(true);
		clWarn.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		// add them all
		vert = new JPanel(new VerticalFlow(false));
		vert.setOpaque(false);
		row = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 5));
		row.setOpaque(false);
		// skip back
		sBack = ButtonFactory.getButton(status.getIcon("back"),
			"prev", events, null);
		sBack.setMnemonic(KeyEvent.VK_Z);
		row.add(sBack);
		// load from schedule
		JButton load = ButtonFactory.getButton(status.getIcon("refresh"), "load", events, null);
		load.setMnemonic(KeyEvent.VK_L);
		row.add(load);
		// closest match
		closest = ButtonFactory.getButton(status.getIcon("goto"), "closest", events, null);
		closest.setMnemonic(KeyEvent.VK_M);
		row.add(closest);
		// skip ahead
		sAhead = ButtonFactory.getButton(status.getIcon("forward"),
			"next", events, null);
		sBack.setMnemonic(KeyEvent.VK_X);
		row.add(sAhead);
		row.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		vert.add(row);
		// scoring label
		scoreTop.setFont(titleFont);
		scoreTop.setForeground(Constants.DARK_BLUE);
		scoreTop.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		vert.add(scoreTop);
		vert.add(clWarn);
		sencl.add(vert, BorderLayout.NORTH);
		// left and right panels
		quanPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
		quanPanel.setBackground(Constants.WHITE);
		quanPanel.add(leftPanel);
		quanPanel.add(rightPanel);
		// now qualitative
		qualPanel = new JPanel(new BorderLayout(0, 10));
		qualPanel.setBackground(Constants.WHITE);
		qualEntry = new JTextField(32);
		qualEntry.addFocusListener(TextSelector.INSTANCE);
		qualEntry.setActionCommand("qe");
		qualEntry.addActionListener(events);
		row = new Box(BoxLayout.X_AXIS);
		row.add(Box.createHorizontalStrut(50));
		row.add(qualEntry);
		row.add(Box.createHorizontalStrut(10));
		row.add(ButtonFactory.getButton("Commit", "sts", events, KeyEvent.VK_A));
		row.add(Box.createHorizontalStrut(50));
		qualPanel.add(row, BorderLayout.SOUTH);
		qualPanel.add(qTeams, BorderLayout.NORTH);
		vLog = new JTextPane();
		vLog.setContentType("text/html");
		vLog.setBorder(BorderFactory.createEmptyBorder());
		vScroll = new JScrollPane(vLog);
		vScroll.setBorder(ButtonFactory.getThinBorder());
		qualPanel.add(vScroll, BorderLayout.CENTER);
		// add it all up
		vert = new JPanel(new BorderLayout());
		vert.setOpaque(false);
		qqParent = new JScrollPane();
		qqParent.setBorder(BorderFactory.createEmptyBorder());
		qqParent.setOpaque(false);
		row = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		row.setOpaque(false);
		row.add(qual);
		row.add(quan);
		vert.add(row, BorderLayout.NORTH);
		vert.add(qqParent, BorderLayout.CENTER);
		sencl.add(vert, BorderLayout.CENTER);
		JComponent south = new JPanel(new VerticalFlow(false));
		south.setOpaque(false);
		// result
		result = new AntialiasedJLabel("Tie Game");
		result.setFont(titleFont);
		result.setForeground(Constants.DARK_BLUE);
		row = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
		row.setOpaque(false);
		row.add(result);
		// commit score button
		row.add(ButtonFactory.getButton("Log Scores", "commit", events,
			KeyEvent.VK_C));
		south.add(row);
		// hotkey help!
		hkhelp = new AntialiasedJLabel(" ");
		hkhelp.setFont(textFont);
		hkhelp.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		south.add(hkhelp);
		sencl.setBackground(Constants.WHITE);
		sencl.add(south, BorderLayout.SOUTH);
	}
	/**
	 * Initializes the image uploader interface.
	 */
	private void setupImageUI() {
		EventListener events = new EventListener();
		AppLib.printDebug("Setting up image UI");
		// loading...
		reading = new Loading();
		reading.setText("Indexing...");
		reading.setVisible(false);
		// Drive list on top.
		drives = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		drives.setOpaque(false);
		jpegs = new JpegFilter();
		// The image goes in the middle.
		img = new JLabel();
		img.setBackground(Color.WHITE);
		img.setOpaque(true);
		img.setHorizontalAlignment(SwingConstants.CENTER);
		img.setVerticalAlignment(SwingConstants.CENTER);
		iencl = new JPanel(new BorderLayout());
		iencl.add(drives, BorderLayout.NORTH);
		JPanel center = new JPanel(new BorderLayout());
		center.setOpaque(false);
		center.setBorder(BorderFactory.createEtchedBorder());
		// The progress bar goes on the bottom.
		progress = new JProgressBar(JProgressBar.HORIZONTAL);
		progress.setUI(new SProgressBarUI());
		setBytes(1);
		progress.setString("No Image");
		progress.setStringPainted(true);
		center.add(progress, BorderLayout.SOUTH);
		JScrollPane pane = new JScrollPane(img);
		pane.setBorder(BorderFactory.createEmptyBorder());
		center.add(pane, BorderLayout.CENTER);
		// The buttons and text box go on top.
		JPanel top = new JPanel(new FlowLayout());
		top.setBackground(Constants.WHITE);
		// Add the previous button.
		JButton btn = ButtonFactory.getButton(status.getIcon("back"), "prev", events, null);
		btn.setMnemonic(KeyEvent.VK_BRACELEFT);
		top.add(btn);
		// Then the reload button.
		top.add(ButtonFactory.getButton(status.getIcon("refresh"), "ireload", events, null));
		// Now the team number.
		top.add(Box.createHorizontalStrut(100));
		top.add(new JLabel("Team number [ENTER to upload]:  "));
		teamNum = new TeamTextField(status, teamsSorted);
		teamNum.addKeyListener(events);
		teamNum.addKeyListener(new DelegateKeyListener(this));
		top.add(teamNum);
		top.add(Box.createHorizontalStrut(70));
		// Then the retate button.
		btn = ButtonFactory.getButton(status.getIcon("rotatel"), "rotatel", events, null);
		btn.setMnemonic(KeyEvent.VK_LESS);
		top.add(btn);
		btn = ButtonFactory.getButton(status.getIcon("rotater"), "rotater", events, null);
		btn.setMnemonic(KeyEvent.VK_GREATER);
		top.add(btn);
		// Last the next button.
		btn = ButtonFactory.getButton(status.getIcon("forward"), "next", events, null);
		btn.setMnemonic(KeyEvent.VK_BRACERIGHT);
		top.add(btn);
		// The list.
		model = new ImageListModel();
		iList = new JList(model);
		iList.setBorder(BorderFactory.createEmptyBorder());
		iList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		iList.setBackground(Constants.WHITE);
		iList.setPrototypeCellValue("0000000000000000");
		iList.addMouseListener(events);
		pane = new JScrollPane(iList);
		pane.setOpaque(false);
		pane.setBorder(ButtonFactory.getThinBorder());
		iencl.add(pane, BorderLayout.WEST);
		// Finish up.
		center.add(top, BorderLayout.NORTH);
		iencl.add(center, BorderLayout.CENTER);
		iencl.setBackground(Constants.WHITE);
		// Custom file chooser.
		customImage = new JFileChooser();
		customImage.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		customImage.setDialogType(JFileChooser.OPEN_DIALOG);
		customImage.setDialogTitle("Select Folder with Images");
		customImage.setMultiSelectionEnabled(false);
		updateDrives();
	}
	/**
	 * Sets up the main pit window.
	 */
	private void setupPitUI() {
		allMatches = new JCheckBox("Show All Matches", false);
		allMatches.setOpaque(false);
		allMatches.addKeyListener(new DelegateKeyListener(this));
		allMatches.setActionCommand("showall");
		allMatches.addActionListener(events);
		queueDelay = new JTextField(3);
		queueDelay.setText("0");
		queueDelay.addKeyListener(new DelegateKeyListener(this));
		queueDelay.addKeyListener(new NumericOnlyListener(false, -2));
		queueDelay.setActionCommand("qd");
		queueDelay.addActionListener(events);
		int i, all, ind; JButton btn; JLabel clk; StarRating rate;
		int showTeams = 2 * ScheduleItem.TPA * showAlliance;
		// init list queue first
		teamList = new JButton[showTeams];
		stars = new StarRating[showTeams];
		Border filler = BorderFactory.createEmptyBorder(1, 10, 1, 10);
		for (i = 0; i < showTeams; i++) {
			all = i / (ScheduleItem.TPA * 2);
			ind = i - (ScheduleItem.TPA * 2 * all);
			// handle the label (actually a button)
			btn = new AntialiasedJButton();
			btn.setBorder(filler);
			btn.setContentAreaFilled(false);
			btn.setBackground(Constants.WHITE);
			if (ind < ScheduleItem.TPA)
				btn.setForeground(Constants.RED);
			else
				btn.setForeground(Constants.BLUE);
			btn.setFocusable(false);
			btn.addActionListener(new CommentButtonListener(all, ind));
			btn.setFont(textFont);
			teamList[i] = btn;
			// handle rating
			rate = new StarRating(status);
			rate.setVisible(false);
			stars[i] = rate;
		}
		// delete and edit buttons now
		delButton = new JButton[showAlliance];
		editButton = new JButton[showAlliance];
		clocks = new JLabel[showAlliance];
		for (i = 0; i < showAlliance; i++) {
			RowListener rl = new RowListener(i);
			// delete button
			btn = ButtonFactory.getButton(status.getIcon("delete"),
				"del", rl, buttonSize);
			btn.setVisible(false);
			delButton[i] = btn;
			// edit button
			btn = ButtonFactory.getButton(status.getIcon("edit"),
				"edit", rl, buttonSize);
			btn.setVisible(false);
			editButton[i] = btn;
			// clocks last
			clk = new JLabel("00:00:00");
			clk.setForeground(Constants.BLACK);
			clk.setBackground(Constants.WHITE);
			clk.setFont(textFont);
			clk.setVisible(false);
			clocks[i] = clk;
		}
		// now the main panels
		pencl = new JPanel(new BorderLayout());
		JComponent pnl = new JPanel();
		// inside panel for background color and layout
		pencl.setBackground(Constants.WHITE);
		// center the next team label
		nextTeam = new AntialiasedJLabel();
		nextTeam.setOpaque(false);
		nextTeam.setFont(titleFont);
		nextTeam.setHorizontalAlignment(SwingConstants.CENTER);
		nextTeam.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		// set up the queue
		queueList = new JPanel(new VerticalFlow(true));
		queueList.setBackground(Constants.WHITE);
		// iterate through array of teams (prep the scoring area)
		for (i = 0; i < showTeams; i++) {
			all = i / (ScheduleItem.TPA * 2);
			ind = i - (ScheduleItem.TPA * 2 * all);
			// add team to box
			pnl = new Box(BoxLayout.X_AXIS);
			pnl.setOpaque(false);
			// on first display clock and delete/edit.
			if (ind == 0) {
				pnl.add(delButton[all]);
				pnl.add(Box.createHorizontalStrut(1));
				pnl.add(editButton[all]);
			} else pnl.add(Box.createHorizontalStrut(buttonSize.width * 2 + 1));
			pnl.add(teamList[i]);
			pnl.add(stars[i]);
			pnl.add(Box.createHorizontalGlue());
			if (ind == 0) pnl.add(clocks[all]);
			// add to list
			queueList.add(pnl);
			if (i % (ScheduleItem.TPA * 2) == ScheduleItem.TPA * 2 - 1)
				queueList.add(Box.createVerticalStrut(5));
		}
		JPanel vert = new JPanel(new VerticalFlow(false));
		vert.setOpaque(false);
		vert.add(nextTeam);
		pnl = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 3));
		pnl.setOpaque(false);
		pnl.add(allMatches);
		pnl.add(Box.createHorizontalStrut(10));
		pnl.add(new JLabel("Queue delay (minutes):"));
		pnl.add(queueDelay);
		pnl.add(Box.createHorizontalStrut(10));
		pnl.add(ButtonFactory.getButton("Full-screen", "fs", events, KeyEvent.VK_F));
		vert.add(pnl);
		pencl.add(vert, BorderLayout.NORTH);
		// scroll pane for middle
		JComponent center = new JScrollPane(queueList);
		center.setOpaque(false);
		center.setBorder(BorderFactory.createTitledBorder(BorderFactory.
			createLineBorder(Constants.BLACK), "In Queue", TitledBorder.LEFT,
			TitledBorder.DEFAULT_POSITION, null, Constants.BLACK));
		pencl.add(center, BorderLayout.CENTER);
	}
	/**
	 * Goes to the closest match.
	 */
	private void gotoClosest() {
		long tt = date / 1000L, bestError = Long.MAX_VALUE, error;
		int i = 0; sIndex = i;
		ScheduleItem match;
		synchronized (queue) {
			Iterator<ScheduleItem> it = queue.iterator();
			while (it.hasNext()) {
				// find the difference between date and time
				match = it.next();
				error = Math.abs(match.getTime() / 1000L - tt);
				if (error < bestError) {
					bestError = error;
					sIndex = i;
				}
				i++;
			}
		}
	}
	/**
	 * Changes the next match up.
	 * 
	 * @param diff the number of matches to skip
	 */
	private void scoreGo(int diff) {
		if (confirmChange() || !user.canScore()) return;
		int si2 = sIndex + diff;
		synchronized (queue) {
			if (si2 < 0 || si2 >= queue.size())
				return;
		}
		sDirty = false;
		sIndex = si2;
		loadItem();
	}
	/**
	 * Tallies and updates the team scores.
	 */
	private void tally(int num) {
		int leftScore = 0, rightScore = 0;
		AllianceTeam item;
		if (data.getData().isAdvScore()) {
			// total from the text boxes
			if (num >= 0 && num < ScheduleItem.TPA) {
				AppLib.printDebug("Totalling team@" + num + " on left");
				item = alliance1.get(num);
				try {
					item.score.setTotalScore(Integer.parseInt(item.total.getText()));
				} catch (Exception e) { }
			} else if (num >= ScheduleItem.TPA && num < ScheduleItem.TPA * 2) {
				num -= ScheduleItem.TPA;
				AppLib.printDebug("Totalling team@" + num + " on right");
				item = alliance2.get(num);
				try {
					item.score.setTotalScore(Integer.parseInt(item.total.getText()));
				} catch (Exception e) { }
			}
		}
		// tally the valid left scores
		Iterator<AllianceTeam> it = alliance1.iterator();
		while (it.hasNext()) {
			item = it.next();
			leftScore += item.score.totalScore();
		}
		// tally the valid right scores
		it = alliance2.iterator();
		while (it.hasNext()) {
			item = it.next();
			rightScore += item.score.totalScore();
		}
		// set totals
		leftTotal.setText(Integer.toString(leftScore));
		rightTotal.setText(Integer.toString(rightScore));
		rally();
	}
	/**
	 * Sets the outcome box.
	 */
	private void rally() {
		try {
			int leftScore = Integer.parseInt(leftTotal.getText());
			int rightScore = Integer.parseInt(rightTotal.getText());
			// compute outcome
			if (leftScore > rightScore)
				result.setText("Red Leads");
			else if (leftScore < rightScore)
				result.setText("Blue Leads");
			else
				result.setText("Tie Game");
		} catch (Exception e) { }
	}
	/**
	 * Selects a team by boxing it. The number is a keyboard style number
	 *  like the one on the screen: 1-9 inclusive.
	 */
	private void select(int num) {
		AllianceTeam item;
		if (num > 0 && num <= ScheduleItem.TPA) {
			num--;
			AppLib.printDebug("Selecting team@" + num + " on left");
			// clear existing rectangle
			if (!selectedSide && selectedIndex < alliance1.size()) {
				item = alliance1.get(selectedIndex);
				item.label.setBorder(BorderFactory.createEmptyBorder());
				item.panel.setVisible(false);
			} else if (selectedIndex < alliance2.size()) {
				item = alliance2.get(selectedIndex);
				item.label.setBorder(BorderFactory.createEmptyBorder());
				item.panel.setVisible(false);
			}
			item = alliance1.get(num);
			// select new one
			item.label.setBorder(ButtonFactory.getThinBorder());
			// show the panel
			item.panel.setVisible(true);
			selectedIndex = num;
			selectedSide = false;
		} else if (num > ScheduleItem.TPA && num <= ScheduleItem.TPA * 2) {
			num -= ScheduleItem.TPA + 1;
			AppLib.printDebug("Selecting team@" + num + " on right");
			// clear existing rectangle
			if (!selectedSide && selectedIndex < alliance1.size()) {
				item = alliance1.get(selectedIndex);
				item.label.setBorder(BorderFactory.createEmptyBorder());
				item.panel.setVisible(false);
			} else if (selectedIndex < alliance2.size()) {
				item = alliance2.get(selectedIndex);
				item.label.setBorder(BorderFactory.createEmptyBorder());
				item.panel.setVisible(false);
			}
			item = alliance2.get(num);
			// select new one
			item.label.setBorder(ButtonFactory.getThinBorder());
			// show the panel
			item.panel.setVisible(true);
			selectedIndex = num;
			selectedSide = true;
		} else if (alliance1.size() + alliance2.size() > 0)
			// nothing
			select(1);
	}
	/**
	 * Updates the list queue, with synchronization provisions.
	 */
	private void updateListQueue() {
		if (itemNeedsLoading) {
			itemNeedsLoading = false;
			select = true;
			loadItem();
			return;
		}
		synchronized (data.getSchedule()) {
			matchList.setList(data.getSchedule().values());
		}
		int late = data.getData().minutesLate();
		if (late == 0) {
			kRunLate.setForeground(Constants.WHITE);
			runlate.setForeground(Constants.WHITE);
			kRunLate.setText("on time");
			runlate.setText("on time");
		} else if (late > 0) {
			kRunLate.setForeground(Constants.LIGHT_RED);
			runlate.setForeground(Constants.LIGHT_RED);
			if (late >= 120) {
				kRunLate.setText("very late");
				runlate.setText("very late");
			} else if (late >= 90) {
				kRunLate.setText("1.5 hours late");
				runlate.setText("1.5 hours late");
			} else if (late >= 60) {
				kRunLate.setText("1 hour late");
				runlate.setText("1 hour late");
			} else {
				kRunLate.setText(late + " min late");
				runlate.setText(late + " min late");
			}
		} else {
			late = -late;
			kRunLate.setForeground(Constants.LIGHT_GREEN);
			runlate.setForeground(Constants.LIGHT_GREEN);
			if (late >= 120) {
				kRunLate.setText("very early");
				runlate.setText("very early");
			} else if (late >= 90) {
				kRunLate.setText("1.5 hours early");
				runlate.setText("1.5 hours early");
			} else if (late >= 60) {
				kRunLate.setText("1 hour early");
				runlate.setText("1 hour early");
			} else {
				kRunLate.setText(late + " min early");
				runlate.setText(late + " min early");
			}
		}
		synchronized (queue) {
			AppLib.printDebug("Updating list queue");
			Iterator<ScheduleItem> it = queue.iterator();
			java.util.List<Integer> teams; Team team;
			String str; ScheduleItem match;
			int i, j = 0, k;
			kQueue.clear();
			for (i = 0; i < showAlliance && it.hasNext();) {
				// show the wanteds
				match = it.next();
				teams = match.getTeams();
				if (!shouldShow(match)) continue;
				editButton[i].setVisible(true);
				delButton[i].setVisible(true);
				editButton[i].setEnabled(user.canRead());
				delButton[i].setEnabled(user.canRead());
				clocks[i].setVisible(true);
				kQueue.add(match);
				// drop in team names
				for (k = 0; k < teams.size() && j < teamList.length; k++) {
					team = data.get(teams.get(k));
					if (k == 0) {
						teamList[j].setForeground(Constants.RED);
						kTeamList[j].setForeground(Constants.RED);
					}
					if (team != null) {
						str = team.getName() + " #" + team.getNumber();
						teamList[j].setText(str);
						kTeamList[j].setText(str);
						double r = team.getRating();
						// super-stars!
						if (r > 0 && user.canRead()) {
							stars[j].setVisible(true);
							stars[j].setStars(r);
						} else stars[j].setVisible(false);
					} else {
						teamList[j].setText("No Entrant");
						kTeamList[j].setText("No Entrant");
						stars[j].setVisible(false);
					}
					j++;
				}
				i++;
			}
			for (; i < showAlliance; i++) {
				// hide the unwanteds
				editButton[i].setEnabled(false);
				editButton[i].setVisible(false);
				delButton[i].setEnabled(false);
				delButton[i].setVisible(false);
				clocks[i].setVisible(false);
				kQueue.add(null);
				// drop in team names
				teamList[j].setText("Empty slot");
				teamList[j].setForeground(Constants.BLACK);
				kTeamList[j].setText(" ");
				kTeamList[j].setForeground(Constants.BLACK);
				stars[j].setVisible(false);
				j++;
				for (k = 1; k < ScheduleItem.TPA * 2 && j < teamList.length; k++) {
					teamList[j].setText(" ");
					kTeamList[j].setText(" ");
					stars[j].setVisible(false);
					j++;
				}
			}
			int num; String title;
			// update left
			for (i = 0; i < ScheduleItem.TPA; i++) {
				num = alliance1.get(i).teamNum;
				if (num == 0)
					title = "No Entrant";
				else {
					team = data.get(num);
					title = team.getName();
				}
				// shorten title
				if (title.length() < 17)
					title = (i + 1) + ": " + num + " " + title;
				else
					title = (i + 1) + ": " + num + " " +
						title.substring(0, 16) + "...";
				alliance1.get(i).label.setText(title);
				qualTeams[i].setText(title);
			}
			// update right
			for (i = 0; i < ScheduleItem.TPA; i++) {
				num = alliance2.get(i).teamNum;
				if (num == 0)
					title = "No Entrant";
				else {
					team = data.get(num);
					title = team.getName();
				}
				// shorten title
				if (title.length() < 17)
					title = (i + 1 + ScheduleItem.TPA) + ": " + num + " " + title;
				else
					title = (i + 1 + ScheduleItem.TPA) + ": " + num + " " +
						title.substring(0, 16) + "...";
				alliance2.get(i).label.setText(title);
				qualTeams[i + ScheduleItem.TPA].setText(title);
			}
		}
		//tally(-1);
	}
	/**
	 * Shows the comments for a given team.
	 */
	private void showComments(Team team) {
		if (team == null || !user.canRead()) return;
		showComments.setTeam(team);
		showComments.setVisible(true);
	}
	/**
	 * Asks the user if they really want to discard their changes.
	 * 
	 * @return false to go on, true to stop
	 */
	private boolean confirmChange() {
		return (sDirty || cDirty) && !AppLib.confirm(status.getWindow(), "The current match has " +
			"been edited.\nReloading the match will erase these edits.\nContinue anyway?");
	}
	/**
	 * Loads teams from the schedule. Queues up next match.
	 */
	private void loadItem() {
		synchronized (queue) {
			if (queue.size() < 1) {
				sDirty = false;
				sBack.setEnabled(false);
				sAhead.setEnabled(false);
				return;
			}
		}
		synchronized (queue) {
			AppLib.printDebug("Loading next match");
			toScore = queue.get(sIndex);
			java.util.List<Score> scores = toScore.getScores();
			java.util.List<Integer> teams = toScore.getTeams();
			AllianceTeam item, item2; int p;
			for (int i = 0; i < ScheduleItem.TPA; i++) {
				item = alliance1.get(i);
				// always set team #s and penalty counts
				item.teamNum = teams.get(i);
				if (scores != null && i < scores.size()) {
					p = scores.get(i).getPenaltyCount();
					item.pen.setText(Integer.toString(p));
					item.score.setPenaltyCount(p);
				} else {
					item.pen.setText("0");
					item.score.setPenaltyCount(0);
				}
				item2 = alliance2.get(i);
				item2.teamNum = teams.get(i + ScheduleItem.TPA);
				if (scores != null && i + ScheduleItem.TPA < scores.size()) {
					p = scores.get(i + ScheduleItem.TPA).getPenaltyCount();
					item2.pen.setText(Integer.toString(p));
					item2.score.setPenaltyCount(p);
				} else {
					item2.pen.setText("0");
					item2.score.setPenaltyCount(0);
				}
				if (data.getData().isAdvScore()) {
					// individuals if advanced
					for (int j = 0; j < hotkeys.size(); j++) {
						if (scores != null && i < scores.size()) {
							p = scores.get(i).getScoreAt(j);
							item.scores[j].setText(Integer.toString(p));
							item.score.setScoreAt(j, p);
						} else {
							item.scores[j].setText("0");
							item.score.setScoreAt(j, 0);
						}
						if (scores != null && i + ScheduleItem.TPA < scores.size()) {
							p = scores.get(i + ScheduleItem.TPA).getScoreAt(j);
							item2.scores[j].setText(Integer.toString(p));
							item2.score.setScoreAt(j, p);
						} else {
							item2.scores[j].setText("0");
							item2.score.setScoreAt(j, 0);
						}
					}
					// set totals
					if (scores != null && i < scores.size()) {
						p = scores.get(i).totalScore();
						item.total.setText(Integer.toString(p));
						item.score.setTotalScore(p);
					} else {
						item.total.setText("0");
						item.score.setTotalScore(0);
					}
					if (scores != null && i + ScheduleItem.TPA < scores.size()) {
						p = scores.get(i + ScheduleItem.TPA).totalScore();
						item2.total.setText(Integer.toString(p));
						item2.score.setTotalScore(p);
					} else {
						item2.total.setText("0");
						item2.score.setTotalScore(0);
					}
				}
			}
			// set all totals
			leftTotal.setText(Integer.toString(toScore.getRedScore()));
			rightTotal.setText(Integer.toString(toScore.getBlueScore()));
			scoreTop.setText(toScore.getLabel() + " " + toScore.getNum() +
				(toScore.getStatus() == ScheduleItem.COMPLETE ? "   (Scored)" : ""));
			updateListQueue();
			sDirty = false;
			sBack.setEnabled(sIndex > 0);
			sAhead.setEnabled(sIndex < queue.size() - 1);
			requestFocus();
		}
	}
	/**
	 * Loads the scores at the specified index into the score object.
	 * 
	 * @param num the index to load
	 */
	private void load(int num) {
		AllianceTeam item;
		int total = 0, s, pen;
		// update scores with text fields
		if (num >= 0 && num < ScheduleItem.TPA) {
			AppLib.printDebug("Scoring team@" + num + " on left");
			item = alliance1.get(num);
			if (data.getData().isAdvScore())
				for (int i = 0; i < item.scores.length; i++)
					try {
						s = Integer.parseInt(item.scores[i].getText());
						total += s;
						item.score.setScoreAt(i, s);
					} catch (Exception e) {
						item.score.setScoreAt(i, 0);
					}
			try {
				pen = Integer.parseInt(item.pen.getText());
				item.score.setPenaltyCount(pen);
				total -= 10 * pen;
			} catch (Exception e) {
				item.score.setPenaltyCount(0);
			}
			item.score.setTotalScore(total);
		} else if (num >= ScheduleItem.TPA && num < ScheduleItem.TPA * 2) {
			num -= ScheduleItem.TPA;
			AppLib.printDebug("Scoring team@" + num + " on right");
			item = alliance2.get(num);
			if (data.getData().isAdvScore())
				for (int i = 0; i < item.scores.length; i++)
					try {
						s = Integer.parseInt(item.scores[i].getText());
						total += s;
						item.score.setScoreAt(i, s);
					} catch (Exception e) {
						item.score.setScoreAt(i, 0);
					}
			try {
				pen = Integer.parseInt(item.pen.getText());
				item.score.setPenaltyCount(pen);
				total -= 10 * pen;
			} catch (Exception e) {
				item.score.setPenaltyCount(0);
			}
			item.score.setTotalScore(total);
			num += ScheduleItem.TPA;
		}
		total(num);
		rally();
	}
	/**
	 * Adds up the scores at the specified index and loads into the total.
	 * 
	 * @param num the index to add up
	 */
	private void total(int num) {
		AllianceTeam item;
		if (num >= 0 && num < ScheduleItem.TPA) {
			AppLib.printDebug("Adding team@" + num + " on left");
			item = alliance1.get(num);
			item.total.setText(Integer.toString(item.score.totalScore()));
		} else if (num >= ScheduleItem.TPA && num < ScheduleItem.TPA * 2) {
			num -= ScheduleItem.TPA;
			AppLib.printDebug("Adding team@" + num + " on right");
			item = alliance2.get(num);
			item.total.setText(Integer.toString(item.score.totalScore()));
		}
		tally(num);
	}
	/**
	 * Adds/subtracts a score.
	 * 
	 * @param num the index to add up
	 * @param field which field to change
	 * @param up whether to go up or down
	 */
	private void score(int num, int field, boolean up) {
		AllianceTeam item;
		int dir = up ? 1 : -1, e;
		if (field == -1) {
			if (num >= 0 && num < ScheduleItem.TPA) {
				AppLib.printDebug("Scoring team@" + num + " on left");
				item = alliance1.get(num);
				e = item.score.getPenaltyCount() + dir;
				if (e >= 0) item.score.setPenaltyCount(e);
			} else if (num >= ScheduleItem.TPA && num < ScheduleItem.TPA * 2) {
				num -= ScheduleItem.TPA;
				AppLib.printDebug("Scoring team@" + num + " on right");
				item = alliance2.get(num);
				e = item.score.getPenaltyCount() + dir;
				if (e >= 0) item.score.setPenaltyCount(e);
				num += ScheduleItem.TPA;
			}
		} else {
			if (!data.getData().isAdvScore()) return;
			if (num >= 0 && num < ScheduleItem.TPA) {
				AppLib.printDebug("Scoring team@" + num + " on left");
				item = alliance1.get(num);
				e = item.score.getScoreAt(field) + dir;
				if (e >= 0) item.score.setScoreAt(field, e);
			} else if (num >= ScheduleItem.TPA && num < ScheduleItem.TPA * 2) {
				num -= ScheduleItem.TPA;
				AppLib.printDebug("Scoring team@" + num + " on right");
				item = alliance2.get(num);
				e = item.score.getScoreAt(field) + dir;
				if (e >= 0) item.score.setScoreAt(field, e);
				num += ScheduleItem.TPA;
			}
		}
		update(num);
	}
	/**
	 * Updates the scores at the specified index.
	 * 
	 * @param num the index to update
	 */
	private void update(int num) {
		AllianceTeam item;
		// penalties only
		if (num >= 0 && num < ScheduleItem.TPA) {
			AppLib.printDebug("Updating team@" + num + " on left");
			item = alliance1.get(num);
			item.pen.setText(Integer.toString(item.score.getPenaltyCount()));
		} else if (num >= ScheduleItem.TPA && num < ScheduleItem.TPA * 2) {
			num -= ScheduleItem.TPA;
			AppLib.printDebug("Updating team@" + num + " on right");
			item = alliance2.get(num);
			item.pen.setText(Integer.toString(item.score.getPenaltyCount()));
			num += ScheduleItem.TPA;
		}
		if (data.getData().isAdvScore()) {
			// all hot keys
			Iterator<Integer> it;
			int i = 0;
			if (num >= 0 && num < ScheduleItem.TPA) {
				item = alliance1.get(num);
				it = item.score.iterator();
				while (it.hasNext() && i < item.scores.length)
					item.scores[i++].setText(Integer.toString(it.next()));
			} else if (num >= ScheduleItem.TPA && num < ScheduleItem.TPA * 2) {
				num -= ScheduleItem.TPA;
				item = alliance2.get(num);
				it = item.score.iterator();
				while (it.hasNext() && i < item.scores.length)
					item.scores[i++].setText(Integer.toString(it.next()));
				num += ScheduleItem.TPA;
			}
		}
		total(num);
	}
	/**
	 * Commits the comments.
	 */
	private void commitComments() {
		if (vList.size() < 1 || toScore == null || !user.canScore() ||
				!AppLib.confirm(status.getWindow(), "Do you really want to commit comments?"))
			return;
		Iterator<TC> it = vList.iterator();
		java.util.List<Integer> tms = toScore.getTeams();
		Comment[] comments = new Comment[tms.size()]; TC tc; int index = 0; Team team;
		Comment comment; Iterator<Comment> it2;
		Iterator<Integer> it3 = tms.iterator();
		while (it3.hasNext()) {
			team = data.get(it3.next());
			if (team != null && team.getComments() != null) {
				// load existing
				it2 = team.getComments().iterator();
				while (it2.hasNext()) {
					comment = it2.next();
					if (comment.getMatch() != null && comment.getMatch().equals(toScore)) {
						comments[index] = comment;
						break;
					}
				}
			}
			index++;
		}
		java.util.List<Integer> list;
		it = vList.iterator(); int rate = 0;
		while (it.hasNext()) {
			tc = it.next();
			index = tms.indexOf(tc.teamNum);
			if (index < 0) continue;
			if (comments[index] == null) {
				// create new
				int sz = data.getData().getUDFs().size();
				list = new ArrayList<Integer>(sz);
				for (int i = 0; i < sz; i++)
					list.add(0);
				comments[index] = new Comment(user, toScore, "", 0, list, date);
			}
			// auto rating
			comment = comments[index];
			rate = 0;
			if (tc.text.startsWith("rate"))
				try {
					rate = Integer.parseInt(tc.text.substring(4).trim());
					if (rate < 0 || rate > 5) rate = 0;
				} catch (Exception e) { }
			if (rate != 0)
				comment.setRating(rate);
			// append
			else if (comment.getText().length() < 1)
				comment.setText(tc.text);
			else
				comment.setText(comment.getText() + "\n" + tc.text);
			comment.setWhen(date);
		}
		// update all non-null teams with comment
		it3 = tms.iterator();
		index = 0;
		load();
		while (it3.hasNext()) {
			team = data.get(it3.next());
			if (team != null && team.getComments() != null && comments[index] != null)
				data.updateComment(team.getNumber(), comments[index]);
			index++;
		}
		emptyLog();
	}
	/**
	 * Commits the score to the system server.
	 */
	private void commitScore() {
		if (!user.canScore() || toScore == null) return;
		if (!AppLib.confirm(status.getWindow(), "Please verify that the scores on the screen " +
				"are correct.\nDo not commit without checking this data!\n\nCommit?"))
			return;
		// decided to commit
		AppLib.printDebug("Committing scores to backend");
		int scoreOne = 0, scoreTwo = 0;
		try {
			scoreOne = Integer.parseInt(leftTotal.getText());
			scoreTwo = Integer.parseInt(rightTotal.getText());
		} catch (Exception ex) {
			AppLib.printWarn(status.getWindow(), "Invalid total score.");
			return;
		}
		// set up totals
		ArrayList<Score> scores = new ArrayList<Score>(ScheduleItem.TPA * 2);
		ArrayList<Integer> teams = new ArrayList<Integer>(ScheduleItem.TPA * 2);
		int num;
		// load valid left scores and teams
		Iterator<AllianceTeam> ait = alliance1.iterator();
		AllianceTeam tm;
		while (ait.hasNext()) {
			tm = ait.next();
			num = tm.teamNum;
			scores.add(tm.score);
			if (num <= 0)
				teams.add(0);
			else
				teams.add(num);
		}
		// load valid right scores and teams
		ait = alliance2.iterator();
		while (ait.hasNext()) {
			tm = ait.next();
			num = tm.teamNum;
			scores.add(tm.score);
			if (num <= 0)
				teams.add(0);
			else
				teams.add(num);
		}
		// read existing match
		try {
			if (toScore.getStatus() == ScheduleItem.COMPLETE && toScore.getScores() != null) {
				AppLib.printDebug("Match already scored");
				String dat = "Warning: This match has already been scored.\n\n";
				dat += "If you continue, the new scores will be:\n";
				if (data.getData().isAdvScore()) {
					Iterator<Integer> it = teams.iterator();
					Iterator<Score> it2 = toScore.getScores().iterator();
					// copy new scores list
					ArrayList<Score> nl = new ArrayList<Score>(ScheduleItem.TPA * 2);
					Score sc;
					int i = 0;
					while (it.hasNext() && it2.hasNext()) {
						sc = it2.next();
						if (scores.get(i).totalScore() != 0) sc = scores.get(i);
						dat += it.next() + ": " + sc + "\n";
						nl.add(sc);
						if (i == ScheduleItem.TPA - 1)
							dat += "Red: " + scoreOne + "\n\n";
						else if (i == ScheduleItem.TPA * 2 - 1)
							dat += "Blue: " + scoreTwo;
						i++;
					}
					scores = nl;
				} else
					dat += "Red: " + scoreOne + "\nBlue: " + scoreTwo;
				dat += "\nContinue anyway?";
				if (!AppLib.confirm(status.getWindow(), dat)) return;
			}
			ScheduleItem newMatch = ScheduleItem.copyValue(toScore);
			newMatch.setRedScore(scoreOne);
			newMatch.setBlueScore(scoreTwo);
			newMatch.setScores(scores);
			newMatch.setTeams(teams);
			sIndex++;
			if (sIndex >= queue.size()) sIndex = queue.size() - 1;
			itemNeedsLoading = true;
			data.scoreMatch(newMatch);
		} catch (Exception e) {
			AppLib.debugException(e);
			return;
		}
	}
	/**
	 * Reloads the file list.
	 */
	private void reload() {
		index = 0;
		if (files == null || files.length == 0) {
			// why continue?
			AppLib.printDebug("Empty drive");
			img.setIcon(null);
			toSend = null;
			unrotated = null;
			synchronized (cache) {
				cache.clear();
			}
		} else {
			// go, go, go!!!
			AppLib.printDebug(files.length + " JPEG files in the drive.");
			index = 0;
			loadImg();
			teamNum.requestFocus();
		}
		if (files != null) model.fireContentsChanged(0, files.length);
		else model.fireContentsChanged(0, 0);
		iList.repaint();
	}
	/**
	 * Locks and loads the next image.
	 */
	private void loadImg() {
		if (files == null || index > files.length - 1 || index < 0)
			return;
		File uploading = files[index];
		try {
			String name = uploading.getAbsolutePath().toLowerCase();
			ImageData io;
			synchronized (cache) {
				if (cache.size() > 3) {
					// delete oldest images
					Iterator<String> it = cache.keySet().iterator();
					while (it.hasNext()) {
						io = cache.get(it.next());
						if (io.timeAdded + 1000 * Constants.TTL_SECONDS < date)
							it.remove();
					}
				}
				io = cache.get(name);
				if (io == null) {
					// not cached
					toSend = loadImageWait(uploading, 0);
					io = new ImageData(new ImageIcon(toSend, "Team Image"));
					cache.put(name, io);
				}
			}
			rotate = 0;
			unrotated = io.image.getImage();
			toSend = (BufferedImage)unrotated;
			img.setIcon(io.image);
			iList.setSelectedIndex(index);
		} catch (Exception e) {
			AppLib.debugException(e);
			// Skip the file.
			img.setIcon(null);
			AppLib.printWarn(status.getWindow(), "Could not read the file \"" +
				uploading.getName() + "\".");
		}
	}
	/**
	 * Updates the rotation of the current image.
	 */
	private void rotate() {
		if (unrotated == null || toSend == null) return;
		toSend = rotateImage(unrotated, rotate);
		img.setIcon(new ImageIcon(toSend, "Team Image"));
	}
	/**
	 * Loads the specified image and blocks until it is done.
	 * 
	 * @param image the file source
	 * @param id the ID to use
	 * @param rotate the rotation
	 * @return the image, scaled
	 */
	private BufferedImage loadImageWait(File uploading, int id) {
		Image image = AppLib.winInfo.createImage(uploading.getAbsolutePath());
		if (image == null)
			throw new RuntimeException("cannot read " + uploading.getName());
		loader.addImage(image, id);
		try {
			loader.waitForID(id);
		} catch (InterruptedException e) { }
		if (loader.isErrorID(id) || !loader.checkID(id))
			throw new RuntimeException("cannot load");
		loader.removeImage(image);
		return scaleImage(image);
	}
	/**
	 * Scales the image to the specified upload size.
	 * 
	 * @param i the image to scale
	 * @return the scaled image
	 */
	private BufferedImage scaleImage(Image i) {
		int iw = i.getWidth(null), ih = i.getHeight(null), fiw, fih;
		// TYPE_INT_RGB important for JPEG
		if (ih > iw) {
			fih = imageSize.width;
			fiw = imageSize.height;
		} else {
			fiw = imageSize.width;
			fih = imageSize.height;
		}
		BufferedImage out = new BufferedImage(fiw, fih, BufferedImage.TYPE_INT_RGB);
		// copy (can this be improved?)
		Graphics2D g2d = out.createGraphics();
		Image scaled = i.getScaledInstance(fiw, fih, Image.SCALE_FAST);
		loader.addImage(scaled, -1);
		try {
			loader.waitForID(-1);
		} catch (Exception e) { }
		loader.removeImage(scaled);
		g2d.drawImage(scaled, 0, 0, null);
		g2d.dispose();
		return out;
	}
	/**
	 * Rotates the given already-scaled image.
	 * 
	 * @param i the image to rotate
	 * @return the image rotated correctly
	 */
	private BufferedImage rotateImage(Image i, int rotate) {
		// 0 = 0 degrees, 1 = 90 degrees, 2 = 180 degrees, 3 = 270 degrees (all CCW)
		int iw = i.getWidth(null), ih = i.getHeight(null), fiw, fih;
		if ((ih > iw && rotate % 2 == 0) || (ih < iw && rotate % 2 == 1)) {
			fih = imageSize.width;
			fiw = imageSize.height;
		} else {
			fiw = imageSize.width;
			fih = imageSize.height;
		}
		// redraw
		BufferedImage out = new BufferedImage(fiw, fih, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = out.createGraphics();
		g2d.translate(fiw / 2, fih / 2);
		if (rotate != 0) g2d.rotate(rotate * Math.PI / 2., 0., 0.);
		g2d.drawImage(i, -iw / 2, -ih / 2, null);
		g2d.dispose();
		return out;
	}
	/**
	 * Sets the progress bar to a given value.
	 */
	private void setProgress(int value) {
		progress.setValue(value);
		progress.setString("Sent " + (value / 1024) + "K / " + size + "K");
	}
	/**
	 * Sets the size of the progress bar.
	 */
	private void setBytes(int bytes) {
		setProgress(0);
		size = bytes / 1024;
		progress.setMaximum(bytes);
	}
	/**
	 * Queues an image for sending and loads the next one.
	 */
	private void send() {
		if (!user.canScore() || files == null || toSend == null) return;
		String txt = teamNum.getText();
		if (!AppLib.positiveInteger(txt) ||
				data.get(Integer.parseInt(txt)) == null) {
			AppLib.printWarn(status.getWindow(), "Please enter a valid team number to " +
				"tag the photo.");
			return;
		}
		upName = txt + ".jpg";
		// queue image in background
		AppLib.printDebug("Preparing upload for " + upName);
		new Thread(new EventListener()).start();
		teamNum.setText("");
		teamNum.update();
		teamNum.requestFocus();
		if (index < files.length - 1) index++;
		unrotated.flush();
		unrotated = null;
		System.gc();
		loadImg();
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
	 * @param files the list to append
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
	 * Updates the drive list.
	 */
	private void updateDrives() {
		if (roots == null) {
			roots = VolumeLister.listVolumes();
			updateDrives0();
		} else {
			File[] newRoots = VolumeLister.listVolumes();
			if (newRoots.length != roots.length) {
				roots = newRoots;
				updateDrives0();
			} else for (int i = 0; i < roots.length; i++)
				if (!roots[i].equals(newRoots[i])) {
					roots = newRoots;
					updateDrives0();
					break;
				}
		}
	}
	/**
	 * Updates the visual drive list.
	 */
	private void updateDrives0() {
		drives.removeAll();
		JButton but; String name;
		driveButtons = new JButton[roots.length + 1];
		for (int i = 0; i < roots.length; i++) {
			name = roots[i].getName().toUpperCase().trim();
			if (name.length() < 1)
				name = roots[i].getAbsolutePath().toUpperCase().trim();
			if (name.length() > 16) name = name.substring(0, 15) + "...";
			but = new JButton(name, status.getIcon("drive"));
			but.setHorizontalTextPosition(SwingConstants.RIGHT);
			but.setActionCommand("drive");
			but.addActionListener(new DriveListener(i));
			but.setFocusable(false);
			but.setSelected(false);
			driveButtons[i] = but;
			drives.add(but);
		}
		but = new JButton("Custom...");
		but.setHorizontalTextPosition(SwingConstants.RIGHT);
		but.setActionCommand("custom");
		but.addActionListener(events);
		but.setFocusable(false);
		but.setSelected(false);
		driveButtons[driveButtons.length - 1] = but;
		drives.add(but);
		setDrive0(-1);
	}
	/**
	 * Changes the active drive to index.
	 * 
	 * @param index the index to set
	 */
	private void setDrive(int index) {
		refresher.command(index);
	}
	/**
	 * Really changes the active drive to index!
	 * 
	 * @param index the index to set
	 */
	private void setDrive0(int index) {
		if (index >= 0) {
			current = null;
			if (index < driveButtons.length - 1) {
				// set the drive
				reading.setVisible(true);
				files = listAll(roots[index]);
				current = roots[index];
			} else if (customImage.showDialog(status.getWindow(), "Select") ==
					JFileChooser.APPROVE_OPTION) {
				// custom images
				File file = customImage.getSelectedFile();
				if (file.canRead() && file.isDirectory()) {
					reading.setVisible(true);
					files = listAll(file);
					current = file;
				} else
					return;
			} else
				return;
			// set the buttons
			for (int i = 0; i < driveButtons.length; i++)
				driveButtons[i].setSelected(false);
			if (files != null && files.length > 0)
				driveButtons[index].setSelected(true);
		} else if (current == null || files == null) {
			// clear
			for (int i = 0; i < driveButtons.length; i++)
				driveButtons[i].setSelected(false);
			files = null;
			current = null;
			unrotated = null;
			toSend = null;
			synchronized (cache) {
				cache.clear();
			}
		}
		reading.setVisible(false);
		reload();
	}
	/**
	 * Runs the bulk uploader.
	 */
	private void importLots() {
		if (!user.canScore()) return;
		File file = AppLib.openFile(this, "csv");
		if (file == null) return;
		// date formats
		java.util.List<MatchLabel> labels = data.getData().getLabels();
		try {
			// variable initialization
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line, token, matchLabel; StringTokenizer str;
			Date in; long realTime; ScheduleItem match; int matchNum, i, j;
			Calendar local = Calendar.getInstance();
			local.setTimeInMillis(date);
			LinkedList<ScheduleItem> list = new LinkedList<ScheduleItem>();
			LinkedList<Integer> teams; char ch;
			String sur, tm; BitSet counts = new BitSet(2 * ScheduleItem.TPA);
			while (br.ready()) {
				line = br.readLine();
				if (line == null) break;
				// blank?
				line = line.trim();
				if (line.length() < 1 || line.indexOf(',') < 0) continue;
				str = new StringTokenizer(line, ",");
				if (str.countTokens() < 3 + ScheduleItem.TPA * 2) continue;
				in = null; realTime = 0L;
				token = str.nextToken().trim().toUpperCase();
				// dates
				for (i = 0; i < formats.length; i++) {
					try {
						// each format
						in = formats[i].parse(token);
						break;
					} catch (Exception e) { }
				}
				if (in == null) continue;
				realTime = in.getTime();
				try {
					matchNum = Integer.parseInt(str.nextToken().trim());
					if ((matchNum < 1 || matchNum > 300) && !AppLib.confirm(status.getWindow(),
						"Match number " + matchNum + " looks invalid.\nContinue anyway?"))
						continue;
					matchLabel = str.nextToken().trim().toLowerCase();
				} catch (Exception e) {
					matchLabel = str.nextToken().trim().toLowerCase();
					matchNum = Integer.parseInt(str.nextToken().trim());
				}
				// look in the table
				Iterator<MatchLabel> it = labels.iterator();
				MatchLabel item;
				MatchLabel newLabel = null;
				while (it.hasNext()) {
					item = it.next();
					if (item.getLabel().equalsIgnoreCase(matchLabel)) {
						newLabel = item;
						break;
					}
				}
				if (newLabel == null) {
					// take the default
					if (labels.size() > 0)
						newLabel = labels.get(0);
					else
						newLabel = MatchLabel.blank;
					AppLib.printDebug("setting default match label " + matchLabel);
				}
				teams = new LinkedList<Integer>();
				counts.clear();
				// read some teams!
				for (i = 0; i < ScheduleItem.TPA * 2; i++) {
					tm = str.nextToken().trim();
					j = 0;
					while (j < 1) {
						try {
							// trial parse
							j = Integer.parseInt(tm);
							if (data.get(j) == null && j > 0) j = -j;
						} catch (Exception e) {
							j = 0;
						}
						// oops
						if (j < 1) tm = JOptionPane.showInputDialog(status.getWindow(),
							"Team number \"" + (-j) + "\" is not valid.\n\nPress OK to enter a " +
							"replacement, or Cancel to skip this match.", Integer.toString(j));
						if (tm == null) break;
					}
					// can I use goto here? PLEASE?
					if (tm == null || j < 1) break;
					sur = str.nextToken().trim();
					if (sur.length() != 1) counts.clear(i);
					else {
						ch = Character.toLowerCase(sur.charAt(0));
						if (ch == 'y' || ch == 't') counts.set(i);
						else counts.clear(i);
					}
					teams.add(j);
				}
				if (i < ScheduleItem.TPA * 2) continue;
				// set info and add
				AppLib.printDebug("ScheduleItem[time=" + realTime + ",label=" + matchLabel +
					",num=" + matchNum + ",status=scheduled,teams=" + teams + "]");
				match = new ScheduleItem(teams, realTime, true);
				match.getSurrogate().or(counts);
				match.setLabel(newLabel);
				match.setNum(matchNum);
				list.add(match);
			}
			br.close();
			// really?
			if (list.size() > 0) {
				if (!AppLib.confirm(status.getWindow(), list.size() + " matches were read from " +
					"the file.\nEverything looks OK.\n\nContinue with the import?"))
					return;
				load();
				data.addMatches(list);
			} else {
				AppLib.printWarn(status.getWindow(), "No matches were read from the file." +
					"Suggestions:\n\n1. Check the file contents. Follow the format as in " +
					"example.csv.\n2. Make sure that the file was saved as Comma Separated " +
					"Values or Comma Delimited Values.\n3. Check the date format.");
			}
		} catch (Exception e) {
			AppLib.debugException(e);
			AppLib.printWarn(status.getWindow(), "Could not import the file. Suggestions:\n\n" +
				"1. Check the file contents. Follow the format as in example.csv.\n" +
				"2. Make sure that the file was saved as Comma Separated Values or Comma " +
				"Delimited Values.\n3. Make sure that the file is readable.");
		}
	}

	/**
	 * Class to listen for clicks of "delete match" or
	 *  "edit match" buttons.
	 */
	private class RowListener implements ActionListener {
		/**
		 * The entry to manage.
		 */
		private int entry;

		/**
		 * Creates a new row listener for the given location in the queue.
		 * 
		 * @param ent the entry to manage
		 */
		public RowListener(int ent) {
			entry = ent;
		}
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd == null || !user.canScore()) return;
			else if (cmd.equals("del") && entry >= 0 && entry < queue.size())
				removeQueue(queue.get(entry));
			else if (cmd.equals("edit") && entry >= 0 && entry < queue.size())
				editQueue(queue.get(entry));
		}
	}

	/**
	 * Thread class to play sounds in background.
	 */
	private class SoundPlayerThread extends Thread {
		/**
		 * The index of the clip to be playing.
		 */
		private int clipIndex;
		/**
		 * The side to play: true=blue, false=red
		 */
		private boolean side;

		/**
		 * Creates a new sound playing thread on the given clip index.
		 * 
		 * @param index the index of the clip to play
		 * @param side the side to play
		 */
		public SoundPlayerThread(int index, boolean side) {
			clipIndex = index;
			this.side = side;
			setName("Sound Thread");
			setPriority(Thread.MIN_PRIORITY + 1);
			setDaemon(true);
		}
		public void run() {
			if (untilMatch != null && attention != null && !soundPlaying) {
				// start sound playing, rewind
				soundPlaying = true;
				untilMatch.setFramePosition(0);
				attention.setFramePosition(0);
				allRed.setFramePosition(0);
				allBlue.setFramePosition(0);
				alliance.setFramePosition(0);
				interval[clipIndex].setFramePosition(0);
				// attention!
				attention.start();
				AppLib.sleep(1000L);
				attention.stop();
				// n minutes
				interval[clipIndex].start();
				AppLib.sleep(1100L);
				interval[clipIndex].stop();
				// until match
				untilMatch.start();
				AppLib.sleep(1300L);
				untilMatch.stop();
				AppLib.sleep(700L);
				// red/blue
				if (!side) {
					allRed.start();
					AppLib.sleep(800L);
					allRed.stop();
				} else {
					allBlue.start();
					AppLib.sleep(800L);
					allBlue.stop();
				}
				// alliance
				alliance.start();
				AppLib.sleep(1000L);
				alliance.stop();
				soundPlaying = false;
			}
		}
	}

	/**
	 * Class to listen for clicks of "get info" buttons.
	 */
	private class CommentButtonListener implements ActionListener {
		/**
		 * The index of the entry in the queue to view.
		 */
		private int entry;
		/**
		 * The index of the team in the entry to view.
		 */
		private int index;

		/**
		 * Creates a new (shared) comment button listener.
		 * 
		 * @param ent the entry index
		 * @param ind the team index
		 */
		public CommentButtonListener(int ent, int ind) {
			entry = ent;
			index = ind;
		}
		public void actionPerformed(ActionEvent e) {
			if (entry >= queue.size() || index >= ScheduleItem.TPA * 2)
				matchEntry();
			else if (user.canRead() && kQueue.get(entry) != null) {
				Team team = data.get(kQueue.get(entry).getTeams().get(index));
				AppLib.printDebug("Showing comments for " + team);
				showComments(team);
			}
		}
	}

	/**
	 * Class to listen for key presses.
	 */
	private class UIKeyListener extends KeyAdapter {
		public void keyTyped(KeyEvent e) {
			char pressed = e.getKeyChar();
			char pr = Character.toUpperCase(pressed);
			int ind = iTabs.getSelectedIndex();
			if (pr == 'M') matchEntry();
			if (pr == 'Z' && ind != 0)
				iTabs.setSelectedIndex(0);
			if (pr == 'C' && tabs > 1 && ind != 1)
				iTabs.setSelectedIndex(1);
			if (pr == 'V' && tabs > 2 && ind != 2)
				iTabs.setSelectedIndex(2);
			if (pr == 'B' && tabs > 3 && ind != 3)
				iTabs.setSelectedIndex(3);
			if (pr == 'N' && tabs > 4 && ind != 4)
				iTabs.setSelectedIndex(4);
			if (pr == 'A' && ind == 3 && quan.isSelected()) commitScore();
			if (pressed == ' ' && ind == 3 && quan.isSelected()) loadItem();
			// team select
			if (pressed >= '1' && pressed <= '9' && ind == 3 && quan.isSelected()) {
				int num = pressed - '0';
				if (num > 2 * ScheduleItem.TPA) return;
				select(num);
			}
			if (pr == 'X' && ind == 3 && quan.isSelected()) {
				// penalty!
				score(selectedIndex + (selectedSide ? ScheduleItem.TPA : 0), -1, pr != pressed);
				tally(-1);
			}
			// hotkeys last
			int index = hotkeys.indexOf(pr);
			if (ind == 3 && index >= 0 && data.getData().isAdvScore() && quan.isSelected()) {
				// must be a valid hotkey!
				score(selectedIndex + (selectedSide ? ScheduleItem.TPA : 0), index, pr != pressed);
				tally(-1);
			}
		}
	}

	/**
	 * A class that tries for reconnections.
	 * 
	 * @author Stephen Carlson
	 */
	private class CWThread extends Thread {
		private static final long serialVersionUID = 0L;

		public CWThread() {
			setName("Waiting for Connection");
			setPriority(Thread.MIN_PRIORITY);
			setDaemon(true);
			start();
		}
		public void run() {
			rcDialog.setVisible(true);
			if (!status.isConnected()) status.showClient(false);
		}
	}

	/**
	 * A class that manages indexing for the image uploader.
	 * 
	 * @author Stephen Carlson
	 */
	private class LoaderThread extends Thread {
		private static final long serialVersionUID = 0L;

		/**
		 * Whether the thread is active and running.
		 */
		private volatile boolean running;
		/**
		 * The index to command.
		 */
		private volatile int index;

		/**
		 * Creates a new image indexer thread.
		 */
		public LoaderThread() {
			index = 0;
			setName("Indexer");
			setDaemon(true);
		}
		public void run() {
			while (true) {
				// index
				while (!running) AppLib.sleep(50L);
				setDrive0(index);
				running = false;
			}
		}
		/**
		 * Tells this thread to index the given drive.
		 * 
		 * @param index the drive index
		 */
		public void command(int index) {
			while (running) AppLib.sleep(50L);
			this.index = index;
			running = true;
		}
	}

	/**
	 * Class to listen for button presses.
	 */
	private class LocalEventListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd == null) return;
			if (cmd.equals("s_autherr"))
				status.showClient(false);
			else if (cmd.equals("match")) matchEntry();
			else if (cmd.equals("saveMatch")) doMatchEntry();
			else if (cmd.equals("showall")) updateListQueue();
			else if (cmd.equals("late")) {
				if (late()) updateListQueue();
			} else if (cmd.equals("list"))
				teamLister.setVisible(true);
			else if (cmd.equals("qe"))
				qualEntry();
			else if (cmd.equals("qd"))
				try {
					qd = Integer.parseInt(queueDelay.getText());
					select = true;
					updateListQueue();
				} catch (Exception ex) { }
			else if (cmd.equals("sts"))
				commitComments();
			else if (cmd.equals("load")) {
				if (confirmChange()) return;
				emptyLog();
				loadItem();
				sFocus();
			} else if (cmd.equals("commit") && quan.isSelected()) {
				commitScore();
				sFocus();
			} else if (cmd.equals("closest")) {
				if (confirmChange()) return;
				emptyLog();
				gotoClosest();
				loadItem();
				sFocus();
			} else if (cmd.equals("next")) {
				if (confirmChange()) return;
				emptyLog();
				scoreGo(1);
				sFocus();
			} else if (cmd.equals("prev")) {
				if (confirmChange()) return;
				emptyLog();
				scoreGo(-1);
				sFocus();
			} else if (cmd.equals("imp"))
				importLots();
			else if (cmd.equals("qq"))
				qq(quan.isSelected());
			else if (cmd.equals("custom"))
				setDrive(roots.length);
			else if (cmd.equals("s_conndrop") && running)
				connStat.setStatus(StatusBox.STATUS_SOSO);
			else if (cmd.equals("disconn") && running)
				status.showClient(false);
			else if (cmd.equals("s_connect") && running) {
				if (status.isConnected())
					connStat.setStatus(StatusBox.STATUS_GOOD);
				else {
					connStat.setStatus(StatusBox.STATUS_BAD);
					if (!rcDialog.isVisible()) new CWThread();
				}
			} else if (cmd.equals("s_client")) {
				boolean toShow = status.isClientShowing();
				if (toShow && !running) start();
				else if (!toShow && running) {
					shutdown();
					status.setConnectionStatus(false);
				}
			} else if (cmd.equals("fs"))
				kiosk.show(!kiosk.isShowing());
		}
	}

	/**
	 * Listens for replace button and replaces the team.
	 */
	private class ReplaceListener implements ActionListener {
		/**
		 * The index of the team to replace.
		 */
		private int index;
		/**
		 * The side that it is on.
		 */
		private boolean side;

		/**
		 * Creates a replacement listener for the given side and index.
		 * 
		 * @param ind the index
		 * @param sd the side
		 */
		public ReplaceListener(int ind, boolean sd) {
			index = ind; side = sd;
		}
		public void actionPerformed(ActionEvent e) {
			if (!user.canScore() || toScore == null) return;
			int replacement = 0;
			String cmd = e.getActionCommand();
			if (cmd == null) return;
			if (cmd.equals("red"))
				showComments(data.get(alliance1.get(index).teamNum));
			else if (cmd.equals("blue"))
				showComments(data.get(alliance2.get(index).teamNum));
			else if (side && index < alliance2.size()) {
				if (cmd.equals("edit")) {
					try {
						replacement = Integer.parseInt(JOptionPane.showInputDialog(me,
							"Enter the replacement team number:",
							Integer.toString(alliance2.get(index).teamNum)));
					} catch (Exception ex) {
						return;
					}
					if ((alliance1.indexOf(replacement) >= 0 ||
							alliance2.indexOf(replacement) >= 0) && replacement != 0) {
						AppLib.printWarn(status.getWindow(), "This team is already a member " +
							"of this match.");
						return;
					}
					if (data.get(replacement) != null || replacement == 0) {
						alliance2.get(index).teamNum = replacement;
						sDirty = true;
						updateListQueue();
					} else
						AppLib.printWarn(status.getWindow(), replacement + " is not a valid team.");
				} else if (cmd.equals("del")) {
					alliance2.get(index).teamNum = 0;
					sDirty = true;
					updateListQueue();
				}
			} else if (index < alliance1.size()) {
				if (cmd.equals("edit")) {
					try {
						replacement = Integer.parseInt(JOptionPane.showInputDialog(me,
							"Enter the replacement team number:",
							Integer.toString(alliance1.get(index).teamNum)));
					} catch (Exception ex) {
						return;
					}
					if ((alliance1.indexOf(replacement) >= 0 ||
							alliance2.indexOf(replacement) >= 0) && replacement != 0) {
						AppLib.printWarn(status.getWindow(), "This team is already a member " +
							"of this match.");
						return;
					}
					if (data.get(replacement) != null || replacement == 0) {
						alliance1.get(index).teamNum = replacement;
						sDirty = true;
						updateListQueue();
					} else
						AppLib.printWarn(status.getWindow(), replacement + " is not a valid team.");
				} else if (cmd.equals("del")) {
					alliance1.get(index).teamNum = 0;
					sDirty = true;
					updateListQueue();
				}
			}
			requestFocus();
		}
	}

	/**
	 * Class to only allow non-negative numeric entries into a text box.
	 */
	private class NumericOnlyListener extends KeyAdapter implements FocusListener {
		/**
		 * Whether the listener should tally. True for scores, false for totals.
		 */
		private boolean shouldUpdate = false;
		/**
		 * The index to update.
		 */
		private int index = 0;

		/**
		 * Creates a new numeric-only listener with the given parameters.
		 * 
		 * @param update whether the scores should be tallied
		 * @param ind the index to update, -1 to not update
		 */
		public NumericOnlyListener(boolean update, int ind) {
			shouldUpdate = update;
			index = ind;
		}
		public void keyTyped(KeyEvent e) {
			char pressed = e.getKeyChar();
			if (pressed == 27 || pressed == 'Q' || pressed == 'q')
				requestFocus();
			if ((pressed < '0' || pressed > '9') && pressed != '\b') e.consume();
		}
		public void keyReleased(KeyEvent e) {
			if (shouldUpdate) tally(index);
			else if (index > -1) load(index);
			else if (index == -1) rally();
		}
		public void focusGained(FocusEvent e) {
			// select appropriate number
			if (index >= 0) {
				select(index + 1);
				requestFocus();
			}
		}
		public void focusLost(FocusEvent e) {
		}
	}

	/**
	 * Class to only take JPG, JPE, JPEG, and JFIF files. Also filters
	 *  on names, hidden status, readability, actually a file, and not empty.
	 */
	public static class JpegFilter implements FileFilter {
		public boolean accept(File f) {
			String s = f.getName().toLowerCase();
			return f.canRead() && (s.endsWith(".jpg") || s.endsWith(".jpe") || s.endsWith(".jpeg")
				|| s.endsWith(".jfif")) && !s.startsWith(".") && !s.startsWith("_") &&
				!f.isHidden() && !f.isDirectory() && f.length() > 0;
		}
	}

	/**
	 * Class to only allow non-negative numeric entries into a text box.
	 *  Also combines functionality with EnterKeyListener to do stuff
	 *  when enter is pressed.
	 */
	private class EventListener extends MouseAdapter implements ActionListener,
			ChangeListener, Runnable, KeyListener {
		/**
		 * Uploads a given file. This is only called when sending.
		 */
		public void run() {
			if (toSend == null || upName == null) return;
			load();
			try {
				Socket sock = new Socket(status.getRemoteHost(), Constants.BULK_PORT);
				// write the actual image to temporary file in JPEG format
				File tempFile = File.createTempFile("img", null);
				ImageIO.write(toSend, "jpeg", tempFile);
				InputStream is = new FileInputStream(tempFile);
				// prepare output stream and progress
				int len = (int)tempFile.length();
				OutputStream os = sock.getOutputStream();
				setBytes(len);
				// handle end of line
				String nm = upName + Constants.BULK_DELIM + len;
				for (int i = 0; i < nm.length(); i++)
					os.write((int)nm.charAt(i) % 255);
				os.write(Constants.BULK_EOL);
				AppLib.printDebug("Uploading " + upName);
				// use the mutex on the buffer
				synchronized (buffer) {
					int read, sent = 0;
					while ((read = is.read(buffer)) > 0) {
						// write output to input stream
						os.write(buffer, 0, read);
						sent += read;
						setProgress(sent);
						Thread.sleep(5L);
					}
				}
				// finish off and indicate success
				progress.setString("Done");
				progress.setValue(0);
				AppLib.printDebug("Done uploading " + upName);
				is.close();
				tempFile.delete();
				stopLoad();
				sock.close();
			} catch (Exception e) {
				// send error!
				AppLib.printWarn(status.getWindow(), "Cannot connect to server. Suggestions:" +
					"\n1. Check for firewalls blocking port " + Constants.BULK_PORT +
					".\n2. Check the network connection.\n3. Ensure that the server is running" +
					" and supports image upload mode.\n4. Contact server administrator.");
				AppLib.debugException(e);
			}
		}
		public void stateChanged(ChangeEvent e) {
			select = true;
		}
		public void actionPerformed(ActionEvent e) {
			// Recieves actions from the previous and next buttons.
			String cmd = e.getActionCommand();
			if (cmd.equals("ireload"))
				reload();
			else if (cmd.equals("rotatel")) {
				rotate = (rotate + 3) % 4;
				rotate();
			} else if (cmd.equals("rotater")) {
				rotate = (rotate + 1) % 4;
				rotate();
			} else if (cmd.equals("prev") && index > 0) {
				index--;
				loadImg();
			} else if (cmd.equals("next") && files != null && index < files.length - 1) {
				index++;
				loadImg();
			}
			teamNum.requestFocus();
			teamNum.selectAll();
		}
		public void mouseClicked(MouseEvent e) {
			int i = iList.getSelectedIndex();
			if (i >= 0 && i < files.length) {
				index = i;
				loadImg();
			}
			teamNum.requestFocus();
			teamNum.selectAll();
		}
		public void keyTyped(KeyEvent e) {
			char pressed = e.getKeyChar();
			if (pressed == '\n' || pressed == '\r' || pressed == 'e') {
				if (pressed == 'e') e.consume();
				send();
				teamNum.requestFocus();
				teamNum.selectAll();
			} else if (pressed < '0' || pressed > '9') e.consume();
			if ((pressed == '[' || pressed == '{') && index > 0) {
				index--;
				loadImg();
			} else if ((pressed == ']' || pressed == '}') && files != null &&
					index < files.length - 1) {
				index++;
				loadImg();
			} else if (pressed == '<' || pressed == ',') {
				rotate = (rotate + 3) % 4;
				rotate();
			} else if (pressed == '>' || pressed == '.') {
				rotate = (rotate + 1) % 4;
				rotate();
			}
		}
		public void keyPressed(KeyEvent e) {
		}
		public void keyReleased(KeyEvent e) {
		}
	}

	/**
	 * A class that stores information about a comment made during a match.
	 */
	private class TC {
		/**
		 * The team number.
		 */
		protected int teamNum;
		/**
		 * The content.
		 */
		protected String text;

		public TC(int team, String comment) {
			teamNum = team;
			text = comment;
		}
	}

	/**
	 * A class that stores information about a team in the scorekeeper window.
	 */
	private class AllianceTeam {
		/**
		 * The team's number.
		 */
		protected int teamNum;
		/**
		 * The label used to show the team.
		 */
		protected JButton label;
		/**
		 * The visual scores.
		 */
		protected JTextField[] scores;
		/**
		 * The panel with the scores.
		 */
		protected JComponent panel;
		/**
		 * The visual total.
		 */
		protected JTextField total;
		/**
		 * The real score.
		 */
		protected Score score;
		/**
		 * The penalty count.
		 */
		protected JTextField pen;

		/**
		 * Creates a new alliance team.
		 * 
		 * @param num the team number
		 * @param lbl the visual team number
		 * @param items the visual scores
		 * @param pnl the scoring panel
		 * @param tot the visual total
		 * @param penalty the penalty text box
		 */
		protected AllianceTeam(int num, JButton lbl, JTextField[] items, JComponent pnl,
				JTextField tot, JTextField penalty) {
			teamNum = num;
			label = lbl;
			scores = items;
			panel = pnl;
			total = tot;
			score = null;
			pen = penalty;
		}
		/**
		 * Reinitializes the score.
		 */
		public void rescore() {
			score = new Score(hotkeys.toScoreArray());
		}
		public boolean equals(Object o) {
			if (o == null) return false;
			if (o instanceof AllianceTeam) return ((AllianceTeam)o).teamNum == teamNum;
			if (o instanceof Integer) return ((Integer)o).intValue() == teamNum;
			return false;
		}
		public String toString() {
			return teamNum + " " + score;
		}
	}

	/**
	 * A class that listens for clicks on the image drive buttons.
	 */
	private class DriveListener implements ActionListener {
		/**
		 * The index of the clicked button.
		 */
		private int index;

		/**
		 * Creates a drive listener with the specified index.
		 * 
		 * @param index the index for this listener
		 */
		public DriveListener(int index) {
			super();
			this.index = index;
		}
		public void actionPerformed(ActionEvent e) {
			setDrive(index);
		}
	}

	/**
	 * The kiosk window.
	 */
	private class KioskWindow extends JPanel {
		private static final long serialVersionUID = 0L;

		/**
		 * The actual window for Full-Screen.
		 */
		private JWindow kiosk;
		/**
		 * The kiosk parent.
		 */
		private JFrame parent;
		/**
		 * The last height (used to generate gradient paints)
		 */
		private int kLastHeight;
		/**
		 * The cached gradient paint.
		 */
		private GradientPaint kPaint;
		/**
		 * The kiosk queue.
		 */
		private JPanel kQueueList;
		/**
		 * Kiosk open?
		 */
		private boolean showing;

		/**
		 * Initializes the mini-pit window in the kiosk.
		 */
		private void setupKioskUI() {
			int i, all, ind; JLabel lbl;
			// init list queue first
			int showTeams = 2 * ScheduleItem.TPA * showAlliance;
			kTeamList = new JLabel[showTeams];
			for (i = 0; i < showTeams; i++) {
				all = i / (ScheduleItem.TPA * 2);
				ind = i - (ScheduleItem.TPA * 2 * all);
				// handle the label (actually a button)
				lbl = new AntialiasedJLabel();
				lbl.setBackground(Constants.WHITE);
				if (ind < ScheduleItem.TPA)
					lbl.setForeground(Constants.RED);
				else
					lbl.setForeground(Constants.BLUE);
				lbl.setFont(titleFont);
				kTeamList[i] = lbl;
			}
			// now the main panels
			kencl = new JPanel(new BorderLayout());
			kencl.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
			JComponent pnl;
			// inside panel for background color and layout
			kencl.setBackground(Constants.WHITE);
			// center the next team label
			kNextTeam = new AntialiasedJLabel();
			kNextTeam.setOpaque(false);
			kNextTeam.setFont(titleFont);
			kNextTeam.setHorizontalAlignment(SwingConstants.CENTER);
			kNextTeam.setAlignmentX(JComponent.CENTER_ALIGNMENT);
			// set up the queue
			kQueueList = new JPanel(new VerticalFlow(true, 0, 0));
			kQueueList.setBackground(Constants.WHITE);
			kQueueList.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
			// iterate through array of teams (prep the scoring area)
			for (i = 0; i < showTeams; i++) {
				all = i / (ScheduleItem.TPA * 2);
				ind = i - (ScheduleItem.TPA * 2 * all);
				// add team to box
				pnl = new Box(BoxLayout.X_AXIS);
				pnl.setOpaque(false);
				pnl.add(Box.createHorizontalStrut(50));
				pnl.add(kTeamList[i]);
				pnl.add(Box.createHorizontalGlue());
				pnl.add(Box.createHorizontalStrut(50));
				// add to list
				kQueueList.add(pnl);
				if (i % (ScheduleItem.TPA * 2) == ScheduleItem.TPA * 2 - 1)
					kQueueList.add(Box.createVerticalStrut(10));
			}
			kencl.add(kNextTeam, BorderLayout.NORTH);
			// scroll pane for middle
			JComponent center = new JScrollPane(kQueueList);
			center.setOpaque(false);
			center.setBorder(BorderFactory.createTitledBorder(BorderFactory.
				createLineBorder(Constants.BLACK), "In Queue", TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, null, Constants.BLACK));
			kencl.add(center, BorderLayout.CENTER);
			JButton fs = ButtonFactory.getButton("Close Kiosk", "fs", events, KeyEvent.VK_F);
			fs.setHorizontalAlignment(SwingConstants.CENTER);
			kencl.add(fs, BorderLayout.SOUTH);
		}
		/**
		 * Creates a kiosk window.
		 */
		public KioskWindow() {
			// initial setup
			kLastHeight = -1;
			kPaint = null;
			parent = new JFrame("Scout449 Kiosk");
			parent.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			parent.setLocation(0, 0);
			parent.setSize(AppLib.winInfo.getScreenSize());
			kiosk = new JWindow(parent);
			setupUI();
			Container c = kiosk.getContentPane();
			c.setLayout(new BorderLayout());
			c.add(this, BorderLayout.CENTER);
			kiosk.validate();
			showing = false;
		}
		/**
		 * Initializes the kiosk UI.
		 */
		private void setupUI() {
			AppLib.printDebug("Setting up kiosk UI");
			setLayout(new BorderLayout(0, 40));
			setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
			// each method in turn
			setOpaque(false);
			setupKioskUI();
			// logo
			JComponent pnl = new Box(BoxLayout.X_AXIS);
			pnl.setOpaque(false);
			JLabel logo = new JLabel(status.getIcon("scout449"));
			pnl.add(logo);
			// until next match
			kioskClock = new CountdownClock(status, 64);
			kRunLate = new AntialiasedJLabel("on time");
			kRunLate.setForeground(Constants.WHITE);
			pnl.add(Box.createHorizontalGlue());
			pnl.add(module(kioskClock, kRunLate));
			pnl.add(Box.createHorizontalStrut(20));
			// next teams
			kFirstTeams = new JLabel[ScheduleItem.TPA * 2];
			JLabel lbl;
			JPanel v2 = new JPanel(new GridLayout(2, 0, 3, 2));
			v2.setOpaque(false);
			v2.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(Constants.WHITE),
				BorderFactory.createEmptyBorder(5, 10, 5, 0)));
			lbl = new AntialiasedJLabel("red   ");
			lbl.setForeground(Constants.RED);
			lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
			lbl.setFont(textFont);
			v2.add(lbl);
			// red
			for (int i = 0; i < ScheduleItem.TPA; i++) {
				lbl = new AntialiasedJLabel("---");
				lbl.setForeground(Constants.WHITE);
				lbl.setFont(textFont);
				kFirstTeams[i] = lbl;
				v2.add(lbl);
			}
			lbl = new AntialiasedJLabel("blue   ");
			lbl.setForeground(Constants.BLUE);
			lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
			lbl.setFont(textFont);
			v2.add(lbl);
			// blue
			for (int i = 0; i < ScheduleItem.TPA; i++) {
				lbl = new AntialiasedJLabel("---");
				lbl.setForeground(Constants.WHITE);
				lbl.setFont(textFont);
				kFirstTeams[i + ScheduleItem.TPA] = lbl;
				v2.add(lbl);
			}
			// add it up
			v2.setMaximumSize(v2.getPreferredSize());
			pnl.add(v2);
			pnl.add(Box.createHorizontalGlue());
			add(pnl, BorderLayout.NORTH);
			add(kencl, BorderLayout.CENTER);
			kPaint = null;
			kLastHeight = 0;
		}
		public void paint(Graphics g) {
			int h = getHeight();
			if (kLastHeight != h) {
				kPaint = new GradientPaint(0, 0, Constants.PURPLE, 0, h, Constants.WHITE);
				kLastHeight = h;
			}
			Graphics2D g2d = (Graphics2D)g;
			g2d.setPaint(kPaint);
			g2d.fillRect(0, 0, getWidth(), getHeight());
			paintChildren(g);
		}
		public void update(Graphics g) {
			paint(g);
		}
		/**
		 * Gets the kiosk window.
		 * 
		 * @return a Window with the kiosk contents
		 */
		public Window getWindow() {
			return kiosk;
		}
		/**
		 * Changes the next team on the kiosk big display.
		 * 
		 * @param name the name of the team to show
		 */
		private void setDisplay(String name) {
			if (name.length() > 0)
				kNextTeam.setText(name);
			else
				kNextTeam.setText("Queue Empty");
		}
		/**
		 * Determines whether the kiosk is showing.
		 *
		 * @return whether the kiosk the showing
		 */
		public boolean isShowing() {
			return showing;
		}
		/**
		 * Shows or hides the kiosk.
		 *
		 * @param showing whether the kiosk should be shown
		 */
		public void show(boolean showing) {
			parent.setVisible(showing);
			if (!this.showing && showing)
				GraphicsEnvironment.getLocalGraphicsEnvironment().
				getDefaultScreenDevice().setFullScreenWindow(getWindow());
			else if (this.showing && !showing)
				GraphicsEnvironment.getLocalGraphicsEnvironment().
				getDefaultScreenDevice().setFullScreenWindow(null);
			this.showing = showing;
			status.getWindow().setVisible(!showing);
			if (showing) kiosk.repaint();
		}
	}

	/**
	 * A class that holds data about the cached images.
	 * 
	 * @author Stephen Carlson
	 */
	private class ImageData {
		/**
		 * The image.
		 */
		protected ImageIcon image;
		/**
		 * When the image was added; used to flush.
		 */
		protected long timeAdded;

		/**
		 * Creates a new image data object on the given image icon.
		 * 
		 * @param image the image icon with the image
		 */
		public ImageData(ImageIcon image) {
			this.image = image;
			timeAdded = date;
		}
	}

	/**
	 * A class that displays the list of images.
	 * 
	 * @author Stephen Carlson
	 */
	private class ImageListModel extends AbstractListModel {
		private static final long serialVersionUID = 0L;

		public Object getElementAt(int index) {
			if (files != null && index >= 0 && index < files.length)
				return files[index].getName();
			return null;
		}
		public int getSize() {
			if (files != null) return files.length;
			return 0;
		}
		public void fireContentsChanged(int index0, int index1) {
			super.fireContentsChanged(this, index0, index1);
		}
	}
}