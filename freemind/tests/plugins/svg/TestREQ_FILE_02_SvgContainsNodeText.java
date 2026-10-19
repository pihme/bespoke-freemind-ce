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

import java.awt.Font;
import java.awt.Rectangle;
import java.io.File;
import java.io.StringWriter;
import java.nio.file.Files;

import javax.swing.JPanel;

import org.apache.batik.svggen.SVGGraphics2D;
import org.junit.jupiter.api.Test;

import freemind.main.HeadlessFreeMind;
import freemind.view.mindmapview.IndependantMapViewCreator;
import freemind.view.mindmapview.MapView;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SVG export of a map view. PDF and the PDF options dialog stay untested.
 */
class TestREQ_FILE_02_SvgContainsNodeText {

	private static final String MAP = "<map version=\"1.0.0\">"
			+ "<node TEXT=\"SvgRoot\">"
			+ "<node TEXT=\"SvgChild\" POSITION=\"right\"/>"
			+ "</node></map>";

	static final class Probe extends ExportVectorGraphic {
		String svg(MapView view) throws Exception {
			SVGGraphics2D graphics = fillSVGGraphics2D(view);
			StringWriter writer = new StringWriter();
			graphics.stream(writer, false);
			return writer.toString();
		}
	}

	static final class FontedCreator extends IndependantMapViewCreator {
		@Override
		public Font getDefaultFont() {
			return new Font(Font.SANS_SERIF, Font.PLAIN, 16);
		}
	}

	@Test
	void TestREQ_FILE_02_SvgDocumentContainsNodeText() throws Exception {
		File mapFile = File.createTempFile("svg-export", ".mm");
		mapFile.deleteOnExit();
		Files.writeString(mapFile.toPath(), MAP);
		JPanel parent = new JPanel();
		parent.setBounds(new Rectangle(0, 0, 800, 600));
		MapView view = new FontedCreator().createMapViewForFile(
				mapFile.getPath(), parent, new HeadlessFreeMind());
		view.getRoot().updateAll();
		parent.doLayout();
		parent.validate();

		String svg = new Probe().svg(view);

		assertTrue(svg.contains("<svg"));
		assertTrue(svg.contains("SvgRoot"));
		assertTrue(svg.contains("SvgChild"));
	}
}
