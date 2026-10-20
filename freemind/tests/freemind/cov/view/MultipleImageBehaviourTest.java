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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

import org.junit.jupiter.api.Test;

import freemind.view.mindmapview.MultipleImage;

public class MultipleImageBehaviourTest {

	@Test
	void aZeroZoomImageStaysEmptyAndADrawnImageIsCached() {
		// backwards compatibility
		BufferedImage source = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
		MultipleImage empty = new MultipleImage(0);
		empty.addImage(new ImageIcon(source));
		assertNull(empty.getImage());

		MultipleImage drawn = new MultipleImage(1);
		drawn.addImage(new ImageIcon(source));
		drawn.addImage(new ImageIcon(source));
		assertSame(drawn.getImage(), drawn.getImage());
	}
}
