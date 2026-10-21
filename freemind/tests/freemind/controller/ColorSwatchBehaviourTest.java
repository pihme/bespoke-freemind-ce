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

package freemind.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Package-private swatch. The test lives in this package so it can read the color. */
@DisplayName("ColorSwatchBehaviourTest")
class ColorSwatchBehaviourTest {

	@Test
	void aSwatchIsElevenPixelsAndPaintsABlackBorderAroundItsColor() {
		// backwards compatibility
		ColorSwatch swatch = new ColorSwatch(Color.RED);
		assertEquals(11, swatch.getIconWidth());
		assertEquals(11, swatch.getIconHeight());
		assertEquals(Color.RED, swatch.getColor());

		BufferedImage image = new BufferedImage(11, 11, BufferedImage.TYPE_INT_RGB);
		Graphics graphics = image.getGraphics();
		swatch.paintIcon(null, graphics, 0, 0);
		graphics.dispose();
		assertEquals(Color.BLACK.getRGB(), image.getRGB(0, 0));
		assertEquals(Color.RED.getRGB(), image.getRGB(5, 5));
	}

	@Test
	void theDefaultColorIsWhite() {
		// backwards compatibility
		ColorSwatch swatch = new ColorSwatch();
		assertEquals(Color.WHITE, swatch.getColor());
	}
}
