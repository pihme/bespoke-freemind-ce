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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.HeadlessException;
import java.awt.IllegalComponentStateException;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import accessories.plugins.NodeAttributeTableRegistration;
import accessories.plugins.NodeAttributeTableRegistration.AttributeHolder;
import accessories.plugins.NodeAttributeTableRegistration.AttributeTableModel;
import freemind.main.FreeMind;
import freemind.modes.MindMapNode;
import freemind.modes.ModeController.NodeLifetimeListener;
import freemind.modes.ModeController.NodeSelectionListener;
import freemind.modes.attributes.Attribute;
import freemind.view.mindmapview.NodeView;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("NodeAttributeTableRegistrationBehaviourTest")
class NodeAttributeTableRegistrationBehaviourTest {

	// backwards compatibility
	@Test
	void attributesMoveBetweenTheTableAndTheNode() throws Exception {
		Opened opened = MapFixture.open("Child");
		opened.frame.setProperty(FreeMind.RESOURCES_SHOW_ATTRIBUTE_PANE, "true");
		opened.frame.setProperty("attribute_table_properties", "<attribute_table_properties/>");
		opened.view.getRoot().updateAll();
		MindMapNode child = opened.child("Child");
		NodeAttributeTableRegistration registration = new NodeAttributeTableRegistration(opened.mode,
				opened.mode.getMap());
		registration.register();
		try {
			assertTrue(registration.getSplitPaneVisible());
			assertTrue(registration.isSelected(new JMenuItem(), null));
			AttributeTableModel model = model(registration);
			assertEquals(String.class, model.getColumnClass(NodeAttributeTableRegistration.KEY_COLUMN));
			assertEquals(Object.class, model.getColumnClass(5));
			assertTrue(model.isCellEditable(0, 0));
			AttributeHolder holder = new AttributeHolder();
			holder.mKey = "Topic";
			holder.mValue = "Mind";
			opened.mode.getMap().setSaved(true);
			assertEquals(0, model.addAttributeHolder(holder, true));
			assertFalse(opened.mode.getMap().isSaved());
			assertEquals("Topic", model.getValueAt(0, NodeAttributeTableRegistration.KEY_COLUMN));
			assertEquals("Mind", model.getValueAt(0, NodeAttributeTableRegistration.VALUE_COLUMN));
			assertNull(model.getValueAt(0, 5));
			assertEquals(holder, model.getAttributeHolder(0));
			opened.mode.getMap().setSaved(true);
			model.setValueAt("Topic", 0, NodeAttributeTableRegistration.KEY_COLUMN);
			assertTrue(opened.mode.getMap().isSaved());
			model.setValueAt("Map", 0, NodeAttributeTableRegistration.VALUE_COLUMN);
			assertFalse(opened.mode.getMap().isSaved());
			model.addAttributeHolder(new Attribute("Rank", "1"), false);
			assertEquals(2, model.getRowCount());

			JTable table = table(registration);
			table.setRowHeight(20);
			table.setSize(120, 80);
			table.doLayout();
			table.setCellEditor(new DefaultCellEditor(new JTextField()));
			opened.mode.getMap().setSaved(true);
			registration.insertUpdate(null);
			registration.removeUpdate(null);
			registration.changedUpdate(null);
			assertFalse(opened.mode.getMap().isSaved());
			table.setCellEditor(null);
			opened.mode.getMap().setSaved(true);
			registration.checkForUpdate();
			assertTrue(opened.mode.getMap().isSaved());

			registration.focusAttributeTable();
			assertEquals(0, table.getSelectedRow());
			MouseEvent popup = new MouseEvent(table, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, 5, 5, 1,
					true, MouseEvent.BUTTON3);
			try {
				table.dispatchEvent(popup);
			} catch (RuntimeException ex) {
				assertTrue(ex instanceof HeadlessException || ex instanceof IllegalComponentStateException, ex.toString());
			}
			JPopupMenu menu = (JPopupMenu) field(registration, "mPopupMenu");
			menu.setInvoker(table);
			table.setRowSelectionInterval(1, 1);
			int rows = model.getRowCount();
			((JMenuItem) menu.getComponent(0)).doClick();
			assertEquals(rows - 1, model.getRowCount());

			NodeLifetimeListener manager = (NodeLifetimeListener) field(registration, "mAttributeManager");
			NodeSelectionListener selection = (NodeSelectionListener) manager;
			selection.onFocusNode(opened.view.getNodeView(child));
			assertEquals(0, model.getRowCount());
			selection.onUpdateNodeHook(child);
			assertEquals(0, model.getRowCount());
			manager.onPreDeleteNode(child);
			manager.onPostDeleteNode(child, opened.root);
			assertEquals(0, child.getAttributeTableLength());
			AttributeHolder saved = new AttributeHolder();
			saved.mKey = "SavedKey";
			saved.mValue = "SavedValue";
			opened.mode.getMap().setSaved(true);
			assertEquals(0, model.addAttributeHolder(saved, true));
			assertFalse(opened.mode.getMap().isSaved());
			selection.onSaveNode(opened.root);
			assertEquals(0, child.getAttributeTableLength());
			selection.onSaveNode(child);
			assertEquals("SavedKey", child.getAttribute(0).getName());
			assertEquals("SavedValue", child.getAttribute(0).getValue());
			selection.onUpdateNodeHook(child);
			assertEquals("SavedKey", model.getValueAt(0, 0));

			Action jump = jump(registration);
			NodeView selectedView = opened.view.getSelected();
			jump.actionPerformed(new ActionEvent(table, 0, "jump"));
			assertEquals(selectedView, opened.view.getSelected());

			registration.hideAttributeTablePanel();
			assertFalse(registration.getSplitPaneVisible());
			registration.showAttributeTablePanel();
			assertTrue(registration.getSplitPaneVisible());
		} finally {
			registration.deRegister();
		}
		assertFalse(registration.getSplitPaneVisible());
		assertTrue(opened.frame.getProperty("attribute_table_properties").contains("attribute_table_properties"));
	}

	private static AttributeTableModel model(NodeAttributeTableRegistration registration) throws Exception {
		return (AttributeTableModel) field(registration, "mAttributeTableModel");
	}

	private static JTable table(NodeAttributeTableRegistration registration) throws Exception {
		return (JTable) field(registration, "mAttributeTable");
	}

	private static Object field(Object target, String name) throws Exception {
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		return field.get(target);
	}

	private static Action jump(NodeAttributeTableRegistration registration) throws Exception {
		Class<?> type = Class.forName("accessories.plugins.NodeAttributeTableRegistration$JumpToMapAction");
		Constructor<?> constructor = type.getDeclaredConstructor(NodeAttributeTableRegistration.class);
		constructor.setAccessible(true);
		return (Action) constructor.newInstance(registration);
	}
}
