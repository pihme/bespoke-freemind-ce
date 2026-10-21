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

import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.controller.FreeMindToolBar;

/** Toolbar buttons keep the action name without its mnemonic marker, and show no text. */
@DisplayName("FreeMindToolBarBehaviourTest")
class FreeMindToolBarBehaviourTest {

	@Test
	void aNamedBarIsHorizontalAndNotFloatable() {
		// backwards compatibility
		FreeMindToolBar bar = new FreeMindToolBar("Tools");
		assertEquals("Tools", bar.getName());
		assertEquals(JToolBar.HORIZONTAL, bar.getOrientation());
		assertFalse(bar.isFloatable());
		assertEquals(new Insets(0, 0, 0, 0), bar.getMargin());
	}

	@Test
	void anOrientationIsKept() {
		// backwards compatibility
		FreeMindToolBar bar = new FreeMindToolBar(JToolBar.VERTICAL);
		assertEquals(JToolBar.VERTICAL, bar.getOrientation());
		assertFalse(bar.isFloatable());
	}

	@Test
	void addingAnActionStripsTheMnemonicAndClearsTheButtonText() {
		// backwards compatibility
		FreeMindToolBar bar = new FreeMindToolBar();
		Action action = new AbstractAction("&Save") {
			@Override
			public void actionPerformed(ActionEvent event) {
			}
		};
		JButton button = bar.add(action);
		assertEquals("", button.getText());
		assertEquals("&Save", button.getName());
		assertEquals("Save", button.getToolTipText());
		assertEquals("Save", button.getAccessibleContext().getAccessibleName());
		assertFalse(button.isFocusable());
		assertFalse(button.isContentAreaFilled());
		assertEquals(new Insets(0, 0, 0, 0), button.getMargin());
	}
}
