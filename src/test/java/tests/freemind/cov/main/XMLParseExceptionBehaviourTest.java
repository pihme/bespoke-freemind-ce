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

package tests.freemind.cov.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.main.XMLParseException;

@DisplayName("XMLParseExceptionBehaviourTest")
class XMLParseExceptionBehaviourTest {

	@Test
	void unnamedParseErrorHasNoLineAndNamedErrorKeepsTheLine() {
		// backwards compatibility
		XMLParseException unnamed = new XMLParseException(null, "boom");
		assertEquals(XMLParseException.NO_LINE, unnamed.getLineNr());
		assertTrue(unnamed.getMessage().contains("XML definition"));
		assertTrue(unnamed.getMessage().contains("boom"));

		XMLParseException named = new XMLParseException("node", 4, "bad");
		assertEquals(4, named.getLineNr());
		assertTrue(named.getMessage().contains("a node element"));
		assertTrue(named.getMessage().contains("line 4"));
		assertTrue(named.getMessage().contains("bad"));
	}
}
