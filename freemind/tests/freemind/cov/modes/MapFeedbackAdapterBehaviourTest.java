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

package tests.freemind.cov.modes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import freemind.extensions.PermanentNodeHook;
import freemind.extensions.PermanentNodeHookAdapter;
import freemind.main.Resources;
import freemind.modes.MapFeedbackAdapter;
import freemind.modes.MindMap;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.MindMapNodeModel;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Depth order, hooks, and the empty view callbacks stay on the model. */
class MapFeedbackAdapterBehaviourTest {

	@Test
	void deeperNodesSortBeforeTheirAncestors() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode child = opened.child("Child");
		MindMapNode grand = new MindMapNodeModel("Grand", opened.mode.getMap());
		child.insert(grand, 0);
		Feedback feedback = new Feedback(opened.mode.getMap());
		List<MindMapNode> nodes = new ArrayList<>(List.of(opened.root, child, grand));
		feedback.sortNodesByDepth(nodes);
		assertEquals(List.of(grand, child, opened.root), nodes);
	}

	@Test
	void createNodeHookAddsANamedHookAndInvocationReachesIt() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode child = opened.child("Child");
		Feedback feedback = new Feedback(opened.mode.getMap());
		PermanentNodeHook created = (PermanentNodeHook) feedback.createNodeHook("plugins/sample", child);
		assertEquals("plugins/sample", created.getName());
		assertTrue(child.getHooks().contains(created));
		CountingHook hook = new CountingHook();
		child.addHook(hook);
		child.invokeHook(hook);
		feedback.invokeHooksRecursively(opened.root, opened.mode.getMap());
		assertTrue(hook.invocations > 0);
		assertSame(child, hook.getNode());
	}

	@Test
	void setPropertyWritesResourcesAndEmptyCallbacksLeaveTheNode() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode child = opened.child("Child");
		boolean folded = child.isFolded();
		Feedback feedback = new Feedback(opened.mode.getMap());
		Properties properties = Resources.getInstance().getProperties();
		String key = "cov.modes.feedback";
		String previous = properties.getProperty(key);
		try {
			feedback.setProperty(key, "stored");
			assertEquals("stored", properties.getProperty(key));
			assertSame(feedback, feedback.getViewFeedback());
			assertNull(feedback.getNodeView(child));
			feedback.setFolded(child, true);
			feedback.select(opened.view.getRoot());
			feedback.paste(child, opened.root);
			feedback.out("note");
			feedback.onViewRemovedHook(opened.view.getRoot());
			feedback.nodeRefresh(child);
			feedback.registerMouseWheelEventHandler(null);
			feedback.deRegisterMouseWheelEventHandler(null);
			assertEquals("Child", child.getText());
			assertEquals(folded, child.isFolded());
		} finally {
			if (previous == null) {
				properties.remove(key);
			} else {
				properties.setProperty(key, previous);
			}
		}
	}

	private static final class Feedback extends MapFeedbackAdapter {
		private final MindMap map;

		Feedback(MindMap map) {
			this.map = map;
		}

		public MindMap getMap() {
			return map;
		}
	}

	private static final class CountingHook extends PermanentNodeHookAdapter {
		int invocations;

		public String getName() {
			return "counting";
		}

		public void invoke(MindMapNode node) {
			invocations++;
		}
	}
}
