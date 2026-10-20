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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.lang.reflect.Constructor;

import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import freemind.main.FreeMindMain;
import freemind.view.mindmapview.EditNodeBase;
import freemind.view.mindmapview.EditNodeDialog;
import freemind.view.mindmapview.EditNodeTextField;
import freemind.view.mindmapview.NodeView;
import tests.freemind.cov.MapFixture.Opened;

public class EditNodeBaseBehaviourTest {

	@Test
	void textKeysAndCopyFollowTheEditor() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha");
		NodeView alpha = ViewMaps.childView(opened, "Alpha");
		EditSupports.Recording control = new EditSupports.Recording();
		EditNodeTextField editor = new EditNodeTextField(alpha, "Hello", null, opened.mode, control);
		editor.setText("Next");
		assertEquals("Next", ViewMaps.call(editor, "getText", new Class<?>[] {}, new Object[] {}));
		assertTrue((Boolean) ViewMaps.call(editor, "binOptionIsTrue", new Class<?>[] { String.class },
				new Object[] { "el__enter_confirms_by_default" }));
		assertFalse((Boolean) ViewMaps.call(editor, "binOptionIsTrue", new Class<?>[] { String.class },
				new Object[] { "no_such_option" }));
		assertSame(opened.frame, ViewMaps.call(editor, "getFrame", new Class<?>[] {}, new Object[] {}));
		assertSame(opened.app, ViewMaps.call(editor, "getController", new Class<?>[] {}, new Object[] {}));
		assertSame(opened.mode, ViewMaps.call(editor, "getModeController", new Class<?>[] {}, new Object[] {}));
		assertSame(opened.view, ViewMaps.call(editor, "getView", new Class<?>[] {}, new Object[] {}));
		assertNotNull(ViewMaps.call(editor, "getText", new Class<?>[] { String.class }, new Object[] { "ok" }));
		editor.closeEdit();
		assertNullListener(editor);
		final boolean[] lost = { false };
		FocusAdapter adapter = new FocusAdapter() {
			public void focusLost(FocusEvent event) {
				lost[0] = true;
			}
		};
		editor.setTextFieldListener(adapter);
		assertSame(adapter, editor.getTextFieldListener());
		editor.closeEdit();
		assertTrue(lost[0]);

		JTextArea area = new JTextArea("Hello");
		try {
			queue(editor, area, null);
			assertTrue(hasQueue(area));
			for (java.awt.event.FocusListener listener : area.getFocusListeners()) {
				if (listener.getClass().getName().contains("KeyEventQueue")) {
					listener.focusLost(new FocusEvent(area, FocusEvent.FOCUS_LOST));
				}
			}
			KeyEvent typed = new KeyEvent(area, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0,
					KeyEvent.VK_UNDEFINED, 'q');
			KeyboardFocusManager.getCurrentKeyboardFocusManager().dispatchEvent(typed);
			EditSupports.releaseQueuedKeys(area);
			assertFalse(hasQueue(area));
			assertEquals("Hello", area.getText());

			area.setCaretPosition(3);
			queue(editor, area, new KeyEvent(area, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0,
					KeyEvent.VK_HOME, KeyEvent.CHAR_UNDEFINED));
			assertEquals(0, area.getCaretPosition());
			area.setCaretPosition(0);
			queue(editor, area, new KeyEvent(area, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0,
					KeyEvent.VK_END, KeyEvent.CHAR_UNDEFINED));
			assertEquals(area.getDocument().getLength(), area.getCaretPosition());
			area.setCaretPosition(1);
			queue(editor, area, new KeyEvent(area, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0,
					KeyEvent.VK_A, KeyEvent.CHAR_UNDEFINED));
			assertEquals(1, area.getCaretPosition());
			queue(editor, area, new KeyEvent(area, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0,
					KeyEvent.VK_X, 'x'));
			assertEquals(area.getText(), area.getSelectedText());
		} finally {
			EditSupports.releaseQueuedKeys(area);
		}

		JTextArea blank = new JTextArea("hello");
		JPopupMenu emptyMenu = popup(editor, blank);
		assertEquals(1, emptyMenu.getComponentCount());
		assertFalse(emptyMenu.getComponent(0).isEnabled());
		blank.selectAll();
		JPopupMenu fullMenu = popup(editor, blank);
		assertTrue(fullMenu.getComponent(0).isEnabled());
		Action copy = copyAction(editor, blank);
		assertThrows(java.awt.HeadlessException.class,
				() -> copy.actionPerformed(new ActionEvent(blank, ActionEvent.ACTION_PERFORMED, "copy")));
		JTextArea untouched = new JTextArea("hello");
		copyAction(editor, untouched).actionPerformed(new ActionEvent(untouched, ActionEvent.ACTION_PERFORMED, "copy"));
		assertEquals(null, untouched.getSelectedText());
		assertThrows(java.awt.HeadlessException.class, () -> editor.getClipboard());
	}

	@Test
	@Tag("gui")
	void dialogActionsSubmitCancelAndSplit() throws Exception {
		// backwards compatibility
		EditSupports.spellOff();
		EditSupports.resetConfirmHolder();
		Opened opened = ViewMaps.open("Alpha");
		EditSupports.windowProperties(opened, "above");
		FreeMindMain original = opened.frame;
		EditSupports.installFrame(opened);
		EditSupports.Recording control = new EditSupports.Recording();
		JDialog dialog = null;
		try {
			EditNodeDialog editor = new EditNodeDialog(ViewMaps.childView(opened, "Alpha"), "Alpha", null,
					opened.mode, control);
			dialog = longDialog(editor);
			assertFalse((Boolean) ViewMaps.call(dialog, "isChanged", new Class<?>[] {}, new Object[] {}));
			ViewMaps.call(dialog, "confirmedSubmit", new Class<?>[] {}, new Object[] {});
			assertTrue(control.cancelled);
			control.cancelCount = 0;
			ViewMaps.call(dialog, "confirmedCancel", new Class<?>[] {}, new Object[] {});
			assertEquals(1, control.cancelCount);
			int before = control.cancelCount;
			dialog.getWindowListeners()[0].windowClosing(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
			assertEquals(before, control.cancelCount);
			action("SubmitAction", dialog).actionPerformed(new ActionEvent(dialog, ActionEvent.ACTION_PERFORMED, "submit"));
			assertTrue(control.okCount >= 1);
			action("SplitAction", dialog).actionPerformed(new ActionEvent(dialog, ActionEvent.ACTION_PERFORMED, "split"));
			assertEquals("Alpha", control.split);
			assertTrue(control.splitAt >= 0);
			action("CancelAction", dialog).actionPerformed(new ActionEvent(dialog, ActionEvent.ACTION_PERFORMED, "cancel"));
			assertTrue(control.cancelCount > before);
			ViewMaps.call(dialog, "setBase", new Class<?>[] { EditNodeBase.class }, new Object[] { editor });
			assertSame(editor, ViewMaps.call(dialog, "getBase", new Class<?>[] {}, new Object[] {}));
		} finally {
			if (dialog != null) {
				dialog.dispose();
			}
			EditSupports.restoreFrame(opened, original);
		}
	}

	private static void assertNullListener(EditNodeBase editor) {
		assertEquals(null, editor.getTextFieldListener());
	}

	private static void queue(EditNodeBase editor, JTextComponent area, KeyEvent event) throws Exception {
		EditSupports.releaseQueuedKeys(area);
		ViewMaps.call(editor, "redispatchKeyEvents", new Class<?>[] { JTextComponent.class, KeyEvent.class },
				new Object[] { area, event });
	}

	private static boolean hasQueue(JTextComponent area) {
		for (java.awt.event.FocusListener listener : area.getFocusListeners()) {
			if (listener.getClass().getName().contains("KeyEventQueue")) {
				return true;
			}
		}
		return false;
	}

	private static JPopupMenu popup(EditNodeBase editor, JTextComponent area) throws Exception {
		Class<?> type = Class.forName("freemind.view.mindmapview.EditNodeBase$EditPopupMenu");
		Constructor<?> ctor = type.getDeclaredConstructor(EditNodeBase.class, JTextComponent.class);
		ctor.setAccessible(true);
		return (JPopupMenu) ctor.newInstance(editor, area);
	}

	private static Action copyAction(EditNodeBase editor, JTextComponent area) throws Exception {
		Class<?> type = Class.forName("freemind.view.mindmapview.EditNodeBase$EditCopyAction");
		Constructor<?> ctor = type.getDeclaredConstructor(EditNodeBase.class, JTextComponent.class);
		ctor.setAccessible(true);
		return (Action) ctor.newInstance(editor, area);
	}

	private static JDialog longDialog(EditNodeDialog editor) throws Exception {
		Constructor<?> ctor = Class.forName("freemind.view.mindmapview.EditNodeDialog$LongNodeDialog")
				.getDeclaredConstructor(EditNodeDialog.class);
		ctor.setAccessible(true);
		return (JDialog) ctor.newInstance(editor);
	}

	private static Action action(String name, JDialog dialog) throws Exception {
		Class<?> outer = Class.forName("freemind.view.mindmapview.EditNodeBase$EditDialog");
		Class<?> type = Class.forName("freemind.view.mindmapview.EditNodeBase$EditDialog$" + name);
		Constructor<?> ctor = type.getDeclaredConstructor(outer);
		ctor.setAccessible(true);
		return (Action) ctor.newInstance(dialog);
	}
}
