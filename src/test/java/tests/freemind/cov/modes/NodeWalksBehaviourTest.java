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

package tests.freemind.cov.modes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.MindMapNode;
import freemind.modes.NodeWalks;
import freemind.modes.mindmapmode.MindMapNodeModel;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Stored children stay when a node is folded. Folded-away children do not. */
@DisplayName("NodeWalksBehaviourTest")
class NodeWalksBehaviourTest {

	@Test
	void aFoldedNodeKeepsStoredChildrenAndShowsNone() throws Exception {
		Opened opened = MapFixture.open("Parent");
		MindMapNodeModel parent = (MindMapNodeModel) opened.child("Parent");
		parent.insert(new MindMapNodeModel("Child", opened.mode.getMindMapMapModel()), 0);
		opened.mode.setFolded(parent, true);
		assertEquals(List.of("Child"), texts(NodeWalks.stored(parent)));
		assertFalse(NodeWalks.foldedAway(parent).hasNext());
	}

	private static List<String> texts(ListIterator<MindMapNode> nodes) {
		List<String> texts = new ArrayList<>();
		while (nodes.hasNext()) {
			texts.add(nodes.next().getText());
		}
		return texts;
	}
}
