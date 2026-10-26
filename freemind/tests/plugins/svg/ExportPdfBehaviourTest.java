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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.HeadlessException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.batik.transcoder.TranscodingHints;
import org.apache.fop.svg.PDFTranscoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.MindMapNode;
import freemind.view.mindmapview.MapView;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("ExportPdfBehaviourTest")
class ExportPdfBehaviourTest {

	@Test
	void exportAsPdfWritesAPdfForHintsAndForNoHintsAndRejectsAMissingView() throws Exception {
		Opened opened = MapFixture.open("Alpha");
		SvgMapSupport.LaidOut map = SvgMapSupport.layOut(opened.frame);
		opened.mode.setView(map.view);
		ExportPdf hook = new ExportPdf();
		hook.setController(opened.mode);
		hook.setProperties(new Properties());

		File hinted = File.createTempFile("hinted", ".pdf");
		hinted.deleteOnExit();
		assertTrue(hook.exportAsPdf(true, map.alpha, hinted, pageHints()));
		assertPdf(hinted);

		File plain = File.createTempFile("plain", ".pdf");
		plain.deleteOnExit();
		assertTrue(hook.exportAsPdf(false, map.root, plain));
		assertPdf(plain);

		File untouched = File.createTempFile("noview", ".pdf");
		untouched.deleteOnExit();
		opened.mode.setView(null);
		try {
			assertFalse(hook.exportAsPdf(false, map.root, untouched, null));
			assertEquals(0, untouched.length());
		} finally {
			opened.mode.setView(map.view);
		}
		assertEquals("Alpha", map.alpha.getText());
	}

	@Test
	void startupMapHookWritesNothingUntilAFileAndAPaperAreBothChosen() throws Exception {
		Opened opened = MapFixture.open("Alpha");
		SvgMapSupport.LaidOut map = SvgMapSupport.layOut(opened.frame);
		opened.mode.setView(map.view);

		ScriptedPdf idle = hook(opened, "map");
		clearSelection(map.view);
		idle.startupMapHook();
		assertTrue(idle.types.isEmpty());

		ScriptedPdf cancelled = hook(opened, "node");
		select(map.view, map.root, map.alpha);
		cancelled.startupMapHook();
		assertEquals(List.of("pdf"), cancelled.types);
		assertTrue(cancelled.extensions.get(0).contains("Alpha")
				|| cancelled.extensions.get(0).contains("ROOT"));

		File rejected = File.createTempFile("paper", ".pdf");
		rejected.deleteOnExit();
		ScriptedPdf dialog = hook(opened, "map");
		dialog.realPaper = true;
		dialog.choices.add(rejected);
		select(map.view, map.root);
		assertThrows(HeadlessException.class, dialog::choosePaper);
		assertThrows(HeadlessException.class, dialog::startupMapHook);
		assertEquals(0, rejected.length());

		File declined = File.createTempFile("declined", ".pdf");
		declined.deleteOnExit();
		ScriptedPdf noPaper = hook(opened, "map");
		noPaper.choices.add(declined);
		select(map.view, map.root);
		noPaper.startupMapHook();
		assertEquals(0, declined.length());
	}

	@Test
	void startupMapHookWritesTheChosenPdfsAndReportsAFailedWrite() throws Exception {
		Opened opened = MapFixture.open("Alpha");
		SvgMapSupport.LaidOut map = SvgMapSupport.layOut(opened.frame);
		opened.mode.setView(map.view);

		File mapPdf = File.createTempFile("map", ".pdf");
		mapPdf.deleteOnExit();
		ScriptedPdf whole = hook(opened, "map");
		whole.paper = pageHints();
		whole.choices.add(mapPdf);
		select(map.view, map.root, map.alpha);
		whole.startupMapHook();
		assertPdf(mapPdf);
		assertEquals(1, whole.extensions.size());
		assertNull(whole.extensions.get(0));

		File first = File.createTempFile("node-a", ".pdf");
		File second = File.createTempFile("node-b", ".pdf");
		first.deleteOnExit();
		second.deleteOnExit();
		ScriptedPdf nodes = hook(opened, "node");
		nodes.paper = pageHints();
		nodes.choices.add(first);
		nodes.choices.add(second);
		select(map.view, map.root, map.alpha);
		nodes.startupMapHook();
		assertPdf(first);
		assertPdf(second);
		assertEquals(2, nodes.extensions.size());
		assertTrue(nodes.extensions.get(0).contains("Alpha"));
		assertTrue(nodes.extensions.get(1).contains("ROOT"));

		File directory = Files.createTempDirectory("pdf-dir").toFile();
		directory.deleteOnExit();
		ScriptedPdf blocked = hook(opened, "map");
		blocked.paper = pageHints();
		blocked.choices.add(directory);
		select(map.view, map.root);
		assertThrows(HeadlessException.class, blocked::startupMapHook);
		assertTrue(directory.isDirectory());
	}

	private static ScriptedPdf hook(Opened opened, String exportType) {
		ScriptedPdf hook = new ScriptedPdf();
		hook.setController(opened.mode);
		Properties properties = new Properties();
		properties.setProperty("export_type", exportType);
		properties.setProperty("export_pdf_text", "PDF");
		hook.setProperties(properties);
		return hook;
	}

	private static HashMap<TranscodingHints.Key, Float> pageHints() {
		HashMap<TranscodingHints.Key, Float> hints = new HashMap<>();
		hints.put(PDFTranscoder.KEY_WIDTH, Float.valueOf(420f));
		hints.put(PDFTranscoder.KEY_HEIGHT, Float.valueOf(280f));
		return hints;
	}

	private static void select(MapView view, MindMapNode... nodes) {
		clearSelection(view);
		view.selectAsTheOnlyOneSelected(view.getNodeView(nodes[0]));
		for (int i = 1; i < nodes.length; i++) {
			view.toggleSelected(view.getNodeView(nodes[i]));
		}
	}

	private static void clearSelection(MapView view) {
		while (view.getSelected() != null) {
			view.deselect(view.getSelected());
		}
	}

	private static void assertPdf(File file) throws Exception {
		byte[] bytes = Files.readAllBytes(file.toPath());
		assertTrue(bytes.length >= 4, "pdf length " + bytes.length);
		assertEquals("%PDF", new String(bytes, 0, 4, StandardCharsets.US_ASCII));
	}

	private static final class ScriptedPdf extends ExportPdf {
		private final List<File> choices = new ArrayList<>();
		private final List<String> extensions = new ArrayList<>();
		private final List<String> types = new ArrayList<>();
		private HashMap<TranscodingHints.Key, Float> paper;
		private boolean realPaper;

		@Override
		protected File chooseFile(String type, String description, String nameExtension) {
			types.add(type);
			extensions.add(nameExtension);
			if (choices.isEmpty()) {
				return null;
			}
			return choices.remove(0);
		}

		@Override
		public HashMap<TranscodingHints.Key, Float> choosePaper() {
			if (realPaper) {
				return super.choosePaper();
			}
			return paper;
		}
	}
}
