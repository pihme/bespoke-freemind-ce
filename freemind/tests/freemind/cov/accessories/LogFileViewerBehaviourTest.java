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

package tests.freemind.cov.accessories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JTextArea;

import org.junit.jupiter.api.Test;

import accessories.plugins.LogFileViewer;
import freemind.extensions.MindMapHook.PluginBaseClassSearcher;
import freemind.main.LogFileLogHandler;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

class LogFileViewerBehaviourTest {

	// backwards compatibility
	@Test
	void logLinesAreAppendedAndTheLevelAndPrintHandlerFollowTheMenu() throws Exception {
		Opened opened = MapFixture.open("Child");
		LogFileViewer.Registration registration = new LogFileViewer.Registration(opened.mode, opened.mode.getMap());
		Exposed viewer = new Exposed();
		viewer.setController(opened.mode);
		viewer.setProperties(new Properties());
		viewer.setPluginBaseClass(new PluginBaseClassSearcher() {
			public Object getPluginBaseObject() {
				return registration;
			}
		});
		assertThrows(HeadlessException.class, viewer::startupMapHook);
		assertEquals(opened.mode, viewer.getMindMapController());
		assertTrue(viewer.isMapModuleChangeAllowed(null, null, null, null));
		viewer.beforeMapModuleChange(null, null, null, null);
		viewer.numberOfOpenMapInformation(1, 0);
		assertTrue(viewer.isMapModuleChangeAllowed(null, null, null, null));
		assertNotNull(viewer.baseLogger());
		assertEquals(registration, viewer.getRegistration());
		assertNull(viewer.baseHandler());

		Field loggerField = LogFileViewer.class.getDeclaredField("logger");
		loggerField.setAccessible(true);
		Logger logger = (Logger) loggerField.get(null);
		LogFileLogHandler handler = new LogFileLogHandler();
		logger.addHandler(handler);
		try {
			assertEquals(handler, viewer.baseHandler());
			Action severe = newLevel(viewer, Level.SEVERE);
			Action info = newLevel(viewer, Level.INFO);
			severe.actionPerformed(new ActionEvent(viewer, 0, "level"));
			assertEquals(Level.SEVERE, handler.getLevel());
			assertTrue(((freemind.controller.MenuItemSelectedListener) severe).isSelected(new JMenuItem(), severe));
			assertFalse(((freemind.controller.MenuItemSelectedListener) info).isSelected(new JMenuItem(), info));

			assertFalse(registration.isPrintActionActive());
			Action print = newPrint(viewer);
			print.actionPerformed(new ActionEvent(viewer, 0, "print"));
			assertTrue(registration.isPrintActionActive());
			assertTrue(((freemind.controller.MenuItemSelectedListener) print).isSelected(new JMenuItem(), print));
			registration.deRegister();
			assertTrue(registration.isPrintActionActive());
			print.actionPerformed(new ActionEvent(viewer, 0, "print"));
			assertTrue(registration.isPrintActionActive());
		} finally {
			logger.removeHandler(handler);
		}

		JTextArea area = new JTextArea("start");
		Field text = LogFileViewer.class.getDeclaredField("mTextArea");
		text.setAccessible(true);
		text.set(viewer, area);
		Object thread = newThread(viewer);
		Field update = LogFileViewer.class.getDeclaredField("mUpdateTextAreaThread");
		update.setAccessible(true);
		update.set(viewer, thread);
		area.setCaretPosition(area.getDocument().getLength());
		viewer.receiveLog(record("ALPHA"));
		runUntil(thread, area, "ALPHA");
		assertTrue(area.getText().contains("ALPHA"), area.getText());
		assertEquals(area.getDocument().getLength(), area.getCaretPosition());

		area.setCaretPosition(0);
		String before = area.getText();
		viewer.receiveLog(record("BETA"));
		runUntil(thread, area, "BETA");
		assertTrue(area.getText().contains("BETA"), area.getText());
		assertTrue(area.getCaretPosition() < area.getDocument().getLength() || before.equals(area.getText()));
		assertEquals(0, area.getCaretPosition());

		Method sleep = thread.getClass().getDeclaredMethod("sleepALittle");
		sleep.setAccessible(true);
		Thread.currentThread().interrupt();
		long started = System.nanoTime();
		sleep.invoke(thread);
		assertTrue((System.nanoTime() - started) / 1_000_000 < 50);

		Method suicide = thread.getClass().getDeclaredMethod("commitSuicide");
		suicide.setAccessible(true);
		suicide.invoke(thread);
		Field suicided = thread.getClass().getDeclaredField("mSuicided");
		suicided.setAccessible(true);
		assertTrue(suicided.getBoolean(thread));
		suicide.invoke(thread);
		assertTrue(suicided.getBoolean(thread));
		Object close = newClose(viewer);
		Field closeField = LogFileViewer.class.getDeclaredField("mCloseAction");
		closeField.setAccessible(true);
		closeField.set(viewer, close);
		assertEquals(close, viewer.getCloseAction());
	}

	private static Object newClose(LogFileViewer viewer) throws Exception {
		Class<?> type = Class.forName("accessories.plugins.LogFileViewer$CloseAction");
		Constructor<?> constructor = type.getDeclaredConstructor(LogFileViewer.class);
		constructor.setAccessible(true);
		return constructor.newInstance(viewer);
	}

	private static final class Exposed extends LogFileViewer {
		Logger baseLogger() {
			return getBaseLogger();
		}

		LogFileLogHandler baseHandler() {
			return getBaseHandler();
		}
	}

	private static LogRecord record(String message) {
		LogRecord record = new LogRecord(Level.INFO, message);
		record.setLoggerName("tests");
		return record;
	}

	private static Action newLevel(LogFileViewer viewer, Level level) throws Exception {
		Class<?> type = Class.forName("accessories.plugins.LogFileViewer$SetLogLevelAction");
		Constructor<?> constructor = type.getDeclaredConstructor(LogFileViewer.class, Level.class);
		constructor.setAccessible(true);
		return (Action) constructor.newInstance(viewer, level);
	}

	private static Action newPrint(LogFileViewer viewer) throws Exception {
		Class<?> type = Class.forName("accessories.plugins.LogFileViewer$PrintOperationAction");
		Constructor<?> constructor = type.getDeclaredConstructor(LogFileViewer.class);
		constructor.setAccessible(true);
		return (Action) constructor.newInstance(viewer);
	}

	private static Object newThread(LogFileViewer viewer) throws Exception {
		Class<?> type = Class.forName("accessories.plugins.LogFileViewer$UpdateTextAreaThread");
		Constructor<?> constructor = type.getDeclaredConstructor(LogFileViewer.class);
		constructor.setAccessible(true);
		return constructor.newInstance(viewer);
	}

	private static void runUntil(Object thread, JTextArea area, String expected) throws Exception {
		Field commit = thread.getClass().getDeclaredField("mCommitSuicide");
		commit.setAccessible(true);
		commit.setBoolean(thread, false);
		Field suicided = thread.getClass().getDeclaredField("mSuicided");
		suicided.setAccessible(true);
		suicided.setBoolean(thread, false);
		Thread worker = new Thread((Runnable) thread);
		worker.start();
		long deadline = System.currentTimeMillis() + 2000;
		while (System.currentTimeMillis() < deadline && !area.getText().contains(expected)) {
			EventQueue.invokeAndWait(() -> {
			});
			Thread.sleep(20);
		}
		commit.setBoolean(thread, true);
		worker.interrupt();
		worker.join(1000);
		EventQueue.invokeAndWait(() -> {
		});
		assertTrue(area.getText().contains(expected), area.getText());
	}
}
