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

package tests.freemind.cov.browsemode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JToolBar;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.controller.Controller;
import freemind.modes.browsemode.BrowseController;
import freemind.modes.browsemode.BrowseMode;
import freemind.modes.browsemode.BrowseToolBar;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** The browse toolbar shows an empty URL and rejects a value that is not a URL. */
@DisplayName("BrowseToolBarBehaviourTest")
class BrowseToolBarBehaviourTest {

	@Test
	void theUrlFieldStartsEmptyAndABadValueDoesNotOpenAMap() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		toolbarActions(opened.app);
		BrowseMode mode = new BrowseMode();
		mode.init(opened.app);
		BrowseController controller = (BrowseController) mode.getDefaultModeController();
		JToolBar bar = controller.getModeToolBar();
		assertTrue(bar instanceof BrowseToolBar);
		assertEquals("browse_url_storage", BrowseToolBar.BROWSE_URL_STORAGE_KEY);
		assertEquals("URL:", labelText(bar));
		JComboBox<?> urls = urlBox(bar);
		assertEquals("", urls.getSelectedItem().toString());

		urls.setSelectedItem("");
		assertEquals("ROOT", opened.app.getMapModule().getModel().getRootNode().getText());

		Window[] before = Window.getWindows();
		try {
			assertThrows(HeadlessException.class, () -> urls.setSelectedItem("not a url"));
			assertEquals(before.length, Window.getWindows().length);
			assertEquals("ROOT", opened.app.getMapModule().getModel().getRootNode().getText());
		} finally {
			disposeNewWindows(before);
		}
	}

	private static String labelText(JToolBar bar) {
		for (Component component : bar.getComponents()) {
			if (component instanceof JLabel label) {
				return label.getText();
			}
		}
		return null;
	}

	private static JComboBox<?> urlBox(JToolBar bar) {
		for (Component component : bar.getComponents()) {
			if (component instanceof JComboBox<?> box) {
				return box;
			}
		}
		return null;
	}

	private static void toolbarActions(Controller app) {
		app.zoomIn = unusedAction();
		app.zoomOut = unusedAction();
		app.showFilterToolbarAction = unusedAction();
	}

	private static Action unusedAction() {
		return new AbstractAction() {
			public void actionPerformed(ActionEvent event) {
			}
		};
	}

	private static void disposeNewWindows(Window[] before) {
		for (Window window : Window.getWindows()) {
			boolean kept = false;
			for (Window old : before) {
				if (old == window) {
					kept = true;
					break;
				}
			}
			if (!kept) {
				window.dispose();
			}
		}
	}
}
