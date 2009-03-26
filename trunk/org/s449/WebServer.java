package org.s449;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * A simple multi-threaded web server. Supports GET, POST, and HEAD.
 *  Allows classes to register to run when a certain URL or set of
 *  URLs is called. If the URL requested doesn't match any patterns,
 *  the servlet will fall through to file serving. Sometimes useful,
 *  sometimes bad.
 *
 * @author Stephen Carlson
 * @version 4.0.0
 */
public class WebServer implements Runnable {
	/**
	 * The default port for a web server.
	 */
	public static final int DEFAULT_PORT = 8080;
	/**
	 * The port for this web server.
	 */
	protected int port = DEFAULT_PORT;
	/**
	 * The file that is the document root of this web server.
	 */
	protected File root;
	/**
	 * The timeout (in milliseconds) for user requests.
	 */
	protected static final int timeout = 3000;
	/**
	 * The number of worker threads that will be started.
	 *  Fewer threads means less memory and CPU usage, but more
	 *  threads can handle higher loads without delay.
	 */
	protected static final int numWorkers = 30;
	/**
	 * The error handler (null if not set).
	 */
	protected ServerPage errorHandler;
	/**
	 * The worker list.
	 */
	protected ArrayList<Worker> threads;
	/**
	 * The registry of listeners.
	 */
	protected List<ServerPage> registry;
	/**
	 * Server running?
	 */
	protected boolean running;
	/**
	 * The server socket.
	 */
	protected ServerSocket ss;

	/**
	 * Creates a web server on the default port.
	 */
	public WebServer() {
		running = false;
		threads = new ArrayList<Worker>(numWorkers);
		registry = new ArrayList<ServerPage>(6);
	}
	/**
	 * Creates a web server on the specified port.
	 * 
	 * @param port the port to listen
	 */
	public WebServer(int port) {
		this();
		setPort(port);
	}
	/**
	 * Loads a thread-safe extension. Some kind of synchronization should
	 *  be implemented to avoid threading problems and deadlock.
	 * 
	 * @param nonSynch the object of a thread-safe extension
	 * to register. The action() method of THE OBJECT PASSED IN will be
	 * called, and possibly more than once or simultaneously.
	 */
	public synchronized void register(ServerPage synch) {
		registry.add(synch);
	}
	/**
	 * Registers a server page class as an error handler. The handler
	 *  must be thread safe.
	 * 
	 * @param handler the object of a thread-safe extension to register
	 *  as the error handler, or null to restore default behavior.
	 */
	public synchronized void registerAsErrorHandler(ServerPage handler) {
		errorHandler = handler;
	}
	/**
	 * Starts this web server in the background.
	 */
	public synchronized void start() {
		if (running) return;
		running = true;
		new Thread(this).start();
	}
	/**
	 * Stops this web server.
	 */
	public synchronized void stop() {
		if (!running) return;
		running = false;
		if (ss != null) try {
			ss.close();
		} catch (Exception e) { }
	}
	/**
	 * Gets the port on which this web server is listening (or will be
	 *  listening).
	 * 
	 * @return the port to which the server may bind
	 */
	public int getPort() {
		return port;
	}
	/**
	 * Changes the port to which this server will listen. If the server
	 *  is running, nothing happens.
	 * 
	 * @param port the new port to which to attach this server
	 */
	public synchronized void setPort(int port) {
		if (!running) this.port = port;
	}
	/**
	 * Starts this web server but blocks the main thread forever. Use start()
	 *  to avoid blocking the calling thread.
	 */
	public void run() {
		root = new File(".");

		for (int i = 0; i < numWorkers; ++i) {
			Worker w = new Worker(this);
			Thread t = new Thread(w, "initial worker #" + i);
			t.setDaemon(true);
			t.start();
			threads.add(w);
		}

		int index = -1, i;
		Socket s;
		try {
			ss = new ServerSocket(port);
			AppLib.printDebug("Opened listener on localhost:" + port);
			while (running) {
				s = ss.accept();
				for (i = (index + 1) % numWorkers; i != index; i = (i + 1) % numWorkers)
					if (threads.get(i).setSocket(s)) break;
				if (i == index) {
					AppLib.printDebug("Load factor! Starting new worker");
					Worker w = new Worker(this);
					w.setSocket(s);
					Thread t = new Thread(w, "additional worker");
					t.setDaemon(true);
					t.setPriority(Thread.NORM_PRIORITY + 1);
					t.start();
					index = 0;
				} else
					index = i;
			}
		} catch (Exception e) {
			if (e.getMessage() == null || e.getMessage().indexOf("closed") < 0)
				AppLib.debugException(e);
		}
		for (i = 0; i < threads.size(); i++)
			threads.get(i).stop();
	}
}

/**
 * Subordinate of a server that does all of the work.
 */
class Worker implements Runnable {
	/**
	 * The hex character key for encoding.
	 */
	private static final String hexChars = "0123456789ABCDEF";

	/**
	 * The currently active socket.
	 */
	private Socket sock;
	/**
	 * The server's document root.
	 */
	private File root;
	/**
	 * The parent class that spawned this worker.
	 */
	private WebServer parent;
	/**
	 * The internet IP address of the client.
	 */
	private String inet;
	/**
	 * Whether the headers were sent.
	 */
	private boolean headersSent;
	/**
	 * Running?
	 */
	private boolean running;

	/**
	 * Creates an idle worker as a parent to the given WebServer.
	 * 
	 * @param in the WebServer from which this Worker should run
	 */
	public Worker(WebServer in) {
		sock = null;
		parent = in;
		root = in.root;
		running = true;
	}
	/**
	 * Starts this worker on a new client.
	 * 
	 * @return true if the worker started to handle the client;
	 * false if another client is already occupying this worker
	 */
	public synchronized boolean setSocket(Socket newSocket) {
		if (sock == null) {
			sock = newSocket;
			notify();
			return true;
		}
		return false;
	}
	/**
	 * Runs this worker but blocks forever. Should be started as a thread!
	 */
	public synchronized void run() {
		while (running) {
			headersSent = false;
			if (sock == null)
				try {
					wait();
				} catch (InterruptedException e) {
					continue;
				}
			if (sock != null) {
				inet = sock.getInetAddress().getHostAddress();
				if (inet == null || inet.length() < 1) inet = "Unknown";
				try {
					handleClient();
				} catch (Exception e) {
					AppLib.printDebug("Internal I/O Error!");
					AppLib.debugException(e);
				}
			}
			sock = null;
		}
	}
	/**
	 * Stops this worker.
	 */
	public synchronized void stop() {
		running = false;
		notify();
	}
	/**
	 * Sends an HTTP status message as part of the headers.
	 * 
	 * @param out where to print the message
	 * @param code the status code (message will be looked up)
	 */
	public static void sendHTTP(PrintWriter out, int code) {
		out.print("HTTP/1.0 ");
		out.print(code);
		out.print(" ");
		out.print(HTTPConstants.messages.get(code));
		out.print("\r\n");
	}
	/**
	 * Breaks down a GET style request into a map.
	 * 
	 * @param get the GET string like "a=b&c=%20d"
	 * @param decode whether to decode the "%20" into a " "
	 * @return a Map object with the keys and values represented
	 *  by this GET string
	 */
	public static Map<String, String> parseGET(String get, boolean decode) {
		Map<String, String> map = new HashMap<String, String>();
		if (get == null || get.length() < 1) return map;
		StringTokenizer str = new StringTokenizer(get, "&");
		String keyval; String key; String val;
		while (str.hasMoreTokens()) {
			keyval = str.nextToken();
			int equals = keyval.indexOf('=');
			if (equals <= 0)
				map.put("", keyval);
			else if (decode) {
				key = urldecode(keyval.substring(0, equals));
				val = urldecode(keyval.substring(equals + 1, keyval.length()));
				map.put(key, val);
			} else {
				key = keyval.substring(0, equals);
				val = keyval.substring(equals + 1, keyval.length());
				map.put(key, val);
			}
		}
		return map;
	}
	/**
	 * Breaks down a GET style request into a map.
	 * 
	 * @param get the GET string like "a=b&c=%20d"
	 * @return a Map object with the keys and values represented
	 *  by this GET string
	 */
	public static Map<String, String> parseGET(String get) {
		return parseGET(get, true);
	}
	/**
	 * Encodes a string as a URL with escape codes.
	 * 
	 * @param in a string with possibly unescaped values
	 * @return a string with all special characters escaped URL style
	 */
	public static String urlencode(String toEncode) {
		StringBuffer nt = new StringBuffer(toEncode.length() + 32);
		char c; int num;
		for (int i = 0; i < toEncode.length(); i++) {
			c = toEncode.charAt(i);
			if (Character.isLetterOrDigit(c)) nt.append(c);
			else {
				num = (int)c;
				nt.append('%');
				nt.append(hexChars.charAt((num / 16) % 16));
				nt.append(hexChars.charAt(num % 16));
			}
		}
		return nt.toString();
	}
	/**
	 * Decodes a string as a URL with escape codes.
	 * 
	 * @param in a string with escaped values
	 * @return a string with all special characters returned to
	 *  their normal state
	 */
	public static String urldecode(String toDecode) {
		StringBuffer nt = new StringBuffer(toDecode.length());
		char c;
		for (int i = 0; i < toDecode.length(); i++) {
			c = toDecode.charAt(i);
			if (c == '%' && i < toDecode.length() - 2) {
				nt.append((char)Integer.parseInt(toDecode.substring(i + 1, i + 3), 16));
				i += 2;
			} else
				nt.append(c);
		}
		return nt.toString();
	}
	/**
	 * Runs the error handler.
	 * 
	 * @param code the error code from HTTPConstants
	 * @param out the output writer
	 * @param url the url that caused the error
	 * @param os the output stream
	 */
	private void runErrorHandler(int code, PrintWriter out, String url, OutputStream os) {
		if (parent.errorHandler == null)
			sendHTTP(out, code);
		else
			parent.errorHandler.action("error", inet, Integer.toString(code) + ";" + url, out, os);
	}
	/**
	 * Handles the client.
	 */
	private void handleClient() throws IOException {
		BufferedReader br = new BufferedReader(
			new InputStreamReader(sock.getInputStream()));
		PrintWriter out = new PrintWriter(sock.getOutputStream());
		sock.setSoTimeout(WebServer.timeout);
		OutputStream os = null;
		String url = null;
		int operation = 0;
		try {
			os = sock.getOutputStream();
			String l1 = br.readLine();
			if (l1 == null) return;
			StringTokenizer str = new StringTokenizer(l1.trim());
			String first = null;
			try {
				first = str.nextToken().toUpperCase();
				url = str.nextToken();
			} catch (Exception e) {
				runErrorHandler(HTTPConstants.HTTP_BAD_REQUEST, out, "", os);
				return;
			}
			if (first.equals("GET")) {
				AppLib.printDebug("Processing GET request from: " + inet);
				operation = 1;
			} else if (first.equals("HEAD")) {
				AppLib.printDebug("Processing HEAD request from: " + inet);
				operation = 0;
			} else if (first.equals("POST")) {
				AppLib.printDebug("Processing POST request from: " + inet);
				operation = 2;
			} else {
				AppLib.printDebug("Not implemented: " + first + " from: " + inet);
				runErrorHandler(HTTPConstants.HTTP_BAD_METHOD, out, "", os);
				return;
			}
			while (str.countTokens() > 1)
				url += str.nextToken();
			if (url.length() < 1) {
				AppLib.printDebug("No file specified...");
				runErrorHandler(HTTPConstants.HTTP_BAD_REQUEST, out, "", os);
				return;
			}
			StringBuffer restOfData = new StringBuffer(512);
			String line = null;
			while ((line = br.readLine()) != null && line.length() > 0 &&
					(br.ready() || operation == 2)) {
				restOfData.append(line);
				restOfData.append("\n");
			}
			if (line != null && (operation == 2 || br.ready())) {
				// read off the last line
				int n = 0;
				char[] buf = new char[64];
				restOfData.append("\n");
				do {
					n = br.read(buf, 0, buf.length);
					restOfData.append(buf, 0, n);
				} while (n >= buf.length);
			}
			url = url.replaceAll("\\.\\.\\/", "");
			if (url.startsWith("/")) url = url.substring(1);
			Iterator<ServerPage> it = parent.registry.iterator();
			ServerPage item;
			//AppLib.printDebug("Asked for " + url);
			while (it.hasNext()) {
				item = it.next();
				if (item.match(url)) {
					//AppLib.printDebug("Matched expression " + e.pattern());
					item.action(url, inet, restOfData.toString().trim(), out,
						sock.getOutputStream());
					out.flush();
					return;
				}
			}
			url = url.replace('/', File.separatorChar);
			if (url.startsWith(File.separator))
				url = url.substring(1);
			File targ = new File(root, url);
			if (targ.isDirectory()) {
				File ind;
				for (int i = 0; i < HTTPConstants.indices.size(); i++) {
					ind = new File(targ, HTTPConstants.indices.get(i));
					if (ind.exists()) {
						targ = ind;
						break;
					}
				}
			}
			boolean OK = targ.exists();
			if (OK) {
				sendHTTP(out, HTTPConstants.HTTP_OK);
				if (targ.isDirectory())
					printInitialHeaders(out);
				else
					printHeaders(out, targ);
			} else
				runErrorHandler(HTTPConstants.HTTP_NOT_FOUND, out, url, os);
			out.flush();
			if (operation > 0 && OK) {
				byte[] b = new byte[2048];
				InputStream is = new FileInputStream(targ);
				int r;
				while ((r = is.read(b)) > 0)
					os.write(b, 0, r);
				os.flush();
			} else if (!OK && parent.errorHandler == null) {
				/*AppLib.printDebug("404 Impending Doom");
				out.print("\n404 Not Found!\nNo such address.");
				out.flush();*/
			}
		} catch (Exception e) {
			if (os != null && url != null && !out.checkError())
				handleException(out, e, url, os);
		} finally {
			sock.close();
		}
	}
	/**
	 * Prints out an internal server error on failure!
	 * 
	 * @param out the stream to which to send the data
	 * @param e the error
	 * @param url the page that caused the error
	 * @param os the output stream
	 */
	private void handleException(PrintWriter out, Exception e, String url, OutputStream os) {
		AppLib.print("Warning: Internal Server Error!");
		AppLib.debugException(e);
		if (parent.errorHandler == null) {
			if (!headersSent) {
				sendHTTP(out, HTTPConstants.HTTP_INTERNAL_ERROR);
				printInitialHeaders(out);
				headersSent = true;
			}
			out.print("\n\n<html><head><title>Internal Server Error</title></head><body>\n" +
				"The server encountered an error:<br><pre>\n");
			e.printStackTrace(out);
			out.print("\n</pre></body></html>");
			out.flush();
		} else
			parent.errorHandler.action("error", inet, "501;" + url, out, os);
	}
	/**
	 * Prints the headers for a given file.
	 * 
	 * @param out the stream to which to send the headers
	 * @param targ the file to send
	 * @throws IOException if an I/O error occurs
	 */
	private void printHeaders(PrintWriter out, File targ) throws IOException {
		out.print("Last-Modified: ");
		out.print(new Date(targ.lastModified()));
		out.print("\r\n");
		String ext = targ.getName();
		if (ext.indexOf('.') > 0)
			ext = ext.substring(ext.lastIndexOf('.'));
		else
			ext = "";
		String ct = HTTPConstants.mimeTypes.get(ext); 
		if (ct == null) ct = HTTPConstants.mimeTypes.get("");
		printHeaders(out, targ.length(), ct);
	}
	/**
	 * Prints out the top-level headers (server, date)
	 * 
	 * @param out the stream to which to send the headers
	 */
	public static void printInitialHeaders(PrintWriter out) {
		out.print("Server: JAVA Application\r\nDate: ");
		out.print(new Date());
		out.print("\r\n");
	}
	/**
	 * Prints out the headers for a given file type and length.
	 * 
	 * @param out the stream to which to send the headers
	 * @param len the length of the output
	 * @param type the content type of the output
	 * @throws IOException if an I/O error occurs
	 */
	private void printHeaders(PrintWriter out, long len, String type) throws IOException {
		printInitialHeaders(out);
		out.print("Content-Length: ");
		out.print(len);
		out.print("\r\n");
		out.print("Content-Type: ");
		out.print(type);
		out.print("\r\n\r\n");
		headersSent = true;
	}
	/**
	 * Sends a directory listing (HTML) over the given connection.
	 * 
	 * @param dir the directory to list
	 * @param out the stream to which to send the data
	 * @throws IOException if an I/O error occurs
	 */
	public static void listDirectory(File dir, PrintWriter out) throws IOException {
		out.println("<html><head><title>Directory Listing</title></head><body>");
		out.println("<a href=\"..\">Parent Directory</a><br>");
		String[] list = dir.list();
		for (int i = 0; list != null && i < list.length; i++) {
			File f = new File(dir, list[i]);
			out.print("<a href=\"");
			if (f.isDirectory())
				out.print(list[i] + "/\">" + list[i]);
			else
				out.print(list[i] + "\">"+ list[i]);
			out.println("</a><br>");
		}
		out.println("</body></html>");
	}
}

/**
 * A class with HTTP constants.
 * 
 * @author Stephen Carlson
 * @version 1.0.0
 */
final class HTTPConstants {
	/* 2XX: generally "OK" */
	public static final int HTTP_OK = 200;
	public static final int HTTP_CREATED = 201;
	public static final int HTTP_ACCEPTED = 202;
	public static final int HTTP_NOT_AUTHORITATIVE = 203;
	public static final int HTTP_NO_CONTENT = 204;
	public static final int HTTP_RESET = 205;
	public static final int HTTP_PARTIAL = 206;

	/* 3XX: relocation/redirect */
	public static final int HTTP_MULT_CHOICE = 300;
	public static final int HTTP_MOVED_PERM = 301;
	public static final int HTTP_MOVED_TEMP = 302;
	public static final int HTTP_SEE_OTHER = 303;
	public static final int HTTP_NOT_MODIFIED = 304;
	public static final int HTTP_USE_PROXY = 305;

	/* 4XX: client error */
	public static final int HTTP_BAD_REQUEST = 400;
	public static final int HTTP_UNAUTHORIZED = 401;
	public static final int HTTP_PAYMENT_REQUIRED = 402;
	public static final int HTTP_FORBIDDEN = 403;
	public static final int HTTP_NOT_FOUND = 404;
	public static final int HTTP_BAD_METHOD = 405;
	public static final int HTTP_NOT_ACCEPTABLE = 406;
	public static final int HTTP_PROXY_AUTH = 407;
	public static final int HTTP_CLIENT_TIMEOUT = 408;
	public static final int HTTP_CONFLICT = 409;
	public static final int HTTP_GONE = 410;
	public static final int HTTP_LENGTH_REQUIRED = 411;
	public static final int HTTP_PRECON_FAILED = 412;
	public static final int HTTP_ENTITY_TOO_LARGE = 413;
	public static final int HTTP_REQ_TOO_LONG = 414;
	public static final int HTTP_UNSUPPORTED_TYPE = 415;

	/* 5XX: server error */
	public static final int HTTP_SERVER_ERROR = 500;
	public static final int HTTP_INTERNAL_ERROR = 501;
	public static final int HTTP_BAD_GATEWAY = 502;
	public static final int HTTP_UNAVAILABLE = 503;
	public static final int HTTP_GATEWAY_TIMEOUT = 504;
	public static final int HTTP_VERSION = 505;

	/**
	 * Map of extensions to content types.
	 */
	public static HashMap<String, String> mimeTypes = new HashMap<String, String>(38);
	/**
	 * Map of HTTP codes to status messages.
	 */
	public static HashMap<Integer, String> messages = new HashMap<Integer, String>(46);
	/**
	 * List of acceptable directory indices.
	 */
	public static List<String> indices = new ArrayList<String>();

	static {
		// default to application/octet-stream - can't go wrong with binary
		mimeTypes.put("", "application/octet-stream");
		mimeTypes.put(".class", "application/octet-stream");
		mimeTypes.put(".exe", "application/octet-stream");
		mimeTypes.put(".zip", "application/zip");
		mimeTypes.put(".gz", "application/x-gzip");
		mimeTypes.put(".tar", "application/x-tar");
		mimeTypes.put(".gif", "image/gif");
		mimeTypes.put(".bmp", "image/bmp");
		mimeTypes.put(".png", "image/png");
		mimeTypes.put(".jpg", "image/jpg");
		mimeTypes.put(".jpeg", "image/jpg");
		mimeTypes.put(".htm", "text/html");
		mimeTypes.put(".html", "text/html");
		mimeTypes.put(".pl", "text/plain");
		mimeTypes.put(".php", "text/plain");
		mimeTypes.put(".asp", "text/plain");
		mimeTypes.put(".cgi", "text/plain");
		mimeTypes.put(".txt", "text/plain");
		mimeTypes.put(".java", "text/plain");
		mimeTypes.put(".pdf", "application/pdf");
		mimeTypes.put(".swf", "application/x-shockwave-flash");
		mimeTypes.put(".wrl", "model/vrml");
		mimeTypes.put(".css", "text/css");
		mimeTypes.put(".xml", "application/xml");
		mimeTypes.put(".xsl", "application/xml");
		mimeTypes.put(".doc", "application/msword");
		mimeTypes.put(".xls", "application/vnd.ms-excel");
		mimeTypes.put(".ppt", "application/vnd.ms-powerpoint");

		indices.add("index.htm");
		indices.add("index.html");
		indices.add("default.htm");
		indices.add("default.html");

		messages.put(200, "OK");
		messages.put(201, "Created");
		messages.put(202, "Accepted");
		messages.put(203, "Non-Authoritative Content");
		messages.put(204, "No Content");
		messages.put(205, "Connection Reset");
		messages.put(206, "Partial Content");
		messages.put(300, "Multiple Choices");
		messages.put(301, "Moved Permanently");
		messages.put(302, "Moved Temporarily");
		messages.put(303, "See Other");
		messages.put(304, "Not Modified");
		messages.put(305, "Use Proxy");
		messages.put(400, "Bad Request");
		messages.put(401, "Unauthorized");
		messages.put(402, "Payment Required");
		messages.put(403, "Forbidden");
		messages.put(404, "Not Found");
		messages.put(405, "Bad Method");
		messages.put(406, "Not Acceptable");
		messages.put(407, "Proxy Authentication Required");
		messages.put(408, "Client Timeout");
		messages.put(409, "Conflict");
		messages.put(410, "Gone");
		messages.put(411, "Length Required");
		messages.put(412, "Precondition Failed");
		messages.put(413, "Entity Too Large");
		messages.put(414, "Request Too Long");
		messages.put(415, "Unsupported Method Type");
		messages.put(500, "Internal Server Error");
		messages.put(501, "Internal Script Error");
		messages.put(502, "Bad Gateway");
		messages.put(503, "Unavailable");
		messages.put(504, "Gateway Timed Out");
		messages.put(505, "Unsupported Version");
	}
}