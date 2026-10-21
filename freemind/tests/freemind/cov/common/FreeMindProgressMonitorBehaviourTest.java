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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Window;

import org.junit.jupiter.api.Test;

import freemind.common.FreeMindProgressMonitor;

/** The progress dialog is a window. Headless construction adds none. */
class FreeMindProgressMonitorBehaviourTest {

	@Test
	void theConstructorThrowsHeadlessExceptionAndAddsNoWindow() {
		// backwards compatibility
		assertTrue(GraphicsEnvironment.isHeadless());
		Window[] before = Window.getWindows();

		HeadlessException thrown = assertThrows(HeadlessException.class,
				() -> new FreeMindProgressMonitor("title"));

		assertEquals(HeadlessException.class, thrown.getClass());
		assertArrayEquals(before, Window.getWindows());
	}
}
