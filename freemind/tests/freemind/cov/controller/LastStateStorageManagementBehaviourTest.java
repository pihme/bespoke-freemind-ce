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

package tests.freemind.cov.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.controller.LastStateStorageManagement;
import freemind.controller.actions.generated.instance.MindmapLastStateStorage;

/** REQ-DED-10. Remembered map states are kept up to 50, newest first. */
@DisplayName("LastStateStorageManagementBehaviourTest")
class LastStateStorageManagementBehaviourTest {

	@Test
	void TestREQ_DED_10_FiftyStatesAreKeptAndTheOldestIsDropped() throws Exception {
		LastStateStorageManagement states = new LastStateStorageManagement("<broken>");
		assertNull(states.getStorage("missing"));

		for (int i = 1; i <= 49; i++) {
			states.changeOrAdd(storage("m" + i, i));
			waitForNextMillis();
		}
		assertTrue(openNames(states).contains("m1"));
		assertEquals(49, openNames(states).size());

		states.changeOrAdd(storage("m50", 50));
		waitForNextMillis();
		assertEquals(50, openNames(states).size());
		assertTrue(openNames(states).contains("m1"));
		assertTrue(openNames(states).contains("m50"));

		states.changeOrAdd(storage("m51", 51));
		List<String> kept = openNames(states);
		assertEquals(50, kept.size());
		assertFalse(kept.contains("m1"));
		assertTrue(kept.contains("m51"));
		assertEquals(2.5f, states.getStorage("m51").getLastZoom());
	}

	@Test
	void changingAnExistingStateUpdatesItAndDoesNotAddASecondCopy() throws Exception {
		// backwards compatibility
		LastStateStorageManagement states = new LastStateStorageManagement("");
		states.changeOrAdd(storage("same", 1));
		waitForNextMillis();
		MindmapLastStateStorage updated = storage("same", 9);
		updated.setLastZoom(4.5f);
		states.changeOrAdd(updated);

		assertEquals(4.5f, states.getStorage("same").getLastZoom());
		assertTrue(states.getXml().indexOf("same") == states.getXml().lastIndexOf("same"));
	}

	@Test
	void clearTabIndicesDropsMapsFromTheOpenListAndTheFocusRoundTrips() {
		// backwards compatibility
		LastStateStorageManagement states = new LastStateStorageManagement("");
		MindmapLastStateStorage open = storage("open", 1);
		open.setTabIndex(2);
		states.changeOrAdd(open);
		states.setLastFocussedTab(2);

		assertEquals(2, states.getLastFocussedTab());
		assertEquals(1, states.getLastOpenList().size());

		states.clearTabIndices();
		assertTrue(states.getLastOpenList().isEmpty());
		assertEquals(-1, states.getStorage("open").getTabIndex());
	}

	/**
	 * Names of states still marked open. {@code getStorage} stamps a new change
	 * time, and the trim keeps one state per time, so a count must not call it.
	 */
	private static List<String> openNames(LastStateStorageManagement states) {
		List<String> names = new ArrayList<>();
		for (MindmapLastStateStorage store : states.getLastOpenList()) {
			names.add(store.getRestorableName());
		}
		return names;
	}

	private static MindmapLastStateStorage storage(String name, int zoom) {
		MindmapLastStateStorage store = new MindmapLastStateStorage();
		store.setRestorableName(name);
		store.setLastZoom(zoom == 51 ? 2.5f : 1.0f);
		store.setTabIndex(0);
		return store;
	}

	private static void waitForNextMillis() throws InterruptedException {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() == start) {
			Thread.sleep(1);
		}
	}
}
