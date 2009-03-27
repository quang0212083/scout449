package org.s449;

import javax.swing.*;
import java.awt.Graphics;
import java.util.*;

/**
 * An analysis ticker to display stats and useful info.
 * 
 * @author Stephen Carlson
 * @version 3.0.0
 */
public class AnalysisTicker extends JLabel {
	private static final long serialVersionUID = 0L;

	/**
	 * The separator string.
	 */
	private static final String sep = "          ";
	/**
	 * The 5 team analysis list.
	 */
	private ArrayList<Team> analysis;
	/**
	 * The index position of the string.
	 */
	private int pos;
	/**
	 * The width of the string.
	 */
	private int width;
	/**
	 * The placebo team.
	 */
	private Team placebo;
	/**
	 * The watched team;
	 */
	private Team watchTeam;
	/**
	 * The schedule.
	 */
	private Event event;
	/**
	 * The scout status object.
	 */
	private ScoutStatus status;

	/**
	 * Creates a new analysis ticker on the given event.
	 * 
	 * @param stat the ScoutStatus responsible for this object
	 * @param evt the event
	 */
	public AnalysisTicker(ScoutStatus stat) {
		super();
		status = stat;
		width = 0;
		pos = -1;
		// load placebo team
		placebo = ScheduleItem.blankTeam;
		// init data
		watchTeam = null;
		analysis = new ArrayList<Team>(6);
	}
	/**
	 * This method should be called to scroll the ticker.
	 */
	public void tick() {
		pos -= 1;
		if (pos <= -width) {
			pos = getWidth();
			loadMoreData();
		}
		repaint();
	}
	public void paint(Graphics g) {
		g.setColor(getForeground());
		g.drawString(getText(), pos, getHeight() - 5);
	}
	public void update(Graphics g) {
		paint(g);
	}
	/**
	 * Loads more analysis data into the list.
	 */
	private void loadMoreData() {
		int i;
		double crit;
		Iterator<Team> it;
		Team team;
		StringBuilder out = new StringBuilder(1024);
		out.append("Scout449 Analysis Ticker");
		out.append(sep);
		// points/game
		analysis.clear();
		for (i = 0; i < 5; i++)
			analysis.add(placebo);
		if (status.getDataStore().isAdvScore()) {
			it = event.getTeams().values().iterator();
			out.append(sep);
			out.append("Top 5 Teams in Points/Game");
			out.append(sep);
			while (it.hasNext()) {
				team = it.next();
				crit = team.getPPG();
				for (i = 0; i < 5; i++)
					if (crit > analysis.get(i).getPPG() || analysis.get(i).equals(placebo)) {
						insertAt(team, i);
						break;
					}
			}
			loadAnalysis(out);
			analysis.clear();
			for (i = 0; i < 5; i++)
				analysis.add(placebo);
		}
		it = event.getTeams().values().iterator();
		out.append(sep);
		out.append("Top 5 User Ranked Teams");
		out.append(sep);
		while (it.hasNext()) {
			team = it.next();
			crit = team.getRating();
			for (i = 0; i < 5; i++)
				if (crit >= analysis.get(i).getRating() || analysis.get(i).equals(placebo)) {
					insertAt(team, i);
					break;
				}
		}
		loadAnalysis(out);
		analysis.clear();
		for (i = 0; i < 5; i++)
			analysis.add(placebo);
		out.append(sep);
		out.append("Top 5 FIRST Ranked Teams");
		out.append(sep);
		it = event.getTeams().values().iterator();
		while (it.hasNext()) {
			team = it.next();
			crit = team.getFIRSTRank();
			for (i = 0; i < 5; i++)
				if (crit <= analysis.get(i).getFIRSTRank() || analysis.get(i).equals(placebo)) {
					insertAt(team, i);
					break;
				}
		}
		loadAnalysis(out);
		if (watchTeam != null) {
			out.append(sep);
			out.append("Status of ");
			out.append(watchTeam.getNumber());
			out.append(" ");
			out.append(watchTeam.getName());
			out.append(": ");
			out.append(sep);
			out.append(watchTeam.getWins());
			out.append("-");
			out.append(watchTeam.getTies());
			out.append("-");
			out.append(watchTeam.getLosses());
			out.append(sep);
		}
		Map<SecondTime, ScheduleItem> schedule = event.getSchedule();
		// match scores
		long time = System.currentTimeMillis() / 60000L - Constants.PAST;
		List<Integer> teams = null;
		Iterator<Integer> it2 = null;
		Iterator<Score> sc = null;
		String s; ScheduleItem match;
		boolean first = true;
		// tick each match
		synchronized (schedule) {
			Iterator<ScheduleItem> it3 = schedule.values().iterator();
			while (it3.hasNext()) {
				match = it3.next();
				if ((match.getTime() + status.getDataStore().minutesLate()) / 60000L > time &&
						match.getStatus() == ScheduleItem.COMPLETE) {
					// header
					if (first) {
						out.append(sep);
						out.append("Match Results Last ");
						out.append(Constants.PAST);
						out.append(" Minutes");
						out.append(sep);
						first = false;
					}
					// display red and blue lists and scores
					teams = match.getTeams();
					out.append(match.getLabel());
					out.append(" ");
					out.append(match.getNum());
					out.append(": Red ");
					out.append(match.getRedScore());
					out.append(", Blue ");
					out.append(match.getBlueScore());
					if (status.getDataStore().isAdvScore()) {
						// only if advanced scoring enabled
						s = "";
						sc = match.getScores().iterator();
						it2 = teams.iterator();
						// handle both iterators now
						while (it2.hasNext() && sc.hasNext()) {
							team = event.get(it2.next());
							s += "[ " + team.getNumber() + ": " + sc.next() + " ]   ";
						}
						out.append("Scores: ");
						out.append(s.trim());
					}
					out.append(sep);
				}
			}
		}
		out.append(sep);
		setText(out.toString());
		width = SwingUtilities.computeStringWidth(getFontMetrics(getFont()), getText());
	}
	/**
	 * Copies the analysis list to the upcoming list.
	 */
	private void loadAnalysis(StringBuilder build) {
		for (int i = 0; i < analysis.size(); i++) {
			Team team = analysis.get(i);
			build.append(i + 1);
			build.append(": ");
			build.append(team.getNumber());
			build.append(" ");
			build.append(team.getName());
			build.append(" (");
			build.append(team.getWins());
			build.append("-");
			build.append(team.getTies());
			build.append("-");
			build.append(team.getLosses());
			build.append("), ");
			if (team.getRating() > 0) {
				build.append(team.getRating());
				build.append(" stars");
			} else
				build.append("unrated");
			build.append(sep);
		}
	}
	/**
	 * Inserts an element into the analysis list.
	 */
	private void insertAt(Team team, int index) {
		analysis.add(index, team);
		if (analysis.size() > 5) analysis.remove(5);
	}
	/**
	 * Gets the watched team.
	 * 
	 * @return the watched team
	 */
	public Team getWatchTeam() {
		return watchTeam;
	}
	/**
	 * Changes the watched team.
	 * 
	 * @param watchTeam the new team to watch
	 */
	public void setWatchTeam(Team watchTeam) {
		this.watchTeam = watchTeam;
	}
	/**
	 * Changes the event.
	 * 
	 * @param evt the new event
	 */
	public void setEvent(Event evt) {
		event = evt;
	}
}