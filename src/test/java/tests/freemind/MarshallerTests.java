/*FreeMind - A Program for creating and viewing Mindmaps
 *Copyright (C) 2000-2007  Christian Foltin, Dimitry Polivaev and others.
 *
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
 *
 * Created on 15.08.2007
 */
/*$Id: MarshallerTests.java,v 1.1.2.1 2008/04/02 20:02:39 christianfoltin Exp $*/

package tests.freemind;

import de.foltin.StringEncoder;
import freemind.common.XmlBindingTools;
import freemind.controller.actions.generated.instance.Pattern;
import freemind.controller.actions.generated.instance.PatternChild;

/**
 * @author foltin
 *
 */
public class MarshallerTests extends FreeMindTestBase {
	/**
	 * Newlines in Jibx attributes are handled correctly.
	 */
	public void testNewLines() {
		Pattern testPattern = new Pattern();
		PatternChild patternChild = new PatternChild();
		String value = "\\ntest\n\\test<>&";
		patternChild.setValue(value);
		assertEquals(value, patternChild.getValue());
		testPattern.setPatternChild(patternChild);
		testPattern.setName("test");
		String marshall = XmlBindingTools.getInstance().marshall(testPattern);
		System.out.println(marshall);
		Pattern testPatternUnmarshalled = (Pattern) XmlBindingTools
				.getInstance().unMarshall(marshall);
		assertEquals("Newline is correctly marshalled?" + marshall,
				testPattern.getName(), testPatternUnmarshalled.getName());
		assertEquals("Newline is correctly marshalled?" + marshall, testPattern
				.getPatternChild().getValue(), testPatternUnmarshalled
				.getPatternChild().getValue());
	}

	public void testStringEncoder() throws Exception {
		String input = "\\ntest\n\\test";
		String encodedString = StringEncoder.encode(input);
		// System.out.println(encodedString);
		assertEquals("\\\\ntest\\u000a\\\\test", encodedString);
		String output = StringEncoder.decode(encodedString);
		assertEquals(input, output);

	}

}
