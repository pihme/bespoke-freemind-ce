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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import plugins.collaboration.socket.TerminateableThread;

@DisplayName("TerminateableThreadBehaviourTest")
class TerminateableThreadBehaviourTest {

	@Test
	// backwards compatibility
	void aThreadThatNeverStartsIsReportedAsStillRunning() {
		Stoppable idle = new Stoppable();
		idle.pause(1);
		idle.commitSuicide();
		idle.start();
		try {
			idle.join(1000);
		} catch (InterruptedException interrupted) {
			Thread.currentThread().interrupt();
		}
		assertFalse(idle.isAlive());
		assertEquals(0, idle.calls);
	}

	@Test
	// backwards compatibility
	void commitSuicideStopsARunningWorker() throws Exception {
		Stoppable worker = new Stoppable();
		worker.pause(5);
		worker.start();
		try {
			SocketCovSupport.waitFor(() -> worker.calls > 0);
			worker.commitSuicide();
			worker.join(1000);
			assertFalse(worker.isAlive());
			assertTrue(worker.calls > 0);
		} finally {
			worker.commitSuicide();
		}
	}

	@Test
	// backwards compatibility
	void aFailingActionIsLoggedAndTheLoopContinues() throws Exception {
		Stoppable worker = new Stoppable();
		worker.pause(5);
		worker.blow = true;
		worker.start();
		try {
			SocketCovSupport.waitFor(() -> worker.calls > 1);
			assertTrue(worker.isAlive());
		} finally {
			worker.commitSuicide();
			worker.join(1000);
		}
		assertFalse(worker.isAlive());
	}

	@Test
	// backwards compatibility
	void anInterruptedSleepIsLoggedAndTheLoopContinues() throws Exception {
		Stoppable worker = new Stoppable();
		worker.pause(200);
		worker.start();
		try {
			SocketCovSupport.waitFor(() -> worker.calls >= 1);
			worker.interrupt();
			SocketCovSupport.waitFor(() -> worker.calls >= 2);
			assertTrue(worker.isAlive());
		} finally {
			worker.pause(1);
			worker.commitSuicide();
			worker.join(1000);
		}
		assertFalse(worker.isAlive());
	}

	@Test
	// backwards compatibility
	void anInterruptedShutdownWaitIsSwallowed() {
		Stoppable idle = new Stoppable();
		idle.pause(5);
		Thread.currentThread().interrupt();
		try {
			idle.commitSuicide();
			assertFalse(Thread.currentThread().isInterrupted());
			assertFalse(idle.isAlive());
		} finally {
			Thread.interrupted();
		}
	}

	private static final class Stoppable extends TerminateableThread {
		int calls;
		boolean blow;

		Stoppable() {
			super("coverage-stoppable");
		}

		void pause(int millis) {
			mSleepTime = millis;
		}

		public boolean processAction() {
			calls++;
			if (blow) {
				throw new IllegalStateException("boom");
			}
			return false;
		}
	}
}
