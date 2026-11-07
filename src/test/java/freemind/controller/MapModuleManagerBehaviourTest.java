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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.controller.MapModuleManager.MapModuleChangeObserver;
import freemind.main.Tools;
import freemind.modes.Mode;
import freemind.modes.mindmapmode.MindMapMapModel;
import freemind.modes.mindmapmode.MindMapMode;
import freemind.view.MapModule;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Open maps switch, reject a duplicate name, and close without asking when forced. */
@DisplayName("MapModuleManagerBehaviourTest")
class MapModuleManagerBehaviourTest {

	@Test
	void aSecondMapWithTheSameFileNameIsNumberedAndNavigationWraps() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MapModuleManager manager = opened.app.getMapModuleManager();
		MindMapMapModel firstModel = (MindMapMapModel) opened.mode.getModel();
		firstModel.setFile(new File("/tmp/cov-a/map.mm"));
		manager.updateMapModuleName();
		assertEquals("map.mm", manager.getMapModule().getDisplayName());

		MindMapMapModel secondModel = new MindMapMapModel(opened.mode);
		secondModel.setFile(new File("/tmp/cov-b/map.mm"));
		manager.newMapModule(secondModel, opened.mode);
		assertEquals("map.mm<2>", manager.getMapModule().getDisplayName());
		assertEquals(List.of("map.mm", "map.mm<2>"), manager.getMapKeys());
		assertEquals(manager.getMapModule(), manager.getMapModules().get("map.mm<2>"));
		assertThrows(UnsupportedOperationException.class,
				() -> manager.getMapModules().put("extra", manager.getMapModule()));
		assertEquals(opened.mode, manager.getModuleGivenModeController(opened.mode).getModeController());

		manager.nextMapModule();
		assertEquals("map.mm", manager.getMapModule().getDisplayName());
		manager.previousMapModule();
		assertEquals("map.mm<2>", manager.getMapModule().getDisplayName());
		manager.previousMapModule();
		assertEquals("map.mm", manager.getMapModule().getDisplayName());

		assertTrue(manager.tryToChangeToMapModule("map.mm<2>"));
		assertEquals("map.mm<2>", manager.getMapModule().getDisplayName());
		assertFalse(manager.tryToChangeToMapModule("missing"));
		assertFalse(manager.tryToChangeToMapModule(null));
		assertEquals("map.mm<2>", manager.getMapModule().getDisplayName());
		assertThrows(IllegalArgumentException.class, () -> manager.changeToMapModule("missing"));

		URL same = Tools.fileToUrl(new File("/tmp/cov-a/map.mm"));
		assertEquals("map.mm", manager.checkIfFileIsAlreadyOpened(same));
		assertNull(manager.checkIfFileIsAlreadyOpened(Tools.fileToUrl(new File("/tmp/cov-c/other.mm"))));
		assertNull(manager.checkIfFileIsAlreadyOpened(new URL("http://example.invalid/map.mm")));
	}

	@Test
	void aVetoKeepsTheCurrentMapAndRemovingItLetsTheChangeThrough() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MapModuleManager manager = opened.app.getMapModuleManager();
		((MindMapMapModel) opened.mode.getModel()).setFile(new File("/tmp/cov-a/map.mm"));
		manager.updateMapModuleName();
		MindMapMapModel secondModel = new MindMapMapModel(opened.mode);
		secondModel.setFile(new File("/tmp/cov-b/other.mm"));
		manager.newMapModule(secondModel, opened.mode);
		Recording observer = new Recording();
		observer.allow = false;
		manager.addListener(observer);
		assertFalse(manager.changeToMapModule("map.mm"));
		assertEquals("other.mm", manager.getMapModule().getDisplayName());

		manager.removeListener(observer);
		assertTrue(manager.changeToMapModule("map.mm"));
		assertEquals("map.mm", manager.getMapModule().getDisplayName());
	}

	@Test
	void swappingReportsTheNewIndexAndAnUnknownModeClearsTheCurrentMap() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MapModuleManager manager = opened.app.getMapModuleManager();
		((MindMapMapModel) opened.mode.getModel()).setFile(new File("/tmp/cov-a/one.mm"));
		manager.updateMapModuleName();
		MindMapMapModel secondModel = new MindMapMapModel(opened.mode);
		secondModel.setFile(new File("/tmp/cov-b/two.mm"));
		manager.newMapModule(secondModel, opened.mode);
		Recording observer = new Recording();
		manager.addListener(observer);
		manager.swapModules(0, 1);
		assertEquals(List.of("two.mm", "one.mm"), manager.getMapKeys());
		assertEquals(0, observer.lastIndex);

		Mode other = new MindMapMode();
		manager.changeToMapOfMode(other);
		assertNull(manager.getMapModule());
	}

	@Test
	void aForcedCloseDropsTheMapAndReturnsItsRestorableName() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MapModuleManager manager = opened.app.getMapModuleManager();
		File file = new File("/tmp/cov-close.mm");
		((MindMapMapModel) opened.mode.getModel()).setFile(file);
		Recording observer = new Recording();
		manager.addListener(observer);
		StringBuffer restorable = new StringBuffer();
		assertTrue(manager.close(true, restorable));
		assertEquals("MindMap:" + file.getAbsolutePath(), restorable.toString());
		assertNull(manager.getMapModule());
		assertTrue(manager.getMapModuleVector().isEmpty());
		assertEquals(1, observer.closes);
	}

	private static final class Recording implements MapModuleChangeObserver {
		private boolean allow = true;
		private int closes;
		private int lastIndex = -1;

		public boolean isMapModuleChangeAllowed(MapModule oldMapModule, Mode oldMode,
				MapModule newMapModule, Mode newMode) {
			return allow;
		}

		public void beforeMapModuleChange(MapModule oldMapModule, Mode oldMode,
				MapModule newMapModule, Mode newMode) {
		}

		public void afterMapModuleChange(MapModule oldMapModule, Mode oldMode,
				MapModule newMapModule, Mode newMode) {
		}

		public void afterMapClose(MapModule oldMapModule, Mode oldMode) {
			closes += 1;
		}

		public void numberOfOpenMapInformation(int number, int index) {
			lastIndex = index;
		}
	}
}
