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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.EventQueue;
import java.util.Collections;
import java.util.Properties;
import java.util.Vector;

import org.junit.jupiter.api.Test;

import accessories.plugins.ClonePasteAction;
import accessories.plugins.ClonePlugin;
import accessories.plugins.ShowCloneNodes;
import freemind.modes.MindMapNode;
import freemind.view.mindmapview.NodeView;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

class ShowCloneNodesBehaviourTest {

	// backwards compatibility
	@Test
	void showingClonesSelectsTheOtherClone() throws Exception {
		Opened opened = MapFixture.open("Alpha", "Beta");
		MindMapNode alpha = opened.child("Alpha");
		MindMapNode beta = opened.child("Beta");
		ClonePasteAction.Registration registration = new ClonePasteAction.Registration(opened.mode,
				opened.mode.getMap());
		PluginBases.install(opened, "accessories/plugins/ClonePlugin", registration);
		registration.register();
		Properties properties = cloneProperties(opened, beta);
		opened.mode.addHook(alpha, Collections.singletonList(alpha), ClonePlugin.PLUGIN_LABEL, properties);
		ClonePlugin.getHook(alpha).processUnfinishedLinks();
		select(opened, alpha);
		assertEquals("Alpha", opened.mode.getSelected().getText());

		ShowCloneNodes hook = new ShowCloneNodes();
		hook.setController(opened.mode);
		hook.setMap(opened.mode.getMap());
		hook.invoke(alpha);
		EventQueue.invokeAndWait(() -> {
		});

		assertEquals("Beta", opened.mode.getSelected().getText());
	}

	// backwards compatibility
	@Test
	void aNodeWithoutClonesContributesNothing() throws Exception {
		Opened opened = MapFixture.open("Lonely");
		MindMapNode lonely = opened.child("Lonely");
		Listing listing = new Listing();
		listing.setController(opened.mode);
		listing.setMap(opened.mode.getMap());
		Vector<MindMapNode> found = new Vector<>();
		listing.collect(found, lonely);
		assertTrue(found.isEmpty());

		select(opened, lonely);
		listing.invoke(lonely);
		EventQueue.invokeAndWait(() -> {
		});
		assertEquals("Lonely", opened.mode.getSelected().getText());
	}

	private static Properties cloneProperties(Opened opened, MindMapNode other) {
		Properties properties = new Properties();
		properties.setProperty(ClonePlugin.XML_STORAGE_CLONE_ID, "CLONE_show");
		properties.setProperty(ClonePlugin.XML_STORAGE_CLONE_ITSELF, ClonePlugin.CLONE_ITSELF_FALSE);
		properties.setProperty(ClonePlugin.XML_STORAGE_CLONES, opened.mode.getNodeID(other));
		return properties;
	}

	private static void select(Opened opened, MindMapNode node) {
		opened.view.getRoot().updateAll();
		NodeView nodeView = opened.view.getNodeView(node);
		opened.view.selectAsTheOnlyOneSelected(nodeView);
	}

	private static final class Listing extends ShowCloneNodes {
		void collect(Vector<MindMapNode> into, MindMapNode node) {
			addClonesToList(into, node);
		}
	}
}
