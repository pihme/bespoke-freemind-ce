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

package tests.freemind.cov.socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import freemind.controller.actions.generated.instance.CollaborationActionBase;
import freemind.controller.actions.generated.instance.XmlAction;
import freemind.main.Tools;
import freemind.modes.ExtendedMapFeedback;
import freemind.modes.mindmapmode.actions.xml.ActionPair;
import plugins.collaboration.socket.ServerCommunication;
import plugins.collaboration.socket.SocketMaster;
import plugins.collaboration.socket.TerminateableThread;

/** Local sockets and reflection used by the collaboration socket tests. */
final class SocketCovSupport {

	static final String MAP = "<map version=\"1.1.0\"><node TEXT=\"ROOT\" ID=\"ID_1\">"
			+ "<node TEXT=\"Child\" ID=\"ID_2\"/></node></map>";

	static final String CONTINUATION = "<cont>";

	private SocketCovSupport() {
	}

	static final class Pipe implements AutoCloseable {
		final ServerSocket server;
		final Socket local;
		final Socket remote;
		private final DataInputStream fromLocal;
		private final DataOutputStream toLocal;

		private Pipe(ServerSocket server, Socket local, Socket remote, DataInputStream fromLocal,
				DataOutputStream toLocal) {
			this.server = server;
			this.local = local;
			this.remote = remote;
			this.fromLocal = fromLocal;
			this.toLocal = toLocal;
		}

		static Pipe open() throws IOException {
			ServerSocket server = new ServerSocket();
			server.setReuseAddress(true);
			server.bind(new InetSocketAddress("127.0.0.1", 0));
			server.setSoTimeout(2000);
			Socket remote = new Socket();
			remote.connect(new InetSocketAddress("127.0.0.1", server.getLocalPort()), 2000);
			remote.setSoTimeout(2000);
			Socket local = server.accept();
			local.setSoTimeout(2000);
			return new Pipe(server, local, remote, new DataInputStream(remote.getInputStream()),
					new DataOutputStream(remote.getOutputStream()));
		}

		CollaborationActionBase read() throws IOException {
			return readMessage(fromLocal);
		}

		void write(CollaborationActionBase command) throws IOException {
			writeMessage(toLocal, command);
		}

		void writeRaw(String utf) throws IOException {
			toLocal.writeUTF(utf);
			toLocal.flush();
		}

		public void close() throws IOException {
			try {
				local.close();
			} catch (IOException ignored) {
				// The peer may already have closed it.
			}
			try {
				remote.close();
			} catch (IOException ignored) {
				// The peer may already have closed it.
			}
			server.close();
		}
	}

	static CollaborationActionBase readMessage(DataInputStream in) throws IOException {
		StringBuilder compressed = new StringBuilder();
		while (true) {
			String chunk = in.readUTF();
			if (chunk.endsWith(CONTINUATION)) {
				compressed.append(chunk, 0, chunk.length() - CONTINUATION.length());
			} else {
				compressed.append(chunk);
				break;
			}
		}
		XmlAction action = Tools.unMarshall(Tools.decompress(compressed.toString()));
		if (!(action instanceof CollaborationActionBase)) {
			return null;
		}
		return (CollaborationActionBase) action;
	}

	static void writeMessage(DataOutputStream out, CollaborationActionBase command) throws IOException {
		out.writeUTF(Tools.compress(Tools.marshall(command)));
		out.flush();
	}

	static ServerCommunication server(SocketMaster master, Socket socket, ExtendedMapFeedback controller,
			boolean singleMap) throws Exception {
		ServerCommunication communication = new ServerCommunication(master, socket, controller, singleMap);
		hurry(communication);
		return communication;
	}

	static ExtendedMapFeedback hostMap(SocketMaster master, File directory, String name) throws Exception {
		File file = new File(directory, name);
		return master.createMapOnServer(name, new Tools.StringReaderCreator(MAP), file);
	}

	static void hurry(Thread thread) {
		set(thread, "mSleepTime", Integer.valueOf(1));
	}

	static void stopOwnedThreads() {
		List<TerminateableThread> alive = new ArrayList<>();
		for (Thread thread : Thread.getAllStackTraces().keySet()) {
			if (thread instanceof TerminateableThread && thread.isAlive()) {
				hurry(thread);
				alive.add((TerminateableThread) thread);
			}
		}
		for (TerminateableThread thread : alive) {
			thread.commitSuicide();
		}
		for (TerminateableThread thread : alive) {
			try {
				thread.join(500);
			} catch (InterruptedException interrupted) {
				Thread.currentThread().interrupt();
			}
		}
	}

	static void waitFor(BooleanSupplier condition) throws InterruptedException {
		long deadline = System.currentTimeMillis() + 3000;
		while (!condition.getAsBoolean() && System.currentTimeMillis() < deadline) {
			Thread.sleep(20);
		}
		if (!condition.getAsBoolean()) {
			throw new AssertionError("timed out waiting for a socket condition");
		}
	}

	static void set(Object target, String name, Object value) {
		Field field = field(target, name);
		try {
			field.set(target, value);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	static Object get(Object target, String name) {
		try {
			return field(target, name).get(target);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	static Object session(SocketMaster master, ExtendedMapFeedback controller) {
		try {
			Method method = SocketMaster.class.getDeclaredMethod("getSessionData", ExtendedMapFeedback.class);
			method.setAccessible(true);
			return method.invoke(master, controller);
		} catch (ReflectiveOperationException e) {
			Throwable cause = e.getCause() == null ? e : e.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			}
			throw new IllegalStateException(cause);
		}
	}

	static boolean lockFailure(Throwable error) {
		return error != null && error.getClass().getName().endsWith("UnableToGetLockException");
	}

	private static Field field(Object target, String name) {
		Class<?> type = target.getClass();
		while (type != null) {
			try {
				Field found = type.getDeclaredField(name);
				found.setAccessible(true);
				return found;
			} catch (NoSuchFieldException e) {
				type = type.getSuperclass();
			}
		}
		throw new IllegalArgumentException("missing field " + name);
	}
}

/** Socket master with the two abstract methods filled in. */
final class TestMaster extends SocketMaster {

	File base = new File(".");
	int titles;
	boolean interruptLock;

	void setPassword(String password) {
		mPassword = password;
	}

	void setPort(int port) {
		mPort = port;
	}

	protected void setTitle() {
		titles++;
	}

	protected File getBaseFile() {
		return base;
	}

	void remember(String name, ExtendedMapFeedback map) {
		addSession(name, map);
	}

	String takeLock(String user, ExtendedMapFeedback map) throws Exception {
		return lock(user, map);
	}

	@Override
	protected String lock(String user, ExtendedMapFeedback map) throws InterruptedException, UnableToGetLockException {
		if (interruptLock) {
			throw new InterruptedException("paused");
		}
		return super.lock(user, map);
	}

	void release(ExtendedMapFeedback map) {
		unlock(map);
	}

	void cast(String doAction, String undoAction, String lockId, ExtendedMapFeedback map) throws Exception {
		broadcastCommand(doAction, undoAction, lockId, map);
	}

	void transact(ActionPair pair, ExtendedMapFeedback map) {
		executeTransaction(pair, map);
	}

	boolean filterEnabled() {
		return mFilterEnabled;
	}
}
