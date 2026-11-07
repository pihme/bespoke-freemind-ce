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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringWriter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.main.StoredNodeText;
import freemind.modes.mindmapmode.MindMapNodeModel;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("StoredNodeTextBehaviourTest")
class StoredNodeTextBehaviourTest {

	@Test
	void plainTextStaysPlainAndHtmlStaysHtml() throws Exception {
		assertFalse(StoredNodeText.isHtml("SELECT * FROM notes"));
		assertEquals("ab", StoredNodeText.clean("a\0b"));
		String html = "<html><body><p>caf&#252;</p></body></html>";
		assertTrue(StoredNodeText.isHtml(html));
		String forFile = StoredNodeText.htmlForFile(html);
		assertTrue(forFile.contains("<p>"));
		assertTrue(forFile.contains("caf\u00fc"));
		assertFalse(forFile.contains("&#252;"));

		Opened opened = MapFixture.open();
		MindMapNodeModel node = opened.root;
		node.setXmlText(html);
		StringWriter writer = new StringWriter();
		node.save(writer, opened.mode.getMap().getLinkRegistry(), true, false);
		String saved = writer.toString();
		assertTrue(saved.contains("richcontent"));
		assertTrue(saved.contains("<p>"));
		assertFalse(saved.startsWith("# "));
	}
}
