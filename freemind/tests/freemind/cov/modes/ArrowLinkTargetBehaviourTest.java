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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.Point;

import org.junit.jupiter.api.Test;

import freemind.main.XMLElement;
import freemind.modes.ArrowLinkAdapter;
import freemind.modes.ArrowLinkTarget;
import freemind.modes.MindMapNode;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** A link target records its source label and does not move the inclination. */
class ArrowLinkTargetBehaviourTest {

	@Test
	void saveNamesTheElementLinkTargetAndStoresTheSource() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode child = opened.child("Child");
		ArrowLinkTarget target = new ArrowLinkTarget(opened.root, child, opened.mode);
		target.setSourceLabel("src-1");
		XMLElement saved = target.save();
		assertEquals("linktarget", saved.getName());
		assertEquals("src-1", saved.getAttribute("SOURCE"));
	}

	@Test
	void changeInclinationLeavesTheStartPointUntouched() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		ArrowLinkTarget target = new ArrowLinkTarget(opened.root, opened.child("Child"), opened.mode);
		target.setStartInclination(new Point(8, 9));
		target.changeInclination(opened.view, 1, 2, 3, 4);
		assertEquals(new Point(8, 9), target.getStartInclination());
	}

	@Test
	void createArrowLinkAdapterCopiesTheEndpoints() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode child = opened.child("Child");
		ArrowLinkTarget target = new ArrowLinkTarget(opened.root, child, opened.mode);
		target.setSourceLabel("src-2");
		ArrowLinkAdapter link = target.createArrowLinkAdapter(opened.mode.getMap().getLinkRegistry());
		assertSame(opened.root, link.getSource());
		assertSame(child, link.getTarget());
		assertNull(link.getDestinationLabel());
	}
}
