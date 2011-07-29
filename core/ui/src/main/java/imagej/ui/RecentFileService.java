//
// RecentFileService.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.ui;

import imagej.AbstractService;
import imagej.event.EventSubscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

//TODO Enable...
// @Service

/**
 * Service for managing the Recently Used Files menu.
 * <p>
 * Behavior: There is a limited number of files presented (maxFilesShown),
 * regardless of the list length. When a file is opened, its path is added to
 * the top of the list. If an image has been saved as a new file, its path is
 * added to the top of the list upon
 * </p>
 * <ul>
 * <li>add(String path)</li>
 * <li>remove(String path)</li>
 * </ul>
 * 
 * @author Grant Harris
 */
public final class RecentFileService extends AbstractService {

	private static final int maxFilesShown = 10;
	int maxDisplayLength = 40; // max. pathname length shown.
	private static final String recentMenuName = "Recent Files";
	/** Maintain list of subscribers, to avoid garbage collection. */
	private List<EventSubscriber<?>> subscribers;
	List<String> recentFiles = new ArrayList<String>();

	public RecentFileService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	// private final JMenuBar menuBar;
//	SwingRecentFileMgr(JMenuBar menuBar) {
//		this.menuBar = menuBar;
//		subscribeToEvents();
//	}
//	
//	void test(JMenuItem menuitem){
//	JPopupMenu fromParent = (JPopupMenu)menuitem.getParent();  
//JMenu foo = (JMenu)fromParent.getInvoker();  
//System.out.println(foo.getName());  
//	}
	@Override
	public void initialize() {
		subscribeToEvents();
	}

	private void subscribeToEvents() {
		subscribers = new ArrayList<EventSubscriber<?>>();
//		final EventSubscriber<FileOpenedEvent> fileOpenedSubscriber =
//				new EventSubscriber<FileOpenedEvent>() {
//
//					@Override
//					public void onEvent(final FileOpenedEvent event) {
//						updateFilesRecentlyOpened(event.getPath());
//					}
//
//				};
//		subscribers.add(fileOpenedSubscriber);
//		Events.subscribe(FileOpenedEvent.class, fileOpenedSubscriber);

		// TODO
		// FileSavedEvent
		// ?? FileClosedEvent
		// DisplayCreatedEvent
		// DisplayDeletedEvent

	}

	private void updateFilesRecentlyOpened(final String path) {
//		JMenu menu = new JMenu(recentMenuName);
//		for (String file : recentFiles) {
//			addOpenAction(file, menu);
//		}
	}

	public void add(final String path) {
		// already there, remove it
		remove(path);
		// add it to the end
		recentFiles.add(path);
	}

	public void remove(final String path) {
		final int idx = recentFiles.indexOf(path);
		if (idx > -1) {
			recentFiles.remove(idx);
		}
	}

	/*
	 * returns list of file paths no longer than maxFilesShown
	 * 
	 */

	public List<String> getList() {
		final List<String> list = new ArrayList();
		int n = 0;

		for (final ListIterator<String> it =
			recentFiles.listIterator(recentFiles.size()); it.hasPrevious();)
		{
			if (n++ > maxFilesShown) {
				break;
			}
			final String t = it.previous();
			list.add(t);
		}
		return list;
	}

//	private void addOpenAction(final String file, JMenu menu) {
//		Action action = new AbstractAction(file) {
//
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				System.out.println("Open File: " + file);
//				// openFile(file);
//			}
//
//		};
//		JMenuItem menuItem = new JMenuItem(action);
//		menu.add(menuItem);
//	}
	public void rebuild() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public List getRecentFileList() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

//private <T extends BasePlugin> PluginEntry<T> createEntry(
//		final IndexItem<Plugin, BasePlugin> item)
//	{
//		final String className = item.className();
//		final Plugin plugin = item.annotation();
//
//		@SuppressWarnings("unchecked")
//		final Class<T> pluginType = (Class<T>) plugin.type();
//
//		final PluginEntry<T> entry = new PluginEntry<T>(className, pluginType);
//		entry.setName(plugin.name()); 
//		entry.setLabel(plugin.label());
//		entry.setDescription(plugin.description());
//		final String iconPath = plugin.iconPath();
//		entry.setIconPath(iconPath);
//		entry.setPriority(plugin.priority());
//		entry.setEnabled(plugin.enabled());
//
//		entry.setToggleParameter(plugin.toggleParameter());
//		entry.setToggleGroup(plugin.toggleGroup());
//
//		final MenuPath menuPath = new MenuPath();
//		final Menu[] menu = plugin.menu();
//		if (menu.length > 0) {
//			parseMenuPath(menuPath, menu);
//		}
//		else {
//			// parse menuPath attribute
//			final String path = plugin.menuPath();
//			if (!path.isEmpty()) parseMenuPath(menuPath, path);
//		}
//
//		// add default icon if none attached to leaf
//		final MenuEntry menuLeaf = menuPath.getLeaf();
//		if (menuLeaf != null && !iconPath.isEmpty()) {
//			final String menuIconPath = menuLeaf.getIconPath();
//			if (menuIconPath == null || menuIconPath.isEmpty()) {
//				menuLeaf.setIconPath(iconPath);
//			}
//		}
//
//		entry.setMenuPath(menuPath);
//
//		return entry;
//	}

	public static void main(final String[] args) {
		final RecentFileService r = new RecentFileService();
		for (int i = 0; i < 20; i++) {
			final String t =
				"C:\\Documents and Settings\\All Users\\Application Data\\Apple Computer\\iTunes\\SC Info\\SCInfo" +
					i + ".txt";
			r.add(t);
		}

		final int n = 0;
//		for (String file : r.recentFiles) {
//			if(n++>r.maxFilesShown) break; 
//			System.out.println(file);
//		}
//		n = 0;
//		for (String file : r.recentFiles) {
//			if(n++>r.maxFilesShown) break; 
//			System.out.println(file);
//		}
		final List<String> list = r.getList();
		for (final String string : list) {
			System.out.println(string);
		}

	}

}
