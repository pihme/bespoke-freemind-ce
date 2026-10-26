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

package tests.freemind.cov.xslt;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Window;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import accessories.plugins.util.xslt.ExportDialog;
import freemind.main.FreeMindMain;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** The dialog reads its title, then headless construction stops before a window exists. */
@DisplayName("ExportDialogBehaviourTest")
class ExportDialogBehaviourTest {

	@Test
	void constructionReadsTheTitleThenThrowsHeadlessException() throws Exception {
		// backwards compatibility
		assertTrue(GraphicsEnvironment.isHeadless());
		Opened opened = MapFixture.open("Alpha");
		File xml = new File("missing-export.mm");
		assertFalse(xml.exists());
		List<String> asked = new ArrayList<>();
		FreeMindMain recording = recordingFrame(opened.frame, asked);
		Window[] before = Window.getWindows();
		opened.app.setFrame(recording);
		try {
			HeadlessException thrown = assertThrows(HeadlessException.class,
					() -> new ExportDialog(xml, opened.mode));

			assertEquals(HeadlessException.class, thrown.getClass());
			assertEquals(List.of("ExportDialog.2"), asked);
			assertArrayEquals(before, Window.getWindows());
			assertFalse(xml.exists());
		} finally {
			opened.app.setFrame(opened.frame);
		}
	}

	private static FreeMindMain recordingFrame(FreeMindMain delegate, List<String> asked) {
		return (FreeMindMain) Proxy.newProxyInstance(FreeMindMain.class.getClassLoader(),
				new Class<?>[] { FreeMindMain.class }, (proxy, method, args) -> {
					if (method.getDeclaringClass() == Object.class) {
						if ("hashCode".equals(method.getName())) {
							return System.identityHashCode(proxy);
						}
						if ("equals".equals(method.getName())) {
							return proxy == args[0];
						}
						if ("toString".equals(method.getName())) {
							return "recording frame";
						}
					}
					if ("getResourceString".equals(method.getName()) && args != null && args.length > 0) {
						asked.add(String.valueOf(args[0]));
					}
					try {
						return method.invoke(delegate, args);
					} catch (InvocationTargetException ex) {
						Throwable cause = ex.getCause();
						if (cause instanceof RuntimeException runtime) {
							throw runtime;
						}
						if (cause instanceof Error error) {
							throw error;
						}
						throw ex;
					}
				});
	}
}
