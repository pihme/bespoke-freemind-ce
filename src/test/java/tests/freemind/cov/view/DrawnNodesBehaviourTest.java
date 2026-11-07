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

package tests.freemind.cov.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringWriter;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.controller.Controller;
import freemind.controller.filter.Filter;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.MindMapNodeModel;
import freemind.view.mindmapview.DrawnNodes;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("DrawnNodesBehaviourTest")
class DrawnNodesBehaviourTest {

	@Test
	void foldingAndTheFilterDecideWhatIsDrawnAndTheFileKeepsTheChildren() throws Exception {
		Opened opened = MapFixture.open("Alpha", "Beta");
		MindMapNode alpha = opened.child("Alpha");
		MindMapNode beta = opened.child("Beta");
		MindMapNode grand = new MindMapNodeModel("Grand", opened.mode.getMap());
		alpha.insert(grand, 0);
		opened.mode.getMap().nodeStructureChanged(alpha);
		assertNotNull(opened.view.getNodeView(grand));

		alpha.setFolded(true);
		opened.mode.getMap().nodeStructureChanged(alpha);
		assertTrue(DrawnNodes.sequence(alpha).isEmpty());
		assertEquals(1, alpha.getChildCount());
		assertEquals(null, opened.view.getNodeView(grand));
		String folded = save(alpha, opened, true);
		assertTrue(folded.contains("FOLDED"));
		assertTrue(folded.contains("Grand"));

		alpha.setFolded(false);
		opened.mode.getMap().setFilter(hiding(beta));
		opened.mode.getMap().nodeStructureChanged(opened.root);
		List<MindMapNode> painted = DrawnNodes.sequence(opened.root);
		assertEquals(List.of(alpha), painted);
		assertTrue(DrawnNodes.pictures(opened.root).contains(beta));
		assertFalse(DrawnNodes.showsContent(beta));
		assertNotNull(opened.view.getNodeView(beta));
		assertFalse(opened.view.getNodeView(beta).isContentVisible());

		String everything = save(opened.root, opened, true);
		String visibleOnly = save(opened.root, opened, false);
		assertTrue(everything.contains("Beta"));
		assertFalse(visibleOnly.contains("Beta"));
		assertTrue(visibleOnly.contains("Alpha"));
		assertFalse(everything.contains("FILTER"));
	}

	private static String save(MindMapNode node, Opened opened, boolean saveHidden) throws Exception {
		StringWriter writer = new StringWriter();
		node.save(writer, opened.mode.getMap().getLinkRegistry(), saveHidden, true);
		return writer.toString();
	}

	private static Filter hiding(MindMapNode hidden) {
		return new Filter() {
			public void applyFilter(Controller controller) {
			}

			public boolean isVisible(MindMapNode node) {
				return node != hidden;
			}

			public boolean areMatchedShown() {
				return true;
			}

			public boolean areHiddenShown() {
				return false;
			}

			public boolean areAncestorsShown() {
				return true;
			}

			public boolean areDescendantsShown() {
				return true;
			}

			public boolean areEclipsedShown() {
				return false;
			}

			public Object getCondition() {
				return null;
			}
		};
	}
}
