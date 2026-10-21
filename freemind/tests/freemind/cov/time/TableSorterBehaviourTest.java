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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Comparator;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

import org.junit.jupiter.api.Test;

import accessories.plugins.time.TableSorter;

/** Backwards-compatible header sorting and comparators. */
class TableSorterBehaviourTest {

	@Test
	void headerClicksCycleSortAndControlKeepsTheOtherColumn() {
		// backwards compatibility
		DefaultTableModel model = new DefaultTableModel(new Object[] { "Name", "Note" }, 0);
		model.addRow(new Object[] { "b", "2" });
		model.addRow(new Object[] { "a", "1" });
		model.addRow(new Object[] { null, "0" });
		OpenSorter sorter = new OpenSorter(model);
		JTable table = new JTable(sorter);
		JTableHeader header = table.getTableHeader();
		int listenersBefore = header.getMouseListeners().length;
		sorter.setTableHeader(header);
		assertTrue(header.getMouseListeners().length > listenersBefore);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.getColumnModel().getColumn(0).setPreferredWidth(100);
		table.getColumnModel().getColumn(1).setPreferredWidth(100);
		table.setSize(200, 80);
		header.setSize(200, 20);
		table.doLayout();

		click(header, 10, 0);
		assertEquals(TableSorter.ASCENDING, sorter.getSortingStatus(0));
		assertNull(sorter.getValueAt(0, 0));
		click(header, 10, 0);
		assertEquals(TableSorter.DESCENDING, sorter.getSortingStatus(0));
		click(header, 10, 0);
		assertEquals(TableSorter.NOT_SORTED, sorter.getSortingStatus(0));
		assertFalse(sorter.isSorting());

		click(header, 10, InputEvent.SHIFT_DOWN_MASK);
		assertEquals(TableSorter.DESCENDING, sorter.getSortingStatus(0));
		click(header, 150, InputEvent.CTRL_DOWN_MASK);
		assertEquals(TableSorter.DESCENDING, sorter.getSortingStatus(0));
		assertEquals(TableSorter.ASCENDING, sorter.getSortingStatus(1));
		assertTrue(sorter.isSorting());

		JTableHeader replacement = new JTableHeader(table.getColumnModel());
		sorter.setTableHeader(replacement);
		assertEquals(listenersBefore, header.getMouseListeners().length);
		assertSame(replacement, sorter.getTableHeader());
		assertSame(model, sorter.getTableModel());
	}

	@Test
	void comparatorsNullsAndASingleUnsortedCellKeepTheirOrder() throws Exception {
		// backwards compatibility
		assertTrue(((Comparator<String>) TableSorter.COMPARABLE_COMAPRATOR).compare("B", "a") < 0);
		assertTrue(((Comparator<Object>) TableSorter.LEXICAL_COMPARATOR).compare("B", "a") > 0);

		DefaultTableModel model = new DefaultTableModel(new Object[] { "Name", "N" }, 0) {
			public Class<?> getColumnClass(int column) {
				return column == 1 ? Integer.class : String.class;
			}
		};
		model.addRow(new Object[] { "b", Integer.valueOf(10) });
		model.addRow(new Object[] { "A", Integer.valueOf(2) });
		OpenSorter sorter = new OpenSorter(model);
		assertSame(TableSorter.COMPARABLE_COMAPRATOR, sorter.comparator(1));
		sorter.setColumnComparator(Integer.class, new Comparator<Integer>() {
			public int compare(Integer left, Integer right) {
				return right.compareTo(left);
			}
		});
		sorter.setSortingStatus(1, TableSorter.ASCENDING);
		assertEquals(Integer.valueOf(10), sorter.getValueAt(0, 1));
		sorter.setColumnComparator(Integer.class, null);
		sorter.setSortingStatus(1, TableSorter.NOT_SORTED);
		sorter.setSortingStatus(1, TableSorter.ASCENDING);
		assertEquals(Integer.valueOf(2), sorter.getValueAt(0, 1));
		assertSame(TableSorter.COMPARABLE_COMAPRATOR, sorter.comparator(0));

		Icon plain = sorter.icon(1, 12);
		assertEquals(12, plain.getIconWidth());
		assertEquals(12, plain.getIconHeight());
		assertTrue(ink(plain, null) > 0);
		sorter.setSortingStatus(0, TableSorter.DESCENDING);
		Icon second = sorter.icon(0, 12);
		assertTrue(ink(second, new JLabel()) > 0);
		assertNull(sorter.icon(3, 12));
		sorter.setSortingStatus(0, TableSorter.NOT_SORTED);
		assertEquals(TableSorter.NOT_SORTED, sorter.getSortingStatus(0));
		assertEquals(TableSorter.ASCENDING, sorter.getSortingStatus(1));

		int[] event = new int[] { -1, -1, -1 };
		sorter.addTableModelListener(e -> {
			event[0] = e.getFirstRow();
			event[1] = e.getLastRow();
			event[2] = e.getColumn();
		});
		java.lang.reflect.Method modelToView = TableSorter.class.getDeclaredMethod("getModelToView");
		modelToView.setAccessible(true);
		int[] order = (int[]) modelToView.invoke(sorter);
		assertEquals(2, order.length);
		model.setValueAt("z", 0, 0);
		assertEquals("z", sorter.getValueAt(order[0], 0));
		assertEquals(order[0], event[0]);
		assertEquals(order[0], event[1]);
		assertEquals(0, event[2]);
		assertEquals("z", model.getValueAt(0, 0));

		sorter.setValueAt("changed", 0, 0);
		assertEquals("changed", model.getValueAt(sorter.modelIndex(0), 0));
		assertTrue(sorter.isCellEditable(0, 0));
		assertEquals("Name", sorter.getColumnName(0));
		assertEquals(String.class, sorter.getColumnClass(0));

		model.addColumn("Extra");
		assertFalse(sorter.isSorting());
		OpenSorter bare = new OpenSorter();
		assertEquals(0, bare.getRowCount());
		assertEquals(0, bare.getColumnCount());
		bare.setTableModel(null);
		assertEquals(0, bare.getRowCount());
	}

	private static int ink(Icon icon, Component component) {
		BufferedImage image = new BufferedImage(40, 40, BufferedImage.TYPE_INT_RGB);
		Graphics graphics = image.getGraphics();
		int count = 0;
		try {
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, 40, 40);
			icon.paintIcon(component, graphics, 4, 4);
			for (int y = 0; y < 40; y++) {
				for (int x = 0; x < 40; x++) {
					if (image.getRGB(x, y) != Color.WHITE.getRGB()) {
						count++;
					}
				}
			}
		} finally {
			graphics.dispose();
		}
		return count;
	}

	private static void click(JTableHeader header, int x, int modifiers) {
		MouseEvent event = new MouseEvent(header, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(),
				modifiers, x, 5, 1, false);
		for (java.awt.event.MouseListener listener : header.getMouseListeners()) {
			listener.mouseClicked(event);
		}
	}

	static final class OpenSorter extends TableSorter {
		OpenSorter() {
			super();
		}

		OpenSorter(TableModel model) {
			super(model);
		}

		Icon icon(int column, int size) {
			return getHeaderRendererIcon(column, size);
		}

		Comparator<?> comparator(int column) {
			return getComparator(column);
		}
	}
}
