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

package tests.freemind.cov.modes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Map;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.formdev.flatlaf.FlatDarkLaf;

import freemind.main.Resources;
import freemind.modes.MindIcon;
import freemind.view.ScalableImageIcon;

/** Icon names order themselves, cache a factory result, and load an image. */
@DisplayName("MindIconBehaviourTest")
class MindIconBehaviourTest {

	@Test
	void nameKeyAndOrder() {
		// backwards compatibility
		String left = unique("left");
		String right = unique("right");
		MindIcon first = MindIcon.factory(left);
		MindIcon second = MindIcon.factory(right);
		assertEquals("Icon_name: " + left, first.toString());
		assertNull(first.getKeyStroke());
		first.setName(null);
		assertEquals("notfound", first.getName());
		assertTrue(MindIcon.factory("idea").compareTo(MindIcon.factory("help")) < 0);
		assertEquals(0, MindIcon.factory("idea").compareTo(MindIcon.factory("idea")));
		int order = first.compareTo(second);
		assertTrue(order != 0);
		assertEquals(-order, second.compareTo(first));
	}

	@Test
	void factoryCachesAndReturnsTheAssociatedIcon() throws Exception {
		// backwards compatibility
		String name = unique("cached");
		ImageIcon image = new ImageIcon();
		MindIcon created = MindIcon.factory(name, image);
		assertSame(created, MindIcon.factory(name, new ImageIcon()));
		assertSame(image, created.getIcon());
		assertSame(image, created.getUnscaledIcon());

		URL url = Resources.getInstance().getResource("images/IconNotFound.png");
		assertNotNull(url);
		String scalableName = unique("scalable");
		ScalableImageIcon scalable = new ScalableImageIcon(url);
		MindIcon scaled = MindIcon.factory(scalableName, scalable);
		ImageIcon unscaled = scaled.getUnscaledIcon();
		assertNotSame(scalable, unscaled);
		assertNotNull(unscaled);
	}

	@Test
	void unknownNameLoadsAFallbackAndTheNameCacheCanBeReused() throws Exception {
		// backwards compatibility
		String name = unique("missing");
		MindIcon icon = MindIcon.factory(name);
		assertNotNull(icon.getIcon());
		assertSame(icon.getIcon(), icon.getIcon());

		Field names = MindIcon.class.getDeclaredField("mAllIconNames");
		names.setAccessible(true);
		Object previous = names.get(null);
		Vector<String> cached = new Vector<>();
		cached.add("cached-icon");
		names.set(null, cached);
		try {
			assertSame(cached, MindIcon.getAllIconNames());
		} finally {
			names.set(null, previous);
		}
	}

	@Test
	void darkLookAndFeelStillReturnsAnIcon() throws Exception {
		// backwards compatibility
		LookAndFeel previous = UIManager.getLookAndFeel();
		Field created = MindIcon.class.getDeclaredField("createdIcons");
		created.setAccessible(true);
		Map cache = (Map) created.get(null);
		Object removed = cache.remove("idea");
		try {
			try {
				UIManager.setLookAndFeel(new FlatDarkLaf());
			} catch (Exception ignored) {
				// A headless JVM may refuse the look and feel. The icon still loads.
			}
			ImageIcon icon = MindIcon.factory("idea").getIcon();
			assertNotNull(icon);
		} finally {
			if (removed != null && !cache.containsKey("idea")) {
				cache.put("idea", removed);
			}
			if (previous != null) {
				UIManager.setLookAndFeel(previous);
			}
		}
	}

	private static String unique(String stem) {
		return "cov-" + stem + "-" + System.nanoTime();
	}
}
