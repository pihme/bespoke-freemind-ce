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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.controller.NodeMouseMotionListener;

/** The node mouse listener forwards events and the selection update only while registered. */
@DisplayName("NodeMouseMotionListenerBehaviourTest")
class NodeMouseMotionListenerBehaviourTest {

	@Test
	void aRegisteredHandlerReceivesMotionSelectionUpdatesAndDeregisterStopsThem() {
		// backwards compatibility
		NodeMouseMotionListener listener = new NodeMouseMotionListener(null);
		int[] counts = new int[8];
		listener.register(new NodeMouseMotionListener.NodeMouseMotionObserver() {
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

			public void updateSelectionMethod() {
				counts[7] += 1;
			}
		});
		fire(listener, 3);
		listener.updateSelectionMethod();
		assertEquals(3, counts[0]);
		assertEquals(3, counts[5]);
		assertEquals(3, counts[6]);
		assertEquals(1, counts[7]);

		listener.deregister();
		fire(listener, 9);
		listener.updateSelectionMethod();
		assertEquals(3, counts[0]);
		assertEquals(1, counts[7]);
	}

	private static void fire(NodeMouseMotionListener listener, int x) {
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
