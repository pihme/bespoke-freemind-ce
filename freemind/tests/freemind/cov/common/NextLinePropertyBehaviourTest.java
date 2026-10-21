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

import org.junit.jupiter.api.Test;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import freemind.common.NextLineProperty;

/** A next-line control has no label and moves the form cursor down one row. */
class NextLinePropertyBehaviourTest {

	@Test
	void labelAndDescriptionAreNullAndEnabledDoesNotChangeThat() {
		// backwards compatibility
		NextLineProperty property = new NextLineProperty();
		assertNull(property.getLabel());
		assertNull(property.getDescription());
		property.setEnabled(false);
		property.setEnabled(true);
		assertNull(property.getLabel());
		assertNull(property.getDescription());
	}

	@Test
	void layoutMovesTheCursorToTheNextRow() {
		// backwards compatibility
		NextLineProperty property = new NextLineProperty();
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref", ""));
		int row = builder.getRow();
		property.layout(builder, key -> {
			throw new AssertionError("next line does not ask for text: " + key);
		});
		assertEquals(row + 1, builder.getRow());
	}
}
