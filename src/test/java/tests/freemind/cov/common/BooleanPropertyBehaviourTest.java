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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import freemind.common.BooleanProperty;
import freemind.common.TextTranslator;

/** A boolean preference stores "true" or "false" and nothing else. */
@DisplayName("BooleanPropertyBehaviourTest")
class BooleanPropertyBehaviourTest {

	@Test
	void trueAndFalseRoundTripAndAnythingElseIsRejected() {
		// backwards compatibility
		BooleanProperty property = property();
		assertEquals("flag", property.getLabel());
		assertEquals("help", property.getDescription());
		assertEquals(BooleanProperty.FALSE_VALUE, property.getValue());

		property.setValue("true");
		assertEquals("true", property.getValue());
		property.setValue("FALSE");
		assertEquals("false", property.getValue());

		assertThrows(IllegalArgumentException.class, () -> property.setValue(""));
		assertThrows(IllegalArgumentException.class, () -> property.setValue(null));
		assertThrows(IllegalArgumentException.class, () -> property.setValue("yes"));
		assertEquals("false", property.getValue());
	}

	@Test
	void checkingTheBoxFiresThePropertyChange() {
		// backwards compatibility
		BooleanProperty property = property();
		String[] seen = { null };
		property.addPropertyChangeListener(event -> {
			assertSame(property, event.getSource());
			assertEquals("flag", event.getPropertyName());
			assertNull(event.getOldValue());
			seen[0] = String.valueOf(event.getNewValue());
		});

		property.setValue("TRUE");
		assertEquals("true", seen[0]);
		property.setValue("false");
		assertEquals("false", seen[0]);
	}

	@Test
	void layoutShowsTheTranslatedLabelAndHonoursEnabled() {
		// backwards compatibility
		BooleanProperty property = property();
		property.setEnabled(false);
		DefaultFormBuilder builder = builder();
		property.layout(builder, translator());

		JLabel label = find(builder.getPanel(), JLabel.class);
		JCheckBox box = find(builder.getPanel(), JCheckBox.class);
		assertEquals("t:flag", label.getText());
		assertEquals("t:help", label.getToolTipText());
		assertFalse(box.isEnabled());

		property.setEnabled(true);
		assertTrue(box.isEnabled());
		assertFalse(box.isSelected());
	}

	private static BooleanProperty property() {
		return new BooleanProperty("help", "flag");
	}

	private static DefaultFormBuilder builder() {
		return new DefaultFormBuilder(new FormLayout("pref, 3dlu, pref:grow", ""));
	}

	private static TextTranslator translator() {
		return key -> "t:" + key;
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
