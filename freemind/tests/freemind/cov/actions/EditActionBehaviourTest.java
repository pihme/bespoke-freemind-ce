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

package tests.freemind.cov.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;

import freemind.main.FreeMind;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.actions.EditAction;
import freemind.view.mindmapview.EditNodeBase;
import freemind.view.mindmapview.NodeView;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Inline edit stores the field text, and a long edit stops at the dialog. */
class EditActionBehaviourTest {

	@Test
	void inlineEditStoresTheFieldText() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Leaf");
		MindMapNode leaf = opened.child("Leaf");
		opened.mode.select(leaf, List.of(leaf));
		EditAction action = opened.mode.edit;
		assertThrows(HeadlessException.class,
				() -> action.actionPerformed(new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "edit")));
		assertEquals("Leaf", leaf.getText());
		Field dialog = EditAction.class.getDeclaredField("mCurrentEditDialog");
		dialog.setAccessible(true);
		EditNodeBase editor = (EditNodeBase) dialog.get(action);
		editor.getEditControl().ok("Typed");
		assertEquals("Typed", leaf.getText());
		assertFalse(opened.mode.isBlocked());
	}

	@Test
	void aMissingViewAndALongEditLeaveTheText() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Plain");
		MindMapNode plain = opened.child("Plain");
		opened.mode.select(plain, List.of(plain));
		EditAction action = new EditAction(opened.mode);
		action.edit(null, null, null, false, false, false);
		action.stopEditing();
		assertEquals("Plain", plain.getText());
		assertFalse(opened.mode.isBlocked());
		opened.frame.setProperty(FreeMind.RESOURCES_REMIND_USE_RICH_TEXT_IN_NEW_LONG_NODES, "false");
		NodeView view = opened.view.getNodeView(plain);
		assertThrows(ClassCastException.class, () -> action.edit(view, view, null, false, false, true));
		assertEquals("Plain", plain.getText());
		opened.mode.setBlocked(false);
		Method setHtmlText = EditAction.class.getDeclaredMethod("setHtmlText", NodeView.class, String.class);
		setHtmlText.setAccessible(true);
		setHtmlText.invoke(action, view, "<html><head><title>x</title></head><body>Kept</body></html>");
		assertEquals("<html><body>Kept</body></html>", plain.getText());
	}
}
