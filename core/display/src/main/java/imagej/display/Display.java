//
// Display.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.display;

import imagej.data.Dataset;
import imagej.data.roi.Overlay;
import imagej.plugin.BasePlugin;

import java.util.List;

import net.imglib2.EuclideanSpace;
import net.imglib2.meta.LabeledAxes;
import net.imglib2.meta.Named;

/**
 * A display is a special kind of ImageJ plugin for visualizing data.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
public interface Display extends BasePlugin, Named, LabeledAxes, EuclideanSpace {

	/**
	 * Tests whether the display is capable of visualizing the given
	 * {@link Dataset}.
	 */
	boolean canDisplay(Dataset dataset);

	/**
	 * Displays the given {@link Dataset} in this display. Typically, this is a
	 * shortcut for calling
	 * <code>addView(new DatasetView(display, dataset))</code>.
	 */
	void display(Dataset dataset);

	/**
	 * Displays the given {@link Overlay} in this display. Typically, this is a
	 * shortcut for calling
	 * <code>addView(new OverlayView(display, overlay))</code>.
	 */
	void display(Overlay overlay);

	/** Updates and redraws the display onscreen. */
	void update();

	/**
	 * Adds a view to this display.
	 * 
	 * @see DisplayView
	 */
	void addView(DisplayView view);

	/** Removes a view from this display. */
	void removeView(DisplayView view);

	/** Removes all views from this display. */
	void removeAllViews();

	/** Gets a list of views linked to the display. */
	List<DisplayView> getViews();

	/** Gets the view currently designated as active. */
	DisplayView getActiveView();

	/** Gets the top-level window containing this display. */
	DisplayWindow getDisplayWindow();
	
	/** Forces the display window to redo its layout	 */
	void redoWindowLayout();

	/** Gets the image canvas upon which this display's output is painted. */
	ImageCanvas getImageCanvas();

	///** Forces display to close. */ 
	//void close();
}
