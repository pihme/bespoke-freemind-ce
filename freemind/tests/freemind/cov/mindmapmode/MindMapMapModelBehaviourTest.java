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

package tests.freemind.cov.mindmapmode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.EventQueue;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Properties;
import java.util.Timer;

import org.junit.jupiter.api.Test;

import freemind.main.Resources;
import freemind.modes.ArrowLinkTarget;
import freemind.modes.NodeAdapter;
import freemind.modes.mindmapmode.EncryptedMindMapNode;
import freemind.modes.mindmapmode.MindMapMapModel;
import freemind.modes.mindmapmode.MindMapNodeModel;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** A map model builds nodes, locks a semaphore, and writes automatic saves. */
class MindMapMapModelBehaviourTest {

	@Test
	void nodeFactoriesUseTheRequestedClassOrFallBack() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("A", "B");
		MindMapMapModel model = (MindMapMapModel) opened.mode.getMap();
		MindMapNodeModel kept = new MindMapNodeModel("Kept", model);
		MindMapMapModel withRoot = new MindMapMapModel(kept, opened.mode);
		try {
			assertSame(kept, withRoot.getRootNode());
			NodeAdapter plain = withRoot.createNodeAdapter(withRoot, null);
			assertEquals(MindMapNodeModel.class, plain.getClass());
			NodeAdapter encrypted = withRoot.createNodeAdapter(withRoot, EncryptedMindMapNode.class.getName());
			assertEquals(EncryptedMindMapNode.class, encrypted.getClass());
			NodeAdapter badName = withRoot.createNodeAdapter(withRoot, "no.such.Node");
			assertEquals(MindMapNodeModel.class, badName.getClass());
			NodeAdapter wrongCtor = withRoot.createNodeAdapter(withRoot, String.class.getName());
			assertEquals(MindMapNodeModel.class, wrongCtor.getClass());

			NodeAdapter cipher = withRoot.createEncryptedNode("cipher-text");
			assertTrue(cipher instanceof EncryptedMindMapNode);
			assertEquals("cipher-text", cipher.getAdditionalInfo());
			assertFalse(cipher.isWriteable());

			ArrowLinkTarget target = withRoot.createArrowLinkTarget(kept, plain);
			assertSame(kept, target.getSource());
			assertSame(plain, target.getTarget());
		} finally {
			withRoot.destroy();
		}

		Object manager = field(model, "lockManager");
		assertTrue(manager.getClass().getName().contains("Dummy"));
		assertNull(model.tryToLock(File.createTempFile("dummy-lock", ".mm")));
		assertFalse(model.isReadOnly());
		run(manager);
	}

	@Test
	void locksHonourFreshOldAndBrokenSemaphoreFiles() throws Exception {
		// backwards compatibility
		Properties properties = Resources.getInstance().getProperties();
		String locking = properties.getProperty("experimental_file_locking_on");
		properties.setProperty("experimental_file_locking_on", "true");
		Opened opened = MapFixture.open();
		silenceAutomaticSave(opened);
		MindMapNodeModel root = new MindMapNodeModel("Locked", (MindMapMapModel) opened.mode.getMap());
		MindMapMapModel model = new MindMapMapModel(root, opened.mode);
		File dir = Files.createTempDirectory("mm-lock").toFile();
		try {
			flushEdt();
			Object manager = field(model, "lockManager");
			run(manager);

			File freshMap = new File(dir, "fresh.mm");
			Files.writeString(freshMap.toPath(), map("Fresh"), StandardCharsets.UTF_8);
			File freshSem = semaphore(freshMap);
			writeSemaphore(freshSem, "other-user", System.currentTimeMillis());
			assertEquals("other-user", model.tryToLock(freshMap));
			assertTrue(freshSem.exists());
			assertTrue(Files.readString(freshSem.toPath(), StandardCharsets.UTF_8).startsWith("other-user"));

			File oldMap = new File(dir, "old.mm");
			Files.writeString(oldMap.toPath(), map("Old"), StandardCharsets.UTF_8);
			File oldSem = semaphore(oldMap);
			writeSemaphore(oldSem, "ancient-user", System.currentTimeMillis() - 6L * 60L * 1000L);
			assertThrows(ClosedChannelException.class, () -> model.tryToLock(oldMap));
			String rewritten = Files.readString(oldSem.toPath(), StandardCharsets.UTF_8);
			assertTrue(rewritten.startsWith(System.getProperty("user.name")));
			assertFalse(rewritten.contains("ancient-user"));
			Method pop = manager.getClass().getMethod("popLockingUserOfOldLock");
			pop.setAccessible(true);
			assertEquals("ancient-user", pop.invoke(manager));
			assertNull(pop.invoke(manager));

			File brokenMap = new File(dir, "broken.mm");
			Files.writeString(brokenMap.toPath(), map("Broken"), StandardCharsets.UTF_8);
			writeSemaphore(semaphore(brokenMap), "user", "not-a-number");
			assertThrows(NumberFormatException.class, () -> model.tryToLock(brokenMap));

			Field locked = manager.getClass().getDeclaredField("lockedSemaphoreFile");
			locked.setAccessible(true);
			locked.set(manager, oldSem);
			run(manager);
			assertTrue(oldSem.exists());
			assertTrue(Files.readString(oldSem.toPath(), StandardCharsets.UTF_8)
					.startsWith(System.getProperty("user.name")));
			Method releaseLock = manager.getClass().getMethod("releaseLock");
			releaseLock.setAccessible(true);
			releaseLock.invoke(manager);
			assertFalse(oldSem.exists());
			assertNull(locked.get(manager));

			Timer timer = new Timer(true);
			Field lockTimer = manager.getClass().getDeclaredField("lockTimer");
			lockTimer.setAccessible(true);
			lockTimer.set(manager, timer);
			Method releaseTimer = manager.getClass().getMethod("releaseTimer");
			releaseTimer.setAccessible(true);
			releaseTimer.invoke(manager);
			assertNull(lockTimer.get(manager));
		} finally {
			model.destroy();
			restore(properties, "experimental_file_locking_on", locking);
		}
	}

	@Test
	void automaticSaveWritesATempMapAndSkipsACleanOrMissingDirectory() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open();
		Properties resources = Resources.getInstance().getProperties();
		String deleteSaves = resources.getProperty("delete_automatic_saves_at_exit");
		String time = opened.frame.getProperty("time_for_automatic_save");
		String path = opened.frame.getProperty("path_to_automatic_saves");
		String count = opened.frame.getProperty("number_of_different_files_for_automatic_save");
		File store = Files.createTempDirectory("mm-autosave").toFile();
		File missing = new File(store, "missing-dir");
		resources.setProperty("delete_automatic_saves_at_exit", "true");
		opened.frame.setProperty("time_for_automatic_save", "1000000000");
		opened.frame.setProperty("number_of_different_files_for_automatic_save", "0");
		opened.frame.setProperty("path_to_automatic_saves", missing.getPath());
		MindMapMapModel model = new MindMapMapModel(opened.mode);
		try {
			flushEdt();
			assertNull(field(model, "timerForAutomaticSaving"));
			model.setReadOnly(true);
			File blocked = new File(store, "blocked.mm");
			assertFalse(model.save(blocked));
			assertFalse(blocked.exists());

			model.setReadOnly(false);
			opened.frame.setProperty("path_to_automatic_saves", "freemind_home");
			File saved = new File(store, "saved.mm");
			assertTrue(model.save(saved));
			assertTrue(Files.readString(saved.toPath(), StandardCharsets.UTF_8).contains("<map"));
			Object firstTimer = field(model, "timerForAutomaticSaving");
			assertNotNull(firstTimer);
			assertTrue(model.save(saved));
			assertNotNull(field(model, "timerForAutomaticSaving"));

			setField(model, "timerForAutomaticSaving", null);
			opened.frame.setProperty("path_to_automatic_saves", "default");
			assertTrue(model.save(saved));
			assertNotNull(field(model, "timerForAutomaticSaving"));

			model.setSaved(true);
			Object clean = newAutomaticSave(model, 0, true, store);
			run(clean);
			assertEquals(0, fmFiles(store).length);

			model.setSaved(false);
			Object savedSince = newAutomaticSave(model, 0, true, store);
			model.setSaved(true);
			run(savedSince);
			assertEquals(0, fmFiles(store).length);

			model.setFile(null);
			assertNull(model.toString());
			model.setSaved(false);
			Object unnamed = newAutomaticSave(model, 0, true, store);
			model.setSaved(false);
			run(unnamed);
			File[] created = fmFiles(store);
			assertEquals(1, created.length);
			assertTrue(created[0].getName().contains("unnamed"));
			assertTrue(Files.readString(created[0].toPath(), StandardCharsets.UTF_8).contains("<map"));
			model.setSaved(false);
			run(unnamed);
			assertEquals(1, fmFiles(store).length);

			model.setFile(new File(store, "named-map.mm"));
			model.setSaved(false);
			Object named = newAutomaticSave(model, 0, true, store);
			model.setSaved(false);
			run(named);
			assertTrue(Arrays.stream(fmFiles(store)).anyMatch(file -> file.getName().contains("named-map")));

			File notDir = new File(store, "not-a-directory");
			assertTrue(notDir.createNewFile());
			int beforeFailure = fmFiles(store).length;
			model.setSaved(false);
			Object failing = newAutomaticSave(model, 1, false, notDir);
			model.setSaved(false);
			run(failing);
			assertEquals(beforeFailure, fmFiles(store).length);
		} finally {
			model.destroy();
			restore(resources, "delete_automatic_saves_at_exit", deleteSaves);
			restoreFrame(opened, "time_for_automatic_save", time);
			restoreFrame(opened, "path_to_automatic_saves", path);
			restoreFrame(opened, "number_of_different_files_for_automatic_save", count);
		}
	}

	private static void silenceAutomaticSave(Opened opened) {
		opened.frame.setProperty("time_for_automatic_save", "1000000000");
	}

	private static void flushEdt() throws Exception {
		if (!EventQueue.isDispatchThread()) {
			EventQueue.invokeAndWait(() -> {
			});
		}
	}

	private static Object newAutomaticSave(MindMapMapModel model, int files, boolean delete, File dir)
			throws Exception {
		Class<?> type = Class.forName("freemind.modes.mindmapmode.MindMapMapModel$DoAutomaticSave");
		Constructor<?> ctor = type.getDeclaredConstructor(MindMapMapModel.class, int.class, boolean.class, File.class);
		ctor.setAccessible(true);
		return ctor.newInstance(model, files, delete, dir);
	}

	private static void run(Object task) throws Exception {
		java.lang.reflect.Method run = task.getClass().getMethod("run");
		run.setAccessible(true);
		run.invoke(task);
	}

	private static File[] fmFiles(File dir) {
		File[] found = dir.listFiles((file, name) -> name.startsWith("FM_"));
		return found == null ? new File[0] : found;
	}

	private static File semaphore(File map) {
		return new File(map.getParentFile(), "$~" + map.getName() + "~");
	}

	private static void writeSemaphore(File file, String user, long stamp) throws Exception {
		writeSemaphore(file, user, Long.toString(stamp));
	}

	private static void writeSemaphore(File file, String user, String stamp) throws Exception {
		Files.writeString(file.toPath(), user + "\n" + stamp + "\n", StandardCharsets.UTF_8);
	}

	private static String map(String text) {
		return "<map version=\"1.1.0\"><node TEXT=\"" + text + "\" ID=\"ID_1\"/></map>";
	}

	private static Object field(MindMapMapModel model, String name) throws Exception {
		Field found = MindMapMapModel.class.getDeclaredField(name);
		found.setAccessible(true);
		return found.get(model);
	}

	private static void setField(MindMapMapModel model, String name, Object value) throws Exception {
		Field found = MindMapMapModel.class.getDeclaredField(name);
		found.setAccessible(true);
		found.set(model, value);
	}

	private static void restore(Properties properties, String key, String value) {
		if (value == null) {
			properties.remove(key);
		} else {
			properties.setProperty(key, value);
		}
	}

	private static void restoreFrame(Opened opened, String key, String value) {
		if (value == null) {
			opened.frame.getProperties().remove(key);
		} else {
			opened.frame.setProperty(key, value);
		}
	}
}
