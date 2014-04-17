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

package imagej.plugins.commands.display;

import imagej.command.Command;
import imagej.command.ContextCommand;
import imagej.data.ChannelCollection;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.DrawingTool;
import imagej.data.display.ColorTables;
import imagej.data.display.DatasetView;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.menu.MenuConstants;
import imagej.render.RenderingService;
import imagej.render.TextRenderer.TextJustification;

import java.util.List;

import net.imglib2.display.ColorTable;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;

import org.scijava.ItemIO;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.ColorRGB;
import org.scijava.util.Colors;

// TODO
// this implementation does not support a "List" button which shows
// the LUT in tabular form when clicked

// TODO ARG
// Displays ColorTable16 LUTs as only 256 colors.

/**
 * This class adapted from legacy ImageJ's LutViewer class.
 * 
 * @author Barry DeZonia
 * @author Wayne Rasband
 */
@Plugin(type = Command.class, menu = {
		@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
			mnemonic = MenuConstants.IMAGE_MNEMONIC),
		@Menu(label = "Color"),
		@Menu(label = "Show LUT", weight = 12) })
public class ShowLUT extends ContextCommand {

	// -- Parameters --
	
	@Parameter
	private ImageDisplayService imgDispService;

	@Parameter
	private DisplayService displayService;
	
	@Parameter
	private DatasetService datasetService;
	
	@Parameter
	private RenderingService renderingService;
	
	@Parameter
	private ImageDisplay display;
	
	@Parameter(type = ItemIO.OUTPUT)
	private Display<?> output;
	
	// -- public interface --
	
	@Override
	public void run() {
		DatasetView view = imgDispService.getActiveDatasetView(display);
		List<ColorTable> colorTables = view.getColorTables();
		int currChannel = display.getIntPosition(Axes.CHANNEL);
		ColorTable colorTable = colorTables.get(currChannel);
		Dataset ds = createDataset(colorTable);
		output = displayService.createDisplay(ds);
		// TODO
		// output.addButton("List", ShowLUTAsTable.class);
	}
	
	public void setDisplay(ImageDisplay disp) {
		display = disp;
	}
	
	public ImageDisplay getDisplay() {
		return display;
	}

	public Display<?> getOutput() {
		return output;
	}

	// -- private helpers --
	
	private Dataset createDataset(ColorTable lut) {
		long[] dims = new long[]{326,188,3};
		String name = "Look-Up Table";
		AxisType[] axes = new AxisType[]{Axes.X, Axes.Y,Axes.CHANNEL};
		int bitsPerPixel = 8;
		boolean signed = false;
		boolean floating = false;
		Dataset ds = datasetService.create(dims, name, axes, bitsPerPixel, signed, floating);
		ds.setRGBMerged(true);
		drawLUTInfo(ds, lut);
		return ds;
	}
	
	private void drawLUTInfo(Dataset ds, ColorTable ct) {
		DrawingTool tool = new DrawingTool(ds, renderingService);
		int xMargin = 35;
		int yMargin = 20;
		int width = 256;
		int height = 128;
		int barHeight = 12;
		boolean isGray = ColorTables.isGrayColorTable(ct);
		int mapSize = ct.getLength();
		int x, y, x1, y1, x2, y2;
		
		int imageWidth = width + 2*xMargin;
		int imageHeight = height + 3*yMargin;

		tool.setChannels(new ChannelCollection(Colors.WHITE));
		tool.fillRect(0, 0, imageWidth, imageHeight);
		tool.setChannels(new ChannelCollection(Colors.BLACK));
		tool.drawRect(xMargin, yMargin, width, height);

		double scale = 256.0/mapSize;
		if (!isGray) tool.setChannels(new ChannelCollection(Colors.RED));
		x1 = xMargin;
		y1 = yMargin + height - ct.getResampled(ColorTable.RED, 256, 0)/2;
		for (int i = 1; i<256; i++) {
			x2 = xMargin + i;
			y2 = yMargin + height - ct.getResampled(ColorTable.RED, 256, (int)(i/scale))/2;
			tool.drawLine(x1, y1, x2, y2);
			x1 = x2;
			y1 = y2;
		}

		if (!isGray) {
			tool.setChannels(new ChannelCollection(Colors.LIGHTGREEN));
			x1 = xMargin;
			y1 = yMargin + height - ct.getResampled(ColorTable.GREEN, 256, 0)/2;
			for (int i = 1; i<256; i++) {
				x2 = xMargin + i;
				y2 = yMargin + height - ct.getResampled(ColorTable.GREEN, 256, (int)(i/scale))/2;
				tool.drawLine(x1, y1, x2, y2);
				x1 = x2;
				y1 = y2;
			}
		}

		if (!isGray) {
			tool.setChannels(new ChannelCollection(Colors.BLUE));
			x1 = xMargin;
			y1 = yMargin + height - ct.getResampled(ColorTable.BLUE, 256, 0)/2;
			for (int i = 1; i<255; i++) {
				x2 = xMargin + i;
				y2 = yMargin + height - ct.getResampled(ColorTable.BLUE, 256, (int)(i/scale))/2;
				tool.drawLine(x1, y1, x2, y2);
				x1 = x2;
				y1 = y2;
			}
		}

		x = xMargin;
		y = yMargin + height + 2;
		
		drawColorBar(tool, ct, x, y, 256, barHeight);
		
		y += barHeight + 15;
		tool.setChannels(new ChannelCollection(Colors.BLACK));
		tool.drawText(x - 4, y, "0", TextJustification.LEFT);
		tool.drawText(x + width - 10, y, ""+(mapSize-1), TextJustification.LEFT);
		tool.drawText(7, yMargin + 4, "255", TextJustification.LEFT);
	}
	
	private void drawColorBar(DrawingTool tool, ColorTable ct, int x, int y, int width, int height) {
		double scale = 256.0 / ct.getLength();
		for (int i = 0; i<256; i++) {
			int index = (int)(i/scale);
			int r = ct.getResampled(ColorTable.RED, 256, index);
			int g = ct.getResampled(ColorTable.GREEN, 256, index);
			int b = ct.getResampled(ColorTable.BLUE, 256, index);
			ColorRGB color = new ColorRGB(r,g,b);
			tool.setChannels(new ChannelCollection(color));
			tool.moveTo(x+i,y);
			tool.lineTo(x+i,y+height);
		}
		tool.setChannels(new ChannelCollection(Colors.BLACK));
		tool.drawRect(x, y, width, height);
	}


}
