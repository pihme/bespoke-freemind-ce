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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.HeadlessException;
import java.awt.Window;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import freemind.common.BooleanProperty;
import freemind.common.PropertyControl;
import freemind.common.ScriptEditorProperty;
import freemind.common.SeparatorProperty;
import freemind.common.StringProperty;
import freemind.preferences.FreemindPropertyContributor;
import freemind.controller.actions.generated.instance.Pattern;
import freemind.controller.actions.generated.instance.PatternScript;
import freemind.main.FreeMind;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.MindMapController.MindMapControllerPlugin;
import plugins.script.ScriptEditorPanel.ScriptHolder;
import plugins.script.ScriptEditorPanel.ScriptModel;
import plugins.script.ScriptingEngine.ErrorHandler;
import plugins.script.ScriptingRegistration;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Pattern scripts, the startup file, and the preference controls. The editor window stays closed. */
class ScriptingRegistrationBehaviourTest {

	@Test
	void registerAndDeregisterInstallTheStarterAndTheCookiesStayOnTheRegistration() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		ScriptingRegistration registration = new ScriptingRegistration(opened.mode, opened.mode.getMap());
		assertNull(registration.getScriptCookies().get("token"));
		registration.getScriptCookies().put("token", "kept");
		assertEquals("kept", registration.getScriptCookies().get("token"));

		assertFalse(opened.mode.getPlugins().contains(registration));
		registration.register();
		try {
			assertTrue(opened.mode.getPlugins().contains(registration));
			ScriptEditorProperty.ScriptEditorStarter starter = starter(opened);
			assertTrue(starter != null);
			Window[] before = Window.getWindows();
			assertThrows(HeadlessException.class, () -> starter.startEditor("return 1"));
			assertArrayEquals(before, Window.getWindows());
			assertEquals("kept", registration.getScriptCookies().get("token"));
		} finally {
			registration.deRegister();
		}
		assertFalse(opened.mode.getPlugins().contains(registration));
		assertTrue(starter(opened) == null);
	}

	@Test
	void preferenceControlsListTheScriptPermissionsAndTheSigningKey() throws Exception {
		// backwards compatibility
		Class<?> type = Class.forName(
				"plugins.script.ScriptingRegistration$ScriptingPluginPropertyContributor");
		Constructor<?> ctor = type.getDeclaredConstructor(
				freemind.modes.mindmapmode.MindMapController.class);
		ctor.setAccessible(true);
		FreemindPropertyContributor contributor = (FreemindPropertyContributor) ctor.newInstance(new Object[] { null });
		List<PropertyControl> controls = contributor.getControls(key -> key);

		assertEquals(7, controls.size());
		assertEquals("plugins/scripting/tab_name", controls.get(0).getLabel());
		assertTrue(controls.get(1) instanceof SeparatorProperty);
		assertEquals("plugins/scripting/separatorPropertyName", controls.get(1).getLabel());
		assertEquals(FreeMind.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_FILE_RESTRICTION,
				((BooleanProperty) controls.get(2)).getLabel());
		assertEquals("false", ((BooleanProperty) controls.get(2)).getValue());
		assertEquals(FreeMind.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_NETWORK_RESTRICTION,
				((BooleanProperty) controls.get(3)).getLabel());
		assertEquals(FreeMind.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_EXEC_RESTRICTION,
				((BooleanProperty) controls.get(4)).getLabel());
		assertEquals(FreeMind.RESOURCES_SIGNED_SCRIPT_ARE_TRUSTED, ((BooleanProperty) controls.get(5)).getLabel());
		assertEquals(FreeMind.RESOURCES_SCRIPT_USER_KEY_NAME_FOR_SIGNING,
				((StringProperty) controls.get(6)).getLabel());
		assertEquals("", ((StringProperty) controls.get(6)).getValue());
	}

	@Test
	void aPatternScriptRunsOnlyAfterAskingIsStoredAndHtmlEntitiesAreDecoded() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode node = opened.child("Child");
		opened.mode.select(node, List.of(node));
		ScriptingRegistration registration = new ScriptingRegistration(opened.mode, opened.mode.getMap());

		registration.act(node, new Pattern());
		Pattern empty = new Pattern();
		empty.setPatternScript(new PatternScript());
		registration.act(node, empty);
		assertEquals("Child", node.getText());

		Pattern pattern = pattern("= '&#x41;&amp;B'");
		opened.frame.setProperty(FreeMind.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING, "");
		Window[] before = Window.getWindows();
		assertThrows(HeadlessException.class, () -> registration.act(node, pattern));
		assertArrayEquals(before, Window.getWindows());
		assertEquals("Child", node.getText());

		opened.frame.setProperty(FreeMind.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING, "false");
		registration.act(node, pattern);
		assertEquals("Child", node.getText());

		opened.frame.setProperty(FreeMind.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING, "true");
		registration.act(node, pattern);
		assertEquals("A&B", node.getText());
	}

	@Test
	void aBrokenPatternScriptPrintsTheLineAndLeavesTheNode() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode node = opened.child("Child");
		opened.mode.select(node, List.of(node));
		opened.frame.setProperty(FreeMind.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING, "true");
		ScriptingRegistration registration = new ScriptingRegistration(opened.mode, opened.mode.getMap());
		PrintStream original = System.out;
		ByteArrayOutputStream captured = new ByteArrayOutputStream();
		System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
		try {
			registration.act(node, pattern("{{{"));
			assertEquals("Child", node.getText());
			assertTrue(captured.toString(StandardCharsets.UTF_8).contains("Line number:"));
		} finally {
			System.setOut(original);
		}
	}

	@Test
	void thePatternModelKeepsEditsUnlessTheDialogIsCanceled() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode node = opened.child("Child");
		opened.mode.select(node, List.of(node));
		opened.frame.setProperty(FreeMind.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING, "true");
		ScriptingRegistration registration = new ScriptingRegistration(opened.mode, opened.mode.getMap());
		ScriptModel model = patternModel(registration, "return 1");

		assertEquals(1, model.getAmountOfScripts());
		assertEquals("Script", model.getScript(4).getScriptName());
		assertEquals("return 1", model.getScript(4).getScript());
		assertFalse(model.isDirty());
		assertEquals(0, model.addNewScript());
		assertEquals("return 1", scriptText(model));

		model.setScript(9, new ScriptHolder("ignored", "return 2"));
		assertTrue(model.isDirty());
		assertEquals("return 2", model.getScript(0).getScript());
		model.endDialog(true);
		assertEquals("return 1", scriptText(model));
		assertFalse(model.isDirty());

		model.setScript(0, new ScriptHolder("ignored", "= 'pattern'"));
		model.endDialog(false);
		assertEquals("= 'pattern'", scriptText(model));
		assertTrue(model.isDirty());

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int[] line = { -2 };
		assertTrue(model.executeScript(0, new PrintStream(out, true, StandardCharsets.UTF_8),
				(ErrorHandler) reported -> line[0] = reported));
		assertEquals(-2, line[0]);
		assertEquals("pattern", node.getText());
		assertTrue(out.toString(StandardCharsets.UTF_8).contains("plugins/ScriptEditor/window.Resultpattern"));
	}

	@Test
	void startupDoneRunsAFileAndIgnoresAMissingOrEmptyOne() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		opened.mode.select(opened.root, List.of(opened.root));
		opened.frame.setProperty(FreeMind.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING, "true");
		ScriptingRegistration registration = new ScriptingRegistration(opened.mode, opened.mode.getMap());
		String previous = System.getProperty("startup_groovy_script");
		Path dir = Files.createTempDirectory("script-startup");
		try {
			System.clearProperty("startup_groovy_script");
			registration.startupDone();
			System.setProperty("startup_groovy_script", "");
			registration.startupDone();
			System.setProperty("startup_groovy_script", "~/no-such-freemind-startup-script-cov.groovy");
			registration.startupDone();
			assertEquals("ROOT", opened.root.getText());

			Path empty = dir.resolve("empty.groovy");
			Files.writeString(empty, "");
			System.setProperty("startup_groovy_script", empty.toString());
			registration.startupDone();
			assertEquals("ROOT", opened.root.getText());

			Path file = dir.resolve("start.groovy");
			Files.writeString(file, "= 'booted'\n");
			System.setProperty("startup_groovy_script", file.toString());
			registration.startupDone();
			assertEquals("booted", opened.root.getText());
		} finally {
			if (previous == null) {
				System.clearProperty("startup_groovy_script");
			} else {
				System.setProperty("startup_groovy_script", previous);
			}
			delete(dir);
		}
	}

	private static ScriptEditorProperty.ScriptEditorStarter starter(Opened opened) {
		for (MindMapControllerPlugin plugin : opened.mode.getPlugins()) {
			if (plugin instanceof ScriptEditorProperty.ScriptEditorStarter found) {
				return found;
			}
		}
		return null;
	}

	private static Pattern pattern(String script) {
		Pattern pattern = new Pattern();
		PatternScript body = new PatternScript();
		body.setValue(script);
		pattern.setPatternScript(body);
		return pattern;
	}

	private static ScriptModel patternModel(ScriptingRegistration registration, String script) throws Exception {
		Class<?> type = Class.forName("plugins.script.ScriptingRegistration$PatternScriptModel");
		Constructor<?> ctor = type.getDeclaredConstructor(ScriptingRegistration.class, String.class);
		ctor.setAccessible(true);
		return (ScriptModel) ctor.newInstance(registration, script);
	}

	private static String scriptText(ScriptModel model) throws Exception {
		Method method = model.getClass().getDeclaredMethod("getScript");
		method.setAccessible(true);
		return (String) method.invoke(model);
	}

	private static void delete(Path dir) throws Exception {
		try (var walk = Files.walk(dir)) {
			walk.sorted(Comparator.reverseOrder()).forEach(path -> {
				try {
					Files.deleteIfExists(path);
				} catch (Exception ex) {
					throw new IllegalStateException(ex);
				}
			});
		}
	}
}
