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
import freemind.view.mindmapview.LeftNodeViewLayout;
import freemind.view.mindmapview.NodeMotionListenerView;
import freemind.view.mindmapview.NodeView;
import freemind.view.mindmapview.NodeViewLayout;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("LeftNodeViewLayoutBehaviourTest")
public class LeftNodeViewLayoutBehaviourTest {

	@Test
	void leftChildrenSitBesideTheContent() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha");
		NodeView left = ViewMaps.addChild(opened.view.getRoot(), "Left", true);
		NodeView grand = ViewMaps.addChild(left, "Grand", true);
		((NodeAdapter) grand.getModel()).setShiftY(-25);
		left.invalidate();
		left.validate();
		assertTrue(left.getLayout() instanceof LeftNodeViewLayout);
		assertTrue(left.getWidth() > 0);
		assertTrue(grand.getX() <= left.getContent().getX());
		NodeViewLayout layout = (NodeViewLayout) left.getLayout();
		Point in = layout.getMainViewInPoint(left);
		Point out = layout.getMainViewOutPoint(left, grand, new Point(0, 0));
		assertEquals(left.getMainView().getWidth() - 1, in.x);
		assertEquals(0, out.x);
		NodeMotionListenerView motion = left.getMotionListenerView();
		assertNotNull(motion);
		layout.layoutNodeMotionListenerView(motion);
		assertEquals(10, motion.getWidth());
		assertEquals(left.getContent().getHeight(), motion.getHeight());
	}
}
