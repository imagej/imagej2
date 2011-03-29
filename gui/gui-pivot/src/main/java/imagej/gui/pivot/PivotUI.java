//
// PivotUI.java
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

package imagej.gui.pivot;

import imagej.plugin.PluginEntry;
import imagej.plugin.PluginUtils;
import imagej.plugin.gui.ShadowMenu;
import imagej.plugin.gui.pivot.PivotMenuCreator;
import imagej.ui.UserInterface;
import imagej.ui.UI;

import java.util.List;

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.Orientation;

/**
 * TODO
 * 
 * @author Curtis Rueden
 */
@UI
public class PivotUI implements Application, UserInterface {

	private Frame frame;
	private BoxPane contentPane;
	private PivotStatusBar statusBar;

	// -- Application methods --

	@Override
	public void startup(final Display display,
		final Map<String, String> properties)
	{
		frame = new Frame();
//		toolBar = new PivotToolBar();
		statusBar = new PivotStatusBar();

		contentPane = new BoxPane();
		contentPane.setOrientation(Orientation.VERTICAL);
		frame.setContent(contentPane);
		createMenuBar();

		contentPane.add(statusBar);

		frame.setTitle("ImageJ");
		frame.setMaximized(true);
		frame.open(display);
	}

	@Override
	public boolean shutdown(final boolean optional) {
		if (frame != null) frame.close();
		return false;
	}

	@Override
	public void suspend() {
		// NB: no action needed.
	}

	@Override
	public void resume() {
		// NB: no action needed.
	}

	// -- UserInterface methods --

	@Override
	public void initialize() {
		final String[] args = { getClass().getName() };
		DesktopApplicationContext.main(args);
	}

	// -- Helper methods --

	private void createMenuBar() {
		final List<PluginEntry<?>> entries = PluginUtils.findPlugins();
		statusBar.setStatus("Discovered " + entries.size() + " plugins");
		final ShadowMenu rootMenu = new ShadowMenu(entries);
		final BoxPane menuPane = new BoxPane();
		new PivotMenuCreator().createMenus(rootMenu, menuPane);
		contentPane.add(menuPane);
	}

}
