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

import java.awt.event.ActionEvent;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.NodeDownAction;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Moving the selected child down swaps it with the sibling below. */
@DisplayName("NodeDownActionBehaviourTest")
class NodeDownActionBehaviourTest {

	@Test
	void selectedChildMovesBelowItsSibling() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("A", "B");
		opened.mode.select(opened.child("A"), List.of(opened.child("A")));
		new NodeDownAction(opened.mode).actionPerformed(
				new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "down"));
		assertEquals(List.of("B", "A"), MapFixture.childTexts(opened.root));
	}

	@Test
	void rootSelectionLeavesTheChildrenInPlace() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("A", "B");
		opened.mode.select(opened.root, List.of(opened.root));
		new NodeDownAction(opened.mode).actionPerformed(
				new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "down"));
		assertEquals(List.of("A", "B"), MapFixture.childTexts(opened.root));
	}
}
