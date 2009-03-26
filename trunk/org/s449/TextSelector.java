package org.s449;

import javax.swing.*;
import java.awt.event.*;

/**
 * Class that, for convenience, selects the contents of a text box when it gets focus.
 * 
 * @author Stephen Carlson
 * @version 4.0.0
 */
public class TextSelector implements FocusListener {
	/**
	 * Only one instance of this class is ever needed, as the source is given focus.
	 */
	public static final TextSelector INSTANCE = new TextSelector();

	private TextSelector() { }
	public void focusGained(FocusEvent e) {
		if (e.getSource() != null && e.getSource() instanceof JTextField)
			((JTextField)e.getSource()).selectAll();
	}
	// I LOST
	public void focusLost(FocusEvent e) {
	}
}