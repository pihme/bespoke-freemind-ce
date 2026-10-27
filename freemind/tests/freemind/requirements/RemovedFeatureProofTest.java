package tests.freemind.requirements;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import freemind.main.FreeMind;

import org.junit.jupiter.api.Test;

class RemovedFeatureProofTest {

	@Test
	void TestREQ_PLUG_08_LegacyCollaborationSourcesAreGone() throws Exception {
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("plugins.collaboration.database.DatabaseStarter"));
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("plugins.collaboration.jabber.JabberStarter"));
		Path root = repoRoot();
		assertFalse(Files.exists(root.resolve("freemind/plugins/CollaborationDatabase.xml")));
		assertFalse(Files.exists(root.resolve("freemind/plugins/CollaborationJabber.xml")));
		assertFalse(Files.exists(root.resolve("freemind/plugins/collaboration")));
		assertFalse(Files.exists(root.resolve("pda")));
	}

	@Test
	void TestREQ_FILE_02_SvgAndPdfExportAreGone() throws Exception {
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("plugins.svg.ExportSvg"));
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("plugins.svg.ExportPdf"));
		Path root = repoRoot();
		assertFalse(Files.exists(root.resolve("freemind/plugins/ExportSvg.xml")));
		assertFalse(Files.exists(root.resolve("freemind/plugins/svg")));
	}

	@Test
	void TestREQ_PLUG_05_LatexOnANodeIsGone() throws Exception {
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("plugins.latex.LatexNodeHook"));
		Path root = repoRoot();
		assertFalse(Files.exists(root.resolve("freemind/plugins/Latex.xml")));
		assertFalse(Files.exists(root.resolve("freemind/plugins/latex")));
	}

	@Test
	void TestREQ_PLUG_06_SocketCollaborationIsGone() throws Exception {
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("plugins.collaboration.socket.MindMapMaster"));
		Path root = repoRoot();
		assertFalse(Files.exists(root.resolve("freemind/plugins/CollaborationSocket.xml")));
		assertFalse(Files.exists(root.resolve("freemind/plugins/collaboration/socket")));
	}

	@Test
	void TestREQ_PLUG_01_GroovyScriptingIsGone() throws Exception {
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("plugins.script.ScriptingEngine"));
		Path root = repoRoot();
		assertFalse(Files.exists(root.resolve("freemind/plugins/ScriptingEngine.xml")));
		assertFalse(Files.exists(root.resolve("freemind/plugins/script")));
	}

	@Test
	void TestREQ_FILE_04_ContextGraphExportIsGone() throws Exception {
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("plugins.contextgraph.ContextGraphMarkdownExport"));
		Path root = repoRoot();
		assertFalse(Files.exists(root.resolve("freemind/plugins/ContextGraph.xml")));
		assertFalse(Files.exists(root.resolve("freemind/plugins/contextgraph")));
	}

	@Test
	void TestREQ_PLUG_03_LuceneSearchIsGone() throws Exception {
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("plugins.search.SearchControllerHook"));
		Path root = repoRoot();
		assertFalse(Files.exists(root.resolve("freemind/plugins/Search.xml")));
		assertFalse(Files.exists(root.resolve("freemind/plugins/search")));
	}

	@Test
	void TestREQ_PLUG_02_GeographicMapIsGone() throws Exception {
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("plugins.map.MapDialog"));
		Path root = repoRoot();
		assertFalse(Files.exists(root.resolve("freemind/plugins/MapViewer.xml")));
		assertFalse(Files.exists(root.resolve("freemind/plugins/map/MapDialog.java")));
		assertTrue(Files.exists(root.resolve("freemind/plugins/map/JMapViewer.jar")));
	}

	@Test
	void TestREQ_PLUG_04_IntegratedHelpIsGone() throws Exception {
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("plugins.help.FreemindHelpStarter"));
		Path root = repoRoot();
		assertFalse(Files.exists(root.resolve("freemind/plugins/FreemindHelp.xml")));
		assertFalse(Files.exists(root.resolve("freemind/plugins/help/doc/freemind.hs")));
		assertTrue(Files.exists(root.resolve("freemind/plugins/help/jhall.jar")));
	}

	@Test
	void TestREQ_PLUG_07_SpellCheckingIsGone() throws Exception {
		assertThrows(NoSuchMethodException.class,
				() -> FreeMind.class.getDeclaredMethod("setupSpellChecking"));
		assertNull(FreeMind.class.getClassLoader().getResource("dictionaries.cnf"));
		Path root = repoRoot();
		Path freemind = root.resolve("freemind");
		assertFalse(Files.exists(freemind.resolve("dictionaries.cnf")));
		assertFalse(Files.exists(freemind.resolve("dictionaries.properties")));
		try (DirectoryStream<Path> dictionaries = Files.newDirectoryStream(freemind, "dictionary_*.ortho")) {
			assertFalse(dictionaries.iterator().hasNext());
		}
		assertFalse(Files.readString(freemind.resolve("freemind.properties")).contains("check_spelling"));
		assertFalse(Files.readString(freemind.resolve("Resources_en.properties"))
				.contains("OptionPanel.check_spelling"));
		assertTrue(Files.exists(freemind.resolve("lib/jortho.jar")));
	}

	@Test
	void TestREQ_FILE_01_HtmlExportIsGone() throws Exception {
		Path root = repoRoot();
		String menus = Files.readString(root.resolve("freemind/mindmap_menus.xml"));
		assertFalse(menus.contains("exportToHTML"));
		assertFalse(menus.contains("exportBranchToHTML"));
		assertFalse(Files.exists(root.resolve("freemind/accessories/plugins/ExportWithXSLT.xml")));
		assertFalse(Files.exists(root.resolve("freemind/accessories/mm2html.xsl")));
		assertFalse(Files.exists(root.resolve("freemind/accessories/toxhtml.xsl")));
		assertFalse(Files.readString(root.resolve("freemind/freemind.properties")).contains("keystroke_export_to_html"));
		assertFalse(Files.readString(root.resolve("freemind/freemind.properties")).contains("keystroke_export_branch_to_html"));
		assertNotNull(Class.forName("freemind.modes.mindmapmode.MindMapHTMLWriter"));
	}

	@Test
	void TestREQ_FILE_07_XsltExportsAreGone() throws Exception {
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("accessories.plugins.ExportWithXSLT"));
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("accessories.plugins.ExportToOoWriter"));
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("accessories.plugins.util.xslt.ExportDialog"));
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("accessories.plugins.util.xslt.XmlExporter"));
		Path root = repoRoot();
		assertFalse(Files.exists(root.resolve("freemind/accessories/plugins/ExportWithXSLT.xml")));
		assertFalse(Files.exists(root.resolve("freemind/accessories/plugins/ExportToOoWriter.xml")));
		assertFalse(Files.exists(root.resolve("freemind/accessories/mm2text.xsl")));
		assertFalse(Files.exists(root.resolve("freemind/accessories/mm2latex.xsl")));
		assertTrue(Files.exists(root.resolve("freemind/accessories/mindmanager2mm.xsl")));
	}

	@Test
	void TestREQ_FILE_06_FolderImportIsGone() throws Exception {
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("freemind.modes.mindmapmode.actions.ImportFolderStructureAction"));
		Path root = repoRoot();
		String menus = Files.readString(root.resolve("freemind/mindmap_menus.xml"));
		assertFalse(menus.contains("importFolderStructure"));
		assertTrue(menus.contains("importBranch"));
		assertTrue(menus.contains("importLinkedBranch"));
		assertTrue(menus.contains("importExplorerFavorites"));
		assertFalse(Files.readString(root.resolve("freemind/Resources_en.properties"))
				.contains("import_folder_structure"));
		assertNotNull(Class.forName("freemind.modes.mindmapmode.actions.ImportExplorerFavoritesAction"));
	}

	@Test
	void TestREQ_SEC_01_EncryptCommandIsGone() throws Exception {
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("accessories.plugins.EncryptNode"));
		Path root = repoRoot();
		assertFalse(Files.exists(root.resolve("freemind/accessories/plugins/EncryptNode.java")));
		assertFalse(Files.exists(root.resolve("freemind/accessories/plugins/EncryptNode.xml")));
		assertFalse(Files.readString(root.resolve("freemind/mindmap_menus.xml")).contains("name=\"crypto\""));
		assertNotNull(Class.forName("freemind.modes.mindmapmode.EncryptedMindMapNode"));
	}

	@Test
	void TestREQ_MODE_01_BrowseAndFileModesAreGone() throws Exception {
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("freemind.modes.browsemode.BrowseMode"));
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("freemind.modes.filemode.FileMode"));
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("freemind.modes.common.plugins.MapNodePositionHolderBase"));
		Path root = repoRoot();
		assertFalse(Files.exists(root.resolve("freemind/freemind/modes/browsemode")));
		assertFalse(Files.exists(root.resolve("freemind/freemind/modes/filemode")));
		String modes = Files.readString(root.resolve("freemind/freemind.properties"));
		assertFalse(modes.contains("freemind.modes.browsemode"));
		assertFalse(modes.contains("freemind.modes.filemode"));
		assertTrue(modes.contains("freemind.modes.mindmapmode.MindMapMode,MindMap"));
	}

	private static Path repoRoot() {
		Path root = Path.of(System.getProperty("user.dir")).toAbsolutePath();
		while (root != null && !Files.exists(root.resolve("settings.gradle.kts"))) {
			root = root.getParent();
		}
		assertNotNull(root);
		return root;
	}
}
