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

import imagej.ImageJPlugin;
import imagej.data.display.ImageDisplay;
import imagej.data.display.OverlayView;
import imagej.data.overlay.Overlay;
import imagej.display.Display;
import imagej.tool.Tool;

import org.jhotdraw.draw.Figure;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.RichPlugin;
import org.scijava.util.RealCoords;

/**
 * Interface for JHotDraw-based adapters, which maintain a bidirectional link
 * between an ImageJ {@link Overlay} and a JHotDraw {@link Figure}.
 * <p>
 * JHotDraw adapters discoverable at runtime must implement this interface and
 * be annotated with @{@link Plugin} with attribute {@link Plugin#type()} =
 * {@link JHotDrawAdapter}.class. While it possible to create a JHotDraw adapter
 * merely by implementing this interface, it is encouraged to instead extend
 * {@link AbstractJHotDrawAdapter}, for convenience.
 * </p>
 * 
 * @author Lee Kamentsky
 * @author Curtis Rueden
 */
public interface JHotDrawAdapter<F extends Figure> extends ImageJPlugin,
	RichPlugin
{

	/**
	 * Determines whether the adapter is designed to work with the given tool.
	 * 
	 * @param tool The tool in question.
	 * @return True iff the adapter is compatible with the given tool.
	 */
	boolean supports(Tool tool);

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
	 * @param view view of the overlay that needs to be changed to bring it
	 *          in-sync with the figure.
	 */
	void updateOverlay(F figure, OverlayView view);

	/**
	 * Update the appearance of the figure to match the overlay
	 * 
	 * @param view view of the overlay to be represented by the figure
	 * @param figure the figure that is to be made to look like the overlay
	 */
	void updateFigure(OverlayView view, F figure);

	JHotDrawTool getCreationTool(ImageDisplay display);

	void mouseDown(Display<?> d, int x, int y);

	void mouseDrag(Display<?> d, int x, int y);

	void report(RealCoords p1, RealCoords p2);

}
