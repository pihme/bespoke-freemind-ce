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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JMenuItem;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.mindmapmode.actions.NodeHookAction;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Hierarchical icons attach to the root, and an unknown hook name is rejected. */
@DisplayName("NodeHookActionBehaviourTest")
class NodeHookActionBehaviourTest {

	@Test
	void hierarchicalIconsAttachToTheRoot() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Leaf");
		NodeHookAction action = new NodeHookAction("accessories/plugins/HierarchicalIcons.properties", opened.mode);
		assertEquals("accessories/plugins/HierarchicalIcons.properties", action.getHookName());
		assertSame(opened.mode, action.getController());
		opened.mode.select(opened.root, List.of(opened.root));
		JMenuItem item = new JMenuItem();
		assertTrue(action.isEnabled(item, action));
		assertFalse(action.isSelected(item, action));
		action.actionPerformed(new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "icons"));
		assertTrue(action.isSelected(item, action));
		assertFalse(opened.root.getHooks().isEmpty());
	}

	@Test
	void anUnknownHookNameIsRejected() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Leaf");
		NodeHookAction action = new NodeHookAction("no.such.NodeHook", opened.mode);
		assertThrows(IllegalArgumentException.class, () -> action.isEnabled(new JMenuItem(), action));
	}
}
