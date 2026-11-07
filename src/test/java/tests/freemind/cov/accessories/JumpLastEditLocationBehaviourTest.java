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

import java.util.Properties;

import javax.swing.JMenuItem;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import accessories.plugins.JumpLastEditLocation;
import freemind.controller.actions.generated.instance.FoldAction;
import freemind.controller.actions.generated.instance.HookNodeAction;
import freemind.controller.actions.generated.instance.NewNodeAction;
import freemind.extensions.MindMapHook.PluginBaseClassSearcher;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.actions.NodeHookAction;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("JumpLastEditLocationBehaviourTest")
class JumpLastEditLocationBehaviourTest {

	// backwards compatibility
	@Test
	void invokeSelectsThePreviousEditAndSkipsThisHook() throws Exception {
		Opened opened = MapFixture.open("Alpha", "Beta");
		MindMapNode alpha = opened.child("Alpha");
		MindMapNode beta = opened.child("Beta");
		JumpLastEditLocation.JumpLastEditLocationRegistration registration =
				new JumpLastEditLocation.JumpLastEditLocationRegistration(opened.mode, opened.mode.getMap());
		String alphaId = opened.mode.getNodeID(alpha);
		String betaId = opened.mode.getNodeID(beta);
		NewNodeAction first = new NewNodeAction();
		first.setNewId(alphaId);
		registration.executeAction(first);
		HookNodeAction self = new HookNodeAction();
		self.setHookName("accessories/plugins/JumpLastEditLocation.properties");
		self.setNode(betaId);
		registration.executeAction(self);
		FoldAction fold = new FoldAction();
		fold.setNode(betaId);
		registration.executeAction(fold);
		NewNodeAction second = new NewNodeAction();
		second.setNewId(betaId);
		registration.executeAction(second);
		registration.executeAction(second);
		assertEquals("Alpha", registration.getLastEditLocation(beta).getText());
		assertEquals("Beta", registration.getLastEditLocation(opened.root).getText());
		JumpLastEditLocation.JumpLastEditLocationRegistration empty =
				new JumpLastEditLocation.JumpLastEditLocationRegistration(opened.mode, opened.mode.getMap());
		NewNodeAction missing = new NewNodeAction();
		missing.setNewId("missing-node");
		empty.executeAction(missing);
		assertNull(empty.getLastEditLocation(opened.root));

		select(opened, beta);
		JumpLastEditLocation hook = new JumpLastEditLocation();
		hook.setController(opened.mode);
		hook.setMap(opened.mode.getMap());
		hook.setProperties(new Properties());
		hook.setPluginBaseClass(new PluginBaseClassSearcher() {
			public Object getPluginBaseObject() {
				return registration;
			}
		});
		hook.invoke(beta);

		assertEquals(alpha, opened.mode.getSelected());
		NodeHookAction action = new NodeHookAction("accessories/plugins/JumpLastEditLocation.properties",
				opened.mode);
		assertTrue(registration.isEnabled(new JMenuItem(), action));
		registration.deRegister();
		assertFalse(registration.isEnabled(new JMenuItem(), new NodeHookAction("other", opened.mode))
				&& registration.getLastEditLocation(alpha) == null);
	}

	private static void select(Opened opened, MindMapNode node) {
		opened.view.getRoot().updateAll();
		opened.view.selectAsTheOnlyOneSelected(opened.view.getNodeView(node));
	}
}
