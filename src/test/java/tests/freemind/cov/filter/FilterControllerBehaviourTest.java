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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Timer;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.controller.Controller;
import freemind.controller.filter.DefaultFilter;
import freemind.controller.filter.Filter;
import freemind.controller.filter.FilterController;
import freemind.controller.filter.condition.Condition;
import freemind.controller.filter.condition.NoFilteringCondition;
import freemind.controller.filter.condition.SelectedViewCondition;
import freemind.main.XMLElement;
import freemind.main.XMLParseException;
import freemind.modes.MindMapNode;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** The filter controller shows, hides, and stores the toolbar's conditions. */
@DisplayName("FilterControllerBehaviourTest")
class FilterControllerBehaviourTest {

	@Test
	void showingTheToolbarAppliesTheMapFilterAndASecondShowKeepsIt() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta");
		try {
			FilterController filters = install(opened);
			DefaultFilter filter = new DefaultFilter(new TextContains("Alpha"), true, false);
			opened.root.getMap().setFilter(filter);
			assertFalse(filters.isVisible());

			filters.showFilterToolbar(true);
			assertTrue(filters.isVisible());
			assertTrue(opened.child("Alpha").isVisible());
			assertFalse(opened.child("Beta").isVisible());
			assertSame(filter, opened.root.getMap().getFilter());

			filters.showFilterToolbar(true);
			assertTrue(filters.isVisible());
			assertSame(filter, opened.root.getMap().getFilter());
			assertFalse(opened.child("Beta").isVisible());
		} finally {
			close(opened);
		}
	}

	@Test
	void hidingTheToolbarMatchesEveryNodeAndASecondHideLeavesThemVisible() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta");
		try {
			FilterController filters = install(opened);
			DefaultFilter filter = new DefaultFilter(new TextContains("Alpha"), false, false);
			opened.root.getMap().setFilter(filter);
			filters.showFilterToolbar(true);
			assertFalse(opened.child("Beta").isVisible());

			filters.showFilterToolbar(false);
			assertFalse(filters.isVisible());
			assertTrue(opened.child("Beta").isVisible());
			assertTrue(opened.child("Alpha").isVisible());
			Filter firstHidden = inactiveFilter();

			filters.showFilterToolbar(true);
			assertFalse(opened.child("Beta").isVisible());
			filters.showFilterToolbar(false);
			assertTrue(opened.child("Beta").isVisible());
			assertSame(firstHidden, inactiveFilter());
			assertTrue(firstHidden.areAncestorsShown());
			assertFalse(firstHidden.areDescendantsShown());
			assertTrue(((Condition) firstHidden.getCondition()).checkNode(opened.app, opened.child("Beta")));
		} finally {
			close(opened);
		}
	}

	@Test
	void hidingWhileAlreadyHiddenDoesNotClearAHiddenNode() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta");
		try {
			FilterController filters = install(opened);
			DefaultFilter filter = new DefaultFilter(new TextContains("Alpha"), false, false);
			opened.root.getMap().setFilter(filter);
			filter.applyFilter(opened.app);
			assertFalse(opened.child("Beta").isVisible());
			assertFalse(filters.isVisible());

			filters.showFilterToolbar(false);
			assertFalse(filters.isVisible());
			assertFalse(opened.child("Beta").isVisible());
		} finally {
			close(opened);
		}
	}

	@Test
	void showingWithoutAFilterThrowsAfterTheToolbarBecomesVisible() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Beta");
		try {
			FilterController filters = install(opened);
			assertTrue(opened.root.getMap().getFilter() instanceof DefaultFilter);
			opened.root.getMap().setFilter(null);
			assertNull(opened.root.getMap().getFilter());

			assertThrows(NullPointerException.class, () -> filters.showFilterToolbar(true));
			assertTrue(filters.isVisible());
			assertNull(opened.root.getMap().getFilter());
			assertTrue(opened.child("Beta").isVisible());

			filters.showFilterToolbar(true);
			assertTrue(filters.isVisible());
			assertNull(opened.root.getMap().getFilter());
			assertTrue(opened.child("Beta").isVisible());
		} finally {
			close(opened);
		}
	}

	@Test
	void showingWithoutAMapThrowsAndLeavesTheToolbarVisible() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Beta");
		try {
			FilterController filters = install(opened);
			filters.afterMapModuleChange(null, null, null, null);
			assertNull(filters.getMap());
			assertThrows(NullPointerException.class, () -> filters.showFilterToolbar(true));
			assertTrue(filters.isVisible());
			assertNull(filters.getMap());
		} finally {
			close(opened);
		}
	}

	@Test
	void anEmptyModelGainsTheTwoStandardConditionsAndSelectsNoFiltering() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta");
		try {
			FilterController filters = install(opened);
			DefaultComboBoxModel<Condition> model = new DefaultComboBoxModel<>();
			filters.setFilterConditionModel(model);
			assertSame(model, filters.getFilterConditionModel());
			assertEquals(2, model.getSize());
			assertSame(NoFilteringCondition.createCondition(), model.getElementAt(0));
			assertSame(SelectedViewCondition.CreateCondition(), model.getElementAt(1));
			assertSame(NoFilteringCondition.createCondition(), model.getSelectedItem());
			assertTrue(model.getElementAt(0).checkNode(opened.app, opened.child("Beta")));
		} finally {
			close(opened);
		}
	}

	@Test
	void aPreselectedConditionStaysSelectedWhenTheModelIsInstalled() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta");
		try {
			FilterController filters = install(opened);
			Condition contains = new TextContains("Alpha");
			DefaultComboBoxModel<Condition> model = new DefaultComboBoxModel<>();
			model.addElement(contains);
			model.setSelectedItem(contains);
			filters.setFilterConditionModel(model);
			assertEquals(3, model.getSize());
			assertSame(NoFilteringCondition.createCondition(), model.getElementAt(0));
			assertSame(SelectedViewCondition.CreateCondition(), model.getElementAt(1));
			assertSame(contains, model.getElementAt(2));
			assertSame(contains, model.getSelectedItem());
			assertTrue(opened.child("Alpha").isVisible());
			assertFalse(opened.child("Beta").isVisible());
		} finally {
			close(opened);
		}
	}

	@Test
	void settingTheModelBeforeTheToolbarExistsKeepsTheModelAndThrows() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha");
		try {
			FilterController filters = new FilterController(opened.app);
			installField(opened, filters);
			DefaultComboBoxModel<Condition> model = new DefaultComboBoxModel<>();
			assertThrows(NullPointerException.class, () -> filters.setFilterConditionModel(model));
			assertSame(model, filters.getFilterConditionModel());
		} finally {
			close(opened);
		}
	}

	@Test
	void closingAMapLeavesTheCurrentMapInPlaceAndAChangeStaysAllowed() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha");
		try {
			FilterController filters = install(opened);
			assertSame(opened.root.getMap(), filters.getMap());
			assertTrue(filters.isMapModuleChangeAllowed(null, null, null, null));
			filters.beforeMapModuleChange(null, null, opened.app.getMapModule(), opened.mode.getMode());
			filters.numberOfOpenMapInformation(0, -1);
			filters.numberOfOpenMapInformation(2, 1);
			filters.afterMapClose(opened.app.getMapModule(), opened.mode.getMode());
			assertSame(opened.root.getMap(), filters.getMap());
			assertFalse(filters.isVisible());
		} finally {
			close(opened);
		}
	}

	@Test
	void aSavedConditionLoadsBackAndAnEmptyFileLoadsNothing() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta");
		Path dir = Files.createTempDirectory("filter-conditions");
		try {
			FilterController filters = install(opened);
			Path source = dir.resolve("one.mmfilter");
			Files.writeString(source,
					"<filter_conditions><node_contains_condition value=\"Alpha\"/></filter_conditions>\n");
			DefaultComboBoxModel<Condition> loaded = new DefaultComboBoxModel<>();
			load(filters, loaded, source.toString());
			assertEquals(1, loaded.getSize());
			assertTrue(loaded.getElementAt(0).checkNode(opened.app, opened.child("Alpha")));
			assertFalse(loaded.getElementAt(0).checkNode(opened.app, opened.child("Beta")));

			Path saved = dir.resolve("again.mmfilter");
			save(filters, loaded, saved.toString());
			DefaultComboBoxModel<Condition> again = new DefaultComboBoxModel<>();
			again.addElement(NoFilteringCondition.createCondition());
			load(filters, again, saved.toString());
			assertEquals(1, again.getSize());
			assertTrue(again.getElementAt(0).checkNode(opened.app, opened.child("Alpha")));
			assertFalse(again.getElementAt(0).checkNode(opened.app, opened.child("Beta")));

			DefaultComboBoxModel<Condition> empty = new DefaultComboBoxModel<>();
			Path none = dir.resolve("none.mmfilter");
			save(filters, empty, none.toString());
			DefaultComboBoxModel<Condition> cleared = new DefaultComboBoxModel<>();
			cleared.addElement(NoFilteringCondition.createCondition());
			load(filters, cleared, none.toString());
			assertEquals(0, cleared.getSize());
		} finally {
			close(opened);
			delete(dir);
		}
	}

	@Test
	void aMissingFileEmptiesTheModelAndABrokenFileDoesToo() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha");
		Path dir = Files.createTempDirectory("filter-broken");
		try {
			FilterController filters = install(opened);
			DefaultComboBoxModel<Condition> missing = new DefaultComboBoxModel<>();
			missing.addElement(NoFilteringCondition.createCondition());
			assertThrows(IOException.class,
					() -> load(filters, missing, dir.resolve("absent.mmfilter").toString()));
			assertEquals(0, missing.getSize());

			Path broken = dir.resolve("broken.mmfilter");
			Files.writeString(broken, "<<<");
			DefaultComboBoxModel<Condition> bad = new DefaultComboBoxModel<>();
			bad.addElement(NoFilteringCondition.createCondition());
			assertThrows(XMLParseException.class, () -> load(filters, bad, broken.toString()));
			assertEquals(0, bad.getSize());

			assertThrows(IOException.class, () -> save(filters, bad, dir.toString()));
			assertEquals(0, bad.getSize());
		} finally {
			close(opened);
			delete(dir);
		}
	}

	@Test
	void savingWithNoToolbarLeavesTheModelUnset() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha");
		try {
			FilterController filters = new FilterController(opened.app);
			installField(opened, filters);
			assertNull(filters.getFilterConditionModel());
			filters.saveConditions();
			assertNull(filters.getFilterConditionModel());
			assertSame(FilterController.getConditionFactory(), FilterController.getConditionFactory());
		} finally {
			close(opened);
		}
	}

	private static FilterController install(Opened opened) throws Exception {
		FilterController filters = new FilterController(opened.app);
		installField(opened, filters);
		filters.afterMapModuleChange(null, null, opened.app.getMapModule(), opened.mode.getMode());
		return filters;
	}

	private static void installField(Opened opened, FilterController filters) throws Exception {
		Field field = Controller.class.getDeclaredField("mFilterController");
		field.setAccessible(true);
		field.set(opened.app, filters);
	}

	private static void load(FilterController filters, DefaultComboBoxModel<Condition> model, String path)
			throws Exception {
		Method method = FilterController.class.getDeclaredMethod("loadConditions", DefaultComboBoxModel.class,
				String.class);
		method.setAccessible(true);
		invoke(method, filters, model, path);
	}

	private static void save(FilterController filters, DefaultComboBoxModel<Condition> model, String path)
			throws Exception {
		Method method = FilterController.class.getDeclaredMethod("saveConditions", DefaultComboBoxModel.class,
				String.class);
		method.setAccessible(true);
		invoke(method, filters, model, path);
	}

	private static Object invoke(Method method, Object target, Object... args) throws Exception {
		try {
			return method.invoke(target, args);
		} catch (InvocationTargetException ex) {
			if (ex.getCause() instanceof Exception cause) {
				throw cause;
			}
			throw ex;
		}
	}

	private static Filter inactiveFilter() throws Exception {
		Field field = FilterController.class.getDeclaredField("inactiveFilter");
		field.setAccessible(true);
		return (Filter) field.get(null);
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
