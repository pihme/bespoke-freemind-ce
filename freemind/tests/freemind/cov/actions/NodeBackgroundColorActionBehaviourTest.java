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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.util.List;

import org.junit.jupiter.api.Test;

import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.actions.NodeBackgroundColorAction;
import freemind.modes.mindmapmode.actions.NodeBackgroundColorAction.RemoveNodeBackgroundColorAction;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** A background stays when the chooser fails, and the remove action clears it. */
class NodeBackgroundColorActionBehaviourTest {

	@Test
	void theChooserLeavesTheBackground() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Wash");
		MindMapNode wash = opened.child("Wash");
		Color kept = new Color(4, 5, 6);
		opened.mode.setNodeBackgroundColor(wash, kept);
		opened.mode.select(wash, List.of(wash));
		assertThrows(HeadlessException.class,
				() -> new NodeBackgroundColorAction(opened.mode).actionPerformed(
						new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "background")));
		assertEquals(kept, wash.getBackgroundColor());
	}

	@Test
	void removeClearsTheBackground() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Wash");
		MindMapNode wash = opened.child("Wash");
		opened.mode.setNodeBackgroundColor(wash, new Color(4, 5, 6));
		opened.mode.select(wash, List.of(wash));
		new RemoveNodeBackgroundColorAction(opened.mode).actionPerformed(
				new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "clear"));
		assertNull(wash.getBackgroundColor());
	}
}
