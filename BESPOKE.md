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

- Feature spec: `docs/freemind-ce-spec.md`. Testspec: none yet.
- Upstream spec: none with numbered requirements. `docs/` is an architecture and component inventory, usable as a map of the code and not as a feature spec. `_bmad-output/implementation-artifacts/tech-spec-freemind-ce-next-gen-modernization.md` is a finished infrastructure plan from 2026-03-11. It is not a product feature list, and several of its claims are stale at this commit (it says tests never run, and that LaTeX is absent from the Gradle settings). Verdict: not usable. See RAW-003.

## 1 raw

- Upstream commit: 88075b1ed47f8e58d813775e41836e08d0681f1f
- Built from: upstream
- Skill: fork
- Gate: `./gradlew test --no-configuration-cache`
- Details: none (or bespoke/1-raw.md)

### Inventory

- Languages and build system: Java 21 (Gradle toolchain) and Gradle Kotlin DSL. Wrapper is Gradle 9.4.1. The root project version is 1.3.4. Apache Ant (`freemind/build.xml`) is a legacy reference and is not the build. Modules are `:freemind` plus plugins svg, script, map, search, help, contextgraph, collaboration socket, and latex.
- Build: `./gradlew build --no-configuration-cache`, or `make build` (compile and test). Run with `./gradlew :freemind:run --no-configuration-cache`. The host default JDK on 2026-10-19 was OpenJDK 17. The project toolchain is Java 21, and the foojay resolver in `settings.gradle.kts` supplies it. An offline Gradle run failed before configuration because that plugin was not cached. The online run configured and compiled.
- Tests and result: `./gradlew test --no-configuration-cache` on 2026-10-19. After the offline failure above, the online run was BUILD SUCCESSFUL in 2m 17s. `:freemind:test` ran 824 tests, 0 failures, 0 errors, 0 skipped. Plugin subprojects reported test NO-SOURCE. GUI tests (`./gradlew testGui --no-configuration-cache`) were not run.
- License: GNU GPL version 2. The file `LICENSE` is the GPL-2.0 text. The README copyright notice grants GPL version 2 or, at the user's option, any later version. Constraint: keep this license.
- Direct dependencies: from `freemind/build.gradle.kts` `implementation` (the `docs/project-overview.md` version table is older). JAXB API 2.3.1 and runtime 2.3.9, activation 1.1.1, FlatLaf and FlatLaf extras 3.7.1, JGoodies Forms 1.9.0 and Common 1.8.1, local `lib/jortho.jar`, local SimplyHTML and gnu-regexp jars, Xalan and serializer 2.7.3, xml-apis 2.0.2 (Gradle warns this relocates to 1.0.b2), Xerces 2.12.2, jsoup 1.22.2, Groovy 3.0.25, local `plugins/map/JMapViewer.jar`, Lucene core, analysis-common, and queryparser 10.4.0, local `plugins/help/jhall.jar`, JLaTeXMath 1.0.7, Batik 1.19, FOP 2.11, SLF4J 2.0.20, Logback 1.6.5.
- Shipped artifacts and size: this fork has no GitHub release. The local test build wrote `freemind/build/libs/freemind-ce-1.3.4.jar` at 14,750,017 bytes. That jar is build output, not a published package. Upstream Denomas/freemind-ce release v1.3.3 (2026-04-04) is the newest release that has files. The tree's version is already 1.3.4. Those upstream files, in bytes: Linux AppImage 66,091,512; Linux deb 100,361,122; Linux rpm 124,806,308; macOS dmg 127,341,773; portable tar.gz 66,592,419; Windows portable zip 66,661,858; Windows exe 123,990,016; Windows msi 123,654,794; source tar 70,870,528; source zip 65,562,467. Linux, macOS, and Windows packages are built with `jpackage` (`:freemind:jpackageLinux`, `jpackageMac`, `jpackageWin`). They were not built in this run.

### Decisions

| ID | Decision | Reason | Decided by | Replay hint |
|----|----------|--------|------------|-------------|
| RAW-001 | Fork Denomas/freemind-ce as public pihme/bespoke-freemind-ce, copying only the default branch `main` at 88075b1ed47f8e58d813775e41836e08d0681f1f, and start the raw stage on `1-raw` at that commit. | The user asked for that repo name, that upstream, and only the main branch. | user | `gh repo fork Denomas/freemind-ce --fork-name bespoke-freemind-ce --default-branch-only`, then `1-raw` at the same commit. |
| RAW-002 | The feature spec is `docs/freemind-ce-spec.md`. No testspec yet. | The repo had no numbered-requirements convention to extend. | agent | Keep later stages on this file. |
| RAW-003 | Do not treat `docs/` or the 2026-03-11 tech spec as the feature spec. | Neither is a numbered user-feature list. The tech spec is an infrastructure plan and is stale against this commit. | agent | Re-read both if upstream docs gain requirement IDs. |

### Last run

- 2026-10-19. Inventory and spec written. Gate `./gradlew test --no-configuration-cache` passed: 824 tests, 0 failures. GUI tests not run. The user has the spec file to read; removal choices are not made.

## 2 covered

- Upstream commit: <sha of 1-raw this run started from>
- Built from: 1-raw@<sha>
- Skill: increase-test-coverage
- Gate: <command(s) that must pass at the end of this stage>
- Details: none (or bespoke/2-covered.md)

### Decisions

| ID | Decision | Reason | Decided by | Replay hint |
|----|----------|--------|------------|-------------|
| COV-001 | | | user / agent | |

### Last run

- Date, result (passed / failed at decision ID), notes

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
