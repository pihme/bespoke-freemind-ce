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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.MindMapNode;
import freemind.modes.NodeAdapter;
import freemind.modes.mindmapmode.listeners.MindMapNodeMotionListener;
import freemind.view.mindmapview.MapView;
import freemind.view.mindmapview.NodeMotionListenerView;
import freemind.view.mindmapview.NodeView;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Dragging a node's move handle changes its gap, and a double click restores one axis. */
@DisplayName("MindMapNodeMotionListenerBehaviourTest")
class MindMapNodeMotionListenerBehaviourTest {

	@Test
	void aDragOnTheRightChangesTheGapAndReleaseKeepsIt() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha");
		MindMapNode alpha = opened.child("Alpha");
		Motion motion = motion(opened, alpha);
		assertEquals(NodeAdapter.HGAP, alpha.getHGap());

		motion.listener.mouseDragged(motion.event(2, 2, 0));
		assertEquals(NodeAdapter.HGAP, alpha.getHGap());
		assertEquals(0, alpha.getShiftY());

		motion.listener.mouseDragged(motion.event(22, 12, 0));
		motion.listener.mouseReleased(motion.event(40, 40, 0));
		assertEquals(NodeAdapter.HGAP + 20, alpha.getHGap());
		assertEquals(10, alpha.getShiftY());
		assertFalse(motion.view.isMouseEntered());
	}

	@Test
	void controlDragChangesTheParentGapAndALeftNodeFlipsTheHorizontalGap() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha", "Beta");
		MindMapNode alpha = opened.child("Alpha");
		Motion motion = motion(opened, alpha);
		motion.listener.mouseDragged(motion.event(4, 4, 0));
		motion.listener.mouseDragged(motion.event(4, 6, InputEvent.CTRL_DOWN_MASK));
		assertEquals(NodeAdapter.VGAP - 2, alpha.getParentNode().getVGap());
		assertEquals(NodeAdapter.HGAP, alpha.getHGap());
		motion.listener.mouseReleased(motion.event(4, 6, 0));

		MindMapNode beta = opened.child("Beta");
		beta.setLeft(true);
		Motion left = motion(opened, beta);
		left.listener.mouseDragged(left.event(4, 4, 0));
		left.listener.mouseDragged(left.event(24, 4, 0));
		assertEquals(NodeAdapter.HGAP - 20, beta.getHGap());
	}

	@Test
	void aDoubleClickRestoresOneAxisAndAPlainClickDoesNot() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha");
		MindMapNode alpha = opened.child("Alpha");
		alpha.setHGap(55);
		alpha.setShiftY(8);
		alpha.getParentNode().setVGap(15);
		Motion motion = motion(opened, alpha);

		motion.listener.mouseClicked(motion.click(1, 0));
		assertEquals(55, alpha.getHGap());
		assertEquals(8, alpha.getShiftY());
		assertEquals(15, alpha.getParentNode().getVGap());

		motion.listener.mouseClicked(motion.click(2, InputEvent.SHIFT_DOWN_MASK));
		assertEquals(55, alpha.getHGap());

		motion.listener.mouseClicked(motion.click(2, 0));
		assertEquals(NodeAdapter.HGAP, alpha.getHGap());
		assertEquals(0, alpha.getShiftY());
		assertEquals(alpha.getVGap(), alpha.getParentNode().getVGap());

		alpha.setHGap(55);
		alpha.setShiftY(8);
		alpha.getParentNode().setVGap(15);
		motion.listener.mouseClicked(motion.click(2, InputEvent.CTRL_DOWN_MASK));
		assertEquals(55, alpha.getHGap());
		assertEquals(8, alpha.getShiftY());
		assertEquals(NodeAdapter.VGAP, alpha.getParentNode().getVGap());
	}

	@Test
	void movingWithoutAButtonDoesNothingAndTheHandleTracksEnterAndExit() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha");
		MindMapNode alpha = opened.child("Alpha");
		Motion motion = motion(opened, alpha);
		motion.listener.mouseMoved(motion.event(3, 3, 0));
		motion.listener.mouseDragged(motion.event(30, 30, 0, false));
		assertEquals(NodeAdapter.HGAP, alpha.getHGap());

		motion.view.setMouseEntered();
		motion.listener.mouseExited(motion.event(30, 30, 0));
		assertFalse(motion.view.isMouseEntered());

		motion.listener.mouseDragged(motion.event(2, 2, 0));
		motion.view.setMouseEntered();
		motion.listener.mouseExited(motion.event(2, 2, 0));
		assertTrue(motion.view.isMouseEntered());

		boolean entered = motion.view.isMouseEntered();
		try {
			motion.listener.mouseEntered(motion.event(2, 2, 0));
			boolean focused = JOptionPane.getFrameForComponent(motion.view).isFocused();
			assertEquals(focused, motion.view.isMouseEntered());
		} catch (HeadlessException ex) {
			assertEquals(entered, motion.view.isMouseEntered());
		}
	}

	@Test
	void aDragOutsideTheVisibleMapStillKeepsTheNewGap() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha");
		MindMapNode alpha = opened.child("Alpha");
		Motion motion = motion(opened, alpha);
		motion.listener.mouseDragged(motion.event(2, 2, 0));
		motion.listener.mouseDragged(motion.event(4000, 4000, 0));
		assertTrue(alpha.getHGap() > NodeAdapter.HGAP || alpha.getShiftY() != 0);
		assertFalse(opened.view.getVisibleRect().contains(new Point(4000, 4000)));
	}

	private static Motion motion(Opened opened, MindMapNode node) {
		placeInScrollPane(opened.view);
		NodeView nodeView = opened.view.getNodeView(node);
		NodeMotionListenerView handle = new NodeMotionListenerView(nodeView);
		nodeView.add(handle);
		handle.setBounds(0, 0, 12, 12);
		return new Motion(new MindMapNodeMotionListener(opened.mode), handle);
	}

	private static void placeInScrollPane(MapView view) {
		if (view.getParent() instanceof javax.swing.JViewport) {
			return;
		}
		JScrollPane pane = new JScrollPane();
		pane.setBounds(0, 0, 220, 160);
		pane.setViewportView(view);
		pane.doLayout();
		view.setSize(900, 700);
	}

	private static final class Motion {
		final MindMapNodeMotionListener listener;
		final NodeMotionListenerView view;

		Motion(MindMapNodeMotionListener listener, NodeMotionListenerView view) {
			this.listener = listener;
			this.view = view;
		}

		MouseEvent event(int x, int y, int extraModifiers) {
			return event(x, y, extraModifiers, true);
		}

		MouseEvent event(int x, int y, int extraModifiers, boolean buttonDown) {
			int modifiers = extraModifiers;
			if (buttonDown) {
				modifiers |= InputEvent.BUTTON1_DOWN_MASK;
			}
			return new MouseEvent(view, MouseEvent.MOUSE_DRAGGED, System.currentTimeMillis(), modifiers, x, y, 1,
					false, MouseEvent.BUTTON1);
		}

		MouseEvent click(int clicks, int modifiers) {
			return new MouseEvent(view, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), modifiers, 2, 2, clicks,
					false, MouseEvent.BUTTON1);
		}
	}
}
