# BESPOKE summary

> Readable summary of [BESPOKE.md](BESPOKE.md), which stays the authoritative protocol. Future stages update both files.

## What this project is

- A tailored fork of [Denomas/freemind-ce](https://github.com/Denomas/freemind-ce), following `main` at upstream commit `88075b1e`.
- Vision: a modernized and streamlined FreeMind. "Modernized" means the UI; "streamlined" means a feature set limited to the core of mind-map editing.
- Constraints: keep the upstream license (GPL-2.0, "or any later version" per the README notice), and keep the `.mm` file format compatible (or add an importer if that ever breaks).
- Released line: `6-bespoke` (the default branch). `main` only carries team rules and none of the stage work.

## Current state at a glance

Figures below are the last ones recorded in BESPOKE.md. They were not re-measured for this summary.

- Coverage: 86.93% instruction coverage (99,136 of 114,035), recorded at the 4-customized exit. The covered stage set an 85% bar.
- Tests: unit 1,423, GUI 511, performance 9, all passing at the 5-trimmed exit. 6-bespoke later removed the jqwik property tests (BSP-002) and recorded no new count.
- Build: Gradle with Java 21 toolchain. `flake.nix` provides the reference shell.
- CI (`.github/workflows/build.yml`): runs `assemble`, `test`, `testGui` inside `nix develop`, plus a SonarQube Cloud scan when a token is set. A separate workflow builds a macOS DMG.
- Run: `./gradlew run --no-configuration-cache` (or `make run`).
- Stage gate: `./gradlew test testGui testPerformance --no-configuration-cache`.
- Entry point: `freemind.main.FreeMindStarter`.

## Stages

**1-raw** (RAW-001 to RAW-006). The fork starts from upstream `88075b1e` and adds `docs/spec.md` as the feature list. Upstream agent tooling (Claude, OpenCode, Serena), the BMAD tech spec, and the GitHub funding, code-owner, and labeler files were removed. The performance tests now actually run, and coverage counts all test runs; starting coverage was 21.3%.

**2-covered** (COV-001 to COV-007). `docs/testspec.md` maps every requirement to the tests that check it, and deduced requirements were added to the spec. New requirement tests and per-package tests raised instruction coverage from 21.3% to 85.03%, with generated JAXB classes excluded. PIT mutation testing was added, and the jqwik and Jazzer fuzz tests run separately from the coverage suite.

**3-tailored** (TAI-001 to TAI-020). Twenty features were removed: scripting, map-file search, the geographic map, integrated help and the Help menu, LaTeX, collaboration, spell checking, Browse and File modes, the encrypt command, HTML, XSLT, SVG, PDF, context-graph, and wiki exports, folder import, printing, non-English languages, and the startup splash. Tests show that each removed feature is gone, and that existing `.mm` files (including encrypted nodes) still open. MindMap mode, PNG export, and the other imports stay.

**4-customized** (CUS-001 to CUS-031). The codebase was reorganized: the Gradle project moved to the repository root with the standard source layout, JAXB classes are generated at build time, and stale docs, launchers, local jars, and leftover setup files were removed. The plugin loader was replaced by ordinary code, and several concerns (settings, stored text, styles, node placement, menus, undo) each got one owning module. User-visible changes: Tab adds a child, font size follows tree depth, and startup reopens the last map.

**5-trimmed** (TRM-001 to TRM-005). Unused runtime libraries were removed: Groovy, Lucene, JLaTeXMath, Batik, FOP, SLF4J/Logback, xml-apis, Xerces, and two redundant direct dependencies. Xalan stays because upgrading old maps needs it. jsoup moved to 1.23.2 to fix a high-severity advisory.

**6-bespoke** (BSP-001 to BSP-002). No package shrinking: this is a desktop app shipped as jpackage outputs, with no container image. jqwik and its property tests were removed; Jazzer fuzzing stays. CI now builds and tests in a Nix shell with a SonarQube scan, a macOS DMG workflow was added, and the README and CONTRIBUTING describe this fork.

## What changed for users

Removed:

- Groovy scripting, Lucene search across saved maps (in-map find stays), geographic map
- Integrated help, the Help menu, and the startup splash
- LaTeX on a node, spell checking
- Socket, database, and Jabber collaboration; Palm sources
- Browse and File modes (MindMap mode stays)
- The encrypt command (existing encrypted nodes still open)
- HTML, XSLT, SVG, PDF, context-graph, and Onekin wiki exports (PNG export stays)
- Folder-structure import (other imports stay)
- Printing, page setup, and print preview
- All languages except English

Customized:

- Tab adds a child node; Enter still adds a sibling, F2 still edits.
- Unchanged font sizes follow tree depth: deepest level 12 pt, +2 pt per level above, including the center node. A size the user sets is kept.
- With no file on the command line, startup reopens the map that was current at the last quit.
- View menu check marks for Toolbar and Secondary Toolbar now render (commit `3abe3054`, not recorded as a decision).

## Known gaps and open points

- **Coverage gate gone from CI.** Commit `4546bf36` removed the coverage report and the "Enforce Coverage Threshold" step from `build.yml`. COV-006 still says CI fails under 85%. The 85% rule remains in `build.gradle.kts` but CI does not run it. CI also does not run `testPerformance`, which is part of the stage gate.
- **Dependabot auto-merge still present.** `.github/workflows/dependabot-auto-merge.yml` exists on `6-bespoke` (extended in `15fc6c2c` and `02520dd3`), while `main` removed it in #6 (`f99d9db9`).
- **Commits without decision entries.** In 3-tailored: `15fc6c2c` (CI rework), `4f36ae0b` (SpotBugs fixes in tests), `3abe3054` (View menu check marks, a behavior change). In 6-bespoke: `dd7af690`, `02520dd3`, `e14e6d7f` (CodeQL removed), `4546bf36` (Nix, SonarQube), `ba026f27`, `3f880a55` (macOS DMG). `GLOSSARY.md` (added in `5b7a36a5`) has no decision either.
- **Work after a stage exit.** 6-bespoke records its exit at BSP-001, then adds seven more commits, including BSP-002.
- **Non-linear 1-raw/2-covered chain.** `2-covered` branches from `1-raw` at `a9db337e`. The later 1-raw commits were applied again on 2-covered (`14dda8d3`, `54018065`, `aa762ca3`), so the tip of `1-raw` is not an ancestor of `2-covered`.
- **Bundled commits in 4-customized.** Several commits carry multiple decisions (e.g. `26e96f0c` covers CUS-002 to CUS-004), which makes replay per decision harder.
