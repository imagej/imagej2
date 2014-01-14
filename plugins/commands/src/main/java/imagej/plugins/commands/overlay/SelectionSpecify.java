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

package imagej.plugins.commands.overlay;

import imagej.command.Command;
import imagej.command.ContextCommand;
import imagej.data.display.ImageDisplay;
import imagej.data.overlay.EllipseOverlay;
import imagej.data.overlay.Overlay;
import imagej.data.overlay.RectangleOverlay;
import imagej.menu.MenuConstants;

import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

// TODO - this needs to be an interactive plugin. Currently making it a regular
// plugin as a placeholder. Crated because we need our own version callable from
// the menu and within the Overlay Manager.

// TODO - IJ1 has more functionality in it's SpecifyROI plugin. Calibration is
// supported. Interactivity / immediate feedback. Can modify the currently
// selected ROI rather than hatch a new one (which we might not want).

/**
 * Adds a user specified {@link Overlay} to an {@link ImageDisplay}.
 * Specification is by dialog rather than by gui manipulation.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, menu = {
	@Menu(label = MenuConstants.EDIT_LABEL, weight = MenuConstants.EDIT_WEIGHT,
		mnemonic = MenuConstants.EDIT_MNEMONIC),
	@Menu(label = "Selection", mnemonic = 's'),
	@Menu(label = "Specify...", mnemonic = 's') }, headless = true)
public class SelectionSpecify extends ContextCommand {

	// -- Parameters --

	@Parameter
	private Context context;

	@Parameter(type = ItemIO.BOTH)
	private ImageDisplay display;

	@Parameter(label = "X")
	private long px;

	@Parameter(label = "Y")
	private long py;

	@Parameter(label = "Width")
	private long w;

	@Parameter(label = "Height")
	private long h;

	@Parameter(label = "Centered")
	private boolean centered;

	@Parameter(label = "Oval")
	private boolean oval;

	// -- Command methods --

	@Override
	public void run() {
		long x = px;
		long y = py;
		if (! centered) {
			x += w / 2;
			y += h / 2;
		}
		if (oval) {
			final EllipseOverlay overlay = new EllipseOverlay(context);
			overlay.setOrigin(x, 0);
			overlay.setOrigin(y, 1);
			overlay.setRadius(w / 2, 0);
			overlay.setRadius(h / 2, 1);
			display.display(overlay);
		}
		else { // rectangle
			final RectangleOverlay overlay = new RectangleOverlay(context);
			overlay.setOrigin(x - w/2, 0);
			overlay.setOrigin(y - h/2, 1);
			overlay.setExtent(w, 0);
			overlay.setExtent(h, 1);
			display.display(overlay);
		}
		display.update();
	}

	// -- accessors --

	public long getPointX() {
		return px;
	}

	public void setPointX(final long px) {
		this.px = px;
	}

	public long getPointY() {
		return py;
	}

	public void setPointY(final long py) {
		this.py = py;
	}

	public long getWidth() {
		return w;
	}

	public void setWidth(final long w) {
		this.w = w;
	}

	public long getHeight() {
		return h;
	}

	public void setHeight(final long h) {
		this.h = h;
	}

	public boolean isCentered() {
		return centered;
	}

	public void setCentered(final boolean centered) {
		this.centered = centered;
	}

	public boolean isOval() {
		return oval;
	}

	public void setOval(final boolean oval) {
		this.oval = oval;
	}

	public ImageDisplay getDisplay() {
		return display;
	}

	public void setDisplay(final ImageDisplay display) {
		this.display = display;
	}

}
