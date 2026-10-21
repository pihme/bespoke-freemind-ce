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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.actions.ImportFolderStructureAction;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** A folder chooser does not import, and a plain file only folds the target. */
class ImportFolderStructureActionBehaviourTest {

	@Test
	void theChooserAddsNoNodes() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Shelf");
		opened.mode.select(opened.child("Shelf"), List.of(opened.child("Shelf")));
		assertThrows(HeadlessException.class,
				() -> new ImportFolderStructureAction(opened.mode).actionPerformed(
						new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "import")));
		assertEquals(List.of("Shelf"), MapFixture.childTexts(opened.root));
		assertEquals(0, opened.child("Shelf").getChildCount());
	}

	@Test
	void aPlainFileFoldsTheTarget() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Shelf");
		MindMapNode shelf = opened.child("Shelf");
		opened.mode.addNewNode(shelf, 0, false);
		Path file = Files.createTempFile("coverage-folder", ".txt");
		try {
			new ImportFolderStructureAction(opened.mode).importFolderStructure(file.toFile(), shelf, true);
			assertTrue(shelf.isFolded());
			assertEquals(1, shelf.getChildCount());
		} finally {
			Files.deleteIfExists(file);
		}
	}
}
