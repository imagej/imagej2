/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

import imagej.command.Command;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.display.ImageDisplay;
import imagej.data.display.OverlayService;
import imagej.data.measure.BasicStats;
import imagej.data.measure.BasicStatsFunction;
import imagej.data.measure.MeasurementService;
import imagej.data.overlay.Overlay;
import imagej.widget.Button;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.ops.function.Function;
import net.imglib2.ops.function.real.RealAdaptiveMedianFunction;
import net.imglib2.ops.function.real.RealArithmeticMeanFunction;
import net.imglib2.ops.function.real.RealImageFunction;
import net.imglib2.ops.function.real.RealMaxFunction;
import net.imglib2.ops.function.real.RealMedianFunction;
import net.imglib2.ops.function.real.RealMinFunction;
import net.imglib2.ops.function.real.RealPointCountFunction;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.ops.pointset.PointSetIterator;
import net.imglib2.ops.pointset.RoiPointSet;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory.Boundary;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.app.StatusService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Shows how to use the MeasurementService.
 * 
 * @author Barry DeZonia
 *
 */
@Plugin(type = Command.class, menuPath = "Plugins>Sandbox>Measurement Demo")
public class MeasurementDemo implements Command {

	// -- Parameters --
	
	@Parameter
	private DatasetService dsSrv;
	
	@Parameter
	private MeasurementService mSrv;
	
	@Parameter
	private OverlayService oSrv;
	
	@Parameter
	private StatusService sSrv;
	
	@Parameter
	private ImageDisplay display;
	
	@Parameter
	private Dataset dataset;
	
	@Parameter(label="Measure mean", callback = "mean")
	private Button mean;
	
	@Parameter(label="Measure min", callback = "min")
	private Button min;
	
	@Parameter(label="Measure max", callback = "max")
	private Button max;

	@Parameter(label="Measure median", callback = "median")
	private Button median;

	@Parameter(label="Measure region size", callback = "regionSize")
	private Button regionSize;

	// -- private variables --
	
	private Function<PointSet,DoubleType> function;
	
	private String funcName;
	
	// -- Command methods --
	
	@Override
	public void run() {
		// does nothing. the whole of this plugin is the interactivity of the button
		// presses with updates to the status line.
	}

	// -- MeasureTest methods --
	
	protected void mean() {
		RealImageFunction<?,DoubleType> imgFunc =
				mSrv.imgFunction(dataset, new DoubleType());
		function = new RealArithmeticMeanFunction<DoubleType>(imgFunc);
		funcName = "Mean";
		calc();
	}
	
	protected void min() {
		RealImageFunction<?,DoubleType> imgFunc =
				mSrv.imgFunction(dataset, new DoubleType());
		function = new RealMinFunction<DoubleType>(imgFunc);
		funcName = "Min";
		calc();
	}
	
	protected void max() {
		RealImageFunction<?,DoubleType> imgFunc =
				mSrv.imgFunction(dataset, new DoubleType());
		function = new RealMaxFunction<DoubleType>(imgFunc);
		funcName = "Max";
		calc();
	}
	
	protected void median() {
		RealImageFunction<?,DoubleType> imgFunc =
				mSrv.imgFunction(dataset, new DoubleType());
		function = new RealMedianFunction<DoubleType>(imgFunc);
		funcName = "Median";
		calc();
	}
	
	protected void regionSize() {
		function = new RealPointCountFunction<DoubleType>(new DoubleType());
		funcName = "Region size";
		calc();
	}
	// -- private helpers --

	private void calc() {
		PointSet points;
		Overlay overlay = oSrv.getActiveOverlay(display);
		if (overlay != null) {
			points = new RoiPointSet(overlay.getRegionOfInterest());
		}
		else {
			long[] dims = display.getDims();
			// 1st plane only
			for (int i = 2; i < dims.length; i++) {
				dims[i] = 1;
			}
			points = new HyperVolumePointSet(dims);
		}
		DoubleType output = new DoubleType();
		mSrv.measure(function, points, output);
		sSrv.showStatus(funcName+" of selected region is "+output.getRealDouble());
	}
	
	private <T extends RealType<T>> OutOfBoundsFactory<T, RandomAccessibleInterval<T>> getOobFactory()
	{
		return new OutOfBoundsMirrorFactory<T,RandomAccessibleInterval<T>>(Boundary.DOUBLE);
	}
	
	private Dataset getTestData() {
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
	
	// -- Other examples --
	
	// standard ways of measuring various values.
	
	// a basic measurement
	private void example1() {
		Dataset ds = getTestData();
		DoubleType output = new DoubleType();
		RealImageFunction<?, DoubleType> imgFunc = mSrv.imgFunction(ds, output);
		RealArithmeticMeanFunction<DoubleType> meanFunc =
				new RealArithmeticMeanFunction<DoubleType>(imgFunc);
		PointSet region = new HyperVolumePointSet(ds.getDims());
		mSrv.measure(meanFunc, region, output);
		System.out.println("arithmetic mean is " + output.getRealDouble());
	}

	// a basic measurement with out of bounds data handling
	private void example2() {
		Dataset ds = getTestData();
		DoubleType output = new DoubleType();
		OutOfBoundsFactory<UnsignedByteType, RandomAccessibleInterval<UnsignedByteType>>
			oobFactory = getOobFactory();
		@SuppressWarnings("unchecked")
		RealImageFunction<?,DoubleType> imgFuncWithOOB =
			new	RealImageFunction<UnsignedByteType,DoubleType>(
						(Img<UnsignedByteType>)ds.getImgPlus(), oobFactory, output);
		RealMaxFunction<DoubleType> maxFunc =
				new RealMaxFunction<DoubleType>(imgFuncWithOOB);
		PointSet region = new HyperVolumePointSet(ds.getDims());
		mSrv.measure(maxFunc,region, output);
		System.out.println("max is " + output.getRealDouble());
	}

	// a measurement that has a metric with nondefault constructor and oob
	private void example3() {
		Dataset ds = getTestData();
		DoubleType output = new DoubleType();
		OutOfBoundsFactory<UnsignedByteType, RandomAccessibleInterval<UnsignedByteType>>
			oobFactory = getOobFactory();
		@SuppressWarnings("unchecked")
		RealImageFunction<?,DoubleType> imgFuncWithOOB =
			new RealImageFunction<UnsignedByteType,DoubleType>(
						(Img<UnsignedByteType>)ds.getImgPlus(), oobFactory, output);
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
		mSrv.measure(adapMedFunc,region, output);
		System.out.println("adaptive median is " + output.getRealDouble());
	}

	// measuring multiple things at a time
	private void example4() {
		Dataset ds = getTestData();
		DoubleType output = new DoubleType();
		RealImageFunction<?, DoubleType> imgFunc = mSrv.imgFunction(ds, output);
		RealArithmeticMeanFunction<DoubleType> meanFunc =
				new RealArithmeticMeanFunction<DoubleType>(imgFunc);
		RealMinFunction<DoubleType> minFunc =
				new RealMinFunction<DoubleType>(imgFunc);
		RealMaxFunction<DoubleType> maxFunc =
				new RealMaxFunction<DoubleType>(imgFunc);
		List<Function<PointSet,DoubleType>> funcList =
				new ArrayList<Function<PointSet,DoubleType>>();
		List<DoubleType> outputList = new ArrayList<DoubleType>();
		funcList.add(meanFunc);
		funcList.add(minFunc);
		funcList.add(maxFunc);
		outputList.add(new DoubleType());
		outputList.add(new DoubleType());
		outputList.add(new DoubleType());
		PointSet region = new HyperVolumePointSet(ds.getDims());
		mSrv.measure(funcList, region, outputList);
		System.out.println("mean = "+outputList.get(0).getRealDouble());
		System.out.println("min = "+outputList.get(1).getRealDouble());
		System.out.println("max = "+outputList.get(2).getRealDouble());
	}
	
	// measuring a custom set of data using an aggregating class
	private void example5() {
		Dataset ds = getTestData();
		DoubleType output = new DoubleType();
		RealImageFunction<?, DoubleType> imgFunc = mSrv.imgFunction(ds, output);
		BasicStatsFunction<DoubleType> statFunc =
				new BasicStatsFunction<DoubleType>(imgFunc, new DoubleType());
		PointSet region = new HyperVolumePointSet(ds.getDims());
		BasicStats stats = new BasicStats();
		mSrv.measure(statFunc, region, stats);
		System.out.println("mean = "+stats.getXBar());
		System.out.println("var = "+stats.getS2n1());
	}
	
	// measuring a user defined function with the service
	private void example6() {
		Dataset ds = getTestData();
		IntType output = new IntType();
		RealImageFunction<?, IntType> imgFunc = mSrv.imgFunction(ds, output);
		CustomFunction func = new CustomFunction(imgFunc);
		PointSet region = new HyperVolumePointSet(ds.getDims());
		mSrv.measure(func, region, output);
		System.out.println("total 7's = "+output.get());
	}
	
	// user defined function for example6 : count the number of 7's in the data
	private class CustomFunction implements Function<PointSet,IntType> {

		private Function<long[],IntType> data;
		private IntType tmp;
		
		public CustomFunction(Function<long[],IntType> data) {
			this.data = data;
			this.tmp = new IntType();
		}
		
		@Override
		public void compute(PointSet input, IntType output) {
			int numSevens = 0;
			PointSetIterator iter = input.iterator();
			while (iter.hasNext()) {
				long[] coord = iter.next();
				data.compute(coord, tmp);
				if (tmp.get() == 7) numSevens++;
			}
			output.set(numSevens);
		}

		@Override
		public IntType createOutput() {
			return new IntType();
		}

		@Override
		public Function<PointSet, IntType> copy() {
			return new CustomFunction(data.copy());
		}
		
	}
}
