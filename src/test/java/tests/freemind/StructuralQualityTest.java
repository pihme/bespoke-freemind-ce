package tests.freemind;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import freemind.modes.mindmapmode.hooks.BuiltInHooks;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Structural quality tests that verify architecture and code quality invariants
 * by scanning source files at test time.
 */
@DisplayName("Structural Quality Invariants")
class StructuralQualityTest {

    /**
     * Maximum allowed System.out/System.err references in production code.
     * This is a bounded count to prevent explosion -- the goal is to reduce over time.
     */
    private static final int MAX_SYSTEM_OUT_ERR_COUNT = 200;

    /**
     * Maximum allowed empty catch blocks in production code.
     */
    private static final int MAX_EMPTY_CATCH_BLOCKS = 200;

    /**
     * Maximum allowed TODO/FIXME comments in source code.
     */
    private static final int MAX_TODO_FIXME_COUNT = 300;

    @Test
    @DisplayName("System.out/System.err usage in production code is bounded")
    void systemOutErrBounded() throws IOException {
        Path root = findProjectRoot();
        int count = 0;

        try (Stream<Path> walk = Files.walk(root)) {
            List<Path> javaFiles = productionJavaFiles(walk);

            Pattern sysOutErr = Pattern.compile(
                    "System\\.(out|err)\\.(println|print|printf|write|format)\\(");

            for (Path file : javaFiles) {
                String content = Files.readString(file);
                String[] lines = content.split("\n");
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (!trimmed.startsWith("//") && !trimmed.startsWith("*")
                            && sysOutErr.matcher(trimmed).find()) {
                        count++;
                    }
                }
            }
        }

        assertTrue(count <= MAX_SYSTEM_OUT_ERR_COUNT,
                "System.out/err usage count (" + count + ") exceeds bound ("
                        + MAX_SYSTEM_OUT_ERR_COUNT + "). Migrate to logger.");
    }

    @Test
    @DisplayName("Serializable classes have serialVersionUID")
    void serializableClassesHaveSerialVersionUID() throws IOException {
        Path root = findProjectRoot();
        List<String> violations = new ArrayList<>();

        Pattern implementsSerializable = Pattern.compile(
                "class\\s+\\w+.*\\bimplements\\b[^{]*\\bSerializable\\b");

        try (Stream<Path> walk = Files.walk(root)) {
            List<Path> javaFiles = productionJavaFiles(walk);

            for (Path file : javaFiles) {
                String content = Files.readString(file);
                if (implementsSerializable.matcher(content).find()
                        && !content.contains("serialVersionUID")) {
                    violations.add(root.getParent().relativize(file).toString());
                }
            }
        }

        assertTrue(violations.isEmpty(),
                "Serializable classes without serialVersionUID:\n"
                        + String.join("\n", violations));
    }

    @Test
    @DisplayName("Empty catch blocks are bounded")
    void emptyCatchBlocksBounded() throws IOException {
        Path root = findProjectRoot();
        int count = 0;

        // Match catch block followed by optional whitespace and closing brace
        Pattern emptyCatch = Pattern.compile(
                "catch\\s*\\([^)]*\\)\\s*\\{\\s*\\}");

        try (Stream<Path> walk = Files.walk(root)) {
            List<Path> javaFiles = productionJavaFiles(walk);

            for (Path file : javaFiles) {
                String content = Files.readString(file);
                Matcher matcher = emptyCatch.matcher(content);
                while (matcher.find()) {
                    count++;
                }
            }
        }

        assertTrue(count <= MAX_EMPTY_CATCH_BLOCKS,
                "Empty catch block count (" + count + ") exceeds bound ("
                        + MAX_EMPTY_CATCH_BLOCKS + "). Add logging or comments.");
    }

    @Test
    @DisplayName("freemind.properties is valid and loadable")
    void propertiesFileLoadable() throws IOException {
        Path propsFile = findProjectRoot()
                .resolve("src/main/resources/freemind.properties");
        assertTrue(Files.exists(propsFile),
                "freemind.properties must exist");

        Properties props = new Properties();
        try (InputStream is = Files.newInputStream(propsFile)) {
            props.load(is);
        }

        assertFalse(props.isEmpty(),
                "freemind.properties must not be empty");
        // Verify some known keys exist
        assertNotNull(props.getProperty("el__max_default_window_height"),
                "Expected property 'el__max_default_window_height' not found");
    }

    @Test
    @DisplayName("Resources_en.properties is loadable")
    void resourceBundleLoadable() throws IOException {
        Path resourceFile = findProjectRoot()
                .resolve("src/main/resources/Resources_en.properties");
        assertTrue(Files.exists(resourceFile),
                "Resources_en.properties must exist");

        Properties props = new Properties();
        try (InputStream is = Files.newInputStream(resourceFile)) {
            props.load(is);
        }

        assertFalse(props.isEmpty(),
                "Resources_en.properties must not be empty");
    }

    @Test
    @DisplayName("freemind_actions.xsd is well-formed XML")
    void xsdSchemaWellFormed() throws Exception {
        Path xsdFile = findProjectRoot()
                .resolve("src/main/schema/freemind_actions.xsd");
        assertTrue(Files.exists(xsdFile),
                "freemind_actions.xsd must exist");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        // This will throw if the XML is malformed
        assertDoesNotThrow(() -> builder.parse(xsdFile.toFile()),
                "freemind_actions.xsd must be well-formed XML");
    }

    @Test
    @DisplayName("TODO/FIXME count is bounded")
    void todoFixmeCountBounded() throws IOException {
        Path root = findProjectRoot();
        int count = 0;

        Pattern todoFixme = Pattern.compile("(?i)\\b(TODO|FIXME)\\b");

        try (Stream<Path> walk = Files.walk(root)) {
            List<Path> javaFiles = walk
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> !p.toString().replace('\\', '/').contains("/build/"))
                    .filter(p -> !p.toString().contains("generated-src"))
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());

            for (Path file : javaFiles) {
                String content = Files.readString(file);
                Matcher matcher = todoFixme.matcher(content);
                while (matcher.find()) {
                    count++;
                }
            }
        }

        assertTrue(count <= MAX_TODO_FIXME_COUNT,
                "TODO/FIXME count (" + count + ") exceeds bound ("
                        + MAX_TODO_FIXME_COUNT + "). Address or triage them.");
    }

    @Test
    @DisplayName("Built-in hooks name classes that exist")
    void builtInHookClassesExist() throws Exception {
        Path root = findProjectRoot();
        Path javaRoot = root.resolve("src/main/java");
        List<String> violations = new ArrayList<>();
        assertFalse(BuiltInHooks.hooks().isEmpty(), "The built-in hooks must be registered");
        for (BuiltInHooks.Hook hook : BuiltInHooks.hooks()) {
            noteMissingClass(javaRoot, hook.type.getName(), violations);
        }
        for (BuiltInHooks.Registration registration : BuiltInHooks.registrations()) {
            noteMissingClass(javaRoot, registration.type.getName(), violations);
        }
        try (Stream<Path> walk = Files.walk(root.resolve("src/main/resources/accessories/plugins"))) {
            long xmlDescriptors = walk.filter(path -> path.toString().endsWith(".xml")).count();
            assertEquals(0, xmlDescriptors, "Plugin XML descriptors are gone");
        }
        assertTrue(violations.isEmpty(),
                "Built-in hooks reference non-existent classes:\n"
                        + String.join("\n", violations));
    }

    private void noteMissingClass(Path javaRoot, String className, List<String> violations) {
        String outerName = className.contains("$")
                ? className.substring(0, className.indexOf('$'))
                : className;
        Path javaFile = javaRoot.resolve(outerName.replace('.', '/') + ".java");
        if (!Files.exists(javaFile)) {
            violations.add(className);
        }
    }

    // --- Utility methods ---

    private List<Path> productionJavaFiles(Stream<Path> walk) {
        return walk
                .filter(p -> p.toString().endsWith(".java"))
                .filter(p -> !p.toString().replace('\\', '/').contains("/build/"))
                .filter(p -> !p.toString().replace('\\', '/').contains("/src/test/"))
                .filter(p -> !p.toString().contains("generated-src"))
                .filter(p -> !p.toString().contains("tests"))
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());
    }

    private Path findProjectRoot() {
        Path current = Paths.get(System.getProperty("user.dir"));
        while (current != null) {
            if (Files.exists(current.resolve("settings.gradle.kts"))) {
                return current;
            }
            current = current.getParent();
        }
        return Paths.get(System.getProperty("user.dir"));
    }
}
