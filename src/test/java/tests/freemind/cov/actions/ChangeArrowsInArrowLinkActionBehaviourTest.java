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

import java.awt.event.ActionEvent;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.MindMapArrowLinkModel;
import freemind.modes.mindmapmode.MindMapMapModel;
import freemind.modes.mindmapmode.actions.ChangeArrowsInArrowLinkAction;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** The arrow heads on a link switch between none and the default head. */
@DisplayName("ChangeArrowsInArrowLinkActionBehaviourTest")
class ChangeArrowsInArrowLinkActionBehaviourTest {

	@Test
	void bothEndsCanBeClearedAndRestored() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("From", "To");
		MindMapNode from = opened.child("From");
		MindMapNode to = opened.child("To");
		opened.mode.addLink(from, to);
		MindMapArrowLinkModel link = (MindMapArrowLinkModel) ((MindMapMapModel) opened.mode.getMap())
				.getLinkRegistry().getAllLinksFromMe(from).get(0);
		ActionEvent event = new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "arrows");
		new ChangeArrowsInArrowLinkAction(opened.mode, "ignored", null, link, false, false).actionPerformed(event);
		assertEquals("None", link.getStartArrow());
		assertEquals("None", link.getEndArrow());
		new ChangeArrowsInArrowLinkAction(opened.mode, "ignored", null, link, true, false).actionPerformed(event);
		assertEquals("Default", link.getStartArrow());
		assertEquals("None", link.getEndArrow());
	}
}
