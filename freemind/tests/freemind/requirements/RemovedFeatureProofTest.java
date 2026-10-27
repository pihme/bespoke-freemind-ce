package tests.freemind.requirements;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

	private static Path repoRoot() {
		Path root = Path.of(System.getProperty("user.dir")).toAbsolutePath();
		while (root != null && !Files.exists(root.resolve("settings.gradle.kts"))) {
			root = root.getParent();
		}
		assertNotNull(root);
		return root;
	}
}
