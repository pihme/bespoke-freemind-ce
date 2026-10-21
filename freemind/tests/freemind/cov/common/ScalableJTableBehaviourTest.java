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

package tests.freemind.cov.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Properties;

import javax.swing.JTable;

import org.junit.jupiter.api.Test;

import freemind.common.ScalableJTable;
import freemind.main.FreeMind;
import freemind.main.HeadlessFreeMind;
import freemind.main.Resources;

/** Row height follows the scaling factor, and a bad factor stays at 100. */
class ScalableJTableBehaviourTest {

	@Test
	void rowHeightScalesWithTheFactorAndABadFactorStaysAtOneHundred() {
		// backwards compatibility
		new HeadlessFreeMind();
		Properties properties = Resources.getInstance().getProperties();
		String key = FreeMind.SCALING_FACTOR_PROPERTY;
		String previous = properties.getProperty(key);
		try {
			int base = new JTable().getRowHeight();
			properties.setProperty(key, "100");
			assertEquals(base, new ScalableJTable().getRowHeight());

			properties.setProperty(key, "200");
			assertEquals(base * 200 / 100, new ScalableJTable().getRowHeight());

			properties.setProperty(key, "50");
			assertEquals(base * 50 / 100, new ScalableJTable().getRowHeight());

			properties.setProperty(key, "nope");
			assertEquals(base, new ScalableJTable().getRowHeight());
		} finally {
			if (previous == null) {
				properties.remove(key);
			} else {
				properties.setProperty(key, previous);
			}
		}
	}
}
