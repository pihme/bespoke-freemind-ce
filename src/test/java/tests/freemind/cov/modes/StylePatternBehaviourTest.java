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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.EdgeAdapter;
import freemind.modes.MindIcon;
import freemind.modes.MindMapNode;
import freemind.modes.StylePattern;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** A pattern reports which parts it carries, and a file loads node, edge, and child patterns. */
@DisplayName("StylePatternBehaviourTest")
class StylePatternBehaviourTest {

	@Test
	void emptyPatternAppliesToNothingUntilAFieldIsSet() {
		// backwards compatibility
		StylePattern pattern = new StylePattern();
		assertFalse(pattern.getAppliesToNode());
		assertFalse(pattern.getAppliesToNodeFont());
		assertFalse(pattern.getAppliesToNodeIcon());
		assertFalse(pattern.getAppliesToEdge());
		assertFalse(pattern.getAppliesToChildren());
		pattern.setNodeColor(Color.BLUE);
		pattern.setNodeBackgroundColor(Color.WHITE);
		pattern.setNodeStyle("bubble");
		pattern.setNodeFontBold(Boolean.TRUE);
		pattern.setNodeFontItalic(Boolean.TRUE);
		pattern.setNodeFontFamily("Dialog");
		pattern.setNodeFontSize(Integer.valueOf(14));
		pattern.setNodeIcon(MindIcon.factory("idea"));
		pattern.setEdgeColor(Color.RED);
		pattern.setEdgeStyle("linear");
		pattern.setEdgeWidth(Integer.valueOf(2));
		pattern.setChildrenStylePattern(pattern);
		assertTrue(pattern.getAppliesToNode());
		assertTrue(pattern.getAppliesToNodeFont());
		assertTrue(pattern.getAppliesToNodeIcon());
		assertTrue(pattern.getAppliesToEdge());
		assertTrue(pattern.getAppliesToChildren());
		assertEquals("idea", pattern.getNodeIcon().getName());
	}

	@Test
	void fileLoadsNodeEdgeFontAndASelfChild() throws Exception {
		// backwards compatibility
		File file = File.createTempFile("patterns", ".xml");
		file.deleteOnExit();
		String xml = "<patterns>"
				+ "<pattern name=\"Self\">"
				+ "<node color=\"#112233\" background_color=\"#445566\" style=\"bubble\" icon=\"idea\" text=\"Hello\">"
				+ "<font name=\"Dialog\" bold=\"true\" italic=\"true\" size=\"18\"/>"
				+ "</node>"
				+ "<edge style=\"linear\" color=\"#ff0000\" width=\"thin\"/>"
				+ "<child pattern=\"Self\"/>"
				+ "</pattern>"
				+ "<pattern name=\"Missing\"><child pattern=\"Nope\"/></pattern>"
				+ "<pattern><edge width=\"3\"/></pattern>"
				+ "<pattern name=\"None\"><node icon=\"none\"/></pattern>"
				+ "</patterns>";
		Files.writeString(file.toPath(), xml, StandardCharsets.UTF_8);
		List patterns = StylePattern.loadPatterns(file);
		StylePattern self = (StylePattern) patterns.get(0);
		assertEquals("Self", self.getName());
		assertEquals(Color.decode("#112233"), self.getNodeColor());
		assertEquals("bubble", self.getNodeStyle());
		assertEquals("Hello", self.getText());
		assertEquals("Dialog", self.getNodeFontFamily());
		assertEquals(Boolean.TRUE, self.getNodeFontBold());
		assertEquals(Integer.valueOf(18), self.getNodeFontSize());
		assertEquals("idea", self.getNodeIcon().getName());
		assertEquals("linear", self.getEdgeStyle());
		assertEquals(Integer.valueOf(EdgeAdapter.WIDTH_THIN), self.getEdgeWidth());
		assertEquals(self, self.getChildrenStylePattern());
		assertEquals("Missing", ((StylePattern) patterns.get(1)).getName());
		assertNull(((StylePattern) patterns.get(1)).getChildrenStylePattern());
		assertEquals(Integer.valueOf(3), ((StylePattern) patterns.get(2)).getEdgeWidth());
		assertNull(((StylePattern) patterns.get(3)).getNodeIcon());
		assertNotNull(self.getEdgeColor());
	}

	@Test
	void constructingFromANodeCopiesColorFontAndEdge() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode child = opened.child("Child");
		opened.mode.setNodeColor(child, Color.BLUE);
		opened.mode.setBold(child, true);
		opened.mode.setEdgeStyle(child, EdgeAdapter.EDGESTYLE_LINEAR);
		StylePattern pattern = new StylePattern(child);
		assertEquals(Color.BLUE, pattern.getNodeColor());
		assertEquals(Boolean.TRUE, pattern.getNodeFontBold());
		assertEquals(EdgeAdapter.EDGESTYLE_LINEAR, pattern.getEdgeStyle());
		assertTrue(pattern.getAppliesToNode());
		assertTrue(pattern.getAppliesToEdge());
	}
}
