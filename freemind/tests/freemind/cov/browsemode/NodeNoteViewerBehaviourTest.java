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

package tests.freemind.cov.browsemode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Component;
import java.lang.reflect.Field;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.controller.Controller;
import freemind.modes.MindMapNode;
import freemind.modes.browsemode.NodeNoteViewer;
import freemind.modes.common.plugins.NodeNoteBase;
import freemind.view.mindmapview.NodeView;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** The browse note viewer shows the note text and then removes that panel. */
@DisplayName("NodeNoteViewerBehaviourTest")
class NodeNoteViewerBehaviourTest {

	@Test
	void aNoteIsShownUpdatedAndCleared() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode child = opened.child("Child");
		NodeView nodeView = opened.view.getNodeView(child);
		NodeNoteViewer viewer = new NodeNoteViewer(opened.mode);

		viewer.onFocusNode(nodeView);
		assertNull(splitPane(opened.app));

		child.setNoteText("one");
		viewer.onFocusNode(nodeView);
		JScrollPane scroll = noteScroll(opened.app);
		JLabel label = (JLabel) scroll.getViewport().getView();
		assertEquals(200, scroll.getPreferredSize().height);
		assertEquals(Color.WHITE, label.getBackground());
		assertEquals(JLabel.TOP, label.getVerticalAlignment());
		assertTrue(label.isOpaque());
		assertEquals(child.getNoteText(), label.getText());

		child.setNoteText("two");
		viewer.onFocusNode(nodeView);
		assertEquals(child.getNoteText(), label.getText());

		viewer.onLostFocusNode(nodeView);
		assertNull(splitPane(opened.app));

		viewer.onUpdateNodeHook(child);
		assertNotNull(child.getStateIcons().get(NodeNoteBase.NODE_NOTE_ICON));

		String note = child.getNoteText();
		viewer.onSaveNode(child);
		viewer.onSelectionChange(nodeView, false);
		assertEquals(note, child.getNoteText());
		assertNull(splitPane(opened.app));
	}

	private static JComponent splitPane(Controller app) throws Exception {
		Field field = Controller.class.getDeclaredField("mOptionalSplitPane");
		field.setAccessible(true);
		return (JComponent) field.get(app);
	}

	private static JScrollPane noteScroll(Controller app) throws Exception {
		JScrollPane scroll = findScroll(splitPane(app));
		assertNotNull(scroll);
		return scroll;
	}

	private static JScrollPane findScroll(Component component) {
		if (component instanceof JScrollPane scroll) {
			return scroll;
		}
		if (component instanceof java.awt.Container container) {
			for (Component child : container.getComponents()) {
				JScrollPane found = findScroll(child);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}
}
