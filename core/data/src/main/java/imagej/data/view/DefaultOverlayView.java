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

package imagej.data.view;

import imagej.data.Data;
import imagej.data.display.ImageDisplay;
import imagej.data.overlay.Overlay;

import org.scijava.plugin.Plugin;

/**
 * A view into an {@link Overlay}, for use with a {@link ImageDisplay}.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = DataView.class)
public class DefaultOverlayView extends AbstractDataView implements
	OverlayView
{

	// -- DataView methods --

	@Override
	public boolean isCompatible(final Data data) {
		return data != null && Overlay.class.isAssignableFrom(data.getClass());
	}

	@Override
	public Overlay getData() {
		return (Overlay) super.getData();
	}

	@Override
	public int getPreferredWidth() {
		// NB: No need to report preferred overlay width.
		return 0;
	}

	@Override
	public int getPreferredHeight() {
		// NB: No need to report preferred overlay height.
		return 0;
	}

	@Override
	public void update() {
		// NB: No action needed.
	}

	@Override
	public void rebuild() {
		// NB: No action needed.
	}

}
