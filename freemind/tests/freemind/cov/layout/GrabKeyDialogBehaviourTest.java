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

package tests.freemind.cov.layout;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import freemind.main.HeadlessFreeMind;
import freemind.preferences.layout.GrabKeyDialog;
import freemind.preferences.layout.GrabKeyDialog.KeyBinding;
import freemind.preferences.layout.KeyEventWorkaround;

/** Shortcut capture records a key name. The dialog itself cannot open headless. */
@DisplayName("GrabKeyDialogBehaviourTest")
class GrabKeyDialogBehaviourTest {

	@Test
	void toStringNamesPressedReleasedTypedAndAnythingElse() {
		// backwards compatibility
		assertEquals(" ", GrabKeyDialog.MODIFIER_SEPARATOR);
		JPanel source = new JPanel();
		KeyEvent pressed = new KeyEvent(source, KeyEvent.KEY_PRESSED, 1L, InputEvent.CTRL_MASK, KeyEvent.VK_A, 'a');
		assertEquals("KEY_PRESSED,keyCode=0x41,keyChar=0x61,modifiers=0x2", GrabKeyDialog.toString(pressed));
		KeyEvent released = new KeyEvent(source, KeyEvent.KEY_RELEASED, 2L, 0, KeyEvent.VK_ENTER,
				KeyEvent.CHAR_UNDEFINED);
		assertEquals("KEY_RELEASED,keyCode=0xa,keyChar=0xffff,modifiers=0x0", GrabKeyDialog.toString(released));
		KeyEvent typed = new KeyEvent(source, KeyEvent.KEY_TYPED, 3L, 0, KeyEvent.VK_UNDEFINED, 'b');
		assertEquals("KEY_TYPED,keyCode=0x0,keyChar=0x62,modifiers=0x0", GrabKeyDialog.toString(typed));
		KeyEvent other = new KeyEvent(source, 0, 4L, 0, KeyEvent.VK_A, 'a');
		assertEquals("unknown type,keyCode=0x41,keyChar=0x61,modifiers=0x0", GrabKeyDialog.toString(other));
	}

	@Test
	void aBindingIsAssignedOnlyWhenTheShortcutHasText() {
		// backwards compatibility
		assertFalse(new KeyBinding("copy", "Copy", null, false).isAssigned());
		assertFalse(new KeyBinding("copy", "Copy", "", false).isAssigned());
		assertTrue(new KeyBinding("copy", "Copy", "control C", false).isAssigned());
	}

	@Test
	void constructorsThrowHeadlessExceptionAndAddNoWindow() {
		// backwards compatibility
		assertTrue(GraphicsEnvironment.isHeadless());
		Window[] before = Window.getWindows();
		KeyBinding binding = new KeyBinding("copy", "Copy", "control C", false);
		Vector<KeyBinding> bindings = new Vector<>();
		assertHeadless(() -> new GrabKeyDialog(null, null, binding, bindings, null));
		assertHeadless(() -> new GrabKeyDialog(null, null, binding, bindings, null, InputEvent.CTRL_MASK));
		assertArrayEquals(before, Window.getWindows());
	}

	@Test
	void theDebugBufferIgnoresInsertedText() throws Exception {
		// backwards compatibility
		Class<?> type = Class.forName("freemind.preferences.layout.GrabKeyDialog$Buffer");
		Constructor<?> constructor = type.getDeclaredConstructor();
		constructor.setAccessible(true);
		Object buffer = constructor.newInstance();
		Method length = type.getDeclaredMethod("getLength");
		length.setAccessible(true);
		Method insert = type.getDeclaredMethod("insert", int.class, String.class);
		insert.setAccessible(true);
		assertEquals(0, length.invoke(buffer));
		insert.invoke(buffer, 0, "Event KEY_PRESSED passed\n");
		assertEquals(0, length.invoke(buffer));
	}

	@Test
	void focusStaysInTheFieldAndTheShortcutIsHiddenUntilAccepted() throws Exception {
		// backwards compatibility
		GrabKeyDialog dialog = dialog();
		assertFalse(dialog.isManagingFocus());
		assertFalse(dialog.getFocusTraversalKeysEnabled());
		assertFalse(dialog.isOK());
		JTextField shortcut = shortcut(dialog);
		shortcut.setText("F5");
		assertNull(dialog.getShortcut());
		assertFalse(shortcut.getFocusTraversalKeysEnabled());
		set(GrabKeyDialog.class, dialog, "isOK", Boolean.TRUE);
		assertTrue(dialog.isOK());
		assertEquals("F5", dialog.getShortcut());

		Class<?> panelType = Class.forName("freemind.preferences.layout.GrabKeyDialog$1");
		Constructor<?> constructor = panelType.getDeclaredConstructor(GrabKeyDialog.class, LayoutManager.class);
		constructor.setAccessible(true);
		JPanel content = (JPanel) constructor.newInstance(dialog, new GridLayout());
		Method managingFocus = panelType.getDeclaredMethod("isManagingFocus");
		managingFocus.setAccessible(true);
		assertEquals(Boolean.FALSE, managingFocus.invoke(content));
		assertFalse(content.getFocusTraversalKeysEnabled());
	}

	@Test
	void symbolicNamesAndExistingBindingsResolveTheShortcut() throws Exception {
		// backwards compatibility
		GrabKeyDialog dialog = dialog();
		assertNull(call(dialog, "getSymbolicName", new Class<?>[] { int.class }, KeyEvent.VK_UNDEFINED));
		assertEquals("a", call(dialog, "getSymbolicName", new Class<?>[] { int.class }, KeyEvent.VK_A));
		assertEquals("ENTER", call(dialog, "getSymbolicName", new Class<?>[] { int.class }, KeyEvent.VK_ENTER));
		assertEquals("0", call(dialog, "getSymbolicName", new Class<?>[] { int.class }, KeyEvent.VK_0));
		assertNull(call(dialog, "getSymbolicName", new Class<?>[] { int.class }, Integer.valueOf(99999)));

		KeyBinding unassigned = new KeyBinding("blank", "Blank", "", false);
		KeyBinding missing = new KeyBinding("none", "None", null, false);
		KeyBinding copy = new KeyBinding("copy", "Copy", "C+n", false);
		KeyBinding chord = new KeyBinding("select", "Select", "C+e C+x", false);
		Vector<KeyBinding> bindings = new Vector<>();
		bindings.add(unassigned);
		bindings.add(missing);
		bindings.add(copy);
		bindings.add(chord);
		set(GrabKeyDialog.class, dialog, "allBindings", bindings);

		assertNull(call(dialog, "getKeyBinding", new Class<?>[] { String.class }, new Object[] { null }));
		assertNull(call(dialog, "getKeyBinding", new Class<?>[] { String.class }, ""));
		assertNull(call(dialog, "getKeyBinding", new Class<?>[] { String.class }, "F1"));
		assertEquals(copy, call(dialog, "getKeyBinding", new Class<?>[] { String.class }, "C+n C+p"));
		KeyBinding prefix = (KeyBinding) call(dialog, "getKeyBinding", new Class<?>[] { String.class }, "C+e");
		assertNotSame(chord, prefix);
		assertTrue(prefix.isPrefix);
		assertEquals("select", prefix.name);
		assertEquals("Select", prefix.label);
		assertEquals("C+e", prefix.shortcut);
		assertTrue(prefix.isAssigned());
	}

	@Test
	void theAssignedLabelNamesTheActionThePrefixOrNobody() throws Exception {
		// backwards compatibility
		GrabKeyDialog dialog = dialog();
		JLabel assigned = (JLabel) get(GrabKeyDialog.class, dialog, "assignedTo");
		KeyBinding copy = new KeyBinding("copy", "Copy", "C+n", false);
		KeyBinding chord = new KeyBinding("select", "Select", "C+e C+x", false);
		Vector<KeyBinding> bindings = new Vector<>();
		bindings.add(copy);
		bindings.add(chord);
		set(GrabKeyDialog.class, dialog, "allBindings", bindings);
		set(GrabKeyDialog.class, dialog, "ok", null);
		call(dialog, "updateAssignedTo", new Class<?>[] { String.class }, new Object[] { null });
		assertEquals("GrabKeyDialog.grab-key.assigned-to GrabKeyDialog.grab-key.assigned-to.none", assigned.getText());

		JButton ok = new JButton("OK");
		set(GrabKeyDialog.class, dialog, "ok", ok);
		call(dialog, "updateAssignedTo", new Class<?>[] { String.class }, "C+n");
		assertEquals("GrabKeyDialog.grab-key.assigned-to Copy", assigned.getText());
		assertTrue(ok.isEnabled());
		call(dialog, "updateAssignedTo", new Class<?>[] { String.class }, "C+e");
		assertEquals("GrabKeyDialog.grab-key.assigned-to GrabKeyDialog.grab-key.assigned-to.prefix C+e",
				assigned.getText());
		assertFalse(ok.isEnabled());
		call(dialog, "updateAssignedTo", new Class<?>[] { String.class }, "");
		assertTrue(ok.isEnabled());
		assertTrue(assigned.getText().endsWith("assigned-to.none"));
	}

	@Test
	void theInputFieldRecordsATranslatedKeyAndDropsADeadOne() throws Exception {
		// backwards compatibility
		resetKeys();
		GrabKeyDialog dialog = dialog();
		JTextField shortcut = shortcut(dialog);
		JLabel assigned = (JLabel) get(GrabKeyDialog.class, dialog, "assignedTo");
		JButton ok = (JButton) get(GrabKeyDialog.class, dialog, "ok");
		KeyEvent letter = key(shortcut, KeyEvent.KEY_PRESSED, KeyEvent.VK_A, 'a', InputEvent.CTRL_MASK);
		process(dialog, letter);
		assertTrue(letter.isConsumed());
		assertEquals("control A", shortcut.getText());
		assertTrue(ok.isEnabled());
		assertTrue(assigned.getText().endsWith("assigned-to.none"));

		resetKeys();
		KeyEvent space = key(shortcut, KeyEvent.KEY_PRESSED, KeyEvent.VK_SPACE, ' ', InputEvent.CTRL_MASK);
		process(dialog, space);
		assertEquals("control SPACE", shortcut.getText());

		resetKeys();
		KeyEvent enter = key(shortcut, KeyEvent.KEY_PRESSED, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED, 0);
		process(dialog, enter);
		assertEquals("ENTER", shortcut.getText());

		resetKeys();
		shortcut.setText("kept");
		KeyEvent dead = key(shortcut, KeyEvent.KEY_PRESSED, KeyEvent.VK_DEAD_GRAVE, KeyEvent.CHAR_UNDEFINED, 0);
		process(dialog, dead);
		assertEquals("kept", shortcut.getText());
		assertFalse(dead.isConsumed());

		resetKeys();
		Class<?> bufferType = Class.forName("freemind.preferences.layout.GrabKeyDialog$Buffer");
		Constructor<?> bufferConstructor = bufferType.getDeclaredConstructor();
		bufferConstructor.setAccessible(true);
		Object buffer = bufferConstructor.newInstance();
		set(GrabKeyDialog.class, dialog, "debugBuffer", buffer);
		assigned.setText("unchanged");
		KeyEvent named = key(shortcut, KeyEvent.KEY_PRESSED, KeyEvent.VK_F5, KeyEvent.CHAR_UNDEFINED, 0);
		process(dialog, named);
		assertEquals("F5", shortcut.getText());
		assertEquals("unchanged", assigned.getText());
		Method length = bufferType.getDeclaredMethod("getLength");
		length.setAccessible(true);
		assertEquals(0, length.invoke(buffer));

		resetKeys();
		set(GrabKeyDialog.class, dialog, "debugBuffer", null);
		setInt(dialog, "modifierMask", InputEvent.CTRL_MASK);
		shortcut.setText("kept");
		KeyEvent masked = key(shortcut, KeyEvent.KEY_PRESSED, KeyEvent.VK_F6, KeyEvent.CHAR_UNDEFINED,
				InputEvent.CTRL_MASK);
		process(dialog, masked);
		assertEquals("F6", shortcut.getText());
		assertTrue(masked.isConsumed());
		assertTrue(assigned.getText().endsWith("assigned-to.none"));
	}

	@Test
	void clearEmptiesTheFieldAndConflictsRefuseToClose() throws Exception {
		// backwards compatibility
		GrabKeyDialog dialog = dialog();
		JTextField shortcut = shortcut(dialog);
		shortcut.setText("F5");
		JButton clear = new JButton("clear");
		set(GrabKeyDialog.class, dialog, "clear", clear);
		Object handler = handler(dialog);
		action(handler, clear);
		assertEquals("", shortcut.getText());
		JLabel assigned = (JLabel) get(GrabKeyDialog.class, dialog, "assignedTo");
		assertTrue(assigned.getText().endsWith("assigned-to.none"));

		shortcut.setText("F5");
		assertEquals(Boolean.TRUE, call(handler, "canClose", new Class<?>[] {}, new Object[] {}));
		assertTrue(dialog.isOK());
		assertEquals("F5", dialog.getShortcut());

		set(GrabKeyDialog.class, dialog, "isOK", Boolean.FALSE);
		shortcut.setText("");
		assertEquals(Boolean.TRUE, call(handler, "canClose", new Class<?>[] {}, new Object[] {}));
		assertTrue(dialog.isOK());

		set(GrabKeyDialog.class, dialog, "isOK", Boolean.FALSE);
		set(GrabKeyDialog.class, dialog, "binding", new KeyBinding("copy", "Copy", "control C", false));
		assertHeadless(() -> call(handler, "canClose", new Class<?>[] {}, new Object[] {}));
		assertFalse(dialog.isOK());

		String shared = "copy";
		KeyBinding current = new KeyBinding(shared, "Copy", "C+a", false);
		KeyBinding other = new KeyBinding(shared, "Copy", "C+b", false);
		Vector<KeyBinding> sameName = new Vector<>();
		sameName.add(other);
		set(GrabKeyDialog.class, dialog, "binding", current);
		set(GrabKeyDialog.class, dialog, "allBindings", sameName);
		shortcut.setText("C+b");
		assertHeadless(() -> call(handler, "canClose", new Class<?>[] {}, new Object[] {}));
		assertFalse(dialog.isOK());

		KeyBinding chord = new KeyBinding("select", "Select", "C+e C+x", false);
		Vector<KeyBinding> prefixed = new Vector<>();
		prefixed.add(chord);
		set(GrabKeyDialog.class, dialog, "binding", new KeyBinding("action", "Action", null, false));
		set(GrabKeyDialog.class, dialog, "allBindings", prefixed);
		shortcut.setText("C+e");
		assertHeadless(() -> call(handler, "canClose", new Class<?>[] {}, new Object[] {}));
		assertFalse(dialog.isOK());

		KeyBinding taken = new KeyBinding("other", "Other", "F5", false);
		Vector<KeyBinding> takenBy = new Vector<>();
		takenBy.add(taken);
		set(GrabKeyDialog.class, dialog, "allBindings", takenBy);
		shortcut.setText("F5");
		assertHeadless(() -> call(handler, "canClose", new Class<?>[] {}, new Object[] {}));
		assertFalse(dialog.isOK());
	}

	@Test
	void theButtonsAcceptRemoveOrCancelWithoutLeavingTheShortcutHalfSet() throws Exception {
		// backwards compatibility
		GrabKeyDialog accepted = dialog();
		JTextField shortcut = shortcut(accepted);
		shortcut.setText("F5");
		JButton ok = (JButton) get(GrabKeyDialog.class, accepted, "ok");
		action(handler(accepted), ok);
		assertTrue(accepted.isOK());
		assertEquals("F5", accepted.getShortcut());

		GrabKeyDialog removed = dialog();
		JTextField removedText = shortcut(removed);
		removedText.setText("F5");
		JButton remove = new JButton("remove");
		set(GrabKeyDialog.class, removed, "remove", remove);
		action(handler(removed), remove);
		assertEquals("", removedText.getText());
		assertTrue(removed.isOK());
		assertEquals("", removed.getShortcut());

		GrabKeyDialog cancelled = dialog();
		shortcut(cancelled).setText("F5");
		JButton cancel = new JButton("cancel");
		set(GrabKeyDialog.class, cancelled, "cancel", cancel);
		action(handler(cancelled), cancel);
		assertFalse(cancelled.isOK());
		assertNull(cancelled.getShortcut());
		assertEquals("F5", shortcut(cancelled).getText());
	}

	private static GrabKeyDialog dialog() throws Exception {
		GrabKeyDialog dialog = allocate(GrabKeyDialog.class);
		put(Window.class, dialog, "ownedWindowList", new Vector<>());
		put(Component.class, dialog, "acc", AccessController.getContext());
		put(Component.class, dialog, "objectLock", new Object());
		dialog.setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);
		put(GrabKeyDialog.class, dialog, "fmMain", new HeadlessFreeMind());
		set(GrabKeyDialog.class, dialog, "allBindings", new Vector<KeyBinding>());
		set(GrabKeyDialog.class, dialog, "assignedTo", new JLabel("old"));
		set(GrabKeyDialog.class, dialog, "ok", new JButton("OK"));
		set(GrabKeyDialog.class, dialog, "binding", new KeyBinding("action", "Action", null, false));
		Class<?> input = Class.forName("freemind.preferences.layout.GrabKeyDialog$InputPane");
		Constructor<?> constructor = input.getDeclaredConstructor(GrabKeyDialog.class);
		constructor.setAccessible(true);
		set(GrabKeyDialog.class, dialog, "shortcut", constructor.newInstance(dialog));
		return dialog;
	}

	private static JTextField shortcut(GrabKeyDialog dialog) throws Exception {
		return (JTextField) get(GrabKeyDialog.class, dialog, "shortcut");
	}

	private static Object handler(GrabKeyDialog dialog) throws Exception {
		Class<?> type = Class.forName("freemind.preferences.layout.GrabKeyDialog$ActionHandler");
		Constructor<?> constructor = type.getDeclaredConstructor(GrabKeyDialog.class);
		constructor.setAccessible(true);
		return constructor.newInstance(dialog);
	}

	private static void action(Object handler, Object source) throws Exception {
		Method method = handler.getClass().getDeclaredMethod("actionPerformed", ActionEvent.class);
		method.setAccessible(true);
		method.invoke(handler, new ActionEvent(source, ActionEvent.ACTION_PERFORMED, "click"));
	}

	private static void process(GrabKeyDialog dialog, KeyEvent event) throws Exception {
		Method method = GrabKeyDialog.class.getDeclaredMethod("processKeyEvent", KeyEvent.class);
		method.setAccessible(true);
		method.invoke(dialog, event);
	}

	private static KeyEvent key(JTextField source, int id, int code, char ch, int modifiers) {
		return new KeyEvent(source, id, 10L, modifiers, code, ch);
	}

	private static Object call(Object target, String name, Class<?>[] types, Object... args) throws Exception {
		Method method = null;
		Class<?> type = target.getClass();
		while (type != null && method == null) {
			try {
				method = type.getDeclaredMethod(name, types);
			} catch (NoSuchMethodException ex) {
				type = type.getSuperclass();
			}
		}
		method.setAccessible(true);
		return method.invoke(target, args);
	}

	private static void assertHeadless(Executable call) {
		Window[] before = Window.getWindows();
		Throwable thrown = assertThrows(Throwable.class, call);
		if (thrown instanceof InvocationTargetException wrapped) {
			thrown = wrapped.getCause();
		}
		assertEquals(HeadlessException.class, thrown.getClass());
		assertArrayEquals(before, Window.getWindows());
	}

	private static void resetKeys() throws Exception {
		Field modifiers = KeyEventWorkaround.class.getDeclaredField("modifiers");
		modifiers.setAccessible(true);
		modifiers.setInt(null, 0);
		Field last = KeyEventWorkaround.class.getDeclaredField("last");
		last.setAccessible(true);
		last.setInt(null, 0);
		Field time = KeyEventWorkaround.class.getDeclaredField("lastKeyTime");
		time.setAccessible(true);
		time.setLong(null, 0L);
	}

	private static GrabKeyDialog allocate(Class<GrabKeyDialog> type) throws Exception {
		Class<?> unsafeType = Class.forName("sun.misc.Unsafe");
		Field theUnsafe = unsafeType.getDeclaredField("theUnsafe");
		theUnsafe.setAccessible(true);
		Object unsafe = theUnsafe.get(null);
		return type.cast(unsafeType.getMethod("allocateInstance", Class.class).invoke(unsafe, type));
	}

	private static void put(Class<?> type, Object target, String name, Object value) throws Exception {
		Field found = type.getDeclaredField(name);
		Class<?> unsafeType = Class.forName("sun.misc.Unsafe");
		Field theUnsafe = unsafeType.getDeclaredField("theUnsafe");
		theUnsafe.setAccessible(true);
		Object unsafe = theUnsafe.get(null);
		long offset = (Long) unsafeType.getMethod("objectFieldOffset", Field.class).invoke(unsafe, found);
		unsafeType.getMethod("putObject", Object.class, long.class, Object.class).invoke(unsafe, target, offset, value);
	}

	private static Field field(Class<?> type, String name) throws Exception {
		Class<?> current = type;
		while (current != null) {
			try {
				Field found = current.getDeclaredField(name);
				found.setAccessible(true);
				return found;
			} catch (NoSuchFieldException ex) {
				current = current.getSuperclass();
			}
		}
		throw new NoSuchFieldException(name);
	}

	private static void set(Class<?> type, Object target, String name, Object value) throws Exception {
		field(type, name).set(target, value);
	}

	private static void setInt(Object target, String name, int value) throws Exception {
		field(target.getClass(), name).setInt(target, value);
	}

	private static Object get(Class<?> type, Object target, String name) throws Exception {
		return field(type, name).get(target);
	}
}
