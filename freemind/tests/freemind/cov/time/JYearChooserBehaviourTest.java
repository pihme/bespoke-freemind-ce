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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.junit.jupiter.api.Test;

import accessories.plugins.time.JDayChooser;
import accessories.plugins.time.JYearChooser;

/** Backwards-compatible year limits and the day chooser they update. */
class JYearChooserBehaviourTest {

	@Test
	void startAndEndYearsClampInsideAtOverZeroAndNegative() {
		// backwards compatibility
		JYearChooser years = new JYearChooser();
		JDayChooser days = new JDayChooser();
		years.setDayChooser(days);
		int[] fired = new int[1];
		years.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (JYearChooser.YEAR_PROPERTY.equals(event.getPropertyName())) {
					fired[0] = ((Integer) event.getNewValue()).intValue();
				}
			}
		});

		years.setStartYear(-5);
		years.setEndYear(5);
		assertEquals(-5, years.getStartYear());
		assertEquals(5, years.getEndYear());

		years.setYear(1);
		assertEquals(1, years.getYear());
		assertEquals(1, days.getYear());
		assertEquals(1, fired[0]);

		years.setYear(0);
		assertEquals(0, years.getYear());
		years.setYear(-5);
		assertEquals(-5, years.getYear());
		years.setYear(-6);
		assertEquals(-5, years.getYear());
		assertEquals(6, days.getYear());
		years.setValue(5);
		assertEquals(5, years.getYear());
		years.setYear(6);
		assertEquals(5, years.getYear());
		assertEquals(5, fired[0]);
	}
}
