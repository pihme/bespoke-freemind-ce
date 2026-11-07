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

package tests.freemind.cov.modes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Font;
import java.io.StringWriter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.main.Tools;
import freemind.modes.MapAdapter;
import freemind.modes.MindMapNode;
import freemind.modes.NodeAdapter;
import freemind.modes.mindmapmode.MindMapNodeModel;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** A save writes the stored style token, and underline round-trips on the font. */
@DisplayName("StoredNodeStyleBehaviourTest")
class StoredNodeStyleBehaviourTest {

	@Test
	void aFoldedCombinedNodeSavesCombinedAndDrawsABubble() throws Exception {
		Opened opened = MapFixture.open("Parent");
		MindMapNode parent = opened.child("Parent");
		((MindMapNodeModel) parent).insert(
				new MindMapNodeModel("Grand", opened.mode.getMindMapMapModel()), 0);
		parent.setStyle(MindMapNode.STYLE_COMBINED);
		opened.mode.setFolded(parent, true);
		assertTrue(parent.isFolded());
		assertEquals(MindMapNode.STYLE_BUBBLE, parent.getStyle());
		String saved = save(opened, parent);
		assertTrue(saved.contains("STYLE=\"combined\""));
		assertFalse(saved.contains("STYLE=\"bubble\""));
	}

	@Test
	void anOpenCombinedNodeSavesCombinedAndDrawsAFork() throws Exception {
		Opened opened = MapFixture.open("Child");
		MindMapNode child = opened.child("Child");
		child.setStyle(MindMapNode.STYLE_COMBINED);
		assertEquals(MindMapNode.STYLE_FORK, child.getStyle());
		String saved = save(opened, child);
		assertTrue(saved.contains("STYLE=\"combined\""));
	}

	@Test
	void underlineIsWrittenOnTheFontAndReadBack() throws Exception {
		Opened opened = MapFixture.open("Child");
		MindMapNode child = opened.child("Child");
		NodeAdapter stored = (NodeAdapter) child;
		stored.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		stored.setUnderlined(true);
		String saved = save(opened, child);
		assertTrue(saved.contains("UNDERLINE=\"true\""));

		String xml = "<map version=\"1.0.1\"><node TEXT=\"Root\">"
				+ "<font NAME=\"Dialog\" SIZE=\"12\" UNDERLINE=\"true\"/></node></map>";
		MindMapNode loaded = opened.mode.getMap().loadTree(
				new Tools.StringReaderCreator(xml), MapAdapter.sDontAskInstance);
		assertTrue(loaded.isUnderlined());
	}

	private static String save(Opened opened, MindMapNode node) throws Exception {
		StringWriter writer = new StringWriter();
		((MindMapNodeModel) node).save(writer, opened.mode.getMap().getLinkRegistry(), true, false);
		return writer.toString();
	}
}
