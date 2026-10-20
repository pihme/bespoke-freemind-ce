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

import java.awt.Point;

import org.junit.jupiter.api.Test;

import freemind.modes.NodeAdapter;
import freemind.modes.mindmapmode.MindMapMapModel;
import freemind.view.mindmapview.NodeMotionListenerView;
import freemind.view.mindmapview.NodeView;
import freemind.view.mindmapview.NodeViewLayout;
import freemind.view.mindmapview.RightNodeViewLayout;
import tests.freemind.cov.MapFixture.Opened;

public class RightNodeViewLayoutBehaviourTest {

	@Test
	void rightChildrenSitBesideTheContent() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha");
		NodeView alpha = ViewMaps.childView(opened, "Alpha");
		NodeView neg = ViewMaps.addChild(alpha, "Neg", false);
		NodeView hidden = ViewMaps.addChild(alpha, "Hid", false);
		((NodeAdapter) neg.getModel()).setShiftY(-20);
		((NodeAdapter) hidden.getModel()).setShiftY(15);
		((MindMapMapModel) opened.view.getModel()).setFilter(new ViewMaps.HideText("Hid"));
		try {
			alpha.invalidate();
			alpha.validate();
			assertTrue(alpha.getLayout() instanceof RightNodeViewLayout);
			assertFalse(hidden.getContent().isVisible());
			assertTrue(neg.getX() >= alpha.getContent().getX());
			assertTrue(alpha.getHeight() > 0);
		} finally {
			((MindMapMapModel) opened.view.getModel()).setFilter(null);
		}
		NodeViewLayout layout = (NodeViewLayout) alpha.getLayout();
		Point in = layout.getMainViewInPoint(alpha);
		Point out = layout.getMainViewOutPoint(alpha, neg, new Point(1, 1));
		assertEquals(0, in.x);
		assertEquals(alpha.getMainView().getWidth() - 1, out.x);
		NodeMotionListenerView motion = alpha.getMotionListenerView();
		assertNotNull(motion);
		layout.layoutNodeMotionListenerView(motion);
		assertEquals(10, motion.getWidth());
	}
}
