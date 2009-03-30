package org.s449.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.s449.*;

/**
 * A panel that can be opened and closed.
 * 
 * @author Stephen Carlson
 * @version 4.0.0
 */
public class ExpandablePanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 0L;

	/**
	 * The scouting status object.
	 */
	private ScoutStatus status;
	/**
	 * Open?
	 */
	private boolean open;
	/**
	 * The title panel.
	 */
	private JComponent title;
	/**
	 * The bottom (expand/contract) panel.
	 */
	private JComponent content;
	/**
	 * The plus/minus image.
	 */
	private JButton plusMinus;

	/**
	 * Creates a new expandable panel, default open.
	 * 
	 * @param stat the ScoutStatus responsible for this object
	 */
	public ExpandablePanel(ScoutStatus stat) {
		super(new VerticalFlow(true));
		open = true;
		status = stat;
		setBackground(Constants.WHITE);
		JComponent horiz = new JPanel();
		horiz.setLayout(new BoxLayout(horiz, BoxLayout.X_AXIS));
		horiz.setBackground(Constants.WHITE);
		title = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		title.setOpaque(false);
		content = new JPanel(new BorderLayout());
		content.setOpaque(false);
		title.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		plusMinus = new JButton();
		plusMinus.setActionCommand("oc");
		plusMinus.addActionListener(this);
		plusMinus.setBorder(BorderFactory.createEmptyBorder());
		plusMinus.setOpaque(false);
		plusMinus.setContentAreaFilled(false);
		plusMinus.setPreferredSize(new Dimension(15, 15));
		plusMinus.setAlignmentY(JComponent.CENTER_ALIGNMENT);
		plusMinus.setMinimumSize(plusMinus.getPreferredSize());
		plusMinus.setMaximumSize(plusMinus.getPreferredSize());
		horiz.add(plusMinus);
		horiz.add(title);
		add(horiz);
		Box h2 = new Box(BoxLayout.X_AXIS);
		content.setBorder(BorderFactory.createCompoundBorder(ButtonFactory.getThinBorder(),
			BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		h2.add(content);
		add(h2);
		update();
		validate();
	}
	/**
	 * Checks if the expandable panel is open.
	 *
	 * @return whether the panel is open
	 */
	public boolean isOpen() {
		return open;
	}
	/**
	 * Opens or closes the expandable panel.
	 *
	 * @param open whether the panel should be open or closed
	 */
	public void setOpen(boolean open) {
		this.open = open;
		update();
	}
	/**
	 * Gets the title of this expandable panel.
	 *
	 * @return the title panel
	 */
	public Container getTitlePane() {
		return title;
	}
	/**
	 * Gets the content of this expandable panel.
	 *
	 * @return the content panel
	 */
	public Container getContentPane() {
		return content;
	}
	/**
	 * Updates the screen to match the flag.
	 */
	private void update() {
		content.setVisible(open);
		if (open)
			plusMinus.setIcon(status.getIcon("contract"));
		else
			plusMinus.setIcon(status.getIcon("expand"));
	}
	public void actionPerformed(ActionEvent e) {
		open = !open;
		update();
	}
}