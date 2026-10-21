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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;

import org.junit.jupiter.api.Test;

import accessories.plugins.time.JDayChooser;
import accessories.plugins.time.JMonthChooser;
import accessories.plugins.time.JYearChooser;

/** Backwards-compatible month wrap, locale names, and spinner. */
class JMonthChooserBehaviourTest {

	@Test
	void monthClampsAndWrapsTheYear() {
		// backwards compatibility
		JMonthChooser months = new JMonthChooser(false);
		JDayChooser days = new JDayChooser();
		JYearChooser years = new JYearChooser();
		years.setYear(2015);
		months.setDayChooser(days);
		months.setYearChooser(years);
		assertFalse(months.hasSpinner());
		assertNull(months.getSpinner());
		assertEquals(12, ((JComboBox<?>) months.getComboBox()).getItemCount());

		months.setMonth(-1);
		assertEquals(0, months.getMonth());
		assertEquals(0, days.getMonth());
		months.setMonth(0);
		assertEquals(0, months.getMonth());
		months.setMonth(5);
		assertEquals(5, months.getMonth());
		months.setMonth(11);
		assertEquals(11, months.getMonth());
		months.setMonth(12);
		assertEquals(11, months.getMonth());

		months.changeMonth(true);
		assertEquals(0, months.getMonth());
		assertEquals(2016, years.getYear());
		months.changeMonth(false);
		assertEquals(11, months.getMonth());
		assertEquals(2015, years.getYear());

		JMonthChooser bare = new JMonthChooser();
		assertTrue(bare.hasSpinner());
		bare.setYearChooser(null);
		bare.setMonth(11);
		bare.changeMonth(true);
		assertEquals(0, bare.getMonth());
		bare.changeMonth(false);
		assertEquals(11, bare.getMonth());
	}

	@Test
	void spinnerStateDoesNotReplaceTheMonthAndLocaleRenamesIt() {
		// backwards compatibility
		JMonthChooser months = new JMonthChooser(true);
		JSpinner spinner = (JSpinner) months.getSpinner();
		Dimension preferred = spinner.getPreferredSize();
		assertEquals(new JTextField().getPreferredSize().height, preferred.height);

		int month = months.getMonth();
		int[] fired = new int[] { -1 };
		months.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (JMonthChooser.MONTH_PROPERTY.equals(event.getPropertyName())) {
					fired[0] = ((Integer) event.getNewValue()).intValue();
				}
			}
		});
		spinner.setValue(Integer.valueOf(4));
		months.stateChanged(new ChangeEvent(spinner));
		assertEquals(month, months.getMonth());
		assertEquals(4, fired[0]);

		months.setMonth(0);
		JComboBox<?> combo = (JComboBox<?>) months.getComboBox();
		months.itemStateChanged(new ItemEvent(combo, ItemEvent.ITEM_STATE_CHANGED,
				combo.getItemAt(0), ItemEvent.DESELECTED));
		assertEquals(0, months.getMonth());
		combo.setSelectedIndex(3);
		assertEquals(3, months.getMonth());

		months.setLocale(Locale.US);
		assertEquals(Locale.US, months.getLocale());
		assertEquals(new DateFormatSymbols(Locale.US).getMonths()[0], combo.getItemAt(0));
		months.setLocale(Locale.GERMANY);
		assertEquals(Locale.GERMANY, months.getLocale());
		assertEquals(new DateFormatSymbols(Locale.GERMANY).getMonths()[0], combo.getItemAt(0));

		Font font = new Font(Font.DIALOG, Font.PLAIN, 15);
		months.setFont(font);
		assertEquals(font, combo.getFont());
		months.updateUI();
		assertInstanceOf(EmptyBorder.class, spinner.getBorder());
		months.setEnabled(false);
		assertFalse(combo.isEnabled());
		assertFalse(spinner.isEnabled());
	}
}
