package org.s449;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import org.s449.ui.*;

/**
 * A dialog to enter a match.
 * 
 * @author Stephen Carlson
 * @version 4.0.0
 */
public class MatchDialog extends BasicDialog {
	/**
	 * The action listener fired on close.
	 */
	private ActionListener close;
	/**
	 * The field for the match number.
	 */
	private JTextField matchNum;
	/**
	 * The field for the time.
	 */
	private TimeEntry time;
	/**
	 * The day-of-week selection box.
	 */
	private JTextField when;
	/**
	 * The label combo box. (practice, qualifications...)
	 */
	private JComboBox label;
	/**
	 * Red teams.
	 */
	private TeamTextField[] red;
	/**
	 * Blue teams.
	 */
	private TeamTextField[] blue;
	/**
	 * The surrogate checkboxes.
	 */
	private JCheckBox[] sBlue;
	/**
	 * The surrogate checkboxes.
	 */
	private JCheckBox[] sRed;
	/**
	 * The event listener.
	 */
	private LocalEventListener events;

	/**
	 * Sets up the match enterer to a blank screen.
	 * 
	 * @param status the ScoutStatus responsible for this match enterer
	 */
	public MatchDialog(ScoutStatus status) {
		super(status);
		EscapeKeyListener wl = new EscapeKeyListener(window);
		close = null;
		events = new LocalEventListener();
		// init window
		window.setTitle("Enter Match");
		window.setResizable(false);
		window.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		Container c = window.getContentPane();
		c.setLayout(new VerticalFlow(true));
		c.add(Box.createVerticalStrut(5));
		// first time and match number
		JComponent row = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 5));
		time = new TimeEntry(status, ScheduleItem.is24);
		time.setActionCommand("saveMatch");
		time.addActionListener(events);
		time.addKeyListener(wl);
		matchNum = new JTextField(3);
		matchNum.setActionCommand("saveMatch");
		matchNum.addActionListener(events);
		matchNum.addKeyListener(wl);
		matchNum.addFocusListener(TextSelector.INSTANCE);
		// when
		when = new JTextField(8);
		when.addKeyListener(wl);
		// label
		label = new JComboBox();
		label.addKeyListener(wl);
		label.removeAllItems();
		List<MatchLabel> labels = status.getDataStore().getLabels();
		Iterator<MatchLabel> it = labels.iterator();
		while (it.hasNext())
			label.addItem(it.next());
		label.setMaximumSize(label.getPreferredSize());
		label.setEditable(false);
		// set it up
		row.add(Box.createHorizontalStrut(2));
		row.add(new JLabel("Time:"));
		row.add(time);
		row.add(Box.createHorizontalStrut(7));
		row.add(new JLabel("When:"));
		row.add(when);
		row.add(Box.createHorizontalStrut(7));
		row.add(new JLabel("Match Number:"));
		row.add(matchNum);
		row.add(Box.createHorizontalStrut(7));
		row.add(new JLabel("Label:"));
		row.add(label);
		row.add(Box.createHorizontalStrut(2));
		// next teams label
		c.add(row);
		c.add(Box.createVerticalStrut(5));
		row = new JPanel();
		row.setLayout(new FlowLayout());
		row.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		// initialize red and blue fields
		blue = new TeamTextField[ScheduleItem.TPA];
		sBlue = new JCheckBox[ScheduleItem.TPA];
		red = new TeamTextField[ScheduleItem.TPA];
		sRed = new JCheckBox[ScheduleItem.TPA];
		JComponent vert; JLabel lbl; LocalKeyListener lk;
		for (int i = 0; i < ScheduleItem.TPA; i++) {
			blue[i] = new TeamTextField(status, status.getClient().getTeamList());
			blue[i].addKeyListener(wl);
			red[i] = new TeamTextField(status, status.getClient().getTeamList());
			red[i].addKeyListener(wl);
			blue[i].setAlignmentX(JComponent.CENTER_ALIGNMENT);
			red[i].setAlignmentX(JComponent.CENTER_ALIGNMENT);
			sBlue[i] = new JCheckBox("S");
			sBlue[i].setFocusable(false);
			sRed[i] = new JCheckBox("S");
			sRed[i].setFocusable(false);
			sBlue[i].setAlignmentX(JComponent.CENTER_ALIGNMENT);
			sRed[i].setAlignmentX(JComponent.CENTER_ALIGNMENT);
			if (i == ScheduleItem.TPA - 1) {
				red[i].setActionCommand("blue0");
				blue[i].setActionCommand("saveMatch");
				// add to end
				lk = new LocalKeyListener(blue[0]);
				red[i].addKeyListener(lk);
				lk = new LocalKeyListener(null);
				blue[i].addKeyListener(lk);
			} else {
				red[i].setActionCommand("red" + (i + 1));
				blue[i].setActionCommand("blue" + (i + 1));
			}
			if (i > 0) {
				// add key listeners to past
				lk = new LocalKeyListener(red[i]);
				red[i - 1].addKeyListener(lk);
				lk = new LocalKeyListener(blue[i]);
				blue[i - 1].addKeyListener(lk);
			}
			red[i].addActionListener(events);
			blue[i].addActionListener(events);
			// can add reds right away
			lbl = new JLabel("Red " + (i + 1));
			lbl.setForeground(Constants.RED);
			lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
			vert = new JPanel(new VerticalFlow(false));
			vert.add(lbl);
			vert.add(Box.createVerticalStrut(5));
			vert.add(red[i]);
			vert.add(sRed[i]);
			row.add(vert);
		}
		row.add(Box.createHorizontalStrut(15));
		// blues now
		for (int i = 0; i < ScheduleItem.TPA; i++) {
			lbl = new JLabel("Blue " + (i + 1));
			lbl.setForeground(Constants.BLUE);
			lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
			vert = new JPanel(new VerticalFlow(false));
			vert.add(lbl);
			vert.add(Box.createVerticalStrut(5));
			vert.add(blue[i]);
			vert.add(sBlue[i]);
			row.add(vert);
		}
		c.add(row);
		c.add(Box.createVerticalStrut(5));
		// key listener run
		time.addKeyListener(new LocalKeyListener(when));
		when.addKeyListener(new LocalKeyListener(matchNum));
		matchNum.addKeyListener(new LocalKeyListener(red[0]));
		// last row
		JButton ok = new JButton("OK");
		ok.setActionCommand("saveMatch");
		ok.setMnemonic(KeyEvent.VK_O);
		ok.setFocusable(false);
		ok.addActionListener(events);
		JButton can = new JButton("Cancel");
		can.setActionCommand("cancel");
		can.setMnemonic(KeyEvent.VK_C);
		can.setFocusable(false);
		can.addActionListener(events);
		row = new JPanel();
		row.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
		row.add(ok);
		row.add(can);
		// finish
		c.add(row);
		c.add(Box.createVerticalStrut(5));
		window.pack();
		window.addKeyListener(wl);
		centerWindow();
	}
	/**
	 * Changes the action listener fired when the dialog is closed. Event
	 *  command will be "match".
	 * 
	 * @param close the new action listener
	 */
	public void setActionListener(ActionListener close) {
		this.close = close;
	}
	/**
	 * Changes the match number in the field
	 * 
	 * @param num the new match number
	 */
	public void setMatchNum(int num) {
		if (num < 1) num = 1;
		matchNum.setText(Integer.toString(num));
	}
	/**
	 * Clears the team fields.
	 */
	public void clearTeamFields() {
		for (int i = 0; i < ScheduleItem.TPA; i++) {
			sRed[i].setSelected(false);
			red[i].setText("");
			red[i].update();
			sBlue[i].setSelected(false);
			blue[i].setText("");
			blue[i].update();
		}
	}
	/**
	 * Gets the match number.
	 * 
	 * @return the match number.
	 */
	public String getMatchNum() {
		return matchNum.getText();
	}
	/**
	 * Gets the time.
	 * 
	 * @return the time like hh:mm, with the date set in the box
	 */
	public long getTime() {
		try {
			long seedTime = Constants.DATE_FORMAT.parse(when.getText()).getTime();
			return time.getTime(seedTime);
		} catch (Exception e) {
			return -1L;
		}
	}
	/**
	 * Gets the teams.
	 * 
	 * @return the teams
	 */
	public List<Integer> getTeams() {
		List<Integer> ret = new ArrayList<Integer>(ScheduleItem.TPA * 2);
		for (int i = 0; i < ScheduleItem.TPA; i++)
			try {
				ret.add(Integer.parseInt(red[i].getText()));
			} catch (NumberFormatException e) {
				AppLib.printWarn(window, "Please enter a valid team number for Red " + (i + 1));
				red[i].requestFocus();
				red[i].selectAll();
				return null;
			}
		for (int i = 0; i < ScheduleItem.TPA; i++)
			try {
				ret.add(Integer.parseInt(blue[i].getText()));
			} catch (NumberFormatException e) {
				AppLib.printWarn(window, "Please enter a valid team number for Blue " + (i + 1));
				blue[i].requestFocus();
				blue[i].selectAll();
				return null;
			}
		return ret;
	}
	/**
	 * Sets the teams.
	 * 
	 * @param list the new teams
	 */
	public void setTeams(List<Integer> list) {
		clearTeamFields();
		if (list == null || list.size() < ScheduleItem.TPA * 2) return;
		Iterator<Integer> it = list.iterator();
		for (int i = 0; i < 3; i++) {
			red[i].setText(Integer.toString(it.next()));
			red[i].update();
		}
		for (int i = 0; i < 3; i++) {
			blue[i].setText(Integer.toString(it.next()));
			blue[i].update();
		}
	}
	/**
	 * Gets the current label.
	 * 
	 * @return the current label
	 */
	public MatchLabel getLabel() {
		return (MatchLabel)label.getSelectedItem();
	}
	/**
	 * Sets the time.
	 * 
	 * @param time the new time
	 */
	public void setTime(long tm) {
		when.setText(Constants.DATE_FORMAT.format(new Date(tm)));
		time.setTime(tm);
	}
	/**
	 * Gets the surrogate teams.
	 * 
	 * @return a bit set of the surrogate teams
	 */
	public BitSet getSurrogate() {
		BitSet sur = new BitSet(2 * ScheduleItem.TPA);
		int i = 0;
		for (; i < ScheduleItem.TPA; i++)
			sur.set(i, sRed[i].isSelected());
		for (; i < 2 * ScheduleItem.TPA; i++)
			sur.set(i, sBlue[i - ScheduleItem.TPA].isSelected());
		return sur;
	}
	/**
	 * Sets all of the fields to match the given match.
	 * 
	 * @param match the match to load
	 */
	public void setMatch(ScheduleItem match) {
		setTime(match.getTime());
		setMatchNum(match.getNum());
		setTeams(match.getTeams());
		label.setSelectedItem(match.getLabel());
		int i = 0; BitSet surrogate = match.getSurrogate();
		for (; i < ScheduleItem.TPA; i++)
			sRed[i].setSelected(surrogate.get(i));
		for (; i < 2 * ScheduleItem.TPA; i++)
			sBlue[i - ScheduleItem.TPA].setSelected(surrogate.get(i));
	}
	/**
	 * Fires the action listener or exits match mode.
	 */
	private void bail(ActionEvent e) {
		close.actionPerformed(e);
	}
	/**
	 * Closes the window.
	 */
	private void close() {
		setVisible(false);
	}
	public void setVisible(boolean b) {
		super.setVisible(b);
		if (b) {
			red[0].requestFocus();
			red[0].selectAll();
		}
	}

	/**
	 * Class to listen for events.
	 */
	private class LocalEventListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd == null) return;
			else if (cmd.equals("cancel")) close();
			else if (cmd.startsWith("red") && cmd.length() > 3) {
				// focus a red field
				JTextField next = red[Integer.parseInt(cmd.substring(3))];
				next.requestFocus();
				next.selectAll();
			} else if (cmd.startsWith("blue") && cmd.length() > 4) {
				// focus a red field
				JTextField next = blue[Integer.parseInt(cmd.substring(4))];
				next.requestFocus();
				next.selectAll();
			} else if (cmd.equals("saveMatch")) bail(e);
		}
	}

	/**
	 * Only allows numeric entries in the team fields.
	 *  A comma will move to the next one.
	 */
	private class LocalKeyListener extends KeyAdapter {
		/**
		 * The next field.
		 */
		private JTextField next;

		/**
		 * Creates a new numeric only listener on a given text field.
		 * 
		 * @param nx the next field
		 */
		public LocalKeyListener(JTextField nx) {
			next = nx;
		}
		public void keyTyped(KeyEvent e) {
			char c = e.getKeyChar();
			if (c == ',') {
				if (next == null) {
					bail(new ActionEvent(e.getSource(), ActionEvent.ACTION_PERFORMED, "saveMatch"));
					return;
				}
				// next
				next.grabFocus();
				next.selectAll();
				e.consume();
			} else if ((c < '0' || c > '9') && c != ':') e.consume();
		}
	}
}