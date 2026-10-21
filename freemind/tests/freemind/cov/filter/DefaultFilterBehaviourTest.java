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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Timer;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.junit.jupiter.api.Test;

import freemind.controller.Controller;
import freemind.controller.filter.DefaultFilter;
import freemind.controller.filter.Filter;
import freemind.controller.filter.FilterInfo;
import freemind.controller.filter.condition.Condition;
import freemind.main.XMLElement;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.MindMapNodeModel;
import freemind.view.mindmapview.NodeView;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** A default filter shows matches, and ancestors or descendants only when asked. */
class DefaultFilterBehaviourTest {

	@Test
	void optionsFollowTheConstructorAndANullConditionShowsEveryNode() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta");
		try {
			Condition contains = new TextContains("Alpha");
			DefaultFilter both = new DefaultFilter(contains, true, true);
			assertSame(contains, both.getCondition());
			assertTrue(both.areMatchedShown());
			assertTrue(both.areAncestorsShown());
			assertTrue(both.areDescendantsShown());
			assertTrue(both.areEclipsedShown());
			assertFalse(both.areHiddenShown());

			DefaultFilter neither = new DefaultFilter(contains, false, false);
			assertFalse(neither.areAncestorsShown());
			assertFalse(neither.areDescendantsShown());
			assertTrue(neither.areMatchedShown());
			assertTrue(neither.areEclipsedShown());

			DefaultFilter ancestorsOnly = new DefaultFilter(contains, true, false);
			assertTrue(ancestorsOnly.areAncestorsShown());
			assertFalse(ancestorsOnly.areDescendantsShown());

			DefaultFilter descendantsOnly = new DefaultFilter(contains, false, true);
			assertFalse(descendantsOnly.areAncestorsShown());
			assertTrue(descendantsOnly.areDescendantsShown());

			MindMapNode beta = opened.child("Beta");
			beta.getFilterInfo().setMatched();
			DefaultFilter open = new DefaultFilter(null, false, false);
			assertNull(open.getCondition());
			assertTrue(open.isVisible(beta));
			assertFalse(open.areHiddenShown());
		} finally {
			close(opened);
		}
	}

	@Test
	void aNullConditionDoesNotChangeMarksOrTheSelection() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha");
		try {
			MindMapNode alpha = opened.child("Alpha");
			alpha.getFilterInfo().setMatched();
			select(opened, alpha);
			DefaultFilter filter = new DefaultFilter(null, true, false);
			opened.root.getMap().setFilter(filter);
			filter.applyFilter(opened.app);
			assertTrue(alpha.getFilterInfo().isMatched());
			assertSame(alpha, opened.view.getSelected().getModel());
		} finally {
			close(opened);
		}
	}

	@Test
	void aFreshNodeStaysVisibleUntilTheFilterHidesIt() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta");
		try {
			DefaultFilter filter = new DefaultFilter(new TextContains("Alpha"), false, false);
			assertEquals(Filter.FILTER_INITIAL_VALUE, flags(opened.child("Beta")));
			assertTrue(filter.isVisible(opened.child("Beta")));

			apply(opened, filter);
			assertTrue(opened.child("Alpha").isVisible());
			assertTrue(opened.child("Alpha").getFilterInfo().isMatched());
			assertFalse(opened.child("Beta").isVisible());
			assertFalse(opened.child("Beta").getFilterInfo().isMatched());
			assertEquals(Filter.FILTER_SHOW_HIDDEN, flags(opened.child("Beta")));
		} finally {
			close(opened);
		}
	}

	@Test
	void ancestorsStayVisibleOnlyWhenThatOptionIsOn() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta");
		try {
			MindMapNode beta = opened.child("Beta");
			MindMapNode nested = new MindMapNodeModel("Alpha under", opened.root.getMap());
			beta.insert(nested, 0);

			DefaultFilter shown = new DefaultFilter(new TextContains("Alpha"), true, false);
			apply(opened, shown);
			assertTrue(beta.getFilterInfo().isAncestor());
			assertTrue(beta.isVisible());
			assertTrue(nested.getFilterInfo().isMatched());

			DefaultFilter hidden = new DefaultFilter(new TextContains("Alpha"), false, false);
			apply(opened, hidden);
			assertTrue(beta.getFilterInfo().isAncestor());
			assertFalse(beta.isVisible());
			assertTrue(nested.isVisible());
		} finally {
			close(opened);
		}
	}

	@Test
	void descendantsOfAMatchStayVisibleOnlyWhenThatOptionIsOn() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta");
		try {
			MindMapNode alpha = opened.child("Alpha");
			MindMapNode plain = new MindMapNodeModel("plain", opened.root.getMap());
			alpha.insert(plain, 0);

			DefaultFilter hidden = new DefaultFilter(new TextContains("Alpha"), true, false);
			apply(opened, hidden);
			assertEquals(Filter.FILTER_SHOW_DESCENDANT | Filter.FILTER_SHOW_HIDDEN, flags(plain));
			assertFalse(plain.isVisible());
			assertTrue(alpha.isVisible());

			DefaultFilter shown = new DefaultFilter(new TextContains("Alpha"), false, true);
			apply(opened, shown);
			assertTrue(plain.isVisible());
			assertFalse(plain.getFilterInfo().isMatched());
		} finally {
			close(opened);
		}
	}

	@Test
	void aMatchUnderAHiddenParentIsEclipsedAndStillShown() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Beta");
		try {
			MindMapNode beta = opened.child("Beta");
			MindMapNode match = new MindMapNodeModel("Alpha under", opened.root.getMap());
			MindMapNode plain = new MindMapNodeModel("plain under", opened.root.getMap());
			beta.insert(match, 0);
			beta.insert(plain, 1);

			DefaultFilter filter = new DefaultFilter(new TextContains("Alpha"), false, false);
			apply(opened, filter);
			assertTrue(beta.getFilterInfo().isAncestor());
			assertFalse(beta.isVisible());
			assertEquals(Filter.FILTER_SHOW_MATCHED | Filter.FILTER_SHOW_ECLIPSED, flags(match));
			assertTrue(match.isVisible());
			assertTrue(filter.areEclipsedShown());
			assertEquals(Filter.FILTER_SHOW_HIDDEN | Filter.FILTER_SHOW_ECLIPSED, flags(plain));
			assertFalse(plain.isVisible());
		} finally {
			close(opened);
		}
	}

	@Test
	void aMatchingRootDoesNotMarkItselfAndDoesMarkItsChildrenAsDescendants() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("nope");
		try {
			DefaultFilter filter = new DefaultFilter(new TextContains("ROOT"), false, false);
			apply(opened, filter);
			assertFalse(opened.root.getFilterInfo().isMatched());
			assertFalse(opened.root.getFilterInfo().isAncestor());
			assertEquals(Filter.FILTER_INITIAL_VALUE, flags(opened.root));
			assertTrue(opened.root.isVisible());
			MindMapNode nope = opened.child("nope");
			assertEquals(Filter.FILTER_SHOW_DESCENDANT | Filter.FILTER_SHOW_HIDDEN, flags(nope));
			assertFalse(nope.isVisible());

			DefaultFilter withDescendants = new DefaultFilter(new TextContains("ROOT"), false, true);
			apply(opened, withDescendants);
			assertTrue(nope.isVisible());
		} finally {
			close(opened);
		}
	}

	@Test
	void aMapWithNoChildrenLeavesTheRootUnmarked() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open();
		try {
			DefaultFilter filter = new DefaultFilter(new TextContains("Alpha"), true, true);
			apply(opened, filter);
			assertEquals(Filter.FILTER_INITIAL_VALUE, flags(opened.root));
			assertFalse(opened.root.getFilterInfo().isAncestor());
			assertFalse(opened.root.getFilterInfo().isMatched());
			assertTrue(opened.root.isVisible());
			assertEquals(0, opened.root.getChildCount());
		} finally {
			close(opened);
		}
	}

	@Test
	void anEmptySelectionIsLeftAlone() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha");
		try {
			while (opened.view.getSelected() != null) {
				opened.view.deselect(opened.view.getSelected());
			}
			opened.view.setSiblingMaxLevel(7);
			DefaultFilter.selectVisibleNode(opened.view);
			assertNull(opened.view.getSelected());
			assertEquals(7, opened.view.getSiblingMaxLevel());
		} finally {
			close(opened);
		}
	}

	@Test
	void aHiddenSelectionMovesToTheNearestVisibleParent() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta");
		try {
			MindMapNode beta = opened.child("Beta");
			MindMapNode nested = opened.mode.addNewNode(beta, 0, false);
			opened.mode.setNodeText(nested, "Nested");
			NodeView nestedView = opened.view.getNodeView(nested);
			assertNotNull(nestedView);
			opened.view.selectAsTheOnlyOneSelected(nestedView);

			DefaultFilter filter = new DefaultFilter(new TextContains("Alpha"), false, false);
			apply(opened, filter);
			assertFalse(beta.isVisible());
			assertFalse(nested.isVisible());
			assertSame(opened.root, opened.view.getSelected().getModel());
			assertEquals(opened.root.getNodeLevel(), opened.view.getSiblingMaxLevel());
			assertEquals(1, opened.view.getSelecteds().size());
		} finally {
			close(opened);
		}
	}

	@Test
	void aHiddenExtraSelectionIsDroppedAndTheVisibleOneStays() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta");
		try {
			NodeView rootView = opened.view.getNodeView(opened.root);
			NodeView betaView = opened.view.getNodeView(opened.child("Beta"));
			opened.view.selectAsTheOnlyOneSelected(rootView);
			opened.view.toggleSelected(betaView);

			DefaultFilter filter = new DefaultFilter(new TextContains("Alpha"), true, false);
			apply(opened, filter);
			assertSame(opened.root, opened.view.getSelected().getModel());
			assertEquals(1, opened.view.getSelecteds().size());
			assertFalse(opened.view.getSelecteds().contains(betaView));
			assertEquals(opened.root.getNodeLevel(), opened.view.getSiblingMaxLevel());
		} finally {
			close(opened);
		}
	}

	@Test
	void aVisibleSelectionKeepsItsPlaceAndSetsTheSiblingLevel() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta");
		try {
			MindMapNode alpha = opened.child("Alpha");
			opened.view.selectAsTheOnlyOneSelected(opened.view.getNodeView(alpha));
			DefaultFilter filter = new DefaultFilter(new TextContains("Alpha"), true, false);
			apply(opened, filter);
			assertSame(alpha, opened.view.getSelected().getModel());
			assertEquals(alpha.getNodeLevel(), opened.view.getSiblingMaxLevel());
			assertTrue(alpha.isVisible());
		} finally {
			close(opened);
		}
	}

	private static void apply(Opened opened, DefaultFilter filter) {
		opened.root.getMap().setFilter(filter);
		filter.applyFilter(opened.app);
	}

	private static void select(Opened opened, MindMapNode node) {
		opened.view.selectAsTheOnlyOneSelected(opened.view.getNodeView(node));
	}

	private static int flags(MindMapNode node) throws Exception {
		Method method = FilterInfo.class.getDeclaredMethod("get");
		method.setAccessible(true);
		return ((Integer) method.invoke(node.getFilterInfo())).intValue();
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

	/** Node text contains a fragment. Used only to drive {@link DefaultFilter}. */
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
