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

package imagej.ui.swing.viewer.image;

import imagej.core.options.OptionsAppearance;
import imagej.data.Dataset;
import imagej.display.Display;
import imagej.options.OptionsService;
import imagej.options.event.OptionsEvent;
import imagej.ui.common.awt.AWTInputEventDispatcher;
import imagej.ui.viewer.DisplayWindow;
import imagej.ui.viewer.image.AbstractImageDisplayViewer;

import org.scijava.event.EventHandler;

/**
 * A Swing image display viewer, which displays 2D planes in grayscale or
 * composite color. Intended to be subclassed by a concrete implementation that
 * provides a {@link DisplayWindow} in which the display should be housed.
 * 
 * @author Curtis Rueden
 * @author Lee Kamentsky
 * @author Grant Harris
 * @author Barry DeZonia
 */
public abstract class AbstractSwingImageDisplayViewer extends
	AbstractImageDisplayViewer implements SwingImageDisplayViewer
{

	protected AWTInputEventDispatcher dispatcher;

	private JHotDrawImageCanvas imgCanvas;
	private SwingDisplayPanel imgPanel;

	// -- SwingImageDisplayViewer methods --

	@Override
	public JHotDrawImageCanvas getCanvas() {
		return imgCanvas;
	}

	// -- DisplayViewer methods --

	@Override
	public void view(final DisplayWindow w, final Display<?> d) {
		super.view(w, d);

		dispatcher = new AWTInputEventDispatcher(getDisplay(), getEventService());

		// broadcast input events (keyboard and mouse)
		imgCanvas = new JHotDrawImageCanvas(this);
		imgCanvas.addEventDispatcher(dispatcher);

		imgPanel = new SwingDisplayPanel(this, getWindow());
		setPanel(imgPanel);

		updateTitle();
	}

	@Override
	public SwingDisplayPanel getPanel() {
		return imgPanel;
	}

	@Override
	public Dataset capture() {
		return getCanvas().capture();
	}

	// -- AbstractImageDisplayViewer methods --

	// CTR TODO - This logic is not Swing-specific and should be factored up.

	@Override
	protected ZoomScaleOption getZoomScaleOption() {
		final OptionsService optionsService =
			getContext().getService(OptionsService.class);
		return optionsService.getOptions(OptionsAppearance.class)
			.isDisplayFractionalScales() ? ZoomScaleOption.OPTIONS_FRACTIONAL_SCALE
			: ZoomScaleOption.OPTIONS_PERCENT_SCALE;
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(@SuppressWarnings("unused") final OptionsEvent e) {
		updateLabel();
	}

}
