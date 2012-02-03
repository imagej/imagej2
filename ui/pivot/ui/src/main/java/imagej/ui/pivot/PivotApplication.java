//
// PivotApplication.java
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

package imagej.ui.pivot;

import imagej.ImageJ;
import imagej.event.EventService;
import imagej.ext.menu.MenuService;
import imagej.ext.ui.pivot.PivotMenuCreator;
import imagej.platform.event.AppMenusCreatedEvent;

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Orientation;

/**
 * Pivot {@link Application} implementation for ImageJ.
 * 
 * @author Curtis Rueden
 */
public class PivotApplication implements Application {

	private ImageJ context;
	private EventService eventService;
	private MenuService menuService;

	private PivotApplicationFrame frame;
	private PivotToolBar toolBar;
	private PivotStatusBar statusBar;

	private BoxPane contentPane;

	// -- Constructor --

	public PivotApplication() {
		// get the current ImageJ context
		context = ImageJ.getContext();
		eventService = context.getService(EventService.class);
		menuService = context.getService(MenuService.class);
	}

	// -- Application methods --

	@Override
	public void startup(final Display display,
		final Map<String, String> properties)
	{
		frame = new PivotApplicationFrame();
		toolBar = new PivotToolBar();
		System.out.println("PivotUI: " + this);
		System.out.println("PivotUI.startup: event service = " + eventService);
		statusBar = new PivotStatusBar(eventService);

		contentPane = new BoxPane();
		contentPane.setOrientation(Orientation.VERTICAL);
		frame.setContent(contentPane);

		// create menus
		final BoxPane menuPane =
			menuService.createMenus(new PivotMenuCreator(), new BoxPane());
		contentPane.add(menuPane);
		eventService.publish(new AppMenusCreatedEvent(menuPane));

		contentPane.add(toolBar);
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

}
