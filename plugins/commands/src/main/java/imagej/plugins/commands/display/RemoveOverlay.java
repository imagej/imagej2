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

package imagej.plugins.commands.display;

import imagej.command.Command;
import imagej.command.ContextCommand;
import imagej.data.display.DataView;
import imagej.data.display.ImageDisplay;
import imagej.data.display.OverlayService;
import imagej.data.display.OverlayView;
import imagej.menu.MenuConstants;

import java.util.ArrayList;

import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Removes the currently selected overlay from all displays
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC),
	@Menu(label = "Overlay"),
	@Menu(label = "Remove Overlay", weight = 3) })
public class RemoveOverlay extends ContextCommand {

	// -- Parameters --
	
	@Parameter(required=true)
	private OverlayService oService;

	@Parameter(required=true)
	private ImageDisplay display;
	
	// -- accessors --
	
	public void setImageDisplay(ImageDisplay disp) {
		display = disp;
	}

	public ImageDisplay getImageDisplay() {
		return display;
	}
	
	// -- run() method --
	
	@Override
	public void run() {
		final ArrayList<DataView> views = new ArrayList<DataView>(display);
		for (final DataView view : views) {
			if (view.isSelected()) {
				if (view instanceof OverlayView) {
					OverlayView overlayView = (OverlayView) view;
					oService.removeOverlay(overlayView.getData());
				}
			}
		}
	}

}
