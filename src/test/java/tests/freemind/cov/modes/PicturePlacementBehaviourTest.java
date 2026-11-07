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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringWriter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.MindMapNodeModel;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Side and the single-child vertical offset stay the values layout already uses. */
@DisplayName("PicturePlacementBehaviourTest")
class PicturePlacementBehaviourTest {

	@Test
	void oneVisibleChildShiftsByMinusTwoEvenWhenTheParentIsFolded() throws Exception {
		Opened opened = MapFixture.open("Parent");
		MindMapNodeModel parent = (MindMapNodeModel) opened.child("Parent");
		MindMapNodeModel only = new MindMapNodeModel("Only", opened.mode.getMindMapMapModel());
		parent.insert(only, 0);
		assertEquals(-2, only.calcShiftY());
		opened.mode.setFolded(parent, true);
		assertTrue(parent.isFolded());
		assertEquals(-2, only.calcShiftY());
		assertEquals(0, opened.root.calcShiftY());
	}

	@Test
	void twoVisibleChildrenAddNoExtraShift() throws Exception {
		Opened opened = MapFixture.open("Only");
		opened.root.insert(new MindMapNodeModel("Second", opened.mode.getMindMapMapModel()), 1);
		assertEquals(0, opened.child("Only").calcShiftY());
		assertEquals(0, opened.child("Second").calcShiftY());
	}

	@Test
	void sideIsStoredForAChildOfTheRootAndCopiedOntoItsChild() throws Exception {
		Opened opened = MapFixture.open("Only");
		MindMapNodeModel only = (MindMapNodeModel) opened.child("Only");
		MindMapNodeModel grand = new MindMapNodeModel("Grand", opened.mode.getMindMapMapModel());
		only.insert(grand, 0);
		only.setLeft(true);
		assertTrue(only.isLeft());
		assertTrue(grand.isLeft());
		assertTrue(save(opened, only).contains("POSITION=\"left\""));
		assertFalse(save(opened, grand).contains("POSITION="));
	}

	private static String save(Opened opened, MindMapNodeModel node) throws Exception {
		StringWriter writer = new StringWriter();
		node.save(writer, opened.mode.getMap().getLinkRegistry(), true, false);
		return writer.toString();
	}
}
