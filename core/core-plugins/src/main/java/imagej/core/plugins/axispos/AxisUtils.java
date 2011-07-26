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

import java.util.HashMap;
import java.util.Map;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.display.Display;
import imagej.display.DisplayService;
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
	
	public static final String
		X="X", Y="Y", CH="Channel", Z="Z", TI="Time", FR="Frequency", SP="Spectra",
		PH="Phase", PO="Polarization", LI="Lifetime";

	public static final String[] AXES_NAMES = {X,Y,Z,CH,TI,FR,SP,PH,PO,LI};

	public static final Axis[] AXES = { Axes.X, Axes.Y, Axes.Z, Axes.CHANNEL,
		Axes.TIME, Axes.FREQUENCY, Axes.SPECTRA, Axes.PHASE, Axes.POLARIZATION,
		Axes.LIFETIME
	};

	private static final Map<String,Axis> nameMap;
	private static final Map<Axis,String> axisMap;
	
	static {
		nameMap = new HashMap<String,Axis>();
		axisMap = new HashMap<Axis,String>();
		for (int i = 0; i < AXES.length; i++) {
			nameMap.put(AXES_NAMES[i], AXES[i]);
			axisMap.put(AXES[i], AXES_NAMES[i]);
		}
	}
	
	/**
	 * Default constructor - private */
	private AxisUtils() {
		// utility class : uninstantiable
	}

	/**
	 * Publishes a new AxisPositionEvent
	 * 
	 * @param change - the size of the change to make to the axis position
	 * @param relative - whether the change is a relative or absolute amount
	 */
	public static void changeCurrentAxisPosition(long change, boolean relative) {
		final DisplayService displayService = ImageJ.get(DisplayService.class);
		final Display display = displayService.getActiveDisplay();
		if (display == null) return; // headless UI or no open images
		Axis axis = display.getActiveAxis();
		if (axis == null) return;
		if (display.getAxisIndex(axis) < 0) return;
		Dataset ds = displayService.getActiveDataset(display);
		int axisIndex = ds.getAxisIndex(axis);
		if (axisIndex < 0) return;
		long max = ds.getExtents().dimension(axisIndex);
		Events.publish(new AxisPositionEvent(display, axis, change, max, relative));
	}
	
	/**
	 * Maps an axis name String into an Axis value.
	 * returns null if some unknown axis specified */
	public static Axis getAxis(String axisName) {
	  // NB - null is a legit return value for Axis.UNKNOWN
		return nameMap.get(axisName);
	}

	public static String getAxisName(Axis axis) {
	  // NB - null is a legit return value for Axis.UNKNOWN
		return axisMap.get(axis);
	}
}
