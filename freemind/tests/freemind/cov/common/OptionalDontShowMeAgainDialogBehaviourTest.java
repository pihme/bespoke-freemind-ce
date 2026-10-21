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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.HeadlessException;
import java.awt.Window;

import javax.swing.JOptionPane;

import org.junit.jupiter.api.Test;

import freemind.common.OptionalDontShowMeAgainDialog;
import freemind.common.OptionalDontShowMeAgainDialog.StandardPropertyHandler;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** A remembered answer skips the dialog. Showing it headless adds no window. */
class OptionalDontShowMeAgainDialogBehaviourTest {

	@Test
	void aRememberedAnswerReturnsWithoutAWindow() {
		// backwards compatibility
		Memory memory = new Memory();
		OptionalDontShowMeAgainDialog dialog = dialog(memory, OptionalDontShowMeAgainDialog.ONLY_OK_SELECTION_IS_STORED);
		Window[] before = Window.getWindows();

		memory.value = "true";
		assertSame(dialog, dialog.show());
		assertEquals(JOptionPane.OK_OPTION, dialog.getResult());

		memory.value = "false";
		assertSame(dialog, dialog.show());
		assertEquals(JOptionPane.CANCEL_OPTION, dialog.getResult());
		assertEquals("false", memory.value);
		assertArrayEquals(before, Window.getWindows());
	}

	@Test
	void showingWhenTheAnswerIsNotRememberedThrowsHeadlessException() {
		// backwards compatibility
		Memory memory = new Memory();
		memory.value = "";
		OptionalDontShowMeAgainDialog dialog = dialog(memory,
				OptionalDontShowMeAgainDialog.BOTH_OK_AND_CANCEL_OPTIONS_ARE_STORED);
		Window[] before = Window.getWindows();

		assertThrows(HeadlessException.class, dialog::show);

		assertEquals(JOptionPane.CANCEL_OPTION, dialog.getResult());
		assertEquals("", memory.value);
		assertArrayEquals(before, Window.getWindows());
	}

	@Test
	void theStandardHandlerStoresThePropertyOnTheController() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open();
		StandardPropertyHandler handler = new StandardPropertyHandler(opened.app, "cov.dont.show");
		assertNull(handler.getProperty());

		handler.setProperty("true");
		assertEquals("true", handler.getProperty());
		assertEquals("true", opened.app.getProperty("cov.dont.show"));

		handler.setProperty("");
		assertEquals("", opened.app.getProperty("cov.dont.show"));

		OptionalDontShowMeAgainDialog dialog = new OptionalDontShowMeAgainDialog(null, null, "message", "title",
				key -> key, handler, OptionalDontShowMeAgainDialog.ONLY_OK_SELECTION_IS_STORED);
		handler.setProperty("true");
		dialog.show();
		assertEquals(JOptionPane.OK_OPTION, dialog.getResult());
	}

	private static OptionalDontShowMeAgainDialog dialog(Memory memory, int messageType) {
		return new OptionalDontShowMeAgainDialog(null, null, "message.id", "title.id", key -> "t:" + key, memory,
				messageType);
	}

	private static final class Memory implements OptionalDontShowMeAgainDialog.DontShowPropertyHandler {
		private String value = "";

		public String getProperty() {
			return value;
		}

		public void setProperty(String pValue) {
			value = pValue;
		}
	}
}
