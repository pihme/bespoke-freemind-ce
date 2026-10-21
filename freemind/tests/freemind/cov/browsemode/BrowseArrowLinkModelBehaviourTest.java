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

package tests.freemind.cov.browsemode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.Point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.browsemode.BrowseArrowLinkModel;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** A browse link names its ends and ignores a drag. */
@DisplayName("BrowseArrowLinkModelBehaviourTest")
class BrowseArrowLinkModelBehaviourTest {

	@Test
	void textNamesTheEndsAndACloneKeepsTheInclination() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		BrowseArrowLinkModel link = new BrowseArrowLinkModel(opened.root, opened.child("Child"), opened.mode);
		link.setStartInclination(new Point(3, 4));
		assertEquals("Source=ROOT, target=Child", link.toString());
		assertNull(link.save());

		BrowseArrowLinkModel copy = (BrowseArrowLinkModel) link.clone();
		link.setStartInclination(new Point(9, 9));
		link.changeInclination(opened.view, 1, 2, 3, 4);
		assertSame(opened.root, copy.getSource());
		assertSame(opened.child("Child"), copy.getTarget());
		assertEquals(new Point(3, 4), copy.getStartInclination());
		assertEquals(new Point(9, 9), link.getStartInclination());
		assertNull(copy.save());
	}
}
