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

package imagej.core.commands.misc;

import imagej.command.Command;
import imagej.data.Dataset;
import imagej.data.Position;
import imagej.data.display.ColorMode;
import imagej.data.display.DatasetView;
import imagej.data.display.ImageDisplay;
import imagej.data.overlay.ThresholdOverlay;
import imagej.data.threshold.ThresholdService;
import imagej.data.types.DataType;
import imagej.data.types.DataTypeService;
import imagej.menu.MenuConstants;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.meta.Axes;

import org.scijava.ItemIO;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC),
	@Menu(label = "Show Info...", accelerator = "control I") },
	headless = true)
public class ShowInfo implements Command {

	// -- Parameters --

	@Parameter
	private DataTypeService typeService;

	@Parameter
	private ThresholdService thresholdService;

	@Parameter
	private ImageDisplay disp;

	@Parameter
	private Dataset ds;

	@Parameter(type = ItemIO.OUTPUT)
	String info;

	// -- Command methods --

	@Override
	public void run() {
		info = infoString();
	}

	// -- helpers --

	private String infoString() {
		List<String> strings = strings();
		StringBuilder builder = new StringBuilder();
		for (String s : strings) {
			builder.append(s);
			builder.append('\n');
		}
		return builder.toString();
	}

	private List<String> strings() {
		ArrayList<String> strings = new ArrayList<String>();
		String s;
		s = textString();
		if (s != null) strings.add(s);
		s = titleString();
		if (s != null) strings.add(s);
		s = widthString();
		if (s != null) strings.add(s);
		s = heightString();
		if (s != null) strings.add(s);
		s = depthString();
		if (s != null) strings.add(s);
		s = resolutionString();
		if (s != null) strings.add(s);
		s = pixelVoxelSizeString();
		if (s != null) strings.add(s);
		s = typeString();
		if (s != null) strings.add(s);
		s = displayRangesString();
		if (s != null) strings.add(s);
		s = currSliceString();
		if (s != null) strings.add(s);
		s = compositeString();
		if (s != null) strings.add(s);
		s = thresholdString();
		if (s != null) strings.add(s);
		s = calibrationString();
		if (s != null) strings.add(s);
		s = sourceString();
		if (s != null) strings.add(s);
		s = selectionString();
		if (s != null) strings.add(s);
		return strings;
	}

	private String textString() {
		// TODO - if metadata includes a textual description then use it here
		return null;
	}

	private String titleString() {
		return "Title: " + ds.getName();
	}

	private String widthString() {
		double cal = ds.axis(0).calibration();
		if (Double.isNaN(cal) || cal == 1) {
			return "Width: " + ds.dimension(0);
		}
		return "Width: " + (cal * ds.dimension(0)) + " (" + ds.dimension(0) + ")";
	}

	private String heightString() {
		double cal = ds.axis(1).calibration();
		if (Double.isNaN(cal) || cal == 1) {
			return "Height: " + ds.dimension(1);
		}
		return "Height: " + (cal * ds.dimension(1)) + " (" + ds.dimension(1) + ")";
	}

	private String depthString() {
		int zIndex = ds.dimensionIndex(Axes.Z);
		if (zIndex < 0) return null;
		double cal = ds.axis(zIndex).calibration();
		if (Double.isNaN(cal) || cal == 1) {
			return "Depth: " + ds.dimension(zIndex);
		}
		return "Depth: " + (cal * ds.dimension(zIndex)) + " (" +
			ds.dimension(zIndex) + ")";
	}

	private String resolutionString() {
		// TODO - can't finish until unit library in place
		// In IJ1 you might see something like "Resolution:  3.175 pixels per µm"
		return null;
	}

	private String pixelVoxelSizeString() {
		int zIndex = ds.dimensionIndex(Axes.Z);
		double xCal = ds.calibration(0);
		double yCal = ds.calibration(1);
		double zCal = (zIndex < 0) ? Double.NaN : ds.calibration(zIndex);
		double xSize = (Double.isNaN(xCal) ? 1 : xCal);
		double ySize = (Double.isNaN(yCal) ? 1 : yCal);
		double zSize = (Double.isNaN(zCal) ? 1 : zCal);
		if (zIndex < 0) { // no z axis
			return "Pixel size: " + xSize + " x " + ySize;
		}
		// z axis present
		return "Voxel size: " + xSize + " x " + ySize + " x " + zSize;
	}

	private String typeString() {
		Object dataObj = ds.getImgPlus().firstElement();
		DataType<?> type = typeService.getTypeByClass(dataObj.getClass());
		if (type == null) return "Type: unknown";
		return "Type: " + type.longName();
	}

	private String displayRangesString() {
		int chAxis = disp.dimensionIndex(Axes.CHANNEL);
		if (chAxis < 0) return null;
		long numChan = disp.dimension(chAxis);
		String tmp = "";
		for (int c = 0; c < numChan; c++) {
			// TODO: dislike this casting
			double min = ((DatasetView) disp.getActiveView()).getChannelMin(c);
			double max = ((DatasetView) disp.getActiveView()).getChannelMax(c);
			tmp += "Display range channel " + c + ": " + min + "-" + max;
			if (c != numChan - 1) tmp += '\n';
		}
		return tmp;
	}

	private String currSliceString() {
		Position position = disp.getActiveView().getPlanePosition();
		if (position.numDimensions() == 0) return null;
		String tmp = "";
		for (int i = 0; i < position.numDimensions(); i++) {
			long dim = disp.dimension(i + 2);
			long pos = position.getLongPosition(i) + 1;
			String label = disp.axis(i + 2).type().toString();
			tmp += "View position " + label + ": " + pos + "/" + dim;
			if (i != position.numDimensions() - 1) tmp += '\n';
		}
		return tmp;
	}

	private String compositeString() {
		// TODO: dislike this casting
		ColorMode mode = ((DatasetView) disp.getActiveView()).getColorMode();
		return "Composite mode: " + mode;
	}

	private String thresholdString() {
		if (thresholdService.hasThreshold(disp)) {
			ThresholdOverlay thresh = thresholdService.getThreshold(disp);
			return "Threshold: " + thresh.getRangeMin() + "-" + thresh.getRangeMax();
		}
		return "Threshold: none";
	}

	private String calibrationString() {
		// TODO
		// In IJ1 this shows the calibration function equation if it has been set.
		// IJ2's axes don't yet support such a concept.
		return null;
	}

	private String sourceString() {
		return "Source: " + ds.getSource();
	}

	private String selectionString() {
		// TODO
		// In IJ1 you might see
		// "Rectangle x =, y=, w=, h= on separate lines
		return null;
	}
}
