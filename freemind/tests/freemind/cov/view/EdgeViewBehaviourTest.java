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

import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import freemind.view.mindmapview.EdgeView;
import freemind.view.mindmapview.LinearEdgeView;

public class EdgeViewBehaviourTest {

	@Test
	void resetClearsThePaintedEndpoints() throws Exception {
		// backwards compatibility
		LinearEdgeView edge = new LinearEdgeView();
		ViewMaps.call(edge, "reset", new Class<?>[0], new Object[0]);
		Field source = EdgeView.class.getDeclaredField("source");
		Field target = EdgeView.class.getDeclaredField("target");
		source.setAccessible(true);
		target.setAccessible(true);
		assertNull(source.get(edge));
		assertNull(target.get(edge));
	}
}
