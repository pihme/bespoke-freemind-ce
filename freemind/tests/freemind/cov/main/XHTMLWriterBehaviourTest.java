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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.Option;

import org.junit.jupiter.api.Test;

import freemind.main.XHTMLWriter;

class XHTMLWriterBehaviourTest {

	@Test
	void filterClosesEmptyTagsAndEscapesAttributeValues() throws Exception {
		// backwards compatibility
		StringWriter target = new StringWriter();
		XHTMLWriter.XHTMLFilterWriter filter = new XHTMLWriter.XHTMLFilterWriter(target);
		String raw = "<br><hr><input><meta><link><area><base><basefont><frame><iframe><col>"
				+ "<img src=\"a&b<c>\">" + "<div>x</div>";
		filter.write(raw, 0, raw.length());
		String xhtml = target.toString();
		assertTrue(xhtml.contains("<br />"));
		assertTrue(xhtml.contains("<hr />"));
		assertTrue(xhtml.contains("<img src=\"a&amp;b&lt;c&gt;\" />"));
		assertTrue(xhtml.contains("<div>x</div>"));
		assertTrue(xhtml.contains("<col />"));
	}

	@Test
	void optionWriterClosesTheOptionAndMainConvertsAFile() throws Exception {
		// backwards compatibility
		HTMLEditorKit kit = new HTMLEditorKit();
		HTMLDocument document = (HTMLDocument) kit.createDefaultDocument();
		StringWriter written = new StringWriter();
		OptionProbe probe = new OptionProbe(written, document);
		Option option = new Option(new SimpleAttributeSet());
		option.setLabel("Choice");
		probe.option(option);
		assertTrue(written.toString().contains("Choice"));
		assertTrue(written.toString().contains("</option>"));

		File source = File.createTempFile("coverage", ".html");
		File target = File.createTempFile("coverage", ".xhtml");
		try {
			Files.writeString(source.toPath(), "<html><body><p>Hi<br>there</p></body></html>",
					StandardCharsets.UTF_8);
			XHTMLWriter.main(new String[] { source.getAbsolutePath(), target.getAbsolutePath() });
			String converted = Files.readString(target.toPath());
			assertTrue(converted.contains("Hi"), converted);
			long length = target.length();
			XHTMLWriter.main(new String[] { source.getAbsolutePath() + ".missing", target.getAbsolutePath() });
			assertEquals(length, target.length());
		} finally {
			source.delete();
			target.delete();
		}
	}

	private static final class OptionProbe extends XHTMLWriter {
		OptionProbe(java.io.Writer writer, HTMLDocument document) {
			super(writer, document);
		}

		void option(Option option) throws IOException {
			writeOption(option);
		}
	}
}
