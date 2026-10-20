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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import freemind.main.Base64Coding;

class Base64CodingBehaviourTest {

	@Test
	void decodeRejectsAnIllegalCharacterAndKeepsPaddingLengths() {
		// backwards compatibility
		assertNotNull(new Base64Coding());
		assertNull(Base64Coding.decode64("@@@@"));
		assertEquals(0, Base64Coding.decode64("").length);
		assertArrayEquals(new byte[] { 'A' }, Base64Coding.decode64("QQ=="));
		assertArrayEquals(new byte[] { 'A' }, Base64Coding.decode64("QQ"));
		assertEquals(1, Base64Coding.decode64("Q").length);
		byte[] hello = "hi".getBytes(StandardCharsets.UTF_8);
		assertArrayEquals(hello, Base64Coding.decode64(Base64Coding.encode64(hello)));
		assertArrayEquals(new byte[] { 0, 1, 2 },
				Base64Coding.decode64(Base64Coding.encode64(new byte[] { 0, 1, 2 })));
	}
}
