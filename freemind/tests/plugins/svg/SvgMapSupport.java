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

import java.awt.Rectangle;
import java.io.File;
import java.nio.file.Files;

import javax.swing.JPanel;

import freemind.main.FreeMindMain;
import freemind.modes.MindMapNode;
import freemind.view.mindmapview.MapView;
import tests.freemind.cov.MapFixture;

/**
 * A file-backed map view. The headless controller view has a zero-size root,
 * so its printed canvas is negative and omits the node text.
 */
final class SvgMapSupport {

	private static final String MAP = "<map version=\"1.0.0\">"
			+ "<node TEXT=\"ROOT\">"
			+ "<node TEXT=\"Alpha\" POSITION=\"right\"/>"
			+ "</node></map>";

	private SvgMapSupport() {
	}

	static LaidOut layOut(FreeMindMain frame) throws Exception {
		File mapFile = File.createTempFile("svg-export", ".mm");
		mapFile.deleteOnExit();
		Files.writeString(mapFile.toPath(), MAP);
		JPanel parent = new JPanel();
		parent.setBounds(new Rectangle(0, 0, 800, 600));
		MapView view = new MapFixture.FontedMapCreator().createMapViewForFile(
				mapFile.getPath(), parent, frame);
		view.getRoot().updateAll();
		parent.doLayout();
		parent.validate();
		MindMapNode root = view.getModel().getRootNode();
		return new LaidOut(view, root, (MindMapNode) root.getChildAt(0));
	}

	static final class LaidOut {
		final MapView view;
		final MindMapNode root;
		final MindMapNode alpha;

		LaidOut(MapView view, MindMapNode root, MindMapNode alpha) {
			this.view = view;
			this.root = root;
			this.alpha = alpha;
		}
	}
}
