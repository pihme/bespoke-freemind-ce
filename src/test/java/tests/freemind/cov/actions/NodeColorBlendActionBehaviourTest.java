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

package tests.freemind.cov.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.actions.NodeColorBlendAction;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Blending pulls the node color a quarter of the way toward the map background. */
@DisplayName("NodeColorBlendActionBehaviourTest")
class NodeColorBlendActionBehaviourTest {

	@Test
	void blendMovesAQuarterTowardTheMapBackground() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Leaf");
		MindMapNode leaf = opened.child("Leaf");
		Color nodeColor = new Color(40, 80, 120);
		opened.mode.setNodeColor(leaf, nodeColor);
		opened.mode.select(leaf, List.of(leaf));
		Color mapColor = opened.view.getBackground();
		new NodeColorBlendAction(opened.mode).actionPerformed(
				new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "blend"));
		assertEquals(new Color((3 * mapColor.getRed() + nodeColor.getRed()) / 4,
				(3 * mapColor.getGreen() + nodeColor.getGreen()) / 4,
				(3 * mapColor.getBlue() + nodeColor.getBlue()) / 4), leaf.getColor());
	}
}
