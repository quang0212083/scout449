package org.s449;

import java.awt.event.*;
import java.awt.*;

/**
 * A class that delegates key events to another component.
 * 
 * @author Stephen Carlson
 * @version 4.0.0
 */
public class DelegateKeyListener extends KeyAdapter {
	/**
	 * The filter string. If null, all events are delegated.
	 */
	private String filter;
	/**
	 * The target component.
	 */
	private Component target;

	/**
	 * Creates a new delegate key listener that passes all events.
	 * 
	 * @param target the destination
	 */
	public DelegateKeyListener(Component target) {
		this.target = target;
		filter = null;
	}
	/**
	 * Creates a new delegate key listener that passes events with
	 *  a character in the filter string.
	 * 
	 * @param target the destination
	 * @param filter the filter string
	 */
	public DelegateKeyListener(Component target, String filter) {
		this.target = target;
		this.filter = filter;
	}
	public void keyTyped(KeyEvent e) {
		if (filter == null || filter.indexOf(e.getKeyChar()) >= 0) {
			KeyListener[] toFire = target.getKeyListeners();
			if (toFire != null)
				for (int i = 0; i < toFire.length; i++)
					toFire[i].keyTyped(e);
		}
	}
}