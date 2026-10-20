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

package tests.freemind.cov.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.BasicStroke;
import java.awt.Color;

import org.junit.jupiter.api.Test;

import freemind.modes.MindMapCloud;
import freemind.modes.mindmapmode.MindMapCloudModel;
import freemind.view.mindmapview.CloudView;
import freemind.view.mindmapview.NodeView;
import tests.freemind.cov.MapFixture.Opened;

public class CloudViewBehaviourTest {

	@Test
	void aCloudPaintsAroundTheNodeAndReportsWidth() throws Exception {
		// backwards compatibility
		Opened opened = ViewMaps.open("Alpha", "Beta");
		NodeView root = opened.view.getRoot();
		MindMapCloudModel cloud = new MindMapCloudModel(opened.root, opened.mode);
		cloud.setColor(Color.YELLOW);
		cloud.setWidth(0);
		opened.root.setCloud(cloud);
		ViewMaps.addChild(root, "Left", true);
		opened.view.validate();
		OpenedCloud view = new OpenedCloud(cloud, root);
		assertEquals(1, view.getRealWidth());
		assertTrue(view.getStroke() instanceof BasicStroke);
		cloud.setWidth(4);
		assertEquals(4, view.getRealWidth());
		assertTrue(view.getStroke().getClass() == BasicStroke.class);
		assertTrue(ViewMaps.ink(ViewMaps.paint(root)) > 0);
		MindMapCloudModel nested = new MindMapCloudModel(opened.child("Alpha"), opened.mode);
		nested.setColor(Color.GREEN);
		opened.child("Alpha").setCloud(nested);
		opened.view.getNodeView(opened.child("Alpha")).treeNodesChanged(
				new javax.swing.event.TreeModelEvent(opened.child("Alpha"),
						new Object[] { opened.child("Alpha") }));
		assertTrue(nested.getIterativeLevel() >= 1 || ViewMaps.ink(ViewMaps.paint(root)) > 0);
	}

	private static final class OpenedCloud extends CloudView {
		OpenedCloud(MindMapCloud cloud, NodeView source) {
			super(cloud, source);
		}
	}
}
