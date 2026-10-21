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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.main.Tools;
import freemind.modes.MindMapNode;
import freemind.modes.NodeAdapter;
import freemind.view.mindmapview.MainView;
import freemind.view.mindmapview.MapView;
import freemind.view.mindmapview.NodeView;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("MainViewBehaviourTest")
public class MainViewBehaviourTest {

	@Test
	void geometryCursorAndZoomedPaintFollowTheNode() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha");
		NodeView alpha = ViewMaps.childView(opened, "Alpha");
		opened.view.validate();
		MainView main = alpha.getMainView();
		main.setSize(Math.max(main.getWidth(), 80), Math.max(main.getHeight(), 24));
		assertEquals(new Dimension(0, 0), main.getMinimumSize());
		assertEquals(Integer.MAX_VALUE, main.getMaximumSize().width);
		int textX = main.getTextX();
		assertTrue(textX >= 0 || alpha.isLeft());
		assertTrue(((Integer) ViewMaps.call(main, "getZoomedFoldingSymbolHalfWidth", new Class<?>[0],
				new Object[0])).intValue() >= 0);
		assertEquals(main.getWidth(), ((Integer) ViewMaps.call(main, "getMainViewWidthWithFoldingMark",
				new Class<?>[0], new Object[0])).intValue());
		assertEquals(main.getHeight(), ((Integer) ViewMaps.call(main, "getMainViewHeightWithFoldingMark",
				new Class<?>[0], new Object[0])).intValue());
		Point toMap = new Point(2, 3);
		Point expected = new Point(2, 3);
		Tools.convertPointToAncestor(main, expected, opened.view);
		ViewMaps.call(main, "convertPointToMap", new Class<?>[] { Point.class }, new Object[] { toMap });
		assertEquals(expected, toMap);
		Point fromMap = new Point(expected);
		Point back = new Point(expected);
		Tools.convertPointFromAncestor(opened.view, back, main);
		ViewMaps.call(main, "convertPointFromMap", new Class<?>[] { Point.class }, new Object[] { fromMap });
		assertEquals(back, fromMap);

		ViewMaps.addChild(alpha, "Kid", false);
		((NodeAdapter) alpha.getModel()).setLink("http://example.test");
		assertTrue(main.isInFollowLinkRegion(1));
		assertTrue(main.updateCursor(1));
		assertFalse(main.updateCursor(Math.max(main.getWidth() - 1, 0)));
		assertEquals(main.isInVerticalRegion(1, 0.5), !alpha.isLeft() || main.getWidth() == 0);
		main.setDraggedOver(new Point(1, 1));
		assertNotEquals(NodeView.DRAGGED_OVER_NO, main.getDraggedOver());
		BufferedImage painted = ViewMaps.paint(main);
		assertTrue(ViewMaps.ink(painted) >= 0);

		opened.view.setZoom(2f);
		Dimension zoomed = main.getPreferredSize();
		assertTrue(zoomed.width >= opened.view.getZoomed(10));
		BufferedImage zoomPaint = new BufferedImage(Math.max(main.getWidth(), 30),
				Math.max(main.getHeight(), 20), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = zoomPaint.createGraphics();
		main.paint(graphics);
		assertTrue(main.getWidth() > 0);
		assertTrue(main.getHeight() > 0);
		graphics.dispose();
		opened.view.setZoom(1f);

		alpha.getModel().setText("");
		alpha.treeNodesChanged(new javax.swing.event.TreeModelEvent(alpha.getModel(),
				new Object[] { alpha.getModel() }));
		Dimension empty = main.getPreferredSize();
		assertTrue(empty.width > 0);
		assertEquals("", main.getText());

		boolean oldFix = ViewMaps.booleanField(MapView.class, "NEED_PREF_SIZE_BUG_FIX");
		ViewMaps.setBooleanField(MapView.class, "NEED_PREF_SIZE_BUG_FIX", true);
		try {
			opened.view.preparePrinting();
			Dimension printing = main.getPreferredSize();
			assertTrue(printing.width >= empty.width);
			opened.view.endPrinting();
		} finally {
			ViewMaps.setBooleanField(MapView.class, "NEED_PREF_SIZE_BUG_FIX", oldFix);
		}

		alpha.getModel().setBackgroundColor(Color.ORANGE);
		opened.view.deselect(alpha);
		BufferedImage background = new BufferedImage(main.getWidth(), main.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D selectedGraphics = background.createGraphics();
		main.paintSelected(selectedGraphics);
		selectedGraphics.dispose();
		assertTrue(ViewMaps.ink(background) > 0 || main.getWidth() == 0);
	}
}
