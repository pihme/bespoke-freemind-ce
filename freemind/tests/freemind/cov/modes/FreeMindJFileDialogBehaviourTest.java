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

package tests.freemind.cov.modes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.junit.jupiter.api.Test;

import freemind.modes.FreeMindFileDialog.DirectoryResultListener;
import freemind.modes.FreeMindJFileDialog;

/** The Swing file dialog records a chosen directory without being shown. */
class FreeMindJFileDialogBehaviourTest {

	@Test
	void approveNotifiesTheListenerAndCancelDoesNot() {
		// backwards compatibility
		Probe dialog = new Probe();
		File directory = new File(System.getProperty("java.io.tmpdir"));
		dialog.setCurrentDirectory(directory);
		File[] chosen = new File[1];
		dialog.registerDirectoryResultListener(dir -> chosen[0] = dir);
		dialog.notifyListener(JFileChooser.CANCEL_OPTION);
		assertNull(chosen[0]);
		dialog.notifyListener(JFileChooser.APPROVE_OPTION);
		assertEquals(dialog.getCurrentDirectory(), chosen[0]);
	}

	@Test
	void aThrowingListenerIsSwallowedAndAMissingListenerIsIgnored() {
		// backwards compatibility
		Probe dialog = new Probe();
		dialog.registerDirectoryResultListener(new DirectoryResultListener() {
			public void setChosenDirectory(File dir) {
				throw new IllegalStateException("listener failed");
			}
		});
		dialog.notifyListener(JFileChooser.APPROVE_OPTION);
		dialog.registerDirectoryResultListener(null);
		dialog.notifyListener(JFileChooser.APPROVE_OPTION);
		assertEquals(JFileChooser.FILES_ONLY, dialog.getFileSelectionMode());
	}

	@Test
	void addedFilterBecomesTheCurrentFilter() {
		// backwards compatibility
		Probe dialog = new Probe();
		FileFilter filter = new FileFilter() {
			public boolean accept(File file) {
				return true;
			}

			public String getDescription() {
				return "maps";
			}
		};
		dialog.addChoosableFileFilterAsDefault(filter);
		assertSame(filter, dialog.getFileFilter());
	}

	private static final class Probe extends FreeMindJFileDialog {
		private static final long serialVersionUID = 1L;

		void notifyListener(int result) {
			callDirectoryListener(result);
		}
	}
}
