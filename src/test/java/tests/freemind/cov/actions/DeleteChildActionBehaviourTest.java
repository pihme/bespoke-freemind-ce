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

import freemind.controller.actions.generated.instance.DeleteNodeAction;
import freemind.main.FreeMind;
import freemind.modes.mindmapmode.actions.DeleteChildAction;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** The root cannot be deleted, and a stored yes removes the selected child. */
@DisplayName("DeleteChildActionBehaviourTest")
class DeleteChildActionBehaviourTest {

	@Test
	void theRootStays() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Kept");
		opened.mode.select(opened.root, List.of(opened.root));
		DeleteChildAction action = new DeleteChildAction(opened.mode);
		assertEquals(DeleteNodeAction.class, action.getDoActionClass());
		assertThrows(HeadlessException.class,
				() -> action.actionPerformed(new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "delete")));
		assertEquals(List.of("Kept"), MapFixture.childTexts(opened.root));
	}

	@Test
	void aStoredYesRemovesTheChild() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Gone");
		opened.frame.setProperty(FreeMind.RESOURCES_DELETE_NODES_WITHOUT_QUESTION, "true");
		opened.mode.select(opened.child("Gone"), List.of(opened.child("Gone")));
		new DeleteChildAction(opened.mode).actionPerformed(
				new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "delete"));
		assertEquals(List.of(), MapFixture.childTexts(opened.root));
	}
}
