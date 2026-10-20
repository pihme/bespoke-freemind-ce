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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.event.MenuEvent;

import org.junit.jupiter.api.Test;

import freemind.controller.FreeMindPopupMenu;
import freemind.controller.MenuItemEnabledListener;
import freemind.controller.MenuItemSelectedListener;
import freemind.controller.StructuredMenuHolder;
import freemind.controller.StructuredMenuItemHolder;
import freemind.main.HeadlessFreeMind;
import freemind.main.Resources;

/** A structured menu keeps its items, separators, and listener results. */
class StructuredMenuHolderBehaviourTest {

	@Test
	void menusKeepTheirItemsAndAPrintedTreeIndentsCategories() {
		// backwards compatibility
		new HeadlessFreeMind();
		StructuredMenuHolder holder = new StructuredMenuHolder();
		JMenu file = new JMenu("File");
		holder.addMenu(file, "root/file/.");
		Action save = action("Save");
		JMenuItem added = holder.addAction(save, "root/file/save");
		holder.addSeparator("root/file");
		holder.addCategory("root/extra");
		holder.addSeparator("pop");
		holder.addSeparator("pop");

		JMenuBar bar = new JMenuBar();
		holder.updateMenus(bar, "root/");
		assertSame(file, bar.getMenu(0));
		assertSame(save, added.getAction());
		assertTrue(bar.getMenu(0).getItemCount() >= 1);

		JToolBar tools = new JToolBar();
		holder.updateMenus(tools, "root/");
		assertTrue(tools.getComponentCount() >= 1);

		FreeMindPopupMenu popup = new FreeMindPopupMenu();
		holder.updateMenus(popup, "pop/");
		assertEquals(1, popup.getComponentCount());
		assertTrue(popup.getComponent(0) instanceof JPopupMenu.Separator);

		String printed = holder.toString();
		assertTrue(printed.contains("JMenuItem"));
		assertTrue(printed.contains("Separator"));
		assertTrue(printed.contains("Category"));
		assertTrue(printed.contains("\n  "));
	}

	@Test
	void aShortMenuOpensANextMenuAfterTheVisibleLimit() {
		// backwards compatibility
		new HeadlessFreeMind();
		String key = StructuredMenuHolder.AMOUNT_OF_VISIBLE_MENU_ITEMS;
		String previous = Resources.getInstance().getProperties().getProperty(key);
		Resources.getInstance().getProperties().setProperty(key, "1");
		try {
			StructuredMenuHolder holder = new StructuredMenuHolder();
			JMenu file = new JMenu("File");
			holder.addMenu(file, "root/file/.");
			holder.addAction(action("One"), "root/file/one");
			holder.addAction(action("Two"), "root/file/two");
			holder.addAction(action("Three"), "root/file/three");
			JMenuBar bar = new JMenuBar();
			holder.updateMenus(bar, "root/");
			JMenu shown = bar.getMenu(0);
			assertEquals(2, shown.getMenuComponentCount());
			JMenu next = (JMenu) shown.getMenuComponent(1);
			assertEquals(Resources.getInstance().getResourceString("StructuredMenuHolder.next"),
					next.getText());
			assertEquals(2, next.getItemCount());
		} finally {
			if (previous == null) {
				Resources.getInstance().getProperties().remove(key);
			} else {
				Resources.getInstance().getProperties().setProperty(key, previous);
			}
		}
	}

	@Test
	void selectingAMenuAppliesEnabledAndSelectedListeners() {
		// backwards compatibility
		new HeadlessFreeMind();
		StructuredMenuHolder.StructuredMenuListener listener =
				new StructuredMenuHolder.StructuredMenuListener();
		Checking check = new Checking(true, true);
		JCheckBoxMenuItem box = new JCheckBoxMenuItem(check);
		listener.addItem(holder(check, box));
		listener.menuSelected(new MenuEvent(box));
		assertTrue(box.isSelected());
		assertTrue(check.enabled);

		PlainItem plain = new PlainItem(true);
		JMenuItem item = new JMenuItem(plain);
		listener.addItem(holder(plain, item));
		listener.menuSelected(new MenuEvent(item));
		Icon selectedIcon = item.getIcon();
		assertTrue(selectedIcon.getIconWidth() > 0);

		plain.selected = false;
		listener.menuSelected(new MenuEvent(item));
		assertNotSame(selectedIcon, item.getIcon());

		listener.menuDeselected(new MenuEvent(item));
		listener.menuCanceled(new MenuEvent(item));
		assertNotSame(selectedIcon, item.getIcon());

		ThrowingEnabled throwing = new ThrowingEnabled();
		JCheckBoxMenuItem broken = new JCheckBoxMenuItem(throwing);
		listener.addItem(holder(throwing, broken));
		listener.menuSelected(new MenuEvent(broken));
		assertFalse(throwing.isEnabled());
		assertFalse(throwing.selectedAsked);
	}

	private static StructuredMenuItemHolder holder(Action action, JMenuItem item) {
		StructuredMenuItemHolder holder = new StructuredMenuItemHolder();
		holder.setAction(action);
		holder.setMenuItem(item);
		return holder;
	}

	private static Action action(String name) {
		return new AbstractAction(name) {
			@Override
			public void actionPerformed(ActionEvent event) {
			}
		};
	}

	private static final class Checking extends AbstractAction
			implements MenuItemEnabledListener, MenuItemSelectedListener {
		private final boolean enabledResult;
		private final boolean selectedResult;
		private boolean enabled;

		Checking(boolean enabledResult, boolean selectedResult) {
			super("check");
			this.enabledResult = enabledResult;
			this.selectedResult = selectedResult;
		}

		@Override
		public void actionPerformed(ActionEvent event) {
		}

		@Override
		public boolean isEnabled(JMenuItem item, Action action) {
			enabled = true;
			return enabledResult;
		}

		@Override
		public boolean isSelected(JMenuItem item, Action action) {
			return selectedResult;
		}
	}

	private static final class PlainItem extends AbstractAction implements MenuItemSelectedListener {
		private boolean selected;

		PlainItem(boolean selected) {
			super("plain");
			this.selected = selected;
		}

		@Override
		public void actionPerformed(ActionEvent event) {
		}

		@Override
		public boolean isSelected(JMenuItem item, Action action) {
			return selected;
		}
	}

	private static final class ThrowingEnabled extends AbstractAction
			implements MenuItemEnabledListener, MenuItemSelectedListener {
		private boolean selectedAsked;

		ThrowingEnabled() {
			super("throw");
		}

		@Override
		public void actionPerformed(ActionEvent event) {
		}

		@Override
		public boolean isEnabled(JMenuItem item, Action action) {
			throw new IllegalStateException("enabled");
		}

		@Override
		public boolean isSelected(JMenuItem item, Action action) {
			selectedAsked = true;
			return true;
		}
	}
}
