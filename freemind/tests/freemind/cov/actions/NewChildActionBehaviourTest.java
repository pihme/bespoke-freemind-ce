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

package tests.freemind.cov.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Test;

import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.actions.NewChildAction;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** A new child is appended, and an unknown mode adds nothing. */
class NewChildActionBehaviourTest {

	@Test
	void aChildIsAppended() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Host");
		opened.frame.setProperty("placenewbranches", "last");
		MindMapNode host = opened.child("Host");
		opened.mode.select(host, List.of(host));
		try {
			new NewChildAction(opened.mode).actionPerformed(
					new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "child"));
		} catch (HeadlessException ex) {
			assertFalse(opened.mode.isBlocked() && host.getChildCount() == 0);
		}
		assertEquals(List.of(""), MapFixture.childTexts(host));
		endEdit(opened);
		assertFalse(opened.mode.isBlocked());
	}

	@Test
	void anUnknownModeAddsNothing() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Host");
		MindMapNode host = opened.child("Host");
		assertNull(new NewChildAction(opened.mode).addNew(host, 99, null));
		assertEquals(0, host.getChildCount());
		assertFalse(opened.mode.isBlocked());
	}

	private static void endEdit(Opened opened) throws Exception {
		try {
			opened.mode.edit.stopEditing();
		} catch (RuntimeException ex) {
			Field dialog = opened.mode.edit.getClass().getDeclaredField("mCurrentEditDialog");
			dialog.setAccessible(true);
			dialog.set(opened.mode.edit, null);
		}
		EventQueue.invokeAndWait(() -> {
		});
		opened.mode.setBlocked(false);
	}
}
