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

package imagej.ui.swing.commands;

import imagej.command.Command;
import imagej.command.ContextCommand;
import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.meta.Axes;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSetIterator;
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
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

//
// TODO
// + Selection of axes to include in histogram computation
//
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
public class HistogramPlot extends ContextCommand implements ActionListener {

	// -- constants --

	private static final String ACTION_LIVE = "LIVE";
	private static final String ACTION_LOG = "LOG";
	private static final String ACTION_COPY = "COPY";
	private static final String ACTION_LIST = "LIST";
	private static final String ACTION_CHANNEL = "CHANNEL";

	// -- instance variables that are Parameters --

	@Parameter
	private ImageDisplayService displayService;

	@Parameter
	private OverlayService overlayService;

	@Parameter
	private ImageDisplay display;

	// -- other instance variables --

	private Dataset dataset;
	private long channels;
	private long[][] histograms;
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
	private int binCount;
	private JFrame frame;
	private JPanel embellPanel;
	private JPanel chartPanel;
	private JButton listButton;
	private JButton copyButton;
	private JButton liveButton;
	private JButton logButton;
	private JButton chanButton;
	private int currHistNum;

	// -- public interface --

	public void setDisplay(ImageDisplay disp) {
		display = disp;
	}
	
	public ImageDisplay getDisplay() {
		return display;
	}

	@Override
	public void run() {
		if (!inputOkay()) return;
		calcBinInfo(); // 1st pass through data
		allocateDataStructures();
		computeStats(); // 2nd pass through data
		currHistNum = histograms.length - 1;
		// create and display window
		createDialogResources();
		display(currHistNum);
	}

	/* OLD - replace with new stuff when histogram branch code merged in imglib

	public static <T extends RealType<T>> int[] computeHistogram(final Img<T> im,
		final T min, final T max, final int bins)
	{
		final HistogramBinMapper<T> mapper = new RealBinMapper<T>(min, max, bins);
		final Histogram<T> histogram = new Histogram<T>(mapper, im);
		histogram.process();
		final int[] d = new int[histogram.getNumBins()];
		for (int j = 0; j < histogram.getNumBins(); j++) {
			d[j] = histogram.getBin(j);
		}
		return d;
	}

	*/

	/**
	 * Returns a JFreeChart containing data from the provided histogram.
	 */
	public static JFreeChart getChart(String title, long[] histogram) {
		final XYSeries series = new XYSeries("histo");
		for (int i = 0; i < histogram.length; i++) {
			series.add(i, histogram[i]);
		}
		final XYSeriesCollection data = new XYSeriesCollection(series);
		final JFreeChart chart =
			ChartFactory.createXYBarChart(title, null, false, null, data,
				PlotOrientation.VERTICAL, false, true, false);
		setTheme(chart);
		// chart.getXYPlot().setForegroundAlpha(0.50f);
		return chart;
	}


	@Override
	public void actionPerformed(ActionEvent evt) {
		String command = evt.getActionCommand();
		if (ACTION_LIVE.equals(command)) {
			// TODO
			Toolkit.getDefaultToolkit().beep();
		}
		if (ACTION_LOG.equals(command)) {
			// TODO
			Toolkit.getDefaultToolkit().beep();
		}
		if (ACTION_COPY.equals(command)) {
			// TODO
			Toolkit.getDefaultToolkit().beep();
		}
		if (ACTION_LIST.equals(command)) {
			// TODO
			Toolkit.getDefaultToolkit().beep();
		}
		if (ACTION_CHANNEL.equals(command)) {
			currHistNum++;
			if (currHistNum >= histograms.length) currHistNum = 0;
			if (currHistNum == histograms.length - 1) {
				chanButton.setText("Composite");
			}
			else {
				chanButton.setText("Channel " + currHistNum);
			}
			display(currHistNum);
		}
	}

	// -- private helpers --

	private boolean inputOkay() {
		dataset = displayService.getActiveDataset(display);
		if (dataset == null) {
			cancel("Input dataset must not be null.");
			return false;
		}
		if (dataset.getImgPlus() == null) {
			cancel("Input Imgplus must not be null.");
			return false;
		}
		return true;
	}

	private void createDialogResources() {
		frame = new JFrame("");
		listButton = new JButton("List");
		listButton.setActionCommand(ACTION_LIST);
		listButton.addActionListener(this);
		copyButton = new JButton("Copy");
		copyButton.setActionCommand(ACTION_COPY);
		copyButton.addActionListener(this);
		logButton = new JButton("Log");
		logButton.setActionCommand(ACTION_LOG);
		logButton.addActionListener(this);
		liveButton = new JButton("Live");
		liveButton.setActionCommand(ACTION_LIVE);
		liveButton.addActionListener(this);
		chanButton = new JButton("Composite");
		chanButton.setActionCommand(ACTION_CHANNEL);
		chanButton.addActionListener(this);
	}

	private void display(int histNumber) {
		setTitle(histNumber);
		Container pane = frame.getContentPane();
		if (chartPanel != null) pane.remove(chartPanel);
		if (embellPanel != null) pane.remove(embellPanel);
		chartPanel = makeChartPanel(histNumber);
		embellPanel = makeEmbellishmentPanel();
		pane.add(chartPanel, BorderLayout.CENTER);
		pane.add(embellPanel, BorderLayout.SOUTH);
		frame.pack();
		frame.setVisible(true);
	}

	private JPanel makeChartPanel(int histNumber) {
		String title = "";
		JFreeChart chart = getChart(title, histograms[histNumber]);
		chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		return chartPanel;
	}

	private JPanel makeEmbellishmentPanel() {
		JPanel valuesPanel = makeValuePanel();
		final JPanel horzPanel = new JPanel();
		horzPanel.setLayout(new BoxLayout(horzPanel, BoxLayout.X_AXIS));
		horzPanel.add(listButton);
		horzPanel.add(copyButton);
		horzPanel.add(logButton);
		horzPanel.add(liveButton);
		horzPanel.add(chanButton);
		final JPanel vertPanel = new JPanel();
		vertPanel.setLayout(new BoxLayout(vertPanel, BoxLayout.Y_AXIS));
		vertPanel.add(valuesPanel);
		vertPanel.add(horzPanel);
		return vertPanel;
	}

	private JPanel makeValuePanel() {
		JPanel valuesPanel = new JPanel();
		final JTextArea text = new JTextArea();
		valuesPanel.add(text, BorderLayout.CENTER);
		final StringBuilder sb = new StringBuilder();
		addStr(sb, "Pixels", sampleCount);
		sb.append("\n");
		addStr(sb, "Min", mins[currHistNum]);
		sb.append("   ");
		addStr(sb, "Max", maxes[currHistNum]);
		sb.append("\n");
		addStr(sb, "Mean", means[currHistNum]);
		sb.append("   ");
		addStr(sb, "StdDev", stdDevs[currHistNum]);
		sb.append("\n");
		addStr(sb, "Bins", binCount);
		sb.append("   ");
		addStr(sb, "Bin Width", binWidth);
		sb.append("\n");
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

	private void setTitle(int histNum) {
		String title;
		if (histNum == histograms.length - 1) {
			title = "Composite histogram of ";
		}
		else {
			title = "Channel " + histNum + " histogram of ";
		}
		title += display.getName();
		frame.setTitle(title);
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
		chart.getTitle().setPaint(Color.black);
	}

	private void calcBinInfo() {
		// calc the data ranges - 1st pass thru data
		dataMin = Double.POSITIVE_INFINITY;
		dataMax = Double.NEGATIVE_INFINITY;
		Cursor<? extends RealType<?>> cursor = dataset.getImgPlus().cursor();
		while (cursor.hasNext()) {
			double val = cursor.next().getRealDouble();
			dataMin = Math.min(dataMin, val);
			dataMax = Math.max(dataMax, val);
		}
		if (dataMin > dataMax) {
			dataMin = 0;
			dataMax = 0;
		}
		double dataRange = dataMax - dataMin;
		if (dataset.isInteger()) dataRange += 1;
		if (dataRange <= 256 && dataset.isInteger()) {
			binCount = (int) dataRange;
			binWidth = 1;
		}
		else {
			binCount = 256;
			binWidth = dataRange / binCount;
		}
	}

	private void allocateDataStructures() {
		// initialize data structures
		int chIndex = dataset.getAxisIndex(Axes.CHANNEL);
		channels = (chIndex < 0) ? 1 : dataset.dimension(chIndex);
		histograms = new long[(int) channels + 1][]; // add one for chan compos
		for (int i = 0; i < histograms.length; i++)
			histograms[i] = new long[binCount];
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
				int index = (int) ((val - dataMin) / binWidth);
				int c = (int) chan;
				histograms[c][index]++;
				sum1s[c] += val;
				sum2s[c] += val * val;
				mins[c] = Math.min(mins[c], val);
				maxes[c] = Math.max(maxes[c], val);
				sampleCount++;
			}
			composVal /= channels;
			int index = (int) ((composVal - dataMin) / binWidth);
			histograms[composH][index]++;
			sum1s[composH] += composVal;
			sum2s[composH] += composVal * composVal;
			mins[composH] = Math.min(mins[composH], composVal);
			maxes[composH] = Math.max(maxes[composH], composVal);
		}
		// calc means etc.
		long pixels = sampleCount / channels;
		for (int i = 0; i < histograms.length; i++) {
			means[i] = sum1s[i] / pixels;
			stdDevs[i] =
				Math.sqrt((sum2s[i] - ((sum1s[i] * sum1s[i]) / pixels)) / (pixels - 1));
		}
	}

}
