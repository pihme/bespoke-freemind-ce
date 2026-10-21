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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Properties;

import javax.swing.JPanel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import freemind.controller.MindMapNodesSelection;
import freemind.controller.NodeDragListener;
import freemind.main.Resources;
import freemind.view.mindmapview.MainView;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** A node drag reports copy, link, or move, and ignores the root and a disabled preference. */
@DisplayName("NodeDragListenerBehaviourTest")
class NodeDragListenerBehaviourTest {

	@Test
	void theCursorMatchesTheDragAction() {
		// backwards compatibility
		NodeDragListener listener = new NodeDragListener(null);
		assertSame(DragSource.DefaultCopyDrop, listener.getCursorByAction(DnDConstants.ACTION_COPY));
		assertSame(DragSource.DefaultLinkDrop, listener.getCursorByAction(DnDConstants.ACTION_LINK));
		assertSame(DragSource.DefaultMoveDrop, listener.getCursorByAction(DnDConstants.ACTION_MOVE));
	}

	@Test
	@Tag("gui")
	void draggingIsIgnoredWhenThePreferenceIsOffOrTheNodeIsTheRoot() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		NodeDragListener listener = new NodeDragListener(opened.app);
		MainView child = opened.view.getNodeView(opened.child("Child")).getMainView();
		RecordingGesture ignored = gesture(child, 0);
		withDrag(false, () -> listener.dragGestureRecognized(ignored));
		assertFalse(ignored.started);

		RecordingGesture root = gesture(opened.view.getRoot().getMainView(), 0);
		withDrag(true, () -> listener.dragGestureRecognized(root));
		assertFalse(root.started);
	}

	@Test
	@Tag("gui")
	void aChildDragStartsAsMoveLinkOrCopyAndTheListenerKeepsTheCursor() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		opened.view.selectAsTheOnlyOneSelected(opened.view.getNodeView(opened.child("Child")));
		NodeDragListener listener = new NodeDragListener(opened.app);
		MainView child = opened.view.getNodeView(opened.child("Child")).getMainView();

		RecordingGesture move = gesture(child, 0);
		withDrag(true, () -> listener.dragGestureRecognized(move));
		assertEquals("MOVE", move.action);
		assertSame(DragSource.DefaultMoveDrop, move.cursor);

		RecordingGesture link = gesture(child, InputEvent.BUTTON3_DOWN_MASK);
		withDrag(true, () -> listener.dragGestureRecognized(link));
		assertEquals("LINK", link.action);
		assertSame(DragSource.DefaultLinkDrop, link.cursor);

		RecordingGesture copy = gesture(child, InputEvent.BUTTON2_DOWN_MASK);
		withDrag(true, () -> listener.dragGestureRecognized(copy));
		assertEquals("COPY", copy.action);
		assertSame(DragSource.DefaultCopyDrop, copy.cursor);

		Cursor before = move.cursor;
		move.listener.dragEnter(null);
		move.listener.dragOver(null);
		move.listener.dragExit(null);
		move.listener.dragDropEnd(null);
		assertSame(before, move.cursor);

		DragSourceContext context = new DragSourceContext(move, DragSource.DefaultMoveDrop,
				null, new Point(0, 0), move.transferable, null);
		move.listener.dropActionChanged(new DragSourceDragEvent(context,
				DnDConstants.ACTION_COPY, DnDConstants.ACTION_COPY, 0));
		assertSame(DragSource.DefaultCopyDrop, context.getCursor());
	}

	private static void withDrag(boolean enabled, DragCall call) throws Exception {
		Properties properties = Resources.getInstance().getProperties();
		String previous = properties.getProperty("draganddrop");
		properties.setProperty("draganddrop", Boolean.toString(enabled));
		try {
			call.run();
		} finally {
			if (previous == null) {
				properties.remove("draganddrop");
			} else {
				properties.setProperty("draganddrop", previous);
			}
		}
	}

	private static RecordingGesture gesture(Component component, int modifiers) {
		Recognizer recognizer = new Recognizer(component);
		MouseEvent trigger = new MouseEvent(component, MouseEvent.MOUSE_PRESSED,
				System.currentTimeMillis(), modifiers, 1, 1, 1, false, MouseEvent.BUTTON1);
		recognizer.remember(trigger);
		return new RecordingGesture(recognizer, DnDConstants.ACTION_MOVE, new Point(1, 1),
				List.of(trigger));
	}

	private interface DragCall {
		void run() throws Exception;
	}

	private static final class Recognizer extends DragGestureRecognizer {
		Recognizer(Component component) {
			super(DragSource.getDefaultDragSource(), component, DnDConstants.ACTION_COPY_OR_MOVE);
		}

		@Override
		protected void registerListeners() {
		}

		@Override
		protected void unregisterListeners() {
		}

		void remember(InputEvent event) {
			appendEvent(event);
		}
	}

	private static final class RecordingGesture extends DragGestureEvent {
		private boolean started;
		private String action;
		private Cursor cursor;
		private Transferable transferable;
		private DragSourceListener listener;

		RecordingGesture(DragGestureRecognizer recognizer, int action, Point origin,
				List<? extends InputEvent> events) {
			super(recognizer, action, origin, events);
		}

		@Override
		public void startDrag(Cursor dragCursor, Transferable transferable, DragSourceListener listener) {
			this.started = true;
			this.cursor = dragCursor;
			this.transferable = transferable;
			this.listener = listener;
			try {
				this.action = String.valueOf(
						transferable.getTransferData(MindMapNodesSelection.dropActionFlavor));
			} catch (Exception ex) {
				this.action = ex.getClass().getSimpleName();
			}
		}
	}
}
