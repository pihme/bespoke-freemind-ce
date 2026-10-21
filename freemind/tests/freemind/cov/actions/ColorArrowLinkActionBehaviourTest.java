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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.util.List;

import org.junit.jupiter.api.Test;

import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.MindMapArrowLinkModel;
import freemind.modes.mindmapmode.MindMapMapModel;
import freemind.modes.mindmapmode.actions.ColorArrowLinkAction;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** The arrow-color chooser closes before it can replace the color. */
class ColorArrowLinkActionBehaviourTest {

	@Test
	void theChooserLeavesTheArrowColor() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("From", "To");
		MindMapNode from = opened.child("From");
		opened.mode.addLink(from, opened.child("To"));
		MindMapArrowLinkModel link = (MindMapArrowLinkModel) ((MindMapMapModel) opened.mode.getMap())
				.getLinkRegistry().getAllLinksFromMe(from).get(0);
		Color kept = new Color(10, 20, 30);
		opened.mode.setArrowLinkColor(link, kept);
		opened.mode.select(from, List.of(from));
		assertThrows(HeadlessException.class,
				() -> new ColorArrowLinkAction(opened.mode, link).actionPerformed(
						new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "arrow")));
		assertEquals(kept, link.getColor());
	}
}
