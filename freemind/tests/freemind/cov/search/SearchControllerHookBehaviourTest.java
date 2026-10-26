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

package tests.freemind.cov.search;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Window;
import java.io.File;
import java.util.logging.Logger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.controller.Controller;
import freemind.main.HeadlessFreeMind;
import freemind.modes.MapAdapter;
import freemind.modes.ModeController;
import plugins.search.SearchControllerHook;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** The search hook reads open maps. The search dialog cannot open while headless. */
@DisplayName("SearchControllerHookBehaviourTest")
class SearchControllerHookBehaviourTest {

	private static final ThreadLocal<ModeController> pendingController = new ThreadLocal<>();

	@Test
	void theLoggerAndFrameComeFromTheOpenMap() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha");
		SearchControllerHook hook = hook(opened);
		assertSame(Logger.getLogger(SearchControllerHook.class.getName()),
				hook.getLogger(SearchControllerHook.class));
		assertNull(hook.getJFrame());
	}

	@Test
	void theWaitingCursorCanBeShownAndCleared() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha");
		SearchControllerHook hook = hook(opened);
		assertDoesNotThrow(() -> {
			hook.setWaitingCursor(true);
			hook.setWaitingCursor(false);
		});
		assertEquals("ROOT", opened.root.getText());
	}

	@Test
	void openTabsAreTheMapFileOrNullWhenTheMapHasNone() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha");
		// HeadlessFreeMind.getController() stays null, so the hook cannot see tabs
		// until the frame reports the controller that opened this map.
		opened.app.setFrame(new ReportingFrame(opened.app));
		SearchControllerHook hook = hook(opened);
		File[] files = hook.getFilesOfOpenTabs();
		assertEquals(1, files.length);
		assertNull(files[0]);

		File mapFile = File.createTempFile("search-tab", ".mm");
		mapFile.deleteOnExit();
		((MapAdapter) opened.mode.getMap()).setFile(mapFile);
		File[] withFile = hook.getFilesOfOpenTabs();
		assertEquals(1, withFile.length);
		assertSame(mapFile, withFile[0]);
	}

	@Test
	void startupRefusesToShowTheSearchDialogWhileHeadless() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha");
		SearchControllerHook hook = hook(opened);
		assertTrue(GraphicsEnvironment.isHeadless());
		Window[] before = Window.getWindows();
		assertThrows(HeadlessException.class, hook::startupMapHook);
		assertArrayEquals(before, Window.getWindows());
	}

	private static SearchControllerHook hook(Opened opened) {
		pendingController.set(opened.mode);
		try {
			SearchControllerHook created = new HookOnMap();
			created.setController(opened.mode);
			return created;
		} finally {
			pendingController.remove();
		}
	}

	/** Supplies the controller while the hook constructor reads it. */
	private static final class HookOnMap extends SearchControllerHook {
		@Override
		protected ModeController getController() {
			ModeController pending = pendingController.get();
			if (pending != null) {
				return pending;
			}
			return super.getController();
		}
	}

	/** Reports the map controller. The stock headless frame does not. */
	private static final class ReportingFrame extends HeadlessFreeMind {
		private final Controller controller;

		ReportingFrame(Controller controller) {
			this.controller = controller;
		}

		@Override
		public Controller getController() {
			return controller;
		}
	}
}
