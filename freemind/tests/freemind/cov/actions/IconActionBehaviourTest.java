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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.event.ActionEvent;
import java.util.List;

import org.junit.jupiter.api.Test;

import freemind.controller.actions.generated.instance.AddIconAction;
import freemind.modes.MindIcon;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.MindMapController;
import freemind.modes.mindmapmode.actions.IconAction;
import freemind.modes.mindmapmode.actions.RemoveIconAction;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** An icon action adds its icon, and control or shift takes icons off. */
class IconActionBehaviourTest {

	@Test
	void controlRemovesTheFirstMatchingIcon() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Leaf");
		MindMapNode leaf = opened.child("Leaf");
		opened.mode.select(leaf, List.of(leaf));
		Exposed action = new Exposed(opened.mode, MindIcon.factory("idea"));
		assertEquals(AddIconAction.class, action.getDoActionClass());
		assertNull(action.getKeyStroke());
		ActionEvent plain = new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "icon");
		action.actionPerformed(plain);
		action.actionPerformed(plain);
		assertEquals(2, leaf.getIcons().size());
		action.actionPerformed(new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "icon",
				ActionEvent.ALT_MASK));
		assertEquals(1, leaf.getIcons().size());
		action.actionPerformed(new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "icon",
				ActionEvent.CTRL_MASK));
		assertEquals(0, leaf.getIcons().size());
	}

	@Test
	void shiftReplacesEveryIconWithOne() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Leaf");
		MindMapNode leaf = opened.child("Leaf");
		opened.mode.addIcon(leaf, MindIcon.factory("button_ok"));
		opened.mode.addIcon(leaf, MindIcon.factory("button_ok"));
		opened.mode.select(leaf, List.of(leaf));
		Exposed action = new Exposed(opened.mode, MindIcon.factory("idea"));
		AddIconAction created = action.created(leaf);
		assertEquals("idea", created.getIconName());
		assertEquals(0, created.getIconPosition());
		action.actionPerformed(new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "icon",
				ActionEvent.SHIFT_MASK));
		assertEquals(1, leaf.getIcons().size());
		assertEquals("idea", leaf.getIcons().get(0).getName());
	}

	private static final class Exposed extends IconAction {
		private static final long serialVersionUID = 1L;

		Exposed(MindMapController controller, MindIcon icon) {
			super(controller, icon, new RemoveIconAction(controller));
		}

		AddIconAction created(MindMapNode node) {
			return createAddIconAction(node, icon, 0);
		}
	}
}
