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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTextField;

import org.junit.jupiter.api.Test;

import freemind.view.mindmapview.EditNodeTextField;
import freemind.view.mindmapview.NodeView;
import tests.freemind.cov.MapFixture.Opened;

public class EditNodeTextFieldBehaviourTest {

	@Test
	void shortFieldUndoesAndEnterSubmits() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha");
		prepare(opened);
		NodeView alpha = ViewMaps.childView(opened, "Alpha");
		opened.view.validate();
		EditSupports.Recording control = new EditSupports.Recording();
		KeyEvent typed = new KeyEvent(alpha, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_A, 'a');
		JTextField field = null;
		try {
			EditNodeTextField editor = new EditNodeTextField(alpha, "Alpha", typed, opened.mode, control);
			field = showUntilShortcutMask(editor);
			assertEquals("Alpha", field.getText());
			mount(opened, field);
			field.getDocument().insertString(field.getText().length(), "Z", null);
			String edited = field.getText();
			field.getActionMap().get("Undo").actionPerformed(new ActionEvent(field, 0, "undo"));
			assertNotEquals(edited, field.getText());
			field.getActionMap().get("Undo").actionPerformed(new ActionEvent(field, 0, "undo"));
			assertNotNull(field.getText());
			assertNull(field.getActionMap().get("Redo"));
			KeyListener keys = keys(field);
			KeyEvent space = key(field, 0, KeyEvent.VK_SPACE, ' ');
			keys.keyPressed(space);
			assertTrue(space.isConsumed());
			assertEquals(0, control.okCount);
			keys.keyPressed(key(field, 0, KeyEvent.VK_ENTER, '\n'));
			assertEquals(1, control.okCount);
			assertNotNull(control.ok);
			keys.keyPressed(key(field, 0, KeyEvent.VK_ENTER, '\n'));
			assertEquals(1, control.okCount);
			keys.keyTyped(key(field, 0, KeyEvent.VK_A, 'a'));
			keys.keyReleased(key(field, 0, KeyEvent.VK_A, 'a'));
		} finally {
			EditSupports.releaseQueuedKeys(field);
		}
	}

	@Test
	void leftFoldedFieldAlignsRightAndEscapeCancels() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha");
		prepare(opened);
		NodeView left = ViewMaps.addChild(opened.view.getRoot(), "L", true);
		left.getModel().setFolded(true);
		opened.view.validate();
		left.getMainView().setSize(10, 12);
		EditSupports.Recording control = new EditSupports.Recording();
		JTextField field = null;
		try {
			EditNodeTextField editor = new EditNodeTextField(left, "L", null, opened.mode, control);
			field = showUntilShortcutMask(editor);
			assertEquals(JTextField.RIGHT, field.getHorizontalAlignment());
			assertTrue(field.getWidth() >= 150);
			mount(opened, field);
			KeyListener keys = keys(field);
			KeyEvent alt = key(field, InputEvent.ALT_DOWN_MASK, KeyEvent.VK_A, 'a');
			keys.keyPressed(alt);
			assertFalse(alt.isConsumed());
			assertEquals(0, control.cancelCount);
			KeyEvent controlKey = key(field, InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_A, 'a');
			keys.keyPressed(controlKey);
			assertEquals(0, control.cancelCount);
			keys.keyPressed(key(field, 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED));
			assertTrue(control.cancelled);
			assertNull(EditSupports.textField(opened.view));
		} finally {
			EditSupports.releaseQueuedKeys(field);
		}
	}

	@Test
	void openChildUsesTheSmallMinimumAndPopup() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha");
		prepare(opened);
		NodeView parent = ViewMaps.addChild(opened.view.getRoot(), "P", true);
		ViewMaps.addChild(parent, "C", true);
		opened.view.validate();
		parent.getMainView().setSize(10, 12);
		EditSupports.Recording control = new EditSupports.Recording();
		JTextField field = null;
		try {
			EditNodeTextField editor = new EditNodeTextField(parent, "P", null, opened.mode, control);
			field = showUntilShortcutMask(editor);
			assertEquals(JTextField.RIGHT, field.getHorizontalAlignment());
			assertEquals(50, field.getWidth());
			mount(opened, field);
			MouseListener mice = (MouseListener) keys(field);
			mice.mouseClicked(mouse(field, MouseEvent.MOUSE_CLICKED, false));
			mice.mouseEntered(mouse(field, MouseEvent.MOUSE_ENTERED, false));
			mice.mouseExited(mouse(field, MouseEvent.MOUSE_EXITED, false));
			MouseEvent press = mouse(field, MouseEvent.MOUSE_PRESSED, true);
			try {
				mice.mousePressed(press);
				assertTrue(press.isConsumed());
			} catch (RuntimeException ex) {
				assertTrue(press.isPopupTrigger());
			}
			MouseEvent released = mouse(field, MouseEvent.MOUSE_RELEASED, true);
			try {
				mice.mouseReleased(released);
			} catch (RuntimeException ex) {
				assertTrue(released.isPopupTrigger());
			}
			ComponentListener component = component(parent, keys(field));
			component.componentResized(new ComponentEvent(parent, ComponentEvent.COMPONENT_RESIZED));
			assertEquals("P", control.ok);
			assertEquals(1, control.okCount);
			component.componentMoved(new ComponentEvent(parent, ComponentEvent.COMPONENT_MOVED));
			component.componentHidden(new ComponentEvent(parent, ComponentEvent.COMPONENT_HIDDEN));
			component.componentShown(new ComponentEvent(parent, ComponentEvent.COMPONENT_SHOWN));
			assertEquals(1, control.okCount);

			setSpelling(opened, "true");
			EditSupports.Recording spelled = new EditSupports.Recording();
			EditNodeTextField spellingEditor = new EditNodeTextField(parent, "P", null, opened.mode, spelled);
			JTextField spellField = showUntilShortcutMask(spellingEditor);
			mount(opened, spellField);
			MouseEvent spellPress = mouse(spellField, MouseEvent.MOUSE_PRESSED, true);
			try {
				((MouseListener) keys(spellField)).mousePressed(spellPress);
			} catch (RuntimeException ex) {
				assertTrue(spellPress.isPopupTrigger());
			}
			FocusListener spellFocus = (FocusListener) keys(spellField);
			spellFocus.focusLost(null);
			spellFocus.focusLost(null);
			assertEquals("P", spelled.ok);
			assertEquals(1, spelled.okCount);
		} finally {
			setSpelling(opened, "false");
			EditSupports.releaseQueuedKeys(field);
		}
	}

	@Test
	void zoomedLongTextCommitsWhenFocusLeaves() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha");
		prepare(opened);
		opened.view.setZoom(2f);
		NodeView alpha = ViewMaps.childView(opened, "Alpha");
		opened.view.validate();
		EditSupports.Recording control = new EditSupports.Recording();
		JTextField field = null;
		JTextField other = null;
		try {
			EditNodeTextField editor = new EditNodeTextField(alpha, "LongEnough", null, opened.mode, control);
			field = showUntilShortcutMask(editor);
			assertTrue(field.getWidth() >= 50);
			assertTrue(field.getFont().getSize() >= 2);
			mount(opened, field);
			KeyListener keys = keys(field);
			KeyEvent meta = key(field, InputEvent.META_DOWN_MASK, KeyEvent.VK_A, 'a');
			keys.keyPressed(meta);
			assertEquals(0, control.okCount);
			((FocusListener) keys).focusGained(new FocusEvent(field, FocusEvent.FOCUS_GAINED));
			assertEquals(0, control.okCount);
			((FocusListener) keys).focusLost(null);
			assertEquals("LongEnough", control.ok);
			assertEquals(1, control.okCount);
			((FocusListener) keys).focusLost(null);
			assertEquals(1, control.okCount);

			EditSupports.Recording again = new EditSupports.Recording();
			EditNodeTextField second = new EditNodeTextField(alpha, "LongEnough", null, opened.mode, again);
			other = showUntilShortcutMask(second);
			mount(opened, other);
			((FocusListener) keys(other)).focusLost(new FocusEvent(other, FocusEvent.FOCUS_LOST));
			assertEquals("LongEnough", again.ok);
			assertEquals(1, again.okCount);
		} finally {
			EditSupports.releaseQueuedKeys(field);
			EditSupports.releaseQueuedKeys(other);
		}
	}

	private static void prepare(Opened opened) throws Exception {
		setSpelling(opened, "false");
		EditSupports.spellOff();
	}

	private static void setSpelling(Opened opened, String value) throws Exception {
		opened.frame.setProperty("check_spelling", value);
		java.lang.reflect.Field main = freemind.main.Resources.class.getDeclaredField("main");
		main.setAccessible(true);
		freemind.main.FreeMindMain holder = (freemind.main.FreeMindMain) main
				.get(freemind.main.Resources.getInstance());
		holder.setProperty("check_spelling", value);
	}

	private static KeyListener keys(JTextField field) {
		for (KeyListener listener : field.getKeyListeners()) {
			if (listener.getClass().getName().contains("TextFieldListener")) {
				return listener;
			}
		}
		throw new AssertionError("missing text field listener");
	}

	private static JTextField showUntilShortcutMask(EditNodeTextField editor) throws Exception {
		assertThrows(HeadlessException.class, editor::show);
		java.lang.reflect.Field field = EditNodeTextField.class.getDeclaredField("textfield");
		field.setAccessible(true);
		JTextField text = (JTextField) field.get(editor);
		assertNotNull(text);
		return text;
	}

	private static void mount(Opened opened, JTextField field) {
		if (field.getParent() == null) {
			opened.view.add(field, 0);
		}
	}

	private static ComponentListener component(NodeView node, KeyListener keys) {
		for (ComponentListener listener : node.getComponentListeners()) {
			if (listener.getClass().getName().contains("TextFieldListener")) {
				return listener;
			}
		}
		node.addComponentListener((ComponentListener) keys);
		return (ComponentListener) keys;
	}

	private static KeyEvent key(JTextField field, int modifiers, int code, char character) {
		return new KeyEvent(field, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), modifiers, code, character);
	}

	private static MouseEvent mouse(JTextField field, int id, boolean popup) {
		int button = id == MouseEvent.MOUSE_PRESSED || id == MouseEvent.MOUSE_RELEASED
				? MouseEvent.BUTTON3 : MouseEvent.NOBUTTON;
		return new MouseEvent(field, id, System.currentTimeMillis(), 0, 1, 1, 1, popup, button);
	}
}
