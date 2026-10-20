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

package tests.freemind.cov.modes;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.datatransfer.Transferable;

import org.junit.jupiter.api.Test;

import freemind.controller.MindMapNodesSelection;
import freemind.modes.ExtendedMapFeedbackImpl;
import freemind.modes.MindMapNode;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Copying through the feedback writes the node XML onto the clipboard flavor. */
class ExtendedMapFeedbackImplBehaviourTest {

	@Test
	void copyCarriesTheNodeText() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode child = opened.child("Child");
		ExtendedMapFeedbackImpl feedback = new ExtendedMapFeedbackImpl();
		feedback.setMap(opened.mode.getMap());
		Transferable copied = feedback.copy(child, false);
		String xml = (String) copied.getTransferData(MindMapNodesSelection.mindMapNodesFlavor);
		assertTrue(xml.contains("Child"), xml);
	}
}
