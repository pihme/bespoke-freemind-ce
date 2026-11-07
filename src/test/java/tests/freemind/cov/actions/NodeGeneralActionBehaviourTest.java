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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.ActionEvent;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.controller.actions.generated.instance.BoldNodeAction;
import freemind.controller.actions.generated.instance.XmlAction;
import freemind.modes.mindmapmode.MindMapController;
import freemind.modes.mindmapmode.MindMapNodeModel;
import freemind.modes.mindmapmode.actions.NodeGeneralAction;
import freemind.modes.mindmapmode.actions.SingleNodeOperation;
import freemind.modes.mindmapmode.actions.xml.ActionPair;
import freemind.modes.mindmapmode.actions.xml.ActorXml;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** A general action applies one node operation, or builds a pair from its actor. */
@DisplayName("NodeGeneralActionBehaviourTest")
class NodeGeneralActionBehaviourTest {

	@Test
	void theSingleNodeOperationChangesTheText() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Leaf");
		MindMapNodeModel leaf = (MindMapNodeModel) opened.child("Leaf");
		opened.mode.select(leaf, List.of(leaf));
		NodeGeneralAction action = new NodeGeneralAction(opened.mode, null, null, new SingleNodeOperation() {
			public void apply(freemind.modes.mindmapmode.MindMapMapModel map, MindMapNodeModel selected) {
				opened.mode.setNodeText(selected, "Touched");
			}
		});
		action.actionPerformed(new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "general"));
		assertEquals("Touched", leaf.getText());
	}

	@Test
	void aMissingOrPlainActorIsRejected() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Ink");
		MindMapNodeModel ink = (MindMapNodeModel) opened.child("Ink");
		opened.mode.setBold(ink, true);
		opened.mode.select(ink, List.of(ink));
		Probe probe = new Probe(opened.mode);
		IllegalArgumentException missing = assertThrows(IllegalArgumentException.class, () -> probe.pair(ink));
		assertTrue(missing.getMessage().contains("doActionClass not defined"));
		probe.addActor(new ActorXml() {
			public void act(XmlAction action) {
			}

			public Class<?> getDoActionClass() {
				return Marker.class;
			}
		});
		probe.use(Marker.class);
		IllegalArgumentException wrong = assertThrows(IllegalArgumentException.class, () -> probe.pair(ink));
		assertTrue(wrong.getMessage().contains("is not a NodeActorXml"));
		probe.use(BoldNodeAction.class);
		ActionPair pair = probe.pair(ink);
		assertFalse(((BoldNodeAction) pair.getDoAction()).isBold());
		probe.actionPerformed(new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "bold"));
		assertFalse(ink.isBold());
	}

	private static final class Marker extends XmlAction {
	}

	private static final class Probe extends NodeGeneralAction {
		private static final long serialVersionUID = 1L;

		Probe(MindMapController controller) {
			super(controller, null, null);
		}

		void use(Class<?> type) {
			setDoActionClass(type);
		}

		ActionPair pair(MindMapNodeModel node) {
			return getActionPair(node);
		}
	}
}
