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

package imagej.core.commands.debug;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.ops.function.real.RealAdaptiveMedianFunction;
import net.imglib2.ops.function.real.RealArithmeticMeanFunction;
import net.imglib2.ops.function.real.RealImageFunction;
import net.imglib2.ops.function.real.RealMaxFunction;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory.Boundary;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import imagej.ImageJ;
import imagej.command.Command;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

// TODO - below is just a place to collect code that could make its way into a
// measurement service.

/**
 * Shows how to use the MeasurementService.
 * 
 * @author Barry DeZonia
 *
 */
@Plugin(menuPath = "Plugins>Sandbox>Measure Tester")
public class MeasureTestPlugin implements Command {

	// -- Parameters --
	
	@Parameter
	private ImageJ context;
	
	// later ...
	//@Parameter
	//private MeasurementService mSrv;
	
	// -- Comand methods --
	
	@Override
	public void run() {
		// later ...
		//mSrv.testMe();
		example1();
		example2();
		example3();
	}

	// -- Code to move elsewhere as publi API to simplify measurement --
	
	// TODO - make this part of some service

	@SuppressWarnings({"rawtypes","unchecked"})
	private <O extends RealType<O>>
		RealImageFunction<?,O>
			getImageFunction(Img<? extends RealType<?>> img, O type)
	{
		return new RealImageFunction(img, type);
	}

	// TODO - make this part of some service

	private <O extends RealType<O>>
		RealImageFunction<?,O>
			getImageFunction(Dataset ds, O type)
	{
			return getImageFunction(ds.getImgPlus(), type);
	}

	// TODO - make this part of some service

	@SuppressWarnings({"rawtypes","unchecked"})
	private <O extends RealType<O>>
		RealImageFunction<?,O>
			getImageFunction(
				Img<? extends RealType<?>> img,
				OutOfBoundsFactory<?, Img<?>> oobFactory, O type)
	{
		return new RealImageFunction(img, oobFactory, type);
	}

	// TODO - make this part of some service

	private <O extends RealType<O>>
		RealImageFunction<?,O>
			getImageFunction(
				Dataset ds, OutOfBoundsFactory<?, Img<?>> oobFactory, O type)
	{
		return getImageFunction(ds.getImgPlus(), oobFactory, type);
	}

	// -- Examples --
	
	// standard ways of measuring various values. figure out ways to generalize
	
	// a basic measurement
	private void example1() {
		Dataset ds = getTestData();
		DoubleType output = new DoubleType();
		RealImageFunction<?, DoubleType> imgFunc = getImageFunction(ds, output);
		RealArithmeticMeanFunction<DoubleType> meanFunc =
				new RealArithmeticMeanFunction<DoubleType>(imgFunc);
		PointSet region = new HyperVolumePointSet(ds.getDims());
		meanFunc.compute(region, output);
		System.out.println("arithmetic mean is " + output.getRealDouble());
	}

	// a basic measurement with out of bounds data handling
	private void example2() {
		Dataset ds = getTestData();
		DoubleType output = new DoubleType();
		OutOfBoundsFactory<?, Img<?>> oobFactory = getOobFactory();
		RealImageFunction<?, DoubleType> imgFuncWithOOB =
				getImageFunction(ds, oobFactory, output);
		RealMaxFunction<DoubleType> maxFunc =
				new RealMaxFunction<DoubleType>(imgFuncWithOOB);
		PointSet region = new HyperVolumePointSet(ds.getDims());
		maxFunc.compute(region, output);
		System.out.println("max is " + output.getRealDouble());
	}

	// a measurement that has a metric with nondefault constructor and oob
	private void example3() {
		Dataset ds = getTestData();
		DoubleType output = new DoubleType();
		OutOfBoundsFactory<?, Img<?>> oobFactory = getOobFactory();
		RealImageFunction<?, DoubleType> imgFuncWithOOB =
				getImageFunction(ds, oobFactory, output);
		// force to (0,0) - tests that oob code is working
		long ctrX = 0; //ds.dimension(0) / 2;
		long ctrY = 0; //ds.dimension(1) / 2;
		long[] posDeltas = new long[]{3,3};
		long[] negDeltas = new long[]{3,3};
		List<PointSet> pointSets = getNestedNeighborhoods(3);
		RealAdaptiveMedianFunction<DoubleType> adapMedFunc =
				new RealAdaptiveMedianFunction<DoubleType>(imgFuncWithOOB, pointSets);
		PointSet region =
				new HyperVolumePointSet(new long[]{ctrX,ctrY}, negDeltas, posDeltas);
		adapMedFunc.compute(region, output);
		System.out.println("adaptive median is " + output.getRealDouble());
	}

	// -- private helpers -

	// TODO - move to a family of methods that provide shortcuts for creating
	// an oob factory? Probably not.
	
	private OutOfBoundsFactory<?, Img<?>> getOobFactory()
	{
		return new OutOfBoundsMirrorFactory(Boundary.DOUBLE);
	}
	
	private Dataset getTestData() {
		DatasetService dsSrv = context.getService(DatasetService.class);
		Dataset ds =
				dsSrv.create(new long[]{7,7}, "tmp", new AxisType[]{Axes.X, Axes.Y},
											8, false, false);
		Cursor<? extends RealType<?>> cursor = ds.getImgPlus().cursor();
		int i = 0;
		while (cursor.hasNext()) {
			cursor.next().setReal(i++);
		}
		return ds;
	}

	// this returns a list of PointSets that are progressively bigger.
	// for illustration.
	private List<PointSet>
		getNestedNeighborhoods(long delta)
	{
		long[] zeroOrigin = new long[2];
		long[] tmpNeg = new long[]{delta, delta};
		long[] tmpPos = new long[]{delta, delta};
		List<PointSet> regions = new ArrayList<PointSet>();
		for (int i = 0; i < 5; i++) {
			PointSet ps = new HyperVolumePointSet(zeroOrigin, tmpNeg, tmpPos);
			regions.add(ps);
			tmpNeg = tmpNeg.clone();
			tmpPos = tmpPos.clone();
			tmpNeg[0]++; tmpNeg[1]++;
			tmpPos[0]++; tmpPos[1]++;
		}
		return regions;
	}
	
}
