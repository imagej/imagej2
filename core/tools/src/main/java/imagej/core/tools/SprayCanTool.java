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

import java.util.Random;

import net.imglib2.meta.Axes;

import imagej.ImageJ;
import imagej.data.ChannelCollection;
import imagej.data.Dataset;
import imagej.data.DrawingTool;
import imagej.data.display.ImageCanvas;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.ext.display.event.input.MsButtonEvent;
import imagej.ext.display.event.input.MsDraggedEvent;
import imagej.ext.display.event.input.MsPressedEvent;
import imagej.ext.display.event.input.MsReleasedEvent;
import imagej.ext.plugin.Plugin;
import imagej.ext.plugin.PluginService;
import imagej.ext.tool.AbstractTool;
import imagej.ext.tool.Tool;
import imagej.options.OptionsService;
import imagej.options.plugins.OptionsChannels;
import imagej.util.IntCoords;
import imagej.util.RealCoords;

/**
 * Implements a spray can drawing tool
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Tool.class, name = "SprayCan", label = "Spray Can",
	description = "Spray Can Tool", iconPath = "/icons/tools/spray-can.png",
	priority = SprayCanTool.PRIORITY)
public class SprayCanTool extends AbstractTool {

	public static final int PRIORITY = -303;

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
			// linear function in rate variable from 4% to 66%
			//double frac = 0.7 * (0.1*(rate) - 0.05);
			// linear function in rate variable from 1% to 63%
			//double frac = (0.07*(rate) - 0.06);
			// linear function in rate variable from 1% to 37%
			double frac = (0.04*(rate) - 0.03);
			// numPixels is fraction of the area of the circle of specified width
			numPixels = (long) (frac * Math.PI * Math.pow(width/2.0,2));
			if (numPixels <= 0) numPixels = 1;
		}
		evt.consume();
	}

	/** On mouse up all resources are freed. */
	@Override
	public void onMouseUp(final MsReleasedEvent evt) {
		if (evt.getButton() != MsButtonEvent.LEFT_BUTTON) return;
		drawingTool = null;
		evt.consume();
	}

	/** On mouse drag sometimes a point is drawn. */
	@Override
	public void onMouseDrag(final MsDraggedEvent evt) {
		if (drawingTool == null) return;
		if (!(evt.getDisplay() instanceof ImageDisplay)) return;
		ImageDisplay disp = (ImageDisplay) evt.getDisplay();
		ImageCanvas canv = disp.getCanvas();
		IntCoords panelCoords = new IntCoords(evt.getX(), evt.getY());
		RealCoords realCoords = canv.panelToImageCoords(panelCoords);
		// begin: adapted from IJ1's SprayCanTool.txt macro courtesy Wayne Rasband
		long ox = realCoords.getLongX();
		long oy = realCoords.getLongY();
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
		// end: adapted from IJ1's SprayCanTool.txt macro courtesy Wayne Rasband
		evt.getDisplay().update();
		evt.consume();
	}

	@Override
	public void configure() {
		final PluginService pluginService =
			getContext().getService(PluginService.class);
		pluginService.run(SprayCanToolConfigPlugin.class, this);
	}

	public void setWidth(int width) { this.width = width; }
	public void setRate(int rate) { this.rate = rate; }
	public void setDotSize(int dotSize) { this.dotSize = dotSize; }
	public int getWidth() { return width; }
	public int getRate() { return rate; }
	public int getDotSize() { return dotSize; }
	

	// -- private helpers --

	/** Allocates and initializes a DrawingTool if possible. */
	private void initDrawingTool(final MsPressedEvent evt) {

		// lookup display info where mouse down event happened
		final ImageJ context = getContext();
		final ImageDisplayService imageDisplayService =
			context.getService(ImageDisplayService.class);
		final ImageDisplay imageDisplay = (ImageDisplay) evt.getDisplay();
		if (imageDisplay == null) return;

		final OptionsService oSrv = context.getService(OptionsService.class);
		OptionsChannels options = oSrv.getOptions(OptionsChannels.class);

		ChannelCollection channels;
		if (evt.getModifiers().isAltDown() || evt.getModifiers().isAltGrDown())
			channels = options.getBgValues();
		else
			channels = options.getFgValues();
		
		// get dataset associated with mouse down event
		final Dataset dataset = imageDisplayService.getActiveDataset(imageDisplay);

		// allocate drawing tool
		drawingTool = new DrawingTool(dataset);
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
		int chanIndex = imageDisplay.getAxisIndex(Axes.CHANNEL);
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
