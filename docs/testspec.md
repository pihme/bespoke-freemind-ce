# FreeMind CE test spec

Maps every ID in `docs/spec.md` to the tests whose assertions check that behaviour. The rows below include the `TestREQ_` methods added with the 21.8% measurement. Older tests were not renamed.

A test counts only when it asserts the behaviour. Opening a menu, counting nodes, or asserting that saved XML is non-empty does not count. GUI tests that only call the same model setter as a unit test are named with that unit test, not as a separate menu check.

## How to read a row

- **Checked** — an assertion covers the behaviour the requirement states. A missing clause is named on the row.
- **Partial** — at least one clause is checked and at least one is not.
- **None** — no assertion checks the behaviour.

49 requirements: 19 checked, 30 partial, 0 none. The seven rows that were none now have a deduced test. Sort, the four edge shapes, and ctrl-wheel zoom are checked as well. Five of the former none rows stay partial because a clause the requirement names is still not asserted.

## Coverage number

Authoritative number: **83.9% instruction** (120,660 of 143,756), measured 2026-10-21. Line coverage 82.9% (29,973 of 36,153). Branch coverage 75.3% (9,371 of 12,443). The unit task ran 1,658 tests, the GUI task 524, and the performance task 9. All three passed. The report was built from the exec files of that run. 85% of 143,756 is 122,193 instructions, so 1,533 are still missing. The overshoot is 86%, about 2,970 more covered instructions. This measurement includes the behaviour tests for `freemind/common`, `freemind/preferences/layout`, `plugins/contextgraph`, and `freemind/modes/browsemode`. `freemind/modes/mindmapmode/listeners` is covered by `tests.freemind.cov.listeners`. `accessories/plugins/dialogs` is covered by `tests.freemind.cov.dialogs`. `accessories.plugins.util.xslt` is covered by `tests.freemind.cov.xslt`. `freemind.modes.common.actions` is covered by `tests.freemind.cov.commonactions`. `plugins/latex` is covered by `tests.freemind.cov.latex`. The next untouched package by missed instructions is `plugins/svg`. `freemind/controller/filter` still misses 1,732 instructions, almost all in the composer dialog, which the headless test does not show. The number just before this measurement was **81.5% instruction** (117,178 of 143,756), line 80.4% (29,051 of 36,153), branch 71.8% (8,939 of 12,443). That run's unit task had 1,520 tests and failed the `@DisplayName` bound. Its GUI task failed two tests. The measured plan that reached 81.5% was `plugins/map`, `accessories/plugins`, `freemind/main`, `freemind/view/mindmapview`, `freemind/controller`, `plugins/collaboration/socket`, `freemind/modes`, `freemind/modes/mindmapmode`, `accessories/plugins/time`, `freemind/controller/filter`, `freemind/modes/mindmapmode/actions`, and `plugins/script`. Before those package rows the number was **62.1% instruction** (89,233 of 143,756) on 2026-10-20, line 59.3% (21,426 of 36,153), branch 50.4% (6,265 of 12,443). The number just before this measurement was **78.6% instruction** (113,036 of 143,756). The previous number, which still counted generated classes, was **62.4% instruction** (91,634 of 146,900) on 2026-10-19. Earlier the same day, while generated classes were still counted: 62.0% (91,041 of 146,900), then 61.8% (90,720 of 146,900), then 60.3% (88,579 of 146,900), then 57.3% (84,212 of 146,900), then 55.7% (81,880 of 146,900), then 53.6% (78,738 of 146,900), then 53.3% (78,332 of 146,900), then 52.2% (76,701 of 146,900), then 51.3% (75,386 of 146,900), then 49.4% (72,501 of 146,900), then 42.9% (63,022 of 146,900), then 29.2% (42,834 of 146,900). After only the first seven deduced methods it was 26.5% (38,982 of 146,900). At `ef0c9af` it was 21.8% (32,021 of 146,900). At `a9db337` it was 21.3% (31,265 of 146,900). The chaos exec contains no tests.

Command. `jacocoTestReport` depends on the unit, GUI, and performance tasks and reads their exec files:

```
DISPLAY=:0 ./gradlew :freemind:test :freemind:testGui :freemind:testPerformance :freemind:jacocoTestReport --no-configuration-cache
```

HTML report: `freemind/build/reports/jacoco/test/html/index.html`. This number is not a failing CI threshold yet. Per-module numbers are not a second score.

`make coverage-report` rebuilds that report from the exec files already in `freemind/build/jacoco/` and prints the instruction, line, and branch totals. The command above is the run that executes the suites. The 83.9% figure includes jqwik and Jazzer execution from before those tests moved to `:freemind:testFuzz`.

## Test strategy

- Unit tests: `:freemind:test`, headless, excludes tags `gui` and `fuzz`. GUI tests: `:freemind:testGui`, needs a display. Performance: `:freemind:testPerformance`, tag `performance`. jqwik property tests and Jazzer fuzz tests: `:freemind:testFuzz`, tag `fuzz`. That task is outside the unit run and outside JaCoCo.
- JUnit 5 is the runner. JaCoCo is the coverage tool. No mutation tool is chosen. `select-tool` runs before any mutation campaign. Mutation stays out of the normal CI pipeline.
- Generated sources: `freemind/generated-src/` is JAXB output (`freemind.controller.actions.generated.instance`). The JaCoCo agent excludes `freemind.controller.actions.generated.instance.*`, and `jacocoTestReport` drops `freemind/controller/actions/generated/**` from class and source directories. Those classes are not in the authoritative count and are not in the test plan. The existing backwards-compatibility check `generatedActionsKeepTheValuesTheyAreGiven` stays; it is not a coverage target.
- Resource limits that drive how much is kept: REQ-DED-08 (`last_opened_list_length`, shipped 25, options 0 through 200) and REQ-DED-09 (`undo_levels`, shipped 100, options 2 through 1000). Other numeric options (wheel velocity, proxy port, delayed-selection time) change a rate or a port. They do not size a collection.
- A test that calls code and asserts nothing is not a check. `ExportTests.testExportPng` still calls `exportFileToPng` and asserts nothing. `TestREQ_FILE_03_PngExportWritesPngBytes` is the check.
- Equivalent mutants: none. Mutation has not been run.

## Clarifications during tests

| # | Requirement | Question | Decision | Commit |
|---|-------------|----------|----------|--------|
| 1 | REQ-DED-08 | Does 0 or a negative `last_opened_list_length` leave the recent-files list empty? | 0 leaves it empty. A negative value empties it and then throws `IndexOutOfBoundsException`. The earlier sentence that said both leave it empty was a wrong deduction. The production code stays. | this change |
| 2 | REQ-DED-09 | Does 0 or a negative `undo_levels` leave the undo list empty? | The loop has the same shape. 0 leaves it empty. A negative value throws `IndexOutOfBoundsException` once the list is empty. The limit runs only for a pair stored as its own entry, not for a pair folded into the previous one. The production code stays. The test is still open. | this change |

REQ-STYLE-02 says the underline menu entry is commented out as not implemented in the view. `NodeAdapterTest.setUnderlinedChangesState` and `FormatMenuGuiTest.format_underline` still set the model flag. That stays a note on the row until you decide otherwise.

## Deduced requirements

The IDs are defined in `docs/spec.md` under **Deduced requirements**. A row there is not a product decision. The tests below are labeled **Deduced** because the observable result was inferred from the current program.

## Deduced missing tests

Labeled **Deduced**. Each row is a test the requirement sentence does not spell out, inferred from the code that implements it. A test counts only when it asserts the result. Status is updated when that assertion has passed.

| ID | Deduced missing test | Test | Status |
|----|----------------------|------|--------|
| REQ-DED-01 | Two leaf nodes join into the selected node's text with one space, and the other node is gone. | `DeducedBehaviourTest.TestREQ_NODE_07_JoinLeavesIntoSelectedText` | passed |
| REQ-DED-02 | Find selects the first case-insensitive word match. Find next selects the later match. | `DeducedBehaviourTest.TestREQ_FIND_01_FindThenFindNext` | passed |
| REQ-DED-03 | A folder import adds a named child per directory and file, with a file URL link. A directory that contains a file is folded. An empty directory stays unfolded. A non-root target that received children is folded. The root stays unfolded. | `DeducedBehaviourTest.TestREQ_FILE_06_ImportFolderAddsNamedChildren` | passed |
| REQ-DED-04 | A node whose text is an `<img>` is shown with a `<base href>` of the map. The stored text stays without that base tag. | `DeducedBehaviourTest.TestREQ_NODE_08_ImageHtmlGainsBaseHrefInTheView` | passed |
| REQ-DED-05 | A new LaTeX hook reads back `E = mc^{2}`, saves that equation, and loads a replacement. | `DeducedBehaviourTest.TestREQ_PLUG_05_LatexHookStoresEquation` | passed |
| REQ-DED-06 | Registering the English dictionary from `dictionaries.cnf` loads a dictionary whose locale language is `en`. | `DeducedBehaviourTest.TestREQ_PLUG_07_EnglishDictionaryLoads` | passed |
| REQ-DED-07 | The help set file loads. Its title contains `FreeMind Help` and its home id is `top`. The help window is not opened. | `DeducedBehaviourTest.TestREQ_PLUG_04_HelpSetLoads` | passed |
| REQ-DED-08 | The recent-files list keeps the shipped 25, one under that, drops the oldest past the limit, stays empty at 0, throws `IndexOutOfBoundsException` at -1 after emptying, and a value past the `int` range leaves the limit at 25. | `LastOpenedListBehaviourTest.TestREQ_DED_08_ShippedLimitKeepsTwentyFiveAndDropsTheOldest`, `TestREQ_DED_08_ZeroKeepsTheListEmpty`, `TestREQ_DED_08_NegativeThrowsAfterEmptyingTheList`, `TestREQ_DED_08_ValuePastIntRangeLeavesTheLimitAtTwentyFive` | passed |
| REQ-DED-09 | The undo list keeps the shipped 100, one under that, drops the oldest past the limit, stays empty at 0, throws `IndexOutOfBoundsException` at -1 after emptying, and a value past the `int` range leaves the limit at 100. Pairs inside 100 ms count as one and do not apply the limit. | not in this change | open |
| REQ-DED-10 | Remembered map states keep 49 and 50, drop the oldest at 51, and an unreadable store has no entry. | `LastStateStorageManagementBehaviourTest.TestREQ_DED_10_FiftyStatesAreKeptAndTheOldestIsDropped` | passed |
| REQ-NODE-04 | Paste of a string leaves that text on a new child. Copy and paste as plain text are still open. | `DeducedBehaviourTest.TestREQ_NODE_04_PasteLeavesThePastedText` | passed. Included in the 29.2% measurement. |
| REQ-NODE-03 | The undo menu stack restores the previous node text. | `DeducedBehaviourTest.TestREQ_NODE_03_UndoMenuRestoresPreviousText` | passed. Included in the 29.2% measurement. |
| REQ-NODE-06 | Cut then paste, which is what a move-drop does, moves the node under the new parent. The drop listener is not driven. | `DeducedBehaviourTest.TestREQ_NODE_06_CutThenPasteMovesNodeToNewParent` | passed. Included in the 29.2% measurement. |
| REQ-NODE-09 | `SortNodes.invoke` reorders the children on the map. | `DeducedBehaviourTest.TestREQ_NODE_09_SortInvokeReordersChildren` | passed. Included in the 29.2% measurement. |
| REQ-STYLE-07 | Each of the four edge styles is stored and the node view paints ink. Color and width are stored. | `DeducedBehaviourTest.TestREQ_STYLE_07_FourEdgeShapesPaint` | passed. Included in the 29.2% measurement. |
| REQ-STYLE-08 | Ctrl plus the mouse wheel reduces the zoom. | `DeducedBehaviourTest.TestREQ_STYLE_08_CtrlWheelChangesZoom` | passed. Included in the 29.2% measurement. |
| REQ-MAP-02 | Revert restores the last saved file. | not in this change | open |
| REQ-FILE-02 | PDF export writes a PDF and the options dialog is still open. | not in this change | open |
| REQ-PLUG-01 | The script editor is still not opened. | not in this change | open |
| REQ-PLUG-02 | A node stores a geographic position. | not in this change | open |
| REQ-APP-01 | The options panel reads and writes a preference. The dialog is still not opened. | not in this change | open |
| REQ-APP-02 | Print preview shows a page. | not in this change | open |
| REQ-MODE-01 | Browse mode is read-only and File mode lists a directory. | not in this change | open |

The file chooser for an image, the join warning dialog, and the find dialog are not what the deduced tests call. `FreeMindBehaviourTest.lookAndFeelBranchesAndSpellCheckRegister` calls `setupSpellChecking` with the preference on, and that call leaves the current spell locale unchanged. That observation is backwards compatibility. It does not change REQ-DED-06.

## Backwards compatibility tests

Labeled **Backwards compatibility**. This is the umbrella for code that no requirement names. The slices and the rule are in `docs/spec.md` under **Backwards compatibility tests**. A backwards-compatibility assertion does not replace a requirement test. Time-management calendars stay in this table. They are not a product requirement.

Slices 1 and 2 also have deduced requirement tests. This table is the reference for every backwards-compatibility test. Every row in this table except the `freemind.main`, `plugins/map`, `freemind.view.mindmapview`, `accessories.plugins`, `freemind.controller`, `plugins.collaboration.socket`, `freemind.modes`, `freemind.modes.mindmapmode`, `accessories.plugins.time`, `freemind.controller.filter`, `freemind.modes.mindmapmode.actions`, `plugins.script`, `freemind.common`, `freemind.preferences.layout`, `plugins.contextgraph`, and `freemind.modes.browsemode` package rows was included in the 62.4% measurement. Those package rows are not in the 62.1% measurement. Slice 5 lists a file in a temporary directory, builds the Linux file chooser, searches a directory of `.mm` files, paints a print-preview page, and exports an image of the open map. It still has no backwards-compatibility test for browse read-only, a script that runs, PDF bytes, the help window, or a checked word. A rendered LaTeX equation is covered by `tests.freemind.cov.latex`. The print-preview window is not opened.

| Slice | What is checked | Test | Status |
|-------|-----------------|------|--------|
| 6 | A time-calendar widget keeps the year, month, and day it was given. | `BackwardsCompatibilityTest.calendarKeepsTheDayThatWasSet` | passed |
| 2 | Folding a node hides its child views. The model child text stays. | `BackwardsCompatibilityTest.foldedNodeHidesItsChild` | passed |
| 1 | A note, an icon add/remove, and a link stay on the node. | `BackwardsCompatibilityTest.noteIconAndLinkStayOnTheNode` | passed |
| 1 | Moving a node up reorders the children. | `BackwardsCompatibilityTest.movingANodeUpReordersTheChildren` | passed |
| 1 | A pattern copies bold, italic, and color. The intersection of a bold node and a plain node drops the bold property. Applying the pattern makes the plain node bold. | `BackwardsCompatibilityTest.patternCopiesBoldItalicAndColor` | passed |
| 2 | An arrow link stores red. After layout, both inclinations are set and the inner bounds are positive. The bitmap is not asserted. | `BackwardsCompatibilityTest.arrowLinkPaintsRedBetweenTwoNodes` | passed |
| 2 | A red cloud paints red pixels around the node. | `BackwardsCompatibilityTest.cloudPaintsRedAroundTheNode` | passed |
| 1 | HTML export writes `<html>` and escapes `ä` as `&auml;`. | `BackwardsCompatibilityTest.htmlExportWritesTheNodeText` | passed |
| 6 | An encrypted node hides its child until the password matches. A wrong password does not. | `BackwardsCompatibilityTest.encryptedNodeHidesTheChildUntilThePasswordMatches` | passed |
| 6 | A table sorter orders rows ascending, then descending. | `BackwardsCompatibilityTest.tableSorterOrdersRows` | passed |
| 4 | `C+ENTER` is key Enter with the modifier word `control`. Tab is consumed. Control-space is a space. Typed `B` stays `B`. `SPACE` parses as a space. An unknown key name is rejected. | `BackwardsCompatibilityTest.keyTranslatorReadsControlEnter` | passed |
| 6 | A clone plugin reads the stored clone ids `alpha,beta,`. | `BackwardsCompatibilityTest.clonePluginReadsStoredCloneIds` | passed |
| 3 | A map position round-trips latitude, longitude, center, zoom, and the Mapnik tile source, and the tooltip HTML contains the position mark and an `http` URL. No tile is downloaded. | `BackwardsCompatibilityTest.mapPositionRoundTripsAndBuildsATileTooltip` | passed |
| 1 | An XML element keeps the umlaut attribute `Käse` through a write and a second parse. | `BackwardsCompatibilityTest.xmlElementKeepsAnUmlautAttribute` | passed |
| 1 | Text comparison, placeholders, extensions, absolute paths, a restorable path, font scaling, icon indexes, and an empty compound action print. A restorable `MindMap:/tmp/file.mm` yields the path `/tmp/file.mm`. | `BackwardsCompatibilityTest.toolsComparePathsPlaceholdersAndIconIndexes` | passed |
| 1 | Integer, double, and boolean attributes, a named integer mapping, and a non-numeric integer throw `XMLParseException`. | `BackwardsCompatibilityTest.xmlElementReadsTypedAttributes` | passed |
| 6 | A pattern file loads the name `Marked`, recursive, red, and bold. | `BackwardsCompatibilityTest.stylePatternLoadsANamedBoldPattern` | passed |
| 1 | Splitting `AlphaBeta` at index 5 leaves `Alpha` on the node and adds `Beta`. | `BackwardsCompatibilityTest.splittingAChildKeepsBothHalves` | passed |
| 1 | Clearing a node drops the note and the bold face, and the color is no longer red. | `BackwardsCompatibilityTest.clearingANodeDropsTheNoteAndTheBoldFace` | passed |
| 1 | Blending a red node toward a white map background stores the 3:1 mix. | `BackwardsCompatibilityTest.blendingANodeColorMovesItTowardTheBackground` | passed |
| — | An optional split pane holds one panel, then a split pane with two, and returns to one. Index 2 is rejected. | `BackwardsCompatibilityTest.optionalSplitPaneHoldsOnePanelThenTwo` | passed |
| 2 | A continuous selection keeps Alpha and Gamma, and the selected nodes are ordered by Y. | `BackwardsCompatibilityTest.continuousSelectionKeepsTheSiblingsBetween` | passed |
| 1 | Find matches a note when the node text does not, misses an absent term, and splits `alpha "beta gamma" delta` into three terms. | `BackwardsCompatibilityTest.findActionFindsANoteAndSplitsAQuotedTerm` | passed |
| — | A file filter accepts a directory and `map.mm`, rejects `notes.txt`, and describes `.mm`. | `BackwardsCompatibilityTest.fileFilterAcceptsAMindMapExtension` | passed |
| — | A sorted map vector keeps keys in order, ignores a duplicate key, and shrinks on remove. | `BackwardsCompatibilityTest.sortedMapVectorKeepsKeysInOrder` | passed |
| 6 | Raising a child of the root moves it to the other side. | `BackwardsCompatibilityTest.raisingAChildChangesItsSide` | passed |
| 6 | New parent wraps the selected child. The new node is not named `Wrapped`. | `BackwardsCompatibilityTest.newParentWrapsTheSelectedChild` | passed |
| 6 | The split hook breaks `Alpha` and `Beta` on a newline. | `BackwardsCompatibilityTest.splitHookBreaksANodeOnANewline` | passed |
| 6 | Hierarchical icons collect the child icon `button_ok` onto the parent state icon. | `BackwardsCompatibilityTest.hierarchicalIconsCollectTheChildIcon` | passed |
| 6 | Format paste copies bold from one node onto another. | `BackwardsCompatibilityTest.formatPasteCopiesBoldOntoAnotherNode` | passed |
| 6 | Unfold all opens a folded child, then fold all folds it again. | `BackwardsCompatibilityTest.unfoldingAllOpensAFoldedChild` | passed |
| 6 | After the revision plugin is on, editing a node sets its background to yellow. | `BackwardsCompatibilityTest.revisionPluginColorsAnEditedNode` | passed |
| 6 | Automatic layout paints the root black and the child `#0033ff`. | `BackwardsCompatibilityTest.automaticLayoutAppliesTheDepthColor` | passed |
| 6 | Creation/modification stores a tooltip that starts with `created `. | `BackwardsCompatibilityTest.creationModificationStoresATooltip` | passed |
| 6 | HTML export with folding writes `fold_document`, a fold button, the icon, a `.mm.html` link, and bold. Heading export writes `<h1>` and the root text. | `BackwardsCompatibilityTest.htmlExportWithFoldingWritesTheScriptAndAHeading` | passed |
| 1 | Applying a pattern directly sets the text, red, green, bubble, a blue bezier edge of width 4, `button_ok`, size 20, bold, italic, strikethrough, and a serif family. A child pattern named `ChildMark` sets the child text to `FromChild`. A blank font size and font name restore the default font. Clearing the icon, text, width, and font name empties the text and the icons, stores the default edge width, and the visible width is thin. | `BackwardsCompatibilityTest.directPatternApplySetsFontEdgeIconAndText` | passed |
| 6 | Saving a pattern named `Marked` with red and loading it back keeps the name and the color. | `BackwardsCompatibilityTest.savedPatternRoundTripsTheNameAndTheColor` | passed |
| 1 | Pasting a copy of `Alpha` adds a second `Alpha` with a different id. | `BackwardsCompatibilityTest.pastingACopyAddsTheChildAgain` | passed |
| 1 | Joining the leaves `Alpha` and `Beta` stores `Alpha Beta` and leaves one child. | `BackwardsCompatibilityTest.joiningLeavesConcatenatesTheirText` | passed |
| 1 | Deleting `Beta` leaves `Alpha`. | `BackwardsCompatibilityTest.deletingAChildRemovesIt` | passed |
| 1 | Undo restores `Alpha` after the text was set to `Changed`, and redo puts `Changed` back. | `BackwardsCompatibilityTest.undoRestoresThePreviousText` | passed |
| 1 | Moving a node stores parent v-gap 12, h-gap 30, and shift 7. | `BackwardsCompatibilityTest.movingANodeStoresItsGaps` | passed |
| 1 | An attribute `Owner` changes from `Ada` to `Grace` and is removed. Removing every icon leaves none. | `BackwardsCompatibilityTest.anAttributeRoundTripsOnTheNode` | passed |
| 1 | A shallow copy keeps the text `Original` and does not keep the note. | `BackwardsCompatibilityTest.aShallowCopyKeepsTheText` | passed |
| 2 | Arrow ends stay at (1, 2) and (3, 4). The start head is `Default`, the end head is `None`, and removing the link leaves none. | `BackwardsCompatibilityTest.arrowEndsAndArrowHeadsStayOnTheLink` | passed |
| 2 | Selecting the root branch keeps `Alpha` and `Beta` selected. A link point is non-null. Making `Beta` the selection and toggling it removes it. | `BackwardsCompatibilityTest.selectingABranchKeepsTheChildSelected` | passed |
| 1 | An XML element reads the CDATA section `Alpha & Beta`. | `BackwardsCompatibilityTest.xmlElementReadsACdataSection` | passed |
| 6 | JSON, YAML, Markdown, XML, and the clean JSON, YAML, and XML writers include the node text `Alpha`. JSON also includes the link, the note, `button_ok`, `Gamma`, bold, a red cloud, `Ada`, and a linear edge. YAML includes the cloud and `Ada`. | `BackwardsCompatibilityTest.contextGraphExportsWriteTheNodeText` | passed |
| 2 | An arrow popup has at least six components. A node has no arrow popup. | `BackwardsCompatibilityTest.arrowPopupListsTheLinkActions` | passed |
| 5 | File mode lists `listed.mm` from a temporary directory. The file node is not writeable. | `BackwardsCompatibilityTest.fileModeListsADirectory` | passed |
| 2 | A map fitted to one page prints page 0 and rejects page 1. Two pages wide still prints page 0 and rejects page 40. With fit turned off, page 0 still prints. | `BackwardsCompatibilityTest.printingAFittedPageExistsAndLaterPagesDoNot` | passed |
| 2 | Right from the root selects a child. Shift-down keeps at least two nodes selected. | `BackwardsCompatibilityTest.keyboardMoveSelectsTheNextNode` | passed |
| — | Country codes keep the last value: `en` is US, `es` is MX, `it` is IT, and the map has 14 entries. | `BackwardsCompatibilityTest.countryMapKeepsTheLastCode` | passed |
| — | Colors, points, booleans, compression, page size, generated ids, changed properties, and the node hierarchy round-trip. A proposed file name drops `:/`. | `BackwardsCompatibilityTest.toolsRoundTripValues` | passed |
| 2 | Linear, bezier, sharp linear, and sharp bezier edges of width 4 use those painters, and the inner bounds stay non-zero. | `BackwardsCompatibilityTest.edgeStylesUseTheMatchingPainter` | passed |
| 6 | Sorting the children `Alpha`, `Zebra` reverses them to `Zebra`, `Alpha`. | `BackwardsCompatibilityTest.sortingChildrenReversesAlphabeticalOrder` | passed |
| 1 | Importing a favorites folder adds `Example` with `https://example.test/fav` and drops the empty directory. | `BackwardsCompatibilityTest.explorerFavoritesImportAUrl` | passed |
| 1 | Plain text split at 5 stays `Alpha` and `Beta`. HTML split at 5 keeps `Alph` in the first half and `Beta` in the second. | `BackwardsCompatibilityTest.htmlContentSplitsIntoTwoHalves` | passed |
| 1 | Saving a node writes the text, the cloud, `#ff0000`, `sharp_bezier`, the link, `button_ok`, `Ada`, and bold. | `BackwardsCompatibilityTest.savingAStyledNodeWritesItsParts` | passed |
| 2 | Scrolling a 900 by 700 view inside a 400 by 300 viewport by 12, 18 moves the origin there. Zoom 1.5 stays. A point at (-1000, -1000) hits no arrow. | `BackwardsCompatibilityTest.scrollingAViewportMovesTheOrigin` | passed |
| — | The standard file chooser on this Linux host is not the AWT dialog. | `BackwardsCompatibilityTest.theLinuxFileChooserIsTheSwingDialog` | passed |
| 2 | The node key listener sends Right to the map and leaves the root selected when Alt is held. | `BackwardsCompatibilityTest.nodeKeyListenerForwardsAnArrow` | passed |
| 1 | Closing a map with force stores `bespoke-close` in the last-place property. | `BackwardsCompatibilityTest.closingAMapWithForceStoresTheLastPlace` | passed |
| 5 | Searching a directory finds `notes.mm` for the word `token`. | `BackwardsCompatibilityTest.searchingAMindMapFileFindsTheNodeText` | passed |
| 6 | A pattern named `Rich` keeps green, blue, bubble, `Hello`, `button_ok`, Serif 20 bold italic, a red bezier edge of width 4, and itself as the child pattern. | `BackwardsCompatibilityTest.aRichPatternKeepsEdgeFontIconAndChild` | passed |
| 2 | Clickable image HTML contains an area, `Alpha`, and `https://example.test/a`. | `BackwardsCompatibilityTest.clickableImageAreasNameTheNodeAndTheLink` | passed |
| 4 | The key workaround drops a dead key, Alt, a letter, and a numpad digit, and it keeps Enter. | `BackwardsCompatibilityTest.keyWorkaroundDropsModifiersAndKeepsEnter` | passed |
| 1 | XML escaping turns a leading space and a tab into `&nbsp;`, keeps the space in `a <`, encodes `<`, and drops an illegal `&#x0B;` character. | `BackwardsCompatibilityTest.htmlEscapesWhitespaceAndDropsIllegalCharacters` | passed |
| 1 | Text export writes the link, `Folder`, and the folded child `SkipMe`. RTF writes bold, italic, strike, color 3, the escaped backslash, and `\\u228?`. | `BackwardsCompatibilityTest.textAndRtfExportWriteTheNodeTheLinkAndTheStyle` | passed |
| 1 | Making `Beta` the root puts `Alpha` under it and `ROOT` under `Alpha`. | `BackwardsCompatibilityTest.changingTheRootReparentsTheOldChain` | passed |
| 1 | A custom tooltip entry stays `kept` for an `https` link and for a missing `.mm` link. | `BackwardsCompatibilityTest.aMindMapLinkTooltipKeepsACustomEntry` | passed |
| 2 | The first and last child views are `Alpha` or `Beta`. A blue background stays blue. Dragging over the node paints a pixel that is not white. | `BackwardsCompatibilityTest.nodeViewNamesItsFirstChildAndPaintsADragHint` | passed |
| 1 | A double attribute round-trips `1.25` and `2.5`. The mapped value of `Zoom` is `1`. An unknown literal is rejected. The mapped double of `zoom` is `7.0`. | `BackwardsCompatibilityTest.xmlDoubleAttributesRoundTrip` | passed |
| 1 | Two `Owner=Ada` attributes are equal and share a hash code. A different value does not. | `BackwardsCompatibilityTest.equalAttributesShareAHashCode` | passed |
| — | `/tmp/file.mm` is absolute. The relative URL of `a.mm` is `a.mm`. A file in a child directory is `sub/b.mm`, and a file beside the parent directory is `../b.mm`. A missing file reads as null. Split-pane F6 and F8 are removed and put back. | `BackwardsCompatibilityTest.pathsRelativeUrlsAndSplitPaneKeysRoundTrip` | passed |
| 1 | Attribute filters both match and miss `Owner=Ada`. | `BackwardsCompatibilityTest.attributeFiltersMatchTheStoredOwner` | passed |
| 6 | Lowering `Beta` moves it under `Alpha`. | `BackwardsCompatibilityTest.loweringANodeMovesItUnderItsSibling` | passed |
| 6 | Raising grandchild `Beta` places it beside `Alpha`. | `BackwardsCompatibilityTest.raisingAGrandchildMovesItBesideItsParent` | passed |
| 6 | Splitting two HTML paragraphs keeps `Alpha` and `Beta` and leaves at least two children. | `BackwardsCompatibilityTest.splittingHtmlParagraphsKeepsBothParts` | passed |
| 1 | Saving the map writes `<map` and `Alpha`. | `BackwardsCompatibilityTest.savingTheMapWritesTheNodeText` | passed |
| 6 | YAML export of an arrow lists `_references:` and `Beta`. A numeric note becomes `B A` and keeps `nope`. | `BackwardsCompatibilityTest.yamlExportListsAnArrowTarget` | passed |
| — | A UTF-8 BOM still reads `Ab`, a UTF-16BE BOM reads `B`, a UTF-16LE BOM reads `C`, a UTF-32BE BOM reads `D`, and a UTF-32LE BOM reads `E`. Text without a BOM reads `Hi`. | `BackwardsCompatibilityTest.unicodeReaderKeepsTheTextBehindTheBom` | passed |
| — | Generated actions keep the string, number, and boolean values they are given. | `BackwardsCompatibilityTest.generatedActionsKeepTheValuesTheyAreGiven` | passed |
| 2 | A plain click folds `Alpha` and leaves the child text `Child`. | `BackwardsCompatibilityTest.plainClickFoldsANodeThatHasAChild` | passed |
| 2 | Ctrl-click keeps `Alpha` selected, and Alt-click keeps that branch selected. | `BackwardsCompatibilityTest.extendSelectionKeepsTheClickedNode` | passed |
| 2 | Changing an arrow inclination moves its start off the original point. | `BackwardsCompatibilityTest.changingAnArrowInclinationMovesItsStart` | passed |
| 6 | Creating the hierarchical-icons hook attaches it, and removing the hook drops it. | `BackwardsCompatibilityTest.creatingAHierarchicalIconHookAttachesIt` | passed |
| 1 | Pasting three lines adds `example.test`, `Nested`, and `ada@example.test`. | `BackwardsCompatibilityTest.pastingLinesAddsALinkAChildAndAnAddress` | passed |
| 6 | Adding the hierarchical-icons hook copies the child icon `idea` onto the root, and removing the hook drops that icon. | `BackwardsCompatibilityTest.addingHierarchicalIconsCopiesTheChildIcon` | passed |
| 1 | Pasting a mind-map node adds `PastedNode`. Pasting HTML adds `LinkText`. Pasting a file adds its name. Pasting an image adds a `.jpeg` image node. | `BackwardsCompatibilityTest.pastingMindMapHtmlFileAndImageAddsNodes` | passed |
| 1 | Pasting the XML node `XmlPaste` keeps that text. | `BackwardsCompatibilityTest.pastingXmlKeepsTheNodeText` | passed |
| 1 | A shallow copy of `Alpha` keeps its link and drops the child. | `BackwardsCompatibilityTest.aShallowCopyKeepsTheLinkAndDropsTheChild` | passed |
| 2 | Right from the root selects `Alpha` or `Beta`. Down, up, left, page down, and shift-down keep a node selected. | `BackwardsCompatibilityTest.arrowKeysWalkSiblingsAndAPage` | passed |
| 2 | A point on a painted arrow is a hit, and a point 500 pixels away is not. | `BackwardsCompatibilityTest.anArrowReportsAHitOnItsCurve` | passed |
| 6 | Clean JSON of an arrow lists `_references` and `Beta`, and a numeric note becomes `Aé Bé`. | `BackwardsCompatibilityTest.cleanJsonListsAnArrowAndANumericNote` | passed |
| 6 | A clone of `Alpha` and `Beta` reports `ChildB` for `ChildA`, and reports `Beta` for `Alpha`. This is not a product requirement. | `BackwardsCompatibilityTest.cloneRegistrationFindsTheMatchingChild` | passed |
| 6 | The day chooser keeps 15 June 2020. A right arrow fires day 16 and leaves the selected day at 15 until `setDay`. Day 0 becomes 1 and day 99 becomes the last day of June. December steps into January of the next year. This is not a product requirement. | `BackwardsCompatibilityTest.dayChooserKeepsTheFifteenthAndStepsRight` | passed |
| 2 | The two contrary colors of blue are different from blue and from each other. | `BackwardsCompatibilityTest.contraryColorsLeaveTheOriginalHue` | passed |
| — | A list keeps `Alpha` before `Beta`. A sorted model keeps `Ada` first. A missing map key is rejected. | `BackwardsCompatibilityTest.listModelsKeepInsertedValuesInOrder` | passed |
| 6 | A blue tile image round-trips and then draws a red cross. This is not a product requirement. | `BackwardsCompatibilityTest.tileImageRoundTripsABlueSquare` | passed |
| 2 | A scalable icon paints a green rectangle at twice the file size. | `BackwardsCompatibilityTest.scalableIconPaintsAtTwiceTheSize` | passed |
| 2 | The box of a bent curve is wider than the ends and taller than a flat line. | `BackwardsCompatibilityTest.aCurveBoxContainsTheBend` | passed |
| 2 | A folded bubble node paints ink. | `BackwardsCompatibilityTest.bubbleStylePaintsARoundedNode` | passed |
| 2 | An existing HTML head is replaced by the configured head. An image node keeps a base URL and `pic`. A long body gains a width. A table keeps `One` and `Two`. Two lines stay in HTML. | `BackwardsCompatibilityTest.aLongHtmlNodeAndATableKeepTheirText` | passed |
| 6 | Unfolding opens `Alpha`, stage 1 folds it, and folding the last branches folds `Alpha` again. The root stays open. This is not a product requirement. | `BackwardsCompatibilityTest.unfoldingReopensAFoldedBranch` | passed |
| 2 | The `idea` icon is added, removed, and added again with shift. | `BackwardsCompatibilityTest.anIconActionAddsAndRemovesTheIdeaIcon` | passed |
| 6 | A node filter that contains `Alpha` hides `Beta` and keeps the root. This is not a product requirement. | `BackwardsCompatibilityTest.aNodeFilterHidesBeta` | passed |
| 6 | A new node copies the stored color `#0000ff`. A fold action is left unchanged. This is not a product requirement. | `BackwardsCompatibilityTest.formattingNewNodesCopiesThePreviousFormat` | passed |
| 6 | The edit before `Beta` is `Alpha`. A fold and the jump hook itself are not stored. This is not a product requirement. | `BackwardsCompatibilityTest.theLastEditLocationIsThePreviousNode` | passed |
| 2 | Fitting a 500 by 400 map into a smaller viewport lowers the zoom from 2. | `BackwardsCompatibilityTest.fittingTheMapChangesTheZoom` | passed |
| 5 | A print preview paints ink on a page. | `BackwardsCompatibilityTest.aPrintPreviewPaintsAPage` | passed |
| 2 | Bold, italic, strike, and the underline flag stay on the selected node. The font becomes size 22 and a serif family. A cloud stores cyan. The edge is bezier width 8. The style is bubble. Blending changes the color. Rich text and plain text both keep Alpha. Icons go from one to none. A green pattern stores that color. Folding Mid folds it, and toggling Alpha's children opens Mid again. Gamma moves up and its shift returns to 0. Selecting the branch and then every node keeps at least three selected. The move cursor is set and then cleared. Underline here is the model flag. | `BackwardsCompatibilityTest.formatActionsChangeTheSelectedNode` | passed |
| 1 | A version 1.1.0 map keeps black text, a white background, fork style, the link, shift 3, gaps 4 and 5, the history times, bold italic strikethrough, a thin red bezier edge, a green cloud, the idea icon, attribute Owner, the note Remember, a folded left child, the grandchild, an arrow to Child, and a substitute for an unknown hook. | `BackwardsCompatibilityTest.aRichMapKeepsEdgeCloudArrowFontAndAttributes` | passed |
| 6 | A present export file is copied and a missing file is skipped. A PNG filter accepts a directory and `pic.PNG` and rejects `pic.gif`. This is not a product requirement. | `BackwardsCompatibilityTest.aPresentExportFileIsCopiedAndAMissingOneIsSkipped` | passed |
| 6 | A spin field clamps 99 to 10 and -3 to 1, then clamps 1 to the minimum 2. A card layout uses the visible child's height and ignores the hidden child. The spin field is not a product requirement. | `BackwardsCompatibilityTest.aSpinFieldClampsAndACardShowsTheVisibleSize` | passed |
| 6 | Calendar repetitions from 15 June 2020 mark that day, return the same marking from the cache, miss 1990, and an unknown repeat type yields no entries. This is not a product requirement. | `BackwardsCompatibilityTest.calendarRepetitionsStepForwardFromJune` | passed |
| 5 | An exported image of the open map contains ink. | `BackwardsCompatibilityTest.anExportImageContainsTheMap` | passed |
| 1 | A plain link stays the URL. A link to a node id contains that node's text. A missing id does not. Replacing `Alpha` inside `<b>` keeps the tag and writes `Beta`. An or-filter matches `Alpha` and `Beta` and misses the root. An and-filter misses `Alpha`. A hovered fold mark on a red edge paints red. | `BackwardsCompatibilityTest.aShortLinkAFoldMarkAndAnOrFilterStayVisible` | passed |
| 6 | A number property keeps 5 and resets 4, 99, and `nope` to 1. A remind value shows OK, Cancel, and Ask. An integer combo snaps 5 to 4 and resets 9 and `nope` to 2. A color swatch paints a black border and a red center. A tall pane shows a vertical bar and a short pane hides it. A content pane lays out the visible child. The eclipsed edge stroke is one dashed stroke. A transferable log contains `Alpha`. A typed `a` runs the matching action and is consumed. `/tmp/alpha` is absolute and its prefix is `/`. A same-host URL stays relative to `b.mm`. A different host stays absolute. `ababa` contains three `a`s, and `aaa` contains `aa` once. The FreeMind base path is not blank. A spin field accepts 5 and leaves 99 and `x` red at 5. A calendar keeps the German locale with week numbers. A triple calendar keeps 15 June 2020, 3 August 2020, and 2 January 2024. Dead grave, alt, a numpad digit, a control character, and a late newline are swallowed. This is not a product requirement. | `BackwardsCompatibilityTest.aSpinFieldASwatchAndAKeyWorkaroundStayInRange` | passed |
| — | A version string `1.2.3` stays a release. `Alpha`, `Beta`, and `RC` keep their numbers. Two tokens and the type `Gamma` are rejected. An unknown stored type is rejected. This is not a product requirement. | `BackwardsCompatibilityTest.aVersionStringKeepsItsNumbersAndItsType` | passed |
| — | A pasted arrow whose source stayed on the map is re-added when the target id is in the paste, and doubled when the stored link's target id is not. A source that is also in the paste is left for that source. A missing source, a missing destination, and a missing stored link are left unrestored. An arrow whose destination stayed on the map keeps that id. An arrow whose destination is in the paste is retargeted. This is not a product requirement. | `BackwardsCompatibilityTest.aPastedArrowKeepsTheEndThatStayedOnTheMap` | passed |
| — | An arrow with no visible ends has empty bounds and misses a point. A thin arrow uses a 1-pixel stroke. A visible source with no target stores a start inclination of (30, 0) and paints red. A visible target with no source stores an end inclination and paints red. Two visible ends keep the inclinations they were given, paint red, and the curve hits its start. This is not a product requirement. | `BackwardsCompatibilityTest.anArrowPaintsFromTheEndThatIsVisible` | passed |
| — | Typing `a` asks to edit and leaves the node text. Control-A does not ask. Home, end, and backspace ask to edit and leave the text. With the shipped move letters, E selects the previous sibling, D the next, S the parent, and F the child. This is not a product requirement. | `BackwardsCompatibilityTest.aTypedKeyAsksToEditAndAMoveLetterChangesTheSelection` | passed |
| — | A JSON export of a folded HTML node keeps `A & B`, the tags, two icons, bubble, italic, blue, yellow, two attributes, `E = mc^{2}`, and the child. Quotes, slashes, and control characters in the note are escaped. A null value is the word null. This is not a product requirement. | `BackwardsCompatibilityTest.aJsonExportKeepsFoldedHtmlStyleAndEscapesText` | passed |
| — | A clone filter copies a fold, a new node, a paste, an undo paste, a move, and a hook onto the other clone. A move rewrites the cloned member and leaves a stranger id. A fold of the clone itself is copied only when the clone includes itself; a move, a delete, and a cut of that clone are not copied. Selecting the clone marks the other clone, focusing the root clears that mark, and focusing the child marks the other child with the clone icon until focus is lost. A saved clone list keeps the other node, drops a missing id, and stores `CLONE_saved`. This is not a product requirement. | `BackwardsCompatibilityTest.aCloneFilterCopiesTheActionOntoTheOtherClone` | passed |
| 1 | A style pattern from a blue bold node applies to the node, the font, and the edge, and not to an icon. An edge clone keeps its own color. An arrow clone keeps the source inclination. A bad end-arrow name stays none. A clean XML export writes `B`, `A`, `~>`, `Beta`, and the escaped token `&amp;#nope;`. Stage 1 folds `Alpha`, and stage 2 opens it. A missing `.mm` link adds no preview image, and an `https` link drops the preview. The open file is already opened. The map paints ink. Equal `alpha` matches `Alpha` and not `Beta`. Greater-than `M` matches neither. The idea icon is contained on `Alpha` and not on `Beta`. Saved filters reload as two conditions. Removing hierarchical icons leaves no hooks. Joining HTML `Gamma` and plain `Delta` keeps both texts and drops `Delta`. Undo keeps `Renamed`. Enter, escape, delete, space, insert, and tab do not edit. Down and shift-left keep a selection. The next and previous map stay on `Renamed`. | `BackwardsCompatibilityTest.aFoldedStageAJoinedHtmlNodeAndAClonedArrowStayOnTheMap` | passed |
| 4 | The color combo selects the entry named `red` and renders its display name. | `StartupBehaviourTest.colorComboSelectsRedByName` | passed |
| 6 | The calendar-marking dialog offers `Never` as the first repetition. This is not a product requirement. | `StartupBehaviourTest.calendarMarkingDialogOffersNever` | passed |
| 6 | The time list shows the root text `New Mindmap` and closes. The filter field is not typed into. | `StartupBehaviourTest.timeListShowsEveryNodeAndCloses` | passed |
| 6 | Time management appends a date to the selected root and closes. The reminder path is not used. | `StartupBehaviourTest.timeManagementAppendsTheDateAndCloses` | passed |
| 6 | The password dialog keeps `secret` when OK is pressed. A short password is not submitted. | `StartupBehaviourTest.passwordDialogKeepsALongEnoughPassword` | passed |
| 6 | The format dialog shows `Change format of nodes`, OK, and Cancel, and the result stays cancel. It is disposed. The close path is not called. | `StartupBehaviourTest.formatDialogShowsTheShippedTitleAndCancels` | passed |
| 1 | The inline editor commits `Renamed root` when Enter is delivered to the field's key listener. | `StartupBehaviourTest.inlineEditorCommitsTheFieldTextOnEnter` | passed |
| — | The classes in `freemind.main` keep the results named by each `*BehaviourTest` in `tests.freemind.cov.main`. Each of those tests is labeled backwards compatibility. Turning spelling on leaves the current spell locale unchanged. A current-version map loads from the startup event. A dialog beside an on-screen component sits to its right and below it, and a component in the screen inset is clamped to that inset. Methods that exit the process or run a system command are in the uncoverable table. | `tests.freemind.cov.main.*BehaviourTest` | passed |
| — | The classes in `plugins/map` keep the results named by each `*BehaviourTest` in `tests.freemind.cov.map`. Each of those tests is labeled backwards compatibility. `MapDialog.startupMapHook` and `FreeMindMapController.AddSearchResultsToMapTask`'s constructor still throw before a dialog is shown when the suite is headless. They are not uncoverable. No method in this package only exits the process or runs a system command. | `tests.freemind.cov.map.*BehaviourTest` | passed |
| — | The classes in `freemind.view.mindmapview` keep the results named by each `*BehaviourTest` in `tests.freemind.cov.view`. Each of those tests is labeled backwards compatibility. The long-node dialog submits, splits, and cancels. The HTML dialog's south buttons do the same. `EditNodeTextField.show` still stops at the menu-shortcut lookup under the headless unit JVM. That method is not uncoverable. | `tests.freemind.cov.view.*BehaviourTest` | passed |
| — | The classes in `accessories.plugins` keep the results named by each `*BehaviourTest` in `tests.freemind.cov.accessories`. Each of those tests is labeled backwards compatibility. Node history keeps 100 entries and drops the tail. Save-all leaves the current map in place. The format, pattern, and icon dialogs, the note editor, the log viewer dialog, and the encrypt password frame still stop before they are shown when the suite is headless. They are not uncoverable. No method in this package only exits the process or runs a system command. | `tests.freemind.cov.accessories.*BehaviourTest` | passed |
| — | The classes in `freemind.controller` keep the results named by each `*BehaviourTest` in `tests.freemind.cov.controller` and `freemind.controller`. Tests with no requirement ID are labeled backwards compatibility. Zoom steps run from 25% to 400%. A title keeps the file path, the unsaved star, and a contributor. Tabs swap, and the split pane leaves when both panels are removed. Next and previous wrap the open maps. Open-map counts enable next and previous above 0, and move-left and move-right when the index allows. The filter toolbar starts hidden and toggles. A recent-file menu entry switches to the already-open map. A stored page is landscape, portrait, or reverse landscape, and a printer job uses landscape. `Controller.quit` exits the process and is in the uncoverable table. The print dialog, page setup, print preview, about, license, properties, error, and color-chooser dialogs are not shown. They are not uncoverable. | `tests.freemind.cov.controller.*BehaviourTest`, `freemind.controller.*BehaviourTest` | passed |
| — | The classes in `plugins.collaboration.socket` keep the results named by each `*BehaviourTest` in `tests.freemind.cov.socket`. Each of those tests is labeled backwards compatibility. A directory of maps is served on an ephemeral port and saved when the copy on disk is gone. A file that is not a directory is rejected. The password, map-chooser, and publish dialogs stop with `HeadlessException` before they are shown. They are not uncoverable. `StandaloneMindMapMaster.main` is not called because it binds port 9001. A failed server-socket bind calls `System.exit(1)` and is in the uncoverable table. | `tests.freemind.cov.socket.*BehaviourTest` | passed |
| — | The classes in `freemind.modes` keep the results named by each `*BehaviourTest` in `tests.freemind.cov.modes`. Each of those tests is labeled backwards compatibility. A pattern file keeps the node, the edge, the font, and a child that points at itself. Folding the selected child folds that child. The AWT file dialog constructor stops with `HeadlessException` before the dialog is shown. The Swing file dialog is constructed and is not shown. Open, save-as, and an unsaved close stay off the dialog. `FreeMindAwtFileDialog.main` calls `System.exit(0)` and is in the uncoverable table. | `tests.freemind.cov.modes.*BehaviourTest` | passed |
| — | The classes in `freemind.modes.mindmapmode` keep the results named by each `*BehaviourTest` in `tests.freemind.cov.mindmapmode` and `freemind.modes.mindmapmode`. Each of those tests is labeled backwards compatibility. A sibling 1.1.0 map imports as a linked branch. HTML export writes the given path. An encrypted node hides a child and restores it when the passwords match. File choosers and the map-source dialog stay unopened. They are not uncoverable. No method in this package only exits the process or runs a system command. | `tests.freemind.cov.mindmapmode.*BehaviourTest`, `freemind.modes.mindmapmode.*BehaviourTest` | passed |
| — | The classes in `accessories.plugins.time` keep the results named by each `*BehaviourTest` in `tests.freemind.cov.time`. Each of those tests is labeled backwards compatibility. A spin field clamps inside the limit, at the limit, just over, at zero, and below zero. A month wraps the year. An ISO week starts on Monday. A future reminder sets a tooltip and shutdown clears it. Removing a reminder drops that hook. A table header cycles ascending, descending, and unsorted. A folded node stays out of the time list until it is shown again. The calendar-marking dialog, the time-management dialog, and the time-list dialog throw `HeadlessException` before they are shown. They are not uncoverable. Close and a map-module change throw `NullPointerException` when no dialog exists. Demo mains that only show a frame were not called. No method in this package only exits the process or runs a system command. | `tests.freemind.cov.time.*BehaviourTest` | passed |
| — | The classes in `freemind.controller.filter` keep the results named by each `*BehaviourTest` in `tests.freemind.cov.filter`. Each of those tests is labeled backwards compatibility. A null condition shows every node. Ancestors and descendants stay visible only when that option is on. A hidden selection moves to the nearest visible parent. The toolbar starts hidden with no-filtering selected. Choosing a contains-condition hides a node that does not match. Unfold opens a folded ancestor. A saved condition loads back, and a broken file falls back to the two standard conditions. The filter composer throws `HeadlessException` before it is shown. It is not uncoverable. Show with no map throws `NullPointerException` and leaves the toolbar visible. No method in this package only exits the process or runs a system command. | `tests.freemind.cov.filter.*BehaviourTest` | passed |
| — | The classes in `freemind.modes.mindmapmode.actions` keep the results named by each `*BehaviourTest` in `tests.freemind.cov.actions`. Each of those tests is labeled backwards compatibility. A new child is appended, a sibling follows the selection, and moving a node up swaps the order. Bold, italic, underline, and strikethrough turn off. A copied node becomes a child on paste, and a string paste drops a null character. Undo keeps two edits, drops the oldest, stays empty at 0, and throws `ArrayIndexOutOfBoundsException` at -1. Two rapid edits stay one compound. Color choosers, file choosers, the link prompt, the join dialog, and an unsaved revert throw `HeadlessException` before they change the map. They are not uncoverable. No method in this package only exits the process or runs a system command. | `tests.freemind.cov.actions.*BehaviourTest` | passed |
| — | The classes in `plugins.script` keep the results named by each `*BehaviourTest` in `tests.freemind.cov.script`. Each of those tests is labeled backwards compatibility. A stored script `= 'ran'` sets the selected node to `ran`. A broken script returns false and reports a line. An external script rewrites the root only when asking is stored as yes. A cookie is visible to the next script. File, network, and exec checks throw `SecurityException` while those flags are off. The build leaves `ScriptingSecurityManager` out of the main compile, and the test compiles that one file without installing it as the JVM manager. The script editor throws `HeadlessException` before it is shown. It is not uncoverable. Signing stops at the password dialog. No method in this package only exits the process or runs a system command. | `tests.freemind.cov.script.*BehaviourTest` | passed |
| — | The classes in `freemind.common` keep the results named by each `*BehaviourTest` in `tests.freemind.cov.common`. Each of those tests is labeled backwards compatibility. A boolean, a string, a number, and a password round-trip through their controls. An empty string is kept for a string and a password. A number outside the range falls back to the minimum. A remembered dialog answer returns without a window. A colour chooser, an icon picker, the progress monitor, and a task that needs that monitor throw `HeadlessException` before they are shown. They are not uncoverable. `FreeMindProgressMonitor.main` calls `System.exit` and is in the uncoverable table. | `tests.freemind.cov.common.*BehaviourTest` | passed |
| — | The classes in `freemind.preferences.layout` keep the results named by each `*BehaviourTest` in `tests.freemind.cov.layout`. Each of those tests is labeled backwards compatibility. Key translation keeps a letter, a tab, and a space, and drops a key release. A card layout ignores a hidden card and sizes to the visible maximum. A saved option panel selects its tab and adds no window. The grab-key dialog constructors throw `HeadlessException` before the dialog is shown. They are not uncoverable. No method in this package only exits the process or runs a system command. | `tests.freemind.cov.layout.*BehaviourTest` | passed |
| — | The classes in `plugins.contextgraph` keep the results named by each `*BehaviourTest` in `tests.freemind.cov.contextgraph`. Each of those tests is labeled backwards compatibility. A cancelled save leaves the previous file unchanged. A blank root is written as `mindmap`. JSON escapes quotes, XML escapes markup, and YAML quotes booleans and markers. HTML is stripped or converted. A directory target writes nothing. No method in this package only exits the process or runs a system command. | `tests.freemind.cov.contextgraph.*BehaviourTest` | passed |
| — | The classes in `freemind.modes.browsemode` keep the results named by each `*BehaviourTest` in `tests.freemind.cov.browsemode`. Each of those tests is labeled backwards compatibility. A browse node stays read-only and keeps its child. A future reminder sets a tooltip and shutdown clears it. A note is shown, updated, and cleared. A real `.mm` file loads. A bad map throws `IOException`. A missing file, an unsupported protocol, and folding an encrypted node throw `HeadlessException` before a dialog is shown. They are not uncoverable. No method in this package only exits the process or runs a system command. | `tests.freemind.cov.browsemode.*BehaviourTest` | passed |
| — | The classes in `freemind.modes.mindmapmode.listeners` keep the results named by each `*BehaviourTest` in `tests.freemind.cov.listeners`. Each of those tests is labeled backwards compatibility. A left drag scrolls the map. Dragging an arrow bends it and release hides the handles. Dragging a node on the right increases its gap, control-drag changes the parent gap, and a left node flips the horizontal gap. A double click restores one axis. A drop copies onto the target, a move reparents the node, and a link joins the selection. A move onto the selected node is refused. A move onto a descendant of another selected node, a copy onto a protected node, and five or more links stop with `HeadlessException` before a dialog is shown. They are not uncoverable. No method in this package only exits the process or runs a system command. | `tests.freemind.cov.listeners.*BehaviourTest` | passed |
| — | The classes in `accessories.plugins.dialogs` keep the results named by each `*BehaviourTest` in `tests.freemind.cov.dialogs`. Each of those tests is labeled backwards compatibility. A list drag copies the selected row. A drop appends when nothing is selected and inserts after the selected row. Dropping onto the same selection leaves it. A move removes the adjusted source index, and a copy leaves the source. A flavor that cannot be read is refused. The format dialog and the pattern dialog throw `HeadlessException` before they are shown. They are not uncoverable. No method in this package only exits the process or runs a system command. | `tests.freemind.cov.dialogs.*BehaviourTest` | passed |
| — | The classes in `accessories.plugins.util.xslt` keep the results named by each `*BehaviourTest` in `tests.freemind.cov.xslt`. Each of those tests is labeled backwards compatibility. An identity stylesheet writes the source element text. A missing result directory throws `FileNotFoundException` before the transform. A broken stylesheet is caught and the result file still exists. The export dialog reads its title and then throws `HeadlessException` before a window exists. It is not uncoverable. The only `System.exit` in this package is inside a comment. | `tests.freemind.cov.xslt.*BehaviourTest` | passed |
| — | The classes in `freemind.modes.common.actions` keep the results named by each `*BehaviourTest` in `tests.freemind.cov.commonactions`. Each of those tests is labeled backwards compatibility. `prepareTextContent` turns null into an empty string, strips HTML, and lowercases only when the search is case-insensitive. Find selects a matching child and returns false for a missing or case-mismatched term. Find next selects the second match. A folded ancestor is unfolded for a match below it, and find next folds it again. With search-in-notes on, a note matches when the node text does not. A new map opens beside the previous one and leaves that root in place. No method in this package only exits the process or runs a system command. | `tests.freemind.cov.commonactions.*BehaviourTest` | passed |
| — | The classes in `plugins.latex` keep the results named by each `*BehaviourTest` in `tests.freemind.cov.latex`. Each of those tests is labeled backwards compatibility. The hook starts at `E = mc^{2}`. Saving writes `EQUATION`, and loading reads it or keeps the current equation. Creating a view adds one equation component, removing the view takes it away, and shutdown clears the rest. The equation text round-trips, a broken formula keeps its text, a larger zoom grows the preferred size, and painting leaves ink. No method in this package only exits the process or runs a system command. | `tests.freemind.cov.latex.*BehaviourTest` | passed |

## Uncoverable

A method is uncoverable when its only effect is `System.exit` or `Runtime.exec` of a system command. These were read and left undriven. Dialogs, network calls, and timers are not in this table.

| Source | Method | Call |
| --- | --- | --- |
| `freemind/freemind/main/FreeMindStarter.java` | `main` | `System.exit(1)` after a failed startup |
| `freemind/freemind/main/FreeMindStarter.java` | `getUserPreferencesFile` | `System.exit(1)` when `defaultPreferences` is null. The non-null path is tested. |
| `freemind/freemind/main/FreeMind.java` | `main` | `System.exit(1)` in the startup catch |
| `freemind/freemind/main/FreeMind.java` | `checkForAnotherInstance` | `System.exit(0)` after a successful read from another instance's socket. A missing or badly formatted port file is tested. |
| `freemind/freemind/main/FreeMind.java` | `FreeMind$3.windowClosing` | `controller.quit` reaches `System.exit(0)` in `Controller.quit` |
| `freemind/freemind/controller/Controller.java` | `quit` | `System.exit(0)` after storing the window and the open maps |
| `freemind/freemind/controller/Controller.java` | `QuitAction.actionPerformed` | calls `quit()`, which reaches `System.exit(0)` |
| `freemind/plugins/collaboration/socket/StandaloneMindMapMaster.java` | `StandaloneMindMapMaster` constructor | `System.exit(1)` when the server socket cannot be opened. The success path on an ephemeral port is tested. |
| `freemind/freemind/modes/FreeMindAwtFileDialog.java` | `main` | `System.exit(0)` after the open dialog |
| `freemind/freemind/main/FreeMind.java` | `initServer` | `new EditServer`, whose constructor calls `Tools.setPermissions` |
| `freemind/freemind/main/EditServer.java` | `EditServer` constructor | `Tools.setPermissions(portFile, 0600)`, which runs `Runtime.getRuntime().exec` |
| `freemind/freemind/main/Tools.java` | `setPermissions` | `Runtime.getRuntime().exec` of `chmod` when `permissions != 0` and `isUnix()`. `setPermissions(path, 0)` is tested. |
| `freemind/freemind/main/Tools.java` | `setHidden` | `Runtime.getRuntime().exec` of `attrib.exe` when `os.name` starts with `Win`. The non-Windows return is tested. |
| `freemind/freemind/view/mindmapview/IndependantMapViewCreator.java` | `main` | `System.exit(0)` when the argument count is not 2, and `System.exit(0)` or `System.exit(1)` after an export attempt |
| `freemind/freemind/view/mindmapview/EditNodeExternalApplication.java` | `EditNodeExternalApplication$1.run` | `Runtime.getRuntime().exec` of the `html_editing_command` property |
| `freemind/freemind/common/FreeMindProgressMonitor.java` | `main` | `System.exit(1)` when the argument is not an integer, and `System.exit(0)` after the monitor is shown |

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

`ActionPairTests` stores action objects and does not apply them. The GUI undo methods delete or recreate nodes by hand.

- `requirements/DeducedBehaviourTest.java` `TestREQ_NODE_03_UndoMenuRestoresPreviousText`: after `setNodeText` from `Before` to `After`, the undo action restores `Before` and disables itself.

### REQ-NODE-04 — Partial

Delete is checked. Cut reduces the child count. Paste of a string leaves that text on a new child. Copy, copy single, and paste as plain text are not checked.

- `requirements/DeducedBehaviourTest.java` `TestREQ_NODE_04_PasteLeavesThePastedText`: pasting `Pasted leaf` adds one child with that text.
- Cut and paste count: `StandaloneMapTests.java` `testXmlChangeWithoutModeController` calls `mapFeedback.cut` and `mapFeedback.paste`.
- Delete: `gui/NodeCrudGuiTest.java` `node_deleteChild`, `node_nestedDeletion`.

### REQ-NODE-05 — Checked

- `requirements/RequirementBehaviourTest.java` `TestREQ_NODE_05_SelectAllAndBranch`: `MapView.selectBranch` on the root selects every node view. `selectBranch` on the Branch node, without extend, selects that node and Leaf, and not the root or Other.

The Edit menu actions are not invoked. `SelectAllAction` and `SelectBranchAction` call this same view method.

### REQ-NODE-06 — Partial

`moveNodes` changes the child index. Cut then paste moves a node under a new parent. The drop listener is not driven. The drag tests create a new node instead of reparenting the original.

- `requirements/DeducedBehaviourTest.java` `TestREQ_NODE_06_CutThenPasteMovesNodeToNewParent`: cutting `MoveMe` and pasting it onto `Dest` leaves `MoveMe` only under `Dest`. `Keep` stays under the root.
- `StandaloneMapTests.java` `testXmlChangeWithoutModeController`: `moveNodes` changes index 0 to 1, back to 0, then to 2.

### REQ-NODE-07 — Checked

- `requirements/DeducedBehaviourTest.java` `TestREQ_NODE_07_JoinLeavesIntoSelectedText`: joining the leaves `Alpha` and `Beta` writes `Alpha Beta` on `Alpha` and removes `Beta`.

The dialog that appears when a selected node has children is not opened.

### REQ-NODE-08 — Partial

The shown HTML gains a base address. The file chooser is not opened, and no drawn image pixel is checked.

- `requirements/DeducedBehaviourTest.java` `TestREQ_NODE_08_ImageHtmlGainsBaseHrefInTheView`: the stored text keeps `<img src="pic.png">` and does not gain `<base `. The view text contains that image and `<base href>` of the map URL.

### REQ-NODE-09 — Checked

- `requirements/DeducedBehaviourTest.java` `TestREQ_NODE_09_SortInvokeReordersChildren`: `SortNodes.invoke` on a root whose children are `beta` then `alpha` leaves them as `alpha` then `beta`.
- `requirements/RequirementBehaviourTest.java` `TestREQ_NODE_09_SortsChildrenByPlainText`: the plugin's `NodeTextComparator` orders `apple` before `beta` (read from HTML) before `Zebra`, treats `Apple` and `apple` as equal, and reverses that order after `setNegative`.

`gui/ToolsMenuGuiTest.java` `tools_sortNodes` uses `containsExactlyInAnyOrder`, so that menu test does not check order.

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

Applying one pattern changes the target node's color. The pattern frame builds at least five buttons. The pattern manager lists at least 20 patterns, including the shipped `Default` pattern. `getResultPattern` is not called. The format dialog is a backwards-compatibility test and does not apply a pattern.

- `StandaloneMapTests.java` `testXmlChangeWithoutModeController`: a pattern taken from a magenta node makes another node magenta.
- `property/XMLRoundTripPropertyTest.java` `patternsRoundTrip` keeps pattern names only.
- `main/StartupBehaviourTest.java` `TestREQ_STYLE_04_PatternFrameBuildsAPattern`: after `StylePatternFrame.init` the frame has components and at least five buttons.
- `main/StartupBehaviourTest.java` `TestREQ_STYLE_04_PatternManagerListsShippedPatterns`: the pattern list has at least 20 entries and one is the shipped `Default` pattern, under that name or under its translation.

### REQ-STYLE-05 — Checked

- `modes/NodeAdapterTest.java` `addIconIncreasesCount`, `removeIconReturnsRemainingCount`.
- `io/MapRoundTripTest.java` `iconsPreservedThroughRoundTrip`.
- `StandaloneMapTests.java` `testXmlChangeWithoutModeController`: add one, remove last, add five, remove all.

### REQ-STYLE-06 — Checked

- `modes/NodeAdapterTest.java` `setAndGetCloud`, `cloudSetAndGetColor`, `setCloudNullRemovesCloud`.
- `actions/ActionFrameworkTest.java` `setCloudEnables`, `setCloudDisables`.
- `BackwardCompatTest.java` `mapWithCloudLoads` checks presence only.

`FormatMenuGuiTest.format_cloudToggle` leaves the cloud null and is not counted.

### REQ-STYLE-07 — Checked

- `requirements/DeducedBehaviourTest.java` `TestREQ_STYLE_07_FourEdgeShapesPaint`: each of `linear`, `bezier`, `sharp_linear`, and `sharp_bezier` is stored, and painting the root after that style leaves a non-white pixel. Setting the edge color to red and the width to 4 stores both.
- `modes/NodeAdapterTest.java` `edgeSetAndGetStyle`, `edgeGetStyleAsIntLinear`, `edgeSetAndGetColor`, `edgeSetAndGetWidth`.
- `StandaloneMapTests.java` `testXmlChangeWithoutModeController`: `sharp_bezier` is stored, and `null` falls back to bezier.

The format menu is not invoked.

### REQ-STYLE-08 — Checked

- `requirements/DeducedBehaviourTest.java` `TestREQ_STYLE_08_CtrlWheelChangesZoom`: a ctrl mouse-wheel event with a positive rotation makes `getZoom` smaller.
- `requirements/RequirementBehaviourTest.java` `TestREQ_STYLE_08_ZoomScalesTheMap`: `MapView.setZoom(1.5f)` makes `getZoom` 1.5 and `getZoomed(100)` 150. Setting 1f makes `getZoomed(100)` 100.

The zoom menu shortcuts are not invoked.

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

### REQ-FIND-01 — Checked

- `requirements/DeducedBehaviourTest.java` `TestREQ_FIND_01_FindThenFindNext`: find selects `alpha one`. Find next selects `prefix alpha two`. The match ignores case.

The find dialog is not opened. `FindTextTests` still checks HTML tag stripping, a time-list row filter, and replace. `gui/EditMenuGuiTest.java` `edit_find` uses the test helper `findNodeByText`.

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

SVG is checked. The PDF options dialog keeps A4 and landscape and writes `pdf_page_format` and `pdf_page_orientation`. No PDF bytes are written.

- `plugins/svg/TestREQ_FILE_02_SvgContainsNodeText.java` `TestREQ_FILE_02_SvgDocumentContainsNodeText`: `fillSVGGraphics2D` writes an SVG document that contains `SvgRoot` and `SvgChild`.
- `main/StartupBehaviourTest.java` `TestREQ_FILE_02_PdfDialogKeepsPaperAndOrientation`: the dialog title is the shipped PDF export settings title, A4 and landscape are selected, OK stores those two properties, and `getResult` is true.

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

### REQ-FILE-06 — Partial

Folder-structure import is checked. A branch, a linked branch, and Explorer favorites are not.

- `requirements/DeducedBehaviourTest.java` `TestREQ_FILE_06_ImportFolderAddsNamedChildren`: the import adds `Sub`, `Empty`, and `note.txt` with file URL links. `Sub`, which contains `inner.txt`, is folded. `Empty` stays unfolded. The non-root target is folded. The root stays unfolded.

`gui/ImportGuiTest.java` still copies node text by hand or loads ordinary `.mm` XML.

### REQ-FILE-07 — Partial

These accessory outputs are checked: plain text, CSV, OPML, a LaTeX article, WordML (non-blank), XBEL, TWiki, Redmine wiki, and an `.odt` file. The spec says further accessories exist and does not name them one by one. This row does not claim those others.

- `export/XsltExportTest.java` `textExportContainsNodeTexts`, `csvExportContainsMultipleNodes`, `mm2opmlProducesOutput`, `mm2latexartclProducesOutput`, `mm2wordmlProducesOutput`, `mm2xbelProducesOutput`, `mm2twikiProducesOutput`, `mm2redminewikiProducesOutput`.
- `TransformTest.java` `testExportOoo`: the OpenOffice export writes an `.odt` file.

## Plugins and modes

### REQ-PLUG-01 — Partial

A Groovy script result and the excluded script security manager are checked. The editor shows `return 1;` under `script0`. The script is not run.

- `requirements/RequirementBehaviourTest.java` `TestREQ_PLUG_01_GroovyScriptReturnsValue`: a `GroovyShell` with the same `ThreadInterrupt` customizer `ScriptingEngine` uses evaluates `1 + 2` to 3. `freemind/build.gradle.kts` excludes `ScriptingSecurityManager.java`.
- `ScriptEditorPanelTest.java` `testErrorLineNumbers`: a Groovy error string yields line number 1.
- `main/StartupBehaviourTest.java` `TestREQ_PLUG_01_ScriptEditorShowsTheScriptText`: the window title is the shipped script-editor title, the list has one entry `script0`, and the text area is `return 1;`.

### REQ-PLUG-02 — Partial

Nominatim XML unmarshalls to places. A map viewer builds its menus, reads tile-source URLs, builds a coordinate link, and parses `47.26 11.39` into one place. Zoom increases. No test attaches a position to a node, and `MapDialog.startupMapHook` is not opened. The position round trip is a backwards-compatibility test, not this requirement.

- `MarshallerTests.java` `testOsmNominatimConversion`, `testNominatimReverse`.
- `main/StartupBehaviourTest.java` `TestREQ_PLUG_02_MapViewBuildsMenusAndReadsACoordinate`: at least four menus, transport and MapQuest base URLs, a link containing `mlat=47.26` and `zoom=12`, a Mapnik tile source, one place at that coordinate, and a higher zoom after the zoom action. Tile downloads are blocked.

### REQ-PLUG-03 — Checked

- `plugins/search/TestREQ_PLUG_03_LuceneFindsIndexedMap.java` `TestREQ_PLUG_03_IndexFindsOneMapAndMissesTheOther`: `FileSearchModel` indexes a directory. A query for a token that is in `hit.mm` returns that file only. The same token in `notes.txt` is not a hit. A token that is in neither file returns no path.

`PluginLogicTest.SearchPluginTests` only checks that the class loads. `plugins/search/SearchViewPanelBehaviourTest.java` shows the search window with `No results`. That window test is not this Lucene requirement.

### REQ-PLUG-04 — Partial

The help set loads. The help window is not opened.

- `requirements/DeducedBehaviourTest.java` `TestREQ_PLUG_04_HelpSetLoads`: the title contains `FreeMind Help` and the home id is `top`.

The help GUI tests still check version strings and non-null objects.

### REQ-PLUG-05 — Partial

The stored equation is checked. It is not rendered on a node. `export/XsltExportTest.java` `mm2latexartclProducesOutput` exports a LaTeX article file, which is REQ-FILE-07.

- `requirements/DeducedBehaviourTest.java` `TestREQ_PLUG_05_LatexHookStoresEquation`: a new hook reads back `E = mc^{2}`, saving writes that equation, and loading `a = b` replaces it.

### REQ-PLUG-06 — Partial

A localhost socket accepts a client through hello, welcome, and lock, and publishing a map creates the file on the server. The note text is not checked, and the received map is not applied.

- `CollaborationTests.java` `testNormalStartup`, `testNormalStartupWithTwoClients`, `testPublishExistingMapStartup`, `testPublishExistingMapWrongName`.

### REQ-PLUG-07 — Partial

An English dictionary loads. No word is checked, and `FreeMind.setupSpellChecking` is not called.

- `requirements/DeducedBehaviourTest.java` `TestREQ_PLUG_07_EnglishDictionaryLoads`: after `SpellChecker.registerDictionaries` for `en`, a dictionary is loaded and the current locale language is `en`.

### REQ-PLUG-08 — Checked

- `requirements/RequirementBehaviourTest.java` `TestREQ_PLUG_08_BuildOmitsLegacyModules`: every `include(` line in `settings.gradle.kts` omits database, jabber, and pda. `freemind/build.gradle.kts` excludes `**/jabber/**` and `**/database/**` from the main Java source set.

### REQ-MODE-01 — Partial

The default `initial_mode` property is `MindMap`. File mode's new model root is `Root` and has children. A temporary directory lists `listed.mm`, and that file node is not writeable. Browse mode starts. Read-only browse is not shown.

- `DefaultPropertiesTest.java` `initialModeDefaultIsMindMap`.
- `main/StartupBehaviourTest.java` `TestREQ_MODE_01_FileModeRootNamesTheFilesystemRoot`: after `createNewMode("File")` the root text is `Root` and it has children.
- `main/StartupBehaviourTest.java` `TestREQ_MODE_01_BrowseModeStarts`: after `createNewMode("Browse")` the mode controller is a `BrowseController`.
- `requirements/BackwardsCompatibilityTest.java` `fileModeListsADirectory`: a temporary directory lists `listed.mm`, and the file node is not writeable.

## Application

### REQ-APP-01 — Partial

The property store changes. The options panel builds and shows the Environment tab. OK does not write a property, and nothing is written to `auto.properties`.

- `requirements/RequirementBehaviourTest.java` `TestREQ_APP_01_PreferenceStoreChanges`: `HeadlessFreeMind.setProperty` then `getProperty` returns the new value, and a second set replaces it.
- `main/StartupBehaviourTest.java` `TestREQ_APP_01_StartupSelectsMindMapAndBuildsTheFileMenu`: the mode is `MindMap`, the menu bar has at least nine menus, and the File menu text is the shipped label.
- `main/StartupBehaviourTest.java` `TestREQ_APP_01_OptionPanelShowsEnvironmentTab`: `OptionPanel.buildPanel` shows the Environment button and a split pane. The dialog is disposed.

`DefaultPropertiesTest` only reads defaults. `HeadlessFreeMind.saveProperties` is empty.

### REQ-APP-02 — Partial

Paper size and imageable area survive a string round trip. Print preview shows the label `- 1 -` and OK disposes the dialog. The page is not painted, and nothing is sent to a printer.

- `ToolsTests.java` `testPageFormatStorage`.
- `main/StartupBehaviourTest.java` `TestREQ_APP_02_PrintPreviewShowsTheFirstPage`: `PreviewDialog` contains `- 1 -`, and OK leaves the dialog not displayable.

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
