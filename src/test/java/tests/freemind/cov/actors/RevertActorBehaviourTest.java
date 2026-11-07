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

package tests.freemind.cov.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.main.Resources;
import freemind.modes.MindMapNode;
import freemind.modes.MapAdapter;
import freemind.modes.mindmapmode.MindMapMapModel;
import freemind.modes.mindmapmode.actions.RevertAction;
import freemind.view.MapModule;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** An unsaved revert stops at the dialog, and a saved map reloads its file. */
@DisplayName("RevertActorBehaviourTest")
class RevertActorBehaviourTest {

	@Test
	void anUnsavedMapStays() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Kept");
		assertNull(opened.mode.getMap().getFile());
		assertThrows(HeadlessException.class,
				() -> new RevertAction(opened.mode).actionPerformed(
						new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "revert")));
		assertEquals(List.of("Kept"), MapFixture.childTexts(opened.root));
	}

	@Test
	void aSavedUnchangedMapReloadsTheFile() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Kept");
		File file = File.createTempFile("revert-actor", ".mm");
		file.deleteOnExit();
		StringWriter stored = new StringWriter();
		opened.mode.getMap().getXml(stored);
		Files.writeString(file.toPath(), stored.toString(), StandardCharsets.UTF_8);
		((MapAdapter) opened.mode.getMap()).setFile(file);
		opened.mode.getMap().setSaved(true);
		opened.child("Kept").setUserObject("Dirty");
		assertTrue(opened.mode.getMap().isSaved());
		keepAnotherMapOpen(opened);
		opened.mode.getActorFactory().getRevertActor().revertMap(opened.mode.getMap(), file);
		assertNotNull(opened.app.getMapModule());
		assertEquals(List.of("Kept"),
				MapFixture.childTexts((MindMapNode) opened.app.getMap().getRootNode()));
	}

	@Test
	void xmlWithoutAFileNameOpensThatMap() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Old");
		StringWriter writer = new StringWriter();
		opened.mode.getMap().getXml(writer);
		String xml = writer.toString().replace("TEXT=\"Old\"", "TEXT=\"Fresh\"");
		keepAnotherMapOpen(opened);
		Set<String> before = revertedNames();
		try {
			opened.mode.getActorFactory().getRevertActor().openXmlInsteadOfMap(xml);
			assertNotNull(opened.app.getMapModule());
			assertEquals(List.of("Fresh"),
					MapFixture.childTexts((MindMapNode) opened.app.getMap().getRootNode()));
		} finally {
			deleteNewRevertedFiles(before);
		}
	}

	private static Set<String> revertedNames() {
		Set<String> names = new HashSet<>();
		File[] files = new File(Resources.getInstance().getFreemindDirectory())
				.listFiles((dir, name) -> name.startsWith("freemind_reverted") && name.endsWith(".mm"));
		if (files != null) {
			for (File file : files) {
				names.add(file.getName());
			}
		}
		return names;
	}

	private static void deleteNewRevertedFiles(Set<String> before) {
		File[] files = new File(Resources.getInstance().getFreemindDirectory())
				.listFiles((dir, name) -> name.startsWith("freemind_reverted") && name.endsWith(".mm"));
		if (files == null) {
			return;
		}
		for (File file : files) {
			if (!before.contains(file.getName())) {
				file.delete();
			}
		}
	}

	/** Leaves a second module open so closing the current map does not hit the headless toolbar. */
	private static void keepAnotherMapOpen(Opened opened) {
		MapModule current = opened.app.getMapModule();
		MindMapMapModel spare = new MindMapMapModel(opened.mode);
		opened.app.getMapModuleManager().newMapModule(spare, opened.mode);
		opened.app.getMapModuleManager().changeToMapModule(current);
	}
}
