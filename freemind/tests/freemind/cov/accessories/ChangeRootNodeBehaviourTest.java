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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;

import javax.swing.JMenuItem;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import accessories.plugins.ChangeRootNode;
import freemind.controller.actions.generated.instance.ChangeRootNodeAction;
import freemind.modes.MindMapNode;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("ChangeRootNodeBehaviourTest")
class ChangeRootNodeBehaviourTest {

	// backwards compatibility
	@Test
	void theSelectedChildBecomesTheRoot() throws Exception {
		Opened opened = MapFixture.open("Next", "Other");
		MindMapNode next = opened.child("Next");
		select(opened, next);
		opened.view.toggleSelected(opened.view.getNodeView(opened.child("Other")));
		ChangeRootNode.Registration registration = new ChangeRootNode.Registration(opened.mode, opened.mode.getMap());
		assertFalse(registration.isEnabled(new JMenuItem(), null));
		select(opened, next);
		assertTrue(registration.isEnabled(new JMenuItem(), null));
		registration.register();
		ChangeRootNode hook = new ChangeRootNode();
		hook.setController(opened.mode);
		hook.setMap(opened.mode.getMap());
		hook.setProperties(new Properties());
		try {
			hook.invoke(next);
			assertEquals("Next", opened.mode.getRootNode().getText());
			ChangeRootNodeAction already = new ChangeRootNodeAction();
			already.setNode(opened.mode.getNodeID(opened.mode.getRootNode()));
			registration.act(already);
			assertEquals("Next", opened.mode.getRootNode().getText());
		} finally {
			registration.deRegister();
		}
	}

	private static void select(Opened opened, MindMapNode node) {
		opened.view.getRoot().updateAll();
		opened.view.selectAsTheOnlyOneSelected(opened.view.getNodeView(node));
	}
}
