package org.s449.ui;

import javax.swing.*;
import java.awt.*;

public class AntialiasedJLabel extends JLabel {
	private static final long serialVersionUID = 0L;
	public AntialiasedJLabel() {
		super();
	}
	public AntialiasedJLabel(String text) {
		super(text);
	}
	public void paint(Graphics g) {
		super.paint(g);
	}
	public void update(Graphics g) {
		paint(g);
	}
}