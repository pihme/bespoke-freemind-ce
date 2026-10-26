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

package tests.freemind.cov.commonactions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.awt.event.ActionEvent;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.MindMapNode;
import freemind.modes.common.actions.NewMapAction;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** New map opens another map and leaves the previous root in place. */
@DisplayName("NewMapActionBehaviourTest")
class NewMapActionBehaviourTest {

	@Test
	void actionPerformedOpensAnotherMapWithANewRoot() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Kept");
		int before = opened.app.getMapModuleManager().getMapModuleVector().size();
		MindMapNode previousRoot = opened.root;

		new NewMapAction(opened.mode).actionPerformed(
				new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "new"));

		assertEquals(before + 1, opened.app.getMapModuleManager().getMapModuleVector().size());
		MindMapNode currentRoot = opened.app.getMapModuleManager().getMapModule().getModel().getRootNode();
		assertNotSame(previousRoot, currentRoot);
		assertEquals("ROOT", previousRoot.getText());
		assertEquals(List.of("Kept"), MapFixture.childTexts(previousRoot));
		assertEquals(opened.frame.getResourceString("new_mindmap"), currentRoot.getText());
		assertEquals(0, currentRoot.getChildCount());
	}
}
