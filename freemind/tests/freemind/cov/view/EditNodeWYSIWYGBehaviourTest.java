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

package tests.freemind.cov.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import freemind.main.FreeMindMain;
import freemind.view.mindmapview.EditNodeBase;
import freemind.view.mindmapview.EditNodeWYSIWYG;
import freemind.view.mindmapview.NodeView;
import tests.freemind.cov.MapFixture.Opened;

public class EditNodeWYSIWYGBehaviourTest {

	@Test
	void constructorKeepsTheNode() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha");
		NodeView alpha = ViewMaps.childView(opened, "Alpha");
		EditSupports.Recording control = new EditSupports.Recording();
		EditNodeWYSIWYG editor = new EditNodeWYSIWYG(alpha, "<html><body>Alpha</body></html>", null, opened.mode,
				control);
		assertSame(alpha, editor.getNode());
		assertSame(control, editor.getEditControl());
		assertEquals("<html><body>Alpha</body></html>",
				ViewMaps.call(editor, "getText", new Class<?>[] {}, new Object[] {}));
	}

	@Test
	@Tag("gui")
	void htmlDialogSubmitsSplitsAndCancels() throws Exception {
		// backwards compatibility
		EditSupports.spellOff();
		Opened opened = ViewMaps.open("Alpha");
		opened.view.validate();
		EditSupports.windowProperties(opened, "above");
		FreeMindMain original = opened.frame;
		EditSupports.installFrame(opened);
		EditSupports.Recording control = new EditSupports.Recording();
		JDialog dialog = null;
		try {
			EditNodeWYSIWYG editor = new EditNodeWYSIWYG(ViewMaps.childView(opened, "Alpha"),
					"<html><body>Alpha</body></html>", null, opened.mode, control);
			Constructor<?> ctor = Class.forName("freemind.view.mindmapview.EditNodeWYSIWYG$HTMLDialog")
					.getDeclaredConstructor(EditNodeBase.class);
			ctor.setAccessible(true);
			dialog = (JDialog) ctor.newInstance(editor);
			Object panel = ViewMaps.call(dialog, "getHtmlEditorPanel", new Class<?>[] {}, new Object[] {});
			assertNotNull(panel);
			assertNotNull(ViewMaps.call(dialog, "getMostRecentFocusOwner", new Class<?>[] {}, new Object[] {}));
			assertEquals(ViewMaps.call(panel, "needsSaving", new Class<?>[] {}, new Object[] {}),
					ViewMaps.call(dialog, "isChanged", new Class<?>[] {}, new Object[] {}));
			ActionListener hyperlink = hyperlink(panel);
			if (hyperlink != null) {
				hyperlink.actionPerformed(new ActionEvent(panel, ActionEvent.ACTION_PERFORMED, "not a url"));
			}
			Action submit = dialog.getRootPane().getActionMap().get("submit");
			assertNotNull(submit);
			submit.actionPerformed(new ActionEvent(dialog, ActionEvent.ACTION_PERFORMED, "submit"));
			assertTrue(control.okCount + control.cancelCount >= 1);
			JPanel actions = (JPanel) ((BorderLayout) dialog.getContentPane().getLayout())
					.getLayoutComponent(BorderLayout.SOUTH);
			assertEquals(3, actions.getComponentCount());
			((JButton) actions.getComponent(2)).doClick();
			assertNotNull(control.split);
			((JButton) actions.getComponent(1)).doClick();
			assertTrue(control.cancelCount >= 1);
		} finally {
			if (dialog != null) {
				dialog.dispose();
			}
			EditSupports.restoreFrame(opened, original);
		}
	}

	private static ActionListener hyperlink(Object panel) {
		Class<?> type = panel.getClass();
		while (type != null) {
			for (Field field : type.getDeclaredFields()) {
				if (ActionListener.class.isAssignableFrom(field.getType())) {
					field.setAccessible(true);
					try {
						return (ActionListener) field.get(panel);
					} catch (IllegalAccessException ex) {
						return null;
					}
				}
			}
			type = type.getSuperclass();
		}
		return null;
	}

}
