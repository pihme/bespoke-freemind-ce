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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Cut removes the node from its parent. */
@DisplayName("CutActorBehaviourTest")
class CutActorBehaviourTest {

	@Test
	void theCutChildLeaves() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Gone", "Stay");
		List<freemind.modes.MindMapNode> cut = new ArrayList<>();
		cut.add(opened.child("Gone"));
		opened.mode.getActorFactory().getCutActor().cut(cut);
		assertEquals(List.of("Stay"), MapFixture.childTexts(opened.root));
	}
}
