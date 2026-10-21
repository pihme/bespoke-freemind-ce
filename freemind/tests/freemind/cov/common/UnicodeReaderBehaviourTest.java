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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.common.UnicodeReader;

/** A reader takes the charset from the BOM, otherwise from the default. */
@DisplayName("UnicodeReaderBehaviourTest")
class UnicodeReaderBehaviourTest {

	@Test
	void aBomSelectsTheMatchingCharsetAndIsNotPartOfTheText() throws Exception {
		// backwards compatibility
		readBom(bytes(0xEF, 0xBB, 0xBF, 'A'));
		readBom(bytes(0xFE, 0xFF, 0x00, 'A'));
		readBom(bytes(0xFF, 0xFE, 'A', 0x00));
		readBom(bytes(0x00, 0x00, 0xFE, 0xFF, 0x00, 0x00, 0x00, 'A'));
		readBom(bytes(0xFF, 0xFE, 0x00, 0x00, 'A', 0x00, 0x00, 0x00));
	}

	@Test
	void withoutABomTheDefaultCharsetIsUsedAndNullMeansUtf8() throws Exception {
		// backwards compatibility
		UnicodeReader latin = new UnicodeReader(new ByteArrayInputStream(new byte[] { (byte) 0xE9 }), "ISO-8859-1");
		assertEquals("ISO-8859-1", latin.getDefaultEncoding());
		assertNull(latin.getEncoding());
		assertEquals("\u00e9", readAll(latin));
		assertTrue(latin.getEncoding().toUpperCase(Locale.ROOT).contains("8859"));

		UnicodeReader utf8 = new UnicodeReader(new ByteArrayInputStream("Z".getBytes(StandardCharsets.UTF_8)), null);
		assertNull(utf8.getDefaultEncoding());
		assertEquals("Z", readAll(utf8));
		assertTrue(utf8.getEncoding().toUpperCase(Locale.ROOT).contains("UTF"));
	}

	@Test
	void anEmptyStreamReadsNothingAndCloseThenReadFails() throws Exception {
		// backwards compatibility
		UnicodeReader empty = new UnicodeReader(new ByteArrayInputStream(new byte[0]), "UTF-8");
		assertEquals(-1, empty.read(new char[4], 0, 4));
		assertTrue(empty.getEncoding().toUpperCase(Locale.ROOT).contains("UTF"));
		empty.close();

		UnicodeReader closed = new UnicodeReader(new ByteArrayInputStream(new byte[] { 'A' }), "UTF-8");
		closed.close();
		assertThrows(IOException.class, () -> closed.read(new char[1], 0, 1));
	}

	private static void readBom(byte[] data) throws IOException {
		UnicodeReader reader = new UnicodeReader(new ByteArrayInputStream(data), "ISO-8859-1");
		assertEquals("A", readAll(reader));
		String encoding = reader.getEncoding().toUpperCase(Locale.ROOT);
		assertFalse(encoding.contains("8859"), encoding);
		reader.close();
	}

	private static String readAll(UnicodeReader reader) throws IOException {
		char[] buf = new char[8];
		StringBuilder text = new StringBuilder();
		int n = reader.read(buf, 0, buf.length);
		while (n > 0) {
			text.append(buf, 0, n);
			n = reader.read(buf, 0, buf.length);
		}
		return text.toString();
	}

	private static byte[] bytes(int... values) {
		byte[] data = new byte[values.length];
		for (int i = 0; i < values.length; i++) {
			data[i] = (byte) values[i];
		}
		return data;
	}
}
