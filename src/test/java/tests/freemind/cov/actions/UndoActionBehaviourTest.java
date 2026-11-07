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

package tests.freemind.cov.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.controller.actions.generated.instance.CompoundAction;
import freemind.controller.actions.generated.instance.EditNodeAction;
import freemind.controller.actions.generated.instance.XmlAction;
import freemind.modes.mindmapmode.MindMapController;
import freemind.modes.mindmapmode.actions.UndoAction;
import freemind.modes.mindmapmode.actions.xml.ActionPair;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** The undo list keeps a fixed number of edits and merges rapid ones. */
@DisplayName("UndoActionBehaviourTest")
class UndoActionBehaviourTest {

	@Test
	void theLevelDropsTheOldestEdit() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Leaf");
		opened.frame.setProperty("undo_levels", "2");
		ListedUndo undo = new ListedUndo(opened.mode);
		undo.add(edit("1"));
		assertEquals(1, undo.size());
		Thread.sleep(150);
		undo.add(edit("2"));
		assertEquals(2, undo.size());
		Thread.sleep(150);
		undo.add(edit("3"));
		assertEquals(2, undo.size());
		assertEquals("3", ((EditNodeAction) undo.newest()).getText());
		assertEquals("2", ((EditNodeAction) undo.older()).getText());

		opened.frame.setProperty("undo_levels", "0");
		ListedUndo empty = new ListedUndo(opened.mode);
		empty.add(edit("zero"));
		assertEquals(0, empty.size());

		opened.frame.setProperty("undo_levels", "-1");
		ListedUndo negative = new ListedUndo(opened.mode);
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> negative.add(edit("minus")));
		assertEquals(0, negative.size());

		opened.frame.setProperty("undo_levels", "many");
		ListedUndo text = new ListedUndo(opened.mode);
		text.add(edit("kept"));
		text.print();
		assertEquals(1, text.size());
		assertEquals("kept", ((EditNodeAction) text.newest()).getText());
		text.clear();
		assertEquals(0, text.size());
	}

	@Test
	void twoQuickEditsStayOneCompound() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Leaf");
		ListedUndo undo = new ListedUndo(opened.mode);
		undo.add(edit("a"));
		undo.add(edit("b"));
		assertEquals(1, undo.size());
		assertTrue(undo.newest() instanceof CompoundAction);
		CompoundAction compound = (CompoundAction) undo.newest();
		assertEquals(2, compound.sizeChoiceList());
		assertEquals("a", ((EditNodeAction) compound.getListChoiceList().get(0)).getText());
		assertEquals("b", ((EditNodeAction) compound.getListChoiceList().get(1)).getText());
	}

	@Test
	void TestREQ_DED_09_ShippedLimitKeepsOneHundredAndDropsTheOldest() throws Exception {
		Opened opened = MapFixture.open("Leaf");
		opened.frame.setProperty("undo_levels", "100");
		ListedUndo undo = new ListedUndo(opened.mode);
		addSeparate(undo, 1, 99);
		assertEquals(99, undo.size());
		assertEquals("99", text(undo.newest()));
		assertEquals("1", text(undo.oldest()));

		addSeparate(undo, 100, 100);
		assertEquals(100, undo.size());
		assertEquals("100", text(undo.newest()));
		assertEquals("1", text(undo.oldest()));

		addSeparate(undo, 101, 101);
		assertEquals(100, undo.size());
		assertEquals("101", text(undo.newest()));
		assertEquals("2", text(undo.oldest()));
	}

	@Test
	void TestREQ_DED_09_ZeroKeepsTheListEmpty() throws Exception {
		Opened opened = MapFixture.open("Leaf");
		opened.frame.setProperty("undo_levels", "0");
		ListedUndo undo = new ListedUndo(opened.mode);
		undo.add(edit("zero"));
		assertEquals(0, undo.size());
	}

	@Test
	void TestREQ_DED_09_NegativeThrowsAfterEmptyingTheList() throws Exception {
		Opened opened = MapFixture.open("Leaf");
		opened.frame.setProperty("undo_levels", "-1");
		ListedUndo undo = new ListedUndo(opened.mode);
		assertThrows(IndexOutOfBoundsException.class, () -> undo.add(edit("minus")));
		assertEquals(0, undo.size());
	}

	@Test
	void TestREQ_DED_09_ValuePastIntRangeLeavesTheLimitAtOneHundred() throws Exception {
		Opened opened = MapFixture.open("Leaf");
		opened.frame.setProperty("undo_levels", "2147483648");
		ListedUndo undo = new ListedUndo(opened.mode);
		addSeparate(undo, 1, 101);
		assertEquals(100, undo.size());
		assertEquals("101", text(undo.newest()));
		assertEquals("2", text(undo.oldest()));
	}

	@Test
	void TestREQ_DED_09_FoldedPairDoesNotApplyTheLimit() throws Exception {
		Opened opened = MapFixture.open("Leaf");
		opened.frame.setProperty("undo_levels", "1");
		ListedUndo undo = new ListedUndo(opened.mode);
		undo.add(edit("a"));
		undo.add(edit("b"));
		assertEquals(1, undo.size());
		CompoundAction compound = (CompoundAction) undo.newest();
		assertEquals(2, compound.sizeChoiceList());
		assertEquals("a", ((EditNodeAction) compound.getListChoiceList().get(0)).getText());
		assertEquals("b", ((EditNodeAction) compound.getListChoiceList().get(1)).getText());

		Thread.sleep(150);
		undo.add(edit("c"));
		assertEquals(1, undo.size());
		assertEquals("c", text(undo.newest()));
	}

	private static void addSeparate(ListedUndo undo, int from, int to) throws Exception {
		for (int i = from; i <= to; i++) {
			if (undo.size() > 0) {
				Thread.sleep(150);
			}
			undo.add(edit(Integer.toString(i)));
		}
	}

	private static String text(XmlAction action) {
		return ((EditNodeAction) action).getText();
	}

	private static ActionPair edit(String text) {
		EditNodeAction done = new EditNodeAction();
		done.setText(text);
		EditNodeAction undone = new EditNodeAction();
		undone.setText("old");
		return new ActionPair(done, undone);
	}

	private static final class ListedUndo extends UndoAction {
		private static final long serialVersionUID = 1L;

		ListedUndo(MindMapController controller) {
			super(controller);
		}

		int size() {
			return actionPairList.size();
		}

		XmlAction newest() {
			return actionPairList.get(0).getDoAction();
		}

		XmlAction older() {
			return actionPairList.get(1).getDoAction();
		}

		XmlAction oldest() {
			return actionPairList.get(actionPairList.size() - 1).getDoAction();
		}
	}
}
