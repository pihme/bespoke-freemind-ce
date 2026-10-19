# BESPOKE.md

This project is a tailored version of an upstream open-source project, built with the [Bespoke assembly line](https://github.com/pihme/bespoke/blob/main/ASSEMBLY-LINE.md). Each stage has its own branch (`1-raw` … `6-bespoke`) and one chapter here. Decisions are recorded here first; a stage that outgrows its chapter moves the details to `bespoke/<n>-<stage>.md` and links it.

## Source

- Upstream repo: https://github.com/Denomas/freemind-ce
- Following: main
- Upstream commit: 88075b1ed47f8e58d813775e41836e08d0681f1f

## Vision / North Star

A modernized and streamlined version of Freemind. Modernized pertains to the UI. Streamlined points at limiting the feature set to the core of mind map modeling.

## Constraints / Do not touch

- Stay in Java.
- Keep the license from upstream.
- Work hard to keep the file format compatible. If that is not possible, add an importer for freemind-ce files.

## Spec

- Feature spec: `docs/spec.md`. Testspec: `docs/testspec.md`.
- Upstream spec: none with numbered requirements. `docs/` is an architecture and component inventory, usable as a map of the code and not as a feature spec. `_bmad-output/implementation-artifacts/tech-spec-freemind-ce-next-gen-modernization.md` is a finished infrastructure plan from 2026-03-11. It is not a product feature list, and several of its claims are stale at this commit (it says tests never run, and that LaTeX is absent from the Gradle settings). Verdict: not usable. See RAW-003.

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

### Last run

- 2026-10-19. Gate passed. Unit 824, GUI 489, performance 9, chaos 0 tests present. JaCoCo instruction coverage 21.3%. The user has `docs/spec.md` to read. Removal choices are not made.

## 2 covered

- Upstream commit: 88075b1ed47f8e58d813775e41836e08d0681f1f
- Built from: 1-raw@a9db337edd4fc0d0d82be531c3dd49b0848e08a4
- Skill: increase-test-coverage
- Gate: `./gradlew :freemind:test :freemind:testGui :freemind:testPerformance :freemind:testChaos --no-configuration-cache`
- Details: `docs/testspec.md`

### Decisions

| ID | Decision | Reason | Decided by | Replay hint |
|----|----------|--------|------------|-------------|
| COV-001 | The test spec is `docs/testspec.md`. It maps each requirement to tests whose assertions check that behaviour, and it leaves gaps as none or partial. No test is renamed to `TestREQ_` in this step. | The coverage skill maps the requirements before new tests. A menu-only test is not a check. | agent | Keep this file. Do not mark a requirement checked because a test runs the code. |

### Last run

- 2026-10-19. Test spec written. 49 requirements: 8 checked, 21 partial, 20 none. Instruction coverage 21.3% (31,265 of 146,900) was measured at `a9db337` and is recorded in the test spec. This stage has not exited: tests are not named after requirement IDs, the coverage number is not a CI threshold, and no mutation tool is chosen.

## 3 tailored

- Upstream commit: <sha of 1-raw this run started from>
- Built from: 2-covered@<sha>
- Skill: remove-features
- Gate: <command(s) that must pass at the end of this stage>
- Details: none (or bespoke/3-tailored.md)

### Decisions

| ID | Decision | Reason | Decided by | Replay hint |
|----|----------|--------|------------|-------------|
| TAI-001 | | | user / agent | |

### Last run

- Date, result (passed / failed at decision ID), notes

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
