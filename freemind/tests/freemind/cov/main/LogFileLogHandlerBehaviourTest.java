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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.Test;

import freemind.main.LogFileLogHandler;

class LogFileLogHandlerBehaviourTest {

	@Test
	void publishForwardsOnlyLoggableRecordsToTheReceiver() {
		// backwards compatibility
		LogFileLogHandler silent = new LogFileLogHandler();
		assertNull(silent.getLogReceiver());
		silent.publish(new LogRecord(Level.INFO, "ignored"));
		assertNull(silent.getLogReceiver());
		silent.flush();
		silent.close();

		List<LogRecord> received = new ArrayList<>();
		LogFileLogHandler.LogReceiver receiver = received::add;
		silent.setLogReceiver(receiver);
		assertSame(receiver, silent.getLogReceiver());
		LogRecord info = new LogRecord(Level.INFO, "kept");
		silent.publish(info);
		assertEquals(1, received.size());
		assertSame(info, received.get(0));

		silent.setLevel(Level.SEVERE);
		silent.publish(new LogRecord(Level.INFO, "filtered"));
		assertEquals(1, received.size());

		LogFileLogHandler wired = new LogFileLogHandler(receiver);
		wired.publish(new LogRecord(Level.INFO, "second"));
		assertEquals("second", received.get(1).getMessage());
	}
}
