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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.text.html.HTMLDocument;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.lightdev.app.shtm.SHTMLPanel;

import accessories.plugins.NodeNote;
import accessories.plugins.NodeNoteRegistration;
import freemind.main.FreeMind;
import freemind.main.FreeMindMain;
import freemind.main.Resources;
import freemind.modes.MapAdapter;
import freemind.modes.MindMapNode;
import freemind.modes.ModeController.NodeLifetimeListener;
import freemind.modes.ModeController.NodeSelectionListener;
import freemind.modes.common.plugins.NodeNoteBase;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("NodeNoteRegistrationBehaviourTest")
class NodeNoteRegistrationBehaviourTest {

	// backwards compatibility
	@Test
	void normalizedNoteTextCollapsesWhitespaceAndSpaceOrAltIsAKey() throws Exception {
		Opened opened = MapFixture.open("Child");
		NodeNoteRegistration registration = new NodeNoteRegistration(opened.mode, opened.mode.getMap());
		Method normalize = NodeNoteRegistration.class.getDeclaredMethod("normalizeString", String.class);
		normalize.setAccessible(true);
		String empty = (String) normalize.invoke(registration, new Object[] { null });
		assertFalse(empty.contains("\n"), empty);
		assertTrue(empty.contains("html"), empty);
		assertEquals("a b c", normalize.invoke(registration, "  a   b \n c  "));
		assertFalse(registration.getSplitPaneVisible());
		assertFalse(registration.isSelected(new JMenuItem(), null));
		assertTrue(spaceOrAltIsHandled());
	}

	// backwards compatibility
	@Test
	void theNoteSplitSavesIconsAndADifferentNoteMakesTheMapDirty() throws Exception {
		Opened opened = MapFixture.open("Child");
		opened.mode.setView(opened.view);
		opened.frame.setProperty(FreeMind.RESOURCES_SHOW_NOTE_PANE, "false");
		opened.frame.setProperty(FreeMind.RESOURCES_USE_DEFAULT_FONT_FOR_NOTES_TOO, "false");
		opened.frame.setProperty(FreeMind.RESOURCES_USE_MARGIN_TOP_ZERO_FOR_NOTES, "false");
		opened.frame.setProperty("defaultfont", "Dialog");
		String previousIcons = resourcesProperty(FreeMind.RESOURCES_DON_T_SHOW_NOTE_ICONS);
		String previousTips = resourcesProperty(FreeMind.RESOURCES_DON_T_SHOW_NOTE_TOOLTIPS);
		String previousSizing = resourcesProperty("experimental_font_sizing_for_long_node_editors");
		resourcesProperty(FreeMind.RESOURCES_DON_T_SHOW_NOTE_ICONS, "false");
		resourcesProperty(FreeMind.RESOURCES_DON_T_SHOW_NOTE_TOOLTIPS, "false");
		resourcesProperty("experimental_font_sizing_for_long_node_editors", "false");
		MindMapNode child = opened.child("Child");
		child.setXmlNoteText("<html><body>alpha</body></html>");
		CaretEditor editor = new CaretEditor();
		ActionMap actions = new ActionMap();
		SHTMLPanel panel = notePanel(editor, actions);
		NodeNoteRegistration registration = new NodeNoteRegistration(opened.mode, opened.mode.getMap());
		try (MockedStatic<NodeNoteRegistration> panels = mockStatic(NodeNoteRegistration.class)) {
			panels.when(NodeNoteRegistration::getHtmlEditorPanel).thenReturn(panel);
			registration.register();
			try {
				assertFalse(registration.getSplitPaneVisible());
				assertFalse(registration.isSelected(new JMenuItem(), null));
				assertNotNull(actions.get("jumpToMapAction"));
				assertTrue(child.getStateIcons().containsKey(NodeNoteBase.NODE_NOTE_ICON));
				assertEquals(child.getNoteText(), child.getToolTip().get("nodeNoteText"));

				resourcesProperty(FreeMind.RESOURCES_DON_T_SHOW_NOTE_ICONS, "true");
				resourcesProperty(FreeMind.RESOURCES_DON_T_SHOW_NOTE_TOOLTIPS, "true");
				((NodeLifetimeListener) notes(registration)).onCreateNodeHook(child);
				assertFalse(child.getStateIcons().containsKey(NodeNoteBase.NODE_NOTE_ICON));
				assertTrue(child.getToolTip().containsKey("nodeNoteText"));
				resourcesProperty(FreeMind.RESOURCES_DON_T_SHOW_NOTE_ICONS, "false");
				resourcesProperty(FreeMind.RESOURCES_DON_T_SHOW_NOTE_TOOLTIPS, "false");

				String beforeEarlySave = child.getNoteText();
				((NodeSelectionListener) notes(registration)).onSaveNode(child);
				assertEquals(beforeEarlySave, child.getNoteText());

				registration.showNotesPanel();
				assertTrue(registration.getSplitPaneVisible());
				assertTrue(registration.isSelected(new JMenuItem(), null));
				opened.frame.setProperty(FreeMind.RESOURCES_USE_DEFAULT_FONT_FOR_NOTES_TOO, "true");
				resourcesProperty("experimental_font_sizing_for_long_node_editors", "false");
				opened.frame.setProperty(FreeMind.RESOURCES_USE_MARGIN_TOP_ZERO_FOR_NOTES, "false");
				registration.showNotesPanel();
				resourcesProperty("experimental_font_sizing_for_long_node_editors", "true");
				opened.frame.setProperty(FreeMind.RESOURCES_USE_MARGIN_TOP_ZERO_FOR_NOTES, "true");
				registration.showNotesPanel();
				assertTrue(registration.getSplitPaneVisible());
				ArgumentCaptor<ActionListener> links = ArgumentCaptor.forClass(ActionListener.class);
				verify(panel, atLeastOnce()).setOpenHyperlinkHandler(links.capture());
				File local = File.createTempFile("note-link", ".txt");
				local.deleteOnExit();
				links.getValue().actionPerformed(new ActionEvent(panel, 0, local.toURI().toURL().toString()));
				links.getValue().actionPerformed(new ActionEvent(panel, 0, "not a url"));
				assertEquals("Child", child.getText());

				Object listener = field(registration, "mNoteDocumentListener");
				Method setNode = listener.getClass().getDeclaredMethod("setNode", MindMapNode.class);
				setNode.setAccessible(true);
				setNode.invoke(listener, child);
				child.setNoteText("same text");
				opened.mode.setSaved(false);
				doThrow(new IllegalStateException("unread while dirty")).when(panel).getDocumentText();
				call(listener, "insertUpdate");
				assertFalse(opened.mode.getMap().isSaved());
				doReturn("  same   text ").when(panel).getDocumentText();
				opened.mode.setSaved(true);
				call(listener, "changedUpdate");
				assertTrue(opened.mode.getMap().isSaved());
				when(panel.getDocumentText()).thenReturn("other");
				call(listener, "removeUpdate");
				assertFalse(opened.mode.getMap().isSaved());

				File mapFile = File.createTempFile("note-map", ".mm");
				mapFile.deleteOnExit();
				((MapAdapter) opened.mode.getMap()).setFile(mapFile);
				NodeSelectionListener selection = (NodeSelectionListener) notes(registration);
				select(opened, child);
				clearInvocations(panel);
				when(panel.getDocumentText()).thenReturn("alpha");
				child.setNoteText("alpha");
				selection.onFocusNode(opened.view.getNodeView(child));
				verify(panel).setCurrentDocumentContent("alpha");

				clearInvocations(panel);
				child.setNoteText(null);
				selection.onFocusNode(opened.view.getNodeView(child));
				verify(panel).setCurrentDocumentContent("");

				setLastContentEmpty(true);
				clearInvocations(panel);
				selection.onFocusNode(opened.view.getNodeView(child));
				verify(panel, never()).setCurrentDocumentContent("");
				((MapAdapter) opened.mode.getMap()).setFile(null);
				child.setNoteText("alpha");
				selection.onFocusNode(opened.view.getNodeView(child));
				assertEquals("alpha", child.getNoteText());

				when(panel.needsSaving()).thenReturn(false);
				when(panel.getDocumentText()).thenReturn("ignored");
				child.setNoteText("stay");
				editor.setCaretPosition(0);
				selection.onSaveNode(child);
				assertEquals("stay", child.getNoteText());

				when(panel.needsSaving()).thenReturn(true);
				when(panel.getDocumentText()).thenReturn(NodeNote.EMPTY_EDITOR_STRING);
				selection.onSaveNode(child);
				assertNull(child.getNoteText());
				when(panel.getDocumentText()).thenReturn(NodeNote.EMPTY_EDITOR_STRING_ALTERNATIVE);
				selection.onSaveNode(child);
				assertNull(child.getNoteText());
				when(panel.getDocumentText()).thenReturn(NodeNote.EMPTY_EDITOR_STRING_ALTERNATIVE2);
				selection.onSaveNode(child);
				assertNull(child.getNoteText());

				editor.rejectCaret = false;
				when(panel.getDocumentText()).thenReturn("<style>h1{}</style>kept");
				editor.setCaretPosition(0);
				assertTrue(editor.getDocument().getLength() > editor.getCaretPosition());
				selection.onSaveNode(child);
				assertEquals("kept", child.getNoteText());

				editor.rejectCaret = true;
				when(panel.getDocumentText()).thenReturn("kept-again");
				selection.onSaveNode(child);
				assertEquals("kept-again", child.getNoteText());
				editor.rejectCaret = false;

				select(opened, opened.root);
				assertEquals(opened.root, opened.mode.getSelected());
				clearInvocations(panel);
				child.setNoteText("off-selection");
				selection.onUpdateNodeHook(child);
				verify(panel, never()).getDocumentText();
				assertTrue(child.getStateIcons().containsKey(NodeNoteBase.NODE_NOTE_ICON));

				select(opened, child);
				when(panel.getDocumentText()).thenReturn(child.getNoteText());
				clearInvocations(panel);
				selection.onUpdateNodeHook(child);
				verify(panel, never()).setCurrentDocumentContent(org.mockito.ArgumentMatchers.any());

				child.setNoteText("different");
				clearInvocations(panel);
				selection.onUpdateNodeHook(child);
				verify(panel).setCurrentDocumentContent("different");
				assertTrue(child.getStateIcons().containsKey(NodeNoteBase.NODE_NOTE_ICON));

				child.setNoteText("");
				clearInvocations(panel);
				when(panel.getDocumentText()).thenReturn("still-there");
				selection.onUpdateNodeHook(child);
				verify(panel).setCurrentDocumentContent("");
				assertFalse(child.getStateIcons().containsKey(NodeNoteBase.NODE_NOTE_ICON));

				child.setNoteText(null);
				clearInvocations(panel);
				selection.onUpdateNodeHook(child);
				verify(panel).setCurrentDocumentContent("");

				when(panel.needsSaving()).thenReturn(true);
				when(panel.getDocumentText()).thenReturn("from-lost-focus");
				((NodeLifetimeListener) notes(registration)).onPreDeleteNode(child);
				((NodeLifetimeListener) notes(registration)).onPostDeleteNode(child, opened.root);
				selection.onLostFocusNode(opened.view.getNodeView(child));
				assertEquals("from-lost-focus", child.getNoteText());
				when(panel.getDocumentText()).thenReturn("nope");
				selection.onSaveNode(child);
				assertEquals("from-lost-focus", child.getNoteText());
				selection.onSelectionChange(opened.view.getNodeView(child), false);

				registration.hideNotesPanel();
				assertFalse(registration.getSplitPaneVisible());
				registration.showNotesPanel();
				assertTrue(registration.getSplitPaneVisible());

				select(opened, child);
				MindMapNode selected = opened.mode.getSelected();
				actions.get("jumpToMapAction").actionPerformed(new ActionEvent(registration, 0, "jump"));
				assertEquals(selected, opened.mode.getSelected());

				clearInvocations(panel);
				when(panel.needsSaving()).thenReturn(false);
				doThrowFlat(panel);
				child.setNoteText("boom-note");
				assertNotNull(opened.view.getNodeView(child));
				selection.onFocusNode(opened.view.getNodeView(child));
				assertEquals("boom-note", child.getNoteText());
				org.mockito.Mockito.doThrow(new NullPointerException("plain")).when(panel)
						.setCurrentDocumentContent(org.mockito.ArgumentMatchers.eq("plain-npe"));
				child.setNoteText("plain-npe");
				assertThrows(NullPointerException.class,
						() -> selection.onFocusNode(opened.view.getNodeView(child)));

				opened.frame.setProperty(FreeMind.RESOURCES_SHOW_NOTE_PANE, "false");
				registration.deRegister();
				assertNotNull(field(registration, "noteViewerComponent"));
				assertNotNull(actions.get("jumpToMapAction"));
				opened.frame.setProperty(FreeMind.RESOURCES_SHOW_NOTE_PANE, "true");
				registration.showNotesPanel();
				registration.deRegister();
				assertNull(field(registration, "noteViewerComponent"));
				assertNull(actions.get("jumpToMapAction"));
				assertFalse(registration.getSplitPaneVisible());
				assertEquals("Child", child.getText());
			} finally {
				setLastContentEmpty(true);
			}
		} finally {
			resourcesProperty(FreeMind.RESOURCES_DON_T_SHOW_NOTE_ICONS, previousIcons);
			resourcesProperty(FreeMind.RESOURCES_DON_T_SHOW_NOTE_TOOLTIPS, previousTips);
			resourcesProperty("experimental_font_sizing_for_long_node_editors", previousSizing);
		}
	}

	private static SHTMLPanel notePanel(JEditorPane editor, ActionMap actions) {
		SHTMLPanel panel = mock(SHTMLPanel.class, Mockito.withSettings().useConstructor(new BorderLayout())
				.defaultAnswer(Mockito.CALLS_REAL_METHODS));
		doReturn((HTMLDocument) editor.getDocument()).when(panel).getDocument();
		doReturn(editor).when(panel).getEditorPane();
		doReturn(editor).when(panel).getMostRecentFocusOwner();
		doReturn(actions).when(panel).getActionMap();
		doReturn(new InputMap()).when(panel).getInputMap(anyInt());
		doReturn("alpha").when(panel).getDocumentText();
		doReturn(false).when(panel).needsSaving();
		doNothing().when(panel).setCurrentDocumentContent(any());
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

	private static void doThrowFlat(SHTMLPanel panel) {
		org.mockito.Mockito.doAnswer(invocation -> {
			FlatComboBoxUI.boom();
			return null;
		}).when(panel).setCurrentDocumentContent(org.mockito.ArgumentMatchers.eq("boom-note"));
	}

	private static Object notes(NodeNoteRegistration registration) throws Exception {
		return field(registration, "mNotesManager");
	}

	private static boolean spaceOrAltIsHandled() throws Exception {
		Class<?> south = Class.forName("accessories.plugins.NodeNoteRegistration$SouthPanel");
		Constructor<?> constructor = south.getDeclaredConstructor();
		constructor.setAccessible(true);
		JComponent panel = (JComponent) constructor.newInstance();
		Method process = south.getDeclaredMethod("processKeyBinding", KeyStroke.class, KeyEvent.class, int.class,
				boolean.class);
		process.setAccessible(true);
		KeyEvent space = new KeyEvent(new JPanel(), KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_SPACE, ' ');
		KeyEvent alt = new KeyEvent(new JPanel(), KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_ALT,
				(char) KeyEvent.VK_ALT);
		KeyEvent letter = new KeyEvent(new JPanel(), KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_A, 'a');
		boolean spaceHandled = (Boolean) process.invoke(panel, KeyStroke.getKeyStroke(' '), space,
				JComponent.WHEN_FOCUSED, true);
		boolean altHandled = (Boolean) process.invoke(panel, KeyStroke.getKeyStroke(KeyEvent.VK_ALT, 0), alt,
				JComponent.WHEN_FOCUSED, true);
		boolean letterHandled = (Boolean) process.invoke(panel, KeyStroke.getKeyStroke('a'), letter,
				JComponent.WHEN_FOCUSED, true);
		assertTrue(spaceHandled);
		assertTrue(altHandled);
		assertFalse(letterHandled);
		return spaceHandled && altHandled && !letterHandled;
	}

	private static void call(Object target, String name) throws Exception {
		Method method = target.getClass().getDeclaredMethod(name, javax.swing.event.DocumentEvent.class);
		method.setAccessible(true);
		method.invoke(target, new Object[] { null });
	}

	private static Object field(Object target, String name) throws Exception {
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		return field.get(target);
	}

	private static void setLastContentEmpty(boolean empty) throws Exception {
		Field last = NodeNoteRegistration.class.getDeclaredField("mLastContentEmpty");
		last.setAccessible(true);
		last.setBoolean(null, empty);
	}

	private static void select(Opened opened, MindMapNode node) {
		opened.view.getRoot().updateAll();
		opened.view.selectAsTheOnlyOneSelected(opened.view.getNodeView(node));
	}

	/** The note catch compares the throwing frame with this name. */
	static final class FlatComboBoxUI {
		static void boom() {
			throw new NullPointerException("editor");
		}
	}

	private static final class CaretEditor extends JEditorPane {
		private static final long serialVersionUID = 1L;
		private boolean rejectCaret;

		private CaretEditor() {
			setContentType("text/html");
			setText("<html><body><p>hello</p></body></html>");
		}

		@Override
		public void setCaretPosition(int pos) {
			if (rejectCaret) {
				throw new IllegalArgumentException("caret " + pos);
			}
			super.setCaretPosition(pos);
		}
	}
}
