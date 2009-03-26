package org.s449;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.util.Calendar;
import java.util.StringTokenizer;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A class representing a time entry dialog.
 * 
 * @author Stephen Carlson
 * @version 4.0.0
 */
public class TimeEntry extends JPanel {
	private static final long serialVersionUID = 0L;

	/**
	 * The time currently entered.
	 */
	private JTextField time;
	/**
	 * The AM/PM selector.
	 */
	private JComboBox ampm;
	/**
	 * The calendar object.
	 */
	private Calendar global;
	/**
	 * The scout status object.
	 */
	private ScoutStatus status;

	/**
	 * Creates a new time entry box.
	 * 
	 * @param stat the ScoutStatus responsible for this object
	 * @param hr true if 24 hour, false if 12 hour
	 */
	public TimeEntry(ScoutStatus stat, boolean hr) {
		setLayout(new FlowLayout());
		time = new JTextField(6);
		time.addFocusListener(TextSelector.INSTANCE);
		status = stat;
		global = Calendar.getInstance();
		add(time);
		if (!hr) {
			ampm = new JComboBox();
			ampm.setEditable(false);
			ampm.addItem("AM");
			ampm.addItem("PM");
			add(ampm);
		}
		setMaximumSize(getPreferredSize());
	}
	public void addKeyListener(KeyListener l) {
		time.addKeyListener(l);
	}
	public void requestFocus() {
		time.requestFocus();
	}
	/**
	 * Sets the time in the box. Erases the user's entry!!
	 * 
	 * @param tm the time to set
	 */
	public void setTime(long tm) {
		global.setTimeInMillis(tm);
		// MAYBE: should this be replaced with timeFormat?
		String output; int hr;
		if (ampm != null) {
			if (global.get(Calendar.AM_PM) == Calendar.PM)
				ampm.setSelectedIndex(1);
			else
				ampm.setSelectedIndex(0);
			hr = global.get(Calendar.HOUR);
			if (hr == 0) hr = 12;
			output = Integer.toString(hr);
		} else {
			hr = global.get(Calendar.HOUR_OF_DAY);
			output = Integer.toString(hr);
		}
		String mm = Integer.toString(global.get(Calendar.MINUTE));
		if (mm.length() < 2) mm = "0" + mm;
		time.setText(output + ":" + mm);
	}
	/**
	 * Gets the time in the box.
	 * 
	 * @param seedTime the reference time (for the date)
	 * @return the time entered
	 */
	public long getTime(long seedTime) {
		global.setTimeInMillis(seedTime);
		StringTokenizer str = new StringTokenizer(time.getText(), ":");
		try {
			// parse hours and minutes
			int hh = Integer.parseInt(str.nextToken());
			int mm = Integer.parseInt(str.nextToken());
			if (hh >= 0 && mm >= 0 && hh < 24 && (ampm == null || hh < 13) && mm < 60) {
				if (ampm == null)
					// set hour of day
					global.set(Calendar.HOUR_OF_DAY, hh);
				else {
					// set am/pm
					global.set(Calendar.HOUR, hh);
					if (ampm.getSelectedIndex() > 0)
						global.set(Calendar.AM_PM, Calendar.PM);
					else
						global.set(Calendar.AM_PM, Calendar.AM);
				}
				global.set(Calendar.MINUTE, mm);
			} else {
				AppLib.printWarn(status.getWindow(), "The time entered was not valid.\n" +
					"It must be in the format hh:mm.");
				return -1L;
			}
		} catch (Exception e) {
			// error in time entry
			AppLib.debugException(e);
			AppLib.printWarn(status.getWindow(), "The time entered was not valid.\n" +
				"It must be in the format hh:mm.");
			return -1L;
		}
		return global.getTimeInMillis();
	}
	/**
	 * Adds an action listener to the text field.
	 * 
	 * @param l the ActionListener to add
	 */
	public void addActionListener(ActionListener l) {
		time.addActionListener(l);
	}
	/**
	 * Sets the action command of this text field.
	 * 
	 * @param cmd the action command to name the event
	 */
	public void setActionCommand(String cmd) {
		time.setActionCommand(cmd);
	}
}