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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ResourceBundle;

import org.junit.jupiter.api.Test;

import freemind.main.FreeMindCommon;
import freemind.main.HeadlessFreeMind;

class FreeMindCommonBehaviourTest {

	@Test
	void languageBundleFallsBackAndNorwegianUsesBokmaal() {
		// backwards compatibility
		HeadlessFreeMind frame = new HeadlessFreeMind();
		frame.setProperty(FreeMindCommon.RESOURCE_LANGUAGE, "no");
		FreeMindCommon common = new FreeMindCommon(frame);
		ResourceBundle bundle = common.getResources();
		assertTrue(bundle.getKeys().hasMoreElements());
		String key = bundle.getKeys().nextElement();
		assertNotNull(common.getResourceString(key));
		assertEquals("__coverage_private", bundle.getString("__coverage_private"));
		assertTrue(common.getResourceString("coverage_missing_zz").contains("[translate me]")
				|| common.getResourceString("coverage_missing_zz").contains("coverage_missing_zz"));
		assertEquals("fallback", common.getResourceString("coverage_missing_zz", "fallback"));
		common.clearLanguageResources();
		assertNotNull(common.getResources().getKeys());
		assertTrue(common.getFreeMindClassLoader() instanceof URLClassLoader);
	}

	@Test
	void adjustablePropertiesStripTheQuestionMarkAndAdaptMacKeystrokes() {
		// backwards compatibility
		HeadlessFreeMind frame = new HeadlessFreeMind();
		FreeMindCommon common = new FreeMindCommon(frame);
		assertNull(common.getAdjustableProperty("missing_property_zz"));
		frame.setProperty("question", "? fallback text");
		assertEquals("fallback text", common.getAdjustableProperty("question"));
		frame.setProperty("keystroke_demo", "control INSERT");
		String os = System.getProperty("os.name");
		System.setProperty("os.name", "Mac OS X");
		try {
			assertEquals("meta TAB", common.getAdjustableProperty("keystroke_demo"));
		} finally {
			System.setProperty("os.name", os);
		}
		assertEquals("control INSERT", common.getAdjustableProperty("keystroke_demo"));
	}

	@Test
	void baseDirComesFromThePropertyOrTheClasspathJar() throws Exception {
		// backwards compatibility
		HeadlessFreeMind frame = new HeadlessFreeMind();
		String previousBase = System.getProperty("freemind.base.dir");
		String classpath = System.getProperty("java.class.path");
		File dir = Files.createTempDirectory("fm-base").toFile();
		File file = File.createTempFile("notdir", ".txt");
		try {
			System.setProperty("freemind.base.dir", dir.getAbsolutePath());
			assertEquals(dir.getCanonicalPath(), new FreeMindCommon(frame).getFreemindBaseDir());
			System.setProperty("freemind.base.dir", file.getAbsolutePath());
			assertThrows(IllegalArgumentException.class, () -> new FreeMindCommon(frame).getFreemindBaseDir());
			System.setProperty("freemind.base.dir", dir.getAbsolutePath() + "/missing-coverage");
			assertThrows(IllegalArgumentException.class, () -> new FreeMindCommon(frame).getFreemindBaseDir());

			System.clearProperty("freemind.base.dir");
			System.setProperty("java.class.path", "/opt/app/lib/freemind.jar");
			assertEquals(new File("/opt/app").getAbsolutePath(),
					new FreeMindCommon(frame).getFreemindBaseDirOld());
			System.setProperty("java.class.path", "freemind.jar");
			assertEquals(new File("").getAbsoluteFile().getParent(),
					new FreeMindCommon(frame).getFreemindBaseDirOld());
			System.setProperty("java.class.path", "/no/freemind/here");
			assertEquals(System.getProperty("user.dir"), new FreeMindCommon(frame).getFreemindBaseDirOld());
		} finally {
			System.setProperty("java.class.path", classpath);
			if (previousBase == null) {
				System.clearProperty("freemind.base.dir");
			} else {
				System.setProperty("freemind.base.dir", previousBase);
			}
			file.delete();
			dir.delete();
		}
	}
}
