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

package tests.freemind.cov.listeners;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.controller.MindMapNodesSelection;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.MindMapMapModel;
import freemind.modes.mindmapmode.MindMapNodeModel;
import freemind.modes.mindmapmode.listeners.MindMapNodeDropListener;
import freemind.view.mindmapview.MainView;
import freemind.view.mindmapview.NodeView;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** A drop copies, moves, or links the selection, and rejects a drop onto itself. */
@DisplayName("MindMapNodeDropListenerBehaviourTest")
class MindMapNodeDropListenerBehaviourTest {

	@Test
	void theHighlightFollowsThePointerAndAnUnknownFlavorIsRefused() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta");
		MindMapNodeDropListener listener = new MindMapNodeDropListener(opened.mode);
		MainView alpha = main(opened, "Alpha");
		alpha.setSize(90, 24);

		Drag over = drag(alpha, new Point(5, 4));
		listener.dragOver(over.event);
		int first = alpha.getDraggedOver();
		assertEquals(alpha.dropAsSibling(5) ? NodeView.DRAGGED_OVER_SIBLING : NodeView.DRAGGED_OVER_SON, first);

		Drag across = drag(alpha, new Point(50, 4));
		listener.dragOver(across.event);
		assertNotEquals(first, alpha.getDraggedOver());

		DropTargetEvent exit = mock(DropTargetEvent.class);
		when(exit.getDropTargetContext()).thenReturn(over.context);
		listener.dragExit(exit);
		assertEquals(NodeView.DRAGGED_OVER_NO, alpha.getDraggedOver());

		alpha.setDraggedOver(NodeView.DRAGGED_OVER_SON);
		listener.dragScroll(drag(alpha, new Point(50, 4)).event);
		listener.dropActionChanged(drag(alpha, new Point(50, 4)).event);
		assertEquals(NodeView.DRAGGED_OVER_SON, alpha.getDraggedOver());

		DropTargetDragEvent text = drag(alpha, new Point(45, 4)).event;
		when(text.isDataFlavorSupported(DataFlavor.stringFlavor)).thenReturn(true);
		listener.dragEnter(text);
		verify(text).acceptDrag(DnDConstants.ACTION_MOVE);

		DropTargetDragEvent files = drag(alpha, new Point(45, 4)).event;
		when(files.isDataFlavorSupported(DataFlavor.stringFlavor)).thenReturn(false);
		when(files.isDataFlavorSupported(MindMapNodesSelection.fileListFlavor)).thenReturn(true);
		listener.dragEnter(files);
		verify(files).acceptDrag(DnDConstants.ACTION_MOVE);

		DropTargetDragEvent refused = drag(alpha, new Point(45, 4)).event;
		when(refused.isDataFlavorSupported(DataFlavor.stringFlavor)).thenReturn(false);
		when(refused.isDataFlavorSupported(MindMapNodesSelection.fileListFlavor)).thenReturn(false);
		listener.dragEnter(refused);
		verify(refused).rejectDrag();
	}

	@Test
	void aCopyLandsOnTheTargetAndAMoveOntoItselfIsRefused() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta");
		MindMapNode alpha = opened.child("Alpha");
		MindMapNode beta = opened.child("Beta");
		opened.mode.select(alpha, List.of(alpha));
		MindMapNodeDropListener listener = new MindMapNodeDropListener(opened.mode);
		MainView betaView = main(opened, "Beta");
		betaView.setSize(90, 24);

		DropTargetDropEvent copy = drop(betaView, new Point(45, 4), DnDConstants.ACTION_COPY, true, null);
		listener.drop(copy);
		verify(copy).dropComplete(true);
		assertEquals(List.of("Alpha"), MapFixture.childTexts(beta));
		assertEquals(2, opened.root.getChildCount());

		opened.mode.select(alpha, List.of(alpha));
		DropTargetDropEvent ontoSelf = drop(main(opened, "Alpha"), new Point(45, 4), DnDConstants.ACTION_MOVE, true,
				null);
		listener.drop(ontoSelf);
		verify(ontoSelf).rejectDrop();
		assertEquals(List.of("Alpha"), MapFixture.childTexts(beta));
	}

	@Test
	void aMoveReparentsTheNodeAndALinkJoinsTheSelectionToTheTarget() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta");
		MindMapNode alpha = opened.child("Alpha");
		MindMapNode beta = opened.child("Beta");
		opened.mode.select(alpha, List.of(alpha));
		MindMapNodeDropListener listener = new MindMapNodeDropListener(opened.mode);
		MainView betaView = main(opened, "Beta");
		betaView.setSize(90, 24);

		DropTargetDropEvent link = drop(betaView, new Point(45, 4), DnDConstants.ACTION_LINK, true,
				action("LINK"));
		listener.drop(link);
		verify(link).dropComplete(true);
		assertTrue(opened.mode.getModel().getLinkRegistry().getAllSources(beta).contains(alpha));

		DropTargetDropEvent move = drop(betaView, new Point(45, 4), DnDConstants.ACTION_MOVE, true, null);
		listener.drop(move);
		verify(move).dropComplete(true);
		assertEquals(0, countNamed(opened.root, "Alpha"));
		assertTrue(countNamed(beta, "Alpha") >= 1);
	}

	@Test
	void aDropFromOutsidePastesAndAProtectedOrCrowdedDropStops() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta", "C", "D", "E");
		MindMapNode alpha = opened.child("Alpha");
		MindMapNode beta = opened.child("Beta");
		MindMapNodeDropListener listener = new MindMapNodeDropListener(opened.mode);
		MainView betaView = main(opened, "Beta");
		betaView.setSize(90, 24);
		Transferable copied = opened.mode.copy(alpha, false);

		DropTargetDropEvent incoming = drop(betaView, new Point(45, 4), DnDConstants.ACTION_COPY, false, copied);
		listener.drop(incoming);
		verify(incoming).dropComplete(true);
		assertTrue(countNamed(beta, "Alpha") >= 1);

		MindMapMapModel map = (MindMapMapModel) opened.mode.getModel();
		MindMapNodeModel locked = new MindMapNodeModel("Locked", map) {
			@Override
			public boolean isWriteable() {
				return false;
			}
		};
		opened.mode.insertNodeInto(locked, opened.root);
		NodeView lockedView = opened.view.getNodeView(locked);
		assertTrue(lockedView != null);
		MainView lockedMain = lockedView.getMainView();
		lockedMain.setSize(90, 24);
		opened.mode.select(alpha, List.of(alpha));
		DropTargetDropEvent protectedDrop = drop(lockedMain, new Point(45, 4), DnDConstants.ACTION_COPY, true,
				action("COPY"));
		listener.drop(protectedDrop);
		verify(protectedDrop).dropComplete(false);
		assertEquals(0, locked.getChildCount());

		List<MindMapNode> five = List.of(alpha, beta, opened.child("C"), opened.child("D"), opened.child("E"));
		opened.mode.select(alpha, five);
		DropTargetDropEvent crowded = drop(betaView, new Point(45, 4), DnDConstants.ACTION_LINK, true,
				action("LINK"));
		listener.drop(crowded);
		verify(crowded).dropComplete(false);
	}

	@Test
	void movingOntoADescendantOfAnotherSelectedNodeStops() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta");
		MindMapNode alpha = opened.child("Alpha");
		MindMapNode beta = opened.child("Beta");
		MindMapNodeModel nest = new MindMapNodeModel("Nest", opened.mode.getModel());
		opened.mode.insertNodeInto(nest, alpha);
		assertTrue(opened.view.getNodeView(nest) != null);
		opened.mode.select(beta, List.of(alpha, beta));
		MindMapNodeDropListener listener = new MindMapNodeDropListener(opened.mode);
		MainView nestView = opened.view.getNodeView(nest).getMainView();
		nestView.setSize(90, 24);
		DropTargetDropEvent move = drop(nestView, new Point(45, 4), DnDConstants.ACTION_MOVE, true, null);
		listener.drop(move);
		verify(move).dropComplete(false);
		assertEquals("Nest", nest.getText());
		assertEquals(alpha, nest.getParentNode());
	}

	@Test
	void aBrokenDropCompletesAsAFailure() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha");
		MindMapNodeDropListener listener = new MindMapNodeDropListener(opened.mode);
		DropTargetDropEvent broken = mock(DropTargetDropEvent.class);
		DropTargetContext context = mock(DropTargetContext.class);
		when(broken.getDropTargetContext()).thenReturn(context);
		when(context.getComponent()).thenReturn(null);
		when(broken.isLocalTransfer()).thenReturn(false);
		when(broken.getDropAction()).thenReturn(DnDConstants.ACTION_COPY);
		when(broken.getTransferable()).thenReturn(opened.mode.copy(opened.child("Alpha"), false));
		listener.drop(broken);
		verify(broken).dropComplete(false);
		assertEquals(1, opened.root.getChildCount());
	}

	private static int countNamed(MindMapNode node, String text) {
		int count = 0;
		for (int i = 0; i < node.getChildCount(); i++) {
			if (text.equals(((MindMapNode) node.getChildAt(i)).getText())) {
				count++;
			}
		}
		return count;
	}

	private static MainView main(Opened opened, String text) {
		return opened.view.getNodeView(opened.child(text)).getMainView();
	}

	private static Transferable noFlavor() {
		Transferable transferable = mock(Transferable.class);
		when(transferable.isDataFlavorSupported(MindMapNodesSelection.dropActionFlavor)).thenReturn(false);
		return transferable;
	}

	private static Transferable action(String name) throws Exception {
		Transferable transferable = mock(Transferable.class);
		when(transferable.isDataFlavorSupported(MindMapNodesSelection.dropActionFlavor)).thenReturn(true);
		when(transferable.getTransferData(MindMapNodesSelection.dropActionFlavor)).thenReturn(name);
		return transferable;
	}

	private static DropTargetDropEvent drop(MainView view, Point where, int action, boolean local,
			Transferable transferable) {
		DropTargetDropEvent event = mock(DropTargetDropEvent.class);
		DropTargetContext context = mock(DropTargetContext.class);
		when(event.getDropTargetContext()).thenReturn(context);
		when(context.getComponent()).thenReturn(view);
		when(event.getLocation()).thenReturn(where);
		when(event.getDropAction()).thenReturn(action);
		when(event.isLocalTransfer()).thenReturn(local);
		Transferable carried = transferable == null ? noFlavor() : transferable;
		when(event.getTransferable()).thenReturn(carried);
		return event;
	}

	private static Drag drag(MainView view, Point where) {
		DropTargetDragEvent event = mock(DropTargetDragEvent.class);
		DropTargetContext context = mock(DropTargetContext.class);
		when(event.getDropTargetContext()).thenReturn(context);
		when(context.getComponent()).thenReturn(view);
		when(event.getLocation()).thenReturn(where);
		return new Drag(event, context);
	}

	private static final class Drag {
		final DropTargetDragEvent event;
		final DropTargetContext context;

		Drag(DropTargetDragEvent event, DropTargetContext context) {
			this.event = event;
			this.context = context;
		}
	}
}
