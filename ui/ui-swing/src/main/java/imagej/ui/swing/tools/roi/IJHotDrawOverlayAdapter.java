//
// IJHotDrawROIAdapter.java
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

package imagej.ui.swing.tools.roi;

import imagej.data.roi.Overlay;

import org.jhotdraw.draw.Figure;

/**
 * Implement the IJHotDrawROIAdapter to create an adapter that lets JHotDraw
 * edit ImageJ ROIs.
 * 
 * @author Lee Kamentsky
 */
public interface IJHotDrawOverlayAdapter {

	/**
	 * Determines whether the adapter can handle a particular roi
	 * 
	 * @param roi - a ROI that might be editable
	 */
	boolean supports(Overlay roi);

	/**
	 * Creates a new ROI of the type indicated by the given name. The name must be
	 * from those returned by getROITypeNames
	 * 
	 * @param name the name of a ROI that this adapter can create
	 * @return a ROI of the associated type in the default initial state
	 */
	Overlay createNewOverlay();

	/** Creates a default figure of the type handled by this adapter. */
	Figure createDefaultFigure();

	/**
	 * Creates an appropriate figure for the ROI and attach the adapter to it. The
	 * adapter should manage changes to the figure's model by propagating them to
	 * the ROI.
	 * 
	 * @param roi
	 */
	Figure attachFigureToOverlay(Overlay overlay);

	/**
	 * The figure and ROI are separated as the editing session ends. This may be a
	 * place where incompleteness in the ROI is reconciled, such as connecting the
	 * final line in a polygon.
	 * 
	 * @param figure
	 */
	void detachFigureFromOverlay(Figure figure);

}
