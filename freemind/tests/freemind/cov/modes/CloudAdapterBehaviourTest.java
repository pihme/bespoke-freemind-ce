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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.lang.reflect.Field;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.main.FreeMind;
import freemind.main.Tools;
import freemind.main.XMLElement;
import freemind.modes.CloudAdapter;
import freemind.modes.MindMapCloud;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.MindMapCloudModel;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Cloud level, saved attributes, and the standard style follow the model. */
@DisplayName("CloudAdapterBehaviourTest")
class CloudAdapterBehaviourTest {

	@Test
	void iterativeLevelCountsAncestorCloudsAndAcceptsADelta() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode child = opened.child("Child");
		MindMapCloud parentCloud = new MindMapCloudModel(opened.root, opened.mode);
		MindMapCloud childCloud = new MindMapCloudModel(child, opened.mode);
		opened.root.setCloud(parentCloud);
		child.setCloud(childCloud);
		assertEquals(0, parentCloud.getIterativeLevel());
		assertEquals(1, childCloud.getIterativeLevel());
		childCloud.changeIterativeLevel(2);
		assertEquals(3, childCloud.getIterativeLevel());
		MindMapCloud untouched = new MindMapCloudModel(child, opened.mode);
		untouched.changeIterativeLevel(4);
		assertEquals(1, untouched.getIterativeLevel());
	}

	@Test
	void saveWritesStyleColorAndWidth() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapCloudModel cloud = new MindMapCloudModel(opened.child("Child"), opened.mode);
		cloud.setStyle("bezier");
		cloud.setColor(Color.BLUE);
		cloud.setWidth(4);
		XMLElement saved = cloud.save();
		assertEquals("cloud", saved.getName());
		assertEquals("bezier", saved.getAttribute("STYLE"));
		assertEquals(Tools.colorToXml(Color.BLUE), saved.getAttribute("COLOR"));
		assertEquals("4", saved.getAttribute("WIDTH"));
	}

	@Test
	void propertyChangeAndAClearedStandardStyleFollowTheFrame() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		String colorKey = FreeMind.RESOURCES_CLOUD_COLOR;
		String styleKey = CloudAdapter.RESOURCES_STANDARDCLOUDSTYLE;
		String previousColor = opened.frame.getProperty(colorKey);
		String previousStyle = opened.frame.getProperty(styleKey);
		Field style = CloudAdapter.class.getDeclaredField("standardStyle");
		style.setAccessible(true);
		String previousStatic = (String) style.get(null);
		try {
			new MindMapCloudModel(opened.root, opened.mode);
			opened.mode.setProperty(colorKey, "#abcdef");
			opened.mode.setProperty(styleKey, "linear");
			MindMapCloudModel heard = new MindMapCloudModel(opened.child("Child"), opened.mode);
			assertEquals(Tools.xmlToColor("#abcdef"), heard.getColor());
			style.set(null, null);
			opened.frame.setProperty(styleKey, "round");
			MindMapCloudModel assigned = new MindMapCloudModel(opened.child("Child"), opened.mode);
			assertEquals("round", assigned.getStyle());
			assertTrue(assigned.getExteriorColor() != null);
		} finally {
			style.set(null, previousStatic);
			restore(opened, colorKey, previousColor);
			restore(opened, styleKey, previousStyle);
		}
	}

	private static void restore(Opened opened, String key, String previous) {
		if (previous != null) {
			opened.mode.setProperty(key, previous);
		}
	}
}
