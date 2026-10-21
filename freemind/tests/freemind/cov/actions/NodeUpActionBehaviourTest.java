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

import org.junit.jupiter.api.Test;

import freemind.modes.mindmapmode.actions.NodeUpAction;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Moving a node up swaps it with the sibling above it. */
class NodeUpActionBehaviourTest {

	@Test
	void theLowerSiblingMovesAboveItsNeighbour() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Lower", "Upper");
		opened.mode.select(opened.child("Upper"), List.of(opened.child("Upper")));
		new NodeUpAction(opened.mode).actionPerformed(
				new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "up"));
		assertEquals(List.of("Upper", "Lower"), MapFixture.childTexts(opened.root));
	}
}
