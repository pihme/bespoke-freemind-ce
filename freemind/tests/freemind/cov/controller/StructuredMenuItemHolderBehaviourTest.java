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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.controller.MenuItemEnabledListener;
import freemind.controller.MenuItemSelectedListener;
import freemind.controller.StructuredMenuItemHolder;

/** A menu holder keeps the item, the action, and listeners the action itself implements. */
@DisplayName("StructuredMenuItemHolderBehaviourTest")
class StructuredMenuItemHolderBehaviourTest {

	@Test
	void aPlainActionDoesNotInstallListeners() {
		// backwards compatibility
		StructuredMenuItemHolder holder = new StructuredMenuItemHolder();
		Action action = new AbstractAction("plain") {
			@Override
			public void actionPerformed(ActionEvent event) {
			}
		};
		holder.setAction(action);
		assertSame(action, holder.getAction());
		assertNull(holder.getEnabledListener());
		assertNull(holder.getSelectionListener());
	}

	@Test
	void anActionThatIsAListenerIsStoredAsThatListener() {
		// backwards compatibility
		StructuredMenuItemHolder holder = new StructuredMenuItemHolder();
		Both action = new Both();
		JMenuItem item = new JMenuItem();
		holder.setAction(action);
		holder.setMenuItem(item);

		assertSame(action, holder.getEnabledListener());
		assertSame(action, holder.getSelectionListener());
		assertSame(item, holder.getMenuItem());
		assertTrue(holder.getEnabledListener().isEnabled(item, action));
		assertTrue(holder.getSelectionListener().isSelected(item, action));
	}

	private static final class Both extends AbstractAction
			implements MenuItemEnabledListener, MenuItemSelectedListener {
		private Both() {
			super("both");
		}

		@Override
		public void actionPerformed(ActionEvent event) {
		}

		@Override
		public boolean isEnabled(JMenuItem item, Action action) {
			return item != null && action == this;
		}

		@Override
		public boolean isSelected(JMenuItem item, Action action) {
			return item != null && action == this;
		}
	}
}
