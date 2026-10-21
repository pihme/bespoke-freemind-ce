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
import static org.junit.jupiter.api.Assertions.assertSame;

import java.beans.PropertyChangeListener;

import org.junit.jupiter.api.Test;

import freemind.common.PropertyBean;

/** Listeners hear the label and the current value, and a removed listener does not. */
class PropertyBeanBehaviourTest {

	@Test
	void aFiredChangeCarriesTheLabelAndTheCurrentValue() {
		// backwards compatibility
		Sample sample = new Sample();
		String[] seen = { null };
		sample.addPropertyChangeListener(event -> {
			assertSame(sample, event.getSource());
			assertEquals("sample", event.getPropertyName());
			assertNull(event.getOldValue());
			seen[0] = String.valueOf(event.getNewValue());
		});

		sample.setValue("next");
		sample.fire();
		assertEquals("next", sample.getValue());
		assertEquals("next", seen[0]);
	}

	@Test
	void aRemovedListenerIsNotCalledAgain() {
		// backwards compatibility
		Sample sample = new Sample();
		int[] fires = { 0 };
		PropertyChangeListener listener = event -> fires[0]++;
		sample.addPropertyChangeListener(listener);
		sample.fire();
		sample.removePropertyChangeListener(listener);
		sample.fire();
		sample.removePropertyChangeListener(listener);
		assertEquals(1, fires[0]);
	}

	@Test
	void everyRegisteredListenerHearsTheSameValue() {
		// backwards compatibility
		Sample sample = new Sample();
		sample.setValue("");
		int[] fires = { 0 };
		sample.addPropertyChangeListener(event -> {
			fires[0]++;
			assertEquals("", event.getNewValue());
		});
		sample.addPropertyChangeListener(event -> fires[0]++);
		sample.fire();
		assertEquals(2, fires[0]);
		assertEquals("", sample.getValue());
	}

	private static final class Sample extends PropertyBean {
		private String value = "start";

		public String getLabel() {
			return "sample";
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		void fire() {
			firePropertyChangeEvent();
		}
	}
}
