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
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.Icon;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.controller.actions.generated.instance.XmlAction;
import freemind.modes.mindmapmode.MindMapController;
import freemind.modes.mindmapmode.actions.MindmapAction;
import freemind.modes.mindmapmode.actions.xml.ActorXml;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** A mind map action keeps its controller and registers the actor it is given. */
@DisplayName("MindmapActionBehaviourTest")
class MindmapActionBehaviourTest {

	@Test
	void theActorIsRegisteredUnderItsActionClass() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Leaf");
		Fixed action = new Fixed(opened.mode);
		Marker marker = new Marker();
		action.addActor(marker);
		assertSame(marker, opened.mode.getActionRegistry().getActor(MarkerAction.class));
		assertSame(opened.mode, action.getMindMapController());
		assertEquals("Fixed title", action.getValue(Action.NAME));
	}

	private static final class Fixed extends MindmapAction {
		private static final long serialVersionUID = 1L;

		Fixed(MindMapController controller) {
			super("Fixed title", (Icon) null, controller);
		}

		public void actionPerformed(ActionEvent event) {
		}
	}

	private static final class MarkerAction extends XmlAction {
	}

	private static final class Marker implements ActorXml {
		public void act(XmlAction action) {
		}

		public Class<?> getDoActionClass() {
			return MarkerAction.class;
		}
	}
}
