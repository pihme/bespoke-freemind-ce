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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Properties;

import javax.swing.JLabel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openstreetmap.gui.jmapviewer.events.JMVCommandEvent;

import freemind.controller.actions.generated.instance.Place;
import freemind.modes.MindMapNode;
import plugins.map.MapDialog;
import plugins.map.MapMarkerLocation;
import plugins.map.MapNodePositionHolder;

@DisplayName("MapDialogBehaviourTest")
class MapDialogBehaviourTest {

	@Test
	// backwards compatibility
	void startupStopsWhenTheWindowCannotBeCreated() throws Exception {
		try (MapCovSupport support = MapCovSupport.open("Child")) {
			MapDialog fresh = new MapDialog();
			fresh.setController(support.opened.mode);
			fresh.setProperties(new Properties());
			assertThrows(HeadlessException.class, fresh::startupMapHook);
			assertSame(support.opened.mode, fresh.getMindMapController());
		}
	}

	@Test
	// backwards compatibility
	void togglesTheSearchBarAndListsPlaces() throws Exception {
		try (MapCovSupport support = MapCovSupport.open("Child")) {
			assertFalse(support.dialog.isSearchBarVisible());
			support.dialog.toggleSearchBar();
			assertTrue(support.dialog.isSearchBarVisible());
			assertFalse(support.dialog.isLimitSearchToRegion());
			support.dialog.toggleLimitSearchToRegion();
			assertTrue(support.dialog.isLimitSearchToRegion());
			assertEquals("search-here", support.searchLabel.getText());
			support.dialog.toggleLimitSearchToRegion();
			assertEquals("search", support.searchLabel.getText());

			support.searchTerm.setText("abc");
			MapCovSupport.call(support.dialog, "focusSearchTerm");
			assertEquals(0, support.searchTerm.getSelectionStart());
			assertEquals(3, support.searchTerm.getSelectionEnd());

			KeyEvent typed = new KeyEvent(support.searchTerm, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0,
					KeyEvent.VK_UNDEFINED, 'z');
			support.dialog.toggleSearchBar(typed);
			assertFalse(support.dialog.isSearchBarVisible());
			assertEquals(0, support.results.getRowCount());
			support.dialog.toggleSearchBar(typed);
			assertTrue(support.dialog.isSearchBarVisible());

			boolean found = support.dialog.getFreeMindMapController().search(support.results, support.resultTable,
					"47.2 11.3", Color.WHITE);
			assertTrue(found);
			assertEquals(1, support.results.getRowCount());
			assertEquals(String.class, support.results.getColumnClass(0));
			assertEquals(Double.class, support.results.getColumnClass(1));
			assertEquals(Object.class, support.results.getColumnClass(2));
			assertEquals(2, support.results.getColumnCount());
			assertNotNull(support.results.getColumnName(0));
			assertTrue(support.results.getValueAt(0, 0).toString().contains("47.2"));
			assertTrue(((Double) support.results.getValueAt(0, 1)) >= 0);
			assertSame(support.results.getPlace(0), support.dialog.getPlace(0));
			assertNotNull(support.results.getMapSearchMarkerLocation(0));
			assertThrows(IllegalArgumentException.class, () -> support.results.getMapSearchMarkerLocation(4));
			assertThrows(IllegalArgumentException.class, () -> support.dialog.getPlace(-1));

			Place far = new Place();
			far.setDisplayName("Far");
			far.setLat(Double.NaN);
			far.setLon(Double.NaN);
			far.setOsmType("node");
			support.results.addPlace(far);
			assertEquals(-1.0, (Double) support.results.getValueAt(1, 1));
			support.results.cursorPositionChanged(new org.openstreetmap.gui.jmapviewer.Coordinate(10, 10));
			assertEquals(10.0, ((org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate) MapCovSupport
					.get(support.results, "mCursorCoordinate")).getLat());

			support.dialog.setSingleSearch();
			support.dialog.displaySearchItem(0);
			assertFalse(support.dialog.isSearchBarVisible());
			assertEquals(47.2, support.cursor().getLat(), 0.001);

			support.dialog.search("47.5 12.5", true);
			assertEquals(47.5, support.cursor().getLat(), 0.001);
		}
	}

	@Test
	// backwards compatibility
	void showsAndHidesMarkersForFoldedNodes() throws Exception {
		try (MapCovSupport support = MapCovSupport.open("Child", "Sibling")) {
			MindMapNode child = support.opened.child("Child");
			MindMapNode sibling = support.opened.child("Sibling");
			MapNodePositionHolder holder = support.attach(child, 49.8, 8.8, 8);
			MapNodePositionHolder other = support.attach(sibling, 50.2, 9.1, 8);
			assertTrue(support.dialog.getAllMapNodePositionHolders().contains(holder));
			support.dialog.registerMapNode(holder);
			assertTrue(support.dialog.getMarkerMap().containsKey(holder));
			support.dialog.registerMapNode(holder);
			assertEquals(2, support.dialog.getMapNodePositionHolders().size());

			child.setText("Moved label");
			support.dialog.onUpdateNodeHook(child);
			assertEquals("Moved label", support.dialog.getMarkerMap().get(holder).getText());
			support.dialog.onSaveNode(child);
			support.dialog.onFocusNode(support.opened.view.getNodeView(child));
			support.dialog.onLostFocusNode(support.opened.view.getNodeView(child));
			assertEquals("Moved label", support.dialog.getMarkerMap().get(holder).getText());

			support.dialog.selectMapPosition(support.opened.view.getNodeView(child), true);
			assertTrue(support.dialog.getMarkerMap().get(holder).isSelected());
			support.dialog.onSelectionChange(support.opened.view.getNodeView(child), false);
			assertFalse(support.dialog.getMarkerMap().get(holder).isSelected());

			support.opened.root.setFolded(true);
			support.dialog.nodeVisibilityChanged(holder, false);
			assertFalse(support.dialog.getMarkerMap().containsKey(holder));
			support.dialog.nodeVisibilityChanged(holder, true);
			assertTrue(support.dialog.getMarkerMap().containsKey(holder));
			support.dialog.addMarkersToMap();
			assertFalse(support.dialog.getMarkerMap().containsKey(holder));
			MapCovSupport.call(support.viewer, "setHideFoldedNodes", new Class<?>[] { boolean.class }, false);
			support.dialog.addMarkersToMap();
			assertTrue(support.dialog.getMarkerMap().containsKey(holder));
			support.dialog.deregisterMapNode(holder);
			assertFalse(support.dialog.getMarkerMap().containsKey(holder));

			support.dialog.registerMapNode(other);
			assertTrue(support.dialog.getMarkerMap().get(other) instanceof MapMarkerLocation);
			assertTrue(support.dialog.isMapModuleChangeAllowed(null, null, null, null));
			int markers = support.dialog.getMarkerMap().size();
			support.dialog.beforeMapModuleChange(null, null, null, null);
			support.dialog.numberOfOpenMapInformation(1, 0);
			assertEquals(markers, support.dialog.getMarkerMap().size());
			assertSame(support.status, support.dialog.getStatusLabel());
			assertSame(support.registration, support.dialog.getRegistration());
			assertNotNull(support.dialog.getCloseAction());
		}
	}

	@Test
	// backwards compatibility
	void updatesZoomLabelsAndStoresTheWindowWhenItCloses() throws Exception {
		try (MapCovSupport support = MapCovSupport.open("Child")) {
			JLabel meters = new JLabel("old-m");
			JLabel zoom = new JLabel("old-z");
			MapCovSupport.set(support.dialog, "mperpLabelValue", meters);
			MapCovSupport.set(support.dialog, "zoomValue", zoom);
			support.dialog.processCommand(new JMVCommandEvent(JMVCommandEvent.COMMAND.ZOOM, support.viewer));
			assertFalse(meters.getText().equals("old-m"));
			assertFalse(zoom.getText().equals("old-z"));
			String zoomText = zoom.getText();
			support.dialog.processCommand(new JMVCommandEvent(JMVCommandEvent.COMMAND.MOVE, support.viewer));
			assertEquals(zoomText, zoom.getText());

			Place place = new Place();
			place.setDisplayName("Pinned");
			place.setLat(49.9);
			place.setLon(8.9);
			place.setOsmType("node");
			support.results.addPlace(place);
			support.resultTable.getSelectionModel().setSelectionInterval(0, 0);
			support.select(support.opened.child("Child"));
			int children = support.opened.child("Child").getParentNode().getChildCount();
			assertThrows(HeadlessException.class,
					() -> MapCovSupport.call(support.dialog, "addSearchResultsToMap"));
			assertEquals(children, support.opened.root.getChildCount());

			try {
				support.dialog.afterMapClose(null, null);
			} catch (RuntimeException ex) {
				assertNotNull(ex);
			}
			assertNull(support.registration.getMapDialog());
			assertNotNull(support.opened.frame.getProperty(MapDialog.class.getName()));
			support.registration.setMapDialog(support.dialog);
			try {
				support.dialog.afterMapModuleChange(null, null, null, null);
			} catch (RuntimeException ex) {
				assertNotNull(ex);
			}
			support.registration.setMapDialog(support.dialog);
			try {
				Object close = MapCovSupport.call(support.dialog, "getCloseAction");
				MapCovSupport.call(close, "actionPerformed", new Class<?>[] { ActionEvent.class },
						new ActionEvent(support.viewer, 0, "close"));
			} catch (RuntimeException ex) {
				assertNotNull(ex);
			}
			assertNull(support.registration.getMapDialog());
			assertNotNull(support.opened.frame.getProperty(MapDialog.class.getName()));
		}
	}
}
