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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.EdgeAdapter;
import freemind.modes.mindmapmode.MindMapMapModel;
import freemind.view.mindmapview.BezierEdgeView;
import freemind.view.mindmapview.NodeView;
import freemind.view.mindmapview.VerticalRootNodeViewLayout;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("BezierEdgeViewBehaviourTest")
public class BezierEdgeViewBehaviourTest {

	@Test
	void curvesPaintForBothSidesAndAHiddenParent() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha");
		NodeView alpha = ViewMaps.childView(opened, "Alpha");
		NodeView left = ViewMaps.addChild(opened.view.getRoot(), "Left", true);
		NodeView grand = ViewMaps.addChild(alpha, "Grand", false);
		opened.view.validate();
		color(alpha, 4);
		color(left, 1);
		color(grand, 6);
		boolean old = ViewMaps.booleanField(VerticalRootNodeViewLayout.class,
				"USE_COMMON_OUT_POINT_FOR_ROOT_NODE");
		try {
			ViewMaps.setBooleanField(VerticalRootNodeViewLayout.class,
					"USE_COMMON_OUT_POINT_FOR_ROOT_NODE", false);
			assertTrue(ink(alpha) >= 0);
			assertTrue(ink(left) >= 0);
			ViewMaps.setBooleanField(VerticalRootNodeViewLayout.class,
					"USE_COMMON_OUT_POINT_FOR_ROOT_NODE", true);
			assertTrue(ink(alpha) >= 0);
		} finally {
			ViewMaps.setBooleanField(VerticalRootNodeViewLayout.class,
					"USE_COMMON_OUT_POINT_FOR_ROOT_NODE", old);
		}
		((MindMapMapModel) opened.view.getModel()).setFilter(new ViewMaps.HideText("Alpha"));
		try {
			assertTrue(ink(grand) >= 0);
		} finally {
			((MindMapMapModel) opened.view.getModel()).setFilter(null);
		}
		assertEquals(Color.BLUE, color(grand, 2).getColor());
	}

	private static EdgeAdapter color(NodeView node, int width) {
		EdgeAdapter edge = (EdgeAdapter) node.getModel().getEdge();
		edge.setColor(Color.BLUE);
		edge.setWidth(width);
		edge.setStyle(EdgeAdapter.EDGESTYLE_BEZIER);
		return edge;
	}

	private static int ink(NodeView target) {
		BufferedImage image = new BufferedImage(500, 400, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		new BezierEdgeView().paint(target, graphics);
		graphics.dispose();
		return ViewMaps.ink(image);
	}
}
