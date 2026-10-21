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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.extensions.MindMapHook.PluginBaseClassSearcher;
import freemind.main.FreeMind;
import freemind.main.Tools.BooleanHolder;
import freemind.modes.MindMapNode;
import freemind.modes.attributes.Attribute;
import freemind.modes.mindmapmode.MindMapController;
import plugins.script.ScriptingEngine;
import plugins.script.ScriptingEngine.ErrorHandler;
import plugins.script.ScriptingRegistration;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Groovy started from a node, a file, or a startup hook, without a confirmation dialog. */
@DisplayName("ScriptingEngineBehaviourTest")
class ScriptingEngineBehaviourTest {

	@AfterEach
	void clearCookies() throws Exception {
		Field field = ScriptingEngine.class.getDeclaredField("sScriptCookies");
		field.setAccessible(true);
		((Map<?, ?>) field.get(null)).clear();
	}

	@Test
	void findLineNumberReadsTheMarkerAndKeepsTheFallback() {
		// backwards compatibility
		assertEquals(4, ScriptingEngine.findLineNumberInString("start\n@ line 4\nend", -1));
		assertEquals(9, ScriptingEngine.findLineNumberInString("@ line 2 and @ line 9 ", 0));
		assertEquals(-1, ScriptingEngine.findLineNumberInString("no marker", -1));
		assertEquals(3, ScriptingEngine.findLineNumberInString("line 8", 3));
	}

	@Test
	void aMissingExternalScriptLeavesTheRootAndNodeScriptsAreNotRun() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode child = opened.child("Child");
		child.addAttribute(new Attribute("script1", "= 'nope'"));
		ScriptingEngine engine = hook(opened, "");
		engine.startupMapHook();
		assertEquals("ROOT", opened.root.getText());
		assertEquals("Child", child.getText());
		assertEquals("= 'nope'", child.getAttribute(0).getValue());

		engine = hook(opened, "/no/such/freemind-script-cov.groovy");
		engine.startupMapHook();
		assertEquals("ROOT", opened.root.getText());
		assertEquals("Child", child.getText());
	}

	@Test
	void anExternalScriptRewritesTheRootOnlyWhenAskingIsStoredAsYes() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		Path dir = Files.createTempDirectory("script-engine");
		try {
			Path file = dir.resolve("hook.groovy");
			Files.writeString(file, "= 'from-file'\n");
			opened.frame.setProperty(FreeMind.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING, "false");
			hook(opened, file.toString()).startupMapHook();
			assertEquals("ROOT", opened.root.getText());

			opened.frame.setProperty(FreeMind.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING, "true");
			hook(opened, file.toString()).startupMapHook();
			assertEquals("from-file", opened.root.getText());
		} finally {
			delete(dir);
		}
	}

	@Test
	void aSyntaxErrorInAnExternalScriptPrintsTheLineAndLeavesTheRoot() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		Path dir = Files.createTempDirectory("script-engine-bad");
		PrintStream original = System.out;
		ByteArrayOutputStream captured = new ByteArrayOutputStream();
		System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
		try {
			Path file = dir.resolve("bad.groovy");
			Files.writeString(file, "{{{\n");
			opened.frame.setProperty(FreeMind.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING, "true");
			hook(opened, file.toString()).startupMapHook();
			assertEquals("ROOT", opened.root.getText());
			assertTrue(captured.toString(StandardCharsets.UTF_8).contains("Line number:"));
		} finally {
			System.setOut(original);
			delete(dir);
		}
	}

	@Test
	void aLeadingEqualsStoresTheResultAndAnIdentifierDoesNot() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode node = opened.child("Child");
		ByteArrayOutputStream stored = new ByteArrayOutputStream();
		assertTrue(run(opened, node, "= 'a=b'", stored, (line) -> {
		}));
		assertEquals("a=b", node.getText());
		assertTrue(stored.toString(StandardCharsets.UTF_8).contains("plugins/ScriptEditor/window.Resulta=b"));

		ByteArrayOutputStream named = new ByteArrayOutputStream();
		assertTrue(run(opened, node, "score=2+3", named, (line) -> {
		}));
		assertEquals("a=b", node.getText());
		assertTrue(named.toString(StandardCharsets.UTF_8).contains("plugins/ScriptEditor/window.Result5"));
	}

	@Test
	void aNullResultLeavesTheTextAndASyntaxErrorReportsTheLine() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode node = opened.child("Child");
		ByteArrayOutputStream blank = new ByteArrayOutputStream();
		assertTrue(run(opened, node, "= null", blank, (line) -> {
		}));
		assertEquals("Child", node.getText());
		assertTrue(blank.toString(StandardCharsets.UTF_8).contains("plugins/ScriptEditor/window.Resultnull"));

		ByteArrayOutputStream bad = new ByteArrayOutputStream();
		int[] line = { -2 };
		assertFalse(run(opened, node, "{{{", bad, reported -> line[0] = reported));
		assertTrue(line[0] != -2);
		assertTrue(bad.toString(StandardCharsets.UTF_8).contains("Line number: " + line[0]));
		assertEquals("Child", node.getText());
	}

	@Test
	void cookiesSurviveALaterScriptAndPreferenceChangesAreRestored() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode node = opened.child("Child");
		opened.frame.setProperty(FreeMind.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING, "true");
		opened.frame.setProperty(FreeMind.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_FILE_RESTRICTION, "true");
		opened.frame.setProperty(FreeMind.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_NETWORK_RESTRICTION, "true");
		opened.frame.setProperty(FreeMind.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_EXEC_RESTRICTION, "true");
		opened.frame.setProperty(FreeMind.RESOURCES_SIGNED_SCRIPT_ARE_TRUSTED, "false");

		assertTrue(run(opened, node, "cookies['cov']='yes'; return 1", new ByteArrayOutputStream(), (line) -> {
		}));
		assertTrue(run(opened, node, "= cookies['cov']", new ByteArrayOutputStream(), (line) -> {
		}));
		assertEquals("yes", node.getText());

		String script = "c.frame.setProperty('" + FreeMind.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING + "', 'false');"
				+ "c.frame.setProperty('" + FreeMind.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_FILE_RESTRICTION + "', 'false');"
				+ "c.frame.setProperty('" + FreeMind.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_NETWORK_RESTRICTION
				+ "', 'false');"
				+ "c.frame.setProperty('" + FreeMind.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_EXEC_RESTRICTION + "', 'false');"
				+ "c.frame.setProperty('" + FreeMind.RESOURCES_SIGNED_SCRIPT_ARE_TRUSTED + "', 'true');"
				+ "return 1";
		assertTrue(run(opened, node, script, new ByteArrayOutputStream(), (line) -> {
		}));
		assertEquals("true", opened.frame.getProperty(FreeMind.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING));
		assertEquals("true", opened.frame.getProperty(FreeMind.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_FILE_RESTRICTION));
		assertEquals("true", opened.frame.getProperty(FreeMind.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_NETWORK_RESTRICTION));
		assertEquals("true", opened.frame.getProperty(FreeMind.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_EXEC_RESTRICTION));
		assertEquals("false", opened.frame.getProperty(FreeMind.RESOURCES_SIGNED_SCRIPT_ARE_TRUSTED));
	}

	@Test
	void anUntrustedSignatureStillStoresTheScriptResult() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode node = opened.child("Child");
		opened.frame.setProperty(FreeMind.RESOURCES_SIGNED_SCRIPT_ARE_TRUSTED, "true");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		assertTrue(run(opened, node, "= 'kept'//SIGN:%%%", out, (line) -> {
		}));
		assertEquals("kept", node.getText());
		assertTrue(out.toString(StandardCharsets.UTF_8).contains("plugins/ScriptEditor/window.Resultkept"));
	}

	@Test
	void anInterruptedScriptStopsWithoutChangingTheNode() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode node = opened.child("Child");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Thread.currentThread().interrupt();
		try {
			boolean ran = run(opened, node, "def n = 0; while (n < 10000000) { n++ }; return n", out, (line) -> {
			});
			assertFalse(ran);
			assertEquals("Child", node.getText());
			String printed = out.toString(StandardCharsets.UTF_8);
			assertTrue(printed.contains("Interrupted") || printed.contains("interrupt"));
		} finally {
			Thread.interrupted();
		}
	}

	private static ScriptingEngine hook(Opened opened, String scriptLocation) {
		ScriptingEngine engine = new ScriptingEngine();
		engine.setController(opened.mode);
		Properties properties = new Properties();
		properties.setProperty("ScriptLocation", scriptLocation);
		engine.setProperties(properties);
		ScriptingRegistration registration = new ScriptingRegistration(opened.mode, opened.mode.getMap());
		engine.setPluginBaseClass(new PluginBaseClassSearcher() {
			public Object getPluginBaseObject() {
				return registration;
			}
		});
		return engine;
	}

	private static boolean run(Opened opened, MindMapNode node, String script, ByteArrayOutputStream out,
			ErrorHandler handler) throws Exception {
		opened.frame.setProperty(FreeMind.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING, "true");
		Method method = ScriptingEngine.class.getDeclaredMethod("executeScript", MindMapNode.class,
				BooleanHolder.class, String.class, MindMapController.class, ErrorHandler.class, PrintStream.class,
				HashMap.class);
		method.setAccessible(true);
		try {
			return (Boolean) method.invoke(null, node, new BooleanHolder(true), script, opened.mode, handler,
					new PrintStream(out, true, StandardCharsets.UTF_8), new HashMap<String, String>());
		} catch (InvocationTargetException ex) {
			if (ex.getCause() instanceof RuntimeException runtime) {
				throw runtime;
			}
			throw new IllegalStateException(ex.getCause());
		}
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
