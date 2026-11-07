/*FreeMind - A Program for creating and viewing Mindmaps
 *Copyright (C) 2000-2026  FreeMind CE contributors
 *See COPYING for Details
 *
 *This program is free software; you can redistribute it and/or
 *modify it under the terms of the GNU General Public License
 *as published by the Free Software Foundation; either version 2
 *of the License, or (at your option) any later version.
 *
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.
 *
 *You should have received a copy of the GNU General Public License
 *along with this program; if not, write to the Free Software
 *Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package freemind.modes;

import java.util.ListIterator;

/**
 * Stored children and folded-away children.
 * A locked node's own answers still apply, because these methods call them.
 */
public final class NodeWalks {

	private NodeWalks() {
	}

	/** Children kept in the file, including children of a folded node. */
	public static ListIterator<MindMapNode> stored(MindMapNode node) {
		return node.childrenUnfolded();
	}

	/**
	 * Children visited when fold and the lock are honored.
	 * Empty when the node is folded, and empty when a locked node hides its children.
	 */
	public static ListIterator<MindMapNode> foldedAway(MindMapNode node) {
		return node.childrenFolded();
	}
}
