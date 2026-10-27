package tests.freemind.requirements;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class RemovedFeatureProofTest {

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
	void TestREQ_PLUG_04_IntegratedHelpIsGone() throws Exception {
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("plugins.help.FreemindHelpStarter"));
		Path root = repoRoot();
		assertFalse(Files.exists(root.resolve("freemind/plugins/FreemindHelp.xml")));
		assertFalse(Files.exists(root.resolve("freemind/plugins/help/doc/freemind.hs")));
		assertTrue(Files.exists(root.resolve("freemind/plugins/help/jhall.jar")));
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
