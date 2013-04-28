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

package imagej.ui.swing.tools.overlay;

import imagej.data.display.ImageDisplay;
import imagej.data.overlay.GeneralPathOverlay;
import imagej.data.overlay.Overlay;
import imagej.data.view.OverlayView;
import imagej.tool.Tool;
import imagej.ui.swing.overlay.AbstractJHotDrawAdapter;
import imagej.ui.swing.overlay.JHotDrawAdapter;
import imagej.ui.swing.overlay.JHotDrawTool;
import imagej.ui.swing.tools.SwingPolygonTool;

import java.awt.Shape;

import net.imglib2.roi.GeneralPathRegionOfInterest;

import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.Figure;
import org.scijava.plugin.Plugin;

/**
 * JHotDraw adapter for composite area selections.
 * 
 * @author Johannes Schindelin
 */
@Plugin(type = JHotDrawAdapter.class, priority = GeneralPathJHotDrawAdapter.PRIORITY)
public class GeneralPathJHotDrawAdapter extends
	AbstractJHotDrawAdapter<GeneralPathOverlay, GeneralPathFigure>
{

	public static final double PRIORITY = SwingPolygonTool.PRIORITY + 0.5;

	private static GeneralPathOverlay downcastOverlay(final Overlay overlay) {
		assert overlay instanceof GeneralPathOverlay;
		return (GeneralPathOverlay) overlay;
	}

	// -- JHotDrawAdapter methods --

	@Override
	public boolean supports(final Tool tool) {
		return false;
	}

	@Override
	public boolean supports(final Overlay overlay, final Figure figure) {
		if (!(overlay instanceof GeneralPathOverlay)) return false;
		return figure == null || figure instanceof GeneralPathFigure;
	}

	@Override
	public Overlay createNewOverlay() {
		final GeneralPathOverlay o = new GeneralPathOverlay(getContext());
		return o;
	}

	@Override
	public Figure createDefaultFigure() {
		final GeneralPathFigure figure =
			new GeneralPathFigure(new PolygonFigure());
		initDefaultSettings(figure);
		figure.set(AttributeKeys.WINDING_RULE, AttributeKeys.WindingRule.EVEN_ODD);
		return figure;
	}

	@Override
	public void updateOverlay(final GeneralPathFigure figure,
		final OverlayView view)
	{
		super.updateOverlay(figure, view);
		final GeneralPathOverlay overlay = downcastOverlay(view.getData());
		final GeneralPathRegionOfInterest roi = overlay.getRegionOfInterest();
		roi.reset();
		BezierPathFunctions.addToRegionOfInterest(figure.getGeneralPath()
			.getPathIterator(null), roi);
		overlay.update();
	}

	@Override
	public void updateFigure(final OverlayView view,
		final GeneralPathFigure figure)
	{
		super.updateFigure(view, figure);
		final GeneralPathOverlay overlay = downcastOverlay(view.getData());
		final GeneralPathRegionOfInterest roi = overlay.getRegionOfInterest();
		figure.setGeneralPath(roi.getGeneralPath());
	}

	@Override
	public JHotDrawTool getCreationTool(final ImageDisplay display) {
		throw new UnsupportedOperationException(); // new IJBezierTool(display,
																								// this);
	}

	@Override
	public Shape toShape(final GeneralPathFigure figure) {
		return figure.getGeneralPath();
	}

}
