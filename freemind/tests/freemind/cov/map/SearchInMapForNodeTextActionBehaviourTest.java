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

package tests.freemind.cov.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.main.FreeMind;
import freemind.modes.MindMapNode;
import plugins.map.Registration;
import plugins.map.SearchInMapForNodeTextAction;

@DisplayName("SearchInMapForNodeTextActionBehaviourTest")
class SearchInMapForNodeTextActionBehaviourTest {

	@Test
	// backwards compatibility
	void skipsTheSearchWhenTheStoredAnswerIsCancel() throws Exception {
		try (MapCovSupport support = MapCovSupport.open("Child", "Sibling")) {
			remember(support, "false");
			MindMapNode first = support.opened.child("Child");
			MindMapNode second = support.opened.child("Sibling");
			support.opened.mode.select(first, List.of(first, second));
			SearchInMapForNodeTextAction action = wired(support);
			assertSame(support.registration, action.getRegistration());
			action.invoke(first);
			assertFalse(support.dialog.isSearchBarVisible());
			assertEquals("", support.searchTerm.getText());
		}
	}

	@Test
	// backwards compatibility
	void searchesThePlainNodeTextWhenTheStoredAnswerIsOk() throws Exception {
		try (MapCovSupport support = MapCovSupport.open("Child")) {
			remember(support, "true");
			MindMapNode node = support.opened.child("Child");
			node.setText("<html><b>Cafe</b></html>");
			support.select(node);
			SearchInMapForNodeTextAction action = wired(support);
			action.invoke(node);
			assertTrue(support.dialog.isSearchBarVisible());
			assertEquals("Cafe", support.searchTerm.getText());
			assertTrue(support.results.getRowCount() >= 1);

			SearchInMapForNodeTextAction detached = wired(support);
			detached.setPluginBaseClass(() -> null);
			int rows = support.results.getRowCount();
			detached.invoke(node);
			assertEquals(rows, support.results.getRowCount());

			SearchInMapForNodeTextAction closed = wired(support);
			support.registration.setMapDialog(null);
			closed.invoke(node);
			assertEquals(rows, support.results.getRowCount());
			assertNull(support.registration.getMapDialog());
			support.registration.setMapDialog(support.dialog);
		}
	}

	private static void remember(MapCovSupport support, String answer) {
		support.opened.frame.setProperty(FreeMind.RESOURCES_SEARCH_FOR_NODE_TEXT_WITHOUT_QUESTION, answer);
	}

	private static SearchInMapForNodeTextAction wired(MapCovSupport support) {
		SearchInMapForNodeTextAction action = new SearchInMapForNodeTextAction();
		action.setController(support.opened.mode);
		action.setPluginBaseClass(() -> support.registration);
		assertSame(support.registration, (Registration) action.getPluginBaseClass());
		return action;
	}
}
