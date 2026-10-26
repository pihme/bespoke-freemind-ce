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

package tests.freemind.cov.listeners;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.Vector;

import javax.swing.JScrollPane;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.ArrowLinkAdapter;
import freemind.modes.MindMapArrowLink;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.listeners.MindMapMouseMotionManager;
import freemind.view.mindmapview.ArrowLinkView;
import freemind.view.mindmapview.MapView;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Dragging the map background scrolls it, and dragging an arrow bends that arrow. */
@DisplayName("MindMapMouseMotionManagerBehaviourTest")
class MindMapMouseMotionManagerBehaviourTest {

	@Test
	void aLeftDragScrollsAndAReleasedDragDoesNotContinue() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha");
		placeInScrollPane(opened.view);
		MindMapMouseMotionManager manager = new MindMapMouseMotionManager(opened.mode);
		Point start = opened.view.getViewPosition();

		manager.mousePressed(mouse(opened.view, MouseEvent.MOUSE_PRESSED, 120, 90, MouseEvent.BUTTON1));
		manager.mouseDragged(mouse(opened.view, MouseEvent.MOUSE_DRAGGED, 80, 60, MouseEvent.BUTTON1));
		Point scrolled = opened.view.getViewPosition();
		assertEquals(start.x + 40, scrolled.x);
		assertEquals(start.y + 30, scrolled.y);

		manager.mouseReleased(mouse(opened.view, MouseEvent.MOUSE_RELEASED, 80, 60, MouseEvent.BUTTON1));
		manager.mouseDragged(mouse(opened.view, MouseEvent.MOUSE_DRAGGED, 10, 10, MouseEvent.BUTTON1));
		assertEquals(scrolled, opened.view.getViewPosition());
	}

	@Test
	void aBlockedMapAndARightButtonLeaveTheScrollWhereItIs() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha");
		placeInScrollPane(opened.view);
		MindMapMouseMotionManager manager = new MindMapMouseMotionManager(opened.mode);
		opened.mode.setBlocked(true);
		manager.mousePressed(mouse(opened.view, MouseEvent.MOUSE_PRESSED, 120, 90, MouseEvent.BUTTON1));
		manager.mouseDragged(mouse(opened.view, MouseEvent.MOUSE_DRAGGED, 40, 20, MouseEvent.BUTTON1));
		assertEquals(new Point(0, 0), opened.view.getViewPosition());

		opened.mode.setBlocked(false);
		manager.mousePressed(mouse(opened.view, MouseEvent.MOUSE_PRESSED, 120, 90, MouseEvent.BUTTON3));
		manager.mouseDragged(mouse(opened.view, MouseEvent.MOUSE_DRAGGED, 40, 20, MouseEvent.BUTTON3));
		assertEquals(new Point(0, 0), opened.view.getViewPosition());
	}

	@Test
	void draggingAnArrowBendsItAndReleaseHidesTheHandles() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("From", "To");
		MindMapNode from = opened.child("From");
		opened.mode.addLink(from, opened.child("To"));
		MindMapArrowLink link = (MindMapArrowLink) opened.mode.getModel().getLinkRegistry()
				.getAllLinksFromMe(from).get(0);
		link.setStartInclination(new Point(40, 40));
		link.setEndInclination(new Point(50, 10));
		Point hit = hitOn(opened.view);
		MindMapMouseMotionManager manager = new MindMapMouseMotionManager(opened.mode);

		manager.mousePressed(mouse(opened.view, MouseEvent.MOUSE_PRESSED, hit.x, hit.y, MouseEvent.BUTTON1));
		assertTrue(((ArrowLinkAdapter) link).getShowControlPointsFlag());
		manager.mouseDragged(mouse(opened.view, MouseEvent.MOUSE_DRAGGED, hit.x + 36, hit.y + 18,
				MouseEvent.BUTTON1));
		Point bentStart = link.getStartInclination();
		Point bentEnd = link.getEndInclination();
		assertTrue(!bentStart.equals(new Point(40, 40)) || !bentEnd.equals(new Point(50, 10)));

		manager.mouseReleased(mouse(opened.view, MouseEvent.MOUSE_RELEASED, hit.x + 36, hit.y + 18,
				MouseEvent.BUTTON1));
		assertFalse(((ArrowLinkAdapter) link).getShowControlPointsFlag());
		assertEquals(bentStart, link.getStartInclination());
		assertEquals(bentEnd, link.getEndInclination());
	}

	private static void placeInScrollPane(MapView view) {
		JScrollPane pane = new JScrollPane();
		pane.setBounds(0, 0, 200, 140);
		pane.setViewportView(view);
		pane.doLayout();
		view.setSize(1600, 1200);
	}

	private static MouseEvent mouse(MapView view, int id, int x, int y, int button) {
		int modifiers = button == MouseEvent.BUTTON1 ? MouseEvent.BUTTON1_DOWN_MASK : MouseEvent.BUTTON3_DOWN_MASK;
		return new MouseEvent(view, id, System.currentTimeMillis(), modifiers, x, y, 1, false, button);
	}

	private static Point hitOn(MapView view) throws Exception {
		view.getRoot().updateAll();
		view.getRoot().doLayout();
		BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
		view.paint(graphics);
		graphics.dispose();
		Field views = MapView.class.getDeclaredField("mArrowLinkViews");
		views.setAccessible(true);
		@SuppressWarnings("unchecked")
		Vector<ArrowLinkView> arrows = (Vector<ArrowLinkView>) views.get(view);
		assertNotNull(arrows);
		assertNotEquals(0, arrows.size());
		ArrowLinkView arrow = arrows.get(0);
		Field curveField = ArrowLinkView.class.getDeclaredField("arrowLinkCurve");
		curveField.setAccessible(true);
		CubicCurve2D curve = (CubicCurve2D) curveField.get(arrow);
		assertNotNull(curve);
		FlatteningPathIterator iterator = new FlatteningPathIterator(curve.getPathIterator(null), 2.0);
		double[] coordinates = new double[6];
		while (!iterator.isDone()) {
			int type = iterator.currentSegment(coordinates);
			if (type == PathIterator.SEG_LINETO || type == PathIterator.SEG_MOVETO) {
				Point candidate = new Point((int) coordinates[0], (int) coordinates[1]);
				if (view.detectCollision(candidate) != null) {
					return candidate;
				}
			}
			iterator.next();
		}
		throw new AssertionError("no hit on " + curve.getBounds());
	}
}
