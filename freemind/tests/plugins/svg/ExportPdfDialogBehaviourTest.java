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

package plugins.svg;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.HeadlessException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ExportPdfDialogBehaviourTest")
class ExportPdfDialogBehaviourTest {

	// The headless unit JVM rejects the dialog constructor, so the window is never shown.
	@Test
	void constructorRejectsAHeadlessDisplay() {
		assertThrows(HeadlessException.class,
				() -> new ExportPdfDialog(null, new String[] { "A4", "LETTER" }, null));
	}
}
