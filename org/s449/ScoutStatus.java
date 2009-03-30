package org.s449;

import javax.swing.*;
import java.util.*;
import java.awt.Container;
import java.awt.Image;
import java.awt.event.*;

/**
 * A class to handle the status of all the Scout449 componenents and provide integration
 *  between far-flung classes to accomplish static independence.
 *
 * @author Stephen Carlson
 * @version 4.0.0
 */
public class ScoutStatus {
	/**
	 * Local server running?
	 */
	private volatile boolean localServer;
	/**
	 * Local transfer running?
	 */
	private volatile boolean localTransfer;
	/**
	 * Connected to remote server?
	 */
	private volatile boolean connected;
	/**
	 * Which view is showing?
	 */
	private volatile boolean view;
	/**
	 * The remote host (if in use).
	 */
	private String remoteHost;
	/**
	 * The action listeners registered to fire on status changes.
	 */
	private List listeners;
	/**
	 * The instance's client.
	 */
	private Client client;
	/**
	 * The instance's server.
	 */
	private Server server;
	/**
	 * The instance's transfer.
	 */
	private Transfer transfer;
	/**
	 * The instance's main window.
	 */
	private JFrame mainWin;
	/**
	 * The instance's home screen.
	 */
	private MainWindow home;
	/**
	 * The instance's importer.
	 */
	private Importer imp;
	/**
	 * The instance's Scout449 object for image and data storage.
	 */
	private Scout449 s449;

	/**
	 * Creates a new master scouting status object.
	 * 
	 * @param creator the source Scout449 object (the main program run)
	 * @param win the main client window
	 */
	protected ScoutStatus(Scout449 creator, JFrame win) {
		s449 = creator;
		mainWin = win;
		localServer = false;
		localTransfer = false;
		connected = false;
		remoteHost = null;
		listeners = new LinkedList();
		client = new Client(this);
		server = new Server(this);
		transfer = new Transfer(this);
		home = new MainWindow(this);
		imp = new Importer(this);
		showClient(false);
	}
	/**
	 * Gets the client for this instance.
	 * 
	 * @return the client
	 */
	public Client getClient() {
		return client;
	}
	/**
	 * Gets the server for this instance.
	 * 
	 * @return the server
	 */
	public Server getServer() {
		return server;
	}
	/**
	 * Gets the transfer for this instance.
	 * 
	 * @return the transfer
	 */
	public Transfer getTransfer() {
		return transfer;
	}
	/**
	 * Gets the home screen for this instance.
	 * 
	 * @return the home screen
	 */
	public MainWindow getHomeScreen() {
		return home;
	}
	/**
	 * Gets the main window for this instance.
	 * 
	 * @return the main window
	 */
	public JFrame getWindow() {
		return mainWin;
	}
	/**
	 * Gets the importer for this instance.
	 *
	 * @return the importer
	 */
	public Importer getImporter() {
		return imp;
	}
	/**
	 * Gets the master Scout449 object for this instance.
	 * 
	 * @return the master
	 */
	public Scout449 getMaster() {
		return s449;
	}
	/**
	 * Adds an action listener for status events.
	 * 
	 * @param l the ActionListener to add
	 */
	public synchronized void addActionListener(ActionListener l) {
		listeners.add(l);
	}
	/**
	 * Removes an action listener for status events.
	 * 
	 * @param l the ActionListener to remove
	 */
	public synchronized void removeActionListener(ActionListener l) {
		listeners.remove(l);
	}
	/**
	 * Removes all of the registered status listeners.
	 */
	protected synchronized void removeAllActionListeners() {
		listeners.clear();
	}
	/**
	 * Starts or stops the server. A system-wide status event "s_server" will be sent.
	 * 
	 * @param running whether the server should be running
	 */
	public void setServerStatus(boolean running) {
		synchronized (this) {
			localServer = running;
		}
		fireActionEvent("s_server");
	}
	/**
	 * Changes the remote host. A system-wide status event "s_host" will be sent.
	 * 
	 * @param remote the new host
	 */
	public void setRemoteHost(String remote) {
		synchronized (this) {
			remoteHost = remote;
		}
		fireActionEvent("s_host");
	}
	/**
	 * Changes the connection status. A system-wide status event "s_connect" will be sent.
	 * 
	 * @param connect whether the connection should be open
	 */
	public void setConnectionStatus(boolean connect) {
		synchronized (this) {
			connected = connect;
		}
		fireActionEvent("s_connect");
	}
	/**
	 * Starts or stops the transfer. A system-wide status event "s_transfer" will be sent.
	 * 
	 * @param running whether the transfer should be running
	 */
	public void setTransferStatus(boolean running) {
		synchronized (this) {
			localTransfer = running;
		}
		fireActionEvent("s_transfer");
	}
	/**
	 * Gets the status of the server.
	 * 
	 * @return whether the server is running
	 */
	public synchronized boolean isServerRunning() {
		return localServer;
	}
	/**
	 * Gets the status of the connection.
	 * 
	 * @return whether the connection is open
	 */
	public synchronized boolean isConnected() {
		return connected;
	}
	/**
	 * Gets the status of the client.
	 * 
	 * @return whether the client is visible
	 */
	public synchronized boolean isClientShowing() {
		return view;
	}
	/**
	 * Gets the status of the transfer.
	 * 
	 * @return whether the transfer is running
	 */
	public synchronized boolean isTransferRunning() {
		return localTransfer;
	}
	/**
	 * Gets the current remote host.
	 * 
	 * @return the remote host
	 */
	public synchronized String getRemoteHost() {
		return remoteHost;
	}
	/**
	 * Sends a system-wide status message to all parts of Scout449.
	 * 
	 * @param message the message to send; command will be prefixed with an "s_"
	 */
	public void sendMessage(String message) {
		fireActionEvent("s_" + message);
	}
	/**
	 * Sends a system-wide status event.
	 * 
	 * @param message the message to send
	 */
	private void fireActionEvent(String message) {
		ActionEvent e = new ActionEvent(mainWin, ActionEvent.ACTION_PERFORMED, message);
		Iterator it = listeners.iterator();
		while (it.hasNext())
			((ActionListener)it.next()).actionPerformed(e);
	}
	/**
	 * Shows or hides the client. Fires an action event.
	 * 
	 * @param which the view to show: true=client, false=main window
	 */
	public void showClient(boolean which) {
		synchronized (this) {
			Container c = mainWin.getContentPane();
			c.removeAll();
			if (which)
				c.add(client);
			else {
				c.add(home);
				home.focus();
			}
			mainWin.validate();
			mainWin.repaint();
			view = which;
		}
		fireActionEvent("s_client");
	}
	/**
	 * Convenience method to get a specified icon.
	 * 
	 * @param name the icon name to load
	 * @return the icon
	 * @see org.s449.Scout449#getIcon(String)
	 */
	public ImageIcon getIcon(String name) {
		return s449.getIcon(name);
	}
	/**
	 * Convenience method to get a specified icon as an Image.
	 * 
	 * @param name the image name to load
	 * @return the image
	 * @see org.s449.Scout449#getImage(String)
	 */
	public Image getImage(String name) {
		return s449.getImage(name);
	}
	/**
	 * Convenience method to get the active backend.
	 * 
	 * @return the active data backend
	 * @see org.s449.Client#getData()
	 */
	public Backend getBackend() {
		return client.getData();
	}
	/**
	 * Convenience method to get the active username.
	 * 
	 * @return the user for the client
	 * @see org.s449.Client#getUser()
	 */
	public UserData getUser() {
		return client.getUser();
	}
	/**
	 * Convenience method to get the active data store.
	 * 
	 * @return the active data store
	 * @see org.s449.Backend#getData()
	 */
	public DataStore getDataStore() {
		return client.getData().getData();
	}
}