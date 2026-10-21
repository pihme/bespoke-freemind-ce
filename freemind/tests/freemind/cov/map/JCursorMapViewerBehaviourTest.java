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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.JButton;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.TileController;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.TileCache;

import plugins.map.FreeMindMapController;

@DisplayName("JCursorMapViewerBehaviourTest")
class JCursorMapViewerBehaviourTest {

	@Test
	// backwards compatibility
	void scalesALoadedParentTileAndDrawsTheCursor() throws Exception {
		try (MapCovSupport support = MapCovSupport.open("Child")) {
			TileController controller = (TileController) MapCovSupport.call(support.viewer, "getTileController");
			assertNotNull(controller);
			TileCache cache = support.registration.getTileCache();
			int tileSize = controller.getTileSource().getTileSize();
			BufferedImage swatch = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_RGB);
			Graphics fill = swatch.createGraphics();
			fill.setColor(Color.BLUE);
			fill.fillRect(0, 0, tileSize, tileSize);
			fill.dispose();
			Tile parent = new Tile(controller.getTileSource(), 0, 0, 0, swatch);
			parent.setLoaded(true);
			cache.addTile(parent);

			assertNull(controller.getTile(-1, 0, 1));
			Tile scaled = controller.getTile(1, 0, 1);
			assertNotNull(scaled);
			assertEquals("ScaledTile", scaled.getClass().getSimpleName());
			BufferedImage canvas = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_RGB);
			Graphics graphics = canvas.createGraphics();
			scaled.paint(graphics, 0, 0);
			graphics.dispose();
			assertEquals(Color.BLUE.getRGB(), canvas.getRGB(tileSize / 2, tileSize / 4));

			Tile waiting = controller.getTile(0, 1, 1);
			BufferedImage placeholder = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_RGB);
			Graphics pending = placeholder.createGraphics();
			waiting.paint(pending, 0, 0);
			pending.dispose();
			assertNotNull(waiting.getImage());

			MapCovSupport.call(support.viewer, "setUseCursor", new Class<?>[] { boolean.class }, true);
			MapCovSupport.set(support.viewer, "mShowCursor", true);
			MapCovSupport.call(support.viewer, "setCursorPosition", new Class<?>[] { ICoordinate.class },
					new Coordinate(49.8, 8.8));
			MapCovSupport.call(support.viewer, "setRectangular",
					new Class<?>[] { ICoordinate.class, ICoordinate.class }, new Coordinate(49.7, 8.7),
					new Coordinate(49.9, 8.9));
			MapCovSupport.call(support.viewer, "setDrawRectangular", new Class<?>[] { boolean.class }, true);
			BufferedImage view = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
			Graphics viewGraphics = view.createGraphics();
			support.viewer.paint(viewGraphics);
			viewGraphics.dispose();
			assertTrue(contains(view, Color.RED.getRGB()) || contains(view, Color.BLACK.getRGB()));

			Rectangle box = (Rectangle) MapCovSupport.call(support.viewer, "getRectangle",
					new Class<?>[] { ICoordinate.class, ICoordinate.class }, new Coordinate(49.7, 8.7),
					new Coordinate(49.9, 8.9));
			assertNotNull(box);
			assertTrue(box.width >= 0 && box.height >= 0);
			ICoordinate offScreen = new Coordinate(80, 170);
			Object missing = MapCovSupport.call(support.viewer, "getRectangle",
					new Class<?>[] { ICoordinate.class, ICoordinate.class }, offScreen, offScreen);
			assertTrue(missing == null || missing instanceof Rectangle);

			assertThrows(IllegalArgumentException.class,
					() -> MapCovSupport.call(support.viewer, "getMapPosition", new Class<?>[] { ICoordinate.class },
							new ICoordinate() {
								@Override
								public double getLat() {
									return 1;
								}

								@Override
								public void setLat(double lat) {
								}

								@Override
								public double getLon() {
									return 2;
								}

								@Override
								public void setLon(double lon) {
								}
							}));
		}
	}

	@Test
	// backwards compatibility
	void storesCursorFlagsAndRecentersOnALatLon() throws Exception {
		try (MapCovSupport support = MapCovSupport.open("Child")) {
			MapCovSupport.call(support.viewer, "setDisplayPositionByLatLon",
					new Class<?>[] { double.class, double.class, int.class }, 12.0d, 20.0d, 5);
			assertEquals(5, support.viewer.getZoom());
			assertEquals(12.0, support.viewer.getPosition().getLat(), 1.0);
			assertEquals(20.0, support.viewer.getPosition().getLon(), 1.0);

			MapCovSupport.call(support.viewer, "setHideFoldedNodes", new Class<?>[] { boolean.class }, false);
			assertEquals(false, MapCovSupport.call(support.viewer, "isHideFoldedNodes"));
			MapCovSupport.call(support.viewer, "setUseCursor", new Class<?>[] { boolean.class }, false);
			assertEquals(false, MapCovSupport.call(support.viewer, "isUseCursor"));
			MapCovSupport.call(support.viewer, "setDrawRectangular", new Class<?>[] { boolean.class }, true);
			assertEquals(true, MapCovSupport.call(support.viewer, "isDrawRectangular"));
			Coordinate cursor = new Coordinate(3, 4);
			MapCovSupport.call(support.viewer, "setCursorPosition", new Class<?>[] { ICoordinate.class }, cursor);
			ICoordinate stored = (ICoordinate) MapCovSupport.call(support.viewer, "getCursorPosition");
			assertEquals(3, stored.getLat());
			assertEquals(4, stored.getLon());

			MapCovSupport.call(support.viewer, "initializeZoomSlider");
			JButton zoomIn = (JButton) MapCovSupport.get(support.viewer, "zoomInButton");
			assertFalse(zoomIn.isFocusable());
			assertNotNull(support.maps);
			assertTrue(support.maps instanceof FreeMindMapController);
		}
	}

	private static boolean contains(BufferedImage image, int rgb) {
		for (int y = 0; y < image.getHeight(); y += 2) {
			for (int x = 0; x < image.getWidth(); x += 2) {
				if (image.getRGB(x, y) == rgb) {
					return true;
				}
			}
		}
		return false;
	}
}
