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

package tests.freemind.cov.commonactions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.main.FreeMind;
import freemind.main.Resources;
import freemind.modes.MindMapNode;
import freemind.modes.common.actions.FindAction;
import freemind.modes.common.actions.FindAction.FindNextAction;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Find selects a matching node, continues with find next, and can search notes. */
@DisplayName("FindActionBehaviourTest")
class FindActionBehaviourTest {

	@Test
	void prepareTextContentStripsHtmlAndHonoursCase() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Plain");
		FindAction find = new FindAction(opened.mode);
		String html = "<html><body>Hello <b>World</b> &#65;</body></html>";

		assertEquals("", find.prepareTextContent(false, null));
		assertEquals("", find.prepareTextContent(true, null));
		assertEquals("hello world a", find.prepareTextContent(false, html));
		assertEquals("Hello World A", find.prepareTextContent(true, html));
		assertEquals("abc", find.prepareTextContent(false, "AbC"));
		assertEquals("AbC", find.prepareTextContent(true, "AbC"));
	}

	@Test
	void findSelectsTheMatchingChildAndAMissingTermDoesNot() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("CamelCase", "Other");
		MindMapNode marked = opened.child("Other");
		marked.setText("<html><body>Hello <span class=\"secret\">World</span></body></html>");
		FindAction find = new FindAction(opened.mode);

		assertTrue(find.find(opened.root, List.of("camelcase"), false));
		assertEquals("CamelCase", opened.mode.getSelected().getText());
		assertTrue(find.find(opened.root, List.of("CamelCase"), true));
		assertEquals("CamelCase", opened.mode.getSelected().getText());
		assertFalse(find.find(opened.root, List.of("camelcase"), true));
		assertEquals("ROOT", opened.mode.getSelected().getText());

		assertTrue(marked.getText().contains("secret"));
		assertTrue(find.find(opened.root, List.of("world"), false));
		assertSame(marked, opened.mode.getSelected());
		assertFalse(find.find(opened.root, List.of("secret"), false));
		assertFalse(find.find(opened.root, List.of("missing"), false));
	}

	@Test
	void findNextSelectsTheSecondMatchingChild() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("First hit", "Second hit", "Other");
		FindAction find = new FindAction(opened.mode);

		assertFalse(find.findNext());
		assertTrue(find.find(opened.root, List.of("hit"), false));
		assertEquals("First hit", opened.mode.getSelected().getText());

		FindNextAction next = new FindNextAction(opened.mode, find);
		next.actionPerformed(new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "find_next"));
		assertEquals("Second hit", opened.mode.getSelected().getText());
		assertFalse(find.findNext());
	}

	@Test
	void aFoldedAncestorIsUnfoldedWhenTheMatchIsBelowIt() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Folder");
		MindMapNode folder = opened.child("Folder");
		MindMapNode nested = opened.mode.addNewNode(folder, 0, false);
		opened.mode.setNodeText(nested, "needle");
		opened.mode.setFolded(folder, true);
		assertTrue(folder.isFolded());
		FindAction find = new FindAction(opened.mode);

		assertTrue(find.find(opened.root, List.of("needle"), false));

		assertFalse(folder.isFolded());
		assertSame(nested, opened.mode.getSelected());
		assertFalse(find.findNext());
		assertTrue(folder.isFolded());
	}

	@Test
	void searchInNotesMatchesTheNoteAndNotTheNodeText() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Visible");
		MindMapNode child = opened.child("Visible");
		opened.mode.setNoteText(child, "hidden token");
		assertFalse(child.getText().toLowerCase().contains("hidden"));
		assertTrue(child.getNoteText().toLowerCase().contains("hidden"));
		FindAction find = new FindAction(opened.mode);
		Properties properties = Resources.getInstance().getProperties();
		String key = FreeMind.RESOURCES_SEARCH_IN_NOTES_TOO;
		String previous = properties.getProperty(key);
		try {
			properties.setProperty(key, "false");
			assertFalse(find.find(opened.root, List.of("hidden"), false));
			properties.setProperty(key, "true");
			assertTrue(find.find(opened.root, List.of("hidden"), false));
			assertSame(child, opened.mode.getSelected());
		} finally {
			if (previous == null) {
				properties.remove(key);
			} else {
				properties.setProperty(key, previous);
			}
		}
	}
}
