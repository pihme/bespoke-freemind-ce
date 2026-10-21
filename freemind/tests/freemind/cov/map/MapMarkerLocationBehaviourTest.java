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

package tests.freemind.cov.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.MindMapNode;
import plugins.map.MapMarkerLocation;
import plugins.map.MapNodePositionHolder;

@DisplayName("MapMarkerLocationBehaviourTest")
class MapMarkerLocationBehaviourTest {

	@Test
	// backwards compatibility
	void labelsTheMarkerWithTheNodeAndWritesLatLonBack() throws Exception {
		try (MapCovSupport support = MapCovSupport.open("Child")) {
			MindMapNode node = support.opened.child("Child");
			node.setColor(Color.BLUE);
			MapNodePositionHolder holder = support.attach(node, 48.1, 11.5);
			MapMarkerLocation marker = new MapMarkerLocation(holder, support.dialog);
			assertSame(holder, marker.getNodePositionHolder());
			assertEquals("Child", marker.getText());
			assertEquals(Color.BLUE, marker.getForeground());
			assertEquals(48.1, marker.getLat());
			assertEquals(11.5, marker.getLon());
			assertTrue(marker.toString().contains("Child"));
			assertTrue(marker.toString().contains("48.1"));

			marker.setLat(47.0);
			marker.setLon(10.0);
			assertEquals(47.0, holder.getPosition().getLat());
			assertEquals(10.0, holder.getPosition().getLon());

			node.setText("Renamed");
			node.setColor(Color.RED);
			marker.update();
			assertEquals("Renamed", marker.getText());
			assertEquals(Color.RED, marker.getForeground());
		}
	}
}
