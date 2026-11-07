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

package freemind.modes.mindmapmode.actions;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.List;

import javax.swing.Action;
import javax.swing.JMenuItem;

import freemind.controller.MenuItemEnabledListener;
import freemind.controller.MenuItemSelectedListener;
import freemind.extensions.HookInstanciationMethod;
import freemind.extensions.MindMapHook;
import freemind.extensions.MindMapHook.PluginBaseClassSearcher;
import freemind.extensions.ModeControllerHook;
import freemind.extensions.NodeHook;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.MindMapController;
import freemind.modes.mindmapmode.commands.MenuCommands.Command;
import freemind.modes.mindmapmode.hooks.MindMapHookFactory;

/**
 * Runs a menu command on the selected nodes, or on the map.
 * The command is constructed and called directly.
 * The hook name stays so a registration can still tell the commands apart.
 */
public class MenuCommandAction extends NodeHookAction {
	private static final long serialVersionUID = 1L;

	private final Command command;
	private final MindMapController controller;

	public MenuCommandAction(Command command, MindMapController controller) {
		super(command.label, controller);
		this.command = command;
		this.controller = controller;
	}

	public Command command() {
		return command;
	}

	public void actionPerformed(ActionEvent event) {
		if (command.modeController) {
			if (controller.getMap() == null) {
				return;
			}
			controller.invokeHook((ModeControllerHook) prepare(command.create.get()));
			return;
		}
		controller.getFrame().setWaitingCursor(true);
		try {
			invokeOnNodes();
		} finally {
			controller.getFrame().setWaitingCursor(false);
		}
	}

	private void invokeOnNodes() {
		MindMapNode focussed = controller.getSelected();
		List<MindMapNode> selecteds = controller.getSelecteds();
		HookInstanciationMethod method = command.instantiation;
		Collection<MindMapNode> destinations = method.getDestinationNodes(controller, focussed, selecteds);
		for (MindMapNode node : destinations) {
			NodeHook hook = (NodeHook) prepare(command.create.get());
			hook.setController(controller);
			hook.setMap(controller.getMap());
			node.invokeHook(hook);
		}
		if (controller.getViewAbstraction().getSelecteds().isEmpty()) {
			controller.select(focussed, selecteds);
		}
	}

	private MindMapHook prepare(MindMapHook hook) {
		hook.setProperties(command.properties);
		hook.setName(command.label);
		MindMapHookFactory factory = (MindMapHookFactory) controller.getHookFactory();
		hook.setPluginBaseClass(new PluginBaseClassSearcher() {
			public Object getPluginBaseObject() {
				return factory.registration(command.pluginLabel);
			}
		});
		return hook;
	}

	public boolean isEnabled(JMenuItem item, Action action) {
		if (controller.getMap() == null) {
			return false;
		}
		if (command.modeController) {
			return true;
		}
		if (controller.getView() == null) {
			return false;
		}
		Object base = registration();
		if (base instanceof MenuItemEnabledListener) {
			return ((MenuItemEnabledListener) base).isEnabled(item, action);
		}
		return true;
	}

	public boolean isSelected(JMenuItem item, Action action) {
		Object base = registration();
		if (base instanceof MenuItemSelectedListener) {
			return ((MenuItemSelectedListener) base).isSelected(item, action);
		}
		if (command.modeController) {
			return false;
		}
		MindMapNode focussed = controller.getSelected();
		List<MindMapNode> selecteds = controller.getSelecteds();
		HookInstanciationMethod method = command.instantiation;
		method.getDestinationNodes(controller, focussed, selecteds);
		MindMapNode center = method.getCenterNode(controller, focussed, selecteds);
		return method.isAlreadyPresent(command.label, center);
	}

	private Object registration() {
		MindMapHookFactory factory = (MindMapHookFactory) controller.getHookFactory();
		return factory.registration(command.pluginLabel);
	}
}
