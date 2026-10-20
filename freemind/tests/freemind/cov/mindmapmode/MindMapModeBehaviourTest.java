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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

import freemind.modes.mindmapmode.MindMapMapModel;
import freemind.modes.mindmapmode.MindMapMode;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** MindMap mode restores a saved map from its file path. */
class MindMapModeBehaviourTest {

	@Test
	void restoreOpensACurrentMapAndRejectsAMissingFile() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Kept");
		File dir = Files.createTempDirectory("mm-mode").toFile();
		File map = new File(dir, "restored.mm");
		Files.writeString(map.toPath(),
				"<map version=\"1.1.0\"><node TEXT=\"Restored\" ID=\"ID_1\"/></map>",
				StandardCharsets.UTF_8);
		MindMapMode mode = new MindMapMode();
		mode.init(opened.app);
		assertEquals("MindMap", mode.toString());

		mode.restore(map.getPath());
		assertEquals("Restored", opened.app.getModeController().getMap().getRootNode().getText());
		((MindMapMapModel) opened.app.getModeController().getMap()).destroy();

		File missing = new File(dir, "missing.mm");
		assertThrows(FileNotFoundException.class, () -> mode.restore(missing.getPath()));
	}
}
