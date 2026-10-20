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

package tests.freemind.cov.mindmapmode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

import freemind.controller.filter.Filter;
import freemind.main.HtmlTools;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.MindMapMapModel;
import freemind.modes.mindmapmode.MindMapNodeModel;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Plain, rich, and filtered nodes export the text a reader can check. */
class MindMapNodeModelBehaviourTest {

	@Test
	void plainAndRichTextKeepLinksStylesAndHiddenGrandchildren() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Keep");
		MindMapMapModel model = (MindMapMapModel) opened.mode.getMap();
		MindMapNodeModel styled = new MindMapNodeModel("Alpha", model);
		styled.setColor(Color.RED);
		styled.setBold(true);
		styled.setItalic(true);
		styled.setStrikethrough(true);
		styled.setFontSize(20);
		styled.setLink("http://example.test/other");
		MindMapNodeModel blank = new MindMapNodeModel("   ", model);
		MindMapNodeModel sized = new MindMapNodeModel("Zero", model);
		sized.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12).deriveFont(0f));
		MindMapNodeModel sameLink = new MindMapNodeModel("http://same.test", model);
		sameLink.setLink("http://same.test");
		MindMapNodeModel marked = new MindMapNodeModel("A\\B{C}\n\u0100", model);
		MindMapNodeModel hidden = new MindMapNodeModel("HIDDENNODE", model);
		MindMapNodeModel grand = new MindMapNodeModel("VISIBLEGRAND", model);
		MindMapNodeModel colorless = new MindMapNodeModel("NoColor", model);
		opened.root.insert(styled, opened.root.getChildCount());
		styled.insert(blank, 0);
		styled.insert(sized, 1);
		styled.insert(sameLink, 2);
		styled.insert(marked, 3);
		styled.insert(hidden, 4);
		hidden.insert(grand, 0);
		hidden.insert(colorless, 1);
		model.setFilter(new HideNamed("HIDDENNODE"));

		StringWriter plain = new StringWriter();
		styled.saveTXT(plain, 0);
		String text = plain.toString();
		assertTrue(text.contains("Alpha"));
		assertTrue(text.contains("<http://example.test/other>"));
		assertTrue(text.contains("o\n"));
		assertTrue(text.contains("<http://same.test>"));
		assertTrue(text.contains("VISIBLEGRAND"));
		assertFalse(text.contains("HIDDENNODE"));
		assertEquals("Alpha", styled.getPlainTextContent());

		HashMap<Color, Integer> colors = new HashMap<>();
		colors.put(Color.RED, 2);
		StringWriter rich = new StringWriter();
		styled.saveRTF(rich, 9, colors);
		String rtf = rich.toString();
		assertTrue(rtf.contains("\\li3150"));
		assertFalse(rtf.contains("outlinelevel"));
		assertTrue(rtf.contains("\\cf2"));
		assertTrue(rtf.contains("\\i "));
		assertTrue(rtf.contains("\\b "));
		assertTrue(rtf.contains("\\strike "));
		assertTrue(rtf.contains("\\fs30"));
		assertTrue(rtf.contains("\\u256?"));
		assertTrue(rtf.contains("\\\\"));
		assertTrue(rtf.contains("\\{"));
		assertTrue(rtf.contains("\\}"));
		assertTrue(rtf.contains(" \\line "));
		assertTrue(rtf.contains("VISIBLEGRAND"));
		assertFalse(rtf.contains("HIDDENNODE"));

		HashSet<Color> collected = new HashSet<>();
		styled.collectColors(collected);
		assertTrue(collected.contains(Color.RED));
		assertFalse(collected.contains(null));

		Writer failing = new Writer() {
			@Override
			public void write(char[] cbuf, int off, int len) throws IOException {
				throw new IOException("stopped");
			}

			@Override
			public void flush() {
			}

			@Override
			public void close() {
			}
		};
		assertThrows(IOException.class, () -> styled.saveRTF(failing, 0, colors));
		assertTrue(styled.isWriteable());
	}

	@Test
	void exportsRejectAForeignListAndADirectory() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Leaf");
		MindMapMapModel model = (MindMapMapModel) opened.mode.getMap();
		List raw = new AbstractList() {
			@Override
			public Object get(int index) {
				throw new IllegalStateException("not a node");
			}

			@Override
			public int size() {
				return 1;
			}
		};
		assertNull(model.getAsHTML(raw));
		assertNull(model.getAsPlainText(raw));
		String rejected = model.getAsRTF(raw);
		assertTrue(rejected == null || rejected.isEmpty());

		File dir = Files.createTempDirectory("mm-txt").toFile();
		assertFalse(model.saveTXT(opened.root, dir));
		File file = new File(dir, "branch.txt");
		assertTrue(model.saveTXT(opened.root, file));
		String saved = Files.readString(file.toPath(), StandardCharsets.UTF_8);
		assertTrue(saved.contains("ROOT"));
		assertTrue(saved.contains("Leaf"));

		List<MindMapNodeModel> nodes = new ArrayList<>();
		nodes.add(opened.root);
		String html = model.getAsHTML(nodes);
		String plain = model.getAsPlainText(nodes);
		String rtf = model.getAsRTF(nodes);
		assertTrue(html.contains("ROOT"));
		assertTrue(plain.contains("Leaf"));
		assertTrue(rtf.contains("\\rtf1"));

		java.io.BufferedWriter bad = new java.io.BufferedWriter(new StringWriter()) {
			@Override
			public void write(String str, int off, int len) throws IOException {
				throw new IOException("stopped");
			}
		};
		assertFalse(model.saveRTF(nodes, bad));
	}

	@Test
	void changeNodeUnescapesHtmlAndToStringUsesTheFileName() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Plain");
		MindMapMapModel model = (MindMapMapModel) opened.mode.getMap();
		assertNull(model.toString());
		File named = new File("named-map.mm");
		model.setFile(named);
		assertEquals("named-map.mm", model.toString());

		MindMapNodeModel html = (MindMapNodeModel) opened.child("Plain");
		html.setUserObject("<html>A</html>");
		model.changeNode(html, "A &amp; B");
		assertEquals(HtmlTools.unescapeHTMLUnicodeEntity("A &amp; B"), html.toString());

		html.setUserObject("plain");
		model.changeNode(html, "next");
		assertEquals("next", html.toString());
	}

	private static final class HideNamed implements Filter {
		private final String hidden;

		HideNamed(String hidden) {
			this.hidden = hidden;
		}

		@Override
		public void applyFilter(freemind.controller.Controller c) {
		}

		@Override
		public boolean isVisible(MindMapNode node) {
			return !hidden.equals(node.getText());
		}

		@Override
		public boolean areMatchedShown() {
			return false;
		}

		@Override
		public boolean areHiddenShown() {
			return false;
		}

		@Override
		public boolean areAncestorsShown() {
			return false;
		}

		@Override
		public boolean areDescendantsShown() {
			return false;
		}

		@Override
		public boolean areEclipsedShown() {
			return false;
		}

		@Override
		public Object getCondition() {
			return null;
		}
	}
}
