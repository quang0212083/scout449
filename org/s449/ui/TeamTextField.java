package org.s449.ui;

import org.s449.*;
import java.awt.event.*;
import java.util.*;

public class TeamTextField extends AutoTextField<Team> {
	private static final long serialVersionUID = 0L;

	/**
	 * The ScoutStatus responsible for this team text field.
	 */
	private ScoutStatus status;

	/**
	 * Class to only allow non-negative numeric entries into a text box.
	 */
	private class NumericOnlyListener extends KeyAdapter {
		private static final long serialVersionUID = 0L;

		public void keyTyped(KeyEvent e) {
			char pressed = e.getKeyChar();
			if ((pressed < '0' || pressed > '9') && pressed != '\b') e.consume();
		}
		public void keyReleased(KeyEvent e) {
			update();
		}
	}

	public TeamTextField(ScoutStatus stat, List<Team> list) {
		super(list);
		setColumns(5);
		status = stat;
		addFocusListener(TextSelector.INSTANCE);
		addKeyListener(new NumericOnlyListener());
		update();
	}
	protected String getMatch(String s) {
		if (s.length() < 1) return null;
		Iterator<Team> it = dataList.iterator();
		while (it.hasNext()) {
			String s1 = Integer.toString(it.next().getNumber());
			if (s1 != null && s1.startsWith(s))
				return s1;
		}
		return null;
	}
	public void update() {
		String text = getText();
		if (text == null || text.length() < 1) setBackground(Constants.WHITE);
		else try {
			int i = Integer.parseInt(text);
			if (status.getBackend().get(i) == null) setBackground(Constants.LIGHT_RED);
			else setBackground(Constants.LIGHT_GREEN);
		} catch (Exception e) {
			setBackground(Constants.RED);
		}
	}
	public int getTeamNum() {
		String text = getText();
		if (text == null || text.length() < 1) return -1;
		try {
			int i = Integer.parseInt(text);
			if (status.getBackend().get(i) == null) return -1;
			else return i;
		} catch (Exception e) {
			return -1;
		}
	}
}