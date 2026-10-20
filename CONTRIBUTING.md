# Contributing to this fork

This tree is a bespoke fork of FreeMind CE. Product direction lives in [BESPOKE.md](BESPOKE.md) and [docs/spec.md](docs/spec.md). Agent rules live in [AGENTS.md](AGENTS.md). This file is the build, test, and commit mechanics.

## Product rules that affect a change

| Rule | What to do |
|---|---|
| User content | Store text as the user typed it, including paths, scripts, SQL, and markup. |
| `.mm` files | An existing file opens. When a change cannot keep that, add an importer. |
| Features | Add one when `docs/spec.md` lists it. Remove one when the user or the current stage asks, and record that in `BESPOKE.md` and `docs/spec.md`. |
| License | Contributions stay under [GPL-2.0](LICENSE). |
| Language and JDK | Not fixed. The toolchain in the build file is what this tree uses today. |

## Prerequisites

- Git
- A JDK the Gradle toolchain can use. The version is set in `freemind/build.gradle.kts`.
- The Gradle wrapper (`./gradlew`)

## Build and test

```bash
make build    # Compile and test
make run      # Run the app
make test     # Tests only
make help     # Every target
```

The Makefile sets `JAVA_HOME` and `--no-configuration-cache`. Command details are in [docs/development-guide.md](docs/development-guide.md).

## Other people's work

Leave a branch you did not start as its author left it. Do not force-push. Do not close or rewrite it.

## Commits

[Conventional Commits](https://www.conventionalcommits.org/), one logical change each. Subject line up to 72 characters, imperative mood. The body says why.

Allowed types: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, `revert`, `deps`.

A behavior change names the spec row and the `BESPOKE.md` decision in the body.

Before committing:

1. `make build` passes.
2. `make test` passes.
3. A dependency change also passes `make audit`.
4. `git diff --staged` contains that change only. Stage files by name.
5. Leave out `build/`, `*.class`, `auto.properties`, and IDE files.

## Security scan

A dependency, including a tracked local JAR, is free of known high-severity vulnerabilities with a fix before it is pushed. The scan is a local CLI.

```bash
make audit
make audit-full
```

Grype is `make audit`. The OWASP HTML report is `make audit-full`. Install Grype with `mise install grype` when it is missing. A high finding with a fix is updated. A high finding with no fix is written down in `config/owasp-suppressions.xml` with the reason.

## Static analysis

`make build` runs PMD and SpotBugs.

| Tool | Config | Report |
|---|---|---|
| PMD 7.21.0 | `freemind/config/pmd-ruleset.xml` | `freemind/build/reports/pmd/` |
| SpotBugs 6.4.8, high confidence | `freemind/config/spotbugs-exclude.xml` | `freemind/build/reports/spotbugs/` |

SpotBugs fails the build. PMD currently reports and does not fail the build, because the tree still has old violations.

## Code style

- Match the surrounding code.
- Fix a warning at the cause.
- Regenerate `generated-src/` with `make jaxb`.
- Leave a local JAR tracked while the build still uses it.

## Tests

Commands, base classes, and layout are in [docs/development-guide.md — Testing](docs/development-guide.md#testing). A test asserts the behavior it claims to check.
