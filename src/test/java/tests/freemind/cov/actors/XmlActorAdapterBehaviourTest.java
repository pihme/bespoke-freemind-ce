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
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.controller.actions.generated.instance.EditNodeAction;
import freemind.controller.actions.generated.instance.XmlAction;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.actions.xml.ActionPair;
import freemind.modes.mindmapmode.actions.xml.actors.XmlActorAdapter;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** An adapter transaction stores the text its act method writes. */
@DisplayName("XmlActorAdapterBehaviourTest")
class XmlActorAdapterBehaviourTest {

	@Test
	void theAdapterStoresTheEditedText() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Leaf");
		MindMapNode leaf = opened.child("Leaf");
		WritingAdapter adapter = new WritingAdapter(opened);
		adapter.edit(leaf, "ViaAdapter");
		assertEquals("ViaAdapter", leaf.getText());
		assertSame(opened.mode, adapter.getExMapFeedback());
	}

	private static final class WritingAdapter extends XmlActorAdapter {
		WritingAdapter(Opened opened) {
			super(opened.mode);
		}

		@Override
		public void act(XmlAction action) {
			EditNodeAction edit = (EditNodeAction) action;
			getNodeFromID(edit.getNode()).setUserObject(edit.getText());
		}

		@Override
		public Class<?> getDoActionClass() {
			return EditNodeAction.class;
		}

		void edit(MindMapNode node, String text) {
			EditNodeAction edit = new EditNodeAction();
			edit.setNode(getNodeID(node));
			edit.setText(text);
			execute(new ActionPair(edit, edit));
		}
	}
}
