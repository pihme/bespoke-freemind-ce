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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import accessories.plugins.time.JCalendar;
import accessories.plugins.time.JTripleCalendar;

/** Backwards-compatible twelve-month panel without a frame. */
@DisplayName("JTripleCalendarBehaviourTest")
class JTripleCalendarBehaviourTest {

	@Test
	void onScreenMonthChangesPositionAndOffScreenMonthDoesNot() {
		// backwards compatibility
		JTripleCalendar now = new JTripleCalendar(4, null);
		assertEquals(4, now.getCurrentMonthPosition());
		assertNotNull(now.getCalendar());
		assertNotNull(now.getDate());
		assertNotNull(now.getYearChooser());
		assertFalse(now.requestFocusInWindow());

		Calendar june = new GregorianCalendar(2030, Calendar.JUNE, 15, 12, 0, 0);
		JTripleCalendar triple = new JTripleCalendar(4, june);
		assertEquals(15, triple.getCalendar().get(Calendar.DAY_OF_MONTH));
		assertEquals(Calendar.JUNE, triple.getCalendar().get(Calendar.MONTH));
		assertEquals(2030, triple.getYearChooser().getYear());
		assertEquals(4, triple.getCurrentMonthPosition());
		assertInstanceOf(LineBorder.class, active(triple).getBorder());
		assertNull(inactive(triple).getBorder());

		triple.setCalendar(new GregorianCalendar(2030, Calendar.JUNE, 20, 12, 0, 0));
		assertEquals(20, triple.getCalendar().get(Calendar.DAY_OF_MONTH));
		assertEquals(4, triple.getCurrentMonthPosition());

		triple.setCalendar(new GregorianCalendar(2030, Calendar.JULY, 3, 12, 0, 0));
		assertEquals(Calendar.JULY, triple.getCalendar().get(Calendar.MONTH));
		assertEquals(5, triple.getCurrentMonthPosition());

		int position = triple.getCurrentMonthPosition();
		triple.setCalendar(new GregorianCalendar(1990, Calendar.JANUARY, 8, 12, 0, 0));
		assertEquals(Calendar.JANUARY, triple.getCalendar().get(Calendar.MONTH));
		assertEquals(1990, triple.getCalendar().get(Calendar.YEAR));
		assertEquals(position, triple.getCurrentMonthPosition());

		triple.setDate(new GregorianCalendar(1990, Calendar.JANUARY, 21, 12, 0, 0).getTime());
		assertEquals(21, triple.getDayChooser().getDay());
		assertThrows(NullPointerException.class, () -> triple.setCalendar(null));
	}

	@Test
	void propertyChangeFromAnotherPanelExchangesTheActiveMonth() {
		// backwards compatibility
		JTripleCalendar triple = new JTripleCalendar(4,
				new GregorianCalendar(2030, Calendar.JUNE, 15, 12, 0, 0));
		JTripleCalendar.JSwitchableCalendar other = inactive(triple);
		int before = triple.getCurrentMonthPosition();
		Calendar moved = (Calendar) other.getCalendar().clone();
		moved.set(Calendar.DAY_OF_MONTH, 2);
		triple.propertyChange(new PropertyChangeEvent(other, JCalendar.CALENDAR_PROPERTY, null, moved));
		assertEquals(other, active(triple));
		assertTrue(triple.getCurrentMonthPosition() != before);
		assertEquals(2, triple.getCalendar().get(Calendar.DAY_OF_MONTH));

		int position = triple.getCurrentMonthPosition();
		Calendar sameMonth = (Calendar) triple.getCalendar().clone();
		sameMonth.set(Calendar.DAY_OF_MONTH, 9);
		triple.propertyChange(new PropertyChangeEvent(active(triple), JCalendar.CALENDAR_PROPERTY, null,
				sameMonth));
		assertEquals(position, triple.getCurrentMonthPosition());
		assertEquals(9, triple.getCalendar().get(Calendar.DAY_OF_MONTH));

		triple.propertyChange(new PropertyChangeEvent(new JPanel(), JCalendar.CALENDAR_PROPERTY, null,
				sameMonth));
		assertEquals(position, triple.getCurrentMonthPosition());
		triple.propertyChange(new PropertyChangeEvent(other, "color", null, "blue"));
		assertEquals(9, triple.getCalendar().get(Calendar.DAY_OF_MONTH));

		JTripleCalendar.JSwitchableCalendar panel = new JTripleCalendar.JSwitchableCalendar();
		Calendar panelDate = new GregorianCalendar(2030, Calendar.APRIL, 11, 12, 0, 0);
		panel.setDate(panelDate);
		assertEquals(Calendar.APRIL, panel.getMonthChooser().getMonth());
		assertEquals(11, panel.getDayChooser().getDay());
		panelDate.set(Calendar.DAY_OF_MONTH, 12);
		panel.setDate(panelDate);
		assertEquals(12, panel.getDayChooser().getDay());
		assertEquals(Calendar.APRIL, panel.getMonthChooser().getMonth());
		panel.setEnabled(false);
		assertFalse(panel.getCalendarWidget().getSelectedDay().isFocusable());
		panel.setEnabled(true);
		assertTrue(panel.getCalendarWidget().getSelectedDay().isFocusable());
		assertNotNull(panel.getCalendar());
	}

	private static JTripleCalendar.JSwitchableCalendar active(JTripleCalendar triple) {
		for (Component component : triple.getComponents()) {
			if (component instanceof JTripleCalendar.JSwitchableCalendar panel
					&& panel.getBorder() instanceof LineBorder) {
				return panel;
			}
		}
		throw new AssertionError("missing active month");
	}

	private static JTripleCalendar.JSwitchableCalendar inactive(JTripleCalendar triple) {
		for (Component component : triple.getComponents()) {
			if (component instanceof JTripleCalendar.JSwitchableCalendar panel && panel.getBorder() == null) {
				return panel;
			}
		}
		throw new AssertionError("missing inactive month");
	}
}
