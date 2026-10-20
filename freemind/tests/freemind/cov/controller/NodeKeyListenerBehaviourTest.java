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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JPanel;

import org.junit.jupiter.api.Test;

import freemind.controller.NodeKeyListener;

/** The node key listener forwards keys only while a handler is registered. */
class NodeKeyListenerBehaviourTest {

	@Test
	void aRegisteredHandlerReceivesEachKeyAndDeregisterStopsIt() {
		// backwards compatibility
		NodeKeyListener listener = new NodeKeyListener(null);
		AtomicInteger pressed = new AtomicInteger();
		AtomicInteger released = new AtomicInteger();
		AtomicInteger typed = new AtomicInteger();
		KeyListener handler = new KeyListener() {
			public void keyPressed(KeyEvent event) {
				pressed.addAndGet(event.getKeyCode());
			}

			public void keyReleased(KeyEvent event) {
				released.addAndGet(event.getKeyCode());
			}

			public void keyTyped(KeyEvent event) {
				typed.addAndGet(event.getKeyChar());
			}
		};
		listener.register(handler);
		listener.keyPressed(key(KeyEvent.KEY_PRESSED, KeyEvent.VK_A, 'a'));
		listener.keyReleased(key(KeyEvent.KEY_RELEASED, KeyEvent.VK_B, 'b'));
		listener.keyTyped(key(KeyEvent.KEY_TYPED, KeyEvent.VK_UNDEFINED, 'c'));
		assertEquals(KeyEvent.VK_A, pressed.get());
		assertEquals(KeyEvent.VK_B, released.get());
		assertEquals('c', typed.get());

		listener.deregister();
		listener.keyPressed(key(KeyEvent.KEY_PRESSED, KeyEvent.VK_D, 'd'));
		listener.keyReleased(key(KeyEvent.KEY_RELEASED, KeyEvent.VK_E, 'e'));
		listener.keyTyped(key(KeyEvent.KEY_TYPED, KeyEvent.VK_UNDEFINED, 'f'));
		assertEquals(KeyEvent.VK_A, pressed.get());
		assertEquals(KeyEvent.VK_B, released.get());
		assertEquals('c', typed.get());
	}

	private static KeyEvent key(int id, int code, char character) {
		return new KeyEvent(new JPanel(), id, System.currentTimeMillis(), 0,
				code, character);
	}
}
