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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.HeadlessException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.view.mindmapview.MapView;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("ExportSvgBehaviourTest")
class ExportSvgBehaviourTest {

	@Test
	void startupWritesUtf8SvgWhenAFileIsChosenAndNothingWhenItIsNot() throws Exception {
		Opened opened = MapFixture.open("Alpha");
		opened.mode.setView(SvgMapSupport.layOut(opened.frame).view);
		File svg = File.createTempFile("export", ".svg");
		svg.deleteOnExit();

		ScriptedSvg cancelled = hook(opened);
		cancelled.startupMapHook();
		assertEquals(0, svg.length());
		assertEquals("svg", cancelled.type);
		assertEquals("SVG file", cancelled.description);

		ScriptedSvg written = hook(opened);
		written.chosen = svg;
		written.startupMapHook();
		String text = Files.readString(svg.toPath(), StandardCharsets.UTF_8);
		assertTrue(text.contains("encoding=\"UTF-8\""), text);
		assertTrue(text.contains("<svg"), text);
		assertTrue(text.contains("Alpha"), text);
		assertTrue(text.contains("ROOT"), text);
		assertEquals("Alpha", opened.child("Alpha").getText());
	}

	@Test
	void startupWritesNothingForAMissingViewAndReportsAFailedWrite() throws Exception {
		Opened opened = MapFixture.open("Alpha");
		opened.mode.setView(SvgMapSupport.layOut(opened.frame).view);
		File svg = File.createTempFile("missing-view", ".svg");
		svg.deleteOnExit();
		ScriptedSvg hidden = hook(opened);
		hidden.chosen = svg;
		MapView view = opened.mode.getView();
		opened.mode.setView(null);
		try {
			hidden.startupMapHook();
			assertEquals(0, svg.length());
		} finally {
			opened.mode.setView(view);
		}

		File directory = Files.createTempDirectory("svg-dir").toFile();
		directory.deleteOnExit();
		ScriptedSvg blocked = hook(opened);
		blocked.chosen = directory;
		assertThrows(HeadlessException.class, blocked::startupMapHook);
		assertTrue(directory.isDirectory());
		assertFalse(new File(directory, "Alpha.svg").isFile());
	}

	private static ScriptedSvg hook(Opened opened) {
		ScriptedSvg hook = new ScriptedSvg();
		hook.setController(opened.mode);
		Properties properties = new Properties();
		properties.setProperty("export_svg_text", "SVG file");
		hook.setProperties(properties);
		return hook;
	}

	private static final class ScriptedSvg extends ExportSvg {
		private File chosen;
		private String type;
		private String description;

		@Override
		protected File chooseFile(String type, String description, String nameExtension) {
			this.type = type;
			this.description = description;
			return chosen;
		}
	}
}
