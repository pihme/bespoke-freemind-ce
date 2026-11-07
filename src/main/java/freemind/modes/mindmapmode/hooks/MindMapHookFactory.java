/*FreeMind - A Program for creating and viewing Mindmaps
 *Copyright (C) 2000-2001  Joerg Mueller <joergmueller@bigfoot.com>
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

package freemind.modes.mindmapmode.hooks;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import freemind.extensions.HookFactory.RegistrationContainer;
import freemind.extensions.HookFactoryAdapter;
import freemind.extensions.HookInstanciationMethod;
import freemind.extensions.MindMapHook;
import freemind.extensions.MindMapHook.PluginBaseClassSearcher;
import freemind.extensions.ModeControllerHook;
import freemind.extensions.NodeHook;
import freemind.main.Resources;
import freemind.modes.mindmapmode.MindMapController;
import freemind.modes.mindmapmode.hooks.BuiltInHooks.Hook;

/**
 * Constructs the built-in hooks. The hook names are the ones a .mm file stores.
 *
 * @author foltin
 *
 */
public class MindMapHookFactory extends HookFactoryAdapter {

	protected static java.util.logging.Logger logger = null;

	private static HashMap<String, Hook> pluginInfo = null;

	private static Vector<String> allPlugins = null;

	public MindMapHookFactory() {
			logger = freemind.main.Resources.getInstance().getLogger(
					this.getClass().getName());

		allRegistrationInstances = new HashMap<>();
	}

	/**
	 * @return a string vector with representatives for plugins.
	 */
	public Vector<String> getPossibleNodeHooks() {
		return searchFor(NodeHook.class, MindMapController.class);
	}

	/**
	 * @return a string vector with representatives for plugins.
	 */
	public Vector<String> getPossibleModeControllerHooks() {
		return searchFor(ModeControllerHook.class, MindMapController.class);
	}

	/**
	 * @return a string vector with representatives for plugins.
	 */
	private Vector<String> searchFor(Class baseClass, Class<?> mode) {
		actualizePlugins();
		Vector<String> returnValue = new Vector<>();
		String modeName = mode.getPackage().getName();
		for (String label : allPlugins) {
			Hook hook = getHook(label);
			logger.finest("Loading: " + label);
			if (baseClass.isAssignableFrom(hook.base)) {
				for (String pmode : hook.modes) {
					if (pmode.equals(modeName)) {
						returnValue.add(label);
					}
				}
			}
		}
		return returnValue;
	}


	private void actualizePlugins() {
		if (pluginInfo != null) {
			return;
		}
		HashMap<String, Hook> info = new HashMap<>();
		Vector<String> plugins = new Vector<>();
		for (Hook hook : BuiltInHooks.hooks()) {
			info.put(hook.label, hook);
			plugins.add(hook.label);
		}
		pluginInfo = info;
		allPlugins = plugins;
	}

	public ModeControllerHook createModeControllerHook(String hookName) {
		return (ModeControllerHook) createJavaHook(hookName, getHook(hookName));
	}

	private MindMapHook createJavaHook(String hookName, Hook descriptor) {
		try {
			MindMapHook hook = descriptor.create.get();
			decorateHook(hookName, descriptor, hook);
			return hook;
		} catch (Throwable e) {
			freemind.main.Resources.getInstance().logException(e,
					"Error occurred loading hook: " + descriptor.type.getName());
			return null;
		}
	}

	/**
	 * Do not call this method directly. Call ModeController.createNodeHook
	 * instead.
	 */
	public NodeHook createNodeHook(String hookName) {
		logger.finest("CreateNodeHook: " + hookName);
		return (NodeHook) createJavaHook(hookName, getHook(hookName));
	}

	private void decorateHook(String hookName, final Hook descriptor, MindMapHook hook) {
		hook.setProperties(descriptor.properties);
		hook.setName(hookName);
		PluginBaseClassSearcher pluginBaseClassSearcher = new PluginBaseClassSearcher() {

			public Object getPluginBaseObject() {
				return lookupPluginBase(descriptor.pluginLabel);
			}
		};
		hook.setPluginBaseClass(pluginBaseClassSearcher);
	}


	public void decorateAction(String hookName, AbstractAction action) {
		Hook descriptor = getHook(hookName);
		String name = fromResource(descriptor.name);
		if (name != null) {
			action.putValue(AbstractAction.NAME, name);
		} else {
			action.putValue(AbstractAction.NAME, descriptor.type.getName());
		}
		String docu = fromResource(descriptor.documentation);
		if (docu != null) {
			action.putValue(AbstractAction.SHORT_DESCRIPTION, docu);
			action.putValue(AbstractAction.LONG_DESCRIPTION, docu);
		}
		String icon = descriptor.iconPath;
		if (icon != null) {
			URL iconUrl = Resources.getInstance().getFreeMindClassLoader().getResource(icon);
			if (iconUrl != null) {
				ImageIcon imageIcon = freemind.view.ImageFactory.getInstance().createIcon(iconUrl);
				action.putValue(AbstractAction.SMALL_ICON, imageIcon);
			} else {
				logger.warning("Icon not found: " + icon);
			}
		}
		String key = fromProperty(descriptor.keyStroke);
		if (key != null)
			action.putValue(AbstractAction.ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(key));

	}

	private String fromResource(String string) {
		if (string != null && string.startsWith("%")) {
			return Resources.getInstance().getResourceString(string.substring(1));
		}
		return string;
	}

	private String fromProperty(String string) {
		if (string != null && string.startsWith("%")) {
			return Resources.getInstance().getProperty(string.substring(1));
		}
		return string;
	}

	/**
	 * @return returns a list of menu position strings for the
	 *         StructuredMenuHolder.
	 */
	public List<String> getHookMenuPositions(String hookName) {
		return getHook(hookName).menus;
	}


	public HookInstanciationMethod getInstanciationMethod(String hookName) {
		return getHook(hookName).instantiation;
	}

	/**
	 * Each Plugin can have a list of HookRegistrations that are called after
	 * the corresponding mode is enabled. (Like singletons.) One of these can
	 * operate as the pluginBase that is accessible to every normal
	 * plugin_action via the getPluginBaseClass method.
	 *
	 * @return A list of RegistrationContainer elements. The field
	 *         hookRegistrationClass of RegistrationContainer is a class that is
	 *         (probably) of HookRegistration type. You have to register every
	 *         registration via the registerRegistrationContainer method when
	 *         instanciated (this is typically done in the ModeController).
	 */
	public List<RegistrationContainer> getRegistrations() {
		String modeName = MindMapController.class.getPackage().getName();
		Vector<RegistrationContainer> returnValue = new Vector<>();
		for (BuiltInHooks.Registration registration : BuiltInHooks.registrations()) {
			boolean modeFound = false;
			for (String possibleMode : registration.modes) {
				if (modeName.equals(possibleMode)) {
					modeFound = true;
				}
			}
			if (!modeFound) {
				continue;
			}
			RegistrationContainer container = new RegistrationContainer();
			container.hookRegistrationClass = registration.type;
			container.pluginLabel = registration.pluginLabel;
			container.isPluginBase = registration.pluginBase;
			container.newRegistration = registration.create;
			returnValue.add(container);
		}
		return returnValue;
	}

	/**
	 * A plugin base class is a common registration class of multiple plugins.
	 * It is useful to embrace several related plugins (example: EncryptedNote
	 * -> Registration).
	 *
	 * @return the base class if declared and successfully instanciated or NULL.
	 */
	public Object getPluginBaseClass(String hookName) {
		logger.finest("getPluginBaseClass: " + hookName);
		return lookupPluginBase(getHook(hookName).pluginLabel);
	}


	public Object registration(String pluginLabel) {
		return lookupPluginBase(pluginLabel);
	}

	private Object lookupPluginBase(String pluginLabel) {
		if (allRegistrationInstances.containsKey(pluginLabel)) {
			return allRegistrationInstances.get(pluginLabel);
		}
		return null;
	}


	private Hook getHook(String hookName) {
		actualizePlugins();
		Hook descriptor = pluginInfo.get(hookName);
		if (hookName == null || descriptor == null)
			throw new IllegalArgumentException("Unknown hook name " + hookName);
		return descriptor;
	}

	public JMenuItem getMenuItem(String pHookName, AbstractAction pHookAction) {
		if (getHook(pHookName).selectable) {
			return new JCheckBoxMenuItem(pHookAction);
		} else {
			return new JMenuItem(pHookAction);
		}
	}

}
