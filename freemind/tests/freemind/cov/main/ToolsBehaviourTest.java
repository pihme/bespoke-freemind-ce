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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.IllegalComponentStateException;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.print.Paper;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIDefaults;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import freemind.controller.MindMapNodesSelection;
import freemind.main.FreeMind;
import freemind.main.FreeMindStarter;
import freemind.main.Resources;
import freemind.main.Tools;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.MindMapController;
import freemind.view.mindmapview.NodeView;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

class ToolsBehaviourTest {

	@Test
	void xmlColoursPointsAndDatesRoundTrip() {
		// backwards compatibility
		assertNull(Tools.xmlToColor(null));
		assertEquals(new Color(1, 2, 3), Tools.xmlToColor("#010203"));
		assertThrows(IllegalArgumentException.class, () -> Tools.xmlToColor("red"));

		assertNull(Tools.xmlToPoint(null));
		assertEquals(new Point(3, -4), Tools.xmlToPoint("java.awt.Point[x=3,y=-4]"));
		assertEquals(new Point(1, 2), Tools.xmlToPoint("1;2"));
		assertThrows(IllegalArgumentException.class, () -> Tools.xmlToPoint("1"));

		assertEquals(1000L, Tools.xmlToDate("1000").getTime());
		long before = System.currentTimeMillis();
		long parsed = Tools.xmlToDate("not-a-long").getTime();
		assertTrue(parsed >= before - 2000L);
		assertTrue(parsed <= System.currentTimeMillis() + 2000L);
	}

	@Test
	void pathsFollowTheOperatingSystemAndRelativeLinks() throws Exception {
		// backwards compatibility
		String osName = System.getProperty("os.name");
		String previousLinks = Resources.getInstance().getProperty("links");
		try {
			System.setProperty("os.name", "Windows 10");
			assertTrue(Tools.isWindows());
			assertTrue(Tools.isAbsolutePath("C:maps"));
			assertTrue(Tools.isAbsolutePath("/maps"));
			assertFalse(Tools.isAbsolutePath("maps"));
			assertEquals("C:\\", Tools.getPrefix("C:\\maps\\a.mm"));
			assertNull(Tools.getPrefix("maps"));
			URL windowsFile = new URL("file:/C:/maps/a.mm");
			assertEquals("C:\\maps\\a.mm", Tools.urlGetFile(windowsFile));
			assertEquals(new URL("file:/D:/other/b.mm").toString(), Tools.toRelativeURL(
					new URL("file:/C:/maps/a.mm"), new URL("file:/D:/other/b.mm")));

			System.setProperty("os.name", "Mac OS X");
			assertTrue(Tools.isMacOsX());
			assertTrue(Tools.isAbsolutePath("/maps"));
			assertFalse(Tools.isAbsolutePath("maps"));
			assertEquals(File.separator, Tools.getPrefix(File.separator + "maps"));

			System.setProperty("os.name", osName);
			assertFalse(Tools.isMacOsX());
			assertTrue(Tools.isUnix());
			assertTrue(Tools.isAboveJava4());
			assertFalse(Tools.isBelowJava6());
			assertEquals("http://other.test/x", Tools.toRelativeURL(null, new URL("http://other.test/x")));
			assertEquals("https://example.test/a/d/e.html", Tools.toRelativeURL(
					new URL("http://example.test/a/b/c.html"),
					new URL("https://example.test/a/d/e.html")));
			assertEquals("../d/e.html", Tools.toRelativeURL(
					new URL("http://example.test/a/b/c.html"),
					new URL("http://example.test/a/d/e.html")));

			File dir = Files.createTempDirectory("cov-links").toFile();
			File map = new File(dir, "map.mm");
			File other = new File(dir, "other.mm");
			assertTrue(map.createNewFile());
			assertTrue(other.createNewFile());
			frame().setProperty("links", "absolute");
			assertEquals(Tools.fileToUrl(other).toString(), Tools.fileToRelativeUrlString(other, map));
			frame().setProperty("links", "relative");
			assertEquals("other.mm", Tools.fileToRelativeUrlString(other, map));
			assertEquals(other.getCanonicalFile(), Tools.urlToFile(other.toURI().toURL()).getCanonicalFile());
			other.delete();
			map.delete();
			dir.delete();
		} finally {
			System.setProperty("os.name", osName);
			if (previousLinks == null) {
				frame().getProperties().remove("links");
			} else {
				frame().setProperty("links", previousLinks);
			}
		}
	}

	@Test
	void textCompareLabelsFontsAndStackTraces() throws Exception {
		// backwards compatibility
		assertTrue(Tools.compareText(null, "a").startsWith("One of the Strings is null"));
		assertTrue(Tools.compareText("abcd", "ab").contains("First string is longer :cd"));
		assertTrue(Tools.compareText("ab", "abcd").contains("Second string is longer :cd"));
		assertTrue(Tools.compareText("ab", "ax").contains("Difference at 1: b!=x"));

		Action action = new AbstractAction("unused") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent event) {
			}
		};
		Tools.setLabelAndMnemonic(action, "&Save");
		assertEquals("Save", action.getValue(Action.NAME));
		assertEquals(Integer.valueOf('S'), action.getValue(Action.MNEMONIC_KEY));
		Tools.setLabelAndMnemonic(action, "&save");
		assertEquals(Integer.valueOf('S'), action.getValue(Action.MNEMONIC_KEY));
		action.putValue(Action.MNEMONIC_KEY, null);
		Tools.setLabelAndMnemonic(action, "& ");
		assertEquals("& ", action.getValue(Action.NAME));
		assertNull(action.getValue(Action.MNEMONIC_KEY));
		action.putValue(Action.NAME, "&Open");
		Tools.setLabelAndMnemonic(action, null);
		assertEquals("Open", action.getValue(Action.NAME));
		assertEquals(Integer.valueOf('O'), action.getValue(Action.MNEMONIC_KEY));

		JButton button = new JButton("plain");
		Tools.setLabelAndMnemonic(button, "&About");
		assertEquals("About", button.getText());
		assertEquals('A', (char) button.getMnemonic());
		assertEquals(0, button.getDisplayedMnemonicIndex());

		String osName = System.getProperty("os.name");
		try {
			System.setProperty("os.name", "Mac OS X");
			JButton mac = new JButton("plain");
			Tools.setLabelAndMnemonic(mac, "&About");
			assertEquals("About", mac.getText());
			assertEquals(0, mac.getMnemonic());
		} finally {
			System.setProperty("os.name", osName);
		}

		assertTrue(Tools.getStackTrace().contains("HERE"));
		assertTrue(Tools.getStacktrace(new IllegalArgumentException("HERE")).contains("HERE"));
		Logger resourcesLogger = resourcesLogger();
		List<String> messages = new ArrayList<>();
		Handler handler = new Handler() {
			@Override
			public void publish(LogRecord record) {
				if (record.getThrown() != null) {
					messages.add(record.getThrown().getMessage());
				}
			}

			@Override
			public void flush() {
			}

			@Override
			public void close() {
			}
		};
		handler.setLevel(Level.ALL);
		resourcesLogger.addHandler(handler);
		try {
			Tools.printStackTrace();
		} finally {
			resourcesLogger.removeHandler(handler);
		}
		assertTrue(messages.contains("HERE"));

		UIDefaults defaults = UIManager.getLookAndFeelDefaults();
		Map<Object, Object> snapshot = new java.util.HashMap<>();
		for (Object key : new ArrayList<>(defaults.keySet())) {
			if (key instanceof String && ((String) key).endsWith(".font")) {
				snapshot.put(key, defaults.get(key));
			}
		}
		defaults.put("CoverageProbe.font", new Font(Font.DIALOG, Font.PLAIN, 11));
		try {
			Tools.scaleAllFonts(2f);
			assertEquals(22f, UIManager.getFont("CoverageProbe.font").getSize2D(), 0.01f);
		} finally {
			for (Map.Entry<Object, Object> entry : snapshot.entrySet()) {
				defaults.put(entry.getKey(), entry.getValue());
				UIManager.put(entry.getKey(), entry.getValue());
			}
			defaults.remove("CoverageProbe.font");
		}

		assertThrows(HeadlessException.class, Tools::isRetina);
		assertFalse(Tools.getFreeMindBasePath().isEmpty());
		String base = URLDecoder.decode(
				FreeMindStarter.class.getProtectionDomain().getCodeSource().getLocation().getPath(),
				StandardCharsets.UTF_8);
		if (!base.endsWith(Tools.CONTENTS_JAVA_FREEMIND_JAR)
				&& !base.endsWith(Tools.FREEMIND_LIB_FREEMIND_JAR)) {
			assertEquals(base, Tools.getFreeMindBasePath());
		}
		InetAddress local = localHost();
		if (local == null) {
			assertNull(Tools.getHostIpAsString());
		} else {
			assertEquals(local.getHostAddress(), Tools.getHostIpAsString());
		}
		assertEquals("héllo", Tools.byteArrayToUTF8String(Tools.uTF8StringToByteArray("héllo")));
	}

	@Test
	void readersPageFormatAndHiddenFlagsStayLocal() throws Exception {
		// backwards compatibility
		assertEquals("abcdef", Tools.readFileStart(new StringReader("abcdef\ngh"), 3).toString());
		assertEquals("abcd", Tools.readFileStart(new StringReader("ab\ncd"), 10).toString());
		assertEquals(0, Tools.readFileStart(throwingReader(), 4).length());
		assertNull(Tools.getFile(throwingReader()));
		File missing = new File(Files.createTempDirectory("cov-missing").toFile(), "absent.txt");
		assertNull(Tools.getFile(missing));
		File present = File.createTempFile("cov-read", ".txt");
		Files.writeString(present.toPath(), "line-one\nline-two");
		assertTrue(Tools.getFile(present).contains("line-one"));
		assertTrue(Tools.getFile(present).contains("line-two"));

		Tools.FileReaderCreator creator = new Tools.FileReaderCreator(present);
		assertEquals(present.getName(), creator.toString());
		assertTrue(Tools.getFile(creator.createReader()).contains("line-one"));
		StringBuilder tooLong = new StringBuilder(present.getParent());
		for (int i = 0; i < 400; i++) {
			tooLong.append("/abcdefghij");
		}
		assertThrows(IllegalArgumentException.class,
				() -> new Tools.FileReaderCreator(new File(tooLong.toString())));

		Paper untouched = new Paper();
		double width = untouched.getWidth();
		Tools.setPageFormatFromString(untouched, "1;2");
		assertEquals(width, untouched.getWidth(), 0.001);
		Paper paper = new Paper();
		Tools.setPageFormatFromString(paper, "100;200;1;2;80;bad");
		assertEquals(100d, paper.getWidth(), 0.001);
		assertEquals(200d, paper.getHeight(), 0.001);
		assertEquals(1d, paper.getImageableX(), 0.001);
		assertEquals(2d, paper.getImageableY(), 0.001);
		assertEquals(80d, paper.getImageableWidth(), 0.001);
		assertEquals(0d, paper.getImageableHeight(), 0.001);

		File hiddenCandidate = File.createTempFile("cov-hidden", ".txt");
		Tools.setHidden(hiddenCandidate, true, true);
		assertTrue(hiddenCandidate.exists());
		assertFalse(hiddenCandidate.isHidden());
		Tools.makeFileHidden(hiddenCandidate, true);
		assertFalse(hiddenCandidate.isHidden());
		Tools.setPermissions(hiddenCandidate.getAbsolutePath(), 0);
		assertTrue(hiddenCandidate.canRead());
		hiddenCandidate.delete();
		present.delete();
		missing.getParentFile().delete();

		String missingUpdate = Tools.getFile(Tools.getUpdateReader(new StringReader("short"),
				"missing-coverage.xslt"));
		assertTrue(missingUpdate.contains("not found"), missingUpdate);
		String map = "<map version=\"1.0.1\"><node TEXT=\"CoverageNode\"/></map>";
		String updated = Tools.getFile(Tools.getUpdateReader(new StringReader(map),
				"freemind/modes/mindmapmode/freemind_version_updater.xslt"));
		assertTrue(updated.contains("CoverageNode"), updated);
	}

	@Test
	void eventQueueWaitsOffTheDispatchThreadAndWarnsOnIt() throws Exception {
		// backwards compatibility
		AtomicBoolean ran = new AtomicBoolean();
		EventQueue.invokeLater(() -> ran.set(true));
		Tools.waitForEventQueue();
		assertTrue(ran.get());

		Logger toolsLogger = Logger.getLogger("Tools");
		List<String> warnings = new ArrayList<>();
		Handler handler = new Handler() {
			@Override
			public void publish(LogRecord record) {
				warnings.add(record.getMessage());
			}

			@Override
			public void flush() {
			}

			@Override
			public void close() {
			}
		};
		handler.setLevel(Level.ALL);
		Level previous = toolsLogger.getLevel();
		toolsLogger.setLevel(Level.ALL);
		toolsLogger.addHandler(handler);
		try {
			SwingUtilities.invokeAndWait(Tools::waitForEventQueue);
		} finally {
			toolsLogger.removeHandler(handler);
			toolsLogger.setLevel(previous);
		}
		assertTrue(warnings.stream().anyMatch(message -> message != null && message.contains("Can't wait")));

		JPanel ancestor = new JPanel();
		ancestor.setLayout(null);
		JPanel child = new JPanel();
		child.setLocation(4, 6);
		ancestor.add(child);
		Point point = new Point(1, 2);
		Tools.convertPointToAncestor(child, point, JPanel.class);
		assertEquals(new Point(5, 8), point);
	}

	@Tag("gui")
	@Test
	void retinaFollowsTheScreenScale() {
		// backwards compatibility
		assertEquals(retinaScaleIsTwo(), Tools.isRetina());
	}

	@Tag("gui")
	@Test
	void dialogSitsBesideTheComponentOnTheScreen() throws Exception {
		// backwards compatibility
		GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice().getDefaultConfiguration();
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
		PlacedFrame owner = new PlacedFrame(new Point(0, 0), screen.width, screen.height, gc);
		PlacedDialog dialog = new PlacedDialog(owner, 40, 30, gc);
		dialog.setLocation(3, 4);
		Tools.setDialogLocationRelativeTo(dialog, null);
		assertEquals(new Point(3, 4), dialog.getLocation());

		int minX = Math.max(0, insets.left);
		int minY = Math.max(0, insets.top);
		int maxX = Math.min(screen.width, screen.width - insets.right);
		int maxY = Math.min(screen.height, screen.height - insets.bottom);
		int besideX = minX + 50;
		int besideY = minY + 50;
		Tools.setDialogLocationRelativeTo(dialog, new PlacedPanel(new Point(besideX, besideY), 10, 10, gc));
		assertEquals(new Point(besideX + 10, besideY + 10), dialog.getLocation());

		Tools.setDialogLocationRelativeTo(dialog, new PlacedPanel(new Point(-1000, 10), 10, 10, gc));
		assertEquals(minX, dialog.getLocation().x);
		Tools.setDialogLocationRelativeTo(dialog, new PlacedPanel(new Point(10, -1000), 10, 10, gc));
		assertEquals(minY, dialog.getLocation().y);
		Tools.setDialogLocationRelativeTo(dialog,
				new PlacedPanel(new Point(screen.width + 500, 10), 10, 10, gc));
		assertEquals(maxX - 40, dialog.getLocation().x);
		Tools.setDialogLocationRelativeTo(dialog,
				new PlacedPanel(new Point(10, screen.height + 500), 10, 10, gc));
		assertEquals(maxY - 30, dialog.getLocation().y);

		Properties previousProps = FreeMind.props;
		if (previousProps == null) {
			FreeMind.props = new Properties();
		}
		try {
			Opened opened = MapFixture.open("Child");
			NodeView nodeView = opened.view.getNodeView(opened.child("Child"));
			assertNotNull(nodeView);
			assertThrows(IllegalComponentStateException.class,
					() -> Tools.setDialogLocationRelativeTo(dialog, nodeView));
		} finally {
			FreeMind.props = previousProps;
		}
	}

	@Test
	void clipboardReturnsTheCopiedNode() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapController mode = opened.mode;
		Clipboard fake = new Clipboard("coverage");
		Field clipboard = MindMapController.class.getDeclaredField("clipboard");
		clipboard.setAccessible(true);
		clipboard.set(mode, fake);
		fake.setContents(new MindMapNodesSelection(null, null, "text", null, null, null, null, null), null);
		assertTrue(Tools.getMindMapNodesFromClipboard(mode).isEmpty());
		MindMapNode child = opened.child("Child");
		String id = child.getObjectId(mode);
		fake.setContents(new MindMapNodesSelection(null, null, "text", null, null, null, null,
				List.of(id)), null);
		java.util.Vector<MindMapNode> nodes = Tools.getMindMapNodesFromClipboard(mode);
		assertEquals(1, nodes.size());
		assertEquals("Child", nodes.get(0).getText());
	}

	@Test
	void singleDesRoundTripAndABrokenAlgorithm() throws Exception {
		// backwards compatibility
		Tools.DesEncrypter broken = new Tools.DesEncrypter(new StringBuffer("pw"), "NoSuchCipher");
		Method init = Tools.DesEncrypter.class.getDeclaredMethod("init", byte[].class, boolean.class);
		init.setAccessible(true);
		init.invoke(broken, new Object[] { null, Boolean.TRUE });
		assertThrows(NullPointerException.class, () -> broken.encrypt("x"));
		assertNull(broken.decrypt(null));

		Tools.SingleDesEncrypter encrypter = new Tools.SingleDesEncrypter(new StringBuffer("secret"));
		init.invoke(encrypter, new Object[] { null, Boolean.FALSE });
		assertNull(encrypter.decrypt("zzzz"));
		String cipher = encrypter.encrypt("hello");
		assertNotNull(cipher);
		assertTrue(cipher.contains(" "));
		assertEquals("hello", encrypter.decrypt(cipher));
		assertNotNull(new Tools.TripleDesEncrypter(new StringBuffer("secret")));
		assertEquals("héllo", Tools.decompress(Tools.compress("héllo")));
	}

	private static Resources frameHolder() {
		return Resources.getInstance();
	}

	private static freemind.main.FreeMindMain frame() throws Exception {
		Field main = Resources.class.getDeclaredField("main");
		main.setAccessible(true);
		return (freemind.main.FreeMindMain) main.get(frameHolder());
	}

	private static Logger resourcesLogger() throws Exception {
		Field logger = Resources.class.getDeclaredField("logger");
		logger.setAccessible(true);
		return (Logger) logger.get(Resources.getInstance());
	}

	private static Reader throwingReader() {
		return new Reader() {
			@Override
			public int read(char[] cbuf, int off, int len) throws IOException {
				throw new IOException("nope");
			}

			@Override
			public void close() throws IOException {
				throw new IOException("close");
			}
		};
	}

	private static InetAddress localHost() {
		try {
			return InetAddress.getLocalHost();
		} catch (UnknownHostException ex) {
			return null;
		}
	}

	private static boolean retinaScaleIsTwo() {
		GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		try {
			Field field = device.getClass().getDeclaredField("scale");
			field.setAccessible(true);
			Object scale = field.get(device);
			return scale instanceof Integer && ((Integer) scale).intValue() == 2;
		} catch (Exception ex) {
			return false;
		}
	}

	private static final class PlacedFrame extends JFrame {
		private static final long serialVersionUID = 1L;
		private final Point where;
		private final int width;
		private final int height;
		private final GraphicsConfiguration gc;

		PlacedFrame(Point where, int width, int height, GraphicsConfiguration gc) {
			this.where = where;
			this.width = width;
			this.height = height;
			this.gc = gc;
		}

		@Override
		public Point getLocationOnScreen() {
			return new Point(where);
		}

		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public GraphicsConfiguration getGraphicsConfiguration() {
			return gc;
		}
	}

	private static final class PlacedDialog extends JDialog {
		private static final long serialVersionUID = 1L;
		private final int width;
		private final int height;
		private final GraphicsConfiguration gc;

		PlacedDialog(JFrame owner, int width, int height, GraphicsConfiguration gc) {
			super(owner);
			this.width = width;
			this.height = height;
			this.gc = gc;
		}

		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public GraphicsConfiguration getGraphicsConfiguration() {
			return gc;
		}
	}

	private static final class PlacedPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private final Point where;
		private final int width;
		private final int height;
		private final GraphicsConfiguration gc;

		PlacedPanel(Point where, int width, int height, GraphicsConfiguration gc) {
			this.where = where;
			this.width = width;
			this.height = height;
			this.gc = gc;
		}

		@Override
		public Point getLocationOnScreen() {
			return new Point(where);
		}

		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public GraphicsConfiguration getGraphicsConfiguration() {
			return gc;
		}
	}

}
