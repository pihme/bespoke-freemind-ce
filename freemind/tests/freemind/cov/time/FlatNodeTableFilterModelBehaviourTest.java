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

package tests.freemind.cov.time;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import org.junit.jupiter.api.Test;

import accessories.plugins.time.FlatNodeTableFilterModel;
import accessories.plugins.time.TimeList.NodeHolder;
import accessories.plugins.time.TimeList.NotesHolder;
import freemind.modes.MindMapNode;
import tests.freemind.cov.MapFixture;

/** Backwards-compatible node and note filtering. */
class FlatNodeTableFilterModelBehaviourTest {

	@Test
	void filterMatchesNodeOrNotesAndIgnoresAStaleTableEvent() throws Exception {
		// backwards compatibility
		MapFixture.Opened opened = MapFixture.open("Alpha", "Alpha extra", "Beta");
		opened.child("Alpha").setNoteText("plain");
		opened.child("Alpha extra").setNoteText("plain");
		opened.child("Beta").setNoteText("secret note");
		MindMapNode blank = opened.mode.addNewNode(opened.root, opened.root.getChildCount(), false);
		blank.setText("");
		NodeModel model = new NodeModel();
		model.nodes.add(opened.child("Alpha"));
		model.nodes.add(opened.child("Alpha extra"));
		model.nodes.add(opened.child("Beta"));
		model.nodes.add(blank);
		FlatNodeTableFilterModel filter = new FlatNodeTableFilterModel(model, 0, 1);
		assertEquals(4, filter.getRowCount());
		assertEquals(2, filter.getColumnCount());
		assertEquals("nodes", filter.getColumnName(0));
		assertEquals(NodeHolder.class, filter.getColumnClass(0));

		filter.setFilter("alpha");
		assertEquals(1, filter.getRowCount());
		assertEquals("Alpha", text(filter, 0));

		filter.setFilter(".*secret.*");
		assertEquals(1, filter.getRowCount());
		assertEquals("Beta", text(filter, 0));
		assertEquals("secret note", ((NotesHolder) filter.getValueAt(0, 1)).toString());

		filter.setFilter("");
		assertEquals(1, filter.getRowCount());
		assertEquals("", text(filter, 0));

		filter.resetFilter();
		assertEquals(4, filter.getRowCount());
		int[] events = new int[1];
		filter.addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent event) {
				events[0]++;
			}
		});
		model.nodes.add(opened.root);
		model.fireTableDataChanged();
		assertEquals(1, events[0]);
		assertEquals(4, filter.getRowCount());
		filter.setFilter(".*");
		assertEquals(5, filter.getRowCount());

		assertThrows(IllegalArgumentException.class, () -> filter.getValueAt(-1, 0));
		assertThrows(IllegalArgumentException.class, () -> filter.getValueAt(filter.getRowCount(), 0));
		FlatNodeTableFilterModel empty = new FlatNodeTableFilterModel(new NodeModel(), 0, 1);
		assertEquals(0, empty.getRowCount());
		assertThrows(IllegalArgumentException.class, () -> empty.getValueAt(0, 0));
		assertThrows(java.util.regex.PatternSyntaxException.class, () -> filter.setFilter("("));
	}

	private static String text(FlatNodeTableFilterModel filter, int row) {
		return ((NodeHolder) filter.getValueAt(row, 0)).toString();
	}

	private static final class NodeModel extends AbstractTableModel {
		private final List<MindMapNode> nodes = new ArrayList<MindMapNode>();

		public int getRowCount() {
			return nodes.size();
		}

		public int getColumnCount() {
			return 2;
		}

		public String getColumnName(int column) {
			return column == 0 ? "nodes" : "notes";
		}

		public Class<?> getColumnClass(int column) {
			return column == 0 ? NodeHolder.class : NotesHolder.class;
		}

		public Object getValueAt(int row, int column) {
			MindMapNode node = nodes.get(row);
			return column == 0 ? new NodeHolder(node) : new NotesHolder(node);
		}
	}
}
