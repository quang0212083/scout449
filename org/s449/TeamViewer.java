package org.s449;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import org.s449.ui.*;

/**
 * A class representing the team viewer window where users can
 *  view and edit information for a specific team.
 * 
 * @author Stephen Carlson
 * @version 4.0.0
 */
public class TeamViewer extends BasicDialog {
	/**
	 * The actual big image.
	 */
	private JLabel bigImage;
	/**
	 * The big image dialog.
	 */
	private JDialog bigImg;
	/**
	 * The team up to bat in the comments window.
	 */
	private Team team;
	/**
	 * The working comments list.
	 */
	private List comments;
	/**
	 * The team name.
	 */
	private JLabel teamName;
	/**
	 * The team's record (W-L-T).
	 */
	private JLabel record;
	/**
	 * Points per game.
	 */
	private JLabel ppg;
	/**
	 * The visual comments list.
	 */
	private JComponent commentsList;
	/**
	 * The team's match list.
	 */
	private MatchList matchList;
	/**
	 * The real match list.
	 */
	private JScrollPane matches;
	/**
	 * The star rating at the top.
	 */
	private StarRating stars;
	/**
	 * The robot type picker.
	 */
	private JComboBox rTypes;
	/**
	 * The UDF editor.
	 */
	private JTable udfEdit;
	/**
	 * The UDF table model.
	 */
	private UDFModel udfModel;
	/**
	 * The allowable robot types.
	 */
	private List robotTypes;
	/**
	 * The UDF field names.
	 */
	private List udfNames;
	/**
	 * The team's image.
	 */
	private JLabel image;
	/**
	 * The thumbnail fetcher.
	 */
	private ThumbnailThread thumbs;
	/**
	 * The cached thumbnails.
	 */
	private Map cache;
	/**
	 * The model for the robot types box.
	 */
	private TypeComboModel model;
	/**
	 * The comment currently editing.
	 */
	private Comment current;
	/**
	 * The visual comment currently editing.
	 */
	private VisualComment vCurrent;
	/**
	 * The list of text areas.
	 */
	private List vComments;
	/**
	 * The event listener.
	 */
	private LocalEventListener events;
	/**
	 * The visual scroll pane.
	 */
	private JScrollPane vSList;
	/**
	 * The window close listener.
	 */
	private EscapeKeyListener wl;

	/**
	 * Creates an initially-hidden team viewer with no team set.
	 * 
	 * @param status the ScoutStatus responsible for this team viewer
	 */
	public TeamViewer(ScoutStatus status) {
		super(status);
		thumbs = new ThumbnailThread();
		cache = new HashMap(100);
		robotTypes = new ArrayList(10);
		udfNames = new ArrayList(10);
		// initialize
		team = null;
		comments = new ArrayList(20);
		vComments = new ArrayList(20);
		events = new LocalEventListener();
		wl = new EscapeKeyListener(window);
		JPanel layout = new JPanel(new BorderLayout());
		AppLib.printDebug("Setting up comment editor");
		// set up window
		window.setTitle("Team Viewer");
		window.setResizable(true);
		window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		window.addWindowListener(events);
		Container c = window.getContentPane();
		c.setLayout(new BorderLayout());
		teamName = new JLabel();
		teamName.setFont(Client.titleFont);
		teamName.setHorizontalAlignment(SwingConstants.CENTER);
		teamName.setAlignmentY(JComponent.CENTER_ALIGNMENT);
		// label for team name
		c.add(teamName, BorderLayout.NORTH);
		JComponent vert = new JPanel();
		vert.setLayout(new BoxLayout(vert, BoxLayout.Y_AXIS));
		image = new JLabel();
		image.setHorizontalAlignment(SwingConstants.CENTER);
		image.setVerticalAlignment(SwingConstants.CENTER);
		image.setPreferredSize(new Dimension(160, 160));
		image.setMinimumSize(image.getPreferredSize());
		image.setMaximumSize(image.getPreferredSize());
		image.setAlignmentY(JComponent.TOP_ALIGNMENT);
		image.setBorder(ButtonFactory.getThinBorder());
		image.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		image.addMouseListener(events);
		vert.add(image);
		vert.add(Box.createVerticalGlue());
		JComponent horiz = new JPanel();
		horiz.setLayout(new BoxLayout(horiz, BoxLayout.X_AXIS));
		horiz.add(Box.createHorizontalStrut(10));
		horiz.add(vert);
		horiz.add(Box.createHorizontalStrut(10));
		vert = new JPanel(new VerticalFlow(false));
		JPanel h2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
		h2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		// team drop combo box
		model = new TypeComboModel();
		rTypes = new JComboBox(model);
		rTypes.setMaximumRowCount(8);
		rTypes.setActionCommand("type");
		rTypes.addActionListener(events);
		rTypes.addKeyListener(wl);
		rTypes.setPreferredSize(new Dimension(200, rTypes.getPreferredSize().height));
		// stars
		stars = new StarRating(status);
		h2.add(stars);
		h2.add(rTypes);
		vert.add(h2);
		// the stats labels
		record = new AntialiasedJLabel();
		record.setFont(Client.textFont);
		record.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		vert.add(record);
		ppg = new AntialiasedJLabel();
		ppg.setFont(Client.textFont);
		ppg.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		vert.add(ppg);
		// udf editor
		udfModel = new UDFModel(false);
		udfEdit = buildTable(udfModel);
		udfEdit.addKeyListener(wl);
		// set up the scroll pane and add
		JScrollPane vp = new JScrollPane(udfEdit);
		vp.setBorder(ButtonFactory.getThinBorder());
		vp.getViewport().setBackground(Constants.WHITE);
		vp.setPreferredSize(new Dimension(452, 92));
		vp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		vert.add(vp);
		vert.add(Box.createVerticalGlue());
		// add it up
		horiz.add(vert);
		horiz.add(Box.createHorizontalStrut(10));
		layout.add(horiz, BorderLayout.NORTH);
		// comments list
		commentsList = new JPanel(new VerticalFlow(true, 10, 10));
		matches = null;
		vSList = new JScrollPane(commentsList);
		vSList.setBorder(BorderFactory.createEmptyBorder());
		layout.add(vSList, BorderLayout.CENTER);
		c.add(layout, BorderLayout.CENTER);
		// close button
		JButton close = new JButton("Close");
		close.setActionCommand("close");
		close.addActionListener(events);
		close.setMnemonic(KeyEvent.VK_C);
		// add/edit button
		JButton addedit = new JButton("Add/Edit");
		addedit.setActionCommand("edit");
		addedit.addActionListener(events);
		addedit.setMnemonic(KeyEvent.VK_E);
		// add it up!
		horiz = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
		horiz.add(addedit);
		horiz.add(close);
		c.add(horiz, BorderLayout.SOUTH);
		// finish up! whew!
		window.setSize(800, 600);
		centerWindow();
		EscapeKeyListener el = new EscapeKeyListener(bigImg);
		// big image window
		bigImage = new JLabel();
		bigImage.setHorizontalAlignment(SwingConstants.CENTER);
		bigImage.setVerticalAlignment(SwingConstants.CENTER);
		bigImage.setPreferredSize(Client.imageSize);
		bigImage.addKeyListener(el);
		bigImg = new JDialog(window, "Image");
		bigImg.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		bigImg.addKeyListener(el);
		c = bigImg.getContentPane();
		c.setLayout(new BorderLayout());
		JScrollPane pane = new JScrollPane(bigImage);
		pane.setBorder(BorderFactory.createEmptyBorder());
		c.add(pane, BorderLayout.CENTER);
		bigImg.pack();
		int w = bigImg.getWidth();
		int h = bigImg.getHeight();
		bigImg.setBounds((screenWidth - w) / 2, (screenHeight - h) / 2, w, h);
		bigImg.setModal(false);
		bigImg.setVisible(false);
		current = null;
		vCurrent = null;
		window.addKeyListener(wl);
	}
	/**
	 * Creates a new table for UDFs.
	 * 
	 * @param model the UDF model
	 * @return a UDF table
	 */
	private JTable buildTable(UDFModel model) {
		JTable table = new JTable(model);
		// set options
		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(false);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setBackground(Constants.WHITE);
		TableColumnModel cols = table.getColumnModel();
		cols.getColumn(0).setPreferredWidth(300);
		cols.getColumn(1).setPreferredWidth(100);
		return table;
	}
	/**
	 * Creates the comments list.
	 */
	private void buildComments() {
		commentsList.removeAll();
		commentsList.add(matches);
		vComments.clear();
		if (comments.size() > 0) {
			Iterator it = comments.iterator();
			int index = 0;
			Comment c; Container pane; JLabel lbl; JButton edit;
			JTextArea area; StarRating star; JTable table; UDFModel mode;
			JScrollPane vp; Font f; JComponent pnl;
			while (it.hasNext()) {
				c = (Comment)it.next();
				// expandable panel with owner, stars, and team num as title
				ExpandablePanel ex = new ExpandablePanel(status);
				pane = ex.getTitlePane();
				pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
				lbl = new JLabel(c.getOwner().getRealName());
				lbl.setForeground(Constants.DARK_BLUE);
				pane.add(lbl);
				pane.add(Box.createHorizontalGlue());
				edit = null;
				if (isEditable(c)) {
					// edit button
					edit = new JButton("Edit");
					edit.addActionListener(new EditListener(index));
					edit.setActionCommand("edit");
					edit.setOpaque(false);
					f = edit.getFont();
					edit.setFont(f.deriveFont(f.getSize2D() - 2.f));
					edit.setMaximumSize(edit.getPreferredSize());
					pane.add(edit);
					pane.add(Box.createHorizontalStrut(15));
				}
				if (!c.getOwner().equals(status.getUser()))
					ex.setOpen(false);
				if (c.getMatch() != null) {
					lbl = new JLabel(c.getMatch().toString());
					lbl.setBackground(Constants.LIGHT_GREEN);
					lbl.setOpaque(true);
					lbl.setForeground(Constants.DARK_BLUE);
					pane.add(lbl);
					pane.add(Box.createHorizontalStrut(20));
				}
				lbl = new JLabel(ScheduleItem.timeFormat(c.getWhen()) + ", " +
					ScheduleItem.dateFormat(c.getWhen()));
				lbl.setForeground(Constants.DARK_BLUE);
				pane.add(lbl);
				pane.add(Box.createHorizontalStrut(15));
				lbl = new JLabel(Integer.toString(c.getOwner().getTeamNum()));
				lbl.setForeground(Constants.RED);
				pane.add(lbl);
				// text area and UDF
				area = new JTextArea(5, 40);
				area.setFont(lbl.getFont());
				area.setBorder(ButtonFactory.getThinBorder());
				area.setEditable(false);
				area.setWrapStyleWord(true);
				area.setText(c.getText());
				area.addKeyListener(wl);
				area.setAlignmentX(JComponent.LEFT_ALIGNMENT);
				mode = new UDFModel(false, c.getUDFs());
				table = buildTable(mode);
				table.addKeyListener(wl);
				vp = new JScrollPane(table);
				vp.setBorder(ButtonFactory.getThinBorder());
				vp.getViewport().setBackground(Constants.WHITE);
				vp.setPreferredSize(new Dimension(450, 90));
				vp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
				// set it all up
				pane = ex.getContentPane();
				pane.setLayout(new VerticalFlow(false));
				star = new StarRating(status, c.getRating());
				star.addKeyListener(wl);
				pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
				pnl.setOpaque(false);
				pnl.add(new JLabel("Your ranking: "));
				pnl.add(star);
				pane.add(pnl);
				pane.add(Box.createVerticalStrut(10));
				pane.add(area);
				pane.add(Box.createVerticalStrut(10));
				pane.add(vp);
				vComments.add(new VisualComment(star, area, table, mode, edit, ex));
				commentsList.add(ex);
				index++;
			}
		}
		commentsList.validate();
		commentsList.repaint();
	}
	/**
	 * Initializes the team viewer.
	 */
	public void init() {
		robotTypes.clear();
		robotTypes.addAll(status.getDataStore().getTypes());
		udfNames.clear();
		udfNames.addAll(status.getDataStore().getUDFs());
		model.reset();
		matchList = new MatchList(status);
		matches = new JScrollPane(matchList);
		matches.setBorder(ButtonFactory.getThinBorder());
		matches.setPreferredSize(new Dimension(730, 100));
	}
	/**
	 * Closes down the team viewer.
	 */
	public void shutdown() {
		thumbs.stopThread();
	}
	/**
	 * Gets the team being edited.
	 * 
	 * @return the team being edited
	 */
	public Team getTeam() {
		return team;
	}
	/**
	 * Changes the team to be edited.
	 * 
	 * @param team the team to edit
	 */
	public void setTeam(Team team) {
		this.team = team;
		String text;
		if (team != null) {
			// update team name and comments list
			teamName.setText(team.getNumber() + ": " + team.getName());
			comments.clear();
			comments.addAll(team.getComments());
			// update record, points/game, etc.
			text = "Record: " + team.getWins() + "-" + team.getLosses() + "-" +
				team.getTies() + " (" + team.getWinPct() + "%)   SP: " + team.getSP() + "   RP: " +
				team.getRP();
			if (team.getFIRSTRank() > 0)
				text += "   FIRST Rank: " + team.getFIRSTRank();
			record.setText(text);
			text = "Alliance Points/Game: " + team.getTeamPPG();
			if (status.getDataStore().isAdvScore())
				text += "   Points/Game: " + team.getPPG();
			ppg.setText(text);
			// update stars
			stars.setStars(team.getRating());
			// change robot type. sick hack.
			rTypes.setActionCommand("notyet");
			rTypes.setSelectedItem(team.getType());
			rTypes.setActionCommand("type");
			thumbs.loadImage(team.getNumber());
			udfModel.setData(team.getData());
			bigImg.setTitle("Image for " + team.getNumber() + " " + team.getName());
			matchList.setList(team.getMatches().values());
			// set big image
			try {
				bigImage.setIcon(new ImageIcon(new URL("http://" + status.getRemoteHost() + ":" +
					status.getClient().getWebPort() + "/image?team=" + team.getNumber() + "")));
			} catch (Exception e) { }
		} else {
			matchList.clearList();
			bigImg.setTitle("Image");
			bigImage.setIcon(null);
			// empty team
			teamName.setText("No team selected");
			comments.clear();
			record.setText("");
			ppg.setText("");
			stars.setStars(0);
			image.setIcon(null);
			// change robot type. sick hack.
			rTypes.setActionCommand("notyet");
			rTypes.setSelectedItem("Other");
			rTypes.setActionCommand("type");
			udfModel.setData(null);
		}
		current = null;
		vCurrent = null;
		buildComments();
		udfModel.fireTableDataChanged();
	}
	/**
	 * Stops editing and saves.
	 */
	private void stopEditing() {
		boolean rebuild = false;
		if (current != null && team != null) {
			AppLib.printDebug("Stopping editing");
			current.setText(vCurrent.text.getText());
			current.setWhen(status.getClient().getTime());
			current.setRating((int)Math.round(vCurrent.stars.getStars()));
			if ((current.getText() != null && current.getText().trim().length() < 1 &&
				AppLib.confirm(window, "Do you want to delete this (now empty) comment?"))) {
				current.setText(null);
				rebuild = true;
			}
			vCurrent.edit.setText("Edit");
			vCurrent.text.setEditable(false);
			if (vCurrent.udfs.isEditing() && vCurrent.udfs.getEditingColumn() == 1) {
				TableCellEditor cellEditor = vCurrent.udfs.getColumnModel().getColumn(1)
					.getCellEditor();
				if (cellEditor == null)
					cellEditor = vCurrent.udfs.getDefaultEditor(vCurrent.udfs.getColumnClass(1));
				if (cellEditor != null) cellEditor.stopCellEditing();
			}
			vCurrent.model.setEditable(false);
			vCurrent.panel.requestFocus();
			vCurrent.stars.setActionListener(null);
			status.getClient().load();
			status.getBackend().updateComment(team.getNumber(), current);
			current = null;
			vCurrent = null;
			team = status.getBackend().get(team.getNumber());
			stars.setStars(team.getRating());
			udfModel.setData(team.getData());
			udfModel.fireTableDataChanged();
			if (rebuild) {
				comments.clear();
				comments.addAll(team.getComments());
				buildComments();
			}
		}
	}
	/**
	 * Starts editing at the given comment index.
	 * 
	 * @param index the comment index to start editing
	 */
	private void startEditing(int index) {
		if (team == null || index < 0 || index >= comments.size()) return;
		// check if it's yours or admin
		Comment cc = (Comment)comments.get(index);
		if (isEditable(cc)) {
			AppLib.printDebug("Starting editing@" + index);
			// ok
			if (current != null) stopEditing();
			current = cc;
			VisualComment vis = (VisualComment)vComments.get(index);
			vis.panel.setOpen(true);
			AppLib.sleep(1L);
			vis.text.setEditable(true);
			vis.model.setEditable(true);
			vis.edit.setText("Save");
			vis.stars.setActionListener(events);
			vCurrent = vis;
			vis.panel.scrollRectToVisible(vis.panel.getBounds());
			vis.text.requestFocus();
		} // else bad
	}
	/**
	 * Is the comment editable by this user?
	 * 
	 * @param cc the comment to check
	 */
	private boolean isEditable(Comment cc) {
		UserData cur = status.getUser();
		return cur.canWrite() && (cur.equals(cc.getOwner()) || cur.isAdmin());
	}
	/**
	 * Saves the changes and closes.
	 */
	private void close() {
		stopEditing();
		bigImg.setVisible(false);
		window.setVisible(false);
		team = null;
	}
	/**
	 * Closes the team viewer without saving. Clears the current team
	 *  to null.
	 */
	public void closeNoSave() {
		team = null;
		bigImg.setVisible(false);
		window.setVisible(false);
	}
	/**
	 * Resets the team viewer for the next run by saving old stuff.
	 */
	public void reset() {
		if (team != null) close();
	}
	/**
	 * Gets the UDF field names.
	 * 
	 * @return the UDF field names as a List
	 */
	public List getUDFNames() {
		return udfNames;
	}
	/**
	 * Reloads the team thumbnail image.
	 */
	private void reImage() {
		if (image != null) image.setIcon((ImageIcon)cache.get(new Integer(team.getNumber())));
	}
	/**
	 * Shows the big image.
	 */
	private void showBig() {
		bigImg.setVisible(true);
	}

	/**
	 * A thread to fetch thumbnails.
	 */
	private class ThumbnailThread extends Thread {
		private static final long serialVersionUID = 0L;
		/**
		 * The synchronization team number.
		 */
		private int teamNum;

		public ThumbnailThread() {
			setName("Thumbnail Fetcher");
			setPriority(Thread.MIN_PRIORITY + 1);
			setDaemon(true);
			teamNum = 0;
			start();
		}
		public void run() {
			ImageIcon icon;
			long lastClear = System.currentTimeMillis(), time;
			while (teamNum >= 0) {
				synchronized (this) {
					if (teamNum > 0) try {
						if (!cache.containsKey(new Integer(teamNum))) {
							icon = new ImageIcon(new URL("http://" + status.getRemoteHost() + ":" +
								status.getClient().getWebPort() + "/thumbnail?team=" + teamNum));
							if (icon.getImage() != null) cache.put(new Integer(teamNum), icon);
						}
						reImage();
					} catch (Exception e) {
						AppLib.debugException(e);
					}
					teamNum = 0;
				}
				AppLib.sleep(50L);
				time = System.currentTimeMillis();
				if (time >= lastClear + 1000 * Constants.TTL_SECONDS) {
					lastClear = time;
					synchronized (this) {
						cache.clear();
					}
				}
			}
		}
		public synchronized void stopThread() {
			teamNum = -1;
		}
		public synchronized void loadImage(int teamNum) {
			this.teamNum = teamNum;
		}
	}

	/**
	 * A class for editing a specified comment.
	 */
	private class EditListener implements ActionListener {
		/**
		 * The index to edit.
		 */
		private int index;

		/**
		 * Creates an edit listener on the given index.
		 * 
		 * @param newIndex the index to edit
		 */
		public EditListener(int newIndex) {
			index = newIndex;
		}
		public void actionPerformed(ActionEvent e) {
			if (current == null) startEditing(index);
			else {
				if (!vCurrent.edit.getText().equalsIgnoreCase("save")) {
					stopEditing();
					startEditing(index);
				} else stopEditing();
			}
		}
	}
	/**
	 * A class for handling local events.
	 */
	private class LocalEventListener extends WindowAdapter implements ActionListener, MouseListener {
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			// Check for close of the window.
			if (cmd == null) return;
			else if (cmd.equals("close")) close();
			else if (cmd.equals("type") && team != null
					&& status.getUser().canWrite()) {
				// change robot type
				String text = (String)rTypes.getSelectedItem();
				if (!team.getType().equals(text)) {
					team.setType(text);
					status.getClient().load();
					status.getBackend().setType(team.getNumber(), text);
				}
			} else if (cmd.equals("edit") && current == null && status.getUser().canWrite()) {
				// add/edit. if user has a comment, go to it. else, add.
				AppLib.printDebug("Add/edit comment");
				int ind = -1;
				for (int i = 0; i < comments.size(); i++)
					if (((Comment)comments.get(i)).getOwner().equals(status.getUser())) {
						ind = i;
						break;
					}
				if (ind < 0) {
					// add
					List list = new ArrayList(udfNames.size());
					for (int i = 0; i < udfNames.size(); i++)
						list.add(new Integer(0));
					comments.add(new Comment(status.getUser(), null, "", 0, list,
						status.getClient().getTime()));
					buildComments();
					ind = comments.size() - 1;
				}
				startEditing(ind);
			}
		}
		public void windowClosing(WindowEvent e) {
			close();
		}
		public void mouseClicked(MouseEvent e) {
			// Click in the label.
			showBig();
		}
		public void mouseEntered(MouseEvent e) { }
		public void mouseExited(MouseEvent e) { }
		public void mousePressed(MouseEvent e) { }
		public void mouseReleased(MouseEvent e) { }
	}

	/**
	 * Table model for UDFs.
	 */
	private class UDFModel extends AbstractTableModel {
		private static final long serialVersionUID = 0L;

		/**
		 * Editing allowed?
		 */
		private boolean allowEditing;
		/**
		 * The data list to use.
		 */
		private List myData;

		/**
		 * Creates a new UDF model.
		 * 
		 * @param edit whether editing is allowed
		 */
		public UDFModel(boolean edit) {
			allowEditing = edit;
			myData = null;
		}
		/**
		 * Creates a new UDF model.
		 * 
		 * @param edit whether editing is allowed
		 * @param myData the UDF data to read
		 */
		public UDFModel(boolean edit, List myData) {
			this(edit);
			this.myData = myData;
		}
		/**
		 * Sets the data in the UDF model.
		 * 
		 * @param myData the new data to display
		 */
		public void setData(List myData) {
			this.myData = myData;
		}
		/**
		 * Sets whether the UDF model is editable.
		 * 
		 * @param edit whether the table is editable
		 */
		public void setEditable(boolean edit) {
			allowEditing = edit;
		}
		public int getColumnCount() {
			return 2;
		}
		public Object getValueAt(int row, int col) {
			if (myData == null || row < 0 || row >= myData.size())
				return "";
			if (col == 0)
				return udfNames.get(row);
			else if (col == 1) {
				// based on type
				int val = ((Integer)myData.get(row)).intValue();
				int type = ((UDF)udfNames.get(row)).getType();
				if (val == 0 && type == UDF.BOOL)
					return "No";
				else if (val == 1 && type == UDF.BOOL)
					return "Yes";
				else return Integer.toString(val);
			}
			return "";
		}
		public int getRowCount() {
			if (myData != null)
				return myData.size();
			return 0;
		}
		public String getColumnName(int col) {
			if (col == 0) return "Field Name";
			else return "Value";
		}
		public boolean isCellEditable(int x, int y) {
			return y == 1 && allowEditing;
		}
		public void setValueAt(Object obj, int row, int col) {
			if (myData == null || !allowEditing || col != 1 || row < 0 || row >= myData.size())
				return;
			try {
				int x = Integer.parseInt(obj.toString());
				int type = ((UDF)udfNames.get(row)).getType();
				if (type == UDF.RATE10 && (x < 0 || x > 10)) {
					AppLib.printWarn(window, "This field must be a rating from 0 to 10.");
					return;
				} else if (type == UDF.BOOL && (x < 0 || x > 1)) {
					AppLib.printWarn(window, "This field must be either 0 or 1.");
					return;
				}
				myData.set(row, new Integer(x));
			} catch (Exception e) {
				AppLib.printWarn(window, "This field must be an integer.");
				return;
			}
		}
	}

	/**
	 * A class to handle robot types.
	 */
	private class TypeComboModel extends AbstractListModel implements ComboBoxModel {
		private static final long serialVersionUID = 0L;

		/**
		 * The selected robot type.
		 */
		private Object selectedObject;

		public void reset() {
			int index = robotTypes.indexOf("Other");
			if (index < 0) index = 0;
			selectedObject = robotTypes.get(index);
		}
		public void setSelectedItem(Object anObject) {
			if ((selectedObject != null && !selectedObject.equals(anObject)) ||
					(selectedObject == null && anObject != null)) {
				selectedObject = anObject;
				fireContentsChanged(this, -1, -1);
			}
		}
		public Object getSelectedItem() {
			return selectedObject;
		}
		public int getSize() {
			return robotTypes.size();
		}
		public Object getElementAt(int index) {
			if (index >= 0 && index < robotTypes.size())
				return robotTypes.get(index);
			else
				return null;
		}
	}

	/**
	 * A class representing a comment on the screen.
	 */
	private class VisualComment {
		protected ExpandablePanel panel;
		protected JTextArea text;
		protected StarRating stars;
		protected JTable udfs;
		protected UDFModel model;
		protected JButton edit;

		public VisualComment(StarRating stars, JTextArea text, JTable udfs, UDFModel model,
				JButton edit, ExpandablePanel panel) {
			this.stars = stars;
			this.text = text;
			this.udfs = udfs;
			this.model = model;
			this.edit = edit;
			this.panel = panel;
		}
	}
}