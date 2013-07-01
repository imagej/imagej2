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

package imagej.ui.swing.widget;

import imagej.data.widget.HistogramBundle;
import imagej.data.widget.HistogramWidget;
import imagej.widget.InputWidget;
import imagej.widget.WidgetModel;

import java.awt.BasicStroke;
import java.awt.Color;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.scijava.plugin.Plugin;

/**
 * @author Barry DeZonia
 */
@Plugin(type = InputWidget.class)
public class SwingHistogramWidget extends SwingInputWidget<HistogramBundle>
	implements HistogramWidget<JPanel>
{

	private HistogramBundle bundle;
	private ChartPanel chartPanel;

	@Override
	public HistogramBundle getValue() {
		return bundle;
	}

	@Override
	public void refreshWidget() {
		if (bundle.hasChanges()) {
			bundle.changesNoted();
			ChartPanel newChartPanel = makeChartPanel(bundle);
			JFreeChart chart = newChartPanel.getChart();
			chartPanel.setChart(chart);
		}
	}

	@Override
	public void set(final WidgetModel model) {
		super.set(model);
		bundle = (HistogramBundle) model.getValue();
		chartPanel = makeChartPanel(bundle);
		bundle.changesNoted();
		getComponent().add(chartPanel);
	}

	@Override
	public boolean supports(WidgetModel model) {
		return model.isType(HistogramBundle.class);
	}

	private ChartPanel makeChartPanel(HistogramBundle b) {
		JFreeChart chart = getChart(null, b);
		ChartPanel panel = new ChartPanel(chart);
		panel.setPreferredSize(new java.awt.Dimension(300, 150));
		return panel;
	}

	/**
	 * Returns a JFreeChart containing data from the provided histogram.
	 */
	private JFreeChart getChart(String title, HistogramBundle bund) {
		// TODO - draw min/max lines from bundle
		final XYSeries series = new XYSeries("histo");
		long total = bund.getHistogram().getBinCount();
		for (long i = 0; i < total; i++) {
			series.add(i, bund.getHistogram().frequency(i));
		}
		return createChart(title, series);
	}

	private JFreeChart createChart(String title, XYSeries series) {
		final XYSeriesCollection data = new XYSeriesCollection(series);
		final JFreeChart chart =
			ChartFactory.createXYBarChart(title, null, false, null, data,
				PlotOrientation.VERTICAL, false, true, false);
		setTheme(chart);
		// chart.getXYPlot().setForegroundAlpha(0.50f);
		return chart;
	}

	private final void setTheme(final JFreeChart chart) {
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

	private final void setBackgroundDefault(final JFreeChart chart) {
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
		TextTitle title = chart.getTitle();
		if (title != null) title.setPaint(Color.black);
	}
}
