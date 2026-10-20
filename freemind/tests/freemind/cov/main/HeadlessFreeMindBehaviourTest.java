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

package tests.freemind.cov.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.net.URLClassLoader;

import org.junit.jupiter.api.Test;

import freemind.main.FreeMindMain;
import freemind.main.HeadlessFreeMind;

class HeadlessFreeMindBehaviourTest {

	@Test
	void headlessFrameReturnsDefaultsAndParsesIntegers() throws Exception {
		// backwards compatibility
		HeadlessFreeMind frame = new HeadlessFreeMind();
		assertFalse(frame.isApplet());
		assertNull(frame.getJFrame());
		assertNull(frame.getView());
		frame.setView(null);
		assertNull(frame.getController());
		assertNull(frame.getFreeMindMenuBar());
		assertNull(frame.getResources());
		assertNull(frame.getContentPane());
		assertNull(frame.getLayeredPane());
		assertNull(frame.getContentComponent());
		assertNull(frame.getScrollPane());
		assertNull(frame.insertComponentIntoSplitPane(null));
		frame.removeSplitPane();
		frame.saveProperties(true);
		frame.setDefaultProperty("unused", "value");
		frame.err("ignored");
		frame.openDocument(new URL("file:///tmp/coverage.mm"));
		frame.registerStartupDoneListener(new FreeMindMain.StartupDoneListener() {
			@Override
			public void startupDone() {
			}
		});
		assertEquals(0, frame.getWinX());
		assertEquals(0, frame.getWinY());
		assertEquals(0, frame.getWinWidth());
		assertEquals(0, frame.getWinHeight());
		assertEquals(0, frame.getWinState());
		assertEquals(".", frame.getFreemindDirectory());
		assertEquals(".", frame.getFreemindBaseDir());
		assertTrue(frame.getLoggerList().isEmpty());
		assertNotNull(frame.getLogger(HeadlessFreeMind.class.getName()));
		FreeMindMain.VersionInformation version = frame.getFreemindVersion();
		assertEquals(1, version.mMaj);
		assertEquals(1, version.mMid);
		assertEquals(0, version.mMin);
		frame.setProperty("count", "12");
		assertEquals(12, frame.getIntProperty("count", 0));
		frame.setProperty("count", "nope");
		assertEquals(5, frame.getIntProperty("count", 5));
		assertTrue(frame.getFreeMindClassLoader() instanceof URLClassLoader);
	}
}
