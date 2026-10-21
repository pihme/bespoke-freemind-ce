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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Window;
import java.io.PrintStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.controller.actions.generated.instance.ScriptEditorWindowConfigurationStorage;
import freemind.main.HeadlessFreeMind;
import plugins.script.ScriptEditorPanel;
import plugins.script.ScriptEditorPanel.ScriptHolder;
import plugins.script.ScriptEditorPanel.ScriptModel;
import plugins.script.ScriptingEngine.ErrorHandler;

/** The script dialog is a window. Headless construction fails before any control exists. */
@DisplayName("ScriptEditorPanelBehaviourTest")
class ScriptEditorPanelBehaviourTest {

	@Test
	void theDialogConstructorThrowsHeadlessExceptionAndAddsNoWindow() {
		// backwards compatibility
		assertTrue(GraphicsEnvironment.isHeadless());
		HeadlessFreeMind frame = new HeadlessFreeMind();
		SilentModel model = new SilentModel();
		Window[] before = Window.getWindows();

		assertThrows(HeadlessException.class, () -> new ScriptEditorPanel(model, frame, true));
		assertThrows(HeadlessException.class, () -> new ScriptEditorPanel(model, frame, false));

		assertEquals(0, model.calls);
		assertArrayEquals(before, Window.getWindows());
	}

	@Test
	void aScriptHolderStoresTheNameAndTheBody() {
		// backwards compatibility
		ScriptHolder holder = new ScriptHolder("script1", "return 1");
		assertEquals("script1", holder.getScriptName());
		assertEquals("return 1", holder.getScript());

		assertSame(holder, holder.setScript("return 2"));
		assertSame(holder, holder.setScriptName("script2"));
		assertEquals("script2", holder.getScriptName());
		assertEquals("return 2", holder.getScript());
	}

	/** Fails the test if the dialog gets far enough to ask the model for anything. */
	private static final class SilentModel implements ScriptModel {
		private int calls;

		public int getAmountOfScripts() {
			calls++;
			throw new AssertionError("dialog asked for scripts");
		}

		public ScriptHolder getScript(int index) {
			calls++;
			throw new AssertionError("dialog asked for a script");
		}

		public void setScript(int index, ScriptHolder script) {
			calls++;
			throw new AssertionError("dialog stored a script");
		}

		public boolean executeScript(int index, PrintStream outStream, ErrorHandler errorHandler) {
			calls++;
			throw new AssertionError("dialog ran a script");
		}

		public void storeDialogPositions(ScriptEditorPanel panel,
				ScriptEditorWindowConfigurationStorage storage, String key) {
			calls++;
			throw new AssertionError("dialog stored its position");
		}

		public ScriptEditorWindowConfigurationStorage decorateDialog(ScriptEditorPanel panel, String key) {
			calls++;
			throw new AssertionError("dialog decorated itself");
		}

		public void endDialog(boolean canceled) {
			calls++;
			throw new AssertionError("dialog ended");
		}

		public boolean isDirty() {
			calls++;
			throw new AssertionError("dialog asked if dirty");
		}

		public int addNewScript() {
			calls++;
			throw new AssertionError("dialog added a script");
		}
	}
}
