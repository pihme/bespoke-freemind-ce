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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringWriter;

import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.junit.jupiter.api.Test;

import freemind.main.FixedHTMLWriter;

class FixedHTMLWriterBehaviourTest {

	@Test
	void cssAttributesBecomeAStyleAndTagsAreSkipped() throws Exception {
		// backwards compatibility
		HTMLEditorKit kit = new HTMLEditorKit();
		HTMLDocument document = (HTMLDocument) kit.createDefaultDocument();
		kit.insertHTML(document, 0, "<p>Hello <b>there</b></p>", 0, 0, HTML.Tag.BODY);
		StringWriter written = new StringWriter();
		new FixedHTMLWriter(written, document).write();
		assertTrue(written.toString().contains("Hello"));

		StringWriter attributes = new StringWriter();
		AttrProbe probe = new AttrProbe(attributes, document);
		SimpleAttributeSet set = new SimpleAttributeSet();
		set.addAttribute(CSS.Attribute.COLOR, "red");
		set.addAttribute(CSS.Attribute.FONT_SIZE, "12pt");
		set.addAttribute(HTML.Tag.B, SimpleAttributeSet.EMPTY);
		set.addAttribute(StyleConstants.Bold, Boolean.TRUE);
		set.addAttribute(HTML.Attribute.ENDTAG, "true");
		set.addAttribute(HTML.Attribute.CLASS, "note");
		probe.dump(set);
		String text = attributes.toString();
		assertTrue(text.contains("color"));
		assertTrue(text.contains("red"));
		assertTrue(text.contains("font-size"));
		assertTrue(text.contains("class=\"note\""));
		assertFalse(text.toLowerCase().contains("endtag"));

		int length = attributes.getBuffer().length();
		probe.dump(null);
		assertEqualsLength(length, attributes.getBuffer().length());

		Element leaf = firstLeaf(document.getDefaultRootElement());
		assertTrue(leaf instanceof AttributeSet);
		probe.dump((AttributeSet) leaf);
		assertTrue(attributes.toString().length() >= length);
	}

	private static void assertEqualsLength(int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(expected + " != " + actual);
		}
	}

	private static Element firstLeaf(Element element) {
		if (element.isLeaf() || element.getElementCount() == 0) {
			return element;
		}
		return firstLeaf(element.getElement(0));
	}

	private static final class AttrProbe extends FixedHTMLWriter {
		AttrProbe(java.io.Writer writer, HTMLDocument document) {
			super(writer, document);
		}

		void dump(AttributeSet set) throws IOException {
			writeAttributes(set);
		}
	}
}
