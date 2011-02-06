package imagej.imglib.examples.function.operation;

import imagej.imglib.examples.function.condition.Condition;
import imagej.imglib.examples.function.observer.Observer;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.cursor.special.RegionOfInterestCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

// This could really be represented as an AssignOperation from one image to itself with a Copy function.
// Would do unnecessary assignments though. Could however query multiple images at the same time this way 

public class QueryOperation<T extends RealType<T>>
{
	private Image<T> image;
	private int[] origin;
	private int[] span;
	private Condition condition;
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
	
	public void setCondition(Condition c)
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
			
			boolean condSatisfied = (condition == null) || (condition.isSatisfied(position, value)); 

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

