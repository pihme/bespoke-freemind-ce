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

package tests.freemind.cov.common;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Window;

import javax.swing.RootPaneContainer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.common.FreeMindTask;

/** A task builds a progress dialog in its constructor, which cannot open headless. */
@DisplayName("FreeMindTaskBehaviourTest")
class FreeMindTaskBehaviourTest {

	@Test
	void constructingATaskThrowsHeadlessExceptionAndStartsNoThread() {
		// backwards compatibility
		assertTrue(GraphicsEnvironment.isHeadless());
		Window[] before = Window.getWindows();
		int[] steps = { 0 };

		HeadlessException thrown = assertThrows(HeadlessException.class, () -> new OneStep(null, steps));

		assertEquals(HeadlessException.class, thrown.getClass());
		assertEquals(0, steps[0]);
		assertArrayEquals(before, Window.getWindows());
		assertFalse(namedThreadIsAlive("cov-freemind-task"));
	}

	private static boolean namedThreadIsAlive(String name) {
		ThreadGroup group = Thread.currentThread().getThreadGroup();
		Thread[] threads = new Thread[group.activeCount() + 8];
		int count = group.enumerate(threads);
		for (int i = 0; i < count; i++) {
			if (threads[i] != null && name.equals(threads[i].getName()) && threads[i].isAlive()) {
				threads[i].interrupt();
				return true;
			}
		}
		return false;
	}

	private static final class OneStep extends FreeMindTask {
		private final int[] steps;

		private OneStep(RootPaneContainer frame, int[] steps) {
			super(frame, 1, "cov-freemind-task");
			this.steps = steps;
		}

		@Override
		protected boolean processAction() {
			steps[0]++;
			return false;
		}
	}
}
