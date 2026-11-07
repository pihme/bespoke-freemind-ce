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

package tests.freemind.cov.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.actions.ImportExplorerFavoritesAction;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** A favorites chooser does not import, and a plain file finds no favorites. */
@DisplayName("ImportExplorerFavoritesActionBehaviourTest")
class ImportExplorerFavoritesActionBehaviourTest {

	@Test
	void theChooserAddsNoNodes() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Marks");
		opened.mode.select(opened.child("Marks"), List.of(opened.child("Marks")));
		assertThrows(HeadlessException.class,
				() -> new ImportExplorerFavoritesAction(opened.mode).actionPerformed(
						new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "favorites")));
		assertEquals(List.of("Marks"), MapFixture.childTexts(opened.root));
	}

	@Test
	void aPlainFileFindsNothing() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Marks");
		MindMapNode marks = opened.child("Marks");
		Path file = Files.createTempFile("coverage-fav", ".txt");
		try {
			assertFalse(new ImportExplorerFavoritesAction(opened.mode).importExplorerFavorites(file.toFile(), marks,
					true));
			assertEquals(0, marks.getChildCount());
		} finally {
			Files.deleteIfExists(file);
		}
	}
}
