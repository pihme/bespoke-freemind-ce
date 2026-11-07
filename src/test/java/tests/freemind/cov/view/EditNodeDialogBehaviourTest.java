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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import freemind.main.FreeMindMain;
import freemind.view.mindmapview.EditNodeDialog;
import freemind.view.mindmapview.NodeView;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("EditNodeDialogBehaviourTest")
public class EditNodeDialogBehaviourTest {

	@Test
	void constructorKeepsTheNodeAndTheText() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha");
		NodeView alpha = ViewMaps.childView(opened, "Alpha");
		EditSupports.Recording control = new EditSupports.Recording();
		KeyEvent event = ViewMaps.key(opened.view, KeyEvent.VK_END, 0);
		EditNodeDialog editor = new EditNodeDialog(alpha, "Alpha", event, opened.mode, control);
		assertSame(alpha, editor.getNode());
		assertSame(control, editor.getEditControl());
		assertEquals("Alpha", ViewMaps.call(editor, "getText", new Class<?>[] {}, new Object[] {}));
	}

	@Test
	@Tag("gui")
	void longNodeDialogEditsSubmitsAndPlacesButtons() throws Exception {
		// backwards compatibility
		EditSupports.spellOff();
		Opened opened = ViewMaps.open("Alpha");
		NodeView alpha = ViewMaps.childView(opened, "Alpha");
		opened.view.validate();
		FreeMindMain original = opened.frame;
		EditSupports.installFrame(opened);
		List<JDialog> dialogs = new ArrayList<>();
		try {
			EditSupports.windowProperties(opened, "below");
			EditSupports.resetConfirmHolder();
			EditSupports.Recording alt = new EditSupports.Recording();
			JDialog below = dialog(alpha, "Alpha", null, opened, alt);
			dialogs.add(below);
			below.pack();
			JTextArea area = EditSupports.find(below, JTextArea.class);
			JScrollPane scroll = EditSupports.find(below, JScrollPane.class);
			JCheckBox box = EditSupports.find(below, JCheckBox.class);
			assertEquals(area.getText().length(), area.getCaretPosition());
			assertTrue(box.getParent().getY() > scroll.getY());
			assertSame(area, ViewMaps.call(below, "getMostRecentFocusOwner", new Class<?>[] {}, new Object[] {}));
			assertFalse((Boolean) ViewMaps.call(below, "isChanged", new Class<?>[] {}, new Object[] {}));
			KeyListener keys = area.getKeyListeners()[0];
			keys.keyTyped(key(area, 0, KeyEvent.VK_A, 'a'));
			keys.keyReleased(key(area, 0, KeyEvent.VK_A, 'a'));
			MouseListener mice = mice(area);
			String beforeMouse = area.getText();
			mice.mouseClicked(mouse(area, MouseEvent.MOUSE_CLICKED, false));
			mice.mouseEntered(mouse(area, MouseEvent.MOUSE_ENTERED, false));
			mice.mouseExited(mouse(area, MouseEvent.MOUSE_EXITED, false));
			mice.mousePressed(mouse(area, MouseEvent.MOUSE_PRESSED, false));
			mice.mouseReleased(mouse(area, MouseEvent.MOUSE_RELEASED, false));
			assertEquals(beforeMouse, area.getText());
			boolean selected = box.isSelected();
			box.doClick();
			assertNotEquals(selected, box.isSelected());
			assertEquals(box.isSelected(), EditSupports.confirmHolder());
			keys.keyPressed(key(area, 0, KeyEvent.VK_ENTER, '\n'));
			assertTrue(area.getText().contains("\n"));
			assertTrue((Boolean) ViewMaps.call(below, "isChanged", new Class<?>[] {}, new Object[] {}));
			keys.keyPressed(key(area, KeyEvent.ALT_MASK, KeyEvent.VK_ENTER, '\n'));
			assertTrue(alt.ok.contains("\n"));

			EditSupports.windowProperties(opened, "above");
			EditSupports.resetConfirmHolder();
			EditSupports.Recording shift = new EditSupports.Recording();
			KeyEvent home = new KeyEvent(alpha, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_HOME,
					KeyEvent.CHAR_UNDEFINED);
			JDialog above = dialog(alpha, "Alpha", home, opened, shift);
			dialogs.add(above);
			above.pack();
			JTextArea upper = EditSupports.find(above, JTextArea.class);
			assertEquals(0, upper.getCaretPosition());
			assertTrue(EditSupports.find(above, JCheckBox.class).getParent().getY() < EditSupports
					.find(above, JScrollPane.class).getY());
			KeyListener upperKeys = upper.getKeyListeners()[0];
			upperKeys.keyPressed(key(upper, KeyEvent.SHIFT_MASK, KeyEvent.VK_ENTER, '\n'));
			assertTrue(upper.getText().contains("\n"));
			upperKeys.keyPressed(key(upper, 0, KeyEvent.VK_ENTER, '\n'));
			assertTrue(shift.ok.contains("\n"));

			EditSupports.resetConfirmHolder();
			EditSupports.Recording cancelled = new EditSupports.Recording();
			KeyEvent end = new KeyEvent(alpha, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_END,
					KeyEvent.CHAR_UNDEFINED);
			JDialog endDialog = dialog(alpha, "Alpha", end, opened, cancelled);
			dialogs.add(endDialog);
			JTextArea endArea = EditSupports.find(endDialog, JTextArea.class);
			assertEquals(endArea.getText().length(), endArea.getCaretPosition());
			endArea.getKeyListeners()[0].keyPressed(key(endArea, 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED));
			assertTrue(cancelled.cancelled);

			EditSupports.resetConfirmHolder();
			EditSupports.Recording split = new EditSupports.Recording();
			KeyEvent typed = new KeyEvent(alpha, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_A, 'a');
			JDialog typedDialog = dialog(alpha, "Alpha", typed, opened, split);
			dialogs.add(typedDialog);
			JTextArea typedArea = EditSupports.find(typedDialog, JTextArea.class);
			assertEquals("Alpha", typedArea.getSelectedText());
			buttons(typedDialog).get(2).doClick();
			assertEquals(typedArea.getText(), split.split);
			assertTrue(split.splitAt >= 0);
			MouseEvent popup = mouse(typedArea, MouseEvent.MOUSE_PRESSED, true);
			try {
				mice(typedArea).mousePressed(popup);
				assertTrue(popup.isConsumed());
			} catch (RuntimeException ex) {
				assertTrue(popup.isPopupTrigger());
			}
			MouseEvent released = mouse(typedArea, MouseEvent.MOUSE_RELEASED, true);
			try {
				mice(typedArea).mouseReleased(released);
			} catch (RuntimeException ex) {
				assertTrue(released.isPopupTrigger());
			}
			buttons(typedDialog).get(0).doClick();
			EventQueue.invokeAndWait(() -> {
			});
			assertNotNull(split.ok);
			buttons(endDialog).get(1).doClick();
			assertTrue(cancelled.cancelled);
		} finally {
			for (JDialog dialog : dialogs) {
				EditSupports.releaseQueuedKeys(EditSupports.find(dialog, JTextArea.class));
				dialog.dispose();
			}
			EditSupports.restoreFrame(opened, original);
		}
	}

	private static JDialog dialog(NodeView node, String text, KeyEvent event, Opened opened,
			EditSupports.Recording control) throws Exception {
		EditNodeDialog editor = new EditNodeDialog(node, text, event, opened.mode, control);
		Constructor<?> ctor = Class.forName("freemind.view.mindmapview.EditNodeDialog$LongNodeDialog")
				.getDeclaredConstructor(EditNodeDialog.class);
		ctor.setAccessible(true);
		return (JDialog) ctor.newInstance(editor);
	}

	private static List<JButton> buttons(Component root) {
		List<JButton> found = new ArrayList<>();
		collect(root, found);
		return found;
	}

	private static void collect(Component root, List<JButton> found) {
		if (root instanceof JButton) {
			found.add((JButton) root);
		}
		if (root instanceof java.awt.Container) {
			for (Component child : ((java.awt.Container) root).getComponents()) {
				collect(child, found);
			}
		}
	}

	private static MouseListener mice(JTextArea area) {
		for (MouseListener listener : area.getMouseListeners()) {
			if (listener.getClass().getName().contains("LongNodeDialog")) {
				return listener;
			}
		}
		throw new AssertionError("missing long-node mouse listener");
	}

	private static KeyEvent key(Component source, int modifiers, int code, char character) {
		return new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), modifiers, code, character);
	}

	private static MouseEvent mouse(Component source, int id, boolean popup) {
		int button = id == MouseEvent.MOUSE_PRESSED || id == MouseEvent.MOUSE_RELEASED
				? MouseEvent.BUTTON3 : MouseEvent.NOBUTTON;
		return new MouseEvent(source, id, System.currentTimeMillis(), 0, 2, 2, 1, popup, button);
	}
}
