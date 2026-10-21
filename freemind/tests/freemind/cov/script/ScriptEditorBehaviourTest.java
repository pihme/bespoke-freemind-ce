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

package tests.freemind.cov.script;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.HeadlessException;
import java.awt.Window;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.junit.jupiter.api.Test;

import freemind.extensions.MindMapHook.PluginBaseClassSearcher;
import freemind.modes.MindMapNode;
import freemind.modes.attributes.Attribute;
import freemind.modes.mindmapmode.MindMapController;
import plugins.script.ScriptEditor;
import plugins.script.ScriptEditorPanel.ScriptHolder;
import plugins.script.ScriptEditorPanel.ScriptModel;
import plugins.script.ScriptingEngine.ErrorHandler;
import plugins.script.ScriptingRegistration;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Node scripts are attributes. The editor dialog itself cannot open while headless. */
class ScriptEditorBehaviourTest {

	@Test
	void openingTheEditorRefusesTheHeadlessDialogAndKeepsAttributes() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode node = opened.child("Child");
		node.addAttribute(new Attribute("note", "keep"));
		node.addAttribute(new Attribute("script1", "return 1"));
		opened.mode.select(node, List.of(node));
		ScriptEditor editor = editor(opened, node);

		Window[] before = Window.getWindows();
		assertThrows(HeadlessException.class, editor::startupMapHook);
		assertArrayEquals(before, Window.getWindows());
		assertEquals(2, node.getAttributeTableLength());
		assertEquals("note", node.getAttribute(0).getName());
		assertEquals("keep", node.getAttribute(0).getValue());
		assertEquals("script1", node.getAttribute(1).getName());
		assertEquals("return 1", node.getAttribute(1).getValue());
		assertEquals("Child", node.getText());
	}

	@Test
	void aChangedScriptIsStoredAndANewScriptIsAppended() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode node = opened.child("Child");
		node.addAttribute(new Attribute("note", "keep"));
		node.addAttribute(new Attribute("script1", "return 1"));
		ScriptModel model = model(opened, node, List.of(
				holder(opened, new Attribute("script1", "return 2"), 1)));

		assertEquals(1, model.getAmountOfScripts());
		assertEquals("script1", model.getScript(0).getScriptName());
		assertEquals("return 2", model.getScript(0).getScript());
		assertFalse(model.isDirty());
		model.setScript(0, new ScriptHolder("script1", "return 2"));
		assertFalse(model.isDirty());
		model.setScript(0, new ScriptHolder("renamed", "return 9"));
		assertTrue(model.isDirty());

		model.endDialog(false);
		assertEquals("note", node.getAttribute(0).getName());
		assertEquals("keep", node.getAttribute(0).getValue());
		assertEquals("renamed", node.getAttribute(1).getName());
		assertEquals("return 9", node.getAttribute(1).getValue());
		assertEquals(2, node.getAttributeTableLength());

		int added = model.addNewScript();
		assertEquals(1, added);
		assertEquals("script1", model.getScript(added).getScriptName());
		assertEquals("", model.getScript(added).getScript());
		assertTrue(model.isDirty());
		assertEquals(2, node.getAttributeTableLength());
		model.endDialog(false);
		assertEquals(3, node.getAttributeTableLength());
		assertEquals("script1", node.getAttribute(2).getName());
		assertEquals("", node.getAttribute(2).getValue());
	}

	@Test
	void theNextFreeScriptNameSkipsNamesAlreadyInTheModel() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode node = opened.child("Child");
		ScriptModel model = model(opened, node, List.of(
				holder(opened, new Attribute("script2", "return 2"), 0)));
		int added = model.addNewScript();
		assertEquals(1, added);
		assertEquals("script1", model.getScript(added).getScriptName());
		assertEquals(0, node.getAttributeTableLength());
	}

	@Test
	void cancelingDropsEditsAndASharedValueReferenceIsNotWrittenBack() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode node = opened.child("Child");
		node.addAttribute(new Attribute("script1", "old"));
		ScriptModel canceled = model(opened, node, List.of(
				holder(opened, new Attribute("script1", "new"), 1)));
		canceled.setScript(0, new ScriptHolder("script1", "edited"));
		assertTrue(canceled.isDirty());
		canceled.endDialog(true);
		assertEquals("script1", node.getAttribute(0).getName());
		assertEquals("old", node.getAttribute(0).getValue());
		assertEquals(1, node.getAttributeTableLength());

		String shared = "same";
		node.addAttribute(new Attribute("script2", shared));
		ScriptModel sameValue = model(opened, node, List.of(
				holder(opened, new Attribute("script8", shared), 1)));
		sameValue.endDialog(false);
		assertEquals("script2", node.getAttribute(1).getName());
		assertEquals("same", node.getAttribute(1).getValue());
	}

	@Test
	void runningTheStoredScriptRewritesTheSelectedNode() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode node = opened.child("Child");
		opened.mode.select(node, List.of(node));
		ScriptModel model = model(opened, node, List.of(
				holder(opened, new Attribute("script1", "= 'ran'"), 0)));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int[] line = { -2 };
		boolean ran = model.executeScript(0, print(out), lineNumber -> line[0] = lineNumber);
		assertTrue(ran);
		assertEquals(-2, line[0]);
		assertEquals("ran", node.getText());
		assertTrue(text(out).contains("plugins/ScriptEditor/window.Resultran"));
	}

	@Test
	void aBrokenScriptReturnsFalseAndReportsALine() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode node = opened.child("Child");
		opened.mode.select(node, List.of(node));
		ScriptModel model = model(opened, node, List.of(
				holder(opened, new Attribute("script1", "{{{"), 0)));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int[] line = { -2 };
		boolean ran = model.executeScript(0, print(out), lineNumber -> line[0] = lineNumber);
		assertFalse(ran);
		assertTrue(line[0] != -2);
		assertTrue(text(out).contains("Line number:"));
		assertEquals("Child", node.getText());
	}

	private static ScriptEditor editor(Opened opened, MindMapNode node) {
		ScriptEditor editor = new ScriptEditor();
		editor.setController(opened.mode);
		editor.setProperties(new Properties());
		ScriptingRegistration registration = new ScriptingRegistration(opened.mode, opened.mode.getMap());
		editor.setPluginBaseClass(new PluginBaseClassSearcher() {
			public Object getPluginBaseObject() {
				return registration;
			}
		});
		opened.mode.select(node, List.of(node));
		return editor;
	}

	private static Object holder(Opened opened, Attribute attribute, int position) throws Exception {
		ScriptEditor editor = editor(opened, opened.root);
		Class<?> type = Class.forName("plugins.script.ScriptEditor$AttributeHolder");
		Constructor<?> ctor = type.getDeclaredConstructor(ScriptEditor.class, Attribute.class, int.class);
		ctor.setAccessible(true);
		return ctor.newInstance(editor, attribute, position);
	}

	private static ScriptModel model(Opened opened, MindMapNode node, List<Object> holders) throws Exception {
		ScriptEditor editor = editor(opened, node);
		Vector<Object> scripts = new Vector<>();
		scripts.addAll(holders);
		Class<?> type = Class.forName("plugins.script.ScriptEditor$NodeScriptModel");
		Constructor<?> ctor = type.getDeclaredConstructor(ScriptEditor.class, Vector.class,
				MindMapNode.class, MindMapController.class);
		ctor.setAccessible(true);
		return (ScriptModel) ctor.newInstance(editor, scripts, node, opened.mode);
	}

	private static PrintStream print(ByteArrayOutputStream out) {
		return new PrintStream(out, true, StandardCharsets.UTF_8);
	}

	private static String text(ByteArrayOutputStream out) {
		return out.toString(StandardCharsets.UTF_8);
	}
}
