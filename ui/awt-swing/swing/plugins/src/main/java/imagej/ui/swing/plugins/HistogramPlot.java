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

package imagej.ui.swing.plugins;

import imagej.data.Dataset;
import imagej.data.display.DatasetView;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.ui.DialogPrompt;
import imagej.ui.UIService;
import imagej.ui.UserInterface;
import imagej.util.RealRect;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import net.imglib2.algorithm.stats.Histogram;
import net.imglib2.algorithm.stats.HistogramBinMapper;
import net.imglib2.algorithm.stats.RealBinMapper;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

// Only operates on IntegerTypes
//
// TODO
// + Selection of axes to include in histogram computation
//
// TODO Add these features from IJ1
// [++] The horizontal LUT bar below the X-axis is scaled to reflect the display range of the image.
// [++] The total pixel Count is also calculated and displayed, as well as the 
// Mean, standard deviation (StdDev), minimum (Min), maximum (Max) and modal (Mode) gray value.
//
// This relies on Larry's Histogram...

/**
 * Histogram plotter.
 * 
 * @author Grant Harris
 */
@Plugin(menu = {
	@Menu(label = "Analyze"),
	@Menu(label = "Histogram Plot", accelerator = "control shift alt H",
		weight = 0) })
public class HistogramPlot implements ImageJPlugin {

//	@Parameter(label = "Value (binary)")
//	private long value;

	@Parameter(persist = false)
	private DatasetView view;

	// -- instance variables that are Parameters --

	@Parameter(persist = false)
	private ImageDisplayService displayService;

	@Parameter(persist = false)
	private OverlayService overlayService;

	@Parameter(persist = false)
	private UIService uiService;

	@Parameter(persist = false)
	private ImageDisplay display;

	private Dataset dataset;
	private RealRect bounds;

	// -- other instance variables --

	private Dataset input;

	int[] histogram;
	double histMin;
	double histMax;
	double binWidth;
	int pixels;

	double min;
	double max;
	private final int BINS = 256;
	private final boolean showBins = true;

	// -- public interface --

	@Override
	public void run() {
		if (!inputOkay()) {
			informUser();
			return;
		}
		dataset = displayService.getActiveDataset(display);
		bounds = overlayService.getSelectionBounds(display);
		// HistogramComputer histoComputer = new HistogramComputer(display, dataset,
		// bounds);
//		HistogramComputer histoComputer = new HistogramComputer(display, dataset, bounds, 0, 4095);
//		int[] histogram = histoComputer.get();
		final StatisticsComputer statComputer =
			new StatisticsComputer(dataset.getImgPlus());
		histMin = dataset.getType().getMinValue();
		histMax = dataset.getType().getMaxValue();
		statComputer.setHistogramBinsMinMax(BINS, histMin, histMax);
		binWidth = (histMax - histMin) / (BINS - 1);
		//
		statComputer.process();
		//
		histogram = statComputer.getHistogram();
		pixels = countPixels(histogram);
		min = statComputer.getMin().getRealDouble();
		max = statComputer.getMax().getRealDouble();
		//
		asChart(histogram, true);
	}

	public static <T extends RealType<T>> int[] computeHistogram(final Img<T> im,
		final T min, final T max, final int bins)
	{
		final HistogramBinMapper<T> mapper = new RealBinMapper<T>(min, max, bins);
		final Histogram<T> histogram = new Histogram<T>(mapper, im);
		histogram.process();
		final int[] d = new int[histogram.getNumBins()];
		for (int j = 0; j < histogram.getNumBins(); j++) {
			d[j] = histogram.getBin(j);
			//System.out.println(d[j]);
		}
		return d;
	}

	/**
	 * Returns the JFreeChart with this histogram, and as a side effect, show it
	 * in a JFrame that provides the means to edit the dimensions and also the
	 * plot properties via a popup menu.
	 */
	public JFreeChart asChart(final int[] d, final boolean show) {
		final XYSeries series = new XYSeries("histo");
		for (int i = 0; i < d.length; i++) {
			series.add(i, d[i]);
		}
		final String title = "Histogram: " + display.getName();
		final XYSeriesCollection data = new XYSeriesCollection(series);
		// data.addSeries(series2);
		final JFreeChart chart =
			ChartFactory.createXYBarChart(title, null, false, null, data,
				PlotOrientation.VERTICAL, false, true, false);

		// ++ chart.getTitle().setFont(null);
		setTheme(chart);
		chart.getXYPlot().setForegroundAlpha(0.50f);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		if (show) {
			final JFrame frame = new JFrame(title);
			frame.getContentPane().add(chartPanel, BorderLayout.CENTER);
			final JPanel valuesPanel = makeValuePanel();
			frame.add(valuesPanel, BorderLayout.SOUTH);
			frame.pack();
			frame.setVisible(true);
		}
		return chart;
	}

	public JFreeChart asChart(final int[] d) {
		return asChart(d, false);
	}

	// -- private interface --

	private int countPixels(final int[] histogram) {
		int sum = 0;
		for (final int v : histogram) {
			sum += v;
		}
		return sum;
	}

	private boolean inputOkay() {
		input = displayService.getActiveDataset(display);
		if (input == null) {
			return false;
		}
		if (input.getImgPlus() == null) {
			return false;
		}
		return input.isInteger() && !input.isRGBMerged();
	}

	private void informUser() {
		final UserInterface ui = uiService.getUI();
		final DialogPrompt dialog =
			ui.dialogPrompt("This plugin requires an integral dataset",
				"Unsupported image type", DialogPrompt.MessageType.INFORMATION_MESSAGE,
				DialogPrompt.OptionType.DEFAULT_OPTION);
		dialog.prompt();
	}

	private JPanel makeValuePanel() {
		final JPanel valuesPanel = new JPanel();
		final JTextArea text = new JTextArea();

		valuesPanel.add(text, BorderLayout.CENTER);
		final StringBuilder sb = new StringBuilder();
		addStr(sb, "Pixels", pixels);
		sb.append("\n");
		addStr(sb, "Min", min);
		sb.append("   ");
		addStr(sb, "Max", max);
		sb.append("\n");
		addStr(sb, "hMin", histMin);
		sb.append("   ");
		addStr(sb, "hMax", histMax);
		sb.append("\n");
		if (showBins) {
			addStr(sb, "Bins", BINS);
			sb.append("   ");
			addStr(sb, "BinWidth", binWidth);
			sb.append("\n");
		}
//		ip.drawString("Count: " + count, col1, row1);

//		ip.drawString("Mean: " + d2s(stats.mean), col1, row2);
//		ip.drawString("StdDev: " + d2s(stats.stdDev), col1, row3);
//		ip.drawString("Mode: " + d2s(stats.dmode) + " (" + stats.maxCount + ")", col2, row3);

//			ip.drawString("Bins: " + d2s(stats.nBins), col1, row4);
//			ip.drawString("Bin Width: " + d2s(binWidth), col2, row4);
//		}
		// valuesPanel.setPreferredSize(new Dimension(200,32));
		text.setFont(new Font("Monospaced", Font.PLAIN, 12));
		text.setText(sb.toString());
		return valuesPanel;
	}

	private void
		addStr(final StringBuilder sb, final String label, final int num)
	{
		sb.append(String.format("%10s:", label));
		sb.append(String.format("%8d", num));
	}

	private void addStr(final StringBuilder sb, final String label,
		final double num)
	{
		sb.append(String.format("%10s:", label));
		sb.append(String.format("%8.2f", num));
	}

	private static final void setTheme(final JFreeChart chart) {
		final XYPlot plot = (XYPlot) chart.getPlot();
		final XYBarRenderer r = (XYBarRenderer) plot.getRenderer();
		final StandardXYBarPainter bp = new StandardXYBarPainter();
		r.setBarPainter(bp);
		r.setSeriesOutlinePaint(0, Color.lightGray);
		r.setShadowVisible(false);
		r.setDrawBarOutline(false);
		setBackgroundDefault(chart);
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();

		// rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setTickLabelsVisible(false);
		rangeAxis.setTickMarksVisible(false);
		final NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		domainAxis.setTickLabelsVisible(false);
		domainAxis.setTickMarksVisible(false);
	}

	private static final void setBackgroundDefault(final JFreeChart chart) {
		final BasicStroke gridStroke =
			new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
				1.0f, new float[] { 2.0f, 1.0f }, 0.0f);
		final XYPlot plot = (XYPlot) chart.getPlot();
		plot.setRangeGridlineStroke(gridStroke);
		plot.setDomainGridlineStroke(gridStroke);
		plot.setBackgroundPaint(new Color(235, 235, 235));
		plot.setRangeGridlinePaint(Color.white);
		plot.setDomainGridlinePaint(Color.white);
		plot.setOutlineVisible(false);
		plot.getDomainAxis().setAxisLineVisible(false);
		plot.getRangeAxis().setAxisLineVisible(false);
		plot.getDomainAxis().setLabelPaint(Color.gray);
		plot.getRangeAxis().setLabelPaint(Color.gray);
		plot.getDomainAxis().setTickLabelPaint(Color.gray);
		plot.getRangeAxis().setTickLabelPaint(Color.gray);
		chart.getTitle().setPaint(Color.gray);
	}

}
