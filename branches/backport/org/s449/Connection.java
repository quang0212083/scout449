package org.s449;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.net.Socket;
import java.util.LinkedList;

/**
 * A class that represents a connection to another computer.
 * 
 * @author Stephen Carlson
 */
public class Connection {
	/**
	 * A class for reading non-blocking object streams.
	 */
	private class NBStreamThread extends Thread {
		/**
		 * Creates, initializes, and starts a non-blocking stream thread.
		 */
		public NBStreamThread() {
			setName("Non-Blocking Object Stream");
			setPriority(Thread.MIN_PRIORITY);
			setDaemon(true);
			start();
		}
		public void run() {
			Object o;
			while (obj != null && br != null) {
				try {
					o = br.readObject();
					if (o instanceof Requestable)
						obj.addLast(o);
					Connection.sleep(50L);
				} catch (ClassNotFoundException e) {
				} catch (ObjectStreamException e) {
				} catch (IOException e) {
					//e.printStackTrace();
					obj = null;
				}
			}
		}
	}

	/**
	 * Reader of the socket's stream.
	 */
	private ObjectInputStream br;
	/**
	 * Writer to the socket's stream.
	 */
	private ObjectOutputStream out;
	/**
	 * The actual socket.
	 */
	private Socket sock;
	/**
	 * The IP of the client.
	 */
	private String ip;
	/**
	 * The object(s) that were just read.
	 */
	private LinkedList obj;

	/**
	 * Creates a new connection to the given host and port.
	 */
	public Connection(String host, int port) throws IOException {
		Socket s = null;
		s = new Socket(host, port);
		setup(s);
	}
	/**
	 * Creates a new connection based on the specified socket.
	 * 
	 * @param socket the socket
	 */
	public Connection(Socket socket) {
		setup(socket);
	}
	private void setup(Socket socket) {
		sock = socket;
		ip = sock.getInetAddress().getHostAddress();
		obj = new LinkedList();
		try {
			out = new ObjectOutputStream(sock.getOutputStream());
			out.flush();
			br = new ObjectInputStream(sock.getInputStream());
		} catch (IOException e) {
			sock = null; br = null; out = null;
			throw new RuntimeException("Stream initialization error!");
		}
		if (br != null) new NBStreamThread();
	}
	/**
	 * Reads a line of data from this socket.
	 * 
	 * @return the next line of data
	 */
	public Object read() {
		while (obj != null && obj.size() <= 0) sleep(10L);
		if (obj == null) throw new RuntimeException("Cannot connect to server!");
		return obj.removeFirst();
	}
	/**
	 * Peeks at a line of data from this socket.
	 * 
	 * @return the next line of data without killing it
	 */
	public Object next() {
		while (obj != null && obj.size() <= 0) sleep(10L);
		if (obj == null) throw new RuntimeException("Cannot connect to server!");
		return obj.getFirst();
	}
	/**
	 * Sends an object to this socket.
	 * 
	 * @param dat the data to send
	 * @throws IOException if an I/O error occurs
	 */
	public void write(Object dat) throws IOException {
		out.reset();
		out.writeObject(dat);
	}
	/**
	 * Writes the cached output to the stream.
	 * 
	 * @throws IOException if an I/O error occurs
	 */
	public void flush() throws IOException {
		out.flush();
	}
	/**
	 * Polls the waiting thread to see if this connection is ready.
	 *  Ready means that there is data waiting to be read.
	 * 
	 * @return ready flag
	 */
	public boolean ready() {
		return obj != null && obj.size() > 0;
	}
	/**
	 * Returns true if the client is dead.
	 * 
	 * @return whether the client is dead
	 */
	public boolean isDead() {
		return obj == null;
	}
	/**
	 * Closes the socket connection.
	 */
	public void close() {
		if (sock == null) return;
		obj = null;
		try {
			br.close(); // stop the NBStreamThread
			out.close();
			sock.close();
		} catch (Exception e) { }
	}
	/**
	 * Gets the socket object representing the connection.
	 * 
	 * @return the socket connection
	 */
	public Socket getSocket() {
		return sock;
	}
	/**
	 * Gets the object input stream responsible for the connection.
	 * 
	 * @return the object input stream that is polling the connection.
	 *  Note that a thread is reading this as well...
	 */
	public ObjectInputStream getInput() {
		return br;
	}
	/**
	 * Gets the object output stream responsible for the connection.
	 * 
	 * @return the object output stream that can be used to send data
	 */
	public ObjectOutputStream getOutput() {
		return out;
	}
	/**
	 * Gets the IP address of the other machine.
	 * 
	 * @return the other IP address
	 */
	public String getIP() {
		return ip;
	}
	public String toString() {
		return "Connection to " + ip;
	}

	/**
	 * Implementation-agnostic sleep method.
	 * 
	 * @param millis the time to wait
	 */
	private static final void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (Exception e) { }
	}
}