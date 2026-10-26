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

import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.controller.actions.generated.instance.XmlAction;
import freemind.modes.MindMap;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.actions.xml.ActionPair;
import freemind.modes.mindmapmode.actions.xml.actors.NodeXmlActorAdapter;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** The node adapter reports the node the map has selected. */
@DisplayName("NodeXmlActorAdapterBehaviourTest")
class NodeXmlActorAdapterBehaviourTest {

	@Test
	void theAdapterSeesTheSelectedChild() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Leaf");
		MindMapNode leaf = opened.child("Leaf");
		opened.mode.select(leaf, List.of(leaf));
		ReadingAdapter adapter = new ReadingAdapter(opened);
		assertSame(leaf, adapter.selected());
		assertSame(opened.mode, adapter.getExMapFeedback());
	}

	private static final class ReadingAdapter extends NodeXmlActorAdapter {
		ReadingAdapter(Opened opened) {
			super(opened.mode);
		}

		@Override
		public void act(XmlAction action) {
		}

		@Override
		public Class<?> getDoActionClass() {
			return XmlAction.class;
		}

		@Override
		public ActionPair apply(MindMap model, MindMapNode selected) {
			return null;
		}

		MindMapNode selected() {
			return getSelected();
		}
	}
}
