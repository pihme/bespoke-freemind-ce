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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import accessories.plugins.NodeAttributeTable;
import accessories.plugins.NodeAttributeTableRegistration;
import freemind.extensions.MindMapHook.PluginBaseClassSearcher;
import freemind.main.FreeMind;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("NodeAttributeTableBehaviourTest")
class NodeAttributeTableBehaviourTest {

	// backwards compatibility
	@Test
	void theCommandShowsAndHidesTheAttributeSplit() throws Exception {
		Opened opened = MapFixture.open("Child");
		opened.frame.setProperty(FreeMind.RESOURCES_SHOW_ATTRIBUTE_PANE, "false");
		opened.frame.setProperty("attribute_table_properties", "<attribute_table_properties/>");
		NodeAttributeTableRegistration registration = new NodeAttributeTableRegistration(opened.mode,
				opened.mode.getMap());
		registration.register();
		try {
			assertFalse(registration.getSplitPaneVisible());
			hook(opened, registration, "show").startupMapHook();
			assertTrue(registration.getSplitPaneVisible());
			assertEquals("true", opened.frame.getProperty(FreeMind.RESOURCES_SHOW_ATTRIBUTE_PANE));

			hook(opened, registration, "hide").startupMapHook();
			assertFalse(registration.getSplitPaneVisible());
			assertEquals("false", opened.frame.getProperty(FreeMind.RESOURCES_SHOW_ATTRIBUTE_PANE));

			hook(opened, registration, "jump").startupMapHook();
			assertTrue(registration.getSplitPaneVisible());
			assertEquals("true", opened.frame.getProperty(FreeMind.RESOURCES_SHOW_ATTRIBUTE_PANE));
			assertEquals("Child", opened.child("Child").getText());
		} finally {
			registration.deRegister();
		}
	}

	private static NodeAttributeTable hook(Opened opened, NodeAttributeTableRegistration registration, String command) {
		NodeAttributeTable hook = new NodeAttributeTable();
		hook.setController(opened.mode);
		hook.setMap(opened.mode.getMap());
		Properties properties = new Properties();
		properties.setProperty("command", command);
		hook.setProperties(properties);
		hook.setPluginBaseClass(new PluginBaseClassSearcher() {
			public Object getPluginBaseObject() {
				return registration;
			}
		});
		return hook;
	}
}
