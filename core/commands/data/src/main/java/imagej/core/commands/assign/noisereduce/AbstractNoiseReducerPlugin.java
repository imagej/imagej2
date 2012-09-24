/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.core.commands.assign.noisereduce;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.ImgPlus;
import net.imglib2.ops.function.Function;
import net.imglib2.ops.function.real.RealImageFunction;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory.Boundary;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import imagej.Cancelable;
import imagej.ImageJ;
import imagej.command.Command;
import imagej.command.CommandModule;
import imagej.command.CommandService;
import imagej.data.Dataset;
import imagej.module.ItemIO;
import imagej.plugin.Parameter;

/**
 * TODO
 * 
 * @author Barry DeZonia
 */
public abstract class AbstractNoiseReducerPlugin<U extends RealType<U>>
	// TODO : extends ContextCommand
	implements Command, Cancelable
{
	// -- constants --

	public enum NeighborhoodType {RADIAL, RECTANGULAR}

	private static final String RADIAL_STRING = "Radial (n dimensional)";
	private static final String RECTANGULAR_STRING = "Rectangular (2 dimensional)";
	
	// -- Parameters --

	@Parameter
	protected ImageJ context;
	
	@Parameter
	protected CommandService commandService;
	
	@Parameter(type=ItemIO.BOTH)
	protected Dataset input;

	@Parameter(label = "Neighborhood type",
			choices = {RADIAL_STRING,RECTANGULAR_STRING})
	protected String neighTypeString = RADIAL_STRING;

	// -- private instance variables --
	
	private NeighborhoodType neighType;
	
	private String cancelReason;

	private Neighborhood userProvidedNeighborhood = null;
	
	// -- public API --
	
	public abstract Function<PointSet,DoubleType> getFunction(
		Function<long[],DoubleType> otherFunc);

	@Override
	public void run() {
		Neighborhood neighborhood = determineNeighborhood(input.numDimensions());
		if (neighborhood == null) return;
		@SuppressWarnings("unchecked")
		ImgPlus<U> inputImg = (ImgPlus<U>) input.getImgPlus();
		OutOfBoundsMirrorFactory<U, RandomAccessibleInterval<U>> oobFactory =
				new OutOfBoundsMirrorFactory<U,RandomAccessibleInterval<U>>(Boundary.DOUBLE);
		Function<long[],DoubleType> otherFunc =
				new RealImageFunction<U,DoubleType>(inputImg, oobFactory, new DoubleType());
		PointSet ps = neighborhood.getPoints();
		Reducer<U,DoubleType> reducer =
				new Reducer<U,DoubleType>(context, inputImg, getFunction(otherFunc), ps);
		Dataset output = reducer.reduceNoise(neighborhood.getDescription());
		input.setImgPlus(output.getImgPlus());
	}

	public void setDataset(Dataset ds) {
		input = ds;
	}
	
	public Dataset getDataset() {
		return input;
	}

	public void setNeighborhood(Neighborhood n) {
		userProvidedNeighborhood = n;
	}
	
	public NeighborhoodType getNeighborhoodType() {
		return neighType;
	}

	public void setNeighborhoodType(NeighborhoodType type) {
		neighType = type;
		setNeighString();
	}
	
	@Override
	public boolean isCanceled() {
		return cancelReason != null;
	}

	@Override
	public String getCancelReason() {
		return cancelReason;
	}

	// -- private helpers --
	
	private Neighborhood determineNeighborhood(int numDims) {
		if (userProvidedNeighborhood != null) return userProvidedNeighborhood;
		setNeighType();
		CommandModule module = null;
		try {
			Map<String,Object> inputs = new HashMap<String,Object>();
			inputs.put("numDims", numDims);
			if (neighType == NeighborhoodType.RADIAL) {
				Future<CommandModule> futureModule =
					commandService.run(RadialNeighborhoodSpecifier.class, inputs);
				module = futureModule.get();
			}
			else { // neighType == RECTANGULAR
				Future<CommandModule> futureModule =
					commandService.run(RectangularNeighborhoodSpecifier.class, inputs);
				module = futureModule.get();
			}
		} catch (Exception e) {
			cancelReason = e.getMessage();
			return null;
		}
		// unnecessary:
		//module.run();
		if (module.isCanceled()) {
			cancelReason = "Neighborhood specification cancelled by user";
			return null;
		}
		return (Neighborhood) module.getOutputs().get("neighborhood");
	}
	
	private void setNeighString() {
		if (neighType == NeighborhoodType.RADIAL)
			neighTypeString = RADIAL_STRING;
		else
			neighTypeString = RECTANGULAR_STRING;
	}
	
	private void setNeighType() {
		if (neighTypeString.equals(RADIAL_STRING))
			neighType = NeighborhoodType.RADIAL;
		else
			neighType = NeighborhoodType.RECTANGULAR;
	}
}
