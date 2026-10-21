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

import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JMenuItem;

import org.junit.jupiter.api.Test;

import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.actions.CloudColorAction;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Cloud color can be chosen only for a clouded node, and the chooser does not apply it. */
class CloudColorActionBehaviourTest {

	@Test
	void aCloudKeepsItsColorAndABareNodeIsDisabled() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Puff", "Bare");
		MindMapNode puff = opened.child("Puff");
		Color kept = new Color(9, 8, 7);
		opened.mode.setCloud(puff, true);
		opened.mode.setCloudColor(puff, kept);
		opened.mode.select(puff, List.of(puff));
		CloudColorAction action = new CloudColorAction(opened.mode);
		assertTrue(action.isEnabled(new JMenuItem(), action));
		assertThrows(HeadlessException.class,
				() -> action.actionPerformed(new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "cloud")));
		assertEquals(kept, puff.getCloud().getColor());
		MindMapNode bare = opened.child("Bare");
		opened.mode.select(bare, List.of(bare));
		assertFalse(action.isEnabled(new JMenuItem(), action));
	}
}
