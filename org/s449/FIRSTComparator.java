package org.s449;

import java.util.*;

/**
 * Comparator to sort by SP then RP (FIRST Rank).
 * 
 * @author Stephen Carlson
 * @version 4.0.0
 */
public class FIRSTComparator implements Comparator {
	public static final FIRSTComparator instance = new FIRSTComparator();

	private FIRSTComparator() { }
	public int compare(Object o, Object t) {
		Team one = (Team)o;
		Team two = (Team)t;
		int sp = two.getSP() - one.getSP();
		if (sp != 0) return sp;
		int rp = two.getRP() - one.getRP();
		if (rp != 0) return rp;
		// max (penalized) match score
		int maxOne = -1, maxTwo = -1, sc;
		ScheduleItem match;
		Iterator it = one.getMatches().values().iterator();
		while (it.hasNext()) {
			match = (ScheduleItem)it.next();
			if (match.getStatus() == ScheduleItem.COMPLETE) {
				// accumulate its match score
				sc = match.getTeams().indexOf(new Integer(one.getNumber()));
				if (sc >= 0 && sc < ScheduleItem.TPA)
					maxOne = Math.max(maxOne, match.getRedScore());
				else if (sc < 2 * ScheduleItem.TPA)
					maxOne = Math.max(maxOne, match.getBlueScore());
			}
		}
		it = two.getMatches().values().iterator();
		while (it.hasNext()) {
			match = (ScheduleItem)it.next();
			if (match.getStatus() == ScheduleItem.COMPLETE) {
				// accumulate its match score
				sc = match.getTeams().indexOf(new Integer(one.getNumber()));
				if (sc >= 0 && sc < ScheduleItem.TPA)
					maxTwo = Math.max(maxTwo, match.getRedScore());
				else if (sc < 2 * ScheduleItem.TPA)
					maxTwo = Math.max(maxTwo, match.getBlueScore());
			}
		}
		int dm = maxTwo - maxOne;
		if (dm != 0) return dm;
		return -1;
	}
}