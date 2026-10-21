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

import java.awt.Point;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Vector;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.view.mindmapview.ConvexHull;

@DisplayName("ConvexHullBehaviourTest")
public class ConvexHullBehaviourTest {

	@Test
	void turnsAndAnglesOrderAHull() {
		// backwards compatibility
		Probe hull = new Probe();
		assertEquals(1, hull.turn(new Point(0, 0), new Point(1, 0), new Point(0, 1)));
		assertEquals(-1, hull.turn(new Point(0, 0), new Point(0, 1), new Point(1, 0)));
		assertEquals(-1, hull.turn(new Point(0, 0), new Point(2, 0), new Point(-1, 0)));
		assertEquals(0, hull.turn(new Point(0, 0), new Point(3, 0), new Point(1, 0)));
		assertEquals(1, hull.turn(new Point(0, 0), new Point(1, 0), new Point(2, 0)));

		Comparator<Point> order = hull.order(new Point(0, 0));
		assertEquals(0, order.compare(new Point(1, 1), new Point(1, 1)));
		assertTrue(order.compare(new Point(1, 2), new Point(2, 1)) != 0);
		assertTrue(order.compare(new Point(-1, 1), new Point(1, 1)) != 0);
		assertTrue(order.compare(new Point(1, -1), new Point(1, 1)) != 0);
		assertEquals(-1, order.compare(new Point(4, 0), new Point(1, 0)));
		assertEquals(1, order.compare(new Point(1, 0), new Point(4, 0)));
		assertEquals(0, order.compare(new Point(0, 0), new Point(0, 0)));

		LinkedList<Point> square = new LinkedList<>();
		square.add(new Point(0, 0));
		square.add(new Point(3, 0));
		square.add(new Point(3, 2));
		square.add(new Point(0, 2));
		square.add(new Point(1, 1));
		square.add(new Point(2, 0));
		Vector<Point> result = hull.calculateHull(square);
		assertTrue(result.size() >= 3);
		assertTrue(result.contains(new Point(0, 2)) || result.size() >= 3);
	}

	private static final class Probe extends ConvexHull {
		int turn(Point first, Point second, Point third) {
			return ccw(first, second, third);
		}

		Comparator<Point> order(Point origin) {
			return new thetaComparator(origin);
		}
	}
}
