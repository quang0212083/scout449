package org.s449;

import java.io.*;
import java.net.*;
import java.util.*;
import org.s449.HotkeyList.Hotkey;
import org.s449.Server.ImageDaemon;

public class WebInterface implements ServerPage {
	/**
	 * Your team is... 449 Blair Robotics Project!
	 */
	private int myTeam = Constants.DEFAULT_TEAM;
	/**
	 * The data.
	 */
	private Backend data;
	/**
	 * Helper for web methods to escape &, <, and > characters.
	 * 
	 * @param toEscape the string to escape
	 * @return the answer
	 */
	private static String htmlspecial(String toEscape) {
		StringBuffer nt = new StringBuffer(toEscape.length() + 32);
		char c;
		for (int i = 0; i < toEscape.length(); i++) {
			c = toEscape.charAt(i);
			switch (c) {
			case '&':
				nt.append("&amp;");
				break;
			case '<':
				nt.append("&lt;");
				break;
			case '>':
				nt.append("&gt;");
				break;
			default:
				nt.append(c);
			}
		}
		return nt.toString();
	}
	/**
	 * Helper for web methods to escape "'" to "\'".
	 * 
	 * @param in the input string
	 * @param out the output string
	 */
	private static String escapeQuotes(String in) {
		StringBuffer nt = new StringBuffer(in.length() + 32);
		char c;
		for (int i = 0; i < in.length(); i++) {
			c = in.charAt(i);
			switch (c) {
			case '\'':
				nt.append("\\\'");
				break;
			default:
				nt.append(c);
			}
		}
		return nt.toString();
	}

	public void action(String url, String ip, String inData, PrintWriter out, OutputStream os) {
		try {
			if (url.length() < 1 || url.equals("home") || url.equals("status"))
				sendStartPage(out);
			else if (url.equals("error"))
				handleHTTPError(out, inData);
			else if (url.equals("list"))
				sendTeamList(out);
			else if (url.equals("scoring"))
				sendMatchScorer(out);
			else if (url.equals("matches") || url.equals("internal-matches")
					|| url.equals("matches?refresh"))
				sendMatchList(out, url);
			else if (url.equals("lookup"))
				sendLookup(out);
			else if (url.startsWith("thumbnail?"))
				sendThumbnail(url, out, os);
			else if (url.startsWith("report?"))
				sendReport(out, url);
			else if (url.startsWith("comment?"))
				sendTeamViewer(out, url);
			else if (url.startsWith("image?"))
				sendImage(out, url);
			else if (url.startsWith("imgrs?"))
				sendIntImage(url, out, os);
			else if (url.startsWith("rate?"))
				sendRate(out, url);
			else if (url.startsWith("set-type?"))
				sendSetType(out, url);
			else if (url.startsWith("set-udf?"))
				sendSetUDF(out, url);
			else if (url.startsWith("type?"))
				sendType(out, url);
			else if (url.startsWith("udf?"))
				sendUDF(out, url);
			else if (url.startsWith("redo"))
				sendRedoThumbs(out, url);
			else if (url.startsWith("comment-add?"))
				sendCommentAdd(out, url);
			else if (url.startsWith("comment-del?")) {
				sendCommentDel(out, url);
			} else
				// 404!!!
				sendInvalid(out, "The specified URL or module was not found.");
			out.println();
		} catch (Exception e) {
			handleException(out, e);
		}
	}
	// MODULE WebActions
	// Sends the home page.
	private void sendStartPage(PrintWriter out) {
		StringBuilder output = new StringBuilder(1024);
		AppLib.printDebug("Sending start page");
		output.append("<a href=\"list\">View Team List</a><br>\n");
		output.append("<a href=\"matches\">View Match List</a><br>\n");
		output.append("<a href=\"matches#sched\">View Scheduled Matches</a><br>\n");
		output.append("<a href=\"itsajax\">AJAX Interface</a><br>\n");
		output.append("<a href=\"scoring\">Score a Match</a>");
		sendWebString(output.toString(), "Home", out);
	}
	// Sends the team list.
	private void sendTeamList(PrintWriter out) {
		StringBuilder output = new StringBuilder(32768);
		// team list
		AppLib.printDebug("Sending team list");
		Team team; int count = data.count();
		Iterator<Integer> it = data.getTeams().iterator();
		// print header
		output.append("<form name=\"input\" method=\"GET\" action=\"comment\">");
		output.append("<label for=\"team\">Lookup Team Number: </label>");
		output.append("<input type=\"text\" name=\"team\" id=\"team\" size=\"4\" maxlength=\"5\"> ");
		output.append("<input type=\"submit\" value=\"Go\"> &nbsp; Jump to: ");
		makeSeries(output, count, "list");
		output.append("</form>\n");
		output.append("<script language=\"JavaScript\" type=\"text/javascript\"><!--\n");
		output.append("window.setTimeout('document.getElementById(\"team\").focus();', 10);\n");
		output.append("// -->\n</script><a name=\"list\"></a>");
		output.append("<table width=\"100%\" border=\"1\" cellpadding=\"0\" ");
		output.append("cellspacing=\"0\">\n");
		int num, printed = 0, x;
		while (it.hasNext()) {
			// iterate each one
			if (printed % 25 == 0) {
				output.append("<tr><th>");
				makeSeriesLabel(output, printed, "list");
				output.append("#</th><th>Name</th><th>Rating</th><th>Type</th>");
				output.append("<th>Record</th><th>Points/Game</th></tr>\n");
			}
			team = data.get(it.next());
			num = team.getNumber();
			// name and number
			output.append("<tr><td><font size=\"+2\">");
			output.append(num);
			output.append("</font></td>\n<td><font size=\"+1\"><a href=\"comment?team=");
			output.append(num);
			output.append("\" name=\"");
			output.append(num);
			output.append("\">");
			output.append(htmlspecial(team.getName()));
			output.append("</a></font></td>\n<td align=\"center\">");
			// rating
			for (x = 0; x < team.getRating(); x++)
				output.append("<img src=\"imgrs?star-lit.png\" alt=\"*\">");
			for (; x < 5; x++)
				output.append("<img src=\"imgrs?star-unlit.png\" alt=\" \">");
			// type
			output.append("</td><td align=\"center\">");
			output.append(htmlspecial(team.getType()));
			// record, ppg
			output.append("</td><td align=\"center\">");
			output.append(team.getWins());
			output.append("-");
			output.append(team.getLosses());
			output.append("-");
			output.append(team.getTies());
			output.append("</td><td align=\"center\">");
			output.append(Math.round(team.getPPG() * 10.) / 10.);
			output.append("</td></tr>\n");
			printed++;
		}
		// the end!
		output.append("</table>Jump to: ");
		makeSeries(output, count, "list");
		sendWebString(output.toString(), "Team List", out);
	}
	// Sends the match list.
	private void sendMatchList(PrintWriter out, String url) {
		// this could be huge...
		StringBuilder output = new StringBuilder(32768);
		boolean isInternal = url.equals("internal-matches");
		AppLib.printDebug("Sending match list");
		// refresh?
		if (!isInternal)
			if (url.endsWith("refresh")) {
				output.append("<meta http-equiv=\"refresh\" content=\"30\">\nUpdate: ");
				output.append("<a href=\"matches\">Off</a> | <b>Every 30 seconds</b><br>\n");
			} else {
				output.append("Update: <b>Off</b> | <a href=\"matches?refresh\">Every 30 seconds");
				output.append("</a><br>\n");
			}
		// vars, vars, vars!
		ScheduleItem item; List<Integer> teams;
		Iterator<ScheduleItem> it;
		int printed = 0, scoreOne = 0, scoreTwo = 0;
		boolean hasMe;
		// iterators for convenience
		Iterator<Integer> teamIt;
		Iterator<Score> scoreIt;
		// lists of done and not done
		List<ScheduleItem> done = new LinkedList<ScheduleItem>();
		List<ScheduleItem> notDone = new LinkedList<ScheduleItem>();
		Map<SecondTime, ScheduleItem> sched = data.getSchedule();
		synchronized (sched) {
			// organize into two sets
			it = sched.values().iterator();
			while (it.hasNext()) {
				item = it.next();
				if (item.getStatus() == ScheduleItem.SCHEDULED)
					done.add(item);
				else
					notDone.add(item);
			}
		}
		// print completed first
		output.append("<a name=\"done\"><b>Completed Matches:</b></a><br>\n");
		if (!isInternal) {
			output.append("Jump to: <a href=\"#sched\">Scheduled Matches</a> | ");
			makeSeries(output, notDone.size(), "done");
		}
		output.append("<table border=\"1\" cellpadding=\"0\" cellspacing=\"0\">\n");
		it = notDone.iterator();
		while (it.hasNext()) {
			item = it.next();
			// iterate through the set
			scoreOne = item.getRedScore();
			scoreTwo = item.getBlueScore();
			scoreIt = item.getScores().iterator();
			// count teams
			teams = item.getTeams();
			teamIt = teams.iterator();
			hasMe = teams.contains(myTeam);
			// send header
			makeTableHeader(output, printed, "done");
			// index
			makeStartCell(output, item, printed, isInternal);
			// print cell bodies. if tied, neither is bolded
			makeTableCell(output, true, hasMe, teamIt, scoreIt, scoreOne,
				scoreOne > scoreTwo, isInternal);
			makeTableCell(output, false, hasMe, teamIt, scoreIt, scoreTwo,
				scoreOne < scoreTwo, isInternal);
			// output time scored too
			makeEndCell(output, item);
			printed++;
		}
		// bottom of table (no matches or count)
		makeEndTable(output, printed);
		if (!isInternal) {
			output.append("Jump to: <a href=\"#sched\">Scheduled Matches</a> | ");
			makeSeries(output, notDone.size(), "done");
		}
		// now scheduled
		output.append("<br><hr><a name=\"sched\"><b>Scheduled Matches:</b></a><br>\n");
		if (!isInternal) {
			output.append("Jump to: <a href=\"#done\">Completed Matches</a> | ");
			makeSeries(output, done.size(), "sched");
		}
		output.append("<table border=\"1\" cellpadding=\"0\" cellspacing=\"0\">\n");
		scoreOne = scoreTwo = printed = 0;
		it = done.iterator();
		while (it.hasNext()) {
			item = it.next();
			// iterate through the set
			teams = item.getTeams();
			teamIt = teams.iterator();
			hasMe = teams.contains(myTeam);
			// send header
			makeTableHeader(output, printed, "sched");
			// index
			makeStartCell(output, item, printed, isInternal);
			// print cell bodies. if tied, neither is bolded
			makeTableCell(output, true, hasMe, teamIt, null, 0, false, isInternal);
			makeTableCell(output, false, hasMe, teamIt, null, 0, false, isInternal);
			// output time scored too
			makeEndCell(output, item);
			printed++;
		}
		// bottom of table (no matches or count)
		makeEndTable(output, printed);
		if (!isInternal) {
			output.append("Jump to: <a href=\"#done\">Completed Matches</a> | ");
			makeSeries(output, done.size(), "sched");
		}
		// select
		if (!isInternal)
			sendWebString(output.toString(), "Match List", out);
		else {
			// this is suited for the content pane - no picture or header,
			//  just title and content
			Worker.sendHTTP(out, HTTPConstants.HTTP_OK);
			printInitialHeaders(out);
			out.println("Content-Type: text/html; charset=ISO-8859-1\n");
			// html headers
			out.print("<html><body><font face=\"Verdana, Arial, sans-serif\">");
			// body text
			out.println("<b>Scout449 Match List</b><hr>");
			out.println(output.toString());
			// html footer
			out.print("</font></body></html>");
		}
	}
	// Helper to generate a series of links.
	private void makeSeries(StringBuilder output, int number, String prefix) {
		// the beginning
		output.append("<a href=\"#");
		output.append(prefix);
		output.append("\">Start</a>");
		for (int i = 25; i < number; i += 25) {
			// every 25
			output.append(" | <a href=\"#");
			output.append(prefix);
			output.append("_");
			output.append(i);
			output.append("\">");
			output.append(i);
			output.append("</a>");
		}
		// the end
		if (number > 1) {
			output.append(" | <a href=\"#");
			output.append(prefix);
			output.append("_end\">End</a><br>\n");
		}
	}
	// Helper to label rows for series.
	private void makeSeriesLabel(StringBuilder output, int printed, String prefix) {
		output.append("<a name=\"");
		output.append(prefix);
		output.append("_");
		output.append(printed);
		output.append("\"></a>");
	}
	// Helper to send the end of the table.
	private void makeEndTable(StringBuilder output, int printed) {
		int w = ScheduleItem.TPA * 2 + 4;
		if (printed <= 0) {
			// empty table
			output.append("<tr><td width=\"");
			output.append(w * 70 + 60);
			output.append("\">No Matches!</td></tr>");
		} else if (printed == 1) {
			// 1 printed
			output.append("<tr><td colspan=\"");
			output.append(w);
			output.append("\">1 match.</td></tr>");
		} else {
			// > 2 printed
			output.append("<tr><td colspan=\"");
			output.append(w);
			output.append("\">");
			output.append(printed);
			output.append(" matches.</td></tr>");
		}
		output.append("</table>\n");
	}
	// Helper to send the start of match cell.
	private void makeStartCell(StringBuilder output, ScheduleItem item, int printed,
			boolean isInternal) {
		output.append("<tr><td width=\"130\" align=\"right\"><b><font size=\"-2\">");
		output.append(item.getLabel());
		output.append("</font> &nbsp;");
		output.append(item.getNum());
		output.append("</b>");
		if (!isInternal) {
			output.append("<br><a href=\"report?time=");
			output.append(item.getTime());
			output.append("\">report</a>");
		}
		output.append("</td>\n");
	}
	// Helper to send the end of match cell.
	private void makeEndCell(StringBuilder output, ScheduleItem item) {
		output.append("<td width=\"80\" align=\"center\"><b>");
		output.append(ScheduleItem.timeFormat(item.getTime()));
		output.append("<br>");
		output.append(ScheduleItem.dateFormat(item.getTime()));
		output.append("</b></td>\n");
	}
	// Helper to send a table header cell.
	private void makeTableHeader(StringBuilder output, int printed, String prefix) {
		if (printed % 25 == 0) {
			// output header
			output.append("<tr><th>#");
			makeSeriesLabel(output, printed, prefix);
			output.append("</th><th colspan=\"");
			output.append(ScheduleItem.TPA + 1);
			output.append("\">Red</th>");
			output.append("<th colspan=\"");
			output.append(ScheduleItem.TPA + 1);
			output.append("\">Blue</th><th>Time</th>");
		}
	}
	// Helper to send a match table cell.
	private void makeTableCell(StringBuilder output, boolean red,
			boolean hasMe, Iterator<Integer> teamIt, Iterator<Score> scoreIt,
			int score, boolean bold, boolean isInternal) {
		String bgColor, fgColor, name;
		if (red)
			name = "Red";
		else
			name = "Blue";
		if (hasMe) {
			if (red)
				bgColor = "#FF0000";
			else
				bgColor = "#0000FF";
			fgColor = "#FFFFFF";
		} else {
			if (red)
				bgColor = "#FFCCCC";
			else
				bgColor = "#CCCCFF";
			fgColor = "#3333FF";
		}
		output.append("<td align=\"center\" width=\"70\" bgcolor=\"");
		output.append(bgColor);
		output.append("\"><font color=\"");
		output.append(fgColor);
		output.append("\">");
		// boldface if this side won
		if (bold) output.append("<b>");
		output.append(name);
		if (bold) output.append("</b>");
		// match total score
		if (scoreIt != null) {
			output.append("<br>");
			output.append(score);
		}
		output.append("</font></td>\n");
		int teamNum;
		for (int i = 0; i < ScheduleItem.TPA; i++) {
			teamNum = teamIt.next();
			// iterate through teams
			output.append("<td align=\"center\" width=\"70\" bgcolor=\"");
			output.append(bgColor);
			output.append("\">");
			if (bold) output.append("<b>");
			// team number and score
			if (!isInternal) {
				output.append("<a style=\"text-decoration: none\" href=\"comment?team=");
				output.append(teamNum);
				output.append("\">");
			}
			output.append("<font color=\"");
			output.append(fgColor);
			output.append("\">#");
			output.append(teamNum);
			output.append("</font>");
			if (!isInternal) output.append("</a>");
			if (scoreIt != null) {
				output.append("<br>");
				output.append(scoreIt.next());
			}
			if (bold) output.append("</b>");
			output.append("</td>\n");
		}
	}
	// Helper to send a report on a team's matches in a regional.
	private void makeRegionalReport(StringBuilder output, Event event, int teamNum) {
		int index, res, i = 0; boolean side;
		Map<SecondTime, ScheduleItem> sched = event.getSchedule();
		// table header
		output.append("<hr><font size=\"+1\">");
		output.append(event.getName());
		output.append(":</font><br>");
		output.append("<table border=\"1\" cellpadding=\"0\"><tr><th width=\"130\">#");
		output.append("</th><th width=\"100\">Time</th><th width=\"80\">Color</th>");
		output.append("<th width=\"80\">Status</th><th width=\"180\">Points Scored</th>");
		output.append("<th width=\"80\">Report</th></tr>\n");
		for (ScheduleItem match : sched.values()) {
			index = match.getTeams().indexOf(teamNum);
			if (index >= 0) {
				side = index < ScheduleItem.TPA;
				// in this match
				output.append("<tr><td align=\"center\"><font size=\"-2\">");
				output.append(match.getLabel());
				output.append("</font> ");
				output.append(match.getNum());
				output.append("</td><td>");
				// time
				output.append(ScheduleItem.timeFormat(match.getTime()));
				output.append(" on ");
				output.append(ScheduleItem.dateFormat(match.getTime()));
				// red/blue
				output.append("</td><td align=\"center\" bgcolor=\"");
				output.append(side ? "#FF0000" : "#0000FF");
				output.append("\">");
				output.append(side ? "Red</td>" : "Blue</td>");
				if (match.getStatus() == ScheduleItem.COMPLETE) {
					// completed report
					output.append("<td align=\"center\">");
					res = match.getRedScore() - match.getBlueScore();
					if (!side) res = -res;
					if (res > 0) output.append("Won");
					else if (res < 0) output.append("Lost");
					else output.append("Tied");
					output.append("</td><td align=\"center\">");
					output.append(match.getScores().get(index));
					output.append("</td>");
				} else
					// scheduled
					output.append("<td colspan=\"2\" align=\"center\">Scheduled</td>");
				// report
				output.append("<td align=\"center\"><a href=\"report?time=");
				output.append(match.getTime());
				output.append("\">report</a></td></tr>\n");
				i++;
			}
		}
		if (i == 0)
			output.append("<tr><td colspan=\"6\" align=\"center\">No Matches</td></tr>");
		output.append("</table>");
	}
	// Sends a form to look up a team.
	private void sendLookup(PrintWriter out) {
		StringBuilder output = new StringBuilder(1024);
		output.append("<form name=\"input\" method=\"GET\" action=\"comment\">");
		output.append("<label for=\"team\">Team Number: </label>");
		output.append("<input type=\"text\" name=\"team\" id=\"team\" size=\"4\" maxlength=\"5\"> ");
		output.append("<input type=\"submit\" value=\"Go\"></form>\n");
		output.append("<script language=\"JavaScript\" type=\"text/javascript\"><!--\n");
		output.append("window.setTimeout('document.getElementById(\"team\").focus();', 10);\n");
		output.append("// -->\n</script>");
		sendWebString(output.toString(), "Lookup Team", out);
	}
	/*
	 * Sends the javascript match scorer. Note that this could be merged with
	 *  the match JS file, but the JS file is left for independent/3rd party
	 *  match scorers.
	 */
	private void sendMatchScorer(PrintWriter out) {
		// send headers
		Worker.sendHTTP(out, HTTPConstants.HTTP_OK);
		printInitialHeaders(out);
		out.println();
		// reach into the JAR!
		try {
			URL scoringURL = getClass().getResource("/scoring.htm");
			URLConnection conn = scoringURL.openConnection();
			// pass thru
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while (br.ready())
				out.println(br.readLine());
			br.close();
			out.flush();
		} catch (IOException e) {
			sendErrorMessage(out, true, "The Online Scoring application is not installed.", e);
		}
	}
	// Sends the team viewer.
	private void sendTeamViewer(PrintWriter out, String url) {
		StringBuilder output = new StringBuilder(16384);
		// parse get string and validate
		Map<String, String> params = Worker.parseGET(url.substring(8));
		if (params.containsKey("team")) {
			// validate team number
			String tm = params.get("team");
			if (!AppLib.positiveInteger(tm))
				sendInvalid(out, "Invalid team number.");
			else {
				int teamNum = Integer.parseInt(tm);
				Team team = data.get(teamNum);
				if (team == null)
					// reject this one
					sendInvalid(out, "Invalid team number.");
				else {
					int i;
					List<Integer> myData = team.getData();
					Iterator<String> comIt; Iterator<Integer> comDt;
					AppLib.printDebug("Sending team viewer: team " + teamNum);
					output.append("<a href=\"list\">Return to team list</a><br><br>\n");
					// image
					output.append("<a href=\"image?team=");
					output.append(teamNum);
					output.append("\"><img align=\"left\" title=\"Click to enlarge\"");
					output.append(" border=\"0\" src=\"thumbnail?team=");
					output.append(teamNum);
					output.append("\" alt=\"[No Image]\" width=\"160\" height=\"120\"></a>");
					// rating
					output.append("\n<b>Rating:</b> ");
					for (i = 0; i < team.getRating(); i++) {
						output.append("<a href=\"rate?team=");
						output.append(teamNum);
						output.append("&rate=");
						output.append(i + 1);
						output.append("\"><img border=\"0\" src=\"imgrs?star-lit.png\"");
						output.append(" alt=\"*\"></a>");
					}
					for (; i < 5; i++) {
						output.append("<a href=\"rate?team=");
						output.append(teamNum);
						output.append("&rate=");
						output.append(i + 1);
						output.append("\"><img border=\"0\" src=\"imgrs?star-unlit.png\"");
						output.append(" alt=\" \"></a>");
					}
					// type
					output.append("&nbsp;&nbsp;\n<b>Type:</b> ");
					output.append("Not Set");
					output.append(htmlspecial(team.getType()));
					output.append(" &nbsp; <a href=\"set-type?team=");
					output.append(teamNum);
					output.append("\">change</a><br>\n<b>Record:</b> ");
					// record
					output.append(team.getWins());
					output.append(" wins, ");
					output.append(team.getLosses());
					output.append(" losses, ");
					output.append(team.getTies());
					output.append(" ties (");
					output.append(team.getWinPct());
					// points/game
					output.append("%)<br>\n<b>Points/Game:</b> ");
					output.append(team.getPPG());
					output.append(" &nbsp; <b>Team Points/Game:</b> ");
					output.append(team.getTeamPPG());
					output.append("<br clear=\"all\">\n<br>\n");
					// go through UDFs
					List<String> udfs = data.getData().getUDFs();
					if (myData != null && udfs != null) {
						comDt = myData.iterator();
						comIt = udfs.iterator();
						for (i = 0; comIt.hasNext() && comDt.hasNext(); i++) {
							output.append("<b>");
							output.append(htmlspecial(comIt.next()));
							output.append("</b>: &nbsp; ");
							output.append(comDt.next());
							output.append("<br>\n");
						}
					}
					output.append("<a href=\"set-udf?team=");
					output.append(teamNum);
					output.append("\">change values</a><br>\n<br>\n");
					i = 0;
					// go through comments and print them
					for (String com : team.getComments()) {
						output.append("<a href=\"comment-del?team=");
						output.append(teamNum);
						output.append("&index=");
						output.append(i);
						output.append("\"><img src=\"imgrs?del.png\" border=\"0\" alt=\"Del\"></a>");
						output.append("&nbsp;&nbsp;");
						output.append(htmlspecial(com));
						output.append("<br>\n");
						i++;
					}
					// indicate if there are none!
					if (i <= 0) output.append("<b> -- No comments -- </b><br>\n");
					output.append("<br><form name=\"add\" method=\"GET\" action=\"comment-add\">");
					output.append("<input type=\"hidden\" name=\"team\" value=\"");
					output.append(teamNum);
					// input
					output.append("\">\n<input type=\"text\" name=\"comment\" size=\"56\" ");
					output.append("maxlength=\"256\"> <input type=\"submit\" value=\"Add ");
					output.append("Comment\"></form>\n<b>Matches Involving This Team</b><br>");
					// list of matches
					Event evt = data.getActive();
					synchronized (data.getSchedule()) {
						makeRegionalReport(output, evt, teamNum);
					}
					// now for all others
					for (Event e : data.getData().getEvents())
						if (!e.equals(evt))
							makeRegionalReport(output, e, teamNum);
					output.append("<br><a href=\"list\">Return to team list</a>");
					sendWebString(output.toString(), "Info for team " + htmlspecial(team.toString()),
						out);
				}
			}
		} else
			sendInvalid(out, "No team number supplied.");
	}
	/**
	 * Sends a general image or no image if none.
	 * 
	 * @param url the image URL
	 * @param out the output writer
	 * @param os the output stream
	 */
	private void sendIntImage(String url, PrintWriter out, OutputStream os) {
		String img = url.substring(6);
		if (img.indexOf('.') <= 0 || img.indexOf('/') >= 0 || img.indexOf('\\') >= 0) {
			// no hacking!
			Worker.sendHTTP(out, HTTPConstants.HTTP_FORBIDDEN);
			printInitialHeaders(out);
			out.println();
			return;
		}
		try {
			String ctype = "png";
			if (img.endsWith(".jpg") || img.endsWith(".jpeg")) ctype = "jpg";
			if (img.endsWith(".gif")) ctype = "gif";
			// get the image
			URL scoringURL = getClass().getResource("/images/" + img);
			URLConnection conn = scoringURL.openConnection();
			InputStream is = conn.getInputStream();
			Worker.sendHTTP(out, HTTPConstants.HTTP_OK);
			printInitialHeaders(out);
			// correct content type
			out.print("Content-Type: image/");
			out.println(ctype);
			out.print("\n");
			out.flush();
			copyStream(is, os);
			is.close();
		} catch (Exception e) {
			// no such image
			Worker.sendHTTP(out, HTTPConstants.HTTP_NOT_FOUND);
			printInitialHeaders(out);
			out.println();
		}
	}
	// Sends an image viewer form.
	private void sendImage(PrintWriter out, String url) {
		StringBuilder output = new StringBuilder(1024);
		// parse get string and validate
		Map<String, String> params = Worker.parseGET(url.substring(6));
		if (params.containsKey("team")) {
			// validate team number
			String tm = params.get("team");
			if (!AppLib.positiveInteger(tm))
				// reject this one
				sendInvalid(out, "Invalid team number.");
			else {
				int teamNum = Integer.parseInt(params.get("team"));
				Team team = data.getPool(teamNum);
				if (team == null)
					// reject this one
					sendInvalid(out, "Invalid team number.");
				else {
					AppLib.printDebug("Image viewing for " + teamNum);
					// success
					output.append("<a href=\"comment?team=");
					output.append(teamNum);
					output.append("\">Return to viewing ");
					output.append(teamNum);
					output.append(".<br><br><img border=\"0\" src=\"images/teams/");
					output.append(teamNum);
					output.append(".jpg\" alt=\"[No Image]\" title=\"Image for team ");
					output.append(teamNum);
					output.append("\" width=\"800\" height=\"600\"></a>");
					sendWebString(output.toString(), "Image for " + htmlspecial(team.toString()),
						out);
				}
			}
		} else
			sendInvalid(out, "No team number supplied.");
	}
	// Sends the rating page.
	private void sendRate(PrintWriter out, String url) {
		// parse get string and validate
		Map<String, String> params = Worker.parseGET(url.substring(5));
		if (params.containsKey("team") && params.containsKey("rate")) {
			// validate team number
			String tm = params.get("team");
			if (!AppLib.positiveInteger(tm))
				// reject this one
				sendInvalid(out, "Invalid team number.");
			else {
				int teamNum = Integer.parseInt(params.get("team"));
				Team team = data.getPool(teamNum);
				String rate = params.get("rate");
				if (team == null || !AppLib.positiveInteger(rate))
					// reject this one
					sendInvalid(out, "Invalid team number or rating.");
				else {
					int rating = Integer.parseInt(rate);
					AppLib.printDebug("Rating " + teamNum);
					if (rating > 5)
						// illegal, too long
						sendInvalid(out, "Invalid rating, must be from 1 to 5.");
					else {
						// success
						data.rate(teamNum, rating);
						sendInfoUpdate(out, team);
					}
				}
			}
		} else
			sendInvalid(out, "No team number supplied.");
	}
	// Sends the robot type page.
	private void sendType(PrintWriter out, String url) {
		// parse get string and validate
		Map<String, String> params = Worker.parseGET(url.substring(5));
		if (params.containsKey("team") && params.containsKey("type")) {
			// validate team number
			String tm = params.get("team");
			if (!AppLib.positiveInteger(tm))
				// reject this one
				sendInvalid(out, "Invalid team number.");
			else {
				int teamNum = Integer.parseInt(params.get("team"));
				Team team = data.getPool(teamNum);
				String type = params.get("type").toLowerCase();
				if (team == null)
					// reject this one
					sendInvalid(out, "Invalid team number.");
				else {
					AppLib.printDebug("Setting robot type for " + teamNum);
					boolean done = false;
					List<String> types = data.getData().getTypes();
					for (String toTest : types)
						// test each one
						if (toTest.toLowerCase().equals(type)) {
							// success!
							data.setType(teamNum, toTest);
							sendInfoUpdate(out, team);
							done = true;
							break;
						}
					if (!done)
						sendInvalid(out, "Invalid robot type. Valid robot types can be one of " +
							types + ".");
				}
			}
		} else
			sendInvalid(out, "No team number supplied.");
	}
	// Sends the set UDFs page.
	private void sendSetUDF(PrintWriter out, String url) {
		StringBuilder output = new StringBuilder(1024);
		Map<String, String> params = Worker.parseGET(url.substring(8));
		if (params.containsKey("team")) {
			// validate team number
			String tm = params.get("team");
			if (!AppLib.positiveInteger(tm))
				// reject this one
				sendInvalid(out, "Invalid team number.");
			else {
				int teamNum = Integer.parseInt(params.get("team"));
				Team team = data.getPool(teamNum);
				if (team == null)
					// reject this one
					sendInvalid(out, "Invalid team number.");
				else {
					// success!!!
					output.append("<form name=\"set-udf\" action=\"udf\" method=\"GET\">\n");
					output.append("<input type=\"hidden\" name=\"team\" value=\"");
					output.append(teamNum);
					output.append("\">\nField: <select name=\"field\">\n");
					int i = 0;
					for (String opt : data.getData().getUDFs()) {
						// go through option list
						output.append("<option value=\"");
						output.append(i);
						output.append("\"");
						if (i == 0)
							output.append(" selected");
						output.append(">");
						output.append(htmlspecial(opt));
						output.append("</option>\n");
						i++;
					}
					output.append("</select><br>\nValue: <input type=\"text\" name=\"value\"");
					output.append("value=\"0\" size=\"3\" maxlength=\"7\"><br>\n");
					output.append("<input type=\"submit\" value=\"Update\"></form>");
					sendWebString(output.toString(), "Change UDF for " + htmlspecial(team.toString()),
						out);
				}
			}
		} else
			sendInvalid(out, "No team number supplied.");
	}
	// Sends the set types page.
	private void sendSetType(PrintWriter out, String url) {
		StringBuilder output = new StringBuilder(1024);
		Map<String, String> params = Worker.parseGET(url.substring(9));
		if (params.containsKey("team")) {
			// validate team number
			String tm = params.get("team");
			if (!AppLib.positiveInteger(tm))
				// reject this one
				sendInvalid(out, "Invalid team number.");
			else {
				int teamNum = Integer.parseInt(params.get("team"));
				Team team = data.getPool(teamNum);
				if (team == null)
					// reject this one
					sendInvalid(out, "Invalid team number.");
				else {
					// success!!!
					output.append("<form name=\"set-type\" action=\"type\" method=\"GET\">\n");
					output.append("<input type=\"hidden\" name=\"team\" value=\"");
					output.append(teamNum);
					output.append("\">\nType: <select name=\"type\">\n");
					int i = 0;
					for (String opt : data.getData().getTypes()) {
						// go through option list
						output.append("<option value='");
						output.append(escapeQuotes(opt));
						output.append("'");
						if (team.getType().equalsIgnoreCase(opt))
							output.append(" selected");
						output.append(">");
						output.append(htmlspecial(opt));
						output.append("</option>\n");
						i++;
					}
					output.append("</select> &nbsp; <input type=\"submit\" value=\"Update\">");
					output.append("</form>");
					sendWebString(output.toString(), "Change type for " + htmlspecial(team.toString()),
						out);
				}
			}
		} else
			sendInvalid(out, "No team number supplied.");
	}
	// Sends the UDF page.
	private void sendUDF(PrintWriter out, String url) {
		// parse get string and validate
		Map<String, String> params = Worker.parseGET(url.substring(4));
		if (params.containsKey("team") && params.containsKey("value") &&
				params.containsKey("field")) {
			// validate team number
			String tm = params.get("team");
			if (!AppLib.positiveInteger(tm))
				// reject this one
				sendInvalid(out, "Invalid team number.");
			else {
				int teamNum = Integer.parseInt(params.get("team"));
				Team team = data.getPool(teamNum);
				String value = params.get("value");
				String fld = params.get("field");
				if (team == null || !AppLib.validInteger(value)
						|| !AppLib.validInteger(fld))
					// reject this one
					sendInvalid(out, "Invalid team number, field, or rating.");
				else {
					// make sure...
					int val = Integer.parseInt(value);
					if (val < 0) val = 0;
					int field = Integer.parseInt(fld);
					List<Integer> dt = team.getData();
					AppLib.printDebug("Setting UDF for " + teamNum);
					if (data == null || field > data.getData().getUDFs().size() ||
							field > dt.size() || field < 0)
						// illegal, too long
						sendInvalid(out, "Invalid field index.");
					else {
						// success
						dt.set(field, val);
						sendInfoUpdate(out, team);
					}
				}
			}
		} else
			sendInvalid(out, "No team number supplied.");
	}
	// Sends a message that says regeneration started.
	private void sendRedoThumbs(PrintWriter out, String url) {
		StringBuilder output = new StringBuilder(1024);
		// this should have no links in!!!!
		if (url.equals("redo?yes=yes")) {
			// yes sure!
			output.append("<b>Your request to re-generate thumbnails is in progress.</b><br>\n");
			output.append("This may take a while and may not start right away. During this time, ");
			output.append("the server may run slowly or exhibit other unusual behavior.\n");
			new ImageDaemon(true).start();
		} else {
			// really sure?
			output.append("<b>You are about to re-generate all thumbnails.</b><br>\n");
			output.append("This may take a while and may not start right away. During this time, ");
			output.append("the server may run slowly or exhibit other unusual behavior.<br>\n<b>");
			output.append("Avoid using this function unless the thumbnails are unfixable and ");
			output.append("nobody will be using the server for a few minutes!</b><br>\n");
			output.append("<br><i>Are you really sure?</i> <a href=\"/\">No</a>\n");
			output.append("<a href=\"redo?yes=yes\"><font color=\"#000000\">Yes</font></a>");
		}
		sendWebString(output.toString(), "Request Processed", out);
	}
	// Sends the add comment page.
	private void sendCommentAdd(PrintWriter out, String url) {
		// parse get string and validate
		Map<String, String> params = Worker.parseGET(url.substring(12));
		if (params.containsKey("team") && params.containsKey("comment")) {
			// validate team number
			String tm = params.get("team");
			if (!AppLib.positiveInteger(tm))
				// reject this one
				sendInvalid(out, "Invalid team number.");
			else {
				int teamNum = Integer.parseInt(params.get("team"));
				Team team = data.getPool(teamNum);
				String comment = params.get("comment");
				if (team == null)
					// reject this one
					sendInvalid(out, "Invalid team number.");
				else {
					AppLib.printDebug("Adding comment to " + teamNum);
					if (comment.length() > 256)
						// illegal, too long
						sendInvalid(out, "Comment is too long (must be less than 256 characters).");
					else {
						// success
						team.getComments().add(0, comment);
						data.setComments(teamNum, team.getComments());
						sendInfoUpdate(out, team);
					}
				}
			}
		} else
			sendInvalid(out, "No team number supplied.");
	}
	// Sends the delete comment page.
	private void sendCommentDel(PrintWriter out, String url) {
		// parse get string and validate
		Map<String, String> params = Worker.parseGET(url.substring(12));
		if (params.containsKey("team") && params.containsKey("index")) {
			// validate team number and index
			String tm = params.get("team");
			String ind = params.get("index");
			if (!AppLib.positiveInteger(tm) || !AppLib.validInteger(ind))
				// reject this one
				sendInvalid(out, "Invalid team number or comment index.");
			else {
				int teamNum = Integer.parseInt(params.get("team"));
				int index = Integer.parseInt(params.get("index"));
				Team team = data.getPool(teamNum);
				if (team == null)
					// reject this one
					sendInvalid(out, "Invalid team number.");
				else {
					AppLib.printDebug("Deleting comment from " + teamNum);
					if (index >= team.getComments().size() || index < 0)
						// out of bounds index
						sendWebString("Comment index is out of bounds.", "Comment Error", out);
					else {
						// success
						team.getComments().remove(index);
						data.setComments(teamNum, team.getComments());
						sendInfoUpdate(out, team);
					}
				}
			}
		} else
			sendInvalid(out, "No team number supplied.");
	}
	// Sends the scouting report page.
	private void sendReport(PrintWriter out, String url) {
		Map<String, String> params = Worker.parseGET(url.substring(7));
		if (params.containsKey("time") || params.containsKey("match")) {
			// validate match
			long tm = 0L;
			try {
				tm = Long.parseLong(params.get("time"));
			} catch (Exception e) {
				sendInvalid(out, "Match number or time is not valid.");
				return;
			}
			synchronized (data.getSchedule()) {
				ScheduleItem match = data.getSchedule().get(new SecondTime(tm));
				if (match == null)
					sendWebString("No matches matched this query. <a href=\"matches\">" +
						"Return to match list</a>\n", "No Report Available", out);
				else
					sendRealReport(out, match);
			}
		} else
			sendInvalid(out, "No match identifier supplied.");
	}
	// Sends the scouting report page for a given match.
	private void sendRealReport(PrintWriter out, ScheduleItem match) {
		StringBuilder output = new StringBuilder(8192);
		// compute title
		String title = match.getLabel() + " #" + match.getNum() + " at " +
			ScheduleItem.timeFormat(match.getTime()) +
			" (" + ScheduleItem.dateFormat(match.getTime()) + ")";
		// html headers
		output.append("<html><head><title>Scouting Report for ");
		output.append(title);
		// html body and header
		output.append("</title>\n</head><body><font size=\"-2\"><i><a href=\"/\">Return to ");
		output.append("homepage</a></i></font><hr>\n<center><b><font size=\"+1\">");
		output.append(title);
		output.append("</font></b></center>\n<font face=\"Verdana, Arial, sans-serif\">\n");
		// body text
		output.append("<table border=\"1\" cellpadding=\"0\" cellspacing=\"1\" width=\"660");
		output.append("\"><tr><th width=\"330\" bgcolor=\"#FF0000\">Red</th>");
		output.append("<th width=\"330\" bgcolor=\"#00FFFF\">Blue</th></tr>\n");
		// print each row
		List<Integer> teams = match.getTeams();
		for (int i = 0; i < ScheduleItem.TPA; i++) {
			// the red team
			output.append("<tr>");
			makeMiniReport(output, teams.get(i));
			// the blue team
			makeMiniReport(output, teams.get(i + ScheduleItem.TPA));
			output.append("</tr>");
		}
		// generated time and version
		output.append("</table><hr><font size=\"-2\"><i><a href=\"/\">Return to homepage");
		output.append("</a></i> : <i>Printed at ");
		long date = System.currentTimeMillis();
		output.append(ScheduleItem.timeFormat(date));
		output.append(" on ");
		output.append(ScheduleItem.dateFormat(date));
		output.append("; version ");
		output.append(Constants.VERSION);
		// html footer
		output.append("</i></font></font></body></html>");
		// http startup
		Worker.sendHTTP(out, HTTPConstants.HTTP_OK);
		printInitialHeaders(out);
		out.println("Content-Type: text/html; charset=ISO-8859-1\n");
		out.print(output.toString());
	}
	// Helper to send a mini-report (used in match report)
	private void makeMiniReport(StringBuilder output, int tm) {
		int j; Team team = data.get(tm);
		List<Integer> myData; List<String> coms; Iterator<String> comIt;
		Iterator<Integer> comDt;
		if (team == null) {
			// this should never happen. make it graceful!
			output.append("<td height=\"250\" valign=\"top\">Team Not Found</td>");
			return;
		}
		output.append("<td height=\"250\" valign=\"top\">\n<img src=\"thumbnail?team=");
		output.append(team.getNumber());
		output.append("\" alt=\"[No Image]\" width=\"120\" height=\"90\" align=\"left");
		output.append("\">\n");
		// team number
		String nn = team.toString();
		if (nn.length() > 20) nn = nn.substring(0, 20) + "...";
		output.append(htmlspecial(nn));
		output.append("<br>");
		// record, ppg
		output.append("<font size=\"-1\"><b>Record: ");
		output.append(team.getWins());
		output.append("-");
		output.append(team.getLosses());
		output.append("-");
		output.append(team.getTies());
		output.append("<br>Points/Game: ");
		output.append(team.getPPG());
		output.append("</b><br>\n");
		output.append(htmlspecial(team.getType()));
		output.append(" ");
		// stars
		for (j = 0; j < team.getRating(); j++)
			output.append("<img src=\"imgrs?star-lit.png\" alt=\"*\">");
		for (; j < 5; j++)
			output.append("<img src=\"imgrs?star-unlit.png\" alt=\" \">");
		output.append("<br>\n");
		myData = team.getData();
		// UDFs. Only the 1st 3 (at most) get printed!
		coms = team.getComments();
		List<String> udfs = data.getData().getUDFs();
		if (myData != null && udfs != null) {
			comDt = myData.iterator();
			comIt = udfs.iterator();
			for (j = 0; comDt.hasNext() && j < Math.max(3, 7 - coms.size()) && comIt.hasNext(); j++) {
				output.append("<b>");
				output.append(htmlspecial(comIt.next()));
				output.append(":</b>&nbsp; ");
				output.append(comDt.next());
				output.append("<br>\n");
			}
		}
		// comments. Only the 1st 4 (at most) get printed!
		if (coms != null) {
			comIt = coms.iterator();
			for (j = 0; comIt.hasNext() && j < 4; j++) {
				output.append(htmlspecial(comIt.next()));
				output.append("<br>\n");
			}
		}
		output.append("<hr><br></font></td>\n");
	}
	/**
	 * Sends the information updated page.
	 * 
	 * @param out the output stream
	 * @param team the team that was updated
	 */
	private void sendInfoUpdate(PrintWriter out, Team team) {
		int teamNum = team.getNumber();
		StringBuilder output = new StringBuilder(1024);
		output.append("<b>Team ");
		output.append(teamNum);
		output.append(" information has been updated.</b><br>\n");
		output.append("<a href=\"comment?team=");
		output.append(teamNum);
		output.append("\">Return to editing ");
		output.append(teamNum);
		output.append(".</a>");
		fireUpdate();
		sendWebString(output.toString(), "Information Updated for " + htmlspecial(team.toString()),
			out);
	}
	/**
	 * Sends an invalid error message page, with help.
	 * 
	 * @param out the output stream
	 * @param reason the reason for the error.
	 */
	private void sendInvalid(PrintWriter out, String reason) {
		//AppLib.printDebug("Invalid data. Reason: " + reason);
		sendErrorMessage(out, false, reason, null);
	}
	/**
	 * Handles an HTTP error such as not found, bad request...
	 * 
	 * @param out the output stream
	 * @param message the error message (from the extra headers)
	 */
	private void handleHTTPError(PrintWriter out, String message) {
		try {
			message = message.trim();
			int index = message.indexOf(';'), code;
			String page;
			if (index >= 0) {
				// url was specified; pull it out
				code = Integer.parseInt(message.substring(0, index));
				page = message.substring(index + 1, message.length());
			} else {
				code = Integer.parseInt(message); page = "";
			}
			if (code == HTTPConstants.HTTP_NOT_FOUND) {
				// try to determine content type
				String ext = new File(page).getName();
				index = ext.indexOf('.');
				if (index > 0) {
					// look at content type; feed the error message only for html
					ext = ext.substring(index, ext.length());
					ext = HTTPConstants.mimeTypes.get(ext);
					if (ext != null && ext.equals("text/html"))
						sendInvalid(out, "The specified URL or module was not found.");
					else {
						Worker.sendHTTP(out, HTTPConstants.HTTP_NOT_FOUND);
						printInitialHeaders(out);
						out.println();
					}
				} else
					// it's a module request (no dot). Epicfail.
					sendInvalid(out, "The specified URL or module was not found.");
			} else
				// general HTTP exception
				sendInvalid(out, code + " " + HTTPConstants.messages.get(code));
		} catch (Exception e) {
			sendInvalid(out, "The specified URL or module was not found.");
		}
	}
	/**
	 * The parent for exception handling. Does everything needed to print a
	 *  graceful failure message.
	 * 
	 * @param out the output stream
	 * @param maybeValid whether the request was an unexpected error
	 * @param reason the reason for the error
	 * @param e the exception (if any)
	 */
	private void sendErrorMessage(PrintWriter out, boolean maybeValid, String reason,
		Exception e) {
		StringBuffer output = new StringBuffer(4096);
		output.append("<b><font size=\"+1\">We're sorry...</font></b><br>\n... but the request ");
		output.append("that you made");
		if (maybeValid)
			output.append(", while possibly valid, could not be processed");
		else
			output.append(" was not valid or not applicable to its target");
		output.append(".<br>\n<i>The reason stated by the ");
		if (reason == null) reason = "An internal error occurred.";
		output.append("server code module is: " + htmlspecial(reason) + "</font></i><br>\n");
		output.append("<b><font size=\"+1\">Suggestions:</font></b><br>\n<ul><li>If you accessed ");
		output.append("this page by direct URL entry or using a bookmark, the link is likely to ");
		output.append("be outdated or old.<br>\nTry going to the <a href=\"list\">team list</a>, ");
		output.append("<a href=\"matches\">match list</a>, or <a href=\"/\">home page</a>.</li>\n");
		output.append("<li>If you tried to edit information about a team, another user may have ");
		output.append("since modified this team's information. Try re-starting your edit from ");
		output.append("the beginning, or going <a href=\"javascript:history.go(-1)\">back</a> ");
		output.append("and trying again. Scout449 users always have priority over web users.</li>");
		output.append("\n<li>If this message is appearing in a Scout449 window, update your copy ");
		output.append("of the program. The version of this server is <i>");
		output.append(Constants.VERSION);
		output.append("</i>. You can see the version of your Scout449 program by reading the ");
		output.append("splash screen.</li>\n<li>Try contacting the Administrator of this server. ");
		output.append("Ask for help on QuickChat, but do <i>not</i> ring the bell!</li>\n<li>");
		output.append("Still stuck? Try <a href=\"javascript:location.reload()\">reloading</a> ");
		output.append("this page or <a href=\"/\">starting over</a> from the beginning.</li>\n");
		if (e != null) {
			output.append("</ul><br><br>Detailed Error Information:<br>\n<code>");
			// can't use printStackTrace because of html escapes
			StackTraceElement[] trace = e.getStackTrace();
            for (int i = 0; i < trace.length; i++)
                output.append("\tat " + htmlspecial(trace[i].toString()));
			output.append("</code>\n");
		} else output.append("</ul>\n");
		sendWebString(output.toString(), maybeValid ? "Server Error (Exceptional Condition)" :
			"User Error (Non-Exceptional Condition)", out);
	}
	/**
	 * Handles an exception.
	 * 
	 * @param out the output stream
	 * @param e the exception that was thrown
	 */
	private void handleException(PrintWriter out, Exception e) {
		sendErrorMessage(out, true, e.getClass().getSimpleName() + ": " + e.getMessage(), e);
	}
	/**
	 * Sends a team thumbnail or no image if none.
	 * 
	 * @param url the thumbnail URL
	 * @param out the output writer
	 * @param os the output stream
	 */
	private void sendThumbnail(String url, PrintWriter out, OutputStream os) {
		// parse get string and validate
		Map<String, String> params = Worker.parseGET(url.substring(10));
		if (params.containsKey("team")) {
			// validate team number and index
			String tm = params.get("team");
			if (!AppLib.positiveInteger(tm)) {
				Worker.sendHTTP(out, HTTPConstants.HTTP_NOT_FOUND);
				printInitialHeaders(out);
				out.println();
			} else try {
				// get the image
				FileInputStream is = new FileInputStream("images/thumbnails/" + tm + ".jpg");
				Worker.sendHTTP(out, HTTPConstants.HTTP_OK);
				printInitialHeaders(out);
				out.println("Content-Type: image/jpeg\n");
				out.flush();
				copyStream(is, os);
				is.close();
			} catch (Exception e) {
				try {
					// get the image
					//FileInputStream is = new FileInputStream("images/none.jpg");
					URL scoringURL = Server.class.getResource("/images/none.jpg");
					URLConnection conn = scoringURL.openConnection();
					InputStream is = conn.getInputStream();
					Worker.sendHTTP(out, HTTPConstants.HTTP_OK);
					printInitialHeaders(out);
					out.println("Content-Type: image/jpeg\n");
					out.flush();
					copyStream(is, os);
					is.close();
				} catch (Exception e2) {
					AppLib.debugException(e2);
					// true epic fail
					Worker.sendHTTP(out, HTTPConstants.HTTP_NOT_FOUND);
					printInitialHeaders(out);
					out.println();
				}
			}
		} else {
			Worker.sendHTTP(out, HTTPConstants.HTTP_NOT_FOUND);
			printInitialHeaders(out);
			out.println();
		}
	}
	/**
	 * Copies one stream to another.
	 * 
	 * @param is the input stream
	 * @param os the output stream
	 */
	private void copyStream(InputStream is, OutputStream os) throws IOException {
		byte[] buffer = new byte[1024];
		int i;
		while (true) {
			// read from the input...
			i = is.read(buffer, 0, buffer.length);
			if (i < 0) break;
			// and write it right back to the output
			os.write(buffer, 0, i);
			os.flush();
		}
	}
	/**
	 * Sends content to the browser.
	 * 
	 * @param body the content to send
	 * @param title the title of the content
	 * @param out the output stream
	 */
	private void sendWebString(String body, String title, PrintWriter out) {
		Worker.sendHTTP(out, HTTPConstants.HTTP_OK);
		printInitialHeaders(out);
		out.println("Content-Type: text/html; charset=ISO-8859-1");
		// compute title
		title = "Scout449 Web Interface : " + title;
		out.println();
		// html headers
		out.print("<html><head><title>");
		out.print(title);
		// html body and java logo
		out.print("</title></head><body bgcolor=\"#D6DFF7\">");
		out.print("<img src=\"imgrs?java_logo.png\" alt=\"[Sun Java]\" align=\"left\">&nbsp;&nbsp;");
		out.print("<font face=\"Verdana, Arial, sans-serif\"><font color=\"#6A91D6\" size=\"+2\">");
		out.print("Application Server</font><br clear=\"all\"><br><b>");
		// body text
		out.print(title);
		out.println("</b><br><font size=\"-2\"><i><a href=\"/\">Return to homepage</a></i></font><hr>");
		out.println(body);
		// generated time and version
		out.print("<hr><font size=\"-2\"><i><a href=\"/\">Return to homepage</a></i> : <i>");
		out.print("Page generated at ");
		long date = System.currentTimeMillis();
		out.print(ScheduleItem.timeFormat(date));
		out.print(" on ");
		out.print(ScheduleItem.dateFormat(date));
		out.print(" by Scout449 version ");
		out.print(Constants.VERSION);
		// html footer
		out.print("</i></font></font></body></html>");
	}
	/**
	 * Prints the (better) start-up headers.
	 * 
	 * @param out the output stream
	 */
	private void printInitialHeaders(PrintWriter out) {
		out.print("Server: Scout449 ");
		out.println(Constants.VERSION);
		out.print("Pragma: no-cache\nDate: ");
		out.println(new Date());
	}
	// END MODULE
}