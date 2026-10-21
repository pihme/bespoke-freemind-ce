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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringWriter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import freemind.main.Resources;
import freemind.main.XMLElement;
import freemind.modes.MindMapNode;
import freemind.modes.common.plugins.MapNodePositionHolderBase;
import plugins.map.FreeMindMapController;
import plugins.map.MapNodePositionHolder;

@DisplayName("MapNodePositionHolderBehaviourTest")
class MapNodePositionHolderBehaviourTest {

	@Test
	// backwards compatibility
	void roundTripsPositionXmlAndBuildsTheTileHtml() throws Exception {
		try (MapCovSupport support = MapCovSupport.open("Child")) {
			MindMapNode node = support.opened.child("Child");
			MapNodePositionHolder holder = support.attach(node, 48.2, 11.4, 6);
			assertSame(node, holder.getNode());
			assertSame(support.registration, MapCovSupport.call(holder, "getRegistration"));
			assertEquals(support.opened.mode.getNodeID(node), holder.getNodeId());
			assertTrue(holder.toString().contains("Child"));
			assertFalse(holder.hasFoldedParents());
			support.opened.root.setFolded(true);
			assertTrue(holder.hasFoldedParents());

			XMLElement stored = new XMLElement();
			stored.setName("hook");
			holder.save(stored);
			String withSource = stored.toString();
			assertTrue(withSource.contains("XML_STORAGE_TILE_SOURCE"));
			assertTrue(withSource.contains("48.2"));

			holder.setTileSource(null);
			MapCovSupport.set(holder, "mTooltipLocation", "pin.png");
			XMLElement withTip = new XMLElement();
			withTip.setName("hook");
			holder.save(withTip);
			assertTrue(withTip.toString().contains("XML_STORAGE_MAP_TOOLTIP_LOCATION"));
			assertFalse(withTip.toString().contains("XML_STORAGE_TILE_SOURCE"));

			XMLElement broken = parameters("nope", "11", "bad", "8", "zoom", null);
			MapNodePositionHolder loaded = new MapNodePositionHolder();
			loaded.loadFrom(broken);
			assertEquals(0.0, loaded.getPosition().getLat());
			assertEquals(11.0, loaded.getPosition().getLon());
			assertEquals(1, loaded.getZoom());

			XMLElement empty = new XMLElement();
			loaded.loadFrom(empty);
			assertEquals(0.0, loaded.getMapCenter().getLat());
			assertEquals(1, loaded.getZoom());

			holder.setTileSource(OsmTileSource.Mapnik.class.getName());
			holder.setPosition(new Coordinate(48.2, 11.4));
			String html = holder.getImageHtml();
			assertTrue(html.contains("BACKGROUND"));
			assertTrue(html.contains("<strong>X</strong>"));
			holder.setTileSource(null);
			assertTrue(holder.getImageHtml().contains("file:./"));

			String bothQuadrants = quadrantHtml(holder);
			assertTrue(bothQuadrants.contains("<strong>X</strong>"));

			StringWriter writer = new StringWriter();
			holder.setTileSource(OsmTileSource.Mapnik.class.getName());
			holder.saveHtml(writer);
			assertTrue(writer.toString().contains("openstreetmap.org"));
			assertTrue(writer.toString().contains("<img src=\""));
			assertTrue(FreeMindMapController.getLink(holder).contains("zoom=8"));
		}
	}

	@Test
	// backwards compatibility
	void registersVisibilityAndMovesWhenThePositionChanges() throws Exception {
		try (MapCovSupport support = MapCovSupport.open("Child")) {
			MindMapNode node = support.opened.child("Child");
			MapNodePositionHolder holder = support.attach(node, 47.0, 8.0, 5);
			assertTrue(support.registration.getMapNodePositionHolders().contains(holder));
			boolean[] seen = new boolean[1];
			support.registration.registerNodeVisibilityListener((changed, visible) -> {
				assertSame(holder, changed);
				seen[0] = visible;
			});
			holder.onViewCreatedHook(support.opened.view.getRoot());
			assertTrue(seen[0]);
			holder.onViewRemovedHook(support.opened.view.getRoot());
			assertFalse(seen[0]);

			holder.changePosition(new Coordinate(46.5, 7.5), new Coordinate(46.4, 7.4), 9,
					OsmTileSource.Mapnik.class.getName());
			assertEquals(9, holder.getZoom());
			assertEquals(46.5, holder.getPosition().getLat());
			assertEquals(7.4, holder.getMapCenter().getLon());

			Resources.getInstance().getProperties().setProperty("node_map_show_tooltip", "true");
			support.opened.frame.setProperty("node_map_show_tooltip", "true");
			MapCovSupport.set(holder, "mTooltipLocation", "pin.png");
			holder.showTooltip();
			assertNotNull(node.getToolTip().get(MapNodePositionHolderBase.NODE_MAP_HOOK_NAME));

			holder.shutdownMapHook();
			assertFalse(support.registration.getMapNodePositionHolders().contains(holder));
		}
	}

	private static String quadrantHtml(MapNodePositionHolder holder) {
		int zoom = 8;
		int tileSize = new OsmTileSource.Mapnik().getTileSize();
		holder.setZoom(zoom);
		holder.setTileSource(OsmTileSource.Mapnik.class.getName());
		boolean lowX = false;
		boolean highX = false;
		boolean lowY = false;
		boolean highY = false;
		StringBuilder html = new StringBuilder();
		for (double value = -40; value <= 60 && !(lowX && highX && lowY && highY); value += 0.25) {
			int exactX = (int) OsmMercator.MERCATOR_256.lonToX(value, zoom);
			int exactY = (int) OsmMercator.MERCATOR_256.latToY(value, zoom);
			int dx = Math.floorMod(exactX, tileSize);
			int dy = Math.floorMod(exactY, tileSize);
			if (!lowX && dx < tileSize / 2) {
				holder.setPosition(new Coordinate(10, value));
				html.append(holder.getImageHtml());
				lowX = true;
			}
			if (!highX && dx >= tileSize / 2) {
				holder.setPosition(new Coordinate(10, value));
				html.append(holder.getImageHtml());
				highX = true;
			}
			if (!lowY && dy < tileSize / 2) {
				holder.setPosition(new Coordinate(value, 10));
				html.append(holder.getImageHtml());
				lowY = true;
			}
			if (!highY && dy >= tileSize / 2) {
				holder.setPosition(new Coordinate(value, 10));
				html.append(holder.getImageHtml());
				highY = true;
			}
		}
		assertTrue(lowX && highX && lowY && highY);
		return html.toString();
	}

	private static XMLElement parameters(String lat, String lon, String mapLat, String mapLon, String zoom,
			String tile) {
		XMLElement root = new XMLElement();
		XMLElement parameters = new XMLElement();
		parameters.setName("Parameters");
		parameters.setAttribute("XML_STORAGE_POS_LAT", lat);
		parameters.setAttribute("XML_STORAGE_POS_LON", lon);
		parameters.setAttribute("XML_STORAGE_MAP_LAT", mapLat);
		parameters.setAttribute("XML_STORAGE_MAP_LON", mapLon);
		parameters.setAttribute("XML_STORAGE_ZOOM", zoom);
		if (tile != null) {
			parameters.setAttribute("XML_STORAGE_TILE_SOURCE", tile);
		}
		root.addChild(parameters);
		return root;
	}
}
