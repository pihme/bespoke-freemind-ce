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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.logging.Logger;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import plugins.search.FileSearchModel;

/** An index stores mind-map paths. A folder with no maps stores nothing. */
@DisplayName("FileSearchModelBehaviourTest")
class FileSearchModelBehaviourTest {

	@Test
	void theMatchingMapPathAndNameAreStoredAndTheOtherFileIsNotAHit() throws Exception {
		// backwards compatibility
		File dir = Files.createTempDirectory("file-search-model").toFile();
		dir.deleteOnExit();
		File matched = new File(dir, "matched.mm");
		File other = new File(dir, "other.mm");
		File notes = new File(dir, "notes.txt");
		matched.deleteOnExit();
		other.deleteOnExit();
		notes.deleteOnExit();
		Files.writeString(matched.toPath(), "<map><node TEXT=\"qxvmodelhit\"/></map>");
		Files.writeString(other.toPath(), "<map><node TEXT=\"qxvmodelother\"/></map>");
		Files.writeString(notes.toPath(), "qxvmodelhit");

		FileSearchModel model = newModel();
		Directory index = model.indexFileOrDirectory(dir.getPath());
		IndexSearcher searcher = searcher(model, index);
		try {
			assertEquals(2, searcher.getIndexReader().numDocs());
			TopDocs found = model.doSearch(query(model, "qxvmodelhit"), 10, searcher);
			assertEquals(1, found.scoreDocs.length);
			Document document = searcher.storedFields().document(found.scoreDocs[0].doc);
			assertEquals(matched.getPath(), model.getPath(document));
			assertEquals(matched.getName(), model.getFilename(document));

			TopDocs otherHit = model.doSearch(query(model, "qxvmodelother"), 10, searcher);
			assertEquals(1, otherHit.scoreDocs.length);
			Document otherDocument = searcher.storedFields().document(otherHit.scoreDocs[0].doc);
			assertEquals(other.getPath(), model.getPath(otherDocument));
			assertEquals(other.getName(), model.getFilename(otherDocument));
		} finally {
			searcher.getIndexReader().close();
		}
	}

	@Test
	void anEmptyDirectoryIndexesZeroDocuments() throws Exception {
		// backwards compatibility
		File dir = Files.createTempDirectory("file-search-empty").toFile();
		dir.deleteOnExit();
		FileSearchModel model = newModel();
		int added = model.indexFiles(writer());
		assertEquals(0, added);

		Directory index = model.indexFileOrDirectory(dir);
		IndexSearcher searcher = searcher(model, index);
		try {
			assertEquals(0, searcher.getIndexReader().numDocs());
			TopDocs found = model.doSearch(query(model, "qxvempty"), 10, searcher);
			assertEquals(0, found.scoreDocs.length);
		} finally {
			searcher.getIndexReader().close();
		}
	}

	@Test
	void aMissingPathIsRefusedAndAMissingNonMapIndexesNothing() throws Exception {
		// backwards compatibility
		File dir = Files.createTempDirectory("file-search-missing").toFile();
		dir.deleteOnExit();
		FileSearchModel model = newModel();
		File missing = new File(dir, "missing.mm");
		FileNotFoundException thrown = assertThrows(FileNotFoundException.class,
				() -> model.indexFileOrDirectory(missing.getPath()));
		assertEquals("Can't open " + missing.getPath(), thrown.getMessage());

		Directory index = model.indexFileOrDirectory(new File(dir, "missing.txt"));
		IndexSearcher searcher = searcher(model, index);
		try {
			assertEquals(0, searcher.getIndexReader().numDocs());
		} finally {
			searcher.getIndexReader().close();
		}
	}

	private static IndexWriter writer() throws Exception {
		return new IndexWriter(new ByteBuffersDirectory(), new IndexWriterConfig());
	}

	private static FileSearchModel newModel() throws Exception {
		Constructor<FileSearchModel> constructor = FileSearchModel.class.getDeclaredConstructor(Logger.class);
		constructor.setAccessible(true);
		return constructor.newInstance(Logger.getLogger(FileSearchModelBehaviourTest.class.getName()));
	}

	private static Query query(FileSearchModel model, String text) throws Exception {
		Method method = FileSearchModel.class.getDeclaredMethod("getQuery", String.class);
		method.setAccessible(true);
		return (Query) invoke(method, model, text);
	}

	private static IndexSearcher searcher(FileSearchModel model, Directory index) throws Exception {
		Method method = FileSearchModel.class.getDeclaredMethod("getSearcher", Directory.class);
		method.setAccessible(true);
		return (IndexSearcher) invoke(method, model, index);
	}

	private static Object invoke(Method method, Object target, Object... args) throws Exception {
		try {
			return method.invoke(target, args);
		} catch (InvocationTargetException ex) {
			if (ex.getCause() instanceof Exception cause) {
				throw cause;
			}
			throw ex;
		}
	}
}
