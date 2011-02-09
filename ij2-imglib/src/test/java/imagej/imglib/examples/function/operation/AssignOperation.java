package imagej.imglib.examples.function.operation;

import imagej.imglib.examples.function.condition.Condition;
import imagej.imglib.examples.function.function.RealFunction;
import imagej.imglib.examples.function.observer.Observer;
import mpicbg.imglib.cursor.LocalizableCursor;
import mpicbg.imglib.cursor.special.RegionOfInterestCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

/**
 * An AssignOperation computes values in an output image. The output image is preallocated here. The AssignOperation uses a RealFunction
 *  to compute each pixel value from any number of input images. The passed in function must accept the same number of parameters as the
 *  number of input images given. (Better name welcomed: ImageAssignment? FunctionalEvaluation? Others?)
 * 
 * A user of this class creates an AssignOperation, constrains it as desired, and calls execute(). Note that execute() can be
 * interrupted from another Thread using the quit() method. (this code is currently single threaded).
 * 
 * The operation can be constrained in a number of ways:
 * 
 * - the output image and each input image can be constrained to sample values only within user specified rectangular subregions. All subregions
 * must be shape compatible before calling execute(). See setInputRegion() and setOutputRegion().
 * 
 * - the generation of an output pixel can be further constrained by Conditions - up to one per image. An output pixel is changed at a pixel location
 * only when all Conditions are satisfied. Conditions can be as complex as desired and can be composed of other Conditions. Conditions can even have
 * a spatial component. See setInputCondition() and setOutputCondition().
 * 
 * - the execute() iteration can be observed by any class implementing an Observer interface. A statistical query could be constructed this way.
 * 
 * An image can be repeated multiple times within the input image list. Therefore separate regions of one image can be used as input.
 * 
 * An image can act as both input and output. Thus one can transform an existing image in place.
 * 
 * Limitations
 * 
 *  - the regions are shape compatible. Thus one pixel is computed from some function of all the pixels located in the same place relative
 * to their own images. Thus you can't compute a function on a neighborhood very easily. As it stands you could create an AssignOperation on 9 input
 * images that are the same input image with 9 different subregions. You could then pass a MedianFunction to calculate a median filter. Could be better.
 * (With appropriate out of bounds behavior this is how you could do a convolution too).
 * 
 * - if a user reuses an input image as an output image they have to take care to not change pixels used in further input calculations. Usually this is
 * not a problem unless you are using a spatial Condition or if you have set the output image subregion to partially overlap it's input image region.
 * We could add some checking.
 * 
 * Ideally we'd enhance Imglib such that functions are more integrated in the core. An Image is just a function that maps input coordinates to
 * output values. An image with an out of bounds behavior is a function composed of two functions: the image function and the out of bounds function.
 * A neighborhood is a function that maps input coordinates to output values over a subdomain (thus it should be an Img also). A RealFunction could
 * operate on neighborhoods and return values. Neighborhoods could be within one Image or span multiple Images. But really Image is wrong here
 * as it could just be a function we sample. If a neighborhood can't span multiple images then we'd want ways to compose images out of other images
 * that is all done by reference and delegation. i.e. a 3d composed z stack image made of thirteen 2d images. See how imgib2 supports these concepts.
 */

@SuppressWarnings("unchecked")
public class AssignOperation<T extends RealType<T>>
{
	// -----------------  instance variables ------------------------------------------

	private int imageCount;
	private MultiImageIterator<T> cursor;
	private T outputVariable;
	private int[][] positions;
	private Observer observer;
	private Condition[] conditions;
	private boolean requireIntersection;
	private RealFunction<T> function;
	private boolean wasInterrupted;

	// -----------------  public interface ------------------------------------------
	
	public AssignOperation(Image<T>[] inputs, Image<T> output, RealFunction<T> func)
	{
		imageCount = inputs.length+1;

		Image<T>[] images = new Image[imageCount];
		images[0] = output;
		for (int i = 1; i <= inputs.length; i++)
			images[i] = inputs[i-1];
		
		cursor = new MultiImageIterator<T>(images);
		
		positions = new int[imageCount][];
		positions[0] = new int[output.getNumDimensions()];
		for (int i = 1; i < imageCount; i++)
		{
			positions[i] = new int[inputs[i-1].getNumDimensions()];
		}
		outputVariable = null;
		observer = null;
		conditions = new Condition[imageCount];
		requireIntersection = true;
		function = func;
		wasInterrupted = false;
		
		if ( ! function.canAccept(inputs.length) )
			throw new IllegalArgumentException("function cannot handle "+inputs.length+" input images");
	}

	public void setObserver(Observer o)
	{
		observer = o;
	}
	
	public void setOutputRegion(int[] origin, int[] span)
	{
		cursor.setRegion(0, origin, span);
	}
	
	public void setOutputCondition(Condition<T> c)
	{
		conditions[0] = c;
	}
	
	public void setInputRegion(int i, int[] origin, int[] span)
	{
		cursor.setRegion(i+1, origin, span);
	}
	
	public void setInputCondition(int i, Condition<T> c)
	{
		conditions[i+1] = c;
	}

	public void intersectConditions()
	{
		requireIntersection = true;
	}
	
	public void unionConditions()
	{
		requireIntersection = false;
	}

	public void execute()
	{
		cursor.initialize();

		RegionOfInterestCursor<T>[] subCursors = cursor.getSubcursors();

		outputVariable = subCursors[0].getType();

		T[] inputVariables = getInputVariables(subCursors);
		
		int[] position = subCursors[0].createPositionArray();
		
		if (observer != null)
			observer.init();

		while (cursor.hasNext())
		{
			if (wasInterrupted)
				break;
			
			cursor.fwd();
			
			double value = Double.NaN;

			boolean conditionsSatisfied = conditionsSatisfied(subCursors); 

			if (conditionsSatisfied)
			{
				function.compute(inputVariables, outputVariable);
				
				value = outputVariable.getRealDouble();
			}
			
			if (observer != null)
			{
				subCursors[0].getPosition(position);
				
				observer.update(position,value,conditionsSatisfied);
			}
		}

		if (observer != null)
			observer.done(wasInterrupted);
	}

	public void quit()
	{
		wasInterrupted = true;
	}

	// -----------------  private interface ------------------------------------------
	
	private boolean conditionsSatisfied(LocalizableCursor<T>[] cursors)
	{
		for (int i = 0; i < conditions.length; i++)
		{
			Condition<T> condition = conditions[i];
			
			if (condition == null)
				continue;
			
			LocalizableCursor<T> subcursor = cursors[i];
			
			subcursor.getPosition(positions[i]);
			
			if (condition.isSatisfied(subcursor, positions[i]))
			{
				if (!requireIntersection)  // if union case we can short circuit with success
					return true;
			}
			else // condition not satisfied
			{
				if (requireIntersection)  // in intersection case we can short circuit with failure
					return false;
			}
		}
		if (requireIntersection) // intersection - if here everything passed
			return true;
		else  // union - if here nothing satisfied the condition
			return false;
	}

	private T[] getInputVariables(RegionOfInterestCursor<T>[] cursors)
	{
		T[] variables = outputVariable.createArray1D(imageCount-1);
		
		for (int i = 0; i < variables.length; i++)
			variables[i] = cursors[i+1].getType();
		
		return variables;
	}

}
