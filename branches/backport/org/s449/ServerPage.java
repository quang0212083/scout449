package org.s449;

/**
 * An interface that should be implemented by any classes
 *  wishing to act as a server-side interface.
 * 
 * @author Stephen Carlson
 * @version 1.0.0
 */
public interface ServerPage {
	/**
	 * Called by the server when an action is required. Must send the
	 *  headers and content to the browser.
	 * 
	 * @param url the url that was passed (with any GET data)
	 * @param inet the IP address or host name of the client
	 * @param inData the input data in the headers
	 * @param out the object representing the output stream to the client
	 * @param os the raw output stream to the client for binary transfers
	 */
	public void action(String url, String inet, String inData,
		java.io.PrintWriter out, java.io.OutputStream os);
	/**
	 * Returns whether this server page should handle the given URL.
	 * 
	 * @param url the URL that was passed
	 * @return whether this page should serve data
	 */
	public boolean match(String url);
}