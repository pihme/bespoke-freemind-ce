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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import freemind.controller.filter.Filter;
import freemind.controller.filter.FilterInfo;

/** Filter marks are bits. Matching or hiding clears the initial bit; other marks do not. */
class FilterInfoBehaviourTest {

	@Test
	void aNewFilterInfoStartsAtTheInitialFlag() throws Exception {
		// backwards compatibility
		FilterInfo info = new FilterInfo();
		assertEquals(Filter.FILTER_INITIAL_VALUE, flags(info));
		assertFalse(info.isMatched());
		assertFalse(info.isAncestor());
	}

	@Test
	void matchingClearsTheInitialFlagAndAnAncestorMarkDoesNot() throws Exception {
		// backwards compatibility
		FilterInfo info = new FilterInfo();
		info.setAncestor();
		assertEquals(Filter.FILTER_INITIAL_VALUE | Filter.FILTER_SHOW_ANCESTOR, flags(info));
		assertTrue(info.isAncestor());
		assertFalse(info.isMatched());

		info.setMatched();
		assertEquals(Filter.FILTER_SHOW_MATCHED | Filter.FILTER_SHOW_ANCESTOR, flags(info));
		assertTrue(info.isMatched());
		assertTrue(info.isAncestor());
		assertEquals(0, flags(info) & Filter.FILTER_INITIAL_VALUE);
	}

	@Test
	void hidingClearsTheInitialFlagAndKeepsAnAncestorMark() throws Exception {
		// backwards compatibility
		FilterInfo info = new FilterInfo();
		info.setAncestor();
		add(info, Filter.FILTER_SHOW_HIDDEN);
		assertEquals(Filter.FILTER_SHOW_ANCESTOR | Filter.FILTER_SHOW_HIDDEN, flags(info));
		assertTrue(info.isAncestor());
		assertFalse(info.isMatched());
	}

	@Test
	void aDescendantMarkSitsBesideTheInitialFlag() throws Exception {
		// backwards compatibility
		FilterInfo info = new FilterInfo();
		info.setDescendant();
		assertEquals(Filter.FILTER_INITIAL_VALUE | Filter.FILTER_SHOW_DESCENDANT, flags(info));
		assertFalse(info.isMatched());
		assertFalse(info.isAncestor());
	}

	@Test
	void resetRestoresTheInitialFlag() throws Exception {
		// backwards compatibility
		FilterInfo info = new FilterInfo();
		info.setMatched();
		info.setAncestor();
		info.setDescendant();
		info.reset();
		assertEquals(Filter.FILTER_INITIAL_VALUE, flags(info));
		assertFalse(info.isMatched());
		assertFalse(info.isAncestor());
	}

	@Test
	void zeroLeavesTheFlagsUntouchedAndNegativeSetsEveryBit() throws Exception {
		// backwards compatibility
		FilterInfo info = new FilterInfo();
		add(info, 0);
		assertEquals(Filter.FILTER_INITIAL_VALUE, flags(info));
		assertFalse(info.isMatched());

		add(info, -1);
		assertEquals(-1, flags(info));
		assertTrue(info.isMatched());
		assertTrue(info.isAncestor());
	}

	private static void add(FilterInfo info, int flag) throws Exception {
		Method method = FilterInfo.class.getDeclaredMethod("add", int.class);
		method.setAccessible(true);
		method.invoke(info, Integer.valueOf(flag));
	}

	private static int flags(FilterInfo info) throws Exception {
		Method method = FilterInfo.class.getDeclaredMethod("get");
		method.setAccessible(true);
		return ((Integer) method.invoke(info)).intValue();
	}
}
