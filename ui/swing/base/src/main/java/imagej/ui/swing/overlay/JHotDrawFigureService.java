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

package imagej.ui.swing.overlay;

import imagej.Priority;
import imagej.data.display.ImageDisplay;
import imagej.data.overlay.FigureService;
import imagej.data.overlay.ThresholdOverlay;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.service.AbstractService;
import imagej.service.Service;
import imagej.ui.UIService;
import imagej.ui.swing.viewer.image.SwingImageDisplayViewer;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.numeric.RealType;


/**
 * JHotDraw implementation of the FigureService. This service is responsible for
 * creating JHotDraw Figures for given ThresholdOverlays.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Service.class, priority = Priority.NORMAL_PRIORITY)
public class JHotDrawFigureService extends AbstractService implements
	FigureService
{
	// TODO - I think this can all go into ThresholdService and FigureService can
	// be eliminated entirely. But first make code complete (i.e. overlay
	// selectable in view, overlay showing up in overlay manager, interacting with
	// other overlays correctly, etc.) before we try to eliminate classes. These
	// capabilities might require the relocation of this method to another project

	@Parameter
	private UIService uiService;

	@Override
	public void createFigure(ImageDisplay display,
		ImgPlus<? extends RealType<?>> imgPlus, ThresholdOverlay overlay)
	{
		SwingThresholdFigure figure =
			new SwingThresholdFigure(display, imgPlus, overlay);
		SwingImageDisplayViewer viewer =
			(SwingImageDisplayViewer) uiService.getDisplayViewer(display);
		viewer.getCanvas().getDrawing().add(figure);
		overlay.setFigure(figure);
	}

	@Override
	public void deleteFigure(ImageDisplay display, ThresholdOverlay overlay) {
		SwingThresholdFigure figure = (SwingThresholdFigure) overlay.getFigure();
		SwingImageDisplayViewer viewer =
			(SwingImageDisplayViewer) uiService.getDisplayViewer(display);
		viewer.getCanvas().getDrawing().remove(figure);
		overlay.setFigure(null);
	}
}
