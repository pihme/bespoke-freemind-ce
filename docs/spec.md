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
| REQ-FILE-04 | Export a context graph as Markdown, or as XML with node count, depth, and date. | Context Graph plugin. | docs |
| REQ-FILE-05 | Export the current branch as its own map. | File → Export. | menu |
| REQ-FILE-06 | Import a branch, a linked branch, Explorer favorites, or a folder structure. | File → Import. | menu |
| REQ-FILE-07 | Further XSLT exports live under `freemind/accessories/` (text, CSV, OPML, LaTeX, word processor, and others). The docs do not list them one by one. | File → Export slots filled by accessories. | tree |

## Plugins and modes

| ID | What the user can do | Entry points | Source |
|----|----------------------|--------------|--------|
| REQ-PLUG-01 | Run a Groovy script, with an editor. A security manager for scripts is excluded from the Java 21 compile. | Extras → scripting. | docs |
| REQ-PLUG-02 | Attach a geographic position and open it on an OpenStreetMap view. | Extras → map. | docs |
| REQ-PLUG-03 | Search map files with a Lucene index. | Search plugin. | docs |
| REQ-PLUG-04 | Open integrated help. | Help plugin, JavaHelp. | docs |
| REQ-PLUG-05 | Render LaTeX on a node. The architecture doc says this plugin is not in the Gradle settings. At this commit `settings.gradle.kts` includes `:freemind:plugins:latex` and the dependency is JLaTeXMath 1.0.7. | LaTeX plugin. | tree; the older doc is stale |
| REQ-PLUG-06 | Collaborate on a map in real time over a TCP socket. | Extras → collaboration. | docs |
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
