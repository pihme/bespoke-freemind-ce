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

package tests.freemind.cov.mindmapmode;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.MindMapArrowLinkModel;
import freemind.view.mindmapview.NodeView;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Dragging a link end scales, flattens, and flips its inclination. */
@DisplayName("MindMapArrowLinkModelBehaviourTest")
class MindMapArrowLinkModelBehaviourTest {

	@Test
	void inclinationsScaleFlattenAndFlipWithTheCloserEnd() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode child = opened.child("Child");
		MindMapArrowLinkModel link = new MindMapArrowLinkModel(opened.root, child, opened.mode);
		NodeView rootView = opened.view.getNodeView(opened.root);
		NodeView childView = opened.view.getNodeView(child);
		rootView.setBounds(10, 10, 120, 40);
		childView.setBounds(420, 280, 120, 40);
		opened.root.setLeft(false);
		child.setLeft(false);

		link.setStartInclination(new Point(0, 0));
		link.setEndInclination(new Point(40, 40));
		Point source = opened.view.getNodeView(opened.root).getLinkPoint(link.getStartInclination());
		link.changeInclination(opened.view, source.x, source.y, 0, 0);
		assertEquals(new Point(10, 0), link.getStartInclination());

		link.setStartInclination(new Point(3, 4));
		source = opened.view.getNodeView(opened.root).getLinkPoint(link.getStartInclination());
		link.changeInclination(opened.view, source.x, source.y, 0, 0);
		assertEquals(new Point(6, 8), link.getStartInclination());

		link.setStartInclination(new Point(100, 1));
		source = opened.view.getNodeView(opened.root).getLinkPoint(link.getStartInclination());
		link.changeInclination(opened.view, source.x, source.y, 0, 0);
		assertEquals(new Point(100, 0), link.getStartInclination());

		link.setStartInclination(new Point(40, 40));
		source = opened.view.getNodeView(opened.root).getLinkPoint(link.getStartInclination());
		link.changeInclination(opened.view, source.x, source.y, 0, 0);
		assertEquals(new Point(40, 40), link.getStartInclination());

		opened.root.setLeft(true);
		link.setStartInclination(new Point(40, 0));
		source = opened.view.getNodeView(opened.root).getLinkPoint(link.getStartInclination());
		link.changeInclination(opened.view, source.x, source.y, 5, 0);
		assertEquals(new Point(35, 0), link.getStartInclination());

		link.setEndInclination(new Point(1, 0));
		Point target = opened.view.getNodeView(child).getLinkPoint(link.getEndInclination());
		link.changeInclination(opened.view, target.x, target.y, 0, 0);
		assertEquals(new Point(10, 0), link.getEndInclination());
	}
}
