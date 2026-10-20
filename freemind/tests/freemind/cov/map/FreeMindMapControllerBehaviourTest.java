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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.event.PopupMenuEvent;

import org.junit.jupiter.api.Test;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import com.sun.net.httpserver.HttpServer;

import freemind.controller.MenuItemEnabledListener;
import freemind.controller.MenuItemSelectedListener;
import freemind.controller.actions.generated.instance.Place;
import freemind.controller.actions.generated.instance.Searchresults;
import freemind.main.Resources;
import freemind.modes.MindMapNode;
import freemind.view.mindmapview.NodeView;
import plugins.map.FreeMindMapController;
import plugins.map.FreeMindMapController.MapQuestOpenMap;
import plugins.map.FreeMindMapController.PositionHolder;
import plugins.map.FreeMindMapController.TransportMap;
import plugins.map.MapMarkerBase;
import plugins.map.MapNodePositionHolder;

class FreeMindMapControllerBehaviourTest {

	@Test
	// backwards compatibility
	void positionHolderMatchesLatLonAndZoom() {
		PositionHolder holder = new PositionHolder(1.5, 2.5, 3);
		assertTrue(holder.equals(holder));
		assertFalse(holder.equals(null));
		assertFalse(holder.equals("other"));
		assertFalse(holder.equals(new PositionHolder(9, 2.5, 3)));
		assertFalse(holder.equals(new PositionHolder(1.5, 9, 3)));
		assertFalse(holder.equals(new PositionHolder(1.5, 2.5, 4)));
		PositionHolder same = new PositionHolder(1.5, 2.5, 3);
		assertTrue(holder.equals(same));
		assertEquals(holder.hashCode(), same.hashCode());
		assertTrue(holder.toString().contains("lat=1.5"));
		assertEquals(1.5, holder.getCoordinate().getLat());
		assertEquals(4, FreeMindMapController.getmTileSources().length);
		assertEquals(OsmTileSource.Mapnik.class.getName(),
				FreeMindMapController.getTileSourceName(new OsmTileSource.Mapnik()));
		assertNull(FreeMindMapController.getTileSourceByName("missing.tile.Source"));
		TransportMap transport = new TransportMap();
		String first = transport.getBaseUrl();
		String second = transport.getBaseUrl();
		assertTrue(first.contains("tile2.opencyclemap.org"));
		assertNotEquals(first, second);
		assertEquals(18, transport.getMaxZoom());
		MapQuestOpenMap quest = new MapQuestOpenMap();
		assertTrue(quest.getBaseUrl().contains("otile1"));
		assertTrue(quest.getBaseUrl().contains("otile2"));
		assertEquals(18, quest.getMaxZoom());
		String link = FreeMindMapController.getLink("missing.tile.Source", new Coordinate(1, 2),
				new Coordinate(3, 4), 5);
		assertTrue(link.contains("layers=M"));
		assertTrue(link.contains("mlat=1"));
	}

	@Test
	// backwards compatibility
	void searchesCoordinatesLocallyAndNamesANetworkFailure() throws Exception {
		try (MapCovSupport support = MapCovSupport.open("Child")) {
			TileSource mapnik = FreeMindMapController.changeTileSource(OsmTileSource.Mapnik.class.getName(),
					support.viewer);
			assertEquals(OsmTileSource.Mapnik.class, mapnik.getClass());
			TileSource http = FreeMindMapController.changeTileSource(
					"plugins.map.FreeMindMapController$HttpMapnik", support.viewer);
			assertEquals("HttpMapnik", http.getClass().getSimpleName());
			assertTrue(http.getTileSize() > new OsmTileSource.Mapnik().getTileSize());
			TileSource transport = FreeMindMapController.changeTileSource(TransportMap.class.getName(),
					support.viewer);
			assertEquals(TransportMap.class, transport.getClass());
			assertEquals(MapQuestOpenMap.class, FreeMindMapController
					.changeTileSource(MapQuestOpenMap.class.getName(), support.viewer).getClass());
			assertNull(FreeMindMapController.changeTileSource("missing.tile.Source", support.viewer));
			assertTrue(FreeMindMapController.getLink(support.maps.getTileSourceAsString(), support.cursor(),
					support.viewer.getPosition(), support.viewer.getZoom()).contains("layers="));

			Searchresults places = support.maps.getSearchResults("47.2 11.3;48.1 12.4");
			assertEquals(2, places.getListPlaceList().size());
			assertEquals(47.2, places.getListPlaceList().get(0).getLat());
			assertEquals("node", places.getListPlaceList().get(0).getOsmType());
			assertEquals(0, support.maps.getSearchResults("   ").getListPlaceList().size());
			assertThrows(NumberFormatException.class, () -> support.maps.getSearchResults("1e 2"));

			Resources.getInstance().getProperties().setProperty("do_reverse_lookup_on_lat_lon_search", "true");
			support.opened.frame.setProperty("do_reverse_lookup_on_lat_lon_search", "true");
			assertNull(support.maps.getReverseLookup(new Coordinate(47.2, 11.3), 8));
			Searchresults looked = support.maps.getSearchResults("47.2 11.3");
			assertEquals("47.2 11.3", looked.getListPlaceList().get(0).getDisplayName().trim());
			Resources.getInstance().getProperties().setProperty("do_reverse_lookup_on_lat_lon_search", "false");

			support.dialog.toggleLimitSearchToRegion();
			Searchresults failed = support.maps.getSearchResults("Innsbruck");
			assertEquals("ERROR", failed.getListPlaceList().get(0).getOsmType());
			assertEquals(support.cursor().getLat(), failed.getListPlaceList().get(0).getLat());
			support.dialog.toggleLimitSearchToRegion();

			assertTrue(support.maps.search(support.results, support.resultTable, "47.2 11.3", Color.WHITE));
			assertEquals(1, support.results.getRowCount());
			assertFalse(support.maps.search(support.results, support.resultTable, "1e 2", Color.WHITE));
			assertFalse(support.maps.search(support.results, support.resultTable, "no-such-town", Color.WHITE));
			assertEquals(Color.RED, support.resultTable.getBackground());
		}
	}

	@Test
	// backwards compatibility
	void readsALocalDocumentAndFollowsAReloadRedirect() throws Exception {
		try (MapCovSupport support = MapCovSupport.open("Child")) {
			java.io.File file = java.io.File.createTempFile("map-wget", ".txt");
			file.deleteOnExit();
			Files.writeString(file.toPath(), "from-file");
			assertEquals("from-file" + System.lineSeparator(),
					support.maps.wget(file.toURI().toURL().toString()));
			assertThrows(MalformedURLException.class, () -> support.maps.wget("http://["));

			HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
			server.setExecutor(command -> {
				Thread thread = new Thread(command, "map-wget");
				thread.setDaemon(true);
				thread.start();
			});
			int port = server.getAddress().getPort();
			server.createContext("/ok", exchange -> {
				byte[] body = "hello-map".getBytes(StandardCharsets.UTF_8);
				exchange.sendResponseHeaders(200, body.length);
				exchange.getResponseBody().write(body);
				exchange.close();
			});
			server.createContext("/missing", exchange -> {
				byte[] body = "gone".getBytes(StandardCharsets.UTF_8);
				exchange.sendResponseHeaders(404, body.length);
				exchange.getResponseBody().write(body);
				exchange.close();
			});
			server.createContext("/bounce", exchange -> {
				byte[] body = "please document.location.reload".getBytes(StandardCharsets.UTF_8);
				exchange.getResponseHeaders().set("Location", "http://127.0.0.1:" + port + "/ok");
				exchange.sendResponseHeaders(404, body.length);
				exchange.getResponseBody().write(body);
				exchange.close();
			});
			server.start();
			try {
				assertEquals("hello-map" + System.lineSeparator(),
						support.maps.wget("http://127.0.0.1:" + port + "/ok"));
				assertEquals("gone" + System.lineSeparator(),
						support.maps.wget("http://127.0.0.1:" + port + "/missing"));
				assertEquals("hello-map" + System.lineSeparator(),
						support.maps.wget("http://127.0.0.1:" + port + "/bounce"));
			} finally {
				server.stop(0);
			}
		}
	}

	@Test
	// backwards compatibility
	void dragsTheMapAndOpensSearchFromTheKeyboard() throws Exception {
		try (MapCovSupport support = MapCovSupport.open("Child", "Sibling")) {
			MindMapNode child = support.opened.child("Child");
			MindMapNode sibling = support.opened.child("Sibling");
			MapNodePositionHolder here = support.attach(child, 49.8, 8.8, 8);
			support.attach(sibling, 49.8, 8.0, 8);
			support.dialog.registerMapNode(here);
			support.dialog.registerMapNode(support.registration.getMapNodePositionHolders().stream()
					.filter(holder -> holder.getNode() == sibling).findFirst().orElseThrow());
			Point hit = support.viewer.getMapPosition(here.getPosition(), true);
			assertNotNull(hit);
			MapMarkerBase marker = support.maps.checkHit(MapCovSupport.mouse(support.viewer, MouseEvent.MOUSE_PRESSED,
					hit.x, hit.y, MouseEvent.BUTTON1, 0, 1, false));
			assertEquals(here, ((plugins.map.MapMarkerLocation) marker).getNodePositionHolder());
			assertNull(support.maps.checkHit(MapCovSupport.mouse(support.viewer, MouseEvent.MOUSE_MOVED, 1, 1,
					MouseEvent.NOBUTTON, 0, 0, false)));

			double beforeLon = support.viewer.getPosition().getLon();
			support.maps.mousePressed(MapCovSupport.mouse(support.viewer, MouseEvent.MOUSE_PRESSED, 80, 80,
					MouseEvent.BUTTON1, MouseEvent.BUTTON1_DOWN_MASK, 1, false));
			support.maps.mouseDragged(MapCovSupport.mouse(support.viewer, MouseEvent.MOUSE_DRAGGED, 40, 80,
					MouseEvent.BUTTON1, MouseEvent.BUTTON1_DOWN_MASK, 0, false));
			support.maps.mouseDragged(MapCovSupport.mouse(support.viewer, MouseEvent.MOUSE_DRAGGED, 10, 80,
					MouseEvent.BUTTON1, MouseEvent.BUTTON1_DOWN_MASK, 0, false));
			support.maps.mouseReleased(MapCovSupport.mouse(support.viewer, MouseEvent.MOUSE_RELEASED, 10, 80,
					MouseEvent.BUTTON1, 0, 1, false));
			assertNotEquals(beforeLon, support.viewer.getPosition().getLon());
			assertTrue(support.maps.getPositionHolderVector().size() >= 1);

			support.maps.mousePressed(MapCovSupport.mouse(support.viewer, MouseEvent.MOUSE_PRESSED, hit.x, hit.y,
					MouseEvent.BUTTON1, MouseEvent.SHIFT_DOWN_MASK, 1, false));
			support.maps.mouseDragged(MapCovSupport.mouse(support.viewer, MouseEvent.MOUSE_DRAGGED, hit.x + 30,
					hit.y + 20, MouseEvent.BUTTON1, MouseEvent.SHIFT_DOWN_MASK | MouseEvent.BUTTON1_DOWN_MASK, 0,
					false));
			assertEquals(true, MapCovSupport.call(support.viewer, "isDrawRectangular"));
			support.maps.mouseReleased(MapCovSupport.mouse(support.viewer, MouseEvent.MOUSE_RELEASED, hit.x + 30,
					hit.y + 20, MouseEvent.BUTTON1, MouseEvent.SHIFT_DOWN_MASK, 1, false));
			assertEquals(false, MapCovSupport.call(support.viewer, "isDrawRectangular"));

			int childrenBefore = support.opened.root.getChildCount();
			support.select(child);
			assertThrows(HeadlessException.class,
					() -> support.maps.mouseClicked(MapCovSupport.mouse(support.viewer, MouseEvent.MOUSE_CLICKED,
							hit.x, hit.y, MouseEvent.BUTTON1, 0, 2, false)));
			assertTrue(support.opened.root.getChildCount() > childrenBefore);
			support.maps.setClickEnabled(true);
			support.maps.setMovementEnabled(true);
			support.maps.setWheelZoomEnabled(true);

			support.maps.mouseClicked(MapCovSupport.mouse(support.viewer, MouseEvent.MOUSE_CLICKED, 50, 50,
					MouseEvent.BUTTON1, 0, 1, false));
			assertNotNull(support.cursor());
			support.maps.mouseEntered(MapCovSupport.mouse(support.viewer, MouseEvent.MOUSE_ENTERED, 1, 1,
					MouseEvent.NOBUTTON, 0, 0, false));
			support.maps.mouseExited(MapCovSupport.mouse(support.viewer, MouseEvent.MOUSE_EXITED, 1, 1,
					MouseEvent.NOBUTTON, 0, 0, false));
			support.maps.mouseMoved(MapCovSupport.mouse(support.viewer, MouseEvent.MOUSE_MOVED, 60, 40,
					MouseEvent.NOBUTTON, 0, 0, false));
			support.maps.actionPerformed(new ActionEvent(support.viewer, 0, "hover"));
			assertTrue(support.status.getText().length() > 0);

			support.maps.setClickEnabled(false);
			double frozen = support.viewer.getPosition().getLon();
			support.maps.mousePressed(MapCovSupport.mouse(support.viewer, MouseEvent.MOUSE_PRESSED, 10, 10,
					MouseEvent.BUTTON1, MouseEvent.BUTTON1_DOWN_MASK, 1, false));
			support.maps.mouseDragged(MapCovSupport.mouse(support.viewer, MouseEvent.MOUSE_DRAGGED, 80, 10,
					MouseEvent.BUTTON1, MouseEvent.BUTTON1_DOWN_MASK, 0, false));
			assertEquals(frozen, support.viewer.getPosition().getLon());
			support.maps.setClickEnabled(true);

			assertThrows(RuntimeException.class, () -> support.maps.setMovementMouseButton(4));
			support.maps.setMovementMouseButton(MouseEvent.BUTTON2);
			assertEquals(MouseEvent.BUTTON2, support.maps.getMovementMouseButton());
			support.maps.setMovementMouseButton(MouseEvent.BUTTON1);

			int zoom = support.viewer.getZoom();
			MouseWheelEvent wheel = new MouseWheelEvent(support.viewer, MouseEvent.MOUSE_WHEEL,
					System.currentTimeMillis(), 0, 20, 20, 0, false, MouseWheelEvent.WHEEL_UNIT_SCROLL, 1, 1);
			support.maps.mouseWheelMoved(wheel);
			assertEquals(zoom - 1, support.viewer.getZoom());
			support.maps.mouseWheelMoved(wheel);
			assertEquals(zoom - 1, support.viewer.getZoom());
			MapCovSupport.set(support.maps, "mWheelZoomLastTime", 0L);
			MouseWheelEvent fast = new MouseWheelEvent(support.viewer, MouseEvent.MOUSE_WHEEL,
					System.currentTimeMillis(), 0, 20, 20, 0, false, MouseWheelEvent.WHEEL_UNIT_SCROLL, 1, 5);
			support.maps.mouseWheelMoved(fast);
			assertEquals(zoom - 3, support.viewer.getZoom());
			support.maps.setWheelZoomEnabled(false);
			int stopped = support.viewer.getZoom();
			support.maps.mouseWheelMoved(fast);
			assertEquals(stopped, support.viewer.getZoom());

			Point center = new Point(10, 10);
			MapCovSupport.call(support.maps, "correctPointByMapCenter", new Class<?>[] { Point.class }, center);
			assertEquals(10 + support.viewer.getCenter().x, center.x);

			double lon = support.viewer.getPosition().getLon();
			support.maps.keyPressed(key(support, KeyEvent.KEY_PRESSED, KeyEvent.VK_RIGHT, KeyEvent.CHAR_UNDEFINED, 0));
			assertTrue(key(support, KeyEvent.KEY_PRESSED, KeyEvent.VK_RIGHT, KeyEvent.CHAR_UNDEFINED, 0).isConsumed()
					|| support.viewer.getPosition().getLon() != lon);
			support.maps.keyPressed(key(support, KeyEvent.KEY_PRESSED, KeyEvent.VK_LEFT, KeyEvent.CHAR_UNDEFINED,
					KeyEvent.SHIFT_DOWN_MASK));
			support.maps.keyPressed(key(support, KeyEvent.KEY_PRESSED, KeyEvent.VK_UP, KeyEvent.CHAR_UNDEFINED, 0));
			support.maps.keyPressed(key(support, KeyEvent.KEY_PRESSED, KeyEvent.VK_DOWN, KeyEvent.CHAR_UNDEFINED, 0));
			double parked = support.viewer.getPosition().getLat();
			support.maps.keyPressed(key(support, KeyEvent.KEY_PRESSED, KeyEvent.VK_LEFT, KeyEvent.CHAR_UNDEFINED,
					KeyEvent.CTRL_DOWN_MASK));
			assertEquals(parked, support.viewer.getPosition().getLat());
			support.maps.keyReleased(key(support, KeyEvent.KEY_RELEASED, KeyEvent.VK_LEFT, KeyEvent.CHAR_UNDEFINED, 0));
			support.maps.keyTyped(key(support, KeyEvent.KEY_TYPED, KeyEvent.VK_UNDEFINED, 'a', 0));
			assertTrue(support.dialog.isSearchBarVisible());
			double hidden = support.viewer.getPosition().getLon();
			support.maps.keyPressed(key(support, KeyEvent.KEY_PRESSED, KeyEvent.VK_RIGHT, KeyEvent.CHAR_UNDEFINED, 0));
			assertEquals(hidden, support.viewer.getPosition().getLon());
			support.maps.keyTyped(key(support, KeyEvent.KEY_TYPED, KeyEvent.VK_UNDEFINED, 'b', 0));
			assertTrue(support.dialog.isSearchBarVisible());

			Point popupAt = support.viewer.getMapPosition(here.getPosition(), true);
			assertNotNull(popupAt);
			assertThrows(IllegalComponentStateException.class, () -> support.maps.mouseReleased(MapCovSupport.mouse(
					support.viewer, MouseEvent.MOUSE_RELEASED, popupAt.x, popupAt.y, MouseEvent.BUTTON3, 0, 1, true)));
			assertEquals(here.getPosition().getLat(), support.cursor().getLat(), 0.001);
		}
	}

	@Test
	// backwards compatibility
	void menuMovesBetweenNodesAndKeepsAShortHistory() throws Exception {
		try (MapCovSupport support = MapCovSupport.open("Child", "Sibling")) {
			MindMapNode west = support.opened.child("Child");
			MindMapNode east = support.opened.child("Sibling");
			MapNodePositionHolder westHolder = support.attach(west, 49.8, 8.0, 9);
			MapNodePositionHolder eastHolder = support.attach(east, 49.8, 9.6, 9);
			support.dialog.registerMapNode(westHolder);
			support.dialog.registerMapNode(eastHolder);
			cursor(support, 49.8, 8.8);
			fire(support, "MapControllerPopupDialog.moveLeft");
			assertEquals(west, support.opened.mode.getSelected());
			assertEquals(8.0, support.cursor().getLon(), 0.001);
			fire(support, "MapControllerPopupDialog.moveRight");
			assertEquals(east, support.opened.mode.getSelected());
			support.attach(support.opened.root, 60.0, 8.7, 8);
			MapNodePositionHolder north = MapNodePositionHolder.getHook(support.opened.root);
			support.dialog.registerMapNode(north);
			cursor(support, 49.8, 8.8);
			fire(support, "MapControllerPopupDialog.moveUp");
			assertEquals(60.0, support.cursor().getLat(), 0.001);
			cursor(support, 49.8, 8.8);
			fire(support, "MapControllerPopupDialog.moveDown");
			assertTrue(support.cursor().getLat() <= 49.8);

			support.maps.showSelectedNodes();
			support.select(west);
			support.opened.mode.select(west, List.of(west));
			support.maps.showNode(westHolder);
			assertEquals(9, support.viewer.getZoom());
			support.opened.mode.select(west, List.of(west, east));
			support.maps.showSelectedNodes();
			assertTrue(support.viewer.getZoom() <= support.maps.getMaxZoom());

			Resources.getInstance().getProperties().setProperty("node_map_home", "");
			MenuItemEnabledListener home = (MenuItemEnabledListener) byClass(support, "MoveHomeAction");
			assertFalse(home.isEnabled(new JMenuItem(), (Action) home));
			fire(support, "MapControllerPopupDialog.MoveHome");
			Resources.getInstance().getProperties().setProperty("node_map_home", "not:valid");
			assertFalse(home.isEnabled(new JMenuItem(), (Action) home));
			Resources.getInstance().getProperties().setProperty("node_map_home", "12.0:34.0:4");
			assertTrue(home.isEnabled(new JMenuItem(), (Action) home));
			fire(support, "MapControllerPopupDialog.MoveHome");
			assertEquals(4, support.viewer.getZoom());
			assertEquals(12.0, support.cursor().getLat(), 0.01);
			fire(support, "MapControllerPopupDialog.SetHome");
			assertTrue(support.opened.frame.getProperty("node_map_home").contains("12.0"));

			int zoom = support.viewer.getZoom();
			fire(support, "MapControllerPopupDialog.ZoomAction1");
			assertEquals(zoom + 1, support.viewer.getZoom());
			fire(support, "MapControllerPopupDialog.ZoomAction-1");
			assertEquals(zoom, support.viewer.getZoom());
			fire(support, "MapControllerPopupDialog.MaxmimalZoomToCursorAction");
			assertTrue(support.viewer.getZoom() >= support.maps.getMaxZoom() - 2);

			boolean markers = support.viewer.getMapMarkersVisible();
			fire(support, "MapControllerPopupDialog.ShowMapMarker");
			assertEquals(!markers, support.viewer.getMapMarkersVisible());
			boolean grid = support.viewer.isTileGridVisible();
			fire(support, "MapControllerPopupDialog.TileGridVisible");
			assertEquals(!grid, support.viewer.isTileGridVisible());
			boolean controls = support.viewer.getZoomControlsVisible();
			fire(support, "MapControllerPopupDialog.ZoomControlsVisible");
			assertEquals(!controls, support.viewer.getZoomControlsVisible());
			MenuItemSelectedListener zoomItem = (MenuItemSelectedListener) byClass(support, "ZoomControlsVisible");
			assertEquals(support.viewer.getZoomControlsVisible(),
					zoomItem.isSelected(new JMenuItem(), (Action) zoomItem));
			fire(support, "MapControllerPopupDialog.HideFoldedNodes");
			assertEquals(false, MapCovSupport.call(support.viewer, "isHideFoldedNodes"));
			boolean searchWas = support.dialog.isSearchBarVisible();
			fire(support, "MapControllerPopupDialog.SearchControlVisible");
			assertEquals(!searchWas, support.dialog.isSearchBarVisible());
			fire(support, "MapControllerPopupDialog.GotoSearch");
			assertTrue(support.dialog.isSearchBarVisible());
			fire(support, "MapControllerPopupDialog.LimitSearchToRegionAction");
			assertTrue(support.dialog.isLimitSearchToRegion());

			Set<String> sources = new HashSet<>();
			for (Action action : support.actions()) {
				if ("ChangeTileSource".equals(action.getClass().getSimpleName()) && sources.add(
						String.valueOf(action.getValue(Action.NAME)))) {
					action.actionPerformed(new ActionEvent(support.viewer, 0, "tile"));
				}
			}
			assertTrue(sources.size() >= 4);
			fire(support, "MapControllerPopupDialog.SetDisplayToFitMapMarkers");
			assertTrue(support.viewer.getZoom() >= 0);

			support.maps.getPositionHolderVector().clear();
			support.maps.setPositionHolderIndex(-1);
			for (int i = 0; i < 52; i++) {
				cursor(support, 10 + i * 0.01, 20 + i * 0.01);
			}
			assertEquals(49, support.maps.getPositionHolderVector().size());
			int index = support.maps.getPositionHolderIndex();
			assertTrue(((MenuItemEnabledListener) byClass(support, "MoveBackwardAction")).isEnabled(new JMenuItem(),
					byClass(support, "MoveBackwardAction")));
			fire(support, "MapControllerPopupDialog.moveBackward");
			assertEquals(index - 1, support.maps.getPositionHolderIndex());
			assertTrue(((MenuItemEnabledListener) byClass(support, "MoveForwardAction")).isEnabled(new JMenuItem(),
					byClass(support, "MoveForwardAction")));
			fire(support, "MapControllerPopupDialog.moveForward");
			assertEquals(index, support.maps.getPositionHolderIndex());
			assertThrows(IllegalArgumentException.class, () -> support.maps.setPositionHolderIndex(-2));
			assertFalse(support.maps.checkPositionHolderIndex(1000));

			support.select(west);
			int before = west.getParentNode().getChildCount();
			support.maps.addNode(west, "Pinned place", 49.2, 8.4);
			assertEquals(before + 1, west.getParentNode().getChildCount());
			assertNotNull(childNamed(west.getParentNode(), "Pinned place"));
			Place place = new Place();
			place.setDisplayName("From place");
			place.setLat(49.3);
			place.setLon(8.5);
			support.maps.addNode(west, place);
			assertNotNull(childNamed(west.getParentNode(), "From place"));
			MapCovSupport.call(support.maps, "placeNodeAt",
					new Class<?>[] { MindMapNode.class, ICoordinate.class, ICoordinate.class, int.class }, west,
					new Coordinate(49.2, 8.4), new Coordinate(49.1, 8.3), 5);
			assertEquals(5, MapNodePositionHolder.getHook(west).getZoom());
			assertEquals(49.2, MapNodePositionHolder.getHook(west).getPosition().getLat(), 0.001);

			JMenuItem item = support.window.getJMenuBar().getMenu(0).getItem(0);
			support.maps.addAccelerator(item, "keystroke_plugins/map/MapDialog_zoomIn");
			assertNotNull(item.getAction().getValue(Action.ACCELERATOR_KEY));

			assertThrows(HeadlessException.class, () -> fire(support, "MapControllerPopupDialog.CopyLinkToClipboardAction"));
			assertThrows(HeadlessException.class,
					() -> fire(support, "MapControllerPopupDialog.CopyCoordinatesToClipboardAction"));
			assertThrows(HeadlessException.class, () -> fire(support, "MapControllerPopupDialog.ExportMapMenu"));
			MapCovSupport.set(support.maps, "mCurrentPopupPositionHolder", westHolder);
			assertThrows(HeadlessException.class, () -> fire(support, "MapControllerPopupDialog.CopyLinkToClipboardAction"));
			int kids = west.getParentNode().getChildCount();
			assertThrows(HeadlessException.class, () -> fire(support, "MapControllerPopupDialog.NewNodeAction"));
			assertTrue(west.getParentNode().getChildCount() > kids);
			String text = west.getText();
			assertThrows(HeadlessException.class, () -> fire(support, "MapControllerPopupDialog.EditNodeInContextMenu"));
			assertEquals(text, west.getText());

			fire(support, "MapControllerPopupDialog.place");
			assertNotNull(MapNodePositionHolder.getHook(support.opened.mode.getSelected()));
			fire(support, "MapControllerPopupDialog.show_nodes");
			fire(support, "MapControllerPopupDialog.ShowNodeMapInContextMenu");
			assertEquals(westHolder.getPosition().getLat(), support.cursor().getLat(), 0.001);
			fire(support, "MapControllerPopupDialog.SelectNodeInContextMenu");
			assertEquals(west, support.opened.mode.getSelected());
			fire(support, "MapControllerPopupDialog.AddMapPictureToNode");
			assertTrue(west.getChildCount() > 0);
			fire(support, "MapControllerPopupDialog.RemoveNodeLocationInContextMenu");
			assertNull(MapNodePositionHolder.getHook(west));
			int pictures = west.getChildCount();
			MapCovSupport.set(support.maps, "mCurrentPopupPositionHolder", null);
			support.maps.addMapPictureToNode();
			assertEquals(pictures, west.getChildCount());

			javax.swing.event.PopupMenuListener popup = (javax.swing.event.PopupMenuListener) MapCovSupport
					.get(support.maps, "popupListenerSingleton");
			popup.popupMenuWillBecomeVisible(new PopupMenuEvent(support.maps.getPopupMenu()));
			assertFalse(support.maps.isMovementEnabled());
			assertFalse(support.maps.isClickEnabled());
			popup.popupMenuWillBecomeInvisible(new PopupMenuEvent(support.maps.getPopupMenu()));
			assertTrue(support.maps.isMovementEnabled());
			popup.popupMenuCanceled(new PopupMenuEvent(support.maps.getContextPopupMenu()));
			assertTrue(support.maps.isWheelZoomEnabled());
			assertNotNull(support.maps.getSearchPopupMenu());
			assertSameGlass(support);
		}
	}

	@Test
	// backwards compatibility
	void editsANewNodeAndAddsTheChosenSearchRows() throws Exception {
		try (MapCovSupport support = MapCovSupport.open("Child")) {
			MindMapNode child = support.opened.child("Child");
			support.select(child);
			MindMapNode created = support.maps.insertNewNode(child);
			support.ensureView(created);
			int count = child.getParentNode().getChildCount();
			Object cancel = editControl(support, support.opened.view.getNodeView(created), created, child, false);
			try {
				MapCovSupport.call(cancel, "cancel");
			} catch (HeadlessException ex) {
				assertNotNull(ex);
			}
			assertEquals(count - 1, child.getParentNode().getChildCount());
			assertTrue(support.maps.isClickEnabled());

			MindMapNode placed = support.maps.insertNewNode(child);
			support.ensureView(placed);
			Object ok = editControl(support, support.opened.view.getNodeView(placed), placed, child, true);
			try {
				MapCovSupport.call(ok, "ok", new Class<?>[] { String.class }, "Placed");
			} catch (HeadlessException ex) {
				assertNotNull(ex);
			}
			assertEquals("Placed", placed.getText());
			MapCovSupport.call(ok, "split", new Class<?>[] { String.class, int.class }, "Placed", 1);
			assertEquals("Placed", placed.getText());

			JTextField field = new JTextField("note");
			Object editor = noteField(support, support.opened.view.getNodeView(child), ok);
			MapCovSupport.set(editor, "textfield", field);
			MapCovSupport.call(editor, "setTextfieldLoaction", new Class<?>[] { Point.class }, new Point(3, 4));
			assertEquals(new Point(3, 4), field.getLocation());
			MapCovSupport.call(editor, "addTextfield");
			assertEquals(support.viewer, field.getParent());

			Place row = new Place();
			row.setDisplayName("Row place");
			row.setLat(48.0);
			row.setLon(8.0);
			row.setOsmType("node");
			support.results.addPlace(row);
			support.select(child);
			int before = child.getParentNode().getChildCount();
			InvocationTargetException refused = assertThrows(InvocationTargetException.class,
					() -> searchTask(support, new int[] { 0 }));
			assertTrue(refused.getCause() instanceof HeadlessException);
			assertEquals(before, child.getParentNode().getChildCount());
			Object task = searchTaskWithoutProgressDialog(support, row);
			assertEquals(true, MapCovSupport.call(task, "processAction"));
			assertEquals(before + 1, child.getParentNode().getChildCount());
			assertNotNull(childNamed(child.getParentNode(), "Row place"));
		}
	}

	private static MindMapNode childNamed(MindMapNode parent, String text) {
		for (int i = 0; i < parent.getChildCount(); i++) {
			MindMapNode candidate = (MindMapNode) parent.getChildAt(i);
			if (text.equals(candidate.getText())) {
				return candidate;
			}
		}
		return null;
	}

	private static void cursor(MapCovSupport support, double lat, double lon) throws Exception {
		MapCovSupport.call(support.maps, "setCursorPosition", new Class<?>[] { ICoordinate.class },
				new Coordinate(lat, lon));
	}

	private static void assertSameGlass(MapCovSupport support) {
		assertNotNull(support.maps.getGlassPane());
	}

	private static KeyEvent key(MapCovSupport support, int id, int code, char character, int modifiers) {
		return new KeyEvent(support.viewer, id, System.currentTimeMillis(), modifiers, code, character);
	}

	private static void fire(MapCovSupport support, String name) {
		support.action(name).actionPerformed(new ActionEvent(support.viewer, ActionEvent.ACTION_PERFORMED, name));
	}

	private static Action byClass(MapCovSupport support, String simpleName) {
		Action found = null;
		for (Action candidate : support.actions()) {
			if (simpleName.equals(candidate.getClass().getSimpleName())) {
				found = candidate;
			}
		}
		if (found == null) {
			throw new AssertionError(simpleName);
		}
		return found;
	}

	private static Object editControl(MapCovSupport support, NodeView view, MindMapNode node, MindMapNode target,
			boolean existing) throws Exception {
		Class<?> type = Class.forName("plugins.map.FreeMindMapController$MapEditTextFieldControl");
		Constructor<?> constructor = type.getDeclaredConstructors()[0];
		constructor.setAccessible(true);
		return constructor.newInstance(support.maps, view, node, target, existing);
	}

	private static Object noteField(MapCovSupport support, NodeView view, Object control) throws Exception {
		Class<?> type = Class.forName("plugins.map.FreeMindMapController$MapEditNoteTextField");
		Constructor<?> constructor = type.getDeclaredConstructors()[0];
		constructor.setAccessible(true);
		return constructor.newInstance(support.maps, view, "note", null, support.opened.mode, control, support.viewer,
				new Point(3, 4));
	}

	private static Object searchTask(MapCovSupport support, int[] rows) throws Exception {
		Class<?> type = Class.forName("plugins.map.FreeMindMapController$AddSearchResultsToMapTask");
		Constructor<?> constructor = type.getDeclaredConstructors()[0];
		constructor.setAccessible(true);
		return constructor.newInstance(support.maps, rows);
	}

	private static Object searchTaskWithoutProgressDialog(MapCovSupport support, Place place) throws Exception {
		Class<?> unsafeType = Class.forName("sun.misc.Unsafe");
		java.lang.reflect.Field theUnsafe = unsafeType.getDeclaredField("theUnsafe");
		theUnsafe.setAccessible(true);
		Object unsafe = theUnsafe.get(null);
		Class<?> type = Class.forName("plugins.map.FreeMindMapController$AddSearchResultsToMapTask");
		Object task = unsafeType.getMethod("allocateInstance", Class.class).invoke(unsafe, type);
		MapCovSupport.set(task, "this$0", support.maps);
		MapCovSupport.set(task, "mPlaces", new Place[] { place });
		return task;
	}
}
