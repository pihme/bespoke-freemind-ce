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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Proxy;
import java.nio.file.Files;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.controller.actions.generated.instance.CollaborationTransaction;
import freemind.controller.actions.generated.instance.CollaborationUserInformation;
import freemind.controller.actions.generated.instance.CollaborationWhoAreYou;
import freemind.controller.actions.generated.instance.CompoundAction;
import freemind.main.Tools;
import freemind.modes.ExtendedMapFeedback;
import freemind.modes.mindmapmode.MindMapMapModel;
import freemind.modes.mindmapmode.actions.xml.ActionPair;
import plugins.collaboration.socket.ServerCommunication;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;
import tests.freemind.cov.socket.SocketCovSupport.Pipe;

@DisplayName("SocketMasterBehaviourTest")
class SocketMasterBehaviourTest {

	@Test
	// backwards compatibility
	void locksASessionAndNamesEveryoneWhoJoined() throws Exception {
		Opened opened = MapFixture.open("Child");
		File dir = Files.createTempDirectory("socket-master").toFile();
		TestMaster master = new TestMaster();
		master.base = dir;
		master.setPassword("secret");
		master.setPort(5151);
		ExtendedMapFeedback map = SocketCovSupport.hostMap(master, dir, "hosted.mm");
		try (Pipe pipe = Pipe.open()) {
			ServerCommunication comm = SocketCovSupport.server(master, pipe.local, map, false);
			assertTrue(pipe.read() instanceof CollaborationWhoAreYou);
			assertEquals("ROOT", map.getMap().getRootNode().getText());
			assertEquals(new File(dir, "hosted.mm"), map.getMap().getFile());
			assertSame(map, master.getFileMap().get("hosted.mm"));
			assertEquals(Integer.valueOf(0), master.getRole());
			assertEquals(5151, master.getPort());
			assertEquals("secret", master.getPassword());

			String lockId = master.takeLock("ada", map);
			assertTrue(lockId.startsWith("Lock_"));
			assertEquals(lockId, master.getLockId(map));
			Exception second = assertThrows(Exception.class, () -> master.takeLock("bea", map));
			assertTrue(SocketCovSupport.lockFailure(second));
			master.release(map);
			assertThrows(IllegalStateException.class, () -> master.getLockId(map));
			master.clearLock(map);
			assertThrows(IllegalStateException.class, () -> master.release(map));
			IllegalArgumentException missing = assertThrows(IllegalArgumentException.class,
					() -> master.takeLock("ada", opened.mode));
			assertTrue(missing.getMessage().contains("not present"));

			assertEquals(Tools.getUserName(), master.getUsers(map));
			comm.setName("ada");
			master.addConnection(comm, map);
			assertTrue(master.getUsers(map).contains("ada"));
			assertTrue(master.getUsers(map).startsWith(Tools.getUserName()));
			int titles = master.titles;
			master.removeConnection(comm);
			assertEquals(titles + 1, master.titles);
			assertEquals(Tools.getUserName(), master.getUsers(map));
			master.removeConnection(comm);
			assertEquals(titles + 2, master.titles);

			master.addConnection(comm, map);
			String doXml = Tools.marshall(new CompoundAction());
			String undoXml = Tools.marshall(new CompoundAction());
			master.cast(doXml, undoXml, lockId, map);
			CollaborationTransaction broadcast = (CollaborationTransaction) pipe.read();
			assertEquals(lockId, broadcast.getId());
			assertEquals(doXml, broadcast.getDoAction());
			assertEquals(undoXml, broadcast.getUndoAction());

			assertTrue(master.filterEnabled());
			master.transact(new ActionPair(new CompoundAction(), new CompoundAction()), map);
			assertTrue(master.filterEnabled());
			ExtendedMapFeedback boom = (ExtendedMapFeedback) Proxy.newProxyInstance(
					ExtendedMapFeedback.class.getClassLoader(),
					new Class<?>[] { ExtendedMapFeedback.class },
					(proxy, method, args) -> {
						if ("doTransaction".equals(method.getName())) {
							throw new IllegalStateException("boom");
						}
						if (method.getReturnType() == boolean.class) {
							return Boolean.FALSE;
						}
						if (method.getReturnType() == int.class) {
							return Integer.valueOf(0);
						}
						return null;
					});
			master.remember("boom.mm", boom);
			IllegalStateException exploded = assertThrows(IllegalStateException.class,
					() -> master.transact(new ActionPair(new CompoundAction(), new CompoundAction()), boom));
			assertEquals("boom", exploded.getMessage());
			assertTrue(master.filterEnabled());

			CollaborationUserInformation info = master.getMasterInformation(map);
			assertEquals(master.getUsers(map), info.getUserIds());
			assertEquals(Tools.getHostName(), info.getMasterHostname());
			assertEquals(Tools.getHostIpAsString(), info.getMasterIp());
			assertEquals(5151, info.getMasterPort());
			comm.terminateSocket();
		} finally {
			release(master);
			SocketCovSupport.stopOwnedThreads();
		}
	}

	private static void release(TestMaster master) {
		for (ExtendedMapFeedback held : master.getFileMap().values()) {
			if (held.getMap() instanceof MindMapMapModel) {
				((MindMapMapModel) held.getMap()).destroy();
			}
		}
	}
}
