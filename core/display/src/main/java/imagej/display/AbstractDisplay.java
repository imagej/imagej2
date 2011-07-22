//
// AbstractDisplay.java
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

import imagej.data.DataObject;
import imagej.data.Dataset;
import imagej.data.roi.Overlay;
import imagej.display.event.DisplayUpdatedEvent;
import imagej.event.Events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
import net.imglib2.meta.LabeledAxes;

/**
 * The abstract display handles axes resolution, maintaining the dimensionality of the
 * EuclideanSpace represented by the display.
 *
 * @author Lee Kamentsky
 */
public abstract class AbstractDisplay implements Display {

	private Axis activeAxis = Axes.Z;
	
	private final ArrayList<DisplayView> views = new ArrayList<DisplayView>();

	public List<Axis> getAxes() {
		ArrayList<Axis> axes = new ArrayList<Axis>();
		for (DisplayView v:this.getViews()) {
			DataObject o = v.getDataObject();
			if (o instanceof Dataset) {
				Dataset dataset = (Dataset)o;
				int nAxes = dataset.getImgPlus().numDimensions();
				LabeledAxes a = (LabeledAxes)(o);
				for (int i=0; i<nAxes; i++) {
					Axis axis = a.axis(i);
					if (! axes.contains(axis)) {
						axes.add(axis);
					}
				}
			}
		}
		for (DisplayView v:this.getViews()) {
			DataObject o = v.getDataObject();
			if (o instanceof Overlay) {
				Overlay overlay = (Overlay)o;
				if (overlay.getRegionOfInterest() == null) continue;
				int nAxes = overlay.getRegionOfInterest().numDimensions();
				LabeledAxes a = (LabeledAxes)(o);
				for (int i=0; i<nAxes; i++) {
					Axis axis = a.axis(i);
					if (! axes.contains(axis)) {
						axes.add(axis);
					}
				}
			}
		}
		return axes;
	}
	/* (non-Javadoc)
	 * @see net.imglib2.meta.LabeledAxes#getAxisIndex(net.imglib2.img.Axis)
	 */
	@Override
	public int getAxisIndex(Axis axis) {
		return getAxes().indexOf(axis);
	}

	/* (non-Javadoc)
	 * @see net.imglib2.meta.LabeledAxes#axis(int)
	 */
	@Override
	public Axis axis(int d) {
		return getAxes().get(d);
	}

	/* (non-Javadoc)
	 * @see net.imglib2.meta.LabeledAxes#axes(net.imglib2.img.Axis[])
	 */
	@Override
	public void axes(Axis[] axes) {
		System.arraycopy(getAxes().toArray(), 0, axes, 0, axes.length);
	}

	/* (non-Javadoc)
	 * @see net.imglib2.meta.LabeledAxes#setAxis(net.imglib2.img.Axis, int)
	 */
	@Override
	public void setAxis(Axis axis, int d) {
		throw new UnsupportedOperationException("You can't change the axes of a display");
	}

	/* (non-Javadoc)
	 * @see net.imglib2.meta.LabeledAxes#calibration(int)
	 */
	@Override
	public double calibration(int d) {
		// The display is calibrated in the base unit
		return 1.0;
	}

	/* (non-Javadoc)
	 * @see net.imglib2.meta.LabeledAxes#calibration(double[])
	 */
	@Override
	public void calibration(double[] cal) {
		Arrays.fill(cal, 1.0);
	}

	/* (non-Javadoc)
	 * @see net.imglib2.meta.LabeledAxes#setCalibration(double, int)
	 */
	@Override
	public void setCalibration(double cal, int d) {
		throw new UnsupportedOperationException("You can't change the calibration of a display yet");
	}
	/* (non-Javadoc)
	 * @see net.imglib2.EuclideanSpace#numDimensions()
	 */
	@Override
	public int numDimensions() {
		return getAxes().size();
	}

	@Override
	public void addView(final DisplayView view) {
		views.add(view);
		update();
		redoWindowLayout();
		Events.publish(new DisplayUpdatedEvent(this));
	}

	@Override
	public DisplayView getActiveView() {
		// CTR TODO - do better than hardcoding first view
		return views.size() > 0 ? views.get(0) : null;
	}

	@Override
	public List<DisplayView> getViews() {
		return Collections.unmodifiableList(views);
	}

	@Override
	public void removeAllViews() {
		views.clear();
		update();
		redoWindowLayout();
		Events.publish(new DisplayUpdatedEvent(this));
	}

	@Override
	public void removeView(final DisplayView view) {
		views.remove(view);
		view.dispose();
		update();
		redoWindowLayout();
		Events.publish(new DisplayUpdatedEvent(this));
	}
	
	@Override
	public Axis getActiveAxis() {
		return activeAxis;
	}
	
	@Override
	public void setActiveAxis(Axis axis) {
		activeAxis = axis;
	}
}
