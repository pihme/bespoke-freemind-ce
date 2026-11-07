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
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.controller.actions.generated.instance.PasteNodeAction;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.MindMapController;
import freemind.modes.mindmapmode.actions.CopyAction;
import freemind.modes.mindmapmode.actions.PasteAction;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Paste adds the copied node as a child of the selection. */
@DisplayName("PasteActionBehaviourTest")
class PasteActionBehaviourTest {

	@Test
	void theCopiedNodeBecomesAChild() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Bit", "Bowl");
		Clipboard board = new Clipboard("coverage");
		Field clipboard = MindMapController.class.getDeclaredField("clipboard");
		clipboard.setAccessible(true);
		clipboard.set(opened.mode, board);
		opened.mode.select(opened.child("Bit"), List.of(opened.child("Bit")));
		new CopyAction(opened.mode).actionPerformed(
				new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "copy"));
		MindMapNode bowl = opened.child("Bowl");
		opened.mode.select(bowl, List.of(bowl));
		PasteAction paste = new PasteAction(opened.mode);
		assertEquals(PasteNodeAction.class, paste.getDoActionClass());
		paste.actionPerformed(new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "paste"));
		assertEquals(List.of("Bit"), MapFixture.childTexts(bowl));
	}
}
