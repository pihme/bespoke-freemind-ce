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

package tests.freemind.cov.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.event.KeyEvent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.view.mindmapview.EditNodeExternalApplication;
import freemind.view.mindmapview.NodeView;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("EditNodeExternalApplicationBehaviourTest")
public class EditNodeExternalApplicationBehaviourTest {

	@Test
	void constructorKeepsTheFirstKeyAndTheNodeText() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha");
		NodeView alpha = ViewMaps.childView(opened, "Alpha");
		KeyEvent event = ViewMaps.key(opened.view, KeyEvent.VK_A, 0);
		EditSupports.Recording control = new EditSupports.Recording();
		EditNodeExternalApplication editor = new EditNodeExternalApplication(alpha, "Alpha", event,
				opened.mode, control);
		assertSame(alpha, editor.getNode());
		assertSame(control, editor.getEditControl());
		assertSame(event, ViewMaps.call(editor, "getFirstEvent", new Class<?>[] {}, new Object[] {}));
		assertEquals("Alpha", ViewMaps.call(editor, "getText", new Class<?>[] {}, new Object[] {}));
	}
}
