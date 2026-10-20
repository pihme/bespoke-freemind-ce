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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JPanel;

import freemind.controller.Controller;
import freemind.controller.filter.Filter;
import freemind.modes.MindMap;
import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.MindMapNodeModel;
import freemind.view.mindmapview.MapView;
import freemind.view.mindmapview.NodeView;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/** Shared headless map setup for view coverage tests. */
public final class ViewMaps {

	private ViewMaps() {
	}

	public static Opened open(String... childTexts) throws Exception {
		Opened opened = MapFixture.open(childTexts);
		placeInViewport(opened.view);
		return opened;
	}

	public static void placeInViewport(MapView view) {
		if (view.getParent() != null) {
			view.getParent().remove(view);
		}
		MapView.ScrollPane scroll = new MapView.ScrollPane();
		JPanel holder = new JPanel(new BorderLayout());
		holder.setBounds(0, 0, 400, 300);
		holder.add(scroll, BorderLayout.CENTER);
		scroll.setBounds(0, 0, 400, 300);
		scroll.setPreferredSize(new Dimension(400, 300));
		scroll.setViewportView(view);
		view.setPreferredSize(new Dimension(1400, 1000));
		view.setSize(1400, 1000);
		holder.setSize(400, 300);
		scroll.setSize(400, 300);
		holder.validate();
		view.validate();
		if (scroll.getViewport().getWidth() <= 0) {
			scroll.getViewport().setSize(380, 260);
		}
	}

	public static NodeView addChild(NodeView parentView, String text, boolean left)
			throws Exception {
		MindMapNodeModel parent = (MindMapNodeModel) parentView.getModel();
		MindMapNodeModel child = new MindMapNodeModel(text, (MindMap) parent.getMap());
		child.setLeft(left);
		parent.insert(child, parent.getChildCount());
		return (NodeView) call(parentView, "insert",
				new Class<?>[] { MindMapNode.class, int.class },
				new Object[] { child, parent.getIndex(child) });
	}

	public static NodeView childView(Opened opened, String text) {
		return opened.view.getNodeView(opened.child(text));
	}

	public static KeyEvent key(java.awt.Component source, int code, int modifiers) {
		return new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
				modifiers, code, KeyEvent.CHAR_UNDEFINED);
	}

	public static BufferedImage paint(java.awt.Component component) {
		int width = Math.max(component.getWidth(), 20);
		int height = Math.max(component.getHeight(), 20);
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setColor(java.awt.Color.WHITE);
		graphics.fillRect(0, 0, width, height);
		component.paint(graphics);
		graphics.dispose();
		return image;
	}

	public static int ink(BufferedImage image) {
		int count = 0;
		for (int y = 0; y < image.getHeight(); y += 2) {
			for (int x = 0; x < image.getWidth(); x += 2) {
				int rgb = image.getRGB(x, y);
				if ((rgb >>> 24) != 0 && (rgb & 0x00ffffff) != 0x00ffffff) {
					count++;
				}
			}
		}
		return count;
	}

	public static Object call(Object target, String name, Class<?>[] types, Object[] args)
			throws Exception {
		Class<?> type = target.getClass();
		while (type != null) {
			try {
				Method method = type.getDeclaredMethod(name, types);
				method.setAccessible(true);
				try {
					return method.invoke(target, args);
				} catch (InvocationTargetException ex) {
					Throwable cause = ex.getCause();
					if (cause instanceof Exception) {
						throw (Exception) cause;
					}
					if (cause instanceof Error) {
						throw (Error) cause;
					}
					throw ex;
				}
			} catch (NoSuchMethodException ex) {
				type = type.getSuperclass();
			}
		}
		throw new NoSuchMethodException(name);
	}

	public static void setBooleanField(Class<?> type, String name, boolean value)
			throws Exception {
		java.lang.reflect.Field field = type.getDeclaredField(name);
		field.setAccessible(true);
		field.setBoolean(null, value);
	}

	public static boolean booleanField(Class<?> type, String name) throws Exception {
		java.lang.reflect.Field field = type.getDeclaredField(name);
		field.setAccessible(true);
		return field.getBoolean(null);
	}

	public static final class HideText implements Filter {
		private final String hidden;

		public HideText(String hidden) {
			this.hidden = hidden;
		}

		public void applyFilter(Controller controller) {
		}

		public boolean isVisible(MindMapNode node) {
			return !hidden.equals(node.getText());
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
			return hidden;
		}
	}
}
