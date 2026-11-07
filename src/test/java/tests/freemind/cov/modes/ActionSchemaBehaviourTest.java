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

package tests.freemind.cov.modes;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The action schema keeps the elements handwritten code uses. */
@DisplayName("ActionSchemaBehaviourTest")
class ActionSchemaBehaviourTest {

	@Test
	void unreadCollaborationSearchAndClasspathTypesAreGone() throws Exception {
		String schema = Files.readString(Path.of("src/main/schema/freemind_actions.xsd"));
		assertFalse(schema.contains("collaboration_"));
		assertFalse(schema.contains("searchresults"));
		assertFalse(schema.contains("reversegeocode"));
		assertFalse(schema.contains("plugin_classpath"));
		assertFalse(schema.contains("result_base"));
		assertFalse(schema.contains("addressparts"));
		assertFalse(schema.contains("name=\"place\""));
		assertTrue(schema.contains("underlined_node_action"));
		assertTrue(schema.contains("place_node_xml_action"));
		assertTrue(schema.contains("calendar_markings"));
	}
}
