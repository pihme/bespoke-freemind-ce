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

package tests.freemind.cov.socket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.mindmapmode.actions.NodeHookAction;
import plugins.collaboration.socket.MindMapClient;
import plugins.collaboration.socket.MindMapMaster;
import plugins.collaboration.socket.SocketConnectionHook;
import plugins.collaboration.socket.SocketRegistration;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("SocketRegistrationBehaviourTest")
class SocketRegistrationBehaviourTest {

	@Test
	// backwards compatibility
	void menuChecksFollowWhicheverCollaborationHookIsActive() throws Exception {
		Opened opened = MapFixture.open("Child");
		SocketRegistration registration = new SocketRegistration(opened.mode, opened.mode.getMap());
		registration.register();
		JMenuItem item = new JMenuItem("share");
		NodeHookAction master = new NodeHookAction(MindMapMaster.LABEL, opened.mode);
		NodeHookAction starter = new NodeHookAction(MindMapClient.SLAVE_STARTER_LABEL, opened.mode);
		NodeHookAction publish = new NodeHookAction(MindMapClient.SLAVE_PUBLISH_LABEL, opened.mode);
		NodeHookAction other = new NodeHookAction("plugins/collaboration/socket/missing", opened.mode);

		assertFalse(registration.isSelected(item, master));
		assertFalse(registration.isSelected(item, starter));
		assertFalse(registration.isSelected(item, publish));
		assertFalse(registration.isSelected(item, other));
		assertFalse(registration.isSelected(item, new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(java.awt.event.ActionEvent event) {
			}
		}));
		assertTrue(registration.isEnabled(item, master));
		assertTrue(registration.isEnabled(item, starter));
		assertTrue(registration.isEnabled(item, publish));

		QuietMaster hosted = new QuietMaster();
		hosted.setController(opened.mode);
		hosted.setName(MindMapMaster.LABEL);
		opened.root.invokeHook(hosted);
		assertTrue(registration.isSelected(item, master));
		assertFalse(registration.isEnabled(item, starter));
		assertFalse(registration.isEnabled(item, publish));
		assertTrue(registration.isEnabled(item, master));

		SocketConnectionHook slave = new SocketConnectionHook();
		slave.setController(opened.mode);
		slave.setName(MindMapClient.SLAVE_HOOK_LABEL);
		opened.root.invokeHook(slave);
		assertTrue(registration.isSelected(item, starter));
		assertFalse(registration.isEnabled(item, master));
		assertFalse(registration.isEnabled(item, publish));

		registration.deRegister();
		assertEquals(2, opened.root.getActivatedHooks().size());
		Exception unknown = assertThrows(IllegalArgumentException.class,
				() -> registration.isEnabled(item, other));
		assertTrue(unknown.getMessage().contains("Unknown action"));
		assertThrows(IllegalArgumentException.class, () -> registration.isEnabled(item, new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(java.awt.event.ActionEvent event) {
			}
		}));
	}

	/** Skips the password dialog so the hook can sit on the root. */
	private static final class QuietMaster extends MindMapMaster {
		public void startupMapHook() {
		}
	}
}
