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

package tests.freemind.cov.latex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.main.XMLElement;
import freemind.modes.MindMapNode;
import freemind.view.mindmapview.NodeView;
import plugins.latex.JZoomedHotEqn;
import plugins.latex.LatexNodeHook;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** A latex hook keeps an equation and one viewer on each node view. */
@DisplayName("LatexNodeHookBehaviourTest")
class LatexNodeHookBehaviourTest {

	@Test
	void theDefaultEquationIsReplacedSavedAndLoaded() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha");
		LatexNodeHook hook = attach(opened, opened.child("Alpha"));
		assertEquals("E = mc^{2}", hook.getContent(null));

		XMLElement stored = new XMLElement();
		hook.save(stored);
		assertEquals("E = mc^{2}", stored.getStringAttribute("EQUATION"));
		assertEquals(hook.getName(), stored.getStringAttribute("name"));

		hook.setContent("formula", "a^2+b^2");
		assertEquals("a^2+b^2", hook.getContent("formula"));
		XMLElement changed = new XMLElement();
		hook.save(changed);
		assertEquals("a^2+b^2", changed.getStringAttribute("EQUATION"));

		XMLElement incoming = new XMLElement();
		incoming.setAttribute("EQUATION", "x=1");
		hook.loadFrom(incoming);
		assertEquals("x=1", hook.getContent(null));

		hook.loadFrom(new XMLElement());
		assertEquals("x=1", hook.getContent(null));
	}

	@Test
	void viewersAreAddedToTheContentPaneAndRemovedAgain() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha");
		MindMapNode node = opened.child("Alpha");
		LatexNodeHook hook = attach(opened, node);
		NodeView view = opened.view.getNodeView(node);
		assertNotNull(view);
		Container pane = view.getContentPane();
		int before = pane.getComponentCount();

		hook.onViewRemovedHook(view);
		assertEquals(before, pane.getComponentCount());
		assertEquals(0, equationCount(pane));

		hook.onViewCreatedHook(view);
		assertEquals(before + 1, pane.getComponentCount());
		assertEquals("E = mc^{2}", equation(pane));

		hook.setContent("formula", "a+b");
		assertEquals("a+b", hook.getContent("formula"));
		assertEquals("a+b", equation(pane));
		assertEquals(before + 1, pane.getComponentCount());

		hook.onViewCreatedHook(view);
		assertEquals(before + 2, pane.getComponentCount());
		assertEquals("a+b", equation(pane));
		hook.onViewRemovedHook(view);
		assertEquals(before + 1, pane.getComponentCount());
		hook.onViewRemovedHook(view);
		assertEquals(before, pane.getComponentCount());
		assertEquals(0, equationCount(pane));

		hook.onViewCreatedHook(view);
		hook.onViewCreatedHook(view);
		assertEquals(before + 2, pane.getComponentCount());
		hook.shutdownMapHook();
		assertEquals(before, pane.getComponentCount());
		assertEquals(0, equationCount(pane));
	}

	@Test
	void invokeAddsOneViewerForEachCurrentNodeView() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha");
		MindMapNode node = opened.child("Alpha");
		LatexNodeHook hook = attach(opened, node);
		hook.setContent("formula", "c=d");
		NodeView view = opened.view.getNodeView(node);
		assertNotNull(view);
		int viewers = opened.mode.getView().getViewers(node).size();
		assertTrue(viewers >= 1);
		Container pane = view.getContentPane();
		int before = pane.getComponentCount();

		hook.invoke(node);
		assertEquals(before + viewers, pane.getComponentCount());
		assertEquals(viewers, equationCount(pane));
		assertEquals("c=d", equation(pane));

		hook.shutdownMapHook();
		assertEquals(before, pane.getComponentCount());
		assertEquals(0, equationCount(pane));
	}

	private static LatexNodeHook attach(Opened opened, MindMapNode node) {
		LatexNodeHook hook = new LatexNodeHook();
		hook.setName("plugins/latex/LatexNodeHook.properties");
		hook.setController(opened.mode);
		hook.setMap(opened.mode.getMap());
		hook.setNode(node);
		return hook;
	}

	private static int equationCount(Container pane) {
		int count = 0;
		for (Component component : pane.getComponents()) {
			if (component instanceof JZoomedHotEqn) {
				count++;
			}
		}
		return count;
	}

	private static String equation(Container pane) {
		String found = null;
		int count = 0;
		for (Component component : pane.getComponents()) {
			if (component instanceof JZoomedHotEqn eqn) {
				count++;
				if (found == null) {
					found = eqn.getEquation();
				} else {
					assertEquals(found, eqn.getEquation());
				}
			}
		}
		assertTrue(count > 0);
		return found;
	}
}
