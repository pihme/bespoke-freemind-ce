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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import org.junit.jupiter.api.Test;

import accessories.plugins.time.TimeManagement;
import accessories.plugins.time.TimeManagementOrganizer;
import freemind.modes.MindMapNode;
import freemind.modes.common.plugins.ReminderHookBase;
import freemind.modes.mindmapmode.actions.NodeHookAction;
import tests.freemind.cov.MapFixture;

/** Backwards-compatible reminder menu visibility. */
class TimeManagementOrganizerBehaviourTest {

	@Test
	void removeReminderIsVisibleOnlyWhenASelectedNodeHasOne() throws Exception {
		// backwards compatibility
		MapFixture.Opened opened = MapFixture.open("Child");
		MindMapNode node = opened.child("Child");
		opened.mode.select(node, List.of(node));
		TimeManagementOrganizer organizer = new TimeManagementOrganizer(opened.mode, opened.mode.getMap());
		organizer.register();
		organizer.deRegister();
		JMenuItem item = new JMenuItem("remove");
		item.setVisible(false);
		AbstractAction other = new AbstractAction("other") {
			public void actionPerformed(ActionEvent event) {
			}
		};
		assertTrue(organizer.isEnabled(item, other));
		assertFalse(item.isVisible());

		NodeHookAction remove = new NodeHookAction("plugins/time/RemoveReminderHook.java", opened.mode);
		assertTrue(organizer.isEnabled(item, remove));
		assertFalse(item.isVisible());
		assertNull(TimeManagementOrganizer.getHook(node));

		Properties properties = new Properties();
		properties.setProperty(ReminderHookBase.REMINDUSERAT,
				Long.toString(System.currentTimeMillis() + 120_000L));
		try {
			opened.mode.addHook(node, List.of(node), TimeManagement.REMINDER_HOOK_NAME, properties);
			assertNotNull(TimeManagementOrganizer.getHook(node));
			assertTrue(organizer.isEnabled(item, remove));
			assertTrue(item.isVisible());
		} finally {
			ReminderHookBase left = TimeManagementOrganizer.getHook(node);
			if (left != null) {
				node.removeHook(left);
			}
		}
		assertTrue(organizer.isEnabled(item, remove));
		assertFalse(item.isVisible());
	}
}
