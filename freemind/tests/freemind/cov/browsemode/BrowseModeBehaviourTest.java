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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JToolBar;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.controller.Controller;
import freemind.main.Tools;
import freemind.modes.MapAdapter;
import freemind.modes.ModeController;
import freemind.modes.browsemode.BrowseController;
import freemind.modes.browsemode.BrowseMode;
import freemind.view.MapModule;
import freemind.view.mindmapview.MapView;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Browse mode names itself, switches maps, and restores a file. */
@DisplayName("BrowseModeBehaviourTest")
class BrowseModeBehaviourTest {

	@Test
	void activateLeavesTheOpenMapUntilBrowseHasNone() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		toolbarActions(opened.app);
		BrowseMode mode = new BrowseMode();
		mode.init(opened.app);
		assertEquals("Browse", mode.toString());
		ModeController created = mode.createModeController();
		assertTrue(created instanceof BrowseController);
		assertNotSame(mode.getDefaultModeController(), created);

		MapModule fixture = opened.app.getMapModule();
		mode.activate();
		assertSame(fixture, opened.app.getMapModule());
		mode.activate();
		assertNull(opened.app.getMapModule());
		assertSame(mode, opened.app.getMode());
	}

	@Test
	void restoreOpensAMapAndAMissingFileShowsAnError() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		toolbarActions(opened.app);
		File dir = Files.createTempDirectory("browse-mode").toFile();
		File map = new File(dir, "loaded.mm");
		Files.writeString(map.toPath(),
				"<map version=\"1.0.1\"><node TEXT=\"Loaded\" ID=\"ID_1\"/></map>",
				StandardCharsets.UTF_8);
		BrowseMode mode = new BrowseMode();
		mode.init(opened.app);
		mode.restore(map.getPath());
		MapModule loaded = opened.app.getMapModule();
		try {
			assertEquals("Loaded", loaded.getModel().getRootNode().getText());
			assertEquals(Tools.fileToUrl(map).toString(), urlText(loaded.getModeController().getModeToolBar()));
		} finally {
			stopViewTimer(loaded.getView());
			((MapAdapter) loaded.getModel()).destroy();
		}

		TrackingMode tracking = new TrackingMode();
		tracking.start(opened.app);
		Window[] before = Window.getWindows();
		try {
			File missing = new File(dir, "missing.mm");
			assertThrows(HeadlessException.class, () -> tracking.restore(missing.getPath()));
			assertEquals(before.length, Window.getWindows().length);
			assertTrue(tracking.controller.created.size() > 0);
		} finally {
			disposeNewWindows(before);
			for (MapAdapter model : tracking.controller.created) {
				model.destroy();
			}
		}
	}

	private static String urlText(JToolBar bar) {
		for (Component component : bar.getComponents()) {
			if (component instanceof JComboBox<?> box) {
				return box.getSelectedItem().toString();
			}
		}
		return null;
	}

	private static void stopViewTimer(MapView view) throws Exception {
		Field field = MapView.class.getDeclaredField("mCenterNodeTimer");
		field.setAccessible(true);
		((java.util.Timer) field.get(view)).cancel();
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
