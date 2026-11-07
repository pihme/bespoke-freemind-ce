# FreeMind CE - Development Guide

## Prerequisites

| Requirement | Version | Notes |
|---|---|---|
| Java (JDK) | 21+ | OpenJDK/Temurin recommended |
| Gradle | 8.6+ | Wrapper included (`./gradlew`) |
| Git | Any recent | For source control |
| IDE (recommended) | IntelliJ IDEA or Eclipse | Java 21 support required |

## Quick Start

```bash
# Clone repository
git clone https://github.com/pihme/bespoke-freemind-ce.git
cd bespoke-freemind-ce

# Build and run using Make (recommended)
make build    # Compile + test
make run      # Run FreeMind CE
make help     # Show all available targets
```

## Makefile Shortcuts (Recommended)

The project includes a cross-platform `Makefile` that auto-detects `JAVA_HOME` and wraps Gradle with the correct flags. It supports macOS (Homebrew/MacPorts), Linux (apt/dnf/SDKMAN), and Windows (WSL/Git Bash). Use `make help` to see all targets:

| Target | Description |
|---|---|
| **Development** | |
| `make build` | Build the project (compile + test) |
| `make run` | Run FreeMind CE |
| `make debug` | Run in debug mode (attach debugger on port 5005) |
| `make test` | Run tests only |
| `make coverage` | Run tests with JaCoCo coverage report |
| `make coverage-report` | Print JaCoCo totals from exec files already on disk |
| `make check` | Build + all quality checks |
| `make clean` | Clean all build artifacts |
| `make jaxb` | Regenerate JAXB classes from XSD schema |
| `make javadoc` | Generate API documentation |
| **Packaging** | |
| `make package` | Build native package for current OS (auto-detect) |
| `make package-mac` | Build macOS DMG package |
| `make package-win` | Build Windows EXE installer |
| `make package-linux` | Build Linux DEB package |
| `make dist-zip` | Create distribution ZIP archive |
| `make install-dist` | Create local distribution layout |
| **General** | |
| `make info` | Show detected Java and system info |
| `make help` | Show this help message |

To override Java detection: `JAVA_HOME=/path/to/jdk make build`

## Raw Gradle Commands

For cases where you need direct Gradle access (CI, IDE integration, advanced options):

| Command | Purpose |
|---|---|
| `./gradlew build` | Full compilation, testing, packaging |
| `./gradlew run` | Run application with JVM args |
| `./gradlew test` | Run all unit tests |
| `./gradlew generateJaxb` | Generate JAXB classes from XSD |
| `./gradlew jar` | Build main JAR |
| `./gradlew installDist` | Create distribution layout |
| `./gradlew distZip` | Create distribution ZIP |
| `./gradlew jpackageMac` | Create macOS .dmg |
| `./gradlew jpackageWin` | Create Windows .exe |
| `./gradlew jpackageLinux` | Create Linux .deb |
| `./gradlew prepareMacDist` | Prepare macOS key mappings |
| `./gradlew javadoc` | Generate API documentation |

> **Important:** Always use `--no-configuration-cache` flag with raw Gradle commands. The Makefile handles this automatically.

## JVM Arguments (applied automatically via `./gradlew run`)

```
-Xms64m -Xmx512m -Xss8M
-Dapple.laf.useScreenMenuBar=true
--add-opens=java.desktop/java.awt=ALL-UNNAMED
--add-opens=java.desktop/java.awt.event=ALL-UNNAMED
--add-opens=java.desktop/javax.swing=ALL-UNNAMED
--add-opens=java.desktop/javax.swing.text=ALL-UNNAMED
--add-opens=java.base/java.lang=ALL-UNNAMED
--add-opens=java.base/java.util=ALL-UNNAMED
```

## Project layout

The repository root is the Gradle project. `rootProject.name` is `bespoke-freemind-ce`.

```
src/main/java/         → production sources; the directory matches the package
src/main/resources/    → classpath resources, including images/ and doc/
src/main/schema/       → freemind.xsd, freemind_actions.xsd, jaxb-bindings.xjb
src/test/java/         → tests; the directory matches the package
src/test/resources/    → test resources
lib/                   → local jars the build still uses
plugins/               → jhall.jar and JMapViewer.jar until the trimmed stage
```

JAXB classes are generated into `build/generated/sources/jaxb/java` when the main sources compile. They are not committed. `make jaxb` runs the same generator. `src/main/schema/jaxb-jibx-compat.patch` puts back the JiBX method names the program already calls. Do not hand-edit the generated classes.

## Configuration Files

| File | Purpose | Location |
|---|---|---|
| `freemind.properties` | Default application settings | `src/main/resources/` |
| `auto.properties` | User overrides | `~/.freemind/` |
| `patterns.xml` | Node styling patterns | `src/main/resources/` |
| `mindmap_menus.xml` | Menu structure definition | `src/main/resources/` |
| `freemind.xsd` | Mind map file format schema | `src/main/schema/` |
| `freemind_actions.xsd` | Action (undo/redo) schema | `src/main/schema/` |
| `Resources_*.properties` | Language resource bundles | `src/main/resources/` |

## Testing

### Running Tests
```bash
# Using Make (recommended)
make test           # Unit tests only
make test-gui       # GUI tests only
make coverage       # Tests + JaCoCo coverage report
make coverage-report # Totals from exec files already on disk

# Using Gradle directly
./gradlew test --no-configuration-cache                    # Unit tests
./gradlew testGui --no-configuration-cache                 # GUI tests
./gradlew test --no-configuration-cache --info              # Verbose output
```

### Test Framework
- **JUnit 4** test classes (`extends TestCase`) running on **JUnit 5 vintage engine**
- **GUI tests:** AssertJ Swing with `GuiTestBase` base class, `@Tag("gui")`, automatic screenshots
- **Property and fuzz:** jqwik and Jazzer, tag `fuzz`, run with `make test-fuzz`. The unit task and the JaCoCo report leave them out.
- **Test base:** `FreeMindTestBase` provides headless FreeMind context via `HeadlessFreeMind`

### Test Structure

| Directory/File | Purpose |
|---|---|
| `src/test/java/tests/freemind/gui/` | GUI test classes (AssertJ Swing) |
| `src/test/java/tests/freemind/property/` | Property-based tests (jqwik) |
| `src/test/java/tests/freemind/fuzz/` | Fuzz tests |
| `src/test/java/tests/freemind/findreplace/` | Search/replace tests |
| `src/test/java/tests/freemind/unicode/` | Unicode handling tests |
| `FreeMindTestBase.java` | Unit test base class |
| `GuiTestBase.java` | GUI test base class (robot, screenshots) |
| `MarshallerTests.java` | JAXB XML serialization |
| `LayoutTests.java` | UI layout verification |
| `TransformTest.java` | XSLT transformation |
| `ExportTests.java` | Export functionality |
| `HtmlConversionTests.java` | HTML export |
| `StandaloneMapTests.java` | Standalone map operations |

## Security Audit

Dependency vulnerability scanning catches known CVEs in project dependencies before they reach production.

### Running Locally

```bash
# Quick scan — Grype (requires: mise install grype)
make audit            # ~30 seconds, fails on High+ with known fix

# Full report — OWASP Dependency-Check (Gradle plugin)
make audit-full       # ~5 minutes, HTML report
open build/reports/dependency-check-report.html
```

### What Gets Scanned

| Source | Examples |
|--------|---------|
| Gradle dependencies | jsoup, Batik, FOP, Lucene, Logback, JUnit |
| Tracked JARs (`lib/`) | jortho.jar, SimplyHTML.jar, bindings.jar |
| Plugin JARs (`plugins/*/`) | JMapViewer.jar, jhall.jar |
| Build tools (non-runtime) | PMD, SpotBugs, JaCoCo |

### Core Principle

> No external API dependency for security tooling. If a tool runs as a local CLI, we use the CLI — not cloud dashboards or paid services. All scanning runs offline after initial database download.

### GitHub Safety Net

Weekly `security-scan.yml` runs Grype + Trivy + OWASP on GitHub Actions with SARIF upload to the Security tab. This catches anything missed locally and provides audit trail.

Full documentation: **[CONTRIBUTING.md — Security Audit](../CONTRIBUTING.md#security-audit-vulnerability-scanning)**

## Static Analysis

Static analysis tools (PMD + SpotBugs), configuration, reports, and the early detection chain are documented in **one place**:

> **[CONTRIBUTING.md — Static Analysis](../CONTRIBUTING.md#static-analysis-quality-gates)** — Tools, config files, report locations, viewing commands, early detection chain.

## Git Remotes

| Remote | URL | Purpose |
|--------|-----|---------|
| `origin` | `https://github.com/pihme/bespoke-freemind-ce.git` | This fork (push/pull) |
| `upstream` | `https://github.com/Denomas/freemind-ce.git` | The project this fork follows |

Stage branches are `1-raw` through `6-bespoke`. `main` is not a stage branch.

## CI/CD Pipeline

Build runs from [`.github/workflows/build.yml`](../.github/workflows/build.yml) on the stage branches `1-raw` through `6-bespoke`. `main` does not run it. The job is Ubuntu 24.04 and Java 21. A documentation-only push skips the build.

## Dependency Management

Dependencies are managed in `build.gradle.kts` with version variables:
- `jaxbImplVersion = "2.3.9"` / `jaxbApiVersion = "2.3.1"`
- `batikVersion = "1.19"`
- `fopVersion = "2.11"`
- `flatlafVersion = "3.7"`
- `jgoodiesFormsVersion = "1.8.0"` / `jgoodiesCommonVersion = "1.8.1"`
- `junitVersion = "4.13.2"`

Repositories: Maven Central + JitPack

## Common Development Tasks

### Modifying menus
Edit `src/main/resources/mindmap_menus.xml`.

### Adding a new action
1. Create the actor class in `src/main/java/freemind/modes/mindmapmode/actions/`
2. Define the XML element in `src/main/schema/freemind_actions.xsd`
3. Register it in `MindMapController`

### Adding translations
Create or edit `Resources_<locale>.properties` in `src/main/resources/`.
