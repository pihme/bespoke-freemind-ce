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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import accessories.plugins.SaveAll;
import freemind.view.MapModule;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("SaveAllBehaviourTest")
class SaveAllBehaviourTest {

	// backwards compatibility
	@Test
	void aSavedMapIsWrittenAndTheOriginalModuleStaysCurrent() throws Exception {
		Opened opened = MapFixture.open("Child");
		opened.mode.getMap().setSaved(true);
		String moduleName = opened.app.getMapModule().toString();
		SaveAll hook = new SaveAll();
		hook.setController(opened.mode);
		hook.setProperties(new Properties());

		hook.startupMapHook();

		assertTrue(opened.mode.getMap().isSaved());
		assertEquals(moduleName, opened.app.getMapModule().toString());
		Method modules = SaveAll.class.getDeclaredMethod("getMapModules");
		modules.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<String, MapModule> found = (Map<String, MapModule>) modules.invoke(hook);
		assertSame(opened.app.getMapModule(), found.get(moduleName));
	}
}
