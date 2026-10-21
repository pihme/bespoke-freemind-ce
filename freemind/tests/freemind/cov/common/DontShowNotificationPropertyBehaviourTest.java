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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JCheckBox;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import freemind.common.DontShowNotificationProperty;

/** "Don't show again" stores true, or an empty string when the box is clear. */
@DisplayName("DontShowNotificationPropertyBehaviourTest")
class DontShowNotificationPropertyBehaviourTest {

	@Test
	void trueAndEmptyRoundTripAndFalseIsRejected() {
		// backwards compatibility
		DontShowNotificationProperty property = new DontShowNotificationProperty("help", "again");
		assertEquals("", property.getValue());
		assertEquals("again", property.getLabel());
		assertEquals("help", property.getDescription());

		property.setValue("true");
		assertEquals("true", property.getValue());
		property.setValue("TRUE");
		assertEquals("true", property.getValue());
		property.setValue("");
		assertEquals("", property.getValue());

		assertThrows(IllegalArgumentException.class, () -> property.setValue("false"));
		assertThrows(IllegalArgumentException.class, () -> property.setValue(null));
		assertEquals("", property.getValue());
	}

	@Test
	void checkingTheBoxFiresAndEnabledReachesTheBox() {
		// backwards compatibility
		DontShowNotificationProperty property = new DontShowNotificationProperty("help", "again");
		String[] seen = { null };
		property.addPropertyChangeListener(event -> seen[0] = String.valueOf(event.getNewValue()));
		property.setValue("true");
		assertEquals("true", seen[0]);

		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 3dlu, pref:grow", ""));
		property.layout(builder, key -> "t:" + key);
		JCheckBox box = find(builder.getPanel(), JCheckBox.class);
		property.setEnabled(false);
		assertFalse(box.isEnabled());
		assertTrue(box.isSelected());
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
