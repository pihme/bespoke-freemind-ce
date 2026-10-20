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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import freemind.main.ExampleFileFilter;

class ExampleFileFilterBehaviourTest {

	@Test
	void descriptionListsExtensionsUntilThatListIsTurnedOff() throws Exception {
		// backwards compatibility
		ExampleFileFilter images = new ExampleFileFilter("jpg", "JPEG");
		images.addExtension("GIF");
		String described = images.getDescription();
		assertTrue(described.startsWith("JPEG ("));
		assertTrue(described.contains(".jpg"));
		assertTrue(described.contains(".gif"));
		assertTrue(images.accept(new File("photo.JPG")));
		assertTrue(images.accept(new File("anim.gif")));
		assertFalse(images.accept(new File("notes.txt")));
		assertFalse(images.accept(null));
		assertTrue(images.accept(new File(".")));

		images.setExtensionListInDescription(false);
		assertFalse(images.isExtensionListInDescription());
		assertEquals("JPEG", images.getDescription());
		assertEquals("JPEG", images.getDescription());

		ExampleFileFilter bare = new ExampleFileFilter("png");
		assertTrue(bare.getDescription().startsWith("("));
		assertTrue(bare.getDescription().contains(".png"));

		ExampleFileFilter several = new ExampleFileFilter(new String[] { "mm", "txt" }, "Maps");
		assertTrue(several.accept(new File("map.mm")));
		assertTrue(several.getDescription().contains("Maps"));

		ExampleFileFilter empty = new ExampleFileFilter();
		assertThrows(NoSuchElementException.class, empty::getDescription);
		Field filters = ExampleFileFilter.class.getDeclaredField("filters");
		filters.setAccessible(true);
		filters.set(empty, null);
		empty.addExtension("Txt");
		assertTrue(empty.accept(new File("a.txt")));
		assertEquals("txt", empty.getExtension(new File("A.TXT")));
		assertNull(empty.getExtension(new File("noext")));
		assertNull(empty.getExtension(new File(".hidden")));
		assertNull(empty.getExtension(null));
	}
}
