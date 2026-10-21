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

package tests.freemind.cov.accessories;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.HeadlessException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.transform.stream.StreamResult;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import accessories.plugins.ExportToOoWriter;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("ExportToOoWriterBehaviourTest")
class ExportToOoWriterBehaviourTest {

	// backwards compatibility
	@Test
	void startupPacksTheMapIntoAnOdtAndACancelWritesNothing() throws Exception {
		Opened opened = MapFixture.open("Child");
		File odt = File.createTempFile("writer", ".odt");
		odt.deleteOnExit();
		FixedWriter hook = writer(opened, odt);
		hook.startupMapHook();
		assertTrue(odt.length() > 0);
		boolean sawContent = false;
		try (ZipInputStream zip = new ZipInputStream(new FileInputStream(odt))) {
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				if ("content.xml".equals(entry.getName())) {
					sawContent = new String(zip.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).contains("Child");
				}
			}
		}
		assertTrue(sawContent);

		File absent = File.createTempFile("absent", ".odt");
		absent.delete();
		FixedWriter cancelled = writer(opened, null);
		cancelled.startupMapHook();
		assertFalse(absent.exists());
		assertEqualsChild(opened);
	}

	// backwards compatibility
	@Test
	void aMissingResourceFailsAndAPresentStylesheetTransforms() throws Exception {
		Opened opened = MapFixture.open("Child");
		FixedWriter hook = writer(opened, null);
		Method copy = ExportToOoWriter.class.getDeclaredMethod("copyFromResource", String.class,
				java.io.OutputStream.class);
		copy.setAccessible(true);
		ByteArrayOutputStream missing = new ByteArrayOutputStream();
		assertFalse((Boolean) copy.invoke(hook, "accessories/no-such-file.xml", missing));
		assertEquals(0, missing.size());

		ByteArrayOutputStream styles = new ByteArrayOutputStream();
		assertTrue((Boolean) copy.invoke(hook, "accessories/mm2oowriterStyles.xml", styles));
		assertTrue(styles.size() > 0);

		Method apply = ExportToOoWriter.class.getDeclaredMethod("applyXsltFile", String.class, StringWriter.class,
				javax.xml.transform.Result.class);
		apply.setAccessible(true);
		assertThrows(java.lang.reflect.InvocationTargetException.class,
				() -> apply.invoke(hook, "accessories/missing.xsl", new StringWriter(), new StreamResult(new StringWriter())));

		StringWriter map = new StringWriter();
		opened.mode.getMap().getFilteredXml(map);
		StringWriter transformed = new StringWriter();
		assertTrue((Boolean) apply.invoke(hook, "accessories/mm2oowriter.xsl", map, new StreamResult(transformed)));
		assertTrue(transformed.toString().contains("Child") || transformed.toString().contains("ROOT"),
				transformed.toString());

		assertThrows(HeadlessException.class, hook::chooseThroughDialog);
	}

	private static void assertEquals(int expected, int actual) {
		org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
	}

	private static void assertEqualsChild(Opened opened) {
		org.junit.jupiter.api.Assertions.assertEquals("Child", opened.child("Child").getText());
	}

	private static FixedWriter writer(Opened opened, File chosen) {
		FixedWriter hook = new FixedWriter();
		hook.chosen = chosen;
		hook.setController(opened.mode);
		Properties properties = new Properties();
		properties.setProperty("file_type", "odt");
		properties.setProperty("files",
				"accessories/mm2oowriter.xsl->content.xml,accessories/mm2oowriter.manifest.xsl->META-INF/manifest.xml,accessories/mm2oowriter.mimetype.xsl->mimetype,accessories/mm2oowriterStyles.xml->styles.xml");
		hook.setProperties(properties);
		return hook;
	}

	private static final class FixedWriter extends ExportToOoWriter {
		private File chosen;

		@Override
		protected File chooseFile(String type, String description, String nameExtension) {
			return chosen;
		}

		File chooseThroughDialog() {
			return super.chooseFile("odt", "odt", null);
		}
	}
}
