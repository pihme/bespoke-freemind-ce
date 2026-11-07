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

package tests.freemind.cov.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.Vector;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.controller.actions.generated.instance.UndoPasteNodeAction;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.MindMapController;
import freemind.modes.mindmapmode.MindMapNodeModel;
import freemind.modes.mindmapmode.actions.UndoAction;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Undoing a paste removes the child that was added. */
@DisplayName("UndoPasteActorBehaviourTest")
class UndoPasteActorBehaviourTest {

	@Test
	void thePastedChildLeaves() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Leaf");
		MindMapNode leaf = opened.child("Leaf");
		opened.mode.addNewNode(leaf, 0, false);
		UndoPasteNodeAction undo = new UndoPasteNodeAction();
		undo.setNode(opened.mode.getNodeID(leaf));
		undo.setAsSibling(false);
		undo.setIsLeft(false);
		undo.setNodeAmount(1);
		opened.mode.getActorFactory().getUndoPasteActor().act(undo);
		assertEquals(0, leaf.getChildCount());
	}

	@Test
	void undoingAClipboardPasteRestoresTheEarlierChildren() throws Exception {
		Opened opened = MapFixture.open("Keep");
		assertTrue(opened.mode.paste(new StringSelection("Pasted"),
				opened.root, false, false));
		assertEquals(2, opened.root.getChildCount());
		opened.mode.undo.actionPerformed(new ActionEvent(opened.mode.undo,
				ActionEvent.ACTION_PERFORMED, "undo"));
		assertEquals(1, opened.root.getChildCount());
		assertEquals("Keep", ((MindMapNode) opened.root.getChildAt(0)).getText());
	}

	@Test
	void branchImportRecordsNoUndoPair() throws Exception {
		Opened opened = MapFixture.open("Keep");
		int before = undoCount(opened.mode);
		MindMapNode imported = new MindMapNodeModel("Imported", opened.mode.getMap());
		opened.mode.paste(imported, opened.root);
		assertEquals(before, undoCount(opened.mode));
		assertEquals(2, opened.root.getChildCount());
		assertEquals("Imported", ((MindMapNode) opened.root.getChildAt(1)).getText());
	}

	private static int undoCount(MindMapController mode) throws Exception {
		Field field = UndoAction.class.getDeclaredField("actionPairList");
		field.setAccessible(true);
		return ((Vector<?>) field.get(mode.undo)).size();
	}
}
