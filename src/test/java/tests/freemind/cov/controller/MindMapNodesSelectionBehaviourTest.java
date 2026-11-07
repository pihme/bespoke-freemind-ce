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

package tests.freemind.cov.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.controller.MindMapNodesSelection;

/** Clipboard payload for a node copy. Each flavor returns the value it was given. */
@DisplayName("MindMapNodesSelectionBehaviourTest")
class MindMapNodesSelectionBehaviourTest {

	@Test
	void eachStoredFlavorReturnsItsPayload() throws Exception {
		// backwards compatibility
		MindMapNodesSelection selection = full();
		assertEquals("image-bytes", selection.getTransferData(DataFlavor.imageFlavor));
		assertEquals("plain", selection.getTransferData(DataFlavor.stringFlavor));
		assertEquals("<nodes/>", selection.getTransferData(MindMapNodesSelection.mindMapNodesFlavor));
		assertEquals("copy", selection.getTransferData(MindMapNodesSelection.dropActionFlavor));
		assertEquals("<p>hi</p>", selection.getTransferData(MindMapNodesSelection.htmlFlavor));
		assertEquals(List.of("/tmp/a.mm"), selection.getTransferData(MindMapNodesSelection.fileListFlavor));
		assertEquals(List.of("id-1"), selection.getTransferData(MindMapNodesSelection.copyNodeIdsFlavor));

		ByteArrayInputStream rtf = (ByteArrayInputStream) selection.getTransferData(MindMapNodesSelection.rtfFlavor);
		assertArrayEquals("{\\rtf1 hi}".getBytes(StandardCharsets.UTF_8), rtf.readAllBytes());
	}

	@Test
	void supportFollowsWhichPayloadsArePresent() throws Exception {
		// backwards compatibility
		MindMapNodesSelection selection = full();
		assertTrue(selection.isDataFlavorSupported(DataFlavor.stringFlavor));
		assertTrue(selection.isDataFlavorSupported(MindMapNodesSelection.fileListFlavor));

		MindMapNodesSelection empty = new MindMapNodesSelection(
				null, null, null, null, null, null, List.of(), null);
		assertFalse(empty.isDataFlavorSupported(DataFlavor.stringFlavor));
		assertFalse(empty.isDataFlavorSupported(MindMapNodesSelection.htmlFlavor));
		assertFalse(empty.isDataFlavorSupported(MindMapNodesSelection.fileListFlavor));
		assertEquals(List.of(), empty.getTransferData(MindMapNodesSelection.fileListFlavor));
		assertThrows(UnsupportedFlavorException.class,
				() -> empty.getTransferData(MindMapNodesSelection.htmlFlavor));
	}

	@Test
	void theFlavorListOmitsTheFileListAndAnUnknownFlavorIsRejected() throws Exception {
		// backwards compatibility
		MindMapNodesSelection selection = full();
		DataFlavor[] flavors = selection.getTransferDataFlavors();
		assertEquals(7, flavors.length);
		assertFalse(List.of(flavors).contains(MindMapNodesSelection.fileListFlavor));

		DataFlavor plain = new DataFlavor("text/plain; class=java.lang.String");
		assertFalse(selection.isDataFlavorSupported(plain));
		assertThrows(UnsupportedFlavorException.class, () -> selection.getTransferData(plain));
	}

	@Test
	void setDropActionReplacesTheDropPayloadAndLosingOwnershipKeepsIt() throws Exception {
		// backwards compatibility
		MindMapNodesSelection selection = full();
		selection.setDropAction("move");
		selection.lostOwnership(null, selection);
		assertEquals("move", selection.getTransferData(MindMapNodesSelection.dropActionFlavor));
		assertEquals("plain", selection.getTransferData(DataFlavor.stringFlavor));
	}

	private static MindMapNodesSelection full() {
		return new MindMapNodesSelection("<nodes/>", "image-bytes", "plain", "{\\rtf1 hi}",
				"<p>hi</p>", "copy", List.of("/tmp/a.mm"), List.of("id-1"));
	}
}
