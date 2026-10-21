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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.Color;
import java.awt.Point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.MindMapArrowLink;
import freemind.modes.mindmapmode.MindMapArrowLinkModel;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Arrow tokens and a clone keep the link's own points and arrow strings. */
@DisplayName("ArrowLinkAdapterBehaviourTest")
class ArrowLinkAdapterBehaviourTest {

	@Test
	void startArrowAcceptsNoneAndDefaultAndIgnoresAnUnknownToken() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapArrowLinkModel link = new MindMapArrowLinkModel(opened.root, opened.child("Child"), opened.mode);
		link.setStartArrow(null);
		assertEquals(MindMapArrowLink.ARROW_NONE, link.getStartArrow());
		link.setStartArrow("none");
		assertEquals(MindMapArrowLink.ARROW_NONE, link.getStartArrow());
		link.setStartArrow("default");
		assertEquals(MindMapArrowLink.ARROW_DEFAULT, link.getStartArrow());
		link.setStartArrow("zigzag");
		assertEquals(MindMapArrowLink.ARROW_DEFAULT, link.getStartArrow());
	}

	@Test
	void endArrowAcceptsNoneAndDefaultAndIgnoresAnUnknownToken() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapArrowLinkModel link = new MindMapArrowLinkModel(opened.root, opened.child("Child"), opened.mode);
		link.setEndArrow("NONE");
		assertEquals(MindMapArrowLink.ARROW_NONE, link.getEndArrow());
		link.setEndArrow("DEFAULT");
		assertEquals(MindMapArrowLink.ARROW_DEFAULT, link.getEndArrow());
		link.setEndArrow("zigzag");
		assertEquals(MindMapArrowLink.ARROW_DEFAULT, link.getEndArrow());
		link.setEndArrow(null);
		assertEquals(MindMapArrowLink.ARROW_NONE, link.getEndArrow());
	}

	@Test
	void cloneCopiesInclinationsAndLeavesNullInclinationsNull() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapArrowLinkModel link = new MindMapArrowLinkModel(opened.root, opened.child("Child"), opened.mode);
		link.setColor(Color.BLUE);
		link.setStartInclination(new Point(3, 4));
		link.setEndInclination(new Point(5, 6));
		link.setStartArrow(MindMapArrowLink.ARROW_DEFAULT);
		MindMapArrowLinkModel copy = (MindMapArrowLinkModel) link.clone();
		assertEquals(Color.BLUE.getRGB(), copy.getColor().getRGB());
		assertNotSame(link.getStartInclination(), copy.getStartInclination());
		assertEquals(new Point(3, 4), copy.getStartInclination());
		assertEquals(new Point(5, 6), copy.getEndInclination());
		assertEquals(MindMapArrowLink.ARROW_DEFAULT, copy.getStartArrow());
		link.setColor(Color.RED);
		assertEquals(Color.BLUE.getRGB(), copy.getColor().getRGB());

		MindMapArrowLinkModel bare = new MindMapArrowLinkModel(opened.root, opened.child("Child"), opened.mode);
		MindMapArrowLinkModel bareCopy = (MindMapArrowLinkModel) bare.clone();
		assertNull(bareCopy.getStartInclination());
		assertNull(bareCopy.getEndInclination());
		assertEquals(MindMapArrowLink.ARROW_NONE, bareCopy.getStartArrow());
		assertEquals(MindMapArrowLink.ARROW_DEFAULT, bareCopy.getEndArrow());
	}
}
