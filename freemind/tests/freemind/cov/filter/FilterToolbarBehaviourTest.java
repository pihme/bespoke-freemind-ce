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

package tests.freemind.cov.filter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Timer;

import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.junit.jupiter.api.Test;

import freemind.controller.Controller;
import freemind.controller.FreeMindToolBar;
import freemind.controller.filter.DefaultFilter;
import freemind.controller.filter.Filter;
import freemind.controller.filter.FilterController;
import freemind.controller.filter.condition.Condition;
import freemind.controller.filter.condition.NoFilteringCondition;
import freemind.controller.filter.condition.SelectedViewCondition;
import freemind.main.Resources;
import freemind.main.XMLElement;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.MindMapNodeModel;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** The toolbar applies the selected condition and unfolds matching branches. */
class FilterToolbarBehaviourTest {

	@Test
	void theToolbarStartsHiddenWithTheTwoStandardConditions() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta");
		try {
			FilterController filters = install(opened);
			FreeMindToolBar bar = toolbar(filters);
			assertFalse(bar.isVisible());
			assertSame(bar, toolbar(filters));
			JComboBox<?> combo = combo(bar);
			DefaultComboBoxModel<?> model = (DefaultComboBoxModel<?>) combo.getModel();
			assertSame(model, filters.getFilterConditionModel());
			assertSame(NoFilteringCondition.createCondition(), model.getElementAt(0));
			assertSame(SelectedViewCondition.CreateCondition(), model.getElementAt(1));
			assertEquals(0, combo.getSelectedIndex());
			assertNotNull(combo.getRenderer());
			assertTrue(ancestors(bar).isSelected());
			assertFalse(descendants(bar).isSelected());
		} finally {
			close(opened);
		}
	}

	@Test
	void aHiddenToolbarIgnoresANewMapFilter() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta");
		try {
			FilterController filters = install(opened);
			FreeMindToolBar bar = toolbar(filters);
			DefaultFilter filter = new DefaultFilter(new TextContains("Alpha"), true, true);
			opened.root.getMap().setFilter(filter);
			filters.afterMapModuleChange(null, null, opened.app.getMapModule(), opened.mode.getMode());
			assertFalse(bar.isVisible());
			assertFalse(descendants(bar).isSelected());
			assertSame(filter, opened.root.getMap().getFilter());
		} finally {
			close(opened);
		}
	}

	@Test
	void aVisibleToolbarSelectsTheMapConditionBeforeTheOptionBoxesApply() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta");
		try {
			FilterController filters = install(opened);
			FreeMindToolBar bar = toolbar(filters);
			MindMapNode plain = new MindMapNodeModel("plain", opened.root.getMap());
			opened.child("Alpha").insert(plain, 0);
			Condition contains = new TextContains("Alpha");
			addCondition(combo(bar), contains);
			DefaultFilter filter = new DefaultFilter(contains, true, true);
			opened.root.getMap().setFilter(filter);
			bar.setVisible(true);
			filters.afterMapModuleChange(null, null, opened.app.getMapModule(), opened.mode.getMode());

			assertTrue(ancestors(bar).isSelected());
			assertTrue(descendants(bar).isSelected());
			assertSame(contains, combo(bar).getSelectedItem());
			assertTrue(opened.child("Alpha").isVisible());
			assertFalse(plain.isVisible());
			assertFalse(opened.child("Beta").isVisible());
			Filter applied = opened.root.getMap().getFilter();
			assertNotSame(applied, filter);
			assertTrue(applied.areAncestorsShown());
			assertFalse(applied.areDescendantsShown());
		} finally {
			close(opened);
		}
	}

	@Test
	void theSameFilterASecondTimeDoesNotPushItsFlagsBackOntoTheCheckboxes() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta");
		try {
			FilterController filters = install(opened);
			FreeMindToolBar bar = toolbar(filters);
			Condition contains = new TextContains("Alpha");
			addCondition(combo(bar), contains);
			bar.setVisible(true);
			combo(bar).setSelectedItem(contains);
			Filter adopted = opened.root.getMap().getFilter();
			assertFalse(adopted.areDescendantsShown());
			assertFalse(descendants(bar).isSelected());
			assertFalse(opened.child("Beta").isVisible());

			descendants(bar).setSelected(true);
			filters.afterMapModuleChange(null, null, opened.app.getMapModule(), opened.mode.getMode());
			assertTrue(descendants(bar).isSelected());
			assertSame(adopted, opened.root.getMap().getFilter());
			assertFalse(adopted.areDescendantsShown());
			assertFalse(opened.child("Beta").isVisible());
		} finally {
			close(opened);
		}
	}

	@Test
	void aNullMapSelectsTheFirstCondition() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta");
		try {
			FilterController filters = install(opened);
			FreeMindToolBar bar = toolbar(filters);
			DefaultFilter filter = new DefaultFilter(new TextContains("Alpha"), false, false);
			opened.root.getMap().setFilter(filter);
			filter.applyFilter(opened.app);
			assertFalse(opened.child("Beta").isVisible());
			bar.setVisible(true);
			combo(bar).setSelectedIndex(1);
			filters.afterMapModuleChange(null, null, null, null);
			assertNull(filters.getMap());
			assertEquals(0, combo(bar).getSelectedIndex());
			assertSame(NoFilteringCondition.createCondition(), combo(bar).getSelectedItem());
			assertFalse(opened.child("Beta").isVisible());
		} finally {
			close(opened);
		}
	}

	@Test
	void choosingAConditionHidesNodesThatDoNotMatch() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta");
		try {
			FilterController filters = install(opened);
			FreeMindToolBar bar = toolbar(filters);
			Condition contains = new TextContains("Alpha");
			addCondition(combo(bar), contains);
			combo(bar).setSelectedItem(contains);
			assertSame(contains, opened.root.getMap().getFilter().getCondition());
			assertTrue(opened.child("Alpha").isVisible());
			assertFalse(opened.child("Beta").isVisible());
			assertTrue(opened.root.getMap().getFilter().areAncestorsShown());
			assertFalse(opened.root.getMap().getFilter().areDescendantsShown());
		} finally {
			close(opened);
		}
	}

	@Test
	void clearingAncestorsHidesAnAncestorThatIsNotItselfAMatch() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta");
		try {
			FilterController filters = install(opened);
			FreeMindToolBar bar = toolbar(filters);
			MindMapNode nested = new MindMapNodeModel("Alpha under", opened.root.getMap());
			opened.child("Beta").insert(nested, 0);
			Condition contains = new TextContains("Alpha");
			addCondition(combo(bar), contains);
			combo(bar).setSelectedItem(contains);
			assertTrue(opened.child("Beta").isVisible());
			assertTrue(opened.child("Beta").getFilterInfo().isAncestor());

			click(ancestors(bar));
			assertFalse(ancestors(bar).isSelected());
			assertFalse(opened.child("Beta").isVisible());
			assertTrue(nested.isVisible());
			assertFalse(opened.root.getMap().getFilter().areAncestorsShown());
		} finally {
			close(opened);
		}
	}

	@Test
	void showingDescendantsRevealsAChildOfAMatch() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta");
		try {
			FilterController filters = install(opened);
			FreeMindToolBar bar = toolbar(filters);
			MindMapNode plain = new MindMapNodeModel("plain", opened.root.getMap());
			opened.child("Alpha").insert(plain, 0);
			Condition contains = new TextContains("Alpha");
			addCondition(combo(bar), contains);
			combo(bar).setSelectedItem(contains);
			assertFalse(plain.isVisible());

			click(descendants(bar));
			assertTrue(descendants(bar).isSelected());
			assertTrue(plain.isVisible());
			assertFalse(opened.child("Beta").isVisible());
			assertTrue(opened.root.getMap().getFilter().areDescendantsShown());
		} finally {
			close(opened);
		}
	}

	@Test
	void resettingTheFilterLetsTheNextCheckboxChangeBuildANewOne() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta");
		try {
			FilterController filters = install(opened);
			FreeMindToolBar bar = toolbar(filters);
			Condition contains = new TextContains("Alpha");
			addCondition(combo(bar), contains);
			combo(bar).setSelectedItem(contains);
			Filter first = opened.root.getMap().getFilter();
			assertFalse(first.areDescendantsShown());

			Method reset = bar.getClass().getDeclaredMethod("resetFilter");
			reset.setAccessible(true);
			reset.invoke(bar);
			assertSame(first, opened.root.getMap().getFilter());

			click(descendants(bar));
			Filter second = opened.root.getMap().getFilter();
			assertNotSame(second, first);
			assertTrue(second.areDescendantsShown());
			assertSame(contains, second.getCondition());
			assertTrue(opened.child("Alpha").isVisible());
		} finally {
			close(opened);
		}
	}

	@Test
	void unfoldOpensAnAncestorAndLeavesAnUnrelatedFoldedNodeFolded() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta");
		try {
			FilterController filters = install(opened);
			FreeMindToolBar bar = toolbar(filters);
			MindMapNode alphaKid = opened.mode.addNewNode(opened.child("Alpha"), 0, false);
			MindMapNode betaKid = opened.mode.addNewNode(opened.child("Beta"), 0, false);
			opened.mode.setNodeText(alphaKid, "Alpha kid");
			opened.mode.setNodeText(betaKid, "Beta kid");
			opened.mode.setFolded(opened.child("Alpha"), true);
			opened.mode.setFolded(opened.child("Beta"), true);
			opened.child("Alpha").getFilterInfo().setAncestor();
			assertTrue(opened.child("Alpha").isFolded());
			assertTrue(opened.child("Beta").isFolded());

			fire(unfold(bar));
			assertFalse(opened.child("Alpha").isFolded());
			assertTrue(opened.child("Beta").isFolded());
		} finally {
			close(opened);
		}
	}

	@Test
	void unfoldWithDescendantsCheckedOpensEveryFoldedBranch() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta");
		try {
			FilterController filters = install(opened);
			FreeMindToolBar bar = toolbar(filters);
			opened.mode.addNewNode(opened.child("Alpha"), 0, false);
			opened.mode.addNewNode(opened.child("Beta"), 0, false);
			opened.mode.setFolded(opened.child("Alpha"), true);
			opened.mode.setFolded(opened.child("Beta"), true);
			descendants(bar).setSelected(true);

			fire(unfold(bar));
			assertFalse(opened.child("Alpha").isFolded());
			assertFalse(opened.child("Beta").isFolded());
		} finally {
			close(opened);
		}
	}

	@Test
	void unfoldDoesNothingWhenNoConditionIsSelected() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha");
		try {
			FilterController filters = install(opened);
			FreeMindToolBar bar = toolbar(filters);
			opened.mode.addNewNode(opened.child("Alpha"), 0, false);
			opened.mode.setFolded(opened.child("Alpha"), true);
			combo(bar).setSelectedIndex(-1);
			assertNull(combo(bar).getSelectedItem());

			fire(unfold(bar));
			assertTrue(opened.child("Alpha").isFolded());
		} finally {
			close(opened);
		}
	}

	@Test
	void editingTheFilterThrowsHeadlessExceptionAndAddsNoWindow() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha");
		try {
			FilterController filters = install(opened);
			FreeMindToolBar bar = toolbar(filters);
			assertTrue(java.awt.GraphicsEnvironment.isHeadless());
			Window[] before = Window.getWindows();
			JButton edit = edit(bar);
			assertThrows(HeadlessException.class, () -> fire(edit));
			assertArrayEquals(before, Window.getWindows());
			Method dialog = bar.getClass().getDeclaredMethod("getFilterDialog");
			dialog.setAccessible(true);
			assertNull(dialog.invoke(bar));
		} finally {
			close(opened);
		}
	}

	@Test
	void reloadingASavedFilePutsTheConditionAfterTheTwoStandardOnes() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta");
		Path dir = Files.createTempDirectory("filter-toolbar");
		try {
			FilterController filters = install(opened);
			FreeMindToolBar bar = toolbar(filters);
			Path source = dir.resolve("alpha.mmfilter");
			Files.writeString(source,
					"<filter_conditions><node_contains_condition value=\"Alpha\"/></filter_conditions>\n");
			setPath(bar, source.toString());
			init(bar);
			DefaultComboBoxModel<?> model = (DefaultComboBoxModel<?>) combo(bar).getModel();
			assertEquals(3, model.getSize());
			assertSame(NoFilteringCondition.createCondition(), model.getElementAt(0));
			assertSame(SelectedViewCondition.CreateCondition(), model.getElementAt(1));
			Condition loaded = (Condition) model.getElementAt(2);
			assertTrue(loaded.checkNode(opened.app, opened.child("Alpha")));
			assertFalse(loaded.checkNode(opened.app, opened.child("Beta")));
			assertEquals(0, combo(bar).getSelectedIndex());

			Path saved = dir.resolve("saved.mmfilter");
			setPath(bar, saved.toString());
			model.setSelectedItem(loaded);
			filters.saveConditions();
			DefaultComboBoxModel<Condition> again = new DefaultComboBoxModel<>();
			load(filters, again, saved.toString());
			assertEquals(1, again.getSize());
			assertTrue(again.getElementAt(0).checkNode(opened.app, opened.child("Alpha")));
		} finally {
			close(opened);
			delete(dir);
		}
	}

	@Test
	void aBrokenFilterFileFallsBackToTheTwoStandardConditions() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta");
		Path dir = Files.createTempDirectory("filter-toolbar-bad");
		try {
			FilterController filters = install(opened);
			FreeMindToolBar bar = toolbar(filters);
			Path broken = dir.resolve("bad.mmfilter");
			Files.writeString(broken, "<<<");
			setPath(bar, broken.toString());
			init(bar);
			DefaultComboBoxModel<?> model = (DefaultComboBoxModel<?>) combo(bar).getModel();
			assertEquals(2, model.getSize());
			assertSame(NoFilteringCondition.createCondition(), model.getSelectedItem());
			assertEquals(0, combo(bar).getSelectedIndex());
			assertTrue(((Condition) model.getElementAt(0)).checkNode(opened.app, opened.child("Beta")));

			setPath(bar, dir.toString());
			int size = model.getSize();
			filters.saveConditions();
			assertEquals(size, model.getSize());
			assertSame(NoFilteringCondition.createCondition(), model.getSelectedItem());
		} finally {
			close(opened);
			delete(dir);
		}
	}

	private static FilterController install(Opened opened) throws Exception {
		FilterController filters = new FilterController(opened.app);
		Field field = Controller.class.getDeclaredField("mFilterController");
		field.setAccessible(true);
		field.set(opened.app, filters);
		filters.afterMapModuleChange(null, null, opened.app.getMapModule(), opened.mode.getMode());
		return filters;
	}

	private static FreeMindToolBar toolbar(FilterController filters) throws Exception {
		Method method = FilterController.class.getMethod("getFilterToolbar");
		return (FreeMindToolBar) method.invoke(filters);
	}

	private static JComboBox<?> combo(FreeMindToolBar bar) {
		for (Component component : bar.getComponents()) {
			if (component instanceof JComboBox<?> box) {
				return box;
			}
		}
		throw new AssertionError("missing condition combo");
	}

	private static void addCondition(JComboBox<?> combo, Condition condition) {
		DefaultComboBoxModel model = (DefaultComboBoxModel) combo.getModel();
		model.addElement(condition);
	}

	private static JCheckBox ancestors(FreeMindToolBar bar) {
		return checkBox(bar, "filter_show_ancestors");
	}

	private static JCheckBox descendants(FreeMindToolBar bar) {
		return checkBox(bar, "filter_show_descendants");
	}

	private static JCheckBox checkBox(FreeMindToolBar bar, String resourceKey) {
		String text = Resources.getInstance().getResourceString(resourceKey);
		for (Component component : bar.getComponents()) {
			if (component instanceof JCheckBox box && text.equals(box.getText())) {
				return box;
			}
		}
		throw new AssertionError(resourceKey);
	}

	private static JButton unfold(FreeMindToolBar bar) {
		String tip = Resources.getInstance().getResourceString("filter_unfold_ancestors");
		for (Component component : bar.getComponents()) {
			if (component instanceof JButton button && tip.equals(button.getToolTipText())) {
				return button;
			}
		}
		throw new AssertionError("unfold");
	}

	private static JButton edit(FreeMindToolBar bar) {
		String unfoldTip = Resources.getInstance().getResourceString("filter_unfold_ancestors");
		for (Component component : bar.getComponents()) {
			if (component instanceof JButton button && button.getAction() != null
					&& !unfoldTip.equals(button.getToolTipText())) {
				return button;
			}
		}
		throw new AssertionError("edit");
	}

	private static void fire(AbstractButton button) {
		button.getAction().actionPerformed(new ActionEvent(button, ActionEvent.ACTION_PERFORMED, ""));
	}

	/** A checkbox click toggles the model and then notifies its action listeners. */
	private static void click(JCheckBox box) {
		box.getModel().setArmed(true);
		box.getModel().setPressed(true);
		box.getModel().setPressed(false);
	}

	private static void setPath(FreeMindToolBar bar, String path) throws Exception {
		Field field = bar.getClass().getDeclaredField("pathToFilterFile");
		field.setAccessible(true);
		field.set(bar, path);
	}

	private static void init(FreeMindToolBar bar) throws Exception {
		Method method = bar.getClass().getDeclaredMethod("initConditions");
		method.setAccessible(true);
		method.invoke(bar);
	}

	private static void load(FilterController filters, DefaultComboBoxModel<Condition> model, String path)
			throws Exception {
		Method method = FilterController.class.getDeclaredMethod("loadConditions", DefaultComboBoxModel.class,
				String.class);
		method.setAccessible(true);
		try {
			method.invoke(filters, model, path);
		} catch (InvocationTargetException ex) {
			if (ex.getCause() instanceof Exception cause) {
				throw cause;
			}
			throw ex;
		}
	}

	private static void close(Opened opened) throws Exception {
		Field viewTimer = opened.view.getClass().getDeclaredField("mCenterNodeTimer");
		viewTimer.setAccessible(true);
		((Timer) viewTimer.get(opened.view)).cancel();
		Field mapTimer = opened.root.getMap().getClass().getSuperclass()
				.getDeclaredField("mTimerForFileChangeObservation");
		mapTimer.setAccessible(true);
		((Timer) mapTimer.get(opened.root.getMap())).cancel();
	}

	private static void delete(Path dir) throws IOException {
		try (var walk = Files.walk(dir)) {
			walk.sorted(Comparator.reverseOrder()).forEach(path -> {
				try {
					Files.deleteIfExists(path);
				} catch (IOException ex) {
					throw new IllegalStateException(ex);
				}
			});
		}
	}

	/** Node text contains a fragment. */
	private static final class TextContains implements Condition {
		private final String text;

		private TextContains(String text) {
			this.text = text;
		}

		public boolean checkNode(Controller controller, MindMapNode node) {
			return node.getText().indexOf(text) > -1;
		}

		public JComponent getListCellRendererComponent() {
			return new JLabel(text);
		}

		public void save(XMLElement element) {
		}
	}
}
