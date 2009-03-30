package org.s449;

import java.awt.*;
import javax.swing.event.*;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import org.s449.ui.*;

/**
 * The main window of Scout449 (that replaces the earlier startup window).
 *  Has a tab for each major section of the program, and allows multiple
 *  pieces to run at once.
 * 
 * @author Stephen Carlson
 * @version 4.0.0
 */
public class MainWindow extends JPanel implements Runnable {
	private static final long serialVersionUID = 0L;
	/**
	 * The button to refresh domains.
	 */
	private JButton refresh;
	/**
	 * The user name.
	 */
	private JTextField user;
	/**
	 * The password.
	 */
	private JPasswordField pass;
	/**
	 * The bad password message.
	 */
	private JLabel invalid;
	/**
	 * The transfer page refresh button.
	 */
	private JButton tRefresh;
	/**
	 * The config username.
	 */
	private JTextField cUser;
	/**
	 * The config password.
	 */
	private JPasswordField cPass;
	/**
	 * The config bad password message.
	 */
	private JLabel cInvalid;
	/**
	 * The show/hide options button.
	 */
	private JButton sh;
	/**
	 * The button to configure.
	 */
	private JButton cfg;
	/**
	 * The tabs on the home screen.
	 */
	private JTabbedPane tabs;
	/**
	 * The domain for login.
	 */
	private JComboBox domain;
	/**
	 * The domain for transfer.
	 */
	private JComboBox tDomain;
	/**
	 * The regional selection box.
	 */
	private JComboBox regional;
	/**
	 * The show/hide panel.
	 */
	private JComponent shOpt;
	/**
	 * Whether the options are shown.
	 */
	private boolean optShown;
	/**
	 * The local event listener for buttons.
	 */
	private LocalEventListener events;
	/**
	 * The last height of the window (for gradient paint).
	 */
	private int lastHeight;
	/**
	 * The cached gradient paint.
	 */
	private GradientPaint paint;
	/**
	 * Whether the program is searching for servers.
	 */
	private boolean refreshing;
	/**
	 * The server status.
	 */
	private StatusBox serverStatus;
	/**
	 * The server run button.
	 */
	private JButton serverRun;
	/**
	 * The transfer status.
	 */
	private StatusBox transferStatus;
	/**
	 * The transfer run button.
	 */
	private JButton transferRun;
	/**
	 * The parent ScoutStatus object.
	 */
	private ScoutStatus status;

	public MainWindow(ScoutStatus stat) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.status = stat;
		AppLib.printDebug("Setting up auth window");
		lastHeight = -1;
		refreshing = false;
		paint = null;
		setOpaque(false);
		add(Box.createVerticalGlue());
		events = new LocalEventListener();
		tabs = new JTabbedPane(JTabbedPane.BOTTOM);
		tabs.setUI(new FrontPanelTabbedPaneUI());
		tabs.setOpaque(false);
		tabs.setBackground(Constants.LIGHT_BLUE_UI);
		tabs.addTab("login", createLogin());
		tabs.addTab("server", createServer());
		tabs.addTab("transfer", createTransfer());
		tabs.addTab("configure", createConfig());
		tabs.setPreferredSize(new Dimension(380, 250));
		tabs.setMaximumSize(tabs.getPreferredSize());
		tabs.addChangeListener(events);
		add(tabs);
		add(Box.createVerticalGlue());
		search();
		updateRegionals();
		setServerRunning(false);
		setTransferRunning(false);
		status.addActionListener(events);
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
	protected void focus() {
		tabs.setSelectedIndex(0);
		user.requestFocus();
		repaint();
	}
	protected JPanel createConfig() {
		AppLib.printDebug("Creating config page");
		JPanel config = initPanel("Configure Local Server");
		JComponent horiz = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
		horiz.setOpaque(false);
		cfg = ButtonFactory.getButton("Configure", "config", events, KeyEvent.VK_L);
		horiz.add(cfg);
		config.add(horiz, BorderLayout.SOUTH);
		JComponent middle = new JPanel(new VerticalFlow(true));
		middle.setOpaque(false);
		middle.add(Box.createVerticalStrut(20));
		horiz = new JPanel();
		horiz.setLayout(new BoxLayout(horiz, BoxLayout.X_AXIS));
		horiz.setOpaque(false);
		horiz.add(Box.createHorizontalStrut(20));
		horiz.add(new AntialiasedJLabel("User ID:"));
		horiz.add(Box.createHorizontalStrut(8));
		cUser = new JTextField(32);
		cUser.addFocusListener(TextSelector.INSTANCE);
		cUser.setActionCommand("config");
		cUser.addActionListener(events);
		horiz.add(cUser);
		horiz.add(Box.createHorizontalStrut(20));
		middle.add(horiz);
		middle.add(Box.createVerticalStrut(2));
		horiz = new JPanel();
		horiz.setLayout(new BoxLayout(horiz, BoxLayout.X_AXIS));
		horiz.setOpaque(false);
		horiz.add(Box.createHorizontalStrut(20));
		horiz.add(new AntialiasedJLabel("Passkey:"));
		horiz.add(Box.createHorizontalStrut(5));
		cPass = new JPasswordField(32);
		cPass.setFont(cUser.getFont());
		cPass.addFocusListener(TextSelector.INSTANCE);
		cPass.setEchoChar('*');
		cPass.setActionCommand("config");
		cPass.addActionListener(events);
		horiz.add(cPass);
		horiz.add(Box.createHorizontalStrut(20));
		middle.add(horiz);
		middle.add(Box.createVerticalStrut(1));
		cInvalid = new JLabel(" ");
		cInvalid.setForeground(Constants.RED);
		cInvalid.setFont(invalid.getFont());
		cInvalid.setHorizontalAlignment(SwingConstants.CENTER);
		middle.add(cInvalid);
		middle.add(Box.createVerticalStrut(6));
		JLabel lbl = new AntialiasedJLabel("Stop all servers running locally before configuring.");
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		middle.add(lbl);
		middle.add(Box.createVerticalStrut(2));
		lbl = new AntialiasedJLabel("Log into the local server with an administrative " +
			"account.");
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		middle.add(lbl);
		config.add(middle, BorderLayout.CENTER);
		return config;
	}
	protected JPanel createTransfer() {
		AppLib.printDebug("Creating transfer page");
		JPanel transfer = initPanel("Transfer Connections");
		JComponent horiz = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
		horiz.setOpaque(false);
		transferRun = ButtonFactory.getButton("Start", "transfer", events, -1);
		horiz.add(transferRun);
		transfer.add(horiz, BorderLayout.SOUTH);
		JComponent middle = new JPanel(new VerticalFlow(true));
		middle.setOpaque(false);
		middle.add(Box.createVerticalStrut(50));
		horiz = new JPanel();
		horiz.setLayout(new BoxLayout(horiz, BoxLayout.X_AXIS));
		horiz.setOpaque(false);
		horiz.add(Box.createHorizontalStrut(20));
		horiz.add(new AntialiasedJLabel("To domain:"));
		horiz.add(Box.createHorizontalStrut(5));
		tDomain = new JComboBox(domain.getModel());
		tDomain.setEditable(true);
		tDomain.setBorder(domain.getBorder());
		tDomain.setOpaque(false);
		horiz.add(tDomain);
		horiz.add(Box.createHorizontalStrut(5));
		tRefresh = ButtonFactory.getButton("Refresh", "refresh", events, -1);
		horiz.add(tRefresh);
		horiz.add(Box.createHorizontalStrut(20));
		middle.add(horiz);
		middle.add(Box.createVerticalStrut(2));
		transferStatus = new StatusBox("Transfer Stopped", StatusBox.STATUS_BAD);
		transferStatus.setHorizontalAlignment(SwingConstants.CENTER);
		middle.add(transferStatus);
		transfer.add(middle, BorderLayout.CENTER);
		return transfer;
	}
	protected JPanel createServer() {
		AppLib.printDebug("Creating server page");
		JPanel server = initPanel("Server");
		JComponent horiz = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
		horiz.setOpaque(false);
		serverRun = ButtonFactory.getButton("Start", "serve", events, KeyEvent.VK_S);
		JButton hide = ButtonFactory.getButton("Minimize", "minimize", events, KeyEvent.VK_H);
		horiz.add(serverRun);
		horiz.add(hide);
		server.add(horiz, BorderLayout.SOUTH);
		JComponent middle = new JPanel(new VerticalFlow(true));
		middle.setOpaque(false);
		middle.add(Box.createVerticalStrut(50));
		horiz = new JPanel();
		horiz.setLayout(new BoxLayout(horiz, BoxLayout.X_AXIS));
		horiz.setOpaque(false);
		horiz.add(Box.createHorizontalStrut(20));
		horiz.add(new AntialiasedJLabel("Select Regional:"));
		horiz.add(Box.createHorizontalStrut(5));
		regional = new JComboBox();
		regional.setEditable(false);
		regional.setOpaque(false);
		horiz.add(regional);
		horiz.add(Box.createHorizontalStrut(20));
		middle.add(horiz);
		middle.add(Box.createVerticalStrut(2));
		serverStatus = new StatusBox("Server Stopped", StatusBox.STATUS_BAD);
		serverStatus.setHorizontalAlignment(SwingConstants.CENTER);
		middle.add(serverStatus);
		server.add(middle, BorderLayout.CENTER);
		return server;
	}
	public void updateRegionals() {
		Event[] regionals = status.getServer().eventList();
		regional.removeAllItems();
		if (regionals == null) {
			regional.addItem("Server Not Configured");
			regional.setEnabled(false);
			serverRun.setEnabled(false);
		} else {
			regional.setEnabled(true);
			serverRun.setEnabled(true);
			for (int i = 0; i < regionals.length; i++)
				regional.addItem(regionals[i]);
		}
		repaint();
	}
	private JPanel initPanel(String titleText) {
		JPanel result = new JPanel();
		result.setOpaque(false);
		result.setLayout(new BorderLayout());
		JLabel lgn = new AntialiasedJLabel(titleText);
		lgn.setForeground(Constants.DARK_BLUE);
		lgn.setHorizontalAlignment(SwingConstants.CENTER);
		lgn.setFont(new Font("Arial", Font.PLAIN, 30));
		result.add(lgn, BorderLayout.NORTH);
		return result;
	}
	protected JPanel createLogin() {
		AppLib.printDebug("Creating login page");
		optShown = true;
		JPanel login = initPanel("Login to Server");
		JComponent horiz = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
		horiz.setOpaque(false);
		JButton ok = ButtonFactory.getButton("Login", "login", events, KeyEvent.VK_L);
		sh = ButtonFactory.getButton("Hide options", "sh", events, KeyEvent.VK_H);
		horiz.add(ok);
		horiz.add(sh);
		login.add(horiz, BorderLayout.SOUTH);
		JComponent middle = new JPanel(new VerticalFlow(true));
		middle.setOpaque(false);
		middle.add(Box.createVerticalStrut(30));
		horiz = new JPanel();
		horiz.setLayout(new BoxLayout(horiz, BoxLayout.X_AXIS));
		horiz.setOpaque(false);
		horiz.add(Box.createHorizontalStrut(20));
		horiz.add(new AntialiasedJLabel("User ID:"));
		horiz.add(Box.createHorizontalStrut(8));
		user = new JTextField(32);
		user.addFocusListener(TextSelector.INSTANCE);
		user.setActionCommand("login");
		user.addActionListener(events);
		horiz.add(user);
		horiz.add(Box.createHorizontalStrut(20));
		middle.add(horiz);
		middle.add(Box.createVerticalStrut(2));
		horiz = new JPanel();
		horiz.setLayout(new BoxLayout(horiz, BoxLayout.X_AXIS));
		horiz.setOpaque(false);
		horiz.add(Box.createHorizontalStrut(20));
		horiz.add(new AntialiasedJLabel("Passkey:"));
		horiz.add(Box.createHorizontalStrut(5));
		pass = new JPasswordField(32);
		pass.setFont(user.getFont());
		pass.addFocusListener(TextSelector.INSTANCE);
		pass.setEchoChar('*');
		pass.setActionCommand("login");
		pass.addActionListener(events);
		horiz.add(pass);
		horiz.add(Box.createHorizontalStrut(20));
		middle.add(horiz);
		middle.add(Box.createVerticalStrut(2));
		shOpt = new JPanel();
		shOpt.setLayout(new BoxLayout(shOpt, BoxLayout.X_AXIS));
		shOpt.setOpaque(false);
		shOpt.add(Box.createHorizontalStrut(20));
		shOpt.add(new AntialiasedJLabel("Domain:"));
		shOpt.add(Box.createHorizontalStrut(5));
		domain = new JComboBox();
		domain.setOpaque(false);
		domain.setEditable(true);
		domain.setBorder(BorderFactory.createCompoundBorder(domain.getBorder(),
			BorderFactory.createEmptyBorder(0, 3, 0, 0)));
		shOpt.add(domain);
		shOpt.add(Box.createHorizontalStrut(5));
		refresh = ButtonFactory.getButton("Refresh", "refresh", events, KeyEvent.VK_R);
		shOpt.add(refresh);
		shOpt.add(Box.createHorizontalStrut(20));
		middle.add(shOpt);
		middle.add(Box.createVerticalStrut(1));
		invalid = new JLabel(" ");
		invalid.setForeground(Constants.RED);
		invalid.setFont(invalid.getFont().deriveFont(Font.BOLD));
		invalid.setHorizontalAlignment(SwingConstants.CENTER);
		middle.add(invalid);
		login.add(middle, BorderLayout.CENTER);
		return login;
	}
	protected void badPass() {
		AppLib.printDebug("Bad user/pass!");
		invalid.setText("Invalid username or password.");
		pass.setText("");
		focus();
		user.selectAll();
	}
	protected void cBadPass() {
		AppLib.printDebug("Bad config user/pass!");
		cInvalid.setText("Invalid username or password.");
		cPass.setText("");
		tabs.setSelectedIndex(3);
		cUser.requestFocus();
		cUser.selectAll();
	}
	private void showHideOptions() {
		AppLib.printDebug("Show/Hide Options");
		optShown = !optShown;
		shOpt.setVisible(optShown);
		if (optShown)
			sh.setText("Hide options");
		else
			sh.setText("Show options");
		repaint();
	}
	private void networkSearch() {
		AppLib.printDebug("Searching for servers...");
		NPC3.detect();
		domain.removeAllItems();
		NPCServer serve;
		Iterator it = NPC3.getServers().iterator();
		while (it.hasNext()) {
			serve = (NPCServer)it.next();
			domain.addItem(serve.getName() + " - " + serve.getIP());
		}
		domain.addItem("local - 127.0.0.1");
		synchronized (refresh) {
			refresh.setText("Refresh");
			refresh.setEnabled(true);
			tRefresh.setText("Refresh");
			tRefresh.setEnabled(true);
			refreshing = false;
		}
		repaint();
	}
	public void init() {
		while (refreshing) AppLib.sleep(10L);
	}
	public void run() {
		networkSearch();
	}
	protected void search() {
		synchronized (refresh) {
			if (refreshing) return;
			refreshing = true;
			refresh.setText("Loading...");
			refresh.setEnabled(false);
			tRefresh.setText("Loading...");
			tRefresh.setEnabled(false);
			repaint();
		}
		new Thread(this).start();
	}
	private void cLogin() {
		AppLib.printDebug("Attempting configure");
		cInvalid.setText(" ");
		cUser.setEnabled(false);
		cPass.setEnabled(false);
		Importer i = status.getImporter();
		i.setUser(new UserData(cUser.getText(), cPass.getPassword()));
		i.start();
		cUser.setEnabled(true);
		cPass.setEnabled(true);
		cPass.setText("");
	}
	private void login() {
		String hostIP = (String)domain.getEditor().getItem();
		if (hostIP == null) {
			badPass();
			return;
		}
		hostIP = hostIP.trim();
		int ind = hostIP.indexOf('-');
		if (ind >= 0) hostIP = hostIP.substring(ind + 1, hostIP.length()).trim();
		if (hostIP.length() < 2) {
			badPass();
			return;
		}
		AppLib.printDebug("Attempting login to " + hostIP);
		invalid.setText(" ");
		user.setEnabled(false);
		pass.setEnabled(false);
		status.getClient().setUser(new UserData(user.getText(), pass.getPassword()));
		status.setRemoteHost(hostIP);
		status.showClient(true);
		user.setEnabled(true);
		pass.setEnabled(true);
	}
	protected void setServerRunning(boolean run) {
		if (run) {
			serverStatus.setText("Server Running");
			serverStatus.setStatus(StatusBox.STATUS_GOOD);
			serverRun.setText("Stop");
			regional.setEnabled(false);
			transferRun.setEnabled(false);
			tRefresh.setEnabled(false);
		} else {
			serverStatus.setText("Server Stopped");
			serverStatus.setStatus(StatusBox.STATUS_BAD);
			serverRun.setText("Start");
			regional.setEnabled(true);
			transferRun.setEnabled(true);
			tRefresh.setEnabled(true);
		}
	}
	protected void setTransferRunning(boolean run) {
		if (run) {
			transferStatus.setText("Transfer Running");
			transferStatus.setStatus(StatusBox.STATUS_GOOD);
			transferRun.setText("Stop");
			tDomain.setEnabled(false);
			tRefresh.setEnabled(false);
			serverRun.setEnabled(false);
		} else {
			transferStatus.setText("Transfer Stopped");
			transferStatus.setStatus(StatusBox.STATUS_BAD);
			transferRun.setText("Start");
			tDomain.setEnabled(true);
			tRefresh.setEnabled(true);
			updateRegionals();
		}
	}

	private class LocalEventListener implements ActionListener, ChangeListener {
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd == null);
			else if (cmd.equals("refresh"))
				search();
			else if (cmd.equals("serve")) {
				status.getServer().setEvent((Event)regional.getSelectedItem());
				status.setServerStatus(!status.isServerRunning());
			} else if (cmd.equals("transfer"))
				status.setTransferStatus(!status.isTransferRunning());
			else if (cmd.equals("s_server"))
				setServerRunning(status.isServerRunning());
			else if (cmd.equals("s_client") && !status.isClientShowing()) {
				pass.setText("");
				focus();
			} else if (cmd.equals("s_autherr"))
				badPass();
			else if (cmd.equals("s_cautherr"))
				cBadPass();
			else if (cmd.equals("s_transfer"))
				setTransferRunning(status.isTransferRunning());
			else if (cmd.equals("s_iusers"))
				status.getServer().configUsers();
			else if (cmd.equals("s_import")) {
				status.getServer().configure();
				updateRegionals();
			} else if (cmd.equals("login"))
				login();
			else if (cmd.equals("config"))
				cLogin();
			else if (cmd.equals("minimize"))
				status.sendMessage("hide");
			else if (cmd.equals("exit")) {
				status.setServerStatus(false);
				status.setTransferStatus(false);
				System.exit(0);
			} else if (cmd.equals("sh"))
				showHideOptions();
		}
		public void stateChanged(ChangeEvent e) {
			int i = tabs.getSelectedIndex();
			AppLib.sleep(1L);
			switch (i) {
			case 0:
				user.requestFocus();
				break;
			case 1:
				regional.requestFocus();
				break;
			case 2:
				tDomain.requestFocus();
				break;
			case 3:
				cUser.requestFocus();
				break;
			default:
			}
		}
	}
}