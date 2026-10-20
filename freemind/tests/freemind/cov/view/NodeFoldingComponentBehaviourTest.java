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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.lang.reflect.Field;

import javax.swing.Timer;

import org.junit.jupiter.api.Test;

import freemind.modes.NodeAdapter;
import freemind.view.mindmapview.NodeFoldingComponent;
import freemind.view.mindmapview.NodeView;
import tests.freemind.cov.MapFixture.Opened;

public class NodeFoldingComponentBehaviourTest {

	@Test
	void foldingButtonPaintsEnteredFoldedAndIdleStates() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha");
		opened.frame.setProperty("resources_display_folding_buttons", "true");
		NodeView alpha = ViewMaps.childView(opened, "Alpha");
		ViewMaps.addChild(alpha, "Grand", false);
		opened.view.validate();
		ViewMaps.call(alpha, "update", new Class<?>[] {}, new Object[] {});
		NodeFoldingComponent fold = new NodeFoldingComponent(alpha);
		fold.setSize(fold.getPreferredSize());
		assertTrue(fold.getPreferredSize().width > 0);
		Timer timer = timer(fold);
		try {
			assertTrue(ViewMaps.ink(ViewMaps.paint(fold)) >= 0);
			mouse(fold, MouseEvent.MOUSE_ENTERED, fold.getWidth() / 2, fold.getHeight() / 2);
			assertTrue(timer.isRunning());
			for (ActionListener listener : timer.getActionListeners()) {
				for (int i = 0; i < 4; i++) {
					listener.actionPerformed(new ActionEvent(timer, ActionEvent.ACTION_PERFORMED, "tick"));
				}
			}
			assertTrue(ViewMaps.ink(ViewMaps.paint(fold)) > 0);
			mouse(fold, MouseEvent.MOUSE_EXITED, 0, 0);
			assertTrue(ViewMaps.ink(ViewMaps.paint(fold)) > 0);
			alpha.getModel().setFolded(true);
			assertTrue(ViewMaps.ink(ViewMaps.paint(fold)) > 0);
			for (ActionListener listener : timer.getActionListeners()) {
				for (int i = 0; i < 20; i++) {
					listener.actionPerformed(new ActionEvent(timer, ActionEvent.ACTION_PERFORMED, "tick"));
				}
			}
			assertFalse(timer.isRunning());
			assertTrue(ViewMaps.ink(ViewMaps.paint(fold)) > 0);
			mouse(fold, MouseEvent.MOUSE_PRESSED, fold.getWidth() / 2, fold.getHeight() / 2);
			mouse(fold, MouseEvent.MOUSE_PRESSED, 0, 0);
			mouse(fold, MouseEvent.MOUSE_RELEASED, 1, 1);
			mouse(fold, MouseEvent.MOUSE_CLICKED, 1, 1);
			mouse(fold, MouseEvent.MOUSE_MOVED, fold.getWidth() / 2, fold.getHeight() / 2);
			mouse(fold, MouseEvent.MOUSE_MOVED, 0, 0);
			ViewMaps.call(fold, "startTimer", new Class<?>[] {}, new Object[] {});
			assertTrue(timer.isRunning());
			ViewMaps.call(fold, "startTimer", new Class<?>[] {}, new Object[] {});
			assertTrue(timer.isRunning());
			ViewMaps.call(fold, "stopTimer", new Class<?>[] {}, new Object[] {});
			assertFalse(timer.isRunning());
			ViewMaps.call(fold, "stopTimer", new Class<?>[] {}, new Object[] {});
			assertFalse(timer.isRunning());
			Point anchor = new Point(80, 40);
			((NodeAdapter) alpha.getModel()).setLeft(false);
			fold.setCorrectedLocation(anchor);
			Point right = fold.getLocation();
			((NodeAdapter) alpha.getModel()).setLeft(true);
			fold.setCorrectedLocation(anchor);
			assertNotEquals(right, fold.getLocation());
			assertEquals(alpha, fold.getNodeView());
		} finally {
			fold.dispose();
			assertFalse(timer.isRunning());
		}
	}

	private static Timer timer(NodeFoldingComponent fold) throws Exception {
		Field field = NodeFoldingComponent.class.getDeclaredField("mTimer");
		field.setAccessible(true);
		return (Timer) field.get(fold);
	}

	private static void mouse(NodeFoldingComponent fold, int id, int x, int y) {
		int button = id == MouseEvent.MOUSE_PRESSED || id == MouseEvent.MOUSE_RELEASED
				|| id == MouseEvent.MOUSE_CLICKED ? MouseEvent.BUTTON1 : MouseEvent.NOBUTTON;
		MouseEvent event = new MouseEvent(fold, id, System.currentTimeMillis(), 0, x, y, 1, false, button);
		if (id == MouseEvent.MOUSE_MOVED) {
			for (MouseMotionListener listener : fold.getMouseMotionListeners()) {
				listener.mouseMoved(event);
			}
			return;
		}
		for (MouseListener listener : fold.getMouseListeners()) {
			if (id == MouseEvent.MOUSE_ENTERED) {
				listener.mouseEntered(event);
			} else if (id == MouseEvent.MOUSE_EXITED) {
				listener.mouseExited(event);
			} else if (id == MouseEvent.MOUSE_PRESSED) {
				listener.mousePressed(event);
			} else if (id == MouseEvent.MOUSE_RELEASED) {
				listener.mouseReleased(event);
			} else if (id == MouseEvent.MOUSE_CLICKED) {
				listener.mouseClicked(event);
			}
		}
	}
}
