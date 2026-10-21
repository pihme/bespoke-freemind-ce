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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Test;

import freemind.modes.mindmapmode.MindMapController;
import freemind.modes.mindmapmode.actions.CopySingleAction;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Copy single puts the focused node on the clipboard and leaves the map in place. */
class CopySingleActionBehaviourTest {

	@Test
	void theFocusedTextIsCopied() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Solo");
		Clipboard board = new Clipboard("coverage");
		Field clipboard = MindMapController.class.getDeclaredField("clipboard");
		clipboard.setAccessible(true);
		clipboard.set(opened.mode, board);
		opened.mode.select(opened.child("Solo"), List.of(opened.child("Solo")));
		new CopySingleAction(opened.mode).actionPerformed(
				new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "copy-single"));
		assertEquals(List.of("Solo"), MapFixture.childTexts(opened.root));
		String plain = (String) board.getContents(null).getTransferData(DataFlavor.stringFlavor);
		assertTrue(plain.contains("Solo"), plain);
	}
}
