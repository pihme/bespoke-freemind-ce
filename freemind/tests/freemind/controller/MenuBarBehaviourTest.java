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

package freemind.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.main.HeadlessFreeMind;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** The menu bar lists the open map and a recent file, and an unbound key does nothing. */
@DisplayName("MenuBarBehaviourTest")
class MenuBarBehaviourTest {

	@Test
	void anUnboundKeyIsNotConsumed() throws Exception {
		// backwards compatibility
		new HeadlessFreeMind();
		MenuBar bar = new MenuBar(new Controller(new HeadlessFreeMind()));
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F24, 0);
		KeyEvent event = new KeyEvent(bar, KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_F24,
				KeyEvent.CHAR_UNDEFINED);
		assertFalse(bar.processKeyBinding(stroke, event, JComponent.WHEN_IN_FOCUSED_WINDOW, true));
		assertNull(bar.getMenuHolder());
		assertNull(bar.getMapsPopupMenu());
	}

	@Test
	void theMenusListTheOpenMapAndARecentFile() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		Controller app = opened.app;
		installActions(app);
		String moduleName = app.getMapModule().toString();
		String recent = "MindMap:/tmp/cov-menu-recent.mm";
		app.getLastOpenedList().add(recent, moduleName);

		MenuBar bar = new MenuBar(app);
		StructuredMenuHolder holder = new StructuredMenuHolder();
		Field menuHolder = MenuBar.class.getDeclaredField("menuHolder");
		menuHolder.setAccessible(true);
		menuHolder.set(bar, holder);
		FreeMindPopupMenu popup = new FreeMindPopupMenu();
		Field popupField = MenuBar.class.getDeclaredField("mapsPopupMenu");
		popupField.setAccessible(true);
		popupField.set(bar, popup);
		buildSkeleton(holder, app);
		invoke(bar, "updateFileMenu");
		invoke(bar, "updateViewMenu");
		invoke(bar, "updateEditMenu");
		invoke(bar, "updateModeMenu");
		invoke(bar, "updateMapsMenu", new Class<?>[] { StructuredMenuHolder.class, String.class },
				new Object[] { holder, MenuBar.MINDMAP_MENU + "mindmaps/" });
		invoke(bar, "updateMapsMenu", new Class<?>[] { StructuredMenuHolder.class, String.class },
				new Object[] { holder, MenuBar.POPUP_MENU });
		invoke(bar, "addAdditionalPopupActions");
		holder.updateMenus(bar, MenuBar.MENU_BAR_PREFIX);
		holder.updateMenus(popup, MenuBar.GENERAL_POPUP_PREFIX);
		assertTrue(bar.getMenuCount() > 0);
		assertNotNull(bar.getMenuHolder());
		assertTrue(bar.getMapsPopupMenu().getComponentCount() > 0);

		fire(bar, moduleName);
		SwingUtilities.invokeAndWait(() -> {
		});
		assertEquals(moduleName, app.getMapModule().toString());

		fireCommand(bar, "MindMap");
		SwingUtilities.invokeAndWait(() -> {
		});
		assertEquals("MindMap", app.getMode().toString());

		fire(bar, recent);
		assertEquals(moduleName, app.getMapModule().toString());

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F24, 0);
		KeyEvent event = new KeyEvent(bar, KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_F24,
				KeyEvent.CHAR_UNDEFINED);
		assertFalse(bar.processKeyBinding(stroke, event, JComponent.WHEN_IN_FOCUSED_WINDOW, true));
	}

	private static void fire(MenuBar bar, String text) {
		for (JMenuItem item : items(bar)) {
			if (text.equals(item.getText())) {
				ActionEvent event = new ActionEvent(item, ActionEvent.ACTION_PERFORMED, item.getActionCommand());
				for (ActionListener listener : item.getActionListeners()) {
					listener.actionPerformed(event);
				}
			}
		}
	}

	private static void fireCommand(MenuBar bar, String command) {
		for (JMenuItem item : items(bar)) {
			if (command.equals(item.getActionCommand())) {
				ActionEvent event = new ActionEvent(item, ActionEvent.ACTION_PERFORMED, command);
				for (ActionListener listener : item.getActionListeners()) {
					listener.actionPerformed(event);
				}
			}
		}
	}

	private static List<JMenuItem> items(MenuBar bar) {
		List<JMenuItem> found = new ArrayList<>();
		collect(bar, found);
		return found;
	}

	private static void collect(java.awt.Component component, List<JMenuItem> found) {
		if (component instanceof JMenuItem && !(component instanceof JMenu)) {
			found.add((JMenuItem) component);
		}
		if (component instanceof JMenu) {
			JMenu menu = (JMenu) component;
			for (int i = 0; i < menu.getMenuComponentCount(); i++) {
				collect(menu.getMenuComponent(i), found);
			}
		} else if (component instanceof javax.swing.JMenuBar) {
			javax.swing.JMenuBar bar = (javax.swing.JMenuBar) component;
			for (int i = 0; i < bar.getMenuCount(); i++) {
				collect(bar.getMenu(i), found);
			}
		}
	}

	private static void buildSkeleton(StructuredMenuHolder holder, Controller app) {
		holder.addMenu(new JMenu(app.getResourceString("file")), MenuBar.FILE_MENU + ".");
		holder.addCategory(MenuBar.FILE_MENU + "open");
		holder.addCategory(MenuBar.FILE_MENU + "close");
		holder.addSeparator(MenuBar.FILE_MENU);
		holder.addCategory(MenuBar.FILE_MENU + "export");
		holder.addSeparator(MenuBar.FILE_MENU);
		holder.addCategory(MenuBar.FILE_MENU + "import");
		holder.addSeparator(MenuBar.FILE_MENU);
		holder.addCategory(MenuBar.FILE_MENU + "print");
		holder.addSeparator(MenuBar.FILE_MENU);
		holder.addCategory(MenuBar.FILE_MENU + "last");
		holder.addSeparator(MenuBar.FILE_MENU);
		holder.addCategory(MenuBar.FILE_MENU + "quit");
		holder.addMenu(new JMenu(app.getResourceString("edit")), MenuBar.EDIT_MENU + ".");
		holder.addMenu(new JMenu(app.getResourceString("menu_view")), MenuBar.VIEW_MENU + ".");
		holder.addMenu(new JMenu(app.getResourceString("menu_insert")), MenuBar.INSERT_MENU + ".");
		holder.addMenu(new JMenu(app.getResourceString("menu_format")), MenuBar.FORMAT_MENU + ".");
		holder.addMenu(new JMenu(app.getResourceString("menu_navigate")), MenuBar.NAVIGATE_MENU + ".");
		holder.addMenu(new JMenu(app.getResourceString("menu_extras")), MenuBar.EXTRAS_MENU + ".");
		holder.addMenu(new JMenu(app.getResourceString("mindmaps")), MenuBar.MINDMAP_MENU + ".");
		holder.addCategory(MenuBar.MINDMAP_MENU + "navigate");
		holder.addSeparator(MenuBar.MINDMAP_MENU);
		holder.addCategory(MenuBar.MINDMAP_MENU + "mindmaps");
		holder.addSeparator(MenuBar.MINDMAP_MENU);
		holder.addCategory(MenuBar.MODES_MENU);
		holder.addCategory(MenuBar.POPUP_MENU + "navigate");
		holder.addMenu(new JMenu(app.getResourceString("help")), MenuBar.HELP_MENU + ".");
		holder.addSeparator(MenuBar.HELP_MENU);
		holder.addCategory(MenuBar.HELP_MENU + "bugs");
		holder.addSeparator(MenuBar.HELP_MENU);
	}

	private static void invoke(MenuBar bar, String name) throws Exception {
		invoke(bar, name, new Class<?>[0], new Object[0]);
	}

	private static void invoke(MenuBar bar, String name, Class<?>[] types, Object[] args) throws Exception {
		Method method = MenuBar.class.getDeclaredMethod(name, types);
		method.setAccessible(true);
		method.invoke(bar, args);
	}

	private static void installActions(Controller app) throws Exception {
		set(app, "documentation", (Action) inner(app, "DocumentationAction"));
		set(app, "freemindUrl", (Action) inner(app, "OpenURLAction",
				new Class<?>[] { Controller.class, String.class, String.class },
				new Object[] { app, "FreeMind", "http://example.invalid/freemind" }));
		set(app, "faq", (Action) inner(app, "OpenURLAction",
				new Class<?>[] { Controller.class, String.class, String.class },
				new Object[] { app, "FAQ", "http://example.invalid/faq" }));
		set(app, "keyDocumentation", (Action) inner(app, "KeyDocumentationAction"));
		set(app, "webDocu", (Action) inner(app, "OpenURLAction",
				new Class<?>[] { Controller.class, String.class, String.class },
				new Object[] { app, "web", "http://example.invalid/doc" }));
		set(app, "license", (Action) inner(app, "LicenseAction"));
		set(app, "about", (Action) inner(app, "AboutAction"));
		set(app, "page", (Action) inner(app, "PageAction"));
		set(app, "print", (Action) inner(app, "PrintAction",
				new Class<?>[] { Controller.class, boolean.class }, new Object[] { app, true }));
		set(app, "printPreview", (Action) inner(app, "PrintPreviewAction"));
		set(app, "close", (Action) staticAction("CloseAction", app));
		set(app, "quit", (Action) inner(app, "QuitAction"));
		set(app, "moveToRoot", (Action) inner(app, "MoveToRootAction"));
		set(app, "navigationPreviousMap", (Action) inner(app, "NavigationPreviousMapAction"));
		set(app, "navigationNextMap", (Action) inner(app, "NavigationNextMapAction"));
		set(app, "navigationMoveMapLeftAction", (Action) inner(app, "NavigationMoveMapLeftAction"));
		set(app, "navigationMoveMapRightAction", (Action) inner(app, "NavigationMoveMapRightAction"));
		set(app, "toggleToolbar", (Action) inner(app, "ToggleToolbarAction"));
		set(app, "toggleLeftToolbar", (Action) inner(app, "ToggleLeftToolbarAction"));
		set(app, "showSelectionAsRectangle", (Action) inner(app, "ShowSelectionAsRectangleAction"));
		set(app, "zoomIn", (Action) inner(app, "ZoomInAction"));
		set(app, "zoomOut", (Action) inner(app, "ZoomOutAction"));
	}

	private static void set(Controller app, String field, Action action) throws Exception {
		Controller.class.getField(field).set(app, action);
	}

	private static Object inner(Controller outer, String name) throws Exception {
		return inner(outer, name, new Class<?>[] { Controller.class }, new Object[] { outer });
	}

	private static Object inner(Controller outer, String name, Class<?>[] extraTypes, Object[] extraArgs)
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

	private static Object staticAction(String name, Controller app) throws Exception {
		Class<?> type = Class.forName("freemind.controller.Controller$" + name);
		Constructor<?> ctor = type.getDeclaredConstructor(Controller.class);
		ctor.setAccessible(true);
		return ctor.newInstance(app);
	}
}
