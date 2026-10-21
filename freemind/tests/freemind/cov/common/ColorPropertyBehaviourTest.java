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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.HeadlessException;
import java.awt.IllegalComponentStateException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Field;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import freemind.common.ColorProperty;
import freemind.main.Tools;

/** A colour preference stores #rrggbb. The chooser and the popup cannot open headless. */
@DisplayName("ColorPropertyBehaviourTest")
class ColorPropertyBehaviourTest {

	@Test
	void aColourRoundTripsAndNullBecomesAWhiteButtonWithANullValue() {
		// backwards compatibility
		ColorProperty property = property();
		assertEquals("ink", property.getLabel());
		assertEquals("help", property.getDescription());
		assertEquals("#000000", property.getValue());

		property.setValue("#00ff00");
		assertEquals("#00ff00", property.getValue());
		property.setValue(null);
		assertNull(property.getValue());

		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 3dlu, pref:grow", ""));
		property.layout(builder, key -> "t:" + key);
		JButton button = find(builder.getPanel(), JButton.class);
		assertEquals("#ffffff", button.getText());
		assertEquals(Color.WHITE, button.getBackground());

		assertThrows(IllegalArgumentException.class, () -> property.setValue(""));
		assertThrows(IllegalArgumentException.class, () -> property.setValue("green"));
		assertNull(property.getValue());
	}

	@Test
	void theResetItemRestoresTheDefaultAndANormalClickDoesNot() throws Exception {
		// backwards compatibility
		ColorProperty property = property();
		property.setValue("#00ff00");
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 3dlu, pref:grow", ""));
		property.layout(builder, key -> "ColorProperty.ResetColor");
		JButton button = find(builder.getPanel(), JButton.class);
		JLabel label = find(builder.getPanel(), JLabel.class);
		assertEquals("ColorProperty.ResetColor", label.getToolTipText());

		MouseEvent plain = mouse(button, false);
		for (MouseListener listener : button.getMouseListeners()) {
			listener.mousePressed(plain);
			listener.mouseReleased(plain);
		}
		assertEquals("#00ff00", property.getValue());

		Field menuField = ColorProperty.class.getDeclaredField("menu");
		menuField.setAccessible(true);
		JPopupMenu menu = (JPopupMenu) menuField.get(property);
		assertEquals(1, menu.getComponentCount());
		((JMenuItem) menu.getComponent(0)).doClick();
		assertEquals("#0000ff", property.getValue());
		assertEquals(Tools.xmlToColor("#0000ff"), button.getBackground());
	}

	@Test
	void theChooserThrowsHeadlessExceptionAndThePopupCannotBePlaced() {
		// backwards compatibility
		ColorProperty property = property();
		property.setValue("#00ff00");
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 3dlu, pref:grow", ""));
		property.layout(builder, key -> key);
		JButton button = find(builder.getPanel(), JButton.class);
		Window[] before = Window.getWindows();
		int[] fires = { 0 };
		property.addPropertyChangeListener(event -> fires[0]++);

		assertThrows(HeadlessException.class,
				() -> property.actionPerformed(new ActionEvent(button, ActionEvent.ACTION_PERFORMED, "pick")));
		MouseEvent popup = mouse(button, true);
		int shown = 0;
		for (MouseListener listener : button.getMouseListeners()) {
			try {
				listener.mousePressed(popup);
			} catch (IllegalComponentStateException ex) {
				shown++;
			}
			try {
				listener.mouseReleased(popup);
			} catch (IllegalComponentStateException ex) {
				shown++;
			}
		}
		assertEquals(2, shown);
		assertEquals("#00ff00", property.getValue());
		assertEquals(0, fires[0]);
		assertArrayEquals(before, Window.getWindows());
		property.setEnabled(false);
		assertFalse(button.isEnabled());
		property.setEnabled(true);
		assertTrue(button.isEnabled());
	}

	private static ColorProperty property() {
		return new ColorProperty("help", "ink", "#0000ff", key -> "t:" + key);
	}

	private static MouseEvent mouse(JButton button, boolean popup) {
		return new MouseEvent(button, MouseEvent.MOUSE_PRESSED, 1L, 0, 1, 1, 1, popup, MouseEvent.BUTTON3);
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
