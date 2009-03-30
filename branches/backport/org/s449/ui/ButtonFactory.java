package org.s449.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicButtonUI;
import org.s449.Constants;

/**
 * A class that hands out button instances.
 * 
 * @author Stephen Carlson
 * @version 1.0.0
 */
public class ButtonFactory {
	/**
	 * The thin black border.
	 */
	public static Border thinBlack = BorderFactory.createLineBorder(Constants.BLACK);
	/**
	 * The thin gray border.
	 */
	public static Border thin = BorderFactory.createLineBorder(Constants.GRAY);
	/**
	 * The black border for buttons.
	 */
	public static Border blackBorder = BorderFactory.createCompoundBorder(thinBlack,
		BorderFactory.createEmptyBorder(1, 10, 1, 10));

	/**
	 * Gets a default button that has a black border and text.
	 * 
	 * @param text the text of the button
	 * @param action the action command
	 * @param evt the event listener
	 * @param mn the key to trigger this button
	 * @return the button
	 */
	public static JButton getButton(String text, String action, ActionListener evt,
			int mn) {
		JButton but = new AntialiasedJButton(text);
		but.setUI(new BasicButtonUI());
		but.setBorder(blackBorder);
		if (mn > 0) but.setMnemonic(mn);
		but.setBackground(Constants.LIGHT_BLUE_UI);
		but.setActionCommand(action);
		but.addActionListener(evt);
		return but;
	}

	/**
	 * Gets a default button with a black border and icon.
	 * 
	 * @param icon the icon of the button
	 * @param action the action command
	 * @param evt the event listener
	 * @param size the size
	 * @return the button
	 */
	public static JButton getButton(Icon icon, String action, ActionListener evt,
			Dimension size) {
		JButton but = new JButton(icon);
		but.setUI(new BasicButtonUI());
		but.setBackground(Constants.LIGHT_BLUE_UI);
		but.setBorder(blackBorder);
		but.setActionCommand(action);
		but.addActionListener(evt);
		if (size != null) {
			but.setPreferredSize(size);
			but.setMaximumSize(size);
		}
		return but;
	}
	/**
	 * Gets the thin border.
	 * 
	 * @return the thin border
	 */
	public static Border getThinBorder() {
		return thin;
	}
	/**
	 * Gets the button border.
	 * 
	 * @return the button border
	 */
	public static Border getButtonBorder() {
		return blackBorder;
	}
}