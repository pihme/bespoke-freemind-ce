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
import freemind.modes.attributes.Attribute;
import freemind.modes.mindmapmode.MindMapCloudModel;
import freemind.modes.mindmapmode.MindMapNodeModel;
import plugins.contextgraph.CleanXmlExport;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("CleanXmlExportBehaviourTest")
class CleanXmlExportBehaviourTest {

	private static final String QUOTED = "Alpha <tag> & \"quote\"";
	private static final String HTML = "<html><b>Beta</b> &amp; &quot;q&quot;</html>";
	private static final String HTML_UPPER = "<HTML>Upper &amp; Z</HTML>";
	private static final String NOTE = "<p>See &#65; &#x42; &#xzz; &#1234567890; &#12 &amp; &lt;tag&gt;</p>";
	private static final String LINK = "https://example.test/a?x=1&y=\"2\"";

	@Test
	void aCancelledChoiceLeavesTheFileUntouched() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open(QUOTED);
		File dir = Files.createTempDirectory("contextgraph").toFile();
		try {
			File out = new File(dir, "out.xml");
			Files.writeString(out.toPath(), "untouched");
			Fixed hook = prepare(opened, null);
			hook.startupMapHook();
			assertEquals("xml", hook.type);
			assertEquals("export", hook.description);
			assertEquals("untouched", Files.readString(out.toPath()));
		} finally {
			delete(dir);
		}
	}

	@Test
	void aBlankRootBecomesMindmapAndMarkupIsEscaped() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open(QUOTED, " \t ", HTML, HTML_UPPER);
		opened.root.setText(" \t ");
		String text = written(opened);
		has(text, "<mindmap>");
		has(text, "</mindmap>");
		has(text, "Alpha &lt;tag&gt; &amp; \"quote\"");
		has(text, "Beta &amp; \"q\"");
		has(text, "Upper &amp; Z");
		lacks(text, "<html>");
		lacks(text, "<HTML>");
		lacks(text, "&quot;");
		lacks(text, "Alpha <tag>");
		lacks(text, "\t");
	}

	@Test
	void aParentTagDropsBracketsAndKeepsNotesIconsAndReferences() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open(QUOTED);
		MindMapNode rich = add(opened.root, "A&B<C>");
		add(rich, " \t ");
		add(rich, "Gamma");
		NodeAdapter adapter = (NodeAdapter) rich;
		adapter.addIcon(MindIcon.factory("idea"), 0);
		adapter.addIcon(MindIcon.factory("help"), 1);
		adapter.setLink(LINK);
		adapter.setNoteText(NOTE);
		adapter.addAttribute(new Attribute("Rank", "yes"));
		adapter.addAttribute(new Attribute("bare", null));
		adapter.setBold(true);
		adapter.setItalic(true);
		adapter.setUnderlined(true);
		adapter.setStrikethrough(true);
		adapter.setCloud(new MindMapCloudModel(rich, adapter.getMapFeedback()));
		opened.mode.addLink(rich, opened.child(QUOTED));
		String text = written(opened);
		has(text, "<A&amp;BC>");
		has(text, "</A&amp;BC>");
		has(text, "[idea, help]");
		has(text, "Rank: yes");
		has(text, "bare: ");
		has(text, "* See A B &amp;#xzz; &amp;#1234567890; &amp;#12 &amp; &lt;tag&gt;");
		has(text, "=> https://example.test/a?x=1&amp;y=\"2\"");
		has(text, "~> Alpha &lt;tag&gt; &amp; \"quote\"");
		has(text, "(bold, italic, underlined, strikethrough)");
		has(text, "{grouped}");
		has(text, "Gamma");
		lacks(text, "<html>");
		lacks(text, "\t");
		lacks(text, "<C>");
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
			File out = new File(dir, "map.xml");
			Fixed hook = prepare(opened, out);
			hook.startupMapHook();
			assertEquals("xml", hook.type);
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

	private static final class Fixed extends CleanXmlExport {
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
