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

package tests.freemind.cov.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.common.NamedObject;

/** A named object compares and hashes by the wrapped object, and prints the name. */
@DisplayName("NamedObjectBehaviourTest")
class NamedObjectBehaviourTest {

	@Test
	void equalityUsesTheWrappedObjectAndTheNameIsWhatIsPrinted() {
		// backwards compatibility
		NamedObject named = new NamedObject("key", "Key");
		assertEquals("key", named.getObject());
		assertEquals("Key", named.getName());
		assertEquals("Key", named.toString());
		assertTrue(named.equals(new NamedObject("key", "other name")));
		assertTrue(named.equals("key"));
		assertFalse(named.equals("other"));
		assertFalse(named.equals(null));
		assertEquals("key".hashCode(), named.hashCode());
		assertEquals(named.hashCode(), new NamedObject("key", "again").hashCode());
	}

	@Test
	void aLiteralUsesTheSameTextForTheNameAndTheObject() {
		// backwards compatibility
		NamedObject literal = NamedObject.literal("plain");
		assertEquals("plain", literal.getObject());
		assertEquals("plain", literal.getName());
		assertEquals("plain", literal.toString());
		assertTrue(literal.equals(NamedObject.literal("plain")));
		assertFalse(literal.equals(NamedObject.literal("other")));
	}

	@Test
	void aNullObjectHashesToZeroAndEqualsThrows() {
		// backwards compatibility
		NamedObject named = new NamedObject(null, "none");
		assertNull(named.getObject());
		assertEquals("none", named.getName());
		assertEquals(0, named.hashCode());
		assertThrows(NullPointerException.class, () -> named.equals("x"));
		assertThrows(NullPointerException.class, () -> named.equals(new NamedObject(null, "other")));
	}
}
