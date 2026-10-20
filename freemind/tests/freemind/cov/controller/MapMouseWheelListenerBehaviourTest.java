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

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JPanel;

import org.junit.jupiter.api.Test;

import freemind.controller.MapMouseWheelListener;

/** The map wheel listener forwards events only while a handler is registered. */
class MapMouseWheelListenerBehaviourTest {

	@Test
	void aRegisteredHandlerReceivesTheRotationAndDeregisterStopsIt() {
		// backwards compatibility
		MapMouseWheelListener listener = new MapMouseWheelListener(null);
		AtomicInteger rotation = new AtomicInteger();
		MouseWheelListener handler = event -> rotation.addAndGet(event.getWheelRotation());
		listener.register(handler);
		listener.mouseWheelMoved(wheel(3));
		assertEquals(3, rotation.get());

		listener.deregister();
		listener.mouseWheelMoved(wheel(4));
		assertEquals(3, rotation.get());
	}

	private static MouseWheelEvent wheel(int rotation) {
		return new MouseWheelEvent(new JPanel(), MouseWheelEvent.MOUSE_WHEEL,
				System.currentTimeMillis(), 0, 0, 0, 0, false,
				MouseWheelEvent.WHEEL_UNIT_SCROLL, 1, rotation);
	}
}
