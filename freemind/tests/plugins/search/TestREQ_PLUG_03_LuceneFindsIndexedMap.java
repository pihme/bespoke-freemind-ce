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

package plugins.search;

import java.io.File;
import java.nio.file.Files;
import java.util.logging.Logger;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Lucene index of .mm files. A hit returns that file. A missing token returns none.
 */
class TestREQ_PLUG_03_LuceneFindsIndexedMap {

	@Test
	void TestREQ_PLUG_03_IndexFindsOneMapAndMissesTheOther() throws Exception {
		File dir = Files.createTempDirectory("lucene-maps").toFile();
		dir.deleteOnExit();
		File hit = new File(dir, "hit.mm");
		File miss = new File(dir, "miss.mm");
		Files.writeString(hit.toPath(), "<map><node TEXT=\"qxvbranchtoken\"/></map>");
		Files.writeString(miss.toPath(), "<map><node TEXT=\"ordinary\"/></map>");
		File notes = new File(dir, "notes.txt");
		Files.writeString(notes.toPath(), "qxvbranchtoken");

		FileSearchModel model = new FileSearchModel(Logger.getLogger("TestREQ_PLUG_03"));
		Directory index = model.indexFileOrDirectory(dir);
		IndexSearcher searcher = model.getSearcher(index);
		try {
			TopDocs found = model.doSearch(model.getQuery("qxvbranchtoken"), 10, searcher);
			String[] paths = model.getFilepathsFromSearchResults(searcher, found);
			assertEquals(1, paths.length);
			assertTrue(paths[0].endsWith("hit.mm"));

			TopDocs absent = model.doSearch(model.getQuery("qxvmissingtoken"), 10, searcher);
			assertEquals(0, model.getFilepathsFromSearchResults(searcher, absent).length);
		} finally {
			searcher.getIndexReader().close();
		}
	}
}
