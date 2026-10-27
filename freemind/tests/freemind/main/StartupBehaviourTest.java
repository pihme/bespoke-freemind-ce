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

package freemind.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.print.PageFormat;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import accessories.plugins.dialogs.ChooseFormatPopupDialog;
import accessories.plugins.dialogs.ManagePatternsPopupDialog;
import accessories.plugins.time.CalendarMarkingDialog;
import accessories.plugins.time.TimeList;
import accessories.plugins.time.TimeManagement;
import freemind.common.TextTranslator;
import freemind.controller.Controller;
import freemind.controller.MenuBar;
import freemind.controller.actions.generated.instance.Pattern;
import freemind.controller.color.ColorPair;
import freemind.controller.color.JColorCombo;
import freemind.controller.printpreview.PreviewDialog;
import freemind.main.Tools;
import freemind.modes.MindIcon;
import freemind.modes.MindMapNode;
import freemind.modes.ModeController;
import freemind.modes.StylePatternFactory;
import freemind.modes.browsemode.BrowseController;
import freemind.modes.common.dialogs.EnterPasswordDialog;
import freemind.modes.common.dialogs.IconSelectionPopupDialog;
import freemind.modes.filemode.FileController;
import freemind.modes.mindmapmode.MindMapController;
import freemind.modes.mindmapmode.dialogs.StylePatternFrame;
import freemind.modes.mindmapmode.dialogs.StylePatternFrame.StylePatternFrameType;
import freemind.preferences.layout.OptionPanel;
import freemind.view.mindmapview.EditNodeBase;
import freemind.view.mindmapview.EditNodeTextField;
import freemind.view.mindmapview.MapView;
import freemind.view.mindmapview.NodeView;
import plugins.script.ScriptEditorPanel;
import plugins.script.ScriptEditorPanel.ScriptHolder;
import plugins.script.ScriptEditorPanel.ScriptModel;
import plugins.script.ScriptingEngine.ErrorHandler;
import plugins.svg.ExportPdfDialog;
import freemind.controller.actions.generated.instance.ScriptEditorWindowConfigurationStorage;

/**
 * Starts the real frame once. The methods check the mode, the options panel,
 * a style-pattern dialog, file mode, and browse mode.
 */
@Tag("gui")
@DisplayName("Startup builds the menus, options, modes, and dialogs")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StartupBehaviourTest {

	private static FreeMind frame;
	private static File autoFile;

	@BeforeAll
	static void startFrame() throws Exception {
		Properties defaults = new FreeMindStarter().readDefaultPreferences();
		Properties user = new Properties();
		user.putAll(defaults);
		autoFile = File.createTempFile("freemind-startup", ".properties");
		frame = new FreeMind(defaults, user, autoFile);
		frame.init(new FeedBack() {
			public void progress(int act, String messageId, Object[] parameters) {
			}

			public int getActualValue() {
				return 0;
			}

			public void setMaximumValue(int max) {
			}

			public void increase(String messageId, Object[] parameters) {
			}
		});
	}

	@AfterAll
	static void closeFrame() {
		if (frame != null) {
			frame.dispose();
		}
		if (autoFile != null) {
			autoFile.delete();
		}
		new HeadlessFreeMind();
	}

	@Test
	@Order(1)
	void TestREQ_APP_01_StartupSelectsMindMapAndBuildsTheFileMenu() {
		assertEquals("MindMap", frame.getController().getMode().toString());
		MenuBar menu = frame.getFreeMindMenuBar();
		assertNotNull(menu);
		String fileMenu = Tools.removeMnemonic(frame.getResourceString("file"));
		StringBuilder texts = new StringBuilder();
		boolean found = false;
		for (int i = 0; i < menu.getMenuCount(); i++) {
			JMenu item = menu.getMenu(i);
			String text = item == null ? "null" : item.getText();
			texts.append('[').append(text).append(']');
			if (fileMenu.equals(text)) {
				found = true;
			}
		}
		assertTrue(found, "wanted " + fileMenu + " in " + texts);
		assertTrue(menu.getMenuCount() >= 9, texts.toString());
		assertInstanceOf(MindMapController.class,
				frame.getController().getMode().getDefaultModeController());
	}

	@Test
	@Order(2)
	void TestREQ_APP_01_OptionPanelShowsEnvironmentTab() {
		JDialog dialog = new JDialog(frame, "options");
		OptionPanel panel = new OptionPanel(frame, dialog, props -> {
		});
		panel.buildPanel();

		String environment = frame.getResourceString("OptionPanel.Environment");
		assertTrue(containsButton(dialog.getContentPane(), environment), environment);
		assertTrue(containsSplit(dialog.getContentPane()));
		dialog.dispose();
	}

	@Test
	@Order(3)
	void TestREQ_STYLE_04_PatternFrameBuildsAPattern() {
		ModeController mode = frame.getController().getMode().getDefaultModeController();
		MindMapController mindMap = assertInstanceOf(MindMapController.class, mode);
		TextTranslator translator = mindMap::getText;
		StylePatternFrame patternFrame = new StylePatternFrame(translator, mindMap,
				StylePatternFrameType.WITH_NAME_AND_CHILDS);
		patternFrame.init();
		assertTrue(patternFrame.getComponentCount() > 0);
		assertTrue(countButtons(patternFrame) >= 5);
	}

	@Test
	@Order(4)
	void TestREQ_MODE_01_FileModeRootNamesTheFilesystemRoot() {
		assertTrue(frame.getController().createNewMode("File"));
		assertEquals("File", frame.getController().getMode().toString());
		FileController fileMode = assertInstanceOf(FileController.class,
				frame.getController().getMode().getDefaultModeController());
		MindMapNode root = fileMode.newModel(fileMode).getRootNode();
		assertEquals("Root", root.getText());
		assertTrue(root.hasChildren());
	}

	@Test
	@Order(5)
	void TestREQ_MODE_01_BrowseModeStarts() {
		assertTrue(frame.getController().createNewMode("Browse"));
		assertEquals("Browse", frame.getController().getMode().toString());
		assertInstanceOf(BrowseController.class,
				frame.getController().getMode().getDefaultModeController());
	}

	@Test
	@Order(6)
	void colorComboSelectsRedByName() {
		JColorCombo combo = new JColorCombo();
		assertTrue(combo.getItemCount() > 10);
		ColorPair red = null;
		for (int i = 0; i < combo.getItemCount(); i++) {
			ColorPair pair = combo.getItemAt(i);
			if ("red".equals(pair.name)) {
				red = pair;
			}
		}
		assertNotNull(red);
		combo.setSelectedItem(red);
		assertEquals(Color.RED, ((ColorPair) combo.getSelectedItem()).color);
		Component rendered = combo.getRenderer().getListCellRendererComponent(
				new JList<>(), red, 0, false, false);
		JLabel label = assertInstanceOf(JLabel.class, rendered);
		assertEquals(red.displayName, label.getText());
		assertTrue(label.getText() != null && !label.getText().isBlank());
	}

	@Test
	@Order(7)
	void TestREQ_FILE_02_PdfDialogKeepsPaperAndOrientation() {
		Controller controller = frame.getController();
		ExportPdfDialog dialog = new ExportPdfDialog(frame,
				new String[] { "A4", "A3", "LETTER" }, controller);
		assertEquals(controller.getResourceString("ExportPdfDialog.PDF_Export_Settings"),
				dialog.getTitle());
		JComboBox<?> papers = findComboContaining(dialog.getContentPane(), "A4");
		papers.setSelectedItem("A4");
		String landscape = controller.getResourceString("ExportPdfDialog.Landscape");
		assertTrue(selectedRadio(dialog.getContentPane(), landscape), landscape);
		clickButton(dialog.getContentPane(), controller.getResourceString("ExportPdfDialog.OK"));
		assertEquals("A4", dialog.getFormat());
		assertEquals(PageFormat.LANDSCAPE, dialog.getOrientation());
		assertEquals("A4", controller.getProperty("pdf_page_format"));
		assertEquals("landscape", controller.getProperty("pdf_page_orientation"));
		assertTrue(dialog.getResult());
		dialog.dispose();
	}

	@Test
	@Order(8)
	void TestREQ_APP_02_PrintPreviewShowsTheFirstPage() {
		MindMapController mindMap = mindMap();
		MapView view = mindMap.getController().getView();
		assertNotNull(view);
		PreviewDialog dialog = new PreviewDialog("Print preview", view, new PageFormat());
		assertTrue(containsLabel(dialog.getContentPane(), "- 1 -"), "page label");
		clickButton(dialog.getContentPane(), "OK");
		assertTrue(!dialog.isDisplayable());
	}

	@Test
	@Order(9)
	void TestREQ_STYLE_04_PatternManagerListsShippedPatterns() {
		MindMapController mindMap = mindMap();
		ManagePatternsPopupDialog dialog = new ManagePatternsPopupDialog(frame, mindMap);
		JList<?> patterns = findList(dialog.getContentPane());
		assertTrue(patterns.getModel().getSize() >= 20,
				"pattern count " + patterns.getModel().getSize());
		boolean sawDefault = false;
		StringBuilder names = new StringBuilder();
		for (int i = 0; i < patterns.getModel().getSize(); i++) {
			Object item = patterns.getModel().getElementAt(i);
			Pattern pattern = assertInstanceOf(Pattern.class, item);
			if (names.length() > 0) {
				names.append(", ");
			}
			names.append(pattern.getName());
			if ("Default".equals(pattern.getName()) || "Default".equals(pattern.getOriginalName())) {
				sawDefault = true;
			}
		}
		assertTrue(sawDefault, names.toString());
		dialog.dispose();
	}

	@Test
	@Order(10)
	void TestREQ_PLUG_01_ScriptEditorShowsTheScriptText() {
		String body = "return 1;";
		ScriptEditorPanel panel = new ScriptEditorPanel(new OneScript(body), frame, true);
		assertEquals(frame.getResourceString("plugins/ScriptEditor/window.title"), panel.getTitle());
		JList<?> scripts = findList(panel.getContentPane());
		assertEquals(1, scripts.getModel().getSize());
		assertEquals("script0", scripts.getModel().getElementAt(0));
		scripts.setSelectedIndex(0);
		JTextArea source = findTextArea(panel.getContentPane());
		assertEquals(body, source.getText());
		panel.dispose();
	}

	@Test
	@Order(12)
	void TestREQ_STYLE_05_IconDialogShowsTheIcon() {
		Vector<freemind.modes.IconInformation> icons = new Vector<>();
		icons.add(MindIcon.factory("button_ok"));
		IconSelectionPopupDialog dialog = new IconSelectionPopupDialog(frame, icons, frame);
		String iconTitle = frame.getResourceString("select_icon");
		assertTrue(iconTitle.equals(dialog.getTitle())
				|| Tools.removeMnemonic(iconTitle).equals(dialog.getTitle()), dialog.getTitle());
		assertTrue(countLabels(dialog.getContentPane()) >= 1);
		dialog.dispose();
	}

	@Test
	@Order(14)
	void calendarMarkingDialogOffersNever() {
		MindMapController mindMap = mindMap();
		CalendarMarkingDialog dialog = new CalendarMarkingDialog(mindMap);
		assertEquals(mindMap.getText("CalendarMarkingDialog.title"), dialog.getTitle());
		String never = mindMap.getText("never");
		JComboBox<?> repetition = findComboContaining(dialog.getContentPane(), never);
		assertTrue(repetition.getItemCount() >= 11, "items " + repetition.getItemCount());
		assertEquals(never, repetition.getItemAt(0));
		dialog.dispose();
	}

	@Test
	@Order(15)
	void timeListShowsEveryNodeAndCloses() {
		MindMapController mindMap = mindMap();
		Properties properties = new Properties();
		properties.setProperty("show_all_nodes", "true");
		TimeList list = new TimeList();
		list.setController(mindMap);
		list.setProperties(properties);
		list.startupMapHook();

		String title = mindMap.getText("plugins/TimeManagement.xml_WindowTitle_All_Nodes");
		JDialog dialog = ownedDialog(title);
		assertEquals(title, dialog.getTitle());
		assertTrue(title.contains("Search"), title);
		assertTrue(containsLabel(dialog.getContentPane(),
				mindMap.getText("plugins/TimeManagement.xml_Find")));
		assertTrue(containsLabel(dialog.getContentPane(),
				mindMap.getText("plugins/TimeManagement.xml_Replace")));
		JTable table = findTable(dialog.getContentPane());
		String rootText = mindMap.getRootNode().getText();
		assertTrue(tableContains(table, rootText),
				"rows " + table.getRowCount() + " root " + rootText);

		dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
		assertFalse(dialog.isVisible());
	}

	@Test
	@Order(16)
	void timeManagementAppendsTheDateAndCloses() {
		MindMapController mindMap = mindMap();
		MindMapNode root = mindMap.getRootNode();
		mindMap.select(root, java.util.List.of(root));
		String before = root.getText();
		TimeManagement management = new TimeManagement();
		management.setController(mindMap);
		management.setProperties(new Properties());
		management.startupMapHook();

		String title = mindMap.getText("plugins/TimeManagement.xml_WindowTitle");
		JDialog dialog = ownedDialog(title);
		assertEquals(title, dialog.getTitle());
		assertTrue(title.contains("Time Management"), title);
		assertTrue(containsFieldText(dialog.getContentPane(),
				Integer.toString(java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY))));
		clickButton(dialog.getJMenuBar(), mindMap.getText("plugins/TimeManagement.xml_appendButton"));
		assertTrue(root.getText().startsWith(before), root.getText());
		assertTrue(root.getText().length() > before.length(), root.getText());
		assertTrue(root.getText().matches(".*\\d.*"), root.getText());

		dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
		assertFalse(dialog.isVisible());
	}

	@Test
	@Order(17)
	void passwordDialogKeepsALongEnoughPassword() {
		MindMapController mindMap = mindMap();
		EnterPasswordDialog dialog = new EnterPasswordDialog(frame, mindMap, false);
		String title = mindMap.getText("accessories/plugins/EncryptNode.properties_8").trim();
		assertEquals(title, dialog.getTitle().trim());
		assertTrue(title.contains("Decrypt"), title);
		findPassword(dialog.getContentPane()).setText("secret");
		clickButton(dialog.getContentPane(),
				mindMap.getText("accessories/plugins/EncryptNode.properties_6").trim());
		assertEquals(EnterPasswordDialog.OK, dialog.getResult());
		assertEquals("secret", dialog.getPassword().toString());
	}

	@Test
	@Order(18)
	void formatDialogShowsTheShippedTitleAndCancels() {
		MindMapController mindMap = mindMap();
		Pattern pattern = StylePatternFactory.createPatternFromNode(mindMap.getRootNode());
		ChooseFormatPopupDialog dialog = new ChooseFormatPopupDialog(frame, mindMap,
				"accessories/plugins/ApplyFormatPlugin.dialog.title", pattern,
				mindMap.getRootNode());
		String title = mindMap.getText("accessories/plugins/ApplyFormatPlugin.dialog.title").trim();
		assertEquals(title, dialog.getTitle().trim());
		assertEquals("Change format of nodes", title);
		assertTrue(containsButton(dialog.getContentPane(), Tools.removeMnemonic(mindMap.getText("ok"))));
		assertTrue(containsButton(dialog.getContentPane(), Tools.removeMnemonic(mindMap.getText("cancel"))));
		assertEquals(ChooseFormatPopupDialog.CANCEL, dialog.getResult());
		dialog.dispose();
		assertFalse(dialog.isDisplayable());
	}

	@Test
	@Order(19)
	void inlineEditorCommitsTheFieldTextOnEnter() throws Exception {
		MindMapController mindMap = mindMap();
		MindMapNode root = mindMap.getRootNode();
		NodeView nodeView = mindMap.getView().getNodeView(root);
		Field spelling = EditNodeBase.class.getDeclaredField("checkSpelling");
		spelling.setAccessible(true);
		boolean previous = spelling.getBoolean(null);
		spelling.setBoolean(null, false);
		try {
			EditNodeTextField editor = new EditNodeTextField(nodeView, root.getText(), null, mindMap,
					new EditNodeBase.EditControl() {
						public void cancel() {
						}

						public void ok(String newText) {
							mindMap.setNodeText(root, newText);
						}

						public void split(String newText, int position) {
						}
					});
			editor.show();
			JTextField field = findEditorField(mindMap.getView());
			field.setText("Renamed root");
			KeyEvent enter = new KeyEvent(field, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
					0, KeyEvent.VK_ENTER, '\n');
			for (java.awt.event.KeyListener listener : field.getKeyListeners()) {
				listener.keyPressed(enter);
			}
		} finally {
			spelling.setBoolean(null, previous);
		}
		assertEquals("Renamed root", root.getText());
	}

	private static MindMapController mindMap() {
		assertTrue(frame.getController().createNewMode("MindMap"));
		if (frame.getController().getView() == null) {
			frame.getController().getMode().getDefaultModeController().newMap();
		}
		assertNotNull(frame.getController().getView());
		return assertInstanceOf(MindMapController.class,
				frame.getController().getModeController());
	}

	private static final class OneScript implements ScriptModel {
		private final String body;

		OneScript(String body) {
			this.body = body;
		}

		public int getAmountOfScripts() {
			return 1;
		}

		public ScriptHolder getScript(int index) {
			return new ScriptHolder("script0", body);
		}

		public void setScript(int index, ScriptHolder script) {
		}

		public boolean executeScript(int index, PrintStream outStream, ErrorHandler errorHandler) {
			return false;
		}

		public void storeDialogPositions(ScriptEditorPanel panel,
				ScriptEditorWindowConfigurationStorage storage, String key) {
		}

		public ScriptEditorWindowConfigurationStorage decorateDialog(ScriptEditorPanel panel, String key) {
			return null;
		}

		public void endDialog(boolean canceled) {
		}

		public boolean isDirty() {
			return false;
		}

		public int addNewScript() {
			return 0;
		}
	}

	private static int countButtons(Container container) {
		int count = 0;
		for (Component component : container.getComponents()) {
			if (component instanceof JButton) {
				count++;
			}
			if (component instanceof Container) {
				count += countButtons((Container) component);
			}
		}
		return count;
	}

	private static boolean containsButton(Container container, String text) {
		for (Component component : container.getComponents()) {
			if (component instanceof JButton && text.equals(((JButton) component).getText())) {
				return true;
			}
			if (component instanceof Container && containsButton((Container) component, text)) {
				return true;
			}
		}
		return false;
	}

	private static boolean containsSplit(Container container) {
		for (Component component : container.getComponents()) {
			if (component instanceof JSplitPane) {
				return true;
			}
			if (component instanceof Container && containsSplit((Container) component)) {
				return true;
			}
		}
		return false;
	}

	private static JComboBox<?> findComboContaining(Container container, Object item) {
		JComboBox<?> found = findCombo(container, item);
		if (found == null) {
			throw new AssertionError("no combo containing " + item);
		}
		return found;
	}

	private static JComboBox<?> findCombo(Container container, Object item) {
		for (Component component : container.getComponents()) {
			if (component instanceof JComboBox<?> combo) {
				for (int i = 0; i < combo.getItemCount(); i++) {
					if (item.equals(combo.getItemAt(i))) {
						return combo;
					}
				}
			}
			if (component instanceof Container nested) {
				JComboBox<?> found = findCombo(nested, item);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}

	private static JList<?> findList(Container container) {
		for (Component component : container.getComponents()) {
			if (component instanceof JList<?> list) {
				return list;
			}
			if (component instanceof Container) {
				JList<?> nested = findList((Container) component);
				if (nested != null) {
					return nested;
				}
			}
		}
		throw new AssertionError("no list");
	}

	private static JTextArea findTextArea(Container container) {
		for (Component component : container.getComponents()) {
			if (component instanceof JTextArea area) {
				return area;
			}
			if (component instanceof Container) {
				try {
					return findTextArea((Container) component);
				} catch (AssertionError ignored) {
				}
			}
		}
		throw new AssertionError("no text area");
	}

	private static boolean selectedRadio(Container container, String text) {
		for (Component component : container.getComponents()) {
			if (component instanceof JRadioButton radio && text.equals(radio.getText()) && radio.isSelected()) {
				return true;
			}
			if (component instanceof Container && selectedRadio((Container) component, text)) {
				return true;
			}
		}
		return false;
	}

	private static boolean containsLabel(Container container, String text) {
		for (Component component : container.getComponents()) {
			if (component instanceof JLabel label && text.equals(label.getText())) {
				return true;
			}
			if (component instanceof Container && containsLabel((Container) component, text)) {
				return true;
			}
		}
		return false;
	}

	private static int countLabels(Container container) {
		int count = 0;
		for (Component component : container.getComponents()) {
			if (component instanceof JLabel) {
				count++;
			}
			if (component instanceof Container) {
				count += countLabels((Container) component);
			}
		}
		return count;
	}

	private static void clickButton(Container container, String text) {
		if (!clickButtonOnce(container, text, Tools.removeMnemonic(text))) {
			throw new AssertionError("no button " + text);
		}
	}

	private static JDialog ownedDialog(String title) {
		StringBuilder seen = new StringBuilder();
		for (Window window : frame.getOwnedWindows()) {
			if (window instanceof JDialog dialog) {
				seen.append('[').append(dialog.getTitle()).append(']');
				if (title.equals(dialog.getTitle())) {
					return dialog;
				}
			}
		}
		throw new AssertionError("no dialog " + title + " among " + seen);
	}

	private static JTable findTable(Container container) {
		for (Component component : container.getComponents()) {
			if (component instanceof JTable table) {
				return table;
			}
			if (component instanceof Container nested) {
				try {
					return findTable(nested);
				} catch (AssertionError ignored) {
				}
			}
		}
		throw new AssertionError("no table");
	}

	private static boolean tableContains(JTable table, String text) {
		for (int row = 0; row < table.getRowCount(); row++) {
			Object value = table.getValueAt(row, TimeList.NODE_TEXT_COLUMN);
			if (value != null && text.equals(value.toString())) {
				return true;
			}
		}
		return false;
	}

	private static boolean containsFieldText(Container container, String text) {
		for (Component component : container.getComponents()) {
			if (component instanceof JTextField field && text.equals(field.getText())) {
				return true;
			}
			if (component instanceof Container nested && containsFieldText(nested, text)) {
				return true;
			}
		}
		return false;
	}

	private static JTextField findEditorField(Container container) {
		for (Component component : container.getComponents()) {
			if (component instanceof JTextField field && field.getKeyListeners().length > 0) {
				return field;
			}
			if (component instanceof Container nested) {
				try {
					return findEditorField(nested);
				} catch (AssertionError ignored) {
				}
			}
		}
		throw new AssertionError("no editor field");
	}

	private static JPasswordField findPassword(Container container) {
		for (Component component : container.getComponents()) {
			if (component instanceof JPasswordField field) {
				return field;
			}
			if (component instanceof Container nested) {
				try {
					return findPassword(nested);
				} catch (AssertionError ignored) {
				}
			}
		}
		throw new AssertionError("no password field");
	}

	private static boolean clickButtonOnce(Container container, String text, String shown) {
		for (Component component : container.getComponents()) {
			if (component instanceof JMenu menu) {
				for (int i = 0; i < menu.getItemCount(); i++) {
					javax.swing.JMenuItem item = menu.getItem(i);
					if (item == null) {
						continue;
					}
					if (text.equals(item.getText()) || shown.equals(item.getText())) {
						item.doClick();
						return true;
					}
					if (item instanceof JMenu && clickButtonOnce((JMenu) item, text, shown)) {
						return true;
					}
				}
				continue;
			}
			if (component instanceof javax.swing.AbstractButton button
					&& (text.equals(button.getText()) || shown.equals(button.getText()))) {
				button.doClick();
				return true;
			}
			if (component instanceof Container && clickButtonOnce((Container) component, text, shown)) {
				return true;
			}
		}
		return false;
	}
}
