package imagej.imglib.examples.function.operation;

import imagej.imglib.examples.function.condition.Condition;
import imagej.imglib.examples.function.observer.Observer;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.cursor.special.RegionOfInterestCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

/** QueryOperation
 * 
 * A simple query facility for gathering information about one Image.
 * 
 * This functionality can be represented more powerfully using an AssignOperation with a NullFunction. Thats a more powerful query mechanism
 * that allows multiple input images and constraining conditions.
 * 
 */

public class QueryOperation<T extends RealType<T>>
{
	// -----------------  instance variables ------------------------------------------

	private Image<T> image;
	private int[] origin;
	private int[] span;
	private Condition<T> condition;
	private Observer observer;
	private RegionOfInterestCursor<T> cursor;
	private boolean interrupted;
	
	// -----------------  public interface ------------------------------------------

	public QueryOperation(Image<T> image, Observer o)  // TODO - query multiple images at the same time????
	{
		this.image = image;
		this.observer = o;
		this.origin = new int[image.getNumDimensions()];
		this.span = image.getDimensions();
		this.condition = null;
		this.cursor = null;
		this.interrupted = false;
	}
	
	public void setRegion(int[] origin, int[] span)
	{
		this.origin = origin;
		this.span = span;
	}
	
	public void setCondition(Condition<T> c)
	{
		condition = c;
	}

	public void execute()
	{
		initCursor();  // done at this point as region can be specified by user after construction

		T variable = cursor.getType();
		
		int[] position = cursor.createPositionArray();
		
		if (observer != null)
			observer.init();

		while (cursor.hasNext())
		{
			if (this.interrupted)
				break;
			
			cursor.fwd();
			
			double value = variable.getRealDouble();
			
			cursor.getPosition(position);
			
			boolean condSatisfied = (condition == null) || (condition.isSatisfied(cursor, position)); 

			observer.update(position,value,condSatisfied);
		}

		if (observer != null)
			observer.done(interrupted);
	}
	
	public void quit()
	{
		interrupted = true;
	}

	// -----------------  private interface ------------------------------------------

	private void initCursor()
	{
		LocalizableByDimCursor<T> dimCursor = image.createLocalizableByDimCursor();
		cursor = new RegionOfInterestCursor<T>(dimCursor, origin, span);
	}
}

