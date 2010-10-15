package imagej.process.operation;

import imagej.process.query.InfoCollector;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class QueryOperation<T extends RealType<T>> extends PositionalSingleCursorRoiOperation<T>
{
	private InfoCollector gatherer;
	
	public QueryOperation(Image<T> image, int[] origin, int[] span, InfoCollector gatherer)
	{
		super(image, origin, span);
		this.gatherer = gatherer;
	}

	@Override
	public void beforeIteration(RealType<T> type)
	{
		this.gatherer.init();
	}

	@Override
	public void insideIteration(int[] position, RealType<T> sample)
	{
		this.gatherer.collectInfo(position, sample.getRealDouble());
	}

	@Override
	public void afterIteration()
	{
		this.gatherer.done();
	}

}
