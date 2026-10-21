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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Dimension;
import java.awt.LayoutManager;
import java.lang.reflect.Method;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.EdgeAdapter;
import freemind.modes.MindMapNode;
import freemind.modes.NodeAdapter;
import freemind.view.mindmapview.MainView;
import freemind.view.mindmapview.NodeView;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("NodeViewFactoryBehaviourTest")
public class NodeViewFactoryBehaviourTest {

	@Test
	void factoryPicksMainViewsEdgesAndContentBounds() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha");
		NodeView alpha = ViewMaps.childView(opened, "Alpha");
		Class<?> factoryClass = Class.forName("freemind.view.mindmapview.NodeViewFactory");
		Method getInstance = factoryClass.getDeclaredMethod("getInstance");
		getInstance.setAccessible(true);
		Object factory = getInstance.invoke(null);
		Method newMain = factoryClass.getDeclaredMethod("newMainView", MindMapNode.class);
		newMain.setAccessible(true);
		assertTrue(name(newMain.invoke(factory, opened.root)).endsWith("RootMainView"));
		assertTrue(name(newMain.invoke(factory, alpha.getModel())).endsWith("ForkMainView"));
		((NodeAdapter) alpha.getModel()).setStyle(MindMapNode.STYLE_BUBBLE);
		assertTrue(name(newMain.invoke(factory, alpha.getModel())).endsWith("BubbleMainView"));
		((NodeAdapter) alpha.getModel()).setStyle("no-such-style");
		assertTrue(name(newMain.invoke(factory, alpha.getModel())).endsWith("ForkMainView"));
		((NodeAdapter) alpha.getModel()).setStyle(MindMapNode.STYLE_FORK);

		Method getEdge = factoryClass.getDeclaredMethod("getEdge", NodeView.class);
		getEdge.setAccessible(true);
		EdgeAdapter edge = (EdgeAdapter) alpha.getModel().getEdge();
		edge.setStyle(EdgeAdapter.EDGESTYLE_LINEAR);
		Object linear = getEdge.invoke(factory, alpha);
		assertSame(linear, getEdge.invoke(factory, alpha));
		assertTrue(name(linear).endsWith("LinearEdgeView"));
		edge.setStyle(EdgeAdapter.EDGESTYLE_BEZIER);
		assertTrue(name(getEdge.invoke(factory, alpha)).endsWith("BezierEdgeView"));
		edge.setStyle(EdgeAdapter.EDGESTYLE_SHARP_LINEAR);
		assertTrue(name(getEdge.invoke(factory, alpha)).endsWith("SharpLinearEdgeView"));
		edge.setStyle(EdgeAdapter.EDGESTYLE_SHARP_BEZIER);
		assertTrue(name(getEdge.invoke(factory, alpha)).endsWith("SharpBezierEdgeView"));

		JComponent content = (JComponent) alpha.getContentPane();
		LayoutManager layout = content.getLayout();
		assertTrue(layout != null);
		JLabel extra = new JLabel("extra");
		extra.setAlignmentX(0.5f);
		JLabel hidden = new JLabel("hidden");
		hidden.setVisible(false);
		content.add(extra);
		content.add(hidden);
		content.setSize(new Dimension(160, 80));
		int count = content.getComponentCount();
		layout.addLayoutComponent("extra", extra);
		layout.layoutContainer(content);
		layout.removeLayoutComponent(extra);
		assertEquals(count, content.getComponentCount());
		assertEquals(160, alpha.getMainView().getWidth());
		assertTrue(extra.getHeight() > 0);
		assertEquals(0, hidden.getWidth());
		Dimension minimum = layout.minimumLayoutSize(content);
		assertTrue(minimum.height >= extra.getHeight());
		assertTrue(alpha.getMainView() instanceof MainView);
	}

	private static String name(Object value) {
		return value.getClass().getName();
	}
}
