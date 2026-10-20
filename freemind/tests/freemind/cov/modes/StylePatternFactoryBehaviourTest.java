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

package tests.freemind.cov.modes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.io.File;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import freemind.common.TextTranslator;
import freemind.controller.actions.generated.instance.Pattern;
import freemind.controller.actions.generated.instance.PatternChild;
import freemind.controller.actions.generated.instance.PatternEdgeWidth;
import freemind.controller.actions.generated.instance.PatternIcon;
import freemind.controller.actions.generated.instance.PatternNodeColor;
import freemind.controller.actions.generated.instance.PatternNodeFontSize;
import freemind.controller.actions.generated.instance.PatternNodeText;
import freemind.controller.actions.generated.instance.Patterns;
import freemind.main.FreeMindMain;
import freemind.main.HeadlessFreeMind;
import freemind.main.Resources;
import freemind.main.Tools;
import freemind.modes.EdgeAdapter;
import freemind.modes.MindIcon;
import freemind.modes.MindMapNode;
import freemind.modes.StylePatternFactory;
import freemind.modes.mindmapmode.MindMapController.MindMapControllerPlugin;
import freemind.modes.mindmapmode.MindMapNodeModel;
import freemind.modes.mindmapmode.actions.ApplyPatternAction.ExternalPatternAction;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Patterns load, save, describe, and apply color, text, icons, and child patterns. */
class StylePatternFactoryBehaviourTest {

	@Test
	void stringsAndFilesRoundTripNames() throws Exception {
		// backwards compatibility
		assertEquals("true", new StylePatternFactory().TRUE_VALUE);
		Pattern dummy = StylePatternFactory.getPatternFromString(null);
		assertEquals("dummy", dummy.getName());
		Pattern named = StylePatternFactory.getPatternFromString("<pattern name='Needs action'/>");
		assertEquals("Needs action", named.getName());
		Patterns empty = StylePatternFactory.getPatternsFromString(null);
		assertTrue(empty.getListChoiceList().isEmpty());
		Patterns one = StylePatternFactory.getPatternsFromString("<patterns><pattern name='Solo'/></patterns>");
		assertEquals("Solo", one.getListChoiceList().get(0).getName());

		File file = File.createTempFile("factory-patterns", ".xml");
		file.deleteOnExit();
		Files.writeString(file.toPath(),
				"<patterns>"
						+ "<pattern name='Needs action'><pattern_child value='Needs action'/></pattern>"
						+ "<pattern><pattern_node_text value='skip'/></pattern>"
						+ "</patterns>",
				StandardCharsets.UTF_8);
		java.lang.reflect.Field mainField = Resources.class.getDeclaredField("main");
		mainField.setAccessible(true);
		Resources resources = Resources.getInstance();
		FreeMindMain previousMain = (FreeMindMain) mainField.get(resources);
		mainField.set(resources, new HeadlessFreeMind() {
			public String getResourceString(String key) {
				if ("__pattern_string_Needs_action".equals(key)) {
					return "Needs action";
				}
				return super.getResourceString(key);
			}
		});
		List<Pattern> loaded;
		try {
			loaded = StylePatternFactory.loadPatterns(file);
		} finally {
			mainField.set(resources, previousMain);
		}
		assertEquals("Needs action", loaded.get(0).getName());
		assertEquals("Needs action", loaded.get(0).getOriginalName());
		assertEquals("Needs action", loaded.get(0).getPatternChild().getValue());
		assertNull(loaded.get(1).getName());

		StringWriter writer = new StringWriter();
		StylePatternFactory.savePatterns(writer, loaded);
		String saved = writer.toString();
		assertTrue(saved.contains("Needs action"), saved);
		assertNull(loaded.get(0).getOriginalName());
	}

	@Test
	void createAndDescribeAStyledNode() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Styled", "Plain");
		MindMapNode styled = opened.child("Styled");
		opened.mode.setNodeColor(styled, Color.BLUE);
		opened.mode.setNodeBackgroundColor(styled, Color.WHITE);
		opened.mode.setNodeStyle(styled, MindMapNode.STYLE_BUBBLE);
		opened.mode.addIcon(styled, MindIcon.factory("idea"));
		opened.mode.setEdgeStyle(styled, EdgeAdapter.EDGESTYLE_LINEAR);
		opened.mode.setEdgeColor(styled, Color.RED);
		Pattern pattern = StylePatternFactory.createPatternFromNode(styled);
		assertEquals(Tools.colorToXml(Color.BLUE), pattern.getPatternNodeColor().getValue());
		assertEquals("idea", pattern.getPatternIcon().getValue());
		assertEquals(EdgeAdapter.EDGESTYLE_LINEAR, pattern.getPatternEdgeStyle().getValue());
		assertEquals(StylePatternFactory.FALSE_VALUE, pattern.getPatternNodeFontBold().getValue());

		PatternNodeColor blank = new PatternNodeColor();
		pattern.setPatternNodeColor(blank);
		TextTranslator translator = key -> key;
		String described = StylePatternFactory.toString(pattern, translator);
		assertTrue(described.contains("-PatternToString.color"), described);
		blank.setValue("#0000ff");
		String filled = StylePatternFactory.toString(pattern, translator);
		assertTrue(filled.contains("+PatternToString.color"), filled);

		Pattern plain = StylePatternFactory.createPatternFromNode(opened.child("Plain"));
		assertNull(plain.getPatternIcon());
		assertNotNull(plain.getPatternNodeFontBold());
	}

	@Test
	void applyPatternSetsTextColorIconAndChildText() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Parent", "Kid");
		MindMapNode parent = opened.child("Parent");
		MindMapNode kid = new MindMapNodeModel("Kid", opened.mode.getMap());
		parent.insert(kid, 0);
		opened.mode.addIcon(parent, MindIcon.factory("idea"));

		Pattern pattern = new Pattern();
		PatternNodeText text = new PatternNodeText();
		text.setValue("Renamed");
		pattern.setPatternNodeText(text);
		PatternNodeColor color = new PatternNodeColor();
		color.setValue(Tools.colorToXml(Color.BLUE));
		pattern.setPatternNodeColor(color);
		PatternIcon icon = new PatternIcon();
		icon.setValue("idea");
		pattern.setPatternIcon(icon);
		PatternNodeFontSize size = new PatternNodeFontSize();
		size.setValue("nope");
		pattern.setPatternNodeFontSize(size);
		PatternEdgeWidth width = new PatternEdgeWidth();
		pattern.setPatternEdgeWidth(width);
		StylePatternFactory.applyPattern(pattern, parent, opened.mode);
		assertEquals("Renamed", parent.getText());
		assertEquals(Color.BLUE, parent.getColor());
		assertEquals(1, parent.getIcons().size());
		assertEquals(EdgeAdapter.DEFAULT_WIDTH, parent.getEdge().getRealWidth());

		icon.setValue(null);
		StylePatternFactory.applyPattern(pattern, parent, opened.mode);
		assertTrue(parent.getIcons().isEmpty());

		Pattern childPattern = new Pattern();
		childPattern.setName("KidStyle");
		PatternNodeText childText = new PatternNodeText();
		childText.setValue("Nested");
		childPattern.setPatternNodeText(childText);
		Pattern parentPattern = new Pattern();
		parentPattern.setName("ParentStyle");
		PatternChild child = new PatternChild();
		child.setValue("KidStyle");
		parentPattern.setPatternChild(child);
		Set<MindMapControllerPlugin> plugins = new HashSet<>();
		plugins.add(new ExternalPatternAction() {
			public void act(MindMapNode node, Pattern applied) {
				node.setShiftY(7);
			}
		});
		StylePatternFactory.applyPattern(parent, parentPattern, List.of(parentPattern, childPattern), plugins,
				opened.mode);
		assertEquals("Nested", kid.getText());
		assertEquals(7, parent.getShiftY());
	}
}
