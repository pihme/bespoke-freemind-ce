# FreeMind CE — agent rules for this fork

This is a bespoke fork of Denomas/freemind-ce. `BESPOKE.md` is the product direction: vision, constraints, the current stage, and decisions. `docs/spec.md` is the feature list. `docs/testspec.md` maps each requirement to the tests that check it.

Read `BESPOKE.md` before changing behavior. A feature is added when `docs/spec.md` lists it. A feature is removed when the user or the current stage asks for that removal. Record the removal in `BESPOKE.md` and in `docs/spec.md`.

## What stays

- Text the user types is stored as typed. Paths, scripts, SQL, and markup go through save, load, and export unchanged.
- An existing `.mm` file opens. When a change cannot keep that, add an importer for freemind-ce files.
- The license stays GPL-2.0.
- The language and the JDK are not fixed. The toolchain in the build file is only what this tree uses today.
- JAXB classes are generated into the build directory when the main sources compile. `make jaxb` runs the same generator. `src/main/schema/jaxb-jibx-compat.patch` puts back the JiBX method names. Do not commit the generated classes and do not hand-edit them.
- `auto.properties` is a runtime file. Leave it uncommitted.
- A warning is fixed at the cause. `@SuppressWarnings` is not the fix.
- Local JARs that the build still uses stay tracked. A JAR nothing calls can leave in the trim stage.
- A dependency change is scanned with `make audit` on this machine. The scan is a local CLI, not a cloud service.
- Another person's branch stays as they left it. Do not force-push, and do not close or rewrite a branch you did not start.

## When the task needs more

| Task | Read |
|---|---|
| Commits, the build, or tests | [CONTRIBUTING.md](CONTRIBUTING.md) |
| Where a class lives | [docs/source-tree-analysis.md](docs/source-tree-analysis.md) |

The reference environment is the dev shell in `flake.nix`. It is binding for build and test. Run them as `nix develop -c <cmd>`, locally and in CI. `make` runs the Gradle targets inside that shell and passes `--no-configuration-cache`. The entry point is `freemind.main.FreeMindStarter`.

A Gradle `--tests` filter is the class's `package` declaration. Read that line. Run a unit check that is already failing on its own, so `testGui` and `jacocoTestReport` still run. Read pass and fail from `build/test-results/*/TEST-*.xml`. Read instruction, line, and branch totals from the root `counter` elements of `build/reports/jacoco/test/jacocoTestReport.xml`.

One Gradle test task is one JVM. `FreeMind.props` and the `Resources` language bundle survive from one test class to the next. The test that asserts one of those resets it and keeps the original expected value. Test classes do not need a class-level `@DisplayName`. jqwik and Jazzer tests carry the tag `fuzz`. `testFuzz` runs them. The unit task and the JaCoCo report leave them out.

| What | Where |
|------|-------|
| Main app class | `src/main/java/freemind/main/FreeMind.java` |
| Controller hub | `src/main/java/freemind/controller/Controller.java` |
| Primary mode | `src/main/java/freemind/modes/mindmapmode/MindMapController.java` |
| Map view | `src/main/java/freemind/view/mindmapview/MapView.java` |
| Node model | `src/main/java/freemind/modes/MindMapNode.java` |

## Subagents

A subagent prompt starts with:

```
Read AGENTS.md and BESPOKE.md. Stay inside this task: [X]
```
