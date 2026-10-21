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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.HeadlessException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import accessories.plugins.ImportMindmanagerFiles;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("ImportMindmanagerFilesBehaviourTest")
class ImportMindmanagerFilesBehaviourTest {

	private static final String DOCUMENT = """
			<ap:Map xmlns:ap="http://schemas.mindjet.com/MindManager/Application/2003">
			  <ap:OneTopic>
			    <ap:Topic>
			      <ap:Text PlainText="ImportedTopic"/>
			    </ap:Topic>
			  </ap:OneTopic>
			</ap:Map>
			""";

	// backwards compatibility
	@Test
	void theMindManagerDocumentBecomesAMindMap() throws Exception {
		Opened opened = MapFixture.open("Child");
		ImportMindmanagerFiles hook = new ImportMindmanagerFiles();
		hook.setController(opened.mode);
		URL xslt = hook.getResource("accessories/mindmanager2mm.xsl");
		if (xslt == null) {
			File local = new File("freemind/accessories/mindmanager2mm.xsl");
			if (!local.isFile()) {
				local = new File("accessories/mindmanager2mm.xsl");
			}
			xslt = local.toURI().toURL();
		}
		String transformed = hook.transForm(new StreamSource(new ByteArrayInputStream(DOCUMENT.getBytes(StandardCharsets.UTF_8))),
				xslt.openStream());
		assertTrue(transformed.contains("ImportedTopic"), transformed);
		assertNull(hook.transForm(new StreamSource(new ByteArrayInputStream("<map/>".getBytes(StandardCharsets.UTF_8))),
				new ByteArrayInputStream("not-xslt".getBytes(StandardCharsets.UTF_8))));

		File zip = File.createTempFile("mind", ".mmap");
		zip.deleteOnExit();
		try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip))) {
			out.putNextEntry(new ZipEntry("skipped.txt"));
			out.write("no".getBytes(StandardCharsets.UTF_8));
			out.closeEntry();
			out.putNextEntry(new ZipEntry("Document.xml"));
			out.write(DOCUMENT.getBytes(StandardCharsets.UTF_8));
			out.closeEntry();
		}
		Method importFile = ImportMindmanagerFiles.class.getDeclaredMethod("importMindmanagerFile", File.class);
		importFile.setAccessible(true);
		try {
			importFile.invoke(hook, zip);
		} catch (java.lang.reflect.InvocationTargetException ex) {
			assertTrue(ex.getCause() instanceof NullPointerException);
		}
		File[] written = zip.getParentFile().listFiles((dir, name) -> name.startsWith(zip.getName())
				&& name.endsWith(".mm"));
		assertTrue(written != null && written.length >= 1);
		String xml = java.nio.file.Files.readString(written[0].toPath());
		assertTrue(xml.contains("ImportedTopic"), xml);
		written[0].delete();

		RuntimeException dialog = assertThrows(RuntimeException.class, hook::startupMapHook);
		assertTrue(dialog instanceof HeadlessException || dialog instanceof ClassCastException);
	}
}
