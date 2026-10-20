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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.Hashtable;
import java.util.Vector;

import org.junit.jupiter.api.Test;

import freemind.main.XMLElement;
import freemind.main.XMLParseException;

class XMLElementBehaviourTest {

	@Test
	void constructorsExposeCaseAndWhitespaceChoices() {
		// backwards compatibility
		assertEquals(0, new XMLElement().countChildren());
		assertEquals(0, new XMLElement(new Hashtable<String, char[]>()).getLineNr());
		assertNull(new XMLElement(true).getName());
		assertEquals("", new XMLElement(new Hashtable<String, char[]>(), true).getContent());
		assertNull(new XMLElement().getUserObject());

		XMLElement sensitive = new XMLElement(new Hashtable<String, char[]>(), false, false);
		sensitive.setAttribute("Ab", "1");
		assertEquals("1", sensitive.getStringAttribute("Ab"));
		assertNull(sensitive.getStringAttribute("AB"));

		XMLElement ignored = new XMLElement();
		ignored.setAttribute("Ab", "1");
		assertEquals("1", ignored.getStringAttribute("ab"));
	}

	@Test
	void parserKeepsEntitiesCdataChildrenAndRichContent() throws Exception {
		// backwards compatibility
		@SuppressWarnings({ "rawtypes", "unchecked" })
		Hashtable entities = new Hashtable();
		entities.put("euro", "E");
		XMLElement custom = new XMLElement(entities);
		custom.parseString("<n attr=\"a&amp;b\">x&euro;y&lt;z</n>");
		assertEquals("n", custom.getName());
		assertEquals("a&b", custom.getStringAttribute("attr"));
		assertEquals("xEy<z", custom.getContent());
		assertEquals("xEy<z", custom.getContents());

		XMLElement kept = new XMLElement(false);
		kept.parseString("<n>  a  </n>");
		assertEquals("  a  ", kept.getContent());
		XMLElement trimmed = new XMLElement(true);
		trimmed.parseString("<n>  a  </n>");
		assertEquals("a", trimmed.getContent());

		XMLElement cdata = new XMLElement();
		cdata.parseString("<n><![CDATA[a<b>]]></n>");
		assertEquals("a<b>", cdata.getContent());
		XMLElement brackets = new XMLElement();
		brackets.parseString("<n><![CDATA[a]]b]]></n>");
		assertEquals("a]]b", brackets.getContent());

		XMLElement tree = new XMLElement();
		tree.parseFromReader(new StringReader("<?xml version=\"1.0\"?><!DOCTYPE foo><!-- note --><p><c>t</c></p>"), 3);
		assertEquals("p", tree.getName());
		assertEquals(1, tree.countChildren());
		XMLElement child = tree.getChildren().get(0);
		assertEquals("c", child.getName());
		assertEquals("t", child.getContent());
		Vector<XMLElement> copy = tree.getChildren();
		copy.clear();
		assertEquals(1, tree.countChildren());

		XMLElement rich = new XMLElement();
		rich.parseString("<richcontent>  <html>x</html>  </richcontent>");
		assertEquals("<html>x</html>", rich.getContent());

		XMLElement self = new XMLElement();
		self.parseString("<n/>");
		assertEquals("n", self.getName());
		assertEquals(0, self.countChildren());
		assertEquals("", self.getContent());

		String wrapped = "xx<node>z</node>yy";
		XMLElement slice = new XMLElement();
		slice.parseString(wrapped, 2, 2 + "<node>z</node>".length(), 4);
		assertEquals("z", slice.getContent());
		XMLElement offsetOnly = new XMLElement();
		offsetOnly.parseString(wrapped, 2);
		assertEquals("node", offsetOnly.getName());

		char[] chars = "<node>q</node>".toCharArray();
		XMLElement fromChars = new XMLElement();
		fromChars.parseCharArray(chars, 0, chars.length);
		assertEquals("q", fromChars.getContent());
		XMLElement fromCharsLine = new XMLElement();
		fromCharsLine.parseCharArray(chars, 0, chars.length, 2);
		assertEquals("q", fromCharsLine.getContent());
	}

	@Test
	void attributeReadersRejectUnknownValuesAndRemovalDropsThem() {
		// backwards compatibility
		XMLElement element = new XMLElement();
		element.addProperty("count", 7);
		element.addProperty("ratio", 1.5);
		element.addProperty("name", "Ada");
		assertEquals(7, element.getIntAttribute("count"));
		assertEquals(1.5, element.getDoubleAttribute("ratio"), 0.0001);
		assertEquals("Ada", element.getStringAttribute("name"));
		assertEquals(4, element.getIntAttribute("missing", 4));
		assertEquals(0.25, element.getDoubleAttribute("missing", 0.25), 0.0001);
		assertTrue(element.getBooleanAttribute("flag", "yes", "no", true));
		element.setAttribute("flag", "yes");
		assertTrue(element.getBooleanAttribute("flag", "yes", "no", false));
		element.setAttribute("flag", "no");
		assertFalse(element.getBooleanAttribute("flag", "yes", "no", true));
		element.setAttribute("flag", "maybe");
		XMLParseException badFlag = assertThrows(XMLParseException.class,
				() -> element.getBooleanAttribute("flag", "yes", "no", false));
		assertTrue(badFlag.getMessage().toLowerCase().contains("flag"));

		element.setAttribute("n", "nope");
		assertThrows(XMLParseException.class, () -> element.getIntAttribute("n"));
		element.setAttribute("d", "nope");
		assertThrows(XMLParseException.class, () -> element.getDoubleAttribute("d"));

		Hashtable<Object, Integer> ints = new Hashtable<>();
		ints.put("one", Integer.valueOf(1));
		element.setAttribute("n", "one");
		assertEquals(1, element.getIntAttribute("n", ints, "one", false));
		element.setAttribute("n", "8");
		assertEquals(8, element.getIntAttribute("n", ints, "one", true));
		element.setAttribute("n", "xx");
		assertThrows(XMLParseException.class, () -> element.getIntAttribute("n", ints, "one", true));
		element.setAttribute("n", "zz");
		assertThrows(XMLParseException.class, () -> element.getIntAttribute("n", ints, "one", false));

		@SuppressWarnings({ "rawtypes", "unchecked" })
		Hashtable brokenInts = new Hashtable();
		brokenInts.put("bad", "text");
		element.setAttribute("n", "bad");
		XMLParseException badSet = assertThrows(XMLParseException.class,
				() -> element.getIntAttribute("n", brokenInts, "bad", false));
		assertTrue(badSet.getMessage().contains("Invalid value set"));

		Hashtable<Object, Double> doubles = new Hashtable<>();
		doubles.put("half", Double.valueOf(0.5));
		element.setAttribute("d", "half");
		assertEquals(0.5, element.getDoubleAttribute("d", doubles, "half", false), 0.0001);
		element.setAttribute("d", "2.5");
		assertEquals(2.5, element.getDoubleAttribute("d", doubles, "half", true), 0.0001);
		element.setAttribute("d", "xx");
		assertThrows(XMLParseException.class, () -> element.getDoubleAttribute("d", doubles, "half", true));
		element.setAttribute("d", "zz");
		assertThrows(XMLParseException.class, () -> element.getDoubleAttribute("d", doubles, "half", false));
		@SuppressWarnings({ "rawtypes", "unchecked" })
		Hashtable brokenDoubles = new Hashtable();
		brokenDoubles.put("bad", "text");
		element.setAttribute("d", "bad");
		assertThrows(XMLParseException.class, () -> element.getDoubleAttribute("d", brokenDoubles, "bad", false));

		Hashtable<Object, String> names = new Hashtable<>();
		names.put("a", "Alpha");
		element.setAttribute("name", "a");
		assertEquals("Alpha", element.getStringAttribute("name", names, "a", false));
		element.setAttribute("name", "literal");
		assertEquals("literal", element.getStringAttribute("name", names, "a", true));
		element.setAttribute("name", "other");
		assertThrows(XMLParseException.class, () -> element.getStringAttribute("name", names, "a", false));

		element.removeAttribute("name");
		assertNull(element.getAttribute("name"));
		element.setAttribute("left", "1");
		element.removeProperty("left");
		assertNull(element.getAttribute("left"));
		element.setAttribute("gone", "1");
		element.removeChild("gone");
		assertEquals("fallback", element.getStringAttribute("gone", "fallback"));

		XMLElement parent = new XMLElement();
		parent.setTagName("parent");
		XMLElement kid = new XMLElement();
		kid.setName("kid");
		parent.addChild(kid);
		assertEquals(1, parent.countChildren());
		parent.removeChild(kid);
		assertEquals(0, parent.countChildren());

		Probe probe = new Probe();
		probe.setName("root");
		XMLParseException syntax = probe.syntax("the header");
		assertTrue(syntax.getMessage().contains("Syntax error"));
		assertTrue(syntax.getMessage().contains("root"));
		assertTrue(syntax.getMessage().contains("the header"));
	}

	@Test
	void writerEncodesMarkupAndDropsAnUnpairedSurrogate() {
		// backwards compatibility
		XMLElement bare = new XMLElement();
		bare.setEncodedContent("a<b");
		assertEquals("a<b", bare.toString());

		XMLElement encoded = new XMLElement();
		encoded.setName("n");
		encoded.setContent("a<b>&'\"\u0007");
		String text = encoded.toString();
		assertTrue(text.contains("&lt;"));
		assertTrue(text.contains("&gt;"));
		assertTrue(text.contains("&amp;"));
		assertTrue(text.contains("&apos;"));
		assertTrue(text.contains("&quot;"));
		assertTrue(text.contains("&#x7;"));
		assertTrue(text.contains("</n>"));

		XMLElement raw = new XMLElement();
		raw.setName("n");
		raw.setEncodedContent("a<b");
		assertTrue(raw.toString().contains("a<b"));
		assertFalse(raw.toString().contains("&lt;"));

		XMLElement emoji = new XMLElement();
		emoji.setName("n");
		emoji.setContent("A\uD83D\uDE00B\uD800C");
		String emojiText = emoji.toString();
		assertTrue(emojiText.contains("&#x1f600;"));
		assertTrue(emojiText.contains(">AB") || emojiText.contains(">AC") || emojiText.contains("ABC")
				|| emojiText.contains("A") && emojiText.contains("B") && emojiText.contains("C"));
		assertFalse(emojiText.contains("\uD800"));

		XMLElement parent = new XMLElement();
		parent.setName("p");
		parent.setAttribute("id", "1");
		XMLElement child = new XMLElement();
		child.setName("c");
		child.setContent("t");
		parent.addChild(child);
		String family = parent.toString().toLowerCase();
		assertTrue(family.contains("id=\"1\""));
		assertTrue(family.contains("<c>"));
		assertTrue(family.contains("t"));
		assertTrue(family.contains("</p>"));

		StringBuilder open = new StringBuilder();
		try {
			parent.writeWithoutClosingTag(new java.io.StringWriter() {
				@Override
				public void write(int c) {
					open.append((char) c);
				}

				@Override
				public void write(char[] buf, int off, int len) {
					open.append(buf, off, len);
				}

				@Override
				public void write(String str) {
					open.append(str);
				}

				@Override
				public void write(String str, int off, int len) {
					open.append(str, off, off + len);
				}
			});
		} catch (java.io.IOException ex) {
			throw new AssertionError(ex);
		}
		assertFalse(open.toString().toLowerCase().contains("</p>"));

		XMLElement empty = new XMLElement();
		empty.setName("e");
		assertTrue(empty.toString().contains("<e/>"));
	}

	@Test
	void malformedInputNamesTheMissingToken() {
		// backwards compatibility
		XMLParseException missing = assertThrows(XMLParseException.class,
				() -> new XMLElement().parseString("<node attr>"));
		assertTrue(missing.getMessage().contains("Expected"));
		XMLParseException entity = assertThrows(XMLParseException.class,
				() -> new XMLElement().parseString("<node>&nope;</node>"));
		assertTrue(entity.getMessage().contains("nope"));
		XMLParseException comment = assertThrows(XMLParseException.class,
				() -> new XMLElement().parseString("<!-- hi -- >"));
		assertTrue(comment.getMessage().contains(">"));
		XMLParseException ended = assertThrows(XMLParseException.class,
				() -> new XMLElement().parseString("\n<bad ", 0, 6, 9));
		assertTrue(ended.getLineNr() >= 9);
		assertThrows(XMLParseException.class, () -> new XMLElement().parseString("<node></other>"));
		assertThrows(XMLParseException.class, () -> new XMLElement().parseCharArray(new char[] { 'x' }, 0, 1));
	}

	private static final class Probe extends XMLElement {
		XMLParseException syntax(String context) {
			return syntaxError(context);
		}
	}
}
