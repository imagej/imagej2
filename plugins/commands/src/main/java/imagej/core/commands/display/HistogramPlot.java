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

package imagej.core.commands.display;

import imagej.command.Command;
import imagej.command.DynamicCommand;
import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.data.event.DatasetRestructuredEvent;
import imagej.data.event.DatasetUpdatedEvent;
import imagej.data.widget.HistogramBundle;
import imagej.module.MutableModuleItem;
import imagej.ui.UIService;
import imagej.widget.Button;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.histogram.Real1dBinMapper;
import net.imglib2.meta.Axes;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSetIterator;
import net.imglib2.type.numeric.RealType;

import org.scijava.ItemVisibility;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

// TODO
// 1) some button commands unimplemented
// 2) dialog looks ugly
// 3) in general we should make desired plot size part of HistBund and use it in
//    the widget to set preferred size.
// 4) Move this out of Swing commands since it is no longer based on Swing

// TODO Add these features from IJ1
// [++] The horizontal LUT bar below the X-axis is scaled to reflect the display
// range of the image.
// [++] The modal gray value is displayed
//
// TODO This does lots of its own calcs. Rely on the final Histogram
// implementation when it's settled. Grant's impl used Larry's histogram. It
// also had a multithreaded stat calc method.

/**
 * Histogram plotter.
 * 
 * @author Grant Harris
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, menu = {
	@Menu(label = "Analyze"),
	@Menu(label = "Histogram Plot", accelerator = "control shift alt H",
		weight = 0) })
public class HistogramPlot<T extends RealType<T>> extends DynamicCommand
{
	// -- instance variables that are Parameters --

	@Parameter
	private UIService uiService;

	@Parameter
	private ImageDisplayService displayService;

	@Parameter
	private OverlayService overlayService;

	@Parameter
	private ImageDisplay display;

	@Parameter(label = "Histogram", initializer = "initBundle")
	private HistogramBundle bundle;

	@Parameter(visibility = ItemVisibility.MESSAGE)
	private String pixelsStr = "Pixels placeholder";

	@Parameter(visibility = ItemVisibility.MESSAGE)
	private String minStr;

	@Parameter(visibility = ItemVisibility.MESSAGE)
	private String maxStr;

	@Parameter(visibility = ItemVisibility.MESSAGE)
	private String meanStr;

	@Parameter(visibility = ItemVisibility.MESSAGE)
	private String stdDevStr;

	@Parameter(visibility = ItemVisibility.MESSAGE)
	private String binsStr;

	@Parameter(visibility = ItemVisibility.MESSAGE)
	private String binWidthStr;

	@Parameter(label = "Static", callback = "liveClicked")
	private Button liveButton;

	@Parameter(label = "List", callback = "listClicked")
	private Button listButton;

	@Parameter(label = "Copy", callback = "copyClicked")
	private Button copyButton;

	@Parameter(label = "Log", callback = "logClicked")
	private Button logButton;

	@Parameter(label = "Composite", callback = "chanClicked")
	private Button chanButton;

	protected void initBundle() {
		build();
		bundle = new HistogramBundle(histograms[histograms.length - 1], -1, -1);
	}

	protected void liveClicked() {
		liveUpdates = !liveUpdates;
		// TODO - doesn't work
		final MutableModuleItem<Button> item =
			getInfo().getMutableInput("liveButton", Button.class);
		item.setLabel(liveUpdates ? "Live" : "Static");
		if (liveUpdates) liveUpdate(dataset);
	}

	protected void listClicked() {
		// TODO
		// In IJ1 this command copies the histogram into a two column text table
		// as a results table. The 1st column contains calibrated data values and
		// the second column contains counts.
		uiService.showDialog("To be implemented");
	}

	protected void copyClicked() {
		// TODO
		// In IJ1 this command copies the histogram into a two column text table
		// on the clipboard. The 1st column contains calibrated data values and
		// the second column contains counts.
		uiService.showDialog("To be implemented");
	}

	protected void logClicked() {
		// TODO
		// In IJ1 this command displays additional data on the chart. The data
		// series is displayed on a log scale in back and the linear scale in
		// front.
		uiService.showDialog("To be implemented");
	}

	protected void chanClicked() {
		final MutableModuleItem<Button> item =
			getInfo().getMutableInput("liveButton", Button.class);
		int nextHistNum =
			(currHistNum >= histograms.length - 1) ? 0 : currHistNum + 1;
		// TODO - does not work
		if (nextHistNum == histograms.length - 1) {
			item.setLabel("Composite");
		}
		else {
			item.setLabel("Channel " + nextHistNum);
		}
		display(nextHistNum);
	}

	// -- other instance variables --

	private Dataset dataset;
	private long channels;
	private Histogram1d<T>[] histograms;
	private double[] means;
	private double[] stdDevs;
	private double[] mins;
	private double[] maxes;
	private double[] sum1s;
	private double[] sum2s;
	private long sampleCount;
	private double binWidth;
	private double dataMin;
	private double dataMax;
	private long binCount;
	private int currHistNum;
	private boolean liveUpdates = false;

	// -- public interface --

	public void setDisplay(ImageDisplay disp) {
		display = disp;
		dataset = displayService.getActiveDataset(display);
	}
	
	public ImageDisplay getDisplay() {
		return display;
	}

	@Override
	public void run() {
		// build();
		// createDialogResources();
		display(histograms.length - 1);
	}

	// -- EventHandlers --

	@EventHandler
	protected void onEvent(DatasetRestructuredEvent evt) {
		liveUpdate(evt.getObject());
	}

	@EventHandler
	protected void onEvent(DatasetUpdatedEvent evt) {
		liveUpdate(evt.getObject());
	}

	// -- private helpers --

	private void display(int histNumber) {
		int h = histNumber;
		if (h >= histograms.length) h = histograms.length - 1;
		currHistNum = h;
		setTitle(histNumber);
		bundle.setHistogram(histograms[histNumber]);
		setValues(histNumber);
		// TODO - refresh the data panel
	}

	private void setValues(int histNumber) {
		pixelsStr = formatStr("Pixels", sampleCount / channels);
		minStr = formatStr("Min", mins[histNumber]);
		maxStr = formatStr("Max", maxes[histNumber]);
		meanStr = formatStr("Mean", means[histNumber]);
		stdDevStr = formatStr("Std Dev", stdDevs[histNumber]);
		binsStr = formatStr("Bins", binCount);
		binWidthStr = formatStr("Bin Width", binWidth);
	}

	private String formatStr(final String label, final long num)
	{
		return String.format("%12s:%10d", label, num);
	}

	private String formatStr(final String label, final double num)
	{
		return String.format("%12s:%10.2f", label, num);
	}

	private void setTitle(int histNum) {
		String title;
		if (histNum == histograms.length - 1) {
			title = "Composite histogram of ";
		}
		else {
			title = "Channel " + histNum + " histogram of ";
		}
		title += display.getName();
		getInfo().setLabel(title); // TODO - not working???
	}

	private void calcBinInfo() {
		// calc the data ranges - 1st pass thru data
		dataMin = Double.POSITIVE_INFINITY;
		dataMax = Double.NEGATIVE_INFINITY;
		Cursor<? extends RealType<?>> cursor = dataset.getImgPlus().cursor();
		while (cursor.hasNext()) {
			double val = cursor.next().getRealDouble();
			if (val < dataMin) dataMin = val;
			if (val > dataMax) dataMax = val;
		}
		if (dataMin > dataMax) {
			dataMin = 0;
			dataMax = 0;
		}
		double dataRange = dataMax - dataMin;
		if (dataset.isInteger()) {
			dataRange += 1;
			if (dataRange <= 65536) {
				binCount = (long) dataRange;
				binWidth = 1;
			}
			else {
				binCount = 65536;
				binWidth = dataRange / binCount;
			}
		}
		else { // float dataset
			binCount = 1000;
			binWidth = dataRange / binCount;
		}
	}

	// NB : this plugin uses low level access. Histograms are
	// designed to be fed an iterable data source. But in the case of this
	// plugin we do direct computations on the histograms' bins for efficency
	// reasons (so we can calc stats from the same data). Histograms thus have
	// both a high level generic API and a low level nongeneric API.

	private void allocateDataStructures() {
		// initialize data structures
		int chIndex = dataset.getAxisIndex(Axes.CHANNEL);
		channels = (chIndex < 0) ? 1 : dataset.dimension(chIndex);
		histograms = new Histogram1d[(int) channels + 1]; // +1 for chan compos
		Real1dBinMapper<T> mapper =
			new Real1dBinMapper<T>(dataMin, dataMax, binCount, false);
		for (int i = 0; i < histograms.length; i++)
			histograms[i] = new Histogram1d<T>(mapper);
		means = new double[histograms.length];
		stdDevs = new double[histograms.length];
		sum1s = new double[histograms.length];
		sum2s = new double[histograms.length];
		mins = new double[histograms.length];
		maxes = new double[histograms.length];
		for (int i = 0; i < histograms.length; i++) {
			mins[i] = Double.POSITIVE_INFINITY;
			maxes[i] = Double.NEGATIVE_INFINITY;
		}
	}

	private void computeStats() {
		// calc stats - 2nd pass thru data
		int chIndex = dataset.getAxisIndex(Axes.CHANNEL);
		int composH = histograms.length - 1;
		RandomAccess<? extends RealType<?>> accessor =
			dataset.getImgPlus().randomAccess();
		long[] span = dataset.getDims();
		if (chIndex >= 0) span[chIndex] = 1; // iterate channels elsewhere
		HyperVolumePointSet pixelSpace = new HyperVolumePointSet(span);
		PointSetIterator pixelSpaceIter = pixelSpace.iterator();
		sampleCount = 0;
		while (pixelSpaceIter.hasNext()) {
			long[] pos = pixelSpaceIter.next();
			accessor.setPosition(pos);
			// count values by channel. also determine composite pixel value (by
			// channel averaging)
			double composVal = 0;
			for (long chan = 0; chan < channels; chan++) {
				if (chIndex >= 0) accessor.setPosition(chan, chIndex);
				double val = accessor.get().getRealDouble();
				composVal += val;
				long index = (long) ((val - dataMin) / binWidth);
				// NB in float case the max data point overflows the index range
				if (index >= binCount) index = binCount - 1;
				int c = (int) chan;
				histograms[c].increment(index);
				sum1s[c] += val;
				sum2s[c] += val * val;
				if (val < mins[c]) mins[c] = val;
				if (val > maxes[c]) maxes[c] = val;
				sampleCount++;
			}
			composVal /= channels;
			long index = (long) ((composVal - dataMin) / binWidth);
			// NB in float case the max data point overflows the index range
			if (index >= binCount) index = binCount - 1;
			histograms[composH].increment(index);
			sum1s[composH] += composVal;
			sum2s[composH] += composVal * composVal;
			if (composVal < mins[composH]) mins[composH] = composVal;
			if (composVal > maxes[composH]) maxes[composH] = composVal;
		}
		// calc means etc.
		long pixels = sampleCount / channels;
		for (int i = 0; i < histograms.length; i++) {
			means[i] = sum1s[i] / pixels;
			stdDevs[i] =
				Math.sqrt((sum2s[i] - ((sum1s[i] * sum1s[i]) / pixels)) / (pixels - 1));
		}
	}

	private void build() {
		dataset = displayService.getActiveDataset(display);
		calcBinInfo();
		allocateDataStructures();
		computeStats();
		// Maybe?
		// setValues(currHistNum);
	}

	private void liveUpdate(Dataset ds) {
		if (!liveUpdates) return;
		if (ds != dataset) return;
		build();
		display(currHistNum);
	}

	/*

	// TODO - avoid this structure. Use a measurement engine and whiteboard
	public class Statistics {

		public long[] histogram;
		public double mean;
		public double stdDev;
		public double min;
		public double max;
	}

	// TODO : maybe have a precomputed data range rather than iterate here?
	// Also maybe compute from a List<PointSet>? Probably no. Maybe we pass in
	// a channel index and make a hist for overall and each chan in pointset
	// region? This would involve one axis of differentiation but irregular
	// regions might not need that info. Anyhow this allows one pass to find all
	// the various channel histograms at once.
	// What about Functions rather than Displays? How would whiteboard record
	// info about the function such that you could map from display or dataset to
	// a function (for later lookup of stats associated with a display)?

	private Statistics computeStats(ImageDisplay disp, PointSet region, int bins)
	{
		Dataset ds = displayService.getActiveDataset(disp);
		RandomAccess<? extends RealType<?>> accessor =
			ds.getImgPlus().randomAccess();
		PointSetIterator pixelSpaceIter = region.iterator();
		double dmin = Double.POSITIVE_INFINITY;
		double dmax = Double.NEGATIVE_INFINITY;
		long values = 0;
		while (pixelSpaceIter.hasNext()) {
			long[] pos = pixelSpaceIter.next();
			accessor.setPosition(pos);
			double val = accessor.get().getRealDouble();
			if (val < dmin) dmin = val;
			if (val > dmax) dmax = val;
			values++;
		}
		long[] histogram = new long[bins];
		double sum1 = 0;
		double sum2 = 0;
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		while (pixelSpaceIter.hasNext()) {
			long[] pos = pixelSpaceIter.next();
			accessor.setPosition(pos);
			double val = accessor.get().getRealDouble();
			int index = (int) Math.round(bins * (val - dmin) / (dmax - dmin));
			// NB in float case the max data point overflows the index range
			if (index >= bins) index = bins - 1;
			histogram[index]++;
			sum1 += val;
			sum2 += val * val;
			if (val < min) min = val;
			if (val > max) max = val;
		}
		double mean = sum1 / values;
		double stdDev =
			(values < 2) ? 0 : Math.sqrt((sum2 - ((sum1 * sum1) / values)) /
				(values - 1));
		Statistics stats = new Statistics();
		stats.histogram = histogram;
		stats.mean = mean;
		stats.stdDev = stdDev;
		stats.min = min;
		stats.max = max;
		return stats;
	}

	*/
}
