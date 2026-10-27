# BESPOKE.md

This project is a tailored version of an upstream open-source project, built with the [Bespoke assembly line](https://github.com/pihme/bespoke/blob/main/ASSEMBLY-LINE.md). Each stage has its own branch (`1-raw` … `6-bespoke`) and one chapter here. Decisions are recorded here first; a stage that outgrows its chapter moves the details to `bespoke/<n>-<stage>.md` and links it.

## Source

- Upstream repo: https://github.com/Denomas/freemind-ce
- Following: main
- Upstream commit: 88075b1ed47f8e58d813775e41836e08d0681f1f

## Vision / North Star

A modernized and streamlined version of Freemind. Modernized pertains to the UI. Streamlined points at limiting the feature set to the core of mind map modeling.

## Constraints / Do not touch

- Keep the license from upstream.
- Work hard to keep the file format compatible. If that is not possible, add an importer for freemind-ce files.

## Spec

- Feature spec: `docs/spec.md`. Testspec: `docs/testspec.md`.
- Upstream spec: none with numbered requirements. `docs/` is an architecture and component inventory, usable as a map of the code and not as a feature spec. The 2026-03-11 tech spec that used to live at `_bmad-output/implementation-artifacts/tech-spec-freemind-ce-next-gen-modernization.md` was an infrastructure plan, not a product feature list, and several of its claims were stale (it said tests never run, and that LaTeX was absent from the Gradle settings). It is not in this tree. See RAW-003 and RAW-006.

## 1 raw

- Upstream commit: 88075b1ed47f8e58d813775e41836e08d0681f1f
- Built from: upstream
- Skill: fork
- Gate: `./gradlew :freemind:test :freemind:testGui :freemind:testPerformance :freemind:testChaos --no-configuration-cache`
- Details: none (or bespoke/1-raw.md)

### Inventory

- Languages and build system: Java 21 (Gradle toolchain) and Gradle Kotlin DSL. Wrapper is Gradle 9.4.1. The root project version is 1.3.4. Apache Ant (`freemind/build.xml`) is a legacy reference and is not the build. Modules are `:freemind` plus plugins svg, script, map, search, help, contextgraph, collaboration socket, and latex.
- Build: `./gradlew build --no-configuration-cache`, or `make build` (compile and test). Run with `./gradlew :freemind:run --no-configuration-cache`. The host default JDK on 2026-10-19 was OpenJDK 17. The project toolchain is Java 21, and the foojay resolver in `settings.gradle.kts` supplies it. An offline Gradle run failed before configuration because that plugin was not cached. The online run configured and compiled.
- Tests and result: 2026-10-19, Java 21 toolchain, display `:0`. `:freemind:test` (excludes the `gui` tag) ran 824 tests, 0 failures, 0 errors, 0 skipped. On the later pass Gradle treated that task as up to date. `:freemind:testGui` ran 489 tests, 0 failures, 0 errors, 0 skipped. `:freemind:testPerformance` first reported NO-SOURCE because the task was not pointed at the test source set. After that wiring (RAW-004) it ran 9 tests, 0 failures. `:freemind:testChaos` runs and finds 0 tests: nothing in the tree is tagged `chaos`. Plugin subprojects have no tests of their own. JaCoCo instruction coverage for the unit, GUI, and performance exec data together is 21.3% (31,265 of 146,900 instructions). Line coverage is 22.4% (8,363 of 37,282). Branch coverage is 20.3% (2,543 of 12,519). The HTML report is `freemind/build/reports/jacoco/test/html/index.html`. Adding the performance exec did not move those totals. The chaos exec contains no tests.
- License: GNU GPL version 2. The file `LICENSE` is the GPL-2.0 text. The README copyright notice grants GPL version 2 or, at the user's option, any later version. Constraint: keep this license.
- Direct dependencies: from `freemind/build.gradle.kts` `implementation` (the `docs/project-overview.md` version table is older). JAXB API 2.3.1 and runtime 2.3.9, activation 1.1.1, FlatLaf and FlatLaf extras 3.7.1, JGoodies Forms 1.9.0 and Common 1.8.1, local `lib/jortho.jar`, local SimplyHTML and gnu-regexp jars, Xalan and serializer 2.7.3, xml-apis 2.0.2 (Gradle warns this relocates to 1.0.b2), Xerces 2.12.2, jsoup 1.22.2, Groovy 3.0.25, local `plugins/map/JMapViewer.jar`, Lucene core, analysis-common, and queryparser 10.4.0, local `plugins/help/jhall.jar`, JLaTeXMath 1.0.7, Batik 1.19, FOP 2.11, SLF4J 2.0.20, Logback 1.6.5.
- Shipped artifacts and size: this fork has no GitHub release. The local test build wrote `freemind/build/libs/freemind-ce-1.3.4.jar` at 14,750,017 bytes. That jar is build output, not a published package. Upstream Denomas/freemind-ce release v1.3.3 (2026-04-04) is the newest release that has files. The tree's version is already 1.3.4. Those upstream files, in bytes: Linux AppImage 66,091,512; Linux deb 100,361,122; Linux rpm 124,806,308; macOS dmg 127,341,773; portable tar.gz 66,592,419; Windows portable zip 66,661,858; Windows exe 123,990,016; Windows msi 123,654,794; source tar 70,870,528; source zip 65,562,467. Linux, macOS, and Windows packages are built with `jpackage` (`:freemind:jpackageLinux`, `jpackageMac`, `jpackageWin`). They were not built in this run.

### Decisions

| ID | Decision | Reason | Decided by | Replay hint |
|----|----------|--------|------------|-------------|
| RAW-001 | Fork Denomas/freemind-ce as public pihme/bespoke-freemind-ce, copying only the default branch `main` at 88075b1ed47f8e58d813775e41836e08d0681f1f, and start the raw stage on `1-raw` at that commit. | The user asked for that repo name, that upstream, and only the main branch. | user | `gh repo fork Denomas/freemind-ce --fork-name bespoke-freemind-ce --default-branch-only`, then `1-raw` at the same commit. |
| RAW-002 | The feature spec is `docs/spec.md`. No testspec yet. | The repo had no numbered-requirements convention to extend. The user asked for the file name `spec`. | user | Keep later stages on this file. |
| RAW-003 | Do not treat `docs/` or the 2026-03-11 tech spec as the feature spec. | Neither is a numbered user-feature list. The tech spec is an infrastructure plan and is stale against this commit. | agent | Re-read both if upstream docs gain requirement IDs. |
| RAW-004 | Point `testPerformance` and `testChaos` at the test source set, and make `jacocoTestReport` read every `build/jacoco/*.exec`. | Without the source set, Gradle reports NO-SOURCE and the performance tests never run. Without the exec files, the coverage report ignores the GUI and performance runs. | agent | Keep both when replaying the raw stage. |
| RAW-005 | There is no constraint to stay on Java 21, and no constraint to stay in Java. The license and the `.mm` file format stay. | The user dropped the Java constraint. | user | Do not put a Java stay-put rule back into `AGENTS.md` or the constraints list. The toolchain in the build file is only what the tree uses today. |
| RAW-006 | Remove `.claude/`, `.opencode/`, `_bmad-output/`, `.github/CODEOWNERS`, `.github/FUNDING.yml`, `.github/labeler.yml`, and `.github/workflows/labeler.yml`. | The user asked to drop upstream agent tooling, the finished BMAD tech spec, and the GitHub funding, code-owner, and auto-label files. | user | Do not restore these paths. |

### Last run

- 2026-10-19. Gate passed. Unit 824, GUI 489, performance 9, chaos 0 tests present. JaCoCo instruction coverage 21.3%. The user has `docs/spec.md` to read. Removal choices are not made.

## 2 covered

- Upstream commit: 88075b1ed47f8e58d813775e41836e08d0681f1f
- Built from: 1-raw@a9db337edd4fc0d0d82be531c3dd49b0848e08a4
- Skill: increase-test-coverage
- Gate: `./gradlew :freemind:test :freemind:testGui :freemind:testPerformance --no-configuration-cache`
- Details: `docs/testspec.md`

### Decisions

| ID | Decision | Reason | Decided by | Replay hint |
|----|----------|--------|------------|-------------|
| COV-001 | The test spec is `docs/testspec.md`. It maps each requirement to tests whose assertions check that behaviour, and it leaves gaps as none or partial. No test is renamed to `TestREQ_` in this step. | The coverage skill maps the requirements before new tests. A menu-only test is not a check. | agent | Keep this file. Do not mark a requirement checked because a test runs the code. |
| COV-002 | New tests are named `TestREQ_` and assert only the clause they reach. `jacocoTestReport` depends on `test`, `testGui`, `testPerformance`, and `testChaos`. | A combined Gradle invocation otherwise refuses the report, because the extra exec files are an implicit dependency. Gaps that need a controller, a dialog, or a real OS launch stay none or partial. | agent | Keep the dependency. Do not mark a row checked for a clause the test does not assert. |
| COV-003 | Deduced behaviour is a labeled section in `docs/spec.md` (`REQ-DED-nn`, never reused, not renumbered into the original 49). Deduced missing tests are labeled in `docs/testspec.md`. Code that no requirement names is the umbrella "Backwards compatibility tests". Underline stays unresolved. Time-management calendars are not a requirement. | The user asked for deduced requirements, deduced missing tests, and that umbrella, each labeled. These rows do not decide which features stay. | user | Keep the labels. Do not turn a backwards-compatibility test into a product requirement. Do not weaken a requirement test to match the code. |
| COV-004 | JaCoCo leaves out `freemind/generated-src` (`freemind.controller.actions.generated.instance`). The authoritative count is instruction coverage on everything else. | Those classes are JAXB output. The coverage skill excludes generated sources before measuring. | agent | Keep the agent exclude and the report class-directory exclude together. |
| COV-005 | Remove `testChaos`. jqwik and Jazzer tests are tag `fuzz` and run only as `testFuzz`. The unit task and JaCoCo leave them out. | `testChaos` found no tests. Property and fuzz runs are a search, not part of the coverage suite. | user | Do not restore `testChaos`. Do not put the `fuzz` tag back into `:freemind:test` or into the JaCoCo exec files. |
| COV-006 | Instruction coverage on the unit, GUI, and performance exec files is the bar. The CI check fails under 85%. The uncoverable methods are the table in `docs/testspec.md`. | The coverage skill records the count when it meets 85% and gates CI just below the measured number. | agent | Keep the 85% instruction limit on those exec files. Do not count `testFuzz` or generated JAXB classes. |
| COV-007 | Mutation is PIT 1.30.1 (`info.solidsoft.pitest` 1.19.0, JUnit 5 plugin 1.2.3). `make mutate` is the local run. The manual workflow is `.github/workflows/mutation.yml`. Fuzz stays jqwik 1.10.1 and Jazzer 0.30.0. | The repo already runs tests with Gradle and JUnit, and already fuzzes with jqwik and Jazzer. A scoped PIT run on the filter condition package completed and reported condition mutants. | agent | Do not add `pitest` to `check` or to the pull-request workflow. Do not mutate generated JAXB classes. Do not run fuzz, GUI, or performance tests inside the mutation task. |

### Last run

- 2026-10-19. Test spec written. 49 requirements: 8 checked, 21 partial, 20 none. Instruction coverage 21.3% (31,265 of 146,900) was measured at `a9db337` and is recorded in the test spec. This stage has not exited: tests are not named after requirement IDs, the coverage number is not a CI threshold, and no mutation tool is chosen.
- 2026-10-19. 14 `TestREQ_` methods added. 49 requirements: 14 checked, 28 partial, 7 none. Gate passed: unit 838, GUI 489, performance 9, chaos 0. Instruction coverage 21.8% (32,021 of 146,900). The covered stage has not exited: seven requirements still have no behavioural test, the coverage number is not a CI threshold, and no mutation tool is chosen. Underline is not decided.
- 2026-10-26. Instruction coverage 85.03% (122,236 of 143,756) at `81a9d2e`. Unit 1,678, GUI 524, performance 9, all passed. The uncoverable inventory is the table in `docs/testspec.md`. The CI check fails under 85% instruction coverage.
- 2026-10-27. REQ-DED-09 is checked. The undo list keeps 99 and 100, drops the oldest at 101, stays empty at 0, throws `IndexOutOfBoundsException` at -1, keeps 100 when the property is `2147483648`, and a folded pair does not apply the limit.
- 2026-10-27. The covered stage exits at this commit. The recorded instruction count stays 85.03% (122,236 of 143,756) at `81a9d2e`. The CI check fails under 85%. The scoped PIT run left five equivalent survivors, listed in `docs/testspec.md`. `.github/workflows/mutation.yml` is manual and has not been dispatched. The gate the next stage reuses is `./gradlew :freemind:test :freemind:testGui :freemind:testPerformance --no-configuration-cache`.

## 3 tailored

- Upstream commit: 88075b1ed47f8e58d813775e41836e08d0681f1f
- Built from: 2-covered@b04d5d1127c639bf60416e1da815fd7c2d575c74
- Skill: remove-features
- Gate: `./gradlew :freemind:test :freemind:testGui :freemind:testPerformance --no-configuration-cache`
- Details: none

### Decisions

| ID | Decision | Reason | Decided by | Replay hint |
|----|----------|--------|------------|-------------|
| TAI-001 | Remove Groovy scripting (REQ-PLUG-01). Proof: `RemovedFeatureProofTest.TestREQ_PLUG_01_GroovyScriptingIsGone`. | The user said everything under Plugins and modes goes. | user | Delete `plugins/ScriptingEngine.xml` and `plugins/script`, and drop `:freemind:plugins:script` from `settings.gradle.kts`. Leave the Groovy dependency for the trimmed stage. A stored script hook is skipped and the map still opens. MindMap mode stays. The pattern dialog can still store a script string. |
| TAI-002 | Remove the geographic map (REQ-PLUG-02). Proof: `RemovedFeatureProofTest.TestREQ_PLUG_02_GeographicMapIsGone`. | The user said everything under Plugins and modes goes. | user | Delete `plugins/MapViewer.xml` and the Java sources under `plugins/map`. Leave `plugins/map/JMapViewer.jar` and its `implementation(files)` line. Browse mode still builds `MapNodePositionHolderBase` until that mode goes. A `.mm` file still opens. |
| TAI-003 | Remove Lucene search of map files (REQ-PLUG-03). Proof: `RemovedFeatureProofTest.TestREQ_PLUG_03_LuceneSearchIsGone`. | The user said everything under Plugins and modes goes. | user | Delete `plugins/Search.xml` and `plugins/search`. Leave the Lucene dependencies for the trimmed stage. In-map find stays. |
| TAI-004 | Remove integrated help (REQ-PLUG-04, REQ-DED-07). Proof: `RemovedFeatureProofTest.TestREQ_PLUG_04_IntegratedHelpIsGone`. | The user said everything under Plugins and modes goes. | user | Delete `plugins/FreemindHelp.xml`, `plugins/help/FreemindHelpStarter.java`, `plugins/help/doc`, and the help Gradle module. Leave `plugins/help/jhall.jar` and its `implementation(files)` line for the trimmed stage. |
| TAI-005 | Remove LaTeX on a node (REQ-PLUG-05, REQ-DED-05). Proof: `RemovedFeatureProofTest.TestREQ_PLUG_05_LatexOnANodeIsGone`. | The user said LaTeX goes, and it is under Plugins and modes. | user | Delete `plugins/Latex.xml` and `plugins/latex`, and drop `:freemind:plugins:latex` from `settings.gradle.kts`. Leave JLaTeXMath for the trimmed stage. XSLT LaTeX export stays. A stored equation hook is skipped and the map still opens. |
| TAI-006 | Remove socket collaboration (REQ-PLUG-06). Proof: `RemovedFeatureProofTest.TestREQ_PLUG_06_SocketCollaborationIsGone`. | The user said everything under Plugins and modes goes. | user | Delete `plugins/CollaborationSocket.xml` and `plugins/collaboration/socket`, and drop `:freemind:plugins:collaboration:socket` from `settings.gradle.kts`. The single-instance port file stays. Database and Jabber sources stay until TAI-008. A `.mm` file still opens. |
| TAI-007 | Remove spell checking (REQ-PLUG-07, REQ-DED-06). Proof: `RemovedFeatureProofTest.TestREQ_PLUG_07_SpellCheckingIsGone`. | The user said everything under Plugins and modes goes. | user | Delete the spell-check registration, the editor and note menus, the preferences control, the English option strings, `check_spelling` in `freemind.properties`, and the dictionary files. Leave `lib/jortho.jar` and its Gradle and Ant lines. Node editing and notes stay. |
| TAI-008 | Remove the leftover database, Jabber, and Palm sources (REQ-PLUG-08). Proof: `RemovedFeatureProofTest.TestREQ_PLUG_08_LegacyCollaborationSourcesAreGone`. | The user said everything under Plugins and modes goes. | user | Delete `plugins/CollaborationDatabase.xml`, `plugins/CollaborationJabber.xml`, `plugins/collaboration`, and `pda`. The single-instance port file stays. `CollaborationGuiTest` stays. |
| TAI-009 | Remove Browse mode and File mode. MindMap mode stays. Proof: `RemovedFeatureProofTest.TestREQ_MODE_01_BrowseAndFileModesAreGone`. | The user said the modes go, and then confirmed MindMap mode stays. | user | Drop Browse and File from `modes_since_0_8_0`. Delete `freemind.modes.browsemode`, `freemind.modes.filemode`, and `MapNodePositionHolderBase`. The documentation command opens its map in MindMap mode. REQ-MODE-01 keeps the MindMap clause. |
| TAI-010 | Remove the command that encrypts a node (REQ-SEC-01). An existing encrypted node in a `.mm` file still loads. Proof: `RemovedFeatureProofTest.TestREQ_SEC_01_EncryptCommandIsGone`. | The user said encryption goes. The file-format constraint keeps the load path. | user | Delete `accessories/plugins/EncryptNode.java`, `EncryptNode.xml`, and the empty Insert crypto category. Keep `EncryptedMindMapNode`, `EnterPasswordDialog`, and the English dialog strings that dialog reads. A saved encrypted node still opens. |
| TAI-011 | Remove SVG export and PDF export (REQ-FILE-02). Proof: `RemovedFeatureProofTest.TestREQ_FILE_02_SvgAndPdfExportAreGone`. | The user said PDF export and SVG export go. | user | Delete `plugins/ExportSvg.xml` and `plugins/svg`, and drop `:freemind:plugins:svg` from `settings.gradle.kts`. PDF export is registered in `ExportSvg.xml`; there is no `ExportPdf.xml`. Leave Batik and FOP for the trimmed stage. PNG export stays. |
| TAI-012 | Remove HTML export (REQ-FILE-01). Proof: `RemovedFeatureProofTest.TestREQ_FILE_01_HtmlExportIsGone`. | The user said exporting to HTML goes. | user | Delete the File → Export HTML actions, the HTML actions in `ExportWithXSLT.xml`, and the HTML stylesheets `mm2html.xsl`, `toxhtml.xsl`, `tohtml.xsl`, and `freemind2html*.xsl`. Rich text stored on a node stays. Copy still uses `MindMapHTMLWriter`. Other XSLT exports stay until TAI-013. |
| TAI-013 | Remove the XSLT exports (REQ-FILE-07). Proof: `RemovedFeatureProofTest.TestREQ_FILE_07_XsltExportsAreGone`. | The user said XSLT exports go. | user | Delete `ExportWithXSLT`, `ExportToOoWriter`, the XSLT export dialog (`ExportDialog`, `XmlExporter`, `WindowClosingAdapter`), and the `mm2*` export stylesheets. Keep `mindmanager2mm.xsl` for Mindmanager import. Leave Xalan for the trimmed stage. PNG export stays. |
| TAI-014 | Remove folder-structure import. Branch import, linked-branch import, and Explorer-favorites import stay. | The user said importing a folder goes. | user | Delete `ImportFolderStructureAction`. Keep the rest of REQ-FILE-06. |
| TAI-015 | Remove every language bundle except English (REQ-APP-03). | The user said languages besides English go. | user | Delete `Resources_*.properties` other than `Resources_en.properties` and the default bundle. |
| TAI-016 | Remove context-graph export (REQ-FILE-04), including its Markdown, XML, JSON, and YAML writers. Proof: `RemovedFeatureProofTest.TestREQ_FILE_04_ContextGraphExportIsGone`. | The user confirmed this plugin goes. It is a plugin module listed under Files. | user | Delete `plugins/ContextGraph.xml` and `plugins/contextgraph`, and drop `:freemind:plugins:contextgraph` from `settings.gradle.kts`. A `.mm` file still opens. |

### Last run

- 2026-10-27. Branch opened from the exited covered stage. The gate passed on the unchanged tree: unit 1,749, GUI 524, performance 9, all passed.
- 2026-10-27. The user named the removals above. MindMap mode stays. PNG export, branch export, and the imports other than a folder stay. The license and `.mm` compatibility stay.
- 2026-10-27. TAI-016 removed context-graph export. The proof test finds neither the class nor the plugin file. Dropped tests: `ContextGraphExportTest`, `tests.freemind.cov.contextgraph`, and the context-graph methods in `BackwardsCompatibilityTest` and `MultiLanguageFileTest`. The gate passed: unit 1,684, GUI 524, performance 9, all passed.
- 2026-10-27. TAI-003 removed Lucene search of map files. In-map find stays. The proof test finds neither the class nor `plugins/Search.xml`. Dropped tests: `TestREQ_PLUG_03_LuceneFindsIndexedMap`, `SearchViewPanelBehaviourTest`, `tests.freemind.cov.search`, `PluginLogicTest.SearchPluginTests`, and `BackwardsCompatibilityTest.searchingAMindMapFileFindsTheNodeText`. The gate passed: unit 1,672, GUI 523, performance 9, all passed.
- 2026-10-27. TAI-004 removed integrated help. The proof test finds neither the class nor the help set. `jhall.jar` stays. Dropped test: `DeducedBehaviourTest.TestREQ_PLUG_04_HelpSetLoads`. `HelpMenuGuiTest` stays. The gate passed: unit 1,672, GUI 523, performance 9, all passed.
- 2026-10-27. TAI-002 removed the geographic map. The proof test finds neither the class nor `plugins/MapViewer.xml`. `JMapViewer.jar` stays. Browse mode still builds `MapNodePositionHolderBase`. Dropped tests: `tests.freemind.cov.map`, `StartupBehaviourTest.TestREQ_PLUG_02_MapViewBuildsMenusAndReadsACoordinate`, the Nominatim methods in `MarshallerTests`, `BackwardsCompatibilityTest.mapPositionRoundTripsAndBuildsATileTooltip`, `BackwardsCompatibilityTest.tileImageRoundTripsABlueSquare`, and `ControllerNullGuardTest.freeMindMapControllerReverseLookupNullCheck`. The gate passed on the rerun: unit 1,637, GUI 522, performance 9, all passed. The first run failed `RootMainViewBehaviourTest` (`expected 24 but was 60`). That assertion was not changed.
- 2026-10-27. TAI-001 removed Groovy scripting. The proof test finds neither the class nor `plugins/ScriptingEngine.xml`. The Groovy dependency stays. The pattern dialog can still store a script string. Dropped tests: `tests.freemind.cov.script`, `ScriptEditorPanelTest`, `SignedScriptTests`, `PluginLogicTest.ScriptingEngineTests`, `PluginLogicTest.SignedScriptHandlerTests`, `RequirementBehaviourTest.TestREQ_PLUG_01_GroovyScriptReturnsValue`, `StartupBehaviourTest.TestREQ_PLUG_01_ScriptEditorShowsTheScriptText`, and `Utf8ConsistencyTest.signedScriptHandlerExplicitCharset`. The gate passed: unit 1,596, GUI 521, performance 9, all passed.
- 2026-10-27. TAI-006 removed socket collaboration. The proof test finds neither the class nor `plugins/CollaborationSocket.xml`. The single-instance port file stays. `CollaborationGuiTest` stays. Dropped tests: `tests.freemind.cov.socket`, `CollaborationTests`, `CollaborationStructuralTest`, and `ThreadSafetyTest.communicationBaseMutexIsObject`. The gate passed: unit 1,524, GUI 521, performance 9, all passed.
- 2026-10-27. TAI-005 removed LaTeX on a node. The proof test finds neither the class nor `plugins/Latex.xml`. JLaTeXMath stays. XSLT LaTeX export stays. Dropped tests: `tests.freemind.cov.latex` and `DeducedBehaviourTest.TestREQ_PLUG_05_LatexHookStoresEquation`. The gate passed on the rerun: unit 1,517, GUI 521, performance 9, all passed. The first run failed `BackwardsCompatibilityTest.selectingABranchKeepsTheChildSelected` (`centerNode` saw a null viewport size). That test was not changed.
- 2026-10-27. TAI-011 removed SVG export and PDF export. The proof test finds neither class nor `plugins/ExportSvg.xml`. Batik and FOP stay. PNG export stays. `ExportGuiTest` methods that only assert `.mm` XML stay. Dropped tests: `tests.plugins.svg` and `StartupBehaviourTest.TestREQ_FILE_02_PdfDialogKeepsPaperAndOrientation`. The gate passed: unit 1,508, GUI 520, performance 9, all passed.
- 2026-10-27. TAI-008 removed database collaboration, Jabber, and the Palm OS tree. The proof test finds neither class nor `plugins/collaboration` nor `pda`. The single-instance port file stays. `CollaborationGuiTest` stays. Dropped tests: `RequirementBehaviourTest.TestREQ_PLUG_08_BuildOmitsLegacyModules` and `HookFrameworkTest.pluginXmlDescriptorsInPluginsDirAreWellFormedXml`. The accessories plugin XML check stays. The gate passed on the rerun: unit 1,507, GUI 520, performance 9, all passed. The first run also failed `BackwardsCompatibilityTest.selectingABranchKeepsTheChildSelected` (`centerNode` saw a null viewport size). That test was not changed.
- 2026-10-27. TAI-007 removed spell checking. The proof test finds no `setupSpellChecking` and no `dictionaries.cnf`. `lib/jortho.jar` stays. Node editing and notes stay. Dropped test: `DeducedBehaviourTest.TestREQ_PLUG_07_EnglishDictionaryLoads`. `FreeMindBehaviourTest.lookAndFeelBranches` keeps the look-and-feel checks. The gate passed: unit 1,507, GUI 520, performance 9, all passed.
- 2026-10-27. TAI-009 removed Browse mode and File mode. MindMap mode stays. The proof test finds neither class. `MapNodePositionHolderBase` goes with Browse mode. The documentation command opens its map in MindMap mode. Dropped tests: `tests.freemind.cov.browsemode`, `StartupBehaviourTest.TestREQ_MODE_01_FileModeRootNamesTheFilesystemRoot`, `StartupBehaviourTest.TestREQ_MODE_01_BrowseModeStarts`, and `BackwardsCompatibilityTest.fileModeListsADirectory`. The gate passed on the rerun: unit 1,487, GUI 518, performance 9, all passed. The first run failed `BackwardsCompatibilityTest.selectingABranchKeepsTheChildSelected` (`centerNode` saw a null viewport size). That test was not changed.
- 2026-10-27. TAI-010 removed the command that encrypts a node. A saved encrypted node still opens. The proof test finds neither the class nor `EncryptNode.xml`, and `EncryptedMindMapNode` still loads. `EnterPasswordDialog` stays. Dropped tests: `EncryptNodeBehaviourTest` and `PluginLogicTest.EncryptNodeTests`. The gate passed: unit 1,480, GUI 518, performance 9, all passed.
- 2026-10-27. TAI-012 removed HTML export. Rich text on a node stays. Copy still uses `MindMapHTMLWriter`. The proof test finds no HTML export menu actions and no `mm2html.xsl`. Other XSLT exports stay. Dropped tests: the HTML methods in `XsltExportTest`, `ContentPreservationExportTest`, and `TransformTest`, `BackwardsCompatibilityTest.htmlExportWritesTheNodeText`, `BackwardsCompatibilityTest.htmlExportWithFoldingWritesTheScriptAndAHeading`, `MindMapControllerBehaviourTest.htmlExportWritesTheMapAndTheSelectedBranch`, and `ExportWithXSLTBehaviourTest.htmlExportWritesTheMapTheIconsAndTheLinkedFiles`. `ExportGuiTest` methods that only assert `.mm` XML stay. The gate passed: unit 1,461, GUI 518, performance 9, all passed.
- 2026-10-27. TAI-013 removed the XSLT exports. Mindmanager import stays. Xalan stays. PNG export stays. The proof test finds neither `ExportWithXSLT` nor `ExportToOoWriter` nor the XSLT export dialog, and `mindmanager2mm.xsl` is still present. The HTML proof now checks that `ExportWithXSLT.xml` is absent. Dropped tests: `XsltExportTest`, `ContentPreservationExportTest`, `TransformTest`, `ExportWithXSLTBehaviourTest`, `ExportToOoWriterBehaviourTest`, `ExportDialogBehaviourTest`, and `XmlExporterBehaviourTest`. `ExportGuiTest` methods that only assert `.mm` XML stay. The gate passed: unit 1,423, GUI 518, performance 9, all passed.

## 4 customized

- Upstream commit: <sha of 1-raw this run started from>
- Built from: 3-tailored@<sha>
- Skill: customize-features
- Gate: <command(s) that must pass at the end of this stage>
- Details: none (or bespoke/4-customized.md)

### Decisions

| ID | Decision | Reason | Decided by | Replay hint |
|----|----------|--------|------------|-------------|
| CUS-001 | | | user / agent | |

### Last run

- Date, result (passed / failed at decision ID), notes

## 5 trimmed

- Upstream commit: <sha of 1-raw this run started from>
- Built from: 4-customized@<sha>
- Skill: trim
- Gate: <command(s) that must pass at the end of this stage>
- Details: none (or bespoke/5-trimmed.md)

### Decisions

| ID | Decision | Reason | Decided by | Replay hint |
|----|----------|--------|------------|-------------|
| TRM-001 | | | user / agent | |

### Last run

- Date, result (passed / failed at decision ID), notes

## 6 bespoke

- Upstream commit: <sha of 1-raw this run started from>
- Built from: 5-trimmed@<sha>
- Skill: shrink-wrap
- Gate: <command(s) that must pass at the end of this stage>
- Details: none (or bespoke/6-bespoke.md)

### Decisions

| ID | Decision | Reason | Decided by | Replay hint |
|----|----------|--------|------------|-------------|
| BSP-001 | | | user / agent | |

### Last run

- Date, result (passed / failed at decision ID), notes
