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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Properties;

import javax.swing.Timer;

import org.junit.jupiter.api.Test;

import freemind.modes.MindMapNode;
import freemind.modes.browsemode.BrowseReminderHook;
import freemind.modes.common.plugins.ReminderHookBase;
import tests.freemind.cov.MapFixture;

/** A browse reminder marks the node, then clears the mark on shutdown. */
class BrowseReminderHookBehaviourTest {

	@Test
	void aZeroReminderLeavesTheNodeUnmarked() throws Exception {
		// backwards compatibility
		MapFixture.Opened opened = MapFixture.open("Child");
		MindMapNode node = opened.child("Child");
		BrowseReminderHook hook = hook(opened, node, 0L);
		hook.invoke(node);
		assertTrue(node.getStateIcons().isEmpty());
		assertFalse(node.getToolTip().containsKey(hook.getName()));
		assertNull(timer(hook));
	}

	@Test
	void aFutureReminderSetsTheTooltipAndShutdownClearsIt() throws Exception {
		// backwards compatibility
		MapFixture.Opened opened = MapFixture.open("Child");
		MindMapNode node = opened.child("Child");
		BrowseReminderHook hook = hook(opened, node, System.currentTimeMillis() + 120_000L);
		try {
			hook.invoke(node);
			assertNotNull(node.getToolTip().get(hook.getName()));
			assertNotNull(node.getStateIcons().get(hook.getStateKey()));
			assertTrue(timer(hook).isRunning());
		} finally {
			hook.shutdownMapHook();
		}
		assertFalse(timer(hook).isRunning());
		assertNull(node.getStateIcons().get(hook.getStateKey()));
		assertFalse(node.getToolTip().containsKey(hook.getName()));
	}

	private static BrowseReminderHook hook(MapFixture.Opened opened, MindMapNode node, long when) {
		BrowseReminderHook hook = new BrowseReminderHook();
		hook.setName(ReminderHookBase.PLUGIN_LABEL);
		hook.setController(opened.mode);
		hook.setMap(opened.mode.getMap());
		hook.setProperties(new Properties());
		hook.setNode(node);
		hook.setRemindUserAt(when);
		return hook;
	}

	private static Timer timer(BrowseReminderHook hook) throws Exception {
		Field field = hook.getClass().getSuperclass().getDeclaredField("timer");
		field.setAccessible(true);
		return (Timer) field.get(hook);
	}
}
