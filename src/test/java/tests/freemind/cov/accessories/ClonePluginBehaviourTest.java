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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.HeadlessException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.mindmapmode.commands.ClonePasteAction;
import freemind.modes.mindmapmode.features.Clone;
import freemind.extensions.MindMapHook.PluginBaseClassSearcher;
import freemind.main.Tools;
import freemind.main.XMLElement;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.MindMapNodeModel;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("ClonePluginBehaviourTest")
class ClonePluginBehaviourTest {

	// backwards compatibility
	@Test
	void registeringACloneLinksThePartnerAndRemovingTheLastCloneDropsTheHook() throws Exception {
		Opened opened = MapFixture.open("Alpha", "Beta");
		MindMapNode alpha = opened.child("Alpha");
		MindMapNode beta = opened.child("Beta");
		install(opened);
		opened.mode.addHook(alpha, Collections.singletonList(alpha), Clone.PLUGIN_LABEL,
				properties(opened, beta));
		Clone hook = Clone.getHook(alpha);
		assertEquals("CLONE_link", hook.getCloneId());
		assertNull(Clone.getHook(null));

		hook.processUnfinishedLinks();
		assertNotNull(Clone.getHook(beta));
		assertTrue(hook.getCloneIdsAsString().contains(opened.mode.getNodeID(beta)), hook.getCloneIdsAsString());

		Method itself = Clone.class.getDeclaredMethod("getCloneItselfValue");
		itself.setAccessible(true);
		assertEquals(Clone.CLONE_ITSELF_FALSE, itself.invoke(hook));
		Method printId = Clone.class.getDeclaredMethod("printNodeId", MindMapNode.class);
		printId.setAccessible(true);
		assertTrue(printId.invoke(hook, alpha).toString().contains("Alpha"));
		assertTrue(printId.invoke(hook, new Object[] { null }).toString().startsWith("NOT FOUND"));
		Method printIds = Clone.class.getDeclaredMethod("printNodeIds", java.util.Collection.class);
		printIds.setAccessible(true);
		String printed = printIds.invoke(hook, java.util.List.of(alpha)).toString();
		assertTrue(printed.contains("Alpha"), printed);

		hook.onPreDeleteNode(alpha);
		hook.onPostDeleteNode(alpha, opened.root);
		assertEquals("CLONE_link", hook.getCloneId());

		Clone partner = Clone.getHook(beta);
		partner.removeClone(alpha);
		assertNull(Clone.getHook(beta));
		assertNull(Clone.getHook(alpha));
		assertEquals("CLONE_link", hook.getCloneId());
	}

	// backwards compatibility
	@Test
	void aDescendantCloneDisablesThePluginAndShutdownForgetsTheClone() throws Exception {
		Opened opened = MapFixture.open("Keeper");
		MindMapNode keeper = opened.child("Keeper");
		MindMapNode nested = new MindMapNodeModel("Nested", opened.mode.getMap());
		keeper.insert(nested, 0);
		ClonePasteAction.Registration registration = install(opened);
		Clone plugin = manual(opened, nested, registration);
		XMLElement raw = stored(opened.mode.getNodeID(keeper));
		plugin.loadFrom(raw);
		assertThrows(HeadlessException.class, () -> plugin.invoke(nested));
		Field disabled = Clone.class.getDeclaredField("mDisabled");
		disabled.setAccessible(true);
		assertTrue(disabled.getBoolean(plugin));
		assertNull(Clone.getHook(nested));

		opened.mode.addHook(keeper, Collections.singletonList(keeper), Clone.PLUGIN_LABEL,
				properties(opened, keeper));
		Clone live = Clone.getHook(keeper);
		live.addClone(nested);
		assertTrue(live.getCloneIdsAsString().contains(opened.mode.getNodeID(nested)));
		live.onCreateNodeHook(opened.root);
		assertEquals("CLONE_link", live.getCloneId());

		live.shutdownMapHook();
		List<Tools.MindMapNodePair> pairs = registration.getCorrespondingNodes(keeper, false);
		assertTrue(pairs.isEmpty());
		assertFalse(keeper.getStateIcons().containsKey(Clone.PLUGIN_LABEL));
	}

	private static ClonePasteAction.Registration install(Opened opened) throws Exception {
		ClonePasteAction.Registration registration = new ClonePasteAction.Registration(opened.mode,
				opened.mode.getMap());
		PluginBases.install(opened, "accessories/plugins/ClonePlugin", registration);
		registration.register();
		return registration;
	}

	private static Clone manual(Opened opened, MindMapNode node,
			ClonePasteAction.Registration registration) {
		Clone plugin = new Clone();
		plugin.setController(opened.mode);
		plugin.setMap(opened.mode.getMap());
		plugin.setNode(node);
		plugin.setName(Clone.PLUGIN_LABEL);
		plugin.setPluginBaseClass(new PluginBaseClassSearcher() {
			public Object getPluginBaseObject() {
				return registration;
			}
		});
		return plugin;
	}

	private static XMLElement stored(String cloneId) {
		XMLElement raw = new XMLElement();
		XMLElement parameters = new XMLElement();
		parameters.setName("Parameters");
		parameters.setAttribute(Clone.XML_STORAGE_CLONES, cloneId);
		parameters.setAttribute(Clone.XML_STORAGE_CLONE_ID, "CLONE_chain");
		parameters.setAttribute(Clone.XML_STORAGE_CLONE_ITSELF, Clone.CLONE_ITSELF_FALSE);
		raw.addChild(parameters);
		return raw;
	}

	private static Properties properties(Opened opened, MindMapNode other) {
		Properties properties = new Properties();
		properties.setProperty(Clone.XML_STORAGE_CLONE_ID, "CLONE_link");
		properties.setProperty(Clone.XML_STORAGE_CLONE_ITSELF, Clone.CLONE_ITSELF_FALSE);
		properties.setProperty(Clone.XML_STORAGE_CLONES, opened.mode.getNodeID(other));
		return properties;
	}
}
