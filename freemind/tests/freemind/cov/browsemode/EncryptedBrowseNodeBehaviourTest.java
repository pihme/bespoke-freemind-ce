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

package tests.freemind.cov.browsemode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.HeadlessException;
import java.awt.Window;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.browsemode.EncryptedBrowseNode;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** An encrypted browse node stays locked when no display can ask for a password. */
@DisplayName("EncryptedBrowseNodeBehaviourTest")
class EncryptedBrowseNodeBehaviourTest {

	@Test
	void constructorsStoreTheTextAndFoldingNeedsAPasswordDialog() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open();
		Window[] before = Window.getWindows();
		try {
			EncryptedBrowseNode named = new EncryptedBrowseNode("Secret", opened.mode);
			EncryptedBrowseNode blank = new EncryptedBrowseNode(opened.mode);
			assertEquals("Secret", named.getText());
			assertEquals("", blank.getText());
			assertNotNull(named.getStateIcons().get("encryptedNode"));
			assertNotNull(blank.getStateIcons().get("encryptedNode"));

			named.setAdditionalInfo("cipher-text");
			named.updateIcon();
			assertNull(named.getAdditionalInfo());
			assertNotNull(named.getStateIcons().get("encryptedNode"));

			named.setFolded(true);
			assertTrue(named.isFolded());
			assertThrows(HeadlessException.class, () -> named.setFolded(false));
			assertEquals(before.length, Window.getWindows().length);
			assertTrue(named.isFolded());
		} finally {
			disposeNewWindows(before);
		}
	}

	private static void disposeNewWindows(Window[] before) {
		for (Window window : Window.getWindows()) {
			boolean kept = false;
			for (Window old : before) {
				if (old == window) {
					kept = true;
					break;
				}
			}
			if (!kept) {
				window.dispose();
			}
		}
	}
}
