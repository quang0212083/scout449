package org.s449.extra;

import java.io.*;
import java.net.*;
import javax.swing.*;

public class SelfUpdater {
	public static void main(String[] args) {
		if (args.length < 1) return;
		File old = new File("scout449.jar");
		String url = args[0];
		if (url.startsWith("\"") && url.endsWith("\"") && url.length() > 2)
			url = url.substring(1, url.length() - 1);
		try {
			Thread.sleep(5000L);
			URL uurl = new URL(url);
			InputStream is = new ProgressMonitorInputStream(null,
				"Updating Scout449", uurl.openStream());
			FileOutputStream fos = new FileOutputStream(old);
			copyStream(is, fos);
			is.close();
			fos.close();
			invokeJVM("-jar scout449.jar");
			System.exit(0);
		} catch (Exception e) {
			if (e instanceof InterruptedIOException) System.exit(0);
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not update Scout449.\n\n"
				+ e.getMessage(), "Updating Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}
	private static void copyStream(InputStream is, OutputStream os) throws IOException {
		byte[] buffer = new byte[16384];
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
	private static void invokeJVM(String params) throws Exception {
		String jreHome = System.getProperty("java.home");
		Runtime.getRuntime().exec(jreHome + File.separator + "bin" + File.separator + "java "
			+ params);
	}
}