//
// AbstractImageDisplay.java
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

package imagej.data.display;

import imagej.data.Data;
import imagej.data.Dataset;
import imagej.data.roi.Overlay;
import imagej.ext.display.AbstractDisplay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
import net.imglib2.meta.LabeledAxes;

/**
 * The abstract display handles axes resolution, maintaining the dimensionality
 * of the EuclideanSpace represented by the display.
 * 
 * @author Lee Kamentsky
 * @author Curtis Rueden
 */
public abstract class AbstractImageDisplay extends AbstractDisplay<DataView>
	implements ImageDisplay
{

	private Axis activeAxis = Axes.Z;

	public AbstractImageDisplay() {
		super(DataView.class);
	}

	// -- LabeledAxes methods --

	@Override
	public int getAxisIndex(final Axis axis) {
		return getAxes().indexOf(axis);
	}

	@Override
	public Axis axis(final int d) {
		return getAxes().get(d);
	}

	@Override
	public void axes(final Axis[] axes) {
		System.arraycopy(getAxes().toArray(), 0, axes, 0, axes.length);
	}

	@Override
	public void setAxis(final Axis axis, final int d) {
		throw new UnsupportedOperationException(
			"You can't change the axes of a display");
	}

	@Override
	public double calibration(final int d) {
		// The display is calibrated in the base unit
		return 1.0;
	}

	@Override
	public void calibration(final double[] cal) {
		Arrays.fill(cal, 1.0);
	}

	@Override
	public void setCalibration(final double cal, final int d) {
		throw new UnsupportedOperationException(
			"You can't change the calibration of a display yet");
	}

	// -- EuclideanSpace methods --

	@Override
	public int numDimensions() {
		return getAxes().size();
	}

	// -- ImageDisplay methods --

	@Override
	public void addView(final DataView view) {
		add(view);
		update();
		redoWindowLayout();
	}

	@Override
	public DataView getActiveView() {
		return size() > 0 ? get(0) : null;
	}

	@Override
	public List<DataView> getViews() {
		final ArrayList<DataView> views = new ArrayList<DataView>();
		views.addAll(this);
		return views;
	}

	@Override
	public void removeAllViews() {
		clear();
		update();
		redoWindowLayout();
	}

	@Override
	public void removeView(final DataView view) {
		remove(view);
		view.dispose();
		update();
		redoWindowLayout();
	}

	@Override
	public Axis getActiveAxis() {
		return activeAxis;
	}

	@Override
	public void setActiveAxis(final Axis axis) {
		activeAxis = axis;
	}

	@Override
	public List<Axis> getAxes() {
		final ArrayList<Axis> axes = new ArrayList<Axis>();
		for (final DataView v : this.getViews()) {
			final Data o = v.getData();
			if (o instanceof Dataset) {
				final Dataset dataset = (Dataset) o;
				final int nAxes = dataset.getImgPlus().numDimensions();
				final LabeledAxes a = (LabeledAxes) (o);
				for (int i = 0; i < nAxes; i++) {
					final Axis axis = a.axis(i);
					if (!axes.contains(axis)) {
						axes.add(axis);
					}
				}
			}
		}
		for (final DataView v : this.getViews()) {
			final Data o = v.getData();
			if (o instanceof Overlay) {
				final Overlay overlay = (Overlay) o;
				if (overlay.getRegionOfInterest() == null) continue;
				final int nAxes = overlay.getRegionOfInterest().numDimensions();
				final LabeledAxes a = (LabeledAxes) (o);
				for (int i = 0; i < nAxes; i++) {
					final Axis axis = a.axis(i);
					if (!axes.contains(axis)) {
						axes.add(axis);
					}
				}
			}
		}
		return axes;
	}

	// -- Display methods --

	@Override
	public boolean canDisplay(final Class<?> c) {
		return Data.class.isAssignableFrom(c) || super.canDisplay(c);
	}

	@Override
	public void display(final Object o) {
		if (o instanceof Dataset) display((Dataset) o);
		else if (o instanceof Overlay) display((Overlay) o);
		else super.display(o);
	}

}
