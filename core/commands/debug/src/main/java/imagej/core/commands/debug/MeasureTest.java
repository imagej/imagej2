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
import net.imglib2.ops.function.Function;
import net.imglib2.ops.function.real.RealAdaptiveMedianFunction;
import net.imglib2.ops.function.real.RealArithmeticMeanFunction;
import net.imglib2.ops.function.real.RealImageFunction;
import net.imglib2.ops.function.real.RealMaxFunction;
import net.imglib2.ops.function.real.RealMedianFunction;
import net.imglib2.ops.function.real.RealMinFunction;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory.Boundary;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import imagej.command.Command;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.display.ImageDisplay;
import imagej.data.display.OverlayService;
import imagej.data.measure.BasicStats;
import imagej.data.measure.BasicStatsFunction;
import imagej.data.measure.MeasurementService;
import imagej.event.StatusService;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.util.RealRect;
import imagej.widget.Button;

/**
 * Shows how to use the MeasurementService.
 * 
 * @author Barry DeZonia
 *
 */
@Plugin(menuPath = "Plugins>Sandbox>Measure Tester")
public class MeasureTest implements Command {

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
	
	@Parameter(label="Calc mean", callback = "chooseMean")
	private Button mean;
	
	@Parameter(label="Calc min", callback = "chooseMin")
	private Button min;
	
	@Parameter(label="Calc max", callback = "chooseMax")
	private Button max;

	@Parameter(label="Calc median", callback = "chooseMedian")
	private Button median;

	// -- private variables --
	
	private Function<PointSet,DoubleType> function;
	
	private String name;
	
	// -- Command methods --
	
	@Override
	public void run() {
	}

	// -- MeasureTest methods --
	
	protected void chooseMean() {
		RealImageFunction<?,DoubleType> imgFunc =
				mSrv.imgFunction(dataset, new DoubleType());
		function = new RealArithmeticMeanFunction<DoubleType>(imgFunc);
		name = "Mean";
		calc();
	}
	
	protected void chooseMin() {
		RealImageFunction<?,DoubleType> imgFunc =
				mSrv.imgFunction(dataset, new DoubleType());
		function = new RealMinFunction<DoubleType>(imgFunc);
		name = "Min";
		calc();
	}
	
	protected void chooseMax() {
		RealImageFunction<?,DoubleType> imgFunc =
				mSrv.imgFunction(dataset, new DoubleType());
		function = new RealMaxFunction<DoubleType>(imgFunc);
		name = "Max";
		calc();
	}
	
	protected void chooseMedian() {
		RealImageFunction<?,DoubleType> imgFunc =
				mSrv.imgFunction(dataset, new DoubleType());
		function = new RealMedianFunction<DoubleType>(imgFunc);
		name = "Median";
		calc();
	}
	
	// -- private helpers --

	private void calc() {
		RealRect bounds = oSrv.getSelectionBounds(display);
		long[] minPt = new long[display.numDimensions()];
		long[] maxPt = new long[display.numDimensions()];
		minPt[0] = (long) bounds.x;
		minPt[1] = (long) bounds.y;
		maxPt[0] = (long) (bounds.x + bounds.width);
		maxPt[1] = (long) (bounds.y + bounds.height);
		HyperVolumePointSet points = new HyperVolumePointSet(minPt,maxPt);
		DoubleType output = new DoubleType();
		mSrv.measure(function, points, output);
		sSrv.showStatus(name+" of selected region is "+output.getRealDouble());
	}
	
	private <T extends RealType<T>> OutOfBoundsFactory<T, Img<T>> getOobFactory()
	{
		return new OutOfBoundsMirrorFactory<T,Img<T>>(Boundary.DOUBLE);
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
		OutOfBoundsFactory<UnsignedByteType, Img<UnsignedByteType>>
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
		OutOfBoundsFactory<UnsignedByteType, Img<UnsignedByteType>>
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

	// measuring ,multiple things at a time
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
}
