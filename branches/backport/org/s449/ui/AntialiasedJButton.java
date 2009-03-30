package org.s449.ui;

import javax.swing.*;
import java.awt.*;

public class AntialiasedJButton extends JButton {
	private static final long serialVersionUID = 0L;
	public AntialiasedJButton() {
		super();
	}
	public AntialiasedJButton(String text) {
		super(text);
	}
	public void paint(Graphics g) {
		super.paint(g);
	}
	public void update(Graphics g) {
		paint(g);
	}
}