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

package tests.freemind.cov;

import java.awt.Font;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import freemind.controller.Controller;
import freemind.main.HeadlessFreeMind;
import freemind.modes.MindMapNode;
import freemind.modes.ModeController;
import freemind.modes.mindmapmode.MindMapController;
import freemind.modes.mindmapmode.MindMapMapModel;
import freemind.modes.mindmapmode.MindMapMode;
import freemind.modes.mindmapmode.MindMapNodeModel;
import freemind.view.mindmapview.IndependantMapViewCreator;
import freemind.view.mindmapview.MapView;

/**
 * Headless map used by coverage tests. A headless frame has no content pane,
 * so the normal controller init cannot build the toolbar. Opening a map still
 * notifies that controller.
 */
public final class MapFixture {

	private MapFixture() {
	}

	public static Opened open(String... childTexts) throws Exception {
		HeadlessFreeMind frame = new HeadlessFreeMind();
		HoldingController app = new HoldingController(frame);
		app.initialization();
		MindMapController[] holder = new MindMapController[1];
		MindMapMode mode = new MindMapMode() {
			@Override
			public ModeController createModeController() {
				holder[0] = new MindMapController(this);
				return holder[0];
			}
		};
		mode.init(app);
		MindMapController modeController = holder[0];
		MindMapMapModel model = new MindMapMapModel(modeController);
		modeController.setModel(model);
		MindMapNodeModel root = new MindMapNodeModel("ROOT", model);
		model.setRoot(root);
		for (int i = 0; i < childTexts.length; i++) {
			root.insert(new MindMapNodeModel(childTexts[i], model), i);
		}
		app.getMapModuleManager().newMapModule(model, modeController);
		MapView view = app.getView();
		JPanel parent = new JPanel();
		parent.setBounds(new Rectangle(0, 0, 800, 600));
		parent.add(view);
		view.setBounds(parent.getBounds());
		parent.doLayout();
		view.addNotify();
		view.getRoot().getMainView().doLayout();
		return new Opened(frame, app, modeController, view, root);
	}

	public static List<String> childTexts(MindMapNode node) {
		List<String> texts = new ArrayList<>();
		for (int i = 0; i < node.getChildCount(); i++) {
			texts.add(((MindMapNode) node.getChildAt(i)).getText());
		}
		return texts;
	}

	public static final class Opened {
		public final HeadlessFreeMind frame;
		public final Controller app;
		public final MindMapController mode;
		public final MapView view;
		public final MindMapNodeModel root;

		Opened(HeadlessFreeMind frame, Controller app, MindMapController mode,
				MapView view, MindMapNodeModel root) {
			this.frame = frame;
			this.app = app;
			this.mode = mode;
			this.view = view;
			this.root = root;
		}

		public MindMapNode child(String text) {
			for (int i = 0; i < root.getChildCount(); i++) {
				MindMapNode child = (MindMapNode) root.getChildAt(i);
				if (text.equals(child.getText())) {
					return child;
				}
			}
			throw new AssertionError("missing child " + text);
		}
	}

	/** Supplies the font the headless creator otherwise leaves null. */
	public static final class FontedMapCreator extends IndependantMapViewCreator {
		@Override
		public Font getDefaultFont() {
			return new Font(Font.SANS_SERIF, Font.PLAIN, 16);
		}
	}

	private static final class HoldingController extends Controller {
		HoldingController(HeadlessFreeMind frame) {
			super(frame);
		}

		@Override
		public void setAllActions(boolean enabled) {
		}

		@Override
		public void afterMapModuleChange(freemind.view.MapModule oldMapModule,
				freemind.modes.Mode oldMode, freemind.view.MapModule newMapModule,
				freemind.modes.Mode newMode) {
		}

		@Override
		public void numberOfOpenMapInformation(int number, int pIndex) {
		}
	}
}
