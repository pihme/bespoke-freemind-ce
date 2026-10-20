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

import java.awt.Color;
import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import freemind.main.FreeMind;
import freemind.main.Tools;
import freemind.modes.LinkAdapter;
import freemind.modes.mindmapmode.MindMapArrowLinkModel;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Link color and style follow the standard properties, including the first assignment. */
class LinkAdapterBehaviourTest {

	@Test
	void propertyChangeUpdatesColorAndStyle() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		new MindMapArrowLinkModel(opened.root, opened.child("Child"), opened.mode);
		String colorKey = FreeMind.RESOURCES_LINK_COLOR;
		String styleKey = LinkAdapter.RESOURCES_STANDARDLINKSTYLE;
		String previousColor = opened.frame.getProperty(colorKey);
		String previousStyle = opened.frame.getProperty(styleKey);
		try {
			opened.mode.setProperty(colorKey, "#00ff00");
			opened.mode.setProperty(styleKey, "dashed");
			MindMapArrowLinkModel link = new MindMapArrowLinkModel(opened.root, opened.child("Child"), opened.mode);
			assertEquals(Tools.xmlToColor("#00ff00"), link.getColor());
			assertEquals("dashed", link.getStyle());
		} finally {
			restore(opened, colorKey, previousColor);
			restore(opened, styleKey, previousStyle);
		}
	}

	@Test
	void aMissingStandardStyleIsTakenFromTheProperty() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		Field style = LinkAdapter.class.getDeclaredField("standardStyle");
		style.setAccessible(true);
		String previousStyle = (String) style.get(null);
		String key = LinkAdapter.RESOURCES_STANDARDLINKSTYLE;
		String previous = opened.frame.getProperty(key);
		style.set(null, null);
		opened.frame.setProperty(key, "solid");
		try {
			MindMapArrowLinkModel link = new MindMapArrowLinkModel(opened.root, opened.child("Child"), opened.mode);
			assertEquals("solid", link.getStyle());
			assertEquals(Color.class, link.getColor().getClass());
		} finally {
			style.set(null, previousStyle);
			restore(opened, key, previous);
		}
	}

	private static void restore(Opened opened, String key, String previous) {
		if (previous != null) {
			opened.mode.setProperty(key, previous);
		}
	}
}
