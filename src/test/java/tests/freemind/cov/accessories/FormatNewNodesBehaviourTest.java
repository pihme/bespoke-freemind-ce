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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import accessories.plugins.FormatNewNodes;
import freemind.controller.actions.generated.instance.FoldAction;
import freemind.modes.mindmapmode.actions.xml.ActionPair;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

@DisplayName("FormatNewNodesBehaviourTest")
class FormatNewNodesBehaviourTest {

	// backwards compatibility
	@Test
	void transactionMarkersLeaveTheFilterResultUnchanged() throws Exception {
		Opened opened = MapFixture.open("Child");
		FormatNewNodes formatter = new FormatNewNodes(opened.mode, opened.mode.getMap());
		FoldAction fold = new FoldAction();
		fold.setNode(opened.mode.getNodeID(opened.child("Child")));
		ActionPair pair = new ActionPair(fold, new FoldAction());

		formatter.startTransaction("format");
		formatter.endTransaction("format");
		ActionPair filtered = formatter.filterAction(pair);

		assertSame(pair, filtered);
		assertEquals(opened.mode.getNodeID(opened.child("Child")),
				((FoldAction) filtered.getDoAction()).getNode());
	}
}
