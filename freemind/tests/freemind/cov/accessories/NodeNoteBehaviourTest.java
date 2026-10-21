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

package tests.freemind.cov.accessories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.awt.BorderLayout;
import java.lang.reflect.Field;
import java.util.Properties;

import javax.swing.JEditorPane;
import javax.swing.text.html.HTMLDocument;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.lightdev.app.shtm.SHTMLPanel;

import accessories.plugins.NodeNote;
import accessories.plugins.NodeNoteRegistration;
import freemind.extensions.MindMapHook.PluginBaseClassSearcher;
import freemind.main.FreeMind;
import freemind.main.FreeMindMain;
import freemind.main.Resources;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("NodeNoteBehaviourTest")
class NodeNoteBehaviourTest {

	// backwards compatibility
	@Test
	void showAndHideToggleTheNoteSplitAndJumpOpensIt() throws Exception {
		Opened opened = MapFixture.open("Child");
		opened.mode.setView(opened.view);
		opened.frame.setProperty(FreeMind.RESOURCES_SHOW_NOTE_PANE, "false");
		opened.frame.setProperty(FreeMind.RESOURCES_USE_DEFAULT_FONT_FOR_NOTES_TOO, "true");
		opened.frame.setProperty(FreeMind.RESOURCES_USE_MARGIN_TOP_ZERO_FOR_NOTES, "true");
		opened.frame.setProperty("defaultfont", "Dialog");
		String previousSizing = resourcesProperty("experimental_font_sizing_for_long_node_editors");
		resourcesProperty("experimental_font_sizing_for_long_node_editors", "true");
		NodeNoteRegistration registration = new NodeNoteRegistration(opened.mode, opened.mode.getMap());
		FocusEditor editor = new FocusEditor();
		try (MockedStatic<NodeNoteRegistration> panels = mockStatic(NodeNoteRegistration.class)) {
			SHTMLPanel panel = notePanel(editor);
			setField(registration, "noteViewerComponent", panel);
			panels.when(NodeNoteRegistration::getHtmlEditorPanel).thenReturn(panel);
			assertFalse(registration.getSplitPaneVisible());

			hook(opened, registration, "show").startupMapHook();
			assertTrue(registration.getSplitPaneVisible());
			assertEquals("true", opened.frame.getProperty(FreeMind.RESOURCES_SHOW_NOTE_PANE));
			assertTrue(editor.focused);

			editor.focused = false;
			hook(opened, registration, "hide").startupMapHook();
			assertFalse(registration.getSplitPaneVisible());
			assertEquals("false", opened.frame.getProperty(FreeMind.RESOURCES_SHOW_NOTE_PANE));
			assertFalse(editor.focused);

			hook(opened, registration, "jump").startupMapHook();
			assertTrue(registration.getSplitPaneVisible());
			assertEquals("true", opened.frame.getProperty(FreeMind.RESOURCES_SHOW_NOTE_PANE));
			assertTrue(editor.focused);
			assertEquals("Child", opened.child("Child").getText());
		} finally {
			resourcesProperty("experimental_font_sizing_for_long_node_editors", previousSizing);
		}
	}

	private static SHTMLPanel notePanel(JEditorPane editor) {
		SHTMLPanel panel = mock(SHTMLPanel.class, Mockito.withSettings().useConstructor(new BorderLayout())
				.defaultAnswer(Mockito.CALLS_REAL_METHODS));
		doReturn((HTMLDocument) editor.getDocument()).when(panel).getDocument();
		doReturn(editor).when(panel).getMostRecentFocusOwner();
		doReturn(editor).when(panel).getEditorPane();
		doReturn("note").when(panel).getDocumentText();
		doNothing().when(panel).setOpenHyperlinkHandler(any());
		return panel;
	}

	private static String resourcesProperty(String key) throws Exception {
		return resourcesMain().getProperty(key);
	}

	private static void resourcesProperty(String key, String value) throws Exception {
		if (value == null) {
			resourcesMain().getProperties().remove(key);
		} else {
			resourcesMain().setProperty(key, value);
		}
	}

	private static FreeMindMain resourcesMain() throws Exception {
		Field mainField = Resources.class.getDeclaredField("main");
		mainField.setAccessible(true);
		return (FreeMindMain) mainField.get(Resources.getInstance());
	}

	private static NodeNote hook(Opened opened, NodeNoteRegistration registration, String command) {
		NodeNote hook = new NodeNote();
		hook.setController(opened.mode);
		hook.setMap(opened.mode.getMap());
		Properties properties = new Properties();
		properties.setProperty("command", command);
		hook.setProperties(properties);
		hook.setPluginBaseClass(new PluginBaseClassSearcher() {
			public Object getPluginBaseObject() {
				return registration;
			}
		});
		return hook;
	}

	private static void setField(Object target, String name, Object value) throws Exception {
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}

	private static final class FocusEditor extends JEditorPane {
		private static final long serialVersionUID = 1L;
		private boolean focused;

		private FocusEditor() {
			setContentType("text/html");
			setText("<html><body><p>hello</p></body></html>");
		}

		@Override
		public void requestFocus() {
			focused = true;
		}
	}
}
