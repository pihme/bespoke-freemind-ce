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

import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.junit.jupiter.api.Test;

import freemind.controller.FreeMindPopupMenu;
import freemind.main.HeadlessFreeMind;

/** Popup menu events reach listeners added on the FreeMind menu, and stop after removal. */
class FreeMindPopupMenuBehaviourTest {

	@Test
	void showHideAndCancelReachTheAddedListenerUntilItIsRemoved() {
		// backwards compatibility
		new HeadlessFreeMind();
		OpenMenu menu = new OpenMenu();
		int[] counts = new int[3];
		MenuListener listener = new MenuListener() {
			public void menuSelected(MenuEvent event) {
				counts[0] += 1;
			}

			public void menuDeselected(MenuEvent event) {
				counts[1] += 1;
			}

			public void menuCanceled(MenuEvent event) {
				counts[2] += 1;
			}
		};
		menu.addMenuListener(listener);
		menu.visible();
		menu.invisible();
		menu.canceled();
		assertEquals(1, counts[0]);
		assertEquals(1, counts[1]);
		assertEquals(1, counts[2]);

		menu.removeMenuListener(listener);
		menu.visible();
		menu.invisible();
		menu.canceled();
		assertEquals(1, counts[0]);
		assertEquals(1, counts[1]);
		assertEquals(1, counts[2]);
	}

	private static final class OpenMenu extends FreeMindPopupMenu {
		void visible() {
			firePopupMenuWillBecomeVisible();
		}

		void invisible() {
			firePopupMenuWillBecomeInvisible();
		}

		void canceled() {
			firePopupMenuCanceled();
		}
	}
}
