package org.s449;

import java.awt.*;
import javax.swing.*;

/**
 * A class that introduces the Scout449 program.
 * 
 * @author Stephen Carlson
 * @version 2.0.0
 */
public class Intro extends JWindow {
	private static final long serialVersionUID = 0L;

	/**
	 * The image to display.
	 */
	private static Image fileImage;
	/**
	 * Loads the image.
	 */
	public static void loadImage() {
		if (fileImage != null) return; // already loaded this
		AppLib.printDebug("Loading introduction image");
		ImageIcon fi = AppLib.loadClassImage("images/title.png");
		// wait for it...
		while (fi.getImageLoadStatus() == MediaTracker.LOADING) AppLib.sleep(20L);
		// load me!
		fileImage = fi.getImage();
		screenImage = screenShot(AppLib.winInfo.getScreenSize());
	}
	/**
	 * The background image.
	 */
	private static Image screenImage;
	/**
	 * The screen shot robot.
	 */
	private static Robot robot;
	/**
	 * Boom! Head shot!
	 * 
	 * @return the screen shot
	 */
	private static Image screenShot(Dimension ss) {
		try {
			if (robot == null) robot = new Robot();
			return robot.createScreenCapture(new Rectangle(0, 0, ss.width, ss.height));
		} catch (Exception e) {
			return null;
		}
	}

	private JFrame window;

	/**
	 * Introduce the Scout449 program!
	 * 
	 * @param stat the Scout449 responsible for this object
	 */
	public Intro(Scout449 stat, JFrame win) {
		super(win);
		window = win;
		win.setTitle("Loading Scout449");
		setVisible(false);
		win.setIconImage(stat.getImage("winicon"));
		loadImage();
		getRootPane().putClientProperty("Window.shadow", Boolean.FALSE);
		win.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		Container c = getContentPane();
		c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
		c.add(Box.createVerticalGlue());
		Loading load = new Loading();
		load.setOpaque(true);
		load.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		c.add(load);
		setCursor(Constants.WAIT);
		c.add(Box.createVerticalGlue());
		// center the window
		Dimension ss = AppLib.winInfo.getScreenSize();
		setBounds((ss.width - Constants.INTRO_WIDTH) / 2,
			(ss.height - Constants.INTRO_HEIGHT) / 2,
			Constants.INTRO_WIDTH, Constants.INTRO_HEIGHT);
		win.setBounds((ss.width - Constants.INTRO_WIDTH) / 2,
			(ss.height - Constants.INTRO_HEIGHT) / 2,
			Constants.INTRO_WIDTH, Constants.INTRO_HEIGHT);
		win.setVisible(true);
		setVisible(true);
	}
	public void paint(Graphics g) {
		super.paintComponents(g);
		if (screenImage != null)
			g.drawImage(screenImage, -getX(), -getY(), null);
		if (fileImage != null)
			g.drawImage(fileImage, 0, 0, null);
	}
	public void setVisible(boolean visible) {
		window.setVisible(visible);
		super.setVisible(visible);
	}
}