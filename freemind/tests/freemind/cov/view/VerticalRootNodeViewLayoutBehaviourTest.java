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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.NodeAdapter;
import freemind.view.mindmapview.NodeMotionListenerView;
import freemind.view.mindmapview.NodeView;
import freemind.view.mindmapview.NodeViewLayout;
import freemind.view.mindmapview.VerticalRootNodeViewLayout;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("VerticalRootNodeViewLayoutBehaviourTest")
public class VerticalRootNodeViewLayoutBehaviourTest {

	@Test
	void rootPlacesBothSidesAndSharesTheOutPoint() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha");
		NodeView root = opened.view.getRoot();
		NodeView right = ViewMaps.childView(opened, "Alpha");
		NodeView left = ViewMaps.addChild(root, "Left", true);
		((NodeAdapter) right.getModel()).setShiftY(-15);
		((NodeAdapter) left.getModel()).setShiftY(20);
		root.invalidate();
		root.validate();
		assertTrue(root.getLayout() instanceof VerticalRootNodeViewLayout);
		assertTrue(root.getWidth() > 0);
		assertTrue(root.getHeight() > 0);
		NodeViewLayout layout = (NodeViewLayout) root.getLayout();
		Point center = layout.getMainViewInPoint(root);
		assertEquals(root.getMainView().getWidth() / 2, center.x);
		assertEquals(root.getMainView().getHeight() / 2, center.y);
		boolean old = ViewMaps.booleanField(VerticalRootNodeViewLayout.class,
				"USE_COMMON_OUT_POINT_FOR_ROOT_NODE");
		try {
			ViewMaps.setBooleanField(VerticalRootNodeViewLayout.class,
					"USE_COMMON_OUT_POINT_FOR_ROOT_NODE", true);
			Point leftOut = layout.getMainViewOutPoint(root, left, new Point(0, 0));
			Point rightOut = layout.getMainViewOutPoint(root, right, new Point(100, 0));
			assertEquals(0, leftOut.x);
			assertEquals(root.getMainView().getWidth() - 1, rightOut.x);
			ViewMaps.setBooleanField(VerticalRootNodeViewLayout.class,
					"USE_COMMON_OUT_POINT_FOR_ROOT_NODE", false);
			Point ellipse = layout.getMainViewOutPoint(root, right, new Point(-40, 10));
			assertNotNull(ellipse);
			assertTrue(ellipse.x >= 0);
		} finally {
			ViewMaps.setBooleanField(VerticalRootNodeViewLayout.class,
					"USE_COMMON_OUT_POINT_FOR_ROOT_NODE", old);
		}
		NodeMotionListenerView motion = right.getMotionListenerView();
		assertNotNull(motion);
		Point before = motion.getLocation();
		layout.layoutNodeMotionListenerView(motion);
		assertEquals(before, motion.getLocation());
	}
}
