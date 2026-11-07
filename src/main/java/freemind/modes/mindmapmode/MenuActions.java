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

package freemind.modes.mindmapmode;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.Action;

import freemind.main.Tools;

/**
 * A menu-document name to the action it names.
 * A name with no public field is absent. The caller logs that and skips it.
 */
public final class MenuActions {

	private MenuActions() {
	}

	public static Action action(Object mode, Object controller, String name)
			throws IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException {
		return (Action) Tools.getField(new Object[] { mode, controller }, name);
	}

	public static boolean resolves(Class<?> modeType, Class<?> controllerType, String name) {
		return hasPublicField(modeType, name) || hasPublicField(controllerType, name);
	}

	public static Set<String> unresolved(Class<?> modeType, Class<?> controllerType, Collection<String> names) {
		Set<String> missing = new LinkedHashSet<>();
		for (String name : names) {
			if (!resolves(modeType, controllerType, name)) {
				missing.add(name);
			}
		}
		return missing;
	}

	private static boolean hasPublicField(Class<?> type, String name) {
		Class<?> cursor = type;
		while (cursor != null) {
			try {
				Field field = cursor.getDeclaredField(name);
				if (Modifier.isPublic(field.getModifiers())) {
					return true;
				}
			} catch (NoSuchFieldException e) {
				// Look on the superclass.
			}
			cursor = cursor.getSuperclass();
		}
		return false;
	}
}
