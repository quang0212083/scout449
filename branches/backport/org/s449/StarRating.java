package org.s449;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * A class that displays a starred rating from 0 (no rating)
 *  or 1 (minimum) to 5 (maximum). This class is mutable
 *  and sharing or memoizing is encouraged.
 * 
 * @author Stephen Carlson
 * @version 4.0.0
 */
public class StarRating extends JComponent {
	private static final long serialVersionUID = 0L;

	/**
	 * The number of stars displayed.
	 */
	private double stars;
	/**
	 * The action listener to fire when the stars is changed.
	 *  If null, the object is immutable.
	 */
	private ActionListener change;
	/**
	 * The scout status object.
	 */
	private ScoutStatus status;

	/**
	 * Creates a new star rating object with 0 stars.
	 * 
	 * @param stat the ScoutStatus responsible for this object
	 */
	public StarRating(ScoutStatus stat) {
		super();
		status = stat;
		change = null;
		Dimension d = new Dimension(80, 16);
		addMouseListener(new StarListener());
		stars = 0;
		setMaximumSize(d);
		setMinimumSize(d);
		setPreferredSize(d);
	}
	/**
	 * Creates a new star rating object with the specified
	 *  number of stars.
	 * 
	 * @param stat the ScoutStatus responsible for this object
	 * @param initialRating the initial number of stars
	 */
	public StarRating(ScoutStatus stat, int initialRating) {
		this(stat);
		stars = initialRating;
	}
	/**
	 * Gets the number of stars displayed.
	 * 
	 * @return the number of stars
	 */
	public double getStars() {
		return stars;
	}
	/**
	 * Changes the number of stars displayed.
	 * 
	 * @param stars the new number of stars
	 */
	public void setStars(double stars) {
		this.stars = stars;
		repaint();
	}
	public void paint(Graphics g) {
		int i, st = (int)Math.floor(stars);
		double starP = stars - st;
		for (i = 0; i < st && i < 5; i++)
			paintImageAt(g, status.getImage("star-lit"), i);
		if (starP < .125 && i < 5) {
			paintImageAt(g, status.getImage("star-unlit"), i);
			i++;
		} else if (starP >= .125 && starP < .375 && i < 5) {
			paintImageAt(g, status.getImage("star-25"), i);
			i++;
		} else if (starP >= .375 && starP < .625 && i < 5) {
			paintImageAt(g, status.getImage("star-50"), i);
			i++;
		} else if (starP >= .625 && starP < .875 && i < 5) {
			paintImageAt(g, status.getImage("star-75"), i);
			i++;
		} else if (i < 5) {
			paintImageAt(g, status.getImage("star-lit"), i);
			i++;
		}
		for (; i < 5; i++)
			paintImageAt(g, status.getImage("star-unlit"), i);
	}
	/**
	 * Draws the specified image with the correct size at the specified
	 *  column on the given Graphics object.
	 */
	private void paintImageAt(Graphics g, Image im, int col) {
		g.drawImage(im, col * 16, 0, 16, 16, null);
	}
	/**
	 * Changes the action listener to be fired when the object is
	 *  changed. If null, the object will be immutable.
	 * 
	 * @param listener the new action listener
	 */
	public void setActionListener(ActionListener listener) {
		change = listener;
	}

	/**
	 * A class to listen for star clicks.
	 */
	private class StarListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			if (change != null) {
				// fire action listener
				setStars(e.getX() / 16 + 1);
				repaint();
				change.actionPerformed(new ActionEvent(e.getSource(),
					ActionEvent.ACTION_PERFORMED, "star"));
			}
		}
	}
}