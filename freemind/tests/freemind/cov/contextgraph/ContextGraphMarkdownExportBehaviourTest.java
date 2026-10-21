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

package tests.freemind.cov.contextgraph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.HeadlessException;
import java.io.File;
import java.nio.file.Files;
import java.util.Properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.MindIcon;
import freemind.modes.MindMapNode;
import freemind.modes.NodeAdapter;
import freemind.modes.mindmapmode.MindMapNodeModel;
import plugins.contextgraph.ContextGraphMarkdownExport;
import plugins.latex.LatexNodeHook;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("ContextGraphMarkdownExportBehaviourTest")
class ContextGraphMarkdownExportBehaviourTest {

	private static final String QUOTED = "Alpha <tag> & \"quote\"";
	private static final String HTML = "<html><b>Beta</b><B>B</B> &amp; &quot;q&quot;</html>";
	private static final String HTML_UPPER = "<HTML><i>Upper</i><I>I</I> &amp; Z</HTML>";
	private static final String NOTE = "<br>one<br/>two<BR>three<P>Z</P> &#65; &amp;";
	private static final String LINK = "https://example.test/a?x=1&y=\"2\"";

	@Test
	void aCancelledChoiceLeavesTheFileUntouched() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open(QUOTED);
		File dir = Files.createTempDirectory("contextgraph").toFile();
		try {
			File out = new File(dir, "out.md");
			Files.writeString(out.toPath(), "untouched");
			Fixed hook = prepare(opened, null);
			hook.startupMapHook();
			assertEquals("md", hook.type);
			assertEquals("export", hook.description);
			assertEquals("untouched", Files.readString(out.toPath()));
		} finally {
			delete(dir);
		}
	}

	@Test
	void plainTextIsKeptHtmlIsConvertedAndABlankNodeIsSkipped() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open(QUOTED);
		NodeAdapter alpha = (NodeAdapter) opened.child(QUOTED);
		alpha.addIcon(MindIcon.factory("idea"), 0);
		alpha.addIcon(MindIcon.factory("help"), 1);
		alpha.setLink(LINK);
		alpha.setNoteText(NOTE);
		opened.child(QUOTED).invokeHook(new QuietLatex());
		add(opened.root, " \t ");
		add(opened.root, HTML);
		add(opened.root, HTML_UPPER);
		add(opened.root, "keep <b>bold</b><B>B</B><i>x</i><I>I</I>");
		String text = written(opened);
		has(text, "# ROOT");
		has(text, "## \uD83D\uDCA1 \u2753 Alpha <tag> & \"quote\" [link](" + LINK + ")");
		has(text, "> one");
		has(text, "> two");
		has(text, "> three");
		has(text, "> Z &#65; &");
		has(text, "$$E = mc^{2}$$");
		has(text, "## **Beta****B** & \"q\"");
		has(text, "## *Upper**I* & Z");
		has(text, "## keep **bold****B***x**I*");
		lacks(text, "<html>");
		lacks(text, "<HTML>");
		lacks(text, "<b>");
		lacks(text, "&quot;");
		lacks(text, "&amp;");
		lacks(text, "\t");
	}

	@Test
	void anEmptyNodeIsSkippedAndADeepNodeIsABullet() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open();
		MindMapNode blank = add(opened.root, " \t ");
		add(blank, "Kept");
		MindMapNode deep = opened.root;
		for (int i = 0; i < 7; i++) {
			deep = add(deep, "Depth" + i);
		}
		String text = written(opened);
		has(text, "### Kept");
		has(text, "###### Depth4");
		has(text, "\n- Depth5");
		has(text, "\n  - Depth6");
		lacks(text, "\t");
		lacks(text, "# Depth5");
		lacks(text, "# Depth6");
	}

	@Test
	void aDirectoryTargetDoesNotWriteTheMap() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open(QUOTED);
		File dir = Files.createTempDirectory("contextgraph-bad").toFile();
		try {
			Fixed hook = prepare(opened, dir);
			assertThrows(HeadlessException.class, hook::startupMapHook);
			assertEquals(0, dir.list().length);
		} finally {
			delete(dir);
		}
	}

	private static String written(Opened opened) throws Exception {
		File dir = Files.createTempDirectory("contextgraph").toFile();
		try {
			File out = new File(dir, "map.md");
			Fixed hook = prepare(opened, out);
			hook.startupMapHook();
			assertEquals("md", hook.type);
			return Files.readString(out.toPath());
		} finally {
			delete(dir);
		}
	}

	private static Fixed prepare(Opened opened, File chosen) {
		Fixed hook = new Fixed(chosen);
		hook.setController(opened.mode);
		Properties properties = new Properties();
		properties.setProperty("file_description", "export");
		properties.setProperty("load_file", "true");
		properties.setProperty("export_error", "export failed");
		hook.setProperties(properties);
		return hook;
	}

	private static MindMapNode add(MindMapNode parent, String text) {
		MindMapNodeModel node = new MindMapNodeModel(text, parent.getMap());
		parent.insert(node, parent.getChildCount());
		return node;
	}

	private static void has(String text, String snippet) {
		assertTrue(text.contains(snippet), snippet + "\n---\n" + text);
	}

	private static void lacks(String text, String snippet) {
		assertFalse(text.contains(snippet), snippet + "\n---\n" + text);
	}

	private static void delete(File dir) {
		File[] files = dir.listFiles();
		if (files != null) {
			for (File file : files) {
				delete(file);
			}
		}
		dir.delete();
	}

	private static final class QuietLatex extends LatexNodeHook {
		@Override
		public void invoke(MindMapNode node) {
		}
	}

	private static final class Fixed extends ContextGraphMarkdownExport {
		private final File chosen;
		private String type;
		private String description;

		private Fixed(File chosen) {
			this.chosen = chosen;
		}

		@Override
		protected File chooseFile(String type, String description, String nameExtension) {
			this.type = type;
			this.description = description;
			return chosen;
		}
	}
}
