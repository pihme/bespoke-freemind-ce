# FreeMind CE feature spec

Raw-stage list of what upstream documents, plus features only visible in the menu or the tree. Later stages edit this file. IDs are not reused.

Upstream has no numbered requirements spec. The verdict is in `BESPOKE.md`.

A line marked **docs** comes from `README.md` or `docs/`. A line marked **menu** comes from `freemind/mindmap_menus.xml`. A line marked **tree** comes from the build files or source layout, where the docs disagree with the tree.

## Map

| ID | What the user can do | Entry points | Source |
|----|----------------------|--------------|--------|
| REQ-MAP-01 | Create a map, open one, and save it. | New, Open, Save. README shortcuts ⌘N, ⌘O, ⌘S. | docs |
| REQ-MAP-02 | Save under a new name, or revert to the last saved file. | Save As, Revert. | menu |
| REQ-MAP-03 | Keep the map in an XML `.mm` file: a tree of nodes with text, color, font, style, icons, links, and notes; edges with style, color, and width; clouds; arrow links between nodes. | `freemind.xsd`. Open and Save. | docs |
| REQ-MAP-04 | Have more than one map open, see recent files, and have the window position remembered. | Map module manager, recent-files list. | docs |

## Nodes

| ID | What the user can do | Entry points | Source |
|----|----------------------|--------------|--------|
| REQ-NODE-01 | Add a child or a sibling and edit its text in place, in a dialog, or in an external application. | Tab, Enter, F2. Insert menu. | docs |
| REQ-NODE-02 | Fold and unfold a node, including its children. | Space. Navigate menu. | docs |
| REQ-NODE-03 | Undo and redo an edit. Edits are stored as XML action pairs. | Undo, Redo. | docs |
| REQ-NODE-04 | Cut, copy, copy a single node, paste, and paste as plain text. Delete a child. | Edit menu. | menu |
| REQ-NODE-05 | Select all nodes, or the current branch. | Edit menu. | menu |
| REQ-NODE-06 | Move the selected node up or down, and drag it to a new parent. | Navigate menu. Drag on the canvas. | docs for drag; menu for up/down |
| REQ-NODE-07 | Join selected nodes. | Extras menu. | menu |
| REQ-NODE-08 | Put an image on a node. | Insert menu, file chooser. The canvas can draw images. | docs for drawing; menu for the command |
| REQ-NODE-09 | Sort nodes. The extras menu has a sorting slot and no actions of its own in `mindmap_menus.xml`. | Extras menu. | menu |

## Appearance

| ID | What the user can do | Entry points | Source |
|----|----------------------|--------------|--------|
| REQ-STYLE-01 | Show a node as a fork or a bubble. The root has its own shape. | Format menu. | docs |
| REQ-STYLE-02 | Set bold, italic, strikethrough, font size, text color, color blend, and background color. Underline is present in the menu file and commented out as not implemented in the view. | Format menu. Toolbar. | menu |
| REQ-STYLE-03 | Use rich text or plain text for a node. | Format menu. | menu |
| REQ-STYLE-04 | Apply and manage node style patterns. | Format menu. `patterns.xml` (24 or more patterns, per the architecture doc). | docs |
| REQ-STYLE-05 | Put icons on a node. | Format menu. | docs |
| REQ-STYLE-06 | Draw a cloud around a node and set its color. | Insert and Format menus. | docs |
| REQ-STYLE-07 | Draw an edge as linear, sharp linear, bezier, or sharp bezier, and set its color and width. | Format menu. | docs for the four shapes; menu for color and width |
| REQ-STYLE-08 | Zoom the map, including with the mouse wheel. | ⌘+ and ⌘−. Map canvas. | docs |

## Links, notes, attributes

| ID | What the user can do | Entry points | Source |
|----|----------------------|--------------|--------|
| REQ-LINK-01 | Attach a file or a text link to a node, follow it, and open the link's directory. | Insert menu. Navigate menu. | docs for links on a node; menu for the commands |
| REQ-LINK-02 | Add an arrow between two nodes, or a link to another node in the same map. | Edit and Insert menus. | docs |
| REQ-NOTE-01 | Keep a note on a node. | Notes are part of the `.mm` node. The navigate menu has an empty notes slot. | docs |
| REQ-ATTR-01 | Filter the visible nodes by text, by an attribute value, or by an icon, and combine those tests with AND or OR. | Filter toolbar. | docs |
| REQ-ATTR-02 | Edit a node's attributes. The attribute-manager entries in the menu file are commented out. The popup still has Edit Attributes. | Popup menu. | menu |
| REQ-FIND-01 | Find text in the map, and find the next match. | ⌘F. Edit menu. | docs |

## Files in and out

| ID | What the user can do | Entry points | Source |
|----|----------------------|--------------|--------|
| REQ-FILE-01 | Export the map or a branch to HTML. | File → Export. README records standalone HTML export as done. | docs |
| REQ-FILE-02 | Export to SVG and to PDF, with a PDF options dialog. | SVG/PDF plugin. | docs |
| REQ-FILE-03 | Export a picture. The release checklist accepts PNG as one of HTML, PDF, or PNG. | File → Export → picture. | docs for the checklist; menu for the slot |
| ~~REQ-FILE-04~~ REMOVED (TAI-016) | Export a context graph as Markdown, or as XML with node count, depth, and date. | Context Graph plugin. | docs |
| REQ-FILE-05 | Export the current branch as its own map. | File → Export. | menu |
| REQ-FILE-06 | Import a branch, a linked branch, Explorer favorites, or a folder structure. | File → Import. | menu |
| REQ-FILE-07 | Further XSLT exports live under `freemind/accessories/` (text, CSV, OPML, LaTeX, word processor, and others). The docs do not list them one by one. | File → Export slots filled by accessories. | tree |

## Plugins and modes

| ID | What the user can do | Entry points | Source |
|----|----------------------|--------------|--------|
| ~~REQ-PLUG-01~~ REMOVED (TAI-001) | Run a Groovy script, with an editor. A security manager for scripts is excluded from the Java 21 compile. | Extras → scripting. | docs |
| ~~REQ-PLUG-02~~ REMOVED (TAI-002) | Attach a geographic position and open it on an OpenStreetMap view. | Extras → map. | docs |
| ~~REQ-PLUG-03~~ REMOVED (TAI-003) | Search map files with a Lucene index. | Search plugin. | docs |
| ~~REQ-PLUG-04~~ REMOVED (TAI-004) | Open integrated help. | Help plugin, JavaHelp. | docs |
| REQ-PLUG-05 | Render LaTeX on a node. The architecture doc says this plugin is not in the Gradle settings. At this commit `settings.gradle.kts` includes `:freemind:plugins:latex` and the dependency is JLaTeXMath 1.0.7. | LaTeX plugin. | tree; the older doc is stale |
| ~~REQ-PLUG-06~~ REMOVED (TAI-006) | Collaborate on a map in real time over a TCP socket. | Extras → collaboration. | docs |
| REQ-PLUG-07 | Check spelling. JOrtho is a documented dependency. | Not named in the menu file read for this spec. | docs |
| REQ-PLUG-08 | Database collaboration and Jabber collaboration are legacy. The Gradle build excludes them. The Palm OS tree under `pda/` is historical and is not a Gradle module. | Not in the active build. | docs |
| REQ-MODE-01 | Edit in MindMap mode, navigate read-only in Browse mode, or browse files in File mode. | Declared in `freemind.properties`. | docs |

## Application

| ID | What the user can do | Entry points | Source |
|----|----------------------|--------------|--------|
| REQ-APP-01 | Change preferences. | Extras → options. `freemind.properties`, with user overrides in `~/.freemind/auto.properties`. | docs |
| REQ-APP-02 | Print, and look at a print preview. | Toolbar Print. Preview dialog. | docs for preview; menu for direct print |
| REQ-APP-03 | Use the application in one of 30 or more languages. | `Resources_*.properties`. | docs |
| REQ-APP-04 | Use a FlatLaf light or dark look. | Documented as the CE look and feel, replacing Metal. | docs |
| REQ-APP-05 | Run on Java 21 on macOS (including Apple Silicon), Windows, and Linux. | `freemind.main.FreeMindStarter`. | docs |
| REQ-SEC-01 | Encrypt a node. The insert menu has an empty crypto category. Tests cover encrypt-node behavior. The docs do not describe the command. | Insert → crypto, if a plugin fills it. | menu and tests |

## Not decided here

This list does not say which of these stay. Removing or adding features is a later stage. The vision says the feature set should end at the core of mind map modeling, and the constraints say the file format stays compatible or gains an importer.

Underline stays unresolved. REQ-STYLE-02 says the view menu entry is commented out. The model flag can still be set. That is not a deduced requirement.

## Deduced requirements

Labeled **Deduced**. These IDs were not chosen as product requirements. They record behaviour the current program already shows, inferred so a missing test has a sentence to check. They do not decide which features stay. IDs are `REQ-DED-nn`, are never reused, and are not renumbered into the 49 rows above. Time-management calendars are not in this table.

| ID | Deduced behaviour | Why it is deduced | Where the program does it |
|----|-------------------|-------------------|---------------------------|
| REQ-DED-01 | Joining leaf nodes writes their plain text, in list order, into the selected node, separated by a single space, and deletes the other selected nodes. A selected node that has children is left unchanged. | REQ-NODE-07 says only "Join selected nodes." | `JoinNodesAction.joinNodes` |
| REQ-DED-02 | Find selects the first visible node whose plain text contains every word, ignoring case. Find next continues through the nodes still queued from that search and selects the next match. | REQ-FIND-01 names the commands and not the match. | `FindAction.find`, `FindAction.findNext` |
| REQ-DED-03 | Importing a folder adds a child for each directory and each file. The node text is the file name. The link is a file URL. A directory that contains something is folded, and so is a target that is not the root once it has children. An empty directory stays unfolded. The root stays unfolded. | REQ-FILE-06 names folder-structure import and not the nodes that appear. Folding a leaf is skipped unless `enable_leaves_folding` is true, and the fold actor returns when the node is the root. | `ImportFolderStructureAction.importFolderStructure`, `ToggleFoldedActor` |
| REQ-DED-04 | An image on a node is HTML text containing `<img src="...">`. When that node is shown, the view inserts a `<base href>` of the map URL if the text does not already have one. The stored node text does not gain that base tag. | REQ-NODE-08 names the command and that the canvas can draw images. It does not name the stored text. | `MindMapController.setImageByFileChooser`, `NodeView.updateText` |
| REQ-DED-05 | A LaTeX hook stores an equation. The default is `E = mc^{2}`. Loading an `EQUATION` attribute replaces it. Saving writes that attribute. | REQ-PLUG-05 says render LaTeX on a node and not the stored value. | `LatexNodeHook` |
| REQ-DED-06 | With spelling turned on, dictionaries are registered from `dictionaries.cnf` for a language that file lists. After registration for `en`, a dictionary is loaded and the current locale language is `en`. | REQ-PLUG-07 names JOrtho and not this switch. | `FreeMind.setupSpellChecking`, `SpellChecker.registerDictionaries` |
| ~~REQ-DED-07~~ REMOVED (TAI-004) | Integrated help is the HelpSet file `plugins/help/doc/freemind.hs`. That set's title contains `FreeMind Help` and its home id is `top`. | REQ-PLUG-04 says open integrated help and not which set. | `FreemindHelpStarter`, `plugins/help/doc/freemind.hs` |
| REQ-DED-08 | Recent maps are kept in a list whose length is the integer property `last_opened_list_length`. The shipped value is 25. The options control stores an integer from 0 to 200. When an open would make the list longer than that integer, the oldest entry is dropped. A value of 0 drops the entry that was just added, so the list stays empty. A negative value removes entries until the list is empty and then throws `IndexOutOfBoundsException`. A value that is not an integer, including one past the `int` range such as `2147483648`, leaves the limit at 25. | The length is a config value that decides how many paths are kept. | `LastOpenedList.add`, `freemind.properties` |
| REQ-DED-09 | Undo stores action pairs up to the integer property `undo_levels`. The shipped value is 100. The options control stores an integer from 2 to 1000. When another pair would make the list longer than that integer, the oldest pair is dropped. Pairs added within 100 ms, or in the same event-dispatch action frame, are folded into one compound pair and count as one entry. That fold does not apply the limit. A value of 0 drops pairs until the list is empty. A negative value removes pairs until the list is empty and then throws `IndexOutOfBoundsException`. A value that is not an integer, including one past the `int` range, leaves the limit at 100. | The depth is a config value that decides how many edits are kept. | `UndoAction.add`, `freemind.properties` |
| REQ-DED-10 | Remembered map positions are kept up to 50. Adding a 51st drops the one with the oldest change time. An empty or unreadable store starts empty. There is no separate zero or negative setting; the bound is the constant `LIST_AMOUNT_LIMIT`. | The list is sized by that constant. | `LastStateStorageManagement.changeOrAdd` |

## Backwards compatibility tests

Umbrella for program behaviour that the requirements above do not name. A test under this heading asserts an observable result of the current program. It does not add a product requirement, and it does not decide that the behaviour stays. If the result contradicts a requirement, the requirement test stays and is not weakened to match the code.

The tests are added in slices. A slice is finished when its assertions are in and the coverage command has been run.

1. Mind-map controller paths that the requirement tests still do not reach.
2. Map view painting that the picture tests still do not reach: edge shapes, clouds, arrow links, mouse-wheel zoom, and an image pixel.
3. The map plugin (`plugins/map`), including an attached geographic position.
4. The preferences panel (`OptionPanel` and the layout package).
5. Browse mode, File mode, print preview, the script editor, the PDF options dialog, the LaTeX viewer, the help window, and the spelling menu.
6. Accessory dialogs the requirements do not name. Time-management calendars are in this slice. Every backwards-compatibility test, including the calendar day check, is listed in `docs/testspec.md` under **Backwards compatibility tests**. That list is the reference. It does not add a product requirement.
