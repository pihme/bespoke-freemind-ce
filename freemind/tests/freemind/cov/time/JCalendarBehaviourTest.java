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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import accessories.plugins.time.JCalendar;
import accessories.plugins.time.JDayChooser;
import accessories.plugins.time.JMonthChooser;
import accessories.plugins.time.JYearChooser;

/** Backwards-compatible calendar wiring that does not open a frame. */
class JCalendarBehaviourTest {

	@Test
	void constructorsKeepTheDateLocaleAndSpinnerTheyAreGiven() {
		// backwards compatibility
		Date date = new GregorianCalendar(2015, Calendar.MARCH, 4, 12, 0, 0).getTime();
		JCalendar fromDate = new JCalendar(date);
		assertEquals(4, fromDate.getCalendar().get(Calendar.DAY_OF_MONTH));
		assertEquals(Calendar.MARCH, fromDate.getCalendar().get(Calendar.MONTH));
		assertTrue(fromDate.getMonthChooser().hasSpinner());
		assertTrue(fromDate.isWeekOfYearVisible());

		Calendar source = new GregorianCalendar(2016, Calendar.JULY, 9);
		JCalendar fromCalendar = new JCalendar(source);
		assertEquals(2016, fromCalendar.getCalendar().get(Calendar.YEAR));
		assertEquals(Calendar.JULY, fromCalendar.getMonthChooser().getMonth());

		JCalendar italian = new JCalendar(Locale.ITALY);
		assertEquals(Locale.ITALY, italian.getLocale());
		assertEquals(Locale.getDefault(), italian.getMonthChooser().getLocale());
		italian.setLocale(Locale.ITALY);
		assertEquals(Locale.ITALY, italian.getMonthChooser().getLocale());

		JCalendar both = new JCalendar(date, Locale.ITALY);
		assertEquals(4, both.getDayChooser().getDay());
		assertEquals(Locale.ITALY, both.getLocale());

		JCalendar noSpinner = new JCalendar(date, false);
		assertFalse(noSpinner.getMonthChooser().hasSpinner());
		assertEquals(2015, noSpinner.getYearChooser().getYear());

		JCalendar localeWithoutSpinner = new JCalendar(Locale.US, false);
		assertEquals(Locale.US, localeWithoutSpinner.getLocale());
		assertFalse(localeWithoutSpinner.getMonthChooser().hasSpinner());

		JCalendar spinnerOff = new JCalendar(false);
		assertFalse(spinnerOff.getMonthChooser().hasSpinner());
		assertTrue(spinnerOff.isEnabled());
	}

	@Test
	void propertyChangesReportTheCalendarWithoutReentering() {
		// backwards compatibility
		JCalendar calendar = new JCalendar(new GregorianCalendar(2015, Calendar.MARCH, 4).getTime(),
				Locale.US, false, false);
		Calendar[] fired = new Calendar[1];
		int[] calls = new int[1];
		calendar.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (JCalendar.CALENDAR_PROPERTY.equals(event.getPropertyName())) {
					calls[0]++;
					fired[0] = (Calendar) event.getNewValue();
					if (calls[0] == 1) {
						calendar.propertyChange(new PropertyChangeEvent(calendar,
								JCalendar.CALENDAR_PROPERTY, null, event.getNewValue()));
					}
				}
			}
		});
		Calendar day = new GregorianCalendar(2015, Calendar.MARCH, 18);
		calendar.propertyChange(new PropertyChangeEvent(calendar.getDayChooser(),
				JDayChooser.DAY_PROPERTY, null, day));
		assertEquals(1, calls[0]);
		assertSame(day, fired[0]);
		assertEquals(4, calendar.getCalendar().get(Calendar.DAY_OF_MONTH));

		calendar.propertyChange(new PropertyChangeEvent(calendar.getMonthChooser(),
				JMonthChooser.MONTH_PROPERTY, Integer.valueOf(2), Integer.valueOf(5)));
		assertEquals(Calendar.JUNE, fired[0].get(Calendar.MONTH));
		assertEquals(Calendar.MARCH, calendar.getCalendar().get(Calendar.MONTH));

		calendar.propertyChange(new PropertyChangeEvent(calendar.getYearChooser(),
				JYearChooser.YEAR_PROPERTY, Integer.valueOf(2015), Integer.valueOf(2018)));
		assertEquals(2018, fired[0].get(Calendar.YEAR));

		Date replacement = new GregorianCalendar(2014, Calendar.JANUARY, 2).getTime();
		calendar.propertyChange(new PropertyChangeEvent(calendar, JCalendar.DATE_PROPERTY,
				calendar.getDate(), replacement));
		assertEquals(replacement.getTime(), fired[0].getTimeInMillis());
		assertEquals(2015, calendar.getDate().getTime() > 0 ? calendar.getCalendar().get(Calendar.YEAR) : 0);
		assertEquals(2015, calendar.getCalendar().get(Calendar.YEAR));

		int seen = calls[0];
		calendar.propertyChange(new PropertyChangeEvent(calendar, "color", null, Color.RED));
		assertEquals(seen, calls[0]);
	}

	@Test
	void nullDateThrowsAndDayLimitsResetOutsideZeroToFour() {
		// backwards compatibility
		JCalendar calendar = new JCalendar(new GregorianCalendar(2015, Calendar.JUNE, 15).getTime(),
				Locale.US, true, true);
		assertThrows(NullPointerException.class, () -> calendar.setCalendar(null));
		assertThrows(NullPointerException.class, () -> calendar.setDate(null));

		calendar.setMaxDayCharacters(2);
		assertEquals(2, calendar.getMaxDayCharacters());
		calendar.setMaxDayCharacters(2);
		assertEquals(2, calendar.getMaxDayCharacters());
		calendar.setMaxDayCharacters(-1);
		assertEquals(0, calendar.getMaxDayCharacters());
		calendar.setMaxDayCharacters(0);
		assertEquals(0, calendar.getMaxDayCharacters());
		calendar.setMaxDayCharacters(1);
		assertEquals(1, calendar.getMaxDayCharacters());
		calendar.setMaxDayCharacters(4);
		assertEquals(4, calendar.getMaxDayCharacters());
		calendar.setMaxDayCharacters(5);
		assertEquals(0, calendar.getMaxDayCharacters());

		calendar.setSelectableDateRange(null, null);
		assertEquals(1, year(calendar.getMinSelectableDate()));
		assertEquals(9999, year(calendar.getMaxSelectableDate()));
		Date later = new GregorianCalendar(2018, Calendar.JANUARY, 1).getTime();
		Date earlier = new GregorianCalendar(2010, Calendar.JANUARY, 1).getTime();
		calendar.setSelectableDateRange(later, earlier);
		assertEquals(1, year(calendar.getMinSelectableDate()));
		assertEquals(9999, year(calendar.getMaxSelectableDate()));
		calendar.setMinSelectableDate(null);
		calendar.setMaxSelectableDate(null);
		assertEquals(1, year(calendar.getMinSelectableDate()));
		assertEquals(9999, year(calendar.getMaxSelectableDate()));

		calendar.setEnabled(false);
		assertFalse(calendar.getMonthChooser().isEnabled());
		calendar.setEnabled(true);
		assertTrue(calendar.isEnabled());
		Font font = new Font(Font.DIALOG, Font.PLAIN, 13);
		calendar.setFont(font);
		assertEquals(font, calendar.getFont());
		calendar.setForeground(Color.MAGENTA);
		assertEquals(Color.MAGENTA, calendar.getForeground());
		calendar.setBackground(Color.YELLOW);
		assertEquals(Color.YELLOW, calendar.getDayChooser().getBackground());
		calendar.setWeekOfYearVisible(false);
		assertFalse(calendar.isWeekOfYearVisible());
		calendar.setDecorationBackgroundVisible(false);
		assertFalse(calendar.isDecorationBackgroundVisible());
		calendar.setDecorationBordersVisible(true);
		assertTrue(calendar.isDecorationBordersVisible());
		calendar.setDecorationBackgroundColor(Color.CYAN);
		assertEquals(Color.CYAN, calendar.getDecorationBackgroundColor());
		calendar.setSundayForeground(Color.RED);
		assertEquals(Color.RED, calendar.getSundayForeground());
		calendar.setWeekdayForeground(Color.BLUE);
		assertEquals(Color.BLUE, calendar.getWeekdayForeground());
	}

	private static int year(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.YEAR);
	}
}
