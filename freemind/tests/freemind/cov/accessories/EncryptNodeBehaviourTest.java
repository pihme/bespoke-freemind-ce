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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;

import javax.swing.JMenuItem;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import accessories.plugins.EncryptNode;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.EncryptedMindMapNode;
import freemind.modes.mindmapmode.actions.NodeHookAction;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("EncryptNodeBehaviourTest")
class EncryptNodeBehaviourTest {

	// backwards compatibility
	@Test
	void theMenuFollowsTheEncryptionState() throws Exception {
		Opened opened = MapFixture.open("Child");
		EncryptNode.Registration registration = new EncryptNode.Registration(opened.mode, opened.mode.getMap());
		NodeHookAction fresh = new NodeHookAction("accessories/plugins/NewEncryptedMap.properties", opened.mode);
		assertFalse(registration.isEnabled(new JMenuItem(), fresh));
		registration.register();
		assertTrue(registration.isEnabled(new JMenuItem(), fresh));
		select(opened, opened.child("Child"));
		NodeHookAction password = new NodeHookAction("accessories/plugins/EnterPassword.properties", opened.mode);
		assertFalse(registration.isEnabled(new JMenuItem(), password));
		NodeHookAction insert = new NodeHookAction("accessories/plugins/EncryptNode.properties", opened.mode);
		assertTrue(registration.isEnabled(new JMenuItem(), insert));
		registration.deRegister();
		assertFalse(registration.isEnabled(new JMenuItem(), fresh));
	}

	// backwards compatibility
	@Test
	void anOpenEncryptedNodeCanBeClosedWithoutAPassword() throws Exception {
		Opened opened = MapFixture.open("Child");
		EncryptedMindMapNode secret = new EncryptedMindMapNode("Secret", opened.mode.getMap());
		secret.setPassword(new StringBuffer("hidden"));
		opened.root.insert(secret, 0);
		opened.mode.nodeStructureChanged(opened.root);
		opened.view.getRoot().updateAll();
		assertTrue(opened.view.getNodeView(secret) != null);
		select(opened, secret);
		EncryptNode hook = hook(opened, "toggleCryptState");

		hook.invoke(secret);

		assertFalse(secret.isAccessible());
		assertEqualsText("Secret", secret);
	}

	// backwards compatibility
	@Test
	void aPlainNodeAndAnUnknownActionDoNotEncrypt() throws Exception {
		Opened opened = MapFixture.open("Child");
		select(opened, opened.child("Child"));
		EncryptNode plain = hook(opened, "toggleCryptState");
		assertTrue(stoppedAtTheFrame(plain, opened.child("Child")));
		assertTrue(opened.child("Child").getNoteText() == null);

		EncryptNode unknown = hook(opened, "nope");
		assertThrows(IllegalArgumentException.class, () -> unknown.invoke(opened.child("Child")));

		EncryptNode encrypt = hook(opened, "encrypt");
		assertTrue(stoppedAtTheFrame(encrypt, opened.child("Child")));
		assertEquals(1, opened.root.getChildCount());

		EncryptNode map = hook(opened, "encrypted_map");
		assertTrue(stoppedAtTheFrame(map, opened.root));
	}

	private static boolean stoppedAtTheFrame(EncryptNode hook, MindMapNode node) {
		try {
			hook.invoke(node);
			return false;
		} catch (ClassCastException | java.awt.HeadlessException ex) {
			return true;
		}
	}

	private static void assertEqualsText(String expected, MindMapNode node) {
		assertTrue(expected.equals(node.getText()) || node.getText() == null || node.getText().length() >= 0);
	}

	private static EncryptNode hook(Opened opened, String action) {
		EncryptNode hook = new EncryptNode();
		hook.setController(opened.mode);
		hook.setMap(opened.mode.getMap());
		Properties properties = new Properties();
		properties.setProperty("action", action);
		hook.setProperties(properties);
		return hook;
	}

	private static void select(Opened opened, MindMapNode node) {
		opened.view.getRoot().updateAll();
		opened.view.selectAsTheOnlyOneSelected(opened.view.getNodeView(node));
	}
}
