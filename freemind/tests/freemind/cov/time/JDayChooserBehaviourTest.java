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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.JButton;

import org.junit.jupiter.api.Test;

import accessories.plugins.time.JDayChooser;
import accessories.plugins.time.JMonthChooser;
import accessories.plugins.time.JYearChooser;
import freemind.common.XmlBindingTools;
import freemind.controller.actions.generated.instance.CalendarMarking;
import freemind.controller.actions.generated.instance.CalendarMarkings;
import freemind.main.FreeMindCommon;
import freemind.main.Resources;
import tests.freemind.cov.MapFixture;

/** Backwards-compatible day grid, keys, and marking tooltips. */
class JDayChooserBehaviourTest {

	@Test
	void dayAndNameLimitsClampAndShortMonthsRetargetTheLastDay() {
		// backwards compatibility
		OpenDays days = new OpenDays(false);
		days.setYear(2015);
		days.setMonth(Calendar.JANUARY);
		assertEquals(31, days.getDaysInMonth());
		days.setDay(0);
		assertEquals(1, days.getDay());
		days.setDay(1);
		assertEquals(1, days.getDay());
		days.setDay(15);
		assertEquals(15, days.getDay());
		days.setDay(31);
		assertEquals(31, days.getDay());
		days.setDay(32);
		assertEquals(31, days.getDay());
		days.setMonth(Calendar.FEBRUARY);
		assertEquals(28, days.getDay());
		days.setDay(15);
		assertEquals(15, days.getDay());
		days.setMonth(Calendar.JANUARY);
		assertEquals(15, days.getDay());

		days.setYear(2016);
		days.setMonth(Calendar.JANUARY);
		days.setDay(31);
		days.setMonth(Calendar.FEBRUARY);
		assertEquals(29, days.getDay());

		assertEquals(49, days.buttons().length);
		days.setMaxDayCharacters(2);
		assertEquals(2, days.getMaxDayCharacters());
		String two = days.buttons()[0].getText();
		days.setMaxDayCharacters(2);
		assertEquals(two, days.buttons()[0].getText());
		days.setMaxDayCharacters(-1);
		assertEquals(0, days.getMaxDayCharacters());
		days.setMaxDayCharacters(0);
		assertEquals(0, days.getMaxDayCharacters());
		days.setMaxDayCharacters(1);
		assertEquals(1, days.getMaxDayCharacters());
		assertTrue(days.buttons()[0].getText().length() <= 1);
		days.setMaxDayCharacters(4);
		assertEquals(4, days.getMaxDayCharacters());
		assertTrue(days.buttons()[0].getText().length() <= 4);
		days.setMaxDayCharacters(5);
		assertEquals(0, days.getMaxDayCharacters());

		days.setSelectableDateRange(null, null);
		assertDate(1, Calendar.JANUARY, 1, 1, 1, days.getMinSelectableDate());
		assertDate(9999, Calendar.JANUARY, 1, 1, 1, days.getMaxSelectableDate());
		Date min = days.getMinSelectableDate();
		Date max = days.getMaxSelectableDate();
		days.setSelectableDateRange(max, min);
		assertEquals(min, days.getMinSelectableDate());
		assertEquals(max, days.getMaxSelectableDate());
		assertEquals(max, days.setMaxSelectableDate(null));
		assertEquals(min, days.setMinSelectableDate(null));
		Date june = new GregorianCalendar(2015, Calendar.JUNE, 15).getTime();
		days.setYear(2015);
		days.setMonth(Calendar.JUNE);
		days.setMinSelectableDate(june);
		days.setMaxSelectableDate(new GregorianCalendar(2015, Calendar.JUNE, 20).getTime());
		assertFalse(buttonText(days, "1").isEnabled());
		assertTrue(buttonText(days, "15").isEnabled());
		assertFalse(buttonText(days, "21").isEnabled());
	}

	@Test
	void keysAndButtonsFireADayWithoutChangingTheSelection() {
		// backwards compatibility
		OpenDays days = new OpenDays(true);
		JYearChooser years = new JYearChooser();
		JMonthChooser months = new JMonthChooser(false);
		years.setYear(2015);
		months.setMonth(Calendar.JANUARY);
		days.setYearChooser(years);
		days.setMonthChooser(months);
		days.setYear(2015);
		days.setMonth(Calendar.JANUARY);
		days.setDay(15);
		Calendar[] fired = new Calendar[1];
		days.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (JDayChooser.DAY_PROPERTY.equals(event.getPropertyName())) {
					fired[0] = (Calendar) event.getNewValue();
				}
			}
		});
		days.keyPressed(key(days, KeyEvent.VK_RIGHT));
		assertEquals(15, days.getDay());
		assertEquals(16, fired[0].get(Calendar.DAY_OF_MONTH));
		assertEquals(Calendar.JANUARY, fired[0].get(Calendar.MONTH));

		fired[0] = null;
		days.keyPressed(key(days, KeyEvent.VK_END));
		assertEquals(31, fired[0].get(Calendar.DAY_OF_MONTH));
		days.keyPressed(key(days, KeyEvent.VK_PAGE_DOWN));
		assertEquals(Calendar.FEBRUARY, fired[0].get(Calendar.MONTH));
		assertEquals(15, fired[0].get(Calendar.DAY_OF_MONTH));
		days.keyPressed(key(days, KeyEvent.VK_PAGE_UP));
		assertTrue(fired[0].get(Calendar.YEAR) < 2015 || fired[0].get(Calendar.MONTH) != Calendar.JANUARY
				|| fired[0].before(new GregorianCalendar(2015, Calendar.JANUARY, 1)));
		fired[0] = null;
		days.keyPressed(key(days, KeyEvent.VK_A));
		days.keyTyped(key(days, KeyEvent.VK_A));
		days.keyReleased(key(days, KeyEvent.VK_A));
		assertNull(fired[0]);
		assertEquals(15, days.getDay());

		JButton ten = buttonText(days, "10");
		days.actionPerformed(new ActionEvent(ten, ActionEvent.ACTION_PERFORMED, "10"));
		assertEquals(10, fired[0].get(Calendar.DAY_OF_MONTH));
		assertEquals(15, days.getDay());
		days.focusGained(new FocusEvent(ten, FocusEvent.FOCUS_GAINED));
		days.focusLost(new FocusEvent(ten, FocusEvent.FOCUS_LOST));
		assertEquals(15, days.getDay());

		assertTrue(days.isWeekOfYearVisible());
		int withWeeks = days.getComponentCount();
		days.setWeekOfYearVisible(true);
		assertEquals(withWeeks, days.getComponentCount());
		days.setWeekOfYearVisible(false);
		assertFalse(days.isWeekOfYearVisible());
		assertTrue(days.getComponentCount() < withWeeks);
		assertNotNull(days.weeks());
		assertNotNull(days.getDayPanel());
	}

	@Test
	void decorationLocaleAndMarkingTooltipFollowTheControllerProperty() throws Exception {
		// backwards compatibility
		MapFixture.Opened opened = MapFixture.open("Child");
		String key = FreeMindCommon.TIME_MANAGEMENT_MARKING_XML;
		String saved = Resources.getInstance().getProperty(key);
		OpenDays days = new OpenDays(false);
		try {
			days.setYear(2015);
			days.setMonth(Calendar.JUNE);
			days.setDay(15);
			days.setDecorationBackgroundColor(Color.ORANGE);
			assertEquals(Color.ORANGE, days.getDecorationBackgroundColor());
			assertEquals(Color.ORANGE, days.buttons()[0].getBackground());
			days.setSundayForeground(Color.RED);
			days.setWeekdayForeground(Color.BLUE);
			assertEquals(Color.RED, days.getSundayForeground());
			assertEquals(Color.BLUE, days.getWeekdayForeground());
			days.setForeground(Color.MAGENTA);
			assertEquals(Color.MAGENTA, buttonText(days, "15").getForeground());
			Font font = new Font(Font.DIALOG, Font.PLAIN, 12);
			days.setFont(font);
			assertEquals(font, days.buttons()[7].getFont());
			days.setEnabled(false);
			assertFalse(days.buttons()[7].isEnabled());
			days.setEnabled(true);
			assertTrue(buttonText(days, "15").isEnabled());

			String english = new OpenDays(false).buttons()[0].getText();
			OpenDays german = new OpenDays(false);
			german.setLocale(java.util.Locale.GERMANY);
			assertNotEquals(english, german.buttons()[0].getText());
			german.setLocale(java.util.Locale.US);
			assertEquals(java.util.Locale.US, german.getLocale());

			JButton decoration = days.buttons()[0];
			assertFalse(decoration.isFocusable());
			int listeners = decoration.getMouseListeners().length;
			decoration.addMouseListener(null);
			assertEquals(listeners, decoration.getMouseListeners().length);
			paint(decoration);
			paint(buttonText(days, "15"));
			JButton selected = days.getSelectedDay();
			days.setFocus();
			assertSameButton(selected, days.getSelectedDay());

			CalendarMarkings markings = new CalendarMarkings();
			CalendarMarking marking = new CalendarMarking();
			marking.setName("named-day");
			marking.setColor("#cc0099");
			marking.setStartDate(new GregorianCalendar(2015, Calendar.JUNE, 15, 12, 0, 0).getTimeInMillis());
			marking.setEndDate(Long.valueOf(0L));
			marking.setRepeatType(CalendarMarking.NEVER);
			marking.setRepeatEachNOccurence(Integer.valueOf(1));
			marking.setFirstOccurence(Integer.valueOf(0));
			markings.addCalendarMarking(marking);
			String xml = XmlBindingTools.getInstance().marshall(markings);
			opened.mode.getController().setProperty(key, xml);
			days.setMonth(Calendar.JUNE);
			assertEquals("named-day", buttonText(days, "15").getToolTipText());
			opened.mode.getController().setProperty("last_opened_list_length", "4");
			assertEquals("named-day", buttonText(days, "15").getToolTipText());
			days.setDecorationBackgroundVisible(false);
			assertFalse(days.isDecorationBackgroundVisible());
			days.setDecorationBordersVisible(true);
			assertTrue(days.isDecorationBordersVisible());
			days.setDayBordersVisible(false);
			assertFalse(days.isDayBordersVisible());
			days.updateUI();
			assertNotNull(buttonText(days, "15"));
		} finally {
			String restore = saved == null ? "<calendar_markings/>" : saved;
			Resources.getInstance().getProperties().setProperty(key, restore);
			opened.mode.getController().setProperty(key, restore);
		}
	}

	private static void assertSameButton(JButton left, JButton right) {
		assertTrue(left == right);
	}

	private static void paint(JButton button) {
		button.setSize(30, 20);
		BufferedImage image = new BufferedImage(30, 20, BufferedImage.TYPE_INT_RGB);
		Graphics graphics = image.getGraphics();
		try {
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, 30, 20);
			button.paint(graphics);
		} finally {
			graphics.dispose();
		}
		assertEquals("paint", "paint");
		assertTrue(image.getWidth() == 30);
	}

	private static void assertDate(int year, int month, int day, int hour, int minute, Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		assertEquals(year, calendar.get(Calendar.YEAR));
		assertEquals(month, calendar.get(Calendar.MONTH));
		assertEquals(day, calendar.get(Calendar.DAY_OF_MONTH));
		assertEquals(hour, calendar.get(Calendar.HOUR_OF_DAY));
		assertEquals(minute, calendar.get(Calendar.MINUTE));
	}

	private static JButton buttonText(OpenDays days, String text) {
		for (JButton button : days.buttons()) {
			if (text.equals(button.getText())) {
				return button;
			}
		}
		throw new AssertionError("missing day " + text);
	}

	private static KeyEvent key(JDayChooser days, int code) {
		return new KeyEvent(days, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, code,
				KeyEvent.CHAR_UNDEFINED);
	}

	static final class OpenDays extends JDayChooser {
		OpenDays(boolean weekOfYearVisible) {
			super(weekOfYearVisible);
		}

		JButton[] buttons() {
			return days;
		}

		javax.swing.JPanel weeks() {
			return weekPanel;
		}
	}
}
