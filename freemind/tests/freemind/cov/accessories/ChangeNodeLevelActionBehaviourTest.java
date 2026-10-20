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

package tests.freemind.cov.accessories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.HeadlessException;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import accessories.plugins.ChangeNodeLevelAction;
import freemind.modes.MindMapNode;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

class ChangeNodeLevelActionBehaviourTest {

	// backwards compatibility
	@Test
	void aNodeMovesUpBesideItsParentAndDownUnderItsSibling() throws Exception {
		Opened opened = MapFixture.open("Parent");
		MindMapNode parent = opened.child("Parent");
		MindMapNode keeper = opened.mode.addNewNode(parent, 0, parent.isLeft());
		opened.mode.setNodeText(keeper, "Keeper");
		MindMapNode mover = opened.mode.addNewNode(parent, 1, parent.isLeft());
		opened.mode.setNodeText(mover, "Mover");
		select(opened, mover);

		hook(opened, upwards(mover)).invoke(opened.root);

		MindMapNode raised = find(opened.root, "Mover");
		assertEquals(opened.root, raised.getParentNode());

		MindMapNode sibling = find(opened.root, "Parent");
		select(opened, raised);
		hook(opened, downwards(raised)).invoke(opened.root);
		MindMapNode lowered = find(opened.root, "Mover");
		assertTrue(lowered.getParentNode() == sibling || lowered.getParentNode() == keeper
				|| lowered.getParentNode() == parent, lowered.getParentNode().getText());
	}

	// backwards compatibility
	@Test
	void theRootAndMixedParentsAreRejected() throws Exception {
		Opened opened = MapFixture.open("Left", "Right");
		MindMapNode left = opened.child("Left");
		MindMapNode nested = opened.mode.addNewNode(left, 0, false);
		opened.mode.setNodeText(nested, "Nested");
		select(opened, opened.root);
		ChangeNodeLevelAction hook = hook(opened, "left");
		assertThrows(HeadlessException.class, () -> hook.invoke(opened.root));
		assertEquals("ROOT", opened.root.getText());

		select(opened, left);
		opened.view.toggleSelected(opened.view.getNodeView(nested));
		assertThrows(HeadlessException.class, () -> hook.invoke(opened.root));
		assertEquals(left, nested.getParentNode());

		select(opened, nested);
		assertThrows(HeadlessException.class, () -> hook.invoke(nested));
		assertEquals(left, nested.getParentNode());
	}

	private static String upwards(MindMapNode node) {
		return node.isLeft() ? "right" : "left";
	}

	private static String downwards(MindMapNode node) {
		return node.isLeft() ? "left" : "right";
	}

	private static MindMapNode find(MindMapNode node, String text) {
		if (text.equals(node.getText())) {
			return node;
		}
		for (int i = 0; i < node.getChildCount(); i++) {
			MindMapNode found = find((MindMapNode) node.getChildAt(i), text);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	private static ChangeNodeLevelAction hook(Opened opened, String actionType) {
		ChangeNodeLevelAction hook = new ChangeNodeLevelAction();
		hook.setController(opened.mode);
		hook.setMap(opened.mode.getMap());
		Properties properties = new Properties();
		properties.setProperty("action_type", actionType);
		hook.setProperties(properties);
		return hook;
	}

	private static void select(Opened opened, MindMapNode node) {
		opened.view.getRoot().updateAll();
		opened.view.selectAsTheOnlyOneSelected(opened.view.getNodeView(node));
	}
}
