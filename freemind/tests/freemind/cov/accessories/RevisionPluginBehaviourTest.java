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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.Properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import accessories.plugins.RevisionPlugin;
import freemind.modes.MindMapNode;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("RevisionPluginBehaviourTest")
class RevisionPluginBehaviourTest {

	// backwards compatibility
	@Test
	void shutdownStopsColoringLaterEdits() throws Exception {
		Field alreadyUsed = RevisionPlugin.class.getDeclaredField("alreadyUsed");
		alreadyUsed.setAccessible(true);
		alreadyUsed.setBoolean(null, false);
		Opened opened = MapFixture.open("Edited", "Later");
		RevisionPlugin hook = new RevisionPlugin();
		hook.setController(opened.mode);
		hook.setMap(opened.mode.getMap());
		hook.setProperties(new Properties());
		try {
			hook.invoke(opened.root);
			opened.mode.setNodeText(opened.child("Edited"), "Changed");
			assertEquals(Color.YELLOW, opened.child("Changed").getBackgroundColor());

			hook.shutdownMapHook();
			MindMapNode later = opened.child("Later");
			opened.mode.setNodeText(later, "Untouched");

			assertEquals("Untouched", later.getText());
			assertNull(later.getBackgroundColor());
		} finally {
			alreadyUsed.setBoolean(null, false);
		}
	}
}
