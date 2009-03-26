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
		if (g instanceof Graphics2D)
			((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		super.paint(g);
	}
	public void update(Graphics g) {
		paint(g);
	}
}