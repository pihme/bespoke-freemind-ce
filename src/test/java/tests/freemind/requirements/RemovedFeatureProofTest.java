package tests.freemind.requirements;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import freemind.controller.Controller;
import freemind.main.FreeMind;
import freemind.view.mindmapview.MapView;

import org.junit.jupiter.api.Test;

class RemovedFeatureProofTest {

	@Test
	void TestREQ_PLUG_08_LegacyCollaborationSourcesAreGone() throws Exception {
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("plugins.collaboration.database.DatabaseStarter"));
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("plugins.collaboration.jabber.JabberStarter"));
		Path root = repoRoot();
		assertFalse(Files.exists(root.resolve("plugins/CollaborationDatabase.xml")));
		assertFalse(Files.exists(root.resolve("plugins/CollaborationJabber.xml")));
		assertFalse(Files.exists(root.resolve("plugins/collaboration")));
		assertFalse(Files.exists(root.resolve("pda")));
	}

	@Test
	void TestREQ_FILE_02_SvgAndPdfExportAreGone() throws Exception {
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("plugins.svg.ExportSvg"));
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("plugins.svg.ExportPdf"));
		Path root = repoRoot();
		assertFalse(Files.exists(root.resolve("plugins/ExportSvg.xml")));
		assertFalse(Files.exists(root.resolve("plugins/svg")));
	}

	@Test
	void TestREQ_PLUG_05_LatexOnANodeIsGone() throws Exception {
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("plugins.latex.LatexNodeHook"));
		Path root = repoRoot();
		assertFalse(Files.exists(root.resolve("plugins/Latex.xml")));
		assertFalse(Files.exists(root.resolve("plugins/latex")));
	}

	@Test
	void TestREQ_PLUG_06_SocketCollaborationIsGone() throws Exception {
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("plugins.collaboration.socket.MindMapMaster"));
		Path root = repoRoot();
		assertFalse(Files.exists(root.resolve("plugins/CollaborationSocket.xml")));
		assertFalse(Files.exists(root.resolve("plugins/collaboration/socket")));
	}

	@Test
	void TestREQ_PLUG_01_GroovyScriptingIsGone() throws Exception {
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("plugins.script.ScriptingEngine"));
		Path root = repoRoot();
		assertFalse(Files.exists(root.resolve("plugins/ScriptingEngine.xml")));
		assertFalse(Files.exists(root.resolve("plugins/script")));
	}

	@Test
	void TestREQ_FILE_04_ContextGraphExportIsGone() throws Exception {
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("plugins.contextgraph.ContextGraphMarkdownExport"));
		Path root = repoRoot();
		assertFalse(Files.exists(root.resolve("plugins/ContextGraph.xml")));
		assertFalse(Files.exists(root.resolve("plugins/contextgraph")));
	}

	@Test
	void TestREQ_PLUG_03_LuceneSearchIsGone() throws Exception {
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("plugins.search.SearchControllerHook"));
		Path root = repoRoot();
		assertFalse(Files.exists(root.resolve("plugins/Search.xml")));
		assertFalse(Files.exists(root.resolve("plugins/search")));
	}

	@Test
	void TestREQ_PLUG_02_GeographicMapIsGone() throws Exception {
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("plugins.map.MapDialog"));
		Path root = repoRoot();
		assertFalse(Files.exists(root.resolve("plugins/MapViewer.xml")));
		assertFalse(Files.exists(root.resolve("plugins/map/MapDialog.java")));
		assertFalse(Files.exists(root.resolve("plugins/map/JMapViewer.jar")));
		assertFalse(Files.exists(root.resolve("plugins/map")));
	}

	@Test
	void TestREQ_PLUG_04_IntegratedHelpIsGone() throws Exception {
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("plugins.help.FreemindHelpStarter"));
		Path root = repoRoot();
		assertFalse(Files.exists(root.resolve("plugins/FreemindHelp.xml")));
		assertFalse(Files.exists(root.resolve("plugins/help/doc/freemind.hs")));
		assertFalse(Files.exists(root.resolve("plugins/help/jhall.jar")));
		assertFalse(Files.exists(root.resolve("plugins/help")));
	}

	@Test
	void TestREQ_PLUG_07_SpellCheckingIsGone() throws Exception {
		assertThrows(NoSuchMethodException.class,
				() -> FreeMind.class.getDeclaredMethod("setupSpellChecking"));
		assertNull(FreeMind.class.getClassLoader().getResource("dictionaries.cnf"));
		Path root = repoRoot();
		Path freemind = root;
		assertFalse(Files.exists(freemind.resolve("dictionaries.cnf")));
		assertFalse(Files.exists(freemind.resolve("dictionaries.properties")));
		try (DirectoryStream<Path> dictionaries = Files.newDirectoryStream(freemind, "dictionary_*.ortho")) {
			assertFalse(dictionaries.iterator().hasNext());
		}
		assertFalse(Files.readString(freemind.resolve("src/main/resources/freemind.properties")).contains("check_spelling"));
		assertFalse(Files.readString(freemind.resolve("src/main/resources/Resources_en.properties"))
				.contains("OptionPanel.check_spelling"));
		assertFalse(Files.exists(freemind.resolve("lib/jortho.jar")));
	}

	@Test
	void TestREQ_FILE_01_HtmlExportIsGone() throws Exception {
		Path root = repoRoot();
		String menus = Files.readString(root.resolve("src/main/resources/mindmap_menus.xml"));
		assertFalse(menus.contains("exportToHTML"));
		assertFalse(menus.contains("exportBranchToHTML"));
		assertFalse(Files.exists(root.resolve("src/main/resources/accessories/plugins/ExportWithXSLT.xml")));
		assertFalse(Files.exists(root.resolve("src/main/resources/accessories/mm2html.xsl")));
		assertFalse(Files.exists(root.resolve("src/main/resources/accessories/toxhtml.xsl")));
		assertFalse(Files.readString(root.resolve("src/main/resources/freemind.properties")).contains("keystroke_export_to_html"));
		assertFalse(Files.readString(root.resolve("src/main/resources/freemind.properties")).contains("keystroke_export_branch_to_html"));
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
		assertFalse(Files.exists(root.resolve("src/main/resources/accessories/plugins/ExportWithXSLT.xml")));
		assertFalse(Files.exists(root.resolve("src/main/resources/accessories/plugins/ExportToOoWriter.xml")));
		assertFalse(Files.exists(root.resolve("src/main/resources/accessories/mm2text.xsl")));
		assertFalse(Files.exists(root.resolve("src/main/resources/accessories/mm2latex.xsl")));
		assertTrue(Files.exists(root.resolve("src/main/resources/accessories/mindmanager2mm.xsl")));
	}

	@Test
	void TestREQ_FILE_06_FolderImportIsGone() throws Exception {
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("freemind.modes.mindmapmode.actions.ImportFolderStructureAction"));
		Path root = repoRoot();
		String menus = Files.readString(root.resolve("src/main/resources/mindmap_menus.xml"));
		assertFalse(menus.contains("importFolderStructure"));
		assertTrue(menus.contains("importBranch"));
		assertTrue(menus.contains("importLinkedBranch"));
		assertTrue(menus.contains("importExplorerFavorites"));
		assertFalse(Files.readString(root.resolve("src/main/resources/Resources_en.properties"))
				.contains("import_folder_structure"));
		assertNotNull(Class.forName("freemind.modes.mindmapmode.actions.ImportExplorerFavoritesAction"));
	}

	@Test
	void TestREQ_APP_03_OnlyEnglishRemains() throws Exception {
		Path freemind = repoRoot();
		Path resources = freemind.resolve("src/main/resources");
		int bundles = 0;
		try (DirectoryStream<Path> files = Files.newDirectoryStream(resources, "Resources_*.properties")) {
			for (Path file : files) {
				bundles++;
				assertEquals("Resources_en.properties", file.getFileName().toString());
			}
		}
		assertEquals(1, bundles);
		assertTrue(Files.readString(resources.resolve("Resources_en.properties")).contains("file = &File"));
		String panel = Files.readString(freemind.resolve("src/main/java/freemind/preferences/layout/OptionPanel.java"));
		assertTrue(panel.contains("\"automatic\", \"en\""));
		assertFalse(panel.contains("\"de\""));
	}

	@Test
	void TestREQ_SEC_01_EncryptCommandIsGone() throws Exception {
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("accessories.plugins.EncryptNode"));
		Path root = repoRoot();
		assertFalse(Files.exists(root.resolve("src/main/java/accessories/plugins/EncryptNode.java")));
		assertFalse(Files.exists(root.resolve("src/main/resources/accessories/plugins/EncryptNode.xml")));
		assertFalse(Files.readString(root.resolve("src/main/resources/mindmap_menus.xml")).contains("name=\"crypto\""));
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
		assertFalse(Files.exists(root.resolve("src/main/java/freemind/modes/browsemode")));
		assertFalse(Files.exists(root.resolve("src/main/java/freemind/modes/filemode")));
		String modes = Files.readString(root.resolve("src/main/resources/freemind.properties"));
		assertFalse(modes.contains("freemind.modes.browsemode"));
		assertFalse(modes.contains("freemind.modes.filemode"));
		assertTrue(modes.contains("freemind.modes.mindmapmode.MindMapMode,MindMap"));
	}

	@Test
	void TestREQ_APP_02_PrintingIsGone() throws Exception {
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("freemind.controller.printpreview.PreviewDialog"));
		assertFalse(java.awt.print.Printable.class.isAssignableFrom(MapView.class));
		assertNotNull(MapView.class.getMethod("preparePrinting"));
		assertNotNull(MapView.class.getMethod("endPrinting"));
		assertThrows(NoSuchMethodException.class, () -> MapView.class.getMethod("print",
				java.awt.Graphics.class, java.awt.print.PageFormat.class, int.class));
		assertThrows(NoSuchFieldException.class, () -> Controller.class.getField("print"));
		assertThrows(NoSuchFieldException.class, () -> Controller.class.getField("printDirect"));
		assertThrows(NoSuchFieldException.class, () -> Controller.class.getField("printPreview"));
		assertThrows(NoSuchFieldException.class, () -> Controller.class.getField("page"));
		Path root = repoRoot();
		assertFalse(Files.exists(root.resolve("src/main/java/freemind/controller/printpreview")));
		assertFalse(Files.exists(root.resolve("src/main/resources/images/fileprint.png")));
		assertFalse(Files.readString(root.resolve("src/main/resources/mindmap_menus.xml")).contains("printDirect"));
		String properties = Files.readString(root.resolve("src/main/resources/freemind.properties"));
		assertFalse(properties.contains("keystroke_print"));
		assertFalse(properties.contains("fit_to_page"));
		assertTrue(properties.contains("printonwhitebackground"));
		assertNotNull(Class.forName("freemind.extensions.ExportHook"));
		assertNotNull(Class.forName("accessories.plugins.FitToPage"));
	}

	@Test
	void TestTAI_019_HelpMenuIsGone() throws Exception {
		assertThrows(NoSuchFieldException.class, () -> Controller.class.getField("documentation"));
		assertThrows(NoSuchFieldException.class, () -> Controller.class.getField("about"));
		assertThrows(NoSuchFieldException.class, () -> Controller.class.getField("license"));
		assertThrows(NoSuchFieldException.class, () -> Controller.class.getField("keyDocumentation"));
		assertThrows(NoSuchFieldException.class, () -> Controller.class.getField("freemindUrl"));
		assertThrows(NoSuchFieldException.class, () -> freemind.controller.MenuBar.class.getField("HELP_MENU"));
		Path root = repoRoot();
		String viewer = Files.readString(root.resolve("src/main/java/freemind/modes/mindmapmode/hooks/BuiltInHooks.java"));
		assertFalse(viewer.contains("menu_bar/help/"));
		assertTrue(viewer.contains("menu_bar/extras/first/logFileViewer"));
		assertNotNull(Class.forName("accessories.plugins.LogFileViewer"));
		assertTrue(Files.exists(root.resolve("LICENSE")));
		assertFalse(Files.exists(root.resolve("license")));
		assertFalse(Files.exists(root.resolve("plugins/help/jhall.jar")));
		assertTrue(Files.exists(root.resolve("src/main/resources/doc/freemind.mm")));
		assertFalse(Files.exists(root.resolve("src/main/resources/doc/FM_Key_Mappings_Quick_Guide.pdf")));
		String resources = Files.readString(root.resolve("src/main/resources/Resources_en.properties"));
		assertFalse(resources.contains("about = About"));
		assertTrue(resources.contains("file = &File"));
		assertFalse(Files.readString(root.resolve("src/main/java/freemind/controller/MenuBar.java")).contains("HELP_MENU"));
		String properties = Files.readString(root.resolve("src/main/resources/freemind.properties"));
		assertFalse(properties.contains("webDocuLocation"));
		assertFalse(properties.contains("webFreeMindLocation"));
		assertFalse(properties.contains("webFAQLocation"));
		assertTrue(properties.contains("simplyhtml.help"));
		assertTrue(properties.contains("browsemode_initial_map"));
	}

	@Test
	void TestTAI_020_StartupSplashIsGone() throws Exception {
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("freemind.main.FreeMindSplashModern"));
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("freemind.main.IFreeMindSplash"));
		Path root = repoRoot();
		assertFalse(Files.exists(root.resolve("src/main/java/freemind/main/FreeMindSplashModern.java")));
		assertFalse(Files.exists(root.resolve("src/main/resources/images/Freemind_Splash_Butterfly_Modern.png")));
		assertFalse(Files.exists(root.resolve("src/main/resources/images/splash.JPG")));
		assertFalse(Files.exists(root.resolve("src/main/resources/images/76812-freemind_v0.4.png")));
		assertFalse(Files.exists(root.resolve("src/main/resources/images/76812-freemind_v0.4.svg")));
		assertFalse(Files.readString(root.resolve("src/main/java/freemind/main/FreeMind.java")).contains("FreeMindSplashModern"));
		assertNotNull(Class.forName("freemind.main.FeedBack"));
		assertTrue(Files.exists(root.resolve("src/main/resources/images/FreeMindIcon256.png")));
		assertTrue(Files.readString(root.resolve("src/main/resources/freemind.properties")).contains("simplyhtml.splashImage"));
	}

	@Test
	void TestTAI_017_OnekinWikiExportIsGone() throws Exception {
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("onekin.WSL.WSL_Main"));
		Path root = repoRoot();
		assertFalse(Files.exists(root.resolve("plugins/wsl")));
		assertFalse(Files.exists(root.resolve("plugins")));
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
