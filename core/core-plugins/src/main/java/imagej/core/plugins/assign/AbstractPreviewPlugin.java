package imagej.core.plugins.assign;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.Position;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.PreviewPlugin;
import net.imglib2.RandomAccess;
import net.imglib2.img.Axes;
import net.imglib2.ops.operation.RegionIterator;
import net.imglib2.ops.operator.UnaryOperator;
import net.imglib2.type.numeric.RealType;


public abstract class AbstractPreviewPlugin implements ImageJPlugin, PreviewPlugin {

	// -- instance variables that are Parameters --

	private Dataset dataset;
	
	private double[] dataBackup;

	private long[] planeOrigin;
	
	private long[] planeSpan;

	private RegionIterator iter;
	
	@Override
	public void run() {
		if (previewOn())
			restoreViewedPlane();
		transformDataset();
	}

	@Override
	public void preview() {
		if (dataBackup == null)
			saveViewedPlane();
		else
			restoreViewedPlane();
		if (previewOn())
			transformViewedPlane();
	}

	@Override
	public void cancel() {
		if (previewOn())
			restoreViewedPlane();
	}

	public abstract Display getDisplay();
	public abstract boolean previewOn();
	public abstract UnaryOperator getOperator();
	
	// -- private helpers --
	
	// NB
	// - It is possible to design an elegant plane cloner but can only assume
	// certain types. And since dataset.setPlane() does not copy data if not
	// PlanarAccess backed it is not helpful to have exact data.
	// - we could make Dataset.copyOfPlane() visible but it assumes things
	// about all types being native types. And again can't set data.
	// - so we opt for copying to an array of doubles. However this could
	// cause precision loss for long data
	
	private void saveViewedPlane() {

		// get Dataset we'll be transforming
		dataset = ImageJ.get(DisplayService.class).getActiveDataset(getDisplay());
		
		// check dimensions of Dataset
		int xIndex = dataset.getAxisIndex(Axes.X);
		int yIndex = dataset.getAxisIndex(Axes.Y);
		if ((xIndex != 0) || (yIndex != 1))
			throw new IllegalArgumentException(
				"display is not ordered with X axis 1st and Y axis 2nd.");
		long[] dims = dataset.getDims();
		long w = dims[0];
		long h = dims[1];
		if (w*h > Integer.MAX_VALUE)
			throw new IllegalArgumentException("plane too large to copy into memory");
		
		// calc origin
		Position planePos = getDisplay().getActiveView().getPlanePosition();
		planeOrigin = new long[dims.length];
		planeOrigin[0] = 0;
		planeOrigin[1] = 0;
		for (int i = 2; i < planeOrigin.length; i++)
			planeOrigin[i] = planePos.getLongPosition(i-2);
		
		// calc span
		planeSpan = new long[dims.length];
		planeSpan[0] = w;
		planeSpan[1] = h;
		for (int i = 2; i < planeSpan.length; i++)
			planeSpan[i] = 1;
		
		// setup region iterator
		RandomAccess<? extends RealType<?>> accessor =
			dataset.getImgPlus().randomAccess();
		iter = new RegionIterator(accessor, planeOrigin, planeSpan);

		// copy data to a double[]
		dataBackup = new double[(int)(w*h)];
		int index = 0;
		while (iter.hasNext()) {
			iter.next();
			dataBackup[index++] = iter.getValue();
		}
	}
	
	private void restoreViewedPlane() {
		
		// restore data from our double[]
		int index = 0;
		iter.reset();
		while (iter.hasNext()) {
			iter.next();
			iter.setValue(dataBackup[index++]);
		}
		dataset.update();
	}
	
	// TODO - transforms all data. Should only transform current selection planes

	private void transformDataset() {
		UnaryOperator op = getOperator();
		InplaceUnaryTransform transform = new InplaceUnaryTransform(dataset, op);
		transform.run();
	}

	// TODO - transforms full plane. Make it only transform current selection.
	
	private void transformViewedPlane() {
		UnaryOperator op = getOperator();
		InplaceUnaryTransform transform = new InplaceUnaryTransform(dataset, op);
		transform.setRegion(planeOrigin, planeSpan);
		transform.run();
	}

}
