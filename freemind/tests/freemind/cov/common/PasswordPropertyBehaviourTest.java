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
import javax.swing.JPasswordField;

import org.junit.jupiter.api.Test;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import freemind.common.PasswordProperty;
import freemind.main.Tools;

/** A password is stored compressed and read back as the same plain text. */
class PasswordPropertyBehaviourTest {

	@Test
	void aPasswordAndAnEmptyPasswordRoundTripThroughCompression() {
		// backwards compatibility
		PasswordProperty property = new PasswordProperty("help", "secret");
		assertEquals("secret", property.getLabel());
		assertEquals("help", property.getDescription());
		assertEquals("", Tools.decompress(property.getValue()));

		property.setValue(Tools.compress(""));
		assertEquals("", Tools.decompress(property.getValue()));
		assertEquals(Tools.compress(""), property.getValue());

		property.setValue(Tools.compress("s3cret"));
		assertEquals("s3cret", Tools.decompress(property.getValue()));
		assertEquals(Tools.compress("s3cret"), property.getValue());
	}

	@Test
	void theFieldShowsThePlainPasswordAndHonoursEnabled() {
		// backwards compatibility
		PasswordProperty property = new PasswordProperty("help", "secret");
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 3dlu, pref:grow", ""));
		property.layout(builder, key -> "t:" + key);
		JPasswordField field = find(builder.getPanel(), JPasswordField.class);

		property.setValue(Tools.compress("s3cret"));
		assertEquals("s3cret", new String(field.getPassword()));
		property.setValue(Tools.compress(""));
		assertEquals(0, field.getPassword().length);

		property.setEnabled(false);
		assertFalse(field.isEnabled());
		property.setEnabled(true);
		assertTrue(field.isEnabled());
		assertEquals("t:secret", find(builder.getPanel(), JLabel.class).getText());
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
