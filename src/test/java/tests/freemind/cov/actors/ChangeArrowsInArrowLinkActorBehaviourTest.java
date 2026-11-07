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

package tests.freemind.cov.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.MindMapArrowLink;
import freemind.modes.MindMapNode;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Arrow heads can be turned on at the start and off at the end. */
@DisplayName("ChangeArrowsInArrowLinkActorBehaviourTest")
class ChangeArrowsInArrowLinkActorBehaviourTest {

	@Test
	void theStartArrowIsDefaultAndTheEndArrowIsNone() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("From", "To");
		MindMapNode from = opened.child("From");
		opened.mode.getActorFactory().getAddArrowLinkActor().addLink(from, opened.child("To"));
		MindMapArrowLink link = (MindMapArrowLink) opened.mode.getMap().getLinkRegistry()
				.getAllLinksFromMe(from).get(0);
		opened.mode.getActorFactory().getChangeArrowsInArrowLinkActor()
				.changeArrowsOfArrowLink(link, true, false);
		assertEquals("Default", link.getStartArrow());
		assertEquals("None", link.getEndArrow());
	}
}
