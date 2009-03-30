package org.s449;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.*;
import org.s449.ui.VerticalFlow;
import javax.swing.*;

/**
 * An editable list that supports add and remove objects.
 * 
 * @author Stephen Carlson
 * @version 1.0
 */
public class EditableList extends JPanel {
	private static final long serialVersionUID = 0L;

	/**
	 * The list.
	 */
	protected JList list;
	/**
	 * The table model.
	 */
	protected EditableListModel model;
	/**
	 * The action listeners.
	 */
	protected List listeners;
	/**
	 * The add button.
	 */
	protected JButton add;
	/**
	 * The remove button.
	 */
	protected JButton rem;

	/**
	 * Creates an editable list.
	 */
	public EditableList(ScoutStatus stat) {
		super(new BorderLayout(5, 5));
		this.model = new EditableListModel();
		LocalEventListener events = new LocalEventListener();
		list = new JList(model);
		list.setBackground(Constants.WHITE);
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.addKeyListener(events);
		list.setPrototypeCellValue("00000000000000000000000000000");
		listeners = new ArrayList(5);
		add(new JScrollPane(list), BorderLayout.CENTER);
		JComponent buttons = new JPanel(new VerticalFlow(false));
		add = new JButton();
		add.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		add.setIcon(stat.getIcon("plus"));
		add.setActionCommand("add");
		add.addActionListener(events);
		buttons.add(add);
		buttons.add(Box.createVerticalStrut(5));
		rem = new JButton();
		rem.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		rem.setIcon(stat.getIcon("minus"));
		rem.setActionCommand("remove");
		rem.addActionListener(events);
		buttons.add(rem);
		add(buttons, BorderLayout.EAST);
	}
	/**
	 * Adds an item to the table.
	 * 
	 * @param item the item to be added
	 */
	public void addItem(Object item) {
		model.addItem(item);
	}
	/**
	 * Removes an item from the table.
	 * 
	 * @param index the index of the item to be removed
	 * @throws IndexOutOfBoundsException if there is no such item
	 */
	public void removeItem(int index) {
		model.removeItem(index);
	}
	/**
	 * Clears the list.
	 */
	public void clear() {
		model.clear();
	}
	/**
	 * Sets the list of items.
	 * 
	 * @param list the new list
	 */
	public void setList(List list) {
		model.setList(list);
	}
	/**
	 * Gets the physical list.
	 * 
	 * @return the JList
	 */
	public JList getListObject() {
		return list;
	}
	/**
	 * Gets the list of values.
	 * 
	 * @return the list of values
	 */
	public List getList() {
		return model.getList();
	}
	/**
	 * Removes all of the selected items.
	 */
	public void removeCurrent() {
		int[] ind = list.getSelectedIndices();
		model.removeInterval(ind);
	}
	/**
	 * Adds an ActionListener to the list. It will be fired
	 *  on add and nothing else.
	 * 
     * @param l the listener to be added
	 */
	public void addActionListener(ActionListener l) {
		listeners.add(l);
	}
	/**
	 * Removes an ActionListener from the list.
	 *
	 * @param l the listener to be removed
	 */
	public void removeActionListener(ActionListener l) {
		listeners.remove(l);
	}
	/**
	 * Sets the action command to command.
	 * 
	 * @param command the new action command
	 */
	public void setActionCommand(String command) {
		add.setActionCommand(command);
	}
	protected void fireAddListener(ActionEvent e) {
		Iterator it = listeners.iterator();
		while (it.hasNext())
			((ActionListener)it.next()).actionPerformed(e);
	}

	/**
	 * Listens for the delete key and the buttons.
	 */
	private class LocalEventListener extends KeyAdapter implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd == null) return;
			else if (cmd.equals("remove"))
				removeCurrent();
			else if (e.getSource().equals(add))
				fireAddListener(e);
		}
		public void keyReleased(KeyEvent e) {
			int code = e.getKeyCode();
			if (code == KeyEvent.VK_DELETE && e.getSource().equals(list))
				removeCurrent();
		}
	}

	/**
	 * Represents an editable list of objects.
	 */
	protected class EditableListModel extends AbstractListModel {
		private static final long serialVersionUID = 0L;
		/**
		 * The object list to be edited.
		 */
		private List objects;

		/**
		 * Creates a new editable list model with a blank list.
		 */
		public EditableListModel() {
			objects = new ArrayList();
		}
		/**
		 * Changes the list of items. The list is copied.
		 * 
		 * @param newList the new list of items. May be null,
		 *  but this will disable adding and removing.
		 */
		public void setList(List newList) {
			if (newList == null) {
				objects = null;
				fireContentsChanged(0, 0);
			} else {
				objects = new ArrayList(newList);
				fireContentsChanged(0, objects.size());
			}
		}
		/**
		 * Gets the list of items.
		 * 
		 * @return the list of items
		 */
		public List getList() {
			return objects;
		}
		/**
		 * Clears the list.
		 */
		public void clear() {
			if (objects != null) {
				int oSize = objects.size();
				objects.clear();
				fireIntervalRemoved(0, oSize);
			}
		}
		/**
		 * Adds an item to the end of the list.
		 * 
		 * @param item the item to add
		 */
		public void addItem(Object item) {
			if (objects != null) {
				objects.add(item);
				fireIntervalAdded(objects.size() - 1);
			}
		}
		/**
		 * Removes an item from the list.
		 * 
		 * @param index the index to remove
		 */
		public void removeItem(int index) {
			if (objects != null) {
				objects.remove(index);
				fireIntervalRemoved(index, index);
			}
		}
		public Object getElementAt(int index) {
			if (objects == null) return null;
			return objects.get(index);
		}
		public int getSize() {
			if (objects == null) return 0;
			return objects.size();
		}
		public void removeInterval(int[] ind) {
			if (ind == null || ind.length < 1) return;
			// determine top and bottom
			int top = ind[ind.length - 1];
			int bot = ind[0];
			if (bot > -1 && bot < objects.size() && top > -1 &&
					top < objects.size() && top >= bot) {
				// remove the bottom index
				for (int index = bot; index <= top; index++)
					objects.remove(bot);
				fireIntervalRemoved(bot, top);
			}
		}
		protected void fireIntervalAdded(int ind) {
			fireIntervalAdded(this, ind, ind);
		}
		protected void fireIntervalRemoved(int lb, int ub) {
			fireIntervalRemoved(this, lb, ub);
		}
		protected void fireContentsChanged(int lb, int ub) {
			fireContentsChanged(this, lb, ub);
		}
	}
	public void setEnabled(boolean enabled) {
		list.setEnabled(enabled);
		rem.setEnabled(enabled);
		add.setEnabled(enabled);
		super.setEnabled(enabled);
	}
}