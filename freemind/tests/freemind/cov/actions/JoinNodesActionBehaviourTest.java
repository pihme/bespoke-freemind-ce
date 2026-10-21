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

package tests.freemind.cov.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.actions.JoinNodesAction;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Joining leaves concatenates their text and drops the extra nodes. */
@DisplayName("JoinNodesActionBehaviourTest")
class JoinNodesActionBehaviourTest {

	@Test
	void aParentWithChildrenIsLeftIntact() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Parent");
		MindMapNode parent = opened.child("Parent");
		opened.mode.addNewNode(parent, 0, false);
		opened.mode.select(parent, List.of(parent));
		assertThrows(HeadlessException.class,
				() -> new JoinNodesAction(opened.mode).actionPerformed(
						new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "join")));
		assertEquals(1, parent.getChildCount());
		assertEquals(List.of("Parent"), MapFixture.childTexts(opened.root));
	}

	@Test
	void plainAndHtmlLeavesBecomeOneNode() throws Exception {
		// backwards compatibility
		Opened plain = MapFixture.open("Red", "Blue");
		MindMapNode red = plain.child("Red");
		MindMapNode blue = plain.child("Blue");
		plain.mode.select(red, List.of(red, blue));
		new JoinNodesAction(plain.mode).joinNodes(red, List.of(red, blue));
		assertEquals("Red Blue", red.getText());
		assertEquals(List.of("Red Blue"), MapFixture.childTexts(plain.root));

		Opened html = MapFixture.open("One", "Two");
		MindMapNode one = html.child("One");
		MindMapNode two = html.child("Two");
		html.mode.setNodeText(one, "<html><body><p>One</p></body></html>");
		html.mode.select(one, List.of(one, two));
		new JoinNodesAction(html.mode).joinNodes(one, List.of(one, two));
		assertEquals("<html><body><p>One</p><html><body><p>Two</p>", one.getText());
		assertEquals(1, html.root.getChildCount());
	}
}
