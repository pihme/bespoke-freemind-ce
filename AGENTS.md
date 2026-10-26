# FreeMind CE — agent rules for this fork

This is a bespoke fork of Denomas/freemind-ce. `BESPOKE.md` is the product direction: vision, constraints, the current stage, and decisions. `docs/spec.md` is the feature list. `docs/testspec.md` maps each requirement to the tests that check it.

Read `BESPOKE.md` before changing behavior. A feature is added when `docs/spec.md` lists it. A feature is removed when the user or the current stage asks for that removal. Record the removal in `BESPOKE.md` and in `docs/spec.md`.

## What stays

- Text the user types is stored as typed. Paths, scripts, SQL, and markup go through save, load, and export unchanged.
- An existing `.mm` file opens. When a change cannot keep that, add an importer for freemind-ce files.
- The license stays GPL-2.0.
- The language and the JDK are not fixed. The toolchain in the build file is only what this tree uses today.
- `generated-src/` is JAXB output. Regenerate it with `make jaxb`.
- `auto.properties` is a runtime file. Leave it uncommitted.
- A warning is fixed at the cause. `@SuppressWarnings` is not the fix.
- Local JARs that the build still uses stay tracked. A JAR nothing calls can leave in the trim stage.
- A dependency change is scanned with `make audit` on this machine. The scan is a local CLI, not a cloud service.
- Another person's branch stays as they left it. Do not force-push, and do not close or rewrite a branch you did not start.

## When the task needs more

| Task | Read |
|---|---|
| Commits, the build, or tests | [CONTRIBUTING.md](CONTRIBUTING.md) |
| Design of the current code | [docs/architecture.md](docs/architecture.md) |
| Where a class lives | [docs/source-tree-analysis.md](docs/source-tree-analysis.md) and [docs/component-inventory.md](docs/component-inventory.md) |
| How to build, test, or package | [docs/development-guide.md](docs/development-guide.md) |
| An upstream-style release | [docs/contributor-workflows.md](docs/contributor-workflows.md) and [docs/merge-release-safety.md](docs/merge-release-safety.md). Those notes describe Denomas/freemind-ce. This fork's rules are this file and `BESPOKE.md`. |

`make help` lists the build targets. `make` passes `--no-configuration-cache`. It keeps a `JAVA_HOME` that is already set. When `JAVA_HOME` is unset, it looks for a JDK 21 on the system paths and stops when it finds none. Set `JAVA_HOME` to a JDK 21 and run `make` again. The entry point is `freemind.main.FreeMindStarter`.

A Gradle `--tests` filter is the class's `package` declaration. Read that line. Run a unit check that is already failing on its own, so `:freemind:testGui` and `jacocoTestReport` still run. Read pass and fail from `freemind/build/test-results/*/TEST-*.xml`. Read instruction, line, and branch totals from the root `counter` elements of `freemind/build/reports/jacoco/test/jacocoTestReport.xml`.

One Gradle test task is one JVM. `FreeMind.props`, the `Resources` language bundle, and `SpellChecker.getCurrentLocale()` survive from one test class to the next. The test that asserts one of those resets it and keeps the original expected value. Test classes do not need a class-level `@DisplayName`. jqwik and Jazzer tests carry the tag `fuzz`. `:freemind:testFuzz` runs them. The unit task and the JaCoCo report leave them out.

| What | Where |
|------|-------|
| Main app class | `freemind/freemind/main/FreeMind.java` |
| Controller hub | `freemind/freemind/controller/Controller.java` |
| Primary mode | `freemind/freemind/modes/mindmapmode/MindMapController.java` |
| Map view | `freemind/freemind/view/mindmapview/MapView.java` |
| Node model | `freemind/freemind/modes/MindMapNode.java` |

## Subagents

A subagent prompt starts with:

```
Read AGENTS.md and BESPOKE.md. Stay inside this task: [X]
```
