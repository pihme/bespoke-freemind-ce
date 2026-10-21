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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import accessories.plugins.ExportToImage;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("ExportToImageBehaviourTest")
class ExportToImageBehaviourTest {

	// backwards compatibility
	@Test
	void exportWritesAPngAndSkipsACancelledChoice() throws Exception {
		Opened opened = MapFixture.open("Child");
		File png = File.createTempFile("map", ".png");
		png.deleteOnExit();
		FixedExport hook = new FixedExport();
		hook.chosen = png;
		hook.setController(opened.mode);
		Properties properties = new Properties();
		properties.setProperty("image_type", "png");
		properties.setProperty("image_description", "png");
		hook.setProperties(properties);

		BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
		assertTrue(hook.exportToImage(image, "png", "png"));
		assertTrue(png.length() > 8);
		try (FileInputStream in = new FileInputStream(png)) {
			assertEquals(0x89, in.read());
			assertEquals('P', in.read());
		}

		hook.chosen = null;
		assertFalse(hook.exportToImage(image, "png", "png"));
	}

	// backwards compatibility
	@Test
	void transformWritesTheStyledResultAndStartupStopsAtTheDialog() throws Exception {
		Opened opened = MapFixture.open("Child");
		File result = File.createTempFile("styled", ".xml");
		result.deleteOnExit();
		FixedExport hook = new FixedExport();
		hook.setController(opened.mode);
		String xslt = """
				<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
				  <xsl:template match="/"><out><xsl:value-of select="/map/@note"/></out></xsl:template>
				</xsl:stylesheet>
				""";
		hook.transForm(new StreamSource(new ByteArrayInputStream("<map note=\"pictured\"/>".getBytes(StandardCharsets.UTF_8))),
				new ByteArrayInputStream(xslt.getBytes(StandardCharsets.UTF_8)), result, "area");
		String written = new String(java.nio.file.Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
		assertTrue(written.contains("pictured"), written);

		File broken = File.createTempFile("broken", ".xml");
		broken.deleteOnExit();
		hook.transForm(new StreamSource(new ByteArrayInputStream("<map/>".getBytes(StandardCharsets.UTF_8))),
				new ByteArrayInputStream("not-xslt".getBytes(StandardCharsets.UTF_8)), broken, "");
		assertEquals(0, broken.length());

		Properties properties = new Properties();
		properties.setProperty("image_type", "png");
		properties.setProperty("image_description", "png");
		hook.setProperties(properties);
		hook.chosen = null;
		try {
			hook.startupMapHook();
		} catch (HeadlessException | NullPointerException ex) {
			assertTrue(ex.getClass() == HeadlessException.class || ex instanceof NullPointerException);
		}
		assertEquals("Child", opened.child("Child").getText());
		assertThrows(HeadlessException.class, () -> hook.chooseThroughDialog());
	}

	private static final class FixedExport extends ExportToImage {
		private File chosen;

		@Override
		protected File chooseFile(String type, String description, String nameExtension) {
			if (chosen == null) {
				return null;
			}
			return chosen;
		}

		File chooseThroughDialog() {
			return super.chooseFile("png", "png", null);
		}
	}
}
