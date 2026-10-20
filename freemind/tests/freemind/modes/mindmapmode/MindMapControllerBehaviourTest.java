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

package freemind.modes.mindmapmode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.filechooser.FileFilter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import freemind.controller.Controller;
import freemind.controller.MapMouseMotionListener;
import freemind.controller.MapMouseWheelListener;
import freemind.controller.MenuItemEnabledListener;
import freemind.controller.NodeDropListener;
import freemind.controller.NodeKeyListener;
import freemind.controller.NodeMotionListener;
import freemind.controller.NodeMouseMotionListener;
import freemind.controller.StructuredMenuHolder;
import freemind.controller.actions.generated.instance.MenuAction;
import freemind.controller.actions.generated.instance.MenuCategory;
import freemind.controller.actions.generated.instance.MenuCheckedAction;
import freemind.controller.actions.generated.instance.MenuRadioAction;
import freemind.controller.actions.generated.instance.MenuSeparator;
import freemind.controller.actions.generated.instance.MenuStructure;
import freemind.controller.actions.generated.instance.MenuSubmenu;
import freemind.extensions.ModeControllerHookAdapter;
import freemind.main.HeadlessFreeMind;
import freemind.main.Resources;
import freemind.main.Tools;
import freemind.modes.MapAdapter;
import freemind.modes.MindMapLink;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.actions.ApplyPatternAction;
import freemind.modes.mindmapmode.actions.IconAction;
import freemind.view.mindmapview.MainView;
import freemind.view.mindmapview.ViewFeedback;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Mind map controller behaviour that stays off modal dialogs and file choosers. */
class MindMapControllerBehaviourTest {

	@Test
	@Timeout(15)
	void startupEnablesActionsAndShutdownDropsTheZoomListener() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Kept");
		installHeadlessControllerParts(opened);
		opened.mode.startupController();
		assertTrue(opened.mode.bold.isEnabled());
		MindMapToolBar bar = (MindMapToolBar) opened.mode.getModeToolBar();
		opened.app.setZoom(1.5f);
		assertEquals(1.5f, opened.view.getZoom(), 0.001f);
		assertEquals("150%", zoomItem(bar));

		opened.mode.shutdownController();
		assertFalse(opened.mode.bold.isEnabled());
		opened.app.setZoom(2.0f);
		assertEquals(2.0f, opened.view.getZoom(), 0.001f);
		assertEquals("150%", zoomItem(bar));
	}

	@Test
	void fileFilterAcceptsDirectoriesAndMindMapExtensions() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open();
		FileFilter filter = opened.mode.getFileFilter();
		File dir = Files.createTempDirectory("mm-filter").toFile();
		assertTrue(filter.accept(dir));
		assertTrue(filter.accept(new File(dir, "map.mm")));
		assertFalse(filter.accept(new File(dir, "map.txt")));
		assertFalse(filter.accept(new File(dir, "noext")));
		assertEquals("mindmaps_desc", filter.getDescription());
	}

	@Test
	void splitKeepsTheRootAndDividesPlainAndHtmlText() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Hello world");
		opened.mode.splitNode(opened.root, 1, "ROOT");
		assertEquals("ROOT", opened.root.getText());
		assertEquals(1, opened.root.getChildCount());

		MindMapNode plain = opened.child("Hello world");
		opened.mode.splitNode(plain, 0, null);
		opened.mode.splitNode(plain, 100, null);
		assertEquals("Hello world", plain.getText());
		assertEquals(1, opened.root.getChildCount());

		opened.mode.splitNode(plain, 5, null);
		assertEquals("Hello", plain.getText());
		assertEquals(2, opened.root.getChildCount());
		assertEquals(" world", ((MindMapNode) opened.root.getChildAt(1)).getText());

		MindMapNode html = new MindMapNodeModel("<html>Hello world</html>",
				opened.mode.getMindMapMapModel());
		opened.root.insert(html, opened.root.getChildCount());
		opened.mode.splitNode(html, 5, null);
		assertEquals(4, opened.root.getChildCount());
		assertNotEquals("<html>Hello world</html>", html.getText());
	}

	@Test
	void extendSelectionHonoursControlShiftAndAlt() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("A", "B");
		MindMapNode a = opened.child("A");
		MindMapNode b = opened.child("B");
		a.insert(new MindMapNodeModel("Grand", opened.mode.getMindMapMapModel()), 0);
		opened.mode.select(a, List.of(a));

		MainView aView = opened.view.getNodeView(a).getMainView();
		assertFalse(opened.mode.extendSelection(click(aView, 0)));
		assertEquals("A", opened.mode.getSelected().getText());

		MainView bView = opened.view.getNodeView(b).getMainView();
		assertTrue(opened.mode.extendSelection(click(bView, MouseEvent.CTRL_DOWN_MASK)));
		assertTrue(opened.mode.getSelecteds().size() >= 2);

		opened.mode.select(a, List.of(a));
		assertTrue(opened.mode.extendSelection(click(bView, MouseEvent.SHIFT_DOWN_MASK)));
		assertTrue(opened.mode.getSelecteds().size() >= 2);

		opened.mode.select(opened.root, List.of(opened.root));
		assertTrue(opened.mode.extendSelection(click(aView, MouseEvent.ALT_DOWN_MASK)));
		assertTrue(opened.view.isSelected(opened.view.getNodeView(a)));
	}

	@Test
	void plainClickFoldsFollowsALinkOrReturns() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Parent", "Target");
		MindMapNode parent = opened.child("Parent");
		parent.insert(new MindMapNodeModel("Grand", opened.mode.getMindMapMapModel()), 0);
		opened.mode.select(parent, List.of(parent, opened.child("Target")));
		boolean folded = parent.isFolded();
		opened.mode.plainClick(click(opened.view.getNodeView(parent).getMainView(), 0));
		assertEquals(folded, parent.isFolded());

		opened.mode.select(parent, List.of(parent));
		opened.mode.plainClick(click(opened.view.getNodeView(parent).getMainView(), 0));
		assertNotEquals(folded, parent.isFolded());

		opened.mode.setSaved(true);
		String id = opened.mode.getNodeID(opened.child("Target"));
		opened.root.setLink("#" + id);
		opened.mode.select(opened.root, List.of(opened.root));
		opened.mode.plainClick(click(opened.view.getNodeView(opened.root).getMainView(), 0));
		assertEquals("Target", opened.mode.getSelected().getText());
	}

	@Test
	@Timeout(10)
	void doubleClickEditsAPlainLeafAndAddNewInsertsAChild() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Leaf");
		MindMapNode leaf = opened.child("Leaf");
		opened.mode.select(leaf, List.of(leaf, opened.root));
		opened.mode.doubleClick(click(opened.view.getNodeView(leaf).getMainView(), 0));
		assertFalse(opened.mode.isBlocked());

		opened.mode.select(leaf, List.of(leaf));
		opened.mode.doubleClick(click(opened.view.getNodeView(leaf).getMainView(),
				MouseEvent.CTRL_DOWN_MASK));
		assertFalse(opened.mode.isBlocked());

		leaf.setLink("#stay");
		opened.mode.doubleClick(click(opened.view.getNodeView(leaf).getMainView(), 0));
		assertFalse(opened.mode.isBlocked());
		leaf.setLink(null);
		opened.mode.setBlocked(false);

		try {
			opened.mode.doubleClick(click(opened.view.getNodeView(leaf).getMainView(), 0));
		} catch (HeadlessException ex) {
			assertNotNull(ex);
		}
		assertTrue(opened.mode.isBlocked());
		endEdit(opened);

		int before = opened.root.getChildCount();
		try {
			MindMapNode created = opened.mode.addNew(opened.root,
					MindMapController.NEW_CHILD_WITHOUT_FOCUS, null);
			assertNotNull(created);
		} catch (HeadlessException ex) {
			assertNotNull(ex);
		}
		assertTrue(opened.root.getChildCount() > before);
		assertTrue(opened.mode.isBlocked());
		endEdit(opened);
	}

	@Test
	void pasteUnfoldsAFoldedTargetWhenAsked() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Folded");
		MindMapNode folded = opened.child("Folded");
		folded.insert(new MindMapNodeModel("Inside", opened.mode.getMindMapMapModel()), 0);
		folded.setFolded(true);
		Properties props = Resources.getInstance().getProperties();
		String old = props.getProperty("unfold_on_paste");
		try {
			props.setProperty("unfold_on_paste", "true");
			assertTrue(opened.mode.paste(opened.mode.copy(opened.root, false), folded, false, false));
			assertFalse(folded.isFolded());
			folded.setFolded(true);
			assertTrue(opened.mode.paste(opened.mode.copy(opened.child("Folded"), false), folded, true, false));
			assertTrue(folded.isFolded());
		} finally {
			restore(props, "unfold_on_paste", old);
		}
	}

	@Test
	void mouseWheelHandlersRegisterAndLeave() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open();
		ViewFeedback.MouseWheelEventHandler handler = event -> false;
		opened.mode.registerMouseWheelEventHandler(handler);
		assertTrue(opened.mode.getRegisteredMouseWheelEventHandler().contains(handler));
		opened.mode.deRegisterMouseWheelEventHandler(handler);
		assertFalse(opened.mode.getRegisteredMouseWheelEventHandler().contains(handler));
	}

	@Test
	void linksFollowAnIdAndRejectAMissingOne() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Target");
		opened.mode.setSaved(true);
		assertNull(opened.mode.getMap().getFile());
		MenuItemEnabledListener follow = (MenuItemEnabledListener) opened.mode.followLink;
		JMenuItem item = new JMenuItem(opened.mode.followLink);
		opened.mode.select(opened.root, List.of(opened.root));
		assertFalse(follow.isEnabled(item, opened.mode.followLink));

		String id = opened.mode.getNodeID(opened.child("Target"));
		opened.root.setLink("#" + id);
		assertTrue(follow.isEnabled(item, opened.mode.followLink));
		opened.mode.followLink.actionPerformed(new ActionEvent(opened.mode.followLink,
				ActionEvent.ACTION_PERFORMED, "follow"));
		assertEquals("Target", opened.mode.getSelected().getText());

		opened.mode.select(opened.root, List.of(opened.root));
		opened.mode.loadURL("#missing-id");
		assertEquals("ROOT", opened.mode.getSelected().getText());

		File note = new File(Files.createTempDirectory("mm-url").toFile(), "note.txt");
		Files.writeString(note.toPath(), "note", StandardCharsets.UTF_8);
		opened.mode.loadURL(note.getAbsolutePath());
		assertEquals("ROOT", opened.root.getText());
		assertNull(opened.mode.getMap().getFile());
	}

	@Test
	void loadUrlOpensACurrentMindMap() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Kept");
		opened.mode.setSaved(true);
		File map = new File(Files.createTempDirectory("mm-load").toFile(), "loaded.mm");
		writeMap(map, "Loaded", null);
		opened.mode.loadURL(map.getAbsolutePath());
		assertEquals("Loaded", opened.app.getModeController().getMap().getRootNode().getText());
		((MindMapMapModel) opened.app.getModeController().getMap()).destroy();
	}

	@Test
	void htmlExportWritesTheMapAndTheSelectedBranch() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Leaf");
		File dir = Files.createTempDirectory("mm-html").toFile();
		File map = new File(dir, "host.mm");
		writeMap(map, "Host", null);
		remember(opened, map);
		opened.mode.select(opened.child("Leaf"), List.of(opened.child("Leaf")));
		opened.mode.exportToHTML.actionPerformed(new ActionEvent(opened.mode.exportToHTML,
				ActionEvent.ACTION_PERFORMED, "html"));
		File whole = new File(map.getPath() + ".html");
		assertTrue(whole.isFile());
		assertTrue(Files.readString(whole.toPath()).contains("ROOT"));

		List<String> before = htmlNames(dir);
		opened.mode.exportBranchToHTML.actionPerformed(new ActionEvent(opened.mode.exportBranchToHTML,
				ActionEvent.ACTION_PERFORMED, "branch"));
		File branch = newHtml(dir, before);
		assertNotNull(branch);
		assertTrue(Files.readString(branch.toPath()).contains("Leaf"));
		assertTrue(whole.delete());
		assertTrue(branch.delete());
	}

	@Test
	void linkedBranchesPasteTheImportedRootOrItsChildren() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Host");
		File dir = Files.createTempDirectory("mm-import").toFile();
		File map = new File(dir, "host.mm");
		File linked = new File(dir, "linked.mm");
		writeMap(map, "Host", null);
		writeMap(linked, "Imported", "Inner");
		remember(opened, map);
		MindMapNode host = opened.child("Host");
		host.setLink("linked.mm");
		opened.mode.select(host, List.of(host));
		assertNotNull(host.getLink());

		opened.mode.importLinkedBranch.actionPerformed(new ActionEvent(opened.mode.importLinkedBranch,
				ActionEvent.ACTION_PERFORMED, "import"));
		assertTrue(texts(host).contains("Imported"));

		opened.mode.importLinkedBranchWithoutRoot.actionPerformed(new ActionEvent(
				opened.mode.importLinkedBranchWithoutRoot, ActionEvent.ACTION_PERFORMED, "import-inner"));
		assertTrue(texts(host).contains("Inner"));
	}

	@Test
	void loadInternallyReadsAWritableOrReadOnlyMap() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Old");
		File dir = Files.createTempDirectory("mm-internal").toFile();
		File writable = new File(dir, "writable.mm");
		writeMap(writable, "Writable", null);
		opened.mode.loadInternally(Tools.fileToUrl(writable), opened.mode.getMindMapMapModel());
		assertEquals("Writable", opened.mode.getMap().getRootNode().getText());
		assertFalse(opened.mode.getMap().isReadOnly());

		File locked = new File(dir, "locked.mm");
		writeMap(locked, "Locked", null);
		assertTrue(locked.setWritable(false));
		try {
			opened.mode.loadInternally(Tools.fileToUrl(locked), opened.mode.getMindMapMapModel());
			assertEquals("Locked", opened.mode.getMap().getRootNode().getText());
			assertTrue(opened.mode.getMap().isReadOnly());
		} finally {
			assertTrue(locked.setWritable(true));
		}

		File missing = new File(dir, "missing.mm");
		assertThrows(FileNotFoundException.class, () -> opened.mode.loadInternally(
				Tools.fileToUrl(missing), opened.mode.getMindMapMapModel()));

		MindMapNode loaded = opened.mode.loadTree(new Tools.StringReaderCreator(
				"<map version=\"1.1.0\"><node TEXT=\"FromReader\" ID=\"ID_9\"/></map>"));
		assertEquals("FromReader", loaded.getText());
	}

	@Test
	void patternsApplyKnownStylesAndReadAUserFile() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Styled");
		boolean hot = false;
		for (ApplyPatternAction action : opened.mode.patterns) {
			if ("Hot".equals(action.getPattern().getName())) {
				hot = true;
			}
		}
		assertTrue(hot);
		MindMapNode styled = opened.child("Styled");
		opened.mode.applyPattern(styled, "NoSuchPattern");
		assertNull(styled.getColor());
		opened.mode.applyPattern(styled, "Hot");
		assertEquals(new Color(255, 0, 0), styled.getColor());

		opened.mode.loadPatterns(new StringReader(
				"<patterns><pattern name=\"Iconed\"><pattern_icon value=\"button_ok\"/>"
						+ "<pattern_node_color value=\"#00ff00\"/></pattern>"
						+ "<pattern name=\"Plain\"><pattern_icon/></pattern></patterns>"));
		ApplyPatternAction iconed = null;
		ApplyPatternAction plain = null;
		for (ApplyPatternAction action : opened.mode.patterns) {
			if ("Iconed".equals(action.getPattern().getName())) {
				iconed = action;
			}
			if ("Plain".equals(action.getPattern().getName())) {
				plain = action;
			}
		}
		assertNotNull(iconed.getValue(Action.SMALL_ICON));
		assertNull(plain.getValue(Action.SMALL_ICON));
		opened.mode.applyPattern(styled, "Iconed");
		assertEquals(new Color(0, 255, 0), styled.getColor());

		try (Reader classpath = opened.mode.getPatternReader()) {
			assertTrue(readAll(classpath).contains("Hot"));
		}
		File dir = Files.createTempDirectory("mm-patterns").toFile();
		File patterns = new File(dir, "patterns.xml");
		Files.writeString(patterns.toPath(), "<patterns><pattern name=\"FromFile\"/></patterns>",
				StandardCharsets.UTF_8);
		HeadlessFreeMind previous = opened.frame;
		try {
			opened.app.setFrame(new PatternFrame(patterns));
			try (Reader reader = opened.mode.getPatternReader()) {
				assertTrue(readAll(reader).contains("FromFile"));
			}
			opened.app.setFrame(new PatternFrame(dir));
			assertThrows(IOException.class, () -> opened.mode.getPatternReader());
		} finally {
			opened.app.setFrame(previous);
		}
	}

	@Test
	void arrowPopupListsEndsAndAForeignTarget() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("A", "B");
		MindMapNode a = opened.child("A");
		MindMapNode b = opened.child("B");
		opened.mode.addLink(opened.root, a);
		opened.mode.addLink(a, b);
		MindMapLink link = opened.mode.getMindMapMapModel().getLinkRegistry()
				.getAllLinks(opened.root).get(0);
		assertTrue(link instanceof MindMapArrowLinkModel);
		JPopupMenu popup = opened.mode.getPopupForModel(link);
		assertNotNull(popup);
		assertTrue(popup.getComponentCount() >= 10);
		assertNull(opened.mode.getPopupForModel(opened.root));
	}

	@Test
	void menusBuildFromHoldersAndFromXml() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open();
		MenuCategory category = new MenuCategory();
		category.setName("file");
		MenuSubmenu submenu = new MenuSubmenu();
		submenu.setName("format");
		submenu.setNameRef("format");
		MenuAction save = new MenuAction();
		save.setField("save");
		save.setKeyRef("keystroke_save");
		MenuCheckedAction bold = new MenuCheckedAction();
		bold.setField("bold");
		bold.setName("boldItem");
		MenuRadioAction fork = new MenuRadioAction();
		fork.setField("fork");
		fork.setName("fork");
		fork.setSelected(Boolean.TRUE);
		MenuRadioAction bubble = new MenuRadioAction();
		bubble.setField("bubble");
		bubble.setName("bubble");
		bubble.setSelected(Boolean.FALSE);
		MenuAction missing = new MenuAction();
		missing.setField("noSuchAction");
		missing.setName("gone");
		submenu.getListChoiceList().add(fork);
		submenu.getListChoiceList().add(bubble);
		category.getListChoiceList().add(save);
		category.getListChoiceList().add(bold);
		category.getListChoiceList().add(submenu);
		category.getListChoiceList().add(new MenuSeparator());
		category.getListChoiceList().add(missing);
		List<Object> list = new ArrayList<>();
		list.add(category);

		StructuredMenuHolder holder = new StructuredMenuHolder();
		opened.mode.processMenuCategory(holder, list, "menu");
		JPopupMenu popup = new JPopupMenu();
		holder.updateMenus(popup, "menu");
		assertTrue(popup.getComponentCount() > 0);

		MenuStructure menus = opened.mode.updateMenusFromXml(
				opened.frame.getResource("mindmap_menus.xml").openStream());
		assertFalse(menus.getMenuCategory().isEmpty());
		assertThrows(IllegalArgumentException.class, () -> opened.mode.updateMenusFromXml(
				new ByteArrayInputStream("<nope/>".getBytes(StandardCharsets.UTF_8))));
	}

	@Test
	void userIconsBecomeActions() throws Exception {
		// backwards compatibility
		new HeadlessFreeMind();
		File iconDir = new File(Resources.getInstance().getFreemindDirectory(), "icons");
		boolean madeDir = !iconDir.exists();
		assertTrue(iconDir.isDirectory() || iconDir.mkdirs());
		byte[] png = java.util.Base64.getDecoder().decode(
				"iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");
		File icon = new File(iconDir, "ab.png");
		File empty = new File(iconDir, ".png");
		File other = new File(iconDir, "note.txt");
		Files.write(icon.toPath(), png);
		Files.write(empty.toPath(), png);
		Files.writeString(other.toPath(), "no", StandardCharsets.UTF_8);
		try {
			Opened opened = MapFixture.open();
			boolean found = false;
			for (IconAction action : opened.mode.iconActions) {
				if ("ab".equals(action.icon.getName())) {
					found = true;
				}
			}
			assertTrue(found);
		} finally {
			assertTrue(icon.delete());
			assertTrue(empty.delete());
			assertTrue(other.delete());
			if (madeDir) {
				iconDir.delete();
			}
		}
	}

	@Test
	void clipboardAccessNeedsADisplay() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open();
		assertThrows(HeadlessException.class,
				() -> opened.mode.setClipboardContents(new StringSelection("copied")));
		assertThrows(HeadlessException.class, () -> opened.mode.getClipboardContents());
	}

	@Test
	@Timeout(5)
	void nodeInformationInterruptStopsWaiting() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("12");
		Object timer = field(opened.mode, "mNodeInformationTimerAction");
		Method interrupt = timer.getClass().getDeclaredMethod("interrupt");
		interrupt.setAccessible(true);
		assertEquals(Boolean.TRUE, interrupt.invoke(timer));

		Field done = timer.getClass().getDeclaredField("mIsDone");
		done.setAccessible(true);
		done.setBoolean(timer, false);
		Thread test = Thread.currentThread();
		Thread helper = new Thread(() -> {
			try {
				Thread.sleep(30);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			test.interrupt();
			try {
				done.setBoolean(timer, true);
			} catch (IllegalAccessException ex) {
				throw new IllegalStateException(ex);
			}
		});
		helper.start();
		try {
			assertEquals(Boolean.TRUE, interrupt.invoke(timer));
		} finally {
			done.setBoolean(timer, true);
			Thread.interrupted();
			helper.join(1000);
		}
	}

	@Test
	void modeHooksZoomAndAThrowingHookIsReported() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Wide");
		java.awt.Container parent = opened.view.getParent();
		parent.remove(opened.view);
		JViewport port = new JViewport();
		port.setView(opened.view);
		port.setExtentSize(new Dimension(100, 80));
		port.setSize(100, 80);
		opened.view.setSize(800, 600);
		assertTrue(opened.view.getParent() instanceof JViewport);
		float before = opened.view.getZoom();
		opened.mode.createModeControllerHook("accessories/plugins/FitToPage.properties");
		EventQueue.invokeAndWait(() -> {
		});
		assertNotEquals(before, opened.view.getZoom(), 0.0001f);

		StartedHook started = new StartedHook();
		opened.mode.invokeHook(started);
		assertTrue(started.started);
		ThrowingHook failing = new ThrowingHook();
		opened.mode.invokeHook(failing);
		assertTrue(failing.started);
		assertThrows(IllegalArgumentException.class,
				() -> opened.mode.createModeControllerHook("no.such.Hook"));
	}

	private static void installHeadlessControllerParts(Opened opened) throws Exception {
		Controller app = opened.app;
		AbstractAction dummy = new AbstractAction() {
			public void actionPerformed(ActionEvent event) {
			}
		};
		app.zoomIn = dummy;
		app.zoomOut = dummy;
		app.showFilterToolbarAction = dummy;
		setControllerField(app, "nodeMouseMotionListener", new NodeMouseMotionListener(app));
		setControllerField(app, "nodeMotionListener", new NodeMotionListener(app));
		setControllerField(app, "nodeKeyListener", new NodeKeyListener(app));
		setControllerField(app, "nodeDropListener", new NodeDropListener(app));
		setControllerField(app, "mapMouseMotionListener", new MapMouseMotionListener(app));
		setControllerField(app, "mapMouseWheelListener", new MapMouseWheelListener(app));
	}

	private static void setControllerField(Controller app, String name, Object value) throws Exception {
		Field field = Controller.class.getDeclaredField(name);
		field.setAccessible(true);
		field.set(app, value);
	}

	private static void endEdit(Opened opened) {
		try {
			opened.mode.edit.stopEditing();
		} catch (RuntimeException ex) {
			try {
				Field dialog = opened.mode.edit.getClass().getDeclaredField("mCurrentEditDialog");
				dialog.setAccessible(true);
				dialog.set(opened.mode.edit, null);
			} catch (ReflectiveOperationException reflection) {
				throw new IllegalStateException(reflection);
			}
			opened.mode.setBlocked(false);
		}
	}

	private static MouseEvent click(MainView view, int modifiers) {
		return new MouseEvent(view, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(),
				modifiers, 4, 4, 1, false, MouseEvent.BUTTON1);
	}

	private static void remember(Opened opened, File file) {
		((MapAdapter) opened.mode.getMap()).setFile(file);
		opened.mode.setSaved(true);
	}

	private static void writeMap(File file, String root, String child) throws IOException {
		String inner = child == null ? "" : "<node TEXT=\"" + child + "\" ID=\"ID_2\"/>";
		Files.writeString(file.toPath(),
				"<map version=\"1.1.0\"><node TEXT=\"" + root + "\" ID=\"ID_1\">" + inner
						+ "</node></map>",
				StandardCharsets.UTF_8);
	}

	private static List<String> texts(MindMapNode node) {
		List<String> names = new ArrayList<>();
		for (int i = 0; i < node.getChildCount(); i++) {
			names.add(((MindMapNode) node.getChildAt(i)).getText());
		}
		return names;
	}

	private static List<String> htmlNames(File dir) {
		List<String> names = new ArrayList<>();
		File[] files = dir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.getName().endsWith(".html")) {
					names.add(file.getName());
				}
			}
		}
		return names;
	}

	private static File newHtml(File dir, List<String> before) {
		File[] files = dir.listFiles();
		if (files == null) {
			return null;
		}
		for (File file : files) {
			if (file.getName().endsWith(".html") && !before.contains(file.getName())) {
				return file;
			}
		}
		return null;
	}

	private static String readAll(Reader reader) throws IOException {
		StringBuilder text = new StringBuilder();
		char[] buffer = new char[2048];
		int count;
		while ((count = reader.read(buffer)) >= 0) {
			text.append(buffer, 0, count);
		}
		return text.toString();
	}

	private static Object zoomItem(MindMapToolBar bar) throws Exception {
		Field zoom = MindMapToolBar.class.getDeclaredField("zoom");
		zoom.setAccessible(true);
		Object combo = zoom.get(bar);
		return combo.getClass().getMethod("getSelectedItem").invoke(combo);
	}

	private static Object field(Object target, String name) throws Exception {
		Field found = target.getClass().getDeclaredField(name);
		found.setAccessible(true);
		return found.get(target);
	}

	private static void restore(Properties props, String key, String old) {
		if (old == null) {
			props.remove(key);
		} else {
			props.setProperty(key, old);
		}
	}

	private static final class PatternFrame extends HeadlessFreeMind {
		private final File patterns;

		PatternFrame(File patterns) {
			this.patterns = patterns;
		}

		@Override
		public File getPatternsFile() {
			return patterns;
		}
	}

	private static final class StartedHook extends ModeControllerHookAdapter {
		private boolean started;

		@Override
		public void startupMapHook() {
			started = true;
		}
	}

	private static final class ThrowingHook extends ModeControllerHookAdapter {
		private boolean started;

		@Override
		public void startupMapHook() {
			started = true;
			throw new IllegalStateException("hook failed");
		}
	}
}
