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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.HeadlessException;
import java.io.IOException;
import java.net.Socket;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.controller.actions.generated.instance.CollaborationActionBase;
import freemind.controller.actions.generated.instance.CollaborationGetOffers;
import freemind.controller.actions.generated.instance.CollaborationGoodbye;
import freemind.controller.actions.generated.instance.CollaborationHello;
import freemind.controller.actions.generated.instance.CollaborationMapOffer;
import freemind.controller.actions.generated.instance.CollaborationOffers;
import freemind.controller.actions.generated.instance.CollaborationReceiveLock;
import freemind.controller.actions.generated.instance.CollaborationRequireLock;
import freemind.controller.actions.generated.instance.CollaborationTransaction;
import freemind.controller.actions.generated.instance.CollaborationUnableToLock;
import freemind.controller.actions.generated.instance.CollaborationUserInformation;
import freemind.controller.actions.generated.instance.CollaborationWelcome;
import freemind.controller.actions.generated.instance.CollaborationWhoAreYou;
import freemind.controller.actions.generated.instance.CollaborationWrongCredentials;
import freemind.controller.actions.generated.instance.CollaborationWrongMap;
import freemind.extensions.PermanentNodeHook;
import freemind.main.FreeMind;
import freemind.main.Tools;
import freemind.modes.ExtendedMapFeedback;
import freemind.modes.mindmapmode.MindMapController;
import freemind.modes.mindmapmode.MindMapMapModel;
import freemind.modes.mindmapmode.actions.xml.ActionPair;
import plugins.collaboration.socket.ClientCommunication;
import plugins.collaboration.socket.CommunicationBase;
import plugins.collaboration.socket.ServerCommunication;
import plugins.collaboration.socket.SocketConnectionHook;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;
import tests.freemind.cov.socket.SocketCovSupport.Pipe;

@DisplayName("ClientCommunicationBehaviourTest")
class ClientCommunicationBehaviourTest {

	private static final String VERSION_KEY = FreeMind.RESOURCES_USE_COLLABORATION_SERVER_WITH_DIFFERENT_VERSION;

	@Test
	// backwards compatibility
	void asksForOffersAndRemembersAVersionChoice() throws Exception {
		Opened opened = MapFixture.open("Child");
		String previous = opened.frame.getProperty(VERSION_KEY);
		try (Pipe pipe = Pipe.open()) {
			ClientCommunication client = client(pipe.local, opened.mode);
			assertEquals(pipe.local.getLocalPort(), client.getPort());
			client.processCommand(who(ServerCommunication.SERVER_VERSION));
			CollaborationGetOffers offers = (CollaborationGetOffers) pipe.read();
			assertEquals(Tools.getUserName(), offers.getUserId());
			assertEquals("secret", offers.getPassword());
			assertEquals(CommunicationBase.STATE_WAIT_FOR_OFFER, client.getCurrentState());

			client.setCurrentState(CommunicationBase.STATE_IDLE);
			client.processCommand(who(ServerCommunication.SERVER_VERSION));
			assertTrue(pipe.read() instanceof CollaborationGetOffers);
			assertEquals(CommunicationBase.STATE_WAIT_FOR_OFFER, client.getCurrentState());
			client.shutdown();
		}
		try (Pipe pipe = Pipe.open()) {
			opened.frame.setProperty(VERSION_KEY, "true");
			ClientCommunication client = client(pipe.local, opened.mode);
			client.processCommand(who("9.9"));
			assertTrue(pipe.read() instanceof CollaborationGetOffers);
			assertEquals(CommunicationBase.STATE_WAIT_FOR_OFFER, client.getCurrentState());
			client.shutdown();
		}
		try (Pipe pipe = Pipe.open()) {
			opened.frame.setProperty(VERSION_KEY, "false");
			ClientCommunication client = client(pipe.local, opened.mode);
			client.processCommand(who("9.9"));
			assertEquals(CommunicationBase.STATE_WAIT_FOR_WHO_ARE_YOU, client.getCurrentState());
			assertTrue(pipe.local.isClosed());
			assertThrows(IOException.class, pipe::read);
		}
		try (Pipe pipe = Pipe.open()) {
			opened.frame.getProperties().remove(VERSION_KEY);
			ClientCommunication client = client(pipe.local, opened.mode);
			assertThrows(HeadlessException.class, () -> client.processCommand(who("9.9")));
			assertFalse(pipe.local.isClosed());
			client.shutdown();
		} finally {
			if (previous == null) {
				opened.frame.getProperties().remove(VERSION_KEY);
			} else {
				opened.frame.setProperty(VERSION_KEY, previous);
			}
			SocketCovSupport.stopOwnedThreads();
		}
	}

	@Test
	// backwards compatibility
	void choosesTheOnlyOfferAndStopsForEveryOtherBundle() throws Exception {
		Opened opened = MapFixture.open("Child");
		try (Pipe pipe = Pipe.open()) {
			ClientCommunication client = client(pipe.local, opened.mode);
			client.setCurrentState(CommunicationBase.STATE_WAIT_FOR_OFFER);
			client.processCommand(bundle(true, "hosted.mm"));
			CollaborationHello hello = (CollaborationHello) pipe.read();
			assertEquals("hosted.mm", hello.getMap());
			assertEquals(CommunicationBase.STATE_WAIT_FOR_WELCOME, client.getCurrentState());
			client.shutdown();
		}
		try (Pipe pipe = Pipe.open()) {
			ClientCommunication client = client(pipe.local, opened.mode);
			client.processCommand(bundle(true, "hosted.mm"));
			assertTrue(pipe.read() instanceof CollaborationHello);
			assertEquals(CommunicationBase.STATE_WAIT_FOR_WELCOME, client.getCurrentState());
			client.shutdown();
		}
		try (Pipe pipe = Pipe.open()) {
			ClientCommunication client = client(pipe.local, opened.mode);
			client.setCurrentState(CommunicationBase.STATE_WAIT_FOR_OFFER);
			client.processCommand(bundle(true));
			assertTrue(pipe.local.isClosed());
			assertEquals(CommunicationBase.STATE_WAIT_FOR_OFFER, client.getCurrentState());
			assertThrows(IOException.class, pipe::read);
		}
		try (Pipe pipe = Pipe.open()) {
			ClientCommunication client = client(pipe.local, opened.mode);
			client.setCurrentState(CommunicationBase.STATE_WAIT_FOR_OFFER);
			assertThrows(HeadlessException.class, () -> client.processCommand(bundle(false, "one.mm", "two.mm")));
			assertFalse(pipe.local.isClosed());
			assertEquals(CommunicationBase.STATE_WAIT_FOR_OFFER, client.getCurrentState());
			client.shutdown();
		} finally {
			SocketCovSupport.stopOwnedThreads();
		}
	}

	@Test
	// backwards compatibility
	void closesAfterRejectedCredentialsOrARejectedMap() throws Exception {
		Opened opened = MapFixture.open("Child");
		try (Pipe pipe = Pipe.open()) {
			ClientCommunication client = client(pipe.local, opened.mode);
			client.setCurrentState(CommunicationBase.STATE_WAIT_FOR_OFFER);
			assertThrows(HeadlessException.class, () -> client.processCommand(new CollaborationWrongCredentials()));
			assertTrue(pipe.local.isClosed());
		}
		try (Pipe pipe = Pipe.open()) {
			ClientCommunication client = client(pipe.local, opened.mode);
			client.setCurrentState(CommunicationBase.STATE_IDLE);
			assertThrows(HeadlessException.class, () -> client.processCommand(new CollaborationWrongMap()));
			assertTrue(pipe.local.isClosed());
		} finally {
			SocketCovSupport.stopOwnedThreads();
		}
	}

	@Test
	// backwards compatibility
	void appliesAForeignTransactionAndKeepsItsOwnLock() throws Exception {
		Opened opened = MapFixture.open("Child");
		CountingHook hook = new CountingHook();
		hook.setController(opened.mode);
		opened.root.invokeHook(hook);
		try (Pipe pipe = Pipe.open()) {
			OpenClient client = new OpenClient(pipe.local, opened.mode);
			client.attach();
			assertSame(client, hook.getClientCommunication());
			CollaborationUserInformation info = new CollaborationUserInformation();
			info.setUserIds("ada");
			info.setMasterHostname("localhost");
			info.setMasterIp("127.0.0.1");
			info.setMasterPort(4242);
			client.processCommand(info);
			assertSame(info, client.getUserInfo());

			CollaborationTransaction foreign = new CollaborationTransaction();
			foreign.setId("from-server");
			foreign.setDoAction(Tools.marshall(new freemind.controller.actions.generated.instance.CompoundAction()));
			foreign.setUndoAction(foreign.getDoAction());
			client.processCommand(foreign);
			assertEquals(1, hook.applied);

			CollaborationReceiveLock lock = new CollaborationReceiveLock();
			lock.setId("mine");
			client.processCommand(lock);
			assertEquals(CommunicationBase.STATE_LOCK_RECEIVED, client.getCurrentState());
			CollaborationTransaction own = new CollaborationTransaction();
			own.setId("mine");
			own.setDoAction(foreign.getDoAction());
			own.setUndoAction(foreign.getUndoAction());
			client.processCommand(own);
			assertEquals(1, hook.applied);

			client.processCommand(new CollaborationUnableToLock());
			assertEquals(CommunicationBase.STATE_IDLE, client.getCurrentState());
			client.processCommand(new CollaborationActionBase());
			assertEquals(CommunicationBase.STATE_IDLE, client.getCurrentState());
			client.shutdown();
		} finally {
			SocketCovSupport.stopOwnedThreads();
		}
	}

	@Test
	// backwards compatibility
	void welcomeOpensTheOfferedMapEvenFromTheWrongState() throws Exception {
		Opened opened = MapFixture.open("Child");
		MindMapController original = opened.mode;
		MindMapController created = null;
		MindMapController second = null;
		try {
			try (Pipe pipe = Pipe.open()) {
				ClientCommunication client = client(pipe.local, original);
				client.setCurrentState(CommunicationBase.STATE_WAIT_FOR_WELCOME);
				leaveRoomForAnotherMap(opened);
				client.processCommand(welcome(SocketCovSupport.MAP));
				created = (MindMapController) client.getController();
				assertNotSame(original, created);
				assertEquals("ROOT", created.getMap().getRootNode().getText());
				assertEquals(CommunicationBase.STATE_IDLE, client.getCurrentState());
				assertSame(client, slave(created));
				client.shutdown();
			}
			try (Pipe pipe = Pipe.open()) {
				ClientCommunication client = client(pipe.local, original);
				leaveRoomForAnotherMap(opened);
				client.processCommand(welcome(SocketCovSupport.MAP));
				second = (MindMapController) client.getController();
				assertNotSame(original, second);
				assertEquals("ROOT", second.getMap().getRootNode().getText());
				assertEquals(CommunicationBase.STATE_IDLE, client.getCurrentState());
				client.shutdown();
			}
		} finally {
			releaseMap(created);
			releaseMap(second);
			SocketCovSupport.stopOwnedThreads();
		}
	}

	@Test
	// backwards compatibility
	void goodbyeStaysQuietAndShutdownSendsOne() throws Exception {
		Opened opened = MapFixture.open("Child");
		try (Pipe pipe = Pipe.open()) {
			ClientCommunication client = client(pipe.local, opened.mode);
			CollaborationGoodbye goodbye = new CollaborationGoodbye();
			goodbye.setUserId("server");
			client.processCommand(goodbye);
			assertTrue(pipe.local.isClosed());
			assertThrows(IOException.class, pipe::read);
		}
		try (Pipe pipe = Pipe.open()) {
			ClientCommunication client = client(pipe.local, opened.mode);
			client.shutdown();
			CollaborationGoodbye goodbye = (CollaborationGoodbye) pipe.read();
			assertEquals(Tools.getUserName(), goodbye.getUserId());
			assertTrue(pipe.local.isClosed());
		} finally {
			SocketCovSupport.stopOwnedThreads();
		}
	}

	@Test
	// backwards compatibility
	void lockRequestReturnsTheIdTheServerAlreadyGranted() throws Exception {
		Opened opened = MapFixture.open("Child");
		try (Pipe pipe = Pipe.open()) {
			PromptClient client = new PromptClient(pipe.local, opened.mode);
			assertEquals("lock-fast", client.granted());
			assertEquals(CommunicationBase.STATE_IDLE, client.getCurrentState());
			assertTrue(pipe.read() instanceof CollaborationRequireLock);
			client.shutdown();
		}
		try (Pipe pipe = Pipe.open()) {
			OpenClient client = new OpenClient(pipe.local, opened.mode);
			pipe.local.close();
			Exception failure = client.failedRequest();
			assertTrue(SocketCovSupport.lockFailure(failure));
			assertEquals(CommunicationBase.STATE_IDLE, client.getCurrentState());
		} finally {
			SocketCovSupport.stopOwnedThreads();
		}
	}

	private static ClientCommunication client(Socket socket, MindMapController controller) throws IOException {
		ClientCommunication client = new ClientCommunication("coverage-client", socket, controller, "secret");
		SocketCovSupport.hurry(client);
		return client;
	}

	private static CollaborationWhoAreYou who(String version) {
		CollaborationWhoAreYou command = new CollaborationWhoAreYou();
		command.setServerVersion(version);
		return command;
	}

	private static CollaborationOffers bundle(boolean single, String... names) {
		CollaborationOffers offers = new CollaborationOffers();
		offers.setIsSingleOffer(Boolean.valueOf(single));
		for (String name : names) {
			CollaborationMapOffer offer = new CollaborationMapOffer();
			offer.setMap(name);
			offers.addCollaborationMapOffer(offer);
		}
		return offers;
	}

	private static CollaborationWelcome welcome(String map) {
		CollaborationWelcome command = new CollaborationWelcome();
		command.setMap(map);
		command.setFilename("hosted.mm");
		return command;
	}

	private static void leaveRoomForAnotherMap(Opened opened) {
		Object manager = opened.app.getMapModuleManager();
		SocketCovSupport.set(manager, "mapModule", null);
		SocketCovSupport.set(manager, "mCurrentMode", null);
	}

	private static void releaseMap(MindMapController controller) {
		if (controller != null && controller.getMap() instanceof MindMapMapModel) {
			((MindMapMapModel) controller.getMap()).destroy();
		}
	}

	private static ClientCommunication slave(MindMapController controller) {
		for (PermanentNodeHook hook : controller.getRootNode().getActivatedHooks()) {
			if (hook instanceof SocketConnectionHook) {
				return ((SocketConnectionHook) hook).getClientCommunication();
			}
		}
		return null;
	}

	private static final class CountingHook extends SocketConnectionHook {
		int applied;

		@Override
		protected void executeTransaction(ActionPair pair, ExtendedMapFeedback controller) {
			applied++;
		}
	}

	private static final class OpenClient extends ClientCommunication {
		OpenClient(Socket socket, MindMapController controller) throws IOException {
			super("coverage-client", socket, controller, "secret");
			mSleepTime = 1;
		}

		void attach() {
			registerClientCommunicationAtHook();
		}

		Exception failedRequest() {
			try {
				sendLockRequest();
				return null;
			} catch (Exception failure) {
				return failure;
			}
		}
	}

	private static final class PromptClient extends ClientCommunication {
		PromptClient(Socket socket, MindMapController controller) throws IOException {
			super("coverage-client", socket, controller, "secret");
			mSleepTime = 1;
		}

		String granted() throws Exception {
			return sendLockRequest();
		}

		@Override
		public synchronized boolean send(CollaborationActionBase command) {
			if (command instanceof CollaborationRequireLock) {
				CollaborationReceiveLock lock = new CollaborationReceiveLock();
				lock.setId("lock-fast");
				try {
					processCommand(lock);
				} catch (IOException failure) {
					throw new IllegalStateException(failure);
				}
			}
			return super.send(command);
		}
	}
}
