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

package tests.freemind.cov.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.HeadlessException;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Vector;

import javax.swing.JMenuItem;

import org.junit.jupiter.api.Test;

import freemind.controller.MindMapNodesSelection;
import freemind.modes.MindMapLink;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.MindMapController;
import freemind.modes.mindmapmode.MindMapMapModel;
import freemind.modes.mindmapmode.actions.AddArrowLinkAction;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** An arrow is added from the selection to the single copied node. */
class AddArrowLinkActionBehaviourTest {

	@Test
	void anEmptyClipboardLeavesTheMapUnlinked() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("From", "To");
		install(opened.mode, new Vector<>());
		MindMapNode from = opened.child("From");
		opened.mode.select(from, List.of(from));
		AddArrowLinkAction action = new AddArrowLinkAction(opened.mode);
		assertFalse(action.isEnabled(new JMenuItem(), action));
		assertThrows(HeadlessException.class,
				() -> action.actionPerformed(new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "arrow")));
		assertEquals(0, registry(opened).getLinkRegistry().getAllLinksFromMe(from).size());
	}

	@Test
	void theCopiedNodeReceivesAnArrow() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("From", "To");
		MindMapNode from = opened.child("From");
		MindMapNode to = opened.child("To");
		Vector<String> ids = new Vector<>();
		ids.add(opened.mode.getNodeID(to));
		install(opened.mode, ids);
		opened.mode.select(from, List.of(from));
		AddArrowLinkAction action = new AddArrowLinkAction(opened.mode);
		assertTrue(action.isEnabled(new JMenuItem(), action));
		action.actionPerformed(new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "arrow"));
		Vector<MindMapLink> links = registry(opened).getLinkRegistry().getAllLinksFromMe(from);
		assertEquals(1, links.size());
		assertSame(to, links.get(0).getTarget());
	}

	private static MindMapMapModel registry(Opened opened) {
		return (MindMapMapModel) opened.mode.getMap();
	}

	private static void install(MindMapController mode, Vector<String> ids) throws Exception {
		Clipboard board = new Clipboard("coverage");
		board.setContents(new MindMapNodesSelection(null, null, null, null, null, null, null, ids), null);
		Field clipboard = MindMapController.class.getDeclaredField("clipboard");
		clipboard.setAccessible(true);
		clipboard.set(mode, board);
	}
}
