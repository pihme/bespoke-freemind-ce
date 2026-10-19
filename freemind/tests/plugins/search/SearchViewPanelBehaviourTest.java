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

package plugins.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JRadioButton;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Builds the multi-map search window. It does not run a search.
 */
@Tag("gui")
@DisplayName("Search window lists no results until a search")
class SearchViewPanelBehaviourTest {

	@Test
	void TestREQ_PLUG_03_SearchWindowListsNoResultsUntilASearch() {
		JFrame owner = new JFrame();
		SearchViewPanel panel = new SearchViewPanel(new ISearchController() {
			public void setWaitingCursor(boolean waiting) {
			}

			public Logger getLogger(Class<?> className) {
				return Logger.getLogger(className.getName());
			}

			public JFrame getJFrame() {
				return owner;
			}

			public File[] getFilesOfOpenTabs() {
				return new File[0];
			}

			public void openMap(String path) {
			}
		});
		assertEquals("Search Multiple Maps", panel.getTitle());
		assertTrue(selectedRadio(panel.getContentPane(), "Directory Search"));
		JList<?> results = findList(panel.getContentPane());
		assertEquals(1, results.getModel().getSize());
		assertEquals("No results", results.getModel().getElementAt(0));
		panel.dispose();
		owner.dispose();
	}

	private static JList<?> findList(Container container) {
		for (Component component : container.getComponents()) {
			if (component instanceof JList<?> list) {
				return list;
			}
			if (component instanceof Container) {
				JList<?> nested = findList((Container) component);
				if (nested != null) {
					return nested;
				}
			}
		}
		return null;
	}

	private static boolean selectedRadio(Container container, String text) {
		for (Component component : container.getComponents()) {
			if (component instanceof JRadioButton radio && text.equals(radio.getText()) && radio.isSelected()) {
				return true;
			}
			if (component instanceof Container && selectedRadio((Container) component, text)) {
				return true;
			}
		}
		return false;
	}
}
