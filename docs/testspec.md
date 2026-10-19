# FreeMind CE test spec

Maps every ID in `docs/spec.md` to the tests whose assertions check that behaviour. The rows below include the `TestREQ_` methods added with the 21.8% measurement. Older tests were not renamed.

A test counts only when it asserts the behaviour. Opening a menu, counting nodes, or asserting that saved XML is non-empty does not count. GUI tests that only call the same model setter as a unit test are named with that unit test, not as a separate menu check.

## How to read a row

- **Checked** — an assertion covers the behaviour the requirement states. A missing clause is named on the row.
- **Partial** — at least one clause is checked and at least one is not.
- **None** — no assertion checks the behaviour.

49 requirements: 14 checked, 28 partial, 7 none.

## Coverage number

Authoritative number: **21.8% instruction** (32,021 of 146,900), measured 2026-10-19 with the `TestREQ_` tests in this change. The same command at `a9db337` was 21.3% (31,265 of 146,900). Line coverage 23.0% (8,559 of 37,282). Branch coverage 20.8% (2,602 of 12,519). The chaos exec contains no tests.

Command. `jacocoTestReport` depends on all four test tasks and reads every `freemind/build/jacoco/*.exec`:

```
./gradlew :freemind:test :freemind:testGui :freemind:testPerformance :freemind:testChaos :freemind:jacocoTestReport --no-configuration-cache
```

HTML report: `freemind/build/reports/jacoco/test/html/index.html`. This number is not a failing CI threshold yet. Per-module numbers are not a second score.

## Test strategy

- Unit tests: `:freemind:test`, headless, excludes tag `gui`. GUI tests: `:freemind:testGui`, needs a display. Performance: `:freemind:testPerformance`, tag `performance`. Chaos: `:freemind:testChaos`, tag `chaos`. Nothing is tagged `chaos`, so that task runs and finds no tests.
- JUnit 5 is the runner. jqwik property tests and `fuzz/MindMapParsingFuzzTest.java` run with the unit task. JaCoCo is the coverage tool. No mutation tool is chosen. `select-tool` runs before any mutation campaign. Mutation stays out of the normal CI pipeline.
- A test that calls code and asserts nothing is not a check. `ExportTests.testExportPng` still calls `exportFileToPng` and asserts nothing. `TestREQ_FILE_03_PngExportWritesPngBytes` is the check.
- Equivalent mutants: none. Mutation has not been run.

## Clarifications during tests

| # | Requirement | Question | Decision | Commit |
|---|-------------|----------|----------|--------|
| | | | | |

No question is decided here. REQ-STYLE-02 says the underline menu entry is commented out as not implemented in the view. `NodeAdapterTest.setUnderlinedChangesState` and `FormatMenuGuiTest.format_underline` still set the model flag. That stays a note on the row until you decide otherwise.

## Map

### REQ-MAP-01 — Checked

Create, open, and save a map.

- `io/MapLifecycleTest.java` `newMapHasNonNullRoot`, `emptyMapGetXmlProducesValidXml`, `getXmlWritesContentToWriter`, `reSaveToSameFileProducesValidXml`: a new map has a root, `getXml` writes a `<map>` document, and saving again keeps a node added after the first save.
- `gui/FileOperationsGuiTest.java` `fileMenu_openMap`, `fileMenu_save`: a `.mm` file loads with root text `Opened`, and the saved document contains the stored node text.
- `io/MapRoundTripTest.java` `smallMapFileRoundTrip`: a `.mm` file reloads with the same node count.

### REQ-MAP-02 — Partial

Save As is checked. Revert is not.

- `gui/FileOperationsGuiTest.java` `fileMenu_saveAs`: the same map written to a second path is byte-for-byte equal.
- Revert to the last saved file: none.

### REQ-MAP-03 — Partial

The `.mm` tree, node text, icons, links, notes, a cloud, and arrow links survive save and load. Node color, font, node style, and edge style, color, and width are set in memory and are not asserted in the saved file.

- Tree and text: `StandaloneMapTests.java` `testStandaloneCreation`; `BackwardCompatTest.java` `mapWithChildrenLoads`; `io/MapRoundTripTest.java` `childOrderingPreserved`, `xmlSpecialCharsPreserved`, `veryLongTextPreserved`.
- Icons: `io/MapRoundTripTest.java` `iconsPreservedThroughRoundTrip` (`button_ok`, `idea`).
- Links: `io/MapLifecycleTest.java` `hyperlinkPreservedThroughRoundTrip`; `io/MapRoundTripTest.java` `fileLinkPreserved`.
- Notes: `io/MapRoundTripTest.java` `htmlNotesPreserved`; `BackwardCompatTest.java` `mapWithRichContentLoads`.
- Cloud present: `BackwardCompatTest.java` `mapWithCloudLoads`. Cloud color is not asserted.
- Arrow link: `io/MapLifecycleTest.java` `arrowLinkAppearsInSavedXml`, `removeLinkTargetNodeNoDanglingReference`.

### REQ-MAP-04 — Partial

A recent-file string and a window rectangle round-trip. More than one map open at once, and restoring a real frame, are not checked.

- `LastOpenedTests.java` `testStrangeCharsInList`: `LastOpenedList.save()` keeps a path that contains `&`.
- `property/XMLRoundTripPropertyTest.java` `windowConfigRoundTrip`: x, y, width, and height survive marshalling. No frame is restored.

## Nodes

### REQ-NODE-01 — Partial

Adding a child or sibling and storing its text are checked on the model. The in-place editor, the dialog, and an external application are not launched.

- `modes/NodeAdapterTest.java` `insertChildIncreasesCount`, `setAndGetText`.
- `gui/NodeCrudGuiTest.java` `node_createChild`, `node_createSibling`, `node_setNodeText`.
- `actions/ActionFrameworkTest.java` `setNodeTextChangesText`.

### REQ-NODE-02 — Checked

- `modes/NodeAdapterTest.java` `notFoldedByDefault`, `setFoldedRoundTrips`.
- `gui/NavigateMenuGuiTest.java` `navigate_foldAll`, `navigate_unfoldAll`: `setFolded` on the branch is true, then false. These call the model, not the menu action.
- `BackwardCompatTest.java` `mapWithFoldedNodesLoads`: `FOLDED="true"` loads folded, and the child is still there.

### REQ-NODE-03 — Checked

- `requirements/RequirementBehaviourTest.java` `TestREQ_NODE_03_UndoRedoUsesXmlActionPair`: `setNodeText` stores an `EditNodeAction` pair whose do text is the new text and whose undo text is the old text. Applying the reversed pair restores the old text. Applying the do action again sets the new text.

`ActionPairTests` stores action objects and does not apply them. The GUI undo methods delete or recreate nodes by hand. The Undo menu stack is not used.

### REQ-NODE-04 — Partial

Delete is checked. Cut reduces the child count. Paste of a string increases the count and does not assert the pasted text. Copy, copy single, and paste as plain text are not checked.

- Cut and paste count: `StandaloneMapTests.java` `testXmlChangeWithoutModeController` calls `mapFeedback.cut` and `mapFeedback.paste`.
- Delete: `gui/NodeCrudGuiTest.java` `node_deleteChild`, `node_nestedDeletion`.

### REQ-NODE-05 — Checked

- `requirements/RequirementBehaviourTest.java` `TestREQ_NODE_05_SelectAllAndBranch`: `MapView.selectBranch` on the root selects every node view. `selectBranch` on the Branch node, without extend, selects that node and Leaf, and not the root or Other.

The Edit menu actions are not invoked. `SelectAllAction` and `SelectBranchAction` call this same view method.

### REQ-NODE-06 — Partial

`moveNodes` changes the child index. The drag tests create a new node instead of reparenting the original.

- `StandaloneMapTests.java` `testXmlChangeWithoutModeController`: `moveNodes` changes index 0 to 1, back to 0, then to 2.

### REQ-NODE-07 — None

No test joins nodes.

### REQ-NODE-08 — None

No test puts an image on a node.

### REQ-NODE-09 — Partial

The plain-text order is checked. `SortNodes.invoke` is not called, so the cut and paste that reorder the map are not checked.

- `requirements/RequirementBehaviourTest.java` `TestREQ_NODE_09_SortsChildrenByPlainText`: the plugin's `NodeTextComparator` orders `apple` before `beta` (read from HTML) before `Zebra`, treats `Apple` and `apple` as equal, and reverses that order after `setNegative`.
- `gui/ToolsMenuGuiTest.java` `tools_sortNodes` uses `containsExactlyInAnyOrder`, so order is not checked.

## Appearance

### REQ-STYLE-01 — Partial

Fork and bubble are stored on a node. The root's own shape is not asserted.

- `modes/NodeAdapterTest.java` `setAndGetStyle`, `setStyleFork`.
- `StandaloneMapTests.java` `testXmlChangeWithoutModeController`: `STYLE_FORK` is stored, and `null` clears `hasStyle`.

### REQ-STYLE-02 — Partial

Bold, italic, strikethrough, font size, text color, and background are checked. Color blend is not. Underline is set on the model even though the spec says the view entry is commented out.

- `actions/ActionFrameworkTest.java` `setBoldChangesState`, `setItalicChangesState`, `setStrikethroughChangesState`, `setFontSizeChangesSize`, `setNodeColorChangesColor`, `setNodeBackgroundColorChanges`, `multipleFormatOperationsCompose`.
- `modes/NodeAdapterTest.java` `setUnderlinedChangesState` sets the underline flag.

### REQ-STYLE-03 — Partial

HTML conversion and tag stripping are checked. A user toggle between rich text and plain text is not.

- `HtmlConversionTests.java` `testSetHtml`, `testHtmlBodyExtraction`.
- `findreplace/FindTextTests.java` `testTagRemoval`, `testTagRemovalOnlyForHtmlText`.

### REQ-STYLE-04 — Partial

Applying one pattern changes the target node's color. The pattern-manager UI is not checked.

- `StandaloneMapTests.java` `testXmlChangeWithoutModeController`: a pattern taken from a magenta node makes another node magenta.
- `property/XMLRoundTripPropertyTest.java` `patternsRoundTrip` keeps pattern names only.

### REQ-STYLE-05 — Checked

- `modes/NodeAdapterTest.java` `addIconIncreasesCount`, `removeIconReturnsRemainingCount`.
- `io/MapRoundTripTest.java` `iconsPreservedThroughRoundTrip`.
- `StandaloneMapTests.java` `testXmlChangeWithoutModeController`: add one, remove last, add five, remove all.

### REQ-STYLE-06 — Checked

- `modes/NodeAdapterTest.java` `setAndGetCloud`, `cloudSetAndGetColor`, `setCloudNullRemovesCloud`.
- `actions/ActionFrameworkTest.java` `setCloudEnables`, `setCloudDisables`.
- `BackwardCompatTest.java` `mapWithCloudLoads` checks presence only.

`FormatMenuGuiTest.format_cloudToggle` leaves the cloud null and is not counted.

### REQ-STYLE-07 — Partial

Bezier, linear, sharp bezier, color, and width are stored. Sharp linear is not set on a node.

- `modes/NodeAdapterTest.java` `edgeSetAndGetStyle`, `edgeGetStyleAsIntLinear`, `edgeSetAndGetColor`, `edgeSetAndGetWidth`.
- `StandaloneMapTests.java` `testXmlChangeWithoutModeController`: `sharp_bezier` is stored, and `null` falls back to bezier.

### REQ-STYLE-08 — Partial

The zoom factor is checked. The mouse wheel is not.

- `requirements/RequirementBehaviourTest.java` `TestREQ_STYLE_08_ZoomScalesTheMap`: `MapView.setZoom(1.5f)` makes `getZoom` 1.5 and `getZoomed(100)` 150. Setting 1f makes `getZoomed(100)` 100.

The older zoom methods only assert that map XML is unchanged.

## Links, notes, attributes

### REQ-LINK-01 — Partial

Setting, clearing, and reloading a file or text link are checked. Following the link and opening its directory are not.

- `modes/NodeAdapterTest.java` `setAndGetLink`, `setLinkNull`.
- `actions/ActionFrameworkTest.java` `setLinkSetsHyperlink`.
- `io/MapRoundTripTest.java` `fileLinkPreserved`, `javascriptLinkPreserved`.

### REQ-LINK-02 — Partial

An arrow between two nodes is checked. A link whose target is another node in the map (`#id`) is not.

- `StandaloneMapTests.java` `testXmlChangeWithoutModeController`: `addLink` stores the target, the ends, and the color; `removeReference` leaves none.
- `io/MapLifecycleTest.java` `arrowLinkViaFeedbackAppearsInRegistryAndXml`.

### REQ-NOTE-01 — Checked

- `modes/NodeAdapterTest.java` `setAndGetNoteText`, `setNoteTextNull`.
- `io/MapRoundTripTest.java` `htmlNotesPreserved`.
- `unicode/Utf8IntegrationTest.java` `noteText_withUnicode_roundTrip`.

### REQ-ATTR-01 — Checked

Text, attribute, and icon filters, AND and OR, and visibility are checked.

- `filter/FilterConditionBasicTest.java` `matchingNode`, `caseSensitive`, `equalTo`, `greaterThan`.
- `filter/FilterConditionAttributeIconTest.java` `equalTo`, `iconPresent`, `attributeExists`.
- `filter/FilterConditionCompositeTest.java` `allTrue`, `oneFalse`; `DisjunctConditionsTests.oneTrue`.
- `filter/FilterIntegrationPipelineTest.java` `matchedNodesVisibleUnmatchedHidden`, `filterWithConjunctCondition`, `filterWithDisjunctCondition`.

### REQ-FIND-01 — None

No test runs find or find next. `FindTextTests` checks HTML tag stripping, a time-list row filter, and replace. `gui/EditMenuGuiTest.java` `edit_find` uses the test helper `findNodeByText`.

### REQ-ATTR-02 — Checked

- `modes/NodeAdapterTest.java` `addAttributeIncreasesCount`, `setAttributeReplacesAtPosition`, `removeAttributeDecreasesCount`.
- `unicode/Utf8IntegrationTest.java` `nodeAttributes_withUnicodeValues`.
- `StandaloneMapTests.java` `testXmlChangeWithoutModeController`: add, replace, insert, and remove leave the asserted keys.

## Files in and out

### REQ-FILE-01 — Checked

- `export/XsltExportTest.java` `htmlExportContainsRootNodeText`, `htmlExportContainsChildNodeTexts`, `standaloneHtmlExportIsWellFormed`.
- `export/ContentPreservationExportTest.java` `htmlExportPreservesHebrewText`.

`gui/ExportGuiTest.java` methods named `export_html*` only assert `.mm` XML and are not counted.

### REQ-FILE-02 — Partial

SVG is checked. PDF and the PDF options dialog are not.

- `plugins/svg/TestREQ_FILE_02_SvgContainsNodeText.java` `TestREQ_FILE_02_SvgDocumentContainsNodeText`: `fillSVGGraphics2D` writes an SVG document that contains `SvgRoot` and `SvgChild`.

The GUI methods only assert `.mm` XML.

### REQ-FILE-03 — Checked

- `requirements/RequirementBehaviourTest.java` `TestREQ_FILE_03_PngExportWritesPngBytes`: `exportFileToPng` writes a file whose first bytes are the PNG signature, `ImageIO` reads a positive width and height, and at least one pixel is not white.

`ExportTests.testExportPng` still asserts nothing. `gui/ExportGuiTest.java` `export_png` only asserts that `.mm` XML is non-empty. The file chooser is not opened.

### REQ-FILE-04 — Partial

Markdown headings and a link, and context-graph XML node text, are checked. Node count, depth, and date in the XML header are not.

- `ContextGraphExportTest.java` `testMarkdownExportContainsHeadings`, `testMarkdownExportContainsLinks`, `testXmlExportWellFormed`.

### REQ-FILE-05 — Checked

- `requirements/RequirementBehaviourTest.java` `TestREQ_FILE_05_BranchBecomesOwnMap`: the Branch node is detached, saved with `MindMapMapModel.save`, and loaded again. The loaded root text is `Branch`, its only child is `Leaf`, and `ROOT` and `Other` are absent.

The file chooser is not opened, and the original map is not given the linked stub that `ExportBranchAction` inserts.

### REQ-FILE-06 — None

No test calls the import actions. `gui/ImportGuiTest.java` copies node text by hand or loads ordinary `.mm` XML.

### REQ-FILE-07 — Partial

These accessory outputs are checked: plain text, CSV, OPML, a LaTeX article, WordML (non-blank), XBEL, TWiki, Redmine wiki, and an `.odt` file. The spec says further accessories exist and does not name them one by one. This row does not claim those others.

- `export/XsltExportTest.java` `textExportContainsNodeTexts`, `csvExportContainsMultipleNodes`, `mm2opmlProducesOutput`, `mm2latexartclProducesOutput`, `mm2wordmlProducesOutput`, `mm2xbelProducesOutput`, `mm2twikiProducesOutput`, `mm2redminewikiProducesOutput`.
- `TransformTest.java` `testExportOoo`: the OpenOffice export writes an `.odt` file.

## Plugins and modes

### REQ-PLUG-01 — Partial

A Groovy script result and the excluded script security manager are checked. The editor is not opened.

- `requirements/RequirementBehaviourTest.java` `TestREQ_PLUG_01_GroovyScriptReturnsValue`: a `GroovyShell` with the same `ThreadInterrupt` customizer `ScriptingEngine` uses evaluates `1 + 2` to 3. `freemind/build.gradle.kts` excludes `ScriptingSecurityManager.java`.
- `ScriptEditorPanelTest.java` `testErrorLineNumbers`: a Groovy error string yields line number 1.

### REQ-PLUG-02 — Partial

Nominatim XML unmarshalls to places. No test attaches a position to a node or opens a map view.

- `MarshallerTests.java` `testOsmNominatimConversion`, `testNominatimReverse`.

### REQ-PLUG-03 — Checked

- `plugins/search/TestREQ_PLUG_03_LuceneFindsIndexedMap.java` `TestREQ_PLUG_03_IndexFindsOneMapAndMissesTheOther`: `FileSearchModel` indexes a directory. A query for a token that is in `hit.mm` returns that file only. The same token in `notes.txt` is not a hit. A token that is in neither file returns no path.

`PluginLogicTest.SearchPluginTests` only checks that the class loads. The search panel is not opened.

### REQ-PLUG-04 — None

No test opens help. The help GUI tests check version strings and non-null objects.

### REQ-PLUG-05 — None

No test renders LaTeX on a node. `export/XsltExportTest.java` `mm2latexartclProducesOutput` exports a LaTeX article file, which is REQ-FILE-07.

### REQ-PLUG-06 — Partial

A localhost socket accepts a client through hello, welcome, and lock, and publishing a map creates the file on the server. The note text is not checked, and the received map is not applied.

- `CollaborationTests.java` `testNormalStartup`, `testNormalStartupWithTwoClients`, `testPublishExistingMapStartup`, `testPublishExistingMapWrongName`.

### REQ-PLUG-07 — None

No test references JOrtho, a spell checker, or the dictionaries.

### REQ-PLUG-08 — Checked

- `requirements/RequirementBehaviourTest.java` `TestREQ_PLUG_08_BuildOmitsLegacyModules`: every `include(` line in `settings.gradle.kts` omits database, jabber, and pda. `freemind/build.gradle.kts` excludes `**/jabber/**` and `**/database/**` from the main Java source set.

### REQ-MODE-01 — Partial

The default `initial_mode` property is `MindMap`. Browse mode and File mode are not started.

- `DefaultPropertiesTest.java` `initialModeDefaultIsMindMap`.

## Application

### REQ-APP-01 — Partial

The property store changes. The options dialog is not opened, and nothing is written to `auto.properties`.

- `requirements/RequirementBehaviourTest.java` `TestREQ_APP_01_PreferenceStoreChanges`: `HeadlessFreeMind.setProperty` then `getProperty` returns the new value, and a second set replaces it.

`DefaultPropertiesTest` only reads defaults. `HeadlessFreeMind.saveProperties` is empty.

### REQ-APP-02 — Partial

Paper size and imageable area survive a string round trip. Nothing prints, and nothing opens print preview.

- `ToolsTests.java` `testPageFormatStorage`.

### REQ-APP-03 — Partial

Thirty or more language files are present, and English and German disagree on one label. The running UI language is not switched.

- `requirements/RequirementBehaviourTest.java` `TestREQ_APP_03_AtLeastThirtyLanguages`: at least 30 `Resources_*.properties` files, `about` is `About` in English, and the German value is different.

The unicode tests preserve map text. That is REQ-MAP-03, not this requirement.

### REQ-APP-04 — Partial

Both FlatLaf themes are applied to `UIManager`. The `lookandfeel` property in `FreeMind.updateLookAndFeel` is not the caller.

- `requirements/RequirementBehaviourTest.java` `TestREQ_APP_04_FlatLafLightAndDark`: `FlatLightLaf.setup` then `FlatDarkLaf.setup` leave `UIManager` on `FlatLightLaf` and then `FlatDarkLaf`.

`FlatLafPropertiesTest` checks that property keys exist.

### REQ-APP-05 — Partial

The build file names Java 21 and a package task for macOS, Windows, and Linux. Nothing is launched on those systems.

- `requirements/RequirementBehaviourTest.java` `TestREQ_APP_05_Java21AndThreePackages`: `freemind/build.gradle.kts` sets `JavaLanguageVersion.of(21)`, registers `jpackageMac`, `jpackageWin`, and `jpackageLinux`, and names `freemind.main.FreeMindStarter`.

### REQ-SEC-01 — Partial

A cipher round-trips a string, and a wrong password does not return the original string. No test encrypts a node in a map.

- `plugins/PluginLogicTest.java` `encryptDecryptRoundTrip`, `wrongPasswordReturnsNull`.
