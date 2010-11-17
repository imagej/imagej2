package imagej2.imglib.process.operation;

import imagej2.process.query.InfoCollector;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

/** A QueryOperation is a read only operation that allows classes that implement the InfoCollector
 * interface to gather information about a region of an Image. Can be quite powerful when used in conjunction
 * with a SelectionFunction via queryOperation.setSelectionFunction(selectionFunc).
 */
public class QueryOperation<T extends RealType<T>> extends PositionalSingleCursorRoiOperation<T>
{
	/** the InfoCollector that defines the query */
	private InfoCollector infoCollector;
	
	/** construct a QueryOperation from an image, a region definition within it, and an InfoCollector.
	 * It is the InfoCollector's job to record observations about the data.
	 */  
	public QueryOperation(Image<T> image, int[] origin, int[] span, InfoCollector infoCollector)
	{
		super(image, origin, span);
		this.infoCollector = infoCollector;
	}

	@Override
	/** lets the InfoCollector know we're about to start the query */
	protected void beforeIteration(RealType<T> type)
	{
		this.infoCollector.init();
	}

	@Override
	/** gives the InfoCollector information about the current sample */
	protected void insideIteration(int[] position, RealType<T> sample)
	{
		this.infoCollector.collectInfo(position, sample.getRealDouble());
	}

	@Override
	/** lets the InfoCollector know we're done with the query */
	protected void afterIteration()
	{
		this.infoCollector.done();
	}

}
