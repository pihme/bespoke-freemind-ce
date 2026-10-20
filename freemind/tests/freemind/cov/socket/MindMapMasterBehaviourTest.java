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
import java.io.DataInputStream;
import java.io.File;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

import freemind.controller.actions.generated.instance.CollaborationGoodbye;
import freemind.controller.actions.generated.instance.CollaborationUserInformation;
import freemind.controller.actions.generated.instance.CollaborationWhoAreYou;
import freemind.extensions.PermanentNodeHook;
import freemind.main.Tools;
import freemind.main.XMLElement;
import freemind.modes.ExtendedMapFeedback;
import freemind.modes.mindmapmode.MindMapMapModel;
import plugins.collaboration.socket.MindMapMaster;
import plugins.collaboration.socket.ServerCommunication;
import plugins.collaboration.socket.TerminateableThread;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;
import tests.freemind.cov.socket.SocketCovSupport.Pipe;

class MindMapMasterBehaviourTest {

	@Test
	// backwards compatibility
	void startupNeedsADisplayUnlessTheMasterIsAlreadyRunning() throws Exception {
		Opened opened = MapFixture.open("Child");
		OpenMaster master = new OpenMaster();
		master.setController(opened.mode);
		assertEquals(Integer.valueOf(0), master.getRole());
		IllegalArgumentException base = assertThrows(IllegalArgumentException.class, master::base);
		assertTrue(base.getMessage().contains("base"));
		master.retitle();
		assertEquals(1, master.titles);
		XMLElement xml = new XMLElement();
		master.loadFrom(xml);
		master.save(xml);
		assertEquals(0, xml.countChildren());
		StringWriter html = new StringWriter();
		master.saveHtml(html);
		assertEquals("", html.toString());
		master.onAddChild(opened.root);
		master.onAddChildren(opened.root);
		master.onLostFocusNode(null);
		master.onNewChild(opened.root);
		master.onRemoveChild(opened.root);
		master.onRemoveChildren(opened.root, opened.root);
		master.onFocusNode(null);
		master.onUpdateChildrenHook(opened.root);
		master.onUpdateNodeHook();
		master.onViewCreatedHook(null);
		master.onViewRemovedHook(null);
		master.processUnfinishedLinks();
		assertEquals("ROOT", opened.root.getText());

		SocketCovSupport.set(master, "mMasterStarted", Boolean.TRUE);
		master.startupMapHook();
		assertNull(SocketCovSupport.get(master, "mServer"));
		assertEquals(0, master.getPort());

		SocketCovSupport.set(master, "mMasterStarted", Boolean.FALSE);
		assertThrows(HeadlessException.class, master::startupMapHook);
		assertFalse((Boolean) SocketCovSupport.get(master, "mMasterStarted"));
		assertNull(SocketCovSupport.get(master, "mServer"));

		File saved = File.createTempFile("socket-master", ".mm");
		((MindMapMapModel) opened.mode.getMap()).setFile(saved);
		assertThrows(HeadlessException.class, master::startupMapHook);
		assertNull(SocketCovSupport.get(master, "mListener"));
		assertEquals(0, master.getPort());

		assertTrue(opened.mode.getHookFactory().getPossibleNodeHooks().contains(MindMapMaster.LABEL));
		master.switchMeOff();
		SwingUtilities.invokeAndWait(() -> assertNull(SocketCovSupport.get(master, "mListener")));
		assertFalse(hostsMaster(opened));
		IllegalArgumentException missingSession = assertThrows(IllegalArgumentException.class,
				master::shutdownMapHook);
		assertTrue(missingSession.getMessage().contains("not present"));
		assertFalse((Boolean) SocketCovSupport.get(master, "mMasterStarted"));
	}

	@Test
	// backwards compatibility
	void shutdownTellsEveryClientTheSessionIsOver() throws Exception {
		Opened opened = MapFixture.open("Child");
		OpenMaster master = new OpenMaster();
		master.setController(opened.mode);
		master.remember("child.mm", opened.mode);
		ServerSocket server = new ServerSocket();
		server.setReuseAddress(true);
		server.bind(new InetSocketAddress("127.0.0.1", 0));
		server.setSoTimeout(200);
		try (Pipe pipe = Pipe.open()) {
			ServerCommunication comm = SocketCovSupport.server(master, pipe.local, opened.mode, true);
			assertTrue(pipe.read() instanceof CollaborationWhoAreYou);
			master.addConnection(comm, opened.mode);
			TerminateableThread listener = masterThread(master);
			SocketCovSupport.hurry(listener);
			SocketCovSupport.set(master, "mListener", listener);
			SocketCovSupport.set(master, "mServer", server);
			SocketCovSupport.set(master, "mMasterStarted", Boolean.TRUE);
			master.shutdownMapHook();
			CollaborationGoodbye goodbye = (CollaborationGoodbye) pipe.read();
			assertEquals(Tools.getUserName(), goodbye.getUserId());
			assertEquals(Tools.getUserName(), master.getUsers(opened.mode));
			assertFalse((Boolean) SocketCovSupport.get(master, "mMasterStarted"));
			assertTrue(server.isClosed());
			assertTrue(pipe.local.isClosed());
		} finally {
			server.close();
			SocketCovSupport.stopOwnedThreads();
		}
	}

	@Test
	// backwards compatibility
	void listenerAcceptsAClientReleasesAnOrphanLockAndSkipsABrokenConnection() throws Exception {
		Opened opened = MapFixture.open("Child");
		OpenMaster master = new OpenMaster();
		master.setController(opened.mode);
		master.remember("child.mm", opened.mode);
		master.usePort(4242);
		ServerSocket server = new ServerSocket();
		server.setReuseAddress(true);
		server.bind(new InetSocketAddress("127.0.0.1", 0));
		server.setSoTimeout(200);
		SocketCovSupport.set(master, "mServer", server);
		TerminateableThread listener = masterThread(master);
		Socket joined = new Socket();
		try (Pipe pipe = Pipe.open(); Pipe brokenPipe = Pipe.open()) {
			ServerCommunication good = SocketCovSupport.server(master, pipe.local, opened.mode, true);
			assertTrue(pipe.read() instanceof CollaborationWhoAreYou);
			master.addConnection(good, opened.mode);
			ServerCommunication broken = SocketCovSupport.server(master, brokenPipe.local, opened.mode, true);
			assertEquals(ServerCommunication.SERVER_VERSION,
					((CollaborationWhoAreYou) brokenPipe.read()).getServerVersion());
			SocketCovSupport.set(broken, "mSocket", null);
			master.addConnection(broken, opened.mode);
			Object session = SocketCovSupport.session(master, opened.mode);
			SocketCovSupport.set(session, "mLockEnabled", Boolean.TRUE);
			SocketCovSupport.set(session, "mLockedAt", Long.valueOf(0));
			SocketCovSupport.set(session, "mLockId", "old-lock");
			SocketCovSupport.set(listener, "mLastTimeUserInformationSent", Long.valueOf(0));
			joined.connect(new InetSocketAddress("127.0.0.1", server.getLocalPort()), 1000);
			joined.setSoTimeout(2000);
			assertTrue(listener.processAction());
			CollaborationWhoAreYou who = (CollaborationWhoAreYou) SocketCovSupport.readMessage(
					new DataInputStream(joined.getInputStream()));
			assertEquals("2.1", who.getServerVersion());
			CollaborationUserInformation info = (CollaborationUserInformation) pipe.read();
			assertEquals("127.0.0.1", info.getMasterIp());
			assertEquals(4242, info.getMasterPort());
			assertTrue(info.getUserIds().startsWith(Tools.getUserName()));
			assertFalse((Boolean) SocketCovSupport.get(session, "mLockEnabled"));
			assertTrue(listener.processAction());
		} finally {
			joined.close();
			server.close();
			SocketCovSupport.stopOwnedThreads();
		}
	}

	private static boolean hostsMaster(Opened opened) {
		for (PermanentNodeHook hook : opened.root.getActivatedHooks()) {
			if (hook instanceof MindMapMaster) {
				return true;
			}
		}
		return false;
	}

	private static TerminateableThread masterThread(MindMapMaster master) throws Exception {
		Class<?> type = null;
		for (Class<?> nested : MindMapMaster.class.getDeclaredClasses()) {
			if ("MasterThread".equals(nested.getSimpleName())) {
				type = nested;
			}
		}
		Constructor<?> ctor = type.getDeclaredConstructor(MindMapMaster.class);
		ctor.setAccessible(true);
		return (TerminateableThread) ctor.newInstance(master);
	}

	private static final class OpenMaster extends MindMapMaster {
		int titles;

		void remember(String name, ExtendedMapFeedback map) {
			addSession(name, map);
		}

		void usePort(int port) {
			mPort = port;
		}

		File base() {
			return getBaseFile();
		}

		void retitle() {
			setTitle();
		}

		@Override
		protected void setTitle() {
			titles++;
			super.setTitle();
		}
	}
}
