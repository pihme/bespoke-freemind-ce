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

package tests.freemind.cov.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Permission;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import freemind.main.HeadlessFreeMind;

/**
 * The build leaves this class out of the unit classpath because SecurityManager
 * is deprecated for removal. The checks are still the sandbox behaviour, so the
 * test compiles that one file and calls them without installing a manager.
 */
class ScriptingSecurityManagerBehaviourTest {

	private static final String FORBIDDEN = "plugins/ScriptEditor.FORBIDDEN_ACTION";

	private static Class<?> managerType;
	private static URLClassLoader loader;
	private static Path classes;

	@BeforeAll
	static void compileTheExcludedManager() throws Exception {
		new HeadlessFreeMind();
		Path source = sourceFile();
		classes = Files.createTempDirectory("script-security");
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		assertNotNull(compiler);
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		int status = compiler.run(null, null, new PrintStream(err, true, StandardCharsets.UTF_8),
				"-cp", System.getProperty("java.class.path"),
				"-d", classes.toString(),
				source.toString());
		assertEquals(0, status, err.toString(StandardCharsets.UTF_8));
		loader = new URLClassLoader(new URL[] { classes.toUri().toURL() },
				ScriptingSecurityManagerBehaviourTest.class.getClassLoader());
		managerType = loader.loadClass("plugins.script.ScriptingSecurityManager");
	}

	@AfterAll
	static void closeLoader() throws Exception {
		if (loader != null) {
			loader.close();
		}
		if (classes != null) {
			try (var walk = Files.walk(classes)) {
				walk.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
					try {
						Files.deleteIfExists(path);
					} catch (Exception ex) {
						throw new IllegalStateException(ex);
					}
				});
			}
		}
	}

	@Test
	void fileNetworkAndExecChecksThrowWhileTheFlagsAreOff() throws Exception {
		// backwards compatibility
		Object manager = manager(false, false, false);
		InetAddress loopback = InetAddress.getLoopbackAddress();

		denies(manager, "checkDelete", new Class<?>[] { String.class }, "/tmp/cov-script");
		denies(manager, "checkRead", new Class<?>[] { String.class }, "/tmp/cov-script");
		denies(manager, "checkRead", new Class<?>[] { String.class, Object.class }, "/tmp/cov-script", "context");
		denies(manager, "checkRead", new Class<?>[] { FileDescriptor.class }, FileDescriptor.in);
		denies(manager, "checkWrite", new Class<?>[] { String.class }, "/tmp/cov-script");
		denies(manager, "checkWrite", new Class<?>[] { FileDescriptor.class }, FileDescriptor.out);
		denies(manager, "checkAccept", new Class<?>[] { String.class, int.class }, "127.0.0.1", 1);
		denies(manager, "checkConnect", new Class<?>[] { String.class, int.class }, "127.0.0.1", 1);
		denies(manager, "checkConnect", new Class<?>[] { String.class, int.class, Object.class }, "127.0.0.1", 1,
				"context");
		denies(manager, "checkListen", new Class<?>[] { int.class }, 1);
		denies(manager, "checkMulticast", new Class<?>[] { InetAddress.class }, loopback);
		denies(manager, "checkMulticast", new Class<?>[] { InetAddress.class, byte.class }, loopback, (byte) 1);
		denies(manager, "checkSetFactory", new Class<?>[] {});
		denies(manager, "checkExec", new Class<?>[] { String.class }, "true");
		denies(manager, "checkLink", new Class<?>[] { String.class }, "not-a-system-library");
	}

	@Test
	void liftingAFlagAllowsThatGroupAndSystemLibrariesStayLoadable() throws Exception {
		// backwards compatibility
		Object open = manager(true, true, true);
		InetAddress loopback = InetAddress.getLoopbackAddress();
		allows(open, "checkDelete", new Class<?>[] { String.class }, "/tmp/cov-script");
		allows(open, "checkRead", new Class<?>[] { String.class }, "/tmp/cov-script");
		allows(open, "checkRead", new Class<?>[] { String.class, Object.class }, "/tmp/cov-script", "context");
		allows(open, "checkRead", new Class<?>[] { FileDescriptor.class }, FileDescriptor.in);
		allows(open, "checkWrite", new Class<?>[] { String.class }, "/tmp/cov-script");
		allows(open, "checkWrite", new Class<?>[] { FileDescriptor.class }, FileDescriptor.out);
		allows(open, "checkAccept", new Class<?>[] { String.class, int.class }, "127.0.0.1", 1);
		allows(open, "checkConnect", new Class<?>[] { String.class, int.class }, "127.0.0.1", 1);
		allows(open, "checkConnect", new Class<?>[] { String.class, int.class, Object.class }, "127.0.0.1", 1,
				"context");
		allows(open, "checkListen", new Class<?>[] { int.class }, 1);
		allows(open, "checkMulticast", new Class<?>[] { InetAddress.class }, loopback);
		allows(open, "checkMulticast", new Class<?>[] { InetAddress.class, byte.class }, loopback, (byte) 1);
		allows(open, "checkSetFactory", new Class<?>[] {});
		allows(open, "checkExec", new Class<?>[] { String.class }, "true");
		allows(open, "checkLink", new Class<?>[] { String.class }, "not-a-system-library");

		Object execClosed = manager(false, false, false);
		allows(execClosed, "checkLink", new Class<?>[] { String.class }, "awt");
		allows(execClosed, "checkLink", new Class<?>[] { String.class }, "net");
		allows(execClosed, "checkLink", new Class<?>[] { String.class }, "jpeg");
		allows(execClosed, "checkLink", new Class<?>[] { String.class }, "fontmanager");
		denies(execClosed, "checkLink", new Class<?>[] { String.class }, "custom");
		assertEquals(Boolean.TRUE, call(execClosed, "checkTopLevelWindow", new Class<?>[] { Object.class },
				new Object()));
	}

	@Test
	void checksThatTheSandboxDoesNotRestrictReturnWithoutThrowing() throws Exception {
		// backwards compatibility
		Object manager = manager(false, false, false);
		allows(manager, "checkAccess", new Class<?>[] { Thread.class }, Thread.currentThread());
		allows(manager, "checkAccess", new Class<?>[] { ThreadGroup.class }, Thread.currentThread().getThreadGroup());
		allows(manager, "checkAwtEventQueueAccess", new Class<?>[] {});
		allows(manager, "checkCreateClassLoader", new Class<?>[] {});
		allows(manager, "checkExit", new Class<?>[] { int.class }, 0);
		allows(manager, "checkMemberAccess", new Class<?>[] { Class.class, int.class }, String.class, 0);
		allows(manager, "checkPackageAccess", new Class<?>[] { String.class }, "java.lang");
		allows(manager, "checkPackageDefinition", new Class<?>[] { String.class }, "java.lang");
		allows(manager, "checkPermission", new Class<?>[] { Permission.class }, new RuntimePermission("getenv.*"));
		allows(manager, "checkPermission", new Class<?>[] { Permission.class, Object.class },
				new RuntimePermission("getenv.*"), "context");
		allows(manager, "checkPrintJobAccess", new Class<?>[] {});
		allows(manager, "checkPropertiesAccess", new Class<?>[] {});
		allows(manager, "checkPropertyAccess", new Class<?>[] { String.class }, "user.home");
		allows(manager, "checkSecurityAccess", new Class<?>[] { String.class }, "getPolicy");
		allows(manager, "checkSystemClipboardAccess", new Class<?>[] {});
		assertEquals(Boolean.TRUE,
				call(manager, "checkTopLevelWindow", new Class<?>[] { Object.class }, "window"));
	}

	private static Path sourceFile() {
		Path dir = Path.of(System.getProperty("user.dir")).toAbsolutePath();
		for (int i = 0; i < 4 && dir != null; i++) {
			Path nested = dir.resolve("freemind/plugins/script/ScriptingSecurityManager.java");
			if (Files.exists(nested)) {
				return nested;
			}
			Path direct = dir.resolve("plugins/script/ScriptingSecurityManager.java");
			if (Files.exists(direct)) {
				return direct;
			}
			dir = dir.getParent();
		}
		throw new IllegalStateException("ScriptingSecurityManager.java not found from " + System.getProperty("user.dir"));
	}

	private static Object manager(boolean files, boolean network, boolean exec) throws Exception {
		return managerType.getConstructor(boolean.class, boolean.class, boolean.class)
				.newInstance(files, network, exec);
	}

	private static void denies(Object manager, String name, Class<?>[] types, Object... args) throws Exception {
		InvocationTargetException ex = assertThrows(InvocationTargetException.class,
				() -> call(manager, name, types, args));
		assertTrue(ex.getCause() instanceof SecurityException);
		assertEquals(FORBIDDEN, ex.getCause().getMessage());
	}

	private static void allows(Object manager, String name, Class<?>[] types, Object... args) throws Exception {
		call(manager, name, types, args);
	}

	private static Object call(Object manager, String name, Class<?>[] types, Object... args) throws Exception {
		Method method = managerType.getMethod(name, types);
		return method.invoke(manager, args);
	}
}
