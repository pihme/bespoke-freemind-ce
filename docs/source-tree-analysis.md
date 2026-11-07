# Source tree

The repository root is the Gradle project `bespoke-freemind-ce`. Sources follow the usual Gradle layout. `build.gradle.kts` adds the generated JAXB directory and excludes two classes the current JDK cannot compile.

```
.
├── src/main/java/
│   ├── freemind/                              # package freemind.*
│   │   ├── main/                              # FreeMindStarter, FreeMind
│   │   ├── controller/                        # menus, toolbars, filter
│   │   ├── view/mindmapview/                  # map canvas
│   │   ├── modes/mindmapmode/                 # the editing mode
│   │   ├── modes/common/
│   │   ├── modes/attributes/
│   │   ├── modes/viewmodes/
│   │   ├── extensions/
│   │   ├── preferences/
│   │   ├── common/
│   │   └── swing/
│   └── accessories/                           # notes, icons, time, clone, and the other built-in features
├── src/main/resources/                        # classpath resources
│   ├── freemind.properties
│   ├── mindmap_menus.xml
│   ├── patterns.xml
│   ├── Resources_en.properties
│   ├── images/                                # toolbar icons, node icons, window icons
│   ├── doc/                                   # freemind.mm and the translated copies
│   ├── accessories/                           # feature properties, xsl, icons
│   └── freemind/modes/                        # mode xslt and xml
├── src/main/schema/                           # freemind.xsd, freemind_actions.xsd, jaxb-jibx-compat.patch
├── src/test/java/                             # directory matches the package
├── src/test/resources/
├── lib/                                       # SimplyHTML.jar
├── config/                                    # Gradle tool files, not program preferences
│   ├── dependency-check/suppressions.xml
│   ├── pmd/ruleset.xml
│   └── spotbugs/exclude.xml
├── jpackage/                                  # .mm association for the installers
│   └── file-associations.properties
├── appimage/                                  # desktop file
├── docs/                                      # spec, testspec, source tree
├── .github/workflows/                         # Build runs on the stage branches
├── build.gradle.kts
├── settings.gradle.kts                        # rootProject.name = bespoke-freemind-ce
├── Makefile
├── AGENTS.md
└── BESPOKE.md
```

## What the program loads

Production Java is `src/main/java`. JAXB classes are generated into `build/generated/sources/jaxb/java` when the main sources compile. They are not committed. `make jaxb` runs the same generator. `src/main/schema/jaxb-jibx-compat.patch` puts back the JiBX method names the program already calls. Do not hand-edit the generated classes.

Classpath resources live in `src/main/resources`, including `freemind.properties`, `patterns.xml`, `mindmap_menus.xml`, `Resources_en.properties`, `images/**`, and `doc/**/*.mm`. Schemas stay in `src/main/schema` and are not on the runtime classpath. `config/` is read by PMD, SpotBugs, and the dependency-check task. The program does not read it. Notes, clones, icons, and the other former plugins are constructed from `BuiltInHooks`. The program does not scan plugin XML.

`FreeMindApplet.java` and `FreeMindSecurityManager.java` stay in the tree and are excluded from compilation.

## Entry points

| Entry | File |
|---|---|
| `freemind.main.FreeMindStarter` | `src/main/java/freemind/main/FreeMindStarter.java` |
| Gradle | `build.gradle.kts` |
| Make | `Makefile` |
| CI | `.github/workflows/build.yml` |

MindMap mode is the only mode. The plugin Gradle modules are gone. The `plugins` directory is gone. `lib/SimplyHTML/SimplyHTML.jar` stays. `gnu-regexp` 1.1.4 comes from Maven Central.
