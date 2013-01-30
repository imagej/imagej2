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

package imagej.ui.swing.tools.overlay;

import java.awt.Shape;
import java.util.LinkedList;
import java.util.List;

import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.overlay.Overlay;
import imagej.data.overlay.ThresholdOverlay;
import imagej.plugin.Plugin;
import imagej.ui.swing.overlay.AbstractJHotDrawAdapter;
import imagej.ui.swing.overlay.IJCreationTool;
import imagej.ui.swing.overlay.JHotDrawAdapter;
import imagej.ui.swing.overlay.JHotDrawTool;
import imagej.ui.swing.overlay.SwingThresholdFigure;

import net.imglib2.img.ImgPlus;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.jhotdraw.draw.Figure;

/**
 * Swing/JHotDraw implementation of threshold tool.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = JHotDrawAdapter.class, name = "Threshold",
	description = "Threshold overlay",
	priority = 1000)
public class SwingThresholdTool extends
	AbstractJHotDrawAdapter<ThresholdOverlay, SwingThresholdFigure>
{
	private static List<Mapping> mappings = new LinkedList<Mapping>();
	
	private class Mapping {
		ImgPlus<? extends RealType<?>> imgPlus;
		ThresholdOverlay overlay;
		SwingThresholdFigure figure;
	}
	
	@Override
	public boolean supports(Overlay overlay, Figure figure) {
		if (!(overlay instanceof ThresholdOverlay)) return false;
		return figure == null || figure instanceof SwingThresholdFigure;
	}

	@Override
	public Overlay createNewOverlay() {
		ImgPlus<? extends RealType<?>> imgPlus = getImgPlus();
		for (Mapping m : mappings) {
			if (m.imgPlus == imgPlus) return m.overlay;
		}
		Mapping m = map(imgPlus);
		return m.overlay;
	}

	@Override
	public Figure createDefaultFigure() {
		ImgPlus<? extends RealType<?>> imgPlus = getImgPlus();
		for (Mapping m : mappings) {
			if (m.imgPlus == imgPlus) return m.figure;
		}
		Mapping m = map(imgPlus);
		return m.figure;
	}

	@Override
	public JHotDrawTool getCreationTool(ImageDisplay display) {
		return new IJCreationTool<SwingThresholdFigure>(display, this);
	}

	@Override
	public Shape toShape(SwingThresholdFigure figure) {
		throw new UnsupportedOperationException("Unimplemented");
	}

	private ImgPlus<? extends RealType<?>> getImgPlus() {
		ImageDisplayService service = getContext().getService(ImageDisplayService.class);
		Dataset ds = service.getActiveDataset();
		if (ds != null) return ds.getImgPlus();
		// make a phantom dataset so we can always have an imgplus for this tool
		DatasetService dss = getContext().getService(DatasetService.class);
		ds = dss.create(new UnsignedByteType(), new long[]{1,1}, "Fred", new AxisType[]{Axes.X, Axes.Y});
		return ds.getImgPlus();
	}
	
	private Mapping map(ImgPlus<? extends RealType<?>> imgPlus) {
		Mapping m = new Mapping();
		m.imgPlus = imgPlus;
		m.overlay = new ThresholdOverlay(getContext(), imgPlus);
		m.figure = new SwingThresholdFigure(imgPlus, m.overlay.getPoints());
		initDefaultSettings(m.figure);
		mappings.add(m);
		return m;
	}
}
