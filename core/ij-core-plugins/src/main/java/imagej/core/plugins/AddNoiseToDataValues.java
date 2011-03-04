package imagej.core.plugins;

import imagej.model.Dataset;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.AddNoise;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;

/**
 * Fills an output Dataset by applying random noise to an input Dataset. This class is used by
 * AddDefaultNoiseToDataValues and AddSpecificNoiseToDataValues. They each manipulate setStdDev().
 * This class can be used to implement simple (1 pixel neighborhood) gaussian noise addition
 * without requiring a plugin.
 *
 * @author Barry DeZonia
 */
public class AddNoiseToDataValues
{
	// ***************  instance variables ***************************************************************

	/** the input Dataset that contains original values */
	private Dataset input;
	
	/** the output Dataset that will contain perturbed values */
	private Dataset output;
	
	/** the stand deviation of the gaussian random value used to create perturbed values */
	private double rangeStdDev;
	
	/** maximum allowable values - varies by underlying data type. For instance (0,255) for 8 bit and (0,65535) for 16 bit.
	 * used to make sure that perturned values do not leave the allowable range for the underlying data type. 
	 */
	private double rangeMin, rangeMax;
	
	// ***************  constructor ***************************************************************

	/** constructor - takes an input Dataset as the baseline data to compute perturbed values from. */
	public AddNoiseToDataValues(Dataset input)
	{
		this.input = input;
	}

	// ***************  public interface ***************************************************************

	/** use this method to specify the output Dataset that will hold output data. if this method is not called
	 * then the add noise operation defaults to creating a new output Dataset and returning it from the run() method. */
	public void setOutput(Dataset output)
	{
		this.output = output;
	}

	/** specify the standard deviation of the gaussian range desired. affects the distance of perturbation of each data value. */
	protected void setStdDev(double stdDev)
	{
		this.rangeStdDev = stdDev;
	}

	/** runs the operation and returns the Dataset that contains the output data */
	public Dataset run()
	{
		if (input == null)  // TODO - temporary code to test these until IJ2 plugins can correctly fill a List<Dataset> @Parameter
		{
			Image<UnsignedShortType> junkImage = Dataset.createPlanarImage("", new UnsignedShortType(), new int[]{200,200});
			Cursor<UnsignedShortType> cursor = junkImage.createCursor();
			int index = 0;
			for (UnsignedShortType pixRef : cursor)
				pixRef.set(index++);
			cursor.close();
			input = new Dataset(junkImage);
		}
		
		calcRangeMinAndMax();
		
		UnaryOperator op = new AddNoise(rangeMin, rangeMax, rangeStdDev);
		
		UnaryOperatorFunction opFunc = new UnaryOperatorFunction(op);
		
		NAryOperation operation = new NAryOperation(input, opFunc);

		operation.setOutput(output);
		
		return operation.run();
	}

	// ***************  private interface ***************************************************************

	/** calculates the min and max allowable data range for the image : depends upon its underlying data type */
	private void calcRangeMinAndMax()
	{
		Cursor<? extends RealType<?>> cursor = (Cursor<? extends RealType<?>>) input.getImage().createCursor();
		rangeMin = cursor.getType().getMinValue();
		rangeMax = cursor.getType().getMaxValue();
		cursor.close();
	}
	
}
