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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JLabel;
import javax.swing.JSpinner;

import org.junit.jupiter.api.Test;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import freemind.common.NumberProperty;

/** A number preference keeps an in-range step and falls back to the minimum. */
class NumberPropertyBehaviourTest {

	@Test
	void anInRangeStepIsStoredAndReadBackAsTextAndInt() {
		// backwards compatibility
		NumberProperty property = property();
		assertEquals("count", property.getLabel());
		assertEquals("help", property.getDescription());
		assertEquals("0", property.getValue());
		assertEquals(0, property.getIntValue());

		property.setValue("4");
		assertEquals("4", property.getValue());
		assertEquals(4, property.getIntValue());
		property.setValue("10");
		assertEquals(10, property.getIntValue());
	}

	@Test
	void valuesOutsideTheStepOrRangeFallBackToTheMinimum() {
		// backwards compatibility
		NumberProperty property = property();
		String[] rejected = { "11", "-1", "3", "", "nope", null };
		for (String bad : rejected) {
			property.setValue("6");
			property.setValue(bad);
			assertEquals(0, property.getIntValue(), String.valueOf(bad));
			assertEquals("0", property.getValue());
		}
	}

	@Test
	void changingTheSpinnerFiresAPropertyChange() {
		// backwards compatibility
		NumberProperty property = property();
		int[] fires = { 0 };
		property.addPropertyChangeListener(event -> {
			fires[0]++;
			assertEquals("8", event.getNewValue());
		});
		property.setValue("8");
		assertEquals(1, fires[0]);
		assertEquals(8, property.getIntValue());
	}

	@Test
	void layoutShowsTheSpinnerAndHonoursEnabled() {
		// backwards compatibility
		NumberProperty property = property();
		property.setValue("2");
		property.setEnabled(false);
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 3dlu, pref:grow", ""));
		property.layout(builder, key -> "t:" + key);
		JSpinner spinner = find(builder.getPanel(), JSpinner.class);
		JLabel label = find(builder.getPanel(), JLabel.class);

		assertEquals("t:count", label.getText());
		assertEquals("t:help", label.getToolTipText());
		assertEquals(Integer.valueOf(2), spinner.getValue());
		assertFalse(spinner.isEnabled());
		property.setEnabled(true);
		assertTrue(spinner.isEnabled());
	}

	private static NumberProperty property() {
		return new NumberProperty("help", "count", 0, 10, 2);
	}

	private static <T extends Component> T find(Component root, Class<T> type) {
		if (type.isInstance(root)) {
			return type.cast(root);
		}
		if (root instanceof Container) {
			for (Component child : ((Container) root).getComponents()) {
				T found = find(child, type);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}
}
