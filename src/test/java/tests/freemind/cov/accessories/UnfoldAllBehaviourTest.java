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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.InputEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import accessories.plugins.UnfoldAll;
import freemind.modes.MindMapNode;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("UnfoldAllBehaviourTest")
class UnfoldAllBehaviourTest {

	// backwards compatibility
	@Test
	void oneStageFoldAndUnfoldChangeOnlyTheDeepestOpenLevel() throws Exception {
		Opened opened = MapFixture.open("Parent");
		MindMapNode parent = opened.child("Parent");
		MindMapNode leaf = opened.mode.addNewNode(parent, 0, false);
		opened.mode.setNodeText(leaf, "Leaf");
		MindMapNode grand = opened.mode.addNewNode(leaf, 0, false);
		opened.mode.setNodeText(grand, "Grand");
		opened.mode.setFolded(leaf, true);
		opened.mode.setFolded(parent, true);
		UnfoldAll hook = hook(opened, "false", "One", "node");

		hook.invoke(parent);

		assertFalse(parent.isFolded());
		assertTrue(leaf.isFolded());

		hook(opened, "true", "One", "root").invoke(leaf);
		assertTrue(parent.isFolded() || leaf.isFolded());
	}

	// backwards compatibility
	@Test
	void theAltWheelFoldsAndUnfoldsFromTheRoot() throws Exception {
		Opened opened = MapFixture.open("Parent");
		MindMapNode parent = opened.child("Parent");
		MindMapNode leaf = opened.mode.addNewNode(parent, 0, false);
		opened.mode.setNodeText(leaf, "Leaf");
		opened.mode.setFolded(parent, true);
		UnfoldAll.Registration registration = new UnfoldAll.Registration(opened.mode, opened.mode.getMap());
		registration.register();
		try {
			assertFalse(registration.handleMouseWheelEvent(wheel(opened, 0)));
			assertTrue(parent.isFolded());

			assertTrue(registration.handleMouseWheelEvent(wheel(opened, 1)));
			assertFalse(parent.isFolded());

			assertTrue(registration.handleMouseWheelEvent(wheel(opened, -1)));
			assertTrue(parent.isFolded());
		} finally {
			registration.deRegister();
		}
	}

	private static UnfoldAll hook(Opened opened, String state, String type, String applyTo) {
		UnfoldAll hook = new UnfoldAll();
		hook.setController(opened.mode);
		hook.setMap(opened.mode.getMap());
		Properties properties = new Properties();
		properties.setProperty("foldingState", state);
		properties.setProperty("foldingType", type);
		properties.setProperty("applyTo", applyTo);
		hook.setProperties(properties);
		return hook;
	}

	private static MouseWheelEvent wheel(Opened opened, int rotation) {
		return new MouseWheelEvent(opened.view, MouseWheelEvent.MOUSE_WHEEL, System.currentTimeMillis(),
				rotation == 0 ? 0 : InputEvent.ALT_MASK, 10, 10, 0, false, MouseWheelEvent.WHEEL_UNIT_SCROLL, 1,
				rotation);
	}
}
