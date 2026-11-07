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

package tests.freemind.cov.dialogs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.HeadlessException;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import accessories.plugins.dialogs.ManagePatternsPopupDialog;
import freemind.controller.actions.generated.instance.Pattern;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** The pattern dialog is not shown when the suite has no display. */
@DisplayName("ManagePatternsPopupDialogBehaviourTest")
class ManagePatternsPopupDialogBehaviourTest {

	@Test
	void openingTheDialogStopsBeforeItIsShown() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha");
		HeadlessException thrown = assertThrows(HeadlessException.class,
				() -> new ManagePatternsPopupDialog(null, opened.mode));
		assertEquals(HeadlessException.class, thrown.getClass());
	}

	@Test
	void aConstructedDialogKeepsItsResultAndTheLastPattern() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha");
		ManagePatternsPopupDialog dialog;
		try {
			dialog = new ManagePatternsPopupDialog(null, opened.mode);
		} catch (HeadlessException ex) {
			assertEquals(HeadlessException.class, ex.getClass());
			return;
		}
		assertNotNull(dialog.getPatternList());
		Pattern pattern = new Pattern();
		pattern.setName("Kept");
		dialog.setLastSelectedPattern(pattern);
		assertEquals("Kept", dialog.getLastSelectedPattern().getName());
		dialog.keyTyped(new KeyEvent(new JPanel(), KeyEvent.KEY_TYPED, 0, 0, KeyEvent.VK_UNDEFINED, 'a'));
		dialog.keyReleased(new KeyEvent(new JPanel(), KeyEvent.KEY_RELEASED, 0, 0, KeyEvent.VK_A, 'a'));
		dialog.keyPressed(new KeyEvent(new JPanel(), KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_ESCAPE, '\u001b'));
		assertEquals(ManagePatternsPopupDialog.CANCEL, dialog.getResult());
	}
}
