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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.Map;

import javax.swing.JPanel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import freemind.preferences.layout.KeyEventTranslator;
import freemind.preferences.layout.KeyEventTranslator.Key;
import freemind.preferences.layout.KeyEventWorkaround;

/** Modifier letters and key names become the shortcut text FreeMind stores. */
class KeyEventTranslatorBehaviourTest {

	private static int savedC;
	private static int savedA;
	private static int savedM;
	private static int savedS;

	@BeforeAll
	static void rememberModifierMapping() throws Exception {
		savedC = modifier("c");
		savedA = modifier("a");
		savedM = modifier("m");
		savedS = modifier("s");
	}

	@AfterEach
	void restoreModifierMapping() throws Exception {
		KeyEventTranslator.setModifierMapping(savedC, savedA, savedM, savedS);
		work("modifiers", 0);
		work("lastKeyTime", 0L);
	}

	@Test
	void theTranslatorObjectDoesNotChangeThePlatformMapping() {
		// backwards compatibility
		new KeyEventTranslator();
		assertEquals("control", KeyEventTranslator.getSymbolicModifierName(InputEvent.CTRL_MASK));
		assertEquals("alt", KeyEventTranslator.getSymbolicModifierName(InputEvent.ALT_MASK));
		assertEquals("meta", KeyEventTranslator.getSymbolicModifierName(InputEvent.META_MASK));
		assertEquals("shift", KeyEventTranslator.getSymbolicModifierName(InputEvent.SHIFT_MASK));
		assertEquals("", KeyEventTranslator.getSymbolicModifierName(0));
	}

	@Test
	void aModifierMappedTwiceIsRejectedAndTheOldMappingStays() {
		// backwards compatibility
		assertThrows(IllegalArgumentException.class, () -> KeyEventTranslator.setModifierMapping(
				InputEvent.CTRL_MASK, InputEvent.CTRL_MASK, 0, InputEvent.SHIFT_MASK));
		assertThrows(IllegalArgumentException.class, () -> KeyEventTranslator.setModifierMapping(
				0, InputEvent.ALT_MASK, InputEvent.ALT_MASK, 0));
		assertThrows(IllegalArgumentException.class, () -> KeyEventTranslator.setModifierMapping(
				0, 0, InputEvent.META_MASK, InputEvent.META_MASK));
		IllegalArgumentException shift = assertThrows(IllegalArgumentException.class,
				() -> KeyEventTranslator.setModifierMapping(0, InputEvent.SHIFT_MASK, 0, InputEvent.SHIFT_MASK));
		assertEquals("SHIFT is mapped to more than one modifier", shift.getMessage());
		assertEquals("control", KeyEventTranslator.getSymbolicModifierName(InputEvent.CTRL_MASK));
	}

	@Test
	void modifierNamesJoinWithASpaceAndTheEventStringDoesNot() {
		// backwards compatibility
		assertNull(KeyEventTranslator.modifiersToString(0));
		assertEquals("alt", KeyEventTranslator.modifiersToString(InputEvent.ALT_MASK));
		assertEquals("meta", KeyEventTranslator.modifiersToString(InputEvent.META_MASK));
		assertEquals("shift", KeyEventTranslator.modifiersToString(InputEvent.SHIFT_MASK));
		assertEquals("control alt meta shift", KeyEventTranslator.modifiersToString(InputEvent.CTRL_MASK
				| InputEvent.ALT_MASK | InputEvent.META_MASK | InputEvent.SHIFT_MASK));

		JPanel source = new JPanel();
		assertNull(KeyEventTranslator.getModifierString(pressed(source, KeyEvent.VK_A, 0)));
		assertEquals("alt", KeyEventTranslator.getModifierString(pressed(source, KeyEvent.VK_A, InputEvent.ALT_MASK)));
		assertEquals("meta", KeyEventTranslator.getModifierString(pressed(source, KeyEvent.VK_A, InputEvent.META_MASK)));
		assertEquals("controlaltmetashift", KeyEventTranslator.getModifierString(pressed(source, KeyEvent.VK_A,
				InputEvent.CTRL_MASK | InputEvent.ALT_MASK | InputEvent.META_MASK | InputEvent.SHIFT_MASK)));

		KeyEventTranslator.setModifierMapping(0, 0, 0, InputEvent.SHIFT_MASK);
		assertEquals("", KeyEventTranslator.getSymbolicModifierName(InputEvent.CTRL_MASK));
		assertEquals("", KeyEventTranslator.modifiersToString(InputEvent.CTRL_MASK));
		assertEquals("shift", KeyEventTranslator.modifiersToString(InputEvent.SHIFT_MASK));
		assertNull(KeyEventTranslator.getModifierString(pressed(source, KeyEvent.VK_A, InputEvent.CTRL_MASK)));
	}

	@Test
	void parseKeyReadsLettersNamesSpaceAndRejectsTheRest() {
		// backwards compatibility
		assertNull(KeyEventTranslator.parseKey(null));
		assertNull(KeyEventTranslator.parseKey(""));
		assertNull(KeyEventTranslator.parseKey("+"));
		assertNull(KeyEventTranslator.parseKey("C+"));
		assertNull(KeyEventTranslator.parseKey("NOT_A_KEY"));
		assertNull(KeyEventTranslator.parseKey("c+enter"));

		Key letter = KeyEventTranslator.parseKey("Q+A");
		assertNull(letter.modifiers);
		assertEquals('A', letter.input);
		assertEquals(0, letter.key);

		Key spaced = KeyEventTranslator.parseKey("C+ ");
		assertEquals("control", spaced.modifiers);
		assertEquals(' ', spaced.input);

		Key word = KeyEventTranslator.parseKey("S+SPACE");
		assertEquals("shift", word.modifiers);
		assertEquals(' ', word.input);
		assertEquals(0, word.key);

		Key tab = KeyEventTranslator.parseKey("SM+TAB");
		assertEquals("meta shift", tab.modifiers);
		assertEquals(KeyEvent.VK_TAB, tab.key);
		assertEquals('\0', tab.input);

		Key plain = KeyEventTranslator.parseKey("x");
		assertNull(plain.modifiers);
		assertEquals('x', plain.input);
	}

	@Test
	void translatedEventsKeepLettersTabsSpacesAndASubstitution() throws Exception {
		// backwards compatibility
		JPanel source = new JPanel();
		Key digit = KeyEventTranslator.translateKeyEvent(pressed(source, KeyEvent.VK_5, InputEvent.CTRL_MASK));
		assertEquals("control", digit.modifiers);
		assertEquals('5', digit.input);
		assertEquals(0, digit.key);

		Key enter = KeyEventTranslator.translateKeyEvent(pressed(source, KeyEvent.VK_ENTER, 0));
		assertNull(enter.modifiers);
		assertEquals(KeyEvent.VK_ENTER, enter.key);
		assertEquals('\0', enter.input);

		KeyEvent plainSpace = pressed(source, KeyEvent.VK_SPACE, 0);
		assertNull(KeyEventTranslator.translateKeyEvent(plainSpace));
		KeyEvent shiftSpace = pressed(source, KeyEvent.VK_SPACE, InputEvent.SHIFT_MASK);
		assertNull(KeyEventTranslator.translateKeyEvent(shiftSpace));
		Key altSpace = KeyEventTranslator.translateKeyEvent(pressed(source, KeyEvent.VK_SPACE, InputEvent.ALT_MASK));
		assertEquals("alt", altSpace.modifiers);
		assertEquals(' ', altSpace.input);

		assertNull(KeyEventTranslator.translateKeyEvent(typed(source, '\t', 0, 5L)));
		assertNull(KeyEventTranslator.translateKeyEvent(typed(source, '\b', 0, 5L)));
		assertNull(KeyEventTranslator.translateKeyEvent(typed(source, ' ', InputEvent.ALT_MASK, 5L)));
		Key typedSpace = KeyEventTranslator.translateKeyEvent(typed(source, ' ', InputEvent.SHIFT_MASK, 5_000L));
		assertEquals("shift", typedSpace.modifiers);
		assertEquals(' ', typedSpace.input);

		work("lastKeyTime", 8_000L);
		work("modifiers", InputEvent.CTRL_MASK);
		assertNull(KeyEventTranslator.translateKeyEvent(typed(source, 'B', 0, 8_100L)));
		Key graph = KeyEventTranslator.translateKeyEvent(typed(source, 'B', InputEvent.ALT_GRAPH_MASK, 8_100L));
		assertNull(graph.modifiers);
		assertEquals('B', graph.input);

		assertNull(KeyEventTranslator.translateKeyEvent(released(source, KeyEvent.VK_ESCAPE)));

		Key from = new Key("control", KeyEvent.VK_F13, '\0');
		Key to = new Key("alt", KeyEvent.VK_F14, '\0');
		KeyEventTranslator.addTranslation(from, to);
		try {
			Key translated = KeyEventTranslator.translateKeyEvent(pressed(source, KeyEvent.VK_F13, InputEvent.CTRL_MASK));
			assertEquals(to, translated);
			assertEquals("alt<" + Integer.toString(KeyEvent.VK_F14, 16) + ",0>", translated.toString());
		} finally {
			translations().remove(from);
		}
	}

	@Test
	void keyEqualityUsesModifiersCodeAndCharacter() {
		// backwards compatibility
		Key left = new Key("control", KeyEvent.VK_ENTER, '\0');
		Key right = new Key("control", KeyEvent.VK_ENTER, '\0');
		assertEquals(left, right);
		assertEquals(left.hashCode(), right.hashCode());
		assertEquals(KeyEvent.VK_ENTER, left.hashCode());
		assertFalse(left.equals(new Key("alt", KeyEvent.VK_ENTER, '\0')));
		assertFalse(left.equals(new Key("control", KeyEvent.VK_F1, '\0')));
		assertFalse(left.equals(new Key("control", KeyEvent.VK_ENTER, 'A')));
		assertFalse(left.equals("control"));
		assertFalse(left.equals(null));

		Key bare = new Key(null, 0, 'A');
		assertEquals("<0,41>", bare.toString());
		assertEquals(65, bare.hashCode());
		assertFalse(bare.equals("A"));
		assertFalse(left.equals(bare));
		assertThrows(NullPointerException.class, () -> bare.equals(left));
		assertEquals("control<" + Integer.toString(KeyEvent.VK_ENTER, 16) + ",0>", left.toString());
	}

	private static Map<?, ?> translations() throws Exception {
		Field field = KeyEventTranslator.class.getDeclaredField("transMap");
		field.setAccessible(true);
		return (Map<?, ?>) field.get(null);
	}

	private static int modifier(String name) throws Exception {
		Field field = KeyEventTranslator.class.getDeclaredField(name);
		field.setAccessible(true);
		return field.getInt(null);
	}

	private static void work(String name, int value) throws Exception {
		Field field = KeyEventWorkaround.class.getDeclaredField(name);
		field.setAccessible(true);
		field.setInt(null, value);
	}

	private static void work(String name, long value) throws Exception {
		Field field = KeyEventWorkaround.class.getDeclaredField(name);
		field.setAccessible(true);
		field.setLong(null, value);
	}

	private static KeyEvent pressed(JPanel source, int code, int modifiers) {
		return new KeyEvent(source, KeyEvent.KEY_PRESSED, 10L, modifiers, code, code >= KeyEvent.VK_A
				&& code <= KeyEvent.VK_Z ? Character.toLowerCase((char) code) : KeyEvent.CHAR_UNDEFINED);
	}

	private static KeyEvent released(JPanel source, int code) {
		return new KeyEvent(source, KeyEvent.KEY_RELEASED, 10L, 0, code, KeyEvent.CHAR_UNDEFINED);
	}

	private static KeyEvent typed(JPanel source, char ch, int modifiers, long when) {
		return new KeyEvent(source, KeyEvent.KEY_TYPED, when, modifiers, KeyEvent.VK_UNDEFINED, ch);
	}
}
