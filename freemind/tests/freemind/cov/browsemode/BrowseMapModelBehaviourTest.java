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

package tests.freemind.cov.browsemode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.StringWriter;
import java.net.URL;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import freemind.modes.MindMap;
import freemind.modes.NodeAdapter;
import freemind.modes.browsemode.BrowseArrowLinkModel;
import freemind.modes.browsemode.BrowseCloudModel;
import freemind.modes.browsemode.BrowseEdgeModel;
import freemind.modes.browsemode.BrowseMapModel;
import freemind.modes.browsemode.BrowseNodeModel;
import freemind.modes.browsemode.EncryptedBrowseNode;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** A browse map names its root, refuses to write XML, and builds browse nodes. */
@DisplayName("BrowseMapModelBehaviourTest")
class BrowseMapModelBehaviourTest {

	@Test
	void aBrowseMapKeepsItsRootAndBuildsBrowseParts() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open();
		BrowseMapModel fresh = null;
		BrowseMapModel given = null;
		try {
			fresh = new BrowseMapModel(null, opened.mode);
			assertEquals("new_mindmap", fresh.getRootNode().getText());
			assertNull(fresh.toString());
			assertNull(fresh.getFile());
			assertNotNull(fresh.getLinkRegistry());
			assertTrue(fresh.save(new File("unused.mm")));
			fresh.setLinkInclinationChanged();
			assertTrue(fresh.isSaved());
			BrowseMapModel map = fresh;
			RuntimeException xml = assertThrows(RuntimeException.class,
					() -> map.getXml(new StringWriter()));
			assertEquals("Unimplemented method called.", xml.getMessage());
			RuntimeException filtered = assertThrows(RuntimeException.class,
					() -> map.getFilteredXml(new StringWriter()));
			assertEquals("Unimplemented method called.", filtered.getMessage());

			BrowseNodeModel root = new BrowseNodeModel("Given", fresh);
			given = new BrowseMapModel(root, opened.mode);
			assertSame(root, given.getRootNode());
			URL url = new URL("file:/tmp/browse.mm");
			given.setURL(url);
			assertEquals(url.toString(), given.toString());
			assertSame(url, given.getURL());

			NodeAdapter plain = given.createNodeAdapter((MindMap) given,
					EncryptedBrowseNode.class.getName());
			assertTrue(plain instanceof BrowseNodeModel);
			assertFalse(plain instanceof EncryptedBrowseNode);
			assertTrue(given.createEdgeAdapter(plain) instanceof BrowseEdgeModel);
			assertTrue(given.createCloudAdapter(plain) instanceof BrowseCloudModel);
			BrowseArrowLinkModel link = (BrowseArrowLinkModel) given.createArrowLinkAdapter(plain, plain);
			assertSame(plain, link.getSource());
			assertSame(plain, link.getTarget());
			assertNull(given.createArrowLinkTarget(plain, plain));

			NodeAdapter encrypted = given.createEncryptedNode("secret");
			assertTrue(encrypted instanceof EncryptedBrowseNode);
			assertNull(encrypted.getAdditionalInfo());
			assertNotNull(encrypted.getStateIcons().get("encryptedNode"));
		} finally {
			if (given != null) {
				given.destroy();
			}
			if (fresh != null) {
				fresh.destroy();
			}
		}
	}
}
