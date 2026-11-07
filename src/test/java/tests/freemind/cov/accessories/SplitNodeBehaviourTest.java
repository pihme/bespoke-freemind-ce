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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.EventQueue;
import java.lang.reflect.Method;
import java.util.Properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import accessories.plugins.SplitNode;
import freemind.modes.MindMapNode;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("SplitNodeBehaviourTest")
class SplitNodeBehaviourTest {

	// backwards compatibility
	@Test
	void htmlParagraphsBecomeSiblingNodes() throws Exception {
		Opened opened = MapFixture.open("Line");
		MindMapNode child = opened.child("Line");
		opened.mode.setNodeText(child, "<html><body><p>Alpha</p><p>Beta</p></body></html>");
		select(opened, child);
		SplitNode hook = hook(opened);

		hook.invoke(child);
		EventQueue.invokeAndWait(() -> {
		});

		assertTrue(child.getText().contains("Alpha"), child.getText());
		assertTrue(MapFixture.childTexts(opened.root).stream().anyMatch(text -> text != null && text.contains("Beta")),
				MapFixture.childTexts(opened.root).toString());
	}

	// backwards compatibility
	@Test
	void theRootAndASingleParagraphStayIntact() throws Exception {
		Opened opened = MapFixture.open("Only");
		select(opened, opened.root);
		SplitNode hook = hook(opened);
		String rootText = opened.root.getText();
		hook.invoke(opened.root);
		assertEquals(rootText, opened.root.getText());

		MindMapNode only = opened.child("Only");
		opened.mode.setNodeText(only, "<html><body><p>Solo</p></body></html>");
		select(opened, only);
		hook.invoke(only);
		assertEquals("<html><body><p>Solo</p></body></html>", only.getText());
		assertEquals(1, opened.root.getChildCount());
	}

	// backwards compatibility
	@Test
	void aDocumentWithoutAMultiChildParentDoesNotSplit() throws Exception {
		SplitNode hook = new SplitNode();
		Method split = SplitNode.class.getDeclaredMethod("splitNode", String.class);
		split.setAccessible(true);
		assertNull(split.invoke(hook, "<html><body><p>Solo</p></body></html>"));
		String[] plain = (String[]) split.invoke(hook, "One\n\nTwo");
		assertEquals("One", plain[0]);
		assertEquals("", plain[1]);
		assertEquals("Two", plain[2]);
	}

	private static SplitNode hook(Opened opened) {
		SplitNode hook = new SplitNode();
		hook.setController(opened.mode);
		hook.setMap(opened.mode.getMap());
		hook.setProperties(new Properties());
		return hook;
	}

	private static void select(Opened opened, MindMapNode node) {
		opened.view.getRoot().updateAll();
		opened.view.selectAsTheOnlyOneSelected(opened.view.getNodeView(node));
	}
}
