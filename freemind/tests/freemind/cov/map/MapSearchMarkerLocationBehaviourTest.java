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
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

import freemind.controller.actions.generated.instance.Place;
import plugins.map.MapSearchMarkerLocation;

class MapSearchMarkerLocationBehaviourTest {

	@Test
	// backwards compatibility
	void drawsACrossWhenSelectedAndADotOtherwise() throws Exception {
		try (MapCovSupport support = MapCovSupport.open("Child")) {
			Place place = new Place();
			place.setDisplayName("Market");
			place.setLat(49.0);
			place.setLon(8.0);
			MapSearchMarkerLocation marker = new MapSearchMarkerLocation(support.dialog, place);
			assertSame(place, marker.getPlace());
			assertEquals("Market", marker.getText());
			assertEquals(Color.RED, marker.getForeground());
			assertEquals(49.0, marker.getLat());
			assertEquals(8.0, marker.getLon());
			assertTrue(marker.toString().contains("Market"));

			marker.setLat(50.5);
			marker.setLon(9.25);
			assertEquals(50.5, place.getLat());
			assertEquals(9.25, place.getLon());
			place.setDisplayName("Square");
			marker.update();
			assertEquals("Square", marker.getText());

			BufferedImage plain = draw(marker, false);
			BufferedImage selected = draw(marker, true);
			assertEquals(Color.RED.getRGB(), plain.getRGB(30, 30));
			assertTrue(selected.getRGB(30 - 8, 30 - 8) != plain.getRGB(30 - 8, 30 - 8)
					|| selected.getRGB(22, 22) != 0);
		}
	}

	private static BufferedImage draw(MapSearchMarkerLocation marker, boolean selected) throws Exception {
		marker.setSelected(selected);
		BufferedImage image = new BufferedImage(60, 60, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		MapCovSupport.call(marker, "paintCenter", new Class<?>[] { java.awt.Graphics.class, Point.class },
				graphics, new Point(30, 30));
		graphics.dispose();
		return image;
	}
}
