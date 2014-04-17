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

package imagej.plugins.commands.assign;

import imagej.data.Dataset;
import imagej.data.display.DatasetView;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.data.overlay.Overlay;
import net.imglib2.ops.operation.real.unary.RealSqrt;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.menu.MenuConstants;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Fills an output Dataset by taking the square root of the data values of an
 * input Dataset.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, menu = {
	@Menu(label = MenuConstants.PROCESS_LABEL,
		weight = MenuConstants.PROCESS_WEIGHT,
		mnemonic = MenuConstants.PROCESS_MNEMONIC),
	@Menu(label = "Math", mnemonic = 'm'),
	@Menu(label = "Square Root...", weight = 16) }, headless = true)
public class SquareRootDataValues<T extends RealType<T>>
	extends ContextCommand
{

	// -- instance variables that are Parameters --

	@Parameter
	private OverlayService overlayService;

	@Parameter
	private ImageDisplayService imgDispService;

	@Parameter(type = ItemIO.BOTH)
	private ImageDisplay display;
	
	@Parameter(label = "Apply to all planes")
	private boolean allPlanes;

	// -- public interface --

	@Override
	public void run() {
		Dataset dataset = imgDispService.getActiveDataset(display);
		Overlay overlay = overlayService.getActiveOverlay(display);
		DatasetView view = imgDispService.getActiveDatasetView(display);
		
		final RealSqrt<DoubleType, DoubleType> op =
			new RealSqrt<DoubleType, DoubleType>();
		
		final InplaceUnaryTransform<T, DoubleType> transform;
		
		if (allPlanes)
			transform = 
				new InplaceUnaryTransform<T, DoubleType>(
					op, new DoubleType(), dataset, overlay);
		else
			transform = 
				new InplaceUnaryTransform<T, DoubleType>(
					op, new DoubleType(), dataset, overlay,
					view.getPlanePosition());
		
		transform.run();
	}

	public ImageDisplay getDisplay() {
		return display;
	}

	public void setDisplay(ImageDisplay display) {
		this.display = display;
	}

	public boolean isAllPlanes() {
		return allPlanes;
	}
	
	public void setAllPlanes(boolean value) {
		this.allPlanes = value;
	}

}
