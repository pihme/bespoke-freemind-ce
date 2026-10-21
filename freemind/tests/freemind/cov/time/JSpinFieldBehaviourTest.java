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

package tests.freemind.cov.time;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusEvent;

import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.ChangeEvent;

import org.junit.jupiter.api.Test;

import accessories.plugins.time.JSpinField;

/** Backwards-compatible spin-field limits, colour, and commit behaviour. */
class JSpinFieldBehaviourTest {

	@Test
	void limitsClampInsideAtOverZeroAndNegative() {
		// backwards compatibility
		OpenSpin inverted = new OpenSpin(5, 3);
		assertEquals(5, inverted.getMinimum());
		assertEquals(5, inverted.getMaximum());
		assertEquals(5, inverted.getValue());

		OpenSpin nonNegative = new OpenSpin(0, 10);
		assertEquals(0, nonNegative.getValue());
		nonNegative.setValue(5);
		assertEquals(5, nonNegative.getValue());
		nonNegative.setValue(0);
		assertEquals(0, nonNegative.getValue());
		nonNegative.setValue(10);
		assertEquals(10, nonNegative.getValue());
		nonNegative.setValue(11);
		assertEquals(10, nonNegative.getValue());
		nonNegative.setValue(-1);
		assertEquals(0, nonNegative.getValue());

		OpenSpin onlyZero = new OpenSpin(0, 0);
		onlyZero.setValue(1);
		assertEquals(0, onlyZero.getValue());
		onlyZero.setValue(-1);
		assertEquals(0, onlyZero.getValue());

		OpenSpin negative = new OpenSpin(-10, -5);
		assertEquals(-5, negative.getValue());
		negative.setValue(-7);
		assertEquals(-7, negative.getValue());
		negative.setValue(-10);
		assertEquals(-10, negative.getValue());
		negative.setValue(-11);
		assertEquals(-10, negative.getValue());
		negative.setValue(0);
		assertEquals(-5, negative.getValue());

		OpenSpin full = new OpenSpin();
		full.setValue(0);
		assertEquals(0, full.getValue());
		full.setValue(Integer.MAX_VALUE);
		assertEquals(Integer.MAX_VALUE, full.getValue());
		full.setValue(Integer.MIN_VALUE);
		assertEquals(Integer.MIN_VALUE, full.getValue());
		full.setMinimum(-3);
		assertEquals(-3, full.getMinimum());
		assertEquals(Integer.MIN_VALUE, full.getValue());
		full.setValue(-3);
		assertEquals(-3, full.getValue());
		full.setValue(-4);
		assertEquals(-3, full.getValue());
	}

	@Test
	void greenTextCommitsAndRedTextDoesNot() {
		// backwards compatibility
		OpenSpin spin = new OpenSpin(0, 10);
		type(spin, "4");
		assertEquals(spin.green(), spin.text().getForeground());
		assertEquals(4, spin.getValue());
		spin.actionPerformed(null);
		assertEquals(4, spin.getValue());

		type(spin, "99");
		assertEquals(Color.RED, spin.text().getForeground());
		spin.actionPerformed(null);
		assertEquals(4, spin.getValue());

		type(spin, "x");
		assertEquals(Color.RED, spin.text().getForeground());
		spin.focusLost(new FocusEvent(spin.text(), FocusEvent.FOCUS_LOST));
		assertEquals(4, spin.getValue());

		type(spin, "6");
		spin.focusLost(new FocusEvent(spin.text(), FocusEvent.FOCUS_LOST));
		assertEquals(6, spin.getValue());
		assertEquals("6", spin.text().getText());

		int before = spin.getValue();
		spin.focusGained(new FocusEvent(spin.text(), FocusEvent.FOCUS_GAINED));
		assertEquals(before, spin.getValue());
	}

	@Test
	void spinnerChangeFontAndWidthFollowTheField() {
		// backwards compatibility
		OpenSpin spin = new OpenSpin(0, 10);
		spin.getSpinner().setSize(40, 10);
		Dimension preferred = spin.getSpinner().getPreferredSize();
		assertEquals(new JTextField().getPreferredSize().height, preferred.height);
		assertTrue(preferred.width > 0);

		((javax.swing.JSpinner) spin.getSpinner()).setValue(Integer.valueOf(8));
		assertEquals(8, spin.getValue());
		spin.stateChanged(new ChangeEvent(spin.getSpinner()));
		assertEquals(8, spin.getValue());

		spin.adjustWidthToMaximumValue();
		assertEquals(new JTextField("10").getPreferredSize().width, spin.text().getPreferredSize().width);

		Font font = new Font(Font.DIALOG, Font.BOLD, 14);
		spin.setFont(font);
		assertEquals(font, spin.text().getFont());
		spin.setForeground(Color.BLUE);
		assertEquals(Color.BLUE, spin.text().getForeground());
		spin.setHorizontalAlignment(SwingConstants.LEFT);
		assertEquals(SwingConstants.LEFT, spin.text().getHorizontalAlignment());

		spin.setEnabled(false);
		assertFalse(spin.text().isEnabled());
		assertFalse(spin.getSpinner().isEnabled());
		Color inactive = UIManager.getColor("TextField.inactiveBackground");
		if (inactive != null) {
			assertEquals(inactive, spin.text().getBackground());
		}
		spin.setEnabled(true);
		assertTrue(spin.text().isEnabled());
	}

	private static void type(OpenSpin spin, String text) {
		spin.text().setText(text);
		spin.caretUpdate(new CaretEvent(spin.text()) {
			public int getDot() {
				return 0;
			}

			public int getMark() {
				return 0;
			}
		});
	}

	static final class OpenSpin extends JSpinField {
		OpenSpin() {
			super();
		}

		OpenSpin(int min, int max) {
			super(min, max);
		}

		JTextField text() {
			return textField;
		}

		Color green() {
			return darkGreen;
		}
	}
}
