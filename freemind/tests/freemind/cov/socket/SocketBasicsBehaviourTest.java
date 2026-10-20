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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import freemind.common.NumberProperty;
import freemind.controller.actions.generated.instance.CollaborationGoodbye;
import freemind.controller.actions.generated.instance.CollaborationUserInformation;
import freemind.controller.actions.generated.instance.CompoundAction;
import freemind.controller.actions.generated.instance.HookNodeAction;
import freemind.main.Resources;
import freemind.main.Tools;
import freemind.modes.ExtendedMapFeedback;
import freemind.modes.mindmapmode.actions.xml.ActionPair;
import freemind.view.MapModule;
import plugins.collaboration.socket.MindMapMaster;
import plugins.collaboration.socket.SocketBasics;
import plugins.collaboration.socket.SocketConnectionHook;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

class SocketBasicsBehaviourTest {

	private static final String PORT_KEY = "plugins.collaboration.socket.port";

	@Test
	// backwards compatibility
	void filtersHookTrafficAndStopsACommandThatCannotLock() throws Exception {
		Opened opened = MapFixture.open("Child");
		Probe probe = attached(opened);
		ActionPair plain = pair(new CollaborationGoodbye());
		assertNull(probe.filterAction(null));
		assertEquals(0, probe.unlocks);

		probe.enableFilter(false);
		assertSame(plain, probe.filterAction(plain));
		assertEquals(0, probe.unlocks);
		probe.enableFilter(true);

		ActionPair slaveHook = pair(hook(SocketConnectionHook.SLAVE_HOOK_LABEL));
		assertSame(slaveHook, probe.filterAction(slaveHook));
		ActionPair masterHook = pair(hook(MindMapMaster.MASTER_HOOK_LABEL));
		assertSame(masterHook, probe.filterAction(masterHook));
		CompoundAction outer = new CompoundAction();
		CompoundAction inner = new CompoundAction();
		inner.addChoice(hook(SocketBasics.SLAVE_HOOK_LABEL));
		outer.addChoice(inner);
		ActionPair nested = new ActionPair(outer, new CompoundAction());
		assertSame(nested, probe.filterAction(nested));
		assertEquals(0, probe.broadcasts);

		ActionPair otherHook = pair(hook("plugins/collaboration/socket/missing"));
		assertSame(otherHook, probe.filterAction(otherHook));
		assertEquals(1, probe.broadcasts);
		assertEquals(1, probe.unlocks);
		assertEquals(Tools.getUserName(), probe.lockedUser);
		assertTrue(probe.lastDo.contains("CollaborationGoodbye") || probe.lastDo.length() > 0);

		probe.failLock = true;
		ActionPair rejected = probe.filterAction(plain);
		assertNotSame(plain, rejected);
		assertTrue(rejected.getDoAction() instanceof CompoundAction);
		assertEquals(2, probe.unlocks);

		probe.failLock = false;
		probe.explode = true;
		ActionPair exploded = probe.filterAction(plain);
		assertNotSame(plain, exploded);
		assertTrue(exploded.getUndoAction() instanceof CompoundAction);
		assertEquals(3, probe.unlocks);
	}

	@Test
	// backwards compatibility
	void titlesTheOpenMapAndRemembersThePort() throws Exception {
		Opened opened = MapFixture.open("Child");
		Probe probe = new Probe();
		MapModule module = opened.app.getMapModule();
		assertEquals("Old", probe.getMapTitle("Old", module, opened.mode.getMap()));
		probe.setController(opened.mode);
		assertSame(opened.mode, probe.feedback());
		assertEquals("Old", probe.getMapTitle("Old", module, opened.mode.getMap()));
		probe.share = true;
		String titled = probe.getMapTitle("Old", module, opened.mode.getMap());
		assertTrue(titled.startsWith("Old"));
		assertNotEquals("Old", titled);
		assertEquals(4242, probe.getPort());
		assertEquals(Integer.valueOf(1), probe.getRole());
		assertEquals(Tools.getUserName(), probe.user());
		probe.password("secret");
		assertEquals("secret", probe.getPassword());
		ActionPair empty = probe.getEmptyActionPair();
		assertNotSame(empty.getDoAction(), empty.getUndoAction());

		String previous = Resources.getInstance().getProperties().getProperty(PORT_KEY);
		try {
			Resources.getInstance().getProperties().setProperty(PORT_KEY, "2000");
			NumberProperty property = probe.port();
			assertEquals("2000", property.getValue());
			property.setValue("2001");
			probe.storePort(property);
			assertEquals("2001", opened.frame.getProperty(PORT_KEY));
		} finally {
			if (previous == null) {
				Resources.getInstance().getProperties().remove(PORT_KEY);
				opened.frame.getProperties().remove(PORT_KEY);
			} else {
				Resources.getInstance().getProperties().setProperty(PORT_KEY, previous);
				opened.frame.setProperty(PORT_KEY, previous);
			}
		}
	}

	@Test
	// backwards compatibility
	void registersAsATitleContributorAndAsAnActionFilter() throws Exception {
		Opened opened = MapFixture.open("Child");
		Probe probe = attached(opened);
		probe.startupMapHook();
		opened.app.setTitle();
		assertTrue(probe.titles >= 1);
		int afterStartup = probe.titles;
		probe.shutdownMapHook();
		opened.app.setTitle();
		assertEquals(afterStartup, probe.titles);
		assertNull(probe.feedback());

		Probe filter = attached(opened);
		filter.register();
		opened.mode.doTransaction("update", filter.getEmptyActionPair());
		assertEquals(1, filter.broadcasts);

		filter.sawDisabled = false;
		filter.transact(filter.getEmptyActionPair());
		assertTrue(filter.sawDisabled);
		assertEquals(1, filter.broadcasts);

		filter.deregister();
		opened.mode.doTransaction("update", filter.getEmptyActionPair());
		assertEquals(1, filter.broadcasts);
	}

	@Test
	// backwards compatibility
	void togglesTheSlaveHookOnTheRoot() throws Exception {
		Opened opened = MapFixture.open("Child");
		assertTrue(opened.mode.getHookFactory().getPossibleNodeHooks().contains(SocketBasics.SLAVE_HOOK_LABEL));
		SocketBasics.togglePermanentHook(opened.mode, SocketBasics.SLAVE_HOOK_LABEL);
		assertTrue(opened.root.getActivatedHooks().stream().anyMatch(hook -> hook instanceof SocketConnectionHook));
		SocketBasics.togglePermanentHook(opened.mode, SocketBasics.SLAVE_HOOK_LABEL);
		assertFalse(opened.root.getActivatedHooks().stream().anyMatch(hook -> hook instanceof SocketConnectionHook));
	}

	private static Probe attached(Opened opened) {
		Probe probe = new Probe();
		probe.setController(opened.mode);
		return probe;
	}

	private static ActionPair pair(freemind.controller.actions.generated.instance.XmlAction action) {
		return new ActionPair(action, new CompoundAction());
	}

	private static HookNodeAction hook(String name) {
		HookNodeAction action = new HookNodeAction();
		action.setHookName(name);
		return action;
	}

	private static final class Probe extends SocketBasics {
		int broadcasts;
		int unlocks;
		int titles;
		String lockedUser;
		String lastDo;
		boolean failLock;
		boolean explode;
		boolean share;
		boolean sawDisabled;

		public Integer getRole() {
			return ROLE_SLAVE;
		}

		public int getPort() {
			return 4242;
		}

		public CollaborationUserInformation getMasterInformation(ExtendedMapFeedback controller) {
			if (!share) {
				return null;
			}
			CollaborationUserInformation info = new CollaborationUserInformation();
			info.setUserIds("ada");
			info.setMasterHostname("localhost");
			info.setMasterIp("127.0.0.1");
			info.setMasterPort(4242);
			return info;
		}

		public String getMapTitle(String oldTitle, MapModule module, freemind.modes.MindMap model) {
			titles++;
			return super.getMapTitle(oldTitle, module, model);
		}

		public ActionPair filterAction(ActionPair pair) {
			if (!mFilterEnabled) {
				sawDisabled = true;
				return pair;
			}
			return super.filterAction(pair);
		}

		void enableFilter(boolean enabled) {
			mFilterEnabled = enabled;
		}

		void password(String value) {
			mPassword = value;
		}

		protected String lock(String user, ExtendedMapFeedback controller) throws UnableToGetLockException {
			if (failLock) {
				throw new LockDenied();
			}
			if (explode) {
				throw new IllegalStateException("nope");
			}
			lockedUser = user;
			return "lock-1";
		}

		protected void broadcastCommand(String doAction, String undoAction, String lockId, ExtendedMapFeedback controller) {
			broadcasts++;
			lastDo = doAction;
		}

		protected void unlock(ExtendedMapFeedback controller) {
			unlocks++;
		}

		ExtendedMapFeedback feedback() {
			return getMapFeedback();
		}

		String user() {
			return getUserName();
		}

		NumberProperty port() {
			return getPortProperty();
		}

		void storePort(NumberProperty property) {
			setPortProperty(property);
		}

		void register() {
			registerFilter();
		}

		void deregister() {
			deregisterFilter();
		}

		void transact(ActionPair pair) {
			executeTransaction(pair, getMapFeedback());
		}

		private static final class LockDenied extends UnableToGetLockException {
		}
	}
}
