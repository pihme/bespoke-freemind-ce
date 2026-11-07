# bespoke-freemind-ce

A desktop mind-map editor. This is a bespoke fork of [FreeMind CE](https://github.com/Denomas/freemind-ce). Maps stay in the `.mm` file format. The feature set is the core of editing a map.

Product direction is in [BESPOKE.md](BESPOKE.md). The feature list is [docs/spec.md](docs/spec.md). Build and test steps are in [CONTRIBUTING.md](CONTRIBUTING.md).

## Changes

Tab adds a child. Enter adds a sibling. F2 edits the selected node.

An unchanged font size follows the depth of the tree. The deepest nodes are 12 points, and each level above is 2 points larger, including the center node. A size you set is stored and stays, including across save and load. Adding or removing a node that changes the depth updates every unchanged size.

With no file on the command line, startup opens the map that was current when the app last closed. A file named on the command line opens instead.

## Not in this fork

Printing, the Help menu, and the startup splash are gone. So are scripting, search across saved maps, LaTeX, spelling, real-time collaboration, SVG and PDF export, and the plugin loader. Find inside the open map stays. The interface is English.

This is a desktop app. There is no container image. Packages, when built, are the jpackage outputs.

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
