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

/**
 * Side and vertical offset for a picture.
 * {@code POSITION} and {@code VSHIFT} stay file values. The single-child
 * offset is not written into the file.
 */
public final class PicturePlacement {

	/** Extra vertical offset when the parent has exactly one visible child. */
	public static final int SINGLE_CHILD_OFFSET = -2;

	private PicturePlacement() {
	}

	public static int verticalOffset(NodeAdapter node) {
		try {
			MindMapNode parent = node.getParentNode();
			int extra = parent.hasExactlyOneVisibleChild() ? SINGLE_CHILD_OFFSET : 0;
			return node.getShiftY() + extra;
		} catch (NullPointerException e) {
			return 0;
		}
	}

	public static boolean isLeft(NodeAdapter node) {
		if (node.getParent() != null && !node.getParentNode().isRoot()) {
			return node.getParentNode().isLeft();
		}
		if (node.storedPosition() == NodeAdapter.UNKNOWN_POSITION && !node.isRoot()) {
			setLeft(node, node.getParentNode().isLeft());
		}
		return node.storedPosition() == NodeAdapter.LEFT_POSITION;
	}

	public static void setLeft(NodeAdapter node, boolean isLeft) {
		int position = isLeft ? NodeAdapter.LEFT_POSITION : NodeAdapter.RIGHT_POSITION;
		node.storedPosition(position);
		if (!node.isRoot()) {
			for (int i = 0; i < node.getChildCount(); i++) {
				NodeAdapter child = (NodeAdapter) node.getChildAt(i);
				child.storedPosition(position);
			}
		}
	}
}
