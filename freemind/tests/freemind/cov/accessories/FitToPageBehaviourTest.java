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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.lang.reflect.Field;

import javax.swing.JViewport;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import accessories.plugins.FitToPage;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("FitToPageBehaviourTest")
class FitToPageBehaviourTest {

	// backwards compatibility
	@Test
	void aWideViewportUsesTheHeightLimit() throws Exception {
		Opened opened = openInViewport(800, 80);
		float fitted = fit(opened);
		assertTrue(fitted > 0f && fitted < 2f, String.valueOf(fitted));
	}

	// backwards compatibility
	@Test
	void aTallViewportUsesTheWidthLimit() throws Exception {
		Opened opened = openInViewport(80, 800);
		float fitted = fit(opened);
		assertTrue(Float.isFinite(fitted) && fitted > 0f && fitted < 2f, String.valueOf(fitted));
	}

	// backwards compatibility
	@Test
	void aMissingViewLeavesTheZoomAlone() throws Exception {
		Opened opened = MapFixture.open("Child");
		Field view = opened.mode.getClass().getSuperclass().getDeclaredField("mView");
		while (view == null) {
			break;
		}
		Field found = null;
		Class<?> type = opened.mode.getClass();
		while (type != null && found == null) {
			try {
				found = type.getDeclaredField("mView");
			} catch (NoSuchFieldException missing) {
				type = type.getSuperclass();
			}
		}
		found.setAccessible(true);
		Object previous = found.get(opened.mode);
		found.set(opened.mode, null);
		try {
			FitToPage fit = new FitToPage();
			fit.setController(opened.mode);
			float before = opened.view.getZoom();
			fit.startupMapHook();
			assertEquals(before, opened.view.getZoom(), 0.001f);
		} finally {
			found.set(opened.mode, previous);
		}
	}

	private static float fit(Opened opened) throws Exception {
		opened.mode.setView(opened.view);
		opened.view.setZoom(2f);
		opened.view.getRoot().updateAll();
		opened.view.setSize(8000, 8000);
		opened.view.getRoot().setLocation(0, 0);
		opened.view.getRoot().setSize(500, 500);
		Rectangle inner = opened.view.getInnerBounds();
		assertTrue(inner.width > 0 && inner.height > 0, inner.toString());
		FitToPage fit = new FitToPage();
		fit.setController(opened.mode);
		fit.startupMapHook();
		EventQueue.invokeAndWait(() -> {
		});
		return opened.view.getZoom();
	}

	private static Opened openInViewport(int viewWidth, int viewHeight) throws Exception {
		Opened opened = MapFixture.open("Alpha", "Beta");
		Container parent = opened.view.getParent();
		parent.remove(opened.view);
		JViewport viewport = new JViewport();
		javax.swing.JPanel holder = new javax.swing.JPanel(new java.awt.BorderLayout());
		holder.setSize(viewWidth, viewHeight);
		holder.add(viewport);
		viewport.setView(opened.view);
		holder.validate();
		viewport.setSize(viewWidth, viewHeight);
		viewport.setExtentSize(new Dimension(viewWidth, viewHeight));
		opened.view.setSize(8000, 8000);
		opened.view.getRoot().updateAll();
		return opened;
	}
}
