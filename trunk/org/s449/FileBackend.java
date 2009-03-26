package org.s449;

import java.io.*;
import java.util.*;

/**
 * Controls java access to the scouting data.
 * 
 * @author Stephen Carlson
 * @version 3.5.0
 */
public class FileBackend extends Backend {
	/**
	 * The DAT file with the data. Data is cached, so editing this file
	 *  while the program is running will be ineffective.
	 */
	private File datFile;

	/**
	 * Creates a new backend data structure backed by a specified file.
	 * 
	 * @param fileName name of the DAT file to read/write configuration data
	 */
	public FileBackend(String fileName) {
		this(new File(fileName));
	}
	/**
	 * Creates a new backend data structure backed by a specified file.
	 * 
	 * @param file the DAT file to read/write configuration data
	 */
	public FileBackend(File file) {
		super();
		datFile = file;
		if (!datFile.exists())
			throw new RuntimeException("Please configure the program before running it.");
		if (!datFile.canRead())
			throw new RuntimeException("Cannot find/access: " + datFile.getAbsolutePath());
		writable = datFile.canWrite();
		if (!writable) AppLib.printWarn(null, "The program's data file is not writable, so changes " +
			"will NOT be saved.\nSuggestions:\n1. Ensure that the file is not set to read-only." +
			"\n2. Check file permissions.\n3. Re-configure the program.");
		flush();
	}
	public void setData(DataStore data) {
		super.setData(data);
		update();
	}
	protected void finalize() throws Throwable {
		update();
		super.finalize();
	}
	/**
	 * Refreshes the data from the file.
	 */
	public synchronized void flush() {
		read();
		firstRank();
	}
	private void read() {
		if (!writable) return;
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(datFile));
			data = (DataStore)ois.readObject();
			if (data.getEvents() == null) data.setEvents(new ArrayList<Event>(5));
			if (data.getUDFs() == null) data.setUDFs(new ArrayList<UDF>(1));
			ois.close();
		} catch (Exception e) {
			data = null;
			throw new RuntimeException("Cannot read: " + datFile.getAbsolutePath());
		}
	}
	/**
	 * Writes the stored data to the file.
	 */
	public synchronized void update() {
		if (!writable) return;
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(datFile));
			oos.writeObject(data);
			oos.close();
		} catch (Exception e) {
			throw new RuntimeException("Cannot write: " + datFile.getAbsolutePath());
		}
	}
	public void addTeam(Team toAdd) {
		super.addTeam(toAdd);
		update();
	}
	public void removeTeam(int toDel) {
		super.removeTeam(toDel);
		update();
	}
	public void addMatch(ScheduleItem match) {
		super.addMatch(match);
		update();
	}
	public void delMatch(ScheduleItem match) {
		super.delMatch(match);
		update();
	}
	public void delMatches(Collection<ScheduleItem> matches) {
		super.delMatches(matches);
		update();
	}
	public void editMatch(long oldTime, ScheduleItem match) {
		super.editMatch(oldTime, match);
		update();
	}
	public void runLate(int minutes) {
		super.runLate(minutes);
		update();
	}
	public void setType(int team, String newType) {
		super.setType(team, newType);
		update();
	}
	public void scoreMatch(ScheduleItem match) {
		super.scoreMatch(match);
		update();
	}
	public void updateComment(int team, Comment comment) {
		super.updateComment(team, comment);
		update();
	}
	public void addMatches(Collection<ScheduleItem> match) {
		super.addMatches(match);
		update();
	}
}