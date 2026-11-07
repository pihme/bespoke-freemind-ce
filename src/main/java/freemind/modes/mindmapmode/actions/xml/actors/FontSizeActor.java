/*FreeMind - A Program for creating and viewing Mindmaps
*Copyright (C) 2000-2014 Christian Foltin, Joerg Mueller, Daniel Polansky, Dimitri Polivaev and others.
*
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

package freemind.modes.mindmapmode.actions.xml.actors;

import freemind.controller.actions.generated.instance.FontSizeNodeAction;
import freemind.controller.actions.generated.instance.XmlAction;
import freemind.main.Tools;
import freemind.modes.ExtendedMapFeedback;
import freemind.modes.MindMapNode;
import freemind.modes.NodeAdapter;
import freemind.modes.mindmapmode.actions.xml.ActionPair;

/**
 * @author foltin
 */
public class FontSizeActor extends XmlActorAdapter {

	/**
	 * @param pMapFeedback
	 */
	public FontSizeActor(ExtendedMapFeedback pMapFeedback) {
		super(pMapFeedback);
	}

	public Class<FontSizeNodeAction> getDoActionClass() {
		return FontSizeNodeAction.class;
	}


	public void setFontSize(MindMapNode node, String fontSizeValue) {
		boolean chosen = node instanceof NodeAdapter && ((NodeAdapter) node).isFontSizeChosen();
		if (!chosen && (fontSizeValue == null || fontSizeValue.isEmpty())) {
			return;
		}
		if (chosen && Tools.safeEquals(fontSizeValue, node.getFontSize())) {
			return;
		}
		execute(getActionPair(node, fontSizeValue));

	}

	public ActionPair getActionPair(MindMapNode node, String fontSizeValue) {
		FontSizeNodeAction fontSizeAction = createFontSizeNodeAction(node,
				fontSizeValue);
		String previous = node.getFontSize();
		if (node instanceof NodeAdapter && !((NodeAdapter) node).isFontSizeChosen()) {
			previous = "";
		}
		FontSizeNodeAction undoFontSizeAction = createFontSizeNodeAction(node,
				previous);
		return new ActionPair(fontSizeAction, undoFontSizeAction);
	}

	private FontSizeNodeAction createFontSizeNodeAction(MindMapNode node,
			String fontSizeValue) {
		FontSizeNodeAction fontSizeAction = new FontSizeNodeAction();
		fontSizeAction.setNode(getNodeID(node));
		fontSizeAction.setSize(fontSizeValue);
		return fontSizeAction;

	}



	public void act(XmlAction action) {
		if (action instanceof FontSizeNodeAction) {
			FontSizeNodeAction fontSizeAction = (FontSizeNodeAction) action;
			MindMapNode node = getNodeFromID(fontSizeAction.getNode());
			String sizeText = fontSizeAction.getSize();
			if (sizeText == null || sizeText.isEmpty()) {
				if (node instanceof NodeAdapter && ((NodeAdapter) node).isFontSizeChosen()) {
					((NodeAdapter) node).setFontSizeChosen(false);
					getExMapFeedback().nodeChanged(node);
				}
				return;
			}
			try {
				int size = Integer.parseInt(sizeText);
				boolean chosen = node instanceof NodeAdapter && ((NodeAdapter) node).isFontSizeChosen();
				if (!chosen || !node.getFontSize().equals(sizeText)) {
					node.setFontSize(size);
					getExMapFeedback().nodeChanged(node);
				}
			} catch (NumberFormatException e) {
				return;
			}
		}
	}


}
