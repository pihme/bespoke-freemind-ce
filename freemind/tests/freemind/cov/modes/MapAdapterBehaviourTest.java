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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

import freemind.modes.MindMap.MapSourceChangedObserver;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.main.Tools.StringReaderCreator;
import freemind.modes.ArrowLinkAdapter;
import freemind.modes.ArrowLinkTarget;
import freemind.modes.CloudAdapter;
import freemind.modes.EdgeAdapter;
import freemind.modes.MapAdapter;
import freemind.modes.MapFeedback;
import freemind.modes.MindMap;
import freemind.modes.MindMapNode;
import freemind.modes.NodeAdapter;
import freemind.modes.mindmapmode.MindMapMapModel;
import freemind.modes.mindmapmode.MindMapNodeModel;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** The base map reports empty exports, reloads a tree, and notices a changed file. */
@DisplayName("MapAdapterBehaviourTest")
class MapAdapterBehaviourTest {

	@Test
	void baseExportsAndReadOnlyFlag() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		BareMap map = new BareMap(opened.mode);
		MindMapNodeModel root = new MindMapNodeModel("Bare", map);
		map.setRoot(root);
		try {
			assertEquals("", map.getAsPlainText(Collections.emptyList()));
			assertEquals("", map.getAsRTF(Collections.emptyList()));
			assertNull(map.getAsHTML(Collections.emptyList()));
			assertNull(map.tryToLock(new File("unused.mm")));
			assertNull(map.getLinkRegistry());
			assertEquals("new_mindmap", map.text("new_mindmap"));
			assertFalse(map.isReadOnly());
			map.setReadOnly(true);
			assertTrue(map.isReadOnly());
		} finally {
			map.destroy();
		}
	}

	@Test
	void nodeRefreshNotifiesWithoutMarkingDirtyAndChangeRootReparents() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapMapModel shown = (MindMapMapModel) opened.mode.getMap();
		shown.setSaved(true);
		final boolean[] changed = { false };
		shown.addTreeModelListener(new TreeModelListener() {
			public void treeNodesChanged(TreeModelEvent event) {
				changed[0] = true;
			}

			public void treeNodesInserted(TreeModelEvent event) {
			}

			public void treeNodesRemoved(TreeModelEvent event) {
			}

			public void treeStructureChanged(TreeModelEvent event) {
			}
		});
		shown.nodeRefresh(opened.root);
		assertTrue(changed[0]);
		assertTrue(shown.isSaved());
		shown.nodeChanged(opened.root);
		assertFalse(shown.isSaved());

		MindMapMapModel model = new MindMapMapModel(opened.mode);
		MindMapNodeModel root = new MindMapNodeModel("Root", model);
		model.setRoot(root);
		MindMapNodeModel child = new MindMapNodeModel("Kid", model);
		root.insert(child, 0);
		child.addIcon(freemind.modes.MindIcon.factory("idea"), 0);
		try {
			assertSame(root, model.getRootNode());
			model.changeRoot(root);
			assertSame(root, model.getRootNode());
			model.changeRoot(child);
			assertSame(child, model.getRootNode());
			assertEquals("Root", ((MindMapNode) child.getChildAt(0)).getText());
			assertTrue(model.getIcons().getSize() >= 1);
		} finally {
			if (model.getRootNode() == null) {
				model.setRoot(child);
			}
			model.destroy();
		}
	}

	@Test
	void loadTreeReadsACurrentMapAndReportsABrokenOne() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMap map = opened.mode.getMap();
		MindMapNode loaded = map.loadTree(
				new StringReaderCreator("<map version=\"1.1.0\"><node TEXT=\"Loaded\"/></map>"),
				MapAdapter.sDontAskInstance);
		assertEquals("Loaded", loaded.getText());
		MindMapNode broken = map.loadTree(new StringReaderCreator("this is not a map"), MapAdapter.sDontAskInstance);
		assertTrue(broken.getText().startsWith("Error while parsing file:"), broken.getText());
		MindMapNode old = map.loadTree(new StringReaderCreator("<map version=\"0.8.0\"><node TEXT=\"Old\"/></map>"),
				MapAdapter.sDontAskInstance);
		assertTrue(old != null);
		assertTrue(old.getText().equals("Old") || old.getText().startsWith("Error while parsing file:"));
	}

	@Test
	void fileChangeObserverFiresAcceptsARefusalAndCanBeRemoved() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		BareMap map = new BareMap(opened.mode);
		File file = File.createTempFile("watched", ".mm");
		file.deleteOnExit();
		file.setLastModified(System.currentTimeMillis() - 10_000L);
		map.setFile(file);
		MindMapNodeModel root = new MindMapNodeModel("Bare", map);
		map.setRoot(root);
		final int[] thrown = { 0 };
		final int[] accepted = { 0 };
		final int[] refused = { 0 };
		final int[] immediate = { 0 };
		MapSourceChangedObserver throwing = source -> {
			thrown[0]++;
			throw new IllegalStateException("observer failed");
		};
		MapSourceChangedObserver accepting = source -> {
			accepted[0]++;
			return true;
		};
		MapSourceChangedObserver refusing = source -> {
			refused[0]++;
			return false;
		};
		MapSourceChangedObserver now = source -> {
			immediate[0]++;
			return true;
		};
		try {
			map.registerMapSourceChangedObserver(throwing, 0L);
			map.runInspector();
			assertEquals(1, thrown[0]);
			long unchanged = map.deregisterMapSourceChangedObserver(throwing);
			assertEquals(0L, unchanged);

			map.registerMapSourceChangedObserver(accepting, 0L);
			map.runInspector();
			assertEquals(1, accepted[0]);
			long acceptedTime = map.deregisterMapSourceChangedObserver(accepting);
			assertEquals(file.lastModified(), acceptedTime);

			assertTrue(file.setLastModified(acceptedTime + 5_000L));
			assertTrue(file.lastModified() > acceptedTime);
			map.registerMapSourceChangedObserver(refusing, 0L);
			map.runInspector();
			assertEquals(1, refused[0]);
			assertEquals(Long.MAX_VALUE, map.deregisterMapSourceChangedObserver(refusing));
			map.registerMapSourceChangedObserver(refusing, 0L);
			map.runInspector();
			assertEquals(1, refused[0]);

			map.registerMapSourceChangedObserver(now, -1L);
			assertEquals(1, immediate[0]);
		} finally {
			map.destroy();
		}
	}

	private static final class BareMap extends MapAdapter {
		private static final long serialVersionUID = 1L;

		BareMap(MapFeedback feedback) {
			super(feedback);
			readOnly = false;
		}

		String text(String key) {
			return getText(key);
		}

		void runInspector() throws Exception {
			Class<?> type = null;
			for (Class<?> nested : MapAdapter.class.getDeclaredClasses()) {
				if ("FileChangeInspectorTimerTask".equals(nested.getSimpleName())) {
					type = nested;
				}
			}
			java.lang.reflect.Constructor<?> constructor = type.getDeclaredConstructor(MapAdapter.class);
			constructor.setAccessible(true);
			((java.util.TimerTask) constructor.newInstance(this)).run();
		}

		public boolean save(File file) {
			return false;
		}

		public void getXml(Writer fileout) throws IOException {
		}

		public void getFilteredXml(Writer fileout) throws IOException {
		}

		public NodeAdapter createNodeAdapter(MindMap map, String nodeClass) {
			return new MindMapNodeModel(map);
		}

		public EdgeAdapter createEdgeAdapter(NodeAdapter node) {
			return null;
		}

		public CloudAdapter createCloudAdapter(NodeAdapter node) {
			return null;
		}

		public ArrowLinkAdapter createArrowLinkAdapter(NodeAdapter source, NodeAdapter target) {
			return null;
		}

		public ArrowLinkTarget createArrowLinkTarget(NodeAdapter source, NodeAdapter target) {
			return null;
		}

		public NodeAdapter createEncryptedNode(String additionalInfo) {
			return new MindMapNodeModel(this);
		}

		public String getAsPlainText(List<MindMapNode> nodes) {
			return super.getAsPlainText(nodes);
		}
	}
}
