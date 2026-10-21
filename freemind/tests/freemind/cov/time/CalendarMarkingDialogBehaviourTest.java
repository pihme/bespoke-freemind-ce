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

package tests.freemind.cov.time;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.HeadlessException;
import java.awt.Window;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

import accessories.plugins.time.CalendarMarkingDialog;
import tests.freemind.cov.MapFixture;

/** Backwards-compatible calendar-marking dialog under a headless JVM. */
class CalendarMarkingDialogBehaviourTest {

	@Test
	void constructionThrowsBeforeAWindowExists() throws Exception {
		// backwards compatibility
		MapFixture.Opened opened = MapFixture.open("Child");
		int windows = Window.getWindows().length;
		HeadlessException thrown = assertThrows(HeadlessException.class,
				() -> new CalendarMarkingDialog(opened.mode));
		assertTrue(thrown.getClass() == HeadlessException.class);
		assertEquals(windows, Window.getWindows().length);
	}

	@Test
	void mainReportsThatAControllerIsRequired() {
		// backwards compatibility
		PrintStream original = System.err;
		ByteArrayOutputStream captured = new ByteArrayOutputStream();
		System.setErr(new PrintStream(captured));
		try {
			CalendarMarkingDialog.main(new String[0]);
		} finally {
			System.setErr(original);
		}
		assertTrue(captured.toString().contains("MindMapController"));
	}
}
