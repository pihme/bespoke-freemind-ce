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

package tests.freemind.cov.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.preferences.layout.VariableSizeCardLayout;

/** A card stack reports the largest visible card, plus gaps and insets. */
@DisplayName("VariableSizeCardLayoutBehaviourTest")
class VariableSizeCardLayoutBehaviourTest {

	@Test
	void anEmptyStackIsOnlyTheGaps() {
		// backwards compatibility
		VariableSizeCardLayout layout = new VariableSizeCardLayout();
		assertEquals(0, layout.getHgap());
		assertEquals(0, layout.getVgap());

		JLabel only = new JLabel();
		only.setPreferredSize(new Dimension(5, 7));
		JPanel parent = new JPanel();
		parent.add(only);
		assertEquals(new Dimension(5, 7), layout.preferredLayoutSize(parent));

		VariableSizeCardLayout gapped = new VariableSizeCardLayout(4, 6);
		assertEquals(new Dimension(8, 12), gapped.preferredLayoutSize(new JPanel()));
	}

	@Test
	void hiddenCardsDoNotCountAndTheVisibleOnesTakeTheMaximum() {
		// backwards compatibility
		VariableSizeCardLayout layout = new VariableSizeCardLayout(4, 6);
		JPanel parent = new JPanel();
		parent.setBorder(new EmptyBorder(1, 2, 3, 4));
		JLabel wide = card(80, 20);
		JLabel tall = card(10, 50);
		tall.setVisible(false);
		parent.add(wide);
		parent.add(tall);

		assertEquals(new Dimension(94, 36), layout.preferredLayoutSize(parent));

		tall.setVisible(true);
		assertEquals(new Dimension(94, 66), layout.preferredLayoutSize(parent));
	}

	private static JLabel card(int width, int height) {
		JLabel label = new JLabel();
		label.setPreferredSize(new Dimension(width, height));
		return label;
	}
}
