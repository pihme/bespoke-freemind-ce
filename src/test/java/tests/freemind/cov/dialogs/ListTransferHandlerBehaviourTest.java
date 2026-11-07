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

package tests.freemind.cov.dialogs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.TransferHandler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import accessories.plugins.dialogs.ListTransferHandler;
import freemind.swing.DefaultListModel;

/** A list drag copies the selection, and a move drops it after the target row. */
@DisplayName("ListTransferHandlerBehaviourTest")
class ListTransferHandlerBehaviourTest {

	@Test
	void onlyALocalListCanBeImportedAndTheSourceAllowsCopyOrMove() {
		// backwards compatibility
		OpenHandler handler = new OpenHandler();
		JList<String> list = list("A", "B");
		assertEquals(TransferHandler.COPY_OR_MOVE, handler.getSourceActions(new JPanel()));
		assertNull(handler.carry(new JPanel()));

		list.setSelectedIndex(1);
		Transferable carried = handler.carry(list);
		DataFlavor flavor = carried.getTransferDataFlavors()[0];
		assertTrue(handler.canImport(list, new DataFlavor[] { flavor }));
		assertFalse(handler.canImport(list, new DataFlavor[] { DataFlavor.stringFlavor }));
		assertFalse(handler.importData(list, stringTransfer()));
		assertEquals("B", list.getModel().getElementAt(1));
		assertThrows(UnsupportedFlavorException.class, () -> carried.getTransferData(DataFlavor.stringFlavor));
	}

	@Test
	void aDropAppendsWhenNothingIsSelectedAndInsertsAfterTheSelectedRow() throws Exception {
		// backwards compatibility
		OpenHandler handler = new OpenHandler();
		JList<String> source = list("A", "B", "C");
		source.setSelectedIndex(0);
		Transferable carried = handler.carry(source);

		JList<String> append = list("Z");
		assertTrue(handler.importData(append, carried));
		assertEquals("A", append.getModel().getElementAt(1));

		JList<String> insert = list("Y", "Z");
		insert.setSelectedIndex(0);
		assertTrue(handler.importData(insert, carried));
		assertEquals("A", insert.getModel().getElementAt(1));
		assertEquals("Z", insert.getModel().getElementAt(2));
	}

	@Test
	void droppingOntoTheSameSelectionLeavesItAndAMoveRemovesTheAdjustedIndex() throws Exception {
		// backwards compatibility
		OpenHandler handler = new OpenHandler();
		JList<String> source = list("A", "B", "C");
		source.setSelectedIndex(1);
		Transferable carried = handler.carry(source);
		assertTrue(handler.importData(source, carried));
		assertEquals(3, source.getModel().getSize());
		handler.finish(source, carried, TransferHandler.MOVE);
		assertEquals(3, source.getModel().getSize());

		JList<String> from = list("A", "B", "C", "D");
		from.setSelectedIndex(2);
		Transferable moved = handler.carry(from);
		JList<String> onto = list("Z");
		onto.setSelectedIndex(0);
		assertTrue(handler.importData(onto, moved));
		handler.finish(from, moved, TransferHandler.MOVE);
		assertEquals("C", onto.getModel().getElementAt(1));
		assertEquals("A", from.getModel().getElementAt(0));
		assertEquals("B", from.getModel().getElementAt(1));
		assertEquals("C", from.getModel().getElementAt(2));
		assertEquals(3, from.getModel().getSize());

		JList<String> kept = list("A", "B");
		kept.setSelectedIndex(0);
		Transferable copied = handler.carry(kept);
		JList<String> dest = list("Z");
		assertTrue(handler.importData(dest, copied));
		handler.finish(kept, copied, TransferHandler.COPY);
		assertEquals(2, kept.getModel().getSize());
	}

	@Test
	void aFlavorThatCannotBeReadIsRefused() throws Exception {
		// backwards compatibility
		OpenHandler handler = new OpenHandler();
		JList<String> source = list("A");
		source.setSelectedIndex(0);
		DataFlavor flavor = handler.carry(source).getTransferDataFlavors()[0];
		JList<String> target = list("Z");

		Transferable unsupported = mock(Transferable.class);
		when(unsupported.getTransferDataFlavors()).thenReturn(new DataFlavor[] { flavor });
		when(unsupported.getTransferData(flavor)).thenThrow(new UnsupportedFlavorException(flavor));
		assertFalse(handler.importData(target, unsupported));

		Transferable broken = mock(Transferable.class);
		when(broken.getTransferDataFlavors()).thenReturn(new DataFlavor[] { flavor });
		when(broken.getTransferData(flavor)).thenThrow(new IOException("closed"));
		assertFalse(handler.importData(target, broken));
		assertEquals(1, target.getModel().getSize());
	}

	private static final class OpenHandler extends ListTransferHandler {
		Transferable carry(javax.swing.JComponent component) {
			return createTransferable(component);
		}

		void finish(javax.swing.JComponent component, Transferable data, int action) {
			exportDone(component, data, action);
		}
	}

	private static JList<String> list(String... values) {
		DefaultListModel<String> model = new DefaultListModel<>();
		for (String value : values) {
			model.addElement(value);
		}
		return new JList<>(model);
	}

	private static Transferable stringTransfer() {
		Transferable transferable = mock(Transferable.class);
		when(transferable.getTransferDataFlavors()).thenReturn(new DataFlavor[] { DataFlavor.stringFlavor });
		return transferable;
	}
}
