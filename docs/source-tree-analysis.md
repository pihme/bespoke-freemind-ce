# Source tree

The Gradle module is `freemind/`. Java sources are not under `src/main/java`. `freemind/build.gradle.kts` names the source directories.

```
.
├── freemind/                         # module :freemind
│   ├── freemind/                     # package freemind.*
│   │   ├── main/                     # FreeMindStarter, FreeMind
│   │   ├── controller/               # menus, toolbars, filter
│   │   ├── view/mindmapview/         # map canvas
│   │   ├── modes/mindmapmode/        # the editing mode
│   │   ├── modes/common/
│   │   ├── modes/attributes/
│   │   ├── modes/viewmodes/
│   │   ├── extensions/
│   │   ├── preferences/
│   │   ├── common/
│   │   └── swing/
│   ├── accessories/                  # notes, icons, time, clone, and the other accessory plugins
│   ├── de/foltin/                    # StringEncoder
│   ├── generated-src/                # JAXB output. Regenerate with make jaxb. Do not edit.
│   ├── plugins/                      # jars kept for the trimmed stage, plus leftover Ant files
│   │   ├── help/jhall.jar
│   │   └── map/JMapViewer.jar
│   ├── tests/
│   ├── images/                       # toolbar icons, node icons, window icons
│   ├── doc/                          # freemind.mm and the translated copies
│   ├── lib/                          # SimplyHTML, gnu-regexp, jortho
│   ├── config/                       # SpotBugs, PMD, OWASP suppressions
│   ├── appimage/                     # desktop file
│   ├── build.gradle.kts
│   ├── build.xml                     # Ant. Not the build.
│   ├── freemind.properties
│   ├── mindmap_menus.xml
│   ├── patterns.xml
│   ├── freemind.xsd
│   └── freemind_actions.xsd
├── docs/                             # spec, testspec, and older architecture notes
├── .github/workflows/                # Build runs on the stage branches
├── build.gradle.kts
├── settings.gradle.kts               # includes only :freemind
├── Makefile
├── AGENTS.md
└── BESPOKE.md
```

## What the program loads

The compile roots are `freemind`, `accessories`, `de`, `generated-src`, and `plugins`. The classpath resources include `freemind.properties`, `patterns.xml`, `mindmap_menus.xml`, `Resources_en.properties`, `images/**`, and `doc/**/*.mm`.

## Entry points

| Entry | File |
|---|---|
| `freemind.main.FreeMindStarter` | `freemind/freemind/main/FreeMindStarter.java` |
| Gradle | `freemind/build.gradle.kts` |
| Make | `Makefile` |
| CI | `.github/workflows/build.yml` |

MindMap mode is the only mode. The plugin Gradle modules are gone. `plugins/help/jhall.jar` and `plugins/map/JMapViewer.jar` stay until the trimmed stage.
