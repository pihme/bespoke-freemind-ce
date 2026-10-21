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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.util.HashMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.main.Tools;
import freemind.main.Tools.StringReaderCreator;
import freemind.modes.MapAdapter;
import freemind.modes.MindMapArrowLink;
import freemind.modes.MindMapLink;
import freemind.modes.MindMapLinkRegistry;
import freemind.modes.MindMapNode;
import freemind.modes.NodeAdapter;
import freemind.modes.XMLElementAdapter;
import freemind.modes.mindmapmode.EncryptedMindMapNode;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** A map file becomes nodes, edges, clouds, arrows, notes, and an encrypted node. */
@DisplayName("XMLElementAdapterBehaviourTest")
class XMLElementAdapterBehaviourTest {

	@Test
	void aMapFileRestoresNodeEdgeCloudArrowNoteAndHook() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		String xml = "<map version=\"1.1.0\">"
				+ "<node TEXT=\"Parent\" ID=\"cov-parent\" FOLDED=\"true\" POSITION=\"left\""
				+ " COLOR=\"#0000ff\" BACKGROUND_COLOR=\"#00ff00\" STYLE=\"bubble\" LINK=\"#cov-child\""
				+ " VSHIFT=\"4\" VGAP=\"5\" HGAP=\"6\" CREATED=\"1000\" MODIFIED=\"2000\">"
				+ "<font NAME=\"Dialog\" SIZE=\"14\" BOLD=\"true\" ITALIC=\"true\" STRIKETHROUGH=\"true\"/>"
				+ "<icon BUILTIN=\"idea\"/>"
				+ "<attribute NAME=\"Kind\" VALUE=\"Note\"/>"
				+ "<edge STYLE=\"linear\" COLOR=\"#ff0000\" WIDTH=\"thin\"/>"
				+ "<cloud STYLE=\"bezier\" COLOR=\"#abcdef\" WIDTH=\"4\"/>"
				+ "<arrowlink DESTINATION=\"cov-child\" COLOR=\"#112233\" STARTARROW=\"NONE\" ENDARROW=\"zigzag\""
				+ " STARTINCLINATION=\"1;2;\" ENDINCLINATION=\"3;4;\"/>"
				+ "<richcontent TYPE=\"NOTE\"><html>Noted</html></richcontent>"
				+ "<hook NAME=\"plugins/sample\"/>"
				+ "<attribute_name NAME=\"Registered\"><attribute_value VALUE=\"One\"/></attribute_name>"
				+ "<node TEXT=\"Child\" ID=\"cov-child\"/>"
				+ "<node TEXT=\"Secret\" ID=\"cov-secret\" COLOR=\"#010203\" ENCRYPTED_CONTENT=\"cipher\"/>"
				+ "</node></map>";
		MindMapNode parent = opened.mode.getMap().loadTree(new StringReaderCreator(xml), MapAdapter.sDontAskInstance);
		assertEquals("Parent", parent.getText());
		assertTrue(parent.isFolded());
		assertTrue(parent.isLeft());
		assertEquals(Color.BLUE, parent.getColor());
		assertEquals(Color.GREEN, parent.getBackgroundColor());
		assertEquals("bubble", parent.getBareStyle());
		assertEquals("#cov-child", parent.getLink());
		assertEquals(4, parent.getShiftY());
		assertEquals(5, parent.getVGap());
		assertEquals(6, parent.getHGap());
		assertEquals(1000L, parent.getHistoryInformation().getCreatedAt().getTime());
		assertEquals(2000L, parent.getHistoryInformation().getLastModifiedAt().getTime());
		assertTrue(parent.getFont().isBold());
		assertTrue(parent.getFont().isItalic());
		assertTrue(parent.isStrikethrough());
		assertEquals("idea", parent.getIcons().get(0).getName());
		assertEquals("Note", parent.getAttribute("Kind"));
		assertEquals("linear", parent.getEdge().getStyle());
		assertEquals(0, parent.getEdge().getWidth());
		assertEquals(Tools.xmlToColor("#abcdef"), parent.getCloud().getColor());
		assertEquals(4, parent.getCloud().getWidth());
		assertFalse(parent.getHooks().isEmpty());
		MindMapNode child = (MindMapNode) parent.getChildAt(0);
		assertEquals("Child", child.getText());
		MindMapNode secret = (MindMapNode) parent.getChildAt(1);
		assertTrue(secret instanceof EncryptedMindMapNode);
		assertEquals("Secret", secret.getText());
		assertEquals("cipher", secret.getAdditionalInfo());
		assertEquals(Tools.xmlToColor("#010203"), secret.getColor());
		MindMapLinkRegistry registry = opened.mode.getMap().getLinkRegistry();
		MindMapArrowLink arrow = (MindMapArrowLink) registry.getAllLinksFromMe(parent).get(0);
		assertEquals(MindMapArrowLink.ARROW_NONE, arrow.getStartArrow());
		assertEquals(MindMapArrowLink.ARROW_DEFAULT, arrow.getEndArrow());
		assertTrue(parent.getNoteText() == null || parent.getNoteText().contains("Noted"));
	}

	@Test
	void idMapAndUserObjectAreReplaced() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		Probe element = new Probe(opened.mode);
		HashMap<String, NodeAdapter> ids = new HashMap<>();
		element.replaceIds(ids);
		assertSame(ids, element.getIDToTarget());
		element.replaceUserObject("token");
		assertEquals("token", element.userObject());
	}

	private static final class Probe extends XMLElementAdapter {
		Probe(freemind.modes.MapFeedback feedback) {
			super(feedback);
		}

		void replaceIds(HashMap<String, NodeAdapter> ids) {
			setIDToTarget(ids);
		}

		Object userObject() {
			return getUserObject();
		}

		void replaceUserObject(Object value) {
			setUserObject(value);
		}
	}
}
