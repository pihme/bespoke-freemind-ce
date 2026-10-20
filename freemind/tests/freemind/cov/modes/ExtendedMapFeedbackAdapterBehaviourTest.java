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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import freemind.extensions.HookFactory;
import freemind.main.Resources;
import freemind.modes.ExtendedMapFeedbackAdapter;
import freemind.modes.MindMap;
import freemind.modes.MindMapNode;
import freemind.modes.NodeAdapter;
import freemind.modes.ViewAbstraction;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** The base feedback looks nodes up, copies a dummy, and refuses to blend colors. */
class ExtendedMapFeedbackAdapterBehaviourTest {

	@Test
	void nodeLookupSelectionAndDummyCopy() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode child = opened.child("Child");
		Feedback feedback = new Feedback(opened.mode.getMap(), opened.view);
		String id = opened.mode.getNodeID(child);
		assertSame(child, feedback.getNodeFromID(id));
		assertThrows(IllegalArgumentException.class, () -> feedback.getNodeFromID("missing-id"));
		feedback.select(child, List.of(child));
		assertSame(child, feedback.getSelected());
		Transferable copied = feedback.copy(child, false);
		assertFalse(copied.isDataFlavorSupported(DataFlavor.stringFlavor));
		assertEquals(0, copied.getTransferDataFlavors().length);
		assertThrows(UnsupportedFlavorException.class, () -> copied.getTransferData(DataFlavor.stringFlavor));
		assertNull(feedback.newNode("fresh", opened.mode.getMap()));
		assertSame(feedback, feedback.load(new File("unused.mm")));
		assertThrows(UnsupportedOperationException.class, () -> feedback.blendNodeColor(child));
	}

	@Test
	void styleChangeMarksTheMapUnsavedAndReachesAnUnstyledChild() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode child = opened.child("Child");
		opened.mode.getMap().setSaved(true);
		Feedback feedback = new Feedback(opened.mode.getMap(), opened.view);
		feedback.nodeStyleChanged(opened.root);
		assertFalse(opened.mode.getMap().isSaved());
		assertEquals("Child", child.getText());
	}

	@Test
	void hookFactoryIsStableAndFoldEditAndCloseLeaveTheChildren() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		int children = opened.root.getChildCount();
		MindMapNode child = opened.child("Child");
		String folding = "enable_leaves_folding";
		String previousFolding = Resources.getInstance().getProperties().getProperty(folding);
		Resources.getInstance().getProperties().setProperty(folding, "true");
		try {
			Feedback feedback = new Feedback(opened.mode.getMap(), opened.view, child);
			HookFactory first = feedback.getHookFactory();
			assertSame(first, feedback.getHookFactory());
			assertTrue(first.getPossibleNodeHooks() != null);
			feedback.toggleFolded();
			assertTrue(child.isFolded());
			assertFalse(opened.root.isFolded());
			feedback.edit(null, false, false);
			feedback.close(true);
			assertEquals(children, opened.root.getChildCount());
			if (!first.getPossibleNodeHooks().isEmpty()) {
				String name = first.getPossibleNodeHooks().get(0);
				feedback.addHook(child, Collections.singletonList(child), name, null);
				feedback.removeHook(child, Collections.singletonList(child), name);
				assertEquals("Child", child.getText());
			}
		} finally {
			if (previousFolding == null) {
				Resources.getInstance().getProperties().remove(folding);
			} else {
				Resources.getInstance().getProperties().setProperty(folding, previousFolding);
			}
		}
	}

	private static final class Feedback extends ExtendedMapFeedbackAdapter {
		private final MindMap map;
		private final ViewAbstraction view;
		private final MindMapNode foldTarget;

		Feedback(MindMap map, ViewAbstraction view) {
			this(map, view, map.getRootNode());
		}

		Feedback(MindMap map, ViewAbstraction view, MindMapNode foldTarget) {
			this.map = map;
			this.view = view;
			this.foldTarget = foldTarget;
		}

		public MindMap getMap() {
			return map;
		}

		public ViewAbstraction getViewAbstraction() {
			return new ViewAbstraction() {
				public void select(freemind.view.mindmapview.NodeView node) {
				}

				public void deselect(freemind.view.mindmapview.NodeView node) {
				}

				public freemind.view.mindmapview.NodeView getNodeView(MindMapNode node) {
					return view.getNodeView(node);
				}

				public boolean isSelected(freemind.view.mindmapview.NodeView node) {
					return false;
				}

				public java.util.LinkedList getSelecteds() {
					java.util.LinkedList selected = new java.util.LinkedList();
					selected.add(foldTarget);
					return selected;
				}

				public freemind.view.mindmapview.NodeView getSelected() {
					return null;
				}

				public java.util.ArrayList<MindMapNode> getSelectedNodesSortedByY() {
					java.util.ArrayList<MindMapNode> nodes = new java.util.ArrayList<>();
					nodes.add(foldTarget);
					return nodes;
				}
			};
		}

		public NodeAdapter getNodeFromID(String nodeID) {
			return super.getNodeFromID(nodeID);
		}
	}
}
