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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

import javax.swing.ImageIcon;
import javax.swing.event.TreeModelEvent;

import org.junit.jupiter.api.Test;

import freemind.modes.MindMapNode;
import freemind.modes.NodeAdapter;
import freemind.modes.attributes.Attribute;
import freemind.modes.mindmapmode.MindMapCloudModel;
import freemind.modes.mindmapmode.MindMapMapModel;
import freemind.modes.mindmapmode.MindMapNodeModel;
import freemind.view.mindmapview.NodeFoldingComponent;
import freemind.view.mindmapview.NodeView;
import tests.freemind.cov.MapFixture.Opened;

public class NodeViewBehaviourTest {

	@Test
	void navigationWalksSiblingsPagesAndHiddenBranches() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha", "Beta", "Gamma");
		NodeView root = opened.view.getRoot();
		NodeView alpha = ViewMaps.childView(opened, "Alpha");
		NodeView beta = ViewMaps.childView(opened, "Beta");
		NodeView left = ViewMaps.addChild(root, "Left", true);
		NodeView mid = ViewMaps.addChild(alpha, "Mid", false);
		NodeView grand = ViewMaps.addChild(mid, "Grand", false);
		opened.view.setSiblingMaxLevel(6);
		opened.view.validate();

		assertSameNode(root, ViewMaps.call(root, "getNextPage", new Class<?>[0], new Object[0]));
		assertSameNode(root, ViewMaps.call(root, "getPreviousPage", new Class<?>[0], new Object[0]));
		assertNotNull(ViewMaps.call(alpha, "getNextPage", new Class<?>[0], new Object[0]));
		assertNotNull(ViewMaps.call(gamma(opened), "getPreviousPage", new Class<?>[0], new Object[0]));
		assertNotNull(ViewMaps.call(alpha, "getNextVisibleSibling", new Class<?>[0], new Object[0]));
		assertNotNull(ViewMaps.call(gamma(opened), "getPreviousVisibleSibling", new Class<?>[0], new Object[0]));
		assertNotNull(ViewMaps.call(alpha, "getNextSiblingSingle", new Class<?>[0], new Object[0]));
		assertNotNull(ViewMaps.call(gamma(opened), "getPreviousSiblingSingle", new Class<?>[0], new Object[0]));

		@SuppressWarnings("unchecked")
		LinkedList<NodeView> lefts = (LinkedList<NodeView>) ViewMaps.call(root, "getLeft",
				new Class<?>[] { boolean.class }, new Object[] { true });
		@SuppressWarnings("unchecked")
		LinkedList<NodeView> rights = (LinkedList<NodeView>) ViewMaps.call(root, "getRight",
				new Class<?>[] { boolean.class }, new Object[] { false });
		assertTrue(lefts.contains(left));
		assertTrue(rights.contains(alpha));
		assertTrue(alpha.isSiblingOf(beta));
		assertFalse(alpha.isSiblingOf(grand));
		assertTrue(alpha.isChildOf(root));
		assertTrue(root.isParentOf(alpha));
		assertFalse(alpha.isChildOf(beta));

		((MindMapMapModel) opened.view.getModel()).setFilter(new ViewMaps.HideText("Mid"));
		try {
			NodeView drilled = (NodeView) ViewMaps.call(alpha, "getNextSiblingSingle",
					new Class<?>[0], new Object[0]);
			assertNotNull(drilled);
			assertFalse(mid.isContentVisible());
			assertTrue((Boolean) ViewMaps.call(mid, "isParentHidden", new Class<?>[0], new Object[0])
					|| alpha.getVisibleParentView() == root);
			NodeView first = (NodeView) ViewMaps.call(alpha, "getFirst",
					new Class<?>[] { Component.class, boolean.class, boolean.class },
					new Object[] { null, false, false });
			assertEquals(grand, first);
			NodeView last = (NodeView) ViewMaps.call(alpha, "getLast",
					new Class<?>[] { Component.class, boolean.class, boolean.class },
					new Object[] { null, false, true });
			assertTrue(last == null || last == grand || !last.isLeft());
			assertFalse(mid.isContentVisible());
			assertEquals(alpha, mid.getVisibleParentView());
			assertEquals(root, alpha.getVisibleParentView());
		} finally {
			((MindMapMapModel) opened.view.getModel()).setFilter(null);
		}
		assertNotNull(root.getPreferredVisibleChild(false));
		assertNotNull(root.getPreferredVisibleChild(true));
		mid.setPreferredChild(grand);
		((MindMapMapModel) opened.view.getModel()).setFilter(new ViewMaps.HideText("Mid"));
		try {
			NodeView preferred = alpha.getPreferredVisibleChild(false);
			assertTrue(preferred == grand || preferred == null || preferred.isContentVisible());
		} finally {
			((MindMapMapModel) opened.view.getModel()).setFilter(null);
		}
		assertNull(grand.getPreferredVisibleChild(false));
	}

	private static TreeModelEvent event(MindMapNodeModel model) {
		return new TreeModelEvent(model, new Object[] { model });
	}

	private static NodeView gamma(Opened opened) {
		return ViewMaps.childView(opened, "Gamma");
	}

	private static void assertSameNode(NodeView expected, Object actual) {
		assertEquals(expected, actual);
	}

	@Test
	void textIconsCloudAndToolTipFollowTheModel() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha");
		NodeView alpha = ViewMaps.childView(opened, "Alpha");
		MindMapNodeModel model = (MindMapNodeModel) alpha.getModel();
		opened.frame.setProperty("html_long_node_head", "<meta name=\"x\" content=\"y\">");
		opened.frame.setProperty("max_node_width", "40");
		java.lang.reflect.Field maxWidth = opened.view.getClass().getDeclaredField("maxNodeWidth");
		maxWidth.setAccessible(true);
		maxWidth.setInt(opened.view, 0);
		model.setText("one\ntwo");
		alpha.treeNodesChanged(event(model));
		assertTrue(alpha.getIsLong());
		assertTrue(alpha.getText().contains("one") || alpha.getText().contains("<"));

		model.setText("<html><img src=\"pic.png\">wide text for the node</html>");
		alpha.treeNodesChanged(event(model));
		assertTrue(alpha.getText().contains("base") || alpha.getText().contains("img"));

		model.setText("<html><head><title>T</title></head><body>Body</body></html>");
		alpha.treeNodesChanged(event(model));
		assertTrue(alpha.getText().contains("meta") || alpha.getText().contains("head"));

		StringBuilder huge = new StringBuilder("<html><body>");
		for (int i = 0; i < 31000; i++) {
			huge.append('a');
		}
		huge.append("</body></html>");
		model.setText(huge.toString());
		alpha.treeNodesChanged(event(model));
		assertTrue(alpha.getText().contains("width="));

		model.setText("<table>\n\t\ncol\tcell\n");
		alpha.treeNodesChanged(event(model));
		assertTrue(alpha.getText().contains("<table"));

		model.setText("short");
		((NodeAdapter) model).setLink("#local");
		((NodeAdapter) model).addIcon(freemind.modes.MindIcon.factory("idea",
				new ImageIcon(new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB))), 0);
		((NodeAdapter) model).setStateIcon("mark",
				new ImageIcon(new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB)));
		model.addAttribute(new Attribute("k", "v"));
		alpha.treeNodesChanged(event(model));
		assertNotNull(alpha.getMainView().getIcon());
		((NodeAdapter) model).setLink("mailto:a@b.c");
		alpha.treeNodesChanged(event(model));
		((NodeAdapter) model).setLink("note.exe");
		alpha.treeNodesChanged(event(model));
		((NodeAdapter) model).setLink("http://example.test");
		alpha.treeNodesChanged(event(model));
		assertNotNull(alpha.getMainView().getIcon());

		model.setToolTip("note", "see</html>more");
		alpha.updateToolTip();
		assertTrue(alpha.getMainView().getToolTipText().contains("see"));
		model.setToolTip("note", null);
		alpha.updateToolTip();
		assertNull(alpha.getMainView().getToolTipText());

		opened.frame.setProperty("max_tooltip_width", "nope");
		java.lang.reflect.Field tipWidth = NodeView.class.getDeclaredField("maxToolTipWidth");
		tipWidth.setAccessible(true);
		tipWidth.setInt(alpha, 0);
		assertEquals(600, alpha.getMaxToolTipWidth());

		MindMapCloudModel cloud = new MindMapCloudModel(model, opened.mode);
		cloud.setColor(Color.CYAN);
		model.setCloud(cloud);
		assertEquals(Color.CYAN, alpha.getTextBackground());
		assertTrue(alpha.getAdditionalCloudHeigth() > 0);
		LinkedList<Point> points = new LinkedList<>();
		alpha.getCoordinates(points);
		assertTrue(points.size() >= 4);
		opened.root.setCloud(new MindMapCloudModel(opened.root, opened.mode));
		points.clear();
		opened.view.getRoot().getCoordinates(points);
		assertFalse(points.isEmpty());

		Color contrary = (Color) ViewMaps.call(alpha, "getAntiColor2",
				new Class<?>[] { Color.class }, new Object[] { Color.BLUE });
		assertNotEquals(Color.BLUE, contrary);
		assertEquals("fork", ViewMaps.call(alpha, "getStyle", new Class<?>[0], new Object[0]));
		assertEquals(Integer.valueOf(-1), ViewMaps.call(alpha, "getAlignment", new Class<?>[0], new Object[0]));
		ViewMaps.call(alpha, "setModel", new Class<?>[] { MindMapNode.class },
				new Object[] { opened.root });
		assertEquals("ROOT", alpha.getModel().getText());
		ViewMaps.call(alpha, "setModel", new Class<?>[] { MindMapNode.class }, new Object[] { model });
		assertEquals(model, alpha.getModel());
	}

	@Test
	void structureChangesFoldingAndPaint() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha", "Beta");
		NodeView root = opened.view.getRoot();
		NodeView alpha = ViewMaps.childView(opened, "Alpha");
		NodeView beta = ViewMaps.childView(opened, "Beta");
		NodeView grand = ViewMaps.addChild(alpha, "Grand", false);
		opened.view.validate();
		ViewMaps.call(alpha, "update", new Class<?>[] {}, new Object[] {});
		assertNotNull(alpha.getMotionListenerView());
		alpha.setVisible(false);
		assertFalse(alpha.isVisible());
		LinkedList<Point> hidden = new LinkedList<>();
		alpha.getCoordinates(hidden);
		assertTrue(hidden.isEmpty());
		alpha.setVisible(true);
		alpha.setSize(300, 200);
		assertFalse(alpha.contains(0, 0));
		assertTrue(alpha.contains(150, 100));

		NodeFoldingComponent fold = folding(alpha);
		assertNotNull(fold);
		boolean wasFolded = alpha.getModel().isFolded();
		for (ActionListener listener : fold.getActionListeners()) {
			listener.actionPerformed(new ActionEvent(fold, ActionEvent.ACTION_PERFORMED, "fold"));
		}
		assertNotEquals(wasFolded, alpha.getModel().isFolded());

		MindMapNodeModel extra = new MindMapNodeModel("Extra", opened.root.getMap());
		alpha.getModel().setFolded(true);
		((MindMapNodeModel) alpha.getModel()).insert(extra, alpha.getModel().getChildCount());
		alpha.treeNodesInserted(new TreeModelEvent(alpha.getModel(), new Object[] { alpha.getModel() },
				new int[] { alpha.getModel().getIndex(extra) }, new Object[] { extra }));
		assertNull(opened.view.getNodeView(extra));
		alpha.getModel().setFolded(false);
		alpha.treeNodesInserted(new TreeModelEvent(alpha.getModel(), new Object[] { alpha.getModel() },
				new int[] { alpha.getModel().getIndex(extra) }, new Object[] { extra }));
		assertNotNull(opened.view.getNodeView(extra));

		alpha.treeStructureChanged(new TreeModelEvent(alpha.getModel(), new Object[] { alpha.getModel() }));
		MindMapNode grandModel = grand.getModel();
		NodeView liveGrand = opened.view.getNodeView(grandModel);
		assertNotNull(liveGrand);
		int index = componentIndex(alpha, liveGrand);
		alpha.setPreferredChild(liveGrand);
		alpha.treeNodesRemoved(new TreeModelEvent(alpha.getModel(), new Object[] { alpha.getModel() },
				new int[] { index }, new Object[] { grandModel }));
		assertNull(opened.view.getNodeView(grandModel));

		BufferedImage painted = ViewMaps.paint(root);
		assertTrue(ViewMaps.ink(painted) > 0);
		((MindMapMapModel) opened.view.getModel()).setFilter(new ViewMaps.HideText("Beta"));
		try {
			BufferedImage again = ViewMaps.paint(root);
			assertNotNull(again);
			assertTrue((Boolean) ViewMaps.call(beta, "isParentHidden", new Class<?>[0], new Object[0])
					|| !beta.isContentVisible());
		} finally {
			((MindMapMapModel) opened.view.getModel()).setFilter(null);
		}
		root.setPreferredChild(alpha);
		assertEquals(alpha, root.getPreferredVisibleChild(false));
		ViewMaps.call(beta, "remove", new Class<?>[0], new Object[0]);
		assertNull(opened.view.getNodeView(opened.child("Beta")));
	}

	private static int componentIndex(NodeView parent, NodeView child) {
		for (int i = 0; i < parent.getComponentCount(); i++) {
			if (parent.getComponent(i) == child) {
				return i;
			}
		}
		throw new AssertionError("missing child component");
	}

	private static NodeFoldingComponent folding(NodeView view) {
		for (Component component : view.getComponents()) {
			if (component instanceof NodeFoldingComponent) {
				return (NodeFoldingComponent) component;
			}
		}
		return null;
	}
}
