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

package tests.freemind.cov.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import freemind.controller.Controller;
import freemind.controller.LastOpenedList;
import tests.freemind.cov.MapFixture;

/** REQ-DED-08. The recent-files list drops the oldest entry past its limit. */
class LastOpenedListBehaviourTest {

	private Controller controller;

	@BeforeEach
	void openMap() throws Exception {
		controller = MapFixture.open("Child").app;
	}

	@Test
	void TestREQ_DED_08_ShippedLimitKeepsTwentyFiveAndDropsTheOldest() {
		controller.getFrame().setProperty("last_opened_list_length", "25");
		LastOpenedList list = new LastOpenedList(controller, "");

		// backwards compatibility is the requirement: one under, at the limit, just over
		addCount(list, 24);
		assertEquals(names(1, 24), entries(list));

		list.add(name(25), name(25));
		assertEquals(names(1, 25), entries(list));

		list.add(name(26), name(26));
		assertEquals(name(26), entries(list).get(0));
		assertFalse(entries(list).contains(name(1)));
		assertEquals(25, entries(list).size());
	}

	@Test
	void TestREQ_DED_08_ZeroKeepsTheListEmpty() {
		controller.getFrame().setProperty("last_opened_list_length", "0");
		LastOpenedList list = new LastOpenedList(controller, "");
		list.add("mindmap:/tmp/a.mm", "a");
		assertEquals(List.of(), entries(list));
	}

	@Test
	void TestREQ_DED_08_NegativeThrowsAfterEmptyingTheList() {
		controller.getFrame().setProperty("last_opened_list_length", "-1");
		LastOpenedList list = new LastOpenedList(controller, "");
		assertThrows(IndexOutOfBoundsException.class,
				() -> list.add("mindmap:/tmp/a.mm", "a"));
		assertEquals(List.of(), entries(list));
	}

	@Test
	void TestREQ_DED_08_ValuePastIntRangeLeavesTheLimitAtTwentyFive() {
		controller.getFrame().setProperty("last_opened_list_length", "2147483648");
		LastOpenedList list = new LastOpenedList(controller, "");
		addCount(list, 26);
		assertEquals(25, entries(list).size());
		assertEquals(name(26), entries(list).get(0));
		assertFalse(entries(list).contains(name(1)));
	}

	@Test
	void openingTheSamePathMovesItToTheFront() {
		// backwards compatibility
		controller.getFrame().setProperty("last_opened_list_length", "25");
		LastOpenedList list = new LastOpenedList(controller, "");
		list.add("mindmap:/tmp/a.mm", "a");
		list.add("mindmap:/tmp/b.mm", "b");
		list.add("mindmap:/tmp/a.mm", "a-again");
		assertEquals(List.of("mindmap:/tmp/a.mm", "mindmap:/tmp/b.mm"), entries(list));
	}

	@Test
	void aNullPathIsIgnored() {
		// backwards compatibility
		controller.getFrame().setProperty("last_opened_list_length", "25");
		LastOpenedList list = new LastOpenedList(controller, "mindmap:/tmp/kept.mm");
		list.add(null, "nope");
		assertEquals(List.of("mindmap:/tmp/kept.mm"), entries(list));
	}

	@Test
	void mapOpenedWithNoModuleLeavesTheLoadedList() {
		// backwards compatibility
		controller.getFrame().setProperty("last_opened_list_length", "25");
		LastOpenedList list = new LastOpenedList(controller, "mindmap:/tmp/kept.mm");
		list.mapOpened(null);
		assertTrue(entries(list).contains("mindmap:/tmp/kept.mm"));
	}

	private static void addCount(LastOpenedList list, int count) {
		for (int i = 1; i <= count; i++) {
			list.add(name(i), name(i));
		}
	}

	private static String name(int i) {
		return "mindmap:/tmp/" + i + ".mm";
	}

	private static List<String> names(int from, int to) {
		List<String> names = new ArrayList<>();
		for (int i = to; i >= from; i--) {
			names.add(name(i));
		}
		return names;
	}

	private static List<String> entries(LastOpenedList list) {
		String saved = list.save();
		if (saved.isEmpty()) {
			return List.of();
		}
		return Arrays.asList(saved.split(";"));
	}
}
