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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.NodeAdapter;
import freemind.view.mindmapview.NodeMotionListenerView;
import freemind.view.mindmapview.NodeView;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("NodeMotionListenerViewBehaviourTest")
public class NodeMotionListenerViewBehaviourTest {

	@Test
	void enteredHandlePaintsAGapWarning() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha");
		NodeView alpha = ViewMaps.childView(opened, "Alpha");
		NodeMotionListenerView motion = alpha.getMotionListenerView();
		assertNotNull(motion);
		assertEquals(alpha, motion.getMovedView());
		motion.setSize(24, 30);
		assertFalse(motion.isMouseEntered());
		int idle = ViewMaps.ink(ViewMaps.paint(motion));
		((NodeAdapter) alpha.getModel()).setHGap(0);
		motion.setMouseEntered();
		assertTrue(motion.isMouseEntered());
		int tight = ViewMaps.ink(ViewMaps.paint(motion));
		assertTrue(tight > idle);
		((NodeAdapter) alpha.getModel()).setHGap(12);
		int loose = ViewMaps.ink(ViewMaps.paint(motion));
		assertTrue(loose > idle);
		motion.setMouseExited();
		assertFalse(motion.isMouseEntered());
		assertEquals(idle, ViewMaps.ink(ViewMaps.paint(motion)));
	}
}
