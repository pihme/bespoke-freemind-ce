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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openstreetmap.gui.jmapviewer.Layer;
import org.openstreetmap.gui.jmapviewer.Style;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

import plugins.map.MapDialog;
import plugins.map.MapMarkerBase;

@DisplayName("MapMarkerBaseBehaviourTest")
class MapMarkerBaseBehaviourTest {

	@Test
	// backwards compatibility
	void paintsABulletAndReportsAHitInsideTheRadius() throws Exception {
		try (MapCovSupport support = MapCovSupport.open("Child")) {
			Bullet marker = new Bullet(support.dialog, 49.8, 8.8);
			marker.setSize(40, 16);
			marker.setSelected(true);
			assertTrue(marker.isSelected());
			assertEquals(49.8, marker.getCoordinate().getLat());
			assertEquals(8.8, marker.getCoordinate().getLon());
			assertTrue(marker.checkHit(0, 0));
			assertTrue(marker.checkHit(20, -4));
			assertFalse(marker.checkHit(80, 80));
			Point shifted = MapMarkerBase.adjustToTextfieldLocation(new Point(10, 10));
			assertEquals(new Point(15, 5), shifted);

			BufferedImage image = new BufferedImage(80, 40, BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics = image.createGraphics();
			marker.paint(graphics, new Point(20, 20), 5);
			marker.paintCenter(graphics, new Point(20, 20));
			graphics.dispose();
			assertEquals(Color.BLACK.getRGB(), image.getRGB(20, 20));
			assertEquals(Color.GRAY.getRGB(), image.getRGB(25, 15));
			assertEquals(MapMarkerBase.CIRCLE_RADIUS, marker.getRadius());
			assertEquals(MapMarker.STYLE.VARIABLE, marker.getMarkerStyle());
			assertEquals(Color.BLACK, marker.getColor());
			assertEquals(Color.WHITE, marker.getBackColor());
			assertTrue(marker.getStroke() instanceof BasicStroke);
		}
	}

	@Test
	// backwards compatibility
	void sharesOneLayerAndStyleAcrossMarkers() throws Exception {
		try (MapCovSupport support = MapCovSupport.open("Child")) {
			Bullet marker = new Bullet(support.dialog, 1, 2);
			Layer previous = marker.getLayer();
			Style previousStyle = marker.getStyle();
			try {
				Layer replacement = new Layer("coverage");
				marker.setLayer(replacement);
				assertSame(replacement, marker.getLayer());
				assertSame(previousStyle, marker.getStyleAssigned());
				assertSame(marker.getStyle(), marker.getStyleAssigned());
			} finally {
				marker.setLayer(previous);
			}
		}
	}

	static final class Bullet extends MapMarkerBase {
		private final double lat;
		private final double lon;

		Bullet(MapDialog dialog, double lat, double lon) {
			super(dialog);
			this.lat = lat;
			this.lon = lon;
		}

		@Override
		public double getLat() {
			return lat;
		}

		@Override
		public double getLon() {
			return lon;
		}

		@Override
		public void setLat(double lat) {
		}

		@Override
		public void setLon(double lon) {
		}

		@Override
		public void paintCenter(java.awt.Graphics graphics, Point position) {
			super.paintCenter(graphics, position);
		}
	}
}
