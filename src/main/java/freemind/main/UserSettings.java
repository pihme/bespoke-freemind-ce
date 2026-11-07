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

package freemind.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Shipped defaults and the runtime auto.properties file.
 * Keys stay the keys already in those files. The filter file is not this module.
 */
public final class UserSettings {

	private UserSettings() {
	}

	public static Properties defaults() {
		String propsLoc = "freemind.properties";
		URL defaultPropsURL = UserSettings.class.getClassLoader().getResource(propsLoc);
		Properties props = new Properties();
		try {
			java.io.InputStreamReader reader = new java.io.InputStreamReader(
					defaultPropsURL.openStream(), StandardCharsets.UTF_8);
			props.load(reader);
			reader.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println("Panic! Error while loading default properties.");
		}
		return props;
	}

	/** auto.properties over the shipped defaults. A missing file leaves the defaults. */
	public static Properties overlay(Properties defaults) {
		Properties auto = new Properties(defaults);
		try {
			File autoPropertiesFile = file(defaults);
			java.io.InputStreamReader reader = new java.io.InputStreamReader(
					new FileInputStream(autoPropertiesFile), StandardCharsets.UTF_8);
			auto.load(reader);
			reader.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println("Panic! Error while loading default properties.");
		}
		return auto;
	}

	public static File file(Properties defaults) {
		if (defaults == null) {
			System.err.println("Panic! Error while loading default properties.");
			System.exit(1);
		}
		File userPropertiesFolder = new File(directory(defaults));
		return new File(userPropertiesFolder, defaults.getProperty("autoproperties"));
	}

	public static String directory(Properties defaults) {
		return System.getProperty("user.home") + File.separator
				+ defaults.getProperty("properties_folder");
	}

	/**
	 * Writes keys that differ from the shipped defaults, under the same header.
	 * Does not write the filter file.
	 */
	public static void store(Properties current, Properties defaults, File autoFile,
			String version) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(
				new FileOutputStream(autoFile), StandardCharsets.UTF_8);
		try {
			writer.write("#FreeMind ");
			writer.write(version);
			writer.write('\n');
			writer.flush();
			Properties toBeStored = Tools.copyChangedProperties(current, defaults);
			toBeStored.store(writer, null);
		} finally {
			writer.close();
		}
	}
}
