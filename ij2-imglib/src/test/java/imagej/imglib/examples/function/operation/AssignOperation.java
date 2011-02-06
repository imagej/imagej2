package imagej.imglib.examples.function.operation;

import imagej.imglib.examples.function.condition.Condition;
import imagej.imglib.examples.function.function.RealFunction;
import imagej.imglib.examples.function.observer.Observer;
import mpicbg.imglib.cursor.special.RegionOfInterestCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

@SuppressWarnings("unchecked")
public class AssignOperation<T extends RealType<T>>
{
	private int imageCount;
	private MultiImageIterator<T> cursor;
	private T outputVariable;
	private int[][] positions;
	private Observer observer;
	private Condition[] conditions;
	private RealFunction<T> function;
	private boolean interrupted;

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
		function = func;
		interrupted = false;
		
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
	
	public void setOutputCondition(Condition c)
	{
		conditions[0] = c;
	}
	
	public void setInputRegion(int i, int[] origin, int[] span)
	{
		cursor.setRegion(i+1, origin, span);
	}
	
	public void setInputCondition(int i, Condition c)
	{
		conditions[i+1] = c;
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
			if (interrupted)
				break;
			
			cursor.next();
			
			double value = Double.NaN;
			
			boolean condSatisfied = conditionsSatisfied(subCursors); 

			if (condSatisfied)
			{
				value = function.compute(inputVariables);
				
				outputVariable.setReal(value);
			}
			
			if (observer != null)
			{
				subCursors[0].getPosition(position);
				
				observer.update(position,value,condSatisfied);
			}
		}

		if (observer != null)
			observer.done(interrupted);
	}

	public void quit()
	{
		interrupted = true;
	}

	// -----------------  private interface ------------------------------------------
	
	private boolean conditionsSatisfied(RegionOfInterestCursor<T>[] cursors)
	{
		for (int i = 0; i < conditions.length; i++)
		{
			RegionOfInterestCursor<? extends RealType<?>> subcursor = cursors[i];
			
			subcursor.getPosition(positions[i]);
			
			Condition c = conditions[i];
			
			if ((c != null) && (!c.isSatisfied(positions[i], subcursor.getType().getRealDouble())))
				return false;
		}
		return true;
	}

	private T[] getInputVariables(RegionOfInterestCursor<T>[] cursors)
	{
		T[] variables = outputVariable.createArray1D(imageCount-1);
		
		for (int i = 0; i < variables.length; i++)
			variables[i] = cursors[i+1].getType();
		
		return variables;
	}

}
