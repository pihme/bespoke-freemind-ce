# Mind map

A mind map is a tree of nodes stored in a `.mm` file and drawn as pictures.

## Language

**Picture**:
The drawn view of one node.
_Avoid_: widget

**Stored style**:
The style token kept on a node and written into the file. It may be fork, bubble, combined, or as_parent.
_Avoid_: drawn style

**Drawn style**:
The shape a picture uses. A stored style of combined is bubble when the node is folded and fork when the node is open.
_Avoid_: stored style

**Underline**:
A font mark kept on the node and written on that node's font in the file.

**Side**:
The node's place to the left or the right of the root. The file records side only for a child of the root.

**Vertical offset**:
The picture's vertical distance from the node's stored shift.

**Stored children**:
The children a node keeps in the file, including the children of a folded node.
_Avoid_: folded-away children

**Folded-away children**:
The stored children a caller visits when fold and the lock are honored. The list is empty when the node is folded or locked.
_Avoid_: stored children

**Replay**:
An undo or a redo. The action is not recorded again.
_Avoid_: undo flag

**Menu name**:
The name a menu document uses to select an action. It is not stored in the map file.
