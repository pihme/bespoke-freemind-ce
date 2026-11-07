# Contributing

This is a personal edition of [FreeMind CE](https://github.com/Denomas/freemind-ce). It is built for one person, and that person is the audience.

If you want a copy of your own, fork it and make it yours. That is the welcome mat. This repository is not collecting pull requests or feature requests, and a fork is the right place for a different idea.

The notes below are how this tree is built, so a fork has a working place to start. Product direction is in [BESPOKE.md](BESPOKE.md). The feature list is [docs/spec.md](docs/spec.md).

## Building

You need Git and Nix. The dev shell in `flake.nix` provides the JDK this tree uses today. `make` runs Gradle inside that shell.

```bash
make build    # Compile and package. This does not run tests.
make test     # Unit tests
make test-gui # GUI tests. These need a display.
make run      # Run the app
make help     # Every target
```

`make` passes `--no-configuration-cache`.

CI uses the same three beats. The build only compiles and packages. The test run includes the unit tests and the GUI tests. Quality control sends the sources to SonarQube Cloud.

## While you are in this tree

Text is stored as it was typed, including paths, scripts, SQL, and markup. An existing `.mm` file opens. When a change cannot keep that, add an importer. A feature is added when `docs/spec.md` lists it, and removed when that removal is recorded in `BESPOKE.md` and in `docs/spec.md`. The language and the JDK are not fixed. The toolchain in the build file is only what this tree uses today.

Leave a branch you did not start as its author left it. Do not force-push, and do not close or rewrite it.

Commits follow [Conventional Commits](https://www.conventionalcommits.org/). One change each. The subject is imperative and at most 72 characters. The body says why. The types are `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, `revert`, and `deps`. A behavior change names the spec row and the `BESPOKE.md` decision.

Before a commit, `make build` and `make test` pass. A dependency change also passes `make audit`. The staged diff is that change only. Leave out `build/`, `*.class`, `auto.properties`, and IDE files.

A dependency, including a tracked local JAR, is free of known high-severity vulnerabilities that already have a fix. `make audit` is the Grype scan. `make audit-full` is the OWASP report. Install Grype with `mise install grype` when it is missing. A high finding with a fix is updated. A high finding with no fix is written down in `config/dependency-check/suppressions.xml`, with the reason.

PMD and SpotBugs are configured, and they run with `make check`, not with the quick build. SpotBugs fails that check. PMD reports and does not fail it, because the tree still has old violations.

| Tool | Config | Report |
|---|---|---|
| PMD 7.21.0 | `config/pmd/ruleset.xml` | `build/reports/pmd/` |
| SpotBugs 6.4.8, high confidence | `config/spotbugs/exclude.xml` | `build/reports/spotbugs/` |

Match the surrounding code. Fix a warning at the cause. JAXB classes are generated into the build directory. `make jaxb` regenerates them, and `src/main/schema/jaxb-jibx-compat.patch` puts the JiBX method names back. Do not commit the generated classes, and do not hand-edit them. Leave a local JAR tracked while the build still uses it.

A test asserts the behavior it claims to check.

## License

The program is under [GPL-2.0](LICENSE). A fork stays under that license too.
