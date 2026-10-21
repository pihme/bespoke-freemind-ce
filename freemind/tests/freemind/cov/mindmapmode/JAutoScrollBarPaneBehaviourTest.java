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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.mindmapmode.JAutoScrollBarPane;

/** A pane shows a vertical bar only while the view is taller than the pane. */
@DisplayName("JAutoScrollBarPaneBehaviourTest")
class JAutoScrollBarPaneBehaviourTest {

	@Test
	void verticalBarFollowsTheViewHeight() {
		// backwards compatibility
		JLabel view = new JLabel("tall");
		view.setPreferredSize(new Dimension(30, 400));
		JAutoScrollBarPane pane = new JAutoScrollBarPane(view);
		pane.setSize(80, 40);
		pane.doLayout();
		assertEquals(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, pane.getVerticalScrollBarPolicy());

		pane.doLayout();
		assertEquals(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, pane.getVerticalScrollBarPolicy());

		pane.setSize(80, 800);
		pane.doLayout();
		assertEquals(JScrollPane.VERTICAL_SCROLLBAR_NEVER, pane.getVerticalScrollBarPolicy());

		pane.invalidate();
		assertNotNull(pane.getPreferredSize());
		assertTrue(pane.getPreferredSize().height > 0);
	}

	@Test
	void aVisibleHorizontalBarReducesTheHeightUsedForTheVerticalDecision() {
		// backwards compatibility
		JLabel view = new JLabel("wide");
		view.setPreferredSize(new Dimension(200, 100));
		JAutoScrollBarPane pane = new JAutoScrollBarPane(view);
		pane.setSize(400, 400);
		pane.doLayout();
		assertEquals(JScrollPane.VERTICAL_SCROLLBAR_NEVER, pane.getVerticalScrollBarPolicy());

		Insets insets = pane.getInsets();
		int content = pane.getViewport().getView().getPreferredSize().height;
		pane.setSize(220, content + insets.top + insets.bottom + 2);
		pane.doLayout();
		assertEquals(JScrollPane.VERTICAL_SCROLLBAR_NEVER, pane.getVerticalScrollBarPolicy());

		pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		pane.getHorizontalScrollBar().setPreferredSize(new Dimension(220, 40));
		pane.doLayout();
		assertTrue(pane.getHorizontalScrollBar().isVisible());
		assertTrue(pane.getHorizontalScrollBar().getHeight() > 2);
		assertEquals(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, pane.getVerticalScrollBarPolicy());
		assertEquals(view, pane.getViewport().getView());
	}
}
