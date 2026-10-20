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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Cursor;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import freemind.main.FreeMindMain;
import freemind.main.HeadlessFreeMind;
import freemind.view.mindmapview.MapView;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Map mouse motion keeps the selection, consumes presses, and shows the maps popup. */
class MapMouseMotionListenerBehaviourTest {

	@Test
	void dragAndPressReachTheReceiverUntilDeregisterAndEmptyMovesDoNot() {
		// backwards compatibility
		MapMouseMotionListener listener = new MapMouseMotionListener(null);
		int[] counts = new int[3];
		MapMouseMotionListener.MapMouseMotionReceiver receiver = new MapMouseMotionListener.MapMouseMotionReceiver() {
			public void mouseDragged(MouseEvent event) {
				counts[0] += event.getX();
			}

			public void mousePressed(MouseEvent event) {
				counts[1] += event.getX();
			}

			public void mouseReleased(MouseEvent event) {
				counts[2] += event.getX();
			}
		};
		listener.register(receiver);
		MouseEvent dragged = mouse(new JPanel(), MouseEvent.MOUSE_DRAGGED, 4, false);
		MouseEvent pressed = mouse(new JPanel(), MouseEvent.MOUSE_PRESSED, 5, false);
		listener.mouseDragged(dragged);
		listener.mousePressed(pressed);
		listener.mouseMoved(mouse(new JPanel(), MouseEvent.MOUSE_MOVED, 9, false));
		listener.mouseEntered(mouse(new JPanel(), MouseEvent.MOUSE_ENTERED, 9, false));
		listener.mouseExited(mouse(new JPanel(), MouseEvent.MOUSE_EXITED, 9, false));
		assertEquals(4, counts[0]);
		assertEquals(5, counts[1]);
		assertTrue(pressed.isConsumed());
		assertFalse(dragged.isConsumed());

		listener.deregister();
		listener.mouseDragged(mouse(new JPanel(), MouseEvent.MOUSE_DRAGGED, 8, false));
		listener.mousePressed(mouse(new JPanel(), MouseEvent.MOUSE_PRESSED, 8, false));
		assertEquals(4, counts[0]);
		assertEquals(5, counts[1]);
	}

	@Test
	void aClickKeepsTheSelectedNodeAndAReleaseClearsTheMoveCursor() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MapView view = opened.view;
		view.selectAsTheOnlyOneSelected(view.getNodeView(opened.child("Child")));
		MapMouseMotionListener listener = new MapMouseMotionListener(opened.app);
		int[] released = new int[1];
		listener.register(new MapMouseMotionListener.MapMouseMotionReceiver() {
			public void mouseDragged(MouseEvent event) {
			}

			public void mousePressed(MouseEvent event) {
			}

			public void mouseReleased(MouseEvent event) {
				released[0] += event.getX();
			}
		});
		listener.mouseClicked(mouse(view, MouseEvent.MOUSE_CLICKED, 1, false));
		assertEquals(opened.child("Child"), view.getSelected().getModel());

		view.setMoveCursor(true);
		assertEquals(Cursor.MOVE_CURSOR, view.getCursor().getType());
		MouseEvent release = mouse(view, MouseEvent.MOUSE_RELEASED, 6, false);
		listener.mouseReleased(release);
		assertEquals(6, released[0]);
		assertTrue(release.isConsumed());
		assertEquals(Cursor.DEFAULT_CURSOR, view.getCursor().getType());
	}

	@Test
	@Tag("gui")
	void aPopupTriggerShowsTheMapsMenuAndDoesNotStartAPress() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MenuFrame frame = new MenuFrame();
		PopupController controller = new PopupController(frame, opened.view);
		MenuBar bar = new MenuBar(controller);
		JPopupMenu popup = new JPopupMenu();
		bar.mapsPopupMenu = popup;
		frame.bar = bar;
		JFrame window = new JFrame("maps-popup");
		JPanel pad = new JPanel();
		window.add(pad);
		window.pack();
		window.setVisible(true);
		try {
			MapMouseMotionListener listener = new MapMouseMotionListener(controller);
			int[] pressed = new int[1];
			int[] released = new int[1];
			listener.register(new MapMouseMotionListener.MapMouseMotionReceiver() {
				public void mouseDragged(MouseEvent event) {
				}

				public void mousePressed(MouseEvent event) {
					pressed[0] += 1;
				}

				public void mouseReleased(MouseEvent event) {
					released[0] += 1;
				}
			});
			MouseEvent press = mouse(pad, MouseEvent.MOUSE_PRESSED, 2, true);
			listener.mousePressed(press);
			assertEquals(0, pressed[0]);
			assertTrue(press.isConsumed());
			assertTrue(popup.isVisible());
			popup.setVisible(false);

			MouseEvent release = mouse(pad, MouseEvent.MOUSE_RELEASED, 3, true);
			listener.mouseReleased(release);
			assertEquals(1, released[0]);
			assertTrue(release.isConsumed());
			assertTrue(popup.isVisible());
		} finally {
			popup.setVisible(false);
			window.dispose();
		}
	}

	private static MouseEvent mouse(java.awt.Component source, int id, int x, boolean popup) {
		return new MouseEvent(source, id, System.currentTimeMillis(), 0, x, 4, 1, popup);
	}

	private static final class MenuFrame extends HeadlessFreeMind {
		private MenuBar bar;

		@Override
		public MenuBar getFreeMindMenuBar() {
			return bar;
		}
	}

	private static final class PopupController extends Controller {
		private final MapView view;
		private final MenuFrame menuFrame;

		PopupController(MenuFrame menuFrame, MapView view) {
			super(menuFrame);
			this.menuFrame = menuFrame;
			this.view = view;
		}

		@Override
		public MapView getView() {
			return view;
		}

		@Override
		public FreeMindMain getFrame() {
			return menuFrame;
		}
	}
}
