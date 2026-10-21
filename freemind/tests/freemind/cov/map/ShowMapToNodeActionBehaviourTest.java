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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.extensions.MindMapHook;
import freemind.modes.MindMapNode;
import plugins.map.MapNodePositionHolder;
import plugins.map.Registration;
import plugins.map.ShowMapToNodeAction;

@DisplayName("ShowMapToNodeActionBehaviourTest")
class ShowMapToNodeActionBehaviourTest {

	@Test
	// backwards compatibility
	void movesTheCursorToTheSelectedNode() throws Exception {
		try (MapCovSupport support = MapCovSupport.open("Child")) {
			MindMapNode node = support.opened.child("Child");
			MapNodePositionHolder holder = support.attach(node, 46.2, 7.4, 6);
			support.dialog.registerMapNode(holder);
			support.select(node);
			ShowMapToNodeAction action = wired(support);
			assertSame(support.registration, action.getRegistration());
			action.invoke(node);
			assertEquals(46.2, support.cursor().getLat(), 0.001);
			assertEquals(6, support.viewer.getZoom());
		}
	}

	@Test
	// backwards compatibility
	void stopsWhenTheRegistrationOrTheDialogIsMissing() throws Exception {
		try (MapCovSupport support = MapCovSupport.open("Child")) {
			MindMapNode node = support.opened.child("Child");
			support.select(node);
			ShowMapToNodeAction detached = wired(support);
			detached.setPluginBaseClass(() -> null);
			double lat = support.cursor().getLat();
			detached.invoke(node);
			assertEquals(lat, support.cursor().getLat());

			ShowMapToNodeAction closed = wired(support);
			support.registration.setMapDialog(null);
			closed.invoke(node);
			assertNull(support.registration.getMapDialog());
			assertEquals(lat, support.cursor().getLat());
			support.registration.setMapDialog(support.dialog);
		}
	}

	private static ShowMapToNodeAction wired(MapCovSupport support) {
		ShowMapToNodeAction action = new ShowMapToNodeAction();
		action.setController(support.opened.mode);
		action.setPluginBaseClass((MindMapHook.PluginBaseClassSearcher) () -> support.registration);
		assertSame(support.registration, (Registration) action.getPluginBaseClass());
		return action;
	}
}
