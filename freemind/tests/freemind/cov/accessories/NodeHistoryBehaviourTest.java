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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.EventQueue;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JMenuItem;

import org.junit.jupiter.api.Test;

import accessories.plugins.NodeHistory;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.actions.NodeHookAction;
import freemind.view.mindmapview.NodeView;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

class NodeHistoryBehaviourTest {

	// backwards compatibility
	@Test
	void backAndForwardSelectTheRecordedNodesAndADuplicateIsIgnored() throws Exception {
		Opened opened = MapFixture.open("Alpha", "Beta");
		opened.view.getRoot().updateAll();
		resetHistory();
		NodeHistory.Registration registration = registration(opened);
		NodeView rootView = opened.view.getNodeView(opened.root);
		NodeView alphaView = opened.view.getNodeView(opened.child("Alpha"));
		NodeView betaView = opened.view.getNodeView(opened.child("Beta"));
		registration.onFocusNode(rootView);
		registration.onFocusNode(rootView);
		assertEquals(1, historySize());
		registration.onFocusNode(alphaView);
		registration.onFocusNode(betaView);
		assertEquals(3, historySize());
		assertFalse(registration.isEnabled(new JMenuItem(),
				new NodeHookAction("accessories/plugins/NodeHistoryForward.properties", opened.mode)));
		assertTrue(registration.isEnabled(new JMenuItem(),
				new NodeHookAction("accessories/plugins/NodeHistoryBack.properties", opened.mode)));

		NodeHistory back = history(opened, "back");
		back.invoke(opened.child("Beta"));
		EventQueue.invokeAndWait(() -> {
		});
		assertEquals("Alpha", opened.mode.getSelected().getText());
		assertFalse(prevented());

		NodeHistory forward = history(opened, "forward");
		forward.invoke(opened.child("Alpha"));
		EventQueue.invokeAndWait(() -> {
		});
		assertEquals("Beta", opened.mode.getSelected().getText());

		int size = historySize();
		registration.onSaveNode(opened.root);
		assertEquals(size, historySize());
		Method print = NodeHistory.class.getDeclaredMethod("printVector");
		print.setAccessible(true);
		print.invoke(back);
		assertEquals(size, historySize());
	}

	// backwards compatibility
	@Test
	void theTailIsDroppedTheListStopsAtOneHundredAndAMissingMapIsSkipped() throws Exception {
		Opened opened = MapFixture.open("Alpha", "Beta");
		opened.view.getRoot().updateAll();
		resetHistory();
		NodeHistory.Registration registration = registration(opened);
		NodeView alphaView = opened.view.getNodeView(opened.child("Alpha"));
		NodeView betaView = opened.view.getNodeView(opened.child("Beta"));
		registration.onFocusNode(alphaView);
		registration.onFocusNode(betaView);
		setPosition(1);
		registration.onFocusNode(opened.view.getNodeView(opened.root));
		assertEquals(2, historySize());
		assertEquals(2, position());

		resetHistory();
		for (int i = 0; i < 101; i++) {
			registration.onFocusNode(i % 2 == 0 ? alphaView : betaView);
		}
		assertEquals(100, historySize());
		assertEquals(100, position());

		resetHistory();
		registration.onFocusNode(alphaView);
		registration.onFocusNode(betaView);
		setPosition(1);
		Field name = Class.forName("accessories.plugins.NodeHistory$NodeHolder").getDeclaredField("mMapModuleName");
		name.setAccessible(true);
		Vector<?> vector = vector();
		name.set(vector.get(1), "closed-map");
		Method getNode = vector.get(1).getClass().getDeclaredMethod("getNode", freemind.controller.Controller.class);
		getNode.setAccessible(true);
		assertNull(getNode.invoke(vector.get(1), opened.app));

		history(opened, "forward").invoke(opened.child("Alpha"));
		EventQueue.invokeAndWait(() -> {
		});
		assertEquals(2, position());
		assertFalse(prevented());

		int before = historySize();
		registration.deRegister();
		try {
			opened.view.selectAsTheOnlyOneSelected(alphaView);
			assertEquals(before, historySize());
		} finally {
			registration.register();
		}
	}

	private static NodeHistory history(Opened opened, String direction) {
		NodeHistory history = new NodeHistory();
		history.setController(opened.mode);
		history.setMap(opened.mode.getMap());
		Properties properties = new Properties();
		properties.setProperty("direction", direction);
		history.setProperties(properties);
		return history;
	}

	private static NodeHistory.Registration registration(Opened opened) {
		NodeHistory.Registration registration = new NodeHistory.Registration(opened.mode, opened.mode.getMap());
		registration.register();
		return registration;
	}

	private static void resetHistory() throws Exception {
		vector().clear();
		setPosition(0);
		Field prevent = NodeHistory.class.getDeclaredField("sPreventRegistration");
		prevent.setAccessible(true);
		prevent.setBoolean(null, false);
	}

	@SuppressWarnings("unchecked")
	private static Vector<Object> vector() throws Exception {
		Field vector = NodeHistory.class.getDeclaredField("sNodeVector");
		vector.setAccessible(true);
		return (Vector<Object>) vector.get(null);
	}

	private static int historySize() throws Exception {
		return vector().size();
	}

	private static int position() throws Exception {
		Field position = NodeHistory.class.getDeclaredField("sCurrentPosition");
		position.setAccessible(true);
		return position.getInt(null);
	}

	private static void setPosition(int value) throws Exception {
		Field position = NodeHistory.class.getDeclaredField("sCurrentPosition");
		position.setAccessible(true);
		position.setInt(null, value);
	}

	private static boolean prevented() throws Exception {
		Field prevent = NodeHistory.class.getDeclaredField("sPreventRegistration");
		prevent.setAccessible(true);
		return prevent.getBoolean(null);
	}
}
