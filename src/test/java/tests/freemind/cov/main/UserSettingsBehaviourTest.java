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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.main.UserSettings;

@DisplayName("UserSettingsBehaviourTest")
class UserSettingsBehaviourTest {

	@Test
	void storeWritesChangedKeysAndKeepsTheShippedOnes() throws Exception {
		Properties defaults = new Properties();
		defaults.setProperty("properties_folder", ".freemind");
		defaults.setProperty("autoproperties", "auto.properties");
		defaults.setProperty("standardbackgroundcolor", "#ffffff");
		Properties current = new Properties(defaults);
		current.setProperty("standardbackgroundcolor", "#000000");
		File dir = Files.createTempDirectory("user-settings").toFile();
		File auto = new File(dir, "auto.properties");
		try {
			UserSettings.store(current, defaults, auto, "1.1.0 Beta 2");
			String written = Files.readString(auto.toPath(), StandardCharsets.UTF_8);
			assertTrue(written.startsWith("#FreeMind 1.1.0 Beta 2\n"), written);
			assertFalse(written.contains("properties_folder"));
			Properties loaded = new Properties();
			loaded.load(Files.newBufferedReader(auto.toPath(), StandardCharsets.UTF_8));
			assertEquals("#000000", loaded.getProperty("standardbackgroundcolor"));
			assertNull(loaded.getProperty("autoproperties"));
		} finally {
			auto.delete();
			dir.delete();
		}
	}
}
