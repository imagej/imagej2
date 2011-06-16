package imagej.core.plugins.restructure;



//
//SetActiveAxis.java
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


import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Plugin;
import imagej.plugin.Parameter;


/**
* Changes the axis to move along to user specified value. This axis of movement
* is used in the move axis position forward/backward plugins.
* 
* @author Barry DeZonia
*/
@Plugin(menu = {
@Menu(label = "Image", mnemonic = 'i'),
@Menu(label = "Stacks", mnemonic = 's'),
@Menu(label = "Set Active Axis...") })
public class SetActiveAxis implements ImageJPlugin {

	@Parameter(label="Axis",choices={  // NB - X & Y excluded right now
		RestructureUtils.Z,
		RestructureUtils.CH,
		RestructureUtils.TI,
		RestructureUtils.FR,
		RestructureUtils.SP,
		RestructureUtils.PH,
		RestructureUtils.PO,
		RestructureUtils.LI})
	String axisName;
	
	// TODO - do not have a static here. Rather copy the idea of active display,
	// active view, etc.

	private static Axis activeAxis = Axes.Z;
	
	public static Axis getActiveAxis() { return activeAxis; } 
	
	@Override
	public void run() {
		Axis newActiveAxis = RestructureUtils.getAxis(axisName);
		if (newActiveAxis != null)
			activeAxis = newActiveAxis;
	}
}
