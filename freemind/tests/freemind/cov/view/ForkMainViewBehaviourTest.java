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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;

import org.junit.jupiter.api.Test;

import freemind.modes.EdgeAdapter;
import freemind.modes.MindMapNode;
import freemind.view.mindmapview.MainView;
import freemind.view.mindmapview.NodeView;
import tests.freemind.cov.MapFixture.Opened;

public class ForkMainViewBehaviourTest {

	@Test
	void foldedLeftForkGrowsByTheFoldingMark() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha");
		NodeView alpha = ViewMaps.childView(opened, "Alpha");
		NodeView grand = ViewMaps.addChild(alpha, "Grand", false);
		alpha.getModel().setLeft(true);
		alpha.getModel().setFolded(true);
		((EdgeAdapter) alpha.getModel().getEdge()).setStyle(EdgeAdapter.EDGESTYLE_SHARP_BEZIER);
		((EdgeAdapter) alpha.getModel().getEdge()).setWidth(0);
		((EdgeAdapter) grand.getModel().getEdge()).setWidth(6);
		opened.view.validate();
		MainView fork = alpha.getMainView();
		assertEquals("freemind.view.mindmapview.ForkMainView", fork.getClass().getName());
		fork.setSize(90, 30);
		int markedWidth = (Integer) ViewMaps.call(fork, "getMainViewWidthWithFoldingMark",
				new Class<?>[0], new Object[0]);
		int markedHeight = (Integer) ViewMaps.call(fork, "getMainViewHeightWithFoldingMark",
				new Class<?>[0], new Object[0]);
		assertTrue(markedWidth > fork.getWidth());
		assertTrue(markedHeight >= fork.getHeight());
		assertTrue(fork.getDeltaX() > 0);
		Point center = (Point) ViewMaps.call(fork, "getCenterPoint", new Class<?>[0], new Object[0]);
		assertEquals(new Point(fork.getWidth() / 2, fork.getHeight() / 2), center);
		assertEquals(-1, ViewMaps.call(fork, "getAlignment", new Class<?>[0], new Object[0]));
		assertEquals(MindMapNode.STYLE_FORK, ViewMaps.call(fork, "getStyle", new Class<?>[0], new Object[0]));
		((EdgeAdapter) alpha.getModel().getEdge()).setStyle(EdgeAdapter.EDGESTYLE_SHARP_LINEAR);
		Point left = (Point) ViewMaps.call(fork, "getLeftPoint", new Class<?>[0], new Object[0]);
		Point right = (Point) ViewMaps.call(fork, "getRightPoint", new Class<?>[0], new Object[0]);
		assertTrue(right.x >= left.x);
		assertTrue(ViewMaps.ink(ViewMaps.paint(fork)) >= 0);
	}
}
