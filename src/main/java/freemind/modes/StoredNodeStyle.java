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

import freemind.main.FreeMind;

/**
 * The style token stored on a node, the style a picture draws, and underline.
 * A save writes the stored token. {@code combined} stays {@code combined} in the file.
 */
public final class StoredNodeStyle {

	private StoredNodeStyle() {
	}

	public static String stored(MindMapNode node) {
		return node.getBareStyle();
	}

	/** The shape a picture uses. {@code combined} is bubble when folded and fork when open. */
	public static String drawn(MindMapNode node) {
		String style = node.getBareStyle();
		String returnedString = style;
		if (style == null) {
			if (node.isRoot()) {
				returnedString = node.getMapFeedback().getProperty(
						FreeMind.RESOURCES_ROOT_NODE_STYLE);
			} else {
				String stdstyle = node.getMapFeedback().getProperty(
						FreeMind.RESOURCES_NODE_STYLE);
				if (stdstyle.equals(MindMapNode.STYLE_AS_PARENT)) {
					returnedString = node.getParentNode().getStyle();
				} else {
					returnedString = stdstyle;
				}
			}
		} else if (node.isRoot() && style.equals(MindMapNode.STYLE_AS_PARENT)) {
			returnedString = node.getMapFeedback().getProperty(
					FreeMind.RESOURCES_ROOT_NODE_STYLE);
		} else if (style.equals(MindMapNode.STYLE_AS_PARENT)) {
			returnedString = node.getParentNode().getStyle();
		}

		if (returnedString.equals(MindMapNode.STYLE_COMBINED)) {
			if (node.isFolded()) {
				return MindMapNode.STYLE_BUBBLE;
			}
			return MindMapNode.STYLE_FORK;
		}
		return returnedString;
	}

	public static boolean underlineToken(String name, String value) {
		return "UNDERLINE".equals(name) && "true".equals(value);
	}

	public static void markUnderline(NodeAdapter node) {
		node.setUnderlined(true);
	}

	public static boolean writesUnderline(MindMapNode node) {
		return node.isUnderlined();
	}
}
