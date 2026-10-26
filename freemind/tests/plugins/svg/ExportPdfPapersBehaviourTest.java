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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.print.Paper;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ExportPdfPapersBehaviourTest")
class ExportPdfPapersBehaviourTest {

	@Test
	void paperNamesIncludeA4AndLetterAndResolveToAPositiveSize() {
		ExportPdfPapers papers = new ExportPdfPapers();
		String[] names = papers.getPaperNames();
		Set<String> nameSet = new HashSet<>(Arrays.asList(names));

		assertTrue(nameSet.contains("A4"));
		assertTrue(nameSet.contains("LETTER"));
		for (String name : names) {
			Paper paper = papers.determinePaper(name);
			assertNotNull(paper, name);
			assertTrue(paper.getWidth() > 0, name);
			assertTrue(paper.getHeight() > 0, name);
		}
		assertNull(papers.determinePaper("nope"));
	}
}
