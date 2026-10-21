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

package tests.freemind.cov.browsemode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.junit.jupiter.api.Test;

import freemind.controller.Controller;
import freemind.controller.MapMouseMotionListener;
import freemind.controller.MapMouseWheelListener;
import freemind.controller.NodeKeyListener;
import freemind.controller.NodeMouseMotionListener;
import freemind.controller.StructuredMenuHolder;
import freemind.main.HeadlessFreeMind;
import freemind.main.XMLElement;
import freemind.modes.MapAdapter;
import freemind.modes.MindMapNode;
import freemind.modes.ModeController;
import freemind.modes.browsemode.BrowseArrowLinkModel;
import freemind.modes.browsemode.BrowseController;
import freemind.modes.browsemode.BrowseHookFactory;
import freemind.modes.browsemode.BrowseMapModel;
import freemind.modes.browsemode.BrowseMode;
import freemind.modes.browsemode.BrowseNodeModel;
import freemind.modes.browsemode.BrowseReminderHook;
import freemind.modes.common.plugins.MapNodePositionHolderBase;
import freemind.modes.common.plugins.NodeNoteBase;
import freemind.modes.common.plugins.ReminderHookBase;
import freemind.view.mindmapview.MainView;
import freemind.view.mindmapview.MapView;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Browse controller opens links, folds a parent, and rejects a map it cannot read. */
class BrowseControllerBehaviourTest {

	@Test
	void aNewControllerBuildsMenusNodesAndAnEmptyStatusLine() throws Exception {
		// backwards compatibility
		Session session = open();
		assertEquals(8, session.controller.getPopupMenu().getComponentCount());
		assertNotNull(session.controller.getModeToolBar());
		assertFalse(action(session.controller.getPopupMenu(), "follow_link").isEnabled());
		assertTrue(session.controller.getHookFactory() instanceof BrowseHookFactory);
		assertTrue(session.controller.getHookFactory().createNodeHook(ReminderHookBase.PLUGIN_LABEL)
				instanceof BrowseReminderHook);

		MindMapNode fresh = session.controller.newNode("Fresh", session.opened.mode.getMap());
		assertTrue(fresh instanceof BrowseNodeModel);
		assertEquals("Fresh", fresh.getText());
		assertFalse(fresh.isWriteable());

		assertTrue(session.frame.messages.isEmpty());
		session.controller.out("hello");
		assertTrue(session.frame.messages.isEmpty());

		StructuredMenuHolder holder = new StructuredMenuHolder();
		session.controller.updateMenus(holder);
		JPopupMenu menus = new JPopupMenu();
		holder.updateMenus(menus, "menu_bar/edit/find");
		assertNotNull(action(menus, "follow_link"));
	}

	@Test
	void startupEnablesFollowLinkAndShutdownDisablesIt() throws Exception {
		// backwards compatibility
		Session session = open();
		installListeners(session.opened.app);
		MapAdapter model = session.controller.newModel(session.controller);
		try {
			assertEquals("new_mindmap", model.getRootNode().getText());
			assertTrue(model.isSaved());
			assertNull(model.getFile());
			assertFalse(action(session.controller.getPopupMenu(), "follow_link").isEnabled());
			session.controller.startupController();
			assertTrue(action(session.controller.getPopupMenu(), "follow_link").isEnabled());
			assertTrue(session.controller.find.isEnabled());
			session.controller.shutdownController();
			assertFalse(action(session.controller.getPopupMenu(), "follow_link").isEnabled());
		} finally {
			model.destroy();
		}
	}

	@Test
	void aLinkPopupNamesTheOtherEnd() throws Exception {
		// backwards compatibility
		Session session = open();
		assertNull(session.controller.getPopupForModel("not a link"));
		BrowseMapModel model = (BrowseMapModel) session.controller.newModel(session.controller);
		try {
			BrowseNodeModel source = (BrowseNodeModel) model.getRootNode();
			source.setText("Source");
			BrowseNodeModel target = new BrowseNodeModel("Target", model);
			BrowseNodeModel extra = new BrowseNodeModel("Extra", model);
			BrowseArrowLinkModel link = new BrowseArrowLinkModel(source, target, session.controller);
			link.setDestinationLabel("to-target");
			model.getLinkRegistry().registerLink(link);
			BrowseArrowLinkModel extraLink = new BrowseArrowLinkModel(target, extra, session.controller);
			extraLink.setDestinationLabel("to-extra");
			model.getLinkRegistry().registerLink(extraLink);

			JPopupMenu popup = session.controller.getPopupForModel(link);
			assertNotNull(popup);
			assertTrue(names(popup).contains("Extra"));
		} finally {
			model.destroy();
		}
	}

	@Test
	void clicksFoldAParentAndOpenALink() throws Exception {
		// backwards compatibility
		Session session = open();
		BrowseMapModel model = (BrowseMapModel) session.controller.newModel(session.controller);
		BrowseNodeModel root = (BrowseNodeModel) model.getRootNode();
		BrowseNodeModel mid = new BrowseNodeModel("Mid", model);
		mid.setNoteText("Noted");
		mid.insert(new BrowseNodeModel("Kid", model), 0);
		root.insert(mid, 0);
		BrowseNodeModel leaf = new BrowseNodeModel("Leaf", model);
		leaf.setLink("http://example.test/page");
		root.insert(leaf, 1);
		BrowseNodeModel plain = new BrowseNodeModel("Plain", model);
		root.insert(plain, 2);
		try {
			session.controller.newMap(model, session.controller);
			assertNotNull(mid.getStateIcons().get(NodeNoteBase.NODE_NOTE_ICON));
			assertFalse(plain.getStateIcons().containsKey(NodeNoteBase.NODE_NOTE_ICON));

			MapView view = session.controller.getView();
			view.selectAsTheOnlyOneSelected(view.getNodeView(mid));
			view.toggleSelected(view.getNodeView(leaf));
			assertEquals(2, session.controller.getSelecteds().size());
			session.controller.plainClick(click(view.getNodeView(mid).getMainView()));
			assertFalse(mid.isFolded());

			view.selectAsTheOnlyOneSelected(view.getNodeView(mid));
			session.controller.plainClick(click(view.getNodeView(mid).getMainView()));
			assertTrue(mid.isFolded());
			session.controller.doubleClick();
			assertFalse(mid.isFolded());

			view.selectAsTheOnlyOneSelected(view.getNodeView(leaf));
			session.controller.plainClick(click(view.getNodeView(leaf).getMainView()));
			assertEquals(List.of("http://example.test/page"), session.frame.documents);
			assertFalse(leaf.isFolded());
			session.controller.doubleClick();
			action(session.controller.getPopupMenu(), "follow_link")
					.actionPerformed(new ActionEvent(leaf, ActionEvent.ACTION_PERFORMED, "follow"));
			assertEquals(3, session.frame.documents.size());
			assertEquals("http://example.test/page", session.frame.documents.get(2));

			view.selectAsTheOnlyOneSelected(view.getNodeView(plain));
			session.controller.plainClick(click(view.getNodeView(plain).getMainView()));
			assertFalse(plain.isFolded());
			assertEquals(3, session.frame.documents.size());
		} finally {
			stopViewTimer(session.controller.getView());
			model.destroy();
		}
	}

	@Test
	void followMapLinkOpensTheStoredPosition() throws Exception {
		// backwards compatibility
		Session session = open();
		MindMapNode child = session.opened.child("Child");
		session.opened.view.selectAsTheOnlyOneSelected(session.opened.view.getNodeView(child));
		session.controller.setView(session.opened.view);
		assertFalse(session.controller.followMapLink.isEnabled(null, null));
		session.controller.followMapLink.actionPerformed(
				new ActionEvent(session.controller.followMapLink, ActionEvent.ACTION_PERFORMED, "map"));
		assertTrue(session.frame.documents.isEmpty());

		MapNodePositionHolderBase hook = (MapNodePositionHolderBase) session.controller.getHookFactory()
				.createNodeHook(MapNodePositionHolderBase.NODE_MAP_HOOK_NAME);
		hook.setController(session.opened.mode);
		hook.setMap(child.getMap());
		hook.loadFrom(position());
		child.invokeHook(hook);
		try {
			assertTrue(session.controller.followMapLink.isEnabled(null, null));
			session.controller.followMapLink.actionPerformed(
					new ActionEvent(session.controller.followMapLink, ActionEvent.ACTION_PERFORMED, "map"));
			assertEquals(1, session.frame.documents.size());
			assertTrue(session.frame.documents.get(0).contains("mlat=1.5"));
			assertTrue(session.frame.documents.get(0).contains("layers=M"));
		} finally {
			hook.shutdownMapHook();
		}
	}

	@Test
	void loadRejectsABadMapAndAnUnsupportedProtocol() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open();
		toolbarActions(opened.app);
		RecordingFrame frame = new RecordingFrame(opened.app);
		opened.app.setFrame(frame);
		TrackingMode mode = new TrackingMode();
		mode.start(opened.app);
		File dir = Files.createTempDirectory("browse-load").toFile();
		File empty = new File(dir, "empty.mm");
		Files.writeString(empty.toPath(), "<map></map>", StandardCharsets.UTF_8);
		File bad = new File(dir, "bad.mm");
		Files.writeString(bad.toPath(), "<map", StandardCharsets.UTF_8);
		File missing = new File(dir, "missing.mm");
		Window[] before = Window.getWindows();
		try {
			assertThrows(java.io.IOException.class, () -> mode.controller.load(empty));
			assertThrows(java.io.IOException.class, () -> mode.controller.load(bad));
			assertThrows(HeadlessException.class, () -> mode.controller.load(new URL("ftp://example.test/a.mm")));
			assertThrows(HeadlessException.class, () -> mode.controller.load(missing));
			assertEquals(before.length, Window.getWindows().length);
			assertEquals("ROOT", opened.app.getMapModule().getModel().getRootNode().getText());
			assertEquals(4, mode.controller.created.size());
		} finally {
			disposeNewWindows(before);
			for (MapAdapter model : mode.controller.created) {
				model.destroy();
			}
		}
	}

	private static Session open() throws Exception {
		Opened opened = MapFixture.open("Child");
		toolbarActions(opened.app);
		RecordingFrame frame = new RecordingFrame(opened.app);
		opened.app.setFrame(frame);
		BrowseMode mode = new BrowseMode();
		mode.init(opened.app);
		return new Session(opened, frame, (BrowseController) mode.getDefaultModeController());
	}

	private static void toolbarActions(Controller app) {
		app.zoomIn = unusedAction();
		app.zoomOut = unusedAction();
		app.showFilterToolbarAction = unusedAction();
	}

	private static Action unusedAction() {
		return new AbstractAction() {
			public void actionPerformed(ActionEvent event) {
			}
		};
	}

	private static void installListeners(Controller app) throws Exception {
		setIfNull(app, "nodeMouseMotionListener", new NodeMouseMotionListener(app));
		setIfNull(app, "nodeKeyListener", new NodeKeyListener(app));
		setIfNull(app, "mapMouseMotionListener", new MapMouseMotionListener(app));
		setIfNull(app, "mapMouseWheelListener", new MapMouseWheelListener(app));
	}

	private static void setIfNull(Controller app, String name, Object value) throws Exception {
		Field field = Controller.class.getDeclaredField(name);
		field.setAccessible(true);
		if (field.get(app) == null) {
			field.set(app, value);
		}
	}

	private static Action action(JPopupMenu popup, String name) {
		for (int i = 0; i < popup.getComponentCount(); i++) {
			if (popup.getComponent(i) instanceof JMenuItem item && item.getAction() != null
					&& name.equals(item.getAction().getValue(Action.NAME))) {
				return item.getAction();
			}
		}
		return null;
	}

	private static String names(JPopupMenu popup) {
		StringBuilder names = new StringBuilder();
		for (int i = 0; i < popup.getComponentCount(); i++) {
			if (popup.getComponent(i) instanceof JMenuItem item && item.getAction() != null) {
				names.append(item.getAction().getValue(Action.NAME));
			}
		}
		return names.toString();
	}

	private static MouseEvent click(MainView view) {
		return new MouseEvent(view, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 4, 4, 1, false,
				MouseEvent.BUTTON1);
	}

	private static XMLElement position() {
		XMLElement hook = new XMLElement();
		hook.setName("hook");
		XMLElement parameters = new XMLElement();
		parameters.setName("Parameters");
		parameters.setAttribute("XML_STORAGE_POS_LAT", "1.5");
		parameters.setAttribute("XML_STORAGE_POS_LON", "2.5");
		parameters.setAttribute("XML_STORAGE_MAP_LAT", "3.5");
		parameters.setAttribute("XML_STORAGE_MAP_LON", "4.5");
		parameters.setAttribute("XML_STORAGE_ZOOM", "12");
		parameters.setAttribute("XML_STORAGE_TILE_SOURCE", MapNodePositionHolderBase.TILE_SOURCE_MAPNIK);
		hook.addChild(parameters);
		return hook;
	}

	private static void stopViewTimer(MapView view) throws Exception {
		if (view == null) {
			return;
		}
		Field field = MapView.class.getDeclaredField("mCenterNodeTimer");
		field.setAccessible(true);
		((java.util.Timer) field.get(view)).cancel();
	}

	private static void disposeNewWindows(Window[] before) {
		for (Window window : Window.getWindows()) {
			boolean kept = false;
			for (Window old : before) {
				if (old == window) {
					kept = true;
					break;
				}
			}
			if (!kept) {
				window.dispose();
			}
		}
	}

	private static final class Session {
		final Opened opened;
		final RecordingFrame frame;
		final BrowseController controller;

		Session(Opened opened, RecordingFrame frame, BrowseController controller) {
			this.opened = opened;
			this.frame = frame;
			this.controller = controller;
		}
	}

	private static final class RecordingFrame extends HeadlessFreeMind {
		final List<String> documents = new ArrayList<String>();
		final List<String> messages = new ArrayList<String>();
		private final Controller app;

		RecordingFrame(Controller app) {
			this.app = app;
		}

		@Override
		public Controller getController() {
			return app;
		}

		@Override
		public void openDocument(URL location) {
			documents.add(location.toString());
		}

		@Override
		public void out(String msg) {
			messages.add(msg);
		}
	}

	private static final class TrackingController extends BrowseController {
		final List<MapAdapter> created = new ArrayList<MapAdapter>();

		TrackingController(freemind.modes.Mode mode) {
			super(mode);
		}

		@Override
		public MapAdapter newModel(ModeController newModeController) {
			MapAdapter model = super.newModel(newModeController);
			created.add(model);
			return model;
		}
	}

	private static final class TrackingMode extends BrowseMode {
		TrackingController controller;

		void start(Controller app) {
			init(app);
			controller = new TrackingController(this);
		}

		@Override
		public ModeController getDefaultModeController() {
			return controller;
		}
	}
}
