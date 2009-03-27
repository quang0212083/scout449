package org.s449;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import org.s449.ui.*;

/**
 * Class to manage centering cells in a table.
 * 
 * @author Stephen Carlson
 * @version 2.0.0
 */
public class CenterRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 0L;

	/**
	 * Border?
	 */
	private boolean border;

	/**
	 * Creates a new center renderer with the given border.
	 * 
	 * @param bord whether there should be a border
	 */
	public CenterRenderer(boolean bord) {
		border = bord;
	}
	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		Component renderer = super.getTableCellRendererComponent(table, value, isSelected,
				hasFocus, row, column);
		if (!(renderer instanceof JLabel)) return renderer;
		JLabel lbl = (JLabel)renderer;
		if (border) lbl.setBorder(ButtonFactory.getThinBorder());
		else lbl.setBorder(null);
		lbl.setHorizontalAlignment(SwingUtilities.CENTER);
		return lbl;
	}
}