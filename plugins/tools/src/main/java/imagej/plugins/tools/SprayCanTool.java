/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.plugins.tools;

import imagej.command.CommandService;
import imagej.data.ChannelCollection;
import imagej.data.Dataset;
import imagej.data.DrawingTool;
import imagej.data.display.ImageCanvas;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.event.DatasetUpdatedEvent;
import imagej.data.options.OptionsChannels;
import imagej.display.event.input.MsButtonEvent;
import imagej.display.event.input.MsDraggedEvent;
import imagej.display.event.input.MsEvent;
import imagej.display.event.input.MsPressedEvent;
import imagej.display.event.input.MsReleasedEvent;
import imagej.options.OptionsService;
import imagej.render.RenderingService;
import imagej.tool.AbstractTool;
import imagej.tool.Tool;

import java.util.Random;

import net.imglib2.meta.Axes;

import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.IntCoords;
import org.scijava.util.RealCoords;

/**
 * Implements a spray can drawing tool
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Tool.class, name = "SprayCan", label = "Spray Can",
	description = "Spray Can Tool", iconPath = "/icons/tools/spray-can.png",
	priority = SprayCanTool.PRIORITY)
public class SprayCanTool extends AbstractTool {

	public static final double PRIORITY = -303;

	@Parameter
	private CommandService commandService;

	@Parameter
	private OptionsService optionsService;

	@Parameter
	private ImageDisplayService imageDisplayService;

	@Parameter
	private RenderingService renderingService;

	@Parameter(required = false)
	private EventService eventService;

	private DrawingTool drawingTool;
	private int width=100, rate=6, dotSize=1;
	private long numPixels = 1;
	private Random rng = new Random();
	
	/** On mouse down the delay counters are reset. */
	@Override
	public void onMouseDown(final MsPressedEvent evt) {
		if (evt.getButton() != MsButtonEvent.LEFT_BUTTON) return;
		if (!(evt.getDisplay() instanceof ImageDisplay)) return;
		initDrawingTool(evt);
		if (drawingTool != null) {
			numPixels = calcPixelCount();
			doOneSpray(evt);
		}
		evt.consume();
	}

	/** On mouse up all resources are freed. */
	@Override
	public void onMouseUp(final MsReleasedEvent evt) {
		if (evt.getButton() != MsButtonEvent.LEFT_BUTTON) return;
		if (drawingTool != null) {
			Dataset dataset = drawingTool.getDataset();
			if (eventService != null) {
				eventService.publish(new DatasetUpdatedEvent(dataset, false));
			}
			drawingTool = null;
		}
		evt.consume();
	}

	/** On mouse drag sometimes a point is drawn. */
	@Override
	public void onMouseDrag(final MsDraggedEvent evt) {
		if (drawingTool == null) return;
		doOneSpray(evt);
		evt.consume();
	}

	@Override
	public void configure() {
		commandService.run(SprayCanToolConfig.class, true, new Object[] {"tool", this}); // FIXME
	}

	public void setWidth(int width) { this.width = width; }
	public void setRate(int rate) { this.rate = rate; }
	public void setDotSize(int dotSize) { this.dotSize = dotSize; }
	public int getWidth() { return width; }
	public int getRate() { return rate; }
	public int getDotSize() { return dotSize; }
	

	// -- private helpers --

	private double calcFraction() {
		// NB - formula arrived at by trying IJ1's version at each of the 10 rate
		// settings and counting the pixels drawn in a radius 50 circle. Then used
		// those values to fit a curve. This is not exactly like IJ1 but works
		// better than previous linear interpolation schemes. 
		return (13 * Math.pow(2.02,rate-1)) / 7854.0;
	}
	
	private long calcPixelCount() {
		double fraction = calcFraction();
		// numPixels is fraction of the area of the circle of specified width
		long count = (long) (fraction * Math.PI * Math.pow(width/2.0,2));
		if (count <= 0) return 1;
		return count;
	}
	
	private void doOneSpray(MsEvent evt) {
		if (!(evt.getDisplay() instanceof ImageDisplay)) return;
		ImageDisplay disp = (ImageDisplay) evt.getDisplay();
		ImageCanvas canv = disp.getCanvas();
		IntCoords panelCoords = new IntCoords(evt.getX(), evt.getY());
		RealCoords realCoords = canv.panelToDataCoords(panelCoords);
		drawPixels(realCoords.getLongX(), realCoords.getLongY());
		evt.getDisplay().update();
	}
	
	// NB: adapted from IJ1's SprayCanTool.txt macro courtesy Wayne Rasband
	private void drawPixels(long ox, long oy) {
		double radius = width / 2.0;
		double radius2 = radius * radius;
		for (int i = 0; i < numPixels; i++) {
			long dx, dy;
			do {
	      dx = (long) ((rng.nextDouble()-0.5)*width);
	      dy = (long) ((rng.nextDouble()-0.5)*width);
			} while (dx*dx + dy*dy > radius2);
	    drawingTool.drawDot(ox + dx, oy + dy);
		}
	}
	
	/** Allocates and initializes a DrawingTool if possible. */
	private void initDrawingTool(final MsPressedEvent evt) {

		// lookup display info where mouse down event happened
		final ImageDisplay imageDisplay = (ImageDisplay) evt.getDisplay();
		if (imageDisplay == null) return;

		OptionsChannels options = optionsService.getOptions(OptionsChannels.class);

		ChannelCollection channels;
		if (evt.getModifiers().isAltDown() || evt.getModifiers().isAltGrDown())
			channels = options.getBgValues();
		else
			channels = options.getFgValues();
		
		// get dataset associated with mouse down event
		final Dataset dataset = imageDisplayService.getActiveDataset(imageDisplay);

		// allocate drawing tool
		drawingTool = new DrawingTool(dataset, renderingService);
		drawingTool.setChannels(channels);

		// set the position of tool to current display's position
		// FIXME - this will break when the view axes are different than the
		// dataset's axes. this could happen from a display that combines multiple
		// datasets. Or perhaps a display that ignores some axes from a dataset.
		final long[] currPos = new long[imageDisplay.numDimensions()];
		for (int i = 0; i < currPos.length; i++)
			currPos[i] = imageDisplay.getLongPosition(i);
		drawingTool.setPosition(currPos);

		// restrict to a single channel if a multichannel image
		int chanIndex = imageDisplay.dimensionIndex(Axes.CHANNEL);
		if (chanIndex >= 0) {
			long currChanPos = currPos[chanIndex];
			drawingTool.setPreferredChannel(currChanPos);
		}

		// define the UV drawing axes that the tool will use
		// TODO - change here to make this work on any two arbitrary axes
		drawingTool.setUAxis(0); // U will be axis 0 (which by convention is X)
		drawingTool.setVAxis(1); // V will be axis 1 (which by convention is Y)

		// set the size used to draw dots
		drawingTool.setLineWidth(getDotSize());
	}

}
