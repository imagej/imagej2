//
// DivideDataValuesBy.java
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

package imagej.core.plugins.assign;

import imagej.ImageJ;
import imagej.data.display.ImageDisplay;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.options.OptionsService;
import net.imglib2.ops.Real;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.ops.operation.unary.real.RealDivideConstant;

/**
 * Fills an output Dataset by dividing an input Dataset by a user defined
 * constant value.
 * 
 * @author Barry DeZonia
 */
@Plugin(
	menu = { @Menu(label = "Process", mnemonic = 'p'),
		@Menu(label = "Math", mnemonic = 'm'),
		@Menu(label = "Divide...", weight = 4) })
public class DivideDataValuesBy extends AbstractPreviewPlugin {

	// -- instance variables that are Parameters --

	@Parameter
	ImageDisplay display;

	@Parameter(label = "Value")
	private double constant;

	@Parameter(label = "Preview")
	private boolean preview;

	// -- public interface --

	@Override
	public UnaryOperation<Real, Real> getOperation() {
		final OptionsService service = ImageJ.get(OptionsService.class);
		final String dbzString =
			(String) service.getOption("imagej.options.plugins.OptionsMisc",
				"divByZeroVal");
		double dbzVal;
		try {
			dbzVal = Double.parseDouble(dbzString);
		}
		catch (final NumberFormatException e) {
			dbzVal = Double.POSITIVE_INFINITY;
		}
		return new RealDivideConstant(constant, dbzVal);
	}

	@Override
	public ImageDisplay getDisplay() {
		return display;
	}

	@Override
	public boolean previewOn() {
		return preview;
	}
}
