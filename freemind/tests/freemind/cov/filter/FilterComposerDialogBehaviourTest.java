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

package tests.freemind.cov.filter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Timer;

import javax.swing.filechooser.FileFilter;

import org.junit.jupiter.api.Test;

import freemind.controller.Controller;
import freemind.controller.FreeMindToolBar;
import freemind.controller.filter.FilterComposerDialog;
import freemind.controller.filter.FilterController;
import freemind.main.Resources;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** The composer is a window. Headless construction fails, and the file filter does not need one. */
class FilterComposerDialogBehaviourTest {

	@Test
	void theConstructorThrowsHeadlessExceptionAndAddsNoWindow() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha");
		try {
			assertTrue(GraphicsEnvironment.isHeadless());
			FilterController filters = new FilterController(opened.app);
			Field field = Controller.class.getDeclaredField("mFilterController");
			field.setAccessible(true);
			field.set(opened.app, filters);
			filters.afterMapModuleChange(null, null, opened.app.getMapModule(), opened.mode.getMode());
			FreeMindToolBar bar = (FreeMindToolBar) FilterController.class.getMethod("getFilterToolbar")
					.invoke(filters);

			Window[] before = Window.getWindows();
			Constructor<?> ctor = FilterComposerDialog.class.getConstructor(Controller.class, bar.getClass());
			assertThrows(HeadlessException.class, () -> {
				try {
					ctor.newInstance(opened.app, bar);
				} catch (InvocationTargetException ex) {
					if (ex.getCause() instanceof HeadlessException headless) {
						throw headless;
					}
					if (ex.getCause() instanceof RuntimeException runtime) {
						throw runtime;
					}
					throw new IllegalStateException(ex.getCause());
				}
			});
			assertArrayEquals(before, Window.getWindows());
		} finally {
			close(opened);
		}
	}

	@Test
	void theFileFilterAcceptsDirectoriesAndMindMapFilterFilesOnly() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open();
		Path dir = Files.createTempDirectory("mmfilter-accept");
		try {
			FileFilter filter = fileFilter();
			File directory = dir.toFile();
			assertTrue(filter.accept(directory));

			File match = new File(directory, "saved.mmfilter");
			assertTrue(match.createNewFile());
			assertTrue(filter.accept(match));

			File upper = new File(directory, "SAVED.MMFILTER");
			assertTrue(upper.createNewFile());
			assertTrue(filter.accept(upper));

			File mindMap = new File(directory, "map.mm");
			assertTrue(mindMap.createNewFile());
			assertFalse(filter.accept(mindMap));

			File bare = new File(directory, "notes");
			assertTrue(bare.createNewFile());
			assertFalse(filter.accept(bare));

			File dotted = new File(directory, ".mmfilter");
			assertTrue(dotted.createNewFile());
			assertFalse(filter.accept(dotted));

			File other = new File(directory, "saved.mmfilter.bak");
			assertTrue(other.createNewFile());
			assertFalse(filter.accept(other));

			assertEquals(Resources.getInstance().getResourceString("mindmaps_filter_desc"), filter.getDescription());
		} finally {
			close(opened);
			delete(dir);
		}
	}

	private static FileFilter fileFilter() throws Exception {
		Class<?> type = Class.forName("freemind.controller.filter.FilterComposerDialog$MindMapFilterFileFilter");
		Field field = type.getDeclaredField("filter");
		field.setAccessible(true);
		return (FileFilter) field.get(null);
	}

	private static void close(Opened opened) throws Exception {
		Field viewTimer = opened.view.getClass().getDeclaredField("mCenterNodeTimer");
		viewTimer.setAccessible(true);
		((Timer) viewTimer.get(opened.view)).cancel();
		Field mapTimer = opened.root.getMap().getClass().getSuperclass()
				.getDeclaredField("mTimerForFileChangeObservation");
		mapTimer.setAccessible(true);
		((Timer) mapTimer.get(opened.root.getMap())).cancel();
	}

	private static void delete(Path dir) throws IOException {
		try (var walk = Files.walk(dir)) {
			walk.sorted(Comparator.reverseOrder()).forEach(path -> {
				try {
					Files.deleteIfExists(path);
				} catch (IOException ex) {
					throw new IllegalStateException(ex);
				}
			});
		}
	}
}
