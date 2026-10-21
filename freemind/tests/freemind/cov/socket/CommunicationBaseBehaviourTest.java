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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.controller.actions.generated.instance.CollaborationActionBase;
import freemind.controller.actions.generated.instance.CollaborationTransaction;
import freemind.controller.actions.generated.instance.CollaborationUserInformation;
import freemind.controller.actions.generated.instance.CollaborationWhoAreYou;
import freemind.controller.actions.generated.instance.CompoundAction;
import freemind.main.Tools;
import freemind.modes.ExtendedMapFeedback;
import freemind.modes.ExtendedMapFeedbackImpl;
import freemind.modes.mindmapmode.actions.xml.ActionPair;
import plugins.collaboration.socket.CommunicationBase;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;
import tests.freemind.cov.socket.SocketCovSupport.Pipe;

@DisplayName("CommunicationBaseBehaviourTest")
class CommunicationBaseBehaviourTest {

	@Test
	// backwards compatibility
	void namesEveryCollaborationState() throws Exception {
		try (Pipe pipe = Pipe.open()) {
			Probe probe = probe(pipe, new ExtendedMapFeedbackImpl());
			assertEquals("STATE_IDLE", probe.label(CommunicationBase.STATE_IDLE));
			assertEquals("STATE_WAIT_FOR_HELLO", probe.label(CommunicationBase.STATE_WAIT_FOR_HELLO));
			assertEquals("STATE_WAIT_FOR_COMMAND", probe.label(CommunicationBase.STATE_WAIT_FOR_COMMAND));
			assertEquals("STATE_WAIT_FOR_WHO_ARE_YOU", probe.label(CommunicationBase.STATE_WAIT_FOR_WHO_ARE_YOU));
			assertEquals("STATE_WAIT_FOR_WELCOME", probe.label(CommunicationBase.STATE_WAIT_FOR_WELCOME));
			assertEquals("STATE_WAIT_FOR_LOCK", probe.label(CommunicationBase.STATE_WAIT_FOR_LOCK));
			assertEquals("STATE_LOCK_RECEIVED", probe.label(CommunicationBase.STATE_LOCK_RECEIVED));
			assertEquals("STATE_WAIT_FOR_OFFER", probe.label(CommunicationBase.STATE_WAIT_FOR_OFFER));
			assertEquals("STATE_WAIT_FOR_GET_OFFERS", probe.label(CommunicationBase.STATE_WAIT_FOR_GET_OFFERS));
			assertEquals("UNKNOWN: 42", probe.label(42));
			probe.setCurrentState(CommunicationBase.STATE_WAIT_FOR_HELLO);
			probe.reportWrong(new CollaborationWhoAreYou());
			assertEquals(CommunicationBase.STATE_WAIT_FOR_HELLO, probe.getCurrentState());
		}
	}

	@Test
	// backwards compatibility
	void sendsACommandAndRebuildsTheActionPair() throws Exception {
		try (Pipe pipe = Pipe.open()) {
			Probe probe = probe(pipe, new ExtendedMapFeedbackImpl());
			String doXml = Tools.marshall(new CompoundAction());
			String undoXml = Tools.marshall(new CompoundAction());
			probe.sendCommand(doXml, undoXml, "lock-7");
			CollaborationTransaction sent = (CollaborationTransaction) pipe.read();
			assertEquals("lock-7", sent.getId());
			assertEquals(doXml, sent.getDoAction());
			assertEquals(undoXml, sent.getUndoAction());

			CollaborationTransaction stored = new CollaborationTransaction();
			stored.setDoAction(Tools.marshall(new CompoundAction()));
			stored.setUndoAction(Tools.marshall(new CompoundAction()));
			stored.setId("pair");
			ActionPair pair = probe.getActionPair(stored);
			assertNotNull(pair.getDoAction());
			assertTrue(pair.getUndoAction() instanceof CompoundAction);
			assertEquals("127.0.0.1", probe.getIpToSocket());

			ExtendedMapFeedbackImpl replacement = new ExtendedMapFeedbackImpl();
			probe.use(replacement);
			assertSame(replacement, probe.getController());
			probe.close();
			assertTrue(pipe.local.isClosed());
			assertFalse(probe.send(new CollaborationWhoAreYou()));
		}
	}

	@Test
	// backwards compatibility
	void splitsACommandThatDoesNotFitInOneUtfFrame() throws Exception {
		try (Pipe pipe = Pipe.open()) {
			Probe probe = probe(pipe, new ExtendedMapFeedbackImpl());
			CollaborationUserInformation bulky = new CollaborationUserInformation();
			bulky.setUserIds(incompressible(90000));
			bulky.setMasterHostname("localhost");
			bulky.setMasterIp("127.0.0.1");
			bulky.setMasterPort(1);
			assertTrue(Tools.compress(Tools.marshall(bulky)).length() > 65500);
			assertTrue(probe.send(bulky));
			CollaborationUserInformation received = (CollaborationUserInformation) pipe.read();
			assertEquals(bulky.getUserIds(), received.getUserIds());
			assertEquals(1, received.getMasterPort());
		}
	}

	@Test
	// backwards compatibility
	void readsASplitFrameAndRunsATransactionOnTheEventQueue() throws Exception {
		try (Pipe pipe = Pipe.open()) {
			Probe probe = probe(pipe, new ExtendedMapFeedbackImpl());
			CollaborationWhoAreYou who = new CollaborationWhoAreYou();
			who.setServerVersion("2.1");
			String compressed = Tools.compress(Tools.marshall(who));
			assertTrue(compressed.length() > 8);
			pipe.writeRaw(compressed.substring(0, 8) + SocketCovSupport.CONTINUATION);
			assertTrue(probe.processAction());
			assertNull(probe.last);
			pipe.writeRaw(compressed.substring(8));
			assertTrue(probe.processAction());
			assertEquals("2.1", ((CollaborationWhoAreYou) probe.last).getServerVersion());

			CollaborationTransaction transaction = new CollaborationTransaction();
			transaction.setId("tx");
			transaction.setDoAction(Tools.marshall(new CompoundAction()));
			transaction.setUndoAction(Tools.marshall(new CompoundAction()));
			pipe.write(transaction);
			assertTrue(probe.processAction());
			SwingUtilities.invokeAndWait(() -> {
			});
			assertEquals("tx", ((CollaborationTransaction) probe.last).getId());

			pipe.writeRaw(Tools.compress("not-xml"));
			assertFalse(probe.processAction());
		}
	}

	@Test
	// backwards compatibility
	void aBrokenCommandIsLoggedAndAQuietSocketRefreshesTheTitle() throws Exception {
		Opened opened = MapFixture.open("Child");
		try (Pipe pipe = Pipe.open()) {
			Probe probe = probe(pipe, opened.mode);
			probe.blow = true;
			pipe.write(new CollaborationWhoAreYou());
			assertTrue(probe.processAction());
			SwingUtilities.invokeAndWait(() -> {
			});
			assertNull(probe.last);

			pipe.local.setSoTimeout(40);
			assertFalse(probe.processAction());
			assertEquals(9, ((Number) SocketCovSupport.get(probe, "mCounter")).intValue());
		}
	}

	@Test
	// backwards compatibility
	void aLongReadFailureClosesTheSocketFromASideThread() throws Exception {
		try (Pipe pipe = Pipe.open()) {
			Probe probe = probe(pipe, new ExtendedMapFeedbackImpl());
			SocketCovSupport.set(probe, "mConnectionErrorOccuredEarlier", Boolean.TRUE);
			SocketCovSupport.set(probe, "mFirstErrorOnConnection",
					Long.valueOf(System.currentTimeMillis() - 31_000L));
			pipe.remote.shutdownOutput();
			assertFalse(probe.processAction());
			SocketCovSupport.waitFor(() -> probe.terminated == 1);
			assertEquals(1, probe.terminated);
			assertTrue(pipe.local.isClosed());
		} finally {
			SocketCovSupport.stopOwnedThreads();
		}
	}

	private static Probe probe(Pipe pipe, ExtendedMapFeedback controller) throws IOException {
		Probe probe = new Probe(pipe, controller);
		SocketCovSupport.hurry(probe);
		return probe;
	}

	private static String incompressible(int length) {
		StringBuilder text = new StringBuilder(length);
		Random random = new Random(1);
		for (int i = 0; i < length; i++) {
			text.append((char) (32 + random.nextInt(95)));
		}
		return text.toString();
	}

	private static final class Probe extends CommunicationBase {
		CollaborationActionBase last;
		boolean blow;
		int terminated;

		Probe(Pipe pipe, ExtendedMapFeedback controller) throws IOException {
			super("coverage-base", pipe.local, controller, new DataOutputStream(pipe.local.getOutputStream()),
					new DataInputStream(pipe.local.getInputStream()));
		}

		public void terminateSocket() {
			terminated++;
			try {
				close();
			} catch (IOException ignored) {
				// Closing a socket that the peer already shut is still success.
			}
		}

		public void processCommand(CollaborationActionBase command) {
			if (blow) {
				throw new IllegalStateException("boom");
			}
			last = command;
		}

		String label(int state) {
			return printState(state);
		}

		void reportWrong(CollaborationActionBase command) {
			printWrongState(command);
		}

		void use(ExtendedMapFeedback controller) {
			setController(controller);
		}
	}
}
