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
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.controller.color.ColorPair;
import freemind.modes.mindmapmode.MindMapToolBar;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Toolbar combos change the selected node and the registered zoom. */
@DisplayName("MindMapToolBarBehaviourTest")
class MindMapToolBarBehaviourTest {

	@Test
	void fontSizeColorAndZoomFollowTheSelectedNode() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Styled");
		opened.mode.select(opened.root, List.of(opened.root));
		MindMapToolBar bar = (MindMapToolBar) opened.mode.getModeToolBar();
		bar.startup();

		Object fonts = field(bar, "fonts");
		String family = otherItem(fonts);
		select(fonts, family);
		assertEquals(family, opened.root.getFontFamilyName());

		select(field(bar, "size"), "24");
		assertEquals("24", opened.root.getFontSize());

		Object colors = field(bar, "colorCombo");
		int colorIndex = selectedIndex(colors) == 0 ? 1 : 0;
		ColorPair pair = (ColorPair) itemAt(colors, colorIndex);
		invoke(colors, "setSelectedIndex", new Class[] { int.class }, colorIndex);
		assertEquals(pair.color, opened.root.getColor());
		bar.selectColor(null);
		assertEquals(freemind.view.mindmapview.MapView.standardNodeTextColor,
				((ColorPair) invoke(colors, "getSelectedItem", new Class[] {}, new Object[] {})).color);
		bar.selectColor(Color.MAGENTA);
		assertEquals(Color.MAGENTA,
				((ColorPair) invoke(colors, "getSelectedItem", new Class[] {}, new Object[] {})).color);

		Object zoom = field(bar, "zoom");
		bar.setZoom(1.5f);
		assertEquals("150%", selectedItem(zoom));
		assertEquals(1.5f, opened.view.getZoom());

		float beforeUserZoom = opened.view.getZoom();
		select(zoom, opened.mode.getText("user_defined_zoom"));
		assertEquals(beforeUserZoom, opened.view.getZoom());
		assertEquals(opened.mode.getText("user_defined_zoom"), selectedItem(zoom));

		select(zoom, "200%");
		assertEquals(2.0f, opened.view.getZoom());
		bar.setZoom(0.33f);
		assertEquals(opened.mode.getText("user_defined_zoom"), selectedItem(zoom));

		opened.app.setZoom(1.5f);
		assertEquals("150%", selectedItem(zoom));
		bar.shutdown();
		opened.app.setZoom(3.0f);
		assertEquals(3.0f, opened.view.getZoom());
		assertNotEquals("300%", selectedItem(zoom));
	}

	private static String otherItem(Object combo) throws Exception {
		Object current = selectedItem(combo);
		int count = (Integer) invoke(combo, "getItemCount", new Class[] {}, new Object[] {});
		for (int i = 0; i < count; i++) {
			Object item = itemAt(combo, i);
			if (item != null && !item.equals(current)) {
				return item.toString();
			}
		}
		return String.valueOf(itemAt(combo, 0));
	}

	private static void select(Object combo, Object item) throws Exception {
		invoke(combo, "setSelectedItem", new Class[] { Object.class }, item);
	}

	private static Object selectedItem(Object combo) throws Exception {
		return invoke(combo, "getSelectedItem", new Class[] {}, new Object[] {});
	}

	private static int selectedIndex(Object combo) throws Exception {
		return (Integer) invoke(combo, "getSelectedIndex", new Class[] {}, new Object[] {});
	}

	private static Object itemAt(Object combo, int index) throws Exception {
		return invoke(combo, "getItemAt", new Class[] { int.class }, index);
	}

	private static Object invoke(Object target, String name, Class<?>[] types, Object... args) throws Exception {
		Method method = target.getClass().getMethod(name, types);
		return method.invoke(target, args);
	}

	private static Object field(MindMapToolBar bar, String name) throws Exception {
		Field found = MindMapToolBar.class.getDeclaredField(name);
		found.setAccessible(true);
		return found.get(bar);
	}
}
