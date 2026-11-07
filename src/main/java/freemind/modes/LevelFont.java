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

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

/**
 * Font size from structural depth. The deepest nodes are 12. Each level above
 * is 2 larger, including the center node. Folded children still count.
 */
public final class LevelFont {

	public static final int DEEPEST = 12;
	public static final int STEP = 2;

	private LevelFont() {
	}

	public static int size(MindMapNode node) {
		MindMapNode root = node;
		while (root.getParentNode() != null) {
			root = root.getParentNode();
		}
		return DEEPEST + STEP * (deepest(root) - depth(node));
	}

	public static int depth(MindMapNode node) {
		int depth = 0;
		MindMapNode parent = node.getParentNode();
		while (parent != null) {
			depth++;
			parent = parent.getParentNode();
		}
		return depth;
	}

	public static int deepest(MindMapNode node) {
		int best = depth(node);
		for (MindMapNode child : node.getChildren()) {
			int childDeepest = deepest(child);
			if (childDeepest > best) {
				best = childDeepest;
			}
		}
		return best;
	}

	/** The same face and style, drawn at the level size. */
	public static Font displayed(Font font, MindMapNode node) {
		Map<TextAttribute, Object> attributes = new HashMap<>();
		for (Map.Entry<? extends TextAttribute, ?> entry : font.getAttributes().entrySet()) {
			attributes.put(entry.getKey(), entry.getValue());
		}
		attributes.put(TextAttribute.SIZE, Float.valueOf(size(node)));
		return new Font(attributes);
	}
}
