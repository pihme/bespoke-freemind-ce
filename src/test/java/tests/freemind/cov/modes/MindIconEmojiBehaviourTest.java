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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.MindIconEmoji;

/** Built-in icon names map to emoji, and unknown names stay bracketed. */
@DisplayName("MindIconEmojiBehaviourTest")
class MindIconEmojiBehaviourTest {

	@Test
	void knownNamesHaveEmojiAndUnknownNamesDoNot() {
		// backwards compatibility
		assertTrue(MindIconEmoji.hasEmoji("idea"));
		assertFalse(MindIconEmoji.hasEmoji("no-such-icon"));
	}

	@Test
	void unknownNameIsBracketedAndIdeaIsNot() {
		// backwards compatibility
		assertEquals("[no-such-icon]", MindIconEmoji.getEmoji("no-such-icon"));
		assertFalse(MindIconEmoji.getEmoji("idea").startsWith("["));
	}
}
