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

import java.util.ArrayList;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.main.HtmlTools;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.MindMapNodeModel;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("HtmlToolsBehaviourTest")
class HtmlToolsBehaviourTest {

	@Test
	void unicodeEntitiesRoundTripAndInvalidCharactersDropOut() {
		// backwards compatibility
		assertEquals("A&#xff;\n\r", HtmlTools.unicodeToHTMLUnicodeEntity("A\u00ff\n\r", true));
		assertEquals("A&#xff;&#xa;&#xd;", HtmlTools.unicodeToHTMLUnicodeEntity("A\u00ff\n\r", false));
		assertEquals("A", HtmlTools.unescapeHTMLUnicodeEntity("&#x41;"));
		assertEquals("A", HtmlTools.unescapeHTMLUnicodeEntity("&#65;"));
		assertEquals("&amp;", HtmlTools.unescapeHTMLUnicodeEntity("&amp;"));
		assertEquals("&#xZZ;", HtmlTools.unescapeHTMLUnicodeEntity("&#xZZ;"));
		assertEquals("", HtmlTools.unescapeHTMLUnicodeEntity("&#xb;"));
		assertEquals("ab", HtmlTools.unescapeHTMLUnicodeEntity("a\u0001b"));
		assertEquals("a&not;", HtmlTools.unescapeHTMLUnicodeEntity("a&not"));

		assertEquals("&#x41;", HtmlTools.removeInvalidXmlCharacters("&#x41;"));
		assertEquals("", HtmlTools.removeInvalidXmlCharacters("&#x1;"));
		assertEquals("&#xZZ;", HtmlTools.removeInvalidXmlCharacters("&#xZZ;"));
		assertEquals("&amp;", HtmlTools.removeInvalidXmlCharacters("&amp;"));
		assertEquals("ab", HtmlTools.removeInvalidXmlCharacters("a\u0001b"));
		assertEquals("a&not;", HtmlTools.removeInvalidXmlCharacters("a&not"));
		assertEquals("&nbsp;a &lt;b&gt;&nbsp;",
				HtmlTools.toXMLEscapedTextExpandingWhitespace(" a <b> "));
	}

	@Test
	void xhtmlConversionAndBodyExtractionKeepVisibleText() {
		// backwards compatibility
		assertNull(HtmlTools.getInstance().toXhtml("plain text"));
		String xhtml = HtmlTools.getInstance().toXhtml("<html><body>Hello</body></html>");
		assertNotNull(xhtml);
		assertTrue(xhtml.contains("Hello"));
		String broken = HtmlTools.getInstance().toXhtml("<html><body><p>Hi</p>");
		assertNotNull(broken);
		assertTrue(broken.contains("Hi"));
		assertTrue(HtmlTools.getInstance().isWellformedXml("<a/>"));
		assertFalse(HtmlTools.getInstance().isWellformedXml("<a>"));
		assertFalse(HtmlTools.getInstance().isWellformedXml(null));
		assertEquals("Hi", HtmlTools.extractHtmlBody("<html><body>Hi</body></html>"));
		assertEquals("Hi", HtmlTools.extractHtmlBody("<html>Hi"));
		assertEquals("Hi", HtmlTools.extractHtmlBody("Hi"));
		assertEquals("x", HtmlTools.extractHtmlBody("<html><body class='a'>x</html>"));
	}

	@Test
	void indexPairsMapReplacedOffsetsBackToTheOriginal() {
		// backwards compatibility
		ArrayList<HtmlTools.IndexPair> pairs = new ArrayList<>();
		pairs.add(new HtmlTools.IndexPair(0, 5, 0, 5, false));
		pairs.add(new HtmlTools.IndexPair(5, 8, 5, 8, true));
		assertEquals(2, HtmlTools.getInstance().getMinimalOriginalPosition(2, pairs));
		assertEquals(3, HtmlTools.getInstance().getMaximalOriginalPosition(3, pairs));
		assertEquals(8, HtmlTools.getInstance().getMaximalOriginalPosition(6, pairs));
		assertThrows(IllegalArgumentException.class,
				() -> HtmlTools.getInstance().getMinimalOriginalPosition(99, pairs));
		assertThrows(IllegalArgumentException.class,
				() -> HtmlTools.getInstance().getMaximalOriginalPosition(-1, pairs));
	}

	@Test
	void htmlListsBecomeChildNodes() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode root = opened.root;
		int before = root.getChildCount();
		HtmlTools.getInstance().insertHtmlIntoNodes(
				"<html><body><p>Lead</p><ul><li>One</li><li>Two <a href=\"http://example.test\">link</a></li></ul>"
						+ "<p style=\"level1\">Down</p><br>tail</body></html>",
				root, new HtmlTools.NodeCreator() {
					@Override
					public MindMapNode createChild(MindMapNode parent) {
						MindMapNodeModel node = new MindMapNodeModel("", parent.getMap());
						parent.insert(node, parent.getChildCount());
						return node;
					}

					@Override
					public void setText(String text, MindMapNode node) {
						node.setText(text);
					}

					@Override
					public void setLink(String link, MindMapNode node) {
						node.setLink(link);
					}
				});
		assertTrue(root.getChildCount() > before);
		String collected = collect(root);
		assertTrue(collected.contains("One"), collected);
		assertTrue(collected.contains("link") || collected.contains("Two"), collected);
	}

	private static String collect(MindMapNode node) {
		StringBuilder text = new StringBuilder();
		text.append(node.getText());
		for (int i = 0; i < node.getChildCount(); i++) {
			text.append('|').append(collect((MindMapNode) node.getChildAt(i)));
		}
		return text.toString();
	}
}
