package imagej.imglib.process.operation;

import imagej.data.DataAccessor;
import imagej.data.Type;
import imagej.process.Index;
import imagej.process.Span;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;


public class GetPlaneOperation<T extends RealType<T>> extends PositionalSingleCursorRoiOperation<T>
{
	// *********** instance variables ******************************************************
	
	private int originX, originY, spanX, spanY;
	private Type asType;
	private Object outputPlane;
	private DataAccessor planeWriter;

	// ************ public interface - private declarations later **************************
	
	public GetPlaneOperation(Image<T> image, int[] origin, int[] span, Type asType)
	{
		super(image, origin, span);
		this.originX = origin[0];
		this.originY = origin[1];
		this.spanX = span[0];
		this.spanY = span[1];
		this.asType = asType;
	}

	@Override
	protected void beforeIteration(RealType<T> type)
	{
		int planeSize = this.spanX * this.spanY;
		
		this.outputPlane = this.asType.allocateStorageArray(planeSize);

		this.planeWriter = this.asType.allocateArrayAccessor(this.outputPlane);
	}

	@Override
	protected void insideIteration(int[] position, RealType<T> sample)
	{
		int index = (position[1] - this.originY) * this.spanX + (position[0] - this.originX); 
		
		this.planeWriter.setReal(index, sample.getRealDouble());
	}

	@Override
	protected void afterIteration()
	{
	}
	
	public Object getOutputPlane()
	{
		return this.outputPlane;
	}

	public static <T extends RealType<T>> Object getPlaneAs(Image<T> img, int[] planePos, Type asType)
	{
		int[] origin = Index.create(0,0,planePos);
		
		int[] span = Span.singlePlane(img.getDimension(0), img.getDimension(1), img.getNumDimensions());
		
		GetPlaneOperation<T> operation = new GetPlaneOperation<T>(img, origin, span, asType);
	
		operation.execute();
		
		return operation.getOutputPlane();
	}
}
