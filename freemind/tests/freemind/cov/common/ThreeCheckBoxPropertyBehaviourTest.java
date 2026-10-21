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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.JButton;
import javax.swing.JLabel;

import org.junit.jupiter.api.Test;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import freemind.common.ThreeCheckBoxProperty;

/** The three-state box cycles true, false, and don_t_touch. */
class ThreeCheckBoxPropertyBehaviourTest {

	@Test
	void theThreeStoredValuesRoundTripAndAnythingElseIsRejected() {
		// backwards compatibility
		ThreeCheckBoxProperty property = property();
		assertEquals(ThreeCheckBoxProperty.TRUE_VALUE, property.getValue());

		property.setValue("false");
		assertEquals("false", property.getValue());
		property.setValue("TRUE");
		assertEquals("true", property.getValue());
		property.setValue("don_t_touch");
		assertEquals(ThreeCheckBoxProperty.DON_T_TOUCH_VALUE, property.getValue());
		property.setValue("DON_T_TOUCH");
		assertEquals("don_t_touch", property.getValue());

		assertThrows(IllegalArgumentException.class, () -> property.setValue(""));
		assertThrows(IllegalArgumentException.class, () -> property.setValue(null));
		assertEquals("don_t_touch", property.getValue());
	}

	@Test
	void clickingTheButtonCyclesTheThreeStatesAndFires() {
		// backwards compatibility
		ThreeCheckBoxProperty property = property();
		property.setValue("true");
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 3dlu, pref:grow", ""));
		property.layout(builder, key -> "t:" + key);
		JButton button = find(builder.getPanel(), JButton.class);
		JLabel label = find(builder.getPanel(), JLabel.class);
		assertEquals("t:tri", label.getText());
		assertEquals("t:help", label.getToolTipText());
		assertEquals("t:help", button.getToolTipText());

		int[] fires = { 0 };
		property.addPropertyChangeListener(event -> fires[0]++);
		button.doClick();
		assertEquals("false", property.getValue());
		button.doClick();
		assertEquals("don_t_touch", property.getValue());
		button.doClick();
		assertEquals("true", property.getValue());
		assertEquals(3, fires[0]);
		assertNotNull(button.getIcon());
	}

	@Test
	void setEnabledReachesTheButton() {
		// backwards compatibility
		ThreeCheckBoxProperty property = property();
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 3dlu, pref:grow", ""));
		property.layout(builder, key -> key);
		JButton button = find(builder.getPanel(), JButton.class);
		property.setEnabled(false);
		assertFalse(button.isEnabled());
		property.setEnabled(true);
		assertTrue(button.isEnabled());
	}

	@Test
	void aNullTransformMeansDontTouchAndAnUnknownStateReadsAsNull() throws Exception {
		// backwards compatibility
		ThreeCheckBoxProperty property = property();
		Method transform = ThreeCheckBoxProperty.class.getDeclaredMethod("transformString", String.class);
		transform.setAccessible(true);
		assertEquals(Integer.valueOf(2), transform.invoke(property, new Object[] { null }));
		assertEquals(Integer.valueOf(0), transform.invoke(property, "true"));
		assertEquals(Integer.valueOf(1), transform.invoke(property, "false"));

		Field state = ThreeCheckBoxProperty.class.getDeclaredField("state");
		state.setAccessible(true);
		state.setInt(property, 9);
		assertNull(property.getValue());
	}

	private static ThreeCheckBoxProperty property() {
		return new ThreeCheckBoxProperty("help", "tri");
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
