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
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import freemind.common.ComboProperty;
import freemind.common.TextTranslator;

/** A combo stores the key, shows the translation, and falls back to the first entry. */
@DisplayName("ComboPropertyBehaviourTest")
class ComboPropertyBehaviourTest {

	@Test
	void aListedValueIncludingEmptyRoundTripsAndAnUnknownValueSelectsTheFirst() {
		// backwards compatibility
		ComboProperty property = new ComboProperty("help", "choice", new String[] { "a", "" }, translator());
		assertEquals("choice", property.getLabel());
		assertEquals("help", property.getDescription());
		assertEquals("a", property.getValue());

		property.setValue("");
		assertEquals("", property.getValue());
		property.setValue("a");
		assertEquals("a", property.getValue());
		property.setValue("missing");
		assertEquals("a", property.getValue());
		property.setValue(null);
		assertEquals("a", property.getValue());
	}

	@Test
	void choosingAnotherEntryFiresUnlessTheConstructorLeftTheListenerOff() {
		// backwards compatibility
		ComboProperty property = new ComboProperty("help", "choice", List.of("a", "b"), translator());
		int[] fires = { 0 };
		property.addPropertyChangeListener(event -> {
			fires[0]++;
			assertEquals("b", event.getNewValue());
		});
		property.setValue("b");
		assertEquals(1, fires[0]);
		assertEquals("b", property.getValue());

		ComboProperty silent = new ComboProperty("help", "choice", List.of("a", "b"), List.of("A", "B"));
		int[] silentFires = { 0 };
		silent.addPropertyChangeListener(event -> silentFires[0]++);
		silent.setValue("b");
		assertEquals("b", silent.getValue());
		assertEquals(0, silentFires[0]);

		ComboProperty direct = new ComboProperty("help", "choice", new String[] { "a" }, List.of("A"));
		assertEquals("a", direct.getValue());
	}

	@Test
	void updateReplacesTheEntriesAndAnEmptyListCannotBeRead() {
		// backwards compatibility
		ComboProperty property = new ComboProperty("help", "choice", new String[] { "a" }, translator());
		property.updateComboBoxEntries(List.of("x", "y"), List.of("X", "Y"));
		assertEquals("x", property.getValue());
		property.setValue("y");
		assertEquals("y", property.getValue());

		property.updateComboBoxEntries(List.of(), List.of());
		property.setValue("y");
		assertThrows(ArrayIndexOutOfBoundsException.class, property::getValue);
	}

	@Test
	void layoutShowsTheTranslationAndHonoursEnabled() {
		// backwards compatibility
		ComboProperty property = new ComboProperty("help", "choice", new String[] { "a", "b" }, translator());
		property.setEnabled(false);
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 3dlu, pref:grow", ""));
		property.layout(builder, translator());
		JLabel label = find(builder.getPanel(), JLabel.class);
		JComboBox<?> box = find(builder.getPanel(), JComboBox.class);
		assertEquals("t:choice", label.getText());
		assertEquals("t:help", label.getToolTipText());
		assertEquals("t:a", box.getItemAt(0));
		assertFalse(box.isEnabled());
		property.setEnabled(true);
		assertTrue(box.isEnabled());
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
