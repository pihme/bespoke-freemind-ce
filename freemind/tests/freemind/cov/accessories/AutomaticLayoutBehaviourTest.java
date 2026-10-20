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

package tests.freemind.cov.accessories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;

import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;

import org.junit.jupiter.api.Test;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import accessories.plugins.AutomaticLayout;
import accessories.plugins.AutomaticLayout.StylePatternListProperty;
import accessories.plugins.AutomaticLayout.StylePatternProperty;
import freemind.common.PropertyControl;
import freemind.common.TextTranslator;
import freemind.main.Tools;
import freemind.modes.MindMapNode;
import freemind.preferences.layout.OptionPanel;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

class AutomaticLayoutBehaviourTest {

	private static final String PATTERNS = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><patterns>"
			+ "<pattern name=\"Level1\"><pattern_node_color value=\"#000000\"/></pattern>"
			+ "<pattern name=\"Level2\"><pattern_node_color value=\"#0033ff\"/></pattern>"
			+ "</patterns>";

	private static final String ONE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<pattern name=\"Level1\"><pattern_node_color value=\"#0033ff\"/></pattern>";

	// backwards compatibility
	@Test
	void addingAChildAppliesTheDepthColor() throws Exception {
		clearPatterns();
		Opened opened = MapFixture.open("Child");
		opened.frame.setProperty("automaticFormat_level", PATTERNS);
		AutomaticLayout hook = new AutomaticLayout();
		hook.setController(opened.mode);
		hook.setMap(opened.mode.getMap());
		hook.setProperties(new Properties());
		hook.setNode(opened.root);
		try {
			hook.invoke(opened.root);
			MindMapNode extra = opened.mode.addNewNode(opened.child("Child"), 0, false);
			opened.mode.setNodeText(extra, "Extra");
			hook.onAddChildren(extra);
			hook.onUpdateChildrenHook(opened.child("Child"));
			hook.onUpdateNodeHook();

			assertEquals(Tools.xmlToColor("#000000"), opened.root.getColor());
			assertEquals(Tools.xmlToColor("#0033ff"), opened.child("Child").getColor());
			assertEquals(Tools.xmlToColor("#0033ff"), extra.getColor());
		} finally {
			clearPatterns();
		}
	}

	// backwards compatibility
	@Test
	void styleControlsExposeAndStoreTheirPatternText() throws Exception {
		Opened opened = MapFixture.open("Child");
		TextTranslator translator = key -> "label-" + key;
		StylePatternProperty single = new StylePatternProperty("desc", "label", translator, opened.mode);
		single.setValue(ONE);
		assertEquals("desc", single.getDescription());
		assertEquals("label", single.getLabel());
		assertEquals(ONE, single.getValue());
		assertTrue(single.getClass().getName().contains("StylePattern"));
		single.setEnabled(false);
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 3dlu, pref:grow", ""));
		single.layout(builder, translator);
		assertTrue(builder.getPanel().getComponentCount() > 0);
		assertThrows(HeadlessException.class, () -> single.actionPerformed(new ActionEvent(single, 0, "edit")));
		assertEquals(ONE, single.getValue());

		StylePatternListProperty list = new StylePatternListProperty("levels", "automaticFormat_level", translator,
				opened.mode);
		list.setValue(PATTERNS);
		assertEquals(PATTERNS, list.getValue());
		assertEquals("levels", list.getDescription());
		assertEquals("automaticFormat_level", list.getLabel());
		list.setEnabled(true);
		DefaultFormBuilder listBuilder = new DefaultFormBuilder(new FormLayout("pref, 3dlu, pref:grow", ""));
		list.layout(listBuilder, translator);
		assertTrue(listBuilder.getPanel().getComponentCount() > 0);
		JList<?> shown = findList(list);
		shown.setSelectedIndex(-1);
		list.valueChanged(new ListSelectionEvent(shown, 0, 0, false));
		assertEquals(PATTERNS, list.getValue());
		assertThrows(HeadlessException.class, () -> shown.setSelectedIndex(0));
		assertEquals(PATTERNS, list.getValue());
	}

	// backwards compatibility
	@Test
	void registrationPublishesThePatternControls() throws Exception {
		clearPatterns();
		Opened opened = MapFixture.open("Child");
		AutomaticLayout.Registration registration = new AutomaticLayout.Registration(opened.mode, opened.mode.getMap());
		registration.register();
		try {
			Field contributor = AutomaticLayout.Registration.class
					.getDeclaredField("mAutomaticLayoutPropertyContributor");
			contributor.setAccessible(true);
			Object controlsOwner = contributor.get(registration);
			java.lang.reflect.Method getControls = controlsOwner.getClass().getDeclaredMethod("getControls",
					TextTranslator.class);
			getControls.setAccessible(true);
			@SuppressWarnings("unchecked")
			List<PropertyControl> controls = (List<PropertyControl>) getControls.invoke(controlsOwner,
					(TextTranslator) key -> key);
			assertEquals(3, controls.size());
			assertTrue(controls.get(2) instanceof StylePatternListProperty);

			Field patterns = AutomaticLayout.class.getDeclaredField("patterns");
			patterns.setAccessible(true);
			patterns.set(null, new freemind.controller.actions.generated.instance.Patterns());
			Field listener = AutomaticLayout.Registration.class.getDeclaredField("listener");
			listener.setAccessible(true);
			freemind.preferences.FreemindPropertyListener propertyListener =
					(freemind.preferences.FreemindPropertyListener) listener.get(null);
			propertyListener.propertyChanged("automaticFormat_level", PATTERNS, "");
			assertEquals(null, patterns.get(null));
			propertyListener.propertyChanged("other", "a", "b");
			patterns.set(null, new freemind.controller.actions.generated.instance.Patterns());
			propertyListener.propertyChanged("automaticFormat_levelExtra", "a", "b");
			assertEquals(null, patterns.get(null));
		} finally {
			Field contributor = AutomaticLayout.Registration.class
					.getDeclaredField("mAutomaticLayoutPropertyContributor");
			contributor.setAccessible(true);
			Object removed = contributor.get(registration);
			registration.deRegister();
			clearPatterns();
			Field contributors = OptionPanel.class.getDeclaredField("sContributors");
			contributors.setAccessible(true);
			assertFalse(((java.util.Set<?>) contributors.get(null)).contains(removed));
		}
	}

	private static JList<?> findList(StylePatternListProperty list) throws Exception {
		Field field = StylePatternListProperty.class.getDeclaredField("mList");
		field.setAccessible(true);
		return (JList<?>) field.get(list);
	}

	private static void clearPatterns() throws Exception {
		Field patterns = AutomaticLayout.class.getDeclaredField("patterns");
		patterns.setAccessible(true);
		patterns.set(null, null);
	}
}
