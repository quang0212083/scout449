package org.s449.ui;

import java.awt.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import org.s449.Constants;

public class SProgressBarUI extends BasicProgressBarUI {
	protected void installDefaults() {
		super.installDefaults();
		progressBar.setForeground(Constants.BLACK);
		progressBar.setBackground(Constants.WHITE);
	}
	protected Color getSelectionForeground() {
		return Constants.WHITE;
	}
	protected Color getSelectionBackground() {
		return Constants.BLACK;
	}
}