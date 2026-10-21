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

package tests.freemind.cov.script;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Window;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.main.HeadlessFreeMind;
import plugins.script.SignedScriptHandler;
import plugins.script.SignedScriptHandler.ScriptContents;

/** Signature text is parsed locally. Signing needs a password dialog, which headless mode refuses. */
@DisplayName("SignedScriptHandlerBehaviourTest")
class SignedScriptHandlerBehaviourTest {

	private static final String SIGNATURE =
			"MCwCFCllrN6Xig7V0nRFGmWBLoBauMiGAhQLoYbNRTjVS1c7A2ev3bvJqUqg8Q==";

	@Test
	void scriptContentsSplitsAPlainSignatureANamedKeyAndNoSignature() {
		// backwards compatibility
		ScriptContents plain = new ScriptContents("body//SIGN:" + SIGNATURE);
		assertEquals("body", plain.mScript);
		assertEquals(SIGNATURE, plain.mSignature);
		assertNull(plain.mKeyName);
		assertEquals("body//SIGN:" + SIGNATURE + "\n", plain.toString());

		ScriptContents named = new ScriptContents("body//SIGN(mykey):" + SIGNATURE);
		assertEquals("body", named.mScript);
		assertEquals("mykey", named.mKeyName);
		assertEquals(SIGNATURE, named.mSignature);
		assertEquals("body//SIGN(mykey):" + SIGNATURE + "\n", named.toString());

		ScriptContents unsigned = new ScriptContents("return 1");
		assertEquals("return 1", unsigned.mScript);
		assertNull(unsigned.mSignature);
		assertNull(unsigned.mKeyName);
		assertEquals("return 1//SIGN:null\n", unsigned.toString());

		ScriptContents atStart = new ScriptContents("//SIGN:" + SIGNATURE);
		assertEquals("//SIGN:" + SIGNATURE, atStart.mScript);
		assertNull(atStart.mSignature);

		ScriptContents namedAtStart = new ScriptContents("//SIGN(k):sig");
		assertEquals("", namedAtStart.mScript);
		assertEquals("k", namedAtStart.mKeyName);
		assertEquals("sig", namedAtStart.mSignature);

		ScriptContents lastPrefix = new ScriptContents("a//SIGN:one//SIGN:two");
		assertEquals("a//SIGN:one", lastPrefix.mScript);
		assertEquals("two", lastPrefix.mSignature);
		assertNull(lastPrefix.mKeyName);

		ScriptContents empty = new ScriptContents();
		assertNull(empty.mScript);
		assertNull(empty.mSignature);
		assertNull(empty.mKeyName);
		assertEquals("null//SIGN:null\n", empty.toString());
	}

	@Test
	void anUnsignedScriptAndABadSignatureAreNotTrusted() throws Exception {
		// backwards compatibility
		new HeadlessFreeMind();
		SignedScriptHandler handler = new SignedScriptHandler();
		ByteArrayOutputStream plainOut = new ByteArrayOutputStream();
		assertFalse(handler.isScriptSigned("return 1", plainOut));
		assertEquals("", plainOut.toString(StandardCharsets.UTF_8));

		ByteArrayOutputStream signedOut = new ByteArrayOutputStream();
		assertTrue(handler.isScriptSigned("test//SIGN:" + SIGNATURE, signedOut));
		assertEquals("", signedOut.toString(StandardCharsets.UTF_8));

		ByteArrayOutputStream tamperedOut = new ByteArrayOutputStream();
		assertFalse(handler.isScriptSigned("2test//SIGN:" + SIGNATURE, tamperedOut));

		ByteArrayOutputStream garbageOut = new ByteArrayOutputStream();
		assertFalse(handler.isScriptSigned("test//SIGN:%%%%", garbageOut));
		assertFalse(garbageOut.toString(StandardCharsets.UTF_8).isEmpty());

		ByteArrayOutputStream namedOut = new ByteArrayOutputStream();
		assertFalse(handler.isScriptSigned("body//SIGN(not-a-real-freemind-key):%%%%", namedOut));
		assertFalse(namedOut.toString(StandardCharsets.UTF_8).isEmpty());
	}

	@Test
	void signingAsksForAPasswordAndTheHeadlessDialogIsRefused() {
		// backwards compatibility
		HeadlessFreeMind frame = new HeadlessFreeMind();
		Window[] before = Window.getWindows();
		SignedScriptHandler handler = new SignedScriptHandler();

		assertTrue(GraphicsEnvironment.isHeadless());
		assertThrows(HeadlessException.class,
				() -> handler.signScript("return 1", key -> key, frame));
		assertArrayEquals(before, Window.getWindows());
	}
}
