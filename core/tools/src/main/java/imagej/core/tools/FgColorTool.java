/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.core.tools;

import java.util.List;

import imagej.ImageJ;
import imagej.data.display.DatasetView;
import imagej.data.display.ImageDisplayService;
import imagej.data.options.OptionsChannels;
import imagej.display.DisplayService;
import imagej.display.event.DisplayActivatedEvent;
import imagej.display.event.DisplayDeletedEvent;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.options.OptionsService;
import imagej.options.event.OptionsEvent;
import imagej.plugin.Plugin;
import imagej.tool.AbstractTool;
import imagej.tool.CustomDrawnTool;
import imagej.tool.IconDrawer;
import imagej.tool.IconService;
import imagej.tool.Tool;
import imagej.util.ColorRGB;
import imagej.util.Colors;

/**
 * 
 * @author Barry DeZonia
 *
 */
@Plugin(type = Tool.class, name = "FG", iconPath = "/icons/tools/blank.png",
	description = "Shows the foreground color for the current display",
	priority = -500)
public class FgColorTool extends AbstractTool implements CustomDrawnTool {

	private IconDrawer drawer;
	private List<EventSubscriber<?>> subscribers;
	
	@Override
	public void setContext(ImageJ context) {
		super.setContext(context);
		EventService service = context.getService(EventService.class);
		subscribers = service.subscribe(this);
	}
	
	//@Override
	//public void setIconDrawer(IconDrawer drawer) {
	//	this.drawer = drawer;
	//}
	
	@Override
	public void drawIcon() {
		ImageDisplayService dispSrv = getContext().getService(ImageDisplayService.class);
		DatasetView view = dispSrv == null ? null : dispSrv.getActiveDatasetView();
		ColorRGB color = Colors.WHITE;
		if (view != null) {
			OptionsService oSrv =  getContext().getService(OptionsService.class);
			if (oSrv != null) {
				OptionsChannels options = oSrv.getOptions(OptionsChannels.class);
				color = view.getColor(options.getFgValues());
			}
		}
		draw(color);
	}
	
	@EventHandler
	protected void onEvent(DisplayActivatedEvent evt) {
		drawIcon();
	}

	@EventHandler
	protected void onEvent(DisplayDeletedEvent evt) {
		DisplayService srv = getContext().getService(DisplayService.class);
		if (srv.getActiveDisplay() == null) drawIcon();
	}

	@EventHandler
	protected void onEvent(OptionsEvent evt) {
		// could have changed OptionsChannels so update
		drawIcon();
	}
	
	private void draw(ColorRGB color) {
		if (drawer == null) drawer = acquireDrawer();
		for (int x = 0; x < drawer.getIconRectangleWidth(); x++) {
			for (int y = 0; y < drawer.getIconRectangleHeight(); y++) {
				drawer.setIconPixel(x, y, color);
			}
		}
	}

	private IconDrawer acquireDrawer() {
		IconService service = getContext().getService(IconService.class);
		return service.acquireDrawer(this);
	}
}
