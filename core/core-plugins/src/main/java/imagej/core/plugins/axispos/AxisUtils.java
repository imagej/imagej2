package imagej.core.plugins.axispos;

//
//AxisUtils.java
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

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.display.Display;
import imagej.display.DisplayManager;
import imagej.display.DisplayView;
import imagej.display.event.AxisPositionEvent;
import imagej.event.Events;
import net.imglib2.img.Axes;
import net.imglib2.img.Axis;


/**
 * Utility methods used within the axis position subpackage
 * 
 * @author Barry DeZonia
 */
public class AxisUtils {
	
	// TODO - do not have a static here. Rather support active axis in the way
	// the active display, active view, and others do.
	
	private static Axis activeAxis = Axes.Z;
	
	public static final String
	X="X", Y="Y", CH="Channel", Z="Z", TI="Time", FR="Frequency", SP="Spectra",
		PH="Phase", PO="Polarization", LI="Lifetime";

	public static final String[] AXES = {X,Y,Z,CH,TI,FR,SP,PH,PO,LI};

	/** default constructor - private */
	private AxisUtils() {
		// utility class : uninstantiable
	}

	/** gets the active axis that user can move along via hotkeys */
	public static Axis getActiveAxis() { return activeAxis; } 
	
	/** sets the active axis that user can move along via hotkeys */
	public static void setActiveAxis(Axis newAxis) { activeAxis = newAxis; } 

	/**
	 * Publishes a new AxisPositionEvent
	 * 
	 * @param change - the size of the change to make to the axis position
	 * @param relative - whether the change is a relative or absolute amount
	 */
	public static void changeCurrentAxisPosition(long change, boolean relative) {
		final DisplayManager manager = ImageJ.get(DisplayManager.class);
		final Display display = manager.getActiveDisplay();
		if (display == null) return; // headless UI or no open images
		Axis axis = getActiveAxis();
		if (axis == null) return;
		if (display.getAxisIndex(axis) < 0) return;
		DisplayView view = display.getActiveView();
		Dataset ds = (Dataset) view.getDataObject();  // TODO - safe?
		int axisIndex = ds.getAxisIndex(axis);
		if (axisIndex < 0) return;
		long[] dimensions = ds.getDims();
		long max = dimensions[axisIndex];
		Events.publish(new AxisPositionEvent(display, axis, change, max, relative));
	}
	
	/** maps an axis name String into an Axis value.
	 * returns null if some unknown axis specified */
	public static Axis getAxis(String axisName) {
		Axis axis = null;
			
		if (axisName.equals(CH)) axis = Axes.CHANNEL;
		else if (axisName.equals(FR)) axis = Axes.FREQUENCY;
		else if (axisName.equals(LI)) axis = Axes.LIFETIME;
		else if (axisName.equals(PH)) axis = Axes.PHASE;
		else if (axisName.equals(PO)) axis = Axes.POLARIZATION;
		else if (axisName.equals(SP)) axis = Axes.SPECTRA;
		else if (axisName.equals(TI)) axis = Axes.TIME;
		else if (axisName.equals(X)) axis = Axes.X;
		else if (axisName.equals(Y)) axis = Axes.Y;
		else if (axisName.equals(Z)) axis = Axes.Z;
	
		// NB : axis could still be null here : Axes.UNKNOWN
		
		return axis;
	}

}
