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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.HeadlessException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Properties;

import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.Test;

import accessories.plugins.ExportWithXSLT;
import freemind.main.Resources;
import freemind.modes.ControllerAdapter;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

class ExportWithXSLTBehaviourTest {

	private static final byte[] PNG = Base64.getDecoder().decode(
			"iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");

	// backwards compatibility
	@Test
	void htmlExportWritesTheMapTheIconsAndTheLinkedFiles() throws Exception {
		Opened opened = MapFixture.open("Child");
		File html = File.createTempFile("export", ".html");
		html.deleteOnExit();
		File iconDir = new File(Resources.getInstance().getFreemindDirectory(), "icons");
		boolean createdDir = false;
		File named = new File(iconDir, "ab.png");
		File skipped = new File(iconDir, ".png");
		try {
			if (!iconDir.exists()) {
				createdDir = iconDir.mkdirs();
			}
			Files.write(named.toPath(), PNG);
			Files.write(skipped.toPath(), PNG);
			FixedExport hook = export(opened, html, true, true);
			assertNull(hook.transform(html));
			hook.startupMapHook();
			assertTrue(hook.isTransformResultWithoutError());
			String page = Files.readString(html.toPath());
			assertTrue(page.contains("Child") || page.contains("ROOT"), page);
			File directory = new File(html.getAbsolutePath() + "_files");
			assertTrue(new File(directory, "map.mm").isFile());
			assertTrue(new File(directory, "icons").isDirectory());
			assertTrue(new File(new File(directory, "icons"), "ab.png").isFile());
			assertFalse(new File(new File(directory, "icons"), ".png").isFile());
		} finally {
			named.delete();
			skipped.delete();
			if (createdDir) {
				iconDir.delete();
			}
		}
	}

	// backwards compatibility
	@Test
	void aBlockedDirectoryAndABadTemplateReportAnError() throws Exception {
		Opened opened = MapFixture.open("Child");
		File html = File.createTempFile("blocked", ".html");
		html.deleteOnExit();
		File occupied = new File(html.getAbsolutePath() + "_files");
		occupied.delete();
		assertTrue(occupied.createNewFile());
		occupied.deleteOnExit();
		FixedExport hook = export(opened, html, true, false);
		hook.startupMapHook();
		assertFalse(hook.isTransformResultWithoutError());
		assertEquals("Child", opened.child("Child").getText());

		File plain = File.createTempFile("plain", ".html");
		plain.deleteOnExit();
		FixedExport bad = export(opened, plain, false, false);
		bad.xslt("images/knotes.png");
		assertEquals("error_applying_template", bad.transform(plain));

		File direct = File.createTempFile("direct", ".html");
		direct.deleteOnExit();
		URLStream xslt = new URLStream();
		assertTrue(bad.transform(new StreamSource(new ByteArrayInputStream("<map/>".getBytes(StandardCharsets.UTF_8))),
				xslt.stream(), direct, ""));
		assertTrue(direct.length() > 0);

		Method copyMap = ExportWithXSLT.class.getDeclaredMethod("copyMap", String.class);
		copyMap.setAccessible(true);
		File folder = Files.createTempDirectory("copymap").toFile();
		assertTrue((Boolean) copyMap.invoke(bad, folder.getAbsolutePath()));
		assertTrue(new File(folder, "map.mm").isFile());

		Field view = ControllerAdapter.class.getDeclaredField("mView");
		view.setAccessible(true);
		Object previous = view.get(opened.mode);
		view.set(opened.mode, null);
		try {
			Method image = ExportWithXSLT.class.getDeclaredMethod("createImageFromMap", String.class);
			image.setAccessible(true);
			image.invoke(bad, folder.getAbsolutePath());
			assertFalse(new File(folder, "image.png").isFile());
		} finally {
			view.set(opened.mode, previous);
		}

		FixedExport cancelled = export(opened, null, false, false);
		cancelled.startupMapHook();
		assertFalse(cancelled.isTransformResultWithoutError());
		assertThrows(HeadlessException.class, cancelled::chooseThroughDialog);
	}

	private static FixedExport export(Opened opened, File chosen, boolean createDir, boolean copyMap) {
		FixedExport hook = new FixedExport();
		hook.chosen = chosen;
		hook.setController(opened.mode);
		Properties properties = new Properties();
		properties.setProperty("file_type", "html");
		properties.setProperty("file_description", "html");
		properties.setProperty("xslt_file", "accessories/toxhtml.xsl");
		properties.setProperty("create_dir", createDir ? "true" : "false");
		properties.setProperty("files_to_copy", "treestyles.css");
		properties.setProperty("file_prefix", "accessories/");
		properties.setProperty("copy_icons", "true");
		properties.setProperty("copy_map", copyMap ? "true" : "false");
		properties.setProperty("load_file", "false");
		properties.setProperty("create_html_linked_image", "false");
		hook.setProperties(properties);
		return hook;
	}

	private static final class FixedExport extends ExportWithXSLT {
		private File chosen;

		@Override
		protected File chooseFile() {
			return chosen;
		}

		File chooseThroughDialog() {
			return super.chooseFile();
		}

		void xslt(String name) {
			getProperties().setProperty("xslt_file", name);
		}
	}

	private static final class URLStream {
		java.io.InputStream stream() {
			String xslt = """
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
					  <xsl:template match="/"><out>styled</out></xsl:template>
					</xsl:stylesheet>
					""";
			return new ByteArrayInputStream(xslt.getBytes(StandardCharsets.UTF_8));
		}
	}
}
