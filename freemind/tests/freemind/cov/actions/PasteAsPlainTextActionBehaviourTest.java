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

package tests.freemind.cov.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Test;

import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.MindMapController;
import freemind.modes.mindmapmode.actions.PasteAsPlainTextAction;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Plain paste adds the clipboard string, and a non-text flavor adds nothing. */
class PasteAsPlainTextActionBehaviourTest {

	@Test
	void aStringBecomesAChildAndAPictureDoesNot() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Host");
		MindMapNode host = opened.child("Host");
		Clipboard board = new Clipboard("coverage");
		Field clipboard = MindMapController.class.getDeclaredField("clipboard");
		clipboard.setAccessible(true);
		clipboard.set(opened.mode, board);
		opened.mode.select(host, List.of(host));
		board.setContents(new Transferable() {
			public DataFlavor[] getTransferDataFlavors() {
				return new DataFlavor[] { DataFlavor.imageFlavor };
			}

			public boolean isDataFlavorSupported(DataFlavor flavor) {
				return DataFlavor.imageFlavor.equals(flavor);
			}

			public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
				throw new UnsupportedFlavorException(flavor);
			}
		}, null);
		PasteAsPlainTextAction action = new PasteAsPlainTextAction(opened.mode);
		action.actionPerformed(new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "plain-paste"));
		assertEquals(0, host.getChildCount());
		board.setContents(new StringSelection("Note\0X"), null);
		action.actionPerformed(new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "plain-paste"));
		assertEquals(List.of("NoteX"), MapFixture.childTexts(host));
	}
}
