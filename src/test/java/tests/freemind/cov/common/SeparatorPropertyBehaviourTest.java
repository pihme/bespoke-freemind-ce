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

package tests.freemind.cov.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import freemind.common.SeparatorProperty;

/** A separator has a label, no description, and asks for separator.text. */
@DisplayName("SeparatorPropertyBehaviourTest")
class SeparatorPropertyBehaviourTest {

	@Test
	void theLabelIsKeptAndThereIsNoDescription() {
		// backwards compatibility
		SeparatorProperty property = new SeparatorProperty("group");
		assertEquals("group", property.getLabel());
		assertNull(property.getDescription());
		property.setEnabled(false);
		property.setEnabled(true);
		assertEquals("group", property.getLabel());
		assertNull(property.getDescription());
	}

	@Test
	void layoutAsksForTheSeparatorTextAndAddsAComponent() {
		// backwards compatibility
		SeparatorProperty property = new SeparatorProperty("group");
		String[] asked = { null };
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref", ""));
		int before = builder.getPanel().getComponentCount();
		property.layout(builder, key -> {
			asked[0] = key;
			return "Group";
		});
		assertEquals("separator.group", asked[0]);
		assertTrue(builder.getPanel().getComponentCount() > before);
	}
}
