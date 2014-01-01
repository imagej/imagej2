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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.plugins.commands.overlay;

import imagej.command.Command;
import imagej.data.Data;
import imagej.data.display.DataView;
import imagej.data.display.ImageDisplay;
import imagej.data.overlay.Overlay;
import imagej.menu.MenuConstants;

import java.util.ArrayList;
import java.util.List;

import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * A plugin to change the properties (e.g., line color, line width) of a set of
 * overlays selected in an image display.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, label = "Overlay Properties...", menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC),
	@Menu(label = "Overlay", mnemonic = 'o'),
	@Menu(label = "Properties...", mnemonic = 'p', weight = 5) },
		headless = true,
		initializer = "initialize")
public class OverlayProperties extends AbstractOverlayProperties {

	// -- parameters --

	@Parameter
	private ImageDisplay display;

	// -- initializers --

	@SuppressWarnings("unused")
	private void initialize() {
		setOverlays(getSelectedOverlays());
		updateValues();
	}

	// -- helpers --
	
	private List<Overlay> getSelectedOverlays() {
		final ArrayList<Overlay> result = new ArrayList<Overlay>();
		if (display == null) return result;
		for (final DataView view : display) {
			if (!view.isSelected()) continue;
			final Data data = view.getData();
			if (!(data instanceof Overlay)) continue;
			final Overlay o = (Overlay) data;
			result.add(o);
		}
		return result;
	}

}
