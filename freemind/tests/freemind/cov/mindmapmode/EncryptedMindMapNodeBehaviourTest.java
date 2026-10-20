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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ListIterator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import freemind.main.Tools.SingleDesEncrypter;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.EncryptedMindMapNode;
import freemind.modes.mindmapmode.MindMapMapModel;
import freemind.modes.mindmapmode.MindMapNodeModel;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** An encrypted node hides its children until the same password opens it. */
class EncryptedMindMapNodeBehaviourTest {

	@Test
	void encryptAndDecryptRoundTripHidesThenRestoresAChild() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open();
		MindMapMapModel model = (MindMapMapModel) opened.mode.getMap();
		EncryptedMindMapNode node = child(model, "secret");
		node.setPassword(new StringBuffer("pw"));
		node.insert(new MindMapNodeModel("hidden", model), 0);
		node.encrypt();
		assertFalse(node.isAccessible());
		assertFalse(node.isWriteable());
		assertEquals(0, node.getChildCount());
		assertFalse(node.hasChildren());
		assertFalse(node.childrenFolded().hasNext());
		assertTrue(node.isFolded());
		node.setFolded(false);
		assertTrue(node.isFolded());
		node.encrypt();
		assertFalse(node.isAccessible());

		StringWriter saved = new StringWriter();
		node.save(saved, model.getLinkRegistry(), true, true);
		assertTrue(saved.toString().contains("ENCRYPTED_CONTENT"));
		assertFalse(saved.toString().contains("TEXT=\"hidden\""));

		assertTrue(node.decrypt(new StringBuffer("pw")));
		assertTrue(node.isAccessible());
		assertTrue(anyChild(node, "hidden"));
		node.setFolded(false);
		assertFalse(node.isFolded());
		assertTrue(node.isWriteable());
	}

	@Test
	void passwordsMustMatchAndCiphertextMustBeReadable() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open();
		MindMapMapModel model = (MindMapMapModel) opened.mode.getMap();
		EncryptedMindMapNode cached = child(model, "cached");
		cached.setPassword(new StringBuffer("secret"));
		assertFalse(cached.checkPassword(new StringBuffer("no")));
		assertFalse(cached.checkPassword(new StringBuffer("secres")));
		assertTrue(cached.checkPassword(new StringBuffer("secret")));

		EncryptedMindMapNode garbage = child(model, "garbage");
		garbage.setAdditionalInfo("AQIDBAUGBwgJCgsMDQ4PEA==");
		assertFalse(garbage.checkPassword(new StringBuffer("pw")));
		assertFalse(garbage.decrypt(new StringBuffer("pw")));

		EncryptedMindMapNode wellFormed = child(model, "xml");
		wellFormed.setAdditionalInfo(cipher("<a></a>", "pw"));
		assertTrue(wellFormed.checkPassword(new StringBuffer("pw")));

		EncryptedMindMapNode nodeShaped = child(model, "node");
		nodeShaped.setAdditionalInfo(cipher("<node TEXT=\"oldkid\"></node>", "pw"));
		assertTrue(nodeShaped.checkPassword(new StringBuffer("pw")));

		EncryptedMindMapNode stale = child(model, "stale");
		stale.setPassword(new StringBuffer("pw"));
		stale.setAdditionalInfo("AQIDBAUGBwgJCgsMDQ4PEA==");
		assertTrue(stale.decrypt(new StringBuffer("pw")));
		assertFalse(stale.isAccessible());
	}

	@Test
	@Timeout(15)
	void anOldNodeCipherIsWrappedIntoAMap() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open();
		MindMapMapModel model = (MindMapMapModel) opened.mode.getMap();
		EncryptedMindMapNode node = child(model, "old");
		node.setAdditionalInfo(cipher("<node TEXT=\"oldkid\"></node>", "pw"));
		assertTrue(node.decrypt(new StringBuffer("pw")));
		assertTrue(node.isAccessible());
		assertTrue(anyChild(node, "oldkid") || anyChild(node, "DUMMY"));
	}

	@Test
	void closedNodesRejectInsertsAndCanStillListChildrenWhileShuttingDown() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open();
		MindMapMapModel model = (MindMapMapModel) opened.mode.getMap();
		EncryptedMindMapNode node = child(model, "closed");
		MindMapNodeModel kept = new MindMapNodeModel("kept", model);
		node.insert(kept, 0);
		node.setAdditionalInfo("stored");
		assertEquals("stored", node.getAdditionalInfo());
		Field decrypted = EncryptedMindMapNode.class.getDeclaredField("isDecrypted");
		decrypted.setAccessible(true);
		assertFalse(decrypted.getBoolean(node));
		assertThrows(IllegalArgumentException.class,
				() -> node.insert(new MindMapNodeModel("extra", model), 0));
		assertFalse(node.childrenUnfolded().hasNext());
		node.setShuttingDown(true);
		assertEquals("kept", node.childrenUnfolded().next().getText());
		node.setShuttingDown(false);
		assertFalse(node.childrenUnfolded().hasNext());

		node.setStoringEncryptedContent(true);
		assertNull(node.getAdditionalInfo());
		node.setShuttingDown(true);
		StringWriter stored = new StringWriter();
		node.save(stored, model.getLinkRegistry(), true, true);
		assertTrue(stored.toString().contains("kept"));

		EncryptedMindMapNode fresh = child(model, "fresh");
		fresh.setMap(model);
		assertNotNull(fresh.getStateIcons().get("decrypted"));

		EncryptedMindMapNode blocked = child(model, "blocked");
		Field accessible = EncryptedMindMapNode.class.getDeclaredField("isAccessible");
		accessible.setAccessible(true);
		accessible.set(blocked, false);
		assertThrows(IOException.class,
				() -> blocked.save(new StringWriter(), model.getLinkRegistry(), true, true));

		EncryptedMindMapNode missingPassword = child(model, "nopw");
		assertThrows(IllegalArgumentException.class, missingPassword::encrypt);
	}

	private static EncryptedMindMapNode child(MindMapMapModel model, String text) {
		EncryptedMindMapNode node = new EncryptedMindMapNode(text, model);
		model.getRootNode().insert(node, model.getRootNode().getChildCount());
		return node;
	}

	private static String cipher(String plain, String password) {
		return new SingleDesEncrypter(new StringBuffer(password)).encrypt(plain);
	}

	private static boolean anyChild(MindMapNode node, String text) {
		ListIterator<MindMapNode> children = node.childrenUnfolded();
		while (children.hasNext()) {
			MindMapNode child = children.next();
			if (text.equals(child.getText()) || anyChild(child, text)) {
				return true;
			}
		}
		return false;
	}
}
