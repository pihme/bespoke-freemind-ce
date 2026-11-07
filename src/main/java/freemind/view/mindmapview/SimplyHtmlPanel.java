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

package freemind.view.mindmapview;

import com.lightdev.app.shtm.SHTMLPanel;
import com.lightdev.app.shtm.TextResources;

import freemind.main.Resources;
import freemind.main.Tools;

/**
 * The one place that constructs the SimplyHTML panel.
 * Notes and the long-node editor both use it.
 */
public final class SimplyHtmlPanel {

	private SimplyHtmlPanel() {
	}

	public static SHTMLPanel create() {
		SHTMLPanel.setResources(new ResourceBridge());
		return SHTMLPanel.createSHTMLPanel();
	}

	private static final class ResourceBridge implements TextResources {
		public String getString(String key) {
			if (Tools.safeEquals("show_splash_screen", key)) {
				return "false";
			}
			if (Tools.safeEquals("default_paste_mode", key)) {
				return "PASTE_HTML";
			}
			key = "simplyhtml." + key;
			String resourceString = Resources.getInstance().getResourceString(key, null);
			if (resourceString == null) {
				resourceString = Resources.getInstance().getProperty(key);
			}
			return resourceString;
		}
	}
}
