package org.s449;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Class to listen for presses of the escape key.
 * 
 * @author Stephen Carlson
 * @version 1.0.0
 */
public class EscapeKeyListener extends KeyAdapter {
	/**
	 * The component to hide when the escape key is pressed.
	 */
	private Component entry;

	/**
	 * Creates a new escape key listener that will hide the given
	 *  component.
	 * 
	 * @param ent the component to be hidden on [Esc]
	 */
	public EscapeKeyListener(Component ent) {
		entry = ent;
	}
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			entry.setVisible(false);
	}
}