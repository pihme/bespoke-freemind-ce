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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import freemind.modes.MindMapNode;
import plugins.map.AddLinkToMapAction;

class AddLinkToMapActionBehaviourTest {

	@Test
	// backwards compatibility
	void writesAnOpenStreetMapLinkOnce() throws Exception {
		try (MapCovSupport support = MapCovSupport.open("Child", "Sibling")) {
			MindMapNode placed = support.opened.child("Child");
			MindMapNode plain = support.opened.child("Sibling");
			support.attach(placed, 49.1, 8.2, 7);
			support.select(placed);
			AddLinkToMapAction action = new AddLinkToMapAction();
			action.setController(support.opened.mode);
			action.setPluginBaseClass(MapCovSupport.base(support.registration));
			action.invoke(placed);
			assertTrue(placed.getLink().contains("openstreetmap.org"));
			assertTrue(placed.getLink().contains("mlat=49.1"));
			String first = placed.getLink();
			action.invoke(placed);
			assertEquals(first, placed.getLink());

			support.opened.mode.select(plain, List.of(plain, placed));
			plain.setLink("keep-me");
			action.invoke(plain);
			assertEquals("keep-me", plain.getLink());
			assertNull(support.opened.root.getLink());
		}
	}
}
