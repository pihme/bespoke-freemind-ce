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

package tests.freemind.cov.accessories;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.HeadlessException;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import accessories.plugins.ApplyFormatPlugin;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

class ApplyFormatPluginBehaviourTest {

	// backwards compatibility
	@Test
	void theFormatDialogCannotOpenHeadlessSoTheNodeStaysPlain() throws Exception {
		Opened opened = MapFixture.open("Child");
		opened.view.getRoot().updateAll();
		opened.view.selectAsTheOnlyOneSelected(opened.view.getNodeView(opened.child("Child")));
		ApplyFormatPlugin hook = new ApplyFormatPlugin();
		hook.setController(opened.mode);
		hook.setMap(opened.mode.getMap());
		hook.setProperties(new Properties());

		assertThrows(HeadlessException.class, () -> hook.invoke(opened.root));

		assertFalse(opened.child("Child").isBold());
	}
}
