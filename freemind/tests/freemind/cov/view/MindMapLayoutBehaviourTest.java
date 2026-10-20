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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Dimension;

import org.junit.jupiter.api.Test;

import freemind.view.mindmapview.MapView;
import freemind.view.mindmapview.MindMapLayout;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

public class MindMapLayoutBehaviourTest {

	@Test
	void layoutSizesTheMapWithAndWithoutAViewport() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha");
		MindMapLayout layout = new MindMapLayout();
		MapView view = opened.view;
		assertNull(view.getViewportSize());
		assertEquals(new Dimension(200, 200), layout.minimumLayoutSize(view));
		int count = view.getComponentCount();
		layout.addLayoutComponent("map", view.getRoot());
		layout.removeLayoutComponent(view.getRoot());
		assertEquals(count, view.getComponentCount());
		Dimension preferred = layout.preferredLayoutSize(view);
		assertTrue(preferred.width > 0);
		assertTrue(preferred.height > 0);
		view.getRoot().invalidate();
		layout.layoutContainer(view);
		assertTrue(view.getRoot().getX() > 0);
		assertTrue(view.getWidth() > view.getRoot().getWidth());
		ViewMaps.placeInViewport(view);
		assertTrue(view.getViewportSize().width > 0);
		Dimension inView = layout.preferredLayoutSize(view);
		assertTrue(inView.width >= preferred.width);
		view.getRoot().invalidate();
		layout.layoutContainer(view);
		assertTrue(view.getRoot().getY() > 0);
	}
}
