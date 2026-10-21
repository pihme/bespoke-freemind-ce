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

package tests.freemind.cov.browsemode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.extensions.NodeHook;
import freemind.extensions.PermanentNodeHookSubstituteUnknown;
import freemind.modes.browsemode.BrowseHookFactory;
import freemind.modes.browsemode.BrowseReminderHook;
import freemind.modes.common.plugins.MapNodePositionHolderBase;
import freemind.modes.common.plugins.ReminderHookBase;

/** Browse mode only builds the reminder and map-position hooks. */
@DisplayName("BrowseHookFactoryBehaviourTest")
class BrowseHookFactoryBehaviourTest {

	@Test
	void knownHooksAreBuiltAndTheOtherQueriesStayEmpty() {
		// backwards compatibility
		BrowseHookFactory factory = new BrowseHookFactory();
		NodeHook reminder = factory.createNodeHook(ReminderHookBase.PLUGIN_LABEL);
		assertTrue(reminder instanceof BrowseReminderHook);
		assertEquals(ReminderHookBase.PLUGIN_LABEL, reminder.getName());

		NodeHook mapHook = factory.createNodeHook(MapNodePositionHolderBase.NODE_MAP_HOOK_NAME);
		assertTrue(mapHook instanceof MapNodePositionHolderBase);
		assertEquals(MapNodePositionHolderBase.NODE_MAP_HOOK_NAME, mapHook.getName());

		NodeHook unknown = factory.createNodeHook("plugins/missing.xml");
		assertTrue(unknown instanceof PermanentNodeHookSubstituteUnknown);
		assertEquals("plugins/missing.xml", unknown.getName());

		assertTrue(factory.getPossibleNodeHooks().isEmpty());
		assertTrue(factory.getPossibleModeControllerHooks().isEmpty());
		assertNull(factory.createModeControllerHook("plugins/missing.xml"));
		assertNull(factory.getHookMenuPositions("plugins/missing.xml"));
		assertNull(factory.getInstanciationMethod("plugins/missing.xml"));
		assertNull(factory.getRegistrations());
		assertNull(factory.getPluginBaseClass("plugins/missing.xml"));
	}
}
