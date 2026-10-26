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

package tests.freemind.cov.xslt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import accessories.plugins.util.xslt.XmlExporter;
import freemind.main.Resources;

/** A stylesheet copies the source text, a missing result directory throws, and a broken stylesheet stays inside the call. */
@DisplayName("XmlExporterBehaviourTest")
class XmlExporterBehaviourTest {

	private static final String SOURCE = """
			<?xml version="1.0" encoding="UTF-8"?>
			<note>source-element-text</note>
			""";

	private static final String COPY_TEXT = """
			<?xml version="1.0" encoding="UTF-8"?>
			<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
			  <xsl:output method="text" encoding="UTF-8"/>
			  <xsl:template match="/">
			    <xsl:value-of select="note"/>
			  </xsl:template>
			</xsl:stylesheet>
			""";

	@Test
	void anIdentityTransformWritesTheSourceElementText() throws Exception {
		// backwards compatibility
		Path dir = Files.createTempDirectory("xslt-copy");
		try {
			File xml = write(dir, "source.xml", SOURCE);
			File xslt = write(dir, "copy.xsl", COPY_TEXT);
			File result = new File(dir.toFile(), "out.txt");

			new XmlExporter().transForm(xml, xslt, result);

			String written = Files.readString(result.toPath(), StandardCharsets.UTF_8);
			assertTrue(written.contains("source-element-text"), written);
		} finally {
			deleteTree(dir);
		}
	}

	@Test
	void aMissingResultDirectoryThrowsFileNotFoundException() throws Exception {
		// backwards compatibility
		Path dir = Files.createTempDirectory("xslt-missing");
		try {
			File xml = write(dir, "source.xml", SOURCE);
			File xslt = write(dir, "copy.xsl", COPY_TEXT);
			File result = new File(new File(dir.toFile(), "absent"), "out.txt");
			assertFalse(result.getParentFile().exists());

			FileNotFoundException thrown = assertThrows(FileNotFoundException.class,
					() -> new XmlExporter().transForm(xml, xslt, result));

			assertEquals(FileNotFoundException.class, thrown.getClass());
			assertTrue(thrown.getMessage().contains(result.getName()), thrown.getMessage());
			assertFalse(result.exists());
		} finally {
			deleteTree(dir);
		}
	}

	@Test
	void aBrokenStylesheetIsCaughtAndTheResultFileExists() throws Exception {
		// backwards compatibility
		Path dir = Files.createTempDirectory("xslt-broken");
		Field instance = Resources.class.getDeclaredField("resourcesInstance");
		instance.setAccessible(true);
		Object previous = instance.get(null);
		try {
			File xml = write(dir, "source.xml", SOURCE);
			File xslt = write(dir, "broken.xsl", "this is not a stylesheet");
			File result = new File(dir.toFile(), "out.txt");

			new XmlExporter().transForm(xml, xslt, result);

			assertTrue(result.isFile());
		} finally {
			instance.set(null, previous);
			deleteTree(dir);
		}
	}

	private static File write(Path dir, String name, String text) throws IOException {
		Path path = dir.resolve(name);
		Files.writeString(path, text, StandardCharsets.UTF_8);
		return path.toFile();
	}

	private static void deleteTree(Path root) throws IOException {
		if (!Files.exists(root)) {
			return;
		}
		try (var walk = Files.walk(root)) {
			for (Path path : walk.sorted(Comparator.reverseOrder()).toList()) {
				Files.deleteIfExists(path);
			}
		}
	}
}
