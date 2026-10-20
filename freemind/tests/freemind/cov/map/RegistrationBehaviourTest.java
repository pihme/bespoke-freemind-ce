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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JMenuItem;

import org.junit.jupiter.api.Test;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileCache;

import freemind.common.TextTranslator;
import freemind.controller.actions.generated.instance.PlaceNodeXmlAction;
import freemind.main.Resources;
import freemind.main.Tools.IntHolder;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.actions.NodeHookAction;
import plugins.map.MapNodePositionHolder;
import plugins.map.Registration;

class RegistrationBehaviourTest {

	@Test
	// backwards compatibility
	void purgesOldTagFilesAndSkipsARunThatIsAlreadyActive() throws Exception {
		try (MapCovSupport support = MapCovSupport.open("Child")) {
			File root = Files.createTempDirectory("map-cache").toFile();
			root.deleteOnExit();
			File child = new File(root, "tiles");
			assertTrue(child.mkdir());
			File oldTags = new File(child, "one.tags");
			File oldPng = new File(child, "one.png");
			File youngTags = new File(child, "two.tags");
			File other = new File(child, "note.txt");
			assertTrue(oldTags.createNewFile());
			assertTrue(oldPng.createNewFile());
			assertTrue(youngTags.createNewFile());
			assertTrue(other.createNewFile());
			assertTrue(oldTags.setLastModified(System.currentTimeMillis() - 10_000));
			assertTrue(oldPng.setLastModified(System.currentTimeMillis() - 10_000));
			File plain = new File(root, "not-a-directory.txt");
			assertTrue(plain.createNewFile());

			Registration.CachePurger purger = support.registration.new CachePurger(root, 1_000);
			IntHolder gate = (IntHolder) MapCovSupport.get(support.registration, "mCachePurgerSemaphore");
			gate.setValue(1);
			purger.run();
			assertTrue(oldTags.exists());
			assertEquals(1, gate.getValue());

			gate.setValue(0);
			purger.run();
			assertFalse(oldTags.exists());
			assertFalse(oldPng.exists());
			assertTrue(youngTags.exists());
			assertTrue(other.exists());
			assertTrue(plain.exists());
			assertEquals(0, gate.getValue());
		}
	}

	@Test
	// backwards compatibility
	void buildsATileLoaderAndEnablesHooksThatHaveAPosition() throws Exception {
		try (MapCovSupport support = MapCovSupport.open("Child")) {
			Resources resources = Resources.getInstance();
			resources.getProperties().setProperty("tile_cache_class", "file");
			support.opened.frame.setProperty("tile_cache_class", "file");
			OsmTileLoader fileLoader = support.registration.createTileLoader(support.registration);
			assertNotNull(fileLoader);
			assertTrue(fileLoader.headers.get("User-agent").toString().startsWith("FreeMind"));

			resources.getProperties().setProperty("tile_cache_class", "memory");
			support.opened.frame.setProperty("tile_cache_class", "memory");
			assertNotNull(support.registration.createTileLoader(support.registration));

			resources.getProperties().setProperty("file_tile_cache_directory", "%/osm-test");
			support.opened.frame.setProperty("file_tile_cache_directory", "%/osm-test");
			File directory = (File) MapCovSupport.call(support.registration, "getCacheDirectory");
			assertTrue(directory.getPath().endsWith("osm-test"));
			assertTrue(((Long) MapCovSupport.call(support.registration, "getCacheMaxAge")) >= 0);

			MindMapNode node = support.opened.child("Child");
			support.select(node);
			JMenuItem item = new JMenuItem();
			assertTrue(support.registration.isEnabled(item, new NodeHookAction(
					"plugins/map/MapDialog_SearchInMapForNodeTextAction.properties", support.opened.mode)));
			assertFalse(support.registration.isEnabled(item,
					new NodeHookAction("plugins/map/MapDialog_ShowMapToNode.properties", support.opened.mode)));
			support.attach(node, 49.0, 8.0);
			assertTrue(support.registration.isEnabled(item,
					new NodeHookAction("plugins/map/MapDialog_AddLinkToMapAction.properties", support.opened.mode)));
			assertTrue(support.registration.isEnabled(item,
					new NodeHookAction("plugins/map/MapDialog_RemoveMapToNode.properties", support.opened.mode)));
			assertTrue(support.registration.isEnabled(item, new NodeHookAction(
					"plugins/map/MapDialog_AddMapImageToNodeAction.properties", support.opened.mode)));
			assertFalse(support.registration.isEnabled(item,
					new NodeHookAction("plugins/map/Unknown.properties", support.opened.mode)));

			Object contributor = MapCovSupport.get(support.registration, "mOptionContributor");
			@SuppressWarnings("unchecked")
			List<Object> controls = (List<Object>) MapCovSupport.call(contributor, "getControls",
					new Class<?>[] { TextTranslator.class }, support.opened.mode);
			assertEquals(4, controls.size());

			TileCache cache = support.registration.getTileCache();
			support.registration.tileLoadingFinished(null, true);
			assertSame(cache, support.registration.getTileCache());
			assertSame(PlaceNodeXmlAction.class, support.registration.getDoActionClass());
		}
	}

	@Test
	// backwards compatibility
	void keepsRegisteredNodesWhenAListenerFailsAndRejectsAMissingHook() throws Exception {
		try (MapCovSupport support = MapCovSupport.open("Child", "Sibling")) {
			MindMapNode node = support.opened.child("Child");
			AtomicInteger calls = new AtomicInteger();
			MapNodePositionHolder.MapNodePositionListener listener = new MapNodePositionHolder.MapNodePositionListener() {
				@Override
				public void registerMapNode(MapNodePositionHolder holder) {
					calls.incrementAndGet();
					throw new IllegalStateException("register");
				}

				@Override
				public void deregisterMapNode(MapNodePositionHolder holder) {
					calls.incrementAndGet();
					throw new IllegalStateException("deregister");
				}
			};
			Registration.NodeVisibilityListener visibility = (holder, visible) -> {
				throw new IllegalStateException("visibility");
			};
			support.registration.registerMapNodePositionListener(listener);
			support.registration.registerNodeVisibilityListener(visibility);
			MapNodePositionHolder holder = support.attach(node, 48.0, 9.0, 4);
			assertTrue(support.registration.getMapNodePositionHolders().contains(holder));
			assertTrue(calls.get() >= 1);
			support.registration.fireNodeVisibilityChanged(true, holder);
			support.registration.deregisterMapNode(holder);
			assertFalse(support.registration.getMapNodePositionHolders().contains(holder));
			support.registration.deregisterMapNodePositionListener(listener);
			support.registration.deregisterNodeVisibilityListener(visibility);

			holder.changePosition(new Coordinate(48.2, 9.2), new Coordinate(48.1, 9.1), 7,
					org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource.Mapnik.class.getName());
			assertEquals(7, holder.getZoom());

			PlaceNodeXmlAction missing = new PlaceNodeXmlAction();
			missing.setNode(support.opened.mode.getNodeID(support.opened.child("Sibling")));
			missing.setCursorLatitude(1.0);
			missing.setCursorLongitude(2.0);
			missing.setMapCenterLatitude(1.0);
			missing.setMapCenterLongitude(2.0);
			missing.setZoom(3);
			missing.setTileSource("none");
			assertThrows(IllegalArgumentException.class, () -> support.registration.act(missing));

			support.registration.deRegister();
			holder.changePosition(new Coordinate(1, 2), new Coordinate(1, 2), 3, "none");
			assertEquals(7, holder.getZoom());
			assertEquals(48.2, holder.getPosition().getLat(), 0.001);
		}
	}
}
