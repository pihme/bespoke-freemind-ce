# bespoke-freemind-ce

A bespoke fork of [FreeMind CE](https://github.com/Denomas/freemind-ce), a Java mind-map editor.

Product direction is in [BESPOKE.md](BESPOKE.md). The feature list is [docs/spec.md](docs/spec.md). Build and test steps are in [CONTRIBUTING.md](CONTRIBUTING.md) and [docs/development-guide.md](docs/development-guide.md).

## Run

The Gradle wrapper builds and runs the app. The toolchain in the build file is Java 21.

```bash
git clone https://github.com/pihme/bespoke-freemind-ce.git
cd bespoke-freemind-ce
./gradlew run --no-configuration-cache
```

`make run` does the same. `make help` lists the other targets.

## Layout

```
src/main/java/          production sources
src/main/resources/     classpath resources
src/main/schema/        XSD schemas
src/test/java/          tests
build.gradle.kts        the build
```

The entry point is `freemind.main.FreeMindStarter`.

## License

GNU General Public License version 2. The text is [LICENSE](LICENSE).

```
Copyright (C) 2000-2026 Joerg Mueller, Daniel Polansky, Christian Foltin,
Dimitry Polivaev, Tolga Karatas (Denomas), and others.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.
```
