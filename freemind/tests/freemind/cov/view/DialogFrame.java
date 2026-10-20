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

package tests.freemind.cov.view;

import java.awt.Container;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import freemind.controller.Controller;
import freemind.controller.MenuBar;
import freemind.main.FreeMindMain;
import freemind.view.mindmapview.MapView;

/** JFrame owner for edit dialogs. HeadlessFreeMind is not a JFrame. */
public class DialogFrame extends JFrame implements FreeMindMain {

	private static final long serialVersionUID = 1L;
	private final FreeMindMain delegate;

	public DialogFrame(FreeMindMain delegate) {
		this.delegate = delegate;
	}

	public JFrame getJFrame() {
		return this;
	}

	public boolean isApplet() {
		return delegate.isApplet();
	}

	public MapView getView() {
		return delegate.getView();
	}

	public void setView(MapView view) {
		delegate.setView(view);
	}

	public Controller getController() {
		return delegate.getController();
	}

	public void setWaitingCursor(boolean waiting) {
		delegate.setWaitingCursor(waiting);
	}

	public File getPatternsFile() {
		return delegate.getPatternsFile();
	}

	public MenuBar getFreeMindMenuBar() {
		return delegate.getFreeMindMenuBar();
	}

	public ResourceBundle getResources() {
		return delegate.getResources();
	}

	public String getResourceString(String key) {
		return delegate.getResourceString(key);
	}

	public String getResourceString(String key, String defaultResource) {
		return delegate.getResourceString(key, defaultResource);
	}

	public void out(String msg) {
		delegate.out(msg);
	}

	public void err(String msg) {
		delegate.err(msg);
	}

	public void openDocument(URL location) throws Exception {
		delegate.openDocument(location);
	}

	public URL getResource(String name) {
		return delegate.getResource(name);
	}

	public int getIntProperty(String key, int defaultValue) {
		return delegate.getIntProperty(key, defaultValue);
	}

	public Properties getProperties() {
		return delegate.getProperties();
	}

	public String getProperty(String key) {
		return delegate.getProperty(key);
	}

	public void setProperty(String key, String value) {
		delegate.setProperty(key, value);
	}

	public void saveProperties(boolean pIsShutdown) {
		delegate.saveProperties(pIsShutdown);
	}

	public String getFreemindDirectory() {
		return delegate.getFreemindDirectory();
	}

	public int getWinHeight() {
		return delegate.getWinHeight();
	}

	public int getWinWidth() {
		return delegate.getWinWidth();
	}

	public int getWinState() {
		return delegate.getWinState();
	}

	public int getWinX() {
		return delegate.getWinX();
	}

	public int getWinY() {
		return delegate.getWinY();
	}

	public VersionInformation getFreemindVersion() {
		return delegate.getFreemindVersion();
	}

	public Logger getLogger(String forClass) {
		return delegate.getLogger(forClass);
	}

	public JSplitPane insertComponentIntoSplitPane(JComponent component) {
		return delegate.insertComponentIntoSplitPane(component);
	}

	public void removeSplitPane() {
		delegate.removeSplitPane();
	}

	public ClassLoader getFreeMindClassLoader() {
		return delegate.getFreeMindClassLoader();
	}

	public String getFreemindBaseDir() {
		return delegate.getFreemindBaseDir();
	}

	public String getAdjustableProperty(String label) {
		return delegate.getAdjustableProperty(label);
	}

	public void setDefaultProperty(String key, String value) {
		delegate.setDefaultProperty(key, value);
	}

	public JComponent getContentComponent() {
		return delegate.getContentComponent();
	}

	public JScrollPane getScrollPane() {
		return delegate.getScrollPane();
	}

	public void registerStartupDoneListener(StartupDoneListener listener) {
		delegate.registerStartupDoneListener(listener);
	}

	public List<Logger> getLoggerList() {
		return delegate.getLoggerList();
	}
}
