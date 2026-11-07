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

package tests.freemind.cov.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Rectangle;
import java.io.File;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.extensions.NodeHook;
import freemind.view.mindmapview.IndependantMapViewCreator;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("IndependantMapViewCreatorBehaviourTest")
public class IndependantMapViewCreatorBehaviourTest {

	@Test
	void printToFileWritesAPngAndAScaledPng() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha");
		opened.view.validate();
		IndependantMapViewCreator creator = new IndependantMapViewCreator();
		NodeHook hook = creator.createNodeHook("plugins.missing", opened.root);
		assertEquals("plugins.missing", hook.getName());
		Rectangle inner = opened.view.getInnerBounds();
		assertTrue(inner.width > 0);
		assertTrue(inner.height > 0);
		File png = File.createTempFile("freemind-view", ".png");
		try {
			IndependantMapViewCreator.printToFile(opened.view, png.getAbsolutePath(), false, 0);
			long full = png.length();
			assertTrue(full > 0);
			IndependantMapViewCreator.printToFile(opened.view, png.getAbsolutePath(), true, 80);
			assertTrue(png.length() > 0);
		} finally {
			png.delete();
		}
	}
}
