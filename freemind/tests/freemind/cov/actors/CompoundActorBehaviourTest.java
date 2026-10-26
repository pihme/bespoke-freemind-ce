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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.controller.actions.generated.instance.BoldNodeAction;
import freemind.controller.actions.generated.instance.CompoundAction;
import freemind.modes.MindMapNode;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** A compound action runs the nested action on the node. */
@DisplayName("CompoundActorBehaviourTest")
class CompoundActorBehaviourTest {

	@Test
	void theNestedBoldIsApplied() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Leaf");
		MindMapNode leaf = opened.child("Leaf");
		BoldNodeAction bold = new BoldNodeAction();
		bold.setNode(opened.mode.getNodeID(leaf));
		bold.setBold(true);
		CompoundAction compound = new CompoundAction();
		compound.addChoice(bold);
		opened.mode.getActorFactory().getCompoundActor().act(compound);
		assertTrue(leaf.isBold());
	}
}
