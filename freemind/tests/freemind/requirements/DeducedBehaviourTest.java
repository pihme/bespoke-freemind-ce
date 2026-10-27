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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import accessories.plugins.SortNodes;

import com.inet.jortho.SpellChecker;

import freemind.controller.Controller;
import freemind.main.HeadlessFreeMind;
import freemind.modes.EdgeAdapter;
import freemind.modes.common.listeners.MindMapMouseWheelEventHandler;
import freemind.main.Tools;
import freemind.main.XMLElement;
import freemind.modes.MapAdapter;
import freemind.modes.MindMapNode;
import freemind.modes.ModeController;
import freemind.modes.common.actions.FindAction;
import freemind.modes.mindmapmode.MindMapController;
import freemind.modes.mindmapmode.MindMapMapModel;
import freemind.modes.mindmapmode.MindMapMode;
import freemind.modes.mindmapmode.MindMapNodeModel;
import freemind.modes.mindmapmode.actions.ImportFolderStructureAction;
import freemind.modes.mindmapmode.actions.JoinNodesAction;
import freemind.view.mindmapview.IndependantMapViewCreator;
import freemind.view.mindmapview.MapView;
import plugins.latex.LatexNodeHook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Deduced tests. Each method checks a REQ-DED sentence from docs/spec.md.
 */
@DisplayName("DeducedBehaviourTest")
class DeducedBehaviourTest {

	@Test
	void TestREQ_NODE_07_JoinLeavesIntoSelectedText() throws Exception {
		Opened opened = openMap("Alpha", "Beta");
		MindMapNode alpha = opened.child("Alpha");
		MindMapNode beta = opened.child("Beta");
		List<MindMapNode> selected = new ArrayList<>();
		selected.add(alpha);
		selected.add(beta);

		new JoinNodesAction(opened.mode).joinNodes(alpha, selected);

		assertEquals("Alpha Beta", alpha.getText());
		assertFalse(childTexts(opened.root).contains("Beta"));
		assertEquals(1, opened.root.getChildCount());
	}

	@Test
	void TestREQ_FIND_01_FindThenFindNext() throws Exception {
		Opened opened = openMap("alpha one", "prefix alpha two");
		FindAction find = new FindAction(opened.mode);

		assertTrue(find.find(opened.root, List.of("alpha"), false));
		assertEquals("alpha one", opened.view.getSelected().getModel().getText());

		assertTrue(find.findNext());
		assertEquals("prefix alpha two", opened.view.getSelected().getModel().getText());
	}

	@Test
	void TestREQ_FILE_06_ImportFolderAddsNamedChildren() throws Exception {
		Opened opened = openMap("Here");
		Path folder = Files.createTempDirectory("bespoke-import");
		Path sub = Files.createDirectory(folder.resolve("Sub"));
		Path inner = Files.createFile(sub.resolve("inner.txt"));
		Path empty = Files.createDirectory(folder.resolve("Empty"));
		Path note = Files.createFile(folder.resolve("note.txt"));
		try {
			MindMapNode here = opened.child("Here");
			ImportFolderStructureAction action = new ImportFolderStructureAction(opened.mode);
			action.importFolderStructure(folder.toFile(), here, true);

			assertTrue(childTexts(here).contains("Sub"), "children: " + childTexts(here));
			assertTrue(childTexts(here).contains("Empty"), "children: " + childTexts(here));
			assertTrue(childTexts(here).contains("note.txt"), "children: " + childTexts(here));
			MindMapNode noteNode = childNamed(here, "note.txt");
			MindMapNode subNode = childNamed(here, "Sub");
			MindMapNode emptyNode = childNamed(here, "Empty");
			assertTrue(noteNode.getLink().contains("note.txt"), "link: " + noteNode.getLink());
			assertTrue(subNode.getLink().contains("Sub"), "link: " + subNode.getLink());
			assertEquals("inner.txt", childNamed(subNode, "inner.txt").getText());
			assertEquals(0, emptyNode.getChildCount());
			assertFalse(emptyNode.isFolded(), "empty directory stays unfolded");
			assertTrue(subNode.isFolded(), "directory with a file is folded");
			assertTrue(here.isFolded(), "non-root target folded");
			assertFalse(opened.root.isFolded(), "root stays unfolded");

			action.importFolderStructure(folder.toFile(), opened.root, true);
			assertFalse(opened.root.isFolded(), "root stays unfolded after import");
			assertTrue(childTexts(opened.root).contains("note.txt"),
					"root children: " + childTexts(opened.root));
		} finally {
			inner.toFile().delete();
			note.toFile().delete();
			sub.toFile().delete();
			empty.toFile().delete();
			folder.toFile().delete();
		}
	}

	@Test
	void TestREQ_NODE_04_PasteLeavesThePastedText() throws Exception {
		Opened opened = openMap();
		assertTrue(opened.mode.paste(new StringSelection("Pasted leaf"),
				opened.root, false, false));
		assertEquals(List.of("Pasted leaf"), childTexts(opened.root));
	}

	@Test
	void TestREQ_NODE_06_CutThenPasteMovesNodeToNewParent() throws Exception {
		Opened opened = openMap("Keep", "MoveMe", "Dest");
		MindMapNode move = opened.child("MoveMe");
		MindMapNode dest = opened.child("Dest");
		Transferable carried = opened.mode.cut(new ArrayList<>(List.of(move)));

		assertFalse(childTexts(opened.root).contains("MoveMe"));
		assertTrue(opened.mode.paste(carried, dest, false, false));
		assertEquals(List.of("MoveMe"), childTexts(dest));
		assertFalse(childTexts(opened.root).contains("MoveMe"));
		assertTrue(childTexts(opened.root).contains("Keep"));
	}

	@Test
	void TestREQ_NODE_09_SortInvokeReordersChildren() throws Exception {
		Opened opened = openMap("beta", "alpha");
		SortNodes sort = new SortNodes();
		sort.setController(opened.mode);
		sort.invoke(opened.root);
		assertEquals(List.of("alpha", "beta"), childTexts(opened.root));
	}

	@Test
	void TestREQ_NODE_03_UndoMenuRestoresPreviousText() throws Exception {
		Opened opened = openMap("Before");
		MindMapNode node = opened.child("Before");
		opened.mode.setNodeText(node, "After");
		assertEquals("After", node.getText());
		assertTrue(opened.mode.undo.isEnabled());

		opened.mode.undo.actionPerformed(new ActionEvent(opened.mode.undo,
				ActionEvent.ACTION_PERFORMED, "undo"));

		assertEquals("Before", node.getText());
		assertFalse(opened.mode.undo.isEnabled());
	}

	@Test
	void TestREQ_STYLE_07_FourEdgeShapesPaint() throws Exception {
		Opened opened = openMap("Child");
		MindMapNode child = opened.child("Child");
		String[] styles = {
				EdgeAdapter.EDGESTYLE_LINEAR,
				EdgeAdapter.EDGESTYLE_BEZIER,
				EdgeAdapter.EDGESTYLE_SHARP_LINEAR,
				EdgeAdapter.EDGESTYLE_SHARP_BEZIER
		};
		for (String style : styles) {
			opened.mode.setEdgeStyle(child, style);
			assertEquals(style, ((EdgeAdapter) child.getEdge()).getStyle());
			opened.view.getRoot().doLayout();
			BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics = image.createGraphics();
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
			opened.view.getRoot().paint(graphics);
			graphics.dispose();
			assertTrue(hasInk(image), style + " bounds " + opened.view.getRoot().getBounds());
		}
		opened.mode.setEdgeColor(child, Color.RED);
		opened.mode.setEdgeWidth(child, 4);
		assertEquals(Color.RED, ((EdgeAdapter) child.getEdge()).getRealColor());
		assertEquals(4, ((EdgeAdapter) child.getEdge()).getRealWidth());
	}

	@Test
	void TestREQ_STYLE_08_CtrlWheelChangesZoom() throws Exception {
		Opened opened = openMap("Child");
		float before = opened.view.getZoom();
		MindMapMouseWheelEventHandler handler = new MindMapMouseWheelEventHandler(opened.mode);
		handler.mouseWheelMoved(new MouseWheelEvent(opened.view,
				MouseWheelEvent.MOUSE_WHEEL, System.currentTimeMillis(),
				InputEvent.CTRL_MASK, 20, 20, 0, false,
				MouseWheelEvent.WHEEL_UNIT_SCROLL, 1, 1));
		assertTrue(opened.view.getZoom() < before,
				"before " + before + " after " + opened.view.getZoom());
	}

	@Test
	void TestREQ_NODE_08_ImageHtmlGainsBaseHrefInTheView() throws Exception {
		String stored = "<html><body><img src=\"pic.png\"/></body></html>";
		String xml = "<map version=\"1.0.0\"><node TEXT=\""
				+ stored.replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
				+ "\"/></map>";
		File file = File.createTempFile("bespoke-image", ".mm");
		file.deleteOnExit();
		Files.writeString(file.toPath(), xml);

		JPanel parent = new JPanel();
		parent.setBounds(new Rectangle(0, 0, 400, 600));
		MapView view = new FontedMapCreator().createMapViewForFile(
				file.getPath(), parent, new HeadlessFreeMind());
		((MapAdapter) view.getModel()).setFile(file);
		view.getRoot().updateAll();
		String shown = view.getRoot().getMainView().getText();
		String modelText = view.getRoot().getModel().getText();

		assertTrue(modelText.contains("pic.png"));
		assertFalse(modelText.contains("<base "));
		assertTrue(shown.contains("pic.png"));
		assertTrue(shown.contains("<base href=\"" + view.getModel().getURL()));
	}

	@Test
	void TestREQ_PLUG_05_LatexHookStoresEquation() {
		LatexNodeHook created = new LatexNodeHook();
		assertEquals("E = mc^{2}", created.getContent("EQUATION"));

		created.setName("plugins.latex.LatexNodeHook");
		XMLElement saved = new XMLElement();
		created.save(saved);
		assertEquals("E = mc^{2}", saved.getAttribute("EQUATION"));

		XMLElement loaded = new XMLElement();
		loaded.setAttribute("EQUATION", "a = b");
		LatexNodeHook restored = new LatexNodeHook();
		restored.loadFrom(loaded);
		assertEquals("a = b", restored.getContent("EQUATION"));
	}

	@Test
	void TestREQ_PLUG_07_EnglishDictionaryLoads() throws Exception {
		URL dictUrl = Tools.class.getClassLoader().getResource("dictionaries.cnf");
		assertNotNull(dictUrl);
		String external = dictUrl.toExternalForm();
		URL base = new URL(external.substring(0, external.lastIndexOf('/') + 1));

		SpellChecker.registerDictionaries(base, "en");

		long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
		while (!SpellChecker.isDictionaryLoaded() && System.nanoTime() < deadline) {
			Thread.sleep(50);
		}
		assertTrue(SpellChecker.isDictionaryLoaded());
		assertEquals("en", SpellChecker.getCurrentLocale().getLanguage());
	}

	private static Opened openMap(String... childTexts) throws Exception {
		HeadlessFreeMind frame = new HeadlessFreeMind();
		HoldingController app = new HoldingController(frame);
		app.initialization();
		MindMapController[] holder = new MindMapController[1];
		MindMapMode mode = new MindMapMode() {
			@Override
			public ModeController createModeController() {
				holder[0] = new MindMapController(this);
				return holder[0];
			}
		};
		mode.init(app);
		MindMapController modeController = holder[0];
		MindMapMapModel model = new MindMapMapModel(modeController);
		modeController.setModel(model);
		MindMapNodeModel root = new MindMapNodeModel("ROOT", model);
		model.setRoot(root);
		for (int i = 0; i < childTexts.length; i++) {
			root.insert(new MindMapNodeModel(childTexts[i], model), i);
		}
		app.getMapModuleManager().newMapModule(model, modeController);
		MapView view = app.getView();
		JPanel parent = new JPanel();
		parent.setBounds(new Rectangle(0, 0, 800, 600));
		parent.add(view);
		view.setBounds(parent.getBounds());
		parent.doLayout();
		view.addNotify();
		view.getRoot().getMainView().doLayout();
		return new Opened(modeController, view, root);
	}

	/**
	 * A headless frame has no content pane, so the normal controller init cannot
	 * build the toolbar. Opening a map still notifies that controller.
	 */
	private static final class HoldingController extends Controller {
		HoldingController(HeadlessFreeMind frame) {
			super(frame);
		}

		@Override
		public void setAllActions(boolean enabled) {
		}

		@Override
		public void afterMapModuleChange(freemind.view.MapModule oldMapModule,
				freemind.modes.Mode oldMode, freemind.view.MapModule newMapModule,
				freemind.modes.Mode newMode) {
		}

		@Override
		public void numberOfOpenMapInformation(int number, int pIndex) {
		}
	}

	private static List<String> childTexts(MindMapNode node) {
		List<String> texts = new ArrayList<>();
		for (int i = 0; i < node.getChildCount(); i++) {
			texts.add(((MindMapNode) node.getChildAt(i)).getText());
		}
		return texts;
	}

	private static boolean hasInk(BufferedImage image) {
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				if ((image.getRGB(x, y) & 0xFFFFFF) != 0xFFFFFF) {
					return true;
				}
			}
		}
		return false;
	}

	private static MindMapNode childNamed(MindMapNode parent, String text) {
		for (int i = 0; i < parent.getChildCount(); i++) {
			MindMapNode child = (MindMapNode) parent.getChildAt(i);
			if (text.equals(child.getText())) {
				return child;
			}
		}
		throw new AssertionError("missing child " + text + " under " + parent.getText());
	}

	private static final class Opened {
		final MindMapController mode;
		final MapView view;
		final MindMapNodeModel root;

		Opened(MindMapController mode, MapView view, MindMapNodeModel root) {
			this.mode = mode;
			this.view = view;
			this.root = root;
		}

		MindMapNode child(String text) {
			for (int i = 0; i < root.getChildCount(); i++) {
				MindMapNode child = (MindMapNode) root.getChildAt(i);
				if (text.equals(child.getText())) {
					return child;
				}
			}
			throw new AssertionError("missing child " + text);
		}
	}

	/** Supplies the font the headless creator otherwise leaves null. */
	static final class FontedMapCreator extends IndependantMapViewCreator {
		@Override
		public Font getDefaultFont() {
			return new Font(Font.SANS_SERIF, Font.PLAIN, 16);
		}
	}
}
