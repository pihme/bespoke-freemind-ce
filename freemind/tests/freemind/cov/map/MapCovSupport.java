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

package tests.freemind.cov.map;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.concurrent.ThreadPoolExecutor;

import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.MenuElement;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.TileCache;
import org.openstreetmap.gui.jmapviewer.interfaces.TileJob;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import accessories.plugins.time.TableSorter;
import freemind.extensions.MindMapHook;
import freemind.main.Resources;
import freemind.modes.MindMapNode;
import freemind.modes.NodeAdapter;
import freemind.modes.mindmapmode.MindMapController;
import freemind.view.mindmapview.MapView;
import freemind.view.mindmapview.NodeView;
import plugins.map.FreeMindMapController;
import plugins.map.MapDialog;
import plugins.map.MapNodePositionHolder;
import plugins.map.Registration;
import tests.freemind.cov.MapFixture;
import tests.freemind.cov.MapFixture.Opened;

/**
 * Headless map-plugin session. A real {@link JDialog} cannot be constructed
 * while {@code java.awt.headless} is set, so the dialog is allocated without
 * its window peer and given a normal root pane.
 */
final class MapCovSupport implements AutoCloseable {

	final Opened opened;
	final Registration registration;
	final MapDialog dialog;
	final JDialog window;
	final JMapViewer viewer;
	final FreeMindMapController maps;
	final MapDialog.ResultTableModel results;
	final JTable resultTable;
	final JTextField searchTerm;
	final JLabel status;
	final JLabel searchLabel;
	private final ProxySelector previousProxy;

	private MapCovSupport(Opened opened) throws Exception {
		this.opened = opened;
		previousProxy = installLocalOnlyProxy();
		Properties resources = Resources.getInstance().getProperties();
		put(opened, resources, MapDialog.TILE_CACHE_PURGE_TIME, "86400000");
		put(opened, resources, "scaling_factor_property", "200");
		put(opened, resources, "do_reverse_lookup_on_lat_lon_search", "false");
		put(opened, resources, "osm_nominatim_connect_timeout_in_ms", "400");
		put(opened, resources, "osm_nominatim_read_timeout_in_ms", "400");
		put(opened, resources, "node_map_show_tooltip", "false");
		put(opened, resources, "place_dialogs_on_first_screen", "false");
		System.setProperty("sun.net.client.defaultConnectTimeout", "400");
		System.setProperty("sun.net.client.defaultReadTimeout", "400");

		registration = new Registration(opened.mode, opened.mode.getMap());
		registration.register();

		dialog = new MapDialog();
		dialog.setController(opened.mode);
		dialog.setProperties(new Properties());
		dialog.setPluginBaseClass(base(registration));
		set(dialog, "mMyMindMapController", opened.mode);

		Class<?> closeType = Class.forName("plugins.map.MapDialog$CloseAction");
		Constructor<?> closeCtor = closeType.getDeclaredConstructor(MapDialog.class);
		closeCtor.setAccessible(true);
		set(dialog, "mCloseAction", closeCtor.newInstance(dialog));

		window = shellDialog();
		Class<?> viewerType = Class.forName("plugins.map.JCursorMapViewer");
		Constructor<?> viewerCtor = viewerType.getDeclaredConstructor(MindMapController.class,
				JDialog.class, TileCache.class, MapDialog.class);
		viewerCtor.setAccessible(true);
		viewer = (JMapViewer) viewerCtor.newInstance(opened.mode, window, registration.getTileCache(),
				dialog);
		viewer.setTileLoader(silentLoader());
		viewer.setSize(400, 300);
		set(dialog, "map", viewer);
		maps = (FreeMindMapController) call(viewer, "getFreeMindMapController");

		Coordinate cursor = new Coordinate(49.8, 8.8);
		call(viewer, "setCursorPosition", new Class<?>[] { ICoordinate.class }, cursor);
		call(viewer, "setDisplayPositionByLatLon", new Class<?>[] { double.class, double.class, int.class },
				49.8d, 8.8d, 8);
		call(viewer, "setUseCursor", new Class<?>[] { boolean.class }, true);

		results = dialog.new ResultTableModel(cursor, opened.mode);
		maps.addCursorPositionListener(results);
		resultTable = new JTable();
		TableSorter sorter = new TableSorter(results);
		resultTable.setModel(sorter);
		sorter.setTableHeader(resultTable.getTableHeader());
		searchTerm = new JTextField();
		status = new JLabel(" ");
		searchLabel = new JLabel("search");
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JLabel("results"), viewer);
		split.setDividerLocation(80);
		set(dialog, "mResultTableModel", results);
		set(dialog, "mResultTable", resultTable);
		set(dialog, "mResultTableSorter", sorter);
		set(dialog, "mSearchTerm", searchTerm);
		set(dialog, "mStatusLabel", status);
		set(dialog, "mSearchStringLabel", searchLabel);
		set(dialog, "mResourceSearchString", "search");
		set(dialog, "mResourceSearchLocationString", "search-here");
		set(dialog, "mSearchSplitPane", split);
		set(dialog, "mLastDividerPosition", 80);
		set(dialog, "mTableOriginalBackgroundColor", resultTable.getBackground());
		set(dialog, "mSearchBarVisible", false);
		set(dialog, "mMapDialog", window);
		window.getRootPane().getContentPane().add(split);
		registration.setMapDialog(dialog);
		registration.registerMapNodePositionListener(dialog);
		registration.registerNodeVisibilityListener(dialog);
		for (int i = 0; i < opened.root.getChildCount(); i++) {
			ensureView((MindMapNode) opened.root.getChildAt(i));
		}
	}

	static MapCovSupport open(String... children) throws Exception {
		String[] texts = children.length == 0 ? new String[] { "Child", "Sibling" } : children;
		Opened opened = MapFixture.open(texts);
		try {
			return new MapCovSupport(opened);
		} catch (Exception | Error failure) {
			stopStaticTimer();
			throw failure;
		}
	}

	MapNodePositionHolder attach(MindMapNode node, double lat, double lon) throws Exception {
		return attach(node, lat, lon, 8);
	}

	MapNodePositionHolder attach(MindMapNode node, double lat, double lon, int zoom) throws Exception {
		MapNodePositionHolder holder = new MapNodePositionHolder();
		holder.setController(opened.mode);
		holder.setPluginBaseClass(base(registration));
		holder.setName("plugins/map/MapNodePositionHolder.properties");
		holder.setPosition(new Coordinate(lat, lon));
		holder.setMapCenter(new Coordinate(lat, lon));
		holder.setZoom(zoom);
		holder.setTileSource(OsmTileSource.Mapnik.class.getName());
		node.addHook(holder);
		((NodeAdapter) node).invokeHook(holder);
		return holder;
	}

	void select(MindMapNode node) throws Exception {
		ensureView(node);
		opened.mode.select(node, List.of(node));
	}

	void ensureView(MindMapNode node) throws Exception {
		MapView view = opened.view;
		if (view.getNodeView(node) != null) {
			return;
		}
		Class<?> factoryType = Class.forName("freemind.view.mindmapview.NodeViewFactory");
		Object factory = factoryType.getMethod("getInstance").invoke(null);
		Method method = factoryType.getDeclaredMethod("newNodeView", MindMapNode.class, int.class, MapView.class,
				Container.class);
		method.setAccessible(true);
		method.invoke(factory, node, 1, view, view.getRoot());
	}

	ICoordinate cursor() throws Exception {
		return (ICoordinate) call(viewer, "getCursorPosition");
	}

	Action action(String name) {
		for (Action candidate : actions()) {
			if (name.equals(candidate.getValue(Action.NAME))) {
				return candidate;
			}
		}
		throw new AssertionError("missing action " + name);
	}

	List<Action> actions() {
		List<Action> found = new ArrayList<>();
		JMenuBar bar = window.getJMenuBar();
		if (bar != null) {
			for (int i = 0; i < bar.getMenuCount(); i++) {
				collect(bar.getMenu(i), found);
			}
		}
		collect(maps.getPopupMenu(), found);
		collect(maps.getContextPopupMenu(), found);
		collect(maps.getSearchPopupMenu(), found);
		return found;
	}

	static MouseEvent mouse(java.awt.Component source, int id, int x, int y, int button, int modifiers,
			int clicks, boolean popup) {
		return new MouseEvent(source, id, System.currentTimeMillis(), modifiers, x, y, clicks, popup, button);
	}

	@Override
	public void close() {
		try {
			registration.deRegister();
		} catch (RuntimeException ignored) {
			// The actor map is already clear when a test deregistered it.
		}
		registration.setMapDialog(null);
		stopStaticTimer();
		stopTileJobs();
		if (previousProxy != null) {
			ProxySelector.setDefault(previousProxy);
		}
		viewer.setTileLoader(silentLoader());
	}

	static MindMapHook.PluginBaseClassSearcher base(Registration registration) {
		return () -> registration;
	}

	static Object call(Object target, String name, Class<?>[] types, Object... args) throws Exception {
		Class<?> type = target.getClass();
		while (type != null) {
			try {
				Method method = type.getDeclaredMethod(name, types);
				method.setAccessible(true);
				try {
					return method.invoke(target, args);
				} catch (InvocationTargetException ex) {
					Throwable cause = ex.getCause();
					if (cause instanceof Exception exception) {
						throw exception;
					}
					if (cause instanceof Error error) {
						throw error;
					}
					throw ex;
				}
			} catch (NoSuchMethodException ex) {
				type = type.getSuperclass();
			}
		}
		throw new NoSuchMethodException(name);
	}

	static Object call(Object target, String name) throws Exception {
		return call(target, name, new Class<?>[0]);
	}

	static Object get(Object target, String name) throws Exception {
		Field field = field(target.getClass(), name);
		field.setAccessible(true);
		return field.get(target);
	}

	static void set(Object target, String name, Object value) throws Exception {
		Field field = field(target.getClass(), name);
		field.setAccessible(true);
		field.set(target, value);
	}

	private static Field field(Class<?> type, String name) throws NoSuchFieldException {
		while (type != null) {
			try {
				return type.getDeclaredField(name);
			} catch (NoSuchFieldException ex) {
				type = type.getSuperclass();
			}
		}
		throw new NoSuchFieldException(name);
	}

	private static void put(Opened opened, Properties resources, String key, String value) {
		opened.frame.setProperty(key, value);
		resources.setProperty(key, value);
	}

	private static void collect(MenuElement element, List<Action> found) {
		if (element instanceof JMenuItem item && item.getAction() != null) {
			found.add(item.getAction());
		}
		if (element instanceof JMenu || element instanceof JPopupMenu || element instanceof JMenuItem) {
			for (MenuElement child : element.getSubElements()) {
				collect(child, found);
			}
		}
	}

	private static JDialog shellDialog() throws Exception {
		Class<?> unsafeType = Class.forName("sun.misc.Unsafe");
		Field theUnsafe = unsafeType.getDeclaredField("theUnsafe");
		theUnsafe.setAccessible(true);
		Object unsafe = theUnsafe.get(null);
		JDialog dialog = (JDialog) unsafeType.getMethod("allocateInstance", Class.class).invoke(unsafe, JDialog.class);
		JRootPane root = new JRootPane();
		root.getContentPane().setLayout(new BorderLayout());
		Method offsetOf = unsafeType.getMethod("objectFieldOffset", Field.class);
		Method putObject = unsafeType.getMethod("putObject", Object.class, long.class, Object.class);
		Method putBoolean = unsafeType.getMethod("putBoolean", Object.class, long.class, boolean.class);
		long rootOffset = (Long) offsetOf.invoke(unsafe, JDialog.class.getDeclaredField("rootPane"));
		putObject.invoke(unsafe, dialog, rootOffset, root);
		long checkOffset = (Long) offsetOf.invoke(unsafe,
				JDialog.class.getDeclaredField("rootPaneCheckingEnabled"));
		putBoolean.invoke(unsafe, dialog, checkOffset, true);
		return dialog;
	}

	private static ProxySelector installLocalOnlyProxy() {
		ProxySelector previous = ProxySelector.getDefault();
		ProxySelector.setDefault(new ProxySelector() {
			@Override
			public List<Proxy> select(URI uri) {
				if (uri == null || uri.getHost() == null || "127.0.0.1".equals(uri.getHost())
						|| "localhost".equals(uri.getHost())) {
					return List.of(Proxy.NO_PROXY);
				}
				return List.of(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 9)));
			}

			@Override
			public void connectFailed(URI uri, SocketAddress sa, java.io.IOException ioe) {
			}
		});
		return previous;
	}

	static TileLoader silentLoader() {
		return new TileLoader() {
			@Override
			public TileJob createTileLoaderJob(org.openstreetmap.gui.jmapviewer.Tile tile) {
				return new TileJob() {
					@Override
					public void run() {
					}

					@Override
					public void submit() {
					}

					@Override
					public void submit(boolean force) {
					}
				};
			}

			@Override
			public void cancelOutstandingTasks() {
			}
		};
	}

	static void stopStaticTimer() {
		try {
			Field timer = Registration.class.getDeclaredField("sTimer");
			timer.setAccessible(true);
			Timer running = (Timer) timer.get(null);
			if (running != null) {
				running.cancel();
			}
			timer.set(null, null);
		} catch (ReflectiveOperationException ex) {
			throw new AssertionError(ex);
		}
	}

	static void stopTileJobs() {
		try {
			Class<?> loader = Class.forName("org.openstreetmap.gui.jmapviewer.OsmTileLoader");
			Field field = loader.getDeclaredField("jobDispatcher");
			field.setAccessible(true);
			Object pool = field.get(null);
			if (pool instanceof ThreadPoolExecutor executor) {
				executor.shutdownNow();
			}
		} catch (ReflectiveOperationException ex) {
			throw new AssertionError(ex);
		}
	}

	static MemoryTileCache memoryCache() {
		return new MemoryTileCache();
	}

	static List<String> empty() {
		return Collections.emptyList();
	}
}
