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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Properties;

import javax.swing.JMenuItem;

import org.junit.jupiter.api.Test;

import accessories.plugins.ClonePasteAction;
import accessories.plugins.ClonePlugin;
import accessories.plugins.DeactivateCloneAction;
import freemind.extensions.MindMapHook.PluginBaseClassSearcher;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.actions.NodeHookAction;
import freemind.view.mindmapview.NodeView;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

class DeactivateCloneActionBehaviourTest {

	// backwards compatibility
	@Test
	void deactivatingTheSelectedCloneRemovesTheHook() throws Exception {
		Opened opened = MapFixture.open("Alpha", "Beta");
		MindMapNode alpha = opened.child("Alpha");
		select(opened, alpha);
		ClonePasteAction.Registration registration = install(opened);
		Properties properties = new Properties();
		properties.setProperty(ClonePlugin.XML_STORAGE_CLONE_ID, "CLONE_off");
		properties.setProperty(ClonePlugin.XML_STORAGE_CLONE_ITSELF, ClonePlugin.CLONE_ITSELF_FALSE);
		opened.mode.addHook(alpha, Collections.singletonList(alpha), ClonePlugin.PLUGIN_LABEL, properties);
		assertTrue(ClonePlugin.getHook(alpha) != null);
		Exposed action = new Exposed();
		action.setController(opened.mode);
		action.setMap(opened.mode.getMap());
		action.setPluginBaseClass(new PluginBaseClassSearcher() {
			public Object getPluginBaseObject() {
				return registration;
			}
		});
		assertSame(registration, action.registration());
		assertTrue(action.isEnabled(new JMenuItem(), new NodeHookAction("other", opened.mode)));

		action.invoke(alpha);

		assertNull(ClonePlugin.getHook(alpha));
		assertFalse(action.isEnabled(new JMenuItem(), new NodeHookAction("other", opened.mode)));
	}

	// backwards compatibility
	@Test
	void aNodeWithoutACloneHookLeavesTheNodeUnchanged() throws Exception {
		Opened opened = MapFixture.open("Plain");
		MindMapNode plain = opened.child("Plain");
		select(opened, plain);
		ClonePasteAction.Registration registration = install(opened);
		Exposed action = new Exposed();
		action.setController(opened.mode);
		action.setMap(opened.mode.getMap());
		action.setPluginBaseClass(new PluginBaseClassSearcher() {
			public Object getPluginBaseObject() {
				return registration;
			}
		});

		assertFalse(action.isEnabled(new JMenuItem(), new NodeHookAction("other", opened.mode)));
		action.invoke(plain);
		assertNull(ClonePlugin.getHook(plain));
		assertEqualsText(plain, "Plain");
	}

	private static ClonePasteAction.Registration install(Opened opened) throws Exception {
		ClonePasteAction.Registration registration = new ClonePasteAction.Registration(opened.mode,
				opened.mode.getMap());
		PluginBases.install(opened, "accessories/plugins/ClonePlugin", registration);
		registration.register();
		return registration;
	}

	private static void assertEqualsText(MindMapNode node, String text) {
		org.junit.jupiter.api.Assertions.assertEquals(text, node.getText());
	}

	private static final class Exposed extends DeactivateCloneAction {
		ClonePasteAction.Registration registration() {
			return getRegistration();
		}
	}

	private static void select(Opened opened, MindMapNode node) {
		opened.view.getRoot().updateAll();
		NodeView nodeView = opened.view.getNodeView(node);
		opened.view.selectAsTheOnlyOneSelected(nodeView);
	}
}
