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
import java.awt.GraphicsEnvironment;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.junit.jupiter.api.Test;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import freemind.common.FontProperty;

/** A font preference keeps a family the JDK actually has, else the first one. */
class FontPropertyBehaviourTest {

	@Test
	void aKnownFamilyRoundTripsAndAnUnknownFamilySelectsTheFirst() {
		// backwards compatibility
		String[] names = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		assertTrue(names.length > 1);
		FontProperty property = new FontProperty("help", "font", key -> key);
		assertEquals("font", property.getLabel());
		assertEquals("help", property.getDescription());
		assertEquals(names[0], property.getValue());

		property.setValue(names[1]);
		assertEquals(names[1], property.getValue());
		property.setValue(names[0]);
		assertEquals(names[0], property.getValue());
		property.setValue("not-a-real-font");
		assertEquals(names[0], property.getValue());
		property.setValue("");
		assertEquals(names[0], property.getValue());
	}

	@Test
	void selectingAnotherFamilyFiresAPropertyChange() {
		// backwards compatibility
		String[] names = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		assertTrue(names.length > 1);
		FontProperty property = new FontProperty("help", "font", key -> key);
		int[] fires = { 0 };
		property.addPropertyChangeListener(event -> {
			fires[0]++;
			assertEquals(names[1], event.getNewValue());
		});
		property.setValue(names[1]);
		assertEquals(1, fires[0]);
		assertEquals(names[1], property.getValue());
	}

	@Test
	void layoutShowsTheFamiliesAndHonoursEnabled() {
		// backwards compatibility
		String[] names = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		FontProperty property = new FontProperty("help", "font", key -> key);
		property.setValue(names[0]);
		property.setEnabled(false);
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 3dlu, pref:grow", ""));
		property.layout(builder, key -> "t:" + key);
		JLabel label = find(builder.getPanel(), JLabel.class);
		JComboBox<?> box = find(builder.getPanel(), JComboBox.class);
		assertEquals("t:font", label.getText());
		assertEquals("t:help", label.getToolTipText());
		assertEquals(names[0], box.getSelectedItem());
		assertFalse(box.isEnabled());
		property.setEnabled(true);
		assertTrue(box.isEnabled());
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
