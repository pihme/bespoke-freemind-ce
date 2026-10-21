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

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.MindMapArrowLink;
import freemind.modes.NodeAdapter;
import freemind.modes.mindmapmode.MindMapMapModel;
import freemind.view.mindmapview.ArrowLinkView;
import freemind.view.mindmapview.NodeView;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("ArrowLinkViewBehaviourTest")
public class ArrowLinkViewBehaviourTest {

	static final class OpenedLink extends ArrowLinkView {
		OpenedLink(MindMapArrowLink link, NodeView source, NodeView target) {
			super(link, source, target);
		}
	}

	@Test
	void changeInclinationLeavesTheLinkModel() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha", "Beta");
		NodeView alpha = ViewMaps.childView(opened, "Alpha");
		NodeView beta = ViewMaps.childView(opened, "Beta");
		MindMapMapModel model = (MindMapMapModel) opened.view.getModel();
		MindMapArrowLink link = model.createArrowLinkAdapter(
				(NodeAdapter) alpha.getModel(), (NodeAdapter) beta.getModel());
		OpenedLink view = new OpenedLink(link, alpha, beta);
		view.changeInclination(1, 2, 3, 4);
		assertSame(link, view.getModel());
	}
}
