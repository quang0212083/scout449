package org.s449;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * A class that represents a graphical match table.
 * 
 * @author Stephen Carlson
 * @version 1.0.0
 */
public class MatchList extends JTable {
	private static final long serialVersionUID = 0L;

	/**
	 * The titles of each column.
	 */
	private String[] titles;
	/**
	 * The widths of each column.
	 */
	private int[] widths;
	/**
	 * The column IDs for each column.
	 */
	private int[] cids;
	/**
	 * The match list.
	 */
	private ArrayList<ScheduleItem> matches;
	/**
	 * The match table model.
	 */
	private MatchTableModel model;
	/**
	 * The parent of this table.
	 */
	private ScoutStatus status;

	/**
	 * Creates a match table with an empty list.
	 * 
	 * @param stat the ScoutStatus responsible for this match table
	 */
	public MatchList(ScoutStatus stat) {
		status = stat;
		matches = new ArrayList<ScheduleItem>(10);
		model = new MatchTableModel();
		init();
		setModel(model);
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		// options
		JTableHeader head = getTableHeader();
		head.setResizingAllowed(true);
		head.setBackground(Constants.WHITE);
		head.setReorderingAllowed(false);
		head.setDefaultRenderer(new CenterRenderer(true));
		setDefaultRenderer(Object.class, new CenterRenderer(false));
		setRowSelectionAllowed(true);
		setColumnSelectionAllowed(false);
		addKeyListener(new MatchKiller());
		addKeyListener(new DelegateKeyListener(stat.getClient()));
		addMouseListener(new LocalEventListener());
		// widths
		TableColumnModel cols = getColumnModel();
		BoldRenderer br = new BoldRenderer();
		for (int i = 0; i < cids.length; i++) {
			cols.getColumn(i).setPreferredWidth(widths[i]);
			// renderers
			switch (cids[i]) {
			case 101:
			case 103:
			case 104:
				cols.getColumn(i).setCellRenderer(br);
				break;
			default:
				if (cids[i] > -2 * ScheduleItem.TPA - 2 && cids[i] < 2 * ScheduleItem.TPA)
					cols.getColumn(i).setCellRenderer(br);
			}
		}
	}
	/**
	 * Initializes.
	 */
	public void init() {
		int i = 3, j;
		// set CIDs according to advanced scoring
		boolean as = status.getDataStore().isAdvScore();
		if (as)
			cids = new int[5 + 4 * ScheduleItem.TPA];
		else
			cids = new int[5 + 2 * ScheduleItem.TPA];
		cids[0] = 100;
		cids[1] = 101;
		cids[2] = 102;
		for (j = 0; j < ScheduleItem.TPA; j++) {
			if (as) {
				cids[i + 2 * j] = j;
				cids[i + 2 * j + 1] = -j - 1;
			} else
				cids[i + j] = j;
		}
		if (as) i += j * 2;
		else i += j;
		cids[i] = 103;
		i++;
		for (j = 0; j < ScheduleItem.TPA; j++) {
			if (as) {
				cids[i + 2 * j] = j + ScheduleItem.TPA;
				cids[i + 2 * j + 1] = -j - ScheduleItem.TPA - 1;
			} else
				cids[i + j] = j + ScheduleItem.TPA;
		}
		if (as) i += j * 2;
		else i += j;
		cids[i] = 104;
		i++;
		// set titles and widths
		titles = new String[i];
		widths = new int[i];
		for (j = 0; j < i; j++) {
			switch (cids[j]) {
			case 100:
				widths[j] = 70;
				titles[j] = "Status";
				break;
			case 101:
				widths[j] = 147;
				titles[j] = "Time";
				break;
			case 102:
				widths[j] = 120;
				titles[j] = "Match";
				break;
			case 103:
				widths[j] = 55;
				titles[j] = "Red";
				break;
			case 104:
				widths[j] = 55;
				titles[j] = "Blue";
				break;
			default:
				if (cids[j] >= 0 && cids[j] < ScheduleItem.TPA) {
					widths[j] = 50;
					titles[j] = "Red " + (cids[j] + 1);
				} else if (cids[j] >= ScheduleItem.TPA && cids[j] < 2 * ScheduleItem.TPA) {
					widths[j] = 50;
					titles[j] = "Blue " + (cids[j] - ScheduleItem.TPA + 1);
				} else if (cids[j] < 0 && cids[j] > -ScheduleItem.TPA - 1) {
					widths[j] = 30;
					titles[j] = " ";
				} else if (cids[j] < -ScheduleItem.TPA && cids[j] > -2 * ScheduleItem.TPA - 2) {
					widths[j] = 30;
					titles[j] = " ";
				} else {
					// error!
					widths[j] = 0;
					titles[j] = "?";
				}
			}
		}
	}
	/**
	 * Empties the list.
	 */
	public void clearList() {
		matches.clear();
		model.fireTableDataChanged();
	}
	/**
	 * Sets the list to collection.
	 * 
	 * @param collection the new match list
	 */
	public void setList(Collection<ScheduleItem> collection) {
		int osize = matches.size();
		matches.clear();
		matches.addAll(collection);
		if (matches.size() != osize)
			model.fireTableDataChanged();
		else
			model.fireTableRowsUpdated(0, osize);
		repaint();
	}
	/**
	 * Removes the current match.
	 */
	private void removeCurrent() {
		int[] rows = getSelectedRows();
		if (rows.length < 1) return;
		String prompt = "Do you really want to delete this match?";
		if (rows.length > 1) prompt = "Do you really want to delete " + rows.length + " matches?";
		if (!AppLib.confirm(status.getWindow(), prompt))
			return;
		if (rows.length == 1) {
			// more efficient
			status.getClient().load();
			status.getBackend().delMatch(matches.get(rows[0]));
		} else {
			ArrayList<ScheduleItem> c = new ArrayList<ScheduleItem>(rows.length);
			for (int i = 0; i < rows.length; i++)
				c.add(matches.get(rows[i]));
			status.getClient().load();
			status.getBackend().delMatches(c);
		}
	}

	/**
	 * Class to manage bolding the right cells.
	 */
	private class BoldRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 0L;

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			Component renderer = super.getTableCellRendererComponent(table, value, isSelected,
				hasFocus, row, column);
			if (!(renderer instanceof JLabel)) return renderer;
			JLabel lbl = (JLabel)renderer;
			lbl.setOpaque(false);
			lbl.setForeground(Constants.BLACK);
			lbl.setBackground(Constants.WHITE);
			int cid = cids[column];
			if (cid == 101)
				// the time
				bold(lbl);
			else if (cid >= 0 && cid < 2 * ScheduleItem.TPA || cid == 103 || cid == 104) {
				// team - bold if win
				ScheduleItem match = matches.get(row);
				if (match.getStatus() == ScheduleItem.COMPLETE) {
					if (match.getRedScore() > match.getBlueScore() && (cid == 103 ||
							(cid < ScheduleItem.TPA && cid < 2 * ScheduleItem.TPA))) {
						lbl.setForeground(Constants.WHITE);
						lbl.setBackground(Constants.RED);
						lbl.setOpaque(true);
						bold(lbl);
					} else if (match.getRedScore() < match.getBlueScore() && (cid == 104 ||
							(cid >= ScheduleItem.TPA && cid < 2 * ScheduleItem.TPA))) {
						lbl.setForeground(Constants.WHITE);
						lbl.setBackground(Constants.BLUE);
						lbl.setOpaque(true);
						bold(lbl);
					}
				}
			}
			lbl.setHorizontalAlignment(SwingUtilities.CENTER);
			return lbl;
		}
		private void bold(JLabel lbl) {
			lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
		}
	}
	/**
	 * A class that manages the data in the table.
	 */
	private class MatchTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 0L;

		public int getColumnCount() {
			if (cids == null) return 0;
			return cids.length;
		}
		public String getColumnName(int columnIndex) {
			if (columnIndex < 0 || columnIndex >= titles.length) return null;
			return titles[columnIndex];
		}
		public int getRowCount() {
			return matches.size();
		}
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex < 0 || columnIndex >= titles.length ||
				rowIndex < 0 || rowIndex >= matches.size()) return null;
			ScheduleItem match = matches.get(rowIndex);
			java.util.List<Integer> teams = match.getTeams();
			java.util.List<Score> scores = match.getScores();
			int cid = cids[columnIndex];
			switch (cid) {
			case 100:
				if (match.getStatus() == ScheduleItem.COMPLETE)
					return "Completed";
				else
					return "Scheduled";
			case 101:
				long lt = match.getTime() + status.getDataStore().minutesLate() * 60000L;
				return ScheduleItem.timeFormat(lt) + " " +
					Constants.DATE_FORMAT.format(new Date(lt));
			case 102:
				if (match.getLabel() == null) return "Not Available";
				return match.getLabel() + " " + match.getNum();
			case 103:
				if (match.getStatus() == ScheduleItem.COMPLETE)
					return match.getRedScore();
				else
					return "";
			case 104:
				if (match.getStatus() == ScheduleItem.COMPLETE)
					return match.getBlueScore();
				else
					return "";
			default:
				if (cid >= 0 && cid < 2 * ScheduleItem.TPA)
					return teams.get(cid);
				if (cid < 0 && cid > -2 * ScheduleItem.TPA - 2) {
					if (scores != null && match.getStatus() == ScheduleItem.COMPLETE)
						return scores.get(-cid - 1);
					else
						return 0;
				}
			}
			return "";
		}
	}

	/**
	 * Deletes matches when delete is pressed.
	 */
	private class MatchKiller extends KeyAdapter {
		private static final long serialVersionUID = 0L;

		public void keyReleased(KeyEvent e) {
			int code = e.getKeyCode();
			if (code == KeyEvent.VK_DELETE)
				removeCurrent();
		}
	}

	/**
	 * Listens for double clicks.
	 */
	private class LocalEventListener extends MouseAdapter {
		private static final long serialVersionUID = 0L;

		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				// Double click in the list.
				int[] rows = getSelectedRows();
				if (rows.length == 1)
					status.getClient().editQueue(matches.get(rows[0]));
			}
		}
	}
}