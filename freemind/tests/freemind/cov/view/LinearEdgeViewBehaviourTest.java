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

import org.junit.jupiter.api.Test;

import freemind.modes.EdgeAdapter;
import freemind.modes.mindmapmode.MindMapMapModel;
import freemind.view.mindmapview.LinearEdgeView;
import freemind.view.mindmapview.NodeView;
import tests.freemind.cov.MapFixture.Opened;

public class LinearEdgeViewBehaviourTest {

	@Test
	void thinThickAndEclipsedStrokesPaint() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha");
		NodeView alpha = ViewMaps.childView(opened, "Alpha");
		NodeView grand = ViewMaps.addChild(alpha, "Grand", false);
		grand.getModel().setLeft(true);
		opened.view.validate();
		EdgeAdapter edge = (EdgeAdapter) grand.getModel().getEdge();
		edge.setColor(Color.RED);
		edge.setWidth(1);
		edge.setStyle(EdgeAdapter.EDGESTYLE_LINEAR);
		assertTrue(ink(grand) >= 0);
		edge.setWidth(8);
		assertTrue(ink(grand) >= 0);
		((MindMapMapModel) opened.view.getModel()).setFilter(new ViewMaps.HideText("Alpha"));
		try {
			assertTrue((Boolean) ViewMaps.call(grand, "isParentHidden", new Class<?>[0], new Object[0]));
			assertTrue(ink(grand) >= 0);
		} finally {
			((MindMapMapModel) opened.view.getModel()).setFilter(null);
		}
		LinearEdgeView view = new LinearEdgeView();
		ViewMaps.call(view, "reset", new Class<?>[0], new Object[0]);
		assertEquals(Color.RED, edge.getColor());
	}

	private static int ink(NodeView target) {
		BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		new LinearEdgeView().paint(target, graphics);
		graphics.dispose();
		return ViewMaps.ink(image);
	}
}
