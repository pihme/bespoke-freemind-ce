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

package tests.freemind.cov.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;

import javax.swing.JPanel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import freemind.preferences.layout.KeyEventWorkaround;

/** Modifier bookkeeping drops the keystrokes FreeMind must not see twice. */
class KeyEventWorkaroundBehaviourTest {

	@AfterEach
	void clearKeyState() throws Exception {
		reset();
	}

	@Test
	void theWorkaroundCanBeConstructedAndItsFlagsStayOff() {
		// backwards compatibility
		KeyEventWorkaround workaround = new KeyEventWorkaround();
		assertFalse(KeyEventWorkaround.ALT_KEY_PRESSED_DISABLED);
		assertFalse(KeyEventWorkaround.ALTERNATIVE_DISPATCHER);
		assertTrue(workaround instanceof KeyEventWorkaround);
	}

	@Test
	void deadKeysAndUnmodifiedLettersAreDropped() throws Exception {
		// backwards compatibility
		reset();
		JPanel source = new JPanel();
		int[] dead = { KeyEvent.VK_DEAD_GRAVE, KeyEvent.VK_DEAD_ACUTE, KeyEvent.VK_DEAD_CIRCUMFLEX,
				KeyEvent.VK_DEAD_TILDE, KeyEvent.VK_DEAD_MACRON, KeyEvent.VK_DEAD_BREVE, KeyEvent.VK_DEAD_ABOVEDOT,
				KeyEvent.VK_DEAD_DIAERESIS, KeyEvent.VK_DEAD_ABOVERING, KeyEvent.VK_DEAD_DOUBLEACUTE,
				KeyEvent.VK_DEAD_CARON, KeyEvent.VK_DEAD_CEDILLA, KeyEvent.VK_DEAD_OGONEK, KeyEvent.VK_DEAD_IOTA,
				KeyEvent.VK_DEAD_VOICED_SOUND, KeyEvent.VK_DEAD_SEMIVOICED_SOUND, 0 };
		for (int code : dead) {
			assertNull(KeyEventWorkaround.processKeyEvent(pressed(source, code, 40L, 0)));
			assertEquals(40L, state("lastKeyTime"));
		}
		assertNull(KeyEventWorkaround.processKeyEvent(pressed(source, KeyEvent.VK_A, 50L, 0)));
		assertNull(KeyEventWorkaround.processKeyEvent(pressed(source, KeyEvent.VK_4, 51L, 0)));
		assertEquals(0L, state("lastKeyTime"));

		KeyEvent altLetter = pressed(source, KeyEvent.VK_A, 60L, InputEvent.ALT_MASK);
		KeyEvent ctrlLetter = pressed(source, KeyEvent.VK_B, 61L, InputEvent.CTRL_MASK);
		KeyEvent metaLetter = pressed(source, KeyEvent.VK_C, 62L, InputEvent.META_MASK);
		KeyEvent chord = pressed(source, KeyEvent.VK_D, 63L, InputEvent.CTRL_MASK | InputEvent.ALT_MASK);
		assertSame(altLetter, KeyEventWorkaround.processKeyEvent(altLetter));
		assertSame(ctrlLetter, KeyEventWorkaround.processKeyEvent(ctrlLetter));
		assertSame(metaLetter, KeyEventWorkaround.processKeyEvent(metaLetter));
		assertSame(chord, KeyEventWorkaround.processKeyEvent(chord));
		assertEquals(0L, state("lastKeyTime"));
	}

	@Test
	void modifierKeysAreTrackedAndOnlyAltReleaseIsConsumed() throws Exception {
		// backwards compatibility
		reset();
		JPanel source = new JPanel();
		assertNull(KeyEventWorkaround.processKeyEvent(pressed(source, KeyEvent.VK_ALT, 100L, 0)));
		assertNull(KeyEventWorkaround.processKeyEvent(pressed(source, KeyEvent.VK_ALT_GRAPH, 101L, 0)));
		assertNull(KeyEventWorkaround.processKeyEvent(pressed(source, KeyEvent.VK_CONTROL, 102L, 0)));
		assertNull(KeyEventWorkaround.processKeyEvent(pressed(source, KeyEvent.VK_SHIFT, 103L, 0)));
		assertNull(KeyEventWorkaround.processKeyEvent(pressed(source, KeyEvent.VK_META, 104L, 0)));
		assertEquals(InputEvent.ALT_MASK | InputEvent.ALT_GRAPH_MASK | InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK
				| InputEvent.META_MASK, (int) state("modifiers"));

		KeyEvent ctrlUp = released(source, KeyEvent.VK_CONTROL, 110L);
		KeyEvent shiftUp = released(source, KeyEvent.VK_SHIFT, 111L);
		KeyEvent metaUp = released(source, KeyEvent.VK_META, 112L);
		KeyEvent graphUp = released(source, KeyEvent.VK_ALT_GRAPH, 113L);
		assertNull(KeyEventWorkaround.processKeyEvent(ctrlUp));
		assertNull(KeyEventWorkaround.processKeyEvent(shiftUp));
		assertNull(KeyEventWorkaround.processKeyEvent(metaUp));
		assertNull(KeyEventWorkaround.processKeyEvent(graphUp));
		assertFalse(ctrlUp.isConsumed());
		assertEquals(InputEvent.ALT_MASK, (int) state("modifiers"));

		int[] arrows = { KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_PAGE_UP,
				KeyEvent.VK_PAGE_DOWN, KeyEvent.VK_END, KeyEvent.VK_HOME };
		for (int code : arrows) {
			KeyEvent arrow = released(source, code, 120L);
			assertSame(arrow, KeyEventWorkaround.processKeyEvent(arrow));
		}

		KeyEvent altUp = released(source, KeyEvent.VK_ALT, 1_000L);
		assertNull(KeyEventWorkaround.processKeyEvent(altUp));
		assertTrue(altUp.isConsumed());
		assertEquals(0, (int) state("modifiers"));

		assertNull(KeyEventWorkaround.processKeyEvent(typed(source, 'B', 1_100L)));
		reset();
		assertNull(KeyEventWorkaround.processKeyEvent(pressed(source, KeyEvent.VK_ALT, 100L, 0)));
		KeyEvent left = released(source, KeyEvent.VK_LEFT, 101L);
		assertSame(left, KeyEventWorkaround.processKeyEvent(left));
		assertNull(KeyEventWorkaround.processKeyEvent(released(source, KeyEvent.VK_ALT, 1_000L)));
		KeyEvent kept = typed(source, 'A', 1_100L);
		assertSame(kept, KeyEventWorkaround.processKeyEvent(kept));
	}

	@Test
	void numpadCharactersAreFilteredUntilTheNumLockWorkaroundClearsThem() throws Exception {
		// backwards compatibility
		reset();
		JPanel source = new JPanel();
		int[] pad = { KeyEvent.VK_NUMPAD0, KeyEvent.VK_NUMPAD1, KeyEvent.VK_MULTIPLY, KeyEvent.VK_ADD,
				KeyEvent.VK_SUBTRACT, KeyEvent.VK_DECIMAL, KeyEvent.VK_DIVIDE };
		for (int code : pad) {
			KeyEvent event = pressed(source, code, 2_000L, 0);
			assertSame(event, KeyEventWorkaround.processKeyEvent(event));
		}
		for (char ch : new char[] { '0', '9', '.', '/', '*', '-', '+' }) {
			KeyEvent again = pressed(source, KeyEvent.VK_NUMPAD0, 2_200L, InputEvent.ALT_MASK);
			assertSame(again, KeyEventWorkaround.processKeyEvent(again));
			assertNull(KeyEventWorkaround.processKeyEvent(typed(source, ch, 2_300L)));
		}
		KeyEvent letterPad = pressed(source, KeyEvent.VK_NUMPAD2, 2_400L, InputEvent.ALT_MASK);
		assertSame(letterPad, KeyEventWorkaround.processKeyEvent(letterPad));
		KeyEvent letter = typed(source, 'a', 2_450L);
		assertSame(letter, KeyEventWorkaround.processKeyEvent(letter));

		KeyEvent keptPad = pressed(source, KeyEvent.VK_NUMPAD3, 3_000L, InputEvent.ALT_MASK);
		assertSame(keptPad, KeyEventWorkaround.processKeyEvent(keptPad));
		KeyEventWorkaround.numericKeypadKey();
		KeyEvent digit = typed(source, '3', 3_100L);
		assertSame(digit, KeyEventWorkaround.processKeyEvent(digit));
	}

	@Test
	void typedControlCharactersAndAStaleShiftAreFiltered() throws Exception {
		// backwards compatibility
		reset();
		JPanel source = new JPanel();
		assertNull(KeyEventWorkaround.processKeyEvent(typed(source, '\u007f', 4_000L)));
		assertNull(KeyEventWorkaround.processKeyEvent(typed(source, '\u00ff', 4_001L)));
		assertNull(KeyEventWorkaround.processKeyEvent(typed(source, '\u0001', 4_002L)));
		KeyEvent backspace = typed(source, '\b', 5_000L);
		KeyEvent tab = typed(source, '\t', 5_001L);
		assertSame(backspace, KeyEventWorkaround.processKeyEvent(backspace));
		assertSame(tab, KeyEventWorkaround.processKeyEvent(tab));

		assertNull(KeyEventWorkaround.processKeyEvent(pressed(source, KeyEvent.VK_SHIFT, 6_000L, 0)));
		assertNull(KeyEventWorkaround.processKeyEvent(typed(source, '\n', 6_800L)));
		assertNull(KeyEventWorkaround.processKeyEvent(typed(source, '\t', 6_801L)));
		KeyEvent letter = typed(source, 'Q', 6_802L);
		assertSame(letter, KeyEventWorkaround.processKeyEvent(letter));
		assertEquals(0, (int) state("modifiers"));

		reset();
		assertNull(KeyEventWorkaround.processKeyEvent(pressed(source, KeyEvent.VK_CONTROL, 7_000L, 0)));
		assertNull(KeyEventWorkaround.processKeyEvent(pressed(source, KeyEvent.VK_ALT, 7_001L, 0)));
		KeyEvent both = typed(source, 'Z', 7_100L);
		assertSame(both, KeyEventWorkaround.processKeyEvent(both));

		reset();
		assertNull(KeyEventWorkaround.processKeyEvent(pressed(source, KeyEvent.VK_META, 8_000L, 0)));
		assertNull(KeyEventWorkaround.processKeyEvent(typed(source, 'Z', 8_100L)));

		KeyEvent other = new KeyEvent(source, 0, 9L, 0, KeyEvent.VK_A, 'a');
		assertSame(other, KeyEventWorkaround.processKeyEvent(other));
	}

	private static void reset() throws Exception {
		Field modifiers = KeyEventWorkaround.class.getDeclaredField("modifiers");
		modifiers.setAccessible(true);
		modifiers.setInt(null, 0);
		Field last = KeyEventWorkaround.class.getDeclaredField("last");
		last.setAccessible(true);
		last.setInt(null, 0);
		Field lastKeyTime = KeyEventWorkaround.class.getDeclaredField("lastKeyTime");
		lastKeyTime.setAccessible(true);
		lastKeyTime.setLong(null, 0L);
	}

	private static long state(String name) throws Exception {
		Field field = KeyEventWorkaround.class.getDeclaredField(name);
		field.setAccessible(true);
		if (field.getType() == int.class) {
			return field.getInt(null);
		}
		return field.getLong(null);
	}

	private static KeyEvent pressed(JPanel source, int code, long when, int modifiers) {
		return new KeyEvent(source, KeyEvent.KEY_PRESSED, when, modifiers, code, KeyEvent.CHAR_UNDEFINED);
	}

	private static KeyEvent released(JPanel source, int code, long when) {
		return new KeyEvent(source, KeyEvent.KEY_RELEASED, when, 0, code, KeyEvent.CHAR_UNDEFINED);
	}

	private static KeyEvent typed(JPanel source, char ch, long when) {
		return new KeyEvent(source, KeyEvent.KEY_TYPED, when, 0, KeyEvent.VK_UNDEFINED, ch);
	}
}
