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

package freemind.view.mindmapview;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import freemind.modes.MindMapNode;
import freemind.modes.NodeWalks;

/**
 * Which child nodes are drawn, and in which order.
 * Folding and the filter are answered here. Neither answer is written into the file.
 * A folded parent still stores its children. A hidden child still has a picture
 * so a later filter can show it; {@link #showsContent} says whether that picture paints.
 */
public final class DrawnNodes {

	private DrawnNodes() {
	}

	/** Pictures created under the parent, in stored order. Empty when the parent draws no children. */
	public static List<MindMapNode> pictures(MindMapNode parent) {
		List<MindMapNode> pictures = new ArrayList<>();
		ListIterator<MindMapNode> children = NodeWalks.foldedAway(parent);
		if (children == null) {
			return pictures;
		}
		while (children.hasNext()) {
			pictures.add(children.next());
		}
		return pictures;
	}

	/**
	 * Children whose content is painted, in stored order.
	 * A folded parent paints none. A child the filter hides is omitted.
	 */
	public static List<MindMapNode> sequence(MindMapNode parent) {
		List<MindMapNode> drawn = new ArrayList<>();
		for (MindMapNode child : pictures(parent)) {
			if (showsContent(child)) {
				drawn.add(child);
			}
		}
		return drawn;
	}

	public static boolean showsContent(MindMapNode node) {
		return node.isVisible();
	}
}
