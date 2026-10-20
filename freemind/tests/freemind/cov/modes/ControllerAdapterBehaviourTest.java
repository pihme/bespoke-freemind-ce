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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.junit.jupiter.api.Test;

import freemind.controller.LastStateStorageManagement;
import freemind.controller.MindMapNodesSelection;
import freemind.controller.StructuredMenuHolder;
import freemind.controller.actions.generated.instance.MindmapLastStateMapStorage;
import freemind.controller.actions.generated.instance.MindmapLastStateStorage;
import freemind.controller.actions.generated.instance.NodeListMember;
import freemind.extensions.PermanentNodeHookAdapter;
import freemind.main.FreeMindCommon;
import freemind.main.Tools;
import freemind.modes.ControllerAdapter;
import freemind.modes.FreeMindFileDialog;
import freemind.modes.MapAdapter;
import freemind.modes.MapFeedback;
import freemind.modes.MindMapNode;
import freemind.modes.ModeController;
import freemind.view.mindmapview.NodeView;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Saving, selection, links, and file drops keep the controller's old results. */
class ControllerAdapterBehaviourTest {

	@Test
	void saveWritesTheMapWhenItIsDirtyAndReportsSuccessWhenItIsClean() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		assertTrue(opened.mode.getModel().isSaved());
		assertTrue(opened.mode.save());
		File file = File.createTempFile("saved-map", ".mm");
		file.deleteOnExit();
		String key = FreeMindCommon.CREATE_THUMBNAIL_ON_SAVE;
		String previous = opened.frame.getProperty(key);
		opened.frame.setProperty(key, "false");
		try {
			opened.mode.getModel().setFile(file);
			opened.mode.getModel().setSaved(false);
			assertTrue(opened.mode.save());
			assertTrue(opened.mode.getModel().isSaved());
			assertTrue(Files.readString(file.toPath()).contains("Child"));
			opened.mode.new SaveAction().actionPerformed(
					new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "save"));
			assertTrue(opened.mode.getModel().isSaved());
			opened.mode.getModel().setSaved(false);
			opened.mode.new SaveAction().actionPerformed(
					new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "save"));
			assertTrue(opened.mode.getModel().isSaved());
			assertTrue(Files.readString(file.toPath()).contains("Child"));
		} finally {
			restore(opened, key, previous);
		}
	}

	@Test
	void refreshDisplayCenterAndDepthUseTheOpenMap() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		final boolean[] changed = { false };
		((MapAdapter) opened.mode.getMap()).addTreeModelListener(new javax.swing.event.TreeModelListener() {
			public void treeNodesChanged(javax.swing.event.TreeModelEvent event) {
				changed[0] = true;
			}

			public void treeNodesInserted(javax.swing.event.TreeModelEvent event) {
			}

			public void treeNodesRemoved(javax.swing.event.TreeModelEvent event) {
			}

			public void treeStructureChanged(javax.swing.event.TreeModelEvent event) {
			}
		});
		opened.mode.refreshMap();
		assertTrue(changed[0]);
		assertEquals("Child", opened.child("Child").getText());

		MindMapNode child = opened.child("Child");
		MindMapNode grand = new freemind.modes.mindmapmode.MindMapNodeModel("Grand", opened.mode.getMap());
		child.insert(grand, 0);
		opened.root.setFolded(true);
		ArrayList<MindMapNode> unfolded = new ArrayList<>();
		opened.mode.displayNode(grand, unfolded);
		assertFalse(opened.root.isFolded());
		assertTrue(unfolded.contains(opened.root));

		MindMapNode before = opened.mode.getSelected();
		opened.mode.centerNode(null);
		assertSame(before, opened.mode.getSelected());
		opened.mode.centerNode(child);
		assertEquals("Child", opened.mode.getSelected().getText());

		opened.view.selectAsTheOnlyOneSelected(opened.view.getNodeView(opened.root));
		opened.view.makeTheSelected(opened.view.getNodeView(child));
		List<MindMapNode> byDepth = opened.mode.getSelectedsByDepth();
		assertEquals("Child", byDepth.get(0).getText());
		assertTrue(byDepth.contains(opened.root));
	}

	@Test
	void copyAndLocalLinksReturnTheSelectedNode() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode child = opened.child("Child");
		opened.view.selectAsTheOnlyOneSelected(opened.view.getNodeView(child));
		Transferable copied = opened.mode.copy();
		Transferable single = opened.mode.copySingle();
		assertTrue(String.valueOf(copied.getTransferData(MindMapNodesSelection.mindMapNodesFlavor)).contains("Child"));
		assertTrue(String.valueOf(single.getTransferData(MindMapNodesSelection.mindMapNodesFlavor)).contains("Child"));

		opened.view.selectAsTheOnlyOneSelected(opened.view.getRoot());
		String id = opened.mode.getNodeID(child);
		opened.mode.loadURL("#" + id);
		assertEquals("Child", opened.mode.getSelected().getText());
		assertEquals("Child", opened.mode.getLinkShortText(linkTo(child, "#" + id)));
		opened.mode.loadURL("#missing-id");
		assertEquals("Child", opened.mode.getSelected().getText());
		assertEquals("link_not_available_any_more", opened.mode.getLinkShortText(linkTo(child, "#missing-id")));
		child.setLink(null);
		opened.mode.loadURL();
		assertEquals("Child", opened.mode.getSelected().getText());
		assertNull(opened.mode.getLinkShortText(child));

		File mapFile = File.createTempFile("here", ".mm");
		mapFile.deleteOnExit();
		opened.mode.getModel().setFile(mapFile);
		child.setLink("notes.txt");
		opened.mode.loadURL();
		assertEquals("Child", opened.mode.getSelected().getText());
		assertEquals("notes.txt", opened.mode.getLinkShortText(child));
	}

	@Test
	void menusAndTheFileChooserExposeTheChosenItems() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		Action action = new AbstractAction("Copy") {
			public void actionPerformed(ActionEvent event) {
			}
		};
		Method addKey = ControllerAdapter.class.getDeclaredMethod("add", JMenu.class, Action.class, String.class);
		addKey.setAccessible(true);
		JMenu menu = new JMenu("File");
		JMenuItem item = (JMenuItem) addKey.invoke(opened.mode, menu, action, "keystroke_copy");
		assertEquals("Copy", item.getText());
		assertNull(item.getAccelerator());
		assertSame(item, menu.getItem(0));

		Method addPlain = ControllerAdapter.class.getDeclaredMethod("add", JMenu.class, Action.class);
		addPlain.setAccessible(true);
		JMenu plain = new JMenu();
		addPlain.invoke(opened.mode, plain, action);
		assertEquals(1, plain.getItemCount());

		StructuredMenuHolder holder = new StructuredMenuHolder();
		Method addHolder = ControllerAdapter.class.getDeclaredMethod("add", StructuredMenuHolder.class, String.class,
				Action.class, String.class);
		addHolder.setAccessible(true);
		JMenuItem unkeyed = (JMenuItem) addHolder.invoke(opened.mode, holder, "menu", action, null);
		JMenuItem keyed = (JMenuItem) addHolder.invoke(opened.mode, holder, "menu", action, "keystroke_copy");
		assertEquals("Copy", unkeyed.getText());
		assertNull(keyed.getAccelerator());

		Method addCheck = ControllerAdapter.class.getDeclaredMethod("addCheckBox", StructuredMenuHolder.class,
				String.class, Action.class, String.class);
		addCheck.setAccessible(true);
		JMenuItem check = (JMenuItem) addCheck.invoke(opened.mode, holder, "menu", action, "keystroke_copy");
		assertEquals("Copy", check.getText());
		assertFalse(check.isSelected());

		Method addRadio = ControllerAdapter.class.getDeclaredMethod("addRadioItem", StructuredMenuHolder.class,
				String.class, Action.class, String.class, boolean.class);
		addRadio.setAccessible(true);
		JMenuItem radio = (JMenuItem) addRadio.invoke(opened.mode, holder, "menu", action, null, true);
		assertTrue(radio.isSelected());

		File mapFile = File.createTempFile("chooser", ".mm");
		mapFile.deleteOnExit();
		opened.mode.getModel().setFile(mapFile);
		FreeMindFileDialog chooser = opened.mode.getFileChooser();
		assertEquals(mapFile.getParentFile(), ((JFileChooser) chooser).getCurrentDirectory());
	}

	@Test
	void listenersSurviveAThrowAndFoldHooksSeeTheView() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		SelectionProbe throwing = new SelectionProbe(true);
		opened.mode.registerNodeSelectionListener(throwing, true);
		assertEquals(1, throwing.focus);
		NodeView childView = opened.view.getNodeView(opened.child("Child"));
		assertNotNull(childView);
		opened.view.selectAsTheOnlyOneSelected(childView);
		SelectionProbe later = new SelectionProbe(false);
		opened.mode.registerNodeSelectionListener(later, true);
		assertTrue(later.focus >= 1);
		assertTrue(later.selection >= 1);
		SelectionProbe throwingSelection = new SelectionProbe(false, true);
		opened.mode.registerNodeSelectionListener(throwingSelection, false);
		opened.mode.changeSelection(childView, false);
		assertEquals(1, throwingSelection.selection);
		opened.mode.firePreSaveEvent(opened.root);
		assertTrue(later.save >= 1);
		opened.mode.nodeChanged(opened.child("Child"));
		assertTrue(later.update >= 1);
		assertFalse(opened.mode.getMap().isSaved());
		opened.mode.onLostFocusNode(childView);
		assertTrue(later.lost >= 1);
		opened.mode.onFocusNode(childView);
		opened.mode.deregisterNodeSelectionListener(throwing);
		opened.mode.onFocusNode(childView);
		assertTrue(later.focus >= 2);
		opened.mode.setVisible(false);
		opened.mode.setVisible(true);
		assertEquals("Child", opened.child("Child").getText());
		opened.mode.deregisterNodeSelectionListener(later);
		int saves = later.save;
		opened.mode.firePreSaveEvent(opened.root);
		assertEquals(saves, later.save);

		final int[] created = { 0 };
		final int[] deleted = { 0 };
		ModeController.NodeLifetimeListener life = new ModeController.NodeLifetimeListener() {
			public void onCreateNodeHook(MindMapNode node) {
				created[0]++;
			}

			public void onPreDeleteNode(MindMapNode node) {
				deleted[0]++;
			}

			public void onPostDeleteNode(MindMapNode node, MindMapNode parent) {
				deleted[0] += 10;
			}
		};
		opened.mode.registerNodeLifetimeListener(life, true);
		assertTrue(created[0] >= 2);
		opened.mode.fireNodePreDeleteEvent(opened.child("Child"));
		opened.mode.fireNodePostDeleteEvent(opened.child("Child"), opened.root);
		assertTrue(deleted[0] >= 11);
		opened.mode.deregisterNodeLifetimeListener(life);
		assertFalse(opened.mode.getNodeLifetimeListeners().contains(life));

		ViewHook hook = new ViewHook();
		MindMapNode child = opened.child("Child");
		child.addHook(hook);
		child.invokeHook(hook);
		NodeView view = opened.view.getNodeView(child);
		opened.mode.onViewCreatedHook(view);
		opened.mode.onViewRemovedHook(view);
		assertEquals(1, hook.created);
		assertEquals(1, hook.removed);
	}

	@Test
	void popupClickWithoutATriggerStaysUnconsumed() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MouseEvent event = new MouseEvent(opened.view, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 4, 4,
				1, false, MouseEvent.BUTTON1);
		opened.mode.showPopupMenu(event);
		assertFalse(event.isConsumed());
		assertFalse(opened.mode.isBlocked());
	}

	@Test
	void loadingAMapTwiceReusesItAndADropAcceptsTheSameFile() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		Object opener = fileOpener(opened.mode);
		DropTargetDropEvent rejected = mock(DropTargetDropEvent.class);
		when(rejected.getCurrentDataFlavors()).thenReturn(new DataFlavor[] { DataFlavor.stringFlavor });
		opener.getClass().getMethod("drop", DropTargetDropEvent.class).invoke(opener, rejected);
		verify(rejected).rejectDrop();

		DropTargetDropEvent empty = mock(DropTargetDropEvent.class);
		Transferable nothing = mock(Transferable.class);
		when(empty.getCurrentDataFlavors()).thenReturn(new DataFlavor[] { DataFlavor.javaFileListFlavor });
		when(empty.getTransferable()).thenReturn(nothing);
		when(nothing.getTransferData(DataFlavor.javaFileListFlavor)).thenReturn(null);
		opener.getClass().getMethod("drop", DropTargetDropEvent.class).invoke(opener, empty);
		verify(empty).acceptDrop(java.awt.dnd.DnDConstants.ACTION_COPY);
		verify(empty).dropComplete(false);

		DropTargetDragEvent accepted = mock(DropTargetDragEvent.class);
		when(accepted.getCurrentDataFlavors()).thenReturn(new DataFlavor[] { DataFlavor.javaFileListFlavor });
		opener.getClass().getMethod("dragEnter", DropTargetDragEvent.class).invoke(opener, accepted);
		verify(accepted, never()).rejectDrag();
		DropTargetDragEvent refused = mock(DropTargetDragEvent.class);
		when(refused.getCurrentDataFlavors()).thenReturn(new DataFlavor[] { DataFlavor.stringFlavor });
		opener.getClass().getMethod("dragEnter", DropTargetDragEvent.class).invoke(opener, refused);
		verify(refused).rejectDrag();
		assertEquals("Child", opened.child("Child").getText());
	}

	@Test
	void lastStateRestoresTheZoomAndACorruptStoreReturnsToTheRoot() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		File file = File.createTempFile("state", ".mm");
		file.deleteOnExit();
		opened.mode.getModel().setFile(file);
		String key = FreeMindCommon.MINDMAP_LAST_STATE_MAP_STORAGE;
		String previous = opened.frame.getProperty(key);
		String childId = opened.mode.getNodeID(opened.child("Child"));
		try {
			MindmapLastStateStorage store = new MindmapLastStateStorage();
			store.setRestorableName(opened.mode.getModel().getRestorable());
			store.setLastZoom(1.25f);
			store.setLastSelected(childId);
			NodeListMember member = new NodeListMember();
			member.setNode(childId);
			store.addNodeListMember(member);
			MindmapLastStateMapStorage map = new MindmapLastStateMapStorage();
			map.getListMindmapLastStateStorageList().add(store);
			opened.frame.setProperty(key, Tools.marshall(map));
			restoreLastState(opened);
			assertEquals("Child", opened.mode.getSelected().getText());
			assertEquals(1.25f, opened.view.getZoom());

			opened.frame.setProperty(key, "<not-xml");
			assertNotNull(new LastStateStorageManagement(opened.frame.getProperty(key)));
			restoreLastState(opened);
			assertSame(opened.root, opened.mode.getSelected());
		} finally {
			restore(opened, key, previous);
		}
	}

	@Test
	void closingStoresEverySelectedNode() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		File file = File.createTempFile("closed", ".mm");
		file.deleteOnExit();
		opened.mode.getModel().setFile(file);
		opened.view.selectAsTheOnlyOneSelected(opened.view.getNodeView(opened.root));
		opened.view.makeTheSelected(opened.view.getNodeView(opened.child("Child")));
		String childId = opened.mode.getNodeID(opened.child("Child"));
		String rootId = opened.mode.getNodeID(opened.root);
		assertTrue(opened.mode.close(true, opened.app.getMapModuleManager()));
		String stored = opened.frame.getProperty(FreeMindCommon.MINDMAP_LAST_STATE_MAP_STORAGE);
		assertTrue(stored.contains(file.getAbsolutePath()));
		assertTrue(stored.contains(childId));
		assertTrue(stored.contains(rootId));
	}

	private static MindMapNode linkTo(MindMapNode node, String link) {
		node.setLink(link);
		return node;
	}

	private static void restore(Opened opened, String key, String previous) {
		if (previous == null) {
			opened.frame.getProperties().remove(key);
		} else {
			opened.frame.setProperty(key, previous);
		}
	}

	private static void restoreLastState(Opened opened) throws Exception {
		Method method = ControllerAdapter.class.getDeclaredMethod("restoreMapsLastState", ModeController.class,
				MapAdapter.class);
		method.setAccessible(true);
		method.invoke(opened.mode, opened.mode, opened.mode.getModel());
	}

	private static Object fileOpener(ControllerAdapter mode) throws Exception {
		Class<?> type = Class.forName("freemind.modes.ControllerAdapter$FileOpener");
		Constructor<?> constructor = type.getDeclaredConstructor(ControllerAdapter.class);
		constructor.setAccessible(true);
		return constructor.newInstance(mode);
	}

	private static final class SelectionProbe implements ModeController.NodeSelectionListener {
		private final boolean throwOnFocus;
		private final boolean throwOnSelection;
		int focus;
		int lost;
		int update;
		int save;
		int selection;

		SelectionProbe(boolean throwOnFocus) {
			this(throwOnFocus, false);
		}

		SelectionProbe(boolean throwOnFocus, boolean throwOnSelection) {
			this.throwOnFocus = throwOnFocus;
			this.throwOnSelection = throwOnSelection;
		}

		public void onUpdateNodeHook(MindMapNode node) {
			update++;
		}

		public void onFocusNode(NodeView node) {
			focus++;
			if (throwOnFocus) {
				throw new IllegalStateException("focus");
			}
		}

		public void onLostFocusNode(NodeView node) {
			lost++;
		}

		public void onSaveNode(MindMapNode node) {
			save++;
		}

		public void onSelectionChange(NodeView node, boolean selected) {
			selection++;
			if (throwOnSelection) {
				throw new IllegalStateException("selection");
			}
		}
	}

	private static final class ViewHook extends PermanentNodeHookAdapter {
		int created;
		int removed;

		public void onViewCreatedHook(NodeView node) {
			created++;
		}

		public void onViewRemovedHook(NodeView node) {
			removed++;
		}
	}
}
