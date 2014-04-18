/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.plugins.uis.swing.overlay;

import java.awt.event.MouseEvent;

import net.imagej.display.ImageDisplay;

import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.tool.CreationTool;

/**
 * A JHotDraw {@link CreationTool} for ImageJ's Swing UI.
 * 
 * @author Lee Kamentsky
 */
public class IJCreationTool<F extends Figure> extends CreationTool implements JHotDrawTool {

	private final ImageDisplay display;
	private final JHotDrawAdapter<F> adapter;

	public IJCreationTool(final ImageDisplay display,
		final JHotDrawAdapter<F> adapter)
	{
		super(adapter.createDefaultFigure());
		this.display = display;
		this.adapter = adapter;
	}

	// -- CreationTool methods --

	@Override
	protected Figure createFigure() {
		return getAdapter().createDefaultFigure();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void creationFinished(final Figure figure) {
		super.creationFinished(figure);
		final JHotDrawService jHotDrawService =
			getDisplay().getContext().getService(JHotDrawService.class);
		jHotDrawService.linkOverlay((F)figure, getAdapter(), getDisplay());
	}

	// -- JHotDrawTool methods --

	@Override
	public ImageDisplay getDisplay() {
		return display;
	}

	@Override
	public JHotDrawAdapter<F> getAdapter() {
		return adapter;
	}

	@Override
	public boolean isConstructing() {
		return createdFigure != null;
	}

	// -- MouseListener methods --

	@Override
	public void mouseClicked(MouseEvent evt) {
		if (!isLeftClick(evt)) return;
		super.mouseClicked(evt);
	}
	
	@Override
	public void mousePressed(MouseEvent evt) {
		if (!isLeftClick(evt)) return;
		super.mousePressed(evt);
		adapter.mouseDown(getDisplay(), evt.getX(), evt.getY());
	}
	
	@Override
	public void mouseReleased(MouseEvent evt) {
		if (!isLeftClick(evt)) return;
		super.mouseReleased(evt);
	}

	// -- MouseMotionListener methods --

	@Override
	public void mouseDragged(MouseEvent evt) {
		super.mouseDragged(evt);
		adapter.mouseDrag(getDisplay(), evt.getX(), evt.getY());
	}

	// -- Helper methods --

	private boolean isLeftClick(final MouseEvent evt) {
		return evt.getButton() == MouseEvent.BUTTON1;
	}

}
