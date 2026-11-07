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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JLabel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import freemind.common.ScriptEditorProperty;
import freemind.modes.mindmapmode.MindMapController;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** A script is stored escaped, and the editor plugin replaces the plain text. */
@DisplayName("ScriptEditorPropertyBehaviourTest")
class ScriptEditorPropertyBehaviourTest {

	@Test
	void nullAndEmptyAndEscapedScriptRoundTrip() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open();
		ScriptEditorProperty property = new ScriptEditorProperty("help", "script", opened.mode);
		assertEquals("script", property.getLabel());
		assertEquals("help", property.getDescription());
		assertEquals("", property.getValue());

		property.setValue(null);
		assertEquals("", property.getValue());
		property.setValue("");
		assertEquals("", property.getValue());
		property.setValue("a&lt;b&#xff;");
		assertEquals("a&lt;b&#xff;", property.getValue());

		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 3dlu, pref:grow", ""));
		property.layout(builder, key -> "t:" + key);
		JButton button = find(builder.getPanel(), JButton.class);
		JLabel label = find(builder.getPanel(), JLabel.class);
		assertEquals("t:script", label.getText());
		assertEquals("t:help", label.getToolTipText());
		assertEquals("a<b\u00ff", button.getText());

		property.setEnabled(false);
		assertFalse(button.isEnabled());
		property.setEnabled(true);
		assertTrue(button.isEnabled());
	}

	@Test
	void theEditorPluginReplacesTheScriptAndANullResultDoesNot() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open();
		ScriptEditorProperty property = new ScriptEditorProperty("help", "script", opened.mode);
		property.setValue("old");
		Editor editor = new Editor("z<q");
		opened.mode.registerPlugin(editor);
		opened.mode.registerPlugin(new MindMapController.MindMapControllerPlugin() {
		});
		String[] seen = { null };
		property.addPropertyChangeListener(event -> seen[0] = String.valueOf(event.getNewValue()));

		property.actionPerformed(new ActionEvent(property, ActionEvent.ACTION_PERFORMED, "edit"));
		assertEquals("old", editor.seen);
		assertEquals("z&lt;q", property.getValue());
		assertEquals("z&lt;q", seen[0]);

		opened.mode.deregisterPlugin(editor);
		Editor reject = new Editor(null);
		opened.mode.registerPlugin(reject);
		seen[0] = null;
		property.actionPerformed(new ActionEvent(property, ActionEvent.ACTION_PERFORMED, "edit"));
		assertEquals("z<q", reject.seen);
		assertEquals("z&lt;q", property.getValue());
		assertNull(seen[0]);
	}

	private static final class Editor implements ScriptEditorProperty.ScriptEditorStarter {
		private final String result;
		private String seen;

		private Editor(String result) {
			this.result = result;
		}

		public String startEditor(String scriptInput) {
			seen = scriptInput;
			return result;
		}
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
