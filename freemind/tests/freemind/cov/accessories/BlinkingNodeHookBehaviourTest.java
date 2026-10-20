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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.EventQueue;
import java.lang.reflect.Constructor;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import accessories.plugins.BlinkingNodeHook;
import freemind.view.mindmapview.NodeView;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

class BlinkingNodeHookBehaviourTest {

	// backwards compatibility
	@Test
	void theTimerCyclesTheNodeColorAndShutdownStopsIt() throws Exception {
		Opened opened = MapFixture.open("Child");
		BlinkingNodeHook hook = new BlinkingNodeHook();
		hook.setController(opened.mode);
		hook.setMap(opened.mode.getMap());
		hook.setProperties(new Properties());
		hook.setNode(opened.child("Child"));
		NodeView view = opened.view.getNodeView(opened.child("Child"));
		view.setVisible(false);
		Class<?> changerType = Class.forName("accessories.plugins.BlinkingNodeHook$TimerColorChanger");
		Constructor<?> constructor = changerType.getDeclaredConstructor(BlinkingNodeHook.class);
		constructor.setAccessible(true);
		Runnable hidden = (Runnable) constructor.newInstance(hook);
		hidden.run();
		EventQueue.invokeAndWait(() -> {
		});
		Color before = view.getMainView().getForeground();

		view.setVisible(true);
		opened.mode.setBlocked(true);
		hidden.run();
		EventQueue.invokeAndWait(() -> {
		});
		assertEquals(before, view.getMainView().getForeground());

		opened.mode.setBlocked(false);
		hook.setNode(null);
		hidden.run();
		EventQueue.invokeAndWait(() -> {
		});
		assertEquals(before, view.getMainView().getForeground());

		hook.setNode(opened.child("Child"));
		hidden.run();
		EventQueue.invokeAndWait(() -> {
		});
		Color first = view.getMainView().getForeground();
		assertTrue(first.equals(Color.BLUE) || first.equals(Color.RED) || first.equals(Color.MAGENTA)
				|| first.equals(Color.CYAN), String.valueOf(first));
		hidden.run();
		EventQueue.invokeAndWait(() -> {
		});
		Color second = view.getMainView().getForeground();
		assertTrue(!second.equals(first), second + " vs " + first);

		hook.invoke(opened.child("Child"));
		hook.shutdownMapHook();
		assertEquals(Color.BLACK, view.getMainView().getForeground());
	}
}
