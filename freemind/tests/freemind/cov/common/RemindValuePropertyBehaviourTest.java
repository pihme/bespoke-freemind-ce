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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JButton;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import freemind.common.RemindValueProperty;
import freemind.common.TextTranslator;

/** Remind stores true, false, or an empty "ask" value, and labels the button. */
@DisplayName("RemindValuePropertyBehaviourTest")
class RemindValuePropertyBehaviourTest {

	@Test
	void trueFalseAndEmptyRoundTripAndTheButtonDropsTheMnemonicMark() {
		// backwards compatibility
		RemindValueProperty property = property();
		assertEquals("true", property.getValue());

		property.setValue("false");
		assertEquals("false", property.getValue());
		property.setValue("");
		assertEquals("", property.getValue());
		property.setValue("TRUE");
		assertEquals("true", property.getValue());

		assertThrows(IllegalArgumentException.class, () -> property.setValue("don_t_touch"));
		assertThrows(IllegalArgumentException.class, () -> property.setValue(null));
		assertEquals("true", property.getValue());

		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 3dlu, pref:grow", ""));
		property.layout(builder, translator());
		JButton button = find(builder.getPanel(), JButton.class);
		property.setValue("true");
		assertEquals("Yes", button.getText());
		property.setValue("false");
		assertEquals("No", button.getText());
		property.setValue("");
		assertEquals("Ask", button.getText());
	}

	@Test
	void clickingTheButtonCyclesTheCaptionAndTheStoredValue() {
		// backwards compatibility
		RemindValueProperty property = property();
		property.setValue("true");
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 3dlu, pref:grow", ""));
		property.layout(builder, translator());
		JButton button = find(builder.getPanel(), JButton.class);

		button.doClick();
		assertEquals("false", property.getValue());
		assertEquals("No", button.getText());
		button.doClick();
		assertEquals("", property.getValue());
		assertEquals("Ask", button.getText());
		button.doClick();
		assertEquals("true", property.getValue());
		assertEquals("Yes", button.getText());
	}

	private static RemindValueProperty property() {
		return new RemindValueProperty("help", "remind", translator());
	}

	private static TextTranslator translator() {
		return key -> {
			if ("OptionalDontShowMeAgainDialog.ok".equals(key)) {
				return "&Yes";
			}
			if ("OptionalDontShowMeAgainDialog.cancel".equals(key)) {
				return "&No";
			}
			if ("OptionPanel.ask".equals(key)) {
				return "A&sk";
			}
			return "t:" + key;
		};
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
