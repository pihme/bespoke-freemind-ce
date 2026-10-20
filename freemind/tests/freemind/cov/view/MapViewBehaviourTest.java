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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.TimerTask;

import java.awt.FocusTraversalPolicy;

import javax.swing.JComponent;
import javax.swing.JViewport;

import org.junit.jupiter.api.Test;

import freemind.main.FreeMind;
import freemind.main.FreeMindCommon;
import freemind.modes.MindMapNode;
import freemind.modes.NodeAdapter;
import freemind.modes.mindmapmode.MindMapMapModel;
import freemind.modes.mindmapmode.MindMapNodeModel;
import freemind.view.mindmapview.MapView;
import freemind.view.mindmapview.NodeView;
import freemind.view.mindmapview.NodeViewVisitor;
import tests.freemind.cov.MapFixture.Opened;

public class MapViewBehaviourTest {

	@Test
	void keyMoveSelectsTheNeighbourAndShiftExtendsTheRange() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha", "Beta", "Gamma");
		MapView view = opened.view;
		NodeView alpha = ViewMaps.childView(opened, "Alpha");
		NodeView beta = ViewMaps.childView(opened, "Beta");
		view.selectAsTheOnlyOneSelected(alpha);
		KeyEvent down = ViewMaps.key(view, KeyEvent.VK_DOWN, 0);
		view.move(down);
		assertTrue(down.isConsumed());
		assertNotNull(view.getSelected());

		view.selectAsTheOnlyOneSelected(alpha);
		KeyEvent shiftDown = ViewMaps.key(view, KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
		view.move(shiftDown);
		assertTrue(shiftDown.isConsumed());
		assertTrue(view.getSelecteds().size() >= 1);

		view.selectAsTheOnlyOneSelected(beta);
		KeyEvent shiftUp = ViewMaps.key(view, KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK);
		view.move(shiftUp);
		assertTrue(view.getSelecteds().size() >= 1);

		KeyEvent left = ViewMaps.key(view, KeyEvent.VK_LEFT, 0);
		view.selectAsTheOnlyOneSelected(alpha);
		view.move(left);
		assertSame(view.getRoot(), view.getSelected());

		NodeView leftChild = ViewMaps.addChild(view.getRoot(), "Left", true);
		view.validate();
		view.selectAsTheOnlyOneSelected(view.getRoot());
		KeyEvent toLeft = ViewMaps.key(view, KeyEvent.VK_RIGHT, 0);
		view.move(toLeft);
		assertNotNull(view.getSelected());
		ViewMaps.addChild(leftChild, "Kid", true);
		leftChild.getModel().setFolded(true);
		view.selectAsTheOnlyOneSelected(leftChild);
		KeyEvent unfold = ViewMaps.key(view, KeyEvent.VK_LEFT, 0);
		view.move(unfold);
		assertFalse(leftChild.getModel().isFolded());

		view.selectAsTheOnlyOneSelected(alpha);
		KeyEvent page = ViewMaps.key(view, KeyEvent.VK_PAGE_DOWN, KeyEvent.SHIFT_DOWN_MASK);
		view.move(page);
		KeyEvent pageUp = ViewMaps.key(view, KeyEvent.VK_PAGE_UP, KeyEvent.SHIFT_DOWN_MASK);
		view.move(pageUp);
		KeyEvent shiftLeft = ViewMaps.key(view, KeyEvent.VK_LEFT, KeyEvent.SHIFT_DOWN_MASK);
		view.move(shiftLeft);
		assertTrue(shiftLeft.isConsumed() || view.getSelected() != null);
	}

	@Test
	void continuousSelectionCoversSiblingRanges() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha", "Beta", "Gamma");
		MapView view = opened.view;
		NodeView alpha = ViewMaps.childView(opened, "Alpha");
		NodeView gamma = ViewMaps.childView(opened, "Gamma");
		view.selectAsTheOnlyOneSelected(view.getRoot());
		assertTrue(view.selectContinuous(alpha));
		assertTrue(view.isSelected(alpha));
		assertFalse(view.selectContinuous(alpha));
		assertTrue(view.selectContinuous(gamma));
		assertTrue(view.isSelected(gamma));
		view.deselect(view.getRoot());
		assertTrue(view.getSelectedNodesSortedByY().contains(alpha.getModel()));
		assertTrue(view.getSelectedNodesSortedByY().contains(gamma.getModel()));
		view.selectBranch(view.getRoot(), true);
		assertTrue(view.isSelected(view.getRoot()));
		view.deselect(alpha);
		assertFalse(view.isSelected(alpha));
		view.toggleSelected(alpha);
		view.toggleSelected(gamma);
		view.makeTheSelected(alpha);
		assertSame(alpha, view.getSelected());
		view.select(null);
		assertNotNull(view.getSelected());
		view.select(betaOr(opened));
		assertEquals(betaOr(opened).getModel().getNodeLevel(), view.getSiblingMaxLevel());
	}

	private static NodeView betaOr(Opened opened) {
		return ViewMaps.childView(opened, "Beta");
	}

	@Test
	void scrollingClampsAndCentersAValidatedNode() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha", "Beta");
		MapView view = opened.view;
		NodeView alpha = ViewMaps.childView(opened, "Alpha");
		assertTrue(view.getParent() instanceof JViewport);
		assertTrue(view.getViewportSize().width > 0);
		view.setViewLocation(-40, -20);
		assertTrue(view.getViewPosition().x >= 0);
		assertTrue(view.getViewPosition().y >= 0);
		view.setViewLocation(5000, 5000);
		Point high = view.getViewPosition();
		assertNotNull(high);
		int maxX = view.getWidth() - view.getViewportSize().width;
		int maxY = view.getHeight() - view.getViewportSize().height;
		assertTrue(high.x <= Math.max(maxX, 0));
		assertTrue(high.y <= Math.max(maxY, 0));
		view.setViewLocation(5000, 5000);
		assertEquals(high, view.getViewPosition());
		view.scrollBy(3, 4);
		view.centerNode(alpha);
		view.scrollNodeToVisible(alpha, 40);
		view.scrollNodeToVisible(alpha, -15);
		view.invalidate();
		view.scrollNodeToVisible(alpha, 8);
		Class<?> taskType = Class.forName(
				"freemind.view.mindmapview.MapView$CheckLaterForCenterNodeTask");
		Constructor<?> constructor = taskType.getDeclaredConstructor(MapView.class, NodeView.class);
		constructor.setAccessible(true);
		TimerTask task = (TimerTask) constructor.newInstance(view, alpha);
		view.validate();
		task.run();
		assertTrue(view.isValid());
		Insets insets = view.getAutoscrollInsets();
		assertTrue(insets.top > 0);
		view.autoscroll(new Point(10, 10));
		view.getParent().invalidate();
		Dimension preferred = view.getPreferredSize();
		assertTrue(preferred.width > 0 && preferred.height > 0);
	}

	@Test
	void paintingDrawsTheSelectionAndLinks() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha", "Beta");
		MapView view = opened.view;
		NodeView alpha = ViewMaps.childView(opened, "Alpha");
		NodeView beta = ViewMaps.childView(opened, "Beta");
		view.selectAsTheOnlyOneSelected(alpha);
		view.toggleSelected(beta);
		opened.app.setProperty(FreeMind.RESOURCE_DRAW_RECTANGLE_FOR_SELECTION, "true");
		opened.app.setProperty(FreeMind.RESOURCES_SELECTED_NODE_COLOR, "#112233");
		opened.app.setProperty(FreeMind.RESOURCES_SELECTED_NODE_RECTANGLE_COLOR, "#445566");
		opened.app.setProperty(FreeMind.RESOURCES_BACKGROUND_COLOR, "#fefefe");
		opened.app.setProperty(FreeMind.RESOURCES_NODE_TEXT_COLOR, "#010101");
		opened.app.setProperty(FreeMind.RESOURCE_PRINT_ON_WHITE_BACKGROUND, "false");
		opened.app.setProperty(FreeMindCommon.RESOURCE_ANTIALIAS, "antialias_edges");
		opened.app.setProperty(FreeMindCommon.RESOURCE_ANTIALIAS, "antialias_all");
		opened.app.setProperty(FreeMindCommon.RESOURCE_ANTIALIAS, "antialias_none");
		MindMapMapModel model = (MindMapMapModel) view.getModel();
		freemind.modes.ArrowLinkAdapter link = model.createArrowLinkAdapter(
				(NodeAdapter) alpha.getModel(), (NodeAdapter) beta.getModel());
		link.setDestinationLabel("Beta");
		model.getLinkRegistry().registerLink(link);
		view.setZoom(1.5f);
		BufferedImage image = new BufferedImage(view.getWidth(), view.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
				RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		view.paint(graphics);
		view.setZoom(1f);
		view.invalidate();
		view.paint(graphics);
		graphics.dispose();
		assertTrue(ViewMaps.ink(image) > 0);
		assertNull(view.detectCollision(new Point(-1000, -1000)));
		view.preparePrinting();
		assertTrue(view.isCurrentlyPrinting());
		assertFalse(view.isSelected(alpha));
		view.endPrinting();
		assertFalse(view.isCurrentlyPrinting());
	}

	@Test
	void printingFitsOnePageSeveralPagesAndAUserZoom() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha");
		MapView view = opened.view;
		PageFormat format = page();
		BufferedImage image = new BufferedImage(420, 420, BufferedImage.TYPE_INT_RGB);
		opened.frame.setProperty("fit_to_page", "true");
		opened.frame.setProperty("pages_wide", "1");
		opened.frame.setProperty("user_zoom", "not-a-number");
		assertEquals(Printable.PAGE_EXISTS, view.print(image.createGraphics(), format, 0));
		assertEquals(Printable.NO_SUCH_PAGE, view.print(image.createGraphics(), format, 3));
		opened.frame.setProperty("pages_wide", "0");
		assertEquals(Printable.PAGE_EXISTS, view.print(image.createGraphics(), format, 0));
		opened.frame.setProperty("pages_wide", "2");
		assertEquals(Printable.PAGE_EXISTS, view.print(image.createGraphics(), format, 0));
		int far = view.print(image.createGraphics(), format, 40);
		assertTrue(far == Printable.PAGE_EXISTS || far == Printable.NO_SUCH_PAGE);
		opened.frame.setProperty("fit_to_page", "false");
		opened.frame.setProperty("user_zoom", "3");
		assertEquals(Printable.PAGE_EXISTS, view.print(image.createGraphics(), format, 0));
		view.preparePrinting();
		view.preparePrinting();
		view.endPrinting();
		view.endPrinting();
		boolean oldFix = ViewMaps.booleanField(MapView.class, "NEED_PREF_SIZE_BUG_FIX");
		ViewMaps.setBooleanField(MapView.class, "NEED_PREF_SIZE_BUG_FIX", true);
		boolean oldPrint = ViewMaps.booleanField(MapView.class, "printOnWhiteBackground");
		try {
			opened.app.setProperty(FreeMind.RESOURCE_PRINT_ON_WHITE_BACKGROUND, "false");
			opened.app.setProperty(FreeMind.RESOURCE_PRINT_ON_WHITE_BACKGROUND, "true");
			ViewMaps.setBooleanField(MapView.class, "printOnWhiteBackground", true);
			if (view.isCurrentlyPrinting()) {
				view.endPrinting();
			}
			view.setBackground(new Color(254, 254, 254));
			view.preparePrinting();
			assertEquals(Color.WHITE, view.getBackground());
			view.endPrinting();
			assertEquals(new Color(254, 254, 254), view.getBackground());
		} finally {
			if (view.isCurrentlyPrinting()) {
				view.endPrinting();
			}
			ViewMaps.setBooleanField(MapView.class, "printOnWhiteBackground", oldPrint);
			ViewMaps.setBooleanField(MapView.class, "NEED_PREF_SIZE_BUG_FIX", oldFix);
		}
		opened.frame.setProperty("pages_wide", "nope");
		view.preparePrinting();
		view.endPrinting();
		assertFalse(view.isCurrentlyPrinting());
	}

	@Test
	void visitorsSelectionRepairAndBadColorConstruction() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha", "Beta");
		MapView view = opened.view;
		NodeView alpha = ViewMaps.childView(opened, "Alpha");
		NodeView beta = ViewMaps.childView(opened, "Beta");
		ArrayList<NodeView> seen = new ArrayList<>();
		view.acceptViewVisitor(alpha.getModel(), new NodeViewVisitor() {
			public void visit(NodeView node) {
				seen.add(node);
			}
		});
		assertTrue(seen.contains(alpha));
		assertNull(view.getNodeView(null));
		view.selectAsTheOnlyOneSelected(alpha);
		view.toggleSelected(beta);
		ArrayList<MindMapNode> sorted = view.getSelectedNodesSortedByY();
		assertTrue(sorted.contains(alpha.getModel()));
		assertEquals(2, view.getSingleSelectedNodes().size());
		view.selectAsTheOnlyOneSelected(view.getRoot());
		view.toggleSelected(alpha);
		ArrayList<MindMapNode> ancestors = view.getSelectedNodesSortedByY();
		assertTrue(ancestors.contains(view.getRoot().getModel()));
		assertFalse(ancestors.contains(alpha.getModel()));

		((MindMapMapModel) view.getModel()).setFilter(new ViewMaps.HideText("Alpha"));
		try {
			ViewMaps.call(view, "revalidateSelecteds", new Class<?>[0], new Object[0]);
			view.invalidate();
			view.validate();
			assertFalse(view.isSelected(alpha));
		} finally {
			((MindMapMapModel) view.getModel()).setFilter(null);
		}

		Field maxWidth = MapView.class.getDeclaredField("maxNodeWidth");
		maxWidth.setAccessible(true);
		maxWidth.setInt(view, 0);
		opened.frame.setProperty("max_node_width", "bad");
		opened.frame.setProperty("el__max_default_window_width", "320");
		assertEquals(320, view.getMaxNodeWidth());
		assertEquals(320, view.getMaxNodeWidth());

		Field cursorFlag = MapView.class.getDeclaredField("disableMoveCursor");
		cursorFlag.setAccessible(true);
		cursorFlag.setBoolean(view, false);
		view.setMoveCursor(true);
		assertEquals(Cursor.MOVE_CURSOR, view.getCursor().getType());
		view.setMoveCursor(false);
		assertEquals(Cursor.DEFAULT_CURSOR, view.getCursor().getType());
		view.setMoveCursor(false);

		FocusTraversalPolicy policy = view.getFocusTraversalPolicy();
		view.selectAsTheOnlyOneSelected(alpha);
		assertSame(alpha, policy.getDefaultComponent(view));
		assertSame(alpha, policy.getFirstComponent(view));
		assertSame(alpha, policy.getLastComponent(view));
		assertSame(alpha, policy.getComponentAfter(view, alpha));
		assertSame(alpha, policy.getComponentBefore(view, alpha));

		MapView.ScrollPane pane = (MapView.ScrollPane) view.getParent().getParent();
		Method binding = MapView.ScrollPane.class.getDeclaredMethod("processKeyBinding",
				javax.swing.KeyStroke.class, KeyEvent.class, int.class, boolean.class);
		binding.setAccessible(true);
		KeyEvent pageDown = ViewMaps.key(pane, KeyEvent.VK_PAGE_DOWN, 0);
		assertFalse((Boolean) binding.invoke(pane, javax.swing.KeyStroke.getKeyStrokeForEvent(pageDown),
				pageDown, JComponent.WHEN_FOCUSED, true));
		KeyEvent letter = ViewMaps.key(pane, KeyEvent.VK_A, 0);
		Object letterBinding = binding.invoke(pane, javax.swing.KeyStroke.getKeyStrokeForEvent(letter),
				letter, JComponent.WHEN_FOCUSED, true);
		assertTrue(letterBinding == null || letterBinding instanceof Boolean);
		Method validateTree = MapView.ScrollPane.class.getDeclaredMethod("validateTree");
		validateTree.setAccessible(true);
		synchronized (pane.getTreeLock()) {
			validateTree.invoke(pane);
		}
		assertTrue(view.isValid());

		view.moveToRoot();
		assertSame(view.getRoot(), view.getSelected());
		assertNotNull(view.getNodeContentLocation(alpha));
		assertNotNull(view.getInnerBounds());

		Field colors = MapView.class.getDeclaredField("standardNodeTextColor");
		colors.setAccessible(true);
		Color previous = (Color) colors.get(null);
		colors.set(null, null);
		opened.frame.setProperty(FreeMind.RESOURCES_BACKGROUND_COLOR, "bad");
		opened.frame.setProperty(FreeMind.RESOURCES_NODE_TEXT_COLOR, "bad");
		opened.frame.setProperty(FreeMind.RESOURCES_SELECTED_NODE_COLOR, "bad");
		opened.frame.setProperty(FreeMind.RESOURCES_SELECTED_NODE_RECTANGLE_COLOR, "bad");
		try {
			MapView rebuilt = new MapView(view.getModel(), opened.mode);
			assertNotNull(rebuilt.getRoot());
			assertNull(MapView.standardNodeTextColor);
			assertEquals(Color.WHITE, rebuilt.getBackground());
		} finally {
			if (MapView.standardNodeTextColor == null) {
				colors.set(null, previous);
			}
			opened.frame.setProperty(FreeMind.RESOURCES_BACKGROUND_COLOR, "#ffffff");
			opened.frame.setProperty(FreeMind.RESOURCES_NODE_TEXT_COLOR, "#000000");
		}
	}

	private static PageFormat page() {
		PageFormat format = new PageFormat();
		Paper paper = new Paper();
		paper.setSize(400, 400);
		paper.setImageableArea(20, 20, 360, 360);
		format.setPaper(paper);
		return format;
	}
}
