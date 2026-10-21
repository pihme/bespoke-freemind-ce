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

import java.awt.Dimension;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.NodeAdapter;
import freemind.modes.mindmapmode.MindMapMapModel;
import freemind.view.mindmapview.NodeView;
import freemind.view.mindmapview.NodeViewLayoutAdapter;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("NodeViewLayoutAdapterBehaviourTest")
public class NodeViewLayoutAdapterBehaviourTest {

	@Test
	void negativeShiftAndHiddenContentResizeTheParent() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha");
		NodeView alpha = ViewMaps.childView(opened, "Alpha");
		NodeView neg = ViewMaps.addChild(alpha, "Neg", false);
		NodeView pos = ViewMaps.addChild(alpha, "Pos", false);
		((NodeAdapter) neg.getModel()).setShiftY(-40);
		((NodeAdapter) pos.getModel()).setShiftY(30);
		NodeViewLayoutAdapter layout = (NodeViewLayoutAdapter) alpha.getLayout();
		int count = alpha.getComponentCount();
		layout.addLayoutComponent("child", neg);
		layout.removeLayoutComponent(neg);
		assertEquals(count, alpha.getComponentCount());
		Dimension minimum = layout.minimumLayoutSize(alpha);
		assertEquals(0, minimum.width);
		assertEquals(0, minimum.height);
		alpha.invalidate();
		alpha.validate();
		assertTrue(alpha.getWidth() > 0);
		assertTrue(alpha.getHeight() > 0);
		assertTrue(neg.getY() != pos.getY());
		((MindMapMapModel) opened.view.getModel()).setFilter(new ViewMaps.HideText("Alpha"));
		try {
			alpha.invalidate();
			alpha.validate();
			assertFalse(alpha.getContent().isVisible());
			assertEquals(0, alpha.getContent().getWidth());
		} finally {
			((MindMapMapModel) opened.view.getModel()).setFilter(null);
		}
	}
}
