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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.MindMapNode;
import freemind.view.mindmapview.MainView;
import freemind.view.mindmapview.NodeView;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("BubbleMainViewBehaviourTest")
public class BubbleMainViewBehaviourTest {

	@Test
	void bubbleReportsCenterTextAndFoldingWidth() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha");
		NodeView alpha = ViewMaps.childView(opened, "Alpha");
		alpha.getModel().setStyle(MindMapNode.STYLE_BUBBLE);
		alpha.getModel().setLeft(true);
		alpha.getModel().setFolded(true);
		alpha.treeNodesChanged(new javax.swing.event.TreeModelEvent(alpha.getModel(),
				new Object[] { alpha.getModel() }));
		opened.view.validate();
		MainView bubble = alpha.getMainView();
		assertEquals("freemind.view.mindmapview.BubbleMainView", bubble.getClass().getName());
		bubble.setSize(100, 40);
		Point center = (Point) ViewMaps.call(bubble, "getCenterPoint", new Class<?>[0], new Object[0]);
		assertEquals(bubble.getWidth() / 2, center.x);
		assertEquals(0, ViewMaps.call(bubble, "getAlignment", new Class<?>[0], new Object[0]));
		assertTrue(bubble.getTextWidth() >= bubble.getWidth() - 20);
		int textX = (Integer) ViewMaps.call(bubble, "getTextX", new Class<?>[0], new Object[0]);
		assertEquals(bubble.getTextX(), textX);
		int marked = (Integer) ViewMaps.call(bubble, "getMainViewWidthWithFoldingMark",
				new Class<?>[0], new Object[0]);
		assertTrue(marked > bubble.getWidth());
		assertTrue(bubble.getDeltaX() > 0);
		assertEquals(MindMapNode.STYLE_BUBBLE, ViewMaps.call(bubble, "getStyle", new Class<?>[0], new Object[0]));
		Point left = (Point) ViewMaps.call(bubble, "getLeftPoint", new Class<?>[0], new Object[0]);
		assertEquals(0, left.x);
		assertTrue(ViewMaps.ink(ViewMaps.paint(bubble)) >= 0);
		opened.view.selectAsTheOnlyOneSelected(alpha);
		assertTrue(bubble.getWidth() > 0);
		assertTrue(ViewMaps.ink(ViewMaps.paint(bubble)) >= 0);
	}
}
