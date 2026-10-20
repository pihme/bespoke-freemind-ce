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

import java.awt.Component;
import java.awt.Container;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.reflect.Field;

import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import freemind.main.FreeMindMain;
import freemind.main.Tools;
import freemind.view.mindmapview.EditNodeBase;
import freemind.view.mindmapview.EditNodeDialog;
import freemind.view.mindmapview.MapView;
import tests.freemind.cov.MapFixture.Opened;

/** Shared editor fixtures. DialogFrame is constructed only from gui-tagged tests. */
public final class EditSupports {

	private EditSupports() {
	}

	public static final class Recording implements EditNodeBase.EditControl {
		public String ok;
		public boolean cancelled;
		public String split;
		public int splitAt = -1;
		public int okCount;
		public int cancelCount;

		public void cancel() {
			cancelled = true;
			cancelCount++;
		}

		public void ok(String newText) {
			ok = newText;
			okCount++;
		}

		public void split(String newText, int position) {
			split = newText;
			splitAt = position;
		}
	}

	public static void spellOff() throws Exception {
		ViewMaps.setBooleanField(EditNodeBase.class, "checkSpelling", false);
	}

	public static void windowProperties(Opened opened, String buttonPosition) {
		opened.frame.setProperty("check_spelling", "false");
		opened.frame.setProperty("el__min_default_window_height", "80");
		opened.frame.setProperty("el__max_default_window_height", "400");
		opened.frame.setProperty("el__min_default_window_width", "80");
		opened.frame.setProperty("el__max_default_window_width", "500");
		opened.frame.setProperty("el__buttons_position", buttonPosition);
		opened.frame.setProperty("el__enter_confirms_by_default", "true");
	}

	public static void installFrame(Opened opened) {
		opened.app.setFrame(new DialogFrame(opened.frame));
	}

	public static void restoreFrame(Opened opened, FreeMindMain original) {
		opened.app.setFrame(original);
	}

	public static void resetConfirmHolder() throws Exception {
		Field field = EditNodeDialog.class.getDeclaredField("booleanHolderForConfirmState");
		field.setAccessible(true);
		field.set(null, null);
	}

	public static boolean confirmHolder() throws Exception {
		Field field = EditNodeDialog.class.getDeclaredField("booleanHolderForConfirmState");
		field.setAccessible(true);
		Object holder = field.get(null);
		return holder != null && ((Tools.BooleanHolder) holder).getValue();
	}

	public static void releaseQueuedKeys(JTextComponent field) {
		if (field == null) {
			return;
		}
		for (FocusListener listener : field.getFocusListeners()) {
			if (listener.getClass().getName().contains("KeyEventQueue")) {
				listener.focusGained(new FocusEvent(field, FocusEvent.FOCUS_GAINED));
			}
		}
	}

	public static JTextField textField(MapView view) {
		return find(view, JTextField.class);
	}

	public static <T extends Component> T find(Component root, Class<T> type) {
		if (type.isInstance(root)) {
			return type.cast(root);
		}
		if (root instanceof Container) {
			for (Component child : ((Container) root).getComponents()) {
				T found = find(child, type);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}
}
