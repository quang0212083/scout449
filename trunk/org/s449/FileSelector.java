package org.s449;

import java.awt.event.*;
import java.io.File;
import javax.swing.*;

/**
 * A file selector not unlike those in a browser.
 * 
 * @author Stephen Carlson
 * @version 1.0.0
 */
public class FileSelector extends Box implements ActionListener {
	private static final long serialVersionUID = -23146128374612934L;
	/**
	 * The text field with the file.
	 */
	private JTextField file;
	/**
	 * The title.
	 */
	private JLabel title;
	/**
	 * The button to select.
	 */
	private JButton select;
	/**
	 * The file in the box.
	 */
	private File myFile;
	/**
	 * The file chooser.
	 */
	private JFileChooser chooser;

	/**
	 * Creates a new file selector with no file.
	 * 
	 * @param tit the label of the file selector
	 * @param dir whether the file selector should select directories
	 */
	public FileSelector(String tit, boolean dir) {
		super(BoxLayout.X_AXIS);
		chooser = new JFileChooser();
		chooser.setFileHidingEnabled(true);
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setDialogTitle("Select File");
		chooser.setMultiSelectionEnabled(false);
		setDirectorySelect(dir);
		select = new JButton("Browse...");
		select.setFocusable(false);
		select.setActionCommand("browse");
		select.addActionListener(this);
		title = new JLabel(tit);
		file = new JTextField(32);
		add(title);
		add(Box.createHorizontalStrut(5));
		add(file);
		add(Box.createHorizontalStrut(5));
		add(select);
		myFile = null;
		validate();
	}
	/**
	 * Sets whether the FileSelector should accept directories (true)
	 *  or files (false). This only affects future selections.
	 * 
	 * @param selectDir whether this object should accept directories
	 */
	public void setDirectorySelect(boolean selectDir) {
		chooser.setFileSelectionMode(selectDir ?
			JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY);
	}
	/**
	 * Sets the file filter for input. Passing null restores the all
	 *  files selection behavior
	 * 
	 * @param filter the new file filter
	 */
	public void setFileFilter(javax.swing.filechooser.FileFilter filter) {
		chooser.setFileFilter(filter);
	}
	/**
	 * Sets the file in the box.
	 * 
	 * @param fileName the name of the file to show
	 */
	public void setFileName(String fileName) {
		if (fileName == null) {
			myFile = null;
			file.setText("");
		} else {
			file.setText(fileName);
			myFile = new File(fileName);
		}
	}
	/**
	 * Sets the file in the box.
	 * 
	 * @param fil the file to show
	 */
	public void setFile(File fil) {
		if (fil == null)
			file.setText("");
		else
			file.setText(fil.getAbsolutePath());
		myFile = fil;
	}
	/**
	 * Changes the title.
	 * 
	 * @param tit the new title
	 */
	public void setTitle(String tit) {
		title.setText(tit);
	}
	/**
	 * Gets the file in the box.
	 * 
	 * @return the file currently selected, or null if no file is selected
	 */
	public File getFile() {
		return myFile;
	}

	// Handles the action events.
	public void actionPerformed(ActionEvent e) {
		// only the browse button
		String cmd = e.getActionCommand();
		if (cmd == null) return;
		else if (cmd.equals("browse")) {
			if (chooser.showDialog(getParent(), "Select") == JFileChooser.APPROVE_OPTION) {
				File fil = chooser.getSelectedFile();
				if (!fil.exists())
					JOptionPane.showMessageDialog(this, "This file does not exist.", "Error",
						JOptionPane.ERROR_MESSAGE);
				else setFile(fil);
			}
		}
	}
}