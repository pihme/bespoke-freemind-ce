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

package tests.freemind.cov.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Font;
import java.awt.event.ComponentEvent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.common.ScalableJButton;

/** The component callbacks are empty: the caption and the font stay put. */
@DisplayName("ScalableJButtonBehaviourTest")
class ScalableJButtonBehaviourTest {

	@Test
	void componentCallbacksLeaveTheCaptionAndTheFontAlone() {
		// backwards compatibility
		ScalableJButton button = new ScalableJButton("Go");
		Font font = button.getFont();
		button.componentResized(new ComponentEvent(button, ComponentEvent.COMPONENT_RESIZED));
		button.componentMoved(new ComponentEvent(button, ComponentEvent.COMPONENT_MOVED));
		button.componentShown(new ComponentEvent(button, ComponentEvent.COMPONENT_SHOWN));
		button.componentHidden(new ComponentEvent(button, ComponentEvent.COMPONENT_HIDDEN));
		assertEquals("Go", button.getText());
		assertEquals(font, button.getFont());
		assertTrue(button.isVisible());
	}

	@Test
	void anEmptyButtonHasAnEmptyCaption() {
		// backwards compatibility
		ScalableJButton button = new ScalableJButton();
		assertEquals("", button.getText());
		button.componentResized(new ComponentEvent(button, ComponentEvent.COMPONENT_RESIZED));
		assertEquals("", button.getText());
	}
}
