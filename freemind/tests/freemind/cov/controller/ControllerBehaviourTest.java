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

package tests.freemind.cov.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import freemind.common.JOptionalSplitPane;
import freemind.controller.Controller;
import freemind.controller.MainToolBar;
import freemind.controller.MenuBar;
import freemind.controller.MenuItemSelectedListener;
import freemind.controller.NodeMouseMotionListener;
import freemind.controller.ZoomListener;
import freemind.controller.filter.FilterController;
import freemind.main.FreeMind;
import freemind.main.FreeMindCommon;
import freemind.main.FreeMindMain;
import freemind.main.HeadlessFreeMind;
import freemind.main.Resources;
import freemind.main.Tools;
import freemind.modes.mindmapmode.MindMapMapModel;
import freemind.modes.mindmapmode.MindMapNodeModel;
import freemind.preferences.FreemindPropertyListener;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** The controller keeps zoom, titles, tabs, and page format. Quit is not called. */
@DisplayName("ControllerBehaviourTest")
class ControllerBehaviourTest {

	@Test
	void zoomStepsAndAListenerFollowTheView() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		RecordingFrame frame = install(opened);
		float[] heard = new float[] { -1f };
		ZoomListener listener = value -> heard[0] = value;
		opened.app.registerZoomListener(listener);
		opened.app.setZoom(1.5f);
		assertEquals(1.5f, opened.view.getZoom(), 0.001f);
		assertEquals(1.5f, heard[0], 0.001f);
		assertEquals(formattedZoom(1.5f), frame.outs.get(frame.outs.size() - 1));
		opened.app.deregisterZoomListener(listener);
		opened.app.setZoom(2f);
		assertEquals(1.5f, heard[0], 0.001f);
		assertEquals(2f, opened.view.getZoom(), 0.001f);

		Action zoomIn = (Action) newInner(opened.app, "ZoomInAction");
		opened.app.setZoom(1f);
		zoomIn.actionPerformed(event("in"));
		assertEquals(1.5f, opened.view.getZoom(), 0.001f);
		opened.app.setZoom(4f);
		zoomIn.actionPerformed(event("in"));
		assertEquals(4f, opened.view.getZoom(), 0.001f);

		Action zoomOut = (Action) newInner(opened.app, "ZoomOutAction");
		opened.app.setZoom(1f);
		zoomOut.actionPerformed(event("out"));
		assertEquals(0.75f, opened.view.getZoom(), 0.001f);
		opened.app.setZoom(0.25f);
		zoomOut.actionPerformed(event("out"));
		assertEquals(0.25f, opened.view.getZoom(), 0.001f);

		String[] zooms = opened.app.getZooms();
		assertEquals("25%", zooms[0]);
		assertEquals("100%", zooms[3]);
		assertEquals("400%", zooms[zooms.length - 1]);
	}

	@Test
	void theTitleKeepsTheFileTheStarAndAContributor() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		RecordingFrame frame = install(opened);
		File file = new File("/tmp/cov-title.mm");
		((MindMapMapModel) opened.mode.getMap()).setFile(file);
		opened.mode.getMap().setSaved(false);
		String raw = opened.app.getMapModule().toString();
		int[] calls = new int[] { 0 };
		opened.app.registerMapTitleContributor((title, module, model) -> title + " extra");
		opened.app.registerMapTitleChangeListener((title, module, model) -> {
			calls[0]++;
			assertEquals(raw, title);
			assertSame(opened.app.getMapModule(), module);
		});
		opened.app.setTitle();
		assertEquals(1, calls[0]);
		assertTrue(frame.title.contains("*"));
		assertTrue(frame.title.contains(file.getAbsolutePath()));
		assertTrue(frame.title.endsWith(" extra"));
	}

	@Test
	void aRegisteredTitleListenerStopsHearingAfterRemoval() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		RecordingFrame frame = install(opened);
		int[] calls = new int[] { 0 };
		freemind.controller.MapModuleManager.MapTitleChangeListener listener =
				(title, module, model) -> calls[0]++;
		freemind.controller.MapModuleManager.MapTitleContributor contributor =
				(title, module, model) -> title + " extra";
		opened.app.registerMapTitleChangeListener(listener);
		opened.app.registerMapTitleContributor(contributor);
		opened.app.setTitle();
		assertEquals(1, calls[0]);
		assertTrue(frame.title.endsWith(" extra"));
		opened.app.deregisterMapTitleChangeListener(listener);
		opened.app.deregisterMapTitleContributor(contributor);
		opened.mode.getMap().setSaved(true);
		opened.app.setTitle();
		assertEquals(1, calls[0]);
		assertFalse(frame.title.endsWith(" extra"));
		assertFalse(frame.title.contains("*"));
	}

	@Test
	void fontsAndPropertiesKeepTheValuesTheyAreGiven() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
		assertSame(opened.app.getFontThroughMap(font), opened.app.getFontThroughMap(font));
		assertEquals(opened.app.getProperty("defaultfont"), opened.app.getDefaultFontFamilyName());
		assertEquals(opened.app.getIntProperty("defaultfontsize", 12), opened.app.getDefaultFontSize());
		assertEquals(opened.app.getDefaultFontFamilyName(), opened.app.getDefaultFont().getFamily());
		assertNull(opened.app.getJFrame());
		assertSame(Controller.getCommonJColorChooser(), Controller.getCommonJColorChooser());

		List<String> seen = new ArrayList<>();
		FreemindPropertyListener listener = (property, value, oldValue) -> {
			if ("cov-key".equals(property)) {
				seen.add(value);
			}
		};
		Controller.addPropertyChangeListener(listener);
		try {
			assertTrue(Controller.getPropertyChangeListeners().contains(listener));
			opened.app.setProperty("cov-key", "one");
			opened.app.setProperty("cov-key", "one");
			opened.app.setProperty("cov-key", "two");
			assertEquals(List.of("one", "two"), seen);
		} finally {
			Controller.removePropertyChangeListener(listener);
		}

		int[] propagated = new int[] { 0 };
		FreemindPropertyListener all = (property, value, oldValue) -> propagated[0]++;
		Controller.addPropertyChangeListenerAndPropagate(all);
		try {
			assertTrue(propagated[0] > 0);
		} finally {
			Controller.removePropertyChangeListener(all);
		}

		URL url = Controller.localDocumentationLinkConverter.convertLocalLink("./doc/map.mm");
		assertTrue(url.getPath().endsWith("doc/map.mm"));
		assertTrue(opened.app.createNewMode("MindMap"));
	}

	@Test
	void aStoredPageFormatKeepsOrientationAndPaper() throws Exception {
		// backwards compatibility
		FormattingController app = new FormattingController(new HeadlessFreeMind());
		remember(app, PageFormat.LANDSCAPE, "landscape");
		remember(app, PageFormat.PORTRAIT, "portrait");
		remember(app, PageFormat.REVERSE_LANDSCAPE, "reverse_landscape");
	}

	@Test
	@Tag("gui")
	void aPrinterJobUsesTheLandscapeOrientation() throws Exception {
		// backwards compatibility
		FormattingController app = new FormattingController(new HeadlessFreeMind());
		app.setProperty("page_orientation", "landscape");
		app.setProperty("page_format", "");
		assertTrue(app.acquire());
		assertEquals(PageFormat.LANDSCAPE, app.getPageFormat().getOrientation());
	}

	@Test
	void tabsMoveAndTheSplitPaneLeavesWhenBothPanelsAreRemoved() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		RecordingFrame frame = install(opened);
		JTabbedPane pane = new JTabbedPane();
		opened.app.addTabbedPane(pane);
		String first = opened.app.getMapModule().toString();
		openAnother(opened, "Second");
		String second = opened.app.getMapModule().toString();
		assertEquals(second, pane.getTitleAt(0));
		assertTrue(opened.app.getMapModuleManager().changeToMapModule(first));
		assertEquals(2, pane.getTabCount());
		String left = pane.getTitleAt(0);
		String right = pane.getTitleAt(1);
		opened.app.moveTab(0, 1);
		assertEquals(right, pane.getTitleAt(0));
		assertEquals(left, pane.getTitleAt(1));

		Action moveRight = (Action) newInner(opened.app, "NavigationMoveMapRightAction");
		moveRight.actionPerformed(event("right"));
		assertEquals(left, pane.getTitleAt(0));
		assertEquals(right, pane.getTitleAt(1));

		JPanel note = new JPanel();
		JPanel attributes = new JPanel();
		opened.app.insertComponentIntoSplitPane(note, Controller.SplitComponentType.NOTE_PANEL);
		assertEquals(1, frame.insertCount);
		assertTrue(frame.inserted instanceof JOptionalSplitPane);
		opened.app.insertComponentIntoSplitPane(attributes, Controller.SplitComponentType.ATTRIBUTE_PANEL);
		assertEquals(1, frame.insertCount);
		assertEquals(0, Controller.SplitComponentType.NOTE_PANEL.getIndex());
		opened.app.removeSplitPane(Controller.SplitComponentType.NOTE_PANEL);
		assertFalse(frame.removedSplit);
		opened.app.removeSplitPane(Controller.SplitComponentType.ATTRIBUTE_PANEL);
		assertTrue(frame.removedSplit);
	}

	@Test
	void navigationActionsWrapTheOpenMapsAndReportHowManyAreOpen() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		String first = opened.app.getMapModule().toString();
		openAnother(opened, "Second");
		String second = opened.app.getMapModule().toString();
		Action next = (Action) newInner(opened.app, "NavigationNextMapAction");
		Action previous = (Action) newInner(opened.app, "NavigationPreviousMapAction");
		next.actionPerformed(event("next"));
		assertEquals(first, opened.app.getMapModule().toString());
		previous.actionPerformed(event("previous"));
		assertEquals(second, opened.app.getMapModule().toString());

		FormattingController app = new FormattingController(new HeadlessFreeMind());
		Action reportNext = (Action) newInner(app, "NavigationNextMapAction");
		Action reportPrevious = (Action) newInner(app, "NavigationPreviousMapAction");
		Action moveLeft = (Action) newInner(app, "NavigationMoveMapLeftAction");
		Action moveRight = (Action) newInner(app, "NavigationMoveMapRightAction");
		setAction(app, "navigationNextMap", reportNext);
		setAction(app, "navigationPreviousMap", reportPrevious);
		setAction(app, "navigationMoveMapLeftAction", moveLeft);
		setAction(app, "navigationMoveMapRightAction", moveRight);
		app.numberOfOpenMapInformation(2, 0);
		assertTrue(reportNext.isEnabled());
		assertTrue(reportPrevious.isEnabled());
		assertFalse(moveLeft.isEnabled());
		assertTrue(moveRight.isEnabled());
		app.numberOfOpenMapInformation(2, 1);
		assertTrue(moveLeft.isEnabled());
		assertFalse(moveRight.isEnabled());
		app.numberOfOpenMapInformation(0, -1);
		assertFalse(reportNext.isEnabled());
		assertFalse(reportPrevious.isEnabled());
	}

	@Test
	void toolbarMenubarAndSelectionPreferencesFollowTheirActions() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		RecordingFrame frame = install(opened);
		MainToolBar toolbar = new MainToolBar(opened.app);
		setField(opened.app, "toolbar", toolbar);
		frame.menuBar = new MenuBar(opened.app);
		Action toggleToolbar = (Action) newInner(opened.app, "ToggleToolbarAction");
		toggleToolbar.actionPerformed(event("toolbar"));
		assertFalse(toolbar.isVisible());
		assertFalse(((MenuItemSelectedListener) toggleToolbar).isSelected(null, toggleToolbar));
		toggleToolbar.actionPerformed(event("toolbar"));
		assertTrue(toolbar.isVisible());

		Action toggleMenu = (Action) newInner(opened.app, "ToggleMenubarAction");
		toggleMenu.actionPerformed(event("menu"));
		assertFalse(frame.menuBar.isVisible());
		assertFalse(((MenuItemSelectedListener) toggleMenu).isSelected(null, toggleMenu));

		java.awt.Component left = opened.mode.getLeftToolBar();
		if (left != null && left.getParent() == null) {
			new JPanel().add(left);
		}
		Action toggleLeft = (Action) newInner(opened.app, "ToggleLeftToolbarAction");
		toggleLeft.actionPerformed(event("left"));
		assertFalse(((MenuItemSelectedListener) toggleLeft).isSelected(null, toggleLeft));
		if (left != null) {
			assertFalse(left.isVisible());
		}

		Controller.OptionAntialiasAction antialias = opened.app.new OptionAntialiasAction();
		antialias.changeAntialias(null);
		antialias.changeAntialias("antialias_all");
		assertEquals("antialias_all", opened.app.getProperty(FreeMindCommon.RESOURCE_ANTIALIAS));

		Action folding = (Action) newInner(opened.app, "OptionHTMLExportFoldingAction");
		folding.actionPerformed(event("fold_all"));
		assertEquals("fold_all", opened.app.getProperty("html_export_folding"));

		setField(opened.app, "nodeMouseMotionListener", new NodeMouseMotionListener(opened.app));
		Action selection = (Action) newInner(opened.app, "OptionSelectionMechanismAction");
		try {
			selection.actionPerformed(event("selection_method_direct"));
			assertEquals("selection_method_direct", opened.app.getProperty("selection_method"));
			assertEquals("selection_method_direct", frame.outs.get(frame.outs.size() - 1));
			opened.app.setProperty(FreeMind.RESOURCES_SELECTION_METHOD, "by_click");
			assertEquals("by_click", opened.app.getProperty("selection_method"));
		} finally {
			Controller.removePropertyChangeListener((FreemindPropertyListener) selection);
		}

		String rectangle = opened.app.getProperty(FreeMind.RESOURCE_DRAW_RECTANGLE_FOR_SELECTION);
		try {
			opened.app.setProperty(FreeMind.RESOURCE_DRAW_RECTANGLE_FOR_SELECTION, "false");
			opened.app.toggleSelectionAsRectangle();
			assertEquals("true", opened.app.getProperty(FreeMind.RESOURCE_DRAW_RECTANGLE_FOR_SELECTION));
			Action rectangleAction = (Action) newInner(opened.app, "ShowSelectionAsRectangleAction");
			assertTrue(((MenuItemSelectedListener) rectangleAction).isSelected(null, rectangleAction));
			rectangleAction.actionPerformed(event("rectangle"));
			assertEquals("false", opened.app.getProperty(FreeMind.RESOURCE_DRAW_RECTANGLE_FOR_SELECTION));
		} finally {
			if (rectangle == null) {
				opened.app.getFrame().getProperties().remove(FreeMind.RESOURCE_DRAW_RECTANGLE_FOR_SELECTION);
			} else {
				opened.app.setProperty(FreeMind.RESOURCE_DRAW_RECTANGLE_FOR_SELECTION, rectangle);
			}
		}
	}

	@Test
	void movingToTheRootSelectsItAndAForcedCloseDropsTheMap() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		opened.view.selectAsTheOnlyOneSelected(opened.view.getNodeView(opened.child("Child")));
		Action move = (Action) newInner(opened.app, "MoveToRootAction");
		move.actionPerformed(event("root"));
		assertSame(opened.root, opened.view.getSelected().getModel());

		opened.app.close(true);
		assertNull(opened.app.getMapModule());
	}

	@Test
	void openingAUrlAndTheKeyDocumentReachesTheFrame() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		RecordingFrame frame = install(opened);
		Action keys = (Action) newInner(opened.app, "KeyDocumentationAction");
		keys.actionPerformed(event("keys"));
		assertTrue(frame.opened.get(0).getPath().endsWith("pdfKeyDocLocation"));

		Action link = (Action) newInner(opened.app, "OpenURLAction",
				new Class<?>[] { Controller.class, String.class, String.class },
				new Object[] { opened.app, "FreeMind", "http://example.invalid/freemind" });
		link.actionPerformed(event("link"));
		assertEquals("http://example.invalid/freemind", frame.opened.get(1).toExternalForm());
	}

	@Test
	void printingActionsStayOffWhenPrintingIsNotAllowed() throws Exception {
		// backwards compatibility
		FormattingController app = new FormattingController(new HeadlessFreeMind());
		Action print = (Action) newInner(app, "PrintAction", new Class<?>[] { Controller.class, boolean.class },
				new Object[] { app, true });
		Action printDirect = (Action) newInner(app, "PrintAction",
				new Class<?>[] { Controller.class, boolean.class }, new Object[] { app, false });
		setAction(app, "print", print);
		setAction(app, "printDirect", printDirect);
		setAction(app, "printPreview", (Action) newInner(app, "PrintPreviewAction"));
		setAction(app, "page", (Action) newInner(app, "PageAction"));
		setAction(app, "close", (Action) newStatic("CloseAction", app));
		setAction(app, "moveToRoot", (Action) newInner(app, "MoveToRootAction"));
		setAction(app, "showSelectionAsRectangle",
				(Action) newInner(app, "ShowSelectionAsRectangleAction"));
		setField(app, "toolbar", new MainToolBar(app));
		setField(app, "isPrintingAllowed", Boolean.FALSE);
		app.setAllActions(true);
		assertFalse(print.isEnabled());
		assertFalse(printDirect.isEnabled());
		assertFalse(app.page.isEnabled());
		assertTrue(app.close.isEnabled());
		setField(app, "isPrintingAllowed", Boolean.TRUE);
		app.setAllActions(false);
		assertFalse(app.close.isEnabled());
		assertFalse(print.isEnabled());
	}

	@Test
	void theFilterToolbarHidesAndShowsAgain() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		FilterController filters = new FilterController(opened.app);
		setField(opened.app, "mFilterController", filters);
		assertSame(filters, opened.app.getFilterController());
		opened.app.getMapModuleManager().changeToMapModule(opened.app.getMapModule().toString());
		Action show = (Action) newInner(opened.app, "ShowFilterToolbarAction");
		assertFalse(filters.isVisible());
		show.actionPerformed(event("filter"));
		assertTrue(filters.isVisible());
		show.actionPerformed(event("filter"));
		assertFalse(filters.isVisible());
	}

	private static void remember(FormattingController app, int orientation, String name) throws Exception {
		PageFormat format = new PageFormat();
		format.setOrientation(orientation);
		app.keep(format);
		assertEquals(name, app.getProperty("page_orientation"));
		assertEquals(Tools.getPageFormatAsString(format.getPaper()), app.getProperty("page_format"));
	}

	private static String formattedZoom(float zoom) {
		return Resources.getInstance().format("user_defined_zoom_status_bar",
				new Object[] { String.valueOf(zoom * 100f) });
	}

	private static void openAnother(Opened opened, String text) {
		MindMapMapModel model = new MindMapMapModel(opened.mode);
		model.setRoot(new MindMapNodeModel(text, model));
		opened.app.getMapModuleManager().newMapModule(model, opened.mode);
	}

	private static RecordingFrame install(Opened opened) {
		RecordingFrame frame = new RecordingFrame();
		opened.app.setFrame(frame);
		return frame;
	}

	private static ActionEvent event(String command) {
		return new ActionEvent(new JPanel(), ActionEvent.ACTION_PERFORMED, command);
	}

	private static Object newInner(Controller outer, String name) throws Exception {
		return newInner(outer, name, new Class<?>[] { Controller.class }, new Object[] { outer });
	}

	private static Object newInner(Controller outer, String name, Class<?>[] extraTypes, Object[] extraArgs)
			throws Exception {
		Class<?> type = Class.forName("freemind.controller.Controller$" + name);
		Class<?>[] types = new Class<?>[extraTypes.length + 1];
		types[0] = Controller.class;
		System.arraycopy(extraTypes, 0, types, 1, extraTypes.length);
		Constructor<?> ctor = type.getDeclaredConstructor(types);
		ctor.setAccessible(true);
		Object[] args = new Object[extraArgs.length + 1];
		args[0] = outer;
		System.arraycopy(extraArgs, 0, args, 1, extraArgs.length);
		return ctor.newInstance(args);
	}

	private static Object newStatic(String name, Controller app) throws Exception {
		Class<?> type = Class.forName("freemind.controller.Controller$" + name);
		Constructor<?> ctor = type.getDeclaredConstructor(Controller.class);
		ctor.setAccessible(true);
		return ctor.newInstance(app);
	}

	private static void setAction(Controller app, String field, Action action) throws Exception {
		Controller.class.getField(field).set(app, action);
	}

	private static void setField(Controller app, String field, Object value) throws Exception {
		Field found = Controller.class.getDeclaredField(field);
		found.setAccessible(true);
		found.set(app, value);
	}

	static final class FormattingController extends Controller {
		FormattingController(FreeMindMain frame) {
			super(frame);
		}

		void keep(PageFormat format) throws Exception {
			Field field = Controller.class.getDeclaredField("pageFormat");
			field.setAccessible(true);
			field.set(this, format);
			storePageFormat();
		}

		boolean acquire() throws Exception {
			Method method = Controller.class.getDeclaredMethod("acquirePrinterJobAndPageFormat");
			method.setAccessible(true);
			return (Boolean) method.invoke(this);
		}
	}

	static final class RecordingFrame extends HeadlessFreeMind {
		final JPanel content = new JPanel();
		final JScrollPane scroll = new JScrollPane(new JPanel());
		final JPanel contentComponent = new JPanel();
		final List<String> outs = new ArrayList<>();
		final List<URL> opened = new ArrayList<>();
		String title = "";
		JComponent inserted;
		int insertCount;
		boolean removedSplit;
		MenuBar menuBar;

		@Override
		public Container getContentPane() {
			return content;
		}

		@Override
		public JScrollPane getScrollPane() {
			return scroll;
		}

		@Override
		public JComponent getContentComponent() {
			return contentComponent;
		}

		@Override
		public void out(String message) {
			outs.add(message);
		}

		@Override
		public void setTitle(String title) {
			this.title = title;
		}

		@Override
		public void openDocument(URL location) {
			opened.add(location);
		}

		@Override
		public JSplitPane insertComponentIntoSplitPane(JComponent component) {
			inserted = component;
			insertCount++;
			return null;
		}

		@Override
		public void removeSplitPane() {
			removedSplit = true;
		}

		@Override
		public MenuBar getFreeMindMenuBar() {
			return menuBar;
		}
	}
}
