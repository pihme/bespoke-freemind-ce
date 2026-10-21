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

package tests.freemind.cov.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.controller.BlindIcon;

/** A spacer icon reports its length and paints nothing. */
@DisplayName("BlindIconBehaviourTest")
class BlindIconBehaviourTest {

	@Test
	void lengthIsBothTheWidthAndTheHeight() {
		// backwards compatibility
		BlindIcon icon = new BlindIcon(12);
		assertEquals(12, icon.getIconWidth());
		assertEquals(12, icon.getIconHeight());
	}

	@Test
	void paintIconLeavesThePixelsUnchanged() {
		// backwards compatibility
		BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
		image.setRGB(3, 4, 0x010203);
		Graphics graphics = image.getGraphics();
		new BlindIcon(12).paintIcon(null, graphics, 3, 4);
		graphics.dispose();
		assertEquals(0x010203, image.getRGB(3, 4) & 0xffffff);
	}
}
