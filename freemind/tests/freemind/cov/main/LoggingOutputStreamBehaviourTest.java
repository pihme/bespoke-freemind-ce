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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("LoggingOutputStreamBehaviourTest")
class LoggingOutputStreamBehaviourTest {

	@Test
	void flushLogsTextAndDropsAnEmptyOrSeparatorOnlyBuffer() throws Exception {
		// backwards compatibility
		Logger logger = Logger.getLogger("tests.freemind.cov.main.LoggingOutputStream");
		logger.setLevel(Level.ALL);
		List<String> messages = new ArrayList<>();
		Handler handler = new Handler() {
			@Override
			public void publish(LogRecord record) {
				messages.add(record.getMessage());
			}

			@Override
			public void flush() {
			}

			@Override
			public void close() {
			}
		};
		handler.setLevel(Level.ALL);
		logger.addHandler(handler);
		try {
			Class<?> type = Class.forName("freemind.main.LoggingOutputStream");
			Constructor<?> constructor = type.getDeclaredConstructor(Logger.class, Level.class);
			constructor.setAccessible(true);
			OutputStream stream = (OutputStream) constructor.newInstance(logger, Level.INFO);
			stream.write("hello".getBytes(StandardCharsets.UTF_8));
			stream.flush();
			assertTrue(messages.stream().anyMatch(message -> message.contains("hello")));
			int afterHello = messages.size();
			stream.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
			stream.flush();
			stream.flush();
			assertEquals(afterHello, messages.size());
		} finally {
			logger.removeHandler(handler);
		}
	}
}
