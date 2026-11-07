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

import freemind.modes.mindmapmode.actions.xml.actors.XmlActorFactory;

/**
 * Bold, italic, strikethrough, and the style-change walk.
 * The controller and the headless adapter both call this.
 */
public final class ActorDispatch {

	private ActorDispatch() {
	}

	public static void setBold(XmlActorFactory factory, MindMapNode node, boolean bold) {
		factory.getBoldActor().setBold(node, bold);
	}

	public static void setItalic(XmlActorFactory factory, MindMapNode node, boolean italic) {
		factory.getItalicActor().setItalic(node, italic);
	}

	public static void setStrikethrough(XmlActorFactory factory, MindMapNode node, boolean strikethrough) {
		factory.getStrikethroughActor().setStrikethrough(node, strikethrough);
	}

	public static void nodeStyleChanged(ExtendedMapFeedback feedback, MindMapNode node) {
		feedback.nodeChanged(node);
		ListIterator<MindMapNode> children = NodeWalks.foldedAway(node);
		while (children.hasNext()) {
			MindMapNode child = children.next();
			if (!(child.hasStyle() && child.getEdge().hasStyle())) {
				nodeStyleChanged(feedback, child);
			}
		}
	}
}
