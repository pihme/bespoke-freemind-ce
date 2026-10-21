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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Timer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import accessories.plugins.time.TimeList;
import accessories.plugins.time.TimeManagement;
import accessories.plugins.time.TimeManagementOrganizer;
import freemind.controller.actions.generated.instance.TimeWindowColumnSetting;
import freemind.controller.actions.generated.instance.TimeWindowConfigurationStorage;
import freemind.main.Resources;
import freemind.modes.MindIcon;
import freemind.modes.MindMapNode;
import freemind.modes.common.plugins.ReminderHookBase;
import freemind.modes.mindmapmode.MindMapNodeModel;
import tests.freemind.cov.MapFixture;

/** Backwards-compatible node table without showing the time-list dialog. */
class TimeListBehaviourTest {

	private MapFixture.Opened opened;
	private OpenList list;
	private Object delayListener;

	@AfterEach
	void release() throws Exception {
		cancelDelay();
		if (opened != null && list != null) {
			opened.mode.getController().getMapModuleManager().removeListener(list);
		}
		if (opened != null) {
			clearHooks(opened.root);
		}
	}

	@Test
	void startupShowsEveryNodeUntilFoldedOnesAreHidden() throws Exception {
		// backwards compatibility
		opened = MapFixture.open("parent");
		MindMapNode parent = opened.child("parent");
		MindMapNodeModel hidden = new MindMapNodeModel("hidden", opened.root.getMap());
		parent.insert(hidden, 0);
		parent.setFolded(true);
		start(true);
		JTable table = installTable();
		assertTrue(texts().contains("parent"));
		assertTrue(texts().contains("hidden"));
		assertTrue(texts().contains("ROOT"));
		int all = table.getRowCount();
		list.toggleFolded();
		assertFalse(texts().contains("hidden"));
		assertTrue(texts().contains("parent"));
		assertTrue(table.getRowCount() < all);
		String stats = Resources.getInstance().format("timelist_statistics",
				new Integer[] { Integer.valueOf(0), Integer.valueOf(table.getRowCount()),
						Integer.valueOf(model().getRowCount()), Integer.valueOf(0) });
		assertEquals(stats, label().getText());
		list.toggleFolded();
		assertTrue(texts().contains("hidden"));
	}

	@Test
	void withoutShowAllOnlyReminderNodesAppear() throws Exception {
		// backwards compatibility
		opened = MapFixture.open("plain", "reminded");
		MindMapNode reminded = opened.child("reminded");
		Properties properties = new Properties();
		properties.setProperty(ReminderHookBase.REMINDUSERAT,
				Long.toString(System.currentTimeMillis() + 120_000L));
		opened.mode.addHook(reminded, List.of(reminded), TimeManagement.REMINDER_HOOK_NAME, properties);
		try {
			start(false);
			JTable table = installTable();
			assertEquals(1, table.getRowCount());
			assertEquals("reminded", texts().get(0));
			assertNotNull(table.getValueAt(0, 0));
			assertInstanceOf(Date.class, table.getValueAt(0, 0));
		} finally {
			clearHooks(opened.root);
		}
	}

	@Test
	void holdersRenderersAndTheDelayedFilterChangeTheVisibleRows() throws Exception {
		// backwards compatibility
		opened = MapFixture.open("alpha", "beta");
		MindMapNode alpha = opened.child("alpha");
		alpha.setText("<html><b>Hello</b>  there</html>");
		alpha.addIcon(MindIcon.factory("bell"), 0);
		alpha.addIcon(MindIcon.factory("idea"), 1);
		MindMapNode beta = opened.child("beta");
		beta.setNoteText("<html>secret</html>");
		start(true);
		JTable table = installTable();
		TimeList.NodeHolder holder = new TimeList.NodeHolder(alpha);
		assertSameNode(alpha, holder.getNode());
		assertTrue(holder.toString().contains("Hello"), holder.toString());
		assertFalse(holder.toString().contains("<"));
		assertTrue(holder.compareTo(new TimeList.NodeHolder(beta)) < 0);
		TimeList.NotesHolder notes = new TimeList.NotesHolder(beta);
		assertTrue(notes.toString().contains("secret"), notes.toString());
		assertFalse(notes.toString().contains("<"));
		assertEquals("", new TimeList.NotesHolder(alpha).toString());

		assertEquals(Date.class, model().getColumnClass(0));
		assertEquals(TimeList.NodeHolder.class, model().getColumnClass(TimeList.NODE_TEXT_COLUMN));
		assertEquals("IconsHolder", model().getColumnClass(2).getSimpleName());
		assertEquals("NotesHolder", model().getColumnClass(5).getSimpleName());

		JLabel nodeLabel = render(table, rowOf("Hello"), TimeList.NODE_TEXT_COLUMN);
		assertTrue(nodeLabel.getText().contains("Hello"));
		JLabel noteLabel = render(table, rowOf("beta"), 5);
		assertTrue(noteLabel.getText().contains("secret"), noteLabel.getText());
		JLabel dateLabel = (JLabel) renderer("DateRenderer");
		dateLabel.setText("stale");
		((javax.swing.table.TableCellRenderer) dateLabel).getTableCellRendererComponent(table, null, false,
				false, 0, 0);
		assertEquals("", dateLabel.getText());
		((javax.swing.table.TableCellRenderer) dateLabel).getTableCellRendererComponent(table,
				new Date(new java.util.GregorianCalendar(2030, java.util.Calendar.JUNE, 15).getTimeInMillis()),
				false, false, 0, 0);
		assertTrue(dateLabel.getText().contains("2030"), dateLabel.getText());
		JLabel icons = (JLabel) renderer("IconsRenderer");
		((javax.swing.table.TableCellRenderer) icons).getTableCellRendererComponent(table,
				table.getValueAt(rowOf("Hello"), 2), false, false, 0, 2);
		assertNotNull(icons.getIcon());
		((javax.swing.table.TableCellRenderer) icons).getTableCellRendererComponent(table,
				table.getValueAt(rowOf("beta"), 2), false, false, 0, 2);
		assertNull(icons.getIcon());
		JLabel plainNode = (JLabel) renderer("NodeRenderer");
		((javax.swing.table.TableCellRenderer) plainNode).getTableCellRendererComponent(table, null, false,
				false, 0, 1);
		assertEquals("", plainNode.getText());
		assertFalse(table.isCellEditable(0, 0));

		layout(table);
		int helloRow = rowOf("Hello");
		int betaRow = rowOf("beta");
		java.awt.Rectangle textCell = table.getCellRect(helloRow, TimeList.NODE_TEXT_COLUMN, true);
		java.awt.Rectangle noteCell = table.getCellRect(betaRow, 5, true);
		java.awt.Rectangle iconCell = table.getCellRect(helloRow, 2, true);
		assertTrue(table.getToolTipText(mouse(table, textCell.x + 1, textCell.y + 1)).contains("Hello"));
		assertTrue(table.getToolTipText(mouse(table, noteCell.x + 1, noteCell.y + 1)).contains("secret"));
		assertNull(table.getToolTipText(mouse(table, iconCell.x + 1, iconCell.y + 1)));

		assertEquals(".*(a.b).*", TimeList.getRegularExpression("a.b"));
		assertEquals("a.*b\\.c", TimeList.getPureRegularExpression("a*b.c"));

		int rows = table.getRowCount();
		delayListener = construct("FilterTextDocumentListener");
		javax.swing.event.DocumentListener listener = (javax.swing.event.DocumentListener) delayListener;
		search().getDocument().addDocumentListener(listener);
		try {
			search().setText("zzz");
			waitUntil(() -> table.getRowCount() == 0);
			listener.changedUpdate(new WholeDocument(search().getDocument()));
			waitUntil(() -> table.getRowCount() == 0);
			search().setText("");
			waitUntil(() -> table.getRowCount() == rows);
		} finally {
			cancelDelay();
		}
	}

	@Test
	void replaceSelectionKeysAndExportStopAtTheMissingDialog() throws Exception {
		// backwards compatibility
		opened = MapFixture.open("alpha", "beta");
		MindMapNode alpha = opened.child("alpha");
		MindMapNode beta = opened.child("beta");
		start(true);
		JTable table = installTable();
		search().setText("alpha");
		replaceField().setText("omega");
		invoke("replace", new Class[] { TimeList.IReplaceInputInformation.class },
				new Object[] { construct("ReplaceAllInfo") });
		assertEquals("omega", alpha.getText());
		assertEquals("", search().getText());
		assertTrue(texts().contains("beta"));

		search().setText("beta");
		replaceField().setText("gamma");
		int betaRow = rowOf("beta");
		table.getSelectionModel().setSelectionInterval(betaRow, betaRow);
		invoke("replace", new Class[] { TimeList.IReplaceInputInformation.class },
				new Object[] { construct("ReplaceSelectedInfo") });
		assertEquals("gamma", beta.getText());
		assertEquals("omega", alpha.getText());

		AbstractAction named = new AbstractAction() {
			public void actionPerformed(ActionEvent event) {
			}
		};
		JButton button = new JButton();
		list.decorate("Do&It", named, button);
		assertEquals("DoIt", named.getValue(Action.NAME));
		assertEquals("DoIt", button.getText());
		assertEquals(KeyEvent.VK_I, button.getMnemonic());

		Object toggle = construct("ToggleViewFoldedNodesAction", new Class[] { String.class },
				new Object[] { "folded" });
		assertTrue((Boolean) invokeOn(toggle, "isSelected",
				new Class[] { javax.swing.JMenuItem.class, Action.class }, new Object[] { null, null }));

		layout(table);
		Object mouse = construct("FlatNodeTableMouseAdapter");
		int row = rowOf("omega");
		((java.awt.event.MouseListener) mouse).mouseClicked(click(table, row, 1));
		assertEquals("omega", alpha.getText());
		assertThrows(NullPointerException.class, () -> ((java.awt.event.MouseListener) mouse)
				.mouseClicked(click(table, row, 2)));

		Object keys = construct("FlatNodeTableKeyListener");
		((java.awt.event.KeyListener) keys).keyPressed(key(table, KeyEvent.KEY_PRESSED, KeyEvent.VK_A));
		((java.awt.event.KeyListener) keys).keyTyped(key(table, KeyEvent.KEY_TYPED, KeyEvent.VK_A));
		assertThrows(NullPointerException.class, () -> ((java.awt.event.KeyListener) keys)
				.keyReleased(key(table, KeyEvent.KEY_RELEASED, KeyEvent.VK_ESCAPE)));
		table.getSelectionModel().setSelectionInterval(row, row);
		assertThrows(NullPointerException.class, () -> ((java.awt.event.KeyListener) keys)
				.keyReleased(key(table, KeyEvent.KEY_RELEASED, KeyEvent.VK_ENTER)));

		table.addKeyListener((java.awt.event.KeyListener) keys);
		Method process = table.getClass().getDeclaredMethod("processKeyEvent", KeyEvent.class);
		process.setAccessible(true);
		process.invoke(table, key(table, KeyEvent.KEY_PRESSED, KeyEvent.VK_ENTER));
		assertEquals("omega", alpha.getText());
		InvocationTargetException released = assertThrows(InvocationTargetException.class,
				() -> process.invoke(table, key(table, KeyEvent.KEY_RELEASED, KeyEvent.VK_ENTER)));
		assertInstanceOf(NullPointerException.class, released.getCause());

		TimeWindowConfigurationStorage storage = new TimeWindowConfigurationStorage();
		storage.setViewFoldedNodes(Boolean.FALSE);
		for (int i = 0; i < table.getColumnCount(); i++) {
			TimeWindowColumnSetting setting = new TimeWindowColumnSetting();
			setting.setColumnWidth(20 + i);
			setting.setColumnSorting(Integer.valueOf(i == 0 ? accessories.plugins.time.TableSorter.ASCENDING
					: accessories.plugins.time.TableSorter.NOT_SORTED));
			storage.addTimeWindowColumnSetting(setting);
		}
		list.configure(storage);
		assertEquals(20, table.getColumnModel().getColumn(0).getPreferredWidth());
		assertEquals(accessories.plugins.time.TableSorter.ASCENDING, sorterStatus(0));
		list.configure(storage);
		assertEquals(20, table.getColumnModel().getColumn(0).getPreferredWidth());
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setWidth(20 + i);
		}
		TimeWindowConfigurationStorage read = list.configuration();
		assertFalse(read.getViewFoldedNodes());
		assertEquals(20, read.getListTimeWindowColumnSettingList().get(0).getColumnWidth());
		assertEquals(Integer.valueOf(accessories.plugins.time.TableSorter.ASCENDING),
				read.getListTimeWindowColumnSettingList().get(0).getColumnSorting());

		String omega = alpha.getText();
		assertThrows(NullPointerException.class, list::exportRows);
		assertEquals(omega, alpha.getText());
		assertTrue(list.isMapModuleChangeAllowed(null, null, null, null));
		list.beforeMapModuleChange(null, null, null, null);
		assertEquals(omega, alpha.getText());
		assertThrows(NullPointerException.class, () -> list.afterMapClose(null, null));
		assertThrows(NullPointerException.class, () -> list.afterMapModuleChange(null, null, null, null));
		list.numberOfOpenMapInformation(1, 0);
		assertEquals(omega, alpha.getText());
	}

	private void start(boolean showAll) {
		list = new OpenList();
		list.setController(opened.mode);
		Properties properties = new Properties();
		properties.setProperty("show_all_nodes", Boolean.toString(showAll));
		list.setProperties(properties);
		assertThrows(HeadlessException.class, list::startupMapHook);
		assertEquals(opened.mode, list.getMindMapController());
	}

	private JTable installTable() throws Exception {
		JTable table = (JTable) construct("FlatNodeTable");
		set("mTimeTable", table);
		set("mFilterTextSearchField", new JTextField());
		set("mFilterTextReplaceField", new JTextField());
		set("mStatisticsLabel", new JLabel());
		set("mDateRenderer", construct("DateRenderer"));
		set("mNodeRenderer", construct("NodeRenderer"));
		set("mNotesRenderer", construct("NotesRenderer"));
		set("mIconsRenderer", construct("IconsRenderer", new Class[] { freemind.modes.ModeController.class },
				new Object[] { opened.mode }));
		invoke("updateModel", new Class[0], new Object[0]);
		return table;
	}

	private Object construct(String name) throws Exception {
		Class<?> type = Class.forName("accessories.plugins.time.TimeList$" + name);
		if (java.lang.reflect.Modifier.isStatic(type.getModifiers())) {
			Constructor<?> constructor = type.getDeclaredConstructor();
			constructor.setAccessible(true);
			return constructor.newInstance();
		}
		return construct(name, new Class[0], new Object[0]);
	}

	private Object construct(String name, Class<?>[] types, Object[] args) throws Exception {
		Class<?> type = Class.forName("accessories.plugins.time.TimeList$" + name);
		if (java.lang.reflect.Modifier.isStatic(type.getModifiers())) {
			Constructor<?> constructor = type.getDeclaredConstructor(types);
			constructor.setAccessible(true);
			return constructor.newInstance(args);
		}
		Constructor<?> constructor = type.getDeclaredConstructor(prepend(types));
		constructor.setAccessible(true);
		return constructor.newInstance(prepend(args));
	}

	private static Class<?>[] prepend(Class<?>[] types) {
		Class<?>[] all = new Class<?>[types.length + 1];
		all[0] = TimeList.class;
		System.arraycopy(types, 0, all, 1, types.length);
		return all;
	}

	private Object[] prepend(Object[] args) {
		Object[] all = new Object[args.length + 1];
		all[0] = list;
		System.arraycopy(args, 0, all, 1, args.length);
		return all;
	}

	private Object invoke(String name, Class<?>[] types, Object[] args) throws Exception {
		Method method = TimeList.class.getDeclaredMethod(name, types);
		method.setAccessible(true);
		return method.invoke(list, args);
	}

	private static Object invokeOn(Object target, String name, Class<?>[] types, Object[] args) throws Exception {
		Method method = target.getClass().getDeclaredMethod(name, types);
		method.setAccessible(true);
		return method.invoke(target, args);
	}

	private void set(String name, Object value) throws Exception {
		Field field = TimeList.class.getDeclaredField(name);
		field.setAccessible(true);
		field.set(list, value);
	}

	private Object get(String name) throws Exception {
		Field field = TimeList.class.getDeclaredField(name);
		field.setAccessible(true);
		return field.get(list);
	}

	private javax.swing.table.DefaultTableModel model() throws Exception {
		return (javax.swing.table.DefaultTableModel) get("mTimeTableModel");
	}

	private JLabel label() throws Exception {
		return (JLabel) get("mStatisticsLabel");
	}

	private JTextField search() throws Exception {
		return (JTextField) get("mFilterTextSearchField");
	}

	private JTextField replaceField() throws Exception {
		return (JTextField) get("mFilterTextReplaceField");
	}

	private Object renderer(String name) throws Exception {
		String field = "m" + name.substring(0, 1).toLowerCase() + name.substring(1);
		if ("DateRenderer".equals(name)) {
			field = "mDateRenderer";
		} else if ("NodeRenderer".equals(name)) {
			field = "mNodeRenderer";
		} else if ("NotesRenderer".equals(name)) {
			field = "mNotesRenderer";
		} else if ("IconsRenderer".equals(name)) {
			field = "mIconsRenderer";
		}
		return get(field);
	}

	private int sorterStatus(int column) throws Exception {
		Object sorter = get("mSorter");
		Method method = sorter.getClass().getMethod("getSortingStatus", int.class);
		return ((Integer) method.invoke(sorter, Integer.valueOf(column))).intValue();
	}

	private java.util.List<String> texts() throws Exception {
		java.util.List<String> texts = new java.util.ArrayList<String>();
		javax.swing.table.DefaultTableModel tableModel = model();
		for (int i = 0; i < tableModel.getRowCount(); i++) {
			texts.add(((TimeList.NodeHolder) tableModel.getValueAt(i, TimeList.NODE_TEXT_COLUMN)).toString());
		}
		return texts;
	}

	private int rowOf(String text) throws Exception {
		JTable table = (JTable) get("mTimeTable");
		for (int row = 0; row < table.getRowCount(); row++) {
			Object value = table.getValueAt(row, TimeList.NODE_TEXT_COLUMN);
			if (value != null && value.toString().contains(text)) {
				return row;
			}
		}
		fail("missing row " + text + " in " + texts());
		return -1;
	}

	private JLabel render(JTable table, int row, int column) {
		Object value = table.getValueAt(row, column);
		javax.swing.table.TableCellRenderer renderer = table.getCellRenderer(row, column);
		return (JLabel) renderer.getTableCellRendererComponent(table, value, false, false, row, column);
	}

	private static void layout(JTable table) {
		table.setRowHeight(20);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setPreferredWidth(50);
			table.getColumnModel().getColumn(i).setWidth(50);
		}
		table.setSize(400, 200);
		table.doLayout();
	}

	private static MouseEvent mouse(JTable table, int x, int y) {
		return new MouseEvent(table, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, x, y, 0, false);
	}

	private static MouseEvent click(JTable table, int row, int clicks) {
		int y = row * table.getRowHeight() + 2;
		return new MouseEvent(table, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 60, y, clicks,
				false);
	}

	private static KeyEvent key(JTable table, int id, int code) {
		if (id == KeyEvent.KEY_TYPED) {
			return new KeyEvent(table, id, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, (char) code);
		}
		return new KeyEvent(table, id, System.currentTimeMillis(), 0, code, KeyEvent.CHAR_UNDEFINED);
	}

	private void waitUntil(java.util.function.BooleanSupplier condition) throws Exception {
		long end = System.currentTimeMillis() + 3000L;
		while (System.currentTimeMillis() < end) {
			if (condition.getAsBoolean()) {
				return;
			}
			Thread.sleep(50L);
		}
		fail("timed out waiting for the time-list filter");
	}

	private void cancelDelay() throws Exception {
		if (delayListener == null) {
			return;
		}
		Field field = delayListener.getClass().getDeclaredField("mTypeDelayTimer");
		field.setAccessible(true);
		Timer timer = (Timer) field.get(delayListener);
		if (timer != null) {
			timer.cancel();
		}
		delayListener = null;
	}

	private static void clearHooks(MindMapNode node) {
		ReminderHookBase hook = TimeManagementOrganizer.getHook(node);
		if (hook != null) {
			node.removeHook(hook);
		}
		for (int i = 0; i < node.getChildCount(); i++) {
			clearHooks((MindMapNode) node.getChildAt(i));
		}
	}

	private static void assertSameNode(MindMapNode left, MindMapNode right) {
		assertTrue(left == right);
	}

	static final class OpenList extends TimeList {
		void toggleFolded() {
			toggleViewFoldedNodes();
		}

		void decorate(String key, AbstractAction action, JButton button) {
			decorateButtonAndAction(key, action, button);
		}

		void configure(TimeWindowConfigurationStorage storage) {
			setTableConfiguration(storage);
		}

		TimeWindowConfigurationStorage configuration() {
			return getTableConfiguration();
		}

		void exportRows() {
			exportSelectedRowsAndClose();
		}
	}

	private static final class WholeDocument implements DocumentEvent {
		private final Document document;

		WholeDocument(Document document) {
			this.document = document;
		}

		public int getOffset() {
			return 0;
		}

		public int getLength() {
			return document.getLength();
		}

		public Document getDocument() {
			return document;
		}

		public EventType getType() {
			return EventType.CHANGE;
		}

		public ElementChange getChange(Element element) {
			return null;
		}
	}
}
