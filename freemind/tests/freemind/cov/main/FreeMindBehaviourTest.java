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

package tests.freemind.cov.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Authenticator;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.inet.jortho.SpellChecker;

import freemind.controller.LastStateStorageManagement;
import freemind.controller.actions.generated.instance.MindmapLastStateMapStorage;
import freemind.controller.actions.generated.instance.MindmapLastStateStorage;
import freemind.main.FeedBack;
import freemind.main.FreeMind;
import freemind.main.FreeMindCommon;
import freemind.main.FreeMindStarter;
import freemind.main.FreeMindMain.StartupDoneListener;
import freemind.main.Resources;
import freemind.main.Tools;
import freemind.modes.ModeController;

@Tag("gui")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("FreeMindBehaviourTest")
class FreeMindBehaviourTest {

	private static FreeMind frame;
	private static File home;
	private static File autoFile;
	private static String previousHome;
	private static PrintStream previousOut;
	private static PrintStream previousErr;
	private static Properties previousProps;
	private static String originalLanguage;
	private static Object previousResourcesMain;
	private static boolean resourcesSwapped;
	private static final CountingFeedBack FEEDBACK = new CountingFeedBack();

	@BeforeAll
	static void startFrame() throws Exception {
		previousOut = System.out;
		previousErr = System.err;
		previousHome = System.getProperty("user.home");
		previousProps = FreeMind.props;
		home = Files.createTempDirectory("cov-freemind-home").toFile();
		System.setProperty("user.home", home.getAbsolutePath());
		Properties defaults = new FreeMindStarter().readDefaultPreferences();
		new File(home, defaults.getProperty("properties_folder")).mkdirs();
		Properties user = new Properties();
		user.putAll(defaults);
		user.setProperty(FreeMind.RESOURCE_LOOKANDFEEL, "metal");
		user.setProperty(FreeMindCommon.CHECK_SPELLING, "false");
		user.setProperty(FreeMind.PROXY_USE_SETTINGS, "false");
		user.setProperty(FreeMindCommon.LOAD_LAST_MAPS_AND_LAYOUT, "false");
		user.setProperty(FreeMindCommon.LOAD_LAST_MAP, "false");
		user.setProperty(FreeMindCommon.LOAD_NEW_MAP, "false");
		user.setProperty("portFile", "coverage.port");
		user.setProperty(FreeMind.RESOURCES_DON_T_OPEN_PORT, "false");
		user.setProperty(FreeMind.RESOURCES_USE_TABBED_PANE, "true");
		user.setProperty("no_scrollbar", "true");
		user.setProperty("toolbarVisible", "false");
		user.setProperty("leftToolbarVisible", "false");
		user.setProperty("appwindow_state", "1");
		user.setProperty("appwindow_width", "100000");
		user.setProperty("appwindow_height", "100000");
		user.setProperty("appwindow_x", "-50");
		user.setProperty("appwindow_y", "-50");
		autoFile = new File(home, "auto.properties");
		try {
			frame = new FreeMind(defaults, user, autoFile);
		} finally {
			System.setOut(previousOut);
			System.setErr(previousErr);
		}
		originalLanguage = frame.getProperty(FreeMindCommon.RESOURCE_LANGUAGE);
		Field main = Resources.class.getDeclaredField("main");
		main.setAccessible(true);
		previousResourcesMain = main.get(Resources.getInstance());
		main.set(Resources.getInstance(), frame);
		resourcesSwapped = true;
		invoke("init", new Class<?>[] { FeedBack.class }, FEEDBACK);
	}

	@AfterAll
	static void closeFrame() throws Exception {
		if (frame != null) {
			if (frame.getController() != null && originalLanguage != null
					&& !originalLanguage.equals(frame.getProperty(FreeMindCommon.RESOURCE_LANGUAGE))) {
				frame.getController().setProperty(FreeMindCommon.RESOURCE_LANGUAGE, originalLanguage);
			}
			frame.dispose();
		}
		if (resourcesSwapped) {
			Field main = Resources.class.getDeclaredField("main");
			main.setAccessible(true);
			main.set(Resources.getInstance(), previousResourcesMain);
		}
		if (previousProps != null) {
			FreeMind.props = previousProps;
		}
		if (previousHome != null) {
			System.setProperty("user.home", previousHome);
		}
		System.setOut(previousOut);
		System.setErr(previousErr);
		Authenticator.setDefault(null);
		if (home != null) {
			deleteTree(home);
		}
	}

	@Test
	@Order(1)
	void frameReportsBoundsCursorStatusAndLoggers() throws Exception {
		// backwards compatibility
		assertNotNull(frame.getController());
		assertNotNull(frame.getResources());
		assertFalse(frame.getLoggerList().isEmpty());
		assertNotNull(frame.getLogger("tests.freemind.cov.main.again"));
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration());
		assertTrue(frame.getWinWidth() <= screen.width - insets.left - insets.right);
		assertTrue(frame.getWinHeight() <= screen.height - insets.top - insets.bottom);
		assertTrue(frame.getWinX() >= insets.left);
		assertEquals(frame.getY(), frame.getWinY());
		assertEquals(frame.getX(), frame.getWinX());
		assertEquals(frame.getWidth(), frame.getWinWidth());
		assertEquals(frame.getHeight(), frame.getWinHeight());
		assertEquals(frame.getExtendedState(), frame.getWinState());
		assertEquals(0, frame.getWinState() & java.awt.Frame.ICONIFIED);

		frame.setWaitingCursor(true);
		assertEquals(Cursor.WAIT_CURSOR, frame.getRootPane().getGlassPane().getCursor().getType());
		assertTrue(frame.getRootPane().getGlassPane().isVisible());
		frame.setWaitingCursor(false);
		assertEquals(Cursor.DEFAULT_CURSOR, frame.getRootPane().getGlassPane().getCursor().getType());
		assertFalse(frame.getRootPane().getGlassPane().isVisible());

		JLabel status = (JLabel) ((BorderLayout) frame.getContentPane().getLayout())
				.getLayoutComponent(BorderLayout.SOUTH);
		frame.err("coverage-status");
		assertEquals("coverage-status", status.getText());
		frame.actionPerformed(new ActionEvent(frame, ActionEvent.ACTION_PERFORMED, "clear"));
		assertEquals("", status.getText());

		ResourceBundle before = frame.getResources();
		String language = frame.getProperty(FreeMindCommon.RESOURCE_LANGUAGE);
		String next = "de".equals(language) ? "en" : "de";
		frame.getController().setProperty(FreeMindCommon.RESOURCE_LANGUAGE, next);
		assertTrue(frame.getResources() != before);
		assertNotNull(frame.getResourceString("file"));
	}

	@Test
	@Order(2)
	void lookAndFeelBranchesAndSpellCheckRegister() throws Exception {
		// backwards compatibility
		String[] names = { "metal", "nothing", "motif", "windows", "mac", "gtk",
				"com.example.MissingLookAndFeel", "not-a-known-look" };
		for (String name : names) {
			frame.setProperty(FreeMind.RESOURCE_LOOKANDFEEL, name);
			invoke("updateLookAndFeel");
		}
		frame.setProperty(FreeMind.RESOURCE_LOOKANDFEEL, "metal");
		invoke("updateLookAndFeel");
		assertEquals("metal", frame.getProperty(FreeMind.RESOURCE_LOOKANDFEEL));
		assertNotNull(frame.getRootPane());

		assertNotNull(FreeMind.class.getClassLoader().getResource("dictionaries.cnf"));
		java.util.Locale spellLocale = SpellChecker.getCurrentLocale();
		frame.setProperty(FreeMindCommon.CHECK_SPELLING, "true");
		invoke("setupSpellChecking");
		assertEquals(spellLocale, SpellChecker.getCurrentLocale());
		frame.setProperty(FreeMindCommon.CHECK_SPELLING, "false");
	}

	@Test
	@Order(3)
	void mapsSplitPaneAndStartupEvents() throws Exception {
		// backwards compatibility
		int before = frame.getController().getMapModuleManager().getMapModules().size();
		invoke("loadMaps", new Class<?>[] { String[].class, ModeController.class, FeedBack.class },
				(Object) new String[0], frame.getController().getModeController(), FEEDBACK);
		assertEquals(before, frame.getController().getMapModuleManager().getMapModules().size());

		ModeController created = (ModeController) invoke("createModeController",
				new Class<?>[] { String[].class }, (Object) new String[0]);
		assertNotNull(created);

		frame.setProperty(FreeMindCommon.LOAD_NEW_MAP, "true");
		int feedbackBefore = FEEDBACK.increases;
		invoke("loadMaps", new Class<?>[] { String[].class, ModeController.class, FeedBack.class },
				(Object) new String[0], created, FEEDBACK);
		assertTrue(frame.getController().getMapModuleManager().getMapModules().size() > before);
		assertTrue(FEEDBACK.increases > feedbackBefore);
		frame.setProperty(FreeMindCommon.LOAD_NEW_MAP, "false");

		File missing = new File(home, "missing.mm");
		PrintStream err = System.err;
		java.io.ByteArrayOutputStream captured = new java.io.ByteArrayOutputStream();
		System.setErr(new PrintStream(captured, true, StandardCharsets.UTF_8));
		try {
			invoke("loadMaps", new Class<?>[] { String[].class, ModeController.class, FeedBack.class },
					(Object) new String[] { missing.getAbsolutePath() }, created, FEEDBACK);
		} finally {
			System.setErr(err);
		}
		assertTrue(captured.toString(StandardCharsets.UTF_8).contains("not found"));

		MindmapLastStateStorage store = new MindmapLastStateStorage();
		store.setRestorableName("MindMap:" + missing.getAbsolutePath());
		store.setLastChanged(1L);
		store.setTabIndex(0);
		store.setLastZoom(1f);
		store.setX(0);
		store.setY(0);
		store.setLastSelected("ID_0");
		MindmapLastStateMapStorage map = new MindmapLastStateMapStorage();
		map.getListMindmapLastStateStorageList().add(store);
		frame.setProperty(FreeMindCommon.MINDMAP_LAST_STATE_MAP_STORAGE, Tools.marshall(map));
		LastStateStorageManagement management = (LastStateStorageManagement) invoke(
				"getLastStateStorageManagement");
		assertEquals(1, management.getLastOpenList().size());
		assertEquals(2, (Integer) invoke("getMaximumNumberOfMapsToLoad",
				new Class<?>[] { String[].class }, (Object) new String[] { "one.mm" }));
		frame.setProperty(FreeMindCommon.LOAD_LAST_MAPS_AND_LAYOUT, "true");
		invoke("loadMaps", new Class<?>[] { String[].class, ModeController.class, FeedBack.class },
				(Object) new String[0], created, FEEDBACK);
		frame.setProperty(FreeMindCommon.LOAD_LAST_MAPS_AND_LAYOUT, "false");

		frame.setProperty(FreeMindCommon.LOAD_LAST_MAP, "true");
		frame.setProperty(FreeMindCommon.ON_START_IF_NOT_SPECIFIED, "MindMap:" + missing.getAbsolutePath());
		invoke("loadMaps", new Class<?>[] { String[].class, ModeController.class, FeedBack.class },
				(Object) new String[0], created, FEEDBACK);
		JLabel status = (JLabel) ((BorderLayout) frame.getContentPane().getLayout())
				.getLayoutComponent(BorderLayout.SOUTH);
		assertTrue(status.getText().contains("error occured"));
		frame.setProperty(FreeMindCommon.LOAD_LAST_MAP, "false");

		String badKey = FreeMindCommon.LOAD_EVENT_DURING_STARTUP + "0";
		frame.setProperty(badKey, missing.getAbsolutePath());
		assertFalse((Boolean) invoke("processLoadEventFromStartupPhase",
				new Class<?>[] { String.class }, badKey));
		assertEquals(missing.getAbsolutePath(), frame.getProperty(badKey));
		assertFalse((Boolean) invoke("processLoadEventFromStartupPhase"));
		File good = new File(home, "loaded.mm");
		Files.writeString(good.toPath(),
				"<map version=\"" + FreeMind.XML_VERSION + "\"><node TEXT=\"Loaded\"/></map>");
		frame.setProperty(badKey, good.getAbsolutePath());
		assertTrue((Boolean) invoke("processLoadEventFromStartupPhase",
				new Class<?>[] { String.class }, badKey));
		assertNull(frame.getProperty(badKey));

		JPanel extra = new JPanel();
		frame.setProperty(FreeMind.J_SPLIT_PANE_SPLIT_TYPE, "not-a-split");
		JSplitPane vertical = frame.insertComponentIntoSplitPane(extra);
		assertEquals(JSplitPane.VERTICAL_SPLIT, vertical.getOrientation());
		frame.setProperty("split_pane_position", "250");
		frame.setProperty("split_pane_last_position", "180");
		invoke("setSplitLocation");
		assertEquals(250, vertical.getDividerLocation());
		assertEquals(180, vertical.getLastDividerLocation());
		vertical.setDividerLocation(0);
		SwingUtilities.invokeAndWait(() -> {
		});
		assertEquals(250, vertical.getDividerLocation());
		JSplitPane again = frame.insertComponentIntoSplitPane(new JPanel());
		assertSame(vertical, again);
		frame.removeSplitPane();
		assertTrue(frame.getIntProperty("split_pane_position", -1) > 0);
		invoke("setSplitLocation");

		frame.setProperty(FreeMind.J_SPLIT_PANE_SPLIT_TYPE, FreeMind.HORIZONTAL_SPLIT_RIGHT);
		JSplitPane horizontal = frame.insertComponentIntoSplitPane(new JPanel());
		assertEquals(JSplitPane.HORIZONTAL_SPLIT, horizontal.getOrientation());
		frame.removeSplitPane();

		Field tabbed = FreeMind.class.getDeclaredField("mTabbedPane");
		tabbed.setAccessible(true);
		Object savedTab = tabbed.get(frame);
		JComponent content = frame.getContentComponent();
		Container contentParent = content.getParent();
		if (contentParent instanceof JTabbedPane) {
			JTabbedPane pane = (JTabbedPane) contentParent;
			int index = pane.indexOfComponent(content);
			if (index >= 0) {
				pane.setComponentAt(index, new JPanel());
			}
		}
		tabbed.set(frame, null);
		try {
			invoke("setContentComponent");
			assertSame(frame.getContentComponent(),
					((BorderLayout) frame.getContentPane().getLayout()).getLayoutComponent(BorderLayout.CENTER));
			invoke("removeContentComponent");
			assertNull(((BorderLayout) frame.getContentPane().getLayout()).getLayoutComponent(BorderLayout.CENTER));
		} finally {
			tabbed.set(frame, savedTab);
			invoke("setContentComponent");
		}

		AtomicInteger done = new AtomicInteger();
		StartupDoneListener listener = done::incrementAndGet;
		frame.registerStartupDoneListener(listener);
		invoke("fireStartupDone");
		SwingUtilities.invokeAndWait(() -> {
		});
		assertEquals(1, done.get());
		frame.registerStartupDoneListener(() -> done.incrementAndGet());
		invoke("fireStartupDone");
		SwingUtilities.invokeAndWait(() -> {
		});
		assertEquals(2, done.get());

		Class<?> focusType = Class.forName("freemind.main.FreeMind$2");
		Constructor<?> constructor = focusType.getDeclaredConstructor(FreeMind.class);
		constructor.setAccessible(true);
		WindowFocusListener listener2 = (WindowFocusListener) constructor.newInstance(frame);
		frame.addWindowFocusListener(listener2);
		int focusListeners = frame.getWindowFocusListeners().length;
		listener2.windowLostFocus(new WindowEvent(frame, WindowEvent.WINDOW_LOST_FOCUS));
		listener2.windowGainedFocus(new WindowEvent(frame, WindowEvent.WINDOW_GAINED_FOCUS));
		assertEquals(focusListeners - 1, frame.getWindowFocusListeners().length);

		frame.saveProperties(true);
		String stored = Files.readString(autoFile.toPath());
		assertTrue(stored.contains("FreeMind"));
		Field server = FreeMind.class.getDeclaredField("mEditServer");
		server.setAccessible(true);
		assertNull(server.get(frame));
	}

	@Test
	@Order(4)
	void portFileProxyAndADeadInstanceDoNotExit() throws Exception {
		// backwards compatibility
		frame.setProperty(FreeMind.RESOURCES_DON_T_OPEN_PORT, "true");
		assertNull(invoke("getPortFile"));
		invoke("checkForAnotherInstance", new Class<?>[] { String[].class }, (Object) new String[0]);
		frame.setProperty(FreeMind.RESOURCES_DON_T_OPEN_PORT, "false");
		String portFile = (String) invoke("getPortFile");
		assertEquals(new File(frame.getFreemindDirectory(), "coverage.port").getPath(), portFile);
		invoke("checkForAnotherInstance", new Class<?>[] { String[].class }, (Object) new String[0]);
		try (OutputStreamWriter writer = new OutputStreamWriter(
				Files.newOutputStream(new File(portFile).toPath()), StandardCharsets.UTF_8)) {
			writer.write("not-a-port-file\n");
		}
		invoke("checkForAnotherInstance", new Class<?>[] { String[].class }, (Object) new String[] { "map.mm" });
		assertTrue(new File(portFile).exists());

		String previousHost = System.getProperty("http.proxyHost");
		String previousPort = System.getProperty("http.proxyPort");
		String previousHttpsHost = System.getProperty("https.proxyHost");
		String previousHttpsPort = System.getProperty("https.proxyPort");
		String previousExceptions = System.getProperty("http.nonProxyHosts");
		try {
			frame.setProperty(FreeMind.PROXY_USE_SETTINGS, "true");
			frame.setProperty(FreeMind.PROXY_IS_AUTHENTICATED, "true");
			frame.setProperty(FreeMind.PROXY_USER, "coverage-user");
			frame.setProperty(FreeMind.PROXY_PASSWORD, Tools.compress("secret"));
			frame.setProperty(FreeMind.PROXY_HOST, "127.0.0.1");
			frame.setProperty(FreeMind.PROXY_PORT, "9");
			frame.setProperty(FreeMind.PROXY_EXCEPTION, "localhost");
			invoke("setupProxy");
			assertEquals("127.0.0.1", System.getProperty("http.proxyHost"));
			assertEquals("9", System.getProperty("http.proxyPort"));
			assertEquals("127.0.0.1", System.getProperty("https.proxyHost"));
			assertEquals("localhost", System.getProperty("http.nonProxyHosts"));
		} finally {
			restoreProperty("http.proxyHost", previousHost);
			restoreProperty("http.proxyPort", previousPort);
			restoreProperty("https.proxyHost", previousHttpsHost);
			restoreProperty("https.proxyPort", previousHttpsPort);
			restoreProperty("http.nonProxyHosts", previousExceptions);
			Authenticator.setDefault(null);
			frame.setProperty(FreeMind.PROXY_USE_SETTINGS, "false");
		}

		boolean browse = Desktop.isDesktopSupported()
				&& Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
		if (!browse) {
			URL url = new URL("file:///tmp/coverage-not-browsed");
			frame.openDocument(url);
			assertEquals("file", url.getProtocol());
		} else {
			assertTrue(browse);
		}
	}

	private static Object invoke(String name, Class<?>[] types, Object... args) throws Exception {
		Method method = FreeMind.class.getDeclaredMethod(name, types);
		method.setAccessible(true);
		return method.invoke(frame, args);
	}

	private static Object invoke(String name) throws Exception {
		return invoke(name, new Class<?>[0]);
	}

	private static void restoreProperty(String key, String value) {
		if (value == null) {
			System.clearProperty(key);
		} else {
			System.setProperty(key, value);
		}
	}

	private static void deleteTree(File file) {
		File[] children = file.listFiles();
		if (children != null) {
			for (File child : children) {
				deleteTree(child);
			}
		}
		file.delete();
	}

	private static final class CountingFeedBack implements FeedBack {
		private int increases;

		@Override
		public void progress(int act, String messageId, Object[] parameters) {
		}

		@Override
		public int getActualValue() {
			return increases;
		}

		@Override
		public void setMaximumValue(int max) {
		}

		@Override
		public void increase(String messageId, Object[] parameters) {
			increases++;
		}
	}
}
