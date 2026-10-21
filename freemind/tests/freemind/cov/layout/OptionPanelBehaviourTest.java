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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import freemind.common.PropertyBean;
import freemind.common.PropertyControl;
import freemind.common.SeparatorProperty;
import freemind.common.StringProperty;
import freemind.common.TextTranslator;
import freemind.common.XmlBindingTools;
import freemind.controller.actions.generated.instance.OptionPanelWindowConfigurationStorage;
import freemind.main.FreeMind;
import freemind.main.FreeMindCommon;
import freemind.main.Resources;
import freemind.preferences.FreemindPropertyContributor;
import freemind.preferences.layout.GrabKeyDialog;
import freemind.preferences.layout.OptionPanel;
import freemind.preferences.layout.VariableSizeCardLayout;
import tests.freemind.cov.MapFixture;

/** Preference values move between the controls and the stored properties. */
class OptionPanelBehaviourTest {

	@AfterEach
	void clearStatics() throws Exception {
		field(OptionPanel.class, "fmMain").set(null, null);
		if (Resources.getInstance() != null && Resources.getInstance().getProperties() != null) {
			Resources.getInstance().getProperties().setProperty("place_dialogs_on_first_screen", "true");
		}
	}

	@Test
	void aNewTabKeepsItsLabelAndDoesNotTouchTheForm() {
		// backwards compatibility
		OptionPanel.NewTabProperty tab = new OptionPanel.NewTabProperty("Environment");
		assertEquals("Environment", tab.getLabel());
		assertEquals("right:max(40dlu;p), 4dlu, 120dlu, 7dlu", tab.getDescription());
		OptionPanel.NewTabProperty custom = new OptionPanel.NewTabProperty("HTML", "pref");
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref", ""));
		custom.layout(builder, key -> key);
		custom.setEnabled(false);
		assertEquals("HTML", custom.getLabel());
		assertEquals("pref", custom.getDescription());
		assertEquals(0, builder.getPanel().getComponentCount());
	}

	@Test
	void contributorsJoinAndLeaveTheStaticSet() throws Exception {
		// backwards compatibility
		Field contributors = field(OptionPanel.class, "sContributors");
		Set<?> set = (Set<?>) contributors.get(null);
		int size = set.size();
		FreemindPropertyContributor contributor = translator -> List.of(new SeparatorProperty("extra"));
		try {
			OptionPanel.addContributor(contributor);
			assertTrue(set.contains(contributor));
			assertEquals(size + 1, set.size());
			OptionPanel.addContributor(contributor);
			assertEquals(size + 1, set.size());
		} finally {
			OptionPanel.removeContributor(contributor);
		}
		assertFalse(set.contains(contributor));
		assertEquals(size, set.size());
	}

	@Test
	void constructionWithoutSavedGeometryThrowsHeadlessAndAddsNoWindow() throws Exception {
		// backwards compatibility
		assertTrue(GraphicsEnvironment.isHeadless());
		Ready ready = frame();
		ready.opened.frame.getProperties().remove("OptionPanel_Window_Properties");
		Window[] before = Window.getWindows();
		HeadlessException thrown = assertThrows(HeadlessException.class,
				() -> new OptionPanel(ready.fm, ready.dialog, props -> {
				}));
		assertEquals(HeadlessException.class, thrown.getClass());
		assertArrayEquals(before, Window.getWindows());

		OptionPanel probe = (OptionPanel) allocate(OptionPanel.class);
		assertNull(probe.getText(null));
		assertEquals(ready.fm.getResourceString("OptionPanel.Environment"), probe.getText("Environment"));
		field(OptionPanel.class, "fmMain").set(null, null);
		IllegalArgumentException missing = assertThrows(IllegalArgumentException.class,
				() -> probe.getText("Environment"));
		assertEquals("FreeMindMain not set yet.", missing.getMessage());
		assertNull(probe.getText(null));
	}

	@Test
	void savedGeometrySelectsThePanelWithoutAddingAWindow() throws Exception {
		// backwards compatibility
		Ready ready = open("Defaults");
		Window[] before = Window.getWindows();
		OptionPanel panel = new OptionPanel(ready.fm, ready.dialog, props -> {
		});
		assertArrayEquals(before, Window.getWindows());
		assertEquals(320, ready.dialog.getWidth());
		assertEquals(240, ready.dialog.getHeight());
		assertEquals(15, ready.dialog.getX());
		assertEquals(25, ready.dialog.getY());
		assertEquals("Defaults", field(OptionPanel.class, "selectedPanel").get(panel));
		assertFalse(ready.dialog.isVisible());
	}

	@Test
	void propertiesRoundTripAndANullValueIsLeftOut() throws Exception {
		// backwards compatibility
		Ready ready = constructed("Defaults");
		ready.opened.frame.setProperty("title", "Ada");
		ready.opened.frame.setProperty("keystroke_copy", "control C");
		ready.opened.frame.setProperty("keystroke_open", "");
		StringProperty title = new StringProperty("help", "title");
		Object copy = key(ready.panel, "keystroke_copy");
		Object open = key(ready.panel, "keystroke_open");
		Vector<PropertyControl> controls = new Vector<>();
		controls.add(new SeparatorProperty("gap"));
		controls.add(title);
		controls.add(new NullValue());
		controls.add((PropertyControl) copy);
		controls.add((PropertyControl) open);
		field(OptionPanel.class, "controls").set(ready.panel, controls);

		ready.panel.setProperties();
		JButton copyButton = (JButton) field(copy.getClass(), "mButton").get(copy);
		assertEquals("control C", copyButton.getText());
		assertEquals("control C", copyButton.getToolTipText());
		assertEquals("Ada", title.getValue());
		assertEquals("", ((JButton) field(open.getClass(), "mButton").get(open)).getText());
		GrabKeyDialog.KeyBinding copyBinding = (GrabKeyDialog.KeyBinding) field(copy.getClass(), "kb").get(copy);
		GrabKeyDialog.KeyBinding openBinding = (GrabKeyDialog.KeyBinding) field(open.getClass(), "kb").get(open);
		assertEquals("control C", copyBinding.shortcut);
		assertTrue(copyBinding.isAssigned());
		assertNull(openBinding.shortcut);
		assertFalse(openBinding.isAssigned());

		Properties stored = (Properties) call(ready.panel, "getOptionProperties");
		assertEquals("Ada", stored.getProperty("title"));
		assertEquals("control C", stored.getProperty("keystroke_copy"));
		assertEquals("", stored.getProperty("keystroke_open"));
		assertNull(stored.getProperty("empty"));
		assertNull(stored.getProperty("gap"));

		Method find = OptionPanel.class.getDeclaredMethod("findControlByKB", GrabKeyDialog.KeyBinding.class);
		find.setAccessible(true);
		assertSame(copy, find.invoke(ready.panel, copyBinding));
		assertNull(find.invoke(ready.panel, new GrabKeyDialog.KeyBinding("keystroke_copy", "keystroke_copy",
				"control C", false)));

		call(copy, "setEnabled", new Class<?>[] { boolean.class }, Boolean.FALSE);
		assertFalse(copyButton.isEnabled());
		call(copy, "setEnabled", new Class<?>[] { boolean.class }, Boolean.TRUE);
		assertTrue(copyButton.isEnabled());
		call(copy, "setValue", new Class<?>[] { String.class }, "alt X");
		assertEquals("alt X", call(copy, "getValue"));
		assertEquals("alt X", copyButton.getToolTipText());
	}

	@Test
	void theKeyButtonRefusesToOpenTheGrabDialog() throws Exception {
		// backwards compatibility
		Ready ready = constructed("Defaults");
		Object copy = key(ready.panel, "keystroke_copy");
		call(copy, "disableModifiers");
		assertEquals(KeyEvent.ALT_MASK | KeyEvent.CTRL_MASK, field(copy.getClass(), "modifierMask").getInt(copy));
		JButton button = (JButton) field(copy.getClass(), "mButton").get(copy);
		button.setText("control C");
		Window[] before = Window.getWindows();
		assertThrows(HeadlessException.class, button::doClick);
		assertArrayEquals(before, Window.getWindows());
		assertEquals("control C", button.getText());
	}

	@Test
	void changingTabMarksThatButtonAndShowsItsCard() throws Exception {
		// backwards compatibility
		Ready ready = constructed("Defaults");
		field(OptionPanel.class, "selectedPanel").set(ready.panel, null);
		CardLayout cards = new VariableSizeCardLayout();
		JPanel stack = new JPanel(cards);
		JPanel environment = new JPanel();
		JPanel html = new JPanel();
		stack.add(environment, "Environment");
		stack.add(html, "HTML");
		JButton environmentButton = new JButton("Environment");
		JButton htmlButton = new JButton("HTML");
		Object environmentAction = tabAction(ready.panel, cards, stack, "Environment");
		Object htmlAction = tabAction(ready.panel, cards, stack, "HTML");
		register(ready.panel, environmentButton, "Environment", environmentAction);
		assertEquals("Environment", field(OptionPanel.class, "selectedPanel").get(ready.panel));
		register(ready.panel, htmlButton, "HTML", htmlAction);
		assertEquals("Environment", field(OptionPanel.class, "selectedPanel").get(ready.panel));

		call(htmlAction, "actionPerformed", new Class<?>[] { ActionEvent.class }, new Object[] { null });
		assertEquals("HTML", field(OptionPanel.class, "selectedPanel").get(ready.panel));
		assertEquals(Color.BLUE, htmlButton.getForeground());
		assertNull(environmentButton.getForeground());
		assertTrue(html.isVisible());
		assertFalse(environment.isVisible());
	}

	@Test
	void cancelStoresTheSelectedTabAndOkWritesThePropertiesFirst() throws Exception {
		// backwards compatibility
		Ready cancelled = constructed("Defaults", props -> {
			throw new AssertionError("cancel wrote properties");
		});
		field(OptionPanel.class, "selectedPanel").set(cancelled.panel, "HTML");
		listener(cancelled.panel, "freemind.preferences.layout.OptionPanel$1").actionPerformed(null);
		assertFalse(cancelled.dialog.isVisible());
		String saved = cancelled.opened.frame.getProperty("OptionPanel_Window_Properties");
		assertNotNull(saved);
		assertTrue(saved.contains("HTML"), saved);

		Properties[] seen = new Properties[1];
		Ready accepted = constructed("Defaults", props -> seen[0] = props);
		StringProperty title = new StringProperty("help", "title");
		title.setValue("Ada");
		Vector<PropertyControl> controls = new Vector<>();
		controls.add(title);
		controls.add(new NullValue());
		field(OptionPanel.class, "controls").set(accepted.panel, controls);
		listener(accepted.panel, "freemind.preferences.layout.OptionPanel$2")
				.actionPerformed(new ActionEvent(accepted.panel, 0, "ok"));
		assertEquals("Ada", seen[0].getProperty("title"));
		assertNull(seen[0].getProperty("empty"));
		assertFalse(accepted.dialog.isVisible());
		assertTrue(accepted.opened.frame.getProperty("OptionPanel_Window_Properties").contains("Defaults"));
	}

	private static Ready frame() throws Exception {
		Ready ready = new Ready();
		ready.opened = MapFixture.open("Child");
		ready.fm = (FreeMind) allocate(FreeMind.class);
		field(FreeMind.class, "controller").set(ready.fm, ready.opened.app);
		field(FreeMind.class, "mFreeMindCommon").set(ready.fm, new FreeMindCommon(ready.opened.frame));
		ready.dialog = (JDialog) allocate(JDialog.class);
		put(Window.class, ready.dialog, "ownedWindowList", new Vector<>());
		put(Container.class, ready.dialog, "component", new ArrayList<Component>());
		put(Component.class, ready.dialog, "acc", AccessController.getContext());
		return ready;
	}

	private static Ready open(String panelName) throws Exception {
		Ready ready = frame();
		Resources.getInstance().getProperties().setProperty("place_dialogs_on_first_screen", "false");
		OptionPanelWindowConfigurationStorage storage = new OptionPanelWindowConfigurationStorage();
		storage.setPanel(panelName);
		storage.setX(15);
		storage.setY(25);
		storage.setWidth(320);
		storage.setHeight(240);
		String xml = XmlBindingTools.getInstance().marshall(storage);
		assertNotNull(xml);
		ready.opened.frame.setProperty("OptionPanel_Window_Properties", xml);
		return ready;
	}

	private static Ready constructed(String panelName) throws Exception {
		return constructed(panelName, props -> {
		});
	}

	private static Ready constructed(String panelName, OptionPanel.OptionPanelFeedback feedback) throws Exception {
		Ready ready = open(panelName);
		field(OptionPanel.class, "fmMain").set(null, null);
		ready.panel = new OptionPanel(ready.fm, ready.dialog, feedback);
		return ready;
	}

	private static Object key(OptionPanel panel, String label) throws Exception {
		Class<?> type = Class.forName("freemind.preferences.layout.OptionPanel$KeyProperty");
		Constructor<?> constructor = type.getDeclaredConstructor(OptionPanel.class, JDialog.class, String.class,
				String.class);
		constructor.setAccessible(true);
		return constructor.newInstance(panel, null, "help", label);
	}

	private static Object tabAction(OptionPanel panel, CardLayout cards, JPanel stack, String name) throws Exception {
		Class<?> type = Class.forName("freemind.preferences.layout.OptionPanel$ChangeTabAction");
		Constructor<?> constructor = type.getDeclaredConstructor(OptionPanel.class, CardLayout.class, JPanel.class,
				String.class);
		constructor.setAccessible(true);
		return constructor.newInstance(panel, cards, stack, name);
	}

	private static void register(OptionPanel panel, JButton button, String name, Object action) throws Exception {
		Method method = OptionPanel.class.getDeclaredMethod("registerTabButton", JButton.class, String.class,
				action.getClass());
		method.setAccessible(true);
		method.invoke(panel, button, name, action);
	}

	private static java.awt.event.ActionListener listener(OptionPanel panel, String name) throws Exception {
		Class<?> type = Class.forName(name);
		Constructor<?> constructor = type.getDeclaredConstructor(OptionPanel.class);
		constructor.setAccessible(true);
		return (java.awt.event.ActionListener) constructor.newInstance(panel);
	}

	private static Object call(Object target, String name) throws Exception {
		return call(target, name, new Class<?>[] {}, new Object[] {});
	}

	private static Object call(Object target, String name, Class<?>[] types, Object... args) throws Exception {
		Method method = target.getClass().getDeclaredMethod(name, types);
		method.setAccessible(true);
		return method.invoke(target, args);
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

	private static Object allocate(Class<?> type) throws Exception {
		Class<?> unsafeType = Class.forName("sun.misc.Unsafe");
		Field theUnsafe = unsafeType.getDeclaredField("theUnsafe");
		theUnsafe.setAccessible(true);
		Object unsafe = theUnsafe.get(null);
		return unsafeType.getMethod("allocateInstance", Class.class).invoke(unsafe, type);
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
		throw new NoSuchFieldException(type.getName() + "." + name);
	}

	private static final class NullValue extends PropertyBean implements PropertyControl {
		public String getLabel() {
			return "empty";
		}

		public void setValue(String value) {
		}

		public String getValue() {
			return null;
		}

		public String getDescription() {
			return null;
		}

		public void layout(DefaultFormBuilder builder, TextTranslator translator) {
		}

		public void setEnabled(boolean enabled) {
		}
	}

	private static final class Ready {
		private MapFixture.Opened opened;
		private FreeMind fm;
		private JDialog dialog;
		private OptionPanel panel;
	}
}
