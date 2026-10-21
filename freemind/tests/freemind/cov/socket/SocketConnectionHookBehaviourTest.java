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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.controller.actions.generated.instance.CollaborationGoodbye;
import freemind.controller.actions.generated.instance.CollaborationTransaction;
import freemind.controller.actions.generated.instance.CollaborationUserInformation;
import freemind.main.Tools;
import freemind.main.XMLElement;
import freemind.modes.ExtendedMapFeedback;
import plugins.collaboration.socket.ClientCommunication;
import plugins.collaboration.socket.CommunicationBase;
import plugins.collaboration.socket.SocketConnectionHook;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;
import tests.freemind.cov.socket.SocketCovSupport.Pipe;

@DisplayName("SocketConnectionHookBehaviourTest")
class SocketConnectionHookBehaviourTest {

	@Test
	// backwards compatibility
	void delegatesLockingAndShutdownToTheClient() throws Exception {
		Opened opened = MapFixture.open("Child");
		OpenHook hook = new OpenHook();
		hook.setController(opened.mode);
		CollaborationUserInformation empty = hook.getMasterInformation(opened.mode);
		assertNull(empty.getUserIds());
		assertEquals(0, empty.getMasterPort());
		hook.startupMapHook();
		assertEquals(Integer.valueOf(1), hook.getRole());
		XMLElement xml = new XMLElement();
		hook.loadFrom(xml);
		hook.save(xml);
		assertEquals(0, xml.countChildren());
		StringWriter html = new StringWriter();
		hook.saveHtml(html);
		assertEquals("", html.toString());
		hook.onAddChild(opened.root);
		hook.onAddChildren(opened.root);
		hook.onLostFocusNode(null);
		hook.onNewChild(opened.root);
		hook.onRemoveChild(opened.root);
		hook.onRemoveChildren(opened.root, opened.root);
		hook.onFocusNode(null);
		hook.onUpdateChildrenHook(opened.root);
		hook.onUpdateNodeHook();
		hook.onViewCreatedHook(null);
		hook.onViewRemovedHook(null);
		hook.processUnfinishedLinks();
		assertEquals("ROOT", opened.root.getText());
		hook.release(opened.mode);

		try (Pipe pipe = Pipe.open()) {
			ClientCommunication client = new ClientCommunication("hook-client", pipe.local, opened.mode, "secret");
			SocketCovSupport.hurry(client);
			hook.setClientCommunication(client);
			assertSame(client, hook.getClientCommunication());
			assertEquals(pipe.local.getLocalPort(), hook.getPort());
			CollaborationUserInformation info = new CollaborationUserInformation();
			info.setUserIds("ada");
			info.setMasterPort(4242);
			client.processCommand(info);
			assertSame(info, hook.getMasterInformation(opened.mode));
			assertEquals("ada", client.getUserInfo().getUserIds());

			String doXml = Tools.marshall(new freemind.controller.actions.generated.instance.CompoundAction());
			hook.cast(doXml, doXml, "lock-9");
			CollaborationTransaction sent = (CollaborationTransaction) pipe.read();
			assertEquals("lock-9", sent.getId());
			assertEquals(doXml, sent.getDoAction());
			assertEquals(CommunicationBase.STATE_WAIT_FOR_WHO_ARE_YOU, client.getCurrentState());

			pipe.remote.close();
			Exception failure = hook.failedLock("ada", opened.mode);
			assertTrue(SocketCovSupport.lockFailure(failure));
			assertEquals(CommunicationBase.STATE_IDLE, client.getCurrentState());
		} finally {
			SocketCovSupport.stopOwnedThreads();
		}
	}

	@Test
	// backwards compatibility
	void shutdownClosesTheClientAfterSayingGoodbye() throws Exception {
		Opened opened = MapFixture.open("Child");
		OpenHook hook = new OpenHook();
		hook.setController(opened.mode);
		try (Pipe pipe = Pipe.open()) {
			ClientCommunication client = new ClientCommunication("hook-client", pipe.local, opened.mode, "secret");
			SocketCovSupport.hurry(client);
			hook.setClientCommunication(client);
			hook.startupMapHook();
			hook.shutdownMapHook();
			CollaborationGoodbye goodbye = (CollaborationGoodbye) pipe.read();
			assertEquals(Tools.getUserName(), goodbye.getUserId());
			assertTrue(pipe.local.isClosed());
			assertThrows(IOException.class, pipe::read);
		} finally {
			SocketCovSupport.stopOwnedThreads();
		}
	}

	private static final class OpenHook extends SocketConnectionHook {
		Exception failedLock(String user, ExtendedMapFeedback controller) {
			try {
				super.lock(user, controller);
				return null;
			} catch (Exception failure) {
				return failure;
			}
		}

		void cast(String doAction, String undoAction, String lockId) throws Exception {
			broadcastCommand(doAction, undoAction, lockId, null);
		}

		void release(ExtendedMapFeedback controller) {
			unlock(controller);
		}
	}
}
