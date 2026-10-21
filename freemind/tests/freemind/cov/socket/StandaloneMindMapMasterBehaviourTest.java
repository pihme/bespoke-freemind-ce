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

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.controller.actions.generated.instance.CollaborationActionBase;
import freemind.controller.actions.generated.instance.CollaborationUserInformation;
import freemind.controller.actions.generated.instance.CollaborationWhoAreYou;
import freemind.modes.ExtendedMapFeedback;
import freemind.modes.mindmapmode.MindMapMapModel;
import plugins.collaboration.socket.ServerCommunication;
import plugins.collaboration.socket.StandaloneMindMapMaster;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;
import tests.freemind.cov.socket.SocketCovSupport.Pipe;

@DisplayName("StandaloneMindMapMasterBehaviourTest")
class StandaloneMindMapMasterBehaviourTest {

	@Test
	// backwards compatibility
	void servesMapsFromADirectoryAndSavesWhenTheCopyOnDiskIsGone() throws Exception {
		Opened opened = MapFixture.open("Child");
		File dir = Files.createTempDirectory("socket-standalone").toFile();
		File mapFile = new File(dir, "served.mm");
		Files.writeString(mapFile.toPath(), SocketCovSupport.MAP);
		Files.writeString(new File(dir, "notes.txt").toPath(), "ignore");
		OpenStandalone master = new OpenStandalone(opened, dir);
		try {
			assertFalse(master.getFileMap().containsKey("notes.txt"));
			ExtendedMapFeedback map = master.getFileMap().get("served.mm");
			assertEquals("ROOT", map.getMap().getRootNode().getText());
			assertEquals(dir, master.base());
			assertEquals(0, master.getPort());
			ServerSocket listening = (ServerSocket) SocketCovSupport.get(master, "mServer");
			assertTrue(listening.getLocalPort() > 0);
			assertEquals("secret", master.getPassword());
			master.retitle();
			assertEquals(1, master.titles);
			IllegalArgumentException controller = assertThrows(IllegalArgumentException.class, master::controller);
			assertTrue(controller.getMessage().contains("No controller"));
			IllegalArgumentException feedback = assertThrows(IllegalArgumentException.class, master::feedback);
			assertTrue(feedback.getMessage().contains("No controller"));

			Object listener = SocketCovSupport.get(master, "mMasterThread");
			SocketCovSupport.waitFor(() -> ((Number) SocketCovSupport.get(listener, "mLastSaveAction")).longValue() != 0L);
			MindMapMapModel model = (MindMapMapModel) map.getMap();
			assertTrue(mapFile.delete());
			model.setSaved(false);
			SocketCovSupport.set(listener, "mLastSaveAction", Long.valueOf(0));
			SocketCovSupport.waitFor(mapFile::exists);
			assertTrue(Files.readString(mapFile.toPath()).contains("ROOT"));

			Object session = SocketCovSupport.session(master, map);
			SocketCovSupport.set(session, "mLockEnabled", Boolean.TRUE);
			SocketCovSupport.set(session, "mLockedAt", Long.valueOf(0));
			SocketCovSupport.set(session, "mLockId", "old-lock");
			SocketCovSupport.waitFor(() -> !((Boolean) SocketCovSupport.get(session, "mLockEnabled")).booleanValue());

			try (Pipe pipe = Pipe.open(); Pipe brokenPipe = Pipe.open()) {
				ServerCommunication good = SocketCovSupport.server(master, pipe.local, map, false);
				assertTrue(pipe.read() instanceof CollaborationWhoAreYou);
				master.addConnection(good, map);
				ServerCommunication broken = SocketCovSupport.server(master, brokenPipe.local, map, false);
				assertEquals("2.1", ((CollaborationWhoAreYou) brokenPipe.read()).getServerVersion());
				SocketCovSupport.set(broken, "mSocket", null);
				master.addConnection(broken, map);
				pipe.remote.setSoTimeout(500);
				CollaborationUserInformation info = null;
				long deadline = System.currentTimeMillis() + 3000;
				while (info == null && System.currentTimeMillis() < deadline) {
					SocketCovSupport.set(listener, "mLastTimeUserInformationSent", Long.valueOf(0));
					try {
						CollaborationActionBase message = pipe.read();
						if (message instanceof CollaborationUserInformation) {
							info = (CollaborationUserInformation) message;
						}
					} catch (SocketTimeoutException waiting) {
						// The master thread reports users after its accept times out.
					}
				}
				assertTrue(info != null && "127.0.0.1".equals(info.getMasterIp()));
				assertTrue(info.getUserIds().contains("Server Communication"));
			}

			try (Socket joined = new Socket()) {
				joined.connect(new InetSocketAddress("127.0.0.1", listening.getLocalPort()), 2000);
				joined.setSoTimeout(2000);
				CollaborationWhoAreYou who = (CollaborationWhoAreYou) SocketCovSupport.readMessage(
						new DataInputStream(joined.getInputStream()));
				assertEquals(ServerCommunication.SERVER_VERSION, who.getServerVersion());
			}

			halt(master);
			if (runShutdownHook(master)) {
				String before = Files.readString(mapFile.toPath());
				assertTrue(runShutdownHook(master));
				assertEquals(before, Files.readString(mapFile.toPath()));
				model.setSaved(false);
				assertTrue(runShutdownHook(master));
				assertTrue(Files.readString(mapFile.toPath()).contains("<map"));
				File blocked = new File(dir, "blocked");
				assertTrue(blocked.mkdir());
				model.setFile(blocked);
				model.setSaved(false);
				assertTrue(runShutdownHook(master));
				model.setFile(mapFile);
				model.setSaved(true);
			}
		} finally {
			stop(master);
		}
	}

	@Test
	// backwards compatibility
	void rejectsAPathThatIsNotADirectoryOfMaps() throws Exception {
		Opened opened = MapFixture.open("Child");
		File note = File.createTempFile("socket-note", ".txt");
		assertThrows(NullPointerException.class,
				() -> new StandaloneMindMapMaster(opened.frame, note, "secret", 0));
		File parent = Files.createTempDirectory("socket-mm-dir").toFile();
		assertTrue(new File(parent, "nested.mm").mkdir());
		assertThrows(IOException.class,
				() -> new StandaloneMindMapMaster(opened.frame, parent, "secret", 0));
	}

	private static void halt(StandaloneMindMapMaster master) throws InterruptedException {
		Object listener = SocketCovSupport.get(master, "mMasterThread");
		if (listener instanceof Thread) {
			SocketCovSupport.hurry((Thread) listener);
		}
		master.terminate();
		if (listener instanceof Thread) {
			((Thread) listener).join(1000);
			assertFalse(((Thread) listener).isAlive());
		}
	}

	private static void stop(StandaloneMindMapMaster master) throws InterruptedException {
		halt(master);
		for (ExtendedMapFeedback held : master.getFileMap().values()) {
			if (held.getMap() instanceof MindMapMapModel) {
				((MindMapMapModel) held.getMap()).destroy();
			}
		}
		SocketCovSupport.stopOwnedThreads();
	}

	private static boolean runShutdownHook(StandaloneMindMapMaster master) {
		try {
			Object listener = SocketCovSupport.get(master, "mMasterThread");
			Class<?> hookType = Class.forName(
					"plugins.collaboration.socket.StandaloneMindMapMaster$MasterThread$1");
			Constructor<?> ctor = hookType.getDeclaredConstructor(listener.getClass(),
					StandaloneMindMapMaster.class);
			ctor.setAccessible(true);
			((Runnable) ctor.newInstance(listener, master)).run();
			return true;
		} catch (ReflectiveOperationException ex) {
			return false;
		}
	}

	private static final class OpenStandalone extends StandaloneMindMapMaster {
		int titles;

		OpenStandalone(Opened opened, File dir) throws Exception {
			super(opened.frame, dir, "secret", 0);
		}

		void retitle() {
			setTitle();
		}

		File base() {
			return getBaseFile();
		}

		freemind.modes.mindmapmode.MindMapController controller() {
			return getMindMapController();
		}

		ExtendedMapFeedback feedback() {
			return getMapFeedback();
		}

		@Override
		protected void setTitle() {
			titles++;
			super.setTitle();
		}
	}
}
