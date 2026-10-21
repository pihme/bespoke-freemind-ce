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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openstreetmap.gui.jmapviewer.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.TileController;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import plugins.map.TileImage;

@DisplayName("TileImageBehaviourTest")
class TileImageBehaviourTest {

	@Test
	// backwards compatibility
	void roundTripsAPngThroughBase64() throws Exception {
		BufferedImage source = new BufferedImage(4, 3, BufferedImage.TYPE_INT_RGB);
		source.setRGB(1, 1, Color.RED.getRGB());
		File file = File.createTempFile("tile", ".png");
		file.deleteOnExit();
		ImageIO.write(source, "png", file);

		TileImage loaded = new TileImage();
		assertFalse(loaded.isLoaded());
		assertFalse(loaded.hasErrors());
		assertTrue(loaded.isDrawingDone());
		assertNull(loaded.save());
		loaded.load(file);
		assertTrue(loaded.isImageCreated());
		assertEquals(4, loaded.getImage().getWidth());
		String coded = loaded.save();
		assertNotNull(coded);

		TileImage again = new TileImage();
		again.load(coded);
		assertEquals(Color.RED.getRGB(), ((BufferedImage) again.getImage()).getRGB(1, 1));
		assertFalse(again.isLoaded());
	}

	@Test
	// backwards compatibility
	void rejectsAMissingFileAndGarbageBase64() {
		TileImage image = new TileImage();
		image.load(new File("/tmp/freemind-missing-tile-image.png"));
		assertNull(image.getImage());
		assertFalse(image.isImageCreated());
		image.load("AAAA");
		assertNull(image.getImage());
		assertTrue(image.isImageCreated());
	}

	@Test
	// backwards compatibility
	void buildsOnePictureWhenEveryTileIsLoaded() throws Exception {
		TileSource source = new OsmTileSource.Mapnik();
		MemoryTileCache cache = new MemoryTileCache();
		TileController controller = new TileController(source, cache, (tile, success) -> {
		});
		controller.setTileLoader(MapCovSupport.silentLoader());
		BufferedImage swatch = new BufferedImage(source.getTileSize(), source.getTileSize(),
				BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = swatch.createGraphics();
		graphics.setColor(Color.BLUE);
		graphics.fillRect(0, 0, swatch.getWidth(), swatch.getHeight());
		graphics.dispose();
		Tile ready = new Tile(source, 0, 0, 1, swatch);
		ready.setLoaded(true);
		cache.addTile(ready);
		Tile waiting = new Tile(source, 1, 0, 1);
		cache.addTile(waiting);

		TileImage pending = new TileImage();
		pending.setTiles(1, 1, 0, 1, controller, Logger.getLogger("tile"), 3, 4);
		assertFalse(pending.isLoaded());
		assertFalse(pending.hasErrors());

		TileImage image = new TileImage();
		image.setTiles(1, 0, 0, 1, controller, Logger.getLogger("tile"), 12, 18);
		assertTrue(image.isLoaded());
		assertTrue(image.isImageCreated());
		assertFalse(image.hasErrors());
		assertEquals(source.getTileSize(), image.getImage().getWidth());
		assertTrue(image.imageUpdate(swatch, 0, 0, 0, 1, 1));
		assertTrue(image.isDrawingDone());

		ready.setError("offline");
		ready.setLoaded(true);
		if (ready.getImage() == null) {
			ready.setImage(swatch);
		}
		TileImage broken = new TileImage();
		broken.setTiles(1, 0, 0, 1, controller, Logger.getLogger("tile"), 1, 1);
		assertTrue(broken.hasErrors());
		assertTrue(broken.isLoaded());
		assertNotNull(broken.getImage());
	}
}
