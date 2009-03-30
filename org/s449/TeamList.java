package org.s449;

import javax.swing.*;
import javax.swing.table.*;
import org.s449.HotkeyList.Hotkey;
import org.s449.ui.*;
import java.util.*;
import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/**
 * A class representing the team sorter window where users can
 *  sort teams by name or statistics and view comments.
 * 
 * @author Stephen Carlson
 * @version 4.0.0
 */
public class TeamList extends JPanel {
	private static final long serialVersionUID = 0L;

	/**
	 * List of available teams.
	 */
	private List data;
	/**
	 * The table with the team list.
	 */
	private JTable list;
	/**
	 * A data model for the table.
	 */
	private TeamTableModel dataModel;
	/**
	 * The search box.
	 */
	private JTextField searchBox;
	/**
	 * The comment viewer used for comment editing.
	 */
	private TeamViewer comments;
	/**
	 * The table header used for mouse events.
	 */
	private JTableHeader head;
	/**
	 * The header renderer.
	 */
	private HeaderRenderer heads;
	/**
	 * Quick-entry box.
	 */
	private TeamTextField qe;
	/**
	 * The titles of the table columns.
	 */
	private String[] titles;
	/**
	 * Column widths.
	 */
	private int[] width;
	/**
	 * The column IDs.
	 */
	private int[] cids;
	/**
	 * The parent.
	 */
	private ScoutStatus status;
	/**
	 * The old length, used for updates.
	 */
	private int olen;

	/**
	 * Creates a team sorter.
	 * 
	 * @param stat the ScoutStatus responsible for this team sorter
	 */
	public TeamList(ScoutStatus stat) {
		super(new BorderLayout(3, 3));
		this.status = stat;
		olen = -1;
		AppLib.printDebug("Setting up team sorter");
		// Init list.
		list = new JTable();
		init();
		data = stat.getClient().getVisualTeamList();
		dataModel = new TeamTableModel();
		list.setModel(dataModel);
		// Set up the top of the list.
		LocalEventListener events = new LocalEventListener();
		list.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setColumnSelectionAllowed(false);
		list.addMouseListener(events);
		// Set the column widths.
		TableColumnModel theColumns = list.getColumnModel();
		for (int i = 0; i < cids.length; i++)
			theColumns.getColumn(i).setPreferredWidth(width[i]);
		// Set options in the table header.
		head = list.getTableHeader();
		head.setReorderingAllowed(false);
		head.setBackground(Constants.WHITE);
		head.addMouseListener(events);
		heads = new HeaderRenderer(status);
		head.setDefaultRenderer(heads);
		// Quick-entry.
		qe = new TeamTextField(status, data);
		qe.addKeyListener(new DelegateKeyListener(stat.getClient()));
		qe.setActionCommand("quick");
		qe.addActionListener(events);
		// Does a search.
		JButton search = ButtonFactory.getButton("Search", "search", events, KeyEvent.VK_S);
		searchBox = new JTextField(16);
		searchBox.setActionCommand("search");
		searchBox.addActionListener(events);
		searchBox.addFocusListener(TextSelector.INSTANCE);
		// Add the list in a scroll pane.
		JScrollPane sp = new JScrollPane(list);
		sp.getViewport().setBackground(Constants.WHITE);
		sp.setBorder(ButtonFactory.getThinBorder());
		add(sp, BorderLayout.CENTER);
		// Add the buttons in the bottom pane.
		java.awt.Container editPane = new Box(BoxLayout.X_AXIS);
		editPane.setBackground(Constants.WHITE);
		editPane.add(Box.createHorizontalStrut(20));
		if (status.getUser().isAdmin()) {
			// add, delete
			editPane.add(ButtonFactory.getButton("Add Team...", "addteam", events, -1));
			editPane.add(Box.createHorizontalStrut(10));
			editPane.add(ButtonFactory.getButton("Remove Team", "delteam", events, -1));
			editPane.add(Box.createHorizontalStrut(10));
			editPane.add(ButtonFactory.getButton("Rename Team", "renteam", events, -1));
			editPane.add(Box.createHorizontalStrut(20));
		}
		editPane.add(new JLabel("Search: "));
		editPane.add(searchBox);
		editPane.add(Box.createHorizontalStrut(10));
		editPane.add(search);
		editPane.add(Box.createHorizontalStrut(20));
		add(editPane, BorderLayout.SOUTH);
		editPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
		editPane.setBackground(Constants.WHITE);
		editPane.add(new JLabel("Lookup Team #:"));
		editPane.add(qe);
		add(editPane, BorderLayout.NORTH);
		list.addKeyListener(new DelegateKeyListener(stat.getClient()));
		setBackground(Constants.WHITE);
	}
	/**
	 * Sets up the CID array.
	 */
	public void init() {
		List udfs = status.getDataStore().getUDFs();
		HotkeyList hotkeys = status.getDataStore().getHotkeys();
		// set up CIDs
		if (status.getDataStore().isAdvScore())
			cids = new int[14 + udfs.size() + hotkeys.size()];
		else
			cids = new int[13 + udfs.size()];
		int i = 0, j;
		for (; i < 6; i++)
			cids[i] = 100 + i;
		if (status.getDataStore().isAdvScore()) {
			// ppg only on adv score
			cids[i] = 100 + i;
			i++;
		}
		for (j = 0; j < 7; j++)
			cids[i + j] = 107 + j;
		i += j;
		for (j = 0; j < udfs.size(); j++)
			cids[i + j] = j;
		i += j;
		if (status.getDataStore().isAdvScore()) {
			for (j = 0; j < hotkeys.size(); j++)
				cids[i + j] = -1 - j;
			i += j;
		}
		// widths and titles
		int[] myWidth = new int[] { 50, 140, 52, 60, 50, 50, 78, 97, 95, 60, 90, 90, 50, 50 };
		String[] myTitles = new String[] { "#", "Team Name", "Wins", "Losses", "Ties", "%",
			"Pts/Game", "Team Pts/Gm", "Opp Pts/Gm", "Rating", "Type", "FIRST Rank", "SP", "RP" };
		width = new int[i];
		titles = new String[i];
		for (j = 0; j < i; j++)
			if (cids[j] >= 100 && cids[j] < 114) {
				width[j] = myWidth[cids[j] - 100];
				titles[j] = myTitles[cids[j] - 100];
			} else if (cids[j] >= 0 && cids[j] < udfs.size()) {
				titles[j] = ((UDF)udfs.get(cids[j])).getName();
				width[j] = SwingUtilities.computeStringWidth(
					list.getFontMetrics(list.getFont()), titles[j]) + 30;
			} else if (cids[j] < 0 && cids[j] > -2 - hotkeys.size()) {
				titles[j] = ((Hotkey)hotkeys.getList().get(-cids[j] - 1)).getDescription();
				width[j] = SwingUtilities.computeStringWidth(
					list.getFontMetrics(list.getFont()), titles[j]) + 30;
			}
	}
	/**
	 * Focuses this team list.
	 */
	public void focus() {
		qe.requestFocus();
	}
	/**
	 * Resets the sort to team #, ascending.
	 */
	public void resetSort() {
		dataModel.sortBy(0, true);
	}
	/**
	 * Updates the list on the screen.
	 */
	public void updateList() {
		if (olen != data.size())
			dataModel.fireTableDataChanged();
		else
			dataModel.fireTableRowsUpdated(0, olen);
		olen = data.size();
		repaint();
	}
	/**
	 * Searches the team list for the given text in comments.
	 */
	private void doSearch() {
		String text = searchBox.getText();
		if (text == null || text.length() < 1) return;
		text = text.toLowerCase();
		ArrayList results = new ArrayList();
		ArrayList what = new ArrayList();
		// search the whole team data list
		Team team;
		Iterator it = data.iterator();
		String comment;
		Iterator it2;
		while (it.hasNext()) {
			team = (Team)it.next();
			if (team.getName().toLowerCase().indexOf(text) >= 0) {
				results.add(team);
				what.add("Team name");
			}
			if (Integer.toString(team.getNumber()).equals(text)) {
				results.add(team);
				what.add("Team number");
			}
			it2 = team.getComments().iterator();
			while (it2.hasNext()) {
				comment = ((Comment)it2.next()).getText();
				if (comment.length() > 0 && comment.toLowerCase().indexOf(text) >= 0) {
					results.add(team);
					what.add("\"" + comment + "\"");
				}
			}
		}
		// show results
		if (results.size() < 1)
			AppLib.printWarn(status.getWindow(), "No results for search: \"" + text + "\"");
		else {
			String[] res = new String[results.size()];
			for (int i = 0; i < results.size(); i++) {
				team = (Team)results.get(i);
				res[i] = team.getNumber() + " " + team.getName() + ": " + what.get(i);
			}
			String ans = (String)JOptionPane.showInputDialog(status.getWindow(), "Search for \"" +
				text + "\" matched:", "Search Results", JOptionPane.INFORMATION_MESSAGE, null,
				res, res[0]);
			if (ans != null && ans.length() > 1) {
				// go to the selected item
				int index = ans.indexOf(' ');
				if (index > 0) try {
					int teamNum = Integer.parseInt(ans.substring(0, index));
					openComments(status.getBackend().get(teamNum));
				} catch (Exception e) { }
			}
		}
		searchBox.grabFocus();
	}
	/**
	 * Changes the comment viewer used by the team sorter.
	 * 
	 * @param commentViewer the new comment viewer
	 */
	public void setCommentViewer(TeamViewer commentViewer) {
		comments = commentViewer;
	}
	/**
	 * Opens the comments viewer on a given team.
	 */
	private void openComments(Team team) {
		if (comments == null) return;
		AppLib.printDebug("Opening viewer for " + team.getNumber());
		comments.reset();
		comments.setTeam(team);
		comments.setVisible(true);
	}

	/**
	 * A list model that reads from the team data.
	 */
	private class TeamTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 0L;

		/**
		 * The index to sort by.
		 */
		private int sortingBy;
		/**
		 * Whether sorting is ascending or not.
		 */
		private boolean ascending;

		/**
		 * Creates a table model that will return team data.
		 */
		public TeamTableModel() {
			sortingBy = 0;
			ascending = true;
		}
		public String getColumnName(int column) { return titles[column]; }
		public int getRowCount() { return data.size(); }
		public int getColumnCount() { return cids.length; }
		public Object getValueAt(int row, int col) {
			Team r = (Team)data.get(row);
			switch(cids[col]) {
			case 100:
				return new Integer(r.getNumber());
			case 101:
				return r.getName();
			case 102:
				return new Integer(r.getWins());
			case 103:
				return new Integer(r.getLosses());
			case 104:
				return new Integer(r.getTies());
			case 105:
				return new Double(r.getWinPct());
			case 106:
				return new Double(r.getPPG());
			case 107:
				return new Double(r.getTeamPPG());
			case 108:
				return new Double(r.getEnPPG());
			case 109:
				return (r.getRating() == 0) ? "None" : Double.toString(r.getRating());
			case 110:
				return r.getType();
			case 111:
				return new Integer(r.getFIRSTRank());
			case 112:
				return new Integer(r.getSP());
			case 113:
				return new Integer(r.getRP());
			default:
				HotkeyList hotkeys = status.getDataStore().getHotkeys();
				List udfs = status.getDataStore().getUDFs();
				if (cids[col] >= 0 && cids[col] < udfs.size())
					return r.getData().get(cids[col]);
				else if (cids[col] < 0 && cids[col] > -2 - hotkeys.size() &&
						-cids[col] - 1 < r.getScores().size())
					return r.getScores().get(-cids[col] - 1);
				return "0";
			}
		}
		/**
		 * Sorts the list by the given field.
		 * 
		 * @param index the field by which to sort
		 */
		protected void sortBy(int index) {
			if (index < 0 || index >= titles.length) return;
			if (sortingBy == index) ascending = !ascending;
			else {
				sortingBy = index;
				switch (cids[sortingBy]) {
				case 100:
				case 101:
				case 103:
				case 108:
				case 110:
				case 111:
					ascending = true;
					break;
				default:
					ascending = false;
				}
			}
			sortBy(index, ascending);
		}
		/**
		 * Sorts the list by the given field in the given direction.
		 * 
		 * @param index the field by which to sort
		 * @param dir whether the sort should be ascending
		 */
		protected void sortBy(int index, boolean dir) {
			heads.setSortDirection(index, dir);
			Collections.sort(data, new TeamComparator(index, dir));
			fireTableRowsUpdated(0, data.size());
			repaint();
		}
    }

	private int signum(double what) {
		if (what < 0.) return -1;
		if (what == 0.) return 0;
		return 1;
	}

	/**
	 * A comparator to sort team data objects by field.
	 */
	private class TeamComparator implements Comparator {
		/**
		 * The column by which to sort (PREPOSITION)
		 */
		private int columnToSort;
		/**
		 * Whether sorting should be ascending.
		 */
		private boolean isSortAsc;

		/**
		 * Creates a new team comparator to sort by the given field.
		 * 
		 * @param toSort the column to sort (NOT the CID)
		 * @param asc whether to go ascending
		 */
		public TeamComparator(int toSort, boolean asc) {
			columnToSort = toSort;
			isSortAsc = asc;
		}
		public int compare(Object o, Object t) {
			Team one = (Team)o;
			Team two = (Team)t;
			if (isSortAsc) return rawCompare(one, two);
			else return -rawCompare(one, two);
		}
		/**
		 * Does a raw, ascending comparison between the two teams.
		 * 
		 * @param one team #1
		 * @param two team #2
		 * @return -1 if one goes before two, 0 if equal, 1 if two goes before one
		 */
		private int rawCompare(Team one, Team two) {
			switch (cids[columnToSort]) {
			case 100:
				return one.getNumber() - two.getNumber();
			case 101:
				return one.getName().compareToIgnoreCase(two.getName());
			case 102:
				return one.getWins() - two.getWins();
			case 103:
				return one.getLosses() - two.getLosses();
			case 104:
				return one.getTies() - two.getTies();
			case 105:
				return signum(one.getWinPct() - two.getWinPct());
			case 106:
				return signum(one.getPPG() - two.getPPG());
			case 107:
				return signum(one.getTeamPPG() - two.getTeamPPG());
			case 108:
				return signum(one.getEnPPG() - two.getEnPPG());
			case 109:
				return signum(one.getRating() - two.getRating());
			case 110:
				return one.getType().compareTo(two.getType());
			case 111:
				return one.getFIRSTRank() - two.getFIRSTRank();
			case 112:
				return one.getSP() - two.getSP();
			case 113:
				return one.getRP() - two.getRP();
			default:
				HotkeyList hotkeys = status.getDataStore().getHotkeys();
				List udfs = status.getDataStore().getUDFs();
				if (cids[columnToSort] >= 0 && cids[columnToSort] < udfs.size())
					return ((Integer)one.getData().get(cids[columnToSort])).intValue() -
						((Integer)two.getData().get(cids[columnToSort])).intValue();
				else if (cids[columnToSort] < 0 && cids[columnToSort] > -2 - hotkeys.size()) {
					int index = -cids[columnToSort] - 1;
					if (index < one.getScores().size() && index < two.getScores().size())
						return ((Integer)one.getScores().get(index)).intValue() -
							((Integer)two.getScores().get(index)).intValue();
					else if (index < one.getScores().size())
						return ((Integer)one.getScores().get(index)).intValue();
					else if (index < two.getScores().size())
						return -((Integer)two.getScores().get(index)).intValue();
				}
				return 0;
			}
		}
	}

	/**
	 * Class to listen for a double click on the team name or a single
	 *  click on the column headers to sort.
	 */
	private class LocalEventListener extends MouseAdapter implements ActionListener {
		// For column indices.
		private TableColumnModel cols;
		
		public LocalEventListener() {
			cols = list.getColumnModel();
		}
		public void mouseClicked(MouseEvent e) {
			if (e.getSource().equals(head)) {
				// Sort by name.
				int index = cols.getColumnIndexAtX(e.getX());
				dataModel.sortBy(index);
			} else if (e.getClickCount() == 2) {
				// Double click in the list.
				int row = list.getSelectedRow();
				list.requestFocus();
				Team tm = (Team)data.get(row);
				if (tm != null) openComments(tm);
			}
		}
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd == null) return;
			else if (cmd.equals("quick")) try {
				// Fire comments viewer.
				Team tm = status.getBackend().get(Integer.parseInt(qe.getText()));
				if (tm != null) openComments(tm);
			} catch (Exception ex) { }
			else if (cmd.equals("search"))
				doSearch();
			else if (cmd.equals("addteam") && status.getUser().isAdmin()) {
				// add a team
				int teamNum = 0;
				try {
					teamNum = Integer.parseInt(JOptionPane.showInputDialog(status.getWindow(),
						"Enter the new team number:", "Add Team",
						JOptionPane.QUESTION_MESSAGE));
				} catch (Exception ex) { return; }
				String teamName = JOptionPane.showInputDialog(status.getWindow(),
					"Enter the new team name:", "Add Team",
					JOptionPane.QUESTION_MESSAGE);
				if (teamName == null || teamName.length() < 1) return;
				// danger danger!
				status.getClient().load();
				status.getBackend().addTeam(new Team(teamName, teamNum,
					status.getDataStore().getUDFs().size()));
			} else if (cmd.equals("delteam") && status.getUser().isAdmin()) {
				// remove a team
				int row = list.getSelectedRow();
				if (row < 0 || row >= data.size()) return;
				int num = ((Team)data.get(row)).getNumber();
				if (AppLib.confirm(status.getWindow(), "Really delete team " + num +
					"?\n\nWARNING: Removing a team can lead to serious issues!")) {
					// danger danger!
					status.getClient().load();
					status.getBackend().removeTeam(num);
				}
			} else if (cmd.equals("renteam") && status.getUser().isAdmin()) {
				// remove a team
				int row = list.getSelectedRow();
				if (row < 0 || row >= data.size()) return;
				Team team = (Team)data.get(row);
				String teamName = JOptionPane.showInputDialog(status.getWindow(),
					"Enter the new team name for " + team + ":", "Add Team",
					JOptionPane.QUESTION_MESSAGE);
				if (teamName == null || teamName.length() < 1) return;
				// danger danger!
				team.setName(teamName);
				status.getClient().load();
				status.getBackend().addTeam(team);
			}
		}
	}
}