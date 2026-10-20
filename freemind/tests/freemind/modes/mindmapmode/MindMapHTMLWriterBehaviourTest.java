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

package freemind.modes.mindmapmode;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import freemind.extensions.PermanentNodeHookAdapter;
import freemind.main.Resources;
import freemind.modes.MindIcon;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** HTML export follows the folding preference and escapes node text. */
class MindMapHTMLWriterBehaviourTest {

	@Test
	void foldingModesIconsLinksAndHooksAreWritten() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open();
		Properties properties = Resources.getInstance().getProperties();
		String folding = properties.getProperty("html_export_folding");
		String icons = properties.getProperty("export_icons_in_html");
		String fontSize = properties.getProperty("defaultfontsize");
		try {
			properties.setProperty("defaultfontsize", "12");
			properties.setProperty("export_icons_in_html", "true");
			MindMapNodeModel root = new MindMapNodeModel("Root & <tag>", (MindMapMapModel) opened.mode.getMap());
			MindMapNodeModel child = new MindMapNodeModel("äöüÄÖÜß \u0100", root.getMap());
			child.setLink("notes.mm");
			child.addIcon(MindIcon.factory("button_ok"), 0);
			child.setColor(Color.BLUE);
			child.setBold(true);
			child.setItalic(true);
			child.setFontSize(24);
			MindMapNodeModel blank = new MindMapNodeModel("   ", root.getMap());
			MindMapNodeModel rich = new MindMapNodeModel("<html><body><b>Rich</b></body></html>", root.getMap());
			MindMapNodeModel plainSize = new MindMapNodeModel("Sized", root.getMap());
			plainSize.setFontSize(12);
			MindMapNodeModel zero = new MindMapNodeModel("Zero", root.getMap());
			zero.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12).deriveFont(0f));
			MindMapNodeModel longChild = new MindMapNodeModel("L".repeat(101), root.getMap());
			MindMapNodeModel folded = new MindMapNodeModel("Folded", root.getMap());
			folded.setFolded(true);
			MindMapNodeModel inside = new MindMapNodeModel("Inside", root.getMap());
			String spaced = "two  spaces\nnext";
			MindMapNodeModel spaces = new MindMapNodeModel(spaced, root.getMap());
			root.insert(child, 0);
			root.insert(blank, 1);
			root.insert(rich, 2);
			root.insert(plainSize, 3);
			root.insert(zero, 4);
			root.insert(longChild, 5);
			root.insert(folded, 6);
			folded.insert(inside, 0);
			root.insert(spaces, 7);
			root.invokeHook(new PermanentNodeHookAdapter() {
				@Override
				public void saveHtml(Writer fileout) throws IOException {
					fileout.write("HOOKMARK");
				}
			});

			properties.setProperty("html_export_folding", "html_export_fold_all");
			String foldedAll = write(root);
			assertTrue(foldedAll.contains("HOOKMARK"));
			assertTrue(foldedAll.contains("notes.mm.html"));
			assertTrue(foldedAll.contains("&auml;"));
			assertTrue(foldedAll.contains("&ouml;"));
			assertTrue(foldedAll.contains("&uuml;"));
			assertTrue(foldedAll.contains("&Auml;"));
			assertTrue(foldedAll.contains("&Ouml;"));
			assertTrue(foldedAll.contains("&Uuml;"));
			assertTrue(foldedAll.contains("&szlig;"));
			assertTrue(foldedAll.contains("&#256;"));
			assertTrue(foldedAll.contains("&amp;"));
			assertTrue(foldedAll.contains("&lt;"));
			assertTrue(foldedAll.contains("&gt;"));
			assertTrue(foldedAll.contains("&nbsp;"));
			assertTrue(foldedAll.contains("<br>"));
			assertTrue(foldedAll.contains("font-size: 200%"));
			assertTrue(foldedAll.contains("font-style: italic"));
			assertTrue(foldedAll.contains("font-weight: bold"));
			assertTrue(foldedAll.contains("color:"));
			assertTrue(foldedAll.contains("Rich"));
			assertTrue(foldedAll.contains("Inside"));
			assertTrue(foldedAll.contains("img src="));
			assertTrue(foldedAll.contains("fold"));

			properties.setProperty("html_export_folding", "html_export_no_folding");
			String open = write(root);
			assertTrue(open.contains("Inside"));
			assertFalse(open.contains("show_folder"));

			properties.setProperty("html_export_folding", "html_export_based_on_headings");
			String headings = write(root);
			assertTrue(headings.contains("<h1>"));
			assertTrue(headings.contains("<h2>"));

			properties.setProperty("html_export_folding", "html_export_fold_currently_folded");
			String current = write(root);
			assertTrue(current.contains("Inside"));
			assertTrue(current.contains("fold"));

			StringWriter listOut = new StringWriter();
			new MindMapHTMLWriter(listOut).saveHTML(Collections.singletonList(spaces));
			assertTrue(listOut.toString().contains("&nbsp;"));
			assertTrue(listOut.toString().contains("<br>"));
			assertFalse(listOut.toString().contains("font-size: 100%"));
		} finally {
			restore(properties, "html_export_folding", folding);
			restore(properties, "export_icons_in_html", icons);
			restore(properties, "defaultfontsize", fontSize);
		}
	}

	private static String write(MindMapNodeModel root) throws IOException {
		StringWriter out = new StringWriter();
		new MindMapHTMLWriter(out).saveHTML(root);
		return out.toString();
	}

	private static void restore(Properties properties, String key, String value) {
		if (value == null) {
			properties.remove(key);
		} else {
			properties.setProperty(key, value);
		}
	}
}
