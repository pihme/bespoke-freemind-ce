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

package tests.freemind.cov.modes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;

import org.junit.jupiter.api.Test;

import freemind.modes.FreemindAction;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** A menu action is enabled only while a map is open, and it keeps its controller. */
class FreemindActionBehaviourTest {

	@Test
	void actionWithoutAControllerIsDisabled() {
		// backwards compatibility
		FreemindAction action = new FreemindAction("plain-title", (Icon) null, null) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
			}
		};
		assertFalse(action.isEnabled(new JMenuItem(), action));
		assertEquals("plain-title", action.getValue(Action.NAME));
	}

	@Test
	void actionOnAnOpenMapIsEnabledAndNamesItselfFromTheResourceKey() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		FreemindAction action = new FreemindAction("node_down", opened.mode) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
			}
		};
		assertEquals("node_down", action.getValue(Action.NAME));
		assertTrue(action.isEnabled(new JMenuItem(), action));
		assertSame(opened.mode, action.getControllerAdapter());
	}
}
