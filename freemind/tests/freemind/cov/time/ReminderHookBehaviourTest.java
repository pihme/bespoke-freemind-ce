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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Properties;

import javax.swing.Timer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import accessories.plugins.time.ReminderHook;
import freemind.modes.MindMapNode;
import tests.freemind.cov.MapFixture;

/** Backwards-compatible reminder tooltip and clock icon. */
@DisplayName("ReminderHookBehaviourTest")
class ReminderHookBehaviourTest {

	@Test
	void aZeroReminderLeavesTheNodeUnmarked() throws Exception {
		// backwards compatibility
		MapFixture.Opened opened = MapFixture.open("Child");
		MindMapNode node = opened.child("Child");
		ReminderHook hook = hook(opened, node, 0L);
		hook.invoke(node);
		assertTrue(node.getStateIcons().isEmpty());
		assertFalse(node.getToolTip().containsKey(hook.getName()));
		assertEquals(0L, hook.getRemindUserAt());
	}

	@Test
	void aFutureReminderSetsTheTooltipAndShutdownClearsIt() throws Exception {
		// backwards compatibility
		MapFixture.Opened opened = MapFixture.open("Child");
		MindMapNode node = opened.child("Child");
		long when = System.currentTimeMillis() + 120_000L;
		ReminderHook hook = hook(opened, node, when);
		try {
			hook.invoke(node);
			assertTrue(when - hook.getRemindUserAt() < 1000L);
			assertNotNull(node.getToolTip().get(hook.getName()));
			assertNotNull(node.getStateIcons().get(hook.getStateKey()));
			assertTrue(opened.root.getStateIcons().isEmpty());
			Timer timer = timer(hook);
			assertNotNull(timer);
			assertTrue(timer.isRunning());
		} finally {
			hook.shutdownMapHook();
		}
		assertFalse(timer(hook).isRunning());
		assertNull(node.getStateIcons().get(hook.getStateKey()));
		assertFalse(node.getToolTip().containsKey(hook.getName()));
	}

	private static ReminderHook hook(MapFixture.Opened opened, MindMapNode node, long when) {
		ReminderHook hook = new ReminderHook();
		hook.setName("plugins/TimeManagementReminder.xml");
		hook.setController(opened.mode);
		hook.setMap(opened.mode.getMap());
		hook.setProperties(new Properties());
		hook.setNode(node);
		hook.setRemindUserAt(when);
		return hook;
	}

	private static Timer timer(ReminderHook hook) throws Exception {
		Field field = hook.getClass().getSuperclass().getDeclaredField("timer");
		field.setAccessible(true);
		return (Timer) field.get(hook);
	}
}
