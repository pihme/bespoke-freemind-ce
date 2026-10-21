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

import java.awt.event.ActionEvent;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.NodeAdapter;
import freemind.modes.mindmapmode.actions.MoveNodeAction;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Reset position restores the default gaps and leaves the root where it is. */
@DisplayName("MoveNodeActionBehaviourTest")
class MoveNodeActionBehaviourTest {

	@Test
	void aShiftedChildReturnsToTheDefaultGaps() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Peg");
		NodeAdapter peg = (NodeAdapter) opened.child("Peg");
		NodeAdapter root = (NodeAdapter) opened.root;
		peg.setHGap(40);
		peg.setShiftY(9);
		root.setVGap(9);
		opened.mode.select(peg, List.of(peg));
		new MoveNodeAction(opened.mode).actionPerformed(
				new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "reset"));
		assertEquals(NodeAdapter.HGAP, peg.getHGap());
		assertEquals(0, peg.getShiftY());
		assertEquals(NodeAdapter.VGAP, root.getVGap());
	}

	@Test
	void theRootKeepsItsOwnGaps() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Peg");
		NodeAdapter root = (NodeAdapter) opened.root;
		root.setHGap(40);
		root.setShiftY(9);
		root.setVGap(9);
		opened.mode.select(opened.root, List.of(opened.root));
		new MoveNodeAction(opened.mode).actionPerformed(
				new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "reset"));
		assertEquals(40, root.getHGap());
		assertEquals(9, root.getShiftY());
		assertEquals(9, root.getVGap());
	}
}
