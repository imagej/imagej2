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
import imagej.data.display.ImageDisplayService;
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
	private ImageDisplayService imageDisplayService;

	@Parameter
	private ImageDisplay disp;

	@Parameter
	private Dataset ds;

	@Parameter(type = ItemIO.OUTPUT)
	private String info;

	// -- ShowInfo methods --
	
	public String getInfo() {
		return info;
	}

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
		s = originString();
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
		return "Title: " + ds.getName() + '\n';
	}

	private String widthString() {
		return dimString(0, "Width: ");
	}

	private String heightString() {
		return dimString(1, "Height: ");
	}

	private String depthString() {
		int zIndex = ds.dimensionIndex(Axes.Z);
		if (zIndex < 0) return null;
		return dimString(zIndex, "Depth: ");
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
			return "Pixel size: " + dToS(xSize) + " x " + dToS(ySize) + '\n';
		}
		// z axis present
		return "Voxel size: " + dToS(xSize) + " x " + dToS(ySize) + " x " +
			dToS(zSize) + '\n';
	}

	private String originString() {
		// TODO
		// In IJ1 the origin of the calibrated space is reported. IJ2 does not yet
		// support a nonzero origin.
		return null;
	}

	private String typeString() {
		Object dataObj = ds.getImgPlus().firstElement();
		DataType<?> type = typeService.getTypeByClass(dataObj.getClass());
		if (type == null) return "Type: unknown" + '\n';
		return "Type: " + type.longName() + '\n';
	}

	private String displayRangesString() {
		int chAxis = disp.dimensionIndex(Axes.CHANNEL);
		if (chAxis < 0) return null;
		long numChan = disp.dimension(chAxis);
		final StringBuilder builder = new StringBuilder();
		for (int c = 0; c < numChan; c++) {
			final DatasetView datasetView =
				imageDisplayService.getActiveDatasetView(disp);
			final double min = datasetView.getChannelMin(c);
			final double max = datasetView.getChannelMax(c);
			builder.append("Display range channel " + c + ": " + min + "-" + max +
				"\n");
		}
		return builder.toString();
	}

	private String currSliceString() {
		Position position = disp.getActiveView().getPlanePosition();
		if (position.numDimensions() == 0) return null;
		String tmp = "";
		for (int i = 0; i < position.numDimensions(); i++) {
			long dim = disp.dimension(i + 2);
			long pos = position.getLongPosition(i) + 1;
			String label = disp.axis(i + 2).type().toString();
			tmp += "View position " + label + ": " + pos + "/" + dim + '\n';
		}
		return tmp;
	}

	private String compositeString() {
		// TODO: dislike this casting
		ColorMode mode = ((DatasetView) disp.getActiveView()).getColorMode();
		return "Composite mode: " + mode + '\n';
	}

	private String thresholdString() {
		if (thresholdService.hasThreshold(disp)) {
			ThresholdOverlay thresh = thresholdService.getThreshold(disp);
			return "Threshold: " + thresh.getRangeMin() + "-" + thresh.getRangeMax() +
				'\n';
		}
		return "Threshold: none" + '\n';
	}

	private String calibrationString() {
		// TODO
		// In IJ1 this shows the calibration function equation if it has been set.
		// IJ2's axes don't yet support such a concept.
		return null;
	}

	private String sourceString() {
		String source = ds.getSource();
		if (source == null) return null;
		return "Source: " + source + '\n';
	}

	private String selectionString() {
		// TODO
		// In IJ1 you might see
		// "Rectangle x =, y=, w=, h= on separate lines
		return null;
	}

	private String dimString(int axisIndex, String label) {
		double cal = ds.axis(axisIndex).calibration();
		long size = ds.dimension(axisIndex);
		String unit = ds.unit(axisIndex);
		String tmp = label;
		if (Double.isNaN(cal) || cal == 1) {
			tmp += size;
			if (unit != null) tmp += " " + unit;
		}
		else {
			tmp += dToS(cal * size);
			if (unit != null) tmp += " " + unit;
			tmp += " (" + size + ")";
		}
		tmp += "\n";
		return tmp;
	}

	private String dToS(double num) {
		return String.format("%1.4f", num);
	}
}
