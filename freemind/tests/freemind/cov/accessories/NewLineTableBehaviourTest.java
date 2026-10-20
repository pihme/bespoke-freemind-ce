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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.EventQueue;

import org.junit.jupiter.api.Test;

import accessories.plugins.NewLineTable;
import accessories.plugins.NodeAttributeTableRegistration;
import accessories.plugins.NodeAttributeTableRegistration.AttributeTableModel;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

class NewLineTableBehaviourTest {

	// backwards compatibility
	@Test
	void theExtraRowReadsBlankAndOnlyTheKeyIsEditable() throws Exception {
		Opened opened = MapFixture.open("Child");
		NodeAttributeTableRegistration registration = new NodeAttributeTableRegistration(opened.mode,
				opened.mode.getMap());
		AttributeTableModel model = registration.new AttributeTableModel(opened.mode);
		NewLineTable table = new NewLineTable();
		table.setModel(model);

		assertEquals(1, table.getRowCount());
		assertEquals("", table.getValueAt(0, NodeAttributeTableRegistration.KEY_COLUMN));
		assertEquals(0, table.convertRowIndexToModel(0));
		assertTrue(table.isCellEditable(0, NodeAttributeTableRegistration.KEY_COLUMN));
		assertFalse(table.isCellEditable(0, NodeAttributeTableRegistration.VALUE_COLUMN));
	}

	// backwards compatibility
	@Test
	void typingInTheExtraRowAppendsAnAttributeAndSelectsIt() throws Exception {
		Opened opened = MapFixture.open("Child");
		opened.mode.getMap().setSaved(true);
		NodeAttributeTableRegistration registration = new NodeAttributeTableRegistration(opened.mode,
				opened.mode.getMap());
		AttributeTableModel model = registration.new AttributeTableModel(opened.mode);
		NewLineTable table = new NewLineTable();
		table.setModel(model);
		table.setBounds(0, 0, 200, 80);

		table.setValueAt("Topic", 0, NodeAttributeTableRegistration.KEY_COLUMN);
		EventQueue.invokeAndWait(() -> {
		});

		assertEquals(2, table.getRowCount());
		assertEquals("Topic", table.getValueAt(0, NodeAttributeTableRegistration.KEY_COLUMN));
		assertEquals("", table.getValueAt(0, NodeAttributeTableRegistration.VALUE_COLUMN));
		assertEquals("", table.getValueAt(1, NodeAttributeTableRegistration.KEY_COLUMN));
		assertTrue(table.isCellEditable(0, NodeAttributeTableRegistration.VALUE_COLUMN));
		assertEquals(0, table.getSelectedRow());
		assertFalse(opened.mode.getMap().isSaved());
	}

	// backwards compatibility
	@Test
	void typingAValueIntoTheExtraRowStoresIt() throws Exception {
		Opened opened = MapFixture.open("Child");
		NodeAttributeTableRegistration registration = new NodeAttributeTableRegistration(opened.mode,
				opened.mode.getMap());
		AttributeTableModel model = registration.new AttributeTableModel(opened.mode);
		NewLineTable table = new NewLineTable();
		table.setModel(model);

		table.setValueAt("only-value", 0, NodeAttributeTableRegistration.VALUE_COLUMN);
		EventQueue.invokeAndWait(() -> {
		});

		assertEquals("", model.getAttributeHolder(0).mKey);
		assertEquals("only-value", model.getAttributeHolder(0).mValue);
		table.setValueAt("renamed", 0, NodeAttributeTableRegistration.KEY_COLUMN);
		assertEquals("renamed", table.getValueAt(0, 0));
		assertEquals(0, table.convertRowIndexToModel(0));
		assertEquals(1, table.convertRowIndexToModel(1));
	}
}
