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

package tests.freemind.requirements;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.tree.MutableTreeNode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import accessories.plugins.SortNodes;
import freemind.controller.actions.generated.instance.EditNodeAction;
import freemind.main.HeadlessFreeMind;
import freemind.main.Tools;
import freemind.modes.ExtendedMapFeedbackImpl;
import freemind.modes.MapAdapter;
import freemind.modes.MindMapNode;
import freemind.modes.NodeAdapter;
import freemind.modes.mindmapmode.MindMapMapModel;
import freemind.modes.mindmapmode.MindMapNodeModel;
import freemind.modes.mindmapmode.actions.xml.ActionFilter;
import freemind.modes.mindmapmode.actions.xml.ActionPair;
import freemind.view.mindmapview.IndependantMapViewCreator;
import freemind.view.mindmapview.MapView;
import freemind.view.mindmapview.NodeView;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Requirement tests whose assertions check behaviour the older suite left open.
 * Each method name carries the requirement id.
 */
@DisplayName("RequirementBehaviourTest")
class RequirementBehaviourTest {

	private static final String BRANCH_MAP = "<map version=\"1.0.0\">"
			+ "<node TEXT=\"ROOT\">"
			+ "<node TEXT=\"Branch\" POSITION=\"right\">"
			+ "<node TEXT=\"Leaf\"/>"
			+ "</node>"
			+ "<node TEXT=\"Other\" POSITION=\"right\"/>"
			+ "</node></map>";

	@Test
	void TestREQ_NODE_03_UndoRedoUsesXmlActionPair() throws Exception {
		ExtendedMapFeedbackImpl feedback = new ExtendedMapFeedbackImpl();
		MindMapMapModel map = new MindMapMapModel(feedback);
		feedback.setMap(map);
		MindMapNode root = map.loadTree(new Tools.StringReaderCreator(BRANCH_MAP),
				MapAdapter.sDontAskInstance);
		map.setRoot(root);
		MindMapNode branch = (MindMapNode) root.getChildAt(0);
		String original = branch.getText();
		ActionPair[] stored = new ActionPair[1];
		feedback.getActionRegistry().registerFilter(new ActionFilter() {
			public ActionPair filterAction(ActionPair pair) {
				stored[0] = pair;
				return pair;
			}
		});

		feedback.setNodeText(branch, "Edited");

		assertNotNull(stored[0]);
		assertTrue(stored[0].getDoAction() instanceof EditNodeAction);
		assertTrue(stored[0].getUndoAction() instanceof EditNodeAction);
		EditNodeAction done = (EditNodeAction) stored[0].getDoAction();
		EditNodeAction undone = (EditNodeAction) stored[0].getUndoAction();
		assertEquals("Edited", done.getText());
		assertEquals(original, undone.getText());
		assertEquals(done.getNode(), undone.getNode());
		assertEquals("Edited", branch.getText());

		feedback.doTransaction("Undo", stored[0].reverse());
		assertEquals(original, branch.getText());

		feedback.doTransaction("Redo", new ActionPair(done, undone));
		assertEquals("Edited", branch.getText());
	}

	@Test
	void TestREQ_NODE_05_SelectAllAndBranch() throws Exception {
		MapView view = mapView(writeMap("select"));
		NodeView rootView = view.getRoot();
		assertFalse(rootView.getChildrenViews().isEmpty());
		int everyNode = countViews(rootView);

		view.selectBranch(rootView, false);

		assertEquals(everyNode, view.getSelecteds().size());
		assertTrue(view.isSelected(rootView));
		assertTrue(selectedTexts(view).contains("Leaf"));
		assertTrue(selectedTexts(view).contains("Other"));

		NodeView branchView = childNamed(rootView, "Branch");
		view.selectBranch(branchView, false);

		assertEquals(countViews(branchView), view.getSelecteds().size());
		assertTrue(view.isSelected(branchView));
		assertTrue(selectedTexts(view).contains("Leaf"));
		assertFalse(view.isSelected(rootView));
		assertFalse(selectedTexts(view).contains("Other"));
	}

	@Test
	void TestREQ_NODE_09_SortsChildrenByPlainText() throws Exception {
		ExtendedMapFeedbackImpl feedback = new ExtendedMapFeedbackImpl();
		MindMapMapModel map = new MindMapMapModel(feedback);
		feedback.setMap(map);
		SortNodes sorter = new SortNodes();
		Class<?> type = Class.forName("accessories.plugins.SortNodes$NodeTextComparator");
		var constructor = type.getDeclaredConstructor(SortNodes.class);
		constructor.setAccessible(true);
		@SuppressWarnings("unchecked")
		Comparator<MindMapNode> comparator = (Comparator<MindMapNode>) constructor.newInstance(sorter);
		MindMapNode apple = new MindMapNodeModel("apple", map);
		MindMapNode zebra = new MindMapNodeModel("Zebra", map);
		MindMapNode htmlBeta = new MindMapNodeModel("<html><body>beta</body></html>", map);

		assertTrue(comparator.compare(apple, zebra) < 0);
		assertTrue(comparator.compare(zebra, apple) > 0);
		assertEquals(0, comparator.compare(new MindMapNodeModel("Apple", map), apple));
		assertTrue(comparator.compare(apple, htmlBeta) < 0);
		assertTrue(comparator.compare(htmlBeta, zebra) < 0);

		var reverse = type.getDeclaredMethod("setNegative");
		reverse.setAccessible(true);
		reverse.invoke(comparator);
		assertTrue(comparator.compare(apple, zebra) > 0);
	}

	@Test
	void TestREQ_STYLE_08_ZoomScalesTheMap() throws Exception {
		MapView view = mapView(writeMap("zoom"));
		float before = view.getZoom();

		view.setZoom(1.5f);

		assertEquals(1.5f, view.getZoom());
		assertEquals(150, view.getZoomed(100));
		assertNotEquals(before, view.getZoom());
		view.setZoom(1f);
		assertEquals(100, view.getZoomed(100));
	}

	@Test
	void TestREQ_FILE_03_PngExportWritesPngBytes() throws Exception {
		System.setProperty("java.awt.headless", "true");
		Path png = Files.createTempFile("freemind-export", ".png");
		try {
			new FontedMapCreator().exportFileToPng(
					writeMap("png").getPath(), png.toString(), new HeadlessFreeMind());
			byte[] bytes = Files.readAllBytes(png);
			assertTrue(bytes.length > 8);
			assertEquals((byte) 0x89, bytes[0]);
			assertEquals((byte) 'P', bytes[1]);
			assertEquals((byte) 'N', bytes[2]);
			assertEquals((byte) 'G', bytes[3]);
			BufferedImage image = ImageIO.read(png.toFile());
			assertNotNull(image);
			assertTrue(image.getWidth() > 0);
			assertTrue(image.getHeight() > 0);
			assertTrue(hasNonWhitePixel(image));
		} finally {
			Files.deleteIfExists(png);
		}
	}

	@Test
	void TestREQ_FILE_05_BranchBecomesOwnMap() throws Exception {
		ExtendedMapFeedbackImpl feedback = new ExtendedMapFeedbackImpl();
		MindMapMapModel map = new MindMapMapModel(feedback);
		feedback.setMap(map);
		MindMapNode root = map.loadTree(new Tools.StringReaderCreator(BRANCH_MAP),
				MapAdapter.sDontAskInstance);
		map.setRoot(root);
		MindMapNode branch = (MindMapNode) root.getChildAt(0);
		((NodeAdapter) root).remove((MutableTreeNode) branch);
		assertTrue(branch.getParentNode() == null);
		branch.setFolded(false);

		ExtendedMapFeedbackImpl branchFeedback = new ExtendedMapFeedbackImpl();
		MindMapMapModel branchMap = new MindMapMapModel((MindMapNodeModel) branch, branchFeedback);
		branchFeedback.setMap(branchMap);
		File out = File.createTempFile("branch-export", ".mm");
		try {
			assertTrue(branchMap.save(out));
			ExtendedMapFeedbackImpl loadedFeedback = new ExtendedMapFeedbackImpl();
			MindMapMapModel loaded = new MindMapMapModel(loadedFeedback);
			loadedFeedback.setMap(loaded);
			MindMapNode loadedRoot = loaded.loadTree(new Tools.FileReaderCreator(out),
					MapAdapter.sDontAskInstance);
			loaded.setRoot(loadedRoot);
			assertEquals("Branch", loadedRoot.getText());
			assertEquals(1, loadedRoot.getChildCount());
			assertEquals("Leaf", ((MindMapNode) loadedRoot.getChildAt(0)).getText());
			assertFalse(texts(loadedRoot).contains("ROOT"));
			assertFalse(texts(loadedRoot).contains("Other"));
		} finally {
			out.delete();
		}
	}

	@Test
	void TestREQ_APP_01_PreferenceStoreChanges() {
		HeadlessFreeMind mind = new HeadlessFreeMind();
		String key = "bespoke.coverage.preference";
		assertTrue(mind.getProperty(key) == null);
		mind.setProperty(key, "changed");
		assertEquals("changed", mind.getProperty(key));
		mind.setProperty(key, "again");
		assertEquals("again", mind.getProperty(key));
	}

	@Test
	void TestREQ_APP_03_AtLeastThirtyLanguages() throws Exception {
		Path dir = moduleDir();
		long count;
		try (Stream<Path> files = Files.list(dir)) {
			count = files.filter(path -> {
				String name = path.getFileName().toString();
				return name.startsWith("Resources_") && name.endsWith(".properties");
			}).count();
		}
		assertTrue(count >= 30, "language files: " + count);
		Properties english = loadProperties(dir.resolve("Resources_en.properties"));
		Properties german = loadProperties(dir.resolve("Resources_de.properties"));
		assertEquals("About", english.getProperty("about").trim());
		assertNotEquals(english.getProperty("about").trim(), german.getProperty("about").trim());
	}

	@Test
	void TestREQ_APP_04_FlatLafLightAndDark() throws Exception {
		LookAndFeel previous = UIManager.getLookAndFeel();
		try {
			FlatLightLaf.setup();
			assertTrue(UIManager.getLookAndFeel().getClass().getName().contains("FlatLightLaf"));
			FlatDarkLaf.setup();
			assertTrue(UIManager.getLookAndFeel().getClass().getName().contains("FlatDarkLaf"));
		} finally {
			if (previous != null) {
				UIManager.setLookAndFeel(previous);
			}
		}
	}

	@Test
	void TestREQ_APP_05_Java21AndThreePackages() throws Exception {
		String build = Files.readString(moduleDir().resolve("build.gradle.kts"));
		assertTrue(build.contains("languageVersion = JavaLanguageVersion.of(21)"));
		assertTrue(build.contains("tasks.register<Exec>(\"jpackageMac\")"));
		assertTrue(build.contains("tasks.register<Exec>(\"jpackageWin\")"));
		assertTrue(build.contains("tasks.register<Exec>(\"jpackageLinux\")"));
		assertTrue(build.contains("freemind.main.FreeMindStarter"));
	}

	private static MapView mapView(File mapFile) throws Exception {
		JPanel parent = new JPanel();
		parent.setBounds(new Rectangle(0, 0, 400, 600));
		MapView view = new IndependantMapViewCreator().createMapViewForFile(
				mapFile.getPath(), parent, new HeadlessFreeMind());
		view.getRoot().getMainView().doLayout();
		return view;
	}

	private static File writeMap(String prefix) throws IOException {
		File file = File.createTempFile(prefix, ".mm");
		file.deleteOnExit();
		Files.writeString(file.toPath(), BRANCH_MAP);
		return file;
	}

	private static int countViews(NodeView node) {
		int count = 1;
		for (NodeView child : node.getChildrenViews()) {
			count += countViews(child);
		}
		return count;
	}

	private static NodeView childNamed(NodeView parent, String text) {
		for (NodeView child : parent.getChildrenViews()) {
			if (text.equals(child.getModel().getText())) {
				return child;
			}
		}
		throw new AssertionError("missing child " + text);
	}

	private static Set<String> selectedTexts(MapView view) {
		Set<String> texts = new LinkedHashSet<>();
		for (NodeView nodeView : view.getSelecteds()) {
			texts.add(nodeView.getModel().getText());
		}
		return texts;
	}

	private static Set<String> texts(MindMapNode node) {
		Set<String> found = new HashSet<>();
		found.add(node.getText());
		for (int i = 0; i < node.getChildCount(); i++) {
			found.addAll(texts((MindMapNode) node.getChildAt(i)));
		}
		return found;
	}

	private static boolean hasNonWhitePixel(BufferedImage image) {
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int rgb = image.getRGB(x, y);
				if ((rgb >>> 24) != 0 && (rgb & 0x00FFFFFF) != 0x00FFFFFF) {
					return true;
				}
			}
		}
		return false;
	}

	/** Supplies the font the headless creator otherwise leaves null, so a picture contains the nodes. */
	static final class FontedMapCreator extends IndependantMapViewCreator {
		@Override
		public Font getDefaultFont() {
			return new Font(Font.SANS_SERIF, Font.PLAIN, 16);
		}
	}

	private static Path moduleDir() {
		Path dir = Path.of(System.getProperty("user.dir"));
		while (dir != null && !Files.exists(dir.resolve("Resources_en.properties"))) {
			dir = dir.getParent();
		}
		if (dir == null) {
			throw new IllegalStateException("Resources_en.properties not found from user.dir");
		}
		return dir;
	}

	private static Properties loadProperties(Path path) throws IOException {
		Properties properties = new Properties();
		try (Reader reader = Files.newBufferedReader(path)) {
			properties.load(reader);
		}
		return properties;
	}
}
