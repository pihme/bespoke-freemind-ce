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

import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import org.junit.jupiter.api.Test;

import freemind.controller.NodeMotionListener;

/** The node motion listener forwards mouse events only while a handler is registered. */
class NodeMotionListenerBehaviourTest {

	@Test
	void aRegisteredHandlerReceivesEachMouseEventAndDeregisterStopsIt() {
		// backwards compatibility
		NodeMotionListener listener = new NodeMotionListener(null);
		int[] counts = new int[7];
		listener.register(new NodeMotionListener.NodeMotionAdapter() {
			public void mouseClicked(MouseEvent event) {
				counts[0] += event.getX();
			}

			public void mouseDragged(MouseEvent event) {
				counts[1] += event.getX();
			}

			public void mouseEntered(MouseEvent event) {
				counts[2] += event.getX();
			}

			public void mouseExited(MouseEvent event) {
				counts[3] += event.getX();
			}

			public void mouseMoved(MouseEvent event) {
				counts[4] += event.getX();
			}

			public void mousePressed(MouseEvent event) {
				counts[5] += event.getX();
			}

			public void mouseReleased(MouseEvent event) {
				counts[6] += event.getX();
			}
		});
		fire(listener, 2);
		assertEquals(2, counts[0]);
		assertEquals(2, counts[1]);
		assertEquals(2, counts[2]);
		assertEquals(2, counts[3]);
		assertEquals(2, counts[4]);
		assertEquals(2, counts[5]);
		assertEquals(2, counts[6]);

		listener.deregister();
		fire(listener, 5);
		assertEquals(2, counts[0]);
		assertEquals(2, counts[6]);
	}

	private static void fire(NodeMotionListener listener, int x) {
		listener.mouseClicked(mouse(MouseEvent.MOUSE_CLICKED, x));
		listener.mouseDragged(mouse(MouseEvent.MOUSE_DRAGGED, x));
		listener.mouseEntered(mouse(MouseEvent.MOUSE_ENTERED, x));
		listener.mouseExited(mouse(MouseEvent.MOUSE_EXITED, x));
		listener.mouseMoved(mouse(MouseEvent.MOUSE_MOVED, x));
		listener.mousePressed(mouse(MouseEvent.MOUSE_PRESSED, x));
		listener.mouseReleased(mouse(MouseEvent.MOUSE_RELEASED, x));
	}

	private static MouseEvent mouse(int id, int x) {
		return new MouseEvent(new JPanel(), id, System.currentTimeMillis(), 0,
				x, 0, 1, false);
	}
}
