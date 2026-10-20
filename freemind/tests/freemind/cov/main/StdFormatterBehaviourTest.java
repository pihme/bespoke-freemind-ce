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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.Test;

class StdFormatterBehaviourTest {

	@Test
	void stdoutRecordsIncludeTheLevelAndOtherLoggersUseTheSimpleFormat() throws Exception {
		// backwards compatibility
		Class<?> type = Class.forName("freemind.main.StdFormatter");
		Field stdoutField = type.getDeclaredField("STDOUT");
		stdoutField.setAccessible(true);
		Level stdout = (Level) stdoutField.get(null);
		assertEquals("STDOUT", stdout.getName());
		Field stderrField = type.getDeclaredField("STDERR");
		stderrField.setAccessible(true);
		Level stderr = (Level) stderrField.get(null);
		assertEquals("STDERR", stderr.getName());

		Constructor<?> constructor = type.getDeclaredConstructor();
		constructor.setAccessible(true);
		Object formatter = constructor.newInstance();
		Method format = type.getDeclaredMethod("format", LogRecord.class);
		format.setAccessible(true);

		LogRecord outRecord = new LogRecord(stdout, "hello {0}");
		outRecord.setLoggerName(stdout.getName());
		outRecord.setParameters(new Object[] { "world" });
		String outText = (String) format.invoke(formatter, outRecord);
		assertTrue(outText.contains("hello world"));
		assertTrue(outText.contains(stdout.getLocalizedName()));

		LogRecord errRecord = new LogRecord(stderr, "bad");
		errRecord.setLoggerName(stderr.getName());
		String errText = (String) format.invoke(formatter, errRecord);
		assertTrue(errText.contains("bad"));
		assertTrue(errText.startsWith(System.lineSeparator()));

		LogRecord plain = new LogRecord(Level.INFO, "plain-message");
		plain.setLoggerName("freemind.other");
		String plainText = (String) format.invoke(formatter, plain);
		assertTrue(plainText.contains("plain-message"));
		assertTrue(plainText.contains("INFO"));
	}
}
