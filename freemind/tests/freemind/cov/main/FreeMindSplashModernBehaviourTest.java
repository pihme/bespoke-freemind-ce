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

package tests.freemind.cov.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import freemind.main.FeedBack;
import freemind.main.FreeMind;
import freemind.main.FreeMindSplashModern;
import freemind.main.HeadlessFreeMind;

@Tag("gui")
class FreeMindSplashModernBehaviourTest {

	@Test
	void progressPaintsTheVersionAndTheBarWithoutShowingTheWindow() throws Exception {
		// backwards compatibility
		Properties previousProps = FreeMind.props;
		if (previousProps == null) {
			FreeMind.props = new Properties();
		}
		try {
			HeadlessFreeMind frame = new HeadlessFreeMind();
			FreeMindSplashModern splash = new FreeMindSplashModern(frame);
			try {
				assertNotNull(splash.getWindowIcon());
				FeedBack feedback = splash.getFeedBack();
				assertSame(feedback, splash.getFeedBack());
				feedback.setMaximumValue(10);
				assertEquals(0, feedback.getActualValue());
				JLabel label = (JLabel) splash.getContentPane().getComponent(0);
				label.setSize(300, 200);
				int quiet = nonWhite(paint(label));
				assertTrue(quiet > 0);

				feedback.progress(0, "coverage.task", new Object[0]);
				SwingUtilities.invokeAndWait(() -> {
				});
				assertEquals(0, feedback.getActualValue());
				assertEquals("coverage.task", label.getClientProperty("progressString"));
				assertNotNull(label.getClientProperty("progressPercent"));
				int emptyBar = nonWhite(paint(label));
				feedback.increase("coverage.task", new Object[] { "step" });
				SwingUtilities.invokeAndWait(() -> {
				});
				assertEquals(1, feedback.getActualValue());
				int filled = nonWhite(paint(label));
				assertTrue(filled >= emptyBar);
				assertFalse(splash.isVisible());
			} finally {
				splash.close();
				assertFalse(splash.isDisplayable());
			}
		} finally {
			FreeMind.props = previousProps;
		}
	}

	private static BufferedImage paint(Component component) {
		BufferedImage image = new BufferedImage(component.getWidth(), component.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = image.getGraphics();
		try {
			component.paint(graphics);
		} finally {
			graphics.dispose();
		}
		return image;
	}

	private static int nonWhite(BufferedImage image) {
		int painted = 0;
		for (int y = 0; y < image.getHeight(); y += 4) {
			for (int x = 0; x < image.getWidth(); x += 4) {
				if ((image.getRGB(x, y) >>> 24) != 0) {
					painted++;
				}
			}
		}
		return painted;
	}
}
