package imagej.core.commands.debug;

import net.imglib2.Cursor;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.ops.function.real.RealAdaptiveMedianFunction;
import net.imglib2.ops.function.real.RealMaxFunction;
import net.imglib2.ops.function.real.RealMedianFunction;
import net.imglib2.ops.function.real.RealMinFunction;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import imagej.command.Command;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.measure.MeasurementService;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

@Plugin(menuPath = "Plugins>Sandbox>Measure Tester")
public class MeasureTestPlugin implements Command {

	@Parameter
	private DatasetService dsSrv;
	
	@Parameter
	private MeasurementService mSrv;
	
	@Override
	public void run() {
		testMe();
	}

	private void testMe() {
		Dataset ds = dsSrv.create(
			new long[]{5,5}, "junk", new AxisType[]{Axes.X, Axes.Y}, 8, false, false);
		Cursor<? extends RealType<?>> cursor = ds.getImgPlus().cursor();
		int i = 0;
		while (cursor.hasNext()) {
			cursor.next().setReal(i++);
		}
		PointSet pts = new HyperVolumePointSet(ds.getDims());
		DoubleType output = new DoubleType();
		mSrv.measure(ds, pts, RealMedianFunction.class, output);
		System.out.println("Median is " + output.getRealDouble());
		mSrv.measure(ds, pts, RealMinFunction.class, output);
		System.out.println("Min is " + output.getRealDouble());
		mSrv.measure(ds, pts, RealMaxFunction.class, output);
		System.out.println("Max is " + output.getRealDouble());
		// NOTE: this one should fail to construct wince it has no 1-arg constructor
		// which points out a limitation with the whole service approach.
		mSrv.measure(ds, pts, RealAdaptiveMedianFunction.class, output);
		System.out.println("AdaptiveMedian is " + output.getRealDouble());
	}
}
