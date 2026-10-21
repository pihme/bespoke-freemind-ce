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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import accessories.plugins.CreationModificationPlugin;
import freemind.modes.MindMapNode;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("CreationModificationPluginBehaviourTest")
class CreationModificationPluginBehaviourTest {

	// backwards compatibility
	@Test
	void updatesAndRemovalKeepTheCreationTooltip() throws Exception {
		Opened opened = MapFixture.open("Dated");
		MindMapNode child = opened.child("Dated");
		MindMapNode grand = opened.mode.addNewNode(child, 0, false);
		opened.mode.setNodeText(grand, "Grand");
		CreationModificationPlugin hook = new CreationModificationPlugin();
		hook.setName("created");
		hook.setController(opened.mode);
		hook.setMap(opened.mode.getMap());
		Properties properties = new Properties();
		properties.setProperty("tooltip_format", "created {0,date}");
		hook.setProperties(properties);
		hook.setNode(opened.root);

		hook.invoke(opened.root);
		hook.onUpdateNodeHook();
		hook.onUpdateChildrenHook(child);
		hook.onAddChildren(grand);
		hook.onNewChild(grand);

		String tip = opened.root.getToolTip().get("created");
		assertNotNull(tip);
		assertTrue(tip.startsWith("created "), tip);
		assertNotNull(grand.getToolTip().get("created"));

		hook.shutdownMapHook();

		assertNull(opened.root.getToolTip().get("created"));
		assertNull(child.getToolTip().get("created"));
		assertNull(grand.getToolTip().get("created"));
	}
}
