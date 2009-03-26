package org.s449;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import org.s449.ui.*;

/**
 * Class to manage centering and visually sorting header cells in a table.
 * 
 * @author Stephen Carlson
 * @version 4.0.0
 */
public class HeaderRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 0L;

	/**
	 * Direction to sort in.
	 */
	private boolean sortDirection;
	/**
	 * Direction to sort in.
	 */
	private int column;
	/**
	 * The ScoutStatus object (for icon-based headers only!)
	 */
	private ScoutStatus status;

	/**
	 * Creates a new center renderer with the given sort direction.
	 * 
	 * @param status the ScoutStatus responsible for this object
	 */
	public HeaderRenderer(ScoutStatus stat) {
		sortDirection = true; // ascending
		column = 0;
		status = stat;
	}
	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		Component renderer = super.getTableCellRendererComponent(table, value, isSelected,
				hasFocus, row, column);
		if (!(renderer instanceof JLabel)) return renderer;
		JLabel lbl = (JLabel)renderer;
		lbl.setBorder(ButtonFactory.getThinBorder());
		lbl.setHorizontalAlignment(SwingUtilities.CENTER);
		lbl.setHorizontalTextPosition(SwingConstants.LEFT);
		if (column == this.column) {
		if (sortDirection)
			lbl.setIcon(status.getIcon("up"));
		else
			lbl.setIcon(status.getIcon("down"));
		} else
			lbl.setIcon(null);
		return lbl;
	}
	/**
	 * Changes the sorting direction.
	 * 
	 * @param col the column to sort
	 * @param sortDir the new sorting direction code
	 */
	public void setSortDirection(int col, boolean sortDir) {
		sortDirection = sortDir;
		column = col;
	}
}