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

package tests.freemind.cov.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.ActionEvent;
import java.util.List;

import org.junit.jupiter.api.Test;

import freemind.controller.actions.generated.instance.Pattern;
import freemind.controller.actions.generated.instance.PatternNodeFontItalic;
import freemind.modes.MindMapNode;
import freemind.modes.StylePatternFactory;
import freemind.modes.mindmapmode.actions.ApplyPatternAction;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** A named italic pattern marks the selected node, and width 2 stays the digit string. */
class ApplyPatternActionBehaviourTest {

	@Test
	void italicPatternMarksTheNode() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Leaf");
		MindMapNode leaf = opened.child("Leaf");
		Pattern pattern = new Pattern();
		pattern.setName("Slant");
		PatternNodeFontItalic italic = new PatternNodeFontItalic();
		italic.setValue(StylePatternFactory.TRUE_VALUE);
		pattern.setPatternNodeFontItalic(italic);
		ApplyPatternAction action = new ApplyPatternAction(opened.mode, pattern);
		assertSame(pattern, action.getPattern());
		opened.mode.select(leaf, List.of(leaf));
		action.actionPerformed(new ActionEvent(opened.view, ActionEvent.ACTION_PERFORMED, "pattern"));
		assertTrue(leaf.isItalic());
		assertEquals("2", ApplyPatternAction.edgeWidthIntToString(2));
	}
}
