package imagej.core.plugins;

import java.util.ArrayList;
import imagej.model.Dataset;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.AddNoise;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
public class AddNoiseToDataValues extends NAryOperation
{
	private double rangeStdDev;
	
	private double rangeMin, rangeMax;
	
	public AddNoiseToDataValues()
	{
	}

	protected void setStdDev(double stdDev)
	{
		this.rangeStdDev = stdDev;
	}
	
	@Override
	public void run()
	{
		if (in == null)  // TODO - temporary code to test these until IJ2 plugins can correctly fill a List<Dataset> @Parameter
		{
			Image<UnsignedShortType> junkImage = Dataset.createPlanarImage("", new UnsignedShortType(), new int[]{200,200});
			Cursor<UnsignedShortType> cursor = junkImage.createCursor();
			int index = 0;
			for (UnsignedShortType pixRef : cursor)
				pixRef.set(index++);
			cursor.close();
			in = new ArrayList<Dataset>();
			in.add(new Dataset(junkImage));
		}
		
		calcRangeMinAndMax();
		UnaryOperator op = new AddNoise(rangeMin, rangeMax, rangeStdDev);
		UnaryOperatorFunction opFunc = new UnaryOperatorFunction(op);
		setFunction(opFunc);
		super.run();
	}

	private void calcRangeMinAndMax()
	{
		Cursor<? extends RealType<?>> cursor = (Cursor<? extends RealType<?>>) in.get(0).getImage().createCursor();
		rangeMin = cursor.getType().getMinValue();
		rangeMax = cursor.getType().getMaxValue();
		cursor.close();
	}
	
}
