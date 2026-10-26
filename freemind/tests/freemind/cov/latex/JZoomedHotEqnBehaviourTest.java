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

package tests.freemind.cov.latex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import plugins.latex.JZoomedHotEqn;
import plugins.latex.LatexNodeHook;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** A zoomed equation component shows the formula stored on its hook. */
@DisplayName("JZoomedHotEqnBehaviourTest")
class JZoomedHotEqnBehaviourTest {

	@Test
	void equationTextRoundTripsAndThePreferredSizeIsNonEmpty() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha");
		JZoomedHotEqn eqn = component(attach(opened, "E = mc^{2}"));
		assertEquals("E = mc^{2}", eqn.getEquation());
		assertNonEmpty(eqn.getPreferredSize());

		eqn.setEquation("a+b");
		assertEquals("a+b", eqn.getEquation());
		assertNonEmpty(eqn.getPreferredSize());

		eqn.setModel(attach(opened, "c=d"));
		assertEquals("c=d", eqn.getEquation());
		assertNonEmpty(eqn.getPreferredSize());

		assertEquals(1, eqn.getMouseListeners().length);
		MouseEvent ignored = new MouseEvent(eqn, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 1, 1, 1,
				false, MouseEvent.BUTTON3);
		for (MouseListener listener : eqn.getMouseListeners()) {
			listener.mouseClicked(ignored);
		}
		assertFalse(ignored.isConsumed());
		assertEquals("c=d", eqn.getEquation());
	}

	@Test
	void aBrokenFormulaKeepsItsTextAndStillHasASize() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha");
		JZoomedHotEqn eqn = component(attach(opened, "E = mc^{2}"));
		eqn.setEquation("\\frac{");
		assertEquals("\\frac{", eqn.getEquation());
		assertNonEmpty(eqn.getPreferredSize());
	}

	@Test
	void aLargerZoomMakesThePreferredSizeLarger() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha");
		JZoomedHotEqn eqn = component(attach(opened, "y"));
		double previous = zoom();
		try {
			zoom(2.0);
			assertEquals(2.0, zoom());
			eqn.setEquation("y");
			Dimension larger = eqn.getPreferredSize();
			zoom(1.0);
			eqn.setEquation("y");
			Dimension smaller = eqn.getPreferredSize();
			assertNonEmpty(larger);
			assertNonEmpty(smaller);
			assertTrue(larger.width > smaller.width || larger.height > smaller.height,
					larger.width + "x" + larger.height + " vs " + smaller.width + "x" + smaller.height);
		} finally {
			zoom(previous);
		}
	}

	@Test
	void paintRecordsInkOrReportsThatAPeerIsRequired() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Alpha");
		JZoomedHotEqn eqn = component(attach(opened, "E = mc^{2}"));
		eqn.setEquation("x");
		assertEquals("x", eqn.getEquation());
		Dimension size = eqn.getPreferredSize();
		assertNonEmpty(size);
		eqn.setSize(size);
		try {
			assertTrue(paintedPixels(eqn) > 0);
		} catch (HeadlessException ex) {
			assertEquals(HeadlessException.class, ex.getClass());
		}
	}

	private static LatexNodeHook attach(Opened opened, String equation) {
		LatexNodeHook hook = new LatexNodeHook();
		hook.setName("plugins/latex/LatexNodeHook.properties");
		hook.setController(opened.mode);
		hook.setMap(opened.mode.getMap());
		hook.setNode(opened.child("Alpha"));
		if (equation != null) {
			hook.setContent("formula", equation);
		}
		return hook;
	}

	private static JZoomedHotEqn component(LatexNodeHook hook) throws Exception {
		Field title = JZoomedHotEqn.class.getDeclaredField("editorTitle");
		title.setAccessible(true);
		Object previous = title.get(null);
		title.set(null, null);
		try {
			Constructor<JZoomedHotEqn> constructor = JZoomedHotEqn.class.getDeclaredConstructor(LatexNodeHook.class);
			constructor.setAccessible(true);
			return constructor.newInstance(hook);
		} finally {
			if (title.get(null) == null) {
				title.set(null, previous);
			}
		}
	}

	private static void zoom(double value) throws Exception {
		Method setZoom = JZoomedHotEqn.class.getDeclaredMethod("setZoom", double.class);
		setZoom.setAccessible(true);
		setZoom.invoke(null, value);
	}

	private static double zoom() throws Exception {
		Method getZoom = JZoomedHotEqn.class.getDeclaredMethod("getZoom");
		getZoom.setAccessible(true);
		return (Double) getZoom.invoke(null);
	}

	private static void assertNonEmpty(Dimension size) {
		assertTrue(size.width > 0 && size.height > 0, size.width + "x" + size.height);
	}

	private static int paintedPixels(JZoomedHotEqn eqn) throws Exception {
		Dimension size = eqn.getPreferredSize();
		int width = Math.max(size.width, 1);
		int height = Math.max(size.height, 1);
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		try {
			Method paint = JZoomedHotEqn.class.getDeclaredMethod("paintComponent", Graphics.class);
			paint.setAccessible(true);
			try {
				paint.invoke(eqn, graphics);
			} catch (InvocationTargetException ex) {
				if (ex.getCause() instanceof HeadlessException headless) {
					throw headless;
				}
				if (ex.getCause() instanceof Exception checked) {
					throw checked;
				}
				throw ex;
			}
		} finally {
			graphics.dispose();
		}
		int ink = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (image.getRGB(x, y) != 0) {
					ink++;
				}
			}
		}
		return ink;
	}
}
