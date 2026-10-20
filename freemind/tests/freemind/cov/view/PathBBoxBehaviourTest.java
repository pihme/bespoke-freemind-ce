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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

public class PathBBoxBehaviourTest {

	@Test
	void boundsIncludeLinesQuadsCubicsAndASecondMove() throws Exception {
		// backwards compatibility
		Class<?> type = Class.forName("freemind.view.mindmapview.PathBBox");
		Constructor<?> constructor = type.getDeclaredConstructor();
		constructor.setAccessible(true);
		assertTrue(constructor.newInstance() != null);
		Method bounds = type.getDeclaredMethod("getBBox", java.awt.Shape.class);
		bounds.setAccessible(true);

		GeneralPath path = new GeneralPath();
		path.moveTo(0, 0);
		path.lineTo(10, 4);
		path.quadTo(20, -10, 30, 4);
		path.curveTo(40, 30, 5, -20, 8, 8);
		path.closePath();
		path.moveTo(50, 50);
		path.lineTo(55, 40);
		Rectangle2D box = (Rectangle2D) bounds.invoke(null, path);
		assertEquals(0, box.getX(), 0.01);
		assertTrue(box.getWidth() >= 50);
		assertTrue(box.getHeight() > 10);
	}
}
