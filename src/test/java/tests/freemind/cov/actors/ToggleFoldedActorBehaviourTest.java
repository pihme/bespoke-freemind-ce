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

import freemind.modes.MindMapNode;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** A parent with a child folds. */
@DisplayName("ToggleFoldedActorBehaviourTest")
class ToggleFoldedActorBehaviourTest {

	@Test
	void aParentFolds() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Stem");
		MindMapNode stem = opened.child("Stem");
		opened.mode.addNewNode(stem, 0, false);
		opened.mode.getActorFactory().getToggleFoldedActor().setFolded(stem, true);
		assertTrue(stem.isFolded());
	}
}
