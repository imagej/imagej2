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

package imagej.plugins.uis.swing.mdi.viewer;

import imagej.display.Display;
import imagej.plugins.uis.swing.mdi.SwingMdiUI;
import imagej.plugins.uis.swing.viewer.image.AbstractSwingImageDisplayViewer;
import imagej.plugins.uis.swing.viewer.image.SwingImageDisplayViewer;
import imagej.ui.UserInterface;
import imagej.ui.viewer.DisplayViewer;
import imagej.ui.viewer.DisplayWindow;

import javax.swing.JInternalFrame;

import org.scijava.plugin.Plugin;

/**
 * Multiple Document Interface implementation of Swing image display viewer. The
 * MDI display is housed in a {@link JInternalFrame}.
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 * @author Lee Kamentsky
 * @see SwingImageDisplayViewer
 */
@Plugin(type = DisplayViewer.class)
public class SwingMdiImageDisplayViewer extends AbstractSwingImageDisplayViewer
{

	// -- DisplayViewer methods --

	@Override
	public boolean isCompatible(final UserInterface ui) {
		return ui instanceof SwingMdiUI;
	}

	@Override
	public void view(final DisplayWindow w, final Display<?> d) {
		super.view(w, d);
		getPanel().addEventDispatcher(dispatcher);
	}

}
