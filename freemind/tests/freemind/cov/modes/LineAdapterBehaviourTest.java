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
import java.lang.reflect.Field;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.main.FreeMind;
import freemind.modes.CloudAdapter;
import freemind.modes.LineAdapter;
import freemind.modes.mindmapmode.MindMapCloudModel;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** A line copies its color on clone and falls back to red when the standard color is unreadable. */
@DisplayName("LineAdapterBehaviourTest")
class LineAdapterBehaviourTest {

	@Test
	void cloudRealWidthIsTheNormalWidth() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		LineAdapter cloud = new MindMapCloudModel(opened.child("Child"), opened.mode);
		assertEquals(3, cloud.getRealWidth());
	}

	@Test
	void cloneKeepsANullColorAndCopiesASetColor() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapCloudModel cloud = new MindMapCloudModel(opened.child("Child"), opened.mode);
		MindMapCloudModel bare = (MindMapCloudModel) cloud.clone();
		assertNull(colorField(bare));
		cloud.setColor(Color.BLUE);
		assertNull(colorField(bare));
		MindMapCloudModel copied = (MindMapCloudModel) cloud.clone();
		assertEquals(Color.BLUE.getRGB(), colorField(copied).getRGB());
		assertNotSame(colorField(cloud), colorField(copied));
		cloud.setColor(Color.RED);
		assertEquals(Color.BLUE.getRGB(), colorField(copied).getRGB());
	}

	@Test
	void unreadableStandardColorBecomesRed() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		Field color = CloudAdapter.class.getDeclaredField("standardColor");
		color.setAccessible(true);
		Color previousColor = (Color) color.get(null);
		String key = FreeMind.RESOURCES_CLOUD_COLOR;
		String previous = opened.frame.getProperty(key);
		color.set(null, null);
		opened.frame.setProperty(key, "zz");
		try {
			MindMapCloudModel cloud = new MindMapCloudModel(opened.child("Child"), opened.mode);
			assertEquals(Color.RED, cloud.getColor());
		} finally {
			color.set(null, previousColor);
			if (previous != null) {
				opened.mode.setProperty(key, previous);
			}
		}
	}

	private static Color colorField(LineAdapter line) throws Exception {
		Field field = LineAdapter.class.getDeclaredField("color");
		field.setAccessible(true);
		return (Color) field.get(line);
	}
}
