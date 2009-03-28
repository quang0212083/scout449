package org.s449;

import java.io.*;

public class UserFile {
	/**
	 * The DAT file with the data. Data is cached, so editing this file
	 *  while the program is running will be ineffective.
	 */
	private File datFile;
	/**
	 * Whether write-back is enabled or available.
	 */
	private boolean writable;
	/**
	 * The file-backed config store.
	 */
	private UserStore data;

	public UserFile(String fileName) {
		this(new File(fileName));
	}
	public UserFile(File file) {
		datFile = file;
		if (!datFile.exists()) try {
			datFile.createNewFile();
			data = new UserStore();
			writable = true;
			update();
		} catch (Exception e) {
			AppLib.debugException(e);
			throw new RuntimeException("Configuration could not be found and cannot be created.");
		}
		if (!datFile.canRead())
			throw new RuntimeException("Cannot find/access: " + datFile.getAbsolutePath());
		writable = datFile.canWrite();
		if (!writable) AppLib.printWarn(null, "The program's config file is not writable, " +
			"so changes will NOT be saved.\nSuggestions:\n1. Ensure that the file is not set " +
			"to read-only.\n2. Check file permissions.\n3. Re-configure the program.");
		flush();
	}
	/**
	 * Refreshes the data from the file.
	 */
	public synchronized void flush() {
		read();
	}
	private void read() {
		if (!writable) return;
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(datFile));
			data = (UserStore)ois.readObject();
			if (data.getName() == null) data.setName("default");
			ois.close();
		} catch (Exception e) {
			data = null;
			AppLib.debugException(e);
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
			AppLib.debugException(e);
			throw new RuntimeException("Cannot write: " + datFile.getAbsolutePath());
		}
	}
	/**
	 * Gets the configuration data.
	 * 
	 * @return the config
	 */
	public UserStore getData() {
		return data;
	}
}