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

package plugins.svg;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.xml.transform.stream.StreamSource;

import org.apache.batik.svggen.SVGGraphics2D;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("ExportVectorGraphicBehaviourTest")
class ExportVectorGraphicBehaviourTest {

	private static final String XSLT = """
			<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
			  <xsl:param name="destination_dir"/>
			  <xsl:param name="area_code"/>
			  <xsl:param name="folding_type"/>
			  <xsl:template match="/">
			    <out><xsl:value-of select="/doc/label"/>|<xsl:value-of select="$area_code"/>|<xsl:value-of select="$destination_dir"/>|<xsl:value-of select="$folding_type"/></out>
			  </xsl:template>
			</xsl:stylesheet>
			""";

	@Test
	void transformWritesTheStyledTextAndRejectsAMissingFileOrStylesheet() throws Exception {
		Opened opened = MapFixture.open("Alpha");
		Probe probe = new Probe();
		probe.setController(opened.mode);
		String folding = opened.frame.getProperty("html_export_folding");
		assertNotNull(folding);

		File directory = Files.createTempDirectory("svg-xslt").toFile();
		directory.deleteOnExit();
		File result = new File(directory, "styled.xml");
		result.deleteOnExit();
		probe.transForm(source("<doc><label>Kept</label></doc>"), xslt(XSLT), result, "zone-9");
		String written = Files.readString(result.toPath(), StandardCharsets.UTF_8);
		assertTrue(written.contains("Kept"), written);
		assertTrue(written.contains("zone-9"), written);
		assertTrue(written.contains("styled.xml_files/"), written);
		assertTrue(written.contains(folding), written);

		File broken = new File(directory, "broken.xml");
		broken.deleteOnExit();
		probe.transForm(source("<doc><label>Kept</label></doc>"), xslt("not-xslt"), broken, "zone-9");
		String failed = Files.readString(broken.toPath(), StandardCharsets.UTF_8);
		assertFalse(failed.contains("Kept"), failed);

		File missing = new File(directory, "gone/out.xml");
		assertThrows(FileNotFoundException.class,
				() -> probe.transForm(source("<doc/>"), xslt(XSLT), missing, "zone-9"));
	}

	@Test
	void graphicsCanvasAndMapViewIncludeThePaintedText() throws Exception {
		Opened opened = MapFixture.open("Alpha");
		SvgMapSupport.LaidOut map = SvgMapSupport.layOut(opened.frame);
		Probe probe = new Probe();

		SVGGraphics2D canvas = probe.createSvgGraphics2D();
		assertNotNull(canvas);
		canvas.setSVGCanvasSize(new Dimension(120, 40));
		canvas.drawString("CanvasMark", 4, 24);
		String painted = svg(canvas);
		assertTrue(painted.contains("<svg"), painted);
		assertTrue(painted.contains("CanvasMark"), painted);

		String document = svg(probe.fillSVGGraphics2D(map.view));
		assertTrue(document.contains("Alpha"), document);
		assertTrue(document.contains("ROOT"), document);

		String node = svg(probe.fillSVGGraphics2D(map.view, map.alpha));
		assertTrue(node.contains("Alpha"), node);
	}

	private static StreamSource source(String xml) {
		return new StreamSource(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
	}

	private static ByteArrayInputStream xslt(String sheet) {
		return new ByteArrayInputStream(sheet.getBytes(StandardCharsets.UTF_8));
	}

	private static String svg(SVGGraphics2D graphics) throws Exception {
		StringWriter writer = new StringWriter();
		graphics.stream(writer, false);
		return writer.toString();
	}

	private static final class Probe extends ExportVectorGraphic {
	}
}
