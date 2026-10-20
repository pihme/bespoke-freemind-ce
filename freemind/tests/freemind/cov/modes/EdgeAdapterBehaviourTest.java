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

package tests.freemind.cov.modes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.Color;

import org.junit.jupiter.api.Test;

import freemind.main.FreeMind;
import freemind.main.Tools;
import freemind.modes.EdgeAdapter;
import freemind.modes.MindMapEdge;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Edge style names become the integer constants, and the standard color follows the property. */
class EdgeAdapterBehaviourTest {

	@Test
	void styleNamesMapToIntegersAndAnUnknownNameThrows() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapEdge edge = opened.root.getEdge();
		String previous = opened.frame.getProperty(FreeMind.RESOURCES_EDGE_STYLE);
		try {
			assertStyle(opened, edge, EdgeAdapter.EDGESTYLE_LINEAR, EdgeAdapter.INT_EDGESTYLE_LINEAR);
			assertStyle(opened, edge, EdgeAdapter.EDGESTYLE_BEZIER, EdgeAdapter.INT_EDGESTYLE_BEZIER);
			assertStyle(opened, edge, EdgeAdapter.EDGESTYLE_SHARP_LINEAR, EdgeAdapter.INT_EDGESTYLE_SHARP_LINEAR);
			assertStyle(opened, edge, EdgeAdapter.EDGESTYLE_SHARP_BEZIER, EdgeAdapter.INT_EDGESTYLE_SHARP_BEZIER);
			opened.mode.setProperty(FreeMind.RESOURCES_EDGE_STYLE, "wavy");
			assertThrows(IllegalArgumentException.class, () -> edge.getStyleAsInt());
		} finally {
			if (previous != null) {
				opened.mode.setProperty(FreeMind.RESOURCES_EDGE_STYLE, previous);
			}
		}
	}

	@Test
	void propertyChangeUpdatesTheStandardEdgeColor() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		String key = FreeMind.RESOURCES_EDGE_COLOR;
		String previous = opened.frame.getProperty(key);
		opened.mode.setProperty(key, "#0000ff");
		try {
			assertEquals(Tools.xmlToColor("#0000ff"), opened.root.getEdge().getColor());
		} finally {
			if (previous != null) {
				opened.mode.setProperty(key, previous);
			}
		}
	}

	private static void assertStyle(Opened opened, MindMapEdge edge, String style, int expected) {
		opened.mode.setProperty(FreeMind.RESOURCES_EDGE_STYLE, style);
		assertEquals(style, edge.getStyle());
		assertEquals(expected, edge.getStyleAsInt());
		assertEquals(Color.class, opened.root.getEdge().getColor().getClass());
	}
}
