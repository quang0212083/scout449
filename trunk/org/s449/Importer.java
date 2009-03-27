package org.s449;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import org.s449.HotkeyList.Hotkey;
import org.s449.ui.VerticalFlow;

/**
 * Imports a list of teams into the new file format.
 * 
 * TODO add internet fetch. Here's the reasons why it isn't there already:
 *  1. New teams don't have names on TheBlueAlliance. Old teams are already in the
 *   backend. Therefore, no gains when fetching from net when there's already data.
 *  2. Lots of internet fetching gets TheBlueAlliance.net angry at us.
 * 
 * @author Stephen Carlson
 * @version 4.0.0
 */
public class Importer {
	/**
	 * The allowable hotkeys.
	 */
	private static final String hkAllow = "DEFGHIJKLOPQRTUWY";
	/**
	 * Available permissions.
	 */
	private static final String[] perms = new String[] {
		"Kiosk Only", "Read Only", "Regular User", "Scorer", "Administrator"
	};

	/**
	 * The server name.
	 */
	private JTextField sName;
	/**
	 * The scouting data file.
	 */
	private String scoutName;
	/**
	 * The configuration file.
	 */
	private UserFile config;
	/**
	 * The input file selector.
	 */
	private FileSelector teamInput;
	/**
	 * The text field for team number.
	 */
	private JTextField myTeam;
	/**
	 * The default match spacing.
	 */
	private JTextField mSpace;
	/**
	 * The time display setting.
	 */
	private JComboBox timeDisplay;
	/**
	 * The UDF names.
	 */
	private EditableList<UDF> udfNames;
	/**
	 * The robot types.
	 */
	private EditableList<String> rTypes;
	/**
	 * The labels.
	 */
	private EditableList<MatchLabel> mLabels;
	/**
	 * The list of hotkeys.
	 */
	private EditableList<Hotkey> hotkeyList;
	/**
	 * The combo box to select regionals.
	 */
	private JComboBox regionals;
	/**
	 * The data model.
	 */
	private RegionalModel rModel;
	/**
	 * The list of regionals.
	 */
	private List<Regional> regionalList;
	/**
	 * The current regional.
	 */
	private Regional current;
	/**
	 * The starting date.
	 */
	private JTextField start;
	/**
	 * The ending date.
	 */
	private JTextField end;
	/**
	 * Advanced scoring?
	 */
	private JCheckBox advScore;
	/**
	 * The window.
	 */
	private JFrame win;
	/**
	 * The parent ScoutStatus object.
	 */
	private ScoutStatus status;
	/**
	 * Running?
	 */
	private boolean running;
	/**
	 * The login user.
	 */
	private UserData user;
	/**
	 * The user table.
	 */
	private UserList uTable;

	/**
	 * Creates a new importer.
	 * 
	 * @param stat the ScoutStatus responsible for this object
	 */
	public Importer(ScoutStatus stat) {
		status = stat;
		AppLib.printDebug("Initializing importer");
		Map<String, String> input = stat.getMaster().getCommandLine();
		scoutName = Server.defaultScoutName;
		String configName = Server.defaultConfigName;
		// check for scout data file change
		String param = input.get("-f");
		if (param != null && (param = param.trim()).length() > 0)
			scoutName = param;
		// check for config file change
		param = input.get("-c");
		if (param != null && (param = param.trim()).length() > 0)
			configName = param;
		config = new UserFile(configName);
		running = false;
		setupUI();
	}
	/**
	 * Sets the login user, if not running.
	 * 
	 * @param user the user with admin rights to configure
	 */
	public void setUser(UserData user) {
		if (!running) this.user = user;
	}
	/**
	 * Configures the program.
	 */
	public void start() {
		if (user == null || (user = config.getData().authUser(user)) == null
				|| !user.isAdmin()) {
			user = null;
			status.sendMessage("cautherr");
			return;
		}
		current = null;
		tryLoadData();
		win.setVisible(true);
		running = true;
	}
	/**
	 * Initializes the user interface.
	 */
	private void setupUI() {
		LocalEventListener events = new LocalEventListener();
		regionalList = new ArrayList<Regional>(3);
		// GUI window
		AppLib.printDebug("Loading configuration GUI");
		win = new JFrame("Configuration");
		win.setIconImage(status.getImage("winicon"));
		win.setResizable(false);
		win.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		Container c = win.getContentPane();
		c.setLayout(new BorderLayout());
		JLabel lbl = new JLabel("Configuring Local Server");
		lbl.setFont(Client.titleFont);
		lbl.setForeground(Constants.DARK_BLUE);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		c.add(lbl, BorderLayout.NORTH);
		JTabbedPane tabs = new JTabbedPane();
		JPanel tab = new JPanel(new VerticalFlow(false));
		// team data
		JComponent vert = new Box(BoxLayout.Y_AXIS);
		vert.setBorder(BorderFactory.createTitledBorder("Team List"));
		// the combo box, add, del
		JPanel horiz = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		horiz.add(new JLabel("Regional: "));
		rModel = new RegionalModel();
		regionals = new JComboBox(rModel);
		regionals.setActionCommand("region");
		regionals.setPrototypeDisplayValue("RegionalRegionalRegionalRegional");
		regionals.addActionListener(events);
		regionals.setEditable(false);
		// add it up
		horiz.add(regionals);
		JButton addRegion = new JButton();
		addRegion.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		addRegion.setIcon(status.getIcon("plus"));
		addRegion.setActionCommand("add");
		addRegion.addActionListener(events);
		horiz.add(addRegion);
		JButton delRegion = new JButton();
		delRegion.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		delRegion.setIcon(status.getIcon("minus"));
		delRegion.setActionCommand("del");
		delRegion.addActionListener(events);
		horiz.add(delRegion);
		vert.add(horiz);
		// start & end dates
		horiz = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		horiz.add(new JLabel("Start Date (mm/dd/yy): "));
		start = new JTextField(9);
		horiz.add(start);
		horiz.add(new JLabel("    End Date: "));
		end = new JTextField(9);
		horiz.add(end);
		vert.add(horiz);
		// the actual file selector
		horiz = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		teamInput = new FileSelector("Team Data File:", false);
		teamInput.setFileFilter(new ExtensionFileFilter("csv", "Comma Separated Values"));
		horiz.add(teamInput);
		vert.add(horiz);
		tab.add(vert);
		advScore = new JCheckBox("Advanced Scoring?");
		advScore.setFocusable(false);
		advScore.setActionCommand("as");
		advScore.addActionListener(events);
		advScore.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		tab.add(advScore);
		// UDF names
		horiz = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		udfNames = new EditableList<UDF>(status);
		udfNames.setBorder(BorderFactory.createTitledBorder("User-Defined Fields"));
		udfNames.setActionCommand("udf");
		udfNames.addActionListener(events);
		horiz.add(udfNames);
		// hotkeys
		hotkeyList = new EditableList<Hotkey>(status);
		hotkeyList.setBorder(BorderFactory.createTitledBorder("Hot Keys"));
		hotkeyList.setActionCommand("hotkey");
		hotkeyList.addActionListener(events);
		hotkeyList.setEnabled(false);
		horiz.add(hotkeyList);
		tab.add(horiz);
		// robot types
		rTypes = new EditableList<String>(status);
		rTypes.setBorder(BorderFactory.createTitledBorder("Robot Types"));
		rTypes.setActionCommand("rtype");
		rTypes.addActionListener(events);
		tab.add(rTypes);
		tabs.addTab("Year Config", tab);
		// user table
		tab = new JPanel(new VerticalFlow(false));
		uTable = new UserList(status);
		uTable.setActionCommand("user");
		uTable.addActionListener(events);
		tab.add(uTable);
		horiz = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		JButton cPass = new JButton("Change Password");
		cPass.setActionCommand("cpass");
		cPass.addActionListener(events);
		horiz.add(cPass);
		JButton cPerm = new JButton("Change Permissions");
		cPerm.setActionCommand("cperm");
		cPerm.addActionListener(events);
		horiz.add(cPerm);
		JButton cName = new JButton("Change Real Name");
		cName.setActionCommand("cname");
		cName.addActionListener(events);
		horiz.add(cName);
		tab.add(horiz);
		tabs.addTab("User Config", tab);
		tab = new JPanel(new VerticalFlow(false));
		// team number
		horiz = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		horiz.add(new JLabel("Your team number: "));
		myTeam = new JTextField(4);
		horiz.add(myTeam);
		// match spacing
		horiz.add(new JLabel("    Default Match Spacing (minutes): "));
		mSpace = new JTextField(4);
		horiz.add(mSpace);
		tab.add(horiz);
		// time 12/24
		horiz = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		horiz.add(new JLabel("Time Display: "));
		timeDisplay = new JComboBox();
		timeDisplay.setEditable(false);
		timeDisplay.addItem("12 Hour (3:00 PM)");
		timeDisplay.addItem("24 Hour (15:00)");
		horiz.add(timeDisplay);
		// server name
		horiz.add(new JLabel("    Server Name: "));
		sName = new JTextField(16);
		horiz.add(sName);
		tab.add(horiz);
		// match labels
		mLabels = new EditableList<MatchLabel>(status);
		mLabels.setActionCommand("label");
		mLabels.addActionListener(events);
		mLabels.setBorder(BorderFactory.createTitledBorder("Match Labels"));
		tab.add(mLabels);
		tabs.addTab("Other", tab);
		c.add(tabs, BorderLayout.CENTER);
		if (status.isServerRunning())
			tabs.setSelectedIndex(1);
		// bottom buttons
		JButton ok = new JButton("Import Data");
		ok.setActionCommand("write");
		ok.setEnabled(!status.isServerRunning());
		ok.addActionListener(events);
		JButton uok = new JButton("Save Users");
		uok.setActionCommand("writeusers");
		uok.addActionListener(events);
		JButton clr = new JButton("Clear");
		clr.setActionCommand("clear");
		clr.setEnabled(!status.isServerRunning());
		clr.addActionListener(events);
		JButton exit = new JButton("Close");
		exit.setActionCommand("bye");
		exit.addActionListener(events);
		horiz = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		horiz.add(ok);
		horiz.add(uok);
		horiz.add(clr);
		horiz.add(exit);
		// pack it up!
		c.add(horiz, BorderLayout.SOUTH);
		win.pack();
		// add match items
		Dimension screenSize = AppLib.winInfo.getScreenSize();
		win.setLocation((screenSize.width - win.getWidth()) / 2,
			(screenSize.height - win.getHeight()) / 2);
	}
	/**
	 * Erases everything!
	 */
	private void clear() {
		if (status.isServerRunning()) return;
		int result = JOptionPane.showConfirmDialog(null, "This will delete the contents of " +
			"every field on the screen!\n\nContinue?",
			"Import", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (result != JOptionPane.YES_OPTION) return;
		mLabels.clear();
		mLabels.addItem(new MatchLabel("Qualifications", true));
		mLabels.addItem(new MatchLabel("Practice", false));
		mLabels.addItem(new MatchLabel("Quarterfinals", true));
		mLabels.addItem(new MatchLabel("Semifinals", true));
		mLabels.addItem(new MatchLabel("Finals", true));
		teamInput.setFile(null);
		rTypes.clear();
		udfNames.clear();
		hotkeyList.clear();
		mSpace.setText("6");
		myTeam.setText(Integer.toString(Constants.DEFAULT_TEAM));
		timeDisplay.setSelectedIndex(0);
		rModel.clear();
		current = null;
		start.setText("");
		end.setText("");
	}
	/**
	 * Tries to load existing data if it is there.
	 */
	private void tryLoadData() {
		try {
			// attempt user reading
			AppLib.printDebug("Opening config");
			Iterator<UserData> it = config.getData().getUsers().values().iterator();
			uTable.clear();
			while (it.hasNext())
				uTable.addItem(it.next());
			sName.setText(config.getData().getName());
			// attempt data reading
			AppLib.printDebug("Opening backend");
			Backend back = new FileBackend(scoutName);
			back.selfCheck();
			DataStore data = back.getData();
			Object[] config = data.getExtraData();
			myTeam.setText(Integer.toString(data.getMyTeam()));
			advScore.setSelected(data.isAdvScore());
			if (config.length > 2) {
				// old config
				AppLib.printDebug("Using old config");
				mSpace.setText(Integer.toString((Integer)config[1]));
				int ind = (Integer)config[2] / 12 - 1;
				if (ind == 0 || ind == 1) timeDisplay.setSelectedIndex(ind);
				udfNames.setList(data.getUDFs());
				hotkeyList.setList(new ArrayList<Hotkey>(data.getHotkeys().getList()));
				mLabels.setList(data.getLabels());
				rTypes.setList(data.getTypes());
			} else
				// load config defaults
				throw new UnsupportedOperationException("No config; loading defaults");
			// load events
			Iterator<Event> it2 = data.getEvents().iterator();
			while (it2.hasNext()) {
				Regional r = new Regional(it2.next());
				r.end -= 86400000L;
				rModel.addItem(r);
			}
			if (regionalList.size() > 0) {
				current = regionalList.get(0);
				rModel.setSelectedItem(current);
				load();
			}
			AppLib.printDebug("Done loading config");
		} catch (Exception e) {
			if (!(e instanceof UnsupportedOperationException)) AppLib.debugException(e);
			AppLib.printDebug("Loading defaults");
			advScore.setSelected(false);
			// defaults!
			mLabels.addItem(new MatchLabel("Qualifications", true));
			mLabels.addItem(new MatchLabel("Practice", false));
			mLabels.addItem(new MatchLabel("Quarterfinals", true));
			mLabels.addItem(new MatchLabel("Semifinals", true));
			mLabels.addItem(new MatchLabel("Finals", true));
			rTypes.clear();
			udfNames.clear();
			hotkeyList.clear();
			mSpace.setText("6");
			myTeam.setText(Integer.toString(Constants.DEFAULT_TEAM));
			timeDisplay.setSelectedIndex(0);
			rModel.clear();
			sName.setText("default");
		}
		hotkeyList.setEnabled(advScore.isSelected());
	}
	/**
	 * Writes the data to the file.
	 */
	private void writeFile() {
		if (status.isServerRunning()) return;
		try {
			if (current == null || regionalList.size() < 1) {
				AppLib.printWarn(win, "Please specify at least one regional.");
				return;
			}
			save();
			// are you sure???
			StringTokenizer teamData;
			int result = JOptionPane.showConfirmDialog(null, "This will import teams from the " +
				"specified files\nand will delete ALL data currently stored!\n\nContinue?",
				"Import", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (result != JOptionPane.YES_OPTION) return;
			AppLib.printDebug("Starting import");
			// prepare output
			File scouting = new File(scoutName);
			scouting.delete();
			scouting.createNewFile();
			// start schedule and data store
			AppLib.printDebug("Initializing schedule");
			TreeMap<Integer, Team> map;
			TreeMap<SecondTime, ScheduleItem> sched;
			DataStore data = new DataStore(new ArrayList<Event>(4));
			data.setAdvScore(advScore.isSelected());
			String sd = myTeam.getText();
			// verify the team number
			if (!AppLib.positiveInteger(sd)) {
				AppLib.printWarn(win, "Team number must be a positive integer.");
				return;
			}
			data.setMyTeam(Integer.parseInt(myTeam.getText()));
			sd = mSpace.getText();
			if (!AppLib.positiveInteger(sd)) {
				AppLib.printWarn(win, "Match spacing must be a positive integer.");
				return;
			}
			AppLib.printDebug("Writing hotkeys");
			HotkeyList hkl = new HotkeyList();
			// handle hotkeys
			if (data.isAdvScore()) hkl.setList(hotkeyList.getList());
			data.setHotkeys(hkl);
			// compute udf weights and udfs
			AppLib.printDebug("Writing UDFs");
			List<UDF> udfs = udfNames.getList();
			data.setUDFs(udfs);
			// load robot types and match labels
			AppLib.printDebug("Writing labels and types");
			List<String> types = rTypes.getList();
			if (!types.contains("other") && !types.contains("Other")) types.add("Other");
			data.setTypes(types);
			List<MatchLabel> lbls = mLabels.getList();
			if (lbls.size() < 1) {
				lbls = new ArrayList<MatchLabel>();
				lbls.add(new MatchLabel("Qualifications", true));
				lbls.add(new MatchLabel("Practice", false));
				lbls.add(new MatchLabel("Quarterfinals", true));
				lbls.add(new MatchLabel("Semifinals", true));
				lbls.add(new MatchLabel("Finals", true));
			}
			data.setLabels(lbls);
			// read the file
			BufferedReader br;
			int teamNum;
			String teamName;
			Team team;
			Regional r;
			Iterator<Regional> it = regionalList.iterator();
			while (it.hasNext()) {
				r = it.next();
				// write the regional to a new event
				AppLib.printDebug("Writing regional " + r);
				map = new TreeMap<Integer, Team>();
				sched = new TreeMap<SecondTime, ScheduleItem>();
				if (r.file != null) {
					br = new BufferedReader(new FileReader(r.file));
					while (br.ready()) {
						// parse out the team number and name
						teamData = new StringTokenizer(br.readLine(), ",");
						teamNum = Integer.parseInt(teamData.nextToken().trim());
						// create team and put away
						teamName = teamData.nextToken().trim();
						team = new Team(teamName, teamNum, udfs.size());
						map.put(teamNum, team);
					}
					br.close();
				} else if (r.based != null) {
					// from the earlier event, but cleared off of all stats, etc.
					Map<Integer, Team> tms = r.based.getTeams();
					Iterator<Integer> it2 = tms.keySet().iterator();
					int nTeam;
					while (it2.hasNext()) {
						nTeam = it2.next();
						map.put(nTeam, new Team(tms.get(nTeam).getName(), nTeam, udfs.size()));
					}
				} else throw new UnsupportedOperationException("no file or base");
				data.getEvents().add(new Event(sched, map, r.code, r.name, r.start,
					r.end + 86400000L));
			}
			AppLib.printDebug("Final configuration");
			long t = System.currentTimeMillis();
			// Time configured, match spacing, time in 24hr or 12hr, array of labels.
			data.setExtraData(new Object[] { t, Integer.valueOf(sd),
				timeDisplay.getSelectedIndex() * 12 + 12 });
			// output writer
			AppLib.printDebug("Writing to data");
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(scoutName));
			oos.writeUnshared(data);
			oos.close();
			// all done!
			AppLib.printWarn(win, "Import successful.");
			status.sendMessage("import");
		} catch (Exception e) {
			AppLib.debugException(e);
			AppLib.printWarn(win, "Could not import! Suggestions:\n1. Check" +
				"the validity of the form  data.\n2. Check the team data file.\n" +
				"3. Start over from the beginning.");
		}
	}
	// Writes only the users file.
	private void writeUsers() {
		String myName = sName.getText();
		if (myName == null || myName.length() < 1) {
			AppLib.printWarn(win, "Server name cannot be empty.");
			return;
		}
		// user data
		AppLib.printDebug("Writing to user config");
		UserStore us = config.getData();
		us.getUsers().clear();
		Iterator<UserData> it2 = uTable.getList().iterator();
		UserData u;
		while (it2.hasNext()) {
			u = it2.next();
			us.setUserData(u.getName(), u);
		}
		us.setName(myName);
		config.update();
		AppLib.printWarn(win, "User information saved.");
		status.sendMessage("iusers");
	}
	/**
	 * Saves away the current regional.
	 */
	private boolean save() {
		if (current != null) {
			AppLib.printDebug("Saving regional " + current);
			if (teamInput.getFile() == null && current.based == null) {
				// reject blank file field
				AppLib.printWarn(win, "Please specify a team data file name.");
				regionals.setActionCommand("notyet");
				regionals.setSelectedItem(current);
				regionals.setActionCommand("region");
				return false;
			}
			current.file = teamInput.getFile();
			try {
				// save dates
				current.start = Constants.DATE_FORMAT.parse(start.getText()).getTime();
				current.end = Constants.DATE_FORMAT.parse(end.getText()).getTime();
				if (current.start / 1000 >= current.end / 1000)
					throw new RuntimeException("no regional length");
			} catch (Exception e1) {
				// error. sick hack too!
				AppLib.printWarn(win, "Please specify a valid start and end date.");
				regionals.setActionCommand("notyet");
				regionals.setSelectedItem(current);
				regionals.setActionCommand("region");
				return false;
			}
			AppLib.printDebug("Finished saving");
		}
		return true;
	}
	/**
	 * Loads the current data.
	 */
	private void load() {
		if (current != null) {
			AppLib.printDebug("Loading regional " + current);
			teamInput.setFile(current.file);
			start.setText(Constants.DATE_FORMAT.format(new Date(current.start)));
			end.setText(Constants.DATE_FORMAT.format(new Date(current.end)));
		}
	}
	/**
	 * Shows a password dialog.
	 * 
	 * @param prompt the prompt message
	 * @return the password entered, or null if canceled
	 */
	private char[] showPasswordDialog(String prompt) {
		JPasswordField pass = new JPasswordField();
		pass.setFont(mSpace.getFont());
		pass.setEchoChar('*');
		JPanel panel = new JPanel(new VerticalFlow(true));
		panel.add(new JLabel(prompt));
		panel.add(pass);
		pass.grabFocus();
		int op = JOptionPane.showConfirmDialog(win, panel, "User", JOptionPane.OK_CANCEL_OPTION);
		if (op != JOptionPane.OK_OPTION) return null;
		return pass.getPassword();
	}
	/**
	 * Gets a permission from the user.
	 * 
	 * @return the permission chosen, or -1 if canceled
	 */
	private int getPermission() {
		String op = (String)JOptionPane.showInputDialog(win, "Select permissions:", "User",
			JOptionPane.QUESTION_MESSAGE, null, perms, perms[2]);
		if (op == null) return -1;
		op = op.toLowerCase().trim();
		if (op.equals("administrator")) return UserData.accessMask(true, true, true, true);
		if (op.equals("scorer")) return UserData.accessMask(true, true, true, false);
		if (op.equals("regular user")) return UserData.accessMask(true, true, false, false);
		if (op.equals("read only")) return UserData.accessMask(true, false, false, false);
		if (op.equals("kiosk only")) return UserData.accessMask(false, false, false, false);
		return -1;
	}

	/**
	 * The local event listener listens for adding items and buttons.
	 */
	private class LocalEventListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd == null) return;
			else if (cmd.equals("udf")) {
				// udf name
				String in = JOptionPane.showInputDialog(win, "Enter UDF name:", "UDF",
					JOptionPane.QUESTION_MESSAGE);
				if (in != null && in.length() > 0) udfNames.addItem(new UDF(in, UDF.RATE10));
			} else if (cmd.equals("rtype")) {
				// robot type
				String in = JOptionPane.showInputDialog(win, "Enter robot type:", "Robot Type",
					JOptionPane.QUESTION_MESSAGE);
				if (in != null && in.length() > 0) rTypes.addItem(in);
			} else if (cmd.equals("label")) {
				// match label
				String in = JOptionPane.showInputDialog(win, "Enter match label:", "Match Label",
					JOptionPane.QUESTION_MESSAGE);
				boolean count = AppLib.confirm(win, "Does it count?");
				if (in != null && in.length() > 0) mLabels.addItem(new MatchLabel(in, count));
			} else if (cmd.equals("hotkey")) {
				// hotkeys
				char ch = 'a'; int num = 0; String name = null;
				String in = JOptionPane.showInputDialog(win, "Enter hot key character:", "Hotkey",
					JOptionPane.QUESTION_MESSAGE);
				if (in != null && in.length() == 1) ch = in.charAt(0);
				else return;
				if (hkAllow.indexOf(ch) < 0) {
					// reserved!
					AppLib.printWarn(win, "This hot key character is reserved.");
					return;
				}
				// get the description
				in = JOptionPane.showInputDialog(win, "Enter hot key description:", "Hotkey",
					JOptionPane.QUESTION_MESSAGE);
				if (in != null && in.length() > 0) name = in;
				else return;
				// points
				in = JOptionPane.showInputDialog(win, "Enter points to add/subtract:", "Hotkey",
					JOptionPane.QUESTION_MESSAGE);
				if (in != null && in.length() > 0) {
					if (AppLib.validInteger(in))
						num = Integer.parseInt(in);
					else {
						// invalid number
						AppLib.printWarn(win, "This value is not an integer.");
						return;
					}
				} else return;
				hotkeyList.addItem(new Hotkey(ch, name, num));
			} else if (cmd.equals("user")) {
				// add user
				String name = null, real = null; int num = 0;
				String in = JOptionPane.showInputDialog(win, "Enter login name:", "User",
					JOptionPane.QUESTION_MESSAGE);
				if (in != null && in.length() > 0) name = in;
				else return;
				Iterator<UserData> it = uTable.getList().iterator();
				while (it.hasNext())
					if (it.next().getName().equalsIgnoreCase(in)) {
						AppLib.printWarn(win, "This user already exists.");
						return;
					}
				in = JOptionPane.showInputDialog(win, "Enter real name:", "User",
					JOptionPane.QUESTION_MESSAGE);
				if (in != null && in.length() > 0) real = in;
				else return;
				char[] pass = showPasswordDialog("Enter password:");
				if (pass == null) return;
				int perm = getPermission();
				if (perm < 0) return;
				in = JOptionPane.showInputDialog(win, "Enter team number:", "User",
					JOptionPane.QUESTION_MESSAGE);
				if (in != null && in.length() > 0) {
					if (AppLib.validInteger(in))
						num = Integer.parseInt(in);
					else {
						// invalid number
						AppLib.printWarn(win, "This value is not an integer.");
						return;
					}
				} else return;
				uTable.addItem(new UserData(name, real, pass, perm, num));
			} else if (cmd.equals("cpass")) {
				// change password
				UserData u = uTable.getSelectedItem();
				if (u == null) return;
				char[] pass = showPasswordDialog("Enter new password:");
				if (pass == null) return;
				u.setPass(UserData.hash(pass));
			} else if (cmd.equals("cname")) {
				// change real name
				UserData u = uTable.getSelectedItem();
				if (u.getName().equalsIgnoreCase("admin")) {
					AppLib.printWarn(win, "The admin user cannot be modified.");
					return;
				}
				String in = JOptionPane.showInputDialog(win, "Enter real name:", "User",
					JOptionPane.QUESTION_MESSAGE);
				if (in == null || in.length() < 1) return;
				u.setRealName(in);
				uTable.model.fireContentsChanged(0, uTable.getList().size());
			} else if (cmd.equals("cperm")) {
				// change permissions
				UserData u = uTable.getSelectedItem();
				if (u.getName().equalsIgnoreCase("admin")) {
					AppLib.printWarn(win, "The admin user cannot be modified.");
					return;
				}
				int perm = getPermission();
				if (perm < 0) return;
				u.setAccess(perm);
			} else if (cmd.equals("region")) {
				// set regional
				if (regionals.getSelectedIndex() < 0 || !save()) return;
				current = regionalList.get(regionals.getSelectedIndex());
				load();
			} else if (cmd.equals("add")) {
				if (!save()) return;
				String name = null, code = null;
				// add regional
				String in = JOptionPane.showInputDialog(win, "Enter regional name:", "Regional",
					JOptionPane.QUESTION_MESSAGE);
				if (in != null && in.length() > 0) name = in;
				else return;
				in = JOptionPane.showInputDialog(win, "Enter 2 letter regional code\n(this can " +
					"be found on the FIRST website):", "Regional", JOptionPane.QUESTION_MESSAGE);
				if (in != null && in.length() == 2) code = in;
				else return;
				current = new Regional(name, code);
				current.end = current.start = System.currentTimeMillis();
				regionals.setActionCommand("notyet");
				rModel.addItem(current);
				regionals.setSelectedIndex(regionalList.size() - 1);
				regionals.setActionCommand("region");
				load();
			} else if (cmd.equals("del")) {
				// remove regional
				if (current == null || regionalList.size() < 1) return;
				int result = JOptionPane.showConfirmDialog(null, "This will remove the current " +
					"regional permanently.\nContinue?", "Import",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (result != JOptionPane.YES_OPTION) return;
				int index = regionals.getSelectedIndex();
				regionals.setActionCommand("notyet");
				rModel.removeItem(index);
				if (regionalList.size() > 0) {
					current = regionalList.get(0);
					regionals.setSelectedIndex(0);
					load();
				} else {
					current = null;
					teamInput.setFile(null);
					start.setText("");
					end.setText("");
				}
				regionals.setActionCommand("region");
			} else if (cmd.equals("write"))
				// save it!
				writeFile();
			else if (cmd.equals("as"))
				// advanced score toggle
				hotkeyList.setEnabled(advScore.isSelected());
			else if (cmd.equals("writeusers"))
				// save the users!
				writeUsers();
			else if (cmd.equals("clear"))
				// get rid of it all
				clear();
			else if (cmd.equals("bye")) {
				// Good-bye.
				win.setVisible(false);
				win.dispose();
			}
		}
	}

	/**
	 * A class describing a regional.
	 */
	private class Regional {
		public String name;
		public String code;
		public File file;
		public Event based;
		public long start;
		public long end;

		public Regional(String name, String code) {
			this.name = name;
			this.code = code.toLowerCase();
			file = null;
			start = end = System.currentTimeMillis();
			based = null;
		}
		public Regional(Event based) {
			this.name = based.getName();
			this.code = based.getCode();
			file = null;
			start = based.getStartDate();
			end = based.getEndDate();
			this.based = based;
		}
		public String toString() {
			return name + " (" + code + ")";
		}
		public boolean equals(Object o) {
			if (!(o instanceof Regional)) return false;
			Regional r = (Regional)o;
			return r.name.equals(name) && r.code.equals(code);
		}
	}

	/**
	 * A class for the regional list.
	 */
	private class RegionalModel extends AbstractListModel implements ComboBoxModel {
		private static final long serialVersionUID = 0L;
		private Object selectedItem = null;

		public Object getSelectedItem() {
			return selectedItem;
		}
		public void setSelectedItem(Object anItem) {
			selectedItem = anItem;
		}
		public Object getElementAt(int index) {
			if (index >= getSize())
				return "";
			else
				return regionalList.get(index);
		}
		public int getSize() {
			return regionalList.size();
		}
		public void addItem(Regional item) {
			regionalList.add(item);
			fireIntervalAdded(this, regionalList.size() - 1, regionalList.size() - 1);
		}
		public void removeItem(int index) {
			regionalList.remove(index);
			fireIntervalRemoved(this, index, index);
		}
		public void clear() {
			int oSize = regionalList.size();
			regionalList.clear();
			fireIntervalRemoved(this, 0, oSize);
		}
	}

	/**
	 * A class that slightly modifies EditableList to represent users.
	 */
	private class UserList extends EditableList<UserData> {
		private static final long serialVersionUID = 0L;

		/**
		 * Creates a new user list.
		 * 
		 * @param stat the ScoutStatus responsible for this object
		 */
		public UserList(ScoutStatus stat) {
			super(stat);
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		/**
		 * Gets the currently selected user.
		 * 
		 * @return the selected user
		 */
		public UserData getSelectedItem() {
			return (UserData)list.getSelectedValue();
		}
		public void removeCurrent() {
			UserData u = getSelectedItem();
			if (u == null) return;
			else if (!u.getName().equalsIgnoreCase("admin")) {
				if (AppLib.confirm(win, "Do you really want to delete this user?\n\nRemoving users" +
						"may cause errors. It is recommended that you clear all data before" +
						"removing users, or at\nleast remove all data entered by that user."))
					super.removeCurrent();
			} else
				AppLib.printWarn(win, "The admin user cannot be removed.\n" +
					"You should change the default admin password to secure Scout449.");
		}
	}
}