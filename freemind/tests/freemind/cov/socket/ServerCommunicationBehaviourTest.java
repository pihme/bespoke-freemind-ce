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

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.controller.actions.generated.instance.CollaborationActionBase;
import freemind.controller.actions.generated.instance.CollaborationGetOffers;
import freemind.controller.actions.generated.instance.CollaborationGoodbye;
import freemind.controller.actions.generated.instance.CollaborationHello;
import freemind.controller.actions.generated.instance.CollaborationOffers;
import freemind.controller.actions.generated.instance.CollaborationPublishNewMap;
import freemind.controller.actions.generated.instance.CollaborationReceiveLock;
import freemind.controller.actions.generated.instance.CollaborationRequireLock;
import freemind.controller.actions.generated.instance.CollaborationTransaction;
import freemind.controller.actions.generated.instance.CollaborationUnableToLock;
import freemind.controller.actions.generated.instance.CollaborationWelcome;
import freemind.controller.actions.generated.instance.CollaborationWhoAreYou;
import freemind.controller.actions.generated.instance.CollaborationWrongCredentials;
import freemind.controller.actions.generated.instance.CollaborationWrongMap;
import freemind.controller.actions.generated.instance.CompoundAction;
import freemind.main.Tools;
import freemind.modes.ExtendedMapFeedback;
import freemind.modes.mindmapmode.MindMapMapModel;
import plugins.collaboration.socket.CommunicationBase;
import plugins.collaboration.socket.ServerCommunication;
import tests.freemind.cov.socket.SocketCovSupport.Pipe;

@DisplayName("ServerCommunicationBehaviourTest")
class ServerCommunicationBehaviourTest {

	@Test
	// backwards compatibility
	void listsHostedMapsOnlyForTheRightPasswordAndMoment() throws Exception {
		try (Link link = Link.open(false, "hosted.mm")) {
			link.comm.processCommand(offers("ada", "secret"));
			assertEquals("ada", link.comm.getName());
			CollaborationOffers listed = (CollaborationOffers) link.pipe.read();
			assertFalse(listed.getIsSingleOffer());
			assertEquals("hosted.mm", listed.getCollaborationMapOffer(0).getMap());
			assertEquals(CommunicationBase.STATE_WAIT_FOR_HELLO, link.comm.getCurrentState());
		}
		try (Link link = Link.open(true, "hosted.mm")) {
			CollaborationOffers listed = (CollaborationOffers) answer(link, offers("ada", "secret"));
			assertTrue(listed.getIsSingleOffer());
			assertEquals(1, listed.getListCollaborationMapOfferList().size());
		}
		try (Link link = Link.open(false, "hosted.mm")) {
			link.comm.processCommand(offers("ada", "nope"));
			assertTrue(link.pipe.read() instanceof CollaborationWrongCredentials);
			assertTrue(link.pipe.local.isClosed());
			assertEquals(CommunicationBase.STATE_WAIT_FOR_GET_OFFERS, link.comm.getCurrentState());
		}
		try (Link link = Link.open(false, "hosted.mm")) {
			link.comm.setCurrentState(CommunicationBase.STATE_IDLE);
			link.pipe.remote.setSoTimeout(300);
			link.comm.processCommand(offers("ada", "secret"));
			assertEquals(CommunicationBase.STATE_IDLE, link.comm.getCurrentState());
			assertThrows(SocketTimeoutException.class, link.pipe::read);
		}
	}

	@Test
	// backwards compatibility
	void publishesAMapUnlessTheNamePasswordOrServerForbidsIt() throws Exception {
		try (Link link = Link.open(false, null)) {
			link.comm.processCommand(publish("ada", "secret", "fresh.mm"));
			assertEquals(CommunicationBase.STATE_IDLE, link.comm.getCurrentState());
			assertTrue(link.master.getUsers(link.master.getFileMap().get("fresh.mm")).contains("ada"));
			String stored = Files.readString(new File(link.dir, "fresh.mm").toPath());
			assertTrue(stored.contains("ROOT"));
		}
		try (Link link = Link.open(false, null)) {
			link.comm.processCommand(publish("ada", "nope", "fresh.mm"));
			assertTrue(link.pipe.read() instanceof CollaborationWrongCredentials);
			assertTrue(link.pipe.local.isClosed());
			assertFalse(new File(link.dir, "fresh.mm").exists());
			assertEquals(CommunicationBase.STATE_WAIT_FOR_GET_OFFERS, link.comm.getCurrentState());
		}
		try (Link link = Link.open(false, null)) {
			assertThrows(IOException.class, () -> link.comm.processCommand(publish("ada", "secret", "nested/evil.mm")));
			assertTrue(link.pipe.read() instanceof CollaborationWrongMap);
			assertTrue(link.master.getFileMap().containsKey("nested/evil.mm"));
			assertEquals(CommunicationBase.STATE_WAIT_FOR_GET_OFFERS, link.comm.getCurrentState());
			assertFalse(new File(link.dir, "nested/evil.mm").exists());
		}
		try (Link link = Link.open(false, "same.mm")) {
			link.comm.processCommand(publish("ada", "secret", "same.mm"));
			assertTrue(link.pipe.read() instanceof CollaborationWrongMap);
			assertEquals(CommunicationBase.STATE_IDLE, link.comm.getCurrentState());
			assertTrue(Files.readString(new File(link.dir, "same.mm").toPath()).contains("ROOT"));
		}
		try (Link link = Link.open(true, "hosted.mm")) {
			link.comm.processCommand(publish("ada", "secret", "another.mm"));
			assertEquals(CommunicationBase.STATE_WAIT_FOR_GET_OFFERS, link.comm.getCurrentState());
			assertFalse(link.master.getFileMap().containsKey("another.mm"));
			link.pipe.remote.setSoTimeout(300);
			assertThrows(SocketTimeoutException.class, link.pipe::read);
		}
	}

	@Test
	// backwards compatibility
	void welcomesAKnownMapAndStillLooksUpAnUnknownOne() throws Exception {
		try (Link link = Link.open(false, "hosted.mm")) {
			link.comm.setCurrentState(CommunicationBase.STATE_WAIT_FOR_HELLO);
			CollaborationHello hello = new CollaborationHello();
			hello.setMap("hosted.mm");
			link.comm.processCommand(hello);
			CollaborationWelcome welcome = (CollaborationWelcome) link.pipe.read();
			assertTrue(welcome.getMap().contains("ROOT"));
			assertEquals("hosted.mm", welcome.getFilename());
			assertEquals(CommunicationBase.STATE_IDLE, link.comm.getCurrentState());
			assertTrue(link.master.getUsers(link.map).contains(link.comm.getName()));
		}
		try (Link link = Link.open(false, "hosted.mm")) {
			link.comm.setCurrentState(CommunicationBase.STATE_WAIT_FOR_HELLO);
			CollaborationHello hello = new CollaborationHello();
			hello.setMap("missing.mm");
			assertThrows(NullPointerException.class, () -> link.comm.processCommand(hello));
			assertTrue(link.pipe.read() instanceof CollaborationWrongMap);
			assertEquals(CommunicationBase.STATE_WAIT_FOR_HELLO, link.comm.getCurrentState());
			assertTrue(link.pipe.local.isClosed());
		}
		try (Link link = Link.open(false, "hosted.mm")) {
			CollaborationHello hello = new CollaborationHello();
			hello.setMap("hosted.mm");
			link.comm.processCommand(hello);
			assertEquals(CommunicationBase.STATE_WAIT_FOR_GET_OFFERS, link.comm.getCurrentState());
			link.pipe.remote.setSoTimeout(300);
			assertThrows(SocketTimeoutException.class, link.pipe::read);
		}
	}

	@Test
	// backwards compatibility
	void appliesATransactionThatHoldsTheLock() throws Exception {
		String doXml = Tools.marshall(new CompoundAction());
		String undoXml = Tools.marshall(new CompoundAction());
		try (Link link = Link.open(false, "hosted.mm")) {
			String lockId = link.master.takeLock("ada", link.map);
			link.master.addConnection(link.comm, link.map);
			link.comm.setCurrentState(CommunicationBase.STATE_WAIT_FOR_COMMAND);
			link.comm.processCommand(transaction(lockId, doXml, undoXml));
			CollaborationTransaction broadcast = (CollaborationTransaction) link.pipe.read();
			assertEquals(lockId, broadcast.getId());
			assertEquals(doXml, broadcast.getDoAction());
			assertEquals(CommunicationBase.STATE_IDLE, link.comm.getCurrentState());
			assertThrows(IllegalStateException.class, () -> link.master.getLockId(link.map));
		}
		try (Link link = Link.open(false, "hosted.mm")) {
			link.master.takeLock("ada", link.map);
			link.comm.setCurrentState(CommunicationBase.STATE_WAIT_FOR_COMMAND);
			link.pipe.remote.setSoTimeout(300);
			link.comm.processCommand(transaction("other", doXml, undoXml));
			assertThrows(SocketTimeoutException.class, link.pipe::read);
			assertEquals(CommunicationBase.STATE_IDLE, link.comm.getCurrentState());
			assertThrows(IllegalStateException.class, () -> link.master.getLockId(link.map));
		}
		try (Link link = Link.open(false, "hosted.mm")) {
			link.comm.setCurrentState(CommunicationBase.STATE_WAIT_FOR_COMMAND);
			assertThrows(IllegalStateException.class,
					() -> link.comm.processCommand(transaction("none", doXml, undoXml)));
			assertEquals(CommunicationBase.STATE_WAIT_FOR_COMMAND, link.comm.getCurrentState());
		}
		try (Link link = Link.open(false, "hosted.mm")) {
			link.comm.setCurrentState(CommunicationBase.STATE_IDLE);
			link.pipe.remote.setSoTimeout(300);
			link.comm.processCommand(transaction("none", doXml, undoXml));
			assertEquals(CommunicationBase.STATE_IDLE, link.comm.getCurrentState());
			assertThrows(SocketTimeoutException.class, link.pipe::read);
		}
	}

	@Test
	// backwards compatibility
	void grantsALockAndReportsWhenTheMapIsAlreadyLocked() throws Exception {
		try (Link link = Link.open(false, "hosted.mm")) {
			link.comm.setCurrentState(CommunicationBase.STATE_IDLE);
			link.comm.setName("ada");
			link.comm.processCommand(new CollaborationRequireLock());
			CollaborationReceiveLock received = (CollaborationReceiveLock) link.pipe.read();
			assertEquals(link.master.getLockId(link.map), received.getId());
			assertTrue(received.getId().startsWith("Lock_"));
			assertEquals(CommunicationBase.STATE_WAIT_FOR_COMMAND, link.comm.getCurrentState());

			link.master.release(link.map);
			link.comm.setCurrentState(CommunicationBase.STATE_WAIT_FOR_GET_OFFERS);
			link.comm.processCommand(new CollaborationRequireLock());
			CollaborationReceiveLock again = (CollaborationReceiveLock) link.pipe.read();
			assertEquals(again.getId(), link.master.getLockId(link.map));
			assertEquals(CommunicationBase.STATE_WAIT_FOR_COMMAND, link.comm.getCurrentState());

			link.comm.processCommand(new CollaborationRequireLock());
			assertTrue(link.pipe.read() instanceof CollaborationUnableToLock);
			assertEquals(CommunicationBase.STATE_WAIT_FOR_COMMAND, link.comm.getCurrentState());
			assertEquals(again.getId(), link.master.getLockId(link.map));
		}
		try (Link link = Link.open(false, "hosted.mm")) {
			link.master.interruptLock = true;
			link.comm.processCommand(new CollaborationRequireLock());
			assertEquals(CommunicationBase.STATE_WAIT_FOR_GET_OFFERS, link.comm.getCurrentState());
			link.pipe.remote.setSoTimeout(300);
			assertThrows(SocketTimeoutException.class, link.pipe::read);
			assertThrows(IllegalStateException.class, () -> link.master.getLockId(link.map));
		}
		try (Link link = Link.open(false, "hosted.mm")) {
			link.comm.setCurrentState(CommunicationBase.STATE_IDLE);
			SocketCovSupport.set(link.comm, "out", null);
			assertThrows(NullPointerException.class, () -> link.comm.processCommand(new CollaborationRequireLock()));
			assertEquals(CommunicationBase.STATE_IDLE, link.comm.getCurrentState());
			assertThrows(IllegalStateException.class, () -> link.master.getLockId(link.map));
		}
	}

	@Test
	// backwards compatibility
	void goodbyeRemovesTheClientAndUnknownCommandsStayPut() throws Exception {
		try (Link link = Link.open(false, "hosted.mm")) {
			int state = link.comm.getCurrentState();
			link.comm.processCommand(new CollaborationActionBase());
			assertEquals(state, link.comm.getCurrentState());
			link.master.addConnection(link.comm, link.map);
			int titles = link.master.titles;
			CollaborationGoodbye goodbye = new CollaborationGoodbye();
			goodbye.setUserId("ada");
			link.comm.processCommand(goodbye);
			assertTrue(link.master.titles > titles);
			assertEquals(Tools.getUserName(), link.master.getUsers(link.map));
			assertTrue(link.pipe.local.isClosed());
			assertEquals(ServerCommunication.SERVER_VERSION, "2.1");
		}
	}

	private static CollaborationGetOffers offers(String user, String password) {
		CollaborationGetOffers command = new CollaborationGetOffers();
		command.setUserId(user);
		command.setPassword(password);
		return command;
	}

	private static CollaborationPublishNewMap publish(String user, String password, String name) {
		CollaborationPublishNewMap command = new CollaborationPublishNewMap();
		command.setUserId(user);
		command.setPassword(password);
		command.setMapName(name);
		command.setMap(SocketCovSupport.MAP);
		return command;
	}

	private static CollaborationTransaction transaction(String id, String doXml, String undoXml) {
		CollaborationTransaction command = new CollaborationTransaction();
		command.setId(id);
		command.setDoAction(doXml);
		command.setUndoAction(undoXml);
		return command;
	}

	private static CollaborationActionBase answer(Link link, CollaborationActionBase command) throws Exception {
		link.comm.processCommand(command);
		return link.pipe.read();
	}

	private static final class Link implements AutoCloseable {
		final File dir;
		final TestMaster master;
		final Pipe pipe;
		final ServerCommunication comm;
		final ExtendedMapFeedback map;

		static Link open(boolean single, String hosted) throws Exception {
			return new Link(new TestMaster(), single, hosted);
		}

		Link(TestMaster master, boolean single, String hosted) throws Exception {
			dir = Files.createTempDirectory("socket-server").toFile();
			this.master = master;
			master.base = dir;
			master.setPassword("secret");
			if (hosted == null) {
				map = null;
			} else {
				map = SocketCovSupport.hostMap(master, dir, hosted);
			}
			pipe = Pipe.open();
			comm = SocketCovSupport.server(master, pipe.local, map, single);
			CollaborationActionBase who = pipe.read();
			assertTrue(who instanceof CollaborationWhoAreYou);
			assertEquals(ServerCommunication.SERVER_VERSION, ((CollaborationWhoAreYou) who).getServerVersion());
		}

		public void close() throws IOException {
			try {
				if (!pipe.local.isClosed()) {
					comm.terminateSocket();
				}
			} catch (IOException ignored) {
				// The command under test may already have closed it.
			}
			pipe.close();
			for (ExtendedMapFeedback held : master.getFileMap().values()) {
				if (held.getMap() instanceof MindMapMapModel) {
					((MindMapMapModel) held.getMap()).destroy();
				}
			}
			SocketCovSupport.stopOwnedThreads();
		}
	}
}
