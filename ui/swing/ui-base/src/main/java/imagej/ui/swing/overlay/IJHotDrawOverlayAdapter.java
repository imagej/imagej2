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

package imagej.ui.swing.overlay;

import imagej.data.display.ImageDisplay;
import imagej.data.display.OverlayView;
import imagej.data.overlay.Overlay;

import org.jhotdraw.draw.Figure;

/**
 * Implement the IJHotDrawOverlayAdapter to create an adapter that lets JHotDraw
 * edit ImageJ ROIs.
 * 
 * @author Lee Kamentsky
 */
public interface IJHotDrawOverlayAdapter {

	/**
	 * @return the priority of this adapter: higher priority adapters will be
	 *         chosen over lower
	 */
	public int getPriority();

	/**
	 * Set the adapter's priority
	 * 
	 * @param priority
	 */
	public void setPriority(int priority);

	/**
	 * Determines whether the adapter can handle a particular overlay, or overlay
	 * / figure combination.
	 * 
	 * @param overlay - an overlay that might be editable
	 * @param figure - a figure that will be either updated by the overlay or will
	 *          update the overlay. The figure can be null: this indicates that
	 *          the adapter is capable of creating the figure associated with the
	 *          overlay/
	 */
	boolean supports(Overlay overlay, Figure figure);

	/**
	 * Creates a new overlay.
	 * 
	 * @return an Overlay of the associated type in the default initial state
	 */
	Overlay createNewOverlay();

	/** Creates a default figure of the type handled by this adapter. */
	Figure createDefaultFigure();

	/**
	 * Update the overlay to match the appearance of the figure
	 * 
	 * @param figure the figure that holds the current correct appearance
	 * @param overlay the overlay that needs to be changed to bring it in-sync
	 *          with the figure.
	 */
	void updateOverlay(Figure figure, OverlayView overlay);

	/**
	 * Update the appearance of the figure to match the overlay
	 * 
	 * @param overlay the overlay to be represented by the figure
	 * @param figure the figure that is to be made to look like the overlay
	 */
	void updateFigure(OverlayView overlay, Figure figure);

	JHotDrawTool getCreationTool(ImageDisplay display,
		OverlayCreatedListener listener);

}
