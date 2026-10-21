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

package tests.freemind.cov.common;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Window;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Properties;

import javax.swing.JDialog;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.jupiter.api.Test;

import freemind.common.XmlBindingTools;
import freemind.controller.actions.generated.instance.NormalWindowConfigurationStorage;
import freemind.controller.actions.generated.instance.WindowConfigurationStorage;
import freemind.main.HeadlessFreeMind;
import freemind.main.Resources;

/** Window geometry round-trips through JAXB. A real dialog cannot be built headless. */
class XmlBindingToolsBehaviourTest {

	@Test
	void marshallAndUnmarshallKeepTheWindowGeometry() throws Exception {
		// backwards compatibility
		XmlBindingTools tools = XmlBindingTools.getInstance();
		assertSame(tools, XmlBindingTools.getInstance());
		Marshaller marshaller = tools.createMarshaller();
		Unmarshaller unmarshaller = tools.createUnmarshaller();
		assertNotNull(marshaller);
		assertNotNull(unmarshaller);
		assertEquals("UTF-8", marshaller.getProperty(Marshaller.JAXB_ENCODING));
		assertEquals(Boolean.FALSE, marshaller.getProperty(Marshaller.JAXB_FORMATTED_OUTPUT));

		NormalWindowConfigurationStorage storage = storage(30, 40, 200, 100);
		String xml = tools.marshall(storage);
		assertNotNull(xml);
		WindowConfigurationStorage restored = (WindowConfigurationStorage) tools.unMarshall(xml);
		assertEquals(30, restored.getX());
		assertEquals(40, restored.getY());
		assertEquals(200, restored.getWidth());
		assertEquals(100, restored.getHeight());

		WindowConfigurationStorage typed = tools.unMarshall(new StringReader(xml), WindowConfigurationStorage.class);
		assertEquals(30, typed.getX());
		assertEquals(100, typed.getHeight());
		assertNull(tools.unMarshall(new StringReader("<nope/>"), WindowConfigurationStorage.class));
		assertNull(tools.unMarshall(brokenReader(), WindowConfigurationStorage.class));
		assertNull(tools.unMarshall("<nope/>"));
	}

	@Test
	void decorateDialogPlacesAMockInsideTheScreenAndRefusesARealFrameHeadless() {
		// backwards compatibility
		new HeadlessFreeMind();
		Properties properties = Resources.getInstance().getProperties();
		String previous = properties.getProperty("place_dialogs_on_first_screen");
		XmlBindingTools tools = XmlBindingTools.getInstance();
		try {
			properties.setProperty("place_dialogs_on_first_screen", "false");
			JDialog inside = mock(JDialog.class);
			WindowConfigurationStorage restored = tools.decorateDialog(tools.marshall(storage(30, 40, 200, 100)), inside);
			assertEquals(30, restored.getX());
			verify(inside).setLocation(30, 40);
			verify(inside).setSize(new Dimension(200, 100));

			JDialog clamped = mock(JDialog.class);
			tools.decorateDialog(tools.marshall(storage(Integer.MAX_VALUE, 50, 10, 12)), clamped);
			verify(clamped).setLocation(Integer.MAX_VALUE - 20, 50);
			verify(clamped).setSize(new Dimension(10, 12));

			properties.setProperty("place_dialogs_on_first_screen", "true");
			JDialog firstScreen = mock(JDialog.class);
			Window[] before = Window.getWindows();
			assertThrows(HeadlessException.class,
					() -> tools.decorateDialog(tools.marshall(storage(30, 40, 200, 100)), firstScreen));
			verify(firstScreen, never()).setLocation(anyInt(), anyInt());

			JDialog missing = mock(JDialog.class);
			assertThrows(HeadlessException.class, () -> tools.decorateDialog((String) null, missing));
			assertThrows(HeadlessException.class, () -> tools.decorateDialog("<nope/>", missing));
			verify(missing, never()).setLocation(anyInt(), anyInt());
			assertArrayEquals(before, Window.getWindows());
		} finally {
			if (previous == null) {
				properties.remove("place_dialogs_on_first_screen");
			} else {
				properties.setProperty("place_dialogs_on_first_screen", previous);
			}
		}
	}

	private static NormalWindowConfigurationStorage storage(int x, int y, int width, int height) {
		NormalWindowConfigurationStorage storage = new NormalWindowConfigurationStorage();
		storage.setX(x);
		storage.setY(y);
		storage.setWidth(width);
		storage.setHeight(height);
		return storage;
	}

	private static Reader brokenReader() {
		return new Reader() {
			public int read(char[] cbuf, int off, int len) throws IOException {
				throw new IOException("broken");
			}

			public void close() {
			}
		};
	}
}
