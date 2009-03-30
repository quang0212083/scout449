package org.s449;

import java.util.*;
import java.io.*;

/**
 * Lists the available roots; used for AutoSync, Image Uploader...
 * 
 * @author Stephen Carlson
 * @version 4.0.0
 */
public class VolumeLister {
	/**
	 * The directory for Mac volumes.
	 */
	private static final File macDir = new File("/Volumes/");
	/**
	 * The directory for *Nix volumes.
	 */
	private static final File nixDir = new File("/media/");
	/**
	 * Volume list.
	 */
	private static File[] volumes;
	/**
	 * Time at last update.
	 */
	private static long lastUpdate;
	/**
	 * The OS name.
	 */
	private static String os;

	private static boolean exclude(int system, String path) {
		if (system == 0) // win
			return path.startsWith("A:") || path.startsWith("B:") || path.startsWith("C:");
		if (system == 1) // mac
			return path.indexOf("HD") > 0;
		if (system == 2) // nix
			return path.indexOf("swap") < 0 && path.indexOf("var") < 0 && path.indexOf("temp") < 0
				&& path.indexOf("tmp") < 0 && path.indexOf("root") < 0 && path.indexOf("rt") < 0;
		return false;
	}
	/**
	 * Gets a list of volumes (for images and FLASH).
	 * 
	 * @return a list of volumes
	 */
	public static synchronized File[] listVolumes() {
		if (volumes == null ||
				System.currentTimeMillis() - 2000L > lastUpdate) {
			File[] files = File.listRoots();
			if (os == null)
				os = System.getProperty("os.name").toLowerCase();
			if (os.indexOf("win") < 0) {
				if (os.indexOf("mac") >= 0)
					volumes = exclude(macDir.listFiles(), 1);
				else
					volumes = exclude(nixDir.listFiles(), 2);
			} else
				volumes = exclude(files, 0);
			lastUpdate = System.currentTimeMillis();
		}
		return volumes;
	}
	/**
	 * Flushes the cache of volumes to force a re-read on the next list.
	 */
	public static synchronized void flushCache() {
		volumes = null;
	}
	/**
	 * Ejects one of the volumes on the list.
	 * 
	 * @param volume the volume to eject
	 */
	public static synchronized void eject(File volume) {
		if (os == null)
			os = System.getProperty("os.name").toLowerCase();
		if (os.indexOf("mac") >= 0)
			run("diskutil unmount " + volume.getAbsolutePath());
		else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);
			run("umount " + volume.getAbsolutePath());
		flushCache();
	}
	private static void run(String name) {
		try {
			Process p = Runtime.getRuntime().exec(name);
			p.waitFor();
		} catch (Exception e) {
			AppLib.debugException(e);
		}
	}
	private static File[] exclude(File[] files, int bad) {
		ArrayList list = new ArrayList(files.length);
		String path;
		for (int i = 0; i < files.length; i++) {
			path = files[i].getAbsolutePath();
			if (!exclude(bad, path)) list.add(files[i]);
		}
		File[] answer = new File[list.size()];
		for (int i = 0; i < list.size(); i++)
			answer[i] = (File)list.get(i);
		return answer;
	}
	/**
	 * Calls up the JVM with the given parameters.
	 * 
	 * @param params the JVM parameters
	 */
	public static void invokeJVM(String params) {
		String jreHome = System.getProperty("java.home");
		try {
			Runtime.getRuntime().exec(jreHome + File.separator + "bin" + File.separator + "java "
				+ params);
		} catch (Exception e) {
			AppLib.debugException(e);
		}
	}
	/**
	 * Compares two versions.
	 * 
	 * @param one the first version
	 * @param two the second version
	 * @return which version is greater: -1 if two is greater, 1 if one is greater, 0 if equal
	 */
	public static int versionCompare(String one, String two) {
		StringTokenizer s1 = new StringTokenizer(one, ".");
		StringTokenizer s2 = new StringTokenizer(two, ".");
		int v1, v2;
		while (s1.hasMoreTokens() && s2.hasMoreTokens()) {
			try {
				v1 = Integer.parseInt(s1.nextToken());
				v2 = Integer.parseInt(s2.nextToken());
			} catch (Exception e) {
				// error
				return 0;
			}
			if (v1 > v2) return 1;
			else if (v2 > v1) return -1;
		}
		if (s1.hasMoreTokens()) return 1;
		if (s2.hasMoreTokens()) return -1;
		return 0;
	}
}