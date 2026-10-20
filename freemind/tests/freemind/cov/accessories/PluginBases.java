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

import java.lang.reflect.Field;
import java.util.Map;

import freemind.extensions.HookFactoryAdapter;
import freemind.extensions.HookRegistration;
import tests.freemind.cov.MapFixture.Opened;

/**
 * Opening a map through {@code MapFixture} does not run plugin registration.
 * Tests that need a plugin base install it here.
 */
final class PluginBases {

	private PluginBases() {
	}

	static void install(Opened opened, String pluginLabel, HookRegistration registration) throws Exception {
		Field field = HookFactoryAdapter.class.getDeclaredField("allRegistrationInstances");
		field.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<String, HookRegistration> instances = (Map<String, HookRegistration>) field
				.get(opened.mode.getHookFactory());
		instances.put(pluginLabel, registration);
	}
}
