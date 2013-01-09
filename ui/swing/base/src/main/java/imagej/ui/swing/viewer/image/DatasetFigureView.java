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

package imagej.ui.swing.viewer.image;

import imagej.AbstractContextual;
import imagej.data.Dataset;
import imagej.data.display.DatasetView;
import imagej.data.display.event.DataViewUpdatedEvent;
import imagej.event.EventHandler;
import imagej.log.LogService;
import imagej.util.awt.AWTImageTools;

import java.awt.Image;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.ImageFigure;

/**
 * A figure view that links an ImageJ {@link DatasetView} to a JHotDraw
 * {@link ImageFigure}.
 * 
 * @author Curtis Rueden
 * @author Lee Kamentsky
 */
public class DatasetFigureView extends AbstractContextual implements FigureView
{

	private final DatasetView datasetView;
	private final ImageFigure figure;

	public DatasetFigureView(final SwingImageDisplayViewer displayViewer,
		final DatasetView datasetView)
	{
		setContext(datasetView.getContext());
		this.datasetView = datasetView;
		final JHotDrawImageCanvas canvas = displayViewer.getCanvas();
		final Drawing drawing = canvas.getDrawing();
		figure = new ImageFigure();
		figure.setSelectable(false);
		figure.setTransformable(false);
		final Dataset dataset = datasetView.getData();
		final double minX = dataset.getImgPlus().realMin(0);
		final double minY = dataset.getImgPlus().realMin(1);
		final double maxX = dataset.getImgPlus().realMax(0);
		final double maxY = dataset.getImgPlus().realMax(1);
		figure.setBounds(new Point2D.Double(minX, minY), new Point2D.Double(maxX,
			maxY));
		drawing.add(figure);
	}

	@EventHandler
	protected void onDataViewUpdatedEvent(final DataViewUpdatedEvent event) {
		if (event.getView() == datasetView) update();
	}

	@Override
	public void update() {
		final LogService log =
			datasetView.getData().getContext().getService(LogService.class);
		log.debug("Updating image figure: " + this);
		final Image image = datasetView.getScreenImage().image();
		final BufferedImage bufImage = AWTImageTools.makeBuffered(image);
		figure.setBounds(new Rectangle2D.Double(0, 0, bufImage.getWidth(),
			bufImage.getHeight()));
		figure.setBufferedImage(bufImage);
	}

	// -- FigureView methods --

	@Override
	public ImageFigure getFigure() {
		return figure;
	}

	@Override
	public DatasetView getDataView() {
		return datasetView;
	}

	@Override
	public void dispose() {
		getFigure().requestRemove();
	}

}
