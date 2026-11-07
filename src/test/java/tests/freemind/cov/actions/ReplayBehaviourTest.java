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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.ActionEvent;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.MindMapNode;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Undo and redo share one replay flag, and a finished undo leaves it clear. */
@DisplayName("ReplayBehaviourTest")
class ReplayBehaviourTest {

	@Test
	void undoAndRedoReadTheSameReplayFlag() throws Exception {
		Opened opened = MapFixture.open("Before");
		assertFalse(opened.mode.isUndoAction());
		assertFalse(opened.mode.undo.isUndoAction());
		opened.mode.replay().start();
		assertTrue(opened.mode.isUndoAction());
		assertTrue(opened.mode.undo.isUndoAction());
		assertTrue(opened.mode.redo.isUndoAction());
		opened.mode.replay().end();
		assertFalse(opened.mode.replay().isReplaying());
		assertFalse(opened.mode.isUndoAction());
	}

	@Test
	void aFinishedUndoLeavesTheFlagClearAndEnablesRedo() throws Exception {
		Opened opened = MapFixture.open("Before");
		MindMapNode leaf = opened.child("Before");
		opened.mode.select(leaf, List.of(leaf));
		opened.mode.setNodeText(leaf, "After");
		ActionEvent event = new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "history");
		opened.mode.undo.actionPerformed(event);
		assertEquals("Before", leaf.getText());
		assertFalse(opened.mode.replay().isReplaying());
		assertFalse(opened.mode.isUndoAction());
		assertFalse(opened.mode.undo.isEnabled());
		assertTrue(opened.mode.redo.isEnabled());
	}
}
