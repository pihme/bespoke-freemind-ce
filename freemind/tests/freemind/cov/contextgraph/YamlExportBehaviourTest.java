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

import java.awt.Color;
import java.awt.HeadlessException;
import java.io.File;
import java.nio.file.Files;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import freemind.modes.EdgeAdapter;
import freemind.modes.MindIcon;
import freemind.modes.MindMapNode;
import freemind.modes.NodeAdapter;
import freemind.modes.attributes.Attribute;
import freemind.modes.mindmapmode.MindMapCloudModel;
import freemind.modes.mindmapmode.MindMapNodeModel;
import plugins.contextgraph.YamlExport;
import plugins.latex.LatexNodeHook;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

class YamlExportBehaviourTest {

	private static final String QUOTED = "Alpha <tag> & \"quote\"";
	private static final String HTML = "<html><b>Beta</b> &amp; &quot;q&quot;</html>";
	private static final String HTML_UPPER = "<HTML>Upper &amp; Z</HTML>";
	private static final String NOTE = "<p>See &#65; &amp; &lt;tag&gt;</p>";
	private static final String LINK = "https://example.test/a?x=1&y=\"2\"";

	@Test
	void aCancelledChoiceLeavesTheFileUntouched() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open(QUOTED);
		File dir = Files.createTempDirectory("contextgraph").toFile();
		try {
			File out = new File(dir, "out.yaml");
			Files.writeString(out.toPath(), "untouched");
			Fixed hook = prepare(opened, null);
			hook.startupMapHook();
			assertEquals("yaml", hook.type);
			assertEquals("export", hook.description);
			assertEquals("untouched", Files.readString(out.toPath()));
		} finally {
			delete(dir);
		}
	}

	@Test
	void quotesColonsAndBooleansAreQuotedAndHtmlIsStripped() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open(QUOTED);
		MindMapNode noted = add(opened.root, "Noted");
		add(noted, "Gamma");
		add(opened.root, " \t ");
		add(opened.root, HTML);
		add(opened.root, HTML_UPPER);
		add(opened.root, "true");
		add(opened.root, "yes");
		add(opened.root, "1.5");
		add(opened.root, "on");
		add(opened.root, "#hash");
		add(opened.root, "{brace");
		add(opened.root, "a\nb\rc\"\t\\");
		NodeAdapter adapter = (NodeAdapter) noted;
		adapter.addIcon(MindIcon.factory("idea"), 0);
		adapter.addIcon(MindIcon.factory("help"), 1);
		adapter.setLink(LINK);
		adapter.setNoteText(NOTE);
		adapter.addAttribute(new Attribute("Rank", "yes"));
		adapter.addAttribute(new Attribute("", "x"));
		adapter.addAttribute(new Attribute("a:b", "plain"));
		adapter.setBold(true);
		adapter.setItalic(true);
		adapter.setUnderlined(true);
		adapter.setStrikethrough(true);
		adapter.setFolded(true);
		adapter.setStyle("bubble");
		adapter.setColor(Color.BLUE);
		adapter.setBackgroundColor(Color.GREEN);
		((EdgeAdapter) adapter.getEdge()).setStyle(EdgeAdapter.EDGESTYLE_LINEAR);
		((EdgeAdapter) adapter.getEdge()).setColor(Color.RED);
		MindMapCloudModel cloud = new MindMapCloudModel(noted, adapter.getMapFeedback());
		cloud.setColor(Color.RED);
		adapter.setCloud(cloud);
		noted.invokeHook(new QuietLatex());
		String text = written(opened);
		has(text, "version: \"1.0\"");
		has(text, "generator: \"FreeMind CE\"");
		has(text, "text: ROOT");
		has(text, "text: \"Alpha <tag> & \\\"quote\\\"\"");
		has(text, "text: \"Beta & \\\"q\\\"\"");
		has(text, "richContent: <html><b>Beta</b> &amp; &quot;q&quot;</html>");
		has(text, "text: Upper & Z");
		has(text, "richContent: <HTML>Upper &amp; Z</HTML>");
		has(text, "text: \"\"");
		has(text, "text: \"true\"");
		has(text, "text: \"yes\"");
		has(text, "text: 1.5");
		has(text, "text: on");
		has(text, "text: \"#hash\"");
		has(text, "text: \"{brace\"");
		has(text, "text: \"a\\nb\\rc\\\"\\t\\\\\"");
		has(text, "text: Noted");
		has(text, "text: Gamma");
		has(text, "folded: true");
		has(text, "bold: true");
		has(text, "italic: true");
		has(text, "style: bubble");
		has(text, "color: \"#0000ff\"");
		has(text, "backgroundColor: \"#00ff00\"");
		has(text, "edge:");
		has(text, "color: \"#ff0000\"");
		has(text, "style: linear");
		has(text, "cloud:");
		has(text, "icons:");
		has(text, "- idea");
		has(text, "- help");
		has(text, "link: \"https://example.test/a?x=1&y=\\\"2\\\"\"");
		has(text, "note: <p>See &#65; &amp; &lt;tag&gt;</p>");
		has(text, "Rank: \"yes\"");
		has(text, "\"\": x");
		has(text, "\"a:b\": plain");
		has(text, "latex: E = mc^{2}");
		has(text, "id: ");
		lacks(text, "text: \"1.5\"");
		lacks(text, "text: \"on\"");
		lacks(text, "text: \"Gamma\"");
		lacks(text, "text: \"Noted\"");
		lacks(text, "underlined");
		lacks(text, "strikethrough");
		lacks(text, "\t");
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
			File out = new File(dir, "map.yaml");
			Fixed hook = prepare(opened, out);
			hook.startupMapHook();
			assertEquals("yaml", hook.type);
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

	private static final class Fixed extends YamlExport {
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
