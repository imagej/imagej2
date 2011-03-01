package imagej.core.plugins;

import java.util.ArrayList;

import imagej.model.Dataset;
import imagej.plugin.Plugin;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.Invert;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "Edit>Invert"
)
public class InvertDataValues extends NAryOperation
{
	private double min, max;
	
	public InvertDataValues()
	{
		min = Double.MAX_VALUE;
		max = -Double.MAX_VALUE;
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
		calcMinAndMax();
		UnaryOperator op = new Invert(min, max);
		UnaryOperatorFunction opFunc = new UnaryOperatorFunction(op);
		setFunction(opFunc);
		super.run();
	}
	
	private void calcMinAndMax()
	{
		min = Double.MAX_VALUE;
		max = -Double.MAX_VALUE;
		
		Cursor<? extends RealType<?>> cursor = (Cursor<? extends RealType<?>>) (in.get(0).getImage().createCursor());
		
		while (cursor.hasNext())
		{
			double value = cursor.next().getRealDouble();
			
			if (value < min)
				min = value;
			if (value > max)
				max = value;
		}
		
		cursor.close();
	}
	
}
