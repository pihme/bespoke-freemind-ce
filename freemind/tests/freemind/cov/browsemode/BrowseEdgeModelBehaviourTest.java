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

import java.awt.Color;

import org.junit.jupiter.api.Test;

import freemind.modes.EdgeAdapter;
import freemind.modes.browsemode.BrowseEdgeModel;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** A browse edge stores color and style and does not write XML. */
class BrowseEdgeModelBehaviourTest {

	@Test
	void colorAndStyleAreStoredAndSaveReturnsNull() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		BrowseEdgeModel edge = new BrowseEdgeModel(opened.child("Child"), opened.mode);
		edge.setColor(Color.RED);
		edge.setStyle(EdgeAdapter.EDGESTYLE_BEZIER);
		assertSame(opened.child("Child"), edge.getTarget());
		assertEquals(Color.RED.getRGB(), edge.getColor().getRGB());
		assertEquals(EdgeAdapter.EDGESTYLE_BEZIER, edge.getStyle());
		assertNull(edge.save());
	}
}
