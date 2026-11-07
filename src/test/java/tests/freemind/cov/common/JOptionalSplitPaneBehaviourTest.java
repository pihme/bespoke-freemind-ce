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

package tests.freemind.cov.common;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JSplitPane;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.common.JOptionalSplitPane;

/** One child is a panel, two children are a split, and a third index is refused. */
@DisplayName("JOptionalSplitPaneBehaviourTest")
class JOptionalSplitPaneBehaviourTest {

	@Test
	void oneComponentStaysAPanelAndTheSameComponentIsNotAddedTwice() {
		// backwards compatibility
		JOptionalSplitPane pane = new JOptionalSplitPane();
		assertEquals(0, pane.getAmountOfComponents());
		assertEquals(-1, pane.getDividerPosition());
		pane.setLastDividerPosition(80);
		assertEquals(80, pane.getLastDividerPosition());
		assertEquals(80, pane.getDividerPosition());

		JLabel first = new JLabel("first");
		pane.setComponent(first, 0);
		assertEquals(1, pane.getAmountOfComponents());
		assertTrue(labels(pane).contains("first"));
		pane.setComponent(first, 0);
		assertEquals(1, pane.getAmountOfComponents());
		assertEquals(1, count(labels(pane), "first"));

		JLabel second = new JLabel("second");
		pane.setComponent(second, 0);
		assertEquals(1, pane.getAmountOfComponents());
		assertTrue(labels(pane).contains("second"));
		assertFalse(labels(pane).contains("first"));
		assertEquals(80, pane.getDividerPosition());
	}

	@Test
	void twoComponentsUseASplitAndRemovingThemCollapsesBackToNothing() {
		// backwards compatibility
		JOptionalSplitPane pane = new JOptionalSplitPane();
		pane.setLastDividerPosition(80);
		JLabel left = new JLabel("left");
		JLabel right = new JLabel("right");
		pane.setComponent(left, 0);
		pane.setComponent(right, 1);

		assertEquals(2, pane.getAmountOfComponents());
		JSplitPane split = (JSplitPane) pane.getComponent(0);
		assertSame(left, split.getLeftComponent());
		assertSame(right, split.getRightComponent());
		assertEquals(split.getDividerLocation(), pane.getDividerPosition());

		JLabel replaced = new JLabel("replaced");
		pane.setComponent(replaced, 1);
		split = (JSplitPane) pane.getComponent(0);
		assertSame(left, split.getLeftComponent());
		assertSame(replaced, split.getRightComponent());

		int location = pane.getDividerPosition();
		pane.removeComponent(0);
		assertEquals(1, pane.getAmountOfComponents());
		assertFalse(pane.getComponent(0) instanceof JSplitPane);
		assertTrue(labels(pane).contains("replaced"));
		assertEquals(location, pane.getLastDividerPosition());

		pane.removeComponent(0);
		assertEquals(1, pane.getAmountOfComponents());
		pane.removeComponent(1);
		assertEquals(0, pane.getAmountOfComponents());
		assertTrue(pane.getComponent(0) instanceof JLabel);
	}

	@Test
	void aBadIndexIsRejectedAndAThirdSlotCannotBeShown() throws Exception {
		// backwards compatibility
		JOptionalSplitPane pane = new JOptionalSplitPane();
		IllegalArgumentException low = assertThrows(IllegalArgumentException.class,
				() -> pane.setComponent(new JLabel("no"), -1));
		assertEquals("Wrong index: -1", low.getMessage());
		IllegalArgumentException high = assertThrows(IllegalArgumentException.class,
				() -> pane.removeComponent(2));
		assertEquals("Wrong index: 2", high.getMessage());

		Field field = JOptionalSplitPane.class.getDeclaredField("mComponentHash");
		field.setAccessible(true);
		Object map = field.get(pane);
		Method put = HashMap.class.getMethod("put", Object.class, Object.class);
		put.invoke(map, Integer.valueOf(0), new JLabel("0"));
		put.invoke(map, Integer.valueOf(1), new JLabel("1"));
		put.invoke(map, Integer.valueOf(2), new JLabel("2"));
		IllegalArgumentException tooMany = assertThrows(IllegalArgumentException.class,
				() -> pane.setComponent(new JLabel("again"), 0));
		assertTrue(tooMany.getMessage().contains("Too many indices"));
		IllegalArgumentException wrong = assertThrows(IllegalArgumentException.class, () -> pane.removeComponent(0));
		assertTrue(wrong.getMessage().contains("Wrong indices"));
	}

	@Test
	void theDemoActionsAddAndRemoveBothSides() throws Exception {
		// backwards compatibility
		JOptionalSplitPane pane = new JOptionalSplitPane();
		Action addLeft = action(pane, 1);
		addLeft.actionPerformed(event(pane));
		assertTrue(labels(pane).contains("links 0"));
		addLeft.actionPerformed(event(pane));
		assertTrue(labels(pane).contains("links 1"));
		assertFalse(labels(pane).contains("links 0"));
		assertEquals(1, pane.getAmountOfComponents());

		Action addRight = action(pane, 2);
		addRight.actionPerformed(event(pane));
		assertEquals(2, pane.getAmountOfComponents());
		assertTrue(labels(pane).contains("rechts 0"));
		addRight.actionPerformed(event(pane));
		assertTrue(labels(pane).contains("rechts 1"));

		action(pane, 3).actionPerformed(event(pane));
		assertEquals(1, pane.getAmountOfComponents());
		assertFalse(labels(pane).contains("links 1"));
		assertTrue(labels(pane).contains("rechts 1"));
		action(pane, 4).actionPerformed(event(pane));
		assertEquals(0, pane.getAmountOfComponents());
	}

	@Test
	void mainThrowsHeadlessExceptionAndAddsNoWindow() {
		// backwards compatibility
		assertTrue(GraphicsEnvironment.isHeadless());
		Window[] before = Window.getWindows();
		assertThrows(HeadlessException.class, () -> JOptionalSplitPane.main(new String[0]));
		assertArrayEquals(before, Window.getWindows());
	}

	private static Action action(JOptionalSplitPane pane, int index) throws Exception {
		Class<?> type = Class.forName("freemind.common.JOptionalSplitPane$" + index);
		Constructor<?> ctor = type.getDeclaredConstructors()[0];
		ctor.setAccessible(true);
		Object[] args = new Object[ctor.getParameterCount()];
		for (int i = 0; i < args.length; i++) {
			Class<?> param = ctor.getParameterTypes()[i];
			if (param == String.class) {
				args[i] = "demo";
			} else if (param == JOptionalSplitPane.class) {
				args[i] = pane;
			} else {
				throw new AssertionError(param.getName());
			}
		}
		return (Action) ctor.newInstance(args);
	}

	private static ActionEvent event(JOptionalSplitPane pane) {
		return new ActionEvent(pane, ActionEvent.ACTION_PERFORMED, "demo");
	}

	private static int count(List<String> texts, String text) {
		int found = 0;
		for (String item : texts) {
			if (text.equals(item)) {
				found++;
			}
		}
		return found;
	}

	private static List<String> labels(Component root) {
		List<String> texts = new ArrayList<>();
		collect(root, texts);
		return texts;
	}

	private static void collect(Component root, List<String> texts) {
		if (root instanceof JLabel) {
			String text = ((JLabel) root).getText();
			if (text != null) {
				texts.add(text);
			}
		}
		if (root instanceof Container) {
			for (Component child : ((Container) root).getComponents()) {
				collect(child, texts);
			}
		}
	}
}
