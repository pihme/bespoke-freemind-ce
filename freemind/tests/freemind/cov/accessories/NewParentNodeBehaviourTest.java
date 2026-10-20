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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.HeadlessException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import accessories.plugins.NewParentNode;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.MindMapNodeModel;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

class NewParentNodeBehaviourTest {

	// backwards compatibility
	@Test
	void selectingOnlyTheRootWrapsTheFormerChildren() throws Exception {
		Opened opened = MapFixture.open("Kept");
		select(opened, opened.root);
		NewParentNode hook = hook(opened);

		hook.invoke(opened.root);

		assertEquals(1, opened.root.getChildCount());
		MindMapNode wrapper = (MindMapNode) opened.root.getChildAt(0);
		assertTrue(MapFixture.childTexts(wrapper).contains("Kept"), MapFixture.childTexts(wrapper).toString());
	}

	// backwards compatibility
	@Test
	void mixedParentsAndTheRootAreRejected() throws Exception {
		Opened opened = MapFixture.open("Left");
		MindMapNode left = opened.child("Left");
		MindMapNode nested = opened.mode.addNewNode(left, 0, false);
		opened.mode.setNodeText(nested, "Nested");
		select(opened, left);
		opened.view.toggleSelected(opened.view.getNodeView(nested));
		NewParentNode hook = hook(opened);
		int before = opened.root.getChildCount();

		assertThrows(HeadlessException.class, () -> hook.invoke(opened.root));

		assertEquals(before, opened.root.getChildCount());
		assertEquals(left, nested.getParentNode());
	}

	// backwards compatibility
	@Test
	void anEmptyMoveReturnsTheNewNodeAndARootMoveIsRejected() throws Exception {
		Opened opened = MapFixture.open("Child");
		NewParentNode hook = hook(opened);
		MindMapNode created = new MindMapNodeModel("Fresh", opened.mode.getMap());
		Method move = NewParentNode.class.getDeclaredMethod("moveToOtherNode", MindMapNode.class, List.class,
				MindMapNode.class, MindMapNode.class);
		move.setAccessible(true);

		assertSame(created, move.invoke(hook, opened.root, Collections.emptyList(), opened.root, created));

		assertThrows(HeadlessException.class, () -> {
			try {
				move.invoke(hook, opened.root, List.of(opened.root), opened.root, created);
			} catch (java.lang.reflect.InvocationTargetException failure) {
				if (failure.getCause() instanceof Exception cause) {
					throw cause;
				}
				throw failure;
			}
		});
		assertEquals(0, created.getChildCount());
	}

	private static NewParentNode hook(Opened opened) {
		NewParentNode hook = new NewParentNode();
		hook.setController(opened.mode);
		hook.setMap(opened.mode.getMap());
		Properties properties = new Properties();
		properties.setProperty("cannot_add_parent_to_root", "cannot add");
		properties.setProperty("cannot_add_parent_diff_parents", "different");
		hook.setProperties(properties);
		return hook;
	}

	private static void select(Opened opened, MindMapNode node) {
		opened.view.getRoot().updateAll();
		opened.view.selectAsTheOnlyOneSelected(opened.view.getNodeView(node));
	}
}
