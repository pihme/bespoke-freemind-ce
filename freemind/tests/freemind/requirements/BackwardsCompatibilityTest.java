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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.LayoutManager;
import java.awt.Stroke;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.logging.Logger;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.SortedMap;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import accessories.plugins.AutomaticLayout;
import accessories.plugins.util.html.ClickableImageCreator;
import accessories.plugins.ChangeNodeLevelAction;
import accessories.plugins.ClonePasteAction;
import accessories.plugins.ClonePlugin;
import accessories.plugins.CreationModificationPlugin;
import accessories.plugins.FitToPage;
import accessories.plugins.FormatNewNodes;
import accessories.plugins.FormatPaste;
import accessories.plugins.JumpLastEditLocation;
import accessories.plugins.HierarchicalIcons;
import accessories.plugins.NewParentNode;
import accessories.plugins.RevisionPlugin;
import accessories.plugins.SortNodes;
import accessories.plugins.SplitNode;
import accessories.plugins.UnfoldAll;
import accessories.plugins.time.CalendarMarkingEvaluator;
import accessories.plugins.time.JCalendar;
import accessories.plugins.time.JDayChooser;
import accessories.plugins.time.JSpinField;
import accessories.plugins.time.JTripleCalendar;
import accessories.plugins.time.TableSorter;
import freemind.common.ComboProperty;
import freemind.common.JOptionalSplitPane;
import freemind.common.NumberProperty;
import freemind.common.RemindValueProperty;
import freemind.common.TextTranslator;
import freemind.common.NamedObject;
import freemind.common.UnicodeReader;
import freemind.controller.Controller;
import freemind.controller.MindMapNodesSelection;
import freemind.controller.actions.generated.instance.CalendarMarking;
import freemind.controller.actions.generated.instance.CalendarMarkings;
import freemind.controller.actions.generated.instance.CompoundAction;
import freemind.controller.actions.generated.instance.CutNodeAction;
import freemind.controller.actions.generated.instance.DeleteNodeAction;
import freemind.controller.actions.generated.instance.FoldAction;
import freemind.controller.actions.generated.instance.HookNodeAction;
import freemind.controller.actions.generated.instance.MoveNodesAction;
import freemind.controller.actions.generated.instance.NewNodeAction;
import freemind.controller.actions.generated.instance.NodeListMember;
import freemind.controller.actions.generated.instance.UndoPasteNodeAction;
import freemind.controller.actions.generated.instance.NodeColorFormatAction;
import freemind.controller.actions.generated.instance.PasteNodeAction;
import freemind.controller.actions.generated.instance.ObjectFactory;
import freemind.controller.actions.generated.instance.XmlAction;
import freemind.controller.MapModuleManager;
import freemind.controller.filter.DefaultFilter;
import freemind.controller.filter.FilterController;
import freemind.controller.filter.condition.ConditionNotSatisfiedDecorator;
import freemind.controller.filter.condition.IconNotContainedCondition;
import freemind.controller.filter.condition.Condition;
import freemind.controller.filter.condition.ConjunctConditions;
import freemind.controller.filter.condition.DisjunctConditions;
import freemind.controller.filter.condition.ConditionFactory;
import freemind.controller.filter.util.ExtendedComboBoxModel;
import freemind.controller.filter.util.SortedComboBoxModel;
import freemind.controller.filter.util.SortedMapListModel;
import freemind.controller.actions.generated.instance.Pattern;
import freemind.controller.actions.generated.instance.PatternChild;
import freemind.controller.actions.generated.instance.PatternEdgeColor;
import freemind.controller.actions.generated.instance.PatternEdgeStyle;
import freemind.controller.actions.generated.instance.PatternEdgeWidth;
import freemind.controller.actions.generated.instance.PatternIcon;
import freemind.controller.actions.generated.instance.PatternNodeBackgroundColor;
import freemind.controller.actions.generated.instance.PatternNodeColor;
import freemind.controller.actions.generated.instance.PatternNodeFontBold;
import freemind.controller.actions.generated.instance.PatternNodeFontItalic;
import freemind.controller.actions.generated.instance.PatternNodeFontName;
import freemind.controller.actions.generated.instance.PatternNodeFontSize;
import freemind.controller.actions.generated.instance.PatternNodeFontStrikethrough;
import freemind.controller.actions.generated.instance.PatternNodeStyle;
import freemind.controller.actions.generated.instance.PatternNodeText;
import freemind.controller.filter.util.SortedMapVector;
import freemind.extensions.ExportHook;
import freemind.extensions.NodeHook;
import freemind.extensions.PermanentNodeHook;
import freemind.main.ExampleFileFilter;
import freemind.main.FreeMind;
import freemind.main.FreeMindCommon;
import freemind.main.FreeMindMain;
import freemind.main.HeadlessFreeMind;
import freemind.main.HtmlTools;
import freemind.main.Resources;
import freemind.main.Tools;
import freemind.main.XMLElement;
import freemind.main.XMLParseException;
import freemind.swing.DefaultListModel;
import freemind.modes.ArrowLinkAdapter;
import freemind.modes.ArrowLinkTarget;
import freemind.modes.EdgeAdapter;
import freemind.modes.MapFeedback;
import freemind.modes.MindMapLinkRegistry;
import freemind.modes.XMLElementAdapter;
import freemind.modes.MapAdapter;
import freemind.modes.FreeMindFileDialog;
import freemind.modes.LineAdapter;
import freemind.modes.MindIcon;
import freemind.modes.attributes.Attribute;
import freemind.modes.MindMapArrowLink;
import freemind.modes.MindMapLink;
import freemind.modes.MindMapNode;
import freemind.modes.NodeAdapter;
import freemind.modes.ModeController;
import freemind.modes.StylePattern;
import freemind.modes.mindmapmode.JAutoScrollBarPane;
import freemind.modes.mindmapmode.actions.UndoAction;
import freemind.modes.mindmapmode.dialogs.IntegerComboProperty;
import freemind.preferences.layout.KeyEventWorkaround;
import freemind.view.mindmapview.EdgeView;
import freemind.modes.StylePatternFactory;
import freemind.modes.common.CommonNodeKeyListener;
import freemind.modes.common.actions.FindAction;
import freemind.modes.filemode.FileMapModel;
import freemind.modes.filemode.FileMode;
import freemind.modes.filemode.FileNodeModel;
import freemind.modes.mindmapmode.EncryptedMindMapNode;
import freemind.modes.mindmapmode.MindMapArrowLinkModel;
import freemind.modes.mindmapmode.MindMapController;
import freemind.modes.mindmapmode.MindMapMapModel;
import freemind.modes.mindmapmode.actions.ApplyPatternAction;
import freemind.modes.mindmapmode.actions.BoldAction;
import freemind.modes.mindmapmode.actions.CloudAction;
import freemind.modes.mindmapmode.actions.EdgeStyleAction;
import freemind.modes.mindmapmode.actions.EdgeWidthAction;
import freemind.modes.mindmapmode.actions.FontFamilyAction;
import freemind.modes.mindmapmode.actions.FontSizeAction;
import freemind.modes.mindmapmode.actions.IconAction;
import freemind.modes.mindmapmode.actions.ItalicAction;
import freemind.modes.mindmapmode.actions.MoveNodeAction;
import freemind.modes.mindmapmode.actions.NodeColorBlendAction;
import freemind.modes.mindmapmode.actions.NodeStyleAction;
import freemind.modes.mindmapmode.actions.NodeUpAction;
import freemind.modes.mindmapmode.actions.RemoveAllIconsAction;
import freemind.modes.mindmapmode.actions.SelectAllAction;
import freemind.modes.mindmapmode.actions.SelectBranchAction;
import freemind.modes.mindmapmode.actions.StrikethroughAction;
import freemind.modes.mindmapmode.actions.ToggleChildrenFoldedAction;
import freemind.modes.mindmapmode.actions.ToggleFoldedAction;
import freemind.modes.mindmapmode.actions.UnderlinedAction;
import freemind.modes.mindmapmode.actions.UsePlainTextAction;
import freemind.modes.mindmapmode.actions.UseRichFormattingAction;
import freemind.modes.mindmapmode.actions.ImportExplorerFavoritesAction;
import freemind.modes.mindmapmode.actions.NodeHookAction;
import freemind.modes.mindmapmode.actions.RemoveIconAction;
import freemind.modes.mindmapmode.actions.xml.ActionPair;
import freemind.modes.mindmapmode.MindMapMode;
import freemind.modes.mindmapmode.MindMapNodeModel;
import freemind.extensions.NodeHookAdapter;
import freemind.preferences.layout.KeyEventTranslator;
import freemind.preferences.layout.VariableSizeCardLayout;
import freemind.preferences.layout.KeyEventWorkaround;
import plugins.search.Search;
import freemind.view.ScalableImageIcon;
import freemind.view.mindmapview.ArrowLinkView;
import freemind.view.mindmapview.MainView;
import freemind.view.mindmapview.MapView;
import freemind.view.mindmapview.NodeFoldingComponent;
import freemind.view.mindmapview.NodeView;
import plugins.latex.LatexNodeHook;
import plugins.map.MapNodePositionHolder;
import plugins.map.TileImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Backwards compatibility tests. Each method checks current behaviour that
 * docs/spec.md does not name as a requirement.
 */
@DisplayName("BackwardsCompatibilityTest")
class BackwardsCompatibilityTest {

	@Test
	void calendarKeepsTheDayThatWasSet() {
		JCalendar calendar = new JCalendar();
		Date chosen = new GregorianCalendar(2020, Calendar.MARCH, 15).getTime();

		calendar.setDate(chosen);

		Calendar read = new GregorianCalendar();
		read.setTime(calendar.getDate());
		assertEquals(2020, read.get(Calendar.YEAR));
		assertEquals(Calendar.MARCH, read.get(Calendar.MONTH));
		assertEquals(15, read.get(Calendar.DAY_OF_MONTH));
		assertEquals(2020, calendar.getYearChooser().getYear());
		assertEquals(Calendar.MARCH, calendar.getMonthChooser().getMonth());
		assertEquals(15, calendar.getDayChooser().getDay());
	}

	@Test
	void foldedNodeHidesItsChild() throws Exception {
		Opened opened = openMap("Parent");
		MindMapNode parent = opened.child("Parent");
		MindMapNode hidden = opened.mode.addNewNode(parent, 0, parent.isLeft());
		opened.mode.setNodeText(hidden, "Hidden");

		opened.mode.setFolded(parent, true);
		opened.view.getRoot().updateAll();

		assertTrue(parent.isFolded());
		assertEquals("Hidden", ((MindMapNode) parent.getChildAt(0)).getText());
		assertEquals(0, opened.view.getNodeView(parent).getChildrenViews().size());
	}

	@Test
	void noteIconAndLinkStayOnTheNode() throws Exception {
		Opened opened = openMap("Child");
		MindMapNode child = opened.child("Child");

		opened.mode.setNoteText(child, "Remember the milk");
		assertTrue(child.getNoteText().contains("Remember the milk"), child.getNoteText());

		opened.mode.addIcon(child, MindIcon.factory("button_ok"));
		assertEquals("button_ok", child.getIcons().get(0).getName());
		assertEquals(0, opened.mode.removeLastIcon(child));
		assertTrue(child.getIcons().isEmpty());

		opened.mode.setLink(child, "https://example.test/map");
		assertEquals("https://example.test/map", child.getLink());
	}

	@Test
	void movingANodeUpReordersTheChildren() throws Exception {
		Opened opened = openMap("Alpha", "Beta");
		MindMapNode beta = opened.child("Beta");

		opened.mode.moveNodes(beta, new ArrayList<>(List.of(beta)), -1);

		assertEquals(List.of("Beta", "Alpha"), childTexts(opened.root));
	}

	@Test
	void patternCopiesBoldItalicAndColor() throws Exception {
		Opened opened = openMap("Styled", "Plain");
		MindMapNode styled = opened.child("Styled");
		MindMapNode plain = opened.child("Plain");
		opened.mode.setBold(styled, true);
		opened.mode.setItalic(styled, true);
		opened.mode.setNodeColor(styled, Color.BLUE);

		Pattern pattern = StylePatternFactory.createPatternFromNode(styled);
		assertEquals(StylePatternFactory.TRUE_VALUE, pattern.getPatternNodeFontBold().getValue());
		assertEquals(StylePatternFactory.TRUE_VALUE, pattern.getPatternNodeFontItalic().getValue());
		assertEquals(Tools.colorToXml(Color.BLUE), pattern.getPatternNodeColor().getValue());
		String described = StylePatternFactory.toString(pattern, key -> key);
		assertTrue(described.contains("+PatternToString.FontBold"), described);
		assertTrue(described.contains("+PatternToString.color"), described);

		MindMapNode other = opened.mode.addNewNode(opened.root, opened.root.getChildCount(), false);
		opened.mode.setNodeText(other, "Third");
		Pattern shared = StylePatternFactory.createPatternFromSelected(styled, List.of(styled, plain));
		assertNull(shared.getPatternNodeFontBold());

		opened.mode.applyPattern(plain, pattern);
		assertTrue(plain.isBold());
		assertTrue(plain.isItalic());
		assertEquals(Color.BLUE, plain.getColor());

		Pattern clearing = StylePatternFactory.getRemoveAllPattern();
		assertNull(clearing.getPatternNodeFontBold().getValue());
		String cleared = StylePatternFactory.toString(clearing, key -> key);
		assertTrue(cleared.contains("-PatternToString.FontBold"), cleared);
	}

	@Test
	void arrowLinkPaintsRedBetweenTwoNodes() throws Exception {
		Opened opened = openMap("From", "To");
		MindMapNode from = opened.child("From");
		MindMapNode to = opened.child("To");
		opened.mode.addLink(from, to);
		MindMapArrowLink link = arrowFrom(opened, from);
		assertEquals(to.getObjectId(opened.mode), link.getTarget().getObjectId(opened.mode));
		assertNull(link.getStartInclination());
		assertNull(link.getEndInclination());

		opened.mode.setArrowLinkColor(link, Color.RED);
		assertEquals(Color.RED, link.getColor());
		opened.view.getRoot().updateAll();
		opened.view.getRoot().doLayout();
		paint(opened.view);

		assertNotNull(link.getStartInclination());
		assertNotNull(link.getEndInclination());
		assertTrue(opened.view.getInnerBounds().width > 0);
		assertTrue(opened.view.getInnerBounds().height > 0);
	}

	@Test
	void cloudPaintsRedAroundTheNode() throws Exception {
		Opened opened = openMap("Cloudy");
		MindMapNode child = opened.child("Cloudy");
		opened.mode.setCloud(child, true);
		opened.mode.setCloudColor(child, Color.RED);
		assertNotNull(child.getCloud());
		assertEquals(Color.RED, child.getCloud().getColor());

		opened.view.getRoot().updateAll();
		opened.view.getRoot().doLayout();
		BufferedImage image = paint(opened.view.getRoot());

		assertTrue(redPixels(image) > 10, "red pixels " + redPixels(image));
	}

	@Test
	void htmlExportWritesTheNodeText() throws Exception {
		Opened opened = openMap("Käse", "Bread");
		File file = File.createTempFile("bespoke-html", ".html");
		file.deleteOnExit();

		MindMapController.saveHTML(opened.root, file);

		String html = Files.readString(file.toPath());
		assertTrue(html.contains("<html>"), html);
		assertTrue(html.contains("&auml;"), html);
		assertTrue(html.contains("se"), html);
		assertTrue(html.contains("Bread"), html);
		assertTrue(html.contains("</html>"), html);
	}

	@Test
	void encryptedNodeHidesTheChildUntilThePasswordMatches() throws Exception {
		Opened opened = openMap("Visible");
		EncryptedMindMapNode locked = new EncryptedMindMapNode("Locked", opened.model);
		opened.root.insert(locked, opened.root.getChildCount());
		MindMapNodeModel hidden = new MindMapNodeModel("Hidden", opened.model);
		locked.insert(hidden, 0);
		locked.setPassword(new StringBuffer("secret"));

		locked.encrypt();

		assertFalse(locked.isAccessible());
		assertTrue(locked.isFolded());
		assertEquals(0, locked.getChildCount());
		assertNotNull(locked.getAdditionalInfo());
		assertFalse(locked.getAdditionalInfo().isEmpty());
		assertFalse(locked.decrypt(new StringBuffer("nope")));
		assertFalse(locked.isAccessible());

		assertTrue(locked.decrypt(new StringBuffer("secret")));
		assertTrue(locked.isAccessible());
		assertTrue(childTexts(locked).contains("Hidden"), childTexts(locked).toString());
	}

	@Test
	void tableSorterOrdersRows() {
		DefaultTableModel model = new DefaultTableModel(new Object[] { "Name" }, 0);
		model.addRow(new Object[] { "cherry" });
		model.addRow(new Object[] { "banana" });
		model.addRow(new Object[] { "apple" });
		TableSorter sorter = new TableSorter(model);

		sorter.setSortingStatus(0, TableSorter.ASCENDING);
		assertEquals("apple", sorter.getValueAt(0, 0));
		assertEquals("banana", sorter.getValueAt(1, 0));
		assertEquals("cherry", sorter.getValueAt(2, 0));
		assertEquals(TableSorter.ASCENDING, sorter.getSortingStatus(0));

		sorter.setSortingStatus(0, TableSorter.DESCENDING);
		assertEquals("cherry", sorter.getValueAt(0, 0));
		assertEquals("apple", sorter.getValueAt(2, 0));
	}

	@Test
	void keyTranslatorReadsControlEnter() {
		KeyEventTranslator.Key parsed = KeyEventTranslator.parseKey("C+ENTER");
		assertNotNull(parsed);
		assertEquals(KeyEvent.VK_ENTER, parsed.key);
		assertEquals("control", parsed.modifiers);

		KeyEvent pressed = new KeyEvent(new JPanel(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
				InputEvent.CTRL_MASK, KeyEvent.VK_A, 'a');
		KeyEventTranslator.Key translated = KeyEventTranslator.translateKeyEvent(pressed);
		assertNotNull(translated);
		assertEquals('A', translated.input);
		assertEquals("control", translated.modifiers);

		assertNull(KeyEventTranslator.parseKey(null));

		KeyEvent tab = new KeyEvent(new JPanel(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_TAB,
				KeyEvent.CHAR_UNDEFINED);
		KeyEventTranslator.Key tabKey = KeyEventTranslator.translateKeyEvent(tab);
		assertTrue(tab.isConsumed());
		assertNotNull(tabKey);
		assertEquals(KeyEvent.VK_TAB, tabKey.key);

		KeyEvent space = new KeyEvent(new JPanel(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
				InputEvent.CTRL_MASK, KeyEvent.VK_SPACE, ' ');
		KeyEventTranslator.Key spaceKey = KeyEventTranslator.translateKeyEvent(space);
		assertNotNull(spaceKey);
		assertEquals(' ', spaceKey.input);

		KeyEvent typed = new KeyEvent(new JPanel(), KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0,
				KeyEvent.VK_UNDEFINED, 'B');
		KeyEventTranslator.Key typedKey = KeyEventTranslator.translateKeyEvent(typed);
		assertNotNull(typedKey);
		assertEquals('B', typedKey.input);
		assertNull(typedKey.modifiers);
		assertNull(KeyEventTranslator.translateKeyEvent(new KeyEvent(new JPanel(), KeyEvent.KEY_TYPED,
				System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, '\n')));

		KeyEventTranslator.Key parsedSpace = KeyEventTranslator.parseKey("SPACE");
		assertNotNull(parsedSpace);
		assertEquals(' ', parsedSpace.input);
		assertNull(KeyEventTranslator.parseKey("NOT_A_KEY"));
		KeyEventTranslator.Key parsedBoth = KeyEventTranslator.parseKey("CA+x");
		assertNotNull(parsedBoth);
		assertEquals('x', parsedBoth.input);
		assertTrue(parsedBoth.modifiers.contains("control"), parsedBoth.modifiers);
		assertTrue(parsedBoth.modifiers.contains("alt"), parsedBoth.modifiers);
		assertEquals("control", KeyEventTranslator.modifiersToString(InputEvent.CTRL_MASK));
		assertNull(KeyEventTranslator.modifiersToString(0));
		String modifierWord = KeyEventTranslator.getModifierString(new KeyEvent(new JPanel(), KeyEvent.KEY_PRESSED,
				System.currentTimeMillis(), InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK, KeyEvent.VK_A, 'a'));
		assertTrue(modifierWord.contains("control"), modifierWord);
		assertTrue(modifierWord.contains("shift"), modifierWord);
	}

	@Test
	void clonePluginReadsStoredCloneIds() {
		XMLElement xml = new XMLElement();
		XMLElement parameters = new XMLElement();
		parameters.setName("Parameters");
		parameters.setAttribute(ClonePlugin.XML_STORAGE_CLONES, "alpha,beta,");
		parameters.setAttribute(ClonePlugin.XML_STORAGE_CLONE_ID, "id-1");
		parameters.setAttribute(ClonePlugin.XML_STORAGE_CLONE_ITSELF, ClonePlugin.CLONE_ITSELF_TRUE);
		xml.addChild(parameters);

		ClonePlugin plugin = new ClonePlugin();
		plugin.loadFrom(xml);

		assertEquals("alpha,beta,", plugin.getCloneIdsAsString());
	}

	@Test
	void mapPositionRoundTripsAndBuildsATileTooltip() {
		MapNodePositionHolder holder = new MapNodePositionHolder();
		holder.setName("plugins.map.MapNodePositionHolder");
		holder.setPosition(new Coordinate(47.26, 11.39));
		holder.setMapCenter(new Coordinate(47.0, 11.0));
		holder.setZoom(12);
		holder.setTileSource(OsmTileSource.Mapnik.class.getName());

		XMLElement saved = new XMLElement();
		holder.save(saved);
		MapNodePositionHolder loaded = new MapNodePositionHolder();
		loaded.loadFrom(saved);

		assertEquals(47.26, loaded.getPosition().getLat(), 0.001);
		assertEquals(11.39, loaded.getPosition().getLon(), 0.001);
		assertEquals(47.0, loaded.getMapCenter().getLat(), 0.001);
		assertEquals(12, loaded.getZoom());
		assertTrue(loaded.getTileSource().contains("Mapnik"), loaded.getTileSource());

		String html = holder.getImageHtml();
		assertTrue(html.contains("<strong>X</strong>"), html);
		assertTrue(html.contains("http"), html);
	}

	@Test
	void xmlElementKeepsAnUmlautAttribute() throws Exception {
		XMLElement element = new XMLElement();
		element.parseString("<map version=\"1.0.0\"><node TEXT=\"Käse\"/></map>");
		assertEquals("1.0.0", element.getStringAttribute("version"));
		XMLElement node = (XMLElement) element.getChildren().get(0);
		assertEquals("Käse", node.getStringAttribute("TEXT"));

		StringWriter written = new StringWriter();
		element.write(written);
		XMLElement again = new XMLElement();
		again.parseFromReader(new StringReader(written.toString()));
		XMLElement againNode = (XMLElement) again.getChildren().get(0);
		assertEquals("Käse", againNode.getStringAttribute("TEXT"));
	}

	@Test
	void toolsComparePathsPlaceholdersAndIconIndexes() throws Exception {
		assertEquals("", Tools.compareText("same", "same"));
		assertTrue(Tools.compareText("same", null).contains("null"));
		assertTrue(Tools.compareText("longer-text", "longer").contains("First string is longer"));
		assertTrue(Tools.compareText("ab", "ax").contains("Difference at 1"));
		assertEquals("Hello Dolly.", Tools.expandPlaceholders("Hello $1.", "Dolly"));
		assertEquals("A and B", Tools.expandPlaceholders("$1 and $2", "A", "B"));
		assertEquals("A B C", Tools.expandPlaceholders("$1 $2 $3", "A", "B", "C"));
		assertEquals("mm", Tools.getExtension("Map.MM"));
		assertEquals("Map", Tools.removeExtension("Map.MM"));
		assertFalse(Tools.isAbsolutePath("relative/file.mm"));
		assertTrue(Tools.isAbsolutePath("/tmp/file.mm"));
		assertEquals("/", Tools.getPrefix("/tmp/file.mm"));
		assertEquals("/tmp/file.mm", Tools.getFileNameFromRestorable("MindMap:/tmp/file.mm"));
		assertEquals("MindMap", Tools.getModeFromRestorable("MindMap:/tmp/file.mm"));
		assertEquals(List.of("a", "b"), Tools.stringToList("a;b"));
		assertEquals("true", Tools.BooleanToXml(true));
		assertEquals("false", Tools.BooleanToXml(false));
		Font scaled = Tools.updateFontSize(new Font(Font.SANS_SERIF, Font.PLAIN, 12), 2f, 10);
		assertEquals(20f, scaled.getSize2D(), 0.01f);
		assertTrue(Tools.urlGetFile(new URL("file:/tmp/map.mm")).contains("map.mm"));
		Vector<String> order = new Vector<>(List.of("a", "b", "c"));
		Tools.swapVectorPositions(order, 0, 2);
		assertEquals(List.of("c", "b", "a"), List.copyOf(order));
		assertThrows(IllegalArgumentException.class, () -> Tools.swapVectorPositions(order, -1, 0));
		assertEquals("CompoundAction []", Tools.printXmlAction(new CompoundAction()));

		Opened opened = openMap("Marked");
		MindMapNode marked = opened.child("Marked");
		opened.mode.addIcon(marked, MindIcon.factory("button_ok"));
		opened.mode.addIcon(marked, MindIcon.factory("button_cancel"));
		opened.mode.addIcon(marked, MindIcon.factory("button_ok"));
		assertEquals(0, Tools.iconFirstIndex(marked, "button_ok"));
		assertEquals(2, Tools.iconLastIndex(marked, "button_ok"));
		assertEquals(-1, Tools.iconLastIndex(marked, "missing"));
	}

	@Test
	void xmlElementReadsTypedAttributes() {
		XMLElement element = new XMLElement();
		element.parseString("<node count=\"3\" ratio=\"1.5\" flag=\"yes\" named=\"three\"/>");
		assertEquals(3, element.getIntAttribute("count"));
		assertEquals(9, element.getIntAttribute("missing", 9));
		assertEquals(1.5, element.getDoubleAttribute("ratio"), 0.001);
		assertEquals(4.0, element.getDoubleAttribute("missing-ratio", 4.0), 0.001);
		assertTrue(element.getBooleanAttribute("flag", "yes", "no", false));
		assertFalse(element.getBooleanAttribute("absent", "yes", "no", false));
		Hashtable<Object, Integer> names = new Hashtable<>();
		names.put("three", 3);
		assertEquals(3, element.getIntAttribute("named", names, "three", false));
		element.setAttribute("count", "nope");
		assertThrows(XMLParseException.class, () -> element.getIntAttribute("count"));
	}

	@Test
	void stylePatternLoadsANamedBoldPattern() throws Exception {
		String xml = "<patterns><pattern name=\"Marked\" recursive=\"true\">"
				+ "<node color=\"#ff0000\"><font bold=\"true\"/></node></pattern></patterns>";
		List<?> patterns = StylePattern.loadPatterns(new StringReader(xml));
		assertEquals(1, patterns.size());
		StylePattern pattern = (StylePattern) patterns.get(0);
		assertEquals("Marked", pattern.getName());
		assertTrue(pattern.getRecursive());
		assertEquals(Color.RED, pattern.getNodeColor());
		assertEquals(Boolean.TRUE, pattern.getNodeFontBold());
	}

	@Test
	void splittingAChildKeepsBothHalves() throws Exception {
		Opened opened = openMap("AlphaBeta");
		MindMapNode child = opened.child("AlphaBeta");

		opened.mode.splitNode(child, 5, null);

		assertEquals("Alpha", child.getText());
		assertTrue(childTexts(opened.root).contains("Beta"), childTexts(opened.root).toString());
	}

	@Test
	void clearingANodeDropsTheNoteAndTheBoldFace() throws Exception {
		Opened opened = openMap("Busy");
		MindMapNode child = opened.child("Busy");
		opened.mode.setNodeColor(child, Color.RED);
		opened.mode.setBold(child, true);
		opened.mode.setNoteText(child, "Remember");

		opened.mode.clearNodeContents(child);

		assertNull(child.getNoteText());
		assertFalse(child.isBold());
		assertFalse(Color.RED.equals(child.getColor()));
	}

	@Test
	void blendingANodeColorMovesItTowardTheBackground() throws Exception {
		Opened opened = openMap("Tinted");
		MindMapNode child = opened.child("Tinted");
		opened.view.setBackground(Color.WHITE);
		opened.mode.setNodeColor(child, Color.RED);

		opened.mode.blendNodeColor(child);

		Color blended = child.getColor();
		assertEquals((3 * 255 + 255) / 4, blended.getRed());
		assertEquals((3 * 255 + 0) / 4, blended.getGreen());
		assertEquals((3 * 255 + 0) / 4, blended.getBlue());
	}

	@Test
	void optionalSplitPaneHoldsOnePanelThenTwo() {
		JOptionalSplitPane pane = new JOptionalSplitPane();
		JPanel first = new JPanel();
		assertThrows(IllegalArgumentException.class, () -> pane.setComponent(new JPanel(), 2));
		pane.setComponent(first, 0);
		pane.setComponent(first, 0);
		assertEquals(1, pane.getAmountOfComponents());
		pane.setComponent(new JPanel(), 1);
		assertEquals(2, pane.getAmountOfComponents());
		assertTrue(pane.getComponent(0) instanceof JSplitPane);
		pane.removeComponent(0);
		assertEquals(1, pane.getAmountOfComponents());
	}

	@Test
	void continuousSelectionKeepsTheSiblingsBetween() throws Exception {
		Opened opened = openMap("Alpha", "Beta", "Gamma");
		opened.view.getRoot().updateAll();
		opened.view.getRoot().doLayout();
		NodeView alpha = opened.view.getNodeView(opened.child("Alpha"));
		NodeView gamma = opened.view.getNodeView(opened.child("Gamma"));
		opened.view.selectAsTheOnlyOneSelected(alpha);

		assertTrue(opened.view.selectContinuous(gamma));

		List<String> selected = new ArrayList<>();
		for (NodeView view : opened.view.getSelecteds()) {
			selected.add(view.getModel().getText());
		}
		assertTrue(selected.contains("Alpha"), selected.toString());
		assertTrue(selected.contains("Gamma"), selected.toString());
		List<MindMapNode> byY = opened.view.getSelectedNodesSortedByY();
		assertTrue(byY.size() >= 2, byY.toString());
		int previousY = Integer.MIN_VALUE;
		for (MindMapNode node : byY) {
			NodeView view = opened.view.getNodeView(node);
			int y = view.getContent().getY();
			assertTrue(y >= previousY, byY.toString());
			previousY = y;
		}
	}

	@Test
	void findActionFindsANoteAndSplitsAQuotedTerm() throws Exception {
		Opened opened = openMap("Visible");
		MindMapNode child = opened.child("Visible");
		opened.mode.getFrame().setProperty(FreeMind.RESOURCES_SEARCH_IN_NOTES_TOO, "true");
		opened.mode.setNoteText(child, "hidden token");
		FindAction find = new FindAction(opened.mode);

		assertTrue(find.find(opened.root, List.of("hidden"), false));
		assertEquals("Visible", opened.mode.getSelected().getText());
		assertFalse(find.find(opened.root, List.of("absent"), false));

		java.lang.reflect.Method splitter = FindAction.class
				.getDeclaredMethod("breakSearchTermIntoSubterms", String.class);
		splitter.setAccessible(true);
		@SuppressWarnings("unchecked")
		Collection<String> parts = (Collection<String>) splitter.invoke(find, "alpha \"beta gamma\" delta");
		assertEquals(List.of("alpha", "beta gamma", "delta"), List.copyOf(parts));
	}

	@Test
	void fileFilterAcceptsAMindMapExtension() throws Exception {
		ExampleFileFilter filter = new ExampleFileFilter(new String[] { "mm" }, "Mind maps");
		File dir = Files.createTempDirectory("bespoke-filter").toFile();
		dir.deleteOnExit();
		File map = new File(dir, "map.mm");
		File notes = new File(dir, "notes.txt");
		assertTrue(map.createNewFile());
		assertTrue(notes.createNewFile());
		map.deleteOnExit();
		notes.deleteOnExit();

		assertTrue(filter.accept(dir));
		assertTrue(filter.accept(map));
		assertFalse(filter.accept(notes));
		assertTrue(filter.getDescription().contains(".mm"), filter.getDescription());
	}

	@Test
	void sortedMapVectorKeepsKeysInOrder() {
		SortedMapVector vector = new SortedMapVector();
		assertEquals(0, vector.add("m", "middle"));
		assertEquals(0, vector.add("a", "alpha"));
		assertEquals(2, vector.add("z", "zeta"));
		assertEquals(0, vector.add("a", "other"));
		assertEquals("alpha", vector.getValue(0));
		assertEquals("middle", vector.getValue(1));
		assertEquals("zeta", vector.getValue("z"));
		vector.remove(1);
		assertEquals(2, vector.size());
		assertEquals("zeta", vector.getValue(1));
		vector.clear();
		assertEquals(0, vector.size());
	}

	@Test
	void raisingAChildChangesItsSide() throws Exception {
		Opened opened = openMap("Moved");
		MindMapNode child = opened.child("Moved");
		boolean wasLeft = child.isLeft();
		selectOnly(opened, child);
		ChangeNodeLevelAction hook = new ChangeNodeLevelAction();
		Properties properties = new Properties();
		properties.setProperty("action_type", wasLeft ? "right" : "left");
		bind(hook, opened, properties);

		hook.invoke(opened.root);

		MindMapNode moved = opened.child("Moved");
		assertEquals(!wasLeft, moved.isLeft());
		assertEquals(opened.root, moved.getParentNode());
	}

	@Test
	void newParentWrapsTheSelectedChild() throws Exception {
		Opened opened = openMap("Wrapped");
		selectOnly(opened, opened.child("Wrapped"));
		NewParentNode hook = new NewParentNode();
		bind(hook, opened, new Properties());

		hook.invoke(opened.root);

		assertEquals(1, opened.root.getChildCount());
		MindMapNode created = (MindMapNode) opened.root.getChildAt(0);
		assertNotEquals("Wrapped", created.getText());
		assertEquals(List.of("Wrapped"), childTexts(created));
	}

	@Test
	void splitHookBreaksANodeOnANewline() throws Exception {
		Opened opened = openMap("Line");
		MindMapNode child = opened.child("Line");
		opened.mode.setNodeText(child, "Alpha\nBeta");
		selectOnly(opened, child);
		SplitNode hook = new SplitNode();
		bind(hook, opened, new Properties());

		hook.invoke(opened.root);
		EventQueue.invokeAndWait(() -> {
		});

		assertEquals("Alpha", child.getText());
		assertTrue(childTexts(opened.root).contains("Beta"), childTexts(opened.root).toString());
	}

	@Test
	void hierarchicalIconsCollectTheChildIcon() throws Exception {
		Opened opened = openMap("Leaf");
		opened.mode.addIcon(opened.child("Leaf"), MindIcon.factory("button_ok"));
		HierarchicalIcons hook = new HierarchicalIcons();
		hook.setName("hierarchical");
		bind(hook, opened, new Properties());

		hook.invoke(opened.root);

		assertNotNull(opened.root.getStateIcons().get("hierarchical"));
		assertEquals("button_ok", opened.child("Leaf").getIcons().get(0).getName());
	}

	@Test
	void formatPasteCopiesBoldOntoAnotherNode() throws Exception {
		Opened opened = openMap("Source", "Target");
		MindMapNode source = opened.child("Source");
		MindMapNode target = opened.child("Target");
		opened.mode.setBold(source, true);
		FormatPaste copy = new FormatPaste();
		Properties copyProperties = new Properties();
		copyProperties.setProperty("actionType", "copy_format");
		bind(copy, opened, copyProperties);
		copy.invoke(source);

		FormatPaste paste = new FormatPaste();
		Properties pasteProperties = new Properties();
		pasteProperties.setProperty("actionType", "paste_format");
		bind(paste, opened, pasteProperties);
		paste.invoke(target);

		assertTrue(source.isBold());
		assertTrue(target.isBold());
	}

	@Test
	void unfoldingAllOpensAFoldedChild() throws Exception {
		Opened opened = openMap("Parent");
		MindMapNode parent = opened.child("Parent");
		MindMapNode leaf = opened.mode.addNewNode(parent, 0, false);
		opened.mode.setNodeText(leaf, "Leaf");
		opened.mode.setFolded(parent, true);
		UnfoldAll hook = new UnfoldAll();
		Properties properties = new Properties();
		properties.setProperty("foldingState", "false");
		properties.setProperty("foldingType", "All");
		properties.setProperty("applyTo", "node");
		bind(hook, opened, properties);

		hook.invoke(parent);

		assertFalse(parent.isFolded());
		assertEquals("Leaf", ((MindMapNode) parent.getChildAt(0)).getText());

		properties.setProperty("foldingState", "true");
		hook.setProperties(properties);
		hook.invoke(parent);
		assertTrue(parent.isFolded());
	}

	@Test
	void revisionPluginColorsAnEditedNode() throws Exception {
		Field alreadyUsed = RevisionPlugin.class.getDeclaredField("alreadyUsed");
		alreadyUsed.setAccessible(true);
		alreadyUsed.setBoolean(null, false);
		Opened opened = openMap("Edited");
		RevisionPlugin hook = new RevisionPlugin();
		bind(hook, opened, new Properties());
		hook.invoke(opened.root);

		opened.mode.setNodeText(opened.child("Edited"), "Changed");

		assertEquals(Color.YELLOW, opened.child("Changed").getBackgroundColor());
		alreadyUsed.setBoolean(null, false);
	}

	@Test
	void automaticLayoutAppliesTheDepthColor() throws Exception {
		Field patterns = AutomaticLayout.class.getDeclaredField("patterns");
		patterns.setAccessible(true);
		patterns.set(null, null);
		Opened opened = openMap("Child");
		opened.mode.getFrame().setProperty("automaticFormat_level",
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?><patterns>"
						+ "<pattern name=\"Level1\"><pattern_node_color value=\"#000000\"/></pattern>"
						+ "<pattern name=\"Level2\"><pattern_node_color value=\"#0033ff\"/></pattern>"
						+ "</patterns>");
		AutomaticLayout hook = new AutomaticLayout();
		bind(hook, opened, new Properties());

		hook.invoke(opened.root);

		assertEquals(Tools.xmlToColor("#000000"), opened.root.getColor());
		assertEquals(Tools.xmlToColor("#0033ff"), opened.child("Child").getColor());
		patterns.set(null, null);
	}

	@Test
	void creationModificationStoresATooltip() throws Exception {
		Opened opened = openMap("Dated");
		CreationModificationPlugin hook = new CreationModificationPlugin();
		hook.setName("created");
		Properties properties = new Properties();
		properties.setProperty("tooltip_format", "created {0,date}");
		bind(hook, opened, properties);

		hook.invoke(opened.root);

		String tip = opened.root.getToolTip().get("created");
		assertNotNull(tip);
		assertTrue(tip.startsWith("created "), tip);
		assertNotNull(opened.child("Dated").getToolTip().get("created"));
	}

	@Test
	void htmlExportWithFoldingWritesTheScriptAndAHeading() throws Exception {
		String folding = resourcesProperty("html_export_folding");
		String icons = resourcesProperty("export_icons_in_html");
		try {
			setResourcesProperty("html_export_folding", "html_export_fold_all");
			setResourcesProperty("export_icons_in_html", "true");
			Opened opened = openMap("Child");
			MindMapNode child = opened.child("Child");
			opened.mode.addNewNode(child, 0, false);
			opened.mode.setNodeText((MindMapNode) child.getChildAt(0), "Grand");
			opened.mode.addIcon(child, MindIcon.factory("button_ok"));
			opened.mode.setBold(child, true);
			opened.mode.setLink(child, "notes.mm");
			File folded = File.createTempFile("bespoke-html-fold", ".html");
			folded.deleteOnExit();

			MindMapController.saveHTML(opened.root, folded);

			String html = Files.readString(folded.toPath());
			assertTrue(html.contains("fold_document"), html);
			assertTrue(html.contains("show_folder"), html);
			assertTrue(html.contains("button_ok"), html);
			assertTrue(html.contains("notes.mm.html"), html);
			assertTrue(html.contains("font-weight: bold;"), html);

			setResourcesProperty("html_export_folding", "html_export_based_on_headings");
			File headings = File.createTempFile("bespoke-html-head", ".html");
			headings.deleteOnExit();
			MindMapController.saveHTML(opened.root, headings);
			String headed = Files.readString(headings.toPath());
			assertTrue(headed.contains("<h1>"), headed);
			assertTrue(headed.contains("ROOT"), headed);
			assertTrue(headed.contains("Child"), headed);
		} finally {
			setResourcesProperty("html_export_folding", folding);
			setResourcesProperty("export_icons_in_html", icons);
		}
	}

	@Test
	void directPatternApplySetsFontEdgeIconAndText() throws Exception {
		Opened opened = openMap("Styled");
		MindMapNode node = opened.child("Styled");
		Pattern pattern = new Pattern();
		pattern.setName("Marked");
		PatternNodeColor color = new PatternNodeColor();
		color.setValue(Tools.colorToXml(Color.RED));
		pattern.setPatternNodeColor(color);
		PatternNodeBackgroundColor background = new PatternNodeBackgroundColor();
		background.setValue(Tools.colorToXml(Color.GREEN));
		pattern.setPatternNodeBackgroundColor(background);
		PatternNodeStyle style = new PatternNodeStyle();
		style.setValue("bubble");
		pattern.setPatternNodeStyle(style);
		PatternEdgeColor edgeColor = new PatternEdgeColor();
		edgeColor.setValue(Tools.colorToXml(Color.BLUE));
		pattern.setPatternEdgeColor(edgeColor);
		PatternEdgeStyle edgeStyle = new PatternEdgeStyle();
		edgeStyle.setValue(EdgeAdapter.EDGESTYLE_BEZIER);
		pattern.setPatternEdgeStyle(edgeStyle);
		PatternEdgeWidth edgeWidth = new PatternEdgeWidth();
		edgeWidth.setValue("4");
		pattern.setPatternEdgeWidth(edgeWidth);
		PatternIcon icon = new PatternIcon();
		icon.setValue("button_ok");
		pattern.setPatternIcon(icon);
		PatternNodeText text = new PatternNodeText();
		text.setValue("Renamed");
		pattern.setPatternNodeText(text);
		PatternNodeFontName fontName = new PatternNodeFontName();
		fontName.setValue("Serif");
		pattern.setPatternNodeFontName(fontName);
		PatternNodeFontSize fontSize = new PatternNodeFontSize();
		fontSize.setValue("20");
		pattern.setPatternNodeFontSize(fontSize);
		PatternNodeFontBold bold = new PatternNodeFontBold();
		bold.setValue(StylePatternFactory.TRUE_VALUE);
		pattern.setPatternNodeFontBold(bold);
		PatternNodeFontItalic italic = new PatternNodeFontItalic();
		italic.setValue(StylePatternFactory.TRUE_VALUE);
		pattern.setPatternNodeFontItalic(italic);
		PatternNodeFontStrikethrough strike = new PatternNodeFontStrikethrough();
		strike.setValue(StylePatternFactory.TRUE_VALUE);
		pattern.setPatternNodeFontStrikethrough(strike);

		StylePatternFactory.applyPattern(pattern, node, opened.mode);

		assertEquals("Renamed", node.getText());
		assertEquals(Color.RED, node.getColor());
		assertEquals(Color.GREEN, node.getBackgroundColor());
		assertEquals("bubble", node.getStyle());
		assertEquals(Color.BLUE, node.getEdge().getColor());
		assertEquals(EdgeAdapter.EDGESTYLE_BEZIER, node.getEdge().getStyle());
		assertEquals(4, node.getEdge().getWidth());
		assertEquals("button_ok", node.getIcons().get(0).getName());
		assertEquals("20", node.getFontSize());
		assertTrue(node.isBold());
		assertTrue(node.isItalic());
		assertTrue(node.isStrikethrough());
		assertEquals(new Font("Serif", Font.PLAIN, 20).getFamily(), node.getFont().getFamily());

		MindMapNode styledChild = opened.mode.addNewNode(node, 0, false);
		opened.mode.setNodeText(styledChild, "Child");
		Pattern childPattern = new Pattern();
		childPattern.setName("ChildMark");
		PatternNodeText childText = new PatternNodeText();
		childText.setValue("FromChild");
		childPattern.setPatternNodeText(childText);
		PatternChild childRef = new PatternChild();
		childRef.setValue("ChildMark");
		pattern.setPatternChild(childRef);
		StylePatternFactory.applyPattern(node, pattern, List.of(childPattern), java.util.Set.of(), opened.mode);
		assertEquals("FromChild", styledChild.getText());
		assertEquals("Renamed", node.getText());

		Pattern blanks = new Pattern();
		PatternNodeFontSize blankSize = new PatternNodeFontSize();
		blankSize.setValue(null);
		blanks.setPatternNodeFontSize(blankSize);
		PatternEdgeWidth blankWidth = new PatternEdgeWidth();
		blankWidth.setValue(null);
		blanks.setPatternEdgeWidth(blankWidth);
		PatternNodeFontName blankName = new PatternNodeFontName();
		blankName.setValue(null);
		blanks.setPatternNodeFontName(blankName);
		StylePatternFactory.applyPattern(node, blanks, List.of(), java.util.Set.of(), opened.mode);
		assertEquals(String.valueOf(opened.mode.getDefaultFont().getSize()), node.getFontSize());
		assertEquals(opened.mode.getDefaultFont().getFamily(), node.getFont().getFamily());

		Pattern clearing = new Pattern();
		PatternIcon clearIcon = new PatternIcon();
		clearIcon.setValue(null);
		clearing.setPatternIcon(clearIcon);
		PatternNodeText clearText = new PatternNodeText();
		clearText.setValue(null);
		clearing.setPatternNodeText(clearText);
		PatternEdgeWidth clearWidth = new PatternEdgeWidth();
		clearWidth.setValue(null);
		clearing.setPatternEdgeWidth(clearWidth);
		PatternNodeFontName clearFont = new PatternNodeFontName();
		clearFont.setValue(null);
		clearing.setPatternNodeFontName(clearFont);
		StylePatternFactory.applyPattern(clearing, node, opened.mode);

		assertEquals("", node.getText());
		assertTrue(node.getIcons().isEmpty());
		assertEquals(LineAdapter.DEFAULT_WIDTH, ((EdgeAdapter) node.getEdge()).getRealWidth());
		assertEquals(EdgeAdapter.WIDTH_THIN, node.getEdge().getWidth());
		assertNotNull(node.getFont());
	}

	@Test
	void savedPatternRoundTripsTheNameAndTheColor() throws Exception {
		Pattern pattern = new Pattern();
		pattern.setName("Marked");
		PatternNodeColor color = new PatternNodeColor();
		color.setValue(Tools.colorToXml(Color.RED));
		pattern.setPatternNodeColor(color);
		StringWriter writer = new StringWriter();
		ArrayList<Pattern> patterns = new ArrayList<>();
		patterns.add(pattern);

		StylePatternFactory.savePatterns(writer, patterns);

		List<Pattern> loaded = StylePatternFactory.loadPatterns(new StringReader(writer.toString()));
		assertEquals(1, loaded.size());
		assertEquals("Marked", loaded.get(0).getName());
		assertEquals(Tools.colorToXml(Color.RED), loaded.get(0).getPatternNodeColor().getValue());
	}

	@Test
	void pastingACopyAddsTheChildAgain() throws Exception {
		Opened opened = openMap("Alpha");
		MindMapNode alpha = opened.child("Alpha");
		Transferable copied = opened.mode.copy(alpha, false);

		boolean pasted = opened.mode.paste(copied, opened.root, false, false);

		assertTrue(pasted);
		assertEquals(2, opened.root.getChildCount());
		assertEquals("Alpha", ((MindMapNode) opened.root.getChildAt(0)).getText());
		assertEquals("Alpha", ((MindMapNode) opened.root.getChildAt(1)).getText());
		assertNotEquals(alpha.getObjectId(opened.mode),
				((MindMapNode) opened.root.getChildAt(1)).getObjectId(opened.mode));
	}

	@Test
	void joiningLeavesConcatenatesTheirText() throws Exception {
		Opened opened = openMap("Alpha", "Beta");
		MindMapNode alpha = opened.child("Alpha");
		MindMapNode beta = opened.child("Beta");
		selectOnly(opened, alpha);

		opened.mode.joinNodes(alpha, new ArrayList<>(List.of(alpha, beta)));

		assertEquals("Alpha Beta", alpha.getText());
		assertEquals(List.of("Alpha Beta"), childTexts(opened.root));
	}

	@Test
	void deletingAChildRemovesIt() throws Exception {
		Opened opened = openMap("Alpha", "Beta");

		opened.mode.deleteNode(opened.child("Beta"));

		assertEquals(List.of("Alpha"), childTexts(opened.root));
	}

	@Test
	void undoRestoresThePreviousText() throws Exception {
		Opened opened = openMap("Alpha");
		MindMapNode alpha = opened.child("Alpha");
		opened.mode.setNodeText(alpha, "Changed");
		assertEquals("Changed", alpha.getText());
		assertTrue(opened.mode.undo.isEnabled());

		opened.mode.undo.actionPerformed(new ActionEvent(opened.mode.undo, ActionEvent.ACTION_PERFORMED, "undo"));

		assertEquals("Alpha", alpha.getText());
		assertTrue(opened.mode.redo.isEnabled());
		opened.mode.redo.actionPerformed(new ActionEvent(opened.mode.redo, ActionEvent.ACTION_PERFORMED, "redo"));
		assertEquals("Changed", alpha.getText());
	}

	@Test
	void movingANodeStoresItsGaps() throws Exception {
		Opened opened = openMap("Moved");
		MindMapNode moved = opened.child("Moved");

		opened.mode.moveNodePosition(moved, 12, 30, 7);

		assertEquals(12, opened.root.getVGap());
		assertEquals(30, moved.getHGap());
		assertEquals(7, moved.getShiftY());
	}

	@Test
	void anAttributeRoundTripsOnTheNode() throws Exception {
		Opened opened = openMap("Tagged");
		MindMapNode tagged = opened.child("Tagged");

		int position = opened.mode.addAttribute(tagged, new Attribute("Owner", "Ada"));

		assertEquals(0, position);
		assertEquals("Owner", tagged.getAttribute(0).getName());
		assertEquals("Ada", tagged.getAttribute(0).getValue());
		assertEquals(0, tagged.getAttributePosition("Owner"));
		opened.mode.setAttribute(tagged, 0, new Attribute("Owner", "Grace"));
		assertEquals("Grace", tagged.getAttribute(0).getValue());
		opened.mode.addIcon(tagged, MindIcon.factory("button_ok"));
		opened.mode.addIcon(tagged, MindIcon.factory("button_cancel"));
		opened.mode.removeAllIcons(tagged);
		assertTrue(tagged.getIcons().isEmpty());
		opened.mode.removeAttribute(tagged, 0);
		assertTrue(tagged.getAttributes().isEmpty());
	}

	@Test
	void aShallowCopyKeepsTheText() throws Exception {
		Opened opened = openMap("Original");
		MindMapNode original = opened.child("Original");
		opened.mode.setNoteText(original, "kept");

		MindMapNode copy = original.shallowCopy();

		assertNotNull(copy);
		assertEquals("Original", copy.getText());
		assertNull(copy.getNoteText());
		assertTrue(original.getNoteText().contains("kept"), original.getNoteText());
		assertNotEquals(original, copy);
	}

	@Test
	void arrowEndsAndArrowHeadsStayOnTheLink() throws Exception {
		Opened opened = openMap("From", "To");
		MindMapNode from = opened.child("From");
		MindMapNode to = opened.child("To");
		opened.mode.addLink(from, to);
		MindMapArrowLink link = arrowFrom(opened, from);

		opened.mode.setArrowLinkEndPoints(link, new Point(1, 2), new Point(3, 4));
		opened.mode.changeArrowsOfArrowLink(link, true, false);

		assertEquals(new Point(1, 2), link.getStartInclination());
		assertEquals(new Point(3, 4), link.getEndInclination());
		assertEquals("Default", link.getStartArrow());
		assertEquals("None", link.getEndArrow());
		opened.mode.removeReference(link);
		assertTrue(opened.model.getLinkRegistry().getAllLinks(from).isEmpty());
	}

	@Test
	void selectingABranchKeepsTheChildSelected() throws Exception {
		Opened opened = openMap("Alpha", "Beta");
		opened.view.getRoot().updateAll();
		opened.view.validate();
		NodeView rootView = opened.view.getRoot();
		NodeView alpha = opened.view.getNodeView(opened.child("Alpha"));
		NodeView beta = opened.view.getNodeView(opened.child("Beta"));

		opened.view.selectBranch(rootView, false);
		opened.view.scrollNodeToVisible(alpha, 12);

		assertTrue(opened.view.getSelecteds().contains(alpha));
		assertTrue(opened.view.getSelecteds().contains(beta));
		assertFalse(opened.view.getSingleSelectedNodes().isEmpty());
		Point declined = alpha.getLinkPoint(new Point(10, 4));
		Point flat = alpha.getLinkPoint(new Point(8, 0));
		Point missing = alpha.getLinkPoint(null);
		assertNotNull(declined);
		assertNotNull(flat);
		assertNotNull(missing);
		assertTrue(declined.x != 0 || declined.y != 0);
		opened.view.makeTheSelected(beta);
		assertEquals(beta, opened.view.getSelected());
		int before = opened.view.getSelecteds().size();
		opened.view.toggleSelected(beta);
		assertNotEquals(before, opened.view.getSelecteds().size());
		assertFalse(opened.view.isSelected(beta));
		if (opened.view.isValid()) {
			opened.view.centerNode(alpha);
		}
	}

	@Test
	void xmlElementReadsACdataSection() throws Exception {
		XMLElement xml = new XMLElement();

		xml.parseString("<note><![CDATA[Alpha & Beta]]></note>");

		assertEquals("note", xml.getName());
		assertEquals("Alpha & Beta", xml.getContent());
	}

	@Test
	void arrowPopupListsTheLinkActions() throws Exception {
		Opened opened = openMap("From", "To");
		MindMapNode from = opened.child("From");
		MindMapNode to = opened.child("To");
		opened.mode.addLink(from, to);
		MindMapArrowLinkModel link = (MindMapArrowLinkModel) arrowFrom(opened, from);

		assertNull(opened.mode.getPopupForModel(from));
		JPopupMenu popup = opened.mode.getPopupForModel(link);

		assertNotNull(popup);
		assertTrue(popup.getComponentCount() >= 6, "components " + popup.getComponentCount());
	}

	@Test
	void fileModeListsADirectory() throws Exception {
		HeadlessFreeMind frame = new HeadlessFreeMind();
		HoldingController app = new HoldingController(frame);
		app.initialization();
		FileMode mode = new FileMode();
		mode.init(app);
		File directory = Files.createTempDirectory("bespoke-files").toFile();
		directory.deleteOnExit();
		File listed = new File(directory, "listed.mm");
		assertTrue(listed.createNewFile());
		listed.deleteOnExit();
		FileMapModel model = new FileMapModel(directory, frame, mode.getDefaultModeController());
		FileNodeModel root = (FileNodeModel) model.getRootNode();
		root.setFolded(false);
		List<String> names = new ArrayList<>();
		root.childrenUnfolded().forEachRemaining(node -> names.add(node.getText()));

		assertTrue(names.contains("listed.mm"), names.toString());
		assertFalse(root.isWriteable());
		assertTrue(root.getLink().contains("bespoke-files"), root.getLink());
	}

	@Test
	void printingAFittedPageExistsAndLaterPagesDoNot() throws Exception {
		String fit = resourcesProperty("fit_to_page");
		String wide = resourcesProperty("pages_wide");
		try {
			setResourcesProperty("fit_to_page", "true");
			setResourcesProperty("pages_wide", "1");
			Opened opened = openMap("Alpha");
			opened.mode.getFrame().setProperty("user_zoom", "1");
			opened.view.getRoot().updateAll();
			opened.view.getRoot().doLayout();
			assertTrue(opened.view.getInnerBounds().width > 0);
			assertTrue(opened.view.getInnerBounds().height > 0);

			BufferedImage image = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics = image.createGraphics();
			PageFormat format = new PageFormat();
			int first = opened.view.print(graphics, format, 0);
			int second = opened.view.print(graphics, format, 1);
			assertEquals(Printable.PAGE_EXISTS, first);
			assertEquals(Printable.NO_SUCH_PAGE, second);
			assertFalse(opened.view.isCurrentlyPrinting());

			setResourcesProperty("pages_wide", "2");
			assertEquals(Printable.PAGE_EXISTS, opened.view.print(graphics, format, 0));
			assertEquals(Printable.NO_SUCH_PAGE, opened.view.print(graphics, format, 40));

			setResourcesProperty("fit_to_page", "false");
			assertEquals(Printable.PAGE_EXISTS, opened.view.print(graphics, format, 0));
			graphics.dispose();
		} finally {
			setResourcesProperty("fit_to_page", fit);
			setResourcesProperty("pages_wide", wide);
		}
	}

	@Test
	void keyboardMoveSelectsTheNextNode() throws Exception {
		Opened opened = openMap("Alpha", "Beta", "Gamma");
		opened.view.getRoot().updateAll();
		opened.view.getRoot().doLayout();
		selectOnly(opened, opened.root);

		KeyEvent right = key(opened.view, KeyEvent.VK_RIGHT, 0);
		opened.view.move(right);
		assertTrue(right.isConsumed());
		String moved = opened.view.getSelected().getModel().getText();
		assertTrue(moved.equals("Alpha") || moved.equals("Beta") || moved.equals("Gamma"), moved);

		selectOnly(opened, opened.child("Alpha"));
		KeyEvent down = key(opened.view, KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK);
		opened.view.move(down);
		assertTrue(down.isConsumed());
		assertTrue(opened.view.getSelecteds().size() >= 2);

		KeyEvent left = key(opened.view, KeyEvent.VK_LEFT, InputEvent.SHIFT_DOWN_MASK);
		opened.view.move(left);
		assertFalse(opened.view.getSelecteds().isEmpty());

		KeyEvent up = key(opened.view, KeyEvent.VK_UP, 0);
		opened.view.move(up);
		assertNotNull(opened.view.getSelected());
		KeyEvent pageDown = key(opened.view, KeyEvent.VK_PAGE_DOWN, 0);
		opened.view.move(pageDown);
		assertNotNull(opened.view.getSelected());
		KeyEvent pageUp = key(opened.view, KeyEvent.VK_PAGE_UP, InputEvent.SHIFT_DOWN_MASK);
		opened.view.move(pageUp);
		assertNotNull(opened.view.getSelected());
	}

	@Test
	void countryMapKeepsTheLastCode() {
		HashMap<String, String> countries = Resources.getInstance().getCountryMap();

		assertEquals("DE", countries.get("de"));
		assertEquals("US", countries.get("en"));
		assertEquals("MX", countries.get("es"));
		assertEquals("IT", countries.get("it"));
		assertEquals("FI", countries.get("fi"));
		assertEquals("UA", countries.get("uk"));
		assertEquals("CN", countries.get("zh"));
		assertEquals(14, countries.size());
	}

	@Test
	void toolsRoundTripValues() throws Exception {
		assertNull(Tools.colorToXml(null));
		assertEquals("#ff0000", Tools.colorToXml(Color.RED));
		assertEquals(Color.RED, Tools.xmlToColor("#ff0000"));
		assertNull(Tools.xmlToColor(null));
		assertEquals("3;4;", Tools.PointToXml(new Point(3, 4)));
		assertEquals(new Point(3, 4), Tools.xmlToPoint("3;4;"));
		assertEquals(new Point(-2, 8), Tools.xmlToPoint("java.awt.Point[x=-2,y=8]"));
		assertEquals("true", Tools.BooleanToXml(true));
		assertFalse(Tools.xmlToBoolean("no"));
		assertEquals("a;b;", Tools.listToString(List.of("a", "b")));
		assertEquals(2, Tools.stringToList("a;b;").size());
		assertTrue(Tools.expandFileName("~").startsWith(System.getProperty("user.home")));
		assertEquals("mm", Tools.getExtension("fork.pork.MM"));
		assertEquals("", Tools.getExtension("noextension"));
		assertEquals("fork.pork", Tools.removeExtension("fork.pork.MM"));
		assertTrue(Tools.isAbsolutePath("/tmp/file.mm"));
		assertFalse(Tools.isAbsolutePath("file.mm"));
		assertTrue(Tools.safeEqualsIgnoreCase("Mind", "mind"));
		assertTrue(Tools.safeEqualsIgnoreCase(null, null));
		assertFalse(Tools.safeEqualsIgnoreCase("Mind", null));
		assertTrue(Tools.safeEquals((Color) null, (Color) null));
		assertEquals("Mind", Tools.firstLetterCapitalized("mind"));
		assertNull(Tools.firstLetterCapitalized(null));
		assertEquals("Hello Dolly.", Tools.expandPlaceholders("Hello $1.", "Dolly"));
		assertEquals("one two three", Tools.expandPlaceholders("$1 $2 $3", "one", "two", "three"));
		assertTrue(Tools.safeEquals(new Tools.BooleanHolder(true), new Tools.BooleanHolder(true)));
		assertFalse(Tools.safeEquals(new Tools.BooleanHolder(true), new Tools.BooleanHolder(false)));
		assertTrue(Tools.safeEquals((Tools.BooleanHolder) null, (Tools.BooleanHolder) null));
		String packed = Tools.compress("Alpha Beta");
		assertEquals("Alpha Beta", Tools.decompress(packed));
		assertEquals(2, Tools.countOccurrences("aa-aa", "aa"));
		assertTrue(Tools.compareText("ab", "ac").contains("Difference at 1"));
		assertTrue(Tools.compareText(null, "ac").contains("null"));
		assertEquals(EdgeAdapter.DEFAULT_WIDTH, Tools.edgeWidthStringToInt(null));
		assertEquals(EdgeAdapter.WIDTH_THIN, Tools.edgeWidthStringToInt(EdgeAdapter.EDGE_WIDTH_THIN_STRING));
		assertEquals(4, Tools.edgeWidthStringToInt("4"));
		Paper paper = new Paper();
		paper.setSize(200, 300);
		paper.setImageableArea(10, 20, 150, 220);
		String encoded = Tools.getPageFormatAsString(paper);
		Paper decoded = new Paper();
		Tools.setPageFormatFromString(decoded, encoded);
		assertEquals(200d, decoded.getWidth());
		assertEquals(300d, decoded.getHeight());
		assertEquals(10d, decoded.getImageableX());
		assertEquals(220d, decoded.getImageableHeight());
		Tools.setPageFormatFromString(decoded, "not-a-format");
		assertEquals(200d, decoded.getWidth());
		HashMap<String, String> ids = new HashMap<>();
		ids.put("ID_1", "taken");
		assertEquals("ID_2", Tools.generateID("ID_2", ids, "ID_"));
		String fresh = Tools.generateID("ID_1", ids, "ID_");
		assertNotEquals("ID_1", fresh);
		assertTrue(fresh.startsWith("ID_"));
		Properties changed = new Properties();
		changed.setProperty("keep", "new");
		changed.setProperty("same", "old");
		Properties defaults = new Properties();
		defaults.setProperty("same", "old");
		Properties stored = Tools.copyChangedProperties(changed, defaults);
		assertEquals("new", stored.getProperty("keep"));
		assertNull(stored.getProperty("same"));
		assertTrue(Tools.getStacktrace(new IllegalStateException("marker")).contains("marker"));
		assertTrue(Tools.isHeadless());
		assertTrue(Tools.isLinux());
		assertEquals("Hello", Tools.removeTranslateComment("Hello" + FreeMindCommon.POSTFIX_TRANSLATE_ME));
		URL withFragment = new URL("https://example.test/a#frag");
		assertEquals("https://example.test/a", Tools.getURLWithoutReference(withFragment).toString());
		String urls = Tools.arrayToUrls(new String[] { "/tmp/file.mm" });
		assertTrue(urls.contains("file.mm"), urls);
		assertEquals(1, Tools.urlStringToUrls(urls).size());
		ByteArrayOutputStream copied = new ByteArrayOutputStream();
		Tools.copyStream(new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8)), copied, true);
		assertEquals("abc", copied.toString(StandardCharsets.UTF_8));
		assertTrue(Tools.readFileStart(new StringReader("one\ntwo\n"), 3).toString().startsWith("one"));
		Font scaled = Tools.updateFontSize(new Font("Serif", Font.PLAIN, 12), 2f, 12);
		assertEquals(24f, scaled.getSize2D());
		KeyStroke stroke = Tools.getKeyStroke("control ENTER");
		assertNotNull(stroke);
		assertEquals(KeyEvent.VK_ENTER, stroke.getKeyCode());
		assertNull(Tools.getKeyStroke(null));
		CompoundAction compound = new CompoundAction();
		String printed = Tools.printXmlAction(compound);
		assertTrue(printed.startsWith("CompoundAction"), printed);
		assertTrue(printed.contains("[]"), printed);
		String base = Tools.getFreeMindBasePath();
		assertNotNull(base);
		assertFalse(base.isEmpty());
		File hidden = File.createTempFile("bespoke-hidden", ".mm");
		hidden.deleteOnExit();
		Tools.setHidden(hidden, true, false);
		Tools.makeFileHidden(hidden, true);
		assertTrue(hidden.exists());
		boolean[] ran = { false };
		Tools.invokeAndWait(() -> ran[0] = true);
		assertTrue(ran[0]);

		Opened opened = openMap("Child");
		MindMapNode child = opened.child("Child");
		String hierarchy = Tools.getNodeTextHierarchy(child, opened.mode);
		assertTrue(hierarchy.contains("Child"), hierarchy);
		assertTrue(hierarchy.contains("ROOT"), hierarchy);
		assertTrue(hierarchy.contains(" <- "), hierarchy);
		MindMapNode named = opened.mode.addNewNode(opened.root, 1, false);
		opened.mode.setNodeText(named, "A:/B");
		assertEquals("AB", Tools.getFileNameProposal(named));
		opened.mode.addIcon(child, MindIcon.factory("button_ok"));
		opened.mode.addIcon(child, MindIcon.factory("button_cancel"));
		assertEquals(0, Tools.iconFirstIndex(child, "button_ok"));
		assertEquals(1, Tools.iconLastIndex(child, "button_cancel"));
		assertEquals(-1, Tools.iconFirstIndex(child, "missing"));
	}

	@Test
	void edgeStylesUseTheMatchingPainter() throws Exception {
		Opened opened = openMap("Alpha");
		MindMapNode alpha = opened.child("Alpha");
		String[] styles = { EdgeAdapter.EDGESTYLE_LINEAR, EdgeAdapter.EDGESTYLE_BEZIER,
				EdgeAdapter.EDGESTYLE_SHARP_LINEAR, EdgeAdapter.EDGESTYLE_SHARP_BEZIER };
		String[] painters = { "LinearEdgeView", "BezierEdgeView", "SharpLinearEdgeView", "SharpBezierEdgeView" };
		Method factoryMethod = Class.forName("freemind.view.mindmapview.NodeViewFactory").getDeclaredMethod("getInstance");
		factoryMethod.setAccessible(true);
		Object factory = factoryMethod.invoke(null);
		Method getEdge = factory.getClass().getDeclaredMethod("getEdge", NodeView.class);
		getEdge.setAccessible(true);

		for (int i = 0; i < styles.length; i++) {
			opened.mode.setEdgeStyle(alpha, styles[i]);
			opened.mode.setEdgeWidth(alpha, 4);
			assertEquals(styles[i], alpha.getEdge().getStyle());
			assertEquals(4, alpha.getEdge().getRealWidth());
			opened.view.getRoot().updateAll();
			opened.view.getRoot().doLayout();
			NodeView view = opened.view.getNodeView(alpha);
			Object edge = getEdge.invoke(factory, view);
			assertEquals(painters[i], edge.getClass().getSimpleName());
			paint(opened.view.getRoot());
			assertTrue(opened.view.getInnerBounds().width > 0);
		}
	}

	@Test
	void sortingChildrenReversesAlphabeticalOrder() throws Exception {
		Opened opened = openMap("Alpha", "Zebra");
		selectOnly(opened, opened.root);
		SortNodes sorter = new SortNodes();
		bind(sorter, opened, new Properties());

		sorter.invoke(opened.root);

		assertEquals(List.of("Zebra", "Alpha"), childTexts(opened.root));
	}

	@Test
	void explorerFavoritesImportAUrl() throws Exception {
		Opened opened = openMap("Anchor");
		File folder = Files.createTempDirectory("bespoke-favs").toFile();
		folder.deleteOnExit();
		File url = new File(folder, "Example.url");
		Files.writeString(url.toPath(), "URL=https://example.test/fav\n");
		url.deleteOnExit();
		File empty = new File(folder, "empty");
		assertTrue(empty.mkdir());
		empty.deleteOnExit();

		ImportExplorerFavoritesAction action = new ImportExplorerFavoritesAction(opened.mode);
		boolean found = action.importExplorerFavorites(folder, opened.root, true);

		assertTrue(found);
		assertTrue(childTexts(opened.root).contains("Example"), childTexts(opened.root).toString());
		assertFalse(childTexts(opened.root).contains("empty"), childTexts(opened.root).toString());
		assertEquals("https://example.test/fav", opened.child("Example").getLink());
	}

	@Test
	void htmlContentSplitsIntoTwoHalves() throws Exception {
		Opened opened = openMap("Alpha");
		Method method = MindMapController.class.getDeclaredMethod("getContent", String.class, int.class);
		method.setAccessible(true);

		assertNull(method.invoke(opened.mode, "AlphaBeta", Integer.valueOf(0)));
		String[] plain = (String[]) method.invoke(opened.mode, "AlphaBeta", Integer.valueOf(5));
		assertEquals("Alpha", plain[0]);
		assertEquals("Beta", plain[1]);
		String[] html = (String[]) method.invoke(opened.mode, "<html><body>Alpha Beta</body></html>", Integer.valueOf(5));
		assertNotNull(html);
		assertNotNull(html[0]);
		assertNotNull(html[1]);
		assertTrue(html[0].contains("Alph"), html[0]);
		assertFalse(html[0].contains("Beta"), html[0]);
		assertTrue(html[1].contains("Beta"), html[1]);
		assertTrue(html[0].contains("<html>"), html[0]);
		assertTrue(html[1].contains("<html>"), html[1]);
	}

	@Test
	void savingAStyledNodeWritesItsParts() throws Exception {
		Opened opened = openMap("Saved");
		MindMapNode node = opened.child("Saved");
		opened.mode.setCloud(node, true);
		opened.mode.setCloudColor(node, Color.RED);
		opened.mode.setEdgeStyle(node, EdgeAdapter.EDGESTYLE_SHARP_BEZIER);
		opened.mode.setLink(node, "https://example.test/saved");
		opened.mode.setNoteText(node, "a note");
		opened.mode.setBold(node, true);
		opened.mode.addIcon(node, MindIcon.factory("button_ok"));
		opened.mode.addAttribute(node, new Attribute("Owner", "Ada"));

		StringWriter out = new StringWriter();
		XMLElement saved = node.save(out, opened.model.getLinkRegistry(), true, true);
		saved.write(out);
		String xml = out.toString();

		assertTrue(xml.contains("Saved"), xml);
		assertTrue(xml.contains("cloud"), xml);
		assertTrue(xml.contains("#ff0000"), xml);
		assertTrue(xml.contains("sharp_bezier"), xml);
		assertTrue(xml.contains("https://example.test/saved"), xml);
		assertTrue(xml.contains("button_ok"), xml);
		assertTrue(xml.contains("Ada"), xml);
		assertTrue(xml.contains("BOLD"), xml);
	}

	@Test
	void scrollingAViewportMovesTheOrigin() throws Exception {
		Opened opened = openMap("Alpha");
		opened.view.getParent().remove(opened.view);
		assertNull(opened.view.getParent());
		JViewport viewport = new JViewport();
		viewport.setBounds(0, 0, 400, 300);
		viewport.setView(opened.view);
		opened.view.setBounds(0, 0, 900, 700);
		assertTrue(opened.view.getParent() instanceof JViewport, String.valueOf(opened.view.getParent()));
		assertEquals(900, opened.view.getWidth());
		assertEquals(400, viewport.getWidth());

		opened.view.scrollBy(12, 18);
		assertEquals(12, opened.view.getViewPosition().x, "view " + opened.view.getSize() + " viewport " + viewport.getSize());
		assertEquals(18, opened.view.getViewPosition().y);
		opened.view.setZoom(1.5f);
		assertEquals(1.5f, opened.view.getZoom());
		assertNotNull(opened.view.getAutoscrollInsets());
		opened.view.autoscroll(new Point(30, 40));
		assertTrue(opened.view.getViewPosition().x >= 0);
		assertTrue(opened.view.getViewPosition().y >= 0);
		assertNull(opened.view.detectCollision(new Point(-1000, -1000)));
	}

	@Test
	void theLinuxFileChooserIsTheSwingDialog() {
		FreeMindFileDialog chooser = Resources.getInstance().getStandardFileChooser(null);

		assertNotNull(chooser);
		assertFalse(chooser.getClass().getName().contains("Awt"), chooser.getClass().getName());
	}

	@Test
	void nodeKeyListenerForwardsAnArrow() throws Exception {
		Opened opened = openMap("Alpha", "Beta");
		opened.view.getRoot().updateAll();
		opened.view.getRoot().doLayout();
		selectOnly(opened, opened.root);
		CommonNodeKeyListener listener = new CommonNodeKeyListener(opened.mode, (event, addNew, editLong) -> {
			throw new AssertionError("edit");
		});

		KeyEvent right = key(opened.view, KeyEvent.VK_RIGHT, 0);
		listener.keyPressed(right);
		String moved = opened.view.getSelected().getModel().getText();
		assertTrue(moved.equals("Alpha") || moved.equals("Beta"), moved);

		selectOnly(opened, opened.root);
		KeyEvent alt = key(opened.view, KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK);
		listener.keyPressed(alt);
		assertEquals("ROOT", opened.view.getSelected().getModel().getText());

		listener.keyReleased(key(opened.view, KeyEvent.VK_SHIFT, 0));
		assertEquals("ROOT", opened.view.getSelected().getModel().getText());
	}

	@Test
	void closingAMapWithForceStoresTheLastPlace() throws Exception {
		Opened opened = openMap("Alpha");
		selectOnly(opened, opened.root);
		File mapFile = File.createTempFile("bespoke-close", ".mm");
		mapFile.deleteOnExit();
		opened.model.setFile(mapFile);

		boolean closed = opened.mode.close(true, opened.mode.getController().getMapModuleManager());

		assertTrue(closed);
		String stored = opened.mode.getFrame().getProperty(FreeMindCommon.MINDMAP_LAST_STATE_MAP_STORAGE);
		assertNotNull(stored);
		assertTrue(stored.contains("bespoke-close"), stored);
	}

	@Test
	void searchingAMindMapFileFindsTheNodeText() throws Exception {
		File directory = Files.createTempDirectory("bespoke-search").toFile();
		directory.deleteOnExit();
		File map = new File(directory, "notes.mm");
		Files.writeString(map.toPath(), "<map version=\"1.0.0\"><node TEXT=\"Alpha token\"/></map>\n");
		map.deleteOnExit();

		Search search = new Search(Logger.getLogger("bespoke.search"));
		Search.SearchResult[] results = search.runSearch("token", new File[] { directory });

		assertTrue(results.length >= 1, "hits " + results.length);
		assertTrue(results[0].getPath().contains("notes.mm"), results[0].getPath());
		assertTrue(results[0].showDetails().contains("notes.mm"), results[0].showDetails());
	}

	@Test
	void aRichPatternKeepsEdgeFontIconAndChild() throws Exception {
		String xml = "<patterns><pattern name=\"Rich\">"
				+ "<node color=\"#00ff00\" background_color=\"#0000ff\" style=\"bubble\" text=\"Hello\" icon=\"button_ok\">"
				+ "<font name=\"Serif\" size=\"20\" bold=\"true\" italic=\"true\"/></node>"
				+ "<edge style=\"bezier\" color=\"#ff0000\" width=\"4\"/>"
				+ "<child pattern=\"Rich\"/></pattern></patterns>";
		List<?> patterns = StylePattern.loadPatterns(new StringReader(xml));
		StylePattern pattern = (StylePattern) patterns.get(0);

		assertEquals("Rich", pattern.getName());
		assertEquals(Color.GREEN, pattern.getNodeColor());
		assertEquals(Color.BLUE, pattern.getNodeBackgroundColor());
		assertEquals("bubble", pattern.getNodeStyle());
		assertEquals("Hello", pattern.getText());
		assertEquals("button_ok", pattern.getNodeIcon().getName());
		assertEquals("Serif", pattern.getNodeFontFamily());
		assertEquals(Integer.valueOf(20), pattern.getNodeFontSize());
		assertEquals(Boolean.TRUE, pattern.getNodeFontBold());
		assertEquals(Boolean.TRUE, pattern.getNodeFontItalic());
		assertEquals("bezier", pattern.getEdgeStyle());
		assertEquals(Color.RED, pattern.getEdgeColor());
		assertEquals(Integer.valueOf(4), pattern.getEdgeWidth());
		assertEquals(pattern, pattern.getChildrenStylePattern());
	}

	@Test
	void clickableImageAreasNameTheNodeAndTheLink() throws Exception {
		Opened opened = openMap("Alpha");
		MindMapNode alpha = opened.child("Alpha");
		opened.mode.setLink(alpha, "https://example.test/a");
		opened.view.getRoot().updateAll();
		opened.view.getRoot().doLayout();

		String html = new ClickableImageCreator(opened.root, opened.mode, "{0}|{1}").generateHtml();

		assertTrue(html.contains("<area"), html);
		assertTrue(html.contains("Alpha"), html);
		assertTrue(html.contains("https://example.test/a"), html);
	}

	@Test
	void keyWorkaroundDropsModifiersAndKeepsEnter() throws Exception {
		JPanel source = new JPanel();
		resetKeyWorkaround();
		try {
			assertNull(KeyEventWorkaround.processKeyEvent(key(source, KeyEvent.VK_DEAD_GRAVE, 0)));
			assertNull(KeyEventWorkaround.processKeyEvent(key(source, KeyEvent.VK_ALT, 0)));
			assertNull(KeyEventWorkaround.processKeyEvent(key(source, KeyEvent.VK_A, 0)));
			assertNull(KeyEventWorkaround.processKeyEvent(key(source, KeyEvent.VK_CONTROL, 0)));
			KeyEvent enter = key(source, KeyEvent.VK_ENTER, 0);
			assertSame(enter, KeyEventWorkaround.processKeyEvent(enter));

			KeyEvent pad = key(source, KeyEvent.VK_NUMPAD5, 0);
			assertSame(pad, KeyEventWorkaround.processKeyEvent(pad));
			long now = System.currentTimeMillis();
			keyWorkaroundTime(now);
			KeyEvent digit = new KeyEvent(source, KeyEvent.KEY_TYPED, now, 0, KeyEvent.VK_UNDEFINED, '5');
			assertNull(KeyEventWorkaround.processKeyEvent(digit));
			KeyEventWorkaround.numericKeypadKey();
			KeyEvent digitKept = new KeyEvent(source, KeyEvent.KEY_TYPED, now, 0, KeyEvent.VK_UNDEFINED, '5');
			assertSame(digitKept, KeyEventWorkaround.processKeyEvent(digitKept));

			assertNull(KeyEventWorkaround.processKeyEvent(
					new KeyEvent(source, KeyEvent.KEY_TYPED, now, 0, KeyEvent.VK_UNDEFINED, '\u0001')));
			keyWorkaroundModifiers(InputEvent.SHIFT_MASK);
			keyWorkaroundTime(0L);
			assertNull(KeyEventWorkaround.processKeyEvent(
					new KeyEvent(source, KeyEvent.KEY_TYPED, now, 0, KeyEvent.VK_UNDEFINED, '\n')));

			resetKeyWorkaround();
			assertNull(KeyEventWorkaround.processKeyEvent(key(source, KeyEvent.VK_ALT, 0)));
			KeyEvent left = new KeyEvent(source, KeyEvent.KEY_RELEASED, now, 0, KeyEvent.VK_LEFT,
					KeyEvent.CHAR_UNDEFINED);
			assertSame(left, KeyEventWorkaround.processKeyEvent(left));
			KeyEvent altLetter = new KeyEvent(source, KeyEvent.KEY_TYPED, now, 0, KeyEvent.VK_UNDEFINED, 'B');
			assertNull(KeyEventWorkaround.processKeyEvent(altLetter));
			KeyEvent altUp = new KeyEvent(source, KeyEvent.KEY_RELEASED, now, 0, KeyEvent.VK_ALT,
					KeyEvent.CHAR_UNDEFINED);
			assertNull(KeyEventWorkaround.processKeyEvent(altUp));
			assertTrue(altUp.isConsumed());
		} finally {
			resetKeyWorkaround();
		}
	}

	@Test
	void htmlEscapesWhitespaceAndDropsIllegalCharacters() {
		String expanded = HtmlTools.toXMLEscapedTextExpandingWhitespace(" a <b>\t");
		assertTrue(expanded.contains("&nbsp;"), expanded);
		assertTrue(expanded.contains("&lt;"), expanded);
		assertTrue(expanded.contains("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"), expanded);
		assertTrue(expanded.contains("a "), expanded);
		assertEquals("<", HtmlTools.toXMLUnescapedText("&lt;"));
		assertEquals("ERROR: none", HtmlTools.toXMLEscapedText(null));
		assertTrue(HtmlTools.toXMLEscapedText("\"").contains("&quot;"));
		assertEquals("ab", HtmlTools.makeValidXml("a\0b"));
		String replaced = HtmlTools.replaceIllegalXmlCharacters("&#x0A;X&#x0B;");
		assertTrue(replaced.contains("\n"), replaced);
		assertFalse(replaced.contains("&#"), replaced);
		assertTrue(HtmlTools.isXMLValidCharacter(' '));
		assertTrue(HtmlTools.isXMLValidCharacter('\n'));
		assertFalse(HtmlTools.isXMLValidCharacter('\0'));
		assertTrue(HtmlTools.getInstance().isWellformedXml("<a/>"));
		assertFalse(HtmlTools.getInstance().isWellformedXml("<<"));
	}

	@Test
	void textAndRtfExportWriteTheNodeTheLinkAndTheStyle() throws Exception {
		Opened opened = openMap("Alpha");
		MindMapNodeModel alpha = (MindMapNodeModel) opened.child("Alpha");
		opened.mode.setNodeText(alpha, "A\\B {ä}\nZ");
		opened.mode.setLink(alpha, "https://example.test/a");
		opened.mode.setBold(alpha, true);
		opened.mode.setItalic(alpha, true);
		opened.mode.setNodeColor(alpha, Color.RED);
		opened.mode.setFontFamily(alpha, "Serif");
		opened.mode.setFontSize(alpha, "16");
		opened.mode.setStrikethrough(alpha, true);
		assertTrue(alpha.isStrikethrough());
		MindMapNodeModel blank = new MindMapNodeModel("   ", opened.model);
		alpha.insert(blank, 0);
		MindMapNode folder = opened.mode.addNewNode(alpha, 1, false);
		opened.mode.setNodeText(folder, "Folder");
		MindMapNode hidden = opened.mode.addNewNode(folder, 0, false);
		opened.mode.setNodeText(hidden, "SkipMe");
		opened.mode.setFolded(folder, true);

		StringWriter text = new StringWriter();
		alpha.saveTXT(text, 0);
		String plain = text.toString();
		assertTrue(plain.contains("A\\B"), plain);
		assertTrue(plain.contains("<https://example.test/a>"), plain);
		assertTrue(plain.contains("o"), plain);
		assertTrue(plain.contains("Folder"), plain);
		assertTrue(plain.contains("SkipMe"), plain);

		HashMap<Color, Integer> colors = new HashMap<>();
		colors.put(alpha.getColor(), Integer.valueOf(3));
		StringWriter rtf = new StringWriter();
		alpha.saveRTF(rtf, 9, colors);
		String rich = rtf.toString();
		assertTrue(rich.contains("\\b"), rich);
		assertTrue(rich.contains("\\i"), rich);
		assertTrue(rich.contains("\\strike"), rich);
		assertTrue(rich.contains("\\cf3"), rich);
		assertTrue(rich.contains("\\fs"), rich);
		assertTrue(rich.contains("\\\\"), rich);
		assertTrue(rich.contains("\\{"), rich);
		assertTrue(rich.contains("\\u228?"), rich);
		assertTrue(rich.contains("\\line"), rich);
		assertTrue(rich.contains("\\ul"), rich);
		assertTrue(rich.contains("\\par"), rich);
		assertTrue(rich.contains("\\li3150"), rich);
		assertFalse(rich.contains("\\outlinelevel"), rich);
		assertTrue(rich.contains("o"), rich);
		assertTrue(rich.contains("SkipMe"), rich);
	}

	@Test
	void changingTheRootReparentsTheOldChain() throws Exception {
		Opened opened = openMap("Alpha");
		MindMapNode alpha = opened.child("Alpha");
		MindMapNode beta = opened.mode.addNewNode(alpha, 0, false);
		opened.mode.setNodeText(beta, "Beta");

		opened.model.changeRoot(beta);

		assertEquals(beta, opened.model.getRootNode());
		assertEquals(alpha, beta.getChildAt(0));
		assertEquals(opened.root, alpha.getChildAt(0));
		assertEquals("ROOT", ((MindMapNode) alpha.getChildAt(0)).getText());
	}

	@Test
	void aMindMapLinkTooltipKeepsACustomEntry() throws Exception {
		Opened opened = openMap("Alpha");
		MindMapNode alpha = opened.child("Alpha");
		alpha.setToolTip("custom", "kept");
		opened.mode.setLink(alpha, "https://example.test/a");

		SortedMap<String, String> httpTip = alpha.getToolTip();
		assertEquals("kept", httpTip.get("custom"));

		opened.mode.setLink(alpha, "file:///tmp/bespoke-missing.mm#mark");
		SortedMap<String, String> fileTip = alpha.getToolTip();
		assertEquals("kept", fileTip.get("custom"));
		alpha.setToolTip("custom", null);
		assertFalse(alpha.getToolTip().containsKey("custom"));
	}

	@Test
	void nodeViewNamesItsFirstChildAndPaintsADragHint() throws Exception {
		Opened opened = openMap("Alpha", "Beta");
		MindMapNode alpha = opened.child("Alpha");
		opened.mode.setNodeBackgroundColor(alpha, Color.BLUE);
		opened.view.getRoot().updateAll();
		opened.view.getRoot().doLayout();
		NodeView rootView = opened.view.getRoot();
		NodeView alphaView = opened.view.getNodeView(alpha);

		Method firstMethod = NodeView.class.getDeclaredMethod("getFirst", java.awt.Component.class, boolean.class,
				boolean.class);
		firstMethod.setAccessible(true);
		NodeView first = (NodeView) firstMethod.invoke(rootView, null, false, false);
		assertNotNull(first);
		assertTrue(first.getModel().getText().equals("Alpha") || first.getModel().getText().equals("Beta"));
		Method lastMethod = NodeView.class.getDeclaredMethod("getLast", java.awt.Component.class, boolean.class,
				boolean.class);
		lastMethod.setAccessible(true);
		NodeView last = (NodeView) lastMethod.invoke(rootView, null, false, false);
		assertNotNull(last);
		assertFalse(last.contains(-10, -10));
		assertNotNull(last.getContentPane());
		assertEquals(Color.BLUE, alphaView.getTextBackground());
		assertNotNull(rootView.getTextBackground());

		BufferedImage image = new BufferedImage(80, 24, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, 80, 24);
		MainView childMain = alphaView.getMainView();
		childMain.setBounds(0, 0, 80, 24);
		childMain.setDraggedOver(new Point(0, 0));
		assertTrue(childMain.getDraggedOver() == NodeView.DRAGGED_OVER_SON
				|| childMain.getDraggedOver() == NodeView.DRAGGED_OVER_SIBLING);
		childMain.setDraggedOver(NodeView.DRAGGED_OVER_SON);
		childMain.paintDragOver(graphics);
		alpha.setLeft(true);
		childMain.setDraggedOver(NodeView.DRAGGED_OVER_SON);
		childMain.paintDragOver(graphics);
		childMain.setDraggedOver(NodeView.DRAGGED_OVER_SIBLING);
		childMain.paintDragOver(graphics);
		MainView rootMain = rootView.getMainView();
		rootMain.setBounds(0, 0, 80, 24);
		rootMain.setDraggedOver(NodeView.DRAGGED_OVER_SON);
		rootMain.paintDragOver(graphics);
		rootMain.setDraggedOver(NodeView.DRAGGED_OVER_SON_LEFT);
		rootMain.paintDragOver(graphics);
		graphics.dispose();
		assertEquals(NodeView.DRAGGED_OVER_SON_LEFT, rootMain.getDraggedOver());
		int painted = 0;
		int white = Color.WHITE.getRGB();
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				if (image.getRGB(x, y) != white) {
					painted++;
				}
			}
		}
		assertTrue(painted > 0, "painted " + painted);
	}

	@Test
	void xmlDoubleAttributesRoundTrip() throws Exception {
		XMLElement element = new XMLElement();
		element.setDoubleAttribute("ratio", 1.25);
		assertEquals(1.25, element.getDoubleAttribute("ratio"), 1e-9);
		assertEquals(9.0, element.getDoubleAttribute("missing", 9.0), 1e-9);
		element.parseString("xx<node TEXT=\"Zoom\" zoom=\"2.5\"/>", 2);
		assertEquals("Zoom", element.getStringAttribute("TEXT"));
		assertEquals(2.5, element.getDoubleAttribute("zoom"), 1e-9);
		assertEquals("Zoom", element.getAttribute("TEXT"));
		assertEquals("fallback", element.getAttribute("absent", "fallback"));
		Hashtable<String, String> mapping = new Hashtable<>();
		mapping.put("Zoom", "1");
		mapping.put("other", "0");
		assertEquals("1", element.getAttribute("TEXT", mapping, "other", false));
		assertEquals("literal", element.getAttribute("absent", mapping, "literal", true));
		assertThrows(XMLParseException.class, () -> element.getAttribute("absent", mapping, "literal", false));
		Hashtable<Object, Double> doubles = new Hashtable<>();
		doubles.put("2.5", Double.valueOf(7.0));
		doubles.put("missing", Double.valueOf(3.0));
		assertEquals(7.0, element.getDoubleAttribute("zoom", doubles, "missing", false), 1e-9);
		assertEquals(3.0, element.getDoubleAttribute("absent", doubles, "missing", false), 1e-9);
		assertEquals(2.5, element.getDoubleAttribute("zoom", new Hashtable<>(), "0", true), 1e-9);
	}

	@Test
	void equalAttributesShareAHashCode() {
		Attribute first = new Attribute("Owner", "Ada");
		Attribute second = new Attribute(first);
		assertEquals(first, second);
		assertEquals(first.hashCode(), second.hashCode());
		assertEquals("[Owner, Ada]", first.toString());
		assertFalse(first.equals(new Attribute("Owner", "Grace")));
		assertFalse(first.equals(null));
		assertFalse(first.equals("Owner"));
		Attribute empty = new Attribute(null, null);
		assertEquals(empty, new Attribute(null, null));
		assertEquals(empty.hashCode(), new Attribute(null, null).hashCode());
	}

	@Test
	void pathsRelativeUrlsAndSplitPaneKeysRoundTrip() throws Exception {
		assertTrue(Tools.isAbsolutePath("/tmp/file.mm"));
		assertFalse(Tools.isAbsolutePath("file.mm"));
		assertEquals("/", Tools.getPrefix("/tmp/file.mm"));
		assertNull(Tools.getPrefix("file.mm"));
		URL fileUrl = new URL("file:///tmp/file.mm");
		assertTrue(Tools.urlGetFile(fileUrl).contains("file.mm"), Tools.urlGetFile(fileUrl));
		assertEquals("a.mm", Tools.toRelativeURL(new URL("file:///tmp/maps/"), new URL("file:///tmp/maps/a.mm")));
		assertEquals("../b.mm", Tools.toRelativeURL(new URL("file:///tmp/maps/sub/a.mm"), new URL("file:///tmp/maps/b.mm")));
		assertEquals("sub/b.mm", Tools.toRelativeURL(new URL("file:///tmp/maps/a.mm"), new URL("file:///tmp/maps/sub/b.mm")));
		assertEquals("../d/e.mm", Tools.toRelativeURL(new URL("file:///tmp/a/b/c.mm"), new URL("file:///tmp/a/d/e.mm")));
		URL otherHost = new URL("https://example.test/a");
		assertEquals(otherHost.toString(), Tools.toRelativeURL(new URL("file:///tmp/maps/"), otherHost));
		assertEquals(otherHost.toString(), Tools.toRelativeURL(null, otherHost));
		String read = Tools.getFile(new StringReader("hello"));
		assertTrue(read.startsWith("hello"), read);
		File stored = File.createTempFile("bespoke-read", ".txt");
		stored.deleteOnExit();
		Files.writeString(stored.toPath(), "stored\n");
		assertTrue(Tools.getFile(stored).startsWith("stored"), Tools.getFile(stored));
		File missing = new File(stored.getParentFile(), "bespoke-missing-read.txt");
		assertNull(Tools.getFile(missing));

		InputMap map = (InputMap) UIManager.get("SplitPane.ancestorInputMap");
		assertNotNull(map);
		KeyStroke f6 = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
		KeyStroke f8 = KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0);
		Object f6Action = map.get(f6);
		Object f8Action = map.get(f8);
		Tools.correctJSplitPaneKeyMap();
		assertNull(map.get(f6));
		assertNull(map.get(f8));
		if (f6Action != null) {
			map.put(f6, f6Action);
		}
		if (f8Action != null) {
			map.put(f8, f8Action);
		}
	}

	@Test
	void attributeFiltersMatchTheStoredOwner() throws Exception {
		Opened opened = openMap("Alpha");
		MindMapNode alpha = opened.child("Alpha");
		opened.mode.addAttribute(alpha, new Attribute("Owner", "Ada"));
		ConditionFactory factory = new ConditionFactory();
		int matched = 0;
		int missed = 0;
		for (NamedObject name : factory.getAttributeConditionNames()) {
			for (boolean ignoreCase : new boolean[] { true, false }) {
				Condition condition = factory.createAttributeCondition("Owner", name, "Ada", ignoreCase);
				assertNotNull(condition, String.valueOf(name));
				if (condition.checkNode(opened.mode.getController(), alpha)) {
					matched++;
				} else {
					missed++;
				}
			}
		}
		assertTrue(matched >= 1, "matched " + matched);
		assertTrue(missed >= 1, "missed " + missed);
	}

	@Test
	void loweringANodeMovesItUnderItsSibling() throws Exception {
		Opened opened = openMap("Alpha", "Beta");
		MindMapNode alpha = opened.child("Alpha");
		MindMapNode beta = opened.child("Beta");
		alpha.setLeft(false);
		beta.setLeft(false);
		selectOnly(opened, beta);
		ChangeNodeLevelAction hook = new ChangeNodeLevelAction();
		Properties properties = new Properties();
		properties.setProperty("action_type", "right");
		bind(hook, opened, properties);

		hook.invoke(opened.root);

		assertTrue(childTexts(opened.child("Alpha")).contains("Beta"), childTexts(opened.child("Alpha")).toString());
	}

	@Test
	void raisingAGrandchildMovesItBesideItsParent() throws Exception {
		Opened opened = openMap("Alpha");
		MindMapNode alpha = opened.child("Alpha");
		alpha.setLeft(false);
		MindMapNode beta = opened.mode.addNewNode(alpha, 0, false);
		opened.mode.setNodeText(beta, "Beta");
		beta.setLeft(false);
		selectOnly(opened, beta);
		ChangeNodeLevelAction hook = new ChangeNodeLevelAction();
		Properties properties = new Properties();
		properties.setProperty("action_type", "left");
		bind(hook, opened, properties);

		hook.invoke(opened.root);

		assertEquals(opened.root, opened.child("Beta").getParentNode());
		assertFalse(childTexts(opened.child("Alpha")).contains("Beta"), childTexts(opened.root).toString());
	}

	@Test
	void splittingHtmlParagraphsKeepsBothParts() throws Exception {
		Opened opened = openMap("Line");
		MindMapNode child = opened.child("Line");
		opened.mode.setNodeText(child, "<html><body><p>Alpha</p><p>Beta</p></body></html>");
		selectOnly(opened, child);
		SplitNode hook = new SplitNode();
		bind(hook, opened, new Properties());

		hook.invoke(opened.root);
		EventQueue.invokeAndWait(() -> {
		});

		String combined = child.getText() + " " + childTexts(opened.root);
		assertTrue(combined.contains("Alpha"), combined);
		assertTrue(combined.contains("Beta"), combined);
		assertTrue(opened.root.getChildCount() >= 2, childTexts(opened.root).toString());
	}

	@Test
	void savingTheMapWritesTheNodeText() throws Exception {
		Opened opened = openMap("Alpha");
		File mapFile = File.createTempFile("bespoke-save", ".mm");
		mapFile.deleteOnExit();

		assertTrue(opened.mode.save(mapFile));

		String saved = Files.readString(mapFile.toPath());
		assertTrue(saved.contains("Alpha"), saved);
		assertTrue(saved.contains("<map"), saved);
	}

	@Test
	void unicodeReaderKeepsTheTextBehindTheBom() throws Exception {
		byte[] utf8 = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 'A', 'b' };
		UnicodeReader utf8Reader = new UnicodeReader(new ByteArrayInputStream(utf8), "UTF-8");
		assertEquals("UTF-8", utf8Reader.getDefaultEncoding());
		assertNull(utf8Reader.getEncoding());
		char[] buffer = new char[8];
		int read = utf8Reader.read(buffer, 0, buffer.length);
		assertTrue(read >= 2, "read " + read);
		assertEquals('A', buffer[0]);
		assertEquals('b', buffer[1]);
		assertTrue(utf8Reader.getEncoding().toUpperCase().contains("UTF"), utf8Reader.getEncoding());
		utf8Reader.close();

		byte[] utf16 = new byte[] { (byte) 0xFE, (byte) 0xFF, 0, 'B' };
		UnicodeReader utf16Reader = new UnicodeReader(new ByteArrayInputStream(utf16), "UTF-8");
		char[] wide = new char[4];
		assertTrue(utf16Reader.read(wide, 0, wide.length) >= 1);
		assertEquals('B', wide[0]);
		assertNotNull(utf16Reader.getEncoding());
		assertFalse(utf16Reader.getEncoding().toUpperCase().contains("UTF-8"), utf16Reader.getEncoding());
		utf16Reader.close();

		UnicodeReader plain = new UnicodeReader(new ByteArrayInputStream("Hi".getBytes(StandardCharsets.UTF_8)), null);
		char[] plainBuffer = new char[4];
		assertEquals(2, plain.read(plainBuffer, 0, plainBuffer.length));
		assertEquals('H', plainBuffer[0]);
		assertEquals('i', plainBuffer[1]);
		assertNotNull(plain.getEncoding());
		plain.close();

		byte[] utf16le = new byte[] { (byte) 0xFF, (byte) 0xFE, 'C', 0 };
		UnicodeReader leReader = new UnicodeReader(new ByteArrayInputStream(utf16le), "UTF-8");
		char[] le = new char[2];
		assertTrue(leReader.read(le, 0, 1) >= 1);
		assertEquals('C', le[0]);
		assertEquals(-1, leReader.read(le, 0, 1));
		leReader.close();

		byte[] utf32be = new byte[] { 0, 0, (byte) 0xFE, (byte) 0xFF, 0, 0, 0, 'D' };
		UnicodeReader be32 = new UnicodeReader(new ByteArrayInputStream(utf32be), "UTF-8");
		char[] beChars = new char[2];
		assertTrue(be32.read(beChars, 0, 1) >= 1);
		assertEquals('D', beChars[0]);
		be32.close();

		byte[] utf32le = new byte[] { (byte) 0xFF, (byte) 0xFE, 0, 0, 'E', 0, 0, 0 };
		UnicodeReader le32 = new UnicodeReader(new ByteArrayInputStream(utf32le), "UTF-8");
		char[] leChars = new char[2];
		assertTrue(le32.read(leChars, 0, 1) >= 1);
		assertEquals('E', leChars[0]);
		le32.close();
	}

	@Test
	void aVersionStringKeepsItsNumbersAndItsType() {
		FreeMindMain.VersionInformation release = new FreeMindMain.VersionInformation("1.2.3");
		assertEquals("1.2.3", release.toString());
		assertEquals(FreeMindMain.VERSION_TYPE_RELEASE, release.mType);
		assertEquals("0.9.0 Alpha 1", new FreeMindMain.VersionInformation("0.9.0 Alpha 1").toString());
		assertEquals("1.1.0 Beta 2", new FreeMindMain.VersionInformation("1.1.0 Beta 2").toString());
		assertEquals("2.0.0 RC 3", new FreeMindMain.VersionInformation("2.0.0 RC 3").toString());
		FreeMindMain.VersionInformation built = new FreeMindMain.VersionInformation(3, 1, 4,
				FreeMindMain.VERSION_TYPE_ALPHA, 8);
		assertEquals("3.1.4 Alpha 8", built.toString());
		assertThrows(IllegalArgumentException.class, () -> new FreeMindMain.VersionInformation("1.2"));
		assertThrows(IllegalArgumentException.class, () -> new FreeMindMain.VersionInformation("1.2.3 Gamma 1"));
		built.mType = 9;
		assertThrows(IllegalArgumentException.class, built::toString);
	}

	@Test
	void aPastedArrowKeepsTheEndThatStayedOnTheMap() throws Exception {
		Opened opened = openMap("Stay", "Pasted", "Copy", "Origin", "FromPaste", "Inside");
		MindMapLinkRegistry registry = opened.model.getLinkRegistry();
		NodeAdapter stay = (NodeAdapter) opened.root.getChildAt(0);
		NodeAdapter pasted = (NodeAdapter) opened.root.getChildAt(1);
		NodeAdapter copy = (NodeAdapter) opened.root.getChildAt(2);
		NodeAdapter origin = (NodeAdapter) opened.root.getChildAt(3);
		NodeAdapter fromPaste = (NodeAdapter) opened.root.getChildAt(4);
		NodeAdapter inside = (NodeAdapter) opened.root.getChildAt(5);
		String stayId = registry.registerLinkTarget(stay, "StayId");
		String pastedId = registry.registerLinkTarget(pasted, "PastedId");
		String copyId = registry.registerLinkTarget(copy, "CopyId");
		String originId = registry.registerLinkTarget(origin, "OriginId");
		String insideId = registry.registerLinkTarget(inside, "InsideId");
		MindMapArrowLinkModel kept = new MindMapArrowLinkModel(stay, origin, opened.mode);
		kept.setDestinationLabel(originId);
		kept.setUniqueId("KeptLink");
		registry.registerLink(kept);

		PasteLinks paste = new PasteLinks(opened.mode);
		paste.remember(pastedId, pasted);
		paste.remember(insideId, inside);

		ArrowLinkTarget readded = new ArrowLinkTarget(null, pasted, opened.mode);
		readded.setSourceLabel(stayId);
		readded.setDestinationLabel(pastedId);
		readded.setTarget(pasted);
		paste.add(readded);

		ArrowLinkTarget doubled = new ArrowLinkTarget(null, copy, opened.mode);
		doubled.setSourceLabel(stayId);
		doubled.setUniqueId("KeptLink");
		doubled.setDestinationLabel(originId);
		doubled.setTarget(copy);
		paste.add(doubled);

		ArrowLinkTarget absent = new ArrowLinkTarget(null, origin, opened.mode);
		absent.setSourceLabel(stayId);
		absent.setUniqueId("MissingLink");
		absent.setDestinationLabel(originId);
		absent.setTarget(origin);
		paste.add(absent);

		ArrowLinkTarget bothEnds = new ArrowLinkTarget(null, copy, opened.mode);
		bothEnds.setSourceLabel(insideId);
		bothEnds.setUniqueId("KeptLink");
		bothEnds.setTarget(copy);
		paste.add(bothEnds);

		ArrowLinkTarget nowhereSource = new ArrowLinkTarget(null, pasted, opened.mode);
		nowhereSource.setSourceLabel("NoSource");
		nowhereSource.setTarget(pasted);
		paste.add(nowhereSource);

		MindMapArrowLinkModel staysPut = new MindMapArrowLinkModel(fromPaste, null, opened.mode);
		staysPut.setDestinationLabel(originId);
		paste.add(staysPut);

		MindMapArrowLinkModel lost = new MindMapArrowLinkModel(fromPaste, null, opened.mode);
		lost.setDestinationLabel("Nowhere");
		paste.add(lost);

		MindMapArrowLinkModel renamed = new MindMapArrowLinkModel(fromPaste, null, opened.mode);
		renamed.setDestinationLabel(pastedId);
		paste.add(renamed);

		paste.finish(registry);

		assertEquals(2, registry.getAllSources(pasted).size());
		assertTrue(registry.getAllSources(pasted).contains(stay));
		assertTrue(registry.getAllSources(pasted).contains(fromPaste));
		assertTrue(registry.getAllLinksFromMe(inside).isEmpty());
		assertEquals(pasted, renamed.getTarget());
		assertEquals(pastedId, renamed.getDestinationLabel());
		MindMapLink doubledLink = null;
		for (MindMapLink link : registry.getAllLinksIntoMe(copy)) {
			if (link.getSource() == stay) {
				doubledLink = link;
			}
		}
		assertNotNull(doubledLink);
		assertEquals(copyId, doubledLink.getDestinationLabel());
		assertNotEquals("KeptLink", doubled.getUniqueId());
		assertEquals(1, registry.getAllLinksIntoMe(copy).size());
		assertEquals(fromPaste, staysPut.getSource());
		assertEquals(origin, staysPut.getTarget());
		assertEquals(originId, staysPut.getDestinationLabel());
		assertEquals(2, registry.getAllLinksIntoMe(origin).size());
		assertNull(lost.getTarget());
		assertFalse(registry.getAllLinksFromMe(fromPaste).contains(lost));
	}

	@Test
	void anArrowPaintsFromTheEndThatIsVisible() throws Exception {
		Opened opened = openMap("From", "To");
		MindMapNode from = opened.child("From");
		MindMapNode to = opened.child("To");
		((NodeAdapter) from).setLeft(true);
		((NodeAdapter) to).setLeft(false);
		opened.view.getRoot().updateAll();
		opened.view.getRoot().doLayout();
		MindMapArrowLinkModel link = new MindMapArrowLinkModel(from, to, opened.mode);
		link.setColor(Color.RED);
		link.setWidth(0);

		ArrowLinkView hidden = arrowView(link, null, null);
		assertEquals(new Rectangle(), hidden.getBounds());
		assertFalse(hidden.detectCollision(new Point(1, 1)));
		assertEquals(1, hidden.getRealWidth());
		Stroke thin = hidden.getStroke();
		assertSame(thin, hidden.getStroke());
		assertEquals(1f, ((BasicStroke) thin).getLineWidth());
		BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
		hidden.paint(graphics);
		assertEquals(new Rectangle(), hidden.getBounds());
		assertEquals(0, redPixels(image));

		link.setWidth(2);
		link.setStartArrow("Default");
		link.setEndArrow("None");
		link.setStartInclination(null);
		link.setEndInclination(null);
		NodeView fromView = opened.view.getNodeView(from);
		ArrowLinkView sourceOnly = arrowView(link, fromView, null);
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
		sourceOnly.paint(graphics);
		assertEquals(new Point(30, 0), link.getStartInclination());
		assertNull(link.getEndInclination());
		assertTrue(sourceOnly.getBounds().width > 0 || sourceOnly.getBounds().height > 0,
				sourceOnly.getBounds().toString());
		assertTrue(redPixels(image) > 0, "source " + redPixels(image));

		link.setStartArrow("None");
		link.setEndArrow("Default");
		link.setEndInclination(null);
		((NodeAdapter) to).setLeft(true);
		NodeView toView = opened.view.getNodeView(to);
		ArrowLinkView targetOnly = arrowView(link, null, toView);
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
		targetOnly.paint(graphics);
		assertEquals(new Point(30, 0), link.getEndInclination());
		assertTrue(redPixels(image) > 0, "target " + redPixels(image));

		link.setWidth(4);
		link.setStartArrow("Default");
		link.setEndArrow("Default");
		link.setStartInclination(new Point(20, 8));
		link.setEndInclination(new Point(12, -6));
		link.showControlPoints(true);
		ArrowLinkView both = arrowView(link, fromView, toView);
		assertEquals(4, both.getRealWidth());
		assertEquals(4f, ((BasicStroke) both.getStroke()).getLineWidth());
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
		both.paint(graphics);
		graphics.dispose();
		assertEquals(new Point(20, 8), link.getStartInclination());
		assertEquals(new Point(12, -6), link.getEndInclination());
		assertTrue(redPixels(image) > 0, "both " + redPixels(image));
		Point start = fromView.getLinkPoint(link.getStartInclination());
		assertTrue(both.detectCollision(start), start.toString());
		assertFalse(both.detectCollision(new Point(-4000, -4000)));
	}

	@Test
	void aTypedKeyAsksToEditAndAMoveLetterChangesTheSelection() throws Exception {
		Opened opened = openMap("Alpha", "Beta", "Gamma");
		MindMapNode beta = opened.child("Beta");
		opened.mode.addNewNode(beta, 0, false);
		opened.mode.setNodeText((MindMapNode) beta.getChildAt(0), "Child");
		opened.view.getRoot().updateAll();
		opened.view.getRoot().doLayout();
		List<String> edits = new ArrayList<>();
		FreeMindMain originalFrame = opened.mode.getController().getFrame();
		opened.mode.getController().setFrame(new HeadlessFreeMind() {
			public String getAdjustableProperty(String label) {
				if ("keystroke_move_up".equals(label)) {
					return "E";
				}
				if ("keystroke_move_down".equals(label)) {
					return "D";
				}
				if ("keystroke_move_left".equals(label)) {
					return "S";
				}
				if ("keystroke_move_right".equals(label)) {
					return "F";
				}
				return null;
			}
		});
		CommonNodeKeyListener listener;
		try {
			listener = new CommonNodeKeyListener(opened.mode, (event, addNew, editLong) -> {
				edits.add(event.getKeyCode() + ":" + event.getKeyChar() + ":" + addNew + ":" + editLong);
			});
		} finally {
			opened.mode.getController().setFrame(originalFrame);
		}

		selectOnly(opened, beta);
		listener.keyPressed(new KeyEvent(opened.view, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0,
				KeyEvent.VK_A, 'a'));
		assertEquals(List.of(KeyEvent.VK_A + ":a:false:false"), edits);
		assertEquals("Beta", opened.view.getSelected().getModel().getText());

		listener.keyPressed(new KeyEvent(opened.view, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
				InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_A, 'a'));
		assertEquals(1, edits.size());

		listener.keyPressed(key(opened.view, KeyEvent.VK_HOME, 0));
		listener.keyPressed(key(opened.view, KeyEvent.VK_END, 0));
		listener.keyPressed(key(opened.view, KeyEvent.VK_BACK_SPACE, 0));
		assertEquals(KeyEvent.VK_HOME + ":" + KeyEvent.CHAR_UNDEFINED + ":false:false", edits.get(1));
		assertEquals(KeyEvent.VK_END + ":" + KeyEvent.CHAR_UNDEFINED + ":false:false", edits.get(2));
		assertEquals(KeyEvent.VK_BACK_SPACE + ":" + KeyEvent.CHAR_UNDEFINED + ":false:false", edits.get(3));
		assertEquals("Beta", beta.getText());

		selectOnly(opened, beta);
		listener.keyPressed(key(opened.view, KeyEvent.VK_E, 0));
		assertEquals("Alpha", opened.view.getSelected().getModel().getText());
		selectOnly(opened, beta);
		listener.keyPressed(key(opened.view, KeyEvent.VK_D, 0));
		assertEquals("Gamma", opened.view.getSelected().getModel().getText());
		selectOnly(opened, beta);
		listener.keyPressed(key(opened.view, KeyEvent.VK_S, 0));
		assertEquals("ROOT", opened.view.getSelected().getModel().getText());
		selectOnly(opened, beta);
		listener.keyPressed(key(opened.view, KeyEvent.VK_F, 0));
		assertEquals("Child", opened.view.getSelected().getModel().getText());
	}

	@Test
	void aCloneFilterCopiesTheActionOntoTheOtherClone() throws Exception {
		Opened opened = openMap("Alpha", "Beta");
		MindMapNode alpha = opened.child("Alpha");
		MindMapNode beta = opened.child("Beta");
		opened.mode.addNewNode(alpha, 0, false);
		opened.mode.setNodeText((MindMapNode) alpha.getChildAt(0), "ChildA");
		opened.mode.addNewNode(beta, 0, false);
		opened.mode.setNodeText((MindMapNode) beta.getChildAt(0), "ChildB");
		MindMapNode childA = (MindMapNode) alpha.getChildAt(0);
		MindMapNode childB = (MindMapNode) beta.getChildAt(0);
		String alphaId = opened.mode.getNodeID(alpha);
		String betaId = opened.mode.getNodeID(beta);
		String childAId = opened.mode.getNodeID(childA);
		String childBId = opened.mode.getNodeID(childB);
		String rootId = opened.mode.getNodeID(opened.root);
		ClonePasteAction.Registration registration = new ClonePasteAction.Registration(opened.mode, opened.model);
		registration.register();
		try {
			FoldAction untouched = new FoldAction();
			untouched.setNode(childAId);
			assertSame(untouched, cloneFiltered(registration, untouched));

			String cloneId = registration.generateNewCloneId(null);
			ClonePlugin first = clonePlugin(opened, alpha);
			ClonePlugin second = clonePlugin(opened, beta);
			assertTrue(registration.registerClone(cloneId, first));
			assertFalse(registration.registerClone(cloneId, second));

			FoldAction onClone = new FoldAction();
			onClone.setNode(alphaId);
			assertSame(onClone, cloneFiltered(registration, onClone));

			FoldAction fold = new FoldAction();
			fold.setNode(childAId);
			CompoundAction foldCompound = (CompoundAction) cloneFiltered(registration, fold);
			assertEquals(childBId, ((FoldAction) foldCompound.getListChoiceList().get(0)).getNode());
			assertEquals(childAId, ((FoldAction) foldCompound.getListChoiceList().get(1)).getNode());

			NewNodeAction created = new NewNodeAction();
			created.setNode(childAId);
			created.setNewId("KEEP");
			CompoundAction createdCompound = (CompoundAction) cloneFiltered(registration, created);
			NewNodeAction copiedNew = (NewNodeAction) createdCompound.getListChoiceList().get(0);
			assertEquals(childBId, copiedNew.getNode());
			assertNotNull(copiedNew.getNewId());
			assertNotEquals("KEEP", copiedNew.getNewId());
			assertEquals("KEEP", created.getNewId());
			assertEquals(childAId, ((NewNodeAction) createdCompound.getListChoiceList().get(1)).getNode());

			NewNodeAction onAlpha = new NewNodeAction();
			onAlpha.setNode(alphaId);
			onAlpha.setNewId("ALPHA");
			CompoundAction alphaNew = (CompoundAction) cloneFiltered(registration, onAlpha);
			NewNodeAction copiedAlpha = (NewNodeAction) alphaNew.getListChoiceList().get(0);
			assertEquals(betaId, copiedAlpha.getNode());
			assertNotEquals("ALPHA", copiedAlpha.getNewId());

			PasteNodeAction paste = new PasteNodeAction();
			paste.setNode(alphaId);
			paste.setAsSibling(false);
			CompoundAction pasteCompound = (CompoundAction) cloneFiltered(registration, paste);
			assertEquals(alphaId, ((PasteNodeAction) pasteCompound.getListChoiceList().get(0)).getNode());
			assertEquals(betaId, ((PasteNodeAction) pasteCompound.getListChoiceList().get(1)).getNode());
			PasteNodeAction sibling = new PasteNodeAction();
			sibling.setNode(alphaId);
			sibling.setAsSibling(true);
			assertSame(sibling, cloneFiltered(registration, sibling));

			UndoPasteNodeAction undoSibling = new UndoPasteNodeAction();
			undoSibling.setNode(alphaId);
			undoSibling.setAsSibling(true);
			assertSame(undoSibling, cloneFiltered(registration, undoSibling));
			UndoPasteNodeAction undoChild = new UndoPasteNodeAction();
			undoChild.setNode(alphaId);
			undoChild.setAsSibling(false);
			CompoundAction undoCompound = (CompoundAction) cloneFiltered(registration, undoChild);
			assertEquals(betaId, ((UndoPasteNodeAction) undoCompound.getListChoiceList().get(0)).getNode());

			MoveNodesAction move = new MoveNodesAction();
			move.setNode(childAId);
			move.setDirection(1);
			NodeListMember member = new NodeListMember();
			member.setNode(childAId);
			move.addNodeListMember(member);
			NodeListMember stranger = new NodeListMember();
			stranger.setNode(rootId);
			move.addNodeListMember(stranger);
			CompoundAction moveCompound = (CompoundAction) cloneFiltered(registration, move);
			MoveNodesAction copiedMove = (MoveNodesAction) moveCompound.getListChoiceList().get(0);
			assertEquals(childBId, copiedMove.getNode());
			assertEquals(1, copiedMove.getDirection());
			assertEquals(childBId, copiedMove.getNodeListMember(0).getNode());
			assertEquals(rootId, copiedMove.getNodeListMember(1).getNode());

			HookNodeAction hookAction = new HookNodeAction();
			hookAction.setNode(childAId);
			hookAction.setHookName(ClonePlugin.PLUGIN_LABEL);
			NodeListMember hookMember = new NodeListMember();
			hookMember.setNode(childAId);
			hookAction.addNodeListMember(hookMember);
			CompoundAction hookCompound = (CompoundAction) cloneFiltered(registration, hookAction);
			HookNodeAction copiedHook = (HookNodeAction) hookCompound.getListChoiceList().get(0);
			assertEquals(childBId, copiedHook.getNode());
			assertEquals(childBId, copiedHook.getNodeListMember(0).getNode());
			assertEquals(ClonePlugin.PLUGIN_LABEL, copiedHook.getHookName());

			CompoundAction outer = new CompoundAction();
			FoldAction inner = new FoldAction();
			inner.setNode(childAId);
			outer.addChoice(inner);
			assertSame(outer, cloneFiltered(registration, outer));
			CompoundAction rewritten = (CompoundAction) outer.getListChoiceList().get(0);
			assertEquals(childBId, ((FoldAction) rewritten.getListChoiceList().get(0)).getNode());

			registration.getCloneProperties(cloneId).setCloneItself(true);
			FoldAction clonedFold = new FoldAction();
			clonedFold.setNode(alphaId);
			CompoundAction itself = (CompoundAction) cloneFiltered(registration, clonedFold);
			assertEquals(betaId, ((FoldAction) itself.getListChoiceList().get(0)).getNode());
			assertEquals(alphaId, ((FoldAction) itself.getListChoiceList().get(1)).getNode());
			MoveNodesAction moveSelf = new MoveNodesAction();
			moveSelf.setNode(alphaId);
			moveSelf.setDirection(-1);
			assertSame(moveSelf, cloneFiltered(registration, moveSelf));
			DeleteNodeAction deleteSelf = new DeleteNodeAction();
			deleteSelf.setNode(alphaId);
			assertSame(deleteSelf, cloneFiltered(registration, deleteSelf));
			CutNodeAction cutSelf = new CutNodeAction();
			cutSelf.setNode(alphaId);
			assertSame(cutSelf, cloneFiltered(registration, cutSelf));

			JMenuItem item = new JMenuItem();
			NodeHookAction other = new NodeHookAction("other", opened.mode);
			assertFalse(registration.isEnabled(item, other));
			Field activated = NodeAdapter.class.getDeclaredField("activatedHooks");
			activated.setAccessible(true);
			HashSet<PermanentNodeHook> hooks = new HashSet<>();
			hooks.add(first);
			activated.set(alpha, hooks);
			selectOnly(opened, alpha);
			assertTrue(registration.isEnabled(item, other));

			Field marked = ClonePasteAction.Registration.class.getDeclaredField("mLastMarkedNodeViews");
			marked.setAccessible(true);
			@SuppressWarnings("unchecked")
			Vector<MindMapNode> markedNodes = (Vector<MindMapNode>) marked.get(registration);
			registration.onFocusNode(null);
			assertTrue(markedNodes.contains(beta));
			registration.onFocusNode(opened.view.getRoot());
			assertTrue(markedNodes.isEmpty());
			registration.onFocusNode(opened.view.getNodeView(childA));
			assertTrue(markedNodes.contains(childB));
			assertTrue(((NodeAdapter) childB).getStateIcons().containsKey(ClonePlugin.PLUGIN_LABEL));
			registration.onLostFocusNode(opened.view.getNodeView(childA));
			assertTrue(markedNodes.isEmpty());
			assertFalse(((NodeAdapter) childB).getStateIcons().containsKey(ClonePlugin.PLUGIN_LABEL));
		} finally {
			registration.deRegister();
		}

		ClonePlugin stored = clonePlugin(opened, alpha);
		XMLElement raw = new XMLElement();
		XMLElement parameters = new XMLElement();
		parameters.setName("Parameters");
		parameters.setAttribute(ClonePlugin.XML_STORAGE_CLONES, betaId + ",missing-id");
		parameters.setAttribute(ClonePlugin.XML_STORAGE_CLONE_ID, "CLONE_saved");
		parameters.setAttribute(ClonePlugin.XML_STORAGE_CLONE_ITSELF, ClonePlugin.CLONE_ITSELF_TRUE);
		raw.addChild(parameters);
		stored.loadFrom(raw);
		Method clonesOf = ClonePlugin.class.getDeclaredMethod("getCloneNodes");
		clonesOf.setAccessible(true);
		@SuppressWarnings("unchecked")
		HashSet<MindMapNode> clones = (HashSet<MindMapNode>) clonesOf.invoke(stored);
		assertTrue(clones.contains(alpha));
		assertTrue(clones.contains(beta));
		assertEquals(2, clones.size());
		assertFalse(stored.getCloneIdsAsString().contains("missing-id"), stored.getCloneIdsAsString());
		assertTrue(stored.getCloneIdsAsString().contains(betaId), stored.getCloneIdsAsString());
		assertEquals(clones, clonesOf.invoke(stored));
		stored.onCreateNodeHook(opened.root);
		@SuppressWarnings("unchecked")
		HashSet<MindMapNode> afterCreate = (HashSet<MindMapNode>) clonesOf.invoke(stored);
		assertTrue(afterCreate.contains(beta));

		ClonePasteAction.CloneProperties properties = new ClonePasteAction.CloneProperties();
		properties.setCloneItself(true);
		Field holder = ClonePlugin.class.getDeclaredField("mClonePropertiesHolder");
		holder.setAccessible(true);
		holder.set(stored, properties);
		stored.setName(ClonePlugin.PLUGIN_LABEL);
		XMLElement saved = new XMLElement();
		stored.save(saved);
		assertEquals(ClonePlugin.PLUGIN_LABEL, saved.getStringAttribute("name"));
		XMLElement written = (XMLElement) saved.getChildren().get(0);
		assertEquals(ClonePlugin.CLONE_ITSELF_TRUE, written.getStringAttribute(ClonePlugin.XML_STORAGE_CLONE_ITSELF));
		assertEquals("CLONE_saved", written.getStringAttribute(ClonePlugin.XML_STORAGE_CLONE_ID));
		assertTrue(written.getStringAttribute(ClonePlugin.XML_STORAGE_CLONES).contains(betaId));
		properties.setCloneItself(false);
		stored.propertiesChanged(properties);
		Field itself = ClonePlugin.class.getDeclaredField("mCloneItself");
		itself.setAccessible(true);
		assertEquals(Boolean.FALSE, itself.get(stored));
	}

	@Test
	void generatedActionsKeepTheValuesTheyAreGiven() throws Exception {
		ObjectFactory factory = new ObjectFactory();
		int created = 0;
		int roundTrips = 0;
		for (Method creator : ObjectFactory.class.getDeclaredMethods()) {
			if (!creator.getName().startsWith("create") || creator.getParameterCount() != 0) {
				continue;
			}
			creator.setAccessible(true);
			Object value = creator.invoke(factory);
			assertNotNull(value, creator.getName());
			created++;
			for (Method setter : value.getClass().getMethods()) {
				if (!setter.getName().startsWith("set") || setter.getParameterCount() != 1) {
					continue;
				}
				Object argument = sampleArgument(setter.getParameterTypes()[0]);
				if (argument == null) {
					continue;
				}
				setter.invoke(value, argument);
				Method getter = beanGetter(value.getClass(), setter.getName().substring(3));
				if (getter == null) {
					continue;
				}
				assertEquals(argument, getter.invoke(value), setter.getName());
				roundTrips++;
			}
		}
		assertTrue(created > 50, "created " + created);
		assertTrue(roundTrips > 50, "round trips " + roundTrips);
	}

	@Test
	void plainClickFoldsANodeThatHasAChild() throws Exception {
		Opened opened = openMap("Alpha");
		MindMapNode alpha = opened.child("Alpha");
		opened.mode.addNewNode(alpha, 0, false);
		opened.mode.setNodeText((MindMapNode) alpha.getChildAt(0), "Child");
		selectOnly(opened, alpha);
		MouseEvent click = new MouseEvent(opened.view.getNodeView(alpha).getMainView(), MouseEvent.MOUSE_CLICKED,
				System.currentTimeMillis(), 0, 5, 5, 1, false, MouseEvent.BUTTON1);

		opened.mode.plainClick(click);

		assertTrue(alpha.isFolded());
		assertEquals("Child", ((MindMapNode) alpha.getChildAt(0)).getText());
	}

	@Test
	void extendSelectionKeepsTheClickedNode() throws Exception {
		Opened opened = openMap("Alpha", "Beta");
		opened.view.getRoot().updateAll();
		opened.view.getRoot().doLayout();
		selectOnly(opened, opened.root);
		NodeView alphaView = opened.view.getNodeView(opened.child("Alpha"));
		MouseEvent control = new MouseEvent(alphaView.getMainView(), MouseEvent.MOUSE_CLICKED,
				System.currentTimeMillis(), InputEvent.CTRL_DOWN_MASK, 5, 5, 1, false, MouseEvent.BUTTON1);

		assertTrue(opened.mode.extendSelection(control));

		assertTrue(control.isConsumed());
		assertTrue(opened.view.isSelected(alphaView));
		NodeView betaView = opened.view.getNodeView(opened.child("Beta"));
		MouseEvent shift = new MouseEvent(betaView.getMainView(), MouseEvent.MOUSE_CLICKED,
				System.currentTimeMillis(), InputEvent.SHIFT_DOWN_MASK, 5, 5, 1, false, MouseEvent.BUTTON1);
		opened.mode.extendSelection(shift);
		assertFalse(opened.view.getSelecteds().isEmpty());
		MouseEvent alt = new MouseEvent(alphaView.getMainView(), MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(),
				InputEvent.ALT_DOWN_MASK, 5, 5, 1, false, MouseEvent.BUTTON1);
		assertTrue(opened.mode.extendSelection(alt));
		assertTrue(opened.view.isSelected(alphaView));
	}

	@Test
	void changingAnArrowInclinationMovesItsStart() throws Exception {
		Opened opened = openMap("From", "To");
		MindMapNode from = opened.child("From");
		opened.mode.addLink(from, opened.child("To"));
		opened.view.getRoot().updateAll();
		opened.view.getRoot().doLayout();
		MindMapArrowLinkModel link = (MindMapArrowLinkModel) arrowFrom(opened, from);
		opened.mode.setArrowLinkEndPoints(link, new Point(1, 2), new Point(3, 4));

		link.changeInclination(opened.view, 0, 0, 40, 15);

		Point start = link.getStartInclination();
		Point end = link.getEndInclination();
		assertNotNull(start);
		assertNotNull(end);
		assertTrue(start.x != 1 || start.y != 2 || end.x != 3 || end.y != 4,
				start.toString() + " " + end.toString());
	}

	@Test
	void creatingAHierarchicalIconHookAttachesIt() throws Exception {
		Opened opened = openMap("Alpha");
		String hookName = null;
		for (String candidate : opened.mode.getHookFactory().getPossibleNodeHooks()) {
			if (candidate.contains("HierarchicalIcons")) {
				hookName = candidate;
			}
		}
		assertNotNull(hookName, opened.mode.getHookFactory().getPossibleNodeHooks().toString());

		NodeHook hook = opened.mode.createNodeHook(hookName, opened.root);

		assertNotNull(hook);
		assertEquals(hookName, hook.getName());
		assertTrue(opened.root.getHooks().contains(hook));
		opened.root.removeHook((PermanentNodeHook) hook);
		assertFalse(opened.root.getHooks().contains(hook));
	}

	@Test
	void pastingLinesAddsALinkAChildAndAnAddress() throws Exception {
		Opened opened = openMap("Alpha");
		String clipboard = "http://www.example.test/path\n    Nested\nada@example.test";

		opened.mode.paste(new StringSelection(clipboard), opened.root);

		String texts = allTexts(opened.root).toString();
		assertTrue(texts.contains("example.test"), texts);
		assertTrue(texts.contains("Nested"), texts);
		assertTrue(texts.contains("ada@example.test"), texts);
	}

	@Test
	void addingHierarchicalIconsCopiesTheChildIcon() throws Exception {
		Opened opened = openMap("Alpha");
		MindMapNode alpha = opened.child("Alpha");
		alpha.addIcon(MindIcon.factory("idea"), MindIcon.LAST);
		String hookName = hierarchicalHookName(opened);
		List<MindMapNode> selected = new ArrayList<>();
		selected.add(opened.root);

		opened.mode.addHook(opened.root, selected, hookName, null);

		assertTrue(opened.root.getActivatedHooks().stream().anyMatch(hook -> hookName.equals(hook.getName())));
		assertNotNull(opened.root.getStateIcons().get(hookName));
		opened.mode.removeHook(opened.root, selected, hookName);
		assertFalse(opened.root.getHooks().stream().anyMatch(hook -> hookName.equals(hook.getName())));
		assertNull(opened.root.getStateIcons().get(hookName));
	}

	@Test
	void pastingMindMapHtmlFileAndImageAddsNodes() throws Exception {
		Opened opened = openMap("Alpha");
		MindMapNodesSelection nodes = new MindMapNodesSelection("<node TEXT=\"PastedNode\"/>", null, null, null, null,
				null, null, null);
		opened.mode.paste(nodes, opened.root);
		assertTrue(allTexts(opened.root).contains("PastedNode"), allTexts(opened.root).toString());

		String html = "<html><body><a href=\"https://example.test/x\">LinkText</a></body></html>";
		opened.mode.paste(new MindMapNodesSelection(null, null, null, null, html, null, null, null), opened.root);
		String htmlTexts = allTexts(opened.root).toString();
		assertTrue(htmlTexts.contains("LinkText"), htmlTexts);

		File listed = File.createTempFile("bespoke-listed", ".mm");
		listed.deleteOnExit();
		opened.mode.paste(new MindMapNodesSelection(null, null, null, null, null, null, List.of(listed), null),
				opened.root);
		assertTrue(allTexts(opened.root).contains(listed.getName()), allTexts(opened.root).toString());

		File mapFile = File.createTempFile("bespoke-image", ".mm");
		mapFile.deleteOnExit();
		opened.model.setFile(mapFile);
		BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
		Transferable imageTransfer = new Transferable() {
			public DataFlavor[] getTransferDataFlavors() {
				return new DataFlavor[] { DataFlavor.imageFlavor };
			}

			public boolean isDataFlavorSupported(DataFlavor flavor) {
				return DataFlavor.imageFlavor.equals(flavor);
			}

			public Object getTransferData(DataFlavor flavor) {
				return image;
			}
		};
		opened.mode.paste(imageTransfer, opened.root);
		String imageTexts = allTexts(opened.root).toString();
		assertTrue(imageTexts.contains(".jpeg"), imageTexts);
		assertTrue(imageTexts.contains("img"), imageTexts);
	}

	@Test
	void pastingXmlKeepsTheNodeText() throws Exception {
		Opened opened = openMap("Alpha");
		MindMapNodeModel pasted = opened.mode.getActorFactory().getPasteActor().pasteXMLWithoutRedisplay(
				"<node TEXT=\"XmlPaste\"/>", opened.root, false, false, false, new HashMap<String, NodeAdapter>());

		assertNotNull(pasted);
		assertEquals("XmlPaste", pasted.getText());
		assertTrue(allTexts(opened.root).contains("XmlPaste"));
	}

	@Test
	void aShallowCopyKeepsTheLinkAndDropsTheChild() throws Exception {
		Opened opened = openMap("Alpha");
		MindMapNode alpha = opened.child("Alpha");
		opened.mode.setLink(alpha, "https://example.test/copy");
		opened.mode.addNewNode(alpha, 0, false);
		opened.mode.setNodeText((MindMapNode) alpha.getChildAt(0), "Child");

		MindMapNode copy = ((MindMapNodeModel) alpha).shallowCopy();

		assertNotNull(copy);
		assertEquals("Alpha", copy.getText());
		assertEquals("https://example.test/copy", copy.getLink());
		assertEquals(0, copy.getChildCount());
	}

	@Test
	void arrowKeysWalkSiblingsAndAPage() throws Exception {
		Opened opened = openMap("Alpha", "Beta");
		opened.view.getRoot().updateAll();
		opened.view.getRoot().doLayout();
		selectOnly(opened, opened.root);
		opened.view.move(key(opened.view, KeyEvent.VK_RIGHT, 0));
		String moved = opened.view.getSelected().getModel().getText();
		assertTrue(moved.equals("Alpha") || moved.equals("Beta"), moved);
		opened.view.move(key(opened.view, KeyEvent.VK_DOWN, 0));
		opened.view.move(key(opened.view, KeyEvent.VK_UP, 0));
		opened.view.move(key(opened.view, KeyEvent.VK_LEFT, 0));
		String walked = opened.view.getSelected().getModel().getText();
		assertTrue(walked.equals("Alpha") || walked.equals("Beta") || walked.equals("ROOT"), walked);

		opened.view.getParent().remove(opened.view);
		JViewport viewport = new JViewport();
		viewport.setBounds(0, 0, 400, 300);
		viewport.setView(opened.view);
		opened.view.setBounds(0, 0, 900, 700);
		selectOnly(opened, opened.child("Alpha"));
		opened.view.move(key(opened.view, KeyEvent.VK_PAGE_DOWN, 0));
		String paged = opened.view.getSelected().getModel().getText();
		assertTrue(paged.equals("Alpha") || paged.equals("Beta") || paged.equals("ROOT"), paged);

		selectOnly(opened, opened.child("Alpha"));
		KeyEvent shiftDown = key(opened.view, KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK);
		opened.view.move(shiftDown);
		assertFalse(opened.view.getSelecteds().isEmpty());
		assertTrue(shiftDown.isConsumed());
	}

	@Test
	void anArrowReportsAHitOnItsCurve() throws Exception {
		Opened opened = openMap("From", "To");
		MindMapNode from = opened.child("From");
		opened.mode.addLink(from, opened.child("To"));
		opened.view.getRoot().updateAll();
		opened.view.getRoot().doLayout();
		paint(opened.view);
		Field views = MapView.class.getDeclaredField("mArrowLinkViews");
		views.setAccessible(true);
		@SuppressWarnings("unchecked")
		Vector<ArrowLinkView> arrows = (Vector<ArrowLinkView>) views.get(opened.view);
		assertFalse(arrows.isEmpty());
		ArrowLinkView arrow = arrows.get(0);
		Field curveField = ArrowLinkView.class.getDeclaredField("arrowLinkCurve");
		curveField.setAccessible(true);
		CubicCurve2D curve = (CubicCurve2D) curveField.get(arrow);
		assertNotNull(curve);
		FlatteningPathIterator iterator = new FlatteningPathIterator(curve.getPathIterator(null), 2.0);
		double[] coordinates = new double[6];
		Point hit = null;
		while (!iterator.isDone()) {
			int type = iterator.currentSegment(coordinates);
			if (type == PathIterator.SEG_LINETO || type == PathIterator.SEG_MOVETO) {
				Point candidate = new Point((int) coordinates[0], (int) coordinates[1]);
				if (arrow.detectCollision(candidate)) {
					hit = candidate;
					break;
				}
			}
			iterator.next();
		}
		assertNotNull(hit, curve.getBounds().toString());
		assertSame(arrow.getModel(), opened.view.detectCollision(hit));
		assertNull(opened.view.detectCollision(new Point(hit.x - 500, hit.y - 500)));
		assertFalse(arrow.detectCollision(new Point(hit.x - 500, hit.y - 500)));
	}

	@Test
	void cloneRegistrationFindsTheMatchingChild() throws Exception {
		Opened opened = openMap("Alpha", "Beta");
		MindMapNode alpha = opened.child("Alpha");
		MindMapNode beta = opened.child("Beta");
		opened.mode.addNewNode(alpha, 0, false);
		opened.mode.setNodeText((MindMapNode) alpha.getChildAt(0), "ChildA");
		opened.mode.addNewNode(beta, 0, false);
		opened.mode.setNodeText((MindMapNode) beta.getChildAt(0), "ChildB");
		MindMapNode childA = (MindMapNode) alpha.getChildAt(0);
		ClonePasteAction.Registration registration = new ClonePasteAction.Registration(opened.mode, opened.model);
		registration.register();
		try {
			assertTrue(registration.getCorrespondingNodes(childA, false).isEmpty());
			String cloneId = registration.generateNewCloneId(null);
			assertTrue(cloneId.startsWith("CLONE_"), cloneId);
			ClonePlugin first = clonePlugin(opened, alpha);
			ClonePlugin second = clonePlugin(opened, beta);
			assertTrue(registration.registerClone(cloneId, first));
			assertFalse(registration.registerClone(cloneId, second));

			assertTrue(pairTexts(registration.getCorrespondingNodes(childA, false)).contains("ChildB"));
			PasteNodeAction asSibling = new PasteNodeAction();
			asSibling.setAsSibling(true);
			assertTrue(pairTexts(registration.getCorrespondingNodes(asSibling, childA)).contains("ChildB"));
			assertTrue(pairTexts(registration.getCorrespondingNodes(new NewNodeAction(), childA)).contains("ChildB"));
			registration.getCloneProperties(cloneId).setCloneItself(true);
			assertTrue(pairTexts(registration.getCorrespondingNodes(new HookNodeAction(), alpha)).contains("Beta"));
			assertTrue(registration.getCorrespondingNodes(opened.root, false).isEmpty());
			registration.deregisterClone(cloneId, second);
			registration.deregisterClone(cloneId, first);
			assertTrue(registration.getCorrespondingNodes(childA, false).isEmpty());
		} finally {
			registration.deRegister();
		}
	}

	@Test
	void dayChooserKeepsTheFifteenthAndStepsRight() {
		Calendar chosen = new GregorianCalendar(2020, Calendar.JUNE, 15);
		JCalendar calendar = new JCalendar(chosen.getTime());
		JDayChooser days = calendar.getDayChooser();
		assertEquals(15, days.getDay());

		Date later = new GregorianCalendar(2020, Calendar.JUNE, 20).getTime();
		Date earlier = new GregorianCalendar(2020, Calendar.JUNE, 10).getTime();
		calendar.setSelectableDateRange(later, earlier);
		assertTrue(days.getMinSelectableDate().before(earlier));
		assertTrue(days.getMaxSelectableDate().after(later));
		calendar.setSelectableDateRange(earlier, later);
		assertEquals(earlier, days.getMinSelectableDate());
		assertEquals(later, days.getMaxSelectableDate());

		days.setDecorationBackgroundColor(Color.BLUE);
		assertEquals(Color.BLUE, days.getDecorationBackgroundColor());
		days.setDecorationBackgroundVisible(false);
		assertFalse(days.isDecorationBackgroundVisible());
		days.setDayBordersVisible(false);
		assertFalse(days.isDayBordersVisible());
		days.setDayBordersVisible(true);
		assertTrue(days.isDayBordersVisible());

		Calendar[] fired = new Calendar[1];
		days.addPropertyChangeListener((PropertyChangeEvent evt) -> {
			if (JDayChooser.DAY_PROPERTY.equals(evt.getPropertyName()) && evt.getNewValue() instanceof Calendar value) {
				fired[0] = value;
			}
		});
		days.keyPressed(key(days, KeyEvent.VK_RIGHT, 0));
		assertEquals(15, days.getDay());
		assertEquals(16, fired[0].get(Calendar.DAY_OF_MONTH));
		days.keyPressed(key(days, KeyEvent.VK_LEFT, 0));
		assertEquals(14, fired[0].get(Calendar.DAY_OF_MONTH));
		days.keyPressed(key(days, KeyEvent.VK_UP, 0));
		assertEquals(8, fired[0].get(Calendar.DAY_OF_MONTH));
		days.keyPressed(key(days, KeyEvent.VK_DOWN, 0));
		assertEquals(22, fired[0].get(Calendar.DAY_OF_MONTH));
		days.keyPressed(key(days, KeyEvent.VK_END, 0));
		assertEquals(days.getDaysInMonth(), fired[0].get(Calendar.DAY_OF_MONTH));
		assertEquals(15, days.getDay());
		days.keyPressed(key(days, KeyEvent.VK_PAGE_DOWN, 0));
		assertTrue(fired[0].getTime().after(chosen.getTime()));
		days.keyPressed(key(days, KeyEvent.VK_PAGE_UP, 0));
		assertTrue(fired[0].getTime().before(chosen.getTime()));
		days.actionPerformed(new ActionEvent(new JButton("20"), ActionEvent.ACTION_PERFORMED, "day"));
		assertEquals(20, fired[0].get(Calendar.DAY_OF_MONTH));
		assertEquals(15, days.getDay());

		days.setDay(16);
		assertEquals(16, days.getDay());
		days.setDay(0);
		assertEquals(1, days.getDay());
		days.setDay(99);
		assertEquals(days.getDaysInMonth(), days.getDay());
		days.setDay(15);
		days.setMonth(Calendar.JULY);
		assertEquals(Calendar.JULY, days.getMonth());
		assertEquals(15, days.getDay());
		days.setYear(2021);
		assertEquals(2021, days.getYear());
		days.setWeekOfYearVisible(true);
		assertTrue(days.isWeekOfYearVisible());
		days.setMaxDayCharacters(3);
		assertEquals(3, days.getMaxDayCharacters());
		days.setMaxDayCharacters(9);
		assertEquals(0, days.getMaxDayCharacters());
		days.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
		days.setForeground(Color.RED);
		assertEquals(Color.RED, days.getForeground());

		int year = calendar.getYearChooser().getYear();
		calendar.getMonthChooser().setMonth(Calendar.DECEMBER);
		calendar.getMonthChooser().changeMonth(true);
		assertEquals(Calendar.JANUARY, calendar.getMonthChooser().getMonth());
		assertEquals(year + 1, calendar.getYearChooser().getYear());

		calendar.setCalendar(new GregorianCalendar(2019, Calendar.JULY, 4));
		assertEquals(4, days.getDay());
		assertEquals(Calendar.JULY, calendar.getMonthChooser().getMonth());
		assertEquals(2019, calendar.getYearChooser().getYear());
		calendar.setEnabled(false);
		assertFalse(days.isEnabled());
		calendar.setEnabled(true);
		assertTrue(days.isEnabled());
	}

	@Test
	void contraryColorsLeaveTheOriginalHue() throws Exception {
		Method first = NodeView.class.getDeclaredMethod("getAntiColor1", Color.class);
		Method second = NodeView.class.getDeclaredMethod("getAntiColor2", Color.class);
		first.setAccessible(true);
		second.setAccessible(true);
		Color fromFirst = (Color) first.invoke(null, Color.BLUE);
		Color fromSecond = (Color) second.invoke(null, Color.BLUE);
		assertNotNull(fromFirst);
		assertNotNull(fromSecond);
		assertFalse(fromFirst.equals(Color.BLUE));
		assertFalse(fromSecond.equals(Color.BLUE));
		assertFalse(fromFirst.equals(fromSecond));
	}

	@Test
	void listModelsKeepInsertedValuesInOrder() {
		DefaultListModel<String> list = new DefaultListModel<>();
		assertTrue(list.isEmpty());
		assertEquals(0, list.getSize());
		list.addElement("Beta");
		list.add(0, "Alpha");
		list.addElement("Beta");
		assertEquals(3, list.size());
		assertEquals("Alpha", list.getElementAt(0));
		assertEquals("Alpha", list.get(0));
		assertEquals("Beta", list.lastElement());
		assertEquals(0, list.indexOf("Alpha"));
		assertEquals(2, list.lastIndexOf("Beta"));
		assertEquals(1, list.indexOf("Beta", 1));
		assertEquals(-1, list.indexOf("Missing"));
		assertTrue(list.contains("Alpha"));
		list.setElementAt("Ada", 0);
		assertEquals("Ada", list.elementAt(0));
		assertEquals("Ada", list.set(0, "Alpha"));
		list.insertElementAt("Mid", 1);
		assertEquals("Mid", list.get(1));
		assertTrue(list.removeElement("Mid"));
		assertFalse(list.contains("Mid"));
		assertEquals("Beta", list.remove(1));
		list.removeElementAt(0);
		assertEquals(1, list.size());
		list.addAll(List.of("C", "D"));
		list.addAll(0, List.of("A"));
		assertEquals("A", list.firstElement());
		Object[] copied = new Object[list.size()];
		list.copyInto(copied);
		assertEquals("A", copied[0]);
		assertEquals(list.size(), list.toArray().length);
		assertTrue(list.elements().hasMoreElements());
		list.ensureCapacity(20);
		assertTrue(list.capacity() >= 20);
		list.trimToSize();
		assertTrue(list.toString().contains("A"));
		assertEquals(list.size(), list.unmodifiableList().size());
		int grown = list.size() + 2;
		list.setSize(grown);
		assertEquals(grown, list.getSize());
		assertNull(list.getElementAt(grown - 1));
		list.setSize(1);
		assertEquals(1, list.size());
		assertThrows(IllegalArgumentException.class, () -> list.removeRange(2, 0));
		list.removeRange(0, 0);
		assertTrue(list.isEmpty());
		list.addElement("Gone");
		list.removeAllElements();
		assertTrue(list.isEmpty());
		list.addElement("Also");
		list.clear();
		assertEquals(0, list.size());

		SortedMapListModel sorted = new SortedMapListModel();
		sorted.add("Beta");
		sorted.add("Alpha");
		sorted.addAll(new Object[] { "Gamma" });
		sorted.addAll(List.of("Ada"));
		assertEquals(4, sorted.getSize());
		assertEquals("Ada", sorted.getElementAt(0));
		assertEquals("Ada", sorted.firstElement());
		assertEquals("Gamma", sorted.lastElement());
		assertTrue(sorted.contains("Beta"));
		assertEquals(2, sorted.getIndexOf("Beta"));
		assertTrue(sorted.iterator().hasNext());
		sorted.replace("Beta", "Beta");
		assertTrue(sorted.contains("Beta"));
		sorted.replace("Beta", "Zed");
		assertFalse(sorted.contains("Beta"));
		assertTrue(sorted.contains("Zed"));
		sorted.remove("Zed");
		assertEquals(-1, sorted.getIndexOf("Zed"));
		sorted.clear();
		assertEquals(0, sorted.getSize());
		sorted.clear();

		sorted.add("Ext");
		ExtendedComboBoxModel combo = new ExtendedComboBoxModel(new Object[] { "Own" });
		assertEquals(1, combo.getSize());
		assertEquals("Own", combo.getElementAt(0));
		combo.setExtensionList(sorted);
		assertEquals(2, combo.getSize());
		assertEquals("Ext", combo.getElementAt(1));
		assertEquals(1, combo.getIndexOf("Ext"));
		assertEquals(0, combo.getIndexOf("Own"));
		assertEquals(-1, combo.getIndexOf("Missing"));
		combo.addSortedElement("Extra");
		assertTrue(combo.getSize() >= 3);
		assertTrue(combo.getIndexOf("Extra") > 0);
		combo.insertElementAt("First", 0);
		assertEquals("First", combo.getElementAt(0));
		combo.removeElement("Own");
		assertEquals(-1, combo.getIndexOf("Own"));
		combo.removeElementAt(0);
		combo.setExtensionList(null);
		assertTrue(combo.getIndexOf("Ext") < 0);
		combo.setExtensionList(sorted);
		combo.removeAllElements();
		assertEquals(0, combo.getSize());

		SortedComboBoxModel choice = new SortedComboBoxModel();
		choice.add("Beta");
		choice.add("Alpha");
		choice.setSelectedItem("Beta");
		assertEquals("Beta", choice.getSelectedItem());
		assertEquals("Alpha", choice.getElementAt(0));

		SortedMapVector vector = new SortedMapVector();
		assertEquals(0, vector.size());
		vector.add("b", "Beta");
		vector.add("a", "Alpha");
		vector.add("c", "Gamma");
		vector.add("b", "Ignored");
		assertEquals(3, vector.size());
		assertEquals("Alpha", vector.getValue(0));
		assertEquals("a", vector.getKey(0));
		assertEquals("Beta", vector.getValue("b"));
		assertTrue(vector.containsKey("c"));
		assertFalse(vector.containsKey("z"));
		assertEquals(1, vector.indexOf("b"));
		assertEquals(-1, vector.indexOf("z"));
		assertTrue(vector.capacity() >= vector.size());
		assertTrue(vector.remove("a"));
		assertFalse(vector.remove("a"));
		vector.remove(0);
		assertFalse(vector.containsKey("b"));
		assertThrows(NoSuchElementException.class, () -> vector.getValue("missing"));
		vector.clear();
		assertEquals(0, vector.size());
	}

	@Test
	void tileImageRoundTripsABlueSquare() throws Exception {
		BufferedImage blue = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = blue.createGraphics();
		graphics.setColor(Color.BLUE);
		graphics.fillRect(0, 0, 8, 8);
		graphics.dispose();
		ByteArrayOutputStream encoded = new ByteArrayOutputStream();
		ImageIO.write(blue, "png", encoded);
		TileImage tiles = new TileImage();
		assertFalse(tiles.isLoaded());
		assertFalse(tiles.hasErrors());
		assertFalse(tiles.isImageCreated());
		tiles.load(Tools.toBase64(encoded.toByteArray()));
		assertTrue(tiles.isImageCreated());
		assertNotNull(tiles.getImage());
		assertEquals(8, ((BufferedImage) tiles.getImage()).getWidth());
		tiles.drawCross();
		assertTrue(redPixels((BufferedImage) tiles.getImage()) > 0);
		assertTrue(tiles.imageUpdate(blue, 0, 0, 0, 8, 8));
		assertTrue(tiles.isDrawingDone());
		String saved = tiles.save();
		assertNotNull(saved);
		assertTrue(saved.length() > 20);
		TileImage restored = new TileImage();
		restored.load(saved);
		assertEquals(8, ((BufferedImage) restored.getImage()).getWidth());
		File missing = File.createTempFile("bespoke-tile", ".png");
		assertTrue(missing.delete());
		restored.load(missing);
		assertNotNull(restored.getImage());
	}

	@Test
	void scalableIconPaintsAtTwiceTheSize() throws Exception {
		File png = File.createTempFile("bespoke-icon", ".png");
		png.deleteOnExit();
		BufferedImage source = new BufferedImage(4, 6, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = source.createGraphics();
		graphics.setColor(Color.GREEN);
		graphics.fillRect(0, 0, 4, 6);
		graphics.dispose();
		ImageIO.write(source, "png", png);
		ScalableImageIcon icon = new ScalableImageIcon(png.toURI().toURL());
		assertEquals(2.0f, icon.getScale());
		icon.setScale(2.0f);
		assertTrue(icon.getIconWidth() >= 8, String.valueOf(icon.getIconWidth()));
		assertTrue(icon.getIconHeight() >= 12, String.valueOf(icon.getIconHeight()));
		BufferedImage canvas = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D painted = canvas.createGraphics();
		painted.setColor(Color.WHITE);
		painted.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		icon.paintIcon(new JPanel(), painted, 0, 0);
		painted.dispose();
		assertTrue(nonWhitePixels(canvas) > 0);
		assertNotNull(icon.getImage());
		assertEquals(4, icon.getUnscaledIcon().getIconWidth());
	}

	@Test
	void aCurveBoxContainsTheBend() throws Exception {
		Method bounds = Class.forName("freemind.view.mindmapview.PathBBox")
				.getDeclaredMethod("getBBox", java.awt.Shape.class);
		bounds.setAccessible(true);
		Rectangle2D quad = (Rectangle2D) bounds.invoke(null, new QuadCurve2D.Double(0, 0, 10, 100, 40, 0));
		assertTrue(quad.getWidth() >= 40, quad.toString());
		assertTrue(quad.getHeight() > 10, quad.toString());
		Rectangle2D cubic = (Rectangle2D) bounds.invoke(null, new CubicCurve2D.Double(0, 0, 0, 80, 100, -40, 100, 10));
		assertTrue(cubic.getWidth() >= 100, cubic.toString());
		assertTrue(cubic.getHeight() > 10, cubic.toString());
	}

	@Test
	void bubbleStylePaintsARoundedNode() throws Exception {
		Opened opened = openMap("Alpha");
		MindMapNode alpha = opened.child("Alpha");
		opened.mode.addNewNode(alpha, 0, false);
		opened.mode.setNodeText((MindMapNode) alpha.getChildAt(0), "Child");
		opened.mode.setNodeStyle(alpha, MindMapNode.STYLE_BUBBLE);
		opened.mode.setFolded(alpha, true);
		EventQueue.invokeAndWait(() -> {
		});
		opened.view.getRoot().updateAll();
		NodeView view = opened.view.getNodeView(alpha);
		assertEquals("BubbleMainView", view.getMainView().getClass().getSimpleName());
		view.setBounds(20, 20, 160, 48);
		view.doLayout();
		assertTrue(alpha.isFolded());
		assertTrue(nonWhitePixels(paint(view)) > 0);
		int foldedWidth = view.getMainView().getWidth();
		assertTrue(foldedWidth > 0);
	}

	@Test
	void aLongHtmlNodeAndATableKeepTheirText() throws Exception {
		Opened opened = openMap("Alpha", "Beta", "Gamma");
		MindMapNode alpha = opened.child("Alpha");
		MindMapNode beta = opened.child("Beta");
		MindMapNode gamma = opened.child("Gamma");
		File mapFile = File.createTempFile("bespoke-html", ".mm");
		mapFile.deleteOnExit();
		opened.model.setFile(mapFile);
		String previousHead = opened.mode.getFrame().getProperty("html_long_node_head");
		opened.mode.getFrame().setProperty("html_long_node_head", "<meta name=\"kept\" content=\"yes\">");
		try {
			opened.mode.setNodeText(alpha, "<html><head><title>T</title></head><body>Alpha</body></html>");
			opened.mode.setNodeText(beta, "<html><img src=\"a.png\">pic</html>");
			opened.mode.setNodeText(gamma, "<html><body>" + "A".repeat(30000) + "</body></html>");
			opened.view.getRoot().updateAll();
			String headed = opened.view.getNodeView(alpha).getMainView().getText();
			assertEquals("<head><meta name=\"kept\" content=\"yes\">", headed);
			String image = opened.view.getNodeView(beta).getMainView().getText();
			assertTrue(image.contains("<base "), image);
			assertTrue(image.contains("pic"), image);
			String wide = opened.view.getNodeView(gamma).getMainView().getText();
			assertTrue(wide.contains("width="), wide.substring(0, Math.min(wide.length(), 180)));
			opened.mode.setNodeText(alpha, "<table>\nOne\tTwo");
			opened.mode.setNodeText(beta, "Line1\nLine2");
			opened.view.getRoot().updateAll();
			String table = opened.view.getNodeView(alpha).getMainView().getText();
			assertTrue(table.contains("One"), table);
			assertTrue(table.contains("Two"), table);
			assertTrue(table.contains("<td"), table);
			String lines = opened.view.getNodeView(beta).getMainView().getText();
			assertTrue(lines.contains("Line1"), lines);
			assertTrue(lines.contains("Line2"), lines);
			assertTrue(lines.contains("<html>"), lines);
		} finally {
			opened.mode.getFrame().setProperty("html_long_node_head", previousHead == null ? "" : previousHead);
		}
	}

	@Test
	void unfoldingReopensAFoldedBranch() throws Exception {
		Opened opened = openMap("Alpha", "Beta");
		MindMapNode alpha = opened.child("Alpha");
		opened.mode.addNewNode(alpha, 0, false);
		opened.mode.setNodeText((MindMapNode) alpha.getChildAt(0), "Child");
		UnfoldAll unfold = new UnfoldAll();
		unfold.setController(opened.mode);
		unfold.setMap(opened.model);
		assertEquals(Integer.MAX_VALUE, unfold.getMinDepth(opened.root));
		opened.mode.setFolded(alpha, true);
		assertEquals(1, unfold.getMinDepth(opened.root));
		unfold.unfoldAll(opened.root);
		assertFalse(opened.root.isFolded());
		assertFalse(alpha.isFolded());
		unfold.unfoldStageN(opened.root, 1);
		assertTrue(alpha.isFolded());
		unfold.unfoldAll(opened.root);
		assertFalse(alpha.isFolded());
		unfold.foldLastBranches(opened.root);
		assertFalse(opened.root.isFolded());
		assertTrue(alpha.isFolded());
		unfold.unfoldAll(opened.root);
		assertFalse(opened.root.isFolded());
		assertFalse(alpha.isFolded());
	}

	@Test
	void anIconActionAddsAndRemovesTheIdeaIcon() throws Exception {
		Opened opened = openMap("Alpha");
		MindMapNode alpha = opened.child("Alpha");
		selectOnly(opened, alpha);
		MindIcon idea = MindIcon.factory("idea");
		assertEquals("idea", idea.getName());
		IconAction action = new IconAction(opened.mode, idea, new RemoveIconAction(opened.mode));
		action.actionPerformed(new ActionEvent(alpha, ActionEvent.ACTION_PERFORMED, "icon"));
		assertEquals(1, alpha.getIcons().size());
		assertEquals("idea", alpha.getIcons().get(0).getName());
		action.actionPerformed(new ActionEvent(alpha, ActionEvent.ACTION_PERFORMED, "icon"));
		assertEquals(2, alpha.getIcons().size());
		action.actionPerformed(new ActionEvent(alpha, ActionEvent.ACTION_PERFORMED, "icon", ActionEvent.ALT_MASK));
		assertEquals(1, alpha.getIcons().size());
		action.actionPerformed(new ActionEvent(alpha, ActionEvent.ACTION_PERFORMED, "icon", ActionEvent.CTRL_MASK));
		assertEquals(0, alpha.getIcons().size());
		action.actionPerformed(new ActionEvent(alpha, ActionEvent.ACTION_PERFORMED, "icon", ActionEvent.SHIFT_MASK));
		assertEquals(1, alpha.getIcons().size());
		assertEquals("idea", alpha.getIcons().get(0).getName());
	}

	@Test
	void aNodeFilterHidesBeta() throws Exception {
		Opened opened = openMap("Alpha", "Beta");
		Field nodeKind = ConditionFactory.class.getDeclaredField("FILTER_NODE");
		nodeKind.setAccessible(true);
		ConditionFactory factory = new ConditionFactory();
		Condition containsAlpha = factory.createCondition((NamedObject) nodeKind.get(null),
				factory.getNodeConditionNames()[0], "Alpha", false);
		assertNotNull(containsAlpha);
		assertTrue(containsAlpha.checkNode(opened.mode.getController(), opened.child("Alpha")));
		assertFalse(containsAlpha.checkNode(opened.mode.getController(), opened.child("Beta")));
		DefaultFilter filter = new DefaultFilter(containsAlpha, true, false);
		opened.model.setFilter(filter);
		selectOnly(opened, opened.child("Alpha"));
		filter.applyFilter(opened.mode.getController());
		assertTrue(opened.child("Alpha").isVisible());
		assertFalse(opened.child("Beta").isVisible());
		assertTrue(opened.root.isVisible());
		assertTrue(filter.areAncestorsShown());
		assertFalse(filter.areDescendantsShown());
		assertTrue(filter.areMatchedShown());
		assertFalse(filter.areHiddenShown());
	}

	@Test
	void formattingNewNodesCopiesThePreviousFormat() throws Exception {
		Opened opened = openMap("Alpha");
		FormatNewNodes formatter = new FormatNewNodes(opened.mode, opened.model);
		formatter.register();
		try {
			NodeColorFormatAction format = new NodeColorFormatAction();
			format.setNode("OLD");
			format.setColor("#0000ff");
			CompoundAction recorded = new CompoundAction();
			recorded.addChoice(format);
			formatter.executeAction(recorded);
			NewNodeAction created = new NewNodeAction();
			created.setNewId("NEW");
			ActionPair unchanged = new ActionPair(new FoldAction(), new FoldAction());
			assertSame(unchanged, formatter.filterAction(unchanged));
			ActionPair filtered = formatter.filterAction(new ActionPair(created, new NewNodeAction()));
			assertTrue(filtered.getDoAction() instanceof CompoundAction);
			CompoundAction compound = (CompoundAction) filtered.getDoAction();
			boolean sawNew = false;
			boolean sawColor = false;
			for (Object choice : compound.getListChoiceList()) {
				if (choice instanceof NewNodeAction node && "NEW".equals(node.getNewId())) {
					sawNew = true;
				}
				if (choice instanceof NodeColorFormatAction color && "NEW".equals(color.getNode())
						&& "#0000ff".equals(color.getColor())) {
					sawColor = true;
				}
			}
			assertTrue(sawNew);
			assertTrue(sawColor);
		} finally {
			formatter.deRegister();
		}
	}

	@Test
	void theLastEditLocationIsThePreviousNode() throws Exception {
		Opened opened = openMap("Alpha", "Beta");
		MindMapNode alpha = opened.child("Alpha");
		MindMapNode beta = opened.child("Beta");
		String alphaId = opened.mode.getNodeID(alpha);
		String betaId = opened.mode.getNodeID(beta);
		JumpLastEditLocation.JumpLastEditLocationRegistration registration =
				new JumpLastEditLocation.JumpLastEditLocationRegistration(opened.mode, opened.model);
		registration.register();
		try {
			NodeHookAction jump = new NodeHookAction("accessories/plugins/JumpLastEditLocation.properties", opened.mode);
			JMenuItem item = new JMenuItem();
			assertFalse(registration.isEnabled(item, jump));
			assertTrue(registration.isEnabled(item, new NodeHookAction("other", opened.mode)));
			assertNull(registration.getLastEditLocation(alpha));
			HookNodeAction self = new HookNodeAction();
			self.setHookName("accessories/plugins/JumpLastEditLocation.properties");
			self.setNode(alphaId);
			registration.executeAction(self);
			FoldAction fold = new FoldAction();
			fold.setNode(alphaId);
			registration.executeAction(fold);
			assertNull(registration.getLastEditLocation(alpha));
			assertFalse(registration.isEnabled(item, jump));
			NewNodeAction missing = new NewNodeAction();
			missing.setNewId("missing-id");
			registration.executeAction(missing);
			assertTrue(registration.isEnabled(item, jump));
			NewNodeAction first = new NewNodeAction();
			first.setNewId(alphaId);
			registration.executeAction(first);
			CompoundAction nested = new CompoundAction();
			NewNodeAction second = new NewNodeAction();
			second.setNewId(betaId);
			nested.addChoice(second);
			registration.executeAction(nested);
			registration.executeAction(second);
			assertEquals("Alpha", registration.getLastEditLocation(beta).getText());
			assertEquals("Beta", registration.getLastEditLocation(opened.root).getText());
			for (int i = 0; i < 12; i++) {
				NewNodeAction again = new NewNodeAction();
				again.setNewId(i % 2 == 0 ? alphaId : betaId);
				registration.executeAction(again);
			}
			assertNotNull(registration.getLastEditLocation(alpha));
		} finally {
			registration.deRegister();
		}
	}

	@Test
	void fittingTheMapChangesTheZoom() throws Exception {
		Opened opened = openMap("Alpha", "Beta");
		java.awt.Container parent = opened.view.getParent();
		parent.remove(opened.view);
		JViewport viewport = new JViewport();
		viewport.setPreferredSize(new Dimension(180, 120));
		viewport.setView(opened.view);
		viewport.setBounds(0, 0, 180, 120);
		opened.view.setBounds(0, 0, 900, 700);
		opened.view.getRoot().updateAll();
		opened.mode.getController().setZoom(2f);
		assertEquals(2f, opened.view.getZoom(), 0.01f);
		opened.view.getRoot().setSize(500, 400);
		Rectangle inner = opened.view.getInnerBounds();
		assertTrue(inner.width > 0 && inner.height > 0, inner.toString());
		FitToPage fit = new FitToPage();
		fit.setController(opened.mode);
		fit.startupMapHook();
		EventQueue.invokeAndWait(() -> {
		});
		float fitted = opened.view.getZoom();
		assertTrue(Float.isFinite(fitted), String.valueOf(fitted));
		assertTrue(fitted > 0f && fitted < 2f, String.valueOf(fitted));
		parent.add(viewport);
	}

	@Test
	@Tag("gui")
	void aPrintPreviewPaintsAPage() throws Exception {
		org.assertj.swing.edt.FailOnThreadViolationRepaintManager.install();
		Class<?> previewClass = Class.forName("freemind.controller.printpreview.Preview");
		java.lang.reflect.Constructor<?> previewConstructor = previewClass
				.getDeclaredConstructor(MapView.class, double.class, PageFormat.class);
		previewConstructor.setAccessible(true);
		Method moveIndex = previewClass.getDeclaredMethod("moveIndex", int.class);
		Method changeZoom = previewClass.getDeclaredMethod("changeZoom", double.class);
		Method resize = previewClass.getDeclaredMethod("resize");
		moveIndex.setAccessible(true);
		changeZoom.setAccessible(true);
		resize.setAccessible(true);
		boolean[] ink = { false };
		int[] preferredWidth = { 0 };
		EventQueue.invokeAndWait(() -> {
			try {
				Opened opened = openMap("Alpha", "Beta");
				opened.view.getRoot().updateAll();
				Object preview = previewConstructor.newInstance(opened.view, 0.25d, new PageFormat());
				JFrame frame = new JFrame("preview");
				try {
					java.awt.Component component = (java.awt.Component) preview;
					frame.getContentPane().add(component);
					frame.pack();
					frame.setVisible(true);
					assertTrue(component.getWidth() > 0);
					BufferedImage image = new BufferedImage(component.getWidth(), component.getHeight(),
							BufferedImage.TYPE_INT_RGB);
					Graphics2D graphics = image.createGraphics();
					graphics.setColor(Color.WHITE);
					graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
					component.paint(graphics);
					graphics.dispose();
					ink[0] = nonWhitePixels(image) > 0;
					moveIndex.invoke(preview, 1);
					changeZoom.invoke(preview, -0.05d);
					resize.invoke(preview);
					preferredWidth[0] = component.getPreferredSize().width;
				} finally {
					frame.dispose();
				}
			} catch (RuntimeException ex) {
				throw ex;
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});
		assertTrue(ink[0]);
		assertTrue(preferredWidth[0] > 0);
	}

	@Test
	@Tag("gui")
	void anExportImageContainsTheMap() throws Exception {
		org.assertj.swing.edt.FailOnThreadViolationRepaintManager.install();
		BufferedImage[] image = new BufferedImage[1];
		EventQueue.invokeAndWait(() -> {
			try {
				Opened opened = openMap("Alpha", "Beta");
				JFrame frame = new JFrame("export");
				try {
					frame.getContentPane().add(opened.view);
					frame.setSize(800, 600);
					frame.setVisible(true);
					opened.view.getRoot().updateAll();
					ExportHook hook = new ExportHook();
					hook.setController(opened.mode);
					image[0] = hook.createBufferedImage();
				} finally {
					frame.dispose();
				}
			} catch (RuntimeException ex) {
				throw ex;
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});
		assertNotNull(image[0]);
		assertTrue(image[0].getWidth() > 0 && image[0].getHeight() > 0);
		assertTrue(nonWhitePixels(image[0]) > 0);
	}

	@Test
	void aShortLinkAFoldMarkAndAnOrFilterStayVisible() throws Exception {
		Opened opened = openMap("Alpha", "Beta");
		MindMapNode alpha = opened.child("Alpha");
		MindMapNode beta = opened.child("Beta");
		opened.mode.setLink(alpha, "https://example.test/a");
		assertEquals("https://example.test/a", opened.mode.getLinkShortText(alpha));
		opened.mode.setLink(opened.root, "#" + opened.mode.getNodeID(alpha));
		assertTrue(opened.mode.getLinkShortText(opened.root).contains("Alpha"),
				opened.mode.getLinkShortText(opened.root));
		opened.mode.setLink(opened.root, "#missing-id");
		String missing = opened.mode.getLinkShortText(opened.root);
		assertNotNull(missing);
		assertFalse(missing.contains("Alpha"), missing);
		HtmlTools.IndexPair pair = new HtmlTools.IndexPair(1, 4, 2, 5, true);
		assertTrue(pair.toString().contains("originalStart: 1"), pair.toString());
		assertTrue(pair.toString().contains("is a tag: true"), pair.toString());
		String replaced = HtmlTools.getInstance().getReplaceResult(
				java.util.regex.Pattern.compile("Alpha"), "Beta", "<b>Alpha</b>");
		assertTrue(replaced.contains("Beta"), replaced);
		assertTrue(replaced.contains("<b>"), replaced);
		Field nodeKind = ConditionFactory.class.getDeclaredField("FILTER_NODE");
		nodeKind.setAccessible(true);
		ConditionFactory factory = new ConditionFactory();
		Condition containsAlpha = factory.createCondition((NamedObject) nodeKind.get(null),
				factory.getNodeConditionNames()[0], "Alpha", false);
		Condition containsBeta = factory.createCondition((NamedObject) nodeKind.get(null),
				factory.getNodeConditionNames()[0], "Beta", false);
		DisjunctConditions either = new DisjunctConditions(new Object[] { containsAlpha, containsBeta });
		assertTrue(either.checkNode(opened.mode.getController(), alpha));
		assertTrue(either.checkNode(opened.mode.getController(), beta));
		assertFalse(either.checkNode(opened.mode.getController(), opened.root));
		JComponent orView = either.getListCellRendererComponent();
		assertTrue(orView.getComponentCount() >= 3);
		ConjunctConditions both = new ConjunctConditions(new Object[] { containsAlpha, containsBeta });
		assertFalse(both.checkNode(opened.mode.getController(), alpha));
		JComponent andView = both.getListCellRendererComponent();
		assertTrue(andView.getComponentCount() >= 3);
		MindMapNode child = opened.mode.addNewNode(alpha, 0, false);
		opened.mode.setNodeText(child, "Child");
		opened.mode.setFolded(alpha, true);
		assertTrue(alpha.isFolded());
		opened.view.getRoot().updateAll();
		((freemind.modes.EdgeAdapter) alpha.getEdge()).setColor(Color.RED);
		NodeFoldingComponent fold = new NodeFoldingComponent(opened.view.getNodeView(alpha));
		try {
			fold.setSize(40, 40);
			Field entered = NodeFoldingComponent.class.getDeclaredField("mIsEntered");
			entered.setAccessible(true);
			entered.setBoolean(fold, true);
			BufferedImage image = new BufferedImage(40, 40, BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics = image.createGraphics();
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, 40, 40);
			fold.paint(graphics);
			graphics.dispose();
			assertTrue(redPixels(image) > 0);
		} finally {
			fold.dispose();
		}
	}

	@Test
	void aSpinFieldASwatchAndAKeyWorkaroundStayInRange() throws Exception {
		NumberProperty number = new NumberProperty("count", "count", 1, 9, 2);
		number.setValue("5");
		assertEquals("5", number.getValue());
		number.setValue("4");
		assertEquals("1", number.getValue());
		number.setValue("99");
		assertEquals("1", number.getValue());
		number.setValue("nope");
		assertEquals("1", number.getValue());

		TextTranslator labels = key -> {
			if (key.endsWith(".ok")) {
				return "&OK";
			}
			if (key.endsWith(".cancel")) {
				return "&Cancel";
			}
			return "&Ask";
		};
		RemindValueProperty remind = new RemindValueProperty("ask", "ask", labels);
		Field buttonField = remind.getClass().getSuperclass().getDeclaredField("mButton");
		buttonField.setAccessible(true);
		JButton remindButton = (JButton) buttonField.get(remind);
		remind.setValue("true");
		assertEquals("true", remind.getValue());
		assertEquals("OK", remindButton.getText());
		remind.setValue("false");
		assertEquals("false", remind.getValue());
		assertEquals("Cancel", remindButton.getText());
		remind.setValue("");
		assertEquals("", remind.getValue());
		assertEquals("Ask", remindButton.getText());

		IntegerComboProperty sizes = new IntegerComboProperty("size", "size",
				new String[] { "2", "4", "8" }, new Vector<>(List.of("2", "4", "8")));
		sizes.setValue("4");
		assertEquals("4", sizes.getValue());
		sizes.setValue("5");
		assertEquals("4", sizes.getValue());
		sizes.setValue("9");
		assertEquals("2", sizes.getValue());
		sizes.setValue("nope");
		assertEquals("2", sizes.getValue());
		ComboProperty boxes = new ComboProperty("box", "box", new String[] { "a", "b" }, labels);
		boxes.updateComboBoxEntries(List.of("c", "d"), List.of("C", "D"));
		assertEquals("c", boxes.getValue());

		Class<?> swatchClass = Class.forName("freemind.controller.ColorSwatch");
		java.lang.reflect.Constructor<?> swatchCtor = swatchClass.getDeclaredConstructor(Color.class);
		swatchCtor.setAccessible(true);
		Object swatch = swatchCtor.newInstance(Color.RED);
		Method paintIcon = swatchClass.getMethod("paintIcon", java.awt.Component.class, java.awt.Graphics.class,
				int.class, int.class);
		paintIcon.setAccessible(true);
		BufferedImage swatchImage = new BufferedImage(11, 11, BufferedImage.TYPE_INT_RGB);
		Graphics2D swatchGraphics = swatchImage.createGraphics();
		swatchGraphics.setColor(Color.WHITE);
		swatchGraphics.fillRect(0, 0, 11, 11);
		paintIcon.invoke(swatch, new JPanel(), swatchGraphics, 0, 0);
		swatchGraphics.dispose();
		assertEquals(Color.BLACK.getRGB(), swatchImage.getRGB(0, 0));
		assertEquals(Color.RED.getRGB(), swatchImage.getRGB(5, 5));

		JLabel tall = new JLabel("Tall");
		tall.setPreferredSize(new Dimension(30, 200));
		JAutoScrollBarPane scroll = new JAutoScrollBarPane(tall);
		scroll.setSize(100, 40);
		scroll.doLayout();
		assertEquals(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, scroll.getVerticalScrollBarPolicy());
		tall.setPreferredSize(new Dimension(30, 10));
		scroll.setSize(100, 200);
		scroll.doLayout();
		assertEquals(JScrollPane.VERTICAL_SCROLLBAR_NEVER, scroll.getVerticalScrollBarPolicy());
		scroll.invalidate();
		assertTrue(scroll.getPreferredSize().width > 0);

		Class<?> layoutClass = Class.forName("freemind.view.mindmapview.NodeViewFactory$ContentPaneLayout");
		java.lang.reflect.Constructor<?> layoutConstructor = layoutClass.getDeclaredConstructor();
		layoutConstructor.setAccessible(true);
		LayoutManager layout = (LayoutManager) layoutConstructor.newInstance();
		JPanel content = new JPanel();
		JLabel shown = new JLabel("Alpha");
		shown.setPreferredSize(new Dimension(40, 16));
		JLabel hidden = new JLabel("Hidden");
		hidden.setPreferredSize(new Dimension(40, 16));
		hidden.setVisible(false);
		content.add(shown);
		content.add(hidden);
		content.setSize(80, 40);
		layout.layoutContainer(content);
		assertEquals(0, shown.getY());
		assertEquals(16, shown.getHeight());
		assertEquals(0, hidden.getHeight());
		assertEquals(16, layout.preferredLayoutSize(content).height);

		Method eclipsed = EdgeView.class.getDeclaredMethod("getEclipsedStroke");
		eclipsed.setAccessible(true);
		Stroke firstStroke = (Stroke) eclipsed.invoke(null);
		assertSame(firstStroke, eclipsed.invoke(null));
		assertTrue(((BasicStroke) firstStroke).getDashArray()[0] > 0f);

		PrintStream formerOut = System.out;
		ByteArrayOutputStream captured = new ByteArrayOutputStream();
		System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
		try {
			Tools.logTransferable(new StringSelection("Alpha"));
		} finally {
			System.setOut(formerOut);
		}
		String logged = captured.toString(StandardCharsets.UTF_8);
		assertTrue(logged.contains("Alpha"), logged);
		assertTrue(logged.contains("Flavor"), logged);

		boolean[] ran = { false };
		Action typed = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				ran[0] = true;
			}
		};
		typed.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('a'));
		typed.putValue(Action.ACTION_COMMAND_KEY, "typed-a");
		KeyEvent typedA = new KeyEvent(new JPanel(), KeyEvent.KEY_TYPED, 0L, 0, KeyEvent.VK_UNDEFINED, 'a');
		Tools.invokeActionsToKeyboardLayoutDependantCharacters(typedA, new Action[] { typed }, new JPanel());
		assertTrue(ran[0]);
		assertTrue(typedA.isConsumed());

		assertTrue(Tools.isAbsolutePath("/tmp/alpha"));
		assertFalse(Tools.isAbsolutePath("alpha"));
		assertEquals("/", Tools.getPrefix("/tmp/alpha"));
		assertNull(Tools.getPrefix("alpha"));
		assertTrue(Tools.urlGetFile(new URL("file:///tmp/alpha.txt")).contains("alpha.txt"));
		URL base = new URL("https://example.test/maps/a.mm");
		URL sibling = new URL("https://example.test/maps/b.mm");
		assertTrue(Tools.toRelativeURL(base, sibling).contains("b.mm"), Tools.toRelativeURL(base, sibling));
		URL foreign = new URL("https://other.test/maps/b.mm");
		assertEquals(foreign.toString(), Tools.toRelativeURL(base, foreign));
		assertEquals(3, Tools.countOccurrences("ababa", "a"));
		assertEquals(1, Tools.countOccurrences("aaa", "aa"));
		String basePath = Tools.getFreeMindBasePath();
		assertNotNull(basePath);
		assertFalse(basePath.isBlank(), basePath);
		FreeMindCommon common = new FreeMindCommon(new HeadlessFreeMind());
		String oldBase = common.getFreemindBaseDirOld();
		if (System.getProperty("java.class.path").contains("freemind.jar")) {
			assertFalse(oldBase.isBlank(), oldBase);
		} else {
			assertEquals(System.getProperty("user.dir"), oldBase);
		}
		assertEquals(oldBase, common.getFreemindBaseDirOld());

		XMLElement widths = new XMLElement();
		widths.parseString("<n width=\"thin\" n=\"4\"/>");
		Hashtable<Object, Integer> widthValues = new Hashtable<>();
		widthValues.put("thin", Integer.valueOf(1));
		assertEquals(1, widths.getIntAttribute("width", widthValues, "missing", false));
		assertEquals(4, widths.getIntAttribute("n", widthValues, "0", true));
		String body = "<node TEXT=\"Alpha\"/>";
		XMLElement sliced = new XMLElement();
		sliced.parseString("xx" + body + "yy", 2, 2 + body.length());
		assertEquals("Alpha", sliced.getStringAttribute("TEXT"));

		String html = HtmlTools.plainToHTML("A & B\t<C>");
		assertTrue(html.contains("&amp;"), html);
		assertTrue(html.contains("&lt;"), html);
		assertTrue(html.contains("<html>"), html);
		String xhtml = HtmlTools.getInstance().toXhtml("<html><body><p>Alpha</p></body></html>");
		assertNotNull(xhtml);
		assertTrue(xhtml.toLowerCase().contains("alpha"), xhtml);
		assertNull(HtmlTools.getInstance().toXhtml("Alpha"));

		MindMapNodesSelection selection = new MindMapNodesSelection("<node/>", null, "Alpha", "{\\rtf}",
				"<b>Alpha</b>", "move", null, List.of("id1"));
		assertTrue(selection.getTransferDataFlavors().length >= 6);
		assertEquals("Alpha", selection.getTransferData(DataFlavor.stringFlavor));
		assertTrue(selection.isDataFlavorSupported(MindMapNodesSelection.mindMapNodesFlavor));
		assertFalse(selection.isDataFlavorSupported(MindMapNodesSelection.fileListFlavor));

		JOptionalSplitPane split = new JOptionalSplitPane();
		split.setComponent(new JLabel("Left"), 0);
		split.setComponent(new JLabel("Right"), 1);
		JLabel replaced = new JLabel("Replaced");
		split.setComponent(replaced, 1);
		assertEquals(2, split.getAmountOfComponents());
		assertEquals(replaced, ((JSplitPane) split.getComponent(0)).getRightComponent());

		DefaultTableModel table = new DefaultTableModel(new Object[] { "Name", "Note" }, 0);
		table.addRow(new Object[] { "b", "1" });
		table.addRow(new Object[] { "a", "2" });
		TableSorter sorter = new TableSorter(table);
		sorter.setSortingStatus(0, TableSorter.ASCENDING);
		assertEquals("a", sorter.getValueAt(0, 0));
		Method modelToView = TableSorter.class.getDeclaredMethod("getModelToView");
		modelToView.setAccessible(true);
		int[] order = (int[]) modelToView.invoke(sorter);
		assertEquals(2, order.length);
		table.setValueAt("9", 0, 1);
		assertEquals("9", table.getValueAt(0, 1));
		table.addRow(new Object[] { "c", "3" });
		assertEquals(3, sorter.getRowCount());

		JSpinField spin = new JSpinField(1, 10);
		spin.adjustWidthToMaximumValue();
		assertTrue(spin.getPreferredSize().width > 0);
		Field textField = JSpinField.class.getDeclaredField("textField");
		textField.setAccessible(true);
		javax.swing.JTextField spinText = (javax.swing.JTextField) textField.get(spin);
		spinText.setText("5");
		spin.caretUpdate(null);
		assertEquals(5, spin.getValue());
		spinText.setText("99");
		spin.caretUpdate(null);
		assertEquals(Color.RED, spinText.getForeground());
		assertEquals(5, spin.getValue());
		spinText.setText("x");
		spin.caretUpdate(null);
		assertEquals(Color.RED, spinText.getForeground());

		JCalendar calendar = new JCalendar();
		calendar.setLocale(Locale.GERMANY);
		assertEquals(Locale.GERMANY, calendar.getLocale());
		calendar.setWeekOfYearVisible(true);
		assertEquals(Locale.GERMANY, calendar.getLocale());
		assertNotNull(calendar.getDayChooser());

		Calendar june = new GregorianCalendar(2020, Calendar.JUNE, 15);
		JTripleCalendar triple = new JTripleCalendar(0, june);
		assertEquals(15, triple.getCalendar().get(Calendar.DAY_OF_MONTH));
		triple.setCalendar(new GregorianCalendar(2020, Calendar.AUGUST, 3));
		assertEquals(Calendar.AUGUST, triple.getCalendar().get(Calendar.MONTH));
		assertEquals(3, triple.getCalendar().get(Calendar.DAY_OF_MONTH));
		triple.setCalendar(new GregorianCalendar(2024, Calendar.JANUARY, 2));
		assertEquals(2024, triple.getCalendar().get(Calendar.YEAR));

		JPanel source = new JPanel();
		try {
			assertNull(KeyEventWorkaround.processKeyEvent(new KeyEvent(source, KeyEvent.KEY_PRESSED, 1L, 0,
					KeyEvent.VK_DEAD_GRAVE, '\0')));
			assertNull(KeyEventWorkaround.processKeyEvent(new KeyEvent(source, KeyEvent.KEY_PRESSED, 2L, 0,
					KeyEvent.VK_ALT, KeyEvent.CHAR_UNDEFINED)));
			KeyEvent left = new KeyEvent(source, KeyEvent.KEY_RELEASED, 3L, 0, KeyEvent.VK_LEFT,
					KeyEvent.CHAR_UNDEFINED);
			assertSame(left, KeyEventWorkaround.processKeyEvent(left));
			assertNull(KeyEventWorkaround.processKeyEvent(new KeyEvent(source, KeyEvent.KEY_RELEASED, 4L, 0,
					KeyEvent.VK_ALT, KeyEvent.CHAR_UNDEFINED)));
			assertNull(KeyEventWorkaround.processKeyEvent(
					new KeyEvent(source, KeyEvent.KEY_TYPED, 5L, 0, KeyEvent.VK_UNDEFINED, 'B')));
			long now = System.currentTimeMillis();
			KeyEvent pad = new KeyEvent(source, KeyEvent.KEY_PRESSED, now, InputEvent.CTRL_DOWN_MASK,
					KeyEvent.VK_NUMPAD1, '1');
			assertSame(pad, KeyEventWorkaround.processKeyEvent(pad));
			assertNull(KeyEventWorkaround.processKeyEvent(
					new KeyEvent(source, KeyEvent.KEY_TYPED, now + 10, 0, KeyEvent.VK_UNDEFINED, '1')));
			assertNull(KeyEventWorkaround.processKeyEvent(new KeyEvent(source, KeyEvent.KEY_PRESSED, now, 0,
					KeyEvent.VK_A, 'a')));
			assertNull(KeyEventWorkaround.processKeyEvent(
					new KeyEvent(source, KeyEvent.KEY_TYPED, now, 0, KeyEvent.VK_UNDEFINED, (char) 1)));
			KeyEventWorkaround.numericKeypadKey();
			long shiftTime = now + 5000;
			assertNull(KeyEventWorkaround.processKeyEvent(new KeyEvent(source, KeyEvent.KEY_PRESSED, shiftTime, 0,
					KeyEvent.VK_SHIFT, KeyEvent.CHAR_UNDEFINED)));
			assertNull(KeyEventWorkaround.processKeyEvent(new KeyEvent(source, KeyEvent.KEY_TYPED, shiftTime + 800, 0,
					KeyEvent.VK_UNDEFINED, '\n')));
		} finally {
			Field modifiers = KeyEventWorkaround.class.getDeclaredField("modifiers");
			modifiers.setAccessible(true);
			modifiers.setInt(null, 0);
			Field last = KeyEventWorkaround.class.getDeclaredField("last");
			last.setAccessible(true);
			last.setInt(null, 0);
			Field lastKeyTime = KeyEventWorkaround.class.getDeclaredField("lastKeyTime");
			lastKeyTime.setAccessible(true);
			lastKeyTime.setLong(null, 0L);
		}
	}

	@Test
	void aFoldedStageAJoinedHtmlNodeAndAClonedArrowStayOnTheMap() throws Exception {
		Opened opened = openMap("Alpha", "Beta");
		MindMapNode alpha = opened.child("Alpha");
		MindMapNode beta = opened.child("Beta");
		MindMapNode child = opened.mode.addNewNode(alpha, 0, false);
		opened.mode.setNodeText(child, "Child");
		opened.mode.setNodeColor(alpha, Color.BLUE);
		opened.mode.setBold(alpha, true);
		StylePattern pattern = new StylePattern(alpha);
		assertTrue(pattern.getAppliesToNode());
		assertTrue(pattern.getAppliesToNodeFont());
		assertTrue(pattern.getAppliesToEdge());
		assertFalse(pattern.getAppliesToNodeIcon());
		assertTrue(pattern.toString().contains("edge:"), pattern.toString());

		EdgeAdapter edge = (EdgeAdapter) alpha.getEdge();
		edge.setColor(Color.RED);
		EdgeAdapter copiedEdge = (EdgeAdapter) edge.clone();
		copiedEdge.setColor(Color.BLUE);
		assertEquals(Color.RED, edge.getColor());
		assertEquals(Color.BLUE, copiedEdge.getColor());

		opened.mode.addLink(alpha, beta);
		MindMapArrowLinkModel link = (MindMapArrowLinkModel) arrowFrom(opened, alpha);
		link.clone();
		link.setStartInclination(new Point(3, 4));
		link.setEndInclination(new Point(5, 6));
		link.setEndArrow("none");
		String none = link.getEndArrow();
		link.setEndArrow("nope");
		assertEquals(none, link.getEndArrow());
		link.setEndArrow("default");
		assertNotEquals(none, link.getEndArrow());
		link.setStartArrow(null);
		assertEquals(none, link.getStartArrow());
		MindMapArrowLinkModel copy = (MindMapArrowLinkModel) link.clone();
		copy.setStartInclination(new Point(9, 9));
		assertEquals(new Point(3, 4), link.getStartInclination());
		assertEquals(new Point(9, 9), copy.getStartInclination());
		assertEquals(new Point(5, 6), copy.getEndInclination());
		assertTrue(copy.toString().contains("Source="), copy.toString());
		assertTrue(opened.model.getLinkRegistry().getAllSources(beta).contains(alpha));

		DepthUnfold unfold = new DepthUnfold();
		bind(unfold, opened, new Properties());
		assertEquals(2, unfold.maxDepth(opened.root));
		unfold.foldStageN(opened.root, 1);
		assertTrue(alpha.isFolded());
		assertTrue(((NodeAdapter) child).hasFoldedParents());
		unfold.unfoldStageN(opened.root, 2);
		assertFalse(alpha.isFolded());
		assertFalse(((NodeAdapter) child).hasFoldedParents());
		assertEquals("Child", child.getText());

		File mapFile = File.createTempFile("tip", ".mm");
		mapFile.deleteOnExit();
		opened.model.setFile(mapFile);
		((NodeAdapter) beta).setLink("missing.mm");
		assertFalse(((NodeAdapter) beta).getToolTip().toString().contains("<img"),
				((NodeAdapter) beta).getToolTip().toString());
		((NodeAdapter) beta).setToolTip("preview", "<img src=\"old\" alt=\"missing.mm\"/>");
		opened.mode.setLink(beta, "https://example.test/b");
		assertFalse(((NodeAdapter) beta).getToolTip().containsKey("preview"),
				((NodeAdapter) beta).getToolTip().toString());

		String found = opened.mode.getController().getMapModuleManager()
				.checkIfFileIsAlreadyOpened(mapFile.toURI().toURL());
		assertNotNull(found);
		File other = File.createTempFile("other", ".mm");
		other.deleteOnExit();
		assertNull(opened.mode.getController().getMapModuleManager()
				.checkIfFileIsAlreadyOpened(other.toURI().toURL()));

		selectOnly(opened, opened.root);
		opened.view.scrollNodeToVisible(opened.view.getNodeView(alpha), 12);
		opened.view.scrollNodeToVisible(opened.view.getNodeView(alpha), -6);
		Method getFirst = NodeView.class.getDeclaredMethod("getFirst", java.awt.Component.class, boolean.class,
				boolean.class);
		getFirst.setAccessible(true);
		NodeView first = (NodeView) getFirst.invoke(opened.view.getRoot(), null, false, false);
		assertNotNull(first);
		assertNotEquals("ROOT", first.getModel().getText());
		BufferedImage painted = paint(opened.view);
		assertTrue(nonWhitePixels(painted) > 0);

		Field nodeKind = ConditionFactory.class.getDeclaredField("FILTER_NODE");
		nodeKind.setAccessible(true);
		Field iconKind = ConditionFactory.class.getDeclaredField("FILTER_ICON");
		iconKind.setAccessible(true);
		ConditionFactory factory = new ConditionFactory();
		NamedObject nodeAttribute = (NamedObject) nodeKind.get(null);
		NamedObject iconAttribute = (NamedObject) iconKind.get(null);
		NamedObject[] nodeNames = factory.getNodeConditionNames();
		assertNull(factory.createCondition(nodeAttribute, nodeNames[0], "", false));
		Condition equalAlpha = factory.createCondition(nodeAttribute, nodeNames[1], "alpha", true);
		assertTrue(equalAlpha.checkNode(opened.mode.getController(), alpha));
		assertFalse(equalAlpha.checkNode(opened.mode.getController(), beta));
		Condition greaterThanM = factory.createCondition(nodeAttribute, nodeNames[3], "M", false);
		assertFalse(greaterThanM.checkNode(opened.mode.getController(), alpha));
		assertFalse(greaterThanM.checkNode(opened.mode.getController(), beta));
		opened.mode.addIcon(alpha, MindIcon.factory("idea"));
		Object[] iconNames = factory.getIconConditionNames();
		Condition hasIdea = factory.createCondition(iconAttribute, (NamedObject) iconNames[0], "idea", false);
		Condition lacksIdea = factory.createCondition(iconAttribute, (NamedObject) iconNames[1], "idea", false);
		assertTrue(hasIdea.checkNode(opened.mode.getController(), alpha));
		assertFalse(lacksIdea.checkNode(opened.mode.getController(), alpha));
		assertTrue(lacksIdea.checkNode(opened.mode.getController(), beta));
		assertEquals(0, IconNotContainedCondition.iconLastIndex(alpha, "idea"));
		assertTrue(hasIdea.getListCellRendererComponent().getComponentCount() >= 2);
		assertTrue(lacksIdea.getListCellRendererComponent().getComponentCount() >= 2);
		ConditionNotSatisfiedDecorator negated = new ConditionNotSatisfiedDecorator(equalAlpha);
		assertFalse(negated.checkNode(opened.mode.getController(), alpha));
		assertTrue(negated.checkNode(opened.mode.getController(), beta));
		assertTrue(negated.getListCellRendererComponent().getComponentCount() >= 2);

		DefaultComboBoxModel<Condition> stored = new DefaultComboBoxModel<>();
		stored.addElement(equalAlpha);
		stored.addElement(negated);
		File filterFile = File.createTempFile("filters", ".xml");
		filterFile.deleteOnExit();
		FilterController filters = new FilterController(opened.mode.getController());
		try {
			Method saveConditions = FilterController.class.getDeclaredMethod("saveConditions",
					DefaultComboBoxModel.class, String.class);
			saveConditions.setAccessible(true);
			saveConditions.invoke(filters, stored, filterFile.getPath());
			assertTrue(Files.readString(filterFile.toPath()).contains("alpha"), Files.readString(filterFile.toPath()));
			DefaultComboBoxModel<Condition> loaded = new DefaultComboBoxModel<>();
			Method loadConditions = FilterController.class.getDeclaredMethod("loadConditions",
					DefaultComboBoxModel.class, String.class);
			loadConditions.setAccessible(true);
			loadConditions.invoke(filters, loaded, filterFile.getPath());
			assertEquals(2, loaded.getSize());
			assertTrue(loaded.getElementAt(0).checkNode(opened.mode.getController(), alpha));
			assertFalse(loaded.getElementAt(1).checkNode(opened.mode.getController(), alpha));
		} finally {
			opened.mode.getController().getMapModuleManager().removeListener(filters);
		}

		HierarchicalIcons icons = new HierarchicalIcons();
		icons.setName("hierarchical");
		bind(icons, opened, new Properties());
		icons.invoke(opened.root);
		assertNotNull(opened.root.getStateIcons().get("hierarchical"));
		((NodeAdapter) opened.root).removeAllHooks();
		assertTrue(((NodeAdapter) opened.root).getHooks().isEmpty());

		MindMapNode gamma = opened.mode.addNewNode(opened.root, opened.root.getChildCount(), false);
		opened.mode.setNodeText(gamma, "<html><body><p>Gamma</p></body></html>");
		MindMapNode delta = opened.mode.addNewNode(opened.root, opened.root.getChildCount(), false);
		opened.mode.setNodeText(delta, "Delta");
		selectOnly(opened, gamma);
		opened.mode.joinNodes(gamma, new ArrayList<>(List.of(gamma, delta)));
		assertTrue(gamma.getText().contains("Gamma"), gamma.getText());
		assertTrue(gamma.getText().contains("Delta"), gamma.getText());
		assertFalse(childTexts(opened.root).contains("Delta"), childTexts(opened.root).toString());

		opened.mode.setNodeText(alpha, "Renamed");
		Field pairs = UndoAction.class.getDeclaredField("actionPairList");
		pairs.setAccessible(true);
		assertFalse(((Vector<?>) pairs.get(opened.mode.undo)).isEmpty());
		opened.mode.undo.print();
		assertEquals("Renamed", alpha.getText());
		assertFalse(((Vector<?>) pairs.get(opened.mode.undo)).isEmpty());

		selectOnly(opened, opened.root);
		CommonNodeKeyListener listener = new CommonNodeKeyListener(opened.mode, (event, addNew, editLong) -> {
			throw new AssertionError("edit");
		});
		for (int code : new int[] { KeyEvent.VK_ENTER, KeyEvent.VK_ESCAPE, KeyEvent.VK_DELETE, KeyEvent.VK_SPACE,
				KeyEvent.VK_INSERT, KeyEvent.VK_TAB }) {
			listener.keyPressed(key(opened.view, code, 0));
		}
		assertEquals("ROOT", opened.view.getSelected().getModel().getText());
		listener.keyPressed(key(opened.view, KeyEvent.VK_DOWN, 0));
		assertNotNull(opened.view.getSelected());

		selectOnly(opened, alpha);
		opened.view.move(key(opened.view, KeyEvent.VK_LEFT, InputEvent.SHIFT_DOWN_MASK));
		assertFalse(opened.view.getSelecteds().isEmpty());
		assertEquals("Renamed", alpha.getText());

		MapModuleManager manager = opened.mode.getController().getMapModuleManager();
		Method next = MapModuleManager.class.getDeclaredMethod("nextMapModule");
		next.setAccessible(true);
		next.invoke(manager);
		Method previous = MapModuleManager.class.getDeclaredMethod("previousMapModule");
		previous.setAccessible(true);
		previous.invoke(manager);
		assertEquals("Renamed", alpha.getText());
	}

	@Test
	void aPresentExportFileIsCopiedAndAMissingOneIsSkipped() throws Exception {
		File sourceDir = Files.createTempDirectory("export-src").toFile();
		File destDir = Files.createTempDirectory("export-dest").toFile();
		Files.writeString(new File(sourceDir, "note.txt").toPath(), "copied");
		CopyingExport export = new CopyingExport();
		export.copy(sourceDir.getAbsolutePath(), "note.txt", destDir.getAbsolutePath());
		assertEquals("copied", Files.readString(new File(destDir, "note.txt").toPath()));
		export.copy(sourceDir.getAbsolutePath(), "missing.txt", destDir.getAbsolutePath());
		assertFalse(new File(destDir, "missing.txt").exists());
		ExportHook.ImageFilter filter = new ExportHook.ImageFilter("png", "PNG");
		assertTrue(filter.accept(sourceDir));
		assertTrue(filter.accept(new File(sourceDir, "pic.PNG")));
		assertFalse(filter.accept(new File(sourceDir, "pic.gif")));
		assertEquals("PNG", filter.getDescription());
		assertEquals("gif", new ExportHook.ImageFilter("gif", null).getDescription());
	}

	@Test
	void formatActionsChangeTheSelectedNode() throws Exception {
		Opened opened = openMap("Alpha", "Beta", "Gamma");
		MindMapNode alpha = opened.child("Alpha");
		MindMapNode mid = opened.mode.addNewNode(alpha, 0, false);
		opened.mode.setNodeText(mid, "Mid");
		opened.mode.addNewNode(mid, 0, false);
		selectOnly(opened, alpha);
		ActionEvent event = new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "format");
		JMenuItem item = new JMenuItem();
		BoldAction bold = new BoldAction(opened.mode);
		bold.actionPerformed(event);
		assertTrue(alpha.isBold());
		assertTrue(bold.isSelected(item, bold));
		ItalicAction italic = new ItalicAction(opened.mode);
		italic.actionPerformed(event);
		assertTrue(alpha.isItalic());
		assertTrue(italic.isSelected(item, italic));
		StrikethroughAction strike = new StrikethroughAction(opened.mode);
		strike.actionPerformed(event);
		assertTrue(alpha.isStrikethrough());
		assertTrue(strike.isSelected(item, strike));
		UnderlinedAction underlined = new UnderlinedAction(opened.mode);
		underlined.actionPerformed(event);
		assertTrue(alpha.isUnderlined());
		assertTrue(underlined.isSelected(item, underlined));
		FontSizeAction size = new FontSizeAction(opened.mode);
		size.actionPerformed("20");
		assertEquals("20", alpha.getFontSize());
		opened.mode.increaseFontSize(alpha, 2);
		assertEquals("22", alpha.getFontSize());
		new FontFamilyAction(opened.mode).actionPerformed("Serif");
		assertTrue(alpha.getFont().getFamily().toLowerCase().contains("serif"), alpha.getFont().getFamily());
		CloudAction cloud = new CloudAction(opened.mode);
		assertFalse(cloud.isSelected(item, cloud));
		cloud.actionPerformed(event);
		assertNotNull(alpha.getCloud());
		assertTrue(cloud.isSelected(item, cloud));
		opened.mode.setCloudColor(alpha, Color.CYAN);
		assertEquals(Color.CYAN, alpha.getCloud().getColor());
		EdgeWidthAction width = new EdgeWidthAction(opened.mode, 8);
		width.actionPerformed(event);
		assertEquals(8, alpha.getEdge().getRealWidth());
		assertTrue(width.isSelected(item, width));
		EdgeStyleAction edgeStyle = new EdgeStyleAction(opened.mode, "bezier");
		edgeStyle.actionPerformed(event);
		assertEquals("bezier", alpha.getEdge().getStyle());
		assertTrue(edgeStyle.isSelected(item, edgeStyle));
		NodeStyleAction nodeStyle = new NodeStyleAction(opened.mode, MindMapNode.STYLE_BUBBLE);
		nodeStyle.actionPerformed(event);
		assertEquals(MindMapNode.STYLE_BUBBLE, alpha.getStyle());
		assertTrue(nodeStyle.isSelected(item, nodeStyle));
		Color beforeBlend = alpha.getColor() == null ? MapView.standardNodeTextColor : alpha.getColor();
		new NodeColorBlendAction(opened.mode).actionPerformed(event);
		assertNotNull(alpha.getColor());
		assertNotEquals(beforeBlend, alpha.getColor());
		new UseRichFormattingAction(opened.mode).actionPerformed(event);
		assertTrue(HtmlTools.isHtmlNode(alpha.getText()), alpha.getText());
		assertTrue(alpha.getText().contains("Alpha"), alpha.getText());
		new UsePlainTextAction(opened.mode).actionPerformed(event);
		assertFalse(HtmlTools.isHtmlNode(alpha.getText()), alpha.getText());
		assertTrue(alpha.getText().contains("Alpha"), alpha.getText());
		opened.mode.addIcon(alpha, MindIcon.factory("idea"));
		assertEquals(1, alpha.getIcons().size());
		new RemoveAllIconsAction(opened.mode,
				new IconAction(opened.mode, MindIcon.factory("idea"), new RemoveIconAction(opened.mode)))
				.actionPerformed(event);
		assertEquals(0, alpha.getIcons().size());
		Pattern pattern = new Pattern();
		pattern.setName("Green");
		PatternNodeColor color = new PatternNodeColor();
		color.setValue("#00aa00");
		pattern.setPatternNodeColor(color);
		ApplyPatternAction apply = new ApplyPatternAction(opened.mode, pattern);
		assertEquals("Green", apply.getPattern().getName());
		apply.apply(opened.model, (MindMapNodeModel) alpha);
		assertEquals(new Color(0, 0xaa, 0), alpha.getColor());
		assertEquals(EdgeAdapter.EDGE_WIDTH_THIN_STRING,
				ApplyPatternAction.edgeWidthIntToString(EdgeAdapter.WIDTH_THIN));
		assertNull(ApplyPatternAction.edgeWidthIntToString(EdgeAdapter.DEFAULT_WIDTH));
		assertEquals("4", ApplyPatternAction.edgeWidthIntToString(4));
		selectOnly(opened, mid);
		new ToggleFoldedAction(opened.mode).actionPerformed(event);
		assertTrue(mid.isFolded());
		selectOnly(opened, alpha);
		new ToggleChildrenFoldedAction(opened.mode).actionPerformed(event);
		assertFalse(mid.isFolded());
		MindMapNode gamma = opened.child("Gamma");
		int before = opened.root.getChildPosition(gamma);
		selectOnly(opened, gamma);
		new NodeUpAction(opened.mode).actionPerformed(event);
		assertTrue(opened.root.getChildPosition(gamma) < before);
		((NodeAdapter) gamma).setShiftY(12);
		selectOnly(opened, gamma);
		new MoveNodeAction(opened.mode).actionPerformed(event);
		assertEquals(0, ((NodeAdapter) gamma).getShiftY());
		selectOnly(opened, alpha);
		new SelectBranchAction(opened.mode).actionPerformed(event);
		assertTrue(opened.mode.getSelecteds().size() >= 2);
		new SelectAllAction(opened.mode).actionPerformed(event);
		assertTrue(opened.mode.getSelecteds().size() >= 3);
		opened.view.setMoveCursor(true);
		assertEquals(Cursor.MOVE_CURSOR, opened.view.getCursor().getType());
		opened.view.setMoveCursor(false);
		assertEquals(Cursor.DEFAULT_CURSOR, opened.view.getCursor().getType());
	}

	@Test
	void aRichMapKeepsEdgeCloudArrowFontAndAttributes() throws Exception {
		Opened opened = openMap("Placeholder");
		String xml = "<map version=\"1.1.0\">"
				+ "<attribute_registry><attribute_name NAME=\"Owner\" VISIBLE=\"true\">"
				+ "<attribute_value VALUE=\"Ada\"/>"
				+ "</attribute_name></attribute_registry>"
				+ "<node TEXT=\"Root\" ID=\"root\" COLOR=\"#000000\" BACKGROUND_COLOR=\"#ffffff\" STYLE=\"fork\" "
				+ "POSITION=\"right\" VSHIFT=\"3\" VGAP=\"4\" HGAP=\"5\" LINK=\"https://example.test\" "
				+ "CREATED=\"1600000000000\" MODIFIED=\"1600000001000\">"
				+ "<font NAME=\"Dialog\" SIZE=\"14\" BOLD=\"true\" ITALIC=\"true\" STRIKETHROUGH=\"true\"/>"
				+ "<edge STYLE=\"bezier\" COLOR=\"#ff0000\" WIDTH=\"thin\"/>"
				+ "<cloud STYLE=\"bezier\" COLOR=\"#00ff00\" WIDTH=\"4\"/>"
				+ "<icon BUILTIN=\"idea\"/>"
				+ "<attribute NAME=\"Owner\" VALUE=\"Ada\"/>"
				+ "<arrowlink STYLE=\"linear\" ID=\"al1\" COLOR=\"#0000ff\" DESTINATION=\"child\" "
				+ "REFERENCETEXT=\"see\" STARTINCLINATION=\"10;20\" ENDINCLINATION=\"30;40\" "
				+ "STARTARROW=\"None\" ENDARROW=\"Default\" WIDTH=\"2\"/>"
				+ "<hook NAME=\"accessories/plugins/NoSuchHook.properties\"/>"
				+ "<richcontent TYPE=\"NOTE\"><html><body>Remember</body></html></richcontent>"
				+ "<node TEXT=\"Child\" ID=\"child\" FOLDED=\"true\" POSITION=\"left\">"
				+ "<font NAME=\"Serif\" SIZE=\"12\"/>"
				+ "<node TEXT=\"Grand\"/>"
				+ "</node>"
				+ "</node>"
				+ "</map>";
		MindMapNode loaded = opened.model.loadTree(new Tools.StringReaderCreator(xml), MapAdapter.sDontAskInstance);
		assertEquals("Root", loaded.getText());
		assertEquals(Color.BLACK, loaded.getColor());
		assertEquals(Color.WHITE, loaded.getBackgroundColor());
		assertEquals("fork", loaded.getStyle());
		assertEquals("https://example.test", loaded.getLink());
		assertEquals(3, ((NodeAdapter) loaded).getShiftY());
		assertEquals(4, ((NodeAdapter) loaded).getVGap());
		assertEquals(5, ((NodeAdapter) loaded).getHGap());
		assertEquals(1600000000000L, loaded.getHistoryInformation().getCreatedAt().getTime());
		assertEquals(1600000001000L, loaded.getHistoryInformation().getLastModifiedAt().getTime());
		assertEquals(Font.BOLD + Font.ITALIC, loaded.getFont().getStyle());
		assertEquals(14, loaded.getFont().getSize());
		assertEquals(TextAttribute.STRIKETHROUGH_ON,
				loaded.getFont().getAttributes().get(TextAttribute.STRIKETHROUGH));
		assertEquals("bezier", loaded.getEdge().getStyle());
		assertEquals(Color.RED, loaded.getEdge().getColor());
		assertEquals(EdgeAdapter.WIDTH_THIN, loaded.getEdge().getRealWidth());
		assertNotNull(loaded.getCloud());
		assertEquals(Color.GREEN, loaded.getCloud().getColor());
		assertEquals("idea", loaded.getIcons().get(0).getName());
		assertEquals("Ada", loaded.getAttribute("Owner"));
		assertTrue(loaded.getNoteText().contains("Remember"), loaded.getNoteText());
		MindMapNode child = (MindMapNode) loaded.getChildAt(0);
		assertEquals("Child", child.getText());
		assertTrue(child.isFolded());
		assertTrue(child.isLeft());
		assertEquals("Grand", ((MindMapNode) child.getChildAt(0)).getText());
		java.util.Vector<MindMapLink> links = opened.model.getLinkRegistry().getAllLinksFromMe(loaded);
		assertFalse(links.isEmpty());
		assertEquals("Child", links.get(0).getTarget().getText());
		assertFalse(loaded.getHooks().isEmpty());
	}

	@Test
	void aSpinFieldClampsAndACardShowsTheVisibleSize() {
		JSpinField spin = new JSpinField(1, 10);
		spin.setValue(4);
		assertEquals(4, spin.getValue());
		spin.setValue(99);
		assertEquals(10, spin.getValue());
		spin.setValue(-3);
		assertEquals(1, spin.getValue());
		spin.setMinimum(2);
		spin.setMaximum(8);
		assertEquals(2, spin.getMinimum());
		assertEquals(8, spin.getMaximum());
		spin.setValue(1);
		assertEquals(2, spin.getValue());
		spin.setHorizontalAlignment(SwingConstants.RIGHT);
		spin.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
		spin.setForeground(Color.BLUE);
		spin.setEnabled(false);
		assertFalse(spin.isEnabled());
		spin.setEnabled(true);
		assertTrue(spin.isEnabled());
		JPanel cards = new JPanel(new VariableSizeCardLayout(4, 6));
		JLabel wide = new JLabel("wide");
		wide.setPreferredSize(new Dimension(80, 20));
		JLabel tall = new JLabel("tall");
		tall.setPreferredSize(new Dimension(10, 50));
		tall.setVisible(false);
		cards.add(wide, "wide");
		cards.add(tall, "tall");
		Dimension preferred = cards.getPreferredSize();
		assertTrue(preferred.width >= 88, preferred.toString());
		assertTrue(preferred.height >= 32 && preferred.height < 50, preferred.toString());
	}

	@Test
	void calendarRepetitionsStepForwardFromJune() {
		Calendar start = CalendarMarkingEvaluator.createIsoCalendar();
		start.set(2020, Calendar.JUNE, 15, 0, 0, 0);
		start.set(Calendar.MILLISECOND, 0);
		long startMillis = start.getTimeInMillis();
		CalendarMarkings markings = new CalendarMarkings();
		markings.addCalendarMarking(marking("never", CalendarMarking.NEVER, startMillis, 0, null));
		markings.addCalendarMarking(marking("daily", CalendarMarking.DAILY, startMillis, 1, null));
		markings.addCalendarMarking(marking("weekly", CalendarMarking.WEEKLY, startMillis, 1, null));
		markings.addCalendarMarking(
				marking("week-day", CalendarMarking.WEEKLY_EVERY_NTH_DAY, startMillis, 1, Calendar.MONDAY));
		markings.addCalendarMarking(marking("monthly", CalendarMarking.MONTHLY, startMillis, 1, null));
		markings.addCalendarMarking(
				marking("month-day", CalendarMarking.MONTHLY_EVERY_NTH_DAY, startMillis, 1, 15));
		markings.addCalendarMarking(
				marking("month-week", CalendarMarking.MONTHLY_EVERY_NTH_WEEK, startMillis, 1, 2));
		markings.addCalendarMarking(marking("yearly", CalendarMarking.YEARLY, startMillis, 1, null));
		markings.addCalendarMarking(
				marking("year-day", CalendarMarking.YEARLY_EVERY_NTH_DAY, startMillis, 1, 100));
		markings.addCalendarMarking(
				marking("year-week", CalendarMarking.YEARLY_EVERY_NTH_WEEK, startMillis, 1, 10));
		markings.addCalendarMarking(
				marking("year-month", CalendarMarking.YEARLY_EVERY_NTH_MONTH, startMillis, 1, Calendar.JUNE));
		CalendarMarkings unknown = new CalendarMarkings();
		unknown.addCalendarMarking(marking("unknown", "nope", startMillis, 1, 1));
		assertTrue(new CalendarMarkingEvaluator(unknown).getAtLeastTheFirstNEntries(3).isEmpty());
		CalendarMarkingEvaluator evaluator = new CalendarMarkingEvaluator(markings);
		java.util.Set<Calendar> dates = evaluator.getAtLeastTheFirstNEntries(6);
		assertTrue(dates.size() >= 6, String.valueOf(dates.size()));
		assertEquals(1, markings.getCalendarMarking(0).getRepeatEachNOccurence().intValue());
		CalendarMarking marked = evaluator.isMarked(start);
		assertNotNull(marked);
		assertEquals(marked, evaluator.isMarked(start));
		Calendar far = CalendarMarkingEvaluator.createIsoCalendar();
		far.set(1990, Calendar.JANUARY, 1, 0, 0, 0);
		far.set(Calendar.MILLISECOND, 0);
		assertNull(evaluator.isMarked(far));
		CalendarMarkings replacement = new CalendarMarkings();
		replacement.addCalendarMarking(marking("only", CalendarMarking.NEVER, startMillis, 1, null));
		evaluator.changeMarkings(replacement);
		assertEquals("only", evaluator.isMarked(start).getName());
		new CalendarMarkingEvaluator(replacement).print();
	}

	private static XmlAction cloneFiltered(ClonePasteAction.Registration registration, XmlAction action) {
		return registration.filterAction(new ActionPair(action, new FoldAction())).getDoAction();
	}

	private static ClonePlugin clonePlugin(Opened opened, MindMapNode node) {
		ClonePlugin plugin = new ClonePlugin();
		plugin.setController(opened.mode);
		plugin.setNode(node);
		XMLElement xml = new XMLElement();
		XMLElement parameters = new XMLElement();
		parameters.setName("Parameters");
		xml.addChild(parameters);
		plugin.loadFrom(xml);
		return plugin;
	}

	private static String pairTexts(List<Tools.MindMapNodePair> pairs) {
		StringBuilder texts = new StringBuilder();
		for (Tools.MindMapNodePair pair : pairs) {
			texts.append(pair.getCorresponding().getText()).append(' ');
		}
		return texts.toString();
	}

	private static String hierarchicalHookName(Opened opened) {
		String hookName = null;
		for (String candidate : opened.mode.getHookFactory().getPossibleNodeHooks()) {
			if (candidate.contains("HierarchicalIcons")) {
				hookName = candidate;
			}
		}
		assertNotNull(hookName, opened.mode.getHookFactory().getPossibleNodeHooks().toString());
		return hookName;
	}

	private static KeyEvent key(java.awt.Component source, int code, int modifiers) {
		return new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), modifiers, code,
				KeyEvent.CHAR_UNDEFINED);
	}

	private static void resetKeyWorkaround() throws Exception {
		keyWorkaroundModifiers(0);
		keyWorkaroundTime(0L);
		Field last = KeyEventWorkaround.class.getDeclaredField("last");
		last.setAccessible(true);
		last.setInt(null, 0);
	}

	private static void keyWorkaroundModifiers(int modifiers) throws Exception {
		Field field = KeyEventWorkaround.class.getDeclaredField("modifiers");
		field.setAccessible(true);
		field.setInt(null, modifiers);
	}

	private static void keyWorkaroundTime(long when) throws Exception {
		Field field = KeyEventWorkaround.class.getDeclaredField("lastKeyTime");
		field.setAccessible(true);
		field.setLong(null, when);
	}

	private static Object sampleArgument(Class<?> type) {
		if (type == String.class) {
			return "Ada";
		}
		if (type == boolean.class || type == Boolean.class) {
			return Boolean.TRUE;
		}
		if (type == int.class || type == Integer.class) {
			return Integer.valueOf(3);
		}
		if (type == long.class || type == Long.class) {
			return Long.valueOf(4L);
		}
		if (type == double.class || type == Double.class) {
			return Double.valueOf(1.5d);
		}
		if (type == float.class || type == Float.class) {
			return Float.valueOf(2.5f);
		}
		if (type == short.class || type == Short.class) {
			return Short.valueOf((short) 1);
		}
		if (type == byte.class || type == Byte.class) {
			return Byte.valueOf((byte) 1);
		}
		return null;
	}

	private static Method beanGetter(Class<?> type, String tail) {
		try {
			return type.getMethod("get" + tail);
		} catch (NoSuchMethodException missing) {
			try {
				return type.getMethod("is" + tail);
			} catch (NoSuchMethodException alsoMissing) {
				return null;
			}
		}
	}

	private static String resourcesProperty(String key) throws Exception {
		return resourcesMain().getProperty(key);
	}

	private static void setResourcesProperty(String key, String value) throws Exception {
		FreeMindMain main = resourcesMain();
		if (value == null) {
			main.getProperties().remove(key);
		} else {
			main.setProperty(key, value);
		}
	}

	private static FreeMindMain resourcesMain() throws Exception {
		Field mainField = Resources.class.getDeclaredField("main");
		mainField.setAccessible(true);
		return (FreeMindMain) mainField.get(Resources.getInstance());
	}

	private static void bind(NodeHookAdapter hook, Opened opened, Properties properties) {
		hook.setController(opened.mode);
		hook.setMap(opened.model);
		hook.setProperties(properties);
	}

	private static void selectOnly(Opened opened, MindMapNode node) {
		opened.view.getRoot().updateAll();
		opened.view.selectAsTheOnlyOneSelected(opened.view.getNodeView(node));
	}

	private static ArrowLinkView arrowView(MindMapArrowLink link, NodeView source, NodeView target) throws Exception {
		Constructor<ArrowLinkView> constructor = ArrowLinkView.class.getDeclaredConstructor(MindMapArrowLink.class,
				NodeView.class, NodeView.class);
		constructor.setAccessible(true);
		return constructor.newInstance(link, source, target);
	}

	private static MindMapArrowLink arrowFrom(Opened opened, MindMapNode source) {
		for (MindMapLink link : opened.model.getLinkRegistry().getAllLinks(source)) {
			if (link instanceof MindMapArrowLink arrow && arrow.getSource() == source) {
				return arrow;
			}
		}
		throw new AssertionError("no arrow from " + source.getText());
	}

	private static BufferedImage paint(java.awt.Component component) {
		BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
		component.paint(graphics);
		graphics.dispose();
		return image;
	}

	private static int redPixels(BufferedImage image) {
		int reds = 0;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int rgb = image.getRGB(x, y);
				int red = (rgb >> 16) & 0xFF;
				int green = (rgb >> 8) & 0xFF;
				int blue = rgb & 0xFF;
				if (red > 180 && green < 80 && blue < 80) {
					reds++;
				}
			}
		}
		return reds;
	}

	private static int nonWhitePixels(BufferedImage image) {
		int ink = 0;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				if ((image.getRGB(x, y) & 0xFFFFFF) != 0xFFFFFF) {
					ink++;
				}
			}
		}
		return ink;
	}

	private static List<String> childTexts(MindMapNode node) {
		List<String> texts = new ArrayList<>();
		for (int i = 0; i < node.getChildCount(); i++) {
			texts.add(((MindMapNode) node.getChildAt(i)).getText());
		}
		return texts;
	}

	private static List<String> allTexts(MindMapNode node) {
		List<String> texts = new ArrayList<>();
		texts.add(node.getText());
		for (int i = 0; i < node.getChildCount(); i++) {
			texts.addAll(allTexts((MindMapNode) node.getChildAt(i)));
		}
		return texts;
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
		return new Opened(modeController, view, model, root);
	}

	private static CalendarMarking marking(String name, String type, long start, int repeat, Integer first) {
		CalendarMarking marking = new CalendarMarking();
		marking.setName(name);
		marking.setColor("#ff0000");
		marking.setStartDate(start);
		marking.setEndDate(Long.valueOf(start + 400L * 24 * 60 * 60 * 1000));
		marking.setRepeatType(type);
		marking.setRepeatEachNOccurence(Integer.valueOf(repeat));
		if (first != null) {
			marking.setFirstOccurence(first);
		}
		return marking;
	}

	private static final class PasteLinks extends XMLElementAdapter {
		PasteLinks(MapFeedback feedback) {
			super(feedback);
		}

		void add(ArrowLinkAdapter link) {
			mArrowLinkAdapters.add(link);
		}

		void remember(String id, NodeAdapter node) {
			mIdToTarget.put(id, node);
		}

		void finish(MindMapLinkRegistry registry) {
			processUnfinishedLinks(registry);
		}
	}

	private static final class DepthUnfold extends UnfoldAll {
		int maxDepth(MindMapNode node) {
			return getMaxDepth(node);
		}
	}

	private static final class CopyingExport extends ExportHook {
		void copy(String dir, String name, String destination) {
			copyFromFile(dir, name, destination);
		}
	}

	private static final class HoldingController extends Controller {
		HoldingController(HeadlessFreeMind frame) {
			super(frame);
		}

		@Override
		public void setAllActions(boolean enabled) {
		}

		@Override
		public void beforeMapModuleChange(freemind.view.MapModule oldMapModule,
				freemind.modes.Mode oldMode, freemind.view.MapModule newMapModule,
				freemind.modes.Mode newMode) {
			if (oldMapModule != null && oldMapModule == newMapModule) {
				return;
			}
			super.beforeMapModuleChange(oldMapModule, oldMode, newMapModule, newMode);
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

	private static final class Opened {
		final MindMapController mode;
		final MapView view;
		final MindMapMapModel model;
		final MindMapNodeModel root;

		Opened(MindMapController mode, MapView view, MindMapMapModel model, MindMapNodeModel root) {
			this.mode = mode;
			this.view = view;
			this.model = model;
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
}
