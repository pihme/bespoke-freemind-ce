/*FreeMind - A Program for creating and viewing Mindmaps
 *Copyright (C) 2000-2026  FreeMind CE contributors
 *See COPYING for Details
 *
 *This program is free software; you can redistribute it and/or
 *modify it under the terms of the GNU General Public License
 *as published by the Free Software Foundation; either version 2
 *of the License, or (at your option) any later version.
 *
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.
 *
 *You should have received a copy of the GNU General Public License
 *along with this program; if not, write to the Free Software
 *Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package tests.freemind.cov.time;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import accessories.plugins.time.CalendarMarkingEvaluator;
import accessories.plugins.time.ICalendarMarkingEvaluator;
import freemind.controller.actions.generated.instance.CalendarMarking;
import freemind.controller.actions.generated.instance.CalendarMarkings;

/** Backwards-compatible repeat rules on {@link ICalendarMarkingEvaluator}. */
class CalendarMarkingEvaluatorBehaviourTest {

	@Test
	void isoWeekRulesAndYearlyEveryNthMonthRestartInJanuary() {
		// backwards compatibility
		Calendar iso = CalendarMarkingEvaluator.createIsoCalendar();
		assertEquals(Calendar.MONDAY, iso.getFirstDayOfWeek());
		assertEquals(4, iso.getMinimalDaysInFirstWeek());

		CalendarMarkingEvaluator evaluator = evaluator(
				mark(CalendarMarking.YEARLY_EVERY_NTH_MONTH, day(2015, Calendar.NOVEMBER, 15), 0L, 2,
						Calendar.NOVEMBER));
		List<Calendar> dates = first(evaluator, 4);
		assertEquals(4, dates.size());
		assertEquals(Calendar.NOVEMBER, dates.get(0).get(Calendar.MONTH));
		assertEquals(2015, dates.get(0).get(Calendar.YEAR));
		assertEquals(Calendar.NOVEMBER, dates.get(1).get(Calendar.MONTH));
		assertEquals(2016, dates.get(1).get(Calendar.YEAR));
		assertEquals(Calendar.NOVEMBER, dates.get(3).get(Calendar.MONTH));
		assertEquals(2018, dates.get(3).get(Calendar.YEAR));
		ICalendarMarkingEvaluator contract = evaluator;
		assertEquals("november", contract.isMarked(day(2016, Calendar.NOVEMBER, 15)).getName());
		assertNull(contract.isMarked(day(2016, Calendar.JANUARY, 15)));

		CalendarMarkingEvaluator shifted = evaluator(
				mark(CalendarMarking.YEARLY_EVERY_NTH_MONTH, day(2015, Calendar.MARCH, 15), 0L, 2,
						Calendar.JANUARY));
		List<Calendar> shiftedDates = first(shifted, 6);
		assertEquals(Calendar.MARCH, shiftedDates.get(0).get(Calendar.MONTH));
		assertEquals(2015, shiftedDates.get(0).get(Calendar.YEAR));
		assertEquals(Calendar.JANUARY, shiftedDates.get(5).get(Calendar.MONTH));
		assertEquals(2016, shiftedDates.get(5).get(Calendar.YEAR));
		for (Calendar date : shiftedDates) {
			assertTrue(date.get(Calendar.YEAR) > 2015
					|| date.get(Calendar.MONTH) >= Calendar.MARCH);
		}
	}

	@Test
	void monthlyEveryNthDayAndWeekStayOnTheChosenSlot() {
		// backwards compatibility
		CalendarMarkingEvaluator days = evaluator(
				mark(CalendarMarking.MONTHLY_EVERY_NTH_DAY, day(2015, Calendar.JANUARY, 28),
						day(2015, Calendar.APRIL, 1).getTimeInMillis(), 5, 28));
		List<Calendar> dates = first(days, 6);
		assertEquals(3, dates.size());
		assertEquals(Calendar.JANUARY, dates.get(0).get(Calendar.MONTH));
		assertEquals(28, dates.get(0).get(Calendar.DAY_OF_MONTH));
		assertEquals(Calendar.FEBRUARY, dates.get(1).get(Calendar.MONTH));
		assertEquals(28, dates.get(1).get(Calendar.DAY_OF_MONTH));
		assertEquals(Calendar.MARCH, dates.get(2).get(Calendar.MONTH));
		ICalendarMarkingEvaluator contract = days;
		assertNull(contract.isMarked(day(2015, Calendar.FEBRUARY, 2)));
		assertEquals("slot", contract.isMarked(day(2015, Calendar.FEBRUARY, 28)).getName());

		CalendarMarkingEvaluator weeks = evaluator(
				mark(CalendarMarking.MONTHLY_EVERY_NTH_WEEK, day(2015, Calendar.JANUARY, 15), 0L, 2, 2));
		List<Calendar> weekDates = first(weeks, 3);
		assertEquals(3, weekDates.size());
		assertTrue(weekDates.get(2).after(weekDates.get(0)));
		int weekMatches = 0;
		int distinctMonths = 0;
		int lastMonth = -1;
		for (Calendar date : weekDates) {
			if (date.get(Calendar.WEEK_OF_MONTH) == 2) {
				weekMatches++;
			}
			int month = date.get(Calendar.YEAR) * 12 + date.get(Calendar.MONTH);
			if (month != lastMonth) {
				distinctMonths++;
				lastMonth = month;
			}
		}
		assertTrue(weekMatches >= 1);
		assertTrue(distinctMonths >= 2);
	}

	@Test
	void entryCountHonoursZeroNegativeAndOpenEndedRepeats() {
		// backwards compatibility
		Calendar start = day(2015, Calendar.JANUARY, 2);
		CalendarMarkingEvaluator open = evaluator(
				mark(CalendarMarking.DAILY, start, 0L, 1, 0));
		assertEquals(0, open.getAtLeastTheFirstNEntries(0).size());
		assertEquals(0, open.getAtLeastTheFirstNEntries(-1).size());
		assertEquals(1, open.getAtLeastTheFirstNEntries(1).size());
		List<Calendar> three = first(open, 3);
		assertEquals(3, three.size());
		assertEquals(2, three.get(0).get(Calendar.DAY_OF_MONTH));
		assertEquals(4, three.get(2).get(Calendar.DAY_OF_MONTH));

		CalendarMarking closed = mark(CalendarMarking.DAILY, start, start.getTimeInMillis(), 1, 0);
		assertEquals(1, evaluator(closed).getAtLeastTheFirstNEntries(5).size());

		CalendarMarking coerced = mark(CalendarMarking.DAILY, start, 0L, 1, 0);
		coerced.setRepeatEachNOccurence(null);
		List<Calendar> fromNull = first(evaluator(coerced), 2);
		assertEquals(2, fromNull.size());
		assertEquals(3, fromNull.get(1).get(Calendar.DAY_OF_MONTH));

		CalendarMarking zeroRepeat = mark(CalendarMarking.DAILY, start, 0L, 0, 0);
		List<Calendar> fromZero = first(evaluator(zeroRepeat), 2);
		assertEquals(2, fromZero.size());
		assertEquals(1, zeroRepeat.getRepeatEachNOccurence().intValue());
	}

	@Test
	void unknownRepeatIsSkippedAndTheCacheClears() {
		// backwards compatibility
		Calendar start = day(2015, Calendar.MAY, 4);
		CalendarMarking unknown = mark("fortnightly", start, 0L, 1, 0);
		CalendarMarkingEvaluator evaluator = evaluator(unknown);
		assertEquals(0, evaluator.getAtLeastTheFirstNEntries(3).size());
		ICalendarMarkingEvaluator contract = evaluator;
		assertThrows(NullPointerException.class, () -> contract.isMarked(start));

		CalendarMarkingEvaluator known = evaluator(mark(CalendarMarking.NEVER, start, 0L, 1, 0));
		ICalendarMarkingEvaluator knownContract = known;
		CalendarMarking first = knownContract.isMarked(start);
		assertSame(first, knownContract.isMarked(start));
		knownContract.changeMarkings(new CalendarMarkings());
		assertNull(knownContract.isMarked(start));
	}

	@Test
	void printWritesOneFiniteDateAndSkipsANullDate() {
		// backwards compatibility
		Calendar start = day(2015, Calendar.MAY, 4);
		CalendarMarkingEvaluator evaluator = evaluator(mark(CalendarMarking.NEVER, start, 0L, 1, 0));
		PrintStream original = System.out;
		ByteArrayOutputStream captured = new ByteArrayOutputStream();
		System.setOut(new PrintStream(captured));
		try {
			evaluator.print();
			int length = captured.size();
			evaluator.printDate(null);
			assertEquals(length, captured.size());
		} finally {
			System.setOut(original);
		}
		String printed = captured.toString().trim();
		assertTrue(printed.contains("2015"), printed);
		assertEquals(1, printed.split("\\R").length);
	}

	private static CalendarMarkingEvaluator evaluator(CalendarMarking marking) {
		CalendarMarkings markings = new CalendarMarkings();
		markings.addCalendarMarking(marking);
		return new CalendarMarkingEvaluator(markings);
	}

	private static CalendarMarking mark(String type, Calendar start, long end, int repeat, int first) {
		CalendarMarking marking = new CalendarMarking();
		marking.setName(type.equals(CalendarMarking.MONTHLY_EVERY_NTH_DAY) ? "slot" : "november");
		if (CalendarMarking.NEVER.equals(type)) {
			marking.setName("once");
		}
		marking.setColor("#cc0099");
		marking.setStartDate(start.getTimeInMillis());
		marking.setEndDate(Long.valueOf(end));
		marking.setRepeatType(type);
		marking.setRepeatEachNOccurence(Integer.valueOf(repeat));
		marking.setFirstOccurence(Integer.valueOf(first));
		return marking;
	}

	private static List<Calendar> first(CalendarMarkingEvaluator evaluator, int count) {
		Set<Calendar> dates = evaluator.getAtLeastTheFirstNEntries(count);
		return new ArrayList<Calendar>(dates);
	}

	private static Calendar day(int year, int month, int dayOfMonth) {
		return new GregorianCalendar(year, month, dayOfMonth, 12, 0, 0);
	}
}
