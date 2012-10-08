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
import imagej.command.CommandService;
import imagej.data.ChannelCollection;
import imagej.data.display.DatasetView;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.event.AxisPositionEvent;
import imagej.data.options.OptionsChannels;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.display.event.DisplayActivatedEvent;
import imagej.display.event.DisplayDeletedEvent;
import imagej.display.event.input.MsButtonEvent;
import imagej.display.event.input.MsClickedEvent;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.event.StatusService;
import imagej.options.OptionsService;
import imagej.options.event.OptionsEvent;
import imagej.tool.AbstractTool;
import imagej.tool.CustomDrawnTool;
import imagej.tool.IconDrawer;
import imagej.tool.IconService;
import imagej.util.ColorRGB;

/**
 * 
 * @author Barry DeZonia
 *
 */
public abstract class AbstractColorTool
	extends AbstractTool
	implements CustomDrawnTool
{
	// -- constants --
	
	public static final int BASE_PRIORITY = -500;
	
	// -- instance variables --
	
	private IconDrawer drawer;
	private List<EventSubscriber<?>> subscribers;
	private final PixelRecorder recorder = new PixelRecorder(true);
	private StatusService statusService = null;
	
	// -- abstract methods --
	
	abstract ColorRGB getEmptyColor();
	abstract ColorRGB getOutlineColor();
	abstract ChannelCollection getChannels(OptionsChannels options);
	abstract void setChannels(OptionsChannels options, ChannelCollection chans);
	abstract void setLastColor(OptionsChannels options, ColorRGB color);
	abstract String getLabel();
	
	// -- Tool methods --
	
	@Override
	public void setContext(ImageJ context) {
		super.setContext(context);
		EventService service = context.getService(EventService.class);
		subscribers = service.subscribe(this);
	}
	
	
	@Override
	public void configure() {
		final CommandService commandService =
			getContext().getService(CommandService.class);
		
		commandService.run(OptionsChannels.class);
	}
	
	@Override
	public void onMouseClick(final MsClickedEvent evt) {

		if (evt.getButton() != MsButtonEvent.LEFT_BUTTON) return;

		// if click did not happen within the bounds of an ImageDisplay then
		// just consume event and return
		if (!recorder.record(evt)) {
			evt.consume();
			return;
		}

		statusService = getContext().getService(StatusService.class);

		final OptionsChannels options = getOptions();

		final ChannelCollection values = recorder.getValues();

		setChannels(options, values);

		setLastColor(options, recorder.getColor());

		// make sure future options reflect those new values
		options.save();

		// let user know the FG or BG values changed
		statusMessage(getLabel(), values);

		evt.consume();
	}
	
	@Override
	public String getDescription() {
		OptionsChannels opts = getOptions();
		ChannelCollection channels = getChannels(opts);
		StringBuilder sb = new StringBuilder();
		sb.append(getLabel());
		sb.append(" = ");
		sb.append(valuesString(channels));
		return sb.toString();
	}

	// -- CustomDrawnTool methods --
	
	@Override
	public void drawIcon() {
		ImageDisplayService dispSrv =
				getContext().getService(ImageDisplayService.class);
		DatasetView view = dispSrv == null ? null : dispSrv.getActiveDatasetView();
		ColorRGB color = getEmptyColor();
		if (view != null) {
			OptionsService oSrv =  getContext().getService(OptionsService.class);
			if (oSrv != null) {
				OptionsChannels options = oSrv.getOptions(OptionsChannels.class);
				ChannelCollection channels = getChannels(options);
				color = view.getColor(channels);
			}
		}
		draw(color);
	}

	// -- event handlers --
	
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
		if (evt.getOptions() instanceof OptionsChannels) drawIcon();
	}
	
	@EventHandler
	protected void onEvent(AxisPositionEvent evt) {
		DisplayService dispSrv = getContext().getService(DisplayService.class);
		Display<?> activeDisplay = dispSrv.getActiveDisplay();
		if (evt.getDisplay() == activeDisplay) drawIcon();
	}
	
	// -- private helpers --
	
	private void draw(ColorRGB fillColor) {
		if (drawer == null) drawer = acquireDrawer();
		int width = drawer.getIconRectangleWidth();
		int height = drawer.getIconRectangleHeight();
		for (int y = 0; y < height; y++) {
			drawer.setIconPixel(0, y, getOutlineColor());
			drawer.setIconPixel(width-1, y, getOutlineColor());
		}
		for (int x = 0; x < width; x++) {
			drawer.setIconPixel(x, 0, getOutlineColor());
			drawer.setIconPixel(x, height-1, getOutlineColor());
		}
		for (int x = 1; x < drawer.getIconRectangleWidth()-1; x++) {
			for (int y = 1; y < drawer.getIconRectangleHeight()-1; y++) {
				drawer.setIconPixel(x, y, fillColor);
			}
		}
	}

	private IconDrawer acquireDrawer() {
		IconService service = getContext().getService(IconService.class);
		return service.acquireDrawer(this);
	}
	
	private String valuesString(ChannelCollection chans) {
		StringBuilder builder = new StringBuilder();
		builder.append("(");
		for (int i = 0; i < chans.getChannelCount(); i++) {
			if (i != 0) builder.append(",");
			String valString;
			if (chans.areInteger())
				valString = String.format("%d", (long)chans.getChannelValue(i));
			else
				valString = String.format("%f", chans.getChannelValue(i));
			builder.append(valString);
		}
		builder.append(")");
		return builder.toString();
	}
	
	private void statusMessage(final String label, ChannelCollection values) {
		StringBuilder builder = new StringBuilder();
		builder.append(label);
		builder.append(" = ");
		builder.append(valuesString(values));
		statusService.showStatus(builder.toString());
	}

	private OptionsChannels getOptions() {
		final OptionsService service =
			getContext().getService(OptionsService.class);

		return service.getOptions(OptionsChannels.class);
	}
}
