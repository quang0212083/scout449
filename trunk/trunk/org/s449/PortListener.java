package org.s449;

import java.io.*;
import java.net.*;

/**
 * A port listener that transfers all requests to another server.
 * 
 * @author Stephen Carlson
 * @version 1.0.0
 */
public class PortListener extends Thread {
	/**
	 * The port to listen and transfer.
	 */
	private int port;
	/**
	 * The target host.
	 */
	private String host;
	/**
	 * The local server socket to listen.
	 */
	private ServerSocket ss;

	/**
	 * Creates a new port listener on the given port to
	 *  the specified host.
	 * 
	 * @param newHost the target host
	 * @param newPort the port on which to listen
	 */
	public PortListener(String newHost, int newPort) {
		port = newPort;
		host = newHost;
		setPriority(Thread.MIN_PRIORITY);
	}
	/**
	 * Runs the transfer listener.
	 */
	public void run() {
		Socket s, dest;
		try {
			// initialize server socket
			ss = new ServerSocket(port);
			while (ss != null) {
				s = ss.accept();
				dest = new Socket(host, port);
				// reciprocality required
				Transferred t = new Transferred(s, dest);
				t.start();
				t = new Transferred(dest, s);
				t.start();
				AppLib.sleep(50L);
			}
		} catch (Exception e) {
			if (e.getMessage().indexOf("already in use") > 0)
				AppLib.printDebug("Port " + port + " is taken!");
		}
	}
	/**
	 * Closes the transfer listener so that it will make no
	 *  new connections. Existing connections will remain
	 *  open until closed by the program or interrupted.
	 */
	public void close() {
		try {
			ss.close();
			ss = null;
		} catch (Exception e) { }
	}

	/**
	 * Handles an individual one-way communication.
	 */
	private class Transferred extends Thread {
		public Socket toHost;
		public Socket toClient;
		private byte[] buffer;

		/**
		 * Creates a new transfer manager on the given socket pair.
		 * 
		 * @param host the host socket
		 * @param client the client socket
		 */
		protected Transferred(Socket host, Socket client) {
			toHost = host; toClient = client;
			buffer = new byte[Constants.BULK_BUFFER];
			setPriority(Thread.MIN_PRIORITY);
			setDaemon(true);
		}
		/**
		 * Closes off this transfer manager.
		 * The loop will automatically stop.
		 */
		protected void close() {
			try {
				toHost.close();
			} catch (Exception e) { }
			try {
				toClient.close();
			} catch (Exception e) { }
		}
		/**
		 * Runs this tranfer manager.
		 */
		public void run() {
			try {
				// open streams
				InputStream is = toClient.getInputStream();
				OutputStream fo = toHost.getOutputStream();
				int i;
				while (true) {
					// read from the input...
					i = is.read(buffer, 0, buffer.length);
					if (i < 0) break;
					// and write it right back to the output
					fo.write(buffer, 0, i);
					fo.flush();
				}
			} catch (Exception e) {
			} finally {
				close();
			}
		}
	}
}