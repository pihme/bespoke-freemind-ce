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

package freemind.modes.mindmapmode.commands;

import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import freemind.extensions.HookInstanciationMethod;
import freemind.extensions.MindMapHook;
import freemind.main.Resources;

/**
 * Menu commands that used to be one-shot hooks.
 * The label is the old hook name. A .mm file does not store it.
 */
public final class MenuCommands {

	private MenuCommands() {
	}

	public static final class Command {
		public final String label;
		public final String pluginLabel;
		public final Supplier<MindMapHook> create;
		public final Class<?> type;
		public final boolean modeController;
		public final String name;
		public final String documentation;
		public final String keyStroke;
		public final String iconPath;
		public final HookInstanciationMethod instantiation;
		public final boolean selectable;
		public final List<String> menus;
		public final Properties properties;

		Command(String label, String pluginLabel, Supplier<MindMapHook> create, Class<?> type,
				boolean modeController, String name, String documentation, String keyStroke, String iconPath,
				HookInstanciationMethod instantiation, boolean selectable, List<String> menus,
				Properties properties) {
			this.label = label;
			this.pluginLabel = pluginLabel;
			this.create = create;
			this.type = type;
			this.modeController = modeController;
			this.name = name;
			this.documentation = documentation;
			this.keyStroke = keyStroke;
			this.iconPath = iconPath;
			this.instantiation = instantiation;
			this.selectable = selectable;
			this.menus = menus;
			this.properties = properties;
		}
	}

	private static Properties props(String... pairs) {
		Properties properties = new Properties();
		for (int i = 0; i < pairs.length; i += 2) {
			properties.setProperty(pairs[i], pairs[i + 1]);
		}
		return properties;
	}

	private static final List<Command> COMMANDS = List.of(
			new Command(
					"accessories/plugins/ApplyFormatPlugin.properties",
					"accessories/plugins/ApplyFormatPlugin",
					() -> new freemind.modes.mindmapmode.commands.ApplyFormat(),
					freemind.modes.mindmapmode.commands.ApplyFormat.class,
					false,
					"%accessories/plugins/ApplyFormatPlugin.properties_name",
					"%accessories/plugins/ApplyFormatPlugin.properties_documentation",
					null,
					null,
					HookInstanciationMethod.ApplyToRoot,
					false,
					List.of("menu_bar/format/change/apply_format_from_menu"),
					
					new Properties()),
			new Command(
					"accessories/plugins/ChangeNodeLevelAction_left.properties",
					"accessories/plugins/ChangeNodeLevelAction",
					() -> new freemind.modes.mindmapmode.commands.ChangeNodeLevelAction(),
					freemind.modes.mindmapmode.commands.ChangeNodeLevelAction.class,
					false,
					"%accessories/plugins/ChangeNodeLevelAction_left.properties_name",
					"%accessories/plugins/ChangeNodeLevelAction_left.properties_documentation",
					"%keystroke_accessories/plugins/ChangeNodeLevelAction_left.properties_key",
					null,
					HookInstanciationMethod.ApplyToRoot,
					false,
					List.of("menu_bar/navigate/nodes/navigate_up"),
					
					props("action_type", "left")),
			new Command(
					"accessories/plugins/ChangeNodeLevelAction_right.properties",
					"accessories/plugins/ChangeNodeLevelAction",
					() -> new freemind.modes.mindmapmode.commands.ChangeNodeLevelAction(),
					freemind.modes.mindmapmode.commands.ChangeNodeLevelAction.class,
					false,
					"%accessories/plugins/ChangeNodeLevelAction_right.properties_name",
					"%accessories/plugins/ChangeNodeLevelAction_right.properties_documentation",
					"%keystroke_accessories/plugins/ChangeNodeLevelAction_right.properties_key",
					null,
					HookInstanciationMethod.ApplyToRoot,
					false,
					List.of("menu_bar/navigate/nodes/navigate_down"),
					
					props("action_type", "right")),
			new Command(
					"accessories/plugins/ChangeRootNode.properties",
					"accessories/plugins/ChangeRootNode",
					() -> new freemind.modes.mindmapmode.commands.ChangeRootNode(),
					freemind.modes.mindmapmode.commands.ChangeRootNode.class,
					false,
					"%accessories/plugins/ChangeRootNode.properties_name",
					"%accessories/plugins/ChangeRootNode.properties_documentation",
					"%keystroke_accessories/plugins/ChangeRootNode.properties_key",
					null,
					HookInstanciationMethod.ApplyToRoot,
					false,
					List.of("menu_bar/extras/first/nodes/root/change_root_node"),
					
					new Properties()),
			new Command(
					"accessories/plugins/ClonePasteAction.properties",
					"accessories/plugins/ClonePlugin",
					() -> new freemind.modes.mindmapmode.commands.ClonePasteAction(),
					freemind.modes.mindmapmode.commands.ClonePasteAction.class,
					false,
					"%accessories/plugins/ClonePasteAction.properties_name",
					"%accessories/plugins/ClonePasteAction.properties_documentation",
					"%keystroke_accessories/plugins/ClonePasteAction.properties_key",
					null,
					HookInstanciationMethod.Other,
					false,
					List.of("menu_bar/edit/paste/paste_as_clone"),
					
					new Properties()),
			new Command(
					"accessories/plugins/DeactivateCloneAction.properties",
					"accessories/plugins/ClonePlugin",
					() -> new freemind.modes.mindmapmode.commands.DeactivateCloneAction(),
					freemind.modes.mindmapmode.commands.DeactivateCloneAction.class,
					false,
					"%accessories/plugins/DeactivateCloneAction.properties_name",
					"%accessories/plugins/DeactivateCloneAction.properties_documentation",
					"%keystroke_accessories/plugins/DeactivateCloneAction.properties_key",
					null,
					HookInstanciationMethod.Other,
					false,
					List.of("menu_bar/edit/paste/paste_as_clone2"),
					
					new Properties()),
			new Command(
					"accessories/plugins/ShowCloneNodes.properties",
					"accessories/plugins/ClonePlugin",
					() -> new freemind.modes.mindmapmode.commands.ShowCloneNodes(),
					freemind.modes.mindmapmode.commands.ShowCloneNodes.class,
					false,
					"%accessories/plugins/ShowCloneNodes.properties_name",
					"%accessories/plugins/ShowCloneNodes.properties_documentation",
					"%keystroke_accessories/plugins/ShowCloneNodes.properties_key",
					null,
					HookInstanciationMethod.Once,
					false,
					List.of("menu_bar/navigate/clones"),
					
					new Properties()),
			new Command(
					"accessories/plugins/ExportToImage_PNG.properties",
					"accessories/plugins/ExportToImage",
					() -> new freemind.modes.mindmapmode.commands.ExportToImage(),
					freemind.modes.mindmapmode.commands.ExportToImage.class,
					true,
					"%accessories/plugins/ExportToImage_PNG.properties_name",
					"%accessories/plugins/ExportToImage_PNG.properties_documentation",
					null,
					null,
					HookInstanciationMethod.Other,
					false,
					List.of("menu_bar/file/export/picture/exportPng"),
					
					props("image_type", "png", "image_description", "Portable Network Graphic (PNG)")),
			new Command(
					"accessories/plugins/ExportToImage_JPEG.properties",
					"accessories/plugins/ExportToImage",
					() -> new freemind.modes.mindmapmode.commands.ExportToImage(),
					freemind.modes.mindmapmode.commands.ExportToImage.class,
					true,
					"%accessories/plugins/ExportToImage_JPEG.properties_name",
					"%accessories/plugins/ExportToImage_JPEG.properties_documentation",
					null,
					null,
					HookInstanciationMethod.Other,
					false,
					List.of("menu_bar/file/export/picture/exportJPEG"),
					
					props("image_type", "jpeg", "image_description", "Compressed image (JPEG)")),
			new Command(
					"accessories/plugins/FitToPage.properties",
					"accessories/plugins/FitToPage",
					() -> new freemind.modes.mindmapmode.commands.FitToPage(),
					freemind.modes.mindmapmode.commands.FitToPage.class,
					true,
					"%accessories/plugins/FitToPage.properties_name",
					"%accessories/plugins/FitToPage.properties_documentation",
					null,
					null,
					HookInstanciationMethod.Other,
					false,
					List.of("menu_bar/view/zoom/FitToPage"),
					
					new Properties()),
			new Command(
					"accessories/plugins/FormatCopy.properties",
					"accessories/plugins/FormatCopy",
					() -> new freemind.modes.mindmapmode.commands.FormatPaste(),
					freemind.modes.mindmapmode.commands.FormatPaste.class,
					false,
					"%accessories/plugins/FormatCopy.properties_name",
					"%accessories/plugins/FormatCopy.properties_documentation",
					"%keystroke_accessories/plugins/FormatCopy.properties.properties_key",
					"accessories/plugins/icons/colorpicker.png",
					HookInstanciationMethod.Other,
					false,
					List.of("menu_bar/edit/paste/copyFormat"),
					
					props("actionType", "copy_format")),
			new Command(
					"accessories/plugins/FormatPaste.properties",
					"accessories/plugins/FormatCopy",
					() -> new freemind.modes.mindmapmode.commands.FormatPaste(),
					freemind.modes.mindmapmode.commands.FormatPaste.class,
					false,
					"%accessories/plugins/FormatPaste.properties_name",
					"%accessories/plugins/FormatPaste.properties_documentation",
					"%keystroke_accessories/plugins/FormatPaste.properties.properties_key",
					"accessories/plugins/icons/color_fill.png",
					HookInstanciationMethod.Other,
					false,
					List.of("menu_bar/edit/paste/pasteFormat"),
					
					props("actionType", "paste_format")),
			new Command(
					"accessories/plugins/IconSelectionPlugin.properties",
					"accessories/plugins/IconSelectionPlugin",
					() -> new freemind.modes.mindmapmode.commands.IconSelection(),
					freemind.modes.mindmapmode.commands.IconSelection.class,
					false,
					"%accessories/plugins/IconSelectionPlugin.properties_name",
					"%accessories/plugins/IconSelectionPlugin.properties_documentation",
					"%keystroke_accessories/plugins/IconSelectionPlugin.properties.properties_key",
					"accessories/plugins/icons/kalzium.png",
					HookInstanciationMethod.ApplyToRoot,
					false,
					List.of("menu_bar/insert/icons/IconSelection"),
					
					new Properties()),
			new Command(
					"accessories/plugins/ImportMindmanagerFiles.properties",
					"accessories/plugins/ImportMindmanagerFiles",
					() -> new freemind.modes.mindmapmode.commands.ImportMindmanagerFiles(),
					freemind.modes.mindmapmode.commands.ImportMindmanagerFiles.class,
					true,
					"%accessories/plugins/ImportMindmanagerFiles.properties_name",
					"",
					null,
					null,
					HookInstanciationMethod.Other,
					false,
					List.of("menu_bar/file/import/ImportMindmanagerFiles"),
					
					new Properties()),
			new Command(
					"accessories/plugins/JumpLastEditLocation.properties",
					"accessories.plugins.JumpLastEditLocation",
					() -> new freemind.modes.mindmapmode.commands.JumpLastEditLocation(),
					freemind.modes.mindmapmode.commands.JumpLastEditLocation.class,
					false,
					"%accessories/plugins/JumpLastEditLocation.properties_name",
					"%accessories/plugins/JumpLastEditLocation.properties_documentation",
					"%keystroke_accessories/plugins/JumpLastEditLocation.properties.properties_key",
					null,
					HookInstanciationMethod.Once,
					false,
					List.of("menu_bar/navigate/folding/jump_last_edit_location"),
					
					new Properties()),
			new Command(
					"accessories/plugins/LogFileViewer.properties",
					"accessories/plugins/LogFileViewer",
					() -> new freemind.modes.mindmapmode.commands.LogFileViewer(),
					freemind.modes.mindmapmode.commands.LogFileViewer.class,
					true,
					"%accessories/plugins/LogFileViewer.properties_name",
					"%accessories/plugins/LogFileViewer.properties_documentation",
					"%keystroke_accessories/plugins/LogFileViewer.properties_key",
					"accessories/plugins/icons/tools-report-bug.png",
					HookInstanciationMethod.Other,
					false,
					List.of("menu_bar/extras/first/logFileViewer"),
					
					new Properties()),
			new Command(
					"accessories/plugins/ManagePatterns.properties",
					"accessories/plugins/ManagePatterns",
					() -> new freemind.modes.mindmapmode.commands.ManagePatterns(),
					freemind.modes.mindmapmode.commands.ManagePatterns.class,
					true,
					"%accessories/plugins/ManagePatterns.properties_name",
					"%accessories/plugins/ManagePatterns.properties_documentation",
					"%keystroke_accessories/plugins/ManagePatterns_manage_patterns_dialog",
					null,
					HookInstanciationMethod.Other,
					false,
					List.of("menu_bar/format/patterns/manage/ManagePatterns", "mindmapmode_popup/patterns/manage/ManagePatterns"),
					
					new Properties()),
			new Command(
					"accessories/plugins/NewParentNode.properties",
					"accessories/plugins/NewParentNode",
					() -> new freemind.modes.mindmapmode.commands.NewParentNode(),
					freemind.modes.mindmapmode.commands.NewParentNode.class,
					false,
					"%accessories/plugins/NewParentNode.properties_name",
					"%accessories/plugins/NewParentNode.properties_documentation",
					"%keystroke_accessories/plugins/NewParentNode.properties_key",
					"accessories/plugins/icons/stock_text_indent.png",
					HookInstanciationMethod.ApplyToRoot,
					false,
					List.of("menu_bar/insert/nodes/automaticLayout"),
					
					new Properties()),
			new Command(
					"accessories/plugins/NodeAttributeTable_jumpto.properties",
					"accessories/plugins/NodeAttributeTable",
					() -> new freemind.modes.mindmapmode.commands.NodeAttributeTable(),
					freemind.modes.mindmapmode.commands.NodeAttributeTable.class,
					false,
					"%accessories/plugins/NodeAttributeTable_jumpto.properties_name",
					"%accessories/plugins/NodeAttributeTable_jumpto.properties_documentation",
					"%keystroke_accessories/plugins/NodeAttributeTable_jumpto.keystroke",
					null,
					HookInstanciationMethod.Other,
					false,
					List.of("menu_bar/navigate/AttributeTables/AttributeTable_jumpto"),
					
					props("command", "jump")),
			new Command(
					"accessories/plugins/NodeAttributeTable_hide_show.properties",
					"accessories/plugins/NodeAttributeTable",
					() -> new freemind.modes.mindmapmode.commands.NodeAttributeTable(),
					freemind.modes.mindmapmode.commands.NodeAttributeTable.class,
					false,
					"%accessories/plugins/NodeAttributeTable_hide_show.properties_name",
					"%accessories/plugins/NodeAttributeTable_hide_show.properties_documentation",
					"%keystroke_edit_attributes",
					null,
					HookInstanciationMethod.Other,
					true,
					List.of("menu_bar/view/AttributeTable_window/AttributeTable_hide_show", "popup/popup/AttributeTable_hide_show"),
					
					props("command", "hide_show")),
			new Command(
					"accessories/plugins/NodeHistoryBack.properties",
					"accessories/plugins/NodeHistory",
					() -> new freemind.modes.mindmapmode.commands.NodeHistory(),
					freemind.modes.mindmapmode.commands.NodeHistory.class,
					false,
					"%accessories/plugins/NodeHistoryBack.properties_name",
					"%accessories/plugins/NodeHistoryBack.properties_documentation",
					"%keystroke_accessories/plugins/NodeHistoryBack.keystroke.alt_BACK",
					"accessories/plugins/icons/back.png",
					HookInstanciationMethod.Other,
					false,
					List.of("menu_bar/navigate/folding/NodeHistoryBack", "mindmapmode_toolbar/folding/NodeHistoryBack"),
					
					props("direction", "back")),
			new Command(
					"accessories/plugins/NodeHistoryForward.properties",
					"accessories/plugins/NodeHistory",
					() -> new freemind.modes.mindmapmode.commands.NodeHistory(),
					freemind.modes.mindmapmode.commands.NodeHistory.class,
					false,
					"%accessories/plugins/NodeHistoryForward.properties_name",
					"%accessories/plugins/NodeHistoryForward.properties_documentation",
					"%keystroke_accessories/plugins/NodeHistoryForward.keystroke.alt_FORWARD",
					"accessories/plugins/icons/forward.png",
					HookInstanciationMethod.Other,
					false,
					List.of("menu_bar/navigate/folding/NodeHistoryForward", "mindmapmode_toolbar/folding/NodeHistoryForward"),
					
					props("direction", "forward")),
			new Command(
					"accessories/plugins/NodeNote_jumpto.properties",
					"accessories/plugins/NodeNote",
					() -> new freemind.modes.mindmapmode.commands.NodeNote(),
					freemind.modes.mindmapmode.commands.NodeNote.class,
					false,
					"%accessories/plugins/NodeNote_jumpto.properties_name",
					"%accessories/plugins/NodeNote_jumpto.properties_documentation",
					"%keystroke_accessories/plugins/NodeNote_jumpto.keystroke.alt_N",
					null,
					HookInstanciationMethod.Other,
					false,
					List.of("menu_bar/navigate/notes/note_jumpto"),
					
					props("command", "jump")),
			new Command(
					"accessories/plugins/NodeNote_hide_show.properties",
					"accessories/plugins/NodeNote",
					() -> new freemind.modes.mindmapmode.commands.NodeNote(),
					freemind.modes.mindmapmode.commands.NodeNote.class,
					false,
					"%accessories/plugins/NodeNote_hide_show.properties_name",
					"%accessories/plugins/NodeNote_hide_show.properties_documentation",
					"%keystroke_accessories/plugins/NodeNote_hide_show.keystroke.control_shift_less",
					null,
					HookInstanciationMethod.Other,
					true,
					List.of("menu_bar/view/note_window/note_hide_show", "popup/popup/note_hide_show"),
					
					props("command", "hide_show")),
			new Command(
					"accessories/plugins/RemoveNote.properties",
					"accessories/plugins/RemoveNote",
					() -> new freemind.modes.mindmapmode.commands.RemoveNote(),
					freemind.modes.mindmapmode.commands.RemoveNote.class,
					false,
					"%accessories/plugins/RemoveNote.properties_name",
					"%accessories/plugins/RemoveNote.properties_documentation",
					"%keystroke_accessories/plugins/RemoveNote.properties.properties_key",
					null,
					HookInstanciationMethod.OnceForRoot,
					false,
					List.of("menu_bar/format/nodes/remove_notes/RemoveNote"),
					
					props("actionType", "remove_note")),
			new Command(
					"accessories/plugins/SaveAll.properties",
					"accessories/plugins/SaveAll",
					() -> new freemind.modes.mindmapmode.commands.SaveAll(),
					freemind.modes.mindmapmode.commands.SaveAll.class,
					true,
					"%accessories/plugins/SaveAll.properties_name",
					"%accessories/plugins/SaveAll.properties_documentation",
					"%keystroke_accessories/plugins/SaveAll.properties_key",
					null,
					HookInstanciationMethod.Other,
					false,
					List.of("menu_bar/file/open/SaveAll"),
					
					new Properties()),
			new Command(
					"accessories/plugins/SortNodes.properties",
					"accessories/plugins/SortNodes",
					() -> new freemind.modes.mindmapmode.commands.SortNodes(),
					freemind.modes.mindmapmode.commands.SortNodes.class,
					false,
					"%accessories/plugins/SortNodes.properties_name",
					"%accessories/plugins/SortNodes.properties_documentation",
					"%keystroke_accessories/plugins/SortNodes.properties_key",
					null,
					HookInstanciationMethod.Other,
					false,
					List.of("menu_bar/extras/first/nodes/sorting/SortNodes"),
					
					new Properties()),
			new Command(
					"accessories/plugins/SplitNode.properties",
					"accessories/plugins/SplitNode",
					() -> new freemind.modes.mindmapmode.commands.SplitNode(),
					freemind.modes.mindmapmode.commands.SplitNode.class,
					false,
					"%accessories/plugins/SplitNode.properties_name",
					"%accessories/plugins/SplitNode.properties_documentation",
					"%keystroke_accessories/plugins/SplitNode.properties_key",
					"accessories/plugins/icons/stock_text_indent.png",
					HookInstanciationMethod.ApplyToRoot,
					false,
					List.of("menu_bar/extras/first/join/SplitNode"),
					
					new Properties()),
			new Command(
					"plugins/TimeManagement.xml",
					"plugins/TimeManagement",
					() -> new freemind.modes.mindmapmode.commands.time.TimeManagement(),
					freemind.modes.mindmapmode.commands.time.TimeManagement.class,
					true,
					"%plugins/TimeManagement.xml_name",
					"%plugins/TimeManagement.xml_documentation",
					"%keystroke_plugins/TimeManagement.xml_key",
					null,
					HookInstanciationMethod.Other,
					false,
					List.of("menu_bar/extras/first/time_management/start"),
					
					new Properties()),
			new Command(
					"plugins/TimeList.xml",
					"plugins/TimeManagement",
					() -> new freemind.modes.mindmapmode.commands.time.TimeList(),
					freemind.modes.mindmapmode.commands.time.TimeList.class,
					true,
					"%plugins/TimeList.xml_name",
					"%plugins/TimeList.xml_documentation",
					null,
					null,
					HookInstanciationMethod.Other,
					false,
					List.of("menu_bar/extras/first/time_management/timelist"),
					
					props("show_all_nodes", "false")),
			new Command(
					"plugins/NodeList.xml",
					"plugins/TimeManagement",
					() -> new freemind.modes.mindmapmode.commands.time.TimeList(),
					freemind.modes.mindmapmode.commands.time.TimeList.class,
					true,
					"%plugins/NodeList.xml_name",
					"%plugins/NodeList.xml_documentation",
					"%keystroke_plugins/TimeList.xml_key",
					null,
					HookInstanciationMethod.Other,
					false,
					List.of("menu_bar/edit/find/findReplace/nodelist"),
					
					props("show_all_nodes", "true")),
			new Command(
					"plugins/time/RemoveReminderHook.java",
					"plugins/TimeManagement",
					() -> new freemind.modes.mindmapmode.commands.time.RemoveReminder(),
					freemind.modes.mindmapmode.commands.time.RemoveReminder.class,
					false,
					"%plugins/RemoveReminder.xml_name",
					"%plugins/RemoveReminder.xml_documentation",
					null,
					null,
					HookInstanciationMethod.Other,
					false,
					List.of("menu_bar/extras/first/time_management/remove_reminder", "mindmapmode_popup/context/remove_reminder"),
					
					new Properties()),
			new Command(
					"accessories/plugins/UnfoldAll.properties",
					"accessories/plugins/UnfoldAll",
					() -> new freemind.modes.mindmapmode.commands.UnfoldAll(),
					freemind.modes.mindmapmode.commands.UnfoldAll.class,
					false,
					"%accessories/plugins/UnfoldAll.properties_name",
					"%accessories/plugins/UnfoldAll.properties_documentation",
					"%keystroke_accessories/plugins/UnfoldAll.keystroke.alt_END",
					"accessories/plugins/icons/hotlistadd.png",
					HookInstanciationMethod.Other,
					false,
					List.of("menu_bar/navigate/folding/unfoldAll"),
					
					props("foldingState", "false", "foldingType", "All", "applyTo", "node")),
			new Command(
					"accessories/plugins/FoldAll.properties",
					"accessories/plugins/UnfoldAll",
					() -> new freemind.modes.mindmapmode.commands.UnfoldAll(),
					freemind.modes.mindmapmode.commands.UnfoldAll.class,
					false,
					"%accessories/plugins/FoldAll.properties_name",
					"%accessories/plugins/FoldAll.properties_documentation",
					"%keystroke_accessories/plugins/UnfoldAll.keystroke.alt_HOME",
					"accessories/plugins/icons/hotlistdel.png",
					HookInstanciationMethod.Other,
					false,
					List.of("menu_bar/navigate/folding/foldAll"),
					
					props("foldingState", "true", "foldingType", "All", "applyTo", "node")),
			new Command(
					"accessories/plugins/UnfoldOneLevel.properties",
					"accessories/plugins/UnfoldAll",
					() -> new freemind.modes.mindmapmode.commands.UnfoldAll(),
					freemind.modes.mindmapmode.commands.UnfoldAll.class,
					false,
					"%accessories/plugins/UnfoldOneLevel.properties_name",
					"%accessories/plugins/UnfoldOneLevel.properties_documentation",
					"%keystroke_accessories/plugins/UnfoldAll.keystroke.alt_PAGE_DOWN",
					"accessories/plugins/icons/edit_add.png",
					HookInstanciationMethod.Other,
					false,
					List.of("menu_bar/navigate/folding/unfoldOneLevel"),
					
					props("foldingState", "false", "foldingType", "OneLevel", "applyTo", "node")),
			new Command(
					"accessories/plugins/FoldOneLevel.properties",
					"accessories/plugins/UnfoldAll",
					() -> new freemind.modes.mindmapmode.commands.UnfoldAll(),
					freemind.modes.mindmapmode.commands.UnfoldAll.class,
					false,
					"%accessories/plugins/FoldOneLevel.properties_name",
					"%accessories/plugins/FoldOneLevel.properties_documentation",
					"%keystroke_accessories/plugins/UnfoldAll.keystroke.alt_PAGE_UP",
					"accessories/plugins/icons/edit_remove.png",
					HookInstanciationMethod.Other,
					false,
					List.of("menu_bar/navigate/folding/foldOneLevel"),
					
					props("foldingState", "true", "foldingType", "OneLevel", "applyTo", "node")),
			new Command(
					"accessories/plugins/UnfoldAllRoot.properties",
					"accessories/plugins/UnfoldAll",
					() -> new freemind.modes.mindmapmode.commands.UnfoldAll(),
					freemind.modes.mindmapmode.commands.UnfoldAll.class,
					false,
					"%accessories/plugins/UnfoldAll.properties_name",
					"%accessories/plugins/UnfoldAll.properties_documentation",
					"%keystroke_accessories/plugins/UnfoldAll.keystroke.alt_END",
					"accessories/plugins/icons/hotlistadd.png",
					HookInstanciationMethod.Other,
					false,
					List.of("mindmapmode_toolbar/folding/unfoldAll"),
					
					props("foldingState", "false", "foldingType", "All", "applyTo", "node")),
			new Command(
					"accessories/plugins/FoldAllRoot.properties",
					"accessories/plugins/UnfoldAll",
					() -> new freemind.modes.mindmapmode.commands.UnfoldAll(),
					freemind.modes.mindmapmode.commands.UnfoldAll.class,
					false,
					"%accessories/plugins/FoldAll.properties_name",
					"%accessories/plugins/FoldAll.properties_documentation",
					"%keystroke_accessories/plugins/UnfoldAll.keystroke.alt_HOME",
					"accessories/plugins/icons/hotlistdel.png",
					HookInstanciationMethod.Other,
					false,
					List.of("mindmapmode_toolbar/folding/foldAll"),
					
					props("foldingState", "true", "foldingType", "All", "applyTo", "node")),
			new Command(
					"accessories/plugins/UnfoldOneLevelRoot.properties",
					"accessories/plugins/UnfoldAll",
					() -> new freemind.modes.mindmapmode.commands.UnfoldAll(),
					freemind.modes.mindmapmode.commands.UnfoldAll.class,
					false,
					"%accessories/plugins/UnfoldOneLevel.properties_name",
					"%accessories/plugins/UnfoldOneLevel.properties_documentation",
					"%keystroke_accessories/plugins/UnfoldAll.keystroke.alt_PAGE_DOWN",
					"accessories/plugins/icons/edit_add.png",
					HookInstanciationMethod.Other,
					false,
					List.of("mindmapmode_toolbar/folding/unfoldOneLevel"),
					
					props("foldingState", "false", "foldingType", "OneLevel", "applyTo", "node")),
			new Command(
					"accessories/plugins/FoldOneLevelRoot.properties",
					"accessories/plugins/UnfoldAll",
					() -> new freemind.modes.mindmapmode.commands.UnfoldAll(),
					freemind.modes.mindmapmode.commands.UnfoldAll.class,
					false,
					"%accessories/plugins/FoldOneLevel.properties_name",
					"%accessories/plugins/FoldOneLevel.properties_documentation",
					"%keystroke_accessories/plugins/UnfoldAll.keystroke.alt_PAGE_UP",
					"accessories/plugins/icons/edit_remove.png",
					HookInstanciationMethod.Other,
					false,
					List.of("mindmapmode_toolbar/folding/foldOneLevel"),
					
					props("foldingState", "true", "foldingType", "OneLevel", "applyTo", "node"))
	);

	public static List<Command> commands() {
		return COMMANDS;
	}

	public static Command command(String label) {
		for (Command command : COMMANDS) {
			if (command.label.equals(label)) {
				return command;
			}
		}
		throw new IllegalArgumentException("Unknown menu command " + label);
	}

	public static void decorate(Command command, AbstractAction action) {
		String name = fromResource(command.name);
		if (name != null) {
			action.putValue(AbstractAction.NAME, name);
		} else {
			action.putValue(AbstractAction.NAME, command.type.getName());
		}
		String docu = fromResource(command.documentation);
		if (docu != null) {
			action.putValue(AbstractAction.SHORT_DESCRIPTION, docu);
			action.putValue(AbstractAction.LONG_DESCRIPTION, docu);
		}
		if (command.iconPath != null) {
			URL iconUrl = Resources.getInstance().getFreeMindClassLoader().getResource(command.iconPath);
			if (iconUrl != null) {
				ImageIcon imageIcon = freemind.view.ImageFactory.getInstance().createIcon(iconUrl);
				action.putValue(AbstractAction.SMALL_ICON, imageIcon);
			}
		}
		String key = fromProperty(command.keyStroke);
		if (key != null) {
			action.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(key));
		}
	}

	public static JMenuItem menuItem(Command command, AbstractAction action) {
		if (command.selectable) {
			return new JCheckBoxMenuItem(action);
		}
		return new JMenuItem(action);
	}

	private static String fromResource(String string) {
		if (string != null && string.startsWith("%")) {
			return Resources.getInstance().getResourceString(string.substring(1));
		}
		return string;
	}

	private static String fromProperty(String string) {
		if (string != null && string.startsWith("%")) {
			return Resources.getInstance().getProperty(string.substring(1));
		}
		return string;
	}
}
