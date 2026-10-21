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

package tests.freemind.cov.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.MindMapNode;
import plugins.map.AddMapImageToNodeAction;

@DisplayName("AddMapImageToNodeActionBehaviourTest")
class AddMapImageToNodeActionBehaviourTest {

	@Test
	// backwards compatibility
	void addsAChildWhoseTextIsTheTileHtml() throws Exception {
		try (MapCovSupport support = MapCovSupport.open("Child", "Sibling")) {
			MindMapNode placed = support.opened.child("Child");
			MindMapNode plain = support.opened.child("Sibling");
			support.attach(placed, 49.1, 8.2, 7);
			support.select(placed);
			int before = placed.getChildCount();
			AddMapImageToNodeAction action = new AddMapImageToNodeAction();
			action.setController(support.opened.mode);
			action.setPluginBaseClass(MapCovSupport.base(support.registration));
			action.invoke(placed);
			assertEquals(before + 1, placed.getChildCount());
			assertTrue(((MindMapNode) placed.getChildAt(0)).getText().contains("BACKGROUND"));

			support.select(plain);
			int plainChildren = plain.getChildCount();
			action.invoke(plain);
			assertEquals(plainChildren, plain.getChildCount());
		}
	}
}
