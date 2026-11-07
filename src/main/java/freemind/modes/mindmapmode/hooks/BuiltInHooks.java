package freemind.modes.mindmapmode.hooks;

import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

import freemind.extensions.HookInstanciationMethod;
import freemind.extensions.MindMapHook;
import freemind.extensions.NodeHookAdapter;
import freemind.extensions.PermanentNodeHookAdapter;
import freemind.extensions.HookFactory.RegistrationFactory;

/**
 * The features that used to be loaded from plugin XML.
 * Each hook name is the string a .mm file stores.
 */
public final class BuiltInHooks {

	private BuiltInHooks() {
	}

	public static final class Hook {
		public final String label;
		public final String pluginLabel;
		public final Supplier<MindMapHook> create;
		public final Class<?> type;
		public final Class<?> base;
		public final String name;
		public final String documentation;
		public final String keyStroke;
		public final String iconPath;
		public final HookInstanciationMethod instantiation;
		public final boolean selectable;
		public final List<String> menus;
		public final List<String> modes;
		public final Properties properties;

		Hook(String label, String pluginLabel, Supplier<MindMapHook> create, Class<?> type,
				Class<?> base, String name, String documentation, String keyStroke, String iconPath,
				HookInstanciationMethod instantiation, boolean selectable, List<String> menus,
				List<String> modes, Properties properties) {
			this.label = label;
			this.pluginLabel = pluginLabel;
			this.create = create;
			this.type = type;
			this.base = base;
			this.name = name;
			this.documentation = documentation;
			this.keyStroke = keyStroke;
			this.iconPath = iconPath;
			this.instantiation = instantiation;
			this.selectable = selectable;
			this.menus = menus;
			this.modes = modes;
			this.properties = properties;
		}
	}

	public static final class Registration {
		public final String pluginLabel;
		public final boolean pluginBase;
		public final Class<?> type;
		public final List<String> modes;
		public final RegistrationFactory create;

		Registration(String pluginLabel, boolean pluginBase, Class<?> type, List<String> modes,
				RegistrationFactory create) {
			this.pluginLabel = pluginLabel;
			this.pluginBase = pluginBase;
			this.type = type;
			this.modes = modes;
			this.create = create;
		}
	}

	private static Properties props(String... pairs) {
		Properties properties = new Properties();
		for (int i = 0; i < pairs.length; i += 2) {
			properties.setProperty(pairs[i], pairs[i + 1]);
		}
		return properties;
	}

	private static final List<Hook> HOOKS = List.of(
			new Hook(
					"accessories/plugins/AutomaticLayout.properties",
					"accessories/plugins/AutomaticLayout",
					() -> new freemind.modes.mindmapmode.features.AutomaticLayout(),
					freemind.modes.mindmapmode.features.AutomaticLayout.class,
					NodeHookAdapter.class,
					"%accessories/plugins/AutomaticLayout.properties_name",
					"%accessories/plugins/AutomaticLayout.properties_documentation",
					null,
					"accessories/plugins/icons/kcmsystem.png",
					HookInstanciationMethod.OnceForRoot,
					true,
					List.of("menu_bar/format/nodes/automaticLayout"),
					List.of("freemind.modes.mindmapmode"),
					new Properties()),
			new Hook(
					"accessories/plugins/BlinkingNodeHook.properties",
					"accessories/plugins/BlinkingNodeHook",
					() -> new freemind.modes.mindmapmode.features.BlinkingNodeHook(),
					freemind.modes.mindmapmode.features.BlinkingNodeHook.class,
					NodeHookAdapter.class,
					"%accessories/plugins/BlinkingNodeHook.properties_name",
					"%accessories/plugins/BlinkingNodeHook.properties_documentation",
					null,
					"accessories/plugins/icons/xeyes.png",
					HookInstanciationMethod.Once,
					true,
					List.of("menu_bar/format/nodes/BlinkingNodeHook"),
					List.of("freemind.modes.mindmapmode"),
					new Properties()),
			new Hook(
					"accessories/plugins/ClonePlugin.properties",
					"accessories/plugins/ClonePlugin",
					() -> new freemind.modes.mindmapmode.features.Clone(),
					freemind.modes.mindmapmode.features.Clone.class,
					NodeHookAdapter.class,
					"%accessories/plugins/ClonePlugin.properties_name",
					"%accessories/plugins/ClonePlugin.properties_documentation",
					null,
					null,
					HookInstanciationMethod.Once,
					false,
					List.of(),
					List.of("freemind.modes.mindmapmode"),
					new Properties()),
			new Hook(
					"accessories/plugins/CreationModificationPlugin_new.properties",
					"accessories/plugins/CreationModificationPlugin_new",
					() -> new freemind.modes.mindmapmode.features.CreationModification(),
					freemind.modes.mindmapmode.features.CreationModification.class,
					NodeHookAdapter.class,
					"%accessories/plugins/CreationModificationPlugin.properties_name",
					"%accessories/plugins/CreationModificationPlugin.properties_documentation",
					null,
					"accessories/plugins/icons/kword.png",
					HookInstanciationMethod.OnceForRoot,
					false,
					List.of("menu_bar/extras/first/nodes/change/CreationModificationPlugin"),
					List.of("freemind.modes.mindmapmode"),
					props("tooltip_format", "<html>Created:  {0,date} {0,time}<br>Modified: {1,date} {1,time}</html>")),
			new Hook(
					"accessories/plugins/HierarchicalIcons.properties",
					"accessories/plugins/HierarchicalIcons",
					() -> new freemind.modes.mindmapmode.features.HierarchicalIcons(),
					freemind.modes.mindmapmode.features.HierarchicalIcons.class,
					NodeHookAdapter.class,
					"%accessories/plugins/HierarchicalIcons.properties_name",
					"%accessories/plugins/HierarchicalIcons.properties_documentation",
					null,
					null,
					HookInstanciationMethod.OnceForRoot,
					true,
					List.of("menu_bar/format/nodes/automaticLayout2"),
					List.of("freemind.modes.mindmapmode"),
					new Properties()),
			new Hook(
					"accessories/plugins/RevisionPlugin.properties",
					"accessories/plugins/RevisionPlugin",
					() -> new freemind.modes.mindmapmode.features.Revision(),
					freemind.modes.mindmapmode.features.Revision.class,
					NodeHookAdapter.class,
					"%accessories/plugins/RevisionPlugin.properties_name",
					"%accessories/plugins/RevisionPlugin.properties_documentation",
					null,
					null,
					HookInstanciationMethod.OnceForRoot,
					false,
					List.of("menu_bar/extras/first/nodes/change/RevisionPlugin.class"),
					List.of("freemind.modes.mindmapmode"),
					new Properties()),
			new Hook(
					"plugins/TimeManagementReminder.xml",
					"plugins/TimeManagement",
					() -> new freemind.modes.mindmapmode.features.time.ReminderHook(),
					freemind.modes.mindmapmode.features.time.ReminderHook.class,
					PermanentNodeHookAdapter.class,
					"%plugins/TimeManagementReminder.xml_name",
					"%plugins/TimeManagementReminder.xml_documentation",
					null,
					null,
					HookInstanciationMethod.Once,
					false,
					List.of(),
					List.of("freemind.modes.mindmapmode"),
					new Properties())
	);

	private static final List<Registration> REGISTRATIONS = List.of(
			new Registration(
					"accessories/plugins/AutomaticLayout",
					true,
					freemind.modes.mindmapmode.features.AutomaticLayout.Registration.class,
					List.of("freemind.modes.mindmapmode"),
					(controller, map) -> new freemind.modes.mindmapmode.features.AutomaticLayout.Registration(controller, map)),
			new Registration(
					"accessories/plugins/ChangeRootNode",
					false,
					freemind.modes.mindmapmode.commands.ChangeRootNode.Registration.class,
					List.of("freemind.modes.mindmapmode"),
					(controller, map) -> new freemind.modes.mindmapmode.commands.ChangeRootNode.Registration(controller, map)),
			new Registration(
					"accessories/plugins/ClonePlugin",
					true,
					freemind.modes.mindmapmode.commands.ClonePasteAction.Registration.class,
					List.of("freemind.modes.mindmapmode"),
					(controller, map) -> new freemind.modes.mindmapmode.commands.ClonePasteAction.Registration(controller, map)),
			new Registration(
					"accessories.plugins.JumpLastEditLocation",
					true,
					freemind.modes.mindmapmode.commands.JumpLastEditLocation.JumpLastEditLocationRegistration.class,
					List.of("freemind.modes.mindmapmode"),
					(controller, map) -> new freemind.modes.mindmapmode.commands.JumpLastEditLocation.JumpLastEditLocationRegistration(controller, map)),
			new Registration(
					"accessories/plugins/LogFileViewer",
					true,
					freemind.modes.mindmapmode.commands.LogFileViewer.Registration.class,
					List.of("freemind.modes.mindmapmode"),
					(controller, map) -> new freemind.modes.mindmapmode.commands.LogFileViewer.Registration(controller, map)),
			new Registration(
					"accessories/plugins/NodeAttributeTable",
					true,
					freemind.modes.mindmapmode.commands.NodeAttributeTableRegistration.class,
					List.of("freemind.modes.mindmapmode"),
					(controller, map) -> new freemind.modes.mindmapmode.commands.NodeAttributeTableRegistration(controller, map)),
			new Registration(
					"accessories/plugins/NodeHistory",
					true,
					freemind.modes.mindmapmode.commands.NodeHistory.Registration.class,
					List.of("freemind.modes.mindmapmode"),
					(controller, map) -> new freemind.modes.mindmapmode.commands.NodeHistory.Registration(controller, map)),
			new Registration(
					"accessories/plugins/NodeNote",
					true,
					freemind.modes.mindmapmode.commands.NodeNoteRegistration.class,
					List.of("freemind.modes.mindmapmode"),
					(controller, map) -> new freemind.modes.mindmapmode.commands.NodeNoteRegistration(controller, map)),
			new Registration(
					"accessories/plugins/RemoveNote",
					true,
					freemind.modes.mindmapmode.commands.RemoveNote.Registration.class,
					List.of("freemind.modes.mindmapmode"),
					(controller, map) -> new freemind.modes.mindmapmode.commands.RemoveNote.Registration(controller, map)),
			new Registration(
					"plugins/TimeManagement",
					true,
					freemind.modes.mindmapmode.commands.time.TimeManagementOrganizer.class,
					List.of("freemind.modes.mindmapmode"),
					(controller, map) -> new freemind.modes.mindmapmode.commands.time.TimeManagementOrganizer(controller, map)),
			new Registration(
					"accessories/plugins/UnfoldAll",
					false,
					freemind.modes.mindmapmode.commands.UnfoldAll.Registration.class,
					List.of("freemind.modes.mindmapmode"),
					(controller, map) -> new freemind.modes.mindmapmode.commands.UnfoldAll.Registration(controller, map))
	);

	public static List<Hook> hooks() {
		return HOOKS;
	}

	public static List<Registration> registrations() {
		return REGISTRATIONS;
	}
}
