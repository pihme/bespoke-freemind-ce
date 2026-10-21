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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import javax.swing.Action;
import javax.swing.JTextField;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import accessories.plugins.time.JDayChooser;
import accessories.plugins.time.JTripleCalendar;
import accessories.plugins.time.TimeManagement;
import accessories.plugins.time.TimeManagementOrganizer;
import freemind.common.XmlBindingTools;
import freemind.controller.actions.generated.instance.CalendarMarking;
import freemind.controller.actions.generated.instance.CalendarMarkings;
import freemind.main.FreeMindCommon;
import freemind.main.Resources;
import freemind.modes.MindMapNode;
import freemind.modes.common.plugins.ReminderHookBase;
import tests.freemind.cov.MapFixture;

/** Backwards-compatible time window actions without showing the dialog. */
@DisplayName("TimeManagementBehaviourTest")
class TimeManagementBehaviourTest {

	private MapFixture.Opened opened;
	private TimeManagement management;
	private MindMapNode child;

	@BeforeEach
	void openMap() throws Exception {
		opened = MapFixture.open("Child");
		child = opened.child("Child");
		opened.mode.select(child, List.of(child));
		management = new TimeManagement();
		management.setController(opened.mode);
		management.setProperties(new Properties());
		assertThrows(HeadlessException.class, management::startupMapHook);
		assertSame(opened.mode, management.getMindMapController());
	}

	@AfterEach
	void release() throws Exception {
		opened.mode.getController().getMapModuleManager().removeListener(management);
		Field current = TimeManagement.class.getDeclaredField("sCurrentlyOpenTimeManagement");
		current.setAccessible(true);
		if (current.get(null) == management) {
			current.set(null, null);
		}
		clearHook(opened.root);
		clearHook(child);
		for (int i = 0; i < opened.root.getChildCount(); i++) {
			clearHook((MindMapNode) opened.root.getChildAt(i));
		}
	}

	@Test
	void timePanelPadsTheMinuteAndABadMinuteKeepsThePreviousOne() throws Exception {
		// backwards compatibility
		useCalendar(new GregorianCalendar(2030, Calendar.JUNE, 15, 8, 15, 30));
		JTextField minute = text("minuteField");
		JTextField hour = text("hourField");
		assertEquals(2, minute.getText().length());
		int shown = Integer.parseInt(minute.getText());
		int now = Calendar.getInstance().get(Calendar.MINUTE);
		assertTrue(shown == now || shown == (now + 59) % 60 || shown == (now + 1) % 60);
		assertTrue(shown >= 0 && shown <= 59);

		hour.setText("9");
		minute.setText("no");
		Calendar parsed = calendarOf(calendarDate());
		assertEquals(9, parsed.get(Calendar.HOUR_OF_DAY));
		assertEquals(0, parsed.get(Calendar.MINUTE));
		assertEquals(0, parsed.get(Calendar.SECOND));
		assertEquals(15, parsed.get(Calendar.DAY_OF_MONTH));

		hour.setText("0");
		minute.setText("0");
		parsed = calendarOf(calendarDate());
		assertEquals(0, parsed.get(Calendar.HOUR_OF_DAY));
		assertEquals(0, parsed.get(Calendar.MINUTE));
		assertEquals(0, parsed.get(Calendar.SECOND));
		assertEquals(15, parsed.get(Calendar.DAY_OF_MONTH));

		hour.setText("-1");
		minute.setText("0");
		parsed = calendarOf(calendarDate());
		assertEquals(14, parsed.get(Calendar.DAY_OF_MONTH));
		assertEquals(23, parsed.get(Calendar.HOUR_OF_DAY));
	}

	@Test
	void todayAppendsADateAndTheObserversLeaveTheNode() throws Exception {
		// backwards compatibility
		useCalendar(new GregorianCalendar(2030, Calendar.JUNE, 15, 8, 15, 30));
		Calendar today = Calendar.getInstance();
		action("TodayAction").actionPerformed(event());
		Calendar shown = managementCalendar();
		assertEquals(today.get(Calendar.YEAR), shown.get(Calendar.YEAR));
		assertEquals(today.get(Calendar.MONTH), shown.get(Calendar.MONTH));
		assertEquals(today.get(Calendar.DAY_OF_MONTH), shown.get(Calendar.DAY_OF_MONTH));

		useCalendar(new GregorianCalendar(2030, Calendar.JUNE, 15, 8, 15, 30));
		text("hourField").setText("12");
		text("minuteField").setText("0");
		int children = opened.root.getChildCount();
		action("AppendDateToSiblingAction").actionPerformed(event());
		assertEquals(children + 1, opened.root.getChildCount());
		MindMapNode sibling = (MindMapNode) opened.root.getChildAt(opened.root.getChildCount() - 1);
		assertTrue(sibling.getText().matches(".*\\d.*"), sibling.getText());
		assertEquals("Child", child.getText());

		opened.mode.select(child, List.of(child));
		int kids = child.getChildCount();
		action("AppendDateToChildAction").actionPerformed(event());
		assertEquals(kids + 1, child.getChildCount());
		assertTrue(((MindMapNode) child.getChildAt(0)).getText().matches(".*\\d.*"));

		opened.mode.select(child, List.of(child));
		action("AppendDateAction").actionPerformed(event());
		assertTrue(child.getText().startsWith("Child "));
		assertTrue(child.getText().matches(".*\\d.*"));

		String text = child.getText();
		management.propertyChange(new PropertyChangeEvent(management, JDayChooser.DAY_PROPERTY, null,
				new GregorianCalendar()));
		management.afterMapClose(null, null);
		management.afterMapModuleChange(null, null, null, null);
		management.numberOfOpenMapInformation(1, 0);
		assertEquals(text, child.getText());
		assertTrue(management.isMapModuleChangeAllowed(null, null, null, null));
		assertSame(opened.mode, management.getMindMapController());
	}

	@Test
	void markingsAndRemindersChangeTheNodeUntilTheDialogWouldOpen() throws Exception {
		// backwards compatibility
		useCalendar(new GregorianCalendar(2030, Calendar.JUNE, 15, 12, 0, 0));
		text("hourField").setText("12");
		text("minuteField").setText("0");
		String key = FreeMindCommon.TIME_MANAGEMENT_MARKING_XML;
		String saved = Resources.getInstance().getProperty(key);
		try {
			CalendarMarkings markings = new CalendarMarkings();
			markings.addCalendarMarking(marking("remove-me", managementCalendar().getTimeInMillis()));
			markings.addCalendarMarking(marking("keep-me",
					new GregorianCalendar(2031, Calendar.JANUARY, 1, 12, 0, 0).getTimeInMillis()));
			String xml = XmlBindingTools.getInstance().marshall(markings);
			Resources.getInstance().getProperties().setProperty(key, xml);
			action("RemoveMarkAction").actionPerformed(event());
			String stored = opened.frame.getProperty(key);
			assertTrue(stored.contains("keep-me"), stored);
			assertFalse(stored.contains("remove-me"), stored);

			int windows = Window.getWindows().length;
			HeadlessException thrown = assertThrows(HeadlessException.class,
					() -> action("AddMarkAction").actionPerformed(event()));
			assertNotNull(thrown);
			assertEquals(windows, Window.getWindows().length);
			assertEquals(stored, opened.frame.getProperty(key));
		} finally {
			String restore = saved == null ? "<calendar_markings/>" : saved;
			Resources.getInstance().getProperties().setProperty(key, restore);
			opened.mode.getController().setProperty(key, restore);
		}

		action("RemindAction").actionPerformed(event());
		ReminderHookBase hook = TimeManagementOrganizer.getHook(child);
		assertNotNull(hook);
		Calendar reminded = calendarOf(new Date(hook.getRemindUserAt()));
		assertEquals(2030, reminded.get(Calendar.YEAR));
		assertEquals(Calendar.JUNE, reminded.get(Calendar.MONTH));
		assertEquals(15, reminded.get(Calendar.DAY_OF_MONTH));
		assertNotNull(child.getToolTip().get(hook.getName()));
		long remindAt = hook.getRemindUserAt();
		assertThrows(HeadlessException.class, () -> management.actionPerformed(event()));
		assertSame(hook, TimeManagementOrganizer.getHook(child));
		assertEquals(remindAt, hook.getRemindUserAt());

		action("RemoveReminders").actionPerformed(event());
		assertNull(TimeManagementOrganizer.getHook(child));
	}

	@Test
	void closeAndMapChangeStopAtTheMissingDialog() throws Exception {
		// backwards compatibility
		useCalendar(new GregorianCalendar(2030, Calendar.JUNE, 15, 12, 0, 0));
		assertThrows(NullPointerException.class, () -> action("CloseAction").actionPerformed(event()));
		Field lastDate = TimeManagement.class.getDeclaredField("lastDate");
		lastDate.setAccessible(true);
		Calendar saved = (Calendar) lastDate.get(null);
		assertEquals(2030, saved.get(Calendar.YEAR));
		assertEquals(Calendar.JUNE, saved.get(Calendar.MONTH));
		assertEquals(15, saved.get(Calendar.DAY_OF_MONTH));
		Field current = TimeManagement.class.getDeclaredField("sCurrentlyOpenTimeManagement");
		current.setAccessible(true);
		assertSame(management, current.get(null));

		assertThrows(NullPointerException.class,
				() -> management.beforeMapModuleChange(null, null, null, null));
		assertSame(management, current.get(null));
	}

	private void useCalendar(Calendar calendar) throws Exception {
		Field field = TimeManagement.class.getDeclaredField("calendar");
		field.setAccessible(true);
		field.set(management, new JTripleCalendar(4, calendar));
		Method panel = TimeManagement.class.getDeclaredMethod("getTimePanel");
		panel.setAccessible(true);
		panel.invoke(management);
	}

	private Calendar managementCalendar() throws Exception {
		Method method = TimeManagement.class.getDeclaredMethod("getCalendar");
		method.setAccessible(true);
		return (Calendar) method.invoke(management);
	}

	private Date calendarDate() throws Exception {
		Method method = TimeManagement.class.getDeclaredMethod("getCalendarDate");
		method.setAccessible(true);
		return (Date) method.invoke(management);
	}

	private JTextField text(String name) throws Exception {
		Field field = TimeManagement.class.getDeclaredField(name);
		field.setAccessible(true);
		return (JTextField) field.get(management);
	}

	private Action action(String name) throws Exception {
		Class<?> type = Class.forName("accessories.plugins.time.TimeManagement$" + name);
		Constructor<?> constructor = type.getDeclaredConstructor(TimeManagement.class);
		constructor.setAccessible(true);
		return (Action) constructor.newInstance(management);
	}

	private static ActionEvent event() {
		return new ActionEvent(new Object(), ActionEvent.ACTION_PERFORMED, "time");
	}

	private static Calendar calendarOf(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar;
	}

	private static CalendarMarking marking(String name, long start) {
		CalendarMarking marking = new CalendarMarking();
		marking.setName(name);
		marking.setColor("#cc0099");
		marking.setStartDate(start);
		marking.setEndDate(Long.valueOf(0L));
		marking.setRepeatType(CalendarMarking.NEVER);
		marking.setRepeatEachNOccurence(Integer.valueOf(1));
		marking.setFirstOccurence(Integer.valueOf(0));
		return marking;
	}

	private static void clearHook(MindMapNode node) {
		ReminderHookBase hook = TimeManagementOrganizer.getHook(node);
		if (hook != null) {
			node.removeHook(hook);
		}
	}
}
