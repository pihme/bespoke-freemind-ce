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

package tests.freemind.cov.mindmapmode;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.controller.Controller;
import freemind.modes.mindmapmode.MenuActions;
import freemind.modes.mindmapmode.MindMapController;

/** A name in the menu document resolves, except the names that already have no action. */
@DisplayName("MenuActionsBehaviourTest")
class MenuActionsBehaviourTest {

	private static final Set<String> KNOWN_MISSES = new LinkedHashSet<>(List.of(
			"remove",
			"showAllAttributes",
			"showSelectedAttributes",
			"hideAllAttributes",
			"showAttributeManagerAction",
			"assignAttributes",
			"editAttributes"));

	@Test
	void everyMenuNameResolvesExceptTheKnownMisses() throws Exception {
		String xml = new String(
				MindMapController.class.getResourceAsStream("/mindmap_menus.xml").readAllBytes(),
				StandardCharsets.UTF_8);
		Matcher matcher = Pattern.compile("field=\"([^\"]+)\"").matcher(xml);
		List<String> names = new ArrayList<>();
		while (matcher.find()) {
			names.add(matcher.group(1));
		}
		assertEquals(KNOWN_MISSES, MenuActions.unresolved(MindMapController.class, Controller.class, names));
	}
}
