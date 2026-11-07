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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import freemind.common.IconProperty;
import freemind.main.HeadlessFreeMind;
import freemind.modes.MindIcon;

/** An icon preference stores the icon name. The picker dialog cannot open headless. */
@DisplayName("IconPropertyBehaviourTest")
class IconPropertyBehaviourTest {

	@Test
	void aKnownIconRoundTripsAndAnUnknownNameLeavesThePreviousOne() {
		// backwards compatibility
		HeadlessFreeMind frame = new HeadlessFreeMind();
		ImageIcon image = new ImageIcon();
		MindIcon icon = MindIcon.factory("cov-common-icon", image);
		MindIcon other = MindIcon.factory("cov-common-other", new ImageIcon());
		Vector<MindIcon> icons = new Vector<>();
		icons.add(icon);
		icons.add(other);
		IconProperty property = new IconProperty("help", "icon", frame, icons);
		assertEquals("icon", property.getLabel());
		assertEquals("help", property.getDescription());
		assertThrows(NullPointerException.class, property::getValue);

		property.setValue("");
		property.setValue("missing");
		assertThrows(NullPointerException.class, property::getValue);

		property.setValue(icon.getName());
		assertEquals(icon.getName(), property.getValue());
		property.setValue(other.getName());
		assertEquals(other.getName(), property.getValue());
		property.setValue("missing");
		assertEquals(other.getName(), property.getValue());

		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 3dlu, pref:grow", ""));
		property.layout(builder, key -> "t:" + key);
		JButton button = find(builder.getPanel(), JButton.class);
		JLabel label = find(builder.getPanel(), JLabel.class);
		assertEquals("t:icon", label.getText());
		assertEquals("t:help", label.getToolTipText());
		assertSame(other.getIcon(), button.getIcon());
		assertEquals(other.getDescription(), button.getToolTipText());

		property.setEnabled(false);
		assertFalse(button.isEnabled());
		property.setEnabled(true);
		assertTrue(button.isEnabled());
	}

	@Test
	void openingThePickerThrowsHeadlessExceptionAndKeepsTheIcon() {
		// backwards compatibility
		HeadlessFreeMind frame = new HeadlessFreeMind();
		MindIcon icon = MindIcon.factory("cov-common-icon", new ImageIcon());
		Vector<MindIcon> icons = new Vector<>();
		icons.add(icon);
		IconProperty property = new IconProperty("help", "icon", frame, icons);
		property.setValue(icon.getName());
		Window[] before = Window.getWindows();
		int[] fires = { 0 };
		property.addPropertyChangeListener(event -> fires[0]++);

		assertThrows(HeadlessException.class,
				() -> property.actionPerformed(new ActionEvent(property, ActionEvent.ACTION_PERFORMED, "pick")));

		assertEquals(icon.getName(), property.getValue());
		assertEquals(0, fires[0]);
		assertArrayEquals(before, Window.getWindows());
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
