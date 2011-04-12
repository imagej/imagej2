//
// AbstractJHotDrawROIAdapter.java
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

package imagej.roi.ui.jhotdraw;

import imagej.roi.ImageJROI;

import java.util.HashMap;
import java.util.Map;

import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.event.FigureEvent;
import org.jhotdraw.draw.event.FigureListener;

/**
 * An abstract class that gives default behavior for the IJHotDrawROIAdapter
 * interface
 * 
 * @author Lee Kamentsky
 */
public abstract class AbstractJHotDrawROIAdapter implements
	IJHotDrawROIAdapter, FigureListener
{

	protected Map<Figure, ImageJROI> map = new HashMap<Figure, ImageJROI>();

	// -- IJHotDrawROIAdapter methods --

	@Override
	public Figure attachFigureToROI(final ImageJROI roi) {
		final Figure figure = createFigureForROI(roi);
		map.put(figure, roi);
		figure.addFigureListener(this);
		return figure;
	}

	@Override
	public void detachFigureFromROI(final Figure figure) {
		figure.removeFigureListener(this);
	}

	// -- FigureListener methods --

	@Override
	public void figureChanged(final FigureEvent event) {
		final Figure figure = event.getFigure();
		updateROIModel(figure, map.get(figure));
	}

	@Override
	public void areaInvalidated(final FigureEvent event) {
		// NB: No action needed.
	}

	@Override
	public void attributeChanged(final FigureEvent event) {
		// NB: No action needed.
	}

	@Override
	public void figureAdded(final FigureEvent event) {
		// NB: No action needed.
	}

	@Override
	public void figureHandlesChanged(final FigureEvent event) {
		// NB: No action needed.
	}

	@Override
	public void figureRemoved(final FigureEvent event) {
		// NB: No action needed.
	}

	@Override
	public void figureRequestRemove(final FigureEvent event) {
		// NB: No action needed.
	}

	// -- Internal API methods --

	/**
	 * Create the appropriate figure for the given ROI.
	 * 
	 * @param roi
	 */
	protected abstract Figure createFigureForROI(ImageJROI roi);

	/**
	 * The implementer should update the ROI model to reflect the data in the
	 * figure.
	 * 
	 * @param figure
	 * @param roi
	 */
	protected abstract void updateROIModel(Figure figure, ImageJROI roi);

}
