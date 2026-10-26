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

package tests.freemind.cov.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import plugins.search.Search;

/** One stored mind map produces one search result. A missing term produces none. */
@DisplayName("SearchBehaviourTest")
class SearchBehaviourTest {

	@Test
	void eachHitIsOneResultAndAnAbsentTermIsAnEmptyArray() throws Exception {
		// backwards compatibility
		File dir = Files.createTempDirectory("search-run").toFile();
		dir.deleteOnExit();
		File alpha = new File(dir, "alpha.mm");
		File beta = new File(dir, "beta.mm");
		File other = new File(dir, "other.mm");
		File notes = new File(dir, "notes.txt");
		alpha.deleteOnExit();
		beta.deleteOnExit();
		other.deleteOnExit();
		notes.deleteOnExit();
		Files.writeString(alpha.toPath(), "<map><node TEXT=\"qxvsharedhit\"/></map>");
		Files.writeString(beta.toPath(), "<map><node TEXT=\"qxvsharedhit\"/></map>");
		Files.writeString(other.toPath(), "<map><node TEXT=\"ordinary\"/></map>");
		Files.writeString(notes.toPath(), "qxvsharedhit");

		Search search = new Search(Logger.getLogger(SearchBehaviourTest.class.getName()));
		Search.SearchResult[] hits = search.runSearch("qxvsharedhit", new File[] { dir });
		assertEquals(2, hits.length);
		Set<String> paths = new HashSet<>();
		Set<Integer> ranks = new HashSet<>();
		for (Search.SearchResult hit : hits) {
			paths.add(hit.getPath());
			ranks.add(hit.getRank());
			assertEquals(new File(hit.getPath()).getName(), hit.getFileName());
			assertTrue(hit.getScore() > 0f);
			assertTrue(hit.getDocId() >= 0);
			assertEquals(-1, hit.getShardIndex());
			assertEquals(hit.getPath() + "\n" + hit.getScore(), hit.showDetails());
			assertEquals(hit.getRank() + " (" + hit.getScore() + ") " + hit.getFileName(), hit.toString());
		}
		assertEquals(Set.of(alpha.getPath(), beta.getPath()), paths);
		assertEquals(Set.of(1, 2), ranks);

		Search.SearchResult[] absent = search.runSearch("qxvabsentterm", new File[] { dir });
		assertEquals(0, absent.length);
	}
}
