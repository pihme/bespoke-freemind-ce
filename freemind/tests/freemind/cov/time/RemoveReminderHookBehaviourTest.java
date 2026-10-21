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

package tests.freemind.cov.time;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import accessories.plugins.time.RemoveReminderHook;
import accessories.plugins.time.TimeManagement;
import accessories.plugins.time.TimeManagementOrganizer;
import freemind.modes.MindMapNode;
import freemind.modes.common.plugins.ReminderHookBase;
import tests.freemind.cov.MapFixture;

/** Backwards-compatible removal of a reminder hook. */
@DisplayName("RemoveReminderHookBehaviourTest")
class RemoveReminderHookBehaviourTest {

	@Test
	void invokeWithoutAReminderLeavesTheNodeText() throws Exception {
		// backwards compatibility
		MapFixture.Opened opened = MapFixture.open("Child");
		MindMapNode node = opened.child("Child");
		RemoveReminderHook hook = new RemoveReminderHook();
		hook.setController(opened.mode);
		hook.invoke(node);
		assertEquals("Child", node.getText());
		assertNull(TimeManagementOrganizer.getHook(node));
	}

	@Test
	void invokeRemovesAFutureReminder() throws Exception {
		// backwards compatibility
		MapFixture.Opened opened = MapFixture.open("Child");
		MindMapNode node = opened.child("Child");
		Properties properties = new Properties();
		properties.setProperty(ReminderHookBase.REMINDUSERAT,
				Long.toString(System.currentTimeMillis() + 120_000L));
		opened.mode.addHook(node, List.of(node), TimeManagement.REMINDER_HOOK_NAME, properties);
		ReminderHookBase present = TimeManagementOrganizer.getHook(node);
		assertNotNull(present);
		try {
			RemoveReminderHook hook = new RemoveReminderHook();
			hook.setController(opened.mode);
			hook.setMap(opened.mode.getMap());
			hook.invoke(node);
			assertNull(TimeManagementOrganizer.getHook(node));
			assertEquals("Child", node.getText());
		} finally {
			ReminderHookBase left = TimeManagementOrganizer.getHook(node);
			if (left != null) {
				node.removeHook(left);
			}
		}
	}
}
