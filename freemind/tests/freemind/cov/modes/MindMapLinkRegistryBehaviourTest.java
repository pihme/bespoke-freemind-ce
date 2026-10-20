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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import freemind.modes.MindMapLinkRegistry;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.MindMapArrowLinkModel;
import freemind.modes.mindmapmode.MindMapNodeModel;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Link registration is idempotent, rewrites a duplicate id, and tracks local hyperlinks. */
class MindMapLinkRegistryBehaviourTest {

	@Test
	void theSameLinkIsRegisteredOnceAndADuplicateIdIsRewritten() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode child = opened.child("Child");
		MindMapLinkRegistry registry = opened.mode.getMap().getLinkRegistry();
		MindMapArrowLinkModel link = new MindMapArrowLinkModel(opened.root, child, opened.mode);
		assertThrows(IllegalArgumentException.class, () -> registry.registerLink(link));
		link.setDestinationLabel("child-label");
		registry.registerLink(link);
		registry.registerLink(link);
		assertEquals(1, registry.getAllLinksIntoMe(child).size());
		assertEquals(1, registry.getAllLinksFromMe(opened.root).size());

		MindMapNode other = new MindMapNodeModel("Other", opened.mode.getMap());
		opened.root.insert(other, 1);
		MindMapArrowLinkModel second = new MindMapArrowLinkModel(other, child, opened.mode);
		second.setDestinationLabel("child-label");
		second.setUniqueId(link.getUniqueId());
		registry.registerLink(second);
		assertNotEquals(link.getUniqueId(), second.getUniqueId());
		assertEquals(2, registry.getAllLinksIntoMe(child).size());
	}

	@Test
	void deregisteringAParentClearsTheChildAndLocalIdsAreRemembered() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode child = opened.child("Child");
		MindMapNode grandchild = new MindMapNodeModel("Grand", opened.mode.getMap());
		child.insert(grandchild, 0);
		MindMapLinkRegistry registry = opened.mode.getMap().getLinkRegistry();
		MindMapArrowLinkModel link = new MindMapArrowLinkModel(opened.root, grandchild, opened.mode);
		link.setDestinationLabel("grand");
		registry.registerLink(link);
		assertFalse(registry.isTargetOfLocalHyperlinks("local-id"));
		registry.registerLocalHyperlinkId("local-id");
		assertTrue(registry.isTargetOfLocalHyperlinks("local-id"));
		registry.deregisterLinkTarget(opened.root);
		assertEquals(0, registry.getAllLinksIntoMe(grandchild).size());
		assertEquals(0, registry.getAllLinks(grandchild).size());
	}
}
