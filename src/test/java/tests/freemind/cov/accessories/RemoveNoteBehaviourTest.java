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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;

import javax.swing.JMenuItem;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import accessories.plugins.RemoveNote;
import freemind.main.FreeMind;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.actions.NodeHookAction;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("RemoveNoteBehaviourTest")
class RemoveNoteBehaviourTest {

	// backwards compatibility
	@Test
	void theMenuIsEnabledOnlyWhenASelectedNodeHasANote() throws Exception {
		Opened opened = MapFixture.open("Noted", "Plain");
		MindMapNode noted = opened.child("Noted");
		opened.mode.setNoteText(noted, "remember");
		select(opened, noted);
		RemoveNote.Registration registration = new RemoveNote.Registration(opened.mode, opened.mode.getMap());
		NodeHookAction action = new NodeHookAction("accessories/plugins/RemoveNote.properties", opened.mode);

		assertTrue(registration.isEnabled(new JMenuItem(), action));
		select(opened, opened.child("Plain"));
		assertFalse(registration.isEnabled(new JMenuItem(), action));
		registration.deRegister();
		registration.register();
		assertFalse(registration.isEnabled(new JMenuItem(), action));
	}

	// backwards compatibility
	@Test
	void confirmingTheRemovalClearsTheSelectedNote() throws Exception {
		Opened opened = MapFixture.open("Noted", "Other");
		opened.frame.setProperty(FreeMind.RESOURCES_REMOVE_NOTES_WITHOUT_QUESTION, "false");
		MindMapNode noted = opened.child("Noted");
		MindMapNode other = opened.child("Other");
		opened.mode.setNoteText(noted, "remember");
		opened.mode.setNoteText(other, "also");
		select(opened, noted);
		RemoveNote hook = new RemoveNote();
		hook.setController(opened.mode);
		hook.setMap(opened.mode.getMap());
		hook.setProperties(new Properties());
		hook.invoke(noted);
		assertEquals("remember", noted.getNoteText());

		opened.frame.setProperty(FreeMind.RESOURCES_REMOVE_NOTES_WITHOUT_QUESTION, "true");
		select(opened, opened.root);
		java.lang.reflect.Method removeNote = RemoveNote.class.getDeclaredMethod("removeNote", MindMapNode.class);
		removeNote.setAccessible(true);
		removeNote.invoke(hook, noted);
		assertNull(noted.getNoteText());
		assertEquals("also", other.getNoteText());
	}

	private static void select(Opened opened, MindMapNode node) {
		opened.view.getRoot().updateAll();
		opened.view.selectAsTheOnlyOneSelected(opened.view.getNodeView(node));
	}
}
