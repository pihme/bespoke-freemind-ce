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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JLabel;
import javax.swing.JTextField;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import freemind.common.StringProperty;

/** A text preference keeps the characters that were stored, including none. */
@DisplayName("StringPropertyBehaviourTest")
class StringPropertyBehaviourTest {

	@Test
	void textRoundTripsIncludingAnEmptyStringAndSelectsItAll() {
		// backwards compatibility
		StringProperty property = new StringProperty("help", "title");
		assertEquals("title", property.getLabel());
		assertEquals("help", property.getDescription());
		assertEquals("", property.getValue());

		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 3dlu, pref:grow", ""));
		property.layout(builder, key -> "t:" + key);
		JTextField field = find(builder.getPanel(), JTextField.class);
		JLabel label = find(builder.getPanel(), JLabel.class);
		assertEquals("t:title", label.getText());
		assertEquals("t:help", label.getToolTipText());

		property.setValue("mind/map");
		assertEquals("mind/map", property.getValue());
		assertEquals("mind/map", field.getText());
		assertEquals(0, field.getSelectionStart());
		assertEquals("mind/map".length(), field.getSelectionEnd());

		property.setValue("");
		assertEquals("", property.getValue());
		assertEquals("", field.getText());
		assertEquals(0, field.getSelectionStart());
		assertEquals(0, field.getSelectionEnd());
	}

	@Test
	void releasingAKeyFiresThePropertyChangeWithTheCurrentText() {
		// backwards compatibility
		StringProperty property = new StringProperty("help", "title");
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 3dlu, pref:grow", ""));
		property.layout(builder, key -> key);
		JTextField field = find(builder.getPanel(), JTextField.class);
		field.setText("typed");
		String[] seen = { null };
		property.addPropertyChangeListener(event -> {
			assertSame(property, event.getSource());
			assertEquals("title", event.getPropertyName());
			assertNull(event.getOldValue());
			seen[0] = String.valueOf(event.getNewValue());
		});

		KeyEvent released = new KeyEvent(field, KeyEvent.KEY_RELEASED, 1L, 0, KeyEvent.VK_D, 'd');
		for (KeyListener listener : field.getKeyListeners()) {
			listener.keyReleased(released);
		}
		assertEquals("typed", seen[0]);
	}

	@Test
	void setEnabledReachesTheTextField() {
		// backwards compatibility
		StringProperty property = new StringProperty("help", "title");
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 3dlu, pref:grow", ""));
		property.layout(builder, key -> key);
		JTextField field = find(builder.getPanel(), JTextField.class);

		property.setEnabled(false);
		assertFalse(field.isEnabled());
		property.setEnabled(true);
		assertTrue(field.isEnabled());
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
