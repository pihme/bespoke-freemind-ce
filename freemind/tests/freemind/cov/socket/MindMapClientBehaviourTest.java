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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.HeadlessException;
import java.util.Properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.extensions.PermanentNodeHook;
import plugins.collaboration.socket.MindMapClient;
import plugins.collaboration.socket.SocketConnectionHook;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("MindMapClientBehaviourTest")
class MindMapClientBehaviourTest {

	private static final String HOST_KEY = "plugins.collaboration.database.host";

	@Test
	// backwards compatibility
	void slaveHooksDoNotTalkToAMaster() throws Exception {
		OpenClient client = new OpenClient();
		assertEquals(Integer.valueOf(1), client.getRole());
		assertEquals(0, client.getPort());
		assertNull(client.locked("ada"));
		client.broadcast("do", "undo", "id");
		client.release();
		client.shutdown();
		assertNull(client.getMasterInformation(null));
	}

	@Test
	// backwards compatibility
	void startupLeavesAnUnsavedPublishAndAJoinedMapBeforeAnyDialog() throws Exception {
		Opened opened = MapFixture.open("Child");
		opened.frame.setProperty(HOST_KEY, "sentinel");
		OpenClient publish = prepared(opened, "publish");
		publish.startupMapHook();
		assertEquals("sentinel", opened.frame.getProperty(HOST_KEY));
		assertFalse(hasSlave(opened));
		assertEquals("Child", opened.child("Child").getText());

		SocketConnectionHook slave = new SocketConnectionHook();
		slave.setController(opened.mode);
		slave.setName(MindMapClient.SLAVE_HOOK_LABEL);
		opened.root.invokeHook(slave);
		assertTrue(hasSlave(opened));
		OpenClient join = prepared(opened, "share");
		join.startupMapHook();
		assertFalse(hasSlave(opened));
		assertEquals("sentinel", opened.frame.getProperty(HOST_KEY));

		OpenClient fresh = prepared(opened, "share");
		assertThrows(HeadlessException.class, fresh::startupMapHook);
		assertEquals("sentinel", opened.frame.getProperty(HOST_KEY));
	}

	private static OpenClient prepared(Opened opened, String callType) {
		OpenClient client = new OpenClient();
		Properties properties = new Properties();
		properties.setProperty("callType", callType);
		client.setProperties(properties);
		client.setController(opened.mode);
		return client;
	}

	private static boolean hasSlave(Opened opened) {
		for (PermanentNodeHook hook : opened.root.getActivatedHooks()) {
			if (hook instanceof SocketConnectionHook) {
				return true;
			}
		}
		return false;
	}

	private static final class OpenClient extends MindMapClient {
		String locked(String user) throws Exception {
			return lock(user, null);
		}

		void broadcast(String doAction, String undoAction, String lockId) throws Exception {
			broadcastCommand(doAction, undoAction, lockId, null);
		}

		void release() {
			unlock(null);
		}
	}
}
