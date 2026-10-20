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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.PasswordAuthentication;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import freemind.main.FreeMindCommon;
import freemind.main.FreeMindStarter;

class FreeMindStarterBehaviourTest {

	@Test
	void currentJavaPassesAndUserPreferencesRoundTrip() throws Exception {
		// backwards compatibility
		FreeMindStarter starter = new FreeMindStarter();
		Properties defaults = starter.readDefaultPreferences();
		assertNotNull(defaults.getProperty("properties_folder"));
		assertNotNull(defaults.getProperty("autoproperties"));

		ByteArrayOutputStream captured = new ByteArrayOutputStream();
		PrintStream previous = System.out;
		System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
		try {
			invoke(starter, "checkJavaVersion");
		} finally {
			System.setOut(previous);
		}
		assertTrue(captured.toString(StandardCharsets.UTF_8).contains("Checking Java Version done."));

		String home = System.getProperty("user.home");
		File tempHome = Files.createTempDirectory("fm-starter").toFile();
		Locale previousLocale = Locale.getDefault();
		try {
			System.setProperty("user.home", tempHome.getAbsolutePath());
			invoke(starter, "createUserDirectory", Properties.class, defaults);
			File folder = new File(tempHome, defaults.getProperty("properties_folder"));
			assertTrue(folder.isDirectory());
			String directory = (String) invoke(starter, "getFreeMindDirectory", Properties.class, defaults);
			assertEquals(folder.getAbsolutePath(), directory);

			File prefs = (File) invoke(starter, "getUserPreferencesFile", Properties.class, defaults);
			assertEquals(new File(folder, defaults.getProperty("autoproperties")).getAbsolutePath(),
					prefs.getAbsolutePath());
			Properties missing = (Properties) invoke(starter, "readUsersPreferences", Properties.class, defaults);
			assertEquals(defaults.getProperty("properties_folder"), missing.getProperty("properties_folder"));

			try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(prefs.toPath()),
					StandardCharsets.UTF_8)) {
				writer.write("coverage_color=blue\n");
			}
			Properties loaded = (Properties) invoke(starter, "readUsersPreferences", Properties.class, defaults);
			assertEquals("blue", loaded.getProperty("coverage_color"));

			Properties localeProps = new Properties();
			invoke(starter, "setDefaultLocale", Properties.class, localeProps);
			assertEquals(previousLocale, Locale.getDefault());
			localeProps.setProperty(FreeMindCommon.RESOURCE_LANGUAGE, "de");
			invoke(starter, "setDefaultLocale", Properties.class, localeProps);
			assertEquals("de", Locale.getDefault().getLanguage());
			localeProps.setProperty(FreeMindCommon.RESOURCE_LANGUAGE, "en_US");
			invoke(starter, "setDefaultLocale", Properties.class, localeProps);
			assertEquals("e", Locale.getDefault().getLanguage());
			assertEquals("U", Locale.getDefault().getCountry());
			localeProps.setProperty(FreeMindCommon.RESOURCE_LANGUAGE, "toolong");
			invoke(starter, "setDefaultLocale", Properties.class, localeProps);
			assertEquals("e", Locale.getDefault().getLanguage());
		} finally {
			System.setProperty("user.home", home);
			Locale.setDefault(previousLocale);
			deleteTree(tempHome);
		}

		FreeMindStarter.ProxyAuthenticator authenticator = new FreeMindStarter.ProxyAuthenticator("user", "secret");
		Method password = FreeMindStarter.ProxyAuthenticator.class.getDeclaredMethod("getPasswordAuthentication");
		password.setAccessible(true);
		PasswordAuthentication authentication = (PasswordAuthentication) password.invoke(authenticator);
		assertEquals("user", authentication.getUserName());
		assertEquals("secret", new String(authentication.getPassword()));
	}

	private static Object invoke(Object target, String name) throws Exception {
		Method method = target.getClass().getDeclaredMethod(name);
		method.setAccessible(true);
		return method.invoke(target);
	}

	private static Object invoke(Object target, String name, Class<?> type, Object arg) throws Exception {
		Method method = target.getClass().getDeclaredMethod(name, type);
		method.setAccessible(true);
		return method.invoke(target, arg);
	}

	private static void deleteTree(File file) {
		File[] children = file.listFiles();
		if (children != null) {
			for (File child : children) {
				deleteTree(child);
			}
		}
		file.delete();
	}
}
