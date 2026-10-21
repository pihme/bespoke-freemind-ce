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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;

import org.junit.jupiter.api.Test;

import freemind.controller.Controller;
import freemind.main.XMLElement;
import freemind.modes.MindMapNode;
import freemind.modes.browsemode.BrowseController;
import freemind.modes.browsemode.BrowseMode;
import freemind.modes.browsemode.BrowsePopupMenu;
import freemind.modes.common.plugins.MapNodePositionHolderBase;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** The browse popup lists its actions and enables the map link only with a position hook. */
class BrowsePopupMenuBehaviourTest {

	@Test
	void theMenuEnablesTheMapLinkOnlyWhileAPositionHookIsActive() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		toolbarActions(opened.app);
		BrowseMode mode = new BrowseMode();
		mode.init(opened.app);
		BrowseController controller = (BrowseController) mode.getDefaultModeController();
		JPopupMenu popup = controller.getPopupMenu();
		assertTrue(popup instanceof BrowsePopupMenu);
		assertEquals(8, popup.getComponentCount());
		assertEquals("find", ((JMenuItem) popup.getComponent(0)).getAction().getValue(Action.NAME));
		assertSame(controller.followMapLink, ((JMenuItem) popup.getComponent(7)).getAction());

		MindMapNode child = opened.child("Child");
		opened.view.selectAsTheOnlyOneSelected(opened.view.getNodeView(child));
		controller.setView(opened.view);
		PopupMenuEvent event = new PopupMenuEvent(popup);
		popup.getPopupMenuListeners()[0].popupMenuWillBecomeVisible(event);
		assertFalse(controller.followMapLink.isEnabled());

		MapNodePositionHolderBase hook = (MapNodePositionHolderBase) controller.getHookFactory()
				.createNodeHook(MapNodePositionHolderBase.NODE_MAP_HOOK_NAME);
		hook.setController(opened.mode);
		hook.setMap(child.getMap());
		hook.loadFrom(position());
		child.invokeHook(hook);
		try {
			popup.getPopupMenuListeners()[0].popupMenuWillBecomeVisible(event);
			assertTrue(controller.followMapLink.isEnabled());
			popup.getPopupMenuListeners()[0].popupMenuWillBecomeInvisible(event);
			popup.getPopupMenuListeners()[0].popupMenuCanceled(event);
			assertTrue(controller.followMapLink.isEnabled());
		} finally {
			hook.shutdownMapHook();
		}
	}

	private static XMLElement position() {
		XMLElement hook = new XMLElement();
		hook.setName("hook");
		XMLElement parameters = new XMLElement();
		parameters.setName("Parameters");
		parameters.setAttribute("XML_STORAGE_POS_LAT", "1.5");
		parameters.setAttribute("XML_STORAGE_POS_LON", "2.5");
		parameters.setAttribute("XML_STORAGE_MAP_LAT", "3.5");
		parameters.setAttribute("XML_STORAGE_MAP_LON", "4.5");
		parameters.setAttribute("XML_STORAGE_ZOOM", "12");
		parameters.setAttribute("XML_STORAGE_TILE_SOURCE", MapNodePositionHolderBase.TILE_SOURCE_MAPNIK);
		hook.addChild(parameters);
		return hook;
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
}
