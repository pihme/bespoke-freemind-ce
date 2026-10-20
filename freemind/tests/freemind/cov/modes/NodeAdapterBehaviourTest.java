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

package tests.freemind.cov.modes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.StringWriter;
import java.util.ListIterator;
import java.util.SortedMap;

import org.junit.jupiter.api.Test;

import freemind.controller.Controller;
import freemind.controller.filter.Filter;
import freemind.extensions.DontSaveMarker;
import freemind.extensions.PermanentNodeHookAdapter;
import freemind.main.FreeMind;
import freemind.main.FreeMindCommon;
import freemind.main.Resources;
import freemind.modes.MindMap;
import freemind.modes.MindMapLinkRegistry;
import freemind.modes.MindMapNode;
import freemind.modes.NodeAdapter;
import freemind.modes.attributes.Attribute;
import freemind.modes.mindmapmode.MindMapCloudModel;
import freemind.modes.mindmapmode.MindMapNodeModel;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Base node text, style, hooks, attributes, and save keep their old results. */
class NodeAdapterBehaviourTest {

	@Test
	void emptyNodeReportsPlainTextAndRefusesEnumeration() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		BareNode bare = new BareNode("Plain words", opened.mode.getMap());
		assertEquals("Plain words", bare.getPlainTextContent());
		assertThrows(UnsupportedOperationException.class, bare::children);
		assertEquals(0, bare.getChildCount());
		assertTrue(bare.getChildren().isEmpty());
		assertFalse(bare.childrenUnfolded().hasNext());
		assertNull(bare.sortedChildrenUnfolded());
		assertTrue(bare.getAllowsChildren());
		bare.setAdditionalInfo("secret");
		assertNull(bare.getAdditionalInfo());
		assertEquals(0, bare.calcShiftY());
		assertEquals(-1, bare.getAttributePosition("missing"));
		assertNull(bare.getAttribute("missing"));
		assertTrue(bare.getAttributeKeyList().isEmpty());
		assertThrows(IllegalArgumentException.class, () -> bare.checkAttributePosition(0));
	}

	@Test
	void shortTextNotesAttributesAndStrikeStayOnTheNode() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode node = opened.child("Child");
		String longText = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghij";
		node.setText(longText);
		assertEquals(longText.substring(0, 40) + " ...", node.getShortText(opened.mode));
		node.setText("Short");
		assertEquals("Short", node.getShortText(opened.mode));
		node.setXmlNoteText(null);
		assertNull(node.getXmlNoteText());
		assertNull(node.getNoteText());
		node.setXmlNoteText("<html><body><p>Noted</p></body></html>");
		assertNotNull(node.getNoteText());
		assertTrue(node.getXmlNoteText().contains("Noted") || node.getNoteText().contains("Noted"));
		assertEquals(-1, node.getAttributePosition("Kind"));
		int added = node.addAttribute(new Attribute("Kind", "Note"));
		assertEquals(0, added);
		node.insertAttribute(0, new Attribute("Rank", "1"));
		assertEquals("1", node.getAttribute("Rank"));
		assertEquals(0, node.getAttributePosition("Rank"));
		assertEquals(1, node.getAttributePosition("Kind"));
		assertTrue(node.getAttributeKeyList().contains("Kind"));
		assertNull(node.getAttribute("missing"));
		assertEquals(-1, node.getAttributePosition("missing"));
		NodeAdapter adapted = (NodeAdapter) node;
		assertThrows(IllegalArgumentException.class, () -> adapted.checkAttributePosition(9));
		assertFalse(node.isStrikethrough());
		adapted.setStrikethrough(true);
		adapted.setBold(true);
		assertTrue(node.isStrikethrough());
		assertTrue(node.isBold());
		StringWriter writer = new StringWriter();
		node.save(writer, opened.mode.getMap().getLinkRegistry(), true, false);
		assertTrue(writer.toString().contains("STRIKETHROUGH"));
		assertTrue(writer.toString().contains("BOLD"));
	}

	@Test
	void styleFollowsTheStandardTheParentAndTheFold() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		String nodeKey = FreeMind.RESOURCES_NODE_STYLE;
		String rootKey = FreeMind.RESOURCES_ROOT_NODE_STYLE;
		String previousNode = opened.frame.getProperty(nodeKey);
		String previousRoot = opened.frame.getProperty(rootKey);
		MindMapNode child = opened.child("Child");
		String childStyle = ((NodeAdapter) child).getBareStyle();
		String rootStyle = opened.root.getBareStyle();
		boolean folded = child.isFolded();
		try {
			opened.root.setStyle(null);
			child.setStyle(null);
			opened.frame.setProperty(rootKey, MindMapNode.STYLE_FORK);
			opened.frame.setProperty(nodeKey, MindMapNode.STYLE_BUBBLE);
			assertEquals(MindMapNode.STYLE_FORK, opened.root.getStyle());
			assertEquals(MindMapNode.STYLE_BUBBLE, child.getStyle());
			opened.frame.setProperty(nodeKey, MindMapNode.STYLE_AS_PARENT);
			assertEquals(MindMapNode.STYLE_FORK, child.getStyle());
			child.setStyle(MindMapNode.STYLE_AS_PARENT);
			assertEquals(MindMapNode.STYLE_FORK, child.getStyle());
			opened.root.setStyle(MindMapNode.STYLE_AS_PARENT);
			assertEquals(MindMapNode.STYLE_FORK, opened.root.getStyle());
			child.setStyle(MindMapNode.STYLE_COMBINED);
			child.setFolded(true);
			assertEquals(MindMapNode.STYLE_BUBBLE, child.getStyle());
			child.setFolded(false);
			assertEquals(MindMapNode.STYLE_FORK, child.getStyle());
		} finally {
			child.setStyle(childStyle);
			child.setFolded(folded);
			opened.root.setStyle(rootStyle);
			restore(opened, nodeKey, previousNode);
			restore(opened, rootKey, previousRoot);
		}
	}

	@Test
	void childrenShiftCloudsHooksAndCopiesStayConsistent() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMap map = opened.mode.getMap();
		NodeAdapter child = (NodeAdapter) opened.child("Child");
		assertEquals(-2, child.calcShiftY());
		MindMapNodeModel left = new MindMapNodeModel("Left", map);
		MindMapNodeModel right = new MindMapNodeModel("Right", map);
		opened.root.insert(right, opened.root.getChildCount());
		opened.root.insert(left, -1);
		right.setLeft(false);
		left.setLeft(true);
		ListIterator<MindMapNode> sorted = opened.root.sortedChildrenUnfolded();
		assertEquals("Left", sorted.next().getText());
		assertTrue(opened.root.isNewChildLeft());
		MindMapNodeModel parent = new MindMapNodeModel("Parent", map);
		opened.root.insert(parent, 0);
		parent.setLeft(true);
		MindMapNodeModel kid = new MindMapNodeModel("Kid", map);
		parent.insert(kid, 0);
		assertTrue(kid.isLeft());
		assertTrue(parent.isNewChildLeft());
		assertTrue(kid.isDescendantOfOrEqual(opened.root));
		assertTrue(opened.root.isDescendantOfOrEqual(opened.root));
		assertFalse(opened.root.isDescendantOf(kid));
		assertEquals(0, parent.getChildPosition(kid));
		assertEquals(-1, parent.getChildPosition(opened.root));
		parent.setLeft(false);
		assertFalse(kid.isLeft());
		kid.setFolded(true);
		assertTrue(parent.hasFoldedStrictDescendant());
		assertTrue(kid.hasFoldedParents() == false);
		parent.setFolded(true);
		assertTrue(kid.hasFoldedParents());
		kid.setFolded(false);
		parent.setFolded(false);
		assertFalse(parent.hasFoldedStrictDescendant());

		MindMapCloudModel childCloud = new MindMapCloudModel(kid, opened.mode);
		kid.setCloud(childCloud);
		assertEquals(0, childCloud.getIterativeLevel());
		MindMapCloudModel parentCloud = new MindMapCloudModel(parent, opened.mode);
		parent.setCloud(parentCloud);
		assertEquals(1, kid.getCloud().getIterativeLevel());
		parent.setCloud(null);
		assertEquals(0, kid.getCloud().getIterativeLevel());

		final int[] calls = { 0 };
		CountingHook counting = new CountingHook(calls);
		parent.addHook(counting);
		parent.invokeHook(counting);
		MindMapNodeModel extra = new MindMapNodeModel("Extra", map);
		parent.insert(extra, 0);
		int afterInsert = calls[0];
		assertTrue(afterInsert >= 2);
		parent.remove(extra);
		assertTrue(calls[0] > afterInsert);
		assertThrows(IllegalArgumentException.class, () -> parent.addHook(null));
		int activated = parent.getActivatedHooks().size();
		parent.invokeHook(new PermanentNodeHookAdapter() {
			public void invoke(MindMapNode node) {
				throw new IllegalStateException("boom");
			}
		});
		assertEquals(activated, parent.getActivatedHooks().size());

		MindMapNode copy = child.shallowCopy();
		assertNotNull(copy);
		assertEquals("Child", copy.getText());
		assertFalse(copy.isFolded());
		assertNotSame(child, copy);
	}

	@Test
	void removeAllHooksRetriesWhenShutdownThrowsAndSaveSkipsMarkers() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMapNode node = opened.child("Child");
		StickyHook sticky = new StickyHook();
		node.addHook(sticky);
		node.invokeHook(sticky);
		node.removeAllHooks();
		assertEquals(2, sticky.names);
		assertFalse(node.getHooks().contains(sticky));

		KeptHook kept = new KeptHook();
		SkipHook skip = new SkipHook();
		node.addHook(kept);
		node.invokeHook(kept);
		node.addHook(skip);
		node.invokeHook(skip);
		StringWriter writer = new StringWriter();
		node.save(writer, opened.mode.getMap().getLinkRegistry(), true, false);
		assertTrue(writer.toString().contains("plugins/kept"));
		assertFalse(writer.toString().contains("plugins/skip"));
	}

	@Test
	void saveSkipsAnInvisibleNodeAndKeepsItsVisibleDescendant() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		MindMap map = opened.mode.getMap();
		MindMapNode hidden = new MindMapNodeModel("Hidden", map);
		MindMapNode grand = new MindMapNodeModel("VisibleGrand", map);
		opened.root.insert(hidden, opened.root.getChildCount());
		hidden.insert(grand, 0);
		Filter previous = map.getFilter();
		map.setFilter(new Filter() {
			public void applyFilter(Controller controller) {
			}

			public boolean isVisible(MindMapNode node) {
				return node != hidden;
			}

			public boolean areMatchedShown() {
				return true;
			}

			public boolean areHiddenShown() {
				return false;
			}

			public boolean areAncestorsShown() {
				return true;
			}

			public boolean areDescendantsShown() {
				return true;
			}

			public boolean areEclipsedShown() {
				return false;
			}

			public Object getCondition() {
				return null;
			}
		});
		try {
			assertFalse(hidden.isVisible());
			assertTrue(grand.isVisible());
			assertTrue(opened.root.hasExactlyOneVisibleChild());
			StringWriter writer = new StringWriter();
			opened.root.save(writer, map.getLinkRegistry(), false, true);
			String xml = writer.toString();
			assertFalse(xml.contains("Hidden"));
			assertTrue(xml.contains("VisibleGrand"));
			assertTrue(xml.contains("Child"));
		} finally {
			map.setFilter(previous);
		}
	}

	@Test
	void tooltipPreviewAppearsForAMapLinkAndLeavesForAnotherLink() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		File dir = new File(System.getProperty("java.io.tmpdir"), "cov-tip-" + System.nanoTime());
		assertTrue(dir.mkdir());
		File mapFile = new File(dir, "map.mm");
		File other = new File(dir, "other.mm");
		assertTrue(mapFile.createNewFile());
		assertTrue(other.createNewFile());
		File thumb = new File(Resources.getInstance().createThumbnailFileName(other));
		assertTrue(thumb.createNewFile() || thumb.exists());
		File previous = opened.mode.getMap().getFile();
		((freemind.modes.MapAdapter) opened.mode.getMap()).setFile(mapFile);
		MindMapNode node = opened.child("Child");
		try {
			node.setLink("other.mm");
			SortedMap<String, String> first = node.getToolTip();
			assertNotNull(first.get("preview"));
			assertTrue(first.get("preview").contains("other.mm"));
			assertEquals(first.get("preview"), node.getToolTip().get("preview"));
			node.setLink("readme.txt");
			assertNull(node.getToolTip().get("preview"));
		} finally {
			node.setLink(null);
			((freemind.modes.MapAdapter) opened.mode.getMap()).setFile(previous);
			thumb.delete();
			other.delete();
			mapFile.delete();
			dir.delete();
		}
	}

	@Test
	void saveOmitsTheIdUntilALocalLinkPointsAtTheNode() throws Exception {
		// backwards compatibility
		Opened opened = MapFixture.open("Child");
		String key = FreeMindCommon.SAVE_ONLY_INTRISICALLY_NEEDED_IDS;
		String previous = opened.frame.getProperty(key);
		MindMapNode child = opened.child("Child");
		MindMapLinkRegistry registry = opened.mode.getMap().getLinkRegistry();
		String id = opened.mode.getNodeID(child);
		try {
			opened.mode.setProperty(key, "true");
			StringWriter omitted = new StringWriter();
			child.save(omitted, registry, true, false);
			assertFalse(omitted.toString().contains("ID=\"" + id + "\""));
			MindMapNode pointer = new MindMapNodeModel("Pointer", opened.mode.getMap());
			opened.root.insert(pointer, opened.root.getChildCount());
			pointer.setLink("#" + id);
			StringWriter kept = new StringWriter();
			child.save(kept, registry, true, false);
			assertTrue(kept.toString().contains("ID=\"" + id + "\""));
		} finally {
			opened.mode.setProperty(key, previous == null ? "false" : previous);
			if (previous == null) {
				opened.frame.getProperties().remove(key);
			}
		}
	}

	private static void restore(Opened opened, String key, String previous) {
		if (previous == null) {
			opened.frame.getProperties().remove(key);
		} else {
			opened.frame.setProperty(key, previous);
		}
	}

	private static final class BareNode extends NodeAdapter {
		BareNode(Object text, MindMap map) {
			super(text, map);
		}

		public boolean isWriteable() {
			return true;
		}
	}

	private static final class CountingHook extends PermanentNodeHookAdapter {
		private final int[] calls;

		CountingHook(int[] calls) {
			this.calls = calls;
		}

		public void onAddChild(MindMapNode added) {
			calls[0]++;
		}

		public void onAddChildren(MindMapNode added) {
			calls[0]++;
		}

		public void onRemoveChild(MindMapNode removed) {
			calls[0]++;
		}

		public void onRemoveChildren(MindMapNode removed, MindMapNode oldParent) {
			calls[0]++;
		}
	}

	private static final class StickyHook extends PermanentNodeHookAdapter {
		int names;

		public String getName() {
			names++;
			return "plugins/sticky";
		}

		public void shutdownMapHook() {
			throw new IllegalStateException("stay");
		}
	}

	private static final class KeptHook extends PermanentNodeHookAdapter {
		public String getName() {
			return "plugins/kept";
		}
	}

	private static final class SkipHook extends PermanentNodeHookAdapter implements DontSaveMarker {
		public String getName() {
			return "plugins/skip";
		}
	}
}
