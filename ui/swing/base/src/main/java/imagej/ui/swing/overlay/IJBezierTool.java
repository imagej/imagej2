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

import imagej.data.display.ImageDisplay;

import org.jhotdraw.draw.BezierFigure;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.tool.BezierTool;

/**
 * A JHotDraw {@link BezierTool} for ImageJ's Swing UI.
 * 
 * @author Lee Kamentsky
 * @author Johannes Schindelin
 */
public class IJBezierTool extends BezierTool implements JHotDrawTool {

	private final ImageDisplay display;
	private final JHotDrawAdapter<BezierFigure> adapter;

	public IJBezierTool(final ImageDisplay display,
		final JHotDrawAdapter<BezierFigure> adapter)
	{
		super((BezierFigure) adapter.createDefaultFigure());
		this.display = display;
		this.adapter = adapter;
	}

	// -- BezierTool methods --

	@Override
	protected BezierFigure createFigure() {
		return (BezierFigure) getAdapter().createDefaultFigure();
	}

	@Override
	protected void finishCreation(final BezierFigure figure,
		final DrawingView drawingView)
	{
		super.finishCreation(figure, drawingView);
		final JHotDrawService jHotDrawService =
			getDisplay().getContext().getService(JHotDrawService.class);
		jHotDrawService.linkOverlay(figure, getAdapter(), getDisplay());
	}

	// -- JHotDrawTool methods --

	@Override
	public ImageDisplay getDisplay() {
		return display;
	}

	@Override
	public JHotDrawAdapter<BezierFigure> getAdapter() {
		return adapter;
	}

	@Override
	public boolean isConstructing() {
		return createdFigure != null;
	}

}
