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

package freemind.main;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Plain text versus HTML for a node, and the string a save writes.
 * HTML stays HTML. This does not rewrite markup into another form.
 */
public final class StoredNodeText {

	private static final Pattern HTML = Pattern.compile("(?s).*<\\s*html.*?>.*");

	private StoredNodeText() {
	}

	public static boolean isHtml(String text) {
		return HTML.matcher(text.toLowerCase(Locale.ENGLISH)).matches();
	}

	public static String clean(String text) {
		return HtmlTools.makeValidXml(text);
	}

	/** HTML written inside richcontent. Numeric character entities become characters. Markup stays. */
	public static String htmlForFile(String xmlText) {
		return HtmlTools.unescapeHTMLUnicodeEntity(clean(xmlText));
	}
}
