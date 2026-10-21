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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.view.mindmapview.MainView;
import freemind.view.mindmapview.NodeView;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("RootMainViewBehaviourTest")
public class RootMainViewBehaviourTest {

	@Test
	void rootDropAndPointsSplitTheEllipse() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha");
		opened.view.validate();
		MainView root = opened.view.getRoot().getMainView();
		assertEquals("freemind.view.mindmapview.RootMainView", root.getClass().getName());
		root.setSize(Math.max(root.getWidth(), 120), Math.max(root.getHeight(), 40));
		assertFalse(root.dropAsSibling(1));
		assertTrue(root.dropPosition(1));
		assertFalse(root.dropPosition(root.getWidth() - 1));
		root.setDraggedOver(new Point(1, 1));
		assertEquals(NodeView.DRAGGED_OVER_SON_LEFT, root.getDraggedOver());
		root.setDraggedOver(new Point(root.getWidth() - 1, 1));
		assertEquals(NodeView.DRAGGED_OVER_SON, root.getDraggedOver());
		Point left = (Point) ViewMaps.call(root, "getLeftPoint", new Class<?>[0], new Object[0]);
		Point center = (Point) ViewMaps.call(root, "getCenterPoint", new Class<?>[0], new Object[0]);
		Point right = (Point) ViewMaps.call(root, "getRightPoint", new Class<?>[0], new Object[0]);
		assertEquals(0, left.x);
		assertEquals(root.getWidth() / 2, center.x);
		assertEquals(root.getWidth() - 1, right.x);
		assertEquals(0, ViewMaps.call(root, "getAlignment", new Class<?>[0], new Object[0]));
		assertTrue(root.getTextX() >= 0);
		BufferedImage image = new BufferedImage(root.getWidth(), root.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		root.paint(graphics);
		root.paintSelected(graphics);
		graphics.dispose();
		assertTrue(ViewMaps.ink(image) > 0);
	}
}
