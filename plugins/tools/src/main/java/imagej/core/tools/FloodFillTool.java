/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

import imagej.command.CommandService;
import imagej.data.ChannelCollection;
import imagej.data.Dataset;
import imagej.data.DrawingTool;
import imagej.data.FloodFiller;
import imagej.data.display.ImageDisplay;
import imagej.data.event.DatasetUpdatedEvent;
import imagej.data.options.OptionsChannels;
import imagej.display.event.input.MsButtonEvent;
import imagej.display.event.input.MsClickedEvent;
import imagej.options.OptionsService;
import imagej.render.RenderingService;
import imagej.tool.AbstractTool;
import imagej.tool.Tool;

import org.scijava.event.EventService;
import org.scijava.plugin.Plugin;

/**
 * Tool implementation for flood fill.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Tool.class, name = "FloodFill", label = "Flood Fill",
	description = "Flood Fill Tool", iconPath = "/icons/tools/flood-fill.png",
	priority = FloodFillTool.PRIORITY)
public class FloodFillTool extends AbstractTool {

	// -- instance variables --

	public static final double PRIORITY = -304;

	enum Connectivity {
		EIGHT, FOUR
	}

	private Connectivity connectivity = Connectivity.EIGHT;

	// -- public interface --

	/** Specify whether this flood fill operation should be 4 or 8 connected. */
	public void setConnectivity(final Connectivity c) {
		connectivity = c;
	}

	/** Gets this flood fill's current connectivity (4 or 8 connected). */
	public Connectivity getConnectivity() {
		return connectivity;
	}

	/** Implements the configuration of this tool. */
	@Override
	public void configure() {
		final CommandService commandService =
			getContext().getService(CommandService.class);
		commandService.run(FloodFillToolConfig.class, "tool", this);
	}

	/** Run flood fill when mouse clicked */
	@Override
	public void onMouseClick(final MsClickedEvent evt) {
		if (evt.getButton() == MsButtonEvent.LEFT_BUTTON) {
			final ImageDisplay imageDisplay = (ImageDisplay) evt.getDisplay();
			if (imageDisplay != null) {
				final PixelRecorder recorder = new PixelRecorder(false);
				if (recorder.record(evt)) {
					final DrawingTool drawingTool =
							initDrawingTool(
								recorder.wasAltKeyDown(), recorder.getDataset(),
								evt.getContext().getService(RenderingService.class));
					final long[] currPos = getCurrPosition(imageDisplay);
					floodFill(recorder.getCX(), recorder.getCY(), currPos, connectivity, drawingTool);
					Dataset dataset = drawingTool.getDataset();
					EventService srv = getContext().getService(EventService.class);
					if (srv != null) srv.publish(new DatasetUpdatedEvent(dataset, false));
				}
				recorder.releaseDataset();
			}
			evt.consume();
		}
	}

	// -- private helpers --

	/** Returns an initialized DrawingTool. */
	private DrawingTool initDrawingTool(boolean altKeyDown, final Dataset ds,
		RenderingService renderingService)
	{
		final OptionsChannels opts = getChannelOptions();
		ChannelCollection fillValues;
		if (altKeyDown)
			fillValues = opts.getBgValues();
		else
			fillValues = opts.getFgValues();
		final DrawingTool tool = new DrawingTool(ds, renderingService);
		tool.setChannels(fillValues);
		// TODO - change here to support arbitrary UV axes
		tool.setUAxis(0);
		tool.setVAxis(1);
		tool.setLineWidth(1);
		return tool;
	}

	/** Returns the current position shown in the associated ImageDisplay. */
	private long[] getCurrPosition(final ImageDisplay imageDisplay) {
		// set the position of tool to current display's position
		// FIXME - this will break when the view axes are different than the
		// dataset's axes. this could happen from a display that combines multiple
		// datasets. Or perhaps a display that ignores some axes from a dataset.
		final long[] currPos = new long[imageDisplay.numDimensions()];
		for (int i = 0; i < currPos.length; i++)
			currPos[i] = imageDisplay.getLongPosition(i);
		return currPos;
	}

	/** Returns an OptionsColor instance */
	private OptionsChannels getChannelOptions() {
		final OptionsService oSrv = getContext().getService(OptionsService.class);
		return oSrv.getOptions(OptionsChannels.class);
	}

	/** Actually does the flood fill. */
	private void floodFill(final long u, final long v, final long[] position,
		final Connectivity c, final DrawingTool dTool)
	{
		dTool.setPosition(position);
		final FloodFiller filler = new FloodFiller(dTool);
		if (c == Connectivity.FOUR) filler.fill4(u, v, position);
		else filler.fill8(u, v, position);
	}

}
