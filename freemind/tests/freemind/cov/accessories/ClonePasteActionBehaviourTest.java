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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.HeadlessException;
import java.awt.datatransfer.Transferable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JMenuItem;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import accessories.plugins.ClonePasteAction;
import accessories.plugins.ClonePlugin;
import freemind.controller.actions.generated.instance.FoldAction;
import freemind.controller.actions.generated.instance.NodeListMember;
import freemind.extensions.MindMapHook.PluginBaseClassSearcher;
import freemind.main.FreeMind;
import freemind.main.Tools;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.MindMapNodeModel;
import freemind.modes.mindmapmode.actions.NodeHookAction;
import freemind.view.mindmapview.NodeView;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("ClonePasteActionBehaviourTest")
class ClonePasteActionBehaviourTest {

	// backwards compatibility
	@Test
	void pastingACopiedNodeAddsACloneAndTheRootIsRejected() throws Exception {
		Opened opened = MapFixture.open("Source", "Shelf");
		opened.frame.setProperty(FreeMind.RESOURCES_COMPLETE_CLONING, "true");
		MindMapNode source = opened.child("Source");
		MindMapNode shelf = opened.child("Shelf");
		select(opened, source);
		registration(opened);
		int before = opened.root.getChildCount();
		Pasting action = new Pasting();
		action.nodes.add(source);
		action.setController(opened.mode);
		action.setMap(opened.mode.getMap());
		ClonePasteAction.Registration registration = registration(opened);
		action.setPluginBaseClass(new PluginBaseClassSearcher() {
			public Object getPluginBaseObject() {
				return registration;
			}
		});
		assertEquals(registration, action.registration());

		action.invoke(shelf);

		assertTrue(opened.root.getChildCount() >= before);
		assertNotNull(ClonePlugin.getHook(source));
		boolean pasted = false;
		for (int i = 0; i < shelf.getChildCount(); i++) {
			if ("Source".equals(((MindMapNode) shelf.getChildAt(i)).getText())) {
				pasted = true;
			}
		}
		assertTrue(pasted);

		Pasting rooted = new Pasting();
		rooted.nodes.add(opened.root);
		rooted.setController(opened.mode);
		rooted.setMap(opened.mode.getMap());
		int rootChildren = opened.root.getChildCount();
		assertThrows(HeadlessException.class, () -> rooted.invoke(shelf));
		assertEquals(rootChildren, opened.root.getChildCount());

		Transferable copy = opened.mode.copy(source, true);
		int shelfChildren = shelf.getChildCount();
		action.addNewClone(source, shelf, copy);
		assertTrue(shelf.getChildCount() > shelfChildren);
	}

	// backwards compatibility
	@Test
	void observersIconsAndAMissingChildIndexAreReported() throws Exception {
		Opened opened = MapFixture.open("Alpha", "Beta");
		MindMapNode alpha = opened.child("Alpha");
		MindMapNode beta = opened.child("Beta");
		MindMapNode kept = new MindMapNodeModel("Kept", opened.mode.getMap());
		alpha.insert(kept, 0);
		MindMapNode only = new MindMapNodeModel("Only", opened.mode.getMap());
		alpha.insert(only, 1);
		beta.insert(new MindMapNodeModel("Kept", opened.mode.getMap()), 0);
		registration(opened);
		Properties properties = new Properties();
		properties.setProperty(ClonePlugin.XML_STORAGE_CLONE_ID, "CLONE_paste");
		properties.setProperty(ClonePlugin.XML_STORAGE_CLONE_ITSELF, ClonePlugin.CLONE_ITSELF_FALSE);
		properties.setProperty(ClonePlugin.XML_STORAGE_CLONES, opened.mode.getNodeID(beta));
		opened.mode.addHook(alpha, Collections.singletonList(alpha), ClonePlugin.PLUGIN_LABEL, properties);
		ClonePlugin.getHook(alpha).processUnfinishedLinks();
		ClonePasteAction.Registration registration = (ClonePasteAction.Registration) opened.mode.getHookFactory()
				.getPluginBaseClass(ClonePlugin.PLUGIN_LABEL);

		ClonePasteAction.CloneProperties observed = new ClonePasteAction.CloneProperties();
		Vector<Boolean> seen = new Vector<>();
		ClonePasteAction.ClonePropertiesObserver observer = properties1 -> seen.add(properties1.isCloneItself());
		observed.registerObserver(observer);
		observed.setCloneItself(true);
		assertEquals(List.of(Boolean.TRUE), seen);
		observed.deregisterObserver(observer);
		observed.setCloneItself(false);
		assertEquals(List.of(Boolean.TRUE), seen);

		registration.onSaveNode(alpha);
		assertEquals("Alpha", alpha.getText());

		Field show = ClonePasteAction.Registration.class.getDeclaredField("sShowIcon");
		show.setAccessible(true);
		boolean previous = show.getBoolean(null);
		try {
			beta.setStateIcon(ClonePlugin.PLUGIN_LABEL, null);
			show.setBoolean(null, false);
			registration.markShadowNode(alpha, true);
			assertFalse(beta.getStateIcons().containsKey(ClonePlugin.PLUGIN_LABEL));
			show.setBoolean(null, true);
			registration.markShadowNode(alpha, true);
			assertTrue(beta.getStateIcons().containsKey(ClonePlugin.PLUGIN_LABEL));
			registration.onFocusNode(null);
			assertTrue(beta.getStateIcons().containsKey(ClonePlugin.PLUGIN_LABEL));
			opened.view.getRoot().updateAll();
			freemind.view.mindmapview.NodeView alphaView = opened.view.getNodeView(alpha);
			assertNotNull(alphaView);
			registration.markShadowNode(alphaView, false);
			assertTrue(beta.getStateIcons().containsKey(ClonePlugin.PLUGIN_LABEL));
			kept.setStateIcon(ClonePlugin.PLUGIN_LABEL, new javax.swing.ImageIcon());
			Field markedField = registration.getClass().getDeclaredField("mLastMarkedNodeViews");
			markedField.setAccessible(true);
			@SuppressWarnings("unchecked")
			Vector<MindMapNode> marked = (Vector<MindMapNode>) markedField.get(registration);
			marked.add(kept);
			registration.markShadowNode(alphaView, false);
			assertFalse(kept.getStateIcons().containsKey(ClonePlugin.PLUGIN_LABEL));
			assertTrue(beta.getStateIcons().containsKey(ClonePlugin.PLUGIN_LABEL));
		} finally {
			show.setBoolean(null, previous);
		}

		List<Tools.MindMapNodePair> missing = registration.getCorrespondingNodes(only, false);
		assertTrue(missing.isEmpty());

		NodeListMember member = new NodeListMember();
		member.setNode(opened.mode.getNodeID(kept));
		FoldAction fold = new FoldAction();
		fold.setNode(opened.mode.getNodeID(kept));
		Tools.MindMapNodePair pair = new Tools.MindMapNodePair((MindMapNode) beta.getChildAt(0), beta);
		registration.changeNodeListMember(pair, fold, member);
		assertEquals(opened.mode.getNodeID((MindMapNode) beta.getChildAt(0)), member.getNode());

		Method printId = ClonePasteAction.Registration.class.getDeclaredMethod("printNodeId", MindMapNode.class);
		printId.setAccessible(true);
		assertTrue(printId.invoke(registration, alpha).toString().contains("Alpha"));
		assertTrue(printId.invoke(registration, new Object[] { null }).toString().startsWith("NOT FOUND"));
		Method printIds = ClonePasteAction.Registration.class.getDeclaredMethod("printNodeIds", HashSet.class);
		printIds.setAccessible(true);
		HashSet<MindMapNode> clones = new HashSet<>();
		clones.add(alpha);
		clones.add(null);
		String printed = printIds.invoke(registration, clones).toString();
		assertTrue(printed.contains("Alpha"), printed);
		assertTrue(printed.contains("NOT FOUND"), printed);

		select(opened, alpha);
		assertTrue(registration.isEnabled(new JMenuItem(), new NodeHookAction("other", opened.mode)));
		Pasting empty = new Pasting();
		empty.setController(opened.mode);
		empty.setMap(opened.mode.getMap());
		assertTrue(empty.getMindMapNodes().isEmpty());
	}

	private static ClonePasteAction.Registration registration(Opened opened) throws Exception {
		ClonePasteAction.Registration registration = new ClonePasteAction.Registration(opened.mode,
				opened.mode.getMap());
		PluginBases.install(opened, "accessories/plugins/ClonePlugin", registration);
		registration.register();
		return registration;
	}

	private static void select(Opened opened, MindMapNode node) {
		opened.view.getRoot().updateAll();
		NodeView nodeView = opened.view.getNodeView(node);
		opened.view.selectAsTheOnlyOneSelected(nodeView);
	}

	private static final class Pasting extends ClonePasteAction {
		private final Vector<MindMapNode> nodes = new Vector<>();

		@Override
		public Vector<MindMapNode> getMindMapNodes() {
			return nodes;
		}

		ClonePasteAction.Registration registration() {
			return getRegistration();
		}
	}
}
